package com.library.management;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Seeds the database with default users, books, and loans on startup.
 */
@Component
public class DataInitializer implements CommandLineRunner {

        private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

        @Autowired
        private UserRepository userRepository;
        @Autowired
        private BookRepository bookRepository;
        @Autowired
        private LoanRepository loanRepository;
        @Autowired
        private PasswordEncoder passwordEncoder;

        @Override
        public void run(String... args) {
                if (userRepository.count() == 0) {
                        seedUsers();
                        seedBooks();
                        seedLoans();
                        logger.info("Database seeding completed successfully ✅");
                }
        }

        private void seedUsers() {
                User admin = new User(null, "admin", "System Administrator", "admin@library.com",
                                passwordEncoder.encode("admin123"), "admin", "active", "2024-01-01", 0);

                User lib = new User(null, "librarian", "Main Librarian", "lib@library.com",
                                passwordEncoder.encode("lib123"), "librarian", "active", "2024-01-05", 2);

                User teacher = new User(null, "priya", "Priya Sharma", "priya@school.com",
                                passwordEncoder.encode("teacher123"), "teacher", "active", "2024-02-10", 1);

                User student = new User(null, "rahul", "Rahul Kumar", "rahul@student.com",
                                passwordEncoder.encode("student123"), "student", "active", "2024-03-15", 0);

                userRepository.saveAll(List.of(admin, lib, teacher, student));
                logger.info("Default users created: admin/admin123, librarian/lib123, priya/teacher123, rahul/student123");
        }

        private void seedBooks() {
                bookRepository.saveAll(List.of(
                                new Book(null, "Clean Code", "Robert C. Martin", "978-0132350884", "Software", true),
                                new Book(null, "The Pragmatic Programmer", "Andy Hunt", "978-0201616224", "Software",
                                                true),
                                new Book(null, "Design Patterns", "Gang of Four", "978-0201633610", "Software", false),
                                new Book(null, "A Brief History of Time", "Stephen Hawking", "978-0553380163",
                                                "Science", true),
                                new Book(null, "The Great Gatsby", "F. Scott Fitzgerald", "978-0743273565", "Fiction",
                                                true),
                                new Book(null, "To Kill a Mockingbird", "Harper Lee", "978-0061120084", "Fiction",
                                                true)));
                logger.info("Sample books catalog seeded.");
        }

        private void seedLoans() {
                loanRepository.saveAll(List.of(
                                new Loan(null, "Priya Sharma", "teacher", "Design Patterns", "978-0201633610",
                                                "2024-05-10", "2024-05-24", "active"),
                                new Loan(null, "Main Librarian", "librarian", "Clean Code", "978-0132350884",
                                                "2024-05-15", "2024-05-22", "returned")));
                logger.info("Sample loan records seeded.");
        }
}
