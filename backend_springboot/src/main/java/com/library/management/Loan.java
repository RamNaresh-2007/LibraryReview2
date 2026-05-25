package com.library.management;

import jakarta.persistence.*;

@Entity
@Table(name = "loans")
public class Loan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String memberName;

    @Column(nullable = false)
    private String memberRole;

    @Column(nullable = false)
    private String bookTitle;

    private String isbn;

    @Column(nullable = false)
    private String issuedDate;

    @Column(nullable = false)
    private String dueDate;

    @Column(nullable = false)
    private String status = "active";

    public Loan() {
    }

    public Loan(Long id, String memberName, String memberRole, String bookTitle, String isbn,
            String issuedDate, String dueDate, String status) {
        this.id = id;
        this.memberName = memberName;
        this.memberRole = memberRole;
        this.bookTitle = bookTitle;
        this.isbn = isbn;
        this.issuedDate = issuedDate;
        this.dueDate = dueDate;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMemberName() {
        return memberName;
    }

    public void setMemberName(String memberName) {
        this.memberName = memberName;
    }

    public String getMemberRole() {
        return memberRole;
    }

    public void setMemberRole(String memberRole) {
        this.memberRole = memberRole;
    }

    public String getBookTitle() {
        return bookTitle;
    }

    public void setBookTitle(String bookTitle) {
        this.bookTitle = bookTitle;
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

    public void setIssuedDate(String issuedDate) {
        this.issuedDate = issuedDate;
    }

    public String getDueDate() {
        return dueDate;
    }

    public void setDueDate(String dueDate) {
        this.dueDate = dueDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
