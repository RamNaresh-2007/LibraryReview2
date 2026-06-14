import User from '../models/user.js';
import bcrypt from 'bcryptjs';
import jwt from 'jsonwebtoken';

export const register = async (userData) => {
    try {
        const { username, email, password } = userData;
        
        // Check if user exists
        const existingUser = await User.findOne({ email });
        if (existingUser) {
            throw new Error('User already exists with this email');
        }

        // Hash password
        const salt = await bcrypt.genSalt(10);
        const hashedPassword = await bcrypt.hash(password, salt);

        // Create user
        const user = new User({
            username,
            email,
            password: hashedPassword
        });

        await user.save();
        return { message: "User registered successfully", userId: user._id };
    } catch (error) {
        throw error;
    }
};

export const login = async (email, password) => {
    try {
        // Find user
        const user = await User.findOne({ email });
        if (!user) {
            throw new Error('Invalid email or password');
        }

        // Check password
        const isMatch = await bcrypt.compare(password, user.password);
        if (!isMatch) {
            throw new Error('Invalid email or password');
        }

        // Generate token
        const token = jwt.sign(
            { id: user._id, email: user.email }, 
            process.env.SECRET_KEY, 
            { expiresIn: '1h' }
        );

        return { token, userId: user._id, username: user.username };
    } catch (error) {
        throw error;
    }
};
