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
                seedUsers(); // Force sync existing users to plain-text
                if (bookRepository.count() == 0) {
                        seedBooks();
                        seedLoans();
                }
                logger.info("Database synchronization completed successfully ✅");
        }

        private void seedUsers() {
                updateUser("admin", "System Administrator", "admin@library.com", "admin123", "admin", "2024-01-01", 0);
                updateUser("librarian", "Main Librarian", "lib@library.com", "lib123", "librarian", "2024-01-05", 2);
                updateUser("priya", "Priya Sharma", "priya@school.com", "teacher123", "teacher", "2024-02-10", 1);
                updateUser("rahul", "Rahul Kumar", "rahul@student.com", "student123", "student", "2024-03-15", 0);
                logger.info("Default users synchronized to plain-text passwords.");
        }

        private void updateUser(String username, String fullname, String email, String password, String role,
                        String joinedDate, int loans) {
                User user = userRepository.findByUsername(username).orElse(new User());
                user.setUsername(username);
                user.setFullname(fullname);
                user.setEmail(email);
                user.setPassword(passwordEncoder.encode(password)); // Now NoOp (Plain-text)
                user.setRole(role);
                user.setStatus("active");
                user.setJoinedDate(joinedDate);
                user.setLoans(loans);
                userRepository.save(user);
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
