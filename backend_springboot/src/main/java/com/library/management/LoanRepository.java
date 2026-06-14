package com.library.management;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * CO1: LoanRepository with rich JPQL demonstrating JOINs, aggregates,
 * CTEs (via native SQL), and window functions.
 * CO4: Spring Data JPA repository pattern.
 */
@Repository
public interface LoanRepository extends JpaRepository<Loan, Long> {

    List<Loan> findByStatus(String status);

    List<Loan> findByMemberNameContainingIgnoreCase(String name);

    List<Loan> findByUserId(Long userId);

    // CO1: JOIN + filter (active loans for a specific user)
    @Query("SELECT l FROM Loan l JOIN l.user u WHERE u.username = :username ORDER BY l.issuedDate DESC")
    List<Loan> findActiveByUsername(@Param("username") String username);

    // CO1: Aggregate — total loans per user (GROUP BY with HAVING)
    @Query("""
            SELECT l.memberName, l.memberRole, COUNT(l) AS total,
                   SUM(CASE WHEN l.status != 'returned' THEN 1 ELSE 0 END) AS active,
                   SUM(CASE WHEN l.status = 'returned' THEN 1 ELSE 0 END) AS returned,
                   SUM(CASE WHEN l.status != 'returned' AND l.dueDate < CAST(CURRENT_DATE AS string) THEN 1 ELSE 0 END) AS overdue
            FROM Loan l
            GROUP BY l.memberName, l.memberRole
            ORDER BY total DESC
            """)
    List<Object[]> loanSummaryPerMember();

    // CO1: Overdue loans with fine calculation
    @Query("SELECT l FROM Loan l WHERE l.status = 'active' AND l.dueDate < CAST(CURRENT_DATE AS string)")
    List<Loan> findOverdueLoans();

    // CO1: Native SQL — window function ROW_NUMBER + LAG (trend)
    @Query(value = """
            SELECT month, loans_issued,
                   LAG(loans_issued) OVER (ORDER BY month) AS prev_month,
                   loans_issued - LAG(loans_issued) OVER (ORDER BY month) AS delta
            FROM (
                SELECT TO_CHAR(CAST(issued_date AS DATE), 'YYYY-MM') AS month,
                       COUNT(*) AS loans_issued
                FROM loans
                GROUP BY TO_CHAR(CAST(issued_date AS DATE), 'YYYY-MM')
            ) trend
            ORDER BY month
            """, nativeQuery = true)
    List<Object[]> loanTrendByMonth();

    // CO1: CTE simulation — top borrowers
    @Query(value = """
            WITH borrower_stats AS (
                SELECT user_id, COUNT(*) AS total,
                       SUM(CASE WHEN status != 'returned' AND due_date < CAST(CURRENT_DATE AS text) THEN fine_amount ELSE 0 END) AS total_fines
                FROM loans
                WHERE user_id IS NOT NULL
                GROUP BY user_id
            )
            SELECT u.fullname, u.role, bs.total, bs.total_fines,
                   RANK() OVER (ORDER BY bs.total DESC) AS borrow_rank
            FROM borrower_stats bs
            JOIN users u ON u.id = bs.user_id
            ORDER BY borrow_rank
            LIMIT :topN
            """, nativeQuery = true)
    List<Object[]> topBorrowers(@Param("topN") int topN);

    // CO1: Stats for dashboard
    @Query("SELECT COUNT(l) FROM Loan l WHERE l.status != 'returned'")
    long countActiveLoans();

    @Query("SELECT COUNT(l) FROM Loan l WHERE l.status != 'returned' AND l.dueDate < CAST(CURRENT_DATE AS string)")
    long countOverdueLoans();
}
