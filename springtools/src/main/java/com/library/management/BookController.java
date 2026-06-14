package com.library.management;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;
import java.util.Map;

/**
 * REST Controller for managing Library Books.
 */
@RestController
@RequestMapping("/api/books")
@Tag(name = "Books", description = "Endpoints for library catalog management")
public class BookController {

    private static final Logger logger = LoggerFactory.getLogger(BookController.class);

    @Autowired
    private BookRepository bookRepository;

    @GetMapping
    @PreAuthorize("hasAnyRole('STUDENT','TEACHER','LIBRARIAN','ADMIN')")
    @Operation(summary = "Get all books", description = "Retrieves the full library catalog")
    public List<Book> getAll() {
        return bookRepository.findAll();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('STUDENT','TEACHER','LIBRARIAN','ADMIN')")
    @Operation(summary = "Get book by ID", description = "Retrieves details of a specific book")
    public ResponseEntity<Book> getById(@PathVariable @NonNull Long id) {
        return bookRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('LIBRARIAN','ADMIN')")
    @Operation(summary = "Add new book", description = "Adds a new book to the library")
    public ResponseEntity<?> create(@RequestBody Book book) {
        logger.info("Adding new book: {}", book.getTitle());
        if (book.getTitle() == null)
            return ResponseEntity.badRequest().body(Map.of("message", "Title is required"));
        return ResponseEntity.ok(bookRepository.save(book));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('LIBRARIAN','ADMIN')")
    @Operation(summary = "Update book details", description = "Updates title, author, or availability of a book")
    public ResponseEntity<Book> update(@PathVariable @NonNull Long id, @RequestBody Book updated) {
        return bookRepository.findById(id).map(book -> {
            book.setTitle(updated.getTitle());
            book.setAuthor(updated.getAuthor());
            book.setIsbn(updated.getIsbn());
            book.setCategory(updated.getCategory());
            book.setAvailable(updated.isAvailable());
            return ResponseEntity.ok(bookRepository.save(book));
        }).orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/{id}/toggle-availability")
    @PreAuthorize("hasAnyRole('LIBRARIAN','ADMIN')")
    @Operation(summary = "Toggle availability", description = "Quickly switch book between Available and Not Available")
    public ResponseEntity<Book> toggle(@PathVariable @NonNull Long id) {
        return bookRepository.findById(id).map(book -> {
            book.setAvailable(!book.isAvailable());
            return ResponseEntity.ok(bookRepository.save(book));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete book", description = "Permanently removes a book from the catalog")
    public ResponseEntity<Void> delete(@PathVariable @NonNull Long id) {
        if (!bookRepository.existsById(id))
            return ResponseEntity.notFound().build();
        bookRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }
}
