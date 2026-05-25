package com.library.management;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LoanRepository extends JpaRepository<Loan, Long> {
    List<Loan> findByStatus(String status);

    List<Loan> findByMemberNameContainingIgnoreCase(String name);
}
