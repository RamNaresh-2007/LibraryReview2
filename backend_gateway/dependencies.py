from fastapi import Header, HTTPException, Depends
from typing import Optional
import jwt
import time
from collections import defaultdict

# ── CO3: Security & Dependency Injection ───────────────────────────

JWT_SECRET = "LibraryManagement2026SecretKeyForJWTAuthenticationMustBe256Bits!"
ALGORITHM = "HS256"

# ── Simple In-memory Rate Limiter ──────────────────────────────────
_request_counts = defaultdict(list)

def rate_limiter(user_id: str = "guest", limit: int = 100, window: int = 60):
    """Simple rate limiter: limit requests per user in a time window (seconds)."""
    current_time = time.time()
    _request_counts[user_id] = [t for t in _request_counts[user_id] if t > current_time - window]
    
    if len(_request_counts[user_id]) >= limit:
        raise HTTPException(status_code=429, detail="Too many requests. Please slow down.")
    
    _request_counts[user_id].append(current_time)

# ── JWT Auth Dependency ───────────────────────────────────────────
async def get_current_user(authorization: Optional[str] = Header(None)):
    if not authorization:
        raise HTTPException(status_code=401, detail="Missing Authorization Header")
    
    try:
        token = authorization.split(" ")[1] if " " in authorization else authorization
        payload = jwt.decode(token, JWT_SECRET, algorithms=[ALGORITHM])
        username: str = payload.get("sub")
        role: str = payload.get("role")
        
        if username is None:
            raise HTTPException(status_code=401, detail="Invalid token payload")
        
        # Apply rate limiting to authenticated users
        rate_limiter(username)
        
        return {"username": username, "role": role}
    except jwt.ExpiredSignatureError:
        raise HTTPException(status_code=401, detail="Token has expired")
    except Exception:
        raise HTTPException(status_code=401, detail="Could not validate credentials")

# ── RBAC Dependency ──────────────────────────────────────────────
def check_role(required_roles: list):
    async def role_checker(user: dict = Depends(get_current_user)):
        if user["role"] not in required_roles:
            raise HTTPException(status_code=403, detail="Insufficient permissions")
        return user
    return role_checker
