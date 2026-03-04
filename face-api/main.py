from fastapi import FastAPI
from pydantic import BaseModel
from typing import Optional, Dict, List
import base64
import io

import numpy as np
from PIL import Image

app = FastAPI()

# Simple in-memory DB: user_id -> face embedding (list of floats)
db_encodings: Dict[int, List[float]] = {}


class EnrollPayload(BaseModel):
    user_id: int
    image_base64: str


class VerifyPayload(BaseModel):
    image_base64: str


def image_to_embedding(b64: str) -> Optional[np.ndarray]:
    """
    Decode base64 image, convert to grayscale 32x32 and flatten to a vector.
    This is a very simple 'embedding' just for demo; not real face recognition.
    """
    try:
        data = base64.b64decode(b64)
        img = Image.open(io.BytesIO(data)).convert("L")  # grayscale
        img = img.resize((32, 32))
        arr = np.asarray(img, dtype=np.float32) / 255.0
        return arr.flatten()
    except Exception:
        return None


@app.post("/face/enroll")
def enroll(payload: EnrollPayload):
    emb = image_to_embedding(payload.image_base64)
    if emb is None:
        return {"status": "error", "reason": "invalid_image"}
    db_encodings[payload.user_id] = emb.tolist()
    return {"status": "ok"}


@app.post("/face/verify")
def verify(payload: VerifyPayload):
    query = image_to_embedding(payload.image_base64)
    if query is None:
        return {"matched_user_id": None, "score": 0.0}

    best_id: Optional[int] = None
    best_score = 0.0

    for uid, stored_list in db_encodings.items():
        stored = np.asarray(stored_list, dtype=np.float32)
        # Cosine similarity between query and stored vector
        denom = (np.linalg.norm(query) * np.linalg.norm(stored)) or 1.0
        score = float(np.dot(query, stored) / denom)
        if score > best_score:
            best_score = score
            best_id = uid

    # Threshold: tune as needed; here 0.9 for "close" match
    if best_id is None or best_score < 0.9:
        return {"matched_user_id": None, "score": float(best_score)}

    return {"matched_user_id": int(best_id), "score": float(best_score)}