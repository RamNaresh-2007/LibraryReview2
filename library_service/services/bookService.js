import Book from '../models/book.js';

export const createBook = async (bookData) => {
    try {
        const book = new Book(bookData);
        return await book.save();
    } catch (error) {
        throw error;
    }
};

export const getAllBooks = async () => {
    try {
        return await Book.find();
    } catch (error) {
        throw error;
    }
};

export const getBookById = async (id) => {
    try {
        return await Book.findById(id);
    } catch (error) {
        throw error;
    }
};

export const updateBook = async (id, updateData) => {
    try {
        return await Book.findByIdAndUpdate(id, updateData, { new: true });
    } catch (error) {
        throw error;
    }
};

export const deleteBook = async (id) => {
    try {
        return await Book.findByIdAndDelete(id);
    } catch (error) {
        throw error;
    }
};
