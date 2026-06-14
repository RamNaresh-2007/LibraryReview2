import express from 'express';
import * as bookService from '../services/bookService.js';
import { verifyToken } from '../middleware/auth.js';

const router = express.Router();

// Create a new book
router.post("/", verifyToken, async (req, res) => {
    try {
        const newBook = await bookService.createBook(req.body);
        res.status(201).json({ code: 201, message: "Book created successfully", data: newBook });
    } catch (error) {
        res.status(500).json({ code: 500, message: "Failed to create book", error: error.message });
    }
});

// Get all books
router.get("/", async (req, res) => {
    try {
        const books = await bookService.getAllBooks();
        res.status(200).json({ code: 200, message: "Books retrieved successfully", data: books });
    } catch (error) {
        res.status(500).json({ code: 500, message: "Failed to retrieve books", error: error.message });
    }
});

// Get a single book by ID
router.get("/:id", async (req, res) => {
    try {
        const book = await bookService.getBookById(req.params.id);
        if (!book) {
            return res.status(404).json({ code: 404, message: "Book not found" });
        }
        res.status(200).json({ code: 200, message: "Book retrieved successfully", data: book });
    } catch (error) {
        res.status(500).json({ code: 500, message: "Failed to retrieve book", error: error.message });
    }
});

// Update a book
router.put("/:id", verifyToken, async (req, res) => {
    try {
        const updatedBook = await bookService.updateBook(req.params.id, req.body);
        if (!updatedBook) {
            return res.status(404).json({ code: 404, message: "Book not found" });
        }
        res.status(200).json({ code: 200, message: "Book updated successfully", data: updatedBook });
    } catch (error) {
        res.status(500).json({ code: 500, message: "Failed to update book", error: error.message });
    }
});

// Delete a book
router.delete("/:id", verifyToken, async (req, res) => {
    try {
        const deletedBook = await bookService.deleteBook(req.params.id);
        if (!deletedBook) {
            return res.status(404).json({ code: 404, message: "Book not found" });
        }
        res.status(200).json({ code: 200, message: "Book deleted successfully" });
    } catch (error) {
        res.status(500).json({ code: 500, message: "Failed to delete book", error: error.message });
    }
});

export default router;
