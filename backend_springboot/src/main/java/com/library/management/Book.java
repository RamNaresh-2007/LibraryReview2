package com.library.management;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;

/**
 * CO1: Entity — 3NF-compliant Book with Category FK (normalised repeating
 * group).
 * CO4: Spring Boot JPA entity with Bean Validation.
 */
@Entity
@Table(name = "books", indexes = {
        @Index(name = "idx_books_author", columnList = "author"),
        @Index(name = "idx_books_category", columnList = "category_id"),
        @Index(name = "idx_books_available", columnList = "available")
})
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @NotBlank(message = "Title is required")
    private String title;

    @Column(nullable = false)
    @NotBlank(message = "Author is required")
    private String author;

    @Column(unique = true, length = 20)
    private String isbn;

    // ── Category FK (CO1: ER Modelling — Many Books to One Category) ────────
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "category_id")
    @JsonIgnoreProperties("books")
    private Category category;

    // ── Legacy string field for backward compatibility ───────────────────────
    @Transient
    private String categoryName;

    private String publisher;

    @Column(name = "publish_year")
    @Min(value = 1000, message = "Invalid publish year")
    @Max(value = 2100, message = "Invalid publish year")
    private Integer publishYear;

    @Column(nullable = false)
    @Min(value = 0, message = "Copies cannot be negative")
    private int copies = 1;

    @Column(nullable = false)
    private boolean available = true;

    @Column(columnDefinition = "TEXT")
    private String description;

    public Book() {
    }

    public Book(Long id, String title, String author, String isbn,
            String categoryName, boolean available) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.isbn = isbn;
        this.categoryName = categoryName;
        this.available = available;
        this.copies = available ? 1 : 0;
    }

    // ── Getters / Setters ───────────────────────────────────────────────────
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category c) {
        this.category = c;
    }

    public String getCategoryName() {
        return category != null ? category.getName() : categoryName;
    }

    public void setCategoryName(String n) {
        this.categoryName = n;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String p) {
        this.publisher = p;
    }

    public Integer getPublishYear() {
        return publishYear;
    }

    public void setPublishYear(Integer y) {
        this.publishYear = y;
    }

    public int getCopies() {
        return copies;
    }

    public void setCopies(int copies) {
        this.copies = copies;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String d) {
        this.description = d;
    }
}
