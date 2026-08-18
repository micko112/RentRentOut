import os
from typing import TypedDict, List, Annotated, AsyncIterator

import aiosqlite
from langchain_community.document_loaders import TextLoader
from langchain_text_splitters import RecursiveCharacterTextSplitter
from langchain_openai import OpenAIEmbeddings, ChatOpenAI
from langchain_chroma import Chroma
from langgraph.graph import StateGraph, START, END
from langgraph.checkpoint.sqlite.aio import AsyncSqliteSaver

from chatbot_logger import log_event

_BASE_DIR = os.path.dirname(os.path.abspath(__file__))
_CHROMA_DIR = os.path.join(_BASE_DIR, "chroma_db")
_CHECKPOINT_DIR = os.path.join(_BASE_DIR, "data")
_CHECKPOINT_PATH = os.path.join(_CHECKPOINT_DIR, "chatbot.sqlite")

# Cap chat history at last 20 lines (~10 exchanges) to bound prompt token cost.
_HISTORY_CAP = 20

llm = ChatOpenAI(model="gpt-4o-mini", temperature=0.6)


def _merge_history(old: List[str], new: List[str]) -> List[str]:
    return (old + new)[-_HISTORY_CAP:]


def _build_vector_store():
    embeddings = OpenAIEmbeddings(model="text-embedding-3-small")
    if os.path.exists(_CHROMA_DIR) and os.listdir(_CHROMA_DIR):
        return Chroma(persist_directory=_CHROMA_DIR, embedding_function=embeddings)

    loader = TextLoader(os.path.join(_BASE_DIR, "baza_znanja.txt"), encoding="utf-8")
    docs = loader.load()
    splitter = RecursiveCharacterTextSplitter(chunk_size=400, chunk_overlap=50)
    chunks = splitter.split_documents(docs)
    return Chroma.from_documents(documents=chunks, embedding=embeddings, persist_directory=_CHROMA_DIR)


_vector_store = _build_vector_store()
retriever = _vector_store.as_retriever(search_kwargs={"k": 3})


class AgentState(TypedDict):
    question: str
    user_context: str
    thread_id: str
    documents: List[str]
    chat_history: Annotated[List[str], _merge_history]
    is_relevant: str
    answer: str


def _tid(state) -> str:
    return state.get("thread_id") or "unknown"


async def router(state):
    question = state["question"]
    history = "\n".join(state.get("chat_history", [])[-6:])
    prompt = f"""Klasifikuj pitanje korisnika u jednu od dve kategorije:

- "retrieve": pitanje traži konkretnu informaciju o platformi izdajemiznajmljujem.com (oglasi, pravila, plaćanje, krediti, ugovori, promocije, registracija, kako nešto funkcioniše)
- "chat": pozdravi ("ćao", "zdravo", "jel si tu"), zahvale ("hvala", "ok"), ćaskanje, nejasna kratka pitanja, teme van platforme

Uzmi u obzir i prethodni razgovor (korisnik se možda nadovezuje).
Prethodni razgovor: {history}
Pitanje: {question}

Odgovori ISKLJUČIVO jednom rečju: retrieve ili chat."""
    decision = (await llm.ainvoke(prompt)).content.strip().lower()
    route = "retrieve_node" if "retrieve" in decision else "chat_node"
    log_event(_tid(state), "router", {"question": question, "decision": decision, "route": route})
    return route


async def retrieve_node(state):
    question = state["question"]
    history = "\n".join(state.get("chat_history", [])[-6:])

    expansion_prompt = f"""Korisnik je postavio pitanje: '{question}'.
Prethodni razgovor: {history}
Preformuliši pitanje na 3 različita načina (sinonimi, profesionalniji rečnik) da poboljšamo pretragu baze znanja.
Vrati ISKLJUČIVO ta 3 pitanja, svako u novom redu, bez dodatnog teksta ili rednih brojeva."""

    expanded_text = (await llm.ainvoke(expansion_prompt)).content.strip()
    queries = [q.strip() for q in expanded_text.split("\n") if q.strip()]
    queries.append(question)

    all_docs: List[str] = []
    seen = set()
    for q in queries:
        for d in await retriever.ainvoke(q):
            if d.page_content not in seen:
                seen.add(d.page_content)
                all_docs.append(d.page_content)

    docs = all_docs[:6]
    log_event(_tid(state), "retrieve", {
        "question": question,
        "expanded_queries": queries,
        "doc_count": len(docs),
    })
    return {"documents": docs}


