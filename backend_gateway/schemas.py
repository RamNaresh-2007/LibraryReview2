from pydantic import BaseModel, EmailStr, Field, validator
from typing import List, Optional, Any
from datetime import date

# ── CO3: Pydantic Models for API Contracts ──────────────────────────

class LoginRequest(BaseModel):
    username: str
    password: str

class RegisterRequest(BaseModel):
    username: str
    password: str
    fullname: str
    email: EmailStr
    role: str = "student"

class UserResponse(BaseModel):
    id: Optional[int] = None
    username: str
    fullname: str
    email: str
    role: str
    status: Optional[str] = "active"
    token: Optional[str] = None
    joinedDate: Optional[str] = None

class BookBase(BaseModel):
    title: str
    author: str
    isbn: Optional[str] = None
    categoryName: Optional[str] = None
    publisher: Optional[str] = None
    publishYear: Optional[int] = None
    copies: int = 1
    available: bool = True
    description: Optional[str] = None

class BookResponse(BookBase):
    id: int

class LoanBase(BaseModel):
    userId: Optional[int] = None
    bookId: Optional[int] = None
    memberName: str
    memberRole: str = "student"
    bookTitle: str
    isbn: Optional[str] = None
    dueDate: str

class LoanResponse(LoanBase):
    id: int
    issuedDate: str
    returnedAt: Optional[str] = None
    status: str
    fineAmount: float = 0.0

class CategoryResponse(BaseModel):
    id: int
    name: str
    description: Optional[str] = None

# ── CO2: NoSQL / MongoDB Models ────────────────────────────────────

class ActivityLog(BaseModel):
    user: str
    action: str
    details: str
    timestamp: str

class AnalyticsDashboard(BaseModel):
    totalBooks: int
    totalMembers: int
    activeLoans: int
    overdueLoans: int
    totalLoans: int
    availableBooks: int
    membersByRole: dict
