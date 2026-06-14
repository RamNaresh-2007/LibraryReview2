from fastapi import APIRouter, Depends, Query
from typing import List
from schemas import BookResponse
import random

from vector_service import vector_service

router = APIRouter(prefix="/api/search", tags=["Vector Search"])

@router.get("/vector", response_model=List[dict])
async def semantic_search(
    q: str = Query(..., description="Query for semantic book search"),
    limit: int = 3
):
    """
    CO2: Vector Database Implementation.
    Uses Cosine Similarity to find relevant matches.
    """
    # Mock query embedding generation (Normally done with a model like BERT)
    query_vec = [0.88, 0.12, 0.05] if "code" in q.lower() or "program" in q.lower() else [0.1, 0.1, 0.9]
    
    results = vector_service.search(query_vec, limit)
    return results
