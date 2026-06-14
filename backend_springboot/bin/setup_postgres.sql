-- =============================================================================
-- DIGITAL LIBRARY MANAGEMENT SYSTEM — FULL RDBMS SETUP (CO1)
-- =============================================================================
-- CO1 Topics: DDL, Constraints, Normalisation, Indexes, Views,
--             Stored Procedures, Triggers, Advanced SQL Queries
-- =============================================================================

-- ─── EXTENSION: Enable pgvector for CO2 Vector DB ────────────────────────────
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
-- CREATE EXTENSION IF NOT EXISTS vector;  -- Uncomment after: apt install postgresql-pgvector

-- =============================================================================
-- PART 1: DDL — Table Creation with Full Constraints (CO1)
-- =============================================================================

DROP TABLE IF EXISTS loan_audit_log   CASCADE;
DROP TABLE IF EXISTS loans            CASCADE;
DROP TABLE IF EXISTS book_embeddings  CASCADE;
DROP TABLE IF EXISTS books            CASCADE;
DROP TABLE IF EXISTS users            CASCADE;
DROP TABLE IF EXISTS categories       CASCADE;

-- ─── categories (1NF → extracted repeating group, fulfils 2NF) ───────────────
CREATE TABLE categories (
    id          SERIAL PRIMARY KEY,
    name        VARCHAR(100) UNIQUE NOT NULL,
    description TEXT
);

-- ─── users (3NF: no transitive dependencies) ─────────────────────────────────
CREATE TABLE users (
    id          SERIAL PRIMARY KEY,
    username    VARCHAR(100) UNIQUE NOT NULL,
    fullname    VARCHAR(255) NOT NULL,
    email       VARCHAR(255) UNIQUE NOT NULL,
    password    VARCHAR(255) NOT NULL,
    role        VARCHAR(50)  NOT NULL DEFAULT 'student'
                    CHECK (role IN ('admin','librarian','teacher','student')),
    status      VARCHAR(50)  NOT NULL DEFAULT 'active'
                    CHECK (status IN ('active','suspended','inactive')),
    joined_date DATE         NOT NULL DEFAULT CURRENT_DATE,
    loans       INTEGER      NOT NULL DEFAULT 0 CHECK (loans >= 0),
    created_at  TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP    NOT NULL DEFAULT NOW()
);

-- ─── books (3NF: isbn determines publisher → extracted if full normalisation) ─
CREATE TABLE books (
    id           SERIAL PRIMARY KEY,
    title        VARCHAR(255) NOT NULL,
    author       VARCHAR(255) NOT NULL,
    isbn         VARCHAR(20)  UNIQUE,
    category_id  INTEGER REFERENCES categories(id) ON DELETE SET NULL,
    publisher    VARCHAR(255),
    publish_year SMALLINT CHECK (publish_year BETWEEN 1000 AND 2100),
    copies       INTEGER NOT NULL DEFAULT 1 CHECK (copies >= 0),
    available    BOOLEAN NOT NULL DEFAULT TRUE,
    description  TEXT,
    created_at   TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMP NOT NULL DEFAULT NOW()
);

-- ─── loans (FK to users.id + books.id, linking table = proper relational join) ─
CREATE TABLE loans (
    id          SERIAL PRIMARY KEY,
    user_id     INTEGER REFERENCES users(id) ON DELETE CASCADE,
    book_id     INTEGER REFERENCES books(id) ON DELETE CASCADE,
    member_name VARCHAR(255) NOT NULL,          -- denormalised cache for display
    member_role VARCHAR(50)  NOT NULL DEFAULT 'student',
    book_title  VARCHAR(255) NOT NULL,          -- denormalised cache for display
    isbn        VARCHAR(20),
    issued_date DATE         NOT NULL DEFAULT CURRENT_DATE,
    due_date    DATE         NOT NULL,
    returned_at DATE,
    status      VARCHAR(50)  NOT NULL DEFAULT 'active'
                    CHECK (status IN ('active','returned','overdue','lost')),
    fine_amount NUMERIC(8,2) DEFAULT 0.00 CHECK (fine_amount >= 0),
    CONSTRAINT chk_due_after_issue CHECK (due_date > issued_date)
);

