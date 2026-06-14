import express from 'express';
import cors from 'cors';
import dotenv from 'dotenv';
import { connectDB } from './config/db.js';
import bookRouter from './controllers/bookController.js';
import userRouter from './controllers/userController.js';

dotenv.config();

const app = express();
app.use(express.json());
app.use(cors());

// Database Connection
connectDB();

// Register routers
app.use("/api/books", bookRouter);
app.use("/api/users", userRouter);

// Default endpoint
app.get("/", async (req, res) => {
    res.json({ code: 200, message: "Library Service Started...." });
});

const PORT = process.env.PORT || 8080;
app.listen(PORT, async () => {
    console.log("Server running on http://localhost:" + PORT);
});
