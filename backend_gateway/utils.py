import httpx
import logging
from fastapi import Request, Response
from fastapi.responses import JSONResponse

import os

logger = logging.getLogger("gateway.utils")

SPRING_BOOT_URL = os.environ.get("SPRING_BOOT_URL", "http://localhost:8085")
NODE_SERVICE_URL = os.environ.get("NODE_SERVICE_URL", "http://localhost:8081")
LIBRARY_SERVICE_URL = os.environ.get("LIBRARY_SERVICE_URL", "http://localhost:8080")
client = httpx.AsyncClient(timeout=30)

async def forward_request(request: Request, path: str, target_root: str = SPRING_BOOT_URL):
    target_url = f"{target_root}/{path}"
    params = dict(request.query_params)
    headers = {
        k: v for k, v in request.headers.items()
        if k.lower() not in ("host", "content-length", "origin")
    }
    
    try:
        body_bytes = await request.body()
    except Exception:
        body_bytes = b""

    try:
        response = await client.request(
            method=request.method,
            url=target_url,
            params=params,
            headers=headers,
            content=body_bytes,
        )

        content_type = response.headers.get("content-type", "")
        
        if "application/json" in content_type:
            try:
                return JSONResponse(
                    content=response.json(),
                    status_code=response.status_code,
                )
            except Exception:
                pass

        return Response(
            content=response.content,
            status_code=response.status_code,
            media_type=content_type or "application/octet-stream",
        )

    except httpx.ConnectError:
        logger.error(f"Cannot reach backend at {target_url}")
        return JSONResponse(status_code=503, content={"message": "Backend service unavailable."})
    except Exception as e:
        logger.exception("Gateway proxy error")
        return JSONResponse(status_code=500, content={"message": f"Gateway error: {str(e)}"})
