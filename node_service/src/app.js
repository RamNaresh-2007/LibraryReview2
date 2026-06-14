const express = require('express');
const http = require('http');
const { Server } = require('socket.io');
const cors = require('cors');
const mongoose = require('mongoose');

// ── CO4: Express Core Architecture ────────────────────────────────
const app = express();
const server = http.createServer(app);
const io = new Server(server, {
    cors: { origin: "*", methods: ["GET", "POST"] }
});

app.use(cors());
app.use(express.json());

// ── CO4: Event Loop & Real-Time (Socket.io) ───────────────────────
io.on('connection', (socket) => {
    console.log('User connected to notifications:', socket.id);

    socket.on('join', (room) => {
        socket.join(room);
        console.log(`User joined room: ${room}`);
    });

    socket.on('disconnect', () => {
        console.log('User disconnected');
    });
});

// ── CO2: MongoDB Integration via Mongoose ────────────────────────
const MONGO_URI = process.env.MONGO_URI || "mongodb://localhost:27017/library_notifications";
mongoose.connect(MONGO_URI)
    .then(() => console.log('Connected to MongoDB via Mongoose'))
    .catch(err => console.error('MongoDB connection error:', err));

// ── CO4: Middleware Chain ──────────────────────────────────────────
app.use((req, res, next) => {
    console.log(`${new Date().toISOString()} - ${req.method} ${req.url}`);
    next();
});

// ── Routes ─────────────────────────────────────────────────────────
app.get('/health', (req, res) => res.json({ status: 'Notification Service Online' }));

app.post('/api/notify', (req, res) => {
    const { userId, message } = req.body;
    if (!userId || !message) return res.status(400).json({ error: 'Missing fields' });

    // Real-time broadcast (CO4 Socket.io)
    io.to(userId).emit('notification', { message, timestamp: new Date() });

    res.json({ success: true, target: userId });
});

// ── Error Handling Middleware ──────────────────────────────────────
app.use((err, req, res, next) => {
    console.error(err.stack);
    res.status(500).json({ error: 'Internal Server Error' });
});

const PORT = 8081;
server.listen(PORT, () => {
    console.log(`Node Service running on port ${PORT}`);
});
