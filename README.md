cd C:\Users\yashp\Downloads\virality-engine-api\virality-engine-api

@"
# Virality Engine API

## Backend Engineering Assignment - Core API & Guardrails

A high-performance Spring Boot microservice with Redis-based guardrails for bot interactions.

## 🚀 Tech Stack
- Java 17+ / Spring Boot 3.x
- PostgreSQL (JPA/Hibernate)
- Redis (Spring Data Redis)
- Docker Compose

## ✅ Features Implemented

### Phase 1: Core API
- ✅ Create posts (Users & Bots)
- ✅ Add comments with depth tracking
- ✅ Like posts
- ✅ PostgreSQL database with JPA

### Phase 2: Redis Virality Engine
- ✅ **Virality Score**: Bot(+1), Like(+20), Comment(+50)
- ✅ **Horizontal Cap**: Max 100 bot replies per post
- ✅ **Vertical Cap**: Max 20 comment depth levels
- ✅ **Cooldown Cap**: 10 minutes between bot-human interactions

### Phase 3: Notification Engine
- ✅ Smart batching with 15-minute cooldown
- ✅ CRON sweeper running every 5 minutes
- ✅ Summarized push notifications

## 🔒 Thread Safety & Race Condition Protection

The horizontal cap uses **atomic Redis operations** to ensure exactly 100 bot replies even under 200 concurrent requests:

\`\`\`java
public boolean tryAddBotReply(Long postId, int maxLimit) {
    String key = "post:" + postId + ":bot_count";
    Long newCount = redisTemplate.opsForValue().increment(key);
    if (newCount != null && newCount <= maxLimit) {
        return true;
    } else {
        redisTemplate.opsForValue().decrement(key);
        return false;
    }
}
\`\`\`

## 📦 Setup Instructions

### Prerequisites
- Docker Desktop
- Java 17+
- Maven

### 1. Start PostgreSQL and Redis
\`\`\`bash
docker-compose up -d
\`\`\`

### 2. Build and Run
\`\`\`bash
mvn clean install
mvn spring-boot:run
\`\`\`

### 3. Test the APIs
Import the Postman collection and test the endpoints.

## 📡 API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | \`/api/posts\` | Create a new post |
| POST | \`/api/posts/{id}/comments\` | Add a comment |
| POST | \`/api/posts/{id}/like\` | Like a post |
| GET | \`/api/posts/{id}/virality-score\` | Get virality score |
| GET | \`/api/users/{id}/notifications/pending\` | Check pending notifications |

## 🧪 Testing the Horizontal Cap

Run 101 bot replies - first 100 succeed, 101st returns HTTP 429:

\`\`\`bash
# Test with multiple bots to avoid cooldown
for i in {1..101}; do
  curl -X POST http://localhost:8080/api/posts/1/comments \
    -H "Content-Type: application/json" \
    -d "{\"content\":\"Test\",\"authorId\":$((i%5+1)),\"authorType\":\"BOT\"}"
done
\`\`\`

**Expected Results:**
- ✅ First 100 requests: HTTP 200
- ❌ Request 101: HTTP 429 (Too Many Requests)

## 📊 Test Results

| Test | Expected | Actual | Status |
|------|----------|--------|--------|
| Horizontal Cap (100 max) | 100 replies | 100 replies | ✅ PASS |
| 101st reply | HTTP 429 | HTTP 429 | ✅ PASS |
| Cooldown (10 min) | HTTP 429 | HTTP 429 | ✅ PASS |
| Notification Batching | Batched | Batched | ✅ PASS |
| CRON Sweeper | Runs every 5min | Runs every 5min | ✅ PASS |

## 📁 Project Structure

\`\`\`
src/main/java/com/yash/virality_engine_api/
├── config/          # Redis configuration
├── controller/      # REST endpoints
├── dto/            # Request/Response DTOs
├── entity/          # JPA entities
├── repository/      # Data access layer
└── service/         # Business logic
    ├── RedisViralityService.java    # Redis operations
    ├── NotificationService.java     # Notification batching
    └── ScheduledNotificationService.java  # CRON sweeper
\`\`\`

## 🐳 Docker Compose

\`\`\`yaml
services:
  postgres:
    image: postgres:15
    ports:
      - "5432:5432"
  redis:
    image: redis:7-alpine
    ports:
      - "6379:6379"
\`\`\`

## 📝 Sample API Requests

### Create a Post
\`\`\`bash
curl -X POST http://localhost:8080/api/posts \
  -H "Content-Type: application/json" \
  -d '{"content":"My first post","authorId":1,"authorType":"USER"}'
\`\`\`

### Add a Comment
\`\`\`bash
curl -X POST http://localhost:8080/api/posts/1/comments \
  -H "Content-Type: application/json" \
  -d '{"content":"Great post!","authorId":2,"authorType":"BOT"}'
\`\`\`

### Like a Post
\`\`\`bash
curl -X POST http://localhost:8080/api/posts/1/like \
  -H "Content-Type: application/json" \
  -d '{"userId":1}'
\`\`\`

## 👨‍💻 Author
Yash Patel

## 📅 Date
$(Get-Date -Format "yyyy-MM-dd")

## 🔗 GitHub Repository
[]
"@ | Out-File -FilePath README.md -Encoding utf8

Write-Host "✅ README.md file created successfully!" -ForegroundColor Green