-- ─── loan_audit_log (audit trail, demonstrates TRIGGER usage) ────────────────
CREATE TABLE loan_audit_log (
    id          SERIAL PRIMARY KEY,
    loan_id     INTEGER NOT NULL,
    action      VARCHAR(50) NOT NULL,           -- INSERT / UPDATE / DELETE
    old_status  VARCHAR(50),
    new_status  VARCHAR(50),
    changed_by  VARCHAR(100),
    changed_at  TIMESTAMP NOT NULL DEFAULT NOW()
);

-- ─── book_embeddings (CO2: Vector DB stub for pgvector) ──────────────────────
CREATE TABLE book_embeddings (
    id          SERIAL PRIMARY KEY,
    book_id     INTEGER UNIQUE REFERENCES books(id) ON DELETE CASCADE,
    embedding   TEXT,                           -- JSON-serialised float array (stub)
    model_name  VARCHAR(100) DEFAULT 'sentence-transformers/all-MiniLM-L6-v2',
    created_at  TIMESTAMP NOT NULL DEFAULT NOW()
    -- With pgvector: embedding vector(384)
);

-- =============================================================================
-- PART 2: INDEXES — Query Optimisation (CO1: Index Design)
-- =============================================================================

CREATE INDEX idx_users_role       ON users(role);
CREATE INDEX idx_users_status     ON users(status);
CREATE INDEX idx_books_author     ON books(author);
CREATE INDEX idx_books_category   ON books(category_id);
CREATE INDEX idx_books_available  ON books(available);
CREATE INDEX idx_loans_user_id    ON loans(user_id);
CREATE INDEX idx_loans_book_id    ON loans(book_id);
CREATE INDEX idx_loans_status     ON loans(status);
CREATE INDEX idx_loans_due_date   ON loans(due_date);
CREATE INDEX idx_audit_loan_id    ON loan_audit_log(loan_id);

-- With pgvector: CREATE INDEX ON book_embeddings USING ivfflat (embedding vector_cosine_ops);

-- =============================================================================
-- PART 3: TRIGGERS — Stored Logic (CO1: Triggers)
-- =============================================================================

-- Trigger 1: Auto-update `updated_at` on any row update
CREATE OR REPLACE FUNCTION update_timestamp()
RETURNS TRIGGER LANGUAGE plpgsql AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$;

CREATE TRIGGER trg_users_updated
    BEFORE UPDATE ON users
    FOR EACH ROW EXECUTE FUNCTION update_timestamp();

CREATE TRIGGER trg_books_updated
    BEFORE UPDATE ON books
    FOR EACH ROW EXECUTE FUNCTION update_timestamp();

-- Trigger 2: Audit log on loan status change
CREATE OR REPLACE FUNCTION log_loan_change()
RETURNS TRIGGER LANGUAGE plpgsql AS $$
BEGIN
    IF TG_OP = 'INSERT' THEN
        INSERT INTO loan_audit_log(loan_id, action, new_status)
        VALUES (NEW.id, 'INSERT', NEW.status);
    ELSIF TG_OP = 'UPDATE' AND OLD.status <> NEW.status THEN
        INSERT INTO loan_audit_log(loan_id, action, old_status, new_status)
        VALUES (NEW.id, 'UPDATE', OLD.status, NEW.status);
    ELSIF TG_OP = 'DELETE' THEN
        INSERT INTO loan_audit_log(loan_id, action, old_status)
        VALUES (OLD.id, 'DELETE', OLD.status);
    END IF;
    RETURN NEW;
END;
$$;

CREATE TRIGGER trg_loan_audit
    AFTER INSERT OR UPDATE OR DELETE ON loans
    FOR EACH ROW EXECUTE FUNCTION log_loan_change();

-- Trigger 3: Auto-mark loan as overdue when due_date passes
CREATE OR REPLACE FUNCTION mark_overdue_loans()
RETURNS void LANGUAGE plpgsql AS $$
BEGIN
    UPDATE loans
    SET    status = 'overdue',
           fine_amount = GREATEST(0, (CURRENT_DATE - due_date) * 2.00)
    WHERE  status = 'active'
      AND  due_date < CURRENT_DATE;
END;
$$;

-- Trigger 4: Decrement book copies on loan issue, increment on return
CREATE OR REPLACE FUNCTION sync_book_copies()
RETURNS TRIGGER LANGUAGE plpgsql AS $$
BEGIN
    IF TG_OP = 'INSERT' AND NEW.book_id IS NOT NULL THEN
        UPDATE books SET copies = GREATEST(copies - 1, 0),
                         available = (copies - 1) > 0
        WHERE  id = NEW.book_id;
    ELSIF TG_OP = 'UPDATE' AND NEW.status = 'returned'
                            AND OLD.status <> 'returned'
                            AND NEW.book_id IS NOT NULL THEN
        UPDATE books SET copies = copies + 1, available = TRUE
        WHERE  id = NEW.book_id;
    END IF;
    RETURN NEW;
