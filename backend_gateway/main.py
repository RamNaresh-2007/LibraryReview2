from fastapi import FastAPI, Request, Response
from fastapi.middleware.cors import CORSMiddleware
from fastapi.responses import JSONResponse
import httpx
import logging

# ── Logging ────────────────────────────────────────────────────────
logging.basicConfig(level=logging.INFO, format="%(asctime)s  %(levelname)s  %(message)s")
logger = logging.getLogger("gateway")

# ── App ────────────────────────────────────────────────────────────
app = FastAPI(
    title="Digital Library API Gateway",
    description="FastAPI Gateway — routes all requests to Spring Boot backend",
    version="1.0.0",
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["http://localhost:5173", "http://localhost:3000", "*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

SPRING_BOOT_URL = "http://localhost:8080"

# ── Persistent Client ──────────────────────────────────────────────
# Singleton client enables connection pooling and speeds up communication
client = httpx.AsyncClient(timeout=30)

@app.on_event("shutdown")
async def shutdown_event():
    await client.aclose()

# ── Health check ───────────────────────────────────────────────────
@app.get("/health", tags=["Gateway"])
async def health():
    """Gateway health check endpoint."""
    try:
        r = await client.get(f"{SPRING_BOOT_URL}/h2-console")
        backend_up = r.status_code < 500
    except Exception:
        backend_up = False
    return {
        "gateway": "up",
        "backend": "up" if backend_up else "unreachable",
        "backend_url": SPRING_BOOT_URL,
    }

# ── Generic reverse-proxy ──────────────────────────────────────────
@app.api_route("/{path:path}", methods=["GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"])
async def proxy(request: Request, path: str):
    """Proxy every request to Spring Boot, forwarding headers & body."""
    target_url = f"{SPRING_BOOT_URL}/{path}"

    # Forward query params
    params = dict(request.query_params)

    # Forward headers (drop host so httpx sets its own)
    headers = {
        k: v for k, v in request.headers.items()
        if k.lower() not in ("host", "content-length")
    }

    # Read body
    body_bytes = await request.body()

    logger.info(f"→ {request.method} /{path}")

    try:
        response = await client.request(
            method=request.method,
            url=target_url,
            params=params,
            headers=headers,
            content=body_bytes,
        )

        logger.info(f"← {response.status_code} /{path}")

        # Try JSON, fall back to raw text
        content_type = response.headers.get("content-type", "")
        if "application/json" in content_type:
            try:
                return JSONResponse(
                    content=response.json(),
                    status_code=response.status_code,
                )
            except Exception:
                pass

        # Non-JSON response (e.g. 204 No Content)
        return Response(
            content=response.content,
            status_code=response.status_code,
            media_type=content_type or "application/octet-stream",
        )

    except httpx.ConnectError:
        logger.error("Cannot reach Spring Boot backend")
        return JSONResponse(
            status_code=503,
            content={"message": "Backend service unavailable."},
        )
    except httpx.TimeoutException:
        logger.error("Request to backend timed out")
        return JSONResponse(
            status_code=504,
            content={"message": "Backend request timed out."},
        )
    except Exception as e:
        logger.exception("Gateway error")
        return JSONResponse(
            status_code=500,
            content={"message": f"Gateway error: {str(e)}"},
        )
