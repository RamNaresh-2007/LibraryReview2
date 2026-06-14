package com.library.management;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

/**
 * CO1: Loan entity — linking table between Users and Books.
 * Demonstrates FK relationships, CHECK constraints, and ACID transactions.
 * CO4: JPA entity with Bean Validation.
 */
@Entity
@Table(name = "loans", indexes = {
        @Index(name = "idx_loans_user_id", columnList = "user_id"),
        @Index(name = "idx_loans_book_id", columnList = "book_id"),
        @Index(name = "idx_loans_status", columnList = "status"),
        @Index(name = "idx_loans_due_date", columnList = "due_date")
})
public class Loan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ── FK relationships (CO1: Relational link) ──────────────────────────────
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id")
    @JsonIgnoreProperties({ "loanHistory", "password", "authorities",
            "accountNonExpired", "accountNonLocked",
            "credentialsNonExpired", "enabled" })
    private User user;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "book_id")
    @JsonIgnoreProperties({ "books" })
    private Book book;

    // ── Denormalised cache fields for display without joins ──────────────────
    @Column(nullable = false)
    @NotBlank(message = "Member name is required")
    private String memberName;

    @Column(nullable = false)
    private String memberRole = "student";

    @Column(nullable = false)
    @NotBlank(message = "Book title is required")
    private String bookTitle;

    private String isbn;

    @Column(nullable = false)
    private String issuedDate;

    @Column(nullable = false)
    @NotBlank(message = "Due date is required")
    private String dueDate;

    private String returnedAt;

    @Column(nullable = false)
    @Pattern(regexp = "active|returned|overdue|lost", message = "Status must be active, returned, overdue, or lost")
    private String status = "active";

    @Column(precision = 8, scale = 2)
    private BigDecimal fineAmount = BigDecimal.ZERO;

    public Loan() {
    }

    public Loan(Long id, String memberName, String memberRole, String bookTitle,
            String isbn, String issuedDate, String dueDate, String status) {
        this.id = id;
        this.memberName = memberName;
        this.memberRole = memberRole;
        this.bookTitle = bookTitle;
        this.isbn = isbn;
        this.issuedDate = issuedDate;
        this.dueDate = dueDate;
        this.status = status;
    }

    // ── Getters / Setters ────────────────────────────────────────────────────
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User u) {
        this.user = u;
    }

    public Book getBook() {
        return book;
    }

    public void setBook(Book b) {
        this.book = b;
    }

    public String getMemberName() {
        return memberName;
    }

    public void setMemberName(String n) {
        this.memberName = n;
    }

    public String getMemberRole() {
        return memberRole;
    }

    public void setMemberRole(String r) {
        this.memberRole = r;
    }

    public String getBookTitle() {
        return bookTitle;
    }

    public void setBookTitle(String t) {
        this.bookTitle = t;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public String getIssuedDate() {
        return issuedDate;
    }

    public void setIssuedDate(String d) {
        this.issuedDate = d;
    }

    public String getDueDate() {
        return dueDate;
    }

    public void setDueDate(String d) {
        this.dueDate = d;
    }

    public String getReturnedAt() {
        return returnedAt;
    }

    public void setReturnedAt(String r) {
        this.returnedAt = r;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public BigDecimal getFineAmount() {
        return fineAmount;
    }

    public void setFineAmount(BigDecimal f) {
        this.fineAmount = f;
    }
}
