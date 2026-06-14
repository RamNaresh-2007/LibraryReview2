from fastapi import FastAPI, Request
from fastapi.middleware.cors import CORSMiddleware
from fastapi.responses import JSONResponse
import logging
import asyncio

from routers import auth_router, books_router, analytics_router, search_router, notifications_router
from mongodb_service import mongo_service

# ── Logging ────────────────────────────────────────────────────────
logging.basicConfig(level=logging.INFO, format="%(asctime)s  %(levelname)s  %(message)s")
logger = logging.getLogger("gateway")

# ── CO3: FastAPI Core Framework ────────────────────────────────────
app = FastAPI(
    title="Digital Library API Gateway (Enhanced)",
    description="CO3 FastAPI | CO2 Polyglot | CO5 Microservices Gateway",
    version="2.0.0",
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# ── CO5: Distributed Concepts (Circuit Breaker State) ───────────────
class CircuitBreaker:
    def __init__(self, name: str):
        self.name = name
        self.failure_count = 0
        self.state = "CLOSED"  # CLOSED, OPEN, HALF-OPEN
        self.last_failure_time = 0
        self.threshold = 5
        self.recovery_timeout = 30

    def record_failure(self):
        self.failure_count += 1
        if self.failure_count >= self.threshold:
            self.state = "OPEN"
            self.last_failure_time = asyncio.get_event_loop().time()
            logger.error(f"Circuit Breaker [{self.name}] is now OPEN")

    def record_success(self):
        self.failure_count = 0
        self.state = "CLOSED"

    def can_proceed(self):
        if self.state == "OPEN":
            if asyncio.get_event_loop().time() - self.last_failure_time > self.recovery_timeout:
                self.state = "HALF-OPEN"
                return True
            return False
        return True

cb_spring = CircuitBreaker("Spring-Backend")

# ── CO5: Saga Pattern Logic (Conceptual) ──────────────────────────
@app.post("/api/saga/issue-book", tags=["Distributed Concepts"])
async def issue_book_saga(request: Request):
    """
    CO5: Saga Pattern — orchestrated distributed transaction.
    """
    from utils import forward_request, NODE_SERVICE_URL, client, SPRING_BOOT_URL
    body = await request.json()
    
    try:
        # Step 1: Spring Boot Book Reservation (PostgreSQL - Relational)
        logger.info("Saga [Step 1]: Creating loan in Spring Boot...")
        headers = {k: v for k, v in request.headers.items() if k.lower() not in ("host", "content-length")}
        
        # We use the internal client to call Spring
        spring_res = await client.post(f"{SPRING_BOOT_URL}/api/loans", json=body, headers=headers)
        
        if spring_res.status_code >= 400:
            logger.error(f"Saga Step 1 Failed: {spring_res.text}")
            return JSONResponse(status_code=spring_res.status_code, content=spring_res.json())

        saved_loan = spring_res.json()

        # Step 2: MongoDB Activity Logging (NoSQL - Document)
        logger.info("Saga [Step 2]: Logging activity in MongoDB...")
        await mongo_service.log_activity(
            username=body.get("memberName", "system"),
            action="issue",
            details=f"Book '{body.get('bookTitle')}' (ISBN: {body.get('isbn')}) issued via Saga Orchestrator"
        )

        # Step 3: Node.js Notification (Microservice Sync)
        logger.info("Saga [Step 3]: Notifying Node.js service...")
        try:
            await client.post(f"{NODE_SERVICE_URL}/api/notify", json={
                "userId": body.get("memberName"),
                "message": f"Success! You have borrowed {body.get('bookTitle')}."
            }, timeout=2)
        except Exception:
            logger.warning("Saga [Step 3]: Notification service skipped (Non-critical)")

        return saved_loan

    except Exception as e:
        logger.error(f"Saga Failed: {e}")
        return JSONResponse(status_code=500, content={"error": f"Distributed transaction failure: {str(e)}"})

# ── Include Routers ────────────────────────────────────────────────
app.include_router(auth_router.router)
app.include_router(books_router.router)
app.include_router(analytics_router.router)
app.include_router(search_router.router)
app.include_router(notifications_router.router)
from routers import library_router
app.include_router(library_router.router)

@app.on_event("startup")
async def startup_db_client():
    logger.info("Gateway starting up...")

@app.on_event("shutdown")
async def shutdown_db_client():
    logger.info("Gateway shutting down...")

# ── Middleware: Request Logging ────────────────────────────────────
@app.middleware("http")
async def log_requests(request: Request, call_next):
    logger.info(f"Incoming: {request.method} {request.url.path}")
    response = await call_next(request)
    return response

# ── Health check ───────────────────────────────────────────────────
@app.get("/health", tags=["Gateway"])
async def health():
    return {
        "status": "ready",
        "gateway": "online",
        "circuit_breaker": cb_spring.state,
        "database": "polyglot-active"
    }

@app.get("/api/activity-log", tags=["NoSQL"])
async def get_activity(user: dict = None):
    """CO2: MongoDB CRUD — Retrieve recent logs"""
    return await mongo_service.get_recent_logs(20)

# Generic fallback
@app.api_route("/{path:path}", methods=["GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"])
async def proxy_fallback(request: Request, path: str):
    from utils import forward_request
    return await forward_request(request, path)
