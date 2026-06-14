from fastapi import APIRouter, Request, Depends
from typing import List
from schemas import BookResponse, BookBase
from utils import forward_request
from dependencies import get_current_user, check_role
from mongodb_service import mongo_service

router = APIRouter(prefix="/api/books", tags=["Books"])

@router.get("", response_model=List[BookResponse])
async def get_books(request: Request, user: dict = Depends(get_current_user)):
    """CO3: Get Catalog — requires valid JWT"""
    return await forward_request(request, "api/books")

@router.get("/{id}", response_model=BookResponse)
async def get_book(request: Request, id: int, user: dict = Depends(get_current_user)):
    return await forward_request(request, f"api/books/{id}")

@router.post("", response_model=BookResponse)
async def create_book(
    request: Request, 
    body: BookBase, 
    user: dict = Depends(check_role(["admin", "librarian"]))
):
    """CO3: RBAC — Only Admin/Librarian can add books"""
    response = await forward_request(request, "api/books")
    if response.status_code in (200, 201):
        # Log to activity_logs
        await mongo_service.log_activity(
            username=user.get("username", "system"),
            action="add_book",
            details=f"Added new book '{body.title}' (ISBN: {body.isbn})"
        )
        # Push a copy of the data to the books collection in MongoDB Atlas
        await mongo_service.add_book(body.dict())
    return response

@router.delete("/{id}")
async def delete_book(
    request: Request, 
    id: int, 
    user: dict = Depends(check_role(["admin"]))
):
    """CO3: RBAC — Only Admin can delete books"""
    return await forward_request(request, f"api/books/{id}")
