package com.library.management;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * REST Controller for managing Book Loans (Issues/Returns).
 */
@RestController
@RequestMapping("/api/loans")
public class LoanController {

    private static final Logger logger = LoggerFactory.getLogger(LoanController.class);

    @Autowired
    private LoanRepository loanRepository;

    @GetMapping
    @PreAuthorize("hasAnyRole('STUDENT','TEACHER','LIBRARIAN','ADMIN')")
    public List<Loan> getAll() {
        return loanRepository.findAll();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('STUDENT','TEACHER','LIBRARIAN','ADMIN')")
    public ResponseEntity<Loan> getById(@PathVariable @NonNull Long id) {
        return loanRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('LIBRARIAN','ADMIN')")
    public ResponseEntity<?> create(@RequestBody Loan loan) {
        if (loan.getMemberName() == null || loan.getBookTitle() == null || loan.getDueDate() == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Member Name, Book Title, and Due Date are required"));
        }

        loan.setIssuedDate(LocalDate.now().toString());
        if (loan.getMemberRole() == null)
            loan.setMemberRole("student");
        loan.setStatus("active");

        logger.info("Issuing book: {} to {}", loan.getBookTitle(), loan.getMemberName());
        return ResponseEntity.ok(loanRepository.save(loan));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('LIBRARIAN','ADMIN')")
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

    @PatchMapping("/{id}/return")
    @PreAuthorize("hasAnyRole('LIBRARIAN','ADMIN')")
    public ResponseEntity<Loan> returnLoan(@PathVariable @NonNull Long id) {
        return loanRepository.findById(id).map(loan -> {
            loan.setStatus("returned");
            logger.info("Book returned: {}", loan.getBookTitle());
            return ResponseEntity.ok(loanRepository.save(loan));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable @NonNull Long id) {
        if (!loanRepository.existsById(id))
            return ResponseEntity.notFound().build();
        loanRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }
}
