const express = require('express');
const mongoose = require('mongoose');
const bodyParser = require('body-parser');
const cors = require('cors');

const app = express();
const PORT = 3000;

// Middleware
app.use(bodyParser.json());
app.use(cors());

// MongoDB Connection
const mongoURI = "mongodb+srv://wirungu:88FTb9l0ht6zjdfQ@cluster0.vsjud.mongodb.net/?retryWrites=true&w=majority&appName=Cluster0";
mongoose.connect(mongoURI, { useNewUrlParser: true, useUnifiedTopology: true })
    .then(() => console.log("MongoDB connected successfully"))
    .catch(err => console.error("MongoDB connection error:", err));

// Define a Schema and Model
const UserSchema = new mongoose.Schema({
    name: String,
    age: Number
});
const User = mongoose.model('User', UserSchema);

// Routes
// Add User
app.post('/addUser', async (req, res) => {
    try {
        const { name, age } = req.body;

        // Check for missing fields
        if (!name || !age) {
            return res.status(400).json({ error: 'Name and age are required' });
        }

        // Check if a user with the same name already exists (case-insensitive)
        const existingUser = await User.findOne({ name: new RegExp(`^${name}$`, 'i') });
        if (existingUser) {
            return res.status(400).json({ error: 'User with the same name already exists!' });
        }

        // Create and save the new user
        const newUser = new User({ name, age });
        const savedUser = await newUser.save();
        res.status(201).json({ message: 'User added successfully', user: savedUser });
    } catch (err) {
        console.error(err);
        res.status(500).json({ error: 'Internal server error' });
    }
});

// Get All Users
app.get('/users', async (req, res) => {
    try {
        const users = await User.find();
        res.status(200).json(users);
    } catch (err) {
        res.status(500).json({ error: err.message });
    }
});

// Update user
app.put('/updateUser/:name', async (req, res) => {
    try {
        const { name } = req.params;
        const { age } = req.body;

        if (!age) {
            return res.status(400).json({ error: 'Age is required' });
        }

        const updatedUser = await User.findOneAndUpdate(
            { name: new RegExp(`^${name}$`, 'i') },
            { age },
            { new: true } // Return the updated document
        );

        if (!updatedUser) {
            return res.status(404).json({ error: 'User not found' });
        }

        res.status(200).json({ message: 'User updated successfully', user: updatedUser });
    } catch (err) {
        console.error(err);
        res.status(500).json({ error: 'Internal server error' });
    }
});

// Delete user
app.delete('/deleteUser/:name', async (req, res) => {
    try {
        const { name } = req.params;

        const deletedUser = await User.findOneAndDelete({ name: new RegExp(`^${name}$`, 'i') });

        if (!deletedUser) {
            return res.status(404).json({ error: 'User not found' });
        }

        res.status(200).json({ message: 'User deleted successfully', user: deletedUser });
    } catch (err) {
        console.error(err);
        res.status(500).json({ error: 'Internal server error' });
    }
});


app.listen(PORT, () => console.log(`Server running on http://localhost:${PORT}`));