async def grade_node(state):
    question = state["question"]
    context = "\n".join(state["documents"])
    prompt = f"""Da li TEKST sadrži informacije koje mogu pomoći u odgovoru na PITANJE — čak i delimično, indirektno, ili povezane informacije koje bi korisnik mogao smatrati korisnim?
Budi blag u oceni — ako ima i najmanja relevantna informacija, odgovori yes.
Odgovori NO samo ako je TEKST potpuno nepovezan sa pitanjem.

TEKST: {context}
PITANJE: {question}

Odgovori ISKLJUČIVO sa: yes ili no."""
    decision = (await llm.ainvoke(prompt)).content.strip().lower()
    verdict = "yes" if "yes" in decision else "no"
    log_event(_tid(state), "grade", {"question": question, "verdict": verdict})
    return {"is_relevant": verdict}


def check_relevance(state):
    return "generate_node" if state["is_relevant"] == "yes" else "escalate_node"


def _build_answer_prompt(state, *, escalate: bool) -> str:
    question = state["question"]
    context = "\n\n".join(state.get("documents", []))
    user_context = state.get("user_context", "")
    history = "\n".join(state.get("chat_history", [])[-10:])

    user_ctx_block = f"\nKontekst korisnika:\n{user_context}" if user_context else ""
    history_block = f"\nPrethodni razgovor:\n{history}" if history else ""

    if not escalate:
        return f"""Ti si "bot Igor", asistent na platformi izdajemiznajmljujem.com — tržištu za iznajmljivanje svih vrsta predmeta i nekretnina: alata, kamera, opreme, vozila, stanova, kuća i svega ostalog.
Odgovaraj na srpskom jeziku, latinicom. Budi kratak, jasan i prijateljski. Ne koristi emojije.{user_ctx_block}{history_block}

Na osnovu KONTEKSTA odgovori na PITANJE korisnika:
KONTEKST:
{context}

PITANJE: {question}
ODGOVOR:"""

    context_block = f"\nDelimično relevantan kontekst iz baze:\n{context}" if context else ""
    return f"""Ti si "bot Igor", asistent na platformi izdajemiznajmljujem.com — tržištu za iznajmljivanje svih vrsta predmeta i nekretnina: alata, kamera, opreme, vozila, stanova, kuća i svega ostalog.
Odgovaraj na srpskom jeziku, latinicom. Budi kratak, jasan i prijateljski. Ne koristi emojije.

U bazi znanja nisi našao direktan odgovor na pitanje korisnika. Uradi sledeće:
1) Ako je pitanje vezano za platformu ili iznajmljivanje (bilo koje vrste predmeta ili nekretnine), pokušaj da pomogneš na osnovu opšteg znanja, ali jasno naglasi da nisi siguran u detalje.
2) Ako je pitanje potpuno van teme (politika, recepti, itd.), ljubazno odbij i vrati korisnika na temu platforme.
3) Za specifična pitanja gde ti treba potvrda, predloži kontakt na izdajemiznajmljujem.rs@gmail.com.{user_ctx_block}{history_block}{context_block}

PITANJE: {question}
ODGOVOR:"""


def _build_chat_prompt(state) -> str:
    question = state["question"]
    history = "\n".join(state.get("chat_history", [])[-10:])
    history_block = f"\nPrethodni razgovor:\n{history}" if history else ""
    return f"""Ti si "bot Igor", asistent na platformi izdajemiznajmljujem.com — tržištu za iznajmljivanje svih vrsta predmeta i nekretnina (alati, kamere, oprema, vozila, stanovi, kuće, i sl.).
Odgovaraj na srpskom jeziku, latinicom. Budi kratak i prijateljski. Ne koristi emojije.
Baviš se isključivo pitanjima o platformi i temama vezanim za iznajmljivanje svih vrsta predmeta ili nekretnina.
Ako pitanje nije vezano za tu temu, ljubazno odbij i vrati korisnika na temu platforme.{history_block}

Korisnik: {question}
Bot:"""


async def generate_node(state):
    prompt = _build_answer_prompt(state, escalate=False)
    answer = (await llm.ainvoke(prompt)).content
    log_event(_tid(state), "generate", {"question": state["question"], "answer_len": len(answer)})
    return {
        "answer": answer,
        "chat_history": [f"Korisnik: {state['question']}", f"Bot: {answer}"],
    }


