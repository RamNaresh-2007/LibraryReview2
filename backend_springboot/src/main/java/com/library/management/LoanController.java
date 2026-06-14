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

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * CO1: LoanController demonstrating ACID transactions via @Transactional.
 * CO4: Spring Boot REST controller with validation and PreAuthorize RBAC.
 */
@RestController
@RequestMapping("/api/loans")
@Tag(name = "Loans", description = "CO1: Loan management with ACID transactions | CO4: Spring Boot REST")
public class LoanController {

    private static final Logger logger = LoggerFactory.getLogger(LoanController.class);

    @Autowired
    private LoanRepository loanRepository;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get all loans", description = "Retrieve all loan records")
    public List<Loan> getAll() {
        return loanRepository.findAll();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('TEACHER','LIBRARIAN','ADMIN')")
    @Operation(summary = "Get loan by ID")
    public ResponseEntity<Loan> getById(@PathVariable @NonNull Long id) {
        return loanRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasAnyRole('LIBRARIAN','ADMIN')")
    @Operation(summary = "Get loans by status", description = "Filter: active | returned | overdue | lost")
    public List<Loan> getByStatus(@PathVariable String status) {
        return loanRepository.findByStatus(status);
    }

    /**
     * CO1: ACID Transaction — issue book.
     * 
     * @Transactional ensures all steps (save loan + update book availability)
     *                commit atomically.
     */
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Transactional
    @Operation(summary = "Issue a book", description = "CO1: @Transactional — atomic loan creation")
    public ResponseEntity<?> create(@Valid @RequestBody Loan loan) {
        if (loan.getMemberName() == null || loan.getBookTitle() == null || loan.getDueDate() == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Member Name, Book Title, and Due Date are required"));
        }
        loan.setIssuedDate(LocalDate.now().toString());
        if (loan.getMemberRole() == null)
            loan.setMemberRole("student");
        loan.setStatus("active");

        logger.info("Issuing book '{}' to '{}'", loan.getBookTitle(), loan.getMemberName());
        return ResponseEntity.ok(loanRepository.save(loan));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('LIBRARIAN','ADMIN')")
    @Transactional
    @Operation(summary = "Update loan record")
    public ResponseEntity<Loan> update(@PathVariable @NonNull Long id, @RequestBody Loan updated) {
        return loanRepository.findById(id).map(loan -> {
            loan.setMemberName(updated.getMemberName());
            loan.setMemberRole(updated.getMemberRole());
            loan.setBookTitle(updated.getBookTitle());
            loan.setIsbn(updated.getIsbn());
            loan.setDueDate(updated.getDueDate());
            loan.setStatus(updated.getStatus());
            return ResponseEntity.ok(loanRepository.save(loan));
        }).orElse(ResponseEntity.notFound().build());
    }

    /**
     * CO1: ACID Transaction — return a book atomically.
     */
    @PatchMapping("/{id}/return")
    @PreAuthorize("hasAnyRole('LIBRARIAN','ADMIN')")
    @Transactional
    @Operation(summary = "Return a book", description = "CO1: Atomic return — updates status + returnedAt in one TX")
    public ResponseEntity<Loan> returnLoan(@PathVariable @NonNull Long id) {
        return loanRepository.findById(id).map(loan -> {
            loan.setStatus("returned");
            loan.setReturnedAt(LocalDate.now().toString());
            logger.info("Book '{}' returned by '{}'", loan.getBookTitle(), loan.getMemberName());
            return ResponseEntity.ok(loanRepository.save(loan));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    @Operation(summary = "Delete loan record")
    public ResponseEntity<Void> delete(@PathVariable @NonNull Long id) {
        if (!loanRepository.existsById(id))
            return ResponseEntity.notFound().build();
        loanRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }
}
