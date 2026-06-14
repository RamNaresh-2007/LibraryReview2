package com.library.management;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * CO1: BookController with search, filter, and catalog management.
 * CO4: Spring Boot REST — IoC-managed, validation, RBAC via PreAuthorize.
 */
@RestController
@RequestMapping("/api/books")
@Tag(name = "Books", description = "Library catalog management | CO1: CRUD + Search | CO4: Spring Boot")
public class BookController {

    private static final Logger logger = LoggerFactory.getLogger(BookController.class);

    @Autowired
    private BookRepository bookRepository;
    @Autowired
    private CategoryRepository categoryRepository;

    @GetMapping
    @PreAuthorize("hasAnyRole('STUDENT','TEACHER','LIBRARIAN','ADMIN')")
    @Operation(summary = "Get all books", description = "Returns the full library catalog")
    public List<Book> getAll() {
        return bookRepository.findAll();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('STUDENT','TEACHER','LIBRARIAN','ADMIN')")
    @Operation(summary = "Get book by ID")
    public ResponseEntity<Book> getById(@PathVariable @NonNull Long id) {
        return bookRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('STUDENT','TEACHER','LIBRARIAN','ADMIN')")
    @Operation(summary = "Full-text book search", description = "CO1: Search across title, author, and ISBN")
    public List<Book> search(@RequestParam String q) {
        return bookRepository.fullTextSearch(q);
    }

    @GetMapping("/available")
    @PreAuthorize("hasAnyRole('STUDENT','TEACHER','LIBRARIAN','ADMIN')")
    @Operation(summary = "Get available books", description = "Index-backed filter on available=true")
    public List<Book> getAvailable() {
        return bookRepository.findByAvailableTrue();
    }

    @GetMapping("/category/{name}")
    @PreAuthorize("hasAnyRole('STUDENT','TEACHER','LIBRARIAN','ADMIN')")
    @Operation(summary = "Books by category", description = "CO1: JOIN query through Category FK")
    public List<Book> getByCategory(@PathVariable String name) {
        return bookRepository.findByCategoryName(name);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('LIBRARIAN','ADMIN')")
    @Transactional
    @Operation(summary = "Add new book", description = "CO1: @Transactional — atomic catalog insert")
    public ResponseEntity<?> create(@Valid @RequestBody Book book) {
        if (book.getTitle() == null || book.getTitle().isBlank())
            return ResponseEntity.badRequest().body(Map.of("message", "Title is required"));

        // Resolve category by name if provided as String
        if (book.getCategory() == null && book.getCategoryName() != null) {
            categoryRepository.findByName(book.getCategoryName())
                    .ifPresent(book::setCategory);
        }

        logger.info("Adding book: {} by {}", book.getTitle(), book.getAuthor());
        return ResponseEntity.ok(bookRepository.save(book));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('LIBRARIAN','ADMIN')")
    @Transactional
    @Operation(summary = "Update book", description = "Updates all book fields atomically")
    public ResponseEntity<Book> update(@PathVariable @NonNull Long id, @RequestBody Book updated) {
        return bookRepository.findById(id).map(book -> {
            book.setTitle(updated.getTitle());
            book.setAuthor(updated.getAuthor());
            book.setIsbn(updated.getIsbn());
            book.setPublisher(updated.getPublisher());
            book.setPublishYear(updated.getPublishYear());
            book.setCopies(updated.getCopies());
            book.setAvailable(updated.isAvailable());
            book.setDescription(updated.getDescription());
            if (updated.getCategory() != null) {
                book.setCategory(updated.getCategory());
            } else if (updated.getCategoryName() != null) {
                categoryRepository.findByName(updated.getCategoryName())
                        .ifPresent(book::setCategory);
            }
            return ResponseEntity.ok(bookRepository.save(book));
        }).orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/{id}/toggle-availability")
    @PreAuthorize("hasAnyRole('LIBRARIAN','ADMIN')")
    @Transactional
    @Operation(summary = "Toggle availability", description = "Switch book between Available / Not Available")
    public ResponseEntity<Book> toggle(@PathVariable @NonNull Long id) {
        return bookRepository.findById(id).map(book -> {
            book.setAvailable(!book.isAvailable());
            return ResponseEntity.ok(bookRepository.save(book));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    @Operation(summary = "Delete book", description = "Permanently removes book from catalog")
    public ResponseEntity<Void> delete(@PathVariable @NonNull Long id) {
        if (!bookRepository.existsById(id))
            return ResponseEntity.notFound().build();
        bookRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }
}
