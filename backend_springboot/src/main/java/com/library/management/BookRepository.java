package com.library.management;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * CO1: JPA Repository with custom JPQL — demonstrates JOINs, aggregates,
 * subqueries.
 * CO4: Spring Data JPA — derived query methods.
 */
@Repository
public interface BookRepository extends JpaRepository<Book, Long> {

    // CO1: Derived query — index-backed search
    List<Book> findByAvailableTrue();

    List<Book> findByAuthorContainingIgnoreCase(String author);

    List<Book> findByTitleContainingIgnoreCase(String title);

    Optional<Book> findByIsbn(String isbn);

    // CO1: JPQL JOIN with Category (demonstrates ER relationship traversal)
    @Query("SELECT b FROM Book b JOIN b.category c WHERE c.name = :categoryName")
    List<Book> findByCategoryName(@Param("categoryName") String categoryName);

    // CO1: Aggregate — count books per category (GROUP BY)
    @Query("SELECT b.category.name, COUNT(b) FROM Book b WHERE b.category IS NOT NULL GROUP BY b.category.name ORDER BY COUNT(b) DESC")
    List<Object[]> countBooksPerCategory();

    // CO1: Subquery — books never borrowed
    @Query("SELECT b FROM Book b WHERE b.id NOT IN (SELECT DISTINCT l.book.id FROM Loan l WHERE l.book IS NOT NULL)")
    List<Book> findNeverBorrowedBooks();

    // CO1: Full-text style search across title + author
    @Query("SELECT b FROM Book b WHERE " +
            "LOWER(b.title) LIKE LOWER(CONCAT('%',:q,'%')) OR " +
            "LOWER(b.author) LIKE LOWER(CONCAT('%',:q,'%')) OR " +
            "LOWER(b.isbn) LIKE LOWER(CONCAT('%',:q,'%'))")
    List<Book> fullTextSearch(@Param("q") String query);

    // CO1: Native SQL — window function RANK
    @Query(value = """
            SELECT b.id, b.title, b.author,
                   COUNT(l.id) AS loan_count,
                   RANK() OVER (ORDER BY COUNT(l.id) DESC) AS popularity_rank
            FROM books b
            LEFT JOIN loans l ON l.book_id = b.id
            GROUP BY b.id, b.title, b.author
            ORDER BY popularity_rank
            LIMIT :topN
            """, nativeQuery = true)
    List<Object[]> findTopBorrowedBooks(@Param("topN") int topN);
}
