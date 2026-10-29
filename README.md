# Classroom Reservation System 易室借

**A classroom booking platform for students and administrators.**

Students find free rooms and book them. Admins review requests and manage keys.

> Software Engineering course project · 5-person team · Team Lead

---

## 🎬 Demo Video

[![Watch the demo on YouTube](https://img.youtube.com/vi/sMxRTr58JmI/hqdefault.jpg)](https://youtu.be/sMxRTr58JmI)

*Click the image to watch the demo on YouTube.*

---

## ✨ Features

### 🎓 For Students
- Browse rooms by floor on an interactive campus map
- View a weekly schedule to see which time slots are free
- Submit booking requests and track their status and history
- Export a room's weekly schedule as a PDF

### 🛠️ For Administrators
- Approve or deny booking requests
- Track key status: who borrowed a key and whether it was returned
- Ban or unban classrooms and users, with email notifications
- Search all reservation records

### 🔐 Booking Rules & Accounts
- Rejects overlapping, duplicate, or banned requests right away
- Firebase sign-in with email/password or Google, plus email verification
- Role-based access for borrowers and administrators
- Students must register with a school email tied to their student ID
- English and Traditional Chinese interface

---

## 🛠 Tech Stack

| Layer | Technology |
|---|---|
| Frontend | React 18, Material UI, React Router, i18next, jsPDF |
| Backend | Spring Boot 3, Java 17, Spring Mail |
| Database | MongoDB Atlas |
| Auth | Firebase Authentication |
| DevOps | Docker, Render (backend), Vercel (frontend) |
| Load Testing | Apache JMeter |

---

## 📂 Project Structure

```
.
├── frontend   # React app
├── backend    # Spring Boot API (with Dockerfile)
└── report     # Requirements, design, and test documents (Chinese)
```

---

## 🚀 Getting Started

**Prerequisites:** Node.js 18+, Java 17, a MongoDB database, and a Firebase project.

**Backend**

Set these environment variables first:

```env
MONGODB_URI=mongodb+srv://<user>:<password>@<cluster>/<database>
MAIL_USERNAME=your-gmail-address
MAIL_PASSWORD=your-gmail-app-password
```

```bash
cd backend
./mvnw spring-boot:run   # Start the API at http://localhost:8080
```

**Frontend**

```bash
cd frontend
npm install
npm start                # Start the app at http://localhost:3000
```

For a local backend, set `baseURL` in `src/config/apiConfig.js`.

---

## 👥 Team

Course project by a 5-person team at NTOU CSE.

**My role (Team Lead):**
- Built the booking service and its conflict-checking rules
- Built Firebase authentication with role-based access
- Containerized the backend with Docker and handled deployment
