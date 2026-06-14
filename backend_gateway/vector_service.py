import math
from typing import List, Dict

# ── CO2: Vector Database Foundations ───────────────────────────────

class VectorSearchService:
    def __init__(self):
        # Mock embedding database for demonstration (CO2: Embeddings)
        # In production: utilize pgvector or use sentence-transformers to generate these
        self.catalog_vectors = {
            1: [0.9, 0.1, 0.05], # Clean Code
            2: [0.85, 0.15, 0.0], # Pragmatic Programmer
            4: [0.1, 0.2, 0.95], # History of Time (Science)
            3: [0.8, 0.3, 0.1],  # Design Patterns
        }

    def cosine_similarity(self, v1: List[float], v2: List[float]) -> float:
        """CO2: Similarity Metrics — Cosine Similarity Calculation"""
        dot_product = sum(a * b for a, b in zip(v1, v2))
        magnitude1 = math.sqrt(sum(a * a for a in v1))
        magnitude2 = math.sqrt(sum(a * a for a in v2))
        if not magnitude1 or not magnitude2:
            return 0.0
        return dot_product / (magnitude1 * magnitude2)

    def search(self, query_vector: List[float], limit: int = 3) -> List[Dict]:
        """CO2: Vector Implementation — ANN Search (Approximated)"""
        results = []
        for book_id, vec in self.catalog_vectors.items():
            score = self.cosine_similarity(query_vector, vec)
            results.append({"bookId": book_id, "similarity": score})
        
        # Sort by similarity score descending
        results.sort(key=lambda x: x["similarity"], reverse=True)
        return results[:limit]

vector_service = VectorSearchService()
