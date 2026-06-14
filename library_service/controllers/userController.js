import express from 'express';
import * as userService from '../services/userService.js';

const router = express.Router();

router.post("/register", async (req, res) => {
    try {
        const result = await userService.register(req.body);
        res.status(201).json({ code: 201, ...result });
    } catch (error) {
        res.status(400).json({ code: 400, message: error.message });
    }
});

router.post("/login", async (req, res) => {
    try {
        const { email, password } = req.body;
        const result = await userService.login(email, password);
        res.status(200).json({ code: 200, ...result });
    } catch (error) {
        res.status(400).json({ code: 400, message: error.message });
    }
});

export default router;
