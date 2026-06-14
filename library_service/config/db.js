import mongoose from "mongoose";
import dotenv from 'dotenv';

dotenv.config();

let db;

export async function connectDB(){
    if(!db){
        try {
            db = await mongoose.connect(process.env.DBURL);
            console.log("MongoDB connected successfully");
        } catch (error) {
            console.error("MongoDB connection error:", error);
            process.exit(1);
        }
    }
};
