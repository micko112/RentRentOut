import os
from typing import TypedDict, List, Annotated
import operator
from langchain_community.document_loaders import TextLoader
from langchain_text_splitters import RecursiveCharacterTextSplitter
from langchain_openai import OpenAIEmbeddings, ChatOpenAI
from langchain_chroma import Chroma
from langgraph.graph import StateGraph, START, END
from langgraph.checkpoint.memory import MemorySaver

_BASE_DIR = os.path.dirname(os.path.abspath(__file__))

llm = ChatOpenAI(model="gpt-4o-mini", temperature=0.6)


_CHROMA_DIR = os.path.join(_BASE_DIR, "chroma_db")


def _build_vector_store():
    embeddings = OpenAIEmbeddings(model="text-embedding-3-small")
    if os.path.exists(_CHROMA_DIR) and os.listdir(_CHROMA_DIR):
        print("Učitavam postojeću vektorsku bazu sa diska...")
        store = Chroma(persist_directory=_CHROMA_DIR, embedding_function=embeddings)
        print("Vektorska baza učitana.")
        return store
    loader = TextLoader(os.path.join(_BASE_DIR, "baza_znanja.txt"), encoding="utf-8")
    docs = loader.load()
    splitter = RecursiveCharacterTextSplitter(chunk_size=400, chunk_overlap=50)
    chunks = splitter.split_documents(docs)
    print(f"Gradim vektorsku bazu iz {len(chunks)} chunk-ova...")
    store = Chroma.from_documents(documents=chunks, embedding=embeddings, persist_directory=_CHROMA_DIR)
    print("Vektorska baza sačuvana na disk.")
    return store


_vector_store = _build_vector_store()
retriever = _vector_store.as_retriever(search_kwargs={"k": 3})


class AgentState(TypedDict):
    question: str
    user_context: str
    documents: List[str]
    chat_history: Annotated[List[str], operator.add]
    is_relevant: str
    answer: str


def router(state):
    question = state["question"]
    history = "\n".join(state.get("chat_history", [])[-6:])
    prompt = f"""Klasifikuj pitanje korisnika u jednu od dve kategorije:

- "retrieve": pitanje traži konkretnu informaciju o platformi izdajemiznajmljujem.com (oglasi, pravila, plaćanje, krediti, ugovori, promocije, registracija, kako nešto funkcioniše)
- "chat": pozdravi ("ćao", "zdravo", "jel si tu"), zahvale ("hvala", "ok"), ćaskanje, nejasna kratka pitanja, teme van platforme

Uzmi u obzir i prethodni razgovor (korisnik se možda nadovezuje).
Prethodni razgovor: {history}
Pitanje: {question}

Odgovori ISKLJUČIVO jednom rečju: retrieve ili chat."""
    decision = llm.invoke(prompt).content.strip().lower()
    return "retrieve_node" if "retrieve" in decision else "chat_node"


def retrieve_node(state):
    question = state["question"]
    history = "\n".join(state.get("chat_history", [])[-6:])

    expansion_prompt = f"""Korisnik je postavio pitanje: '{question}'.
Prethodni razgovor: {history}
Preformuliši pitanje na 3 različita načina (sinonimi, profesionalniji rečnik) da poboljšamo pretragu baze znanja.
Vrati ISKLJUČIVO ta 3 pitanja, svako u novom redu, bez dodatnog teksta ili rednih brojeva."""

    expanded_text = llm.invoke(expansion_prompt).content.strip()
    queries = [q.strip() for q in expanded_text.split("\n") if q.strip()]
    queries.append(question)

    all_docs = []
    seen = set()
    for q in queries:
        for d in retriever.invoke(q):
            if d.page_content not in seen:
                seen.add(d.page_content)
                all_docs.append(d.page_content)

    return {"documents": all_docs[:6]}


