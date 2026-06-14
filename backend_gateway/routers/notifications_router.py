from fastapi import APIRouter, Request
from utils import forward_request, NODE_SERVICE_URL

router = APIRouter(prefix="/api/notify", tags=["Notifications"])

@router.post("")
async def send_notification(request: Request):
    """CO4/CO5: Proxy to Node.js Notification microservice"""
    return await forward_request(request, "api/notify", target_root=NODE_SERVICE_URL)

@router.get("/health")
async def node_health(request: Request):
    """Check Node.js service health through Gateway"""
    return await forward_request(request, "health", target_root=NODE_SERVICE_URL)
