"""
Minimal sentiment API for feedback. Expects POST /sentiment with {"text":"..."}
and returns {"sentiment":"positive|negative|neutral", "score":0.0-1.0}.
Run: uvicorn main:app --reload --host 0.0.0.0 --port 8001
"""
from fastapi import FastAPI
from pydantic import BaseModel
import re

app = FastAPI()

# Normalize: lowercase, remove accents for matching
def _norm(s):
    if not s:
        return ""
    s = s.lower().strip()
    for a, b in [("é", "e"), ("è", "e"), ("ê", "e"), ("ë", "e"), ("à", "a"), ("â", "a"), ("ù", "u"), ("û", "u"), ("ô", "o"), ("î", "i"), ("ï", "i"), ("ç", "c")]:
        s = s.replace(a, b)
    return s

# Keywords: check if they appear as whole words or in common phrases (French + English)
POSITIVE = {
    "merci", "super", "bien", "excellent", "genial", "génial", "content", "satisfait", "bravo", "parfait", "top",
    "bon", "bonne", "sympa", "agreable", "agréable", "ravi", "recommand", "efficace", "pro", "propre",
    "good", "great", "nice", "excellent", "happy", "love", "best", "awesome", "perfect", "thanks", "thank you",
    "tres bien", "très bien", "trop bien", "c est bien", "c'est bien", "tres bon", "très bon", "tres bonne", "très bonne",
}
NEGATIVE = {
    "mal", "nul", "probleme", "problème", "insatisfait", "decu", "déçu", "erreur", "bug", "lent", "impossible",
    "pas bien", "pas bon", "mauvais", "horrible", "nul", "decevant", "décevant", "frustr", "enerv", "énerv",
    "bad", "poor", "worst", "terrible", "slow", "broken", "error", "fail", "disappoint", "angry", "hate",
    "pas content", "pas satisfait", "trop lent", "ne marche pas", "ca ne va pas", "ça ne va pas",
}


class SentimentRequest(BaseModel):
    text: str


def _count_matches(text_norm, keywords):
    """Count how many distinct (normalized) keywords appear in the text."""
    seen_norm = set()
    count = 0
    for kw in keywords:
        kw_norm = _norm(kw)
        if kw_norm in seen_norm:
            continue
        seen_norm.add(kw_norm)
        if re.search(r"\b" + re.escape(kw_norm) + r"\b", text_norm) or kw_norm in text_norm:
            count += 1
    return count


@app.post("/sentiment")
def sentiment(req: SentimentRequest):
    text = (req.text or "").strip()
    if not text:
        return {"sentiment": "neutral", "score": 0.5}

    text_norm = _norm(text)
    pos = _count_matches(text_norm, POSITIVE)
    neg = _count_matches(text_norm, NEGATIVE)

    if pos > neg:
        # 1 match -> 0.65, 2 -> 0.75, ...
        score = min(0.5 + 0.15 * (pos - neg), 1.0)
        return {"sentiment": "positive", "score": round(score, 2)}
    if neg > pos:
        score = max(0.5 - 0.15 * (neg - pos), 0.0)
        return {"sentiment": "negative", "score": round(score, 2)}
    return {"sentiment": "neutral", "score": 0.5}
