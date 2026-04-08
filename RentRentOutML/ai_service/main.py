from fastapi import FastAPI
from pydantic import BaseModel
import torch
import torch.nn as nn
import joblib

# 1. MORAMO PONOVITI KLASU MODELA DA BI PYTORCH ZNAO KAKO DA UČITA TEŽINE
class RentRentOutKategorizator(nn.Module):
    def __init__(self, ulazna_velicina, broj_klasa):
        super(RentRentOutKategorizator, self).__init__()
        self.mreza = nn.Sequential(
            nn.Linear(ulazna_velicina, 256),
            nn.ReLU(),
            nn.Dropout(0.3),
            nn.Linear(256, 128),
            nn.ReLU(),
            nn.Dropout(0.2),
            nn.Linear(128, broj_klasa)
        )

    def forward(self, x):
        return self.mreza(x)

# 2. INICIJALIZACIJA APLIKACIJE I UČITAVANJE ARTIFAKATA (Učitava se samo jednom pri paljenju!)
app = FastAPI(title="RentRentOut AI Service")

print("Učitavam model i artifakte u RAM...")
# Učitavamo prevodioca i vektorizator
vectorizer = joblib.load("tfidf_vectorizer.pkl")
label_encoder = joblib.load("label_encoder.pkl")

# Inicijalizujemo praznu mrežu, pa u nju "ubrizgavamo" sačuvane težine (mozak)
ulazna_velicina = len(vectorizer.vocabulary_)
broj_klasa = len(label_encoder.classes_)

model = RentRentOutKategorizator(ulazna_velicina, broj_klasa)
model.load_state_dict(torch.load("rentrentout_model.pth", map_location=torch.device('cpu'), weights_only=True))
model.eval() # OBAVEZNO! Gasimo Dropout jer sada samo koristimo model, ne treniramo ga
print("Model uspešno učitan i spreman!")

# 3. DEFINIŠEMO STRUKTURU ZAHTEVA (Kao DTO u Spring Boot-u)
class AdRequest(BaseModel):
    title: str

# 4. PRAVIMO ENDPOINT (RUTU)
@app.post("/api/predict-category")
def predict_category(request: AdRequest):
    # a) Tekst -> Brojevi (TF-IDF)
    X_novi = vectorizer.transform([request.title]).toarray()
    
    # b) Numpy -> PyTorch Tenzor
    X_tenzor = torch.tensor(X_novi, dtype=torch.float32)
    
    # c) Pogađanje top-5 (Inference)
    with torch.no_grad(): # Gasimo praćenje gradijenata radi brzine
        izlaz = model(X_tenzor)
        top5_indeksi = izlaz.topk(5, dim=1).indices[0].tolist()

    # d) Prevođenje indeksa nazad u MySQL ID-eve
    top5_ids = [int(label_encoder.inverse_transform([i])[0]) for i in top5_indeksi]

    return {
        "title": request.title,
        "predicted_category_ids": top5_ids
    }