END;
$$;

CREATE TRIGGER trg_sync_copies
    AFTER INSERT OR UPDATE ON loans
    FOR EACH ROW EXECUTE FUNCTION sync_book_copies();

-- =============================================================================
-- PART 4: STORED PROCEDURES (CO1: Stored Logic)
-- =============================================================================

-- Procedure: Issue a book (ACID transaction)
CREATE OR REPLACE PROCEDURE issue_book(
    p_user_id  INTEGER,
    p_book_id  INTEGER,
    p_due_date DATE
) LANGUAGE plpgsql AS $$
DECLARE
    v_username   TEXT;
    v_booktitle  TEXT;
    v_isbn       TEXT;
    v_member_role TEXT;
BEGIN
    SELECT username, role INTO v_username, v_member_role FROM users WHERE id = p_user_id;
    SELECT title, isbn INTO v_booktitle, v_isbn FROM books WHERE id = p_book_id;

    IF NOT FOUND THEN
        RAISE EXCEPTION 'Invalid user or book ID';
    END IF;

    IF NOT (SELECT available FROM books WHERE id = p_book_id) THEN
        RAISE EXCEPTION 'Book % is not available', v_booktitle;
    END IF;

    INSERT INTO loans(user_id, book_id, member_name, member_role,
                      book_title, isbn, issued_date, due_date, status)
    VALUES (p_user_id, p_book_id, v_username, v_member_role,
            v_booktitle, v_isbn, CURRENT_DATE, p_due_date, 'active');

    UPDATE users SET loans = loans + 1 WHERE id = p_user_id;
END;
$$;

-- Procedure: Return a book
CREATE OR REPLACE PROCEDURE return_book(p_loan_id INTEGER) LANGUAGE plpgsql AS $$
BEGIN
    UPDATE loans
    SET    status = 'returned', returned_at = CURRENT_DATE,
           fine_amount = GREATEST(0, (CURRENT_DATE - due_date) * 2.00)
    WHERE  id = p_loan_id AND status IN ('active','overdue');

    IF NOT FOUND THEN
        RAISE EXCEPTION 'Loan % not found or already returned', p_loan_id;
    END IF;
END;
$$;

-- =============================================================================
-- PART 5: VIEWS (CO1: Schema Design)
-- =============================================================================

-- View: Active loans with user and book details (JOIN demonstration)
CREATE OR REPLACE VIEW vw_active_loans AS
SELECT
    l.id             AS loan_id,
    u.fullname       AS member_name,
    u.role           AS member_role,
    b.title          AS book_title,
    b.author         AS author,
    b.isbn           AS isbn,
    l.issued_date,
    l.due_date,
    l.status,
    l.fine_amount,
    CURRENT_DATE - l.due_date AS days_overdue
FROM loans l
JOIN users u ON l.user_id = u.id
JOIN books b ON l.book_id = b.id;

-- View: Library statistics
CREATE OR REPLACE VIEW vw_library_stats AS
SELECT
    (SELECT COUNT(*) FROM books)                       AS total_books,
    (SELECT SUM(copies) FROM books)                    AS total_copies,
    (SELECT COUNT(*) FROM books WHERE available)       AS available_books,
    (SELECT COUNT(*) FROM users)                       AS total_members,
    (SELECT COUNT(*) FROM loans WHERE status='active') AS active_loans,
    (SELECT COUNT(*) FROM loans WHERE status='overdue') AS overdue_loans,
    (SELECT COALESCE(SUM(fine_amount),0) FROM loans)  AS total_fines_collected;

-- =============================================================================
-- PART 6: ADVANCED SQL QUERIES (CO1: Window Functions, CTEs, Aggregates)
-- =============================================================================

-- ── Window Function: Rank users by number of loans ───────────────────────────
/*
SELECT
    u.fullname,
    u.role,
    COUNT(l.id)                                                      AS total_loans,
    RANK()     OVER (ORDER BY COUNT(l.id) DESC)                     AS loan_rank,
    DENSE_RANK() OVER (PARTITION BY u.role ORDER BY COUNT(l.id) DESC) AS role_rank,
    ROW_NUMBER() OVER (ORDER BY u.id)                               AS row_num
FROM users u
LEFT JOIN loans l ON l.user_id = u.id
GROUP BY u.id, u.fullname, u.role;
*/

