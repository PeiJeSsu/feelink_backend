# FeelInk Backend

Spring Boot backend for **[FeelInk 超繪聊](https://github.com/PeiJeSsu/feelink_frontend)**, an AI emotional support platform.

- 🏆 **3rd Place**, 30th InnoServe Awards (1,964 teams nationwide)
- 🎬 **[Watch the demo video](https://youtu.be/wIL9Pozo4Vw)**
- 👉 Screenshots and architecture: **[frontend repository](https://github.com/PeiJeSsu/feelink_frontend)**

---

## What This Service Does

- **Streaming AI chat**: streams Gemini replies to the browser over SSE
- **Three AI personalities**: warm, curious, and creative, each with its own prompt
- **Crisis-aware responses**: a 5-level risk rubric decides when to offer hotlines
- **Real resources**: finds real counseling services via web search (SerpAPI)
- **Canvas analysis**: the AI reads colors, subjects, and composition of a drawing
- **AI drawing**: generates objects with Gemini, background removed by remove.bg
- **Daily emotion report**: scores eight emotions and infers the user's needs
- **Persistence**: chats, canvases, and reports are stored in MongoDB

---

## 🛠 Tech Stack

- **Framework:** Spring Boot 3.4 · Java 17 · Spring Web · Spring WebFlux
- **Database:** MongoDB Atlas (Spring Data MongoDB)
- **AI & APIs:** Gemini · Google Cloud Natural Language · SerpAPI · remove.bg
- **Testing & Quality:** JUnit 5 · JaCoCo · SonarQube Cloud
- **CI/CD:** GitHub Actions
- **Deployment:** Google Cloud Run

---

## 📡 API Overview

| Endpoint | Purpose |
|---|---|
| `POST /chat` | Send a message and stream the AI reply (SSE) |
| `POST /analysis` | Upload a canvas image for AI analysis (SSE) |
| `POST /generate/simple` | Generate a drawing object for the canvas |
| `/api/users` | User profiles, synced with Firebase accounts |
| `/api/chatrooms` | Create, list, update, and delete chatrooms |
| `/api/messages` | Message history and today's emotion summary |
| `/api/canvas` | Save and load canvases |
| `/api/emotion-analysis` | Generate and retrieve daily emotion reports |

---

## ✅ Testing & CI/CD

- **440+ JUnit tests** (1,300+ together with the frontend's Jest tests)
- GitHub Actions runs all tests, JaCoCo coverage, and SonarQube on every push and PR
- Workflows are in [`.github/workflows`](.github/workflows)

---

## 🚀 Getting Started

**Prerequisites:**
- Java 17
- A MongoDB database
- A Google Cloud project with Gemini (Vertex AI) and Natural Language API enabled

Create a `.env` file in the project root:

```env
MONGODB_URI=mongodb+srv://<user>:<password>@<cluster>/<database>
GOOGLE_CLOUD_PROJECT_ID=your-gcp-project-id
GOOGLE_CLOUD_LOCATION=us-central1
SERPAPI_KEY=your-serpapi-key
REMOVEBG_API_KEY=your-removebg-key
REMOVEBG_API_URL=https://api.remove.bg/v1.0/removebg
```

Sign in to Google Cloud, then run:

```bash
gcloud auth application-default login
./mvnw spring-boot:run   # Start the API at http://localhost:8080
./mvnw test              # Run the test suite
```

---

## 👥 Team

Capstone project by a 4-person team at NTOU CSE, advised by Prof. Shang-Pin Ma.
