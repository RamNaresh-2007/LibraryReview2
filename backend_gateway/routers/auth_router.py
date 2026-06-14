from fastapi import APIRouter, Request
from schemas import LoginRequest, RegisterRequest, UserResponse
from utils import forward_request

from mongodb_service import mongo_service

router = APIRouter(prefix="/api/auth", tags=["Authentication"])

@router.post("/login", response_model=UserResponse)
async def login(request: Request, body: LoginRequest):
    """CO3: REST Login — delegates to Spring Boot + Logs to MongoDB"""
    response = await forward_request(request, "api/auth/login")
    if response.status_code == 200:
        await mongo_service.log_activity(body.username, "login", "User successfully authenticated")
    return response

@router.post("/register", response_model=UserResponse)
async def register(request: Request, body: RegisterRequest):
    """CO3: REST Registration — delegates to Spring Boot + Logs to MongoDB"""
    response = await forward_request(request, "api/auth/register")
    if response.status_code == 200:
        await mongo_service.log_activity(body.username, "register", f"New user registered as {body.role}")
    return response
