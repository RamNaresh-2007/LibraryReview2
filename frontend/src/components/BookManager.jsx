import React, { useEffect, useRef, useState } from 'react';
import './BookManager.css';
import ProgressBar from './ProgressBar';
import { apiGet, apiPost, apiPut, apiPatch, apiDelete } from '../lib';

const EMPTY_BOOK = { id: '', title: '', author: '', isbn: '', category: '', available: true };

const BookManager = ({ role }) => {
    const [books, setBooks] = useState([]);
    const [isProgress, setIsProgress] = useState(false);
    const [showPopup, setShowPopup] = useState(false);
    const [bookData, setBookData] = useState(EMPTY_BOOK);
    const [errorData, setErrorData] = useState({});
    const [search, setSearch] = useState('');
    const [filterCat, setFilterCat] = useState('All');
    const [apiError, setApiError] = useState('');
    const ftitled = useRef();

    const canManage = role === 'admin' || role === 'librarian';

    const fetchBooks = async () => {
        setIsProgress(true);
        try {
            const d = await apiGet('/api/books');
            setBooks(d || []);
        } catch (err) {
            setApiError('Load failure');
        } finally {
            setIsProgress(false);
        }
    };

    useEffect(() => { fetchBooks(); }, []);

    // ── derived (optimized with useMemo) ──────────────────────────
    const categories = React.useMemo(() => ['All', ...new Set(books.map(b => b.category).filter(Boolean))], [books]);

    const filtered = React.useMemo(() => {
        return books.filter(b => {
            const matchSearch = (b.title || '').toLowerCase().includes(search.toLowerCase()) ||
                (b.author || '').toLowerCase().includes(search.toLowerCase());
            const matchCat = filterCat === 'All' || b.category === filterCat;
            return matchSearch && matchCat;
        });
    }, [books, search, filterCat]);

    const handleInput = React.useCallback((e) => {
        const { name, value } = e.target;
        setBookData(prev => ({ ...prev, [name]: value }));
    }, []);

    const validateData = () => {
        let errors = {};
        if (!bookData.title) errors.title = true;
        if (!bookData.author) errors.author = true;
        if (!bookData.isbn) errors.isbn = true;
        if (!bookData.category) errors.category = true;
        setErrorData(errors);
        return Object.keys(errors).length > 0;
    };

    // ── CRUD actions ───────────────────────────────────────────────
    const saveBook = async () => {
        if (!canManage) return;
        if (validateData()) return;
        setIsProgress(true);
        setApiError('');

        const isEdit = !!bookData.id;
        const path = isEdit ? `/api/books/${bookData.id}` : `/api/books`;

        try {
            const saved = isEdit ? await apiPut(path, bookData) : await apiPost(path, bookData);
            setBooks(prev => isEdit
                ? prev.map(b => b.id === saved.id ? saved : b)
                : [...prev, saved]
            );
            setShowPopup(false);
        } catch (err) {
            setApiError(err.message || 'Save failed');
        } finally {
            setIsProgress(false);
        }
    };

    const deleteBook = async (id) => {
        if (!canManage) return;
        if (!window.confirm('Delete this book?')) return;
        setIsProgress(true);
        try {
            await apiDelete(`/api/books/${id}`);
            setBooks(prev => prev.filter(b => b.id !== id));
        } catch (err) {
            setApiError(err.message || 'Delete failed');
        } finally {
            setIsProgress(false);
        }
    };

    const toggleAvail = async (id) => {
        if (!canManage) return;
        // Optimistic update
        const originalBooks = [...books];
        setBooks(prev => prev.map(b => b.id === id ? { ...b, available: !b.available } : b));

        try {
            const updated = await apiPatch(`/api/books/${id}/toggle-availability`);
            setBooks(prev => prev.map(b => b.id === updated.id ? updated : b));
        } catch (err) {
            setBooks(originalBooks);
            setApiError('Toggle failed. Please try again.');
        }
    };

    const editBook = (book) => { setBookData(book); setErrorData({}); setShowPopup(true); setTimeout(() => ftitled.current?.focus(), 0); };
    const addNew = () => { setBookData(EMPTY_BOOK); setErrorData({}); setShowPopup(true); setTimeout(() => ftitled.current?.focus(), 0); };

    const available = books.filter(b => b.available).length;

    return (
        <div className='book-manager'>
            <div className='bm-header'>
                <div>
                    <h2>Book Catalog</h2>
                    <div className='bm-meta'>
                        <span>{books.length} books</span>
                        <span className='sep'>·</span>
                        <span className='avail'>{available} available</span>
                        <span className='sep'>·</span>
                        <span className='loaned'>{books.length - available} on loan</span>
                    </div>
                </div>
                {canManage && <button className='btn-add-book' onClick={addNew}>+ Add Book</button>}
            </div>

            {apiError && <div className='api-error-banner'>{apiError}</div>}

            <div className='bm-toolbar'>
                <input className='bm-search' placeholder='🔍 Search title or author…' value={search} onChange={e => setSearch(e.target.value)} />
                <div className='cat-pills'>
                    {categories.map(c => (
                        <button key={c} className={`cat-pill ${filterCat === c ? 'active' : ''}`} onClick={() => setFilterCat(c)}>{c}</button>
                    ))}
                </div>
            </div>

            <div className='bm-table-wrap'>
                <table className='bm-table'>
                    <thead>
                        <tr>
                            <th>#</th>
                            <th>Book</th>
                            <th>Category</th>
                            <th>ISBN</th>
                            <th>Status</th>
                            {canManage && <th>Actions</th>}
                        </tr>
                    </thead>
                    <tbody>
                        {isProgress && books.length === 0 && (
                            <tr><td colSpan={6} className='empty-row'>Loading catalog…</td></tr>
                        )}
                        {!isProgress && filtered.length === 0 && (
                            <tr><td colSpan={6} className='empty-row'>No books match your search</td></tr>
                        )}
                        {filtered.map((book, i) => (
                            <tr key={book.id}>
                                <td className='idx-cell'>{i + 1}</td>
                                <td>
                                    <div className='book-info'>
                                        <span className='b-title'>{book.title}</span>
                                        <span className='b-author'>{book.author}</span>
                                    </div>
                                </td>
                                <td><span className='cat-badge'>{book.category}</span></td>
                                <td className='isbn-cell'>{book.isbn}</td>
                                <td>
                                    <button
                                        disabled={!canManage}
                                        className={`avail-toggle ${book.available ? 'avail' : 'loan'}`}
                                        onClick={() => toggleAvail(book.id)}
                                        title={canManage ? "Click to toggle availability" : "Viewing current status"}
                                    >
                                        {book.available ? '✓ Available' : '⊗ On Loan'}
                                    </button>
                                </td>
                                {canManage && (
                                    <td>
                                        <div className='action-btns'>
                                            <button className='btn-edit' onClick={() => editBook(book)}>✏️ Edit</button>
                                            <button className='btn-del' onClick={() => deleteBook(book.id)}>🗑</button>
                                        </div>
                                    </td>
                                )}
                            </tr>
                        ))}
                    </tbody>
                </table>
            </div>

            {showPopup && (
                <div className='overlay'>
                    <div className='book-popup'>
                        <span className='close-x' onClick={() => setShowPopup(false)}>&times;</span>
                        <h3>{bookData.id ? '✏️ Edit Book' : '📚 Add New Book'}</h3>

                        <label>Book Title *</label>
                        <input ref={ftitled} className={errorData.title ? 'error' : ''} name='title' placeholder='Enter book title' value={bookData.title} onChange={handleInput} />

                        <label>Author *</label>
                        <input className={errorData.author ? 'error' : ''} name='author' placeholder='Enter author name' value={bookData.author} onChange={handleInput} />

                        <div className='popup-row'>
                            <div>
                                <label>Category *</label>
                                <input className={errorData.category ? 'error' : ''} name='category' placeholder='e.g. CS, Math' value={bookData.category} onChange={handleInput} />
                            </div>
                            <div>
                                <label>ISBN *</label>
                                <input className={errorData.isbn ? 'error' : ''} name='isbn' placeholder='ISBN number' value={bookData.isbn} onChange={handleInput} />
                            </div>
                        </div>

                        <div className='popup-check'>
                            <input type='checkbox' id='avail' checked={bookData.available} onChange={e => setBookData(prev => ({ ...prev, available: e.target.checked }))} />
                            <label htmlFor='avail'>Mark as Available</label>
                        </div>

                        <button className='btn-save-book' onClick={saveBook} disabled={isProgress}>
                            {isProgress ? 'Saving…' : bookData.id ? 'Save Changes' : 'Add Book'}
                        </button>
                    </div>
                </div>
            )}
            <ProgressBar isProgress={isProgress} />
        </div>
    );
};

export default BookManager;
