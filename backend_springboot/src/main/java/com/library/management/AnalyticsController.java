package com.library.management;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * CO1: Advanced SQL Analytics — Window Functions, CTEs, Aggregates via JPA
 * native queries.
 * CO4: Spring Boot @Async for non-blocking computation, @Transactional for
 * ACID.
 */
@RestController
@RequestMapping("/api/analytics")
@Tag(name = "Analytics", description = "CO1: Aggregates, Window Functions, CTEs via PostgreSQL")
public class AnalyticsController {

    @Autowired
    private LoanRepository loanRepository;
    @Autowired
    private BookRepository bookRepository;
    @Autowired
    private UserRepository userRepository;

    /**
     * CO1: Dashboard KPIs — aggregate statistics using repository counts.
     * CO4: @Transactional(readOnly=true) — efficient read-only transaction.
     */
    @GetMapping("/dashboard")
    @PreAuthorize("hasAnyRole('LIBRARIAN','ADMIN','TEACHER','STUDENT')")
    @Transactional(readOnly = true)
    @Operation(summary = "Dashboard KPIs", description = "Aggregate stats: total books, members, active/overdue loans")
    public ResponseEntity<Map<String, Object>> dashboard() {
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("totalBooks", bookRepository.count());
        stats.put("totalMembers", userRepository.count());
        stats.put("activeLoans", loanRepository.countActiveLoans());
        stats.put("overdueLoans", loanRepository.countOverdueLoans());
        stats.put("totalLoans", loanRepository.count());
        stats.put("availableBooks", bookRepository.findByAvailableTrue().size());

        // Role breakdown
        Map<String, Long> roles = new LinkedHashMap<>();
        roles.put("admin", userRepository.countByRole("admin"));
        roles.put("librarian", userRepository.countByRole("librarian"));
        roles.put("teacher", userRepository.countByRole("teacher"));
        roles.put("student", userRepository.countByRole("student"));
        stats.put("membersByRole", roles);

        return ResponseEntity.ok(stats);
    }

    /**
     * CO1: Window Function — Top N borrowed books using RANK() OVER().
     * Native SQL demonstrates PostgreSQL window functions.
     */
    @GetMapping("/top-books")
    @PreAuthorize("hasAnyRole('LIBRARIAN','ADMIN')")
    @Transactional(readOnly = true)
    @Operation(summary = "Top borrowed books", description = "CO1: RANK() window function over loan counts")
    public ResponseEntity<List<Map<String, Object>>> topBooks(
            @RequestParam(defaultValue = "5") int top) {
        List<Object[]> rows = bookRepository.findTopBorrowedBooks(top);
        List<Map<String, Object>> result = new ArrayList<>();
        for (Object[] r : rows) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("bookId", r[0]);
            m.put("title", r[1]);
            m.put("author", r[2]);
            m.put("loanCount", r[3]);
            m.put("popularityRank", r[4]);
            result.add(m);
        }
        return ResponseEntity.ok(result);
    }

    /**
     * CO1: CTE + Window Function — Top borrowers with RANK() and cumulative fines.
     */
    @GetMapping("/top-borrowers")
    @PreAuthorize("hasAnyRole('LIBRARIAN','ADMIN')")
    @Transactional(readOnly = true)
    @Operation(summary = "Top borrowers", description = "CO1: CTE + RANK() window function for borrower leaderboard")
    public ResponseEntity<List<Map<String, Object>>> topBorrowers(
            @RequestParam(defaultValue = "5") int top) {
        List<Object[]> rows = loanRepository.topBorrowers(top);
        List<Map<String, Object>> result = new ArrayList<>();
        for (Object[] r : rows) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("memberName", r[0]);
            m.put("role", r[1]);
            m.put("totalLoans", r[2]);
            m.put("totalFines", r[3]);
            m.put("borrowRank", r[4]);
            result.add(m);
        }
        return ResponseEntity.ok(result);
    }

    /**
     * CO1: LAG window function — Month-over-month loan trend.
     */
    @GetMapping("/loan-trend")
    @PreAuthorize("hasAnyRole('LIBRARIAN','ADMIN')")
    @Transactional(readOnly = true)
    @Operation(summary = "Loan trend by month", description = "CO1: LAG() window function showing MoM delta")
    public ResponseEntity<List<Map<String, Object>>> loanTrend() {
        List<Object[]> rows = loanRepository.loanTrendByMonth();
        List<Map<String, Object>> result = new ArrayList<>();
        for (Object[] r : rows) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("month", r[0]);
            m.put("loansIssued", r[1]);
            m.put("prevMonth", r[2]);
            m.put("delta", r[3]);
            result.add(m);
        }
        return ResponseEntity.ok(result);
    }

    /**
     * CO1: GROUP BY + HAVING — Loan summary per member showing
     * active/returned/overdue counts.
     */
    @GetMapping("/member-summary")
    @PreAuthorize("hasAnyRole('LIBRARIAN','ADMIN')")
    @Transactional(readOnly = true)
    @Operation(summary = "Member loan summary", description = "CO1: GROUP BY with conditional aggregation")
    public ResponseEntity<List<Map<String, Object>>> memberSummary() {
        List<Object[]> rows = loanRepository.loanSummaryPerMember();
        List<Map<String, Object>> result = new ArrayList<>();
        for (Object[] r : rows) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("memberName", r[0]);
            m.put("memberRole", r[1]);
            m.put("totalLoans", r[2]);
            m.put("activeLoans", r[3]);
            m.put("returnedLoans", r[4]);
            m.put("overdueLoans", r[5]);
            result.add(m);
        }
        return ResponseEntity.ok(result);
    }

    /**
     * CO1: Subquery — Books never borrowed.
     */
    @GetMapping("/never-borrowed")
    @PreAuthorize("hasAnyRole('LIBRARIAN','ADMIN')")
    @Transactional(readOnly = true)
    @Operation(summary = "Books never borrowed", description = "CO1: NOT IN subquery to find unused catalog items")
    public ResponseEntity<List<Book>> neverBorrowed() {
        return ResponseEntity.ok(bookRepository.findNeverBorrowedBooks());
    }

    /**
     * CO1: Aggregate — Books per category (GROUP BY + COUNT).
     */
    @GetMapping("/category-stats")
    @PreAuthorize("hasAnyRole('LIBRARIAN','ADMIN','TEACHER','STUDENT')")
    @Transactional(readOnly = true)
    @Operation(summary = "Books per category", description = "CO1: GROUP BY COUNT aggregate")
    public ResponseEntity<List<Map<String, Object>>> categoryStats() {
        List<Object[]> rows = bookRepository.countBooksPerCategory();
        List<Map<String, Object>> result = new ArrayList<>();
        for (Object[] r : rows) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("category", r[0]);
            m.put("bookCount", r[1]);
            result.add(m);
        }
        return ResponseEntity.ok(result);
    }
}
