from motor.motor_asyncio import AsyncIOMotorClient
import datetime
import logging

# ── CO2: MongoDB Document Engineering (BSON/CRUD/Aggregation) ──────

MONGO_URI = "mongodb://admin:admin@ac-8ah9cqr-shard-00-00.7jmuxfw.mongodb.net:27017,ac-8ah9cqr-shard-00-01.7jmuxfw.mongodb.net:27017,ac-8ah9cqr-shard-00-02.7jmuxfw.mongodb.net:27017/digital_library?ssl=true&replicaSet=atlas-c0qz00-shard-0&authSource=admin&appName=Cluster0"
DB_NAME = "digital_library"

class MongoDBService:
    def __init__(self):
        # Set a slightly longer timeout for cloud connection
        self.client = AsyncIOMotorClient(MONGO_URI, serverSelectionTimeoutMS=5000)
        self.db = self.client[DB_NAME]
        self.collection = self.db["activity_logs"]
        self.books_collection = self.db["books"]
        self.logger = logging.getLogger("gateway.mongodb")

    async def log_activity(self, username: str, action: str, details: str):
        """CO2: MongoDB CRUD — Create (Insert)"""
        doc = {
            "user": username,
            "action": action,
            "details": details,
            "timestamp": datetime.datetime.utcnow()
        }
        try:
            result = await self.collection.insert_one(doc)
            return str(result.inserted_id)
        except Exception as e:
            self.logger.error(f"Failed to log to MongoDB: {e}")
            return None

    async def add_book(self, book_data: dict):
        """Insert a copy of the book into MongoDB Atlas books collection"""
        doc = {
            "title": book_data.get("title"),
            "author": book_data.get("author"),
            "isbn": book_data.get("isbn"),
            "categoryName": book_data.get("categoryName"),
            "available": book_data.get("available", True),
            "createdAt": datetime.datetime.utcnow()
        }
        try:
            result = await self.books_collection.insert_one(doc)
            return str(result.inserted_id)
        except Exception as e:
            self.logger.error(f"Failed to add book to MongoDB Atlas: {e}")
            return None

    async def get_recent_logs(self, limit: int = 10):
        """CO2: MongoDB CRUD — Read"""
        try:
            cursor = self.collection.find().sort("timestamp", -1).limit(limit)
            logs = []
            async for doc in cursor:
                doc["_id"] = str(doc["_id"])
                doc["timestamp"] = doc["timestamp"].isoformat()
                logs.append(doc)
            return logs
        except Exception as e:
            self.logger.error(f"Failed to read from MongoDB: {e}")
            return []

    async def get_stats(self):
        """CO2: MongoDB Aggregation Pipeline"""
        pipeline = [
            {"$group": {"_id": "$action", "count": {"$sum": 1}}},
            {"$sort": {"count": -1}}
        ]
        try:
            results = await self.collection.aggregate(pipeline).to_list(length=100)
            return {r["_id"]: r["count"] for r in results}
        except Exception:
            return {}

mongo_service = MongoDBService()