-- ── CTE: Overdue summary with running totals ─────────────────────────────────
/*
WITH overdue_fines AS (
    SELECT
        user_id,
        SUM(fine_amount)                              AS total_fine,
        COUNT(*)                                      AS overdue_count
    FROM loans
    WHERE status = 'overdue'
    GROUP BY user_id
),
ranked AS (
    SELECT
        u.fullname,
        of.total_fine,
        of.overdue_count,
        SUM(of.total_fine) OVER (ORDER BY of.total_fine DESC ROWS UNBOUNDED PRECEDING) AS running_total
    FROM overdue_fines of
    JOIN users u ON u.id = of.user_id
)
SELECT * FROM ranked ORDER BY total_fine DESC;
*/

-- ── SUBQUERY: Books never borrowed ───────────────────────────────────────────
/*
SELECT id, title, author
FROM books
WHERE id NOT IN (SELECT DISTINCT book_id FROM loans WHERE book_id IS NOT NULL);
*/

-- ── LAG/LEAD: Loan activity trend month-over-month ───────────────────────────
/*
SELECT
    TO_CHAR(issued_date,'YYYY-MM')              AS month,
    COUNT(*)                                   AS loans_issued,
    LAG(COUNT(*))  OVER (ORDER BY TO_CHAR(issued_date,'YYYY-MM')) AS prev_month,
    COUNT(*) - LAG(COUNT(*)) OVER (ORDER BY TO_CHAR(issued_date,'YYYY-MM')) AS delta
FROM loans
GROUP BY TO_CHAR(issued_date,'YYYY-MM')
ORDER BY month;
*/

-- =============================================================================
-- PART 7: SEED DATA
-- =============================================================================

INSERT INTO categories(name, description) VALUES
    ('Software',   'Computer science and software engineering'),
    ('Science',    'Natural and applied sciences'),
    ('Fiction',    'Fiction and literary works'),
    ('History',    'Historical texts and biographies'),
    ('Mathematics','Pure and applied mathematics');

INSERT INTO users(username, fullname, email, password, role, status, joined_date, loans)
VALUES
    ('admin',     'System Administrator','admin@library.com','admin123',    'admin',    'active','2024-01-01',0),
    ('librarian', 'Main Librarian',      'lib@library.com',  'lib123',      'librarian','active','2024-01-05',2),
    ('priya',     'Priya Sharma',        'priya@school.com', 'teacher123',  'teacher',  'active','2024-02-10',1),
    ('rahul',     'Rahul Kumar',         'rahul@student.com','student123',  'student',  'active','2024-03-15',0)
ON CONFLICT (username) DO NOTHING;

INSERT INTO books(title, author, isbn, category_id, publisher, publish_year, copies, available, description)
VALUES
    ('Clean Code',             'Robert C. Martin', '978-0132350884',1,'Prentice Hall',  2008,3,TRUE, 'A Handbook of Agile Software Craftsmanship'),
    ('The Pragmatic Programmer','Andy Hunt',        '978-0201616224',1,'Addison-Wesley',1999,2,TRUE, 'From Journeyman to Master'),
    ('Design Patterns',        'Gang of Four',     '978-0201633610',1,'Addison-Wesley',1994,1,FALSE,'Elements of Reusable Object-Oriented Software'),
    ('A Brief History of Time','Stephen Hawking',  '978-0553380163',2,'Bantam Books',   1988,2,TRUE, 'From the Big Bang to Black Holes'),
    ('The Great Gatsby',       'F. Scott Fitzgerald','978-0743273565',3,'Scribner',      1925,4,TRUE, 'A story of the fabulously wealthy Jay Gatsby'),
    ('To Kill a Mockingbird',  'Harper Lee',       '978-0061120084',3,'HarperCollins',  1960,3,TRUE, 'Winner of the Pulitzer Prize'),
    ('Introduction to Algorithms','Cormen et al.', '978-0262033848',5,'MIT Press',      2009,2,TRUE, 'Comprehensive algorithms textbook'),
    ('Database System Concepts','Silberschatz',    '978-0073523323',1,'McGraw-Hill',    2010,2,TRUE, 'Classic DBMS textbook')
ON CONFLICT (isbn) DO NOTHING;
