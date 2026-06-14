from fastapi import APIRouter, Request, Depends
from typing import List, Dict, Any
from utils import forward_request
from dependencies import check_role

router = APIRouter(prefix="/api/analytics", tags=["Analytics"])

@router.get("/dashboard")
async def get_dashboard(request: Request, user: dict = Depends(check_role(["admin", "librarian", "teacher", "student"]))):
    """CO1/CO3: Aggregate dashboard stats from Spring Boot"""
    return await forward_request(request, "api/analytics/dashboard")

@router.get("/top-books")
async def get_top_books(request: Request, user: dict = Depends(check_role(["admin", "librarian"]))):
    """CO1: Window function results (RANK)"""
    return await forward_request(request, "api/analytics/top-books")

@router.get("/loan-trend")
async def get_loan_trend(request: Request, user: dict = Depends(check_role(["admin", "librarian"]))):
    """CO1: Trend analysis results (LAG)"""
    return await forward_request(request, "api/analytics/loan-trend")

@router.get("/category-stats")
async def get_category_stats(request: Request, user: dict = Depends(check_role(["admin", "librarian", "teacher", "student"]))):
    """CO1: Group By statistics"""
    return await forward_request(request, "api/analytics/category-stats")