async def escalate_node(state):
    prompt = _build_answer_prompt(state, escalate=True)
    answer = (await llm.ainvoke(prompt)).content
    log_event(_tid(state), "escalate", {"question": state["question"], "answer_len": len(answer)})
    return {
        "answer": answer,
        "chat_history": [f"Korisnik: {state['question']}", f"Bot: {answer}"],
    }


async def chat_node(state):
    prompt = _build_chat_prompt(state)
    answer = (await llm.ainvoke(prompt)).content
    log_event(_tid(state), "chat", {"question": state["question"], "answer_len": len(answer)})
    return {
        "answer": answer,
        "chat_history": [f"Korisnik: {state['question']}", f"Bot: {answer}"],
    }


workflow = StateGraph(AgentState)
workflow.add_node("retrieve_node", retrieve_node)
workflow.add_node("grade_node", grade_node)
workflow.add_node("generate_node", generate_node)
workflow.add_node("escalate_node", escalate_node)
workflow.add_node("chat_node", chat_node)

workflow.add_conditional_edges(
    START, router, {"retrieve_node": "retrieve_node", "chat_node": "chat_node"}
)
workflow.add_edge("retrieve_node", "grade_node")
workflow.add_conditional_edges(
    "grade_node",
    check_relevance,
    {"generate_node": "generate_node", "escalate_node": "escalate_node"},
)
workflow.add_edge("generate_node", END)
workflow.add_edge("escalate_node", END)
workflow.add_edge("chat_node", END)

os.makedirs(_CHECKPOINT_DIR, exist_ok=True)
_checkpoint_conn = aiosqlite.connect(_CHECKPOINT_PATH, check_same_thread=False)
checkpointer = AsyncSqliteSaver(_checkpoint_conn)

agent = workflow.compile(checkpointer=checkpointer)
print("LangGraph agent compiled (async, AsyncSQLite checkpointer, logger wired).")


# ─── Streaming helpers ───────────────────────────────────────────────────────
# For SSE we replicate the router→(retrieve→grade→(generate|escalate) | chat)
# flow but stream tokens from the final answering LLM call.

async def _classify_route(question: str, history_lines: List[str]) -> str:
    history = "\n".join(history_lines[-6:])
    prompt = f"""Klasifikuj pitanje korisnika u jednu od dve kategorije:
- retrieve
- chat
Prethodni razgovor: {history}
Pitanje: {question}
Odgovori ISKLJUČIVO jednom rečju."""
    decision = (await llm.ainvoke(prompt)).content.strip().lower()
    return "retrieve" if "retrieve" in decision else "chat"


async def stream_answer(question: str, thread_id: str, user_context: str) -> AsyncIterator[str]:
    """Yield answer token-by-token. State history is loaded/saved via checkpointer."""
    config = {"configurable": {"thread_id": thread_id}}

    snapshot = await agent.aget_state(config)
    prior = snapshot.values if snapshot and snapshot.values else {}
    history_lines: List[str] = list(prior.get("chat_history", []))

    route = await _classify_route(question, history_lines)
    log_event(thread_id, "router_stream", {"question": question, "route": route})

    if route == "retrieve":
        docs_state: List[str] = []
        try:
            for d in await retriever.ainvoke(question):
                docs_state.append(d.page_content)
        except Exception as e:
            log_event(thread_id, "retrieve_stream_error", {"error": str(e)})
        state = {
            "question": question,
            "user_context": user_context,
            "documents": docs_state[:6],
            "chat_history": history_lines,
        }
        prompt = _build_answer_prompt(state, escalate=(len(docs_state) == 0))
        event = "generate_stream" if docs_state else "escalate_stream"
    else:
        state = {
            "question": question,
            "user_context": user_context,
            "chat_history": history_lines,
        }
        prompt = _build_chat_prompt(state)
        event = "chat_stream"

    collected: List[str] = []
    async for chunk in llm.astream(prompt):
        piece = getattr(chunk, "content", "") or ""
        if piece:
            collected.append(piece)
            yield piece

    full_answer = "".join(collected)
    log_event(thread_id, event, {"question": question, "answer_len": len(full_answer)})

    # Persist to checkpointer so future turns see this exchange in history.
    new_history = _merge_history(history_lines, [f"Korisnik: {question}", f"Bot: {full_answer}"])
    _node_for_event = {
        "generate_stream": "generate_node",
        "escalate_stream": "escalate_node",
        "chat_stream": "chat_node",
    }
    await agent.aupdate_state(
        config,
        {
            "question": question,
            "user_context": user_context,
            "answer": full_answer,
            "chat_history": new_history,
        },
        as_node=_node_for_event.get(event, "chat_node"),
    )
