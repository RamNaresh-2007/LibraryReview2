package com.library.management;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * CO1: Seeds all tables including the normalised Category table.
 * CO4: Spring CommandLineRunner — executed after ApplicationContext is ready.
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
        private CategoryRepository categoryRepository;
        @Autowired
        private PasswordEncoder passwordEncoder;

        @Override
        public void run(String... args) {
                seedCategories();
                seedUsers();
                if (bookRepository.count() == 0) {
                        seedBooks();
                        seedLoans();
                }
                logger.info("✅ Database seed completed — CO1 schema ready");
        }

        // CO1: Normalised table seed (extracted repeating group → Category entity)
        private void seedCategories() {
                List<String[]> cats = List.of(
                                new String[] { "Software", "Computer science and software engineering" },
                                new String[] { "Science", "Natural and applied sciences" },
                                new String[] { "Fiction", "Fiction and literary works" },
                                new String[] { "History", "Historical texts and biographies" },
                                new String[] { "Mathematics", "Pure and applied mathematics" });
                for (String[] c : cats) {
                        if (!categoryRepository.existsByName(c[0])) {
                                categoryRepository.save(new Category(c[0], c[1]));
                        }
                }
                logger.info("Categories seeded.");
        }

        private void seedUsers() {
                upsert("admin", "System Administrator", "admin@library.com", "admin123", "admin", "2024-01-01", 0);
                upsert("admin2", "Secondary Admin", "admin2@library.com", "admin123", "admin", "2024-01-02", 0);
                upsert("librarian", "Main Librarian", "lib@library.com", "lib123", "librarian", "2024-01-05", 2);
                upsert("alice_lib", "Alice Librarian", "alice@library.com", "lib123", "librarian", "2024-02-01", 0);
                upsert("priya", "Priya Sharma", "priya@school.com", "teacher123", "teacher", "2024-02-10", 1);
                upsert("mr_smith", "John Smith", "smith@school.com", "teacher123", "teacher", "2024-03-01", 0);
                upsert("mrs_davis", "Sarah Davis", "davis@school.com", "teacher123", "teacher", "2024-03-02", 0);
                upsert("rahul", "Rahul Kumar", "rahul@student.com", "student123", "student", "2024-03-15", 0);
                upsert("john_doe", "John Doe", "john@student.com", "student123", "student", "2024-04-01", 1);
                upsert("jane_doe", "Jane Doe", "jane@student.com", "student123", "student", "2024-04-02", 0);
                upsert("timmy", "Timmy Turner", "timmy@student.com", "student123", "student", "2024-04-03", 0);
                upsert("dr_who", "The Doctor", "doctor@school.com", "teacher123", "teacher", "2024-03-05", 0);
                upsert("emily", "Emily Clark", "emily@student.com", "student123", "student", "2024-04-10", 0);
                logger.info("Default users synchronized.");
        }

        private void upsert(String username, String fullname, String email,
                        String password, String role, String joinedDate, int loans) {
                User user = userRepository.findByUsername(username).orElse(new User());
                user.setUsername(username);
                user.setFullname(fullname);
                user.setEmail(email);
                user.setPassword(passwordEncoder.encode(password));
                user.setRole(role);
                user.setStatus("active");
                user.setJoinedDate(joinedDate);
                user.setLoans(loans);
                userRepository.save(user);
        }

        private void seedBooks() {
                Category sw = categoryRepository.findByName("Software").orElse(null);
                Category sci = categoryRepository.findByName("Science").orElse(null);
                Category fic = categoryRepository.findByName("Fiction").orElse(null);
                Category math = categoryRepository.findByName("Mathematics").orElse(null);

                List<Book> books = List.of(
                                mkBook("Clean Code", "Robert C. Martin", "978-0132350884", sw, "Prentice Hall", 2008, 3,
                                                true,
                                                "A Handbook of Agile Software Craftsmanship"),
                                mkBook("The Pragmatic Programmer", "Andy Hunt", "978-0201616224", sw, "Addison-Wesley",
                                                1999, 2, true,
                                                "From Journeyman to Master"),
                                mkBook("Design Patterns", "Gang of Four", "978-0201633610", sw, "Addison-Wesley", 1994,
                                                1, false,
                                                "Elements of Reusable Object-Oriented Software"),
                                mkBook("A Brief History of Time", "Stephen Hawking", "978-0553380163", sci,
                                                "Bantam Books", 1988, 2, true,
                                                "From the Big Bang to Black Holes"),
                                mkBook("The Selfish Gene", "Richard Dawkins", "978-0192860927", sci, "Oxford Press",
                                                1976, 2, true, "Evolutionary biology masterpiece"),
                                mkBook("The Great Gatsby", "F. Scott Fitzgerald", "978-0743273565", fic, "Scribner",
                                                1925, 4, true,
                                                "A story of the fabulously wealthy Jay Gatsby"),
                                mkBook("To Kill a Mockingbird", "Harper Lee", "978-0061120084", fic, "HarperCollins",
                                                1960, 3, true,
                                                "Winner of the Pulitzer Prize"),
                                mkBook("1984", "George Orwell", "978-0451524935", fic, "Signet Classic",
                                                1949, 5, true, "Dystopian social science fiction novel"),
                                mkBook("Introduction to Algorithms", "Cormen et al.", "978-0262033848", math,
                                                "MIT Press", 2009, 2, true,
                                                "Comprehensive algorithms textbook"),
                                mkBook("Calculus", "James Stewart", "978-1285740621", math, "Cengage",
                                                2015, 3, true, "Essential calculus textbook"),
                                mkBook("Database System Concepts", "Silberschatz", "978-0073523323", sw, "McGraw-Hill",
                                                2010, 2, true,
                                                "Classic DBMS textbook"),
                                mkBook("Sapiens", "Yuval Noah Harari", "978-0062316097", categoryRepository.findByName("History").orElse(null), "Harper",
                                                2015, 4, true, "A brief history of humankind"),
                                mkBook("Guns, Germs, and Steel", "Jared Diamond", "978-0393317558", categoryRepository.findByName("History").orElse(null), "W. W. Norton",
                                                1997, 2, true, "The fates of human societies"),
                                mkBook("The Lord of the Rings", "J.R.R. Tolkien", "978-0544003415", fic, "Houghton Mifflin", 1954, 5, true, "Epic high-fantasy novel"),
                                mkBook("Pride and Prejudice", "Jane Austen", "978-0141439518", fic, "Penguin Classics", 1813, 3, true, "Classic romance novel"),
                                mkBook("The Code Breaker", "Walter Isaacson", "978-1982115852", sci, "Simon & Schuster", 2021, 2, true, "Jennifer Doudna, Gene Editing, and the Future of the Human Race"),
                                mkBook("Thinking, Fast and Slow", "Daniel Kahneman", "978-0374533557", sci, "Farrar, Straus and Giroux", 2011, 4, true, "Psychology and behavioral economics"),
                                mkBook("Artificial Intelligence", "Stuart Russell", "978-0134610993", sw, "Pearson", 2020, 2, true, "A Modern Approach"),
                                mkBook("Structure and Interpretation of Computer Programs", "Harold Abelson", "978-0262510875", sw, "MIT Press", 1996, 1, true, "Classic CS textbook")
                );
                bookRepository.saveAll(books);
                logger.info("Sample books seeded with Category FK (CO1: normalised).");
        }

        private Book mkBook(String title, String author, String isbn, Category cat,
                        String publisher, int year, int copies, boolean available, String desc) {
                Book b = new Book();
                b.setTitle(title);
                b.setAuthor(author);
                b.setIsbn(isbn);
                b.setCategory(cat);
                b.setPublisher(publisher);
                b.setPublishYear(year);
                b.setCopies(copies);
                b.setAvailable(available);
                b.setDescription(desc);
                return b;
        }

        private void seedLoans() {
                loanRepository.saveAll(List.of(
                                mkLoan("Priya Sharma", "teacher", "Design Patterns", "978-0201633610", "2024-05-10",
                                                "2024-05-24", "active"),
                                mkLoan("Main Librarian", "librarian", "Clean Code", "978-0132350884", "2024-05-15",
                                                "2024-05-22", "returned"),
                                mkLoan("John Doe", "student", "A Brief History of Time", "978-0553380163", "2024-04-01",
                                                "2024-04-15", "overdue"),
                                mkLoan("Jane Doe", "student", "Pride and Prejudice", "978-0141439518", "2024-05-20", "2024-06-03", "active"),
                                mkLoan("Rahul Kumar", "student", "The Code Breaker", "978-1982115852", "2024-05-21", "2024-06-04", "active"),
                                mkLoan("Timmy Turner", "student", "The Great Gatsby", "978-0743273565", "2024-03-10", "2024-03-24", "lost")
                ));
                logger.info("Sample loan records seeded.");
        }

        private Loan mkLoan(String member, String role, String book, String isbn,
                        String issued, String due, String status) {
                return new Loan(null, member, role, book, isbn, issued, due, status);
        }
}
