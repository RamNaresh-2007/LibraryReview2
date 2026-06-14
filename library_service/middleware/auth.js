import jwt from 'jsonwebtoken';

export const verifyToken = (req, res, next) => {
    const token = req.header('Authorization');

    if (!token) {
        return res.status(401).json({ code: 401, message: 'Access Denied. No token provided.' });
    }

    try {
        // Bearer Token format check
        const actualToken = token.startsWith('Bearer ') ? token.slice(7) : token;
        const verified = jwt.verify(actualToken, process.env.SECRET_KEY);
        req.user = verified;
        next();
    } catch (error) {
        res.status(400).json({ code: 400, message: 'Invalid Token' });
    }
};