def grade_node(state):
    question = state["question"]
    context = "\n".join(state["documents"])
    prompt = f"""Da li TEKST sadrži informacije koje mogu pomoći u odgovoru na PITANJE — čak i delimično, indirektno, ili povezane informacije koje bi korisnik mogao smatrati korisnim?
Budi blag u oceni — ako ima i najmanja relevantna informacija, odgovori yes.
Odgovori NO samo ako je TEKST potpuno nepovezan sa pitanjem.

TEKST: {context}
PITANJE: {question}

Odgovori ISKLJUČIVO sa: yes ili no."""
    decision = llm.invoke(prompt).content.strip().lower()
    return {"is_relevant": "yes" if "yes" in decision else "no"}


def generate_node(state):
    context = "\n\n".join(state["documents"])
    question = state["question"]
    user_context = state.get("user_context", "")
    history = "\n".join(state.get("chat_history", [])[-10:])

    user_ctx_block = f"\nKontekst korisnika:\n{user_context}" if user_context else ""
    history_block = f"\nPrethodni razgovor:\n{history}" if history else ""

    prompt = f"""Ti si "bot Igor", asistent na platformi izdajemiznajmljujem.com.
Odgovaraj na srpskom jeziku, latinicom. Budi kratak, jasan i prijateljski. Ne koristi emojije.{user_ctx_block}{history_block}

Na osnovu KONTEKSTA odgovori na PITANJE korisnika:
KONTEKST:
{context}

PITANJE: {question}
ODGOVOR:"""

    answer = llm.invoke(prompt).content
    return {
        "answer": answer,
        "chat_history": [f"Korisnik: {question}", f"Bot: {answer}"],
    }


def escalate_node(state):
    question = state["question"]
    context = "\n\n".join(state.get("documents", []))
    user_context = state.get("user_context", "")
    history = "\n".join(state.get("chat_history", [])[-10:])

    user_ctx_block = f"\nKontekst korisnika:\n{user_context}" if user_context else ""
    history_block = f"\nPrethodni razgovor:\n{history}" if history else ""
    context_block = f"\nDelimično relevantan kontekst iz baze:\n{context}" if context else ""

    prompt = f"""Ti si "bot Igor", asistent na platformi izdajemiznajmljujem.com.
Odgovaraj na srpskom jeziku, latinicom. Budi kratak, jasan i prijateljski. Ne koristi emojije.

U bazi znanja nisi našao direktan odgovor na pitanje korisnika. Uradi sledeće:
1) Ako je pitanje vezano za platformu ili iznajmljivanje, pokušaj da pomogneš na osnovu opšteg znanja, ali jasno naglasi da nisi siguran u detalje.
2) Ako je pitanje potpuno van teme (politika, recepti, itd.), ljubazno odbij i vrati korisnika na temu platforme.
3) Za specifična pitanja gde ti treba potvrda, predloži kontakt na izdajemiznajmljujem.rs@gmail.com.{user_ctx_block}{history_block}{context_block}

PITANJE: {question}
ODGOVOR:"""

    answer = llm.invoke(prompt).content
    return {
        "answer": answer,
        "chat_history": [f"Korisnik: {question}", f"Bot: {answer}"],
    }


def chat_node(state):
    question = state["question"]
    history = "\n".join(state.get("chat_history", [])[-10:])
    history_block = f"\nPrethodni razgovor:\n{history}" if history else ""

    prompt = f"""Ti si "bot Igor", asistent na platformi izdajemiznajmljujem.com.
Odgovaraj na srpskom jeziku, latinicom. Budi kratak i prijateljski. Ne koristi emojije.
Baviš se isključivo pitanjima o platformi i pravnim temama vezanim za iznajmljivanje.
Ako pitanje nije vezano za tu temu, ljubazno odbij i vrati korisnika na temu platforme.{history_block}

Korisnik: {question}
Bot:"""

    answer = llm.invoke(prompt).content
    return {
        "answer": answer,
        "chat_history": [f"Korisnik: {question}", f"Bot: {answer}"],
    }


def check_relevance(state):
    return "generate_node" if state["is_relevant"] == "yes" else "escalate_node"


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

memory = MemorySaver()
agent = workflow.compile(checkpointer=memory)
print("LangGraph agent uspešno kompajliran.")
