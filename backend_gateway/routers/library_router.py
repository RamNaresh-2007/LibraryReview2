from fastapi import APIRouter, Request
from utils import forward_request

router = APIRouter(prefix="/api/v2", tags=["Node Library Service"])

@router.api_route("/{path:path}", methods=["GET", "POST", "PUT", "PATCH", "DELETE"])
async def proxy_library_service(request: Request, path: str):
    """
    Proxies all /api/v2/... requests to the new Node.js Library Service (port 8080).
    For example: /api/v2/api/books -> routes to http://localhost:8080/api/books
    """
    from utils import LIBRARY_SERVICE_URL
    return await forward_request(request, path, target_root=LIBRARY_SERVICE_URL)
