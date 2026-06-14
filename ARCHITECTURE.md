# Digital Library System Architecture (CO1–CO5)

This document describes the high-performance, polyglot microservices architecture implemented for the Digital Library Management system.

## ── 1. Service Decomposition (CO5) ───────────────────────────
The system is divided into three primary services and a unified gateway:

- **Core Library Service (Spring Boot)**: Manages relational data (Books, Users, Loans) in **PostgreSQL**.
- **Notification Service (Node.js)**: Handles real-time events and notifications using **Socket.io** and **MongoDB**.
- **Intelligence Gateway (FastAPI)**: Orchestrates requests, implements security/rate-limiting, and manages the **Saga Pattern**.

## ── 2. Polyglot Persistence (CO1, CO2) ────────────────────────
We use the "Database-per-Service" pattern to ensure loose coupling and specialized storage:

| Storage | Engine | Purpose |
| :--- | :--- | :--- |
| **Relational** | PostgreSQL | 3NF normalized catalog, transactions, ACID compliance, Window Functions. |
| **Document** | MongoDB | Activity logs, notification history, unstructured event data. |
| **Vector** | pgvector (Stub) | Semantic search and recommendation system (similarity metrics). |

## ── 3. Gateway Resilience & Patterns (CO3, CO5) ──────────────
The FastAPI Gateway acts as a smart orchestrator:

- **Circuit Breaker**: Prevents cascading failures if the Spring Boot or Node service goes down.
- **Saga Pattern**: Executes a distributed "Issue Book" transaction across PostgreSQL (Spring) and MongoDB (Activity Log).
- **Rate Limiting**: Protects services from exhaustion using an in-memory sliding window.
- **Unified OpenAPI**: Exposes a single Swagger UI for all microservices.

## ── 4. Advanced SQL (CO1) ────────────────────────────────────
The system leverages PostgreSQL's full power via custom native queries in Spring Boot:
- **RANK() / DENSE_RANK()**: For popularity leaderboards.
- **LAG()**: For month-over-month loan trend analysis.
- **CTEs**: For complex hierarchical queries (e.g., top-borrower with fine aggregation).

## ── 5. Real-Time (CO4) ───────────────────────────────────────
The Node.js service provides an event-driven layer:
- **Express Middleware**: JWT verification and Joi validation.
- **Socket.io**: Pushes instant notifications to the React frontend when books are issued or returned.
