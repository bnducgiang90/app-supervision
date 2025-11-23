# 🎉 START HERE - Realtime Chat Application

## Xin chào! Chào mừng bạn đến với Realtime Chat Application

Đây là ứng dụng chat realtime được build với **Java 21**, **Spring Boot WebFlux**, **Server-Sent Events (SSE)**, **PostgreSQL (R2DBC)**, và **MinIO**.

---

## 🚀 Bắt đầu trong 30 giây

### Bạn chưa biết gì về project này?

**→ Đọc file này trước! (bạn đang đọc đúng rồi đấy!)**

### Bạn muốn chạy app ngay?

**→ Mở [QUICKSTART.md](QUICKSTART.md)** - Chạy app trong 5 phút

### Bạn muốn hiểu chi tiết?

**→ Mở [INDEX.md](INDEX.md)** - Hub tổng hợp tất cả tài liệu

---

## 📦 Project này có gì?

### ✨ Features chính

- 💬 **Real-time messaging** - Chat realtime với Server-Sent Events (SSE)
- 👥 **Group chat** - Tạo group, thêm members, quản lý conversations
- 📁 **File upload** - Upload ảnh, video, files trong chat
- 🗄️ **PostgreSQL** - Lưu trữ users, groups, messages với R2DBC reactive
- 💾 **Dual storage** - Lựa chọn MinIO hoặc Local file system
- 🌐 **REST APIs** - 20+ endpoints để quản lý tất cả
- 🎨 **Web UI** - Interface đẹp để test ngay trong browser

### 🛠️ Tech Stack

| Component | Technology |
|-----------|-----------|
| Language | ☕ Java 21 |
| Framework | 🍃 Spring Boot 3.2 + WebFlux |
| Real-time | ⚡ Server-Sent Events (SSE) |
| Database | 🐘 PostgreSQL 16 |
| DB Driver | 🔄 R2DBC (Reactive) |
| Storage | 📦 MinIO / Local FS |
| Build | 🔨 Maven |
| Container | 🐳 Docker Compose |

---

## 📁 Các file quan trọng

### 📚 Documentation (BẮT ĐẦU TỪ ĐÂY!)

1. **[START_HERE.md](START_HERE.md)** ← Bạn đang ở đây
   - Overview về project
   - Hướng dẫn navigate docs

2. **[INDEX.md](INDEX.md)** 📖
   - Hub tổng hợp tất cả docs
   - Guide theo role (PM, Dev, QA...)
   - Quick search

3. **[QUICKSTART.md](QUICKSTART.md)** ⚡
   - Chạy app trong 5 phút
   - Quick troubleshooting
   - Essential commands

4. **[README.md](README.md)** 📘
   - Tài liệu đầy đủ
   - Setup chi tiết
   - Configuration guide

5. **[API_EXAMPLES.md](API_EXAMPLES.md)** 🌐
   - Ví dụ sử dụng APIs
   - cURL commands
   - JavaScript SSE examples

6. **[PROJECT_STRUCTURE.md](PROJECT_STRUCTURE.md)** 🏗️
   - Giải thích cấu trúc code
   - Data flow diagrams
   - Component details

### 🔧 Configuration Files

- **[pom.xml](pom.xml)** - Maven dependencies
- **[docker-compose.yml](docker-compose.yml)** - Services setup
- **[application.yml](src/main/resources/application.yml)** - Main config
- **[application-dev.yml](src/main/resources/application-dev.yml)** - Dev config
- **[schema.sql](src/main/resources/schema.sql)** - Database schema

### 🧪 Testing Tools

- **[test-api.sh](test-api.sh)** - Automated API tests
- **[postman-collection.json](postman-collection.json)** - Postman collection
- **[index.html](src/main/resources/static/index.html)** - Web UI client

### 💻 Source Code

```
src/main/java/com/chatapp/
├── config/          # 4 configuration classes
├── controller/      # 5 REST controllers
├── dto/             # Request/Response DTOs
├── entity/          # 5 database entities
├── exception/       # Error handling
├── repository/      # 5 R2DBC repositories
└── service/         # 5 business services
```

**Total: 27 Java files** với ~3,000+ lines of code

---

## 🎯 Roadmap cho người mới

### Phase 1: Setup và chạy (10 phút)

1. ✅ Đọc file này (START_HERE.md)
2. ✅ Mở [QUICKSTART.md](QUICKSTART.md)
3. ✅ Chạy `docker-compose up -d`
4. ✅ Build: `mvn clean package`
5. ✅ Run: `java -jar target/realtime-chat-1.0.0.jar`
6. ✅ Test: Mở `http://localhost:8080`

### Phase 2: Hiểu features (20 phút)

1. ✅ Test Web UI
2. ✅ Chạy `./test-api.sh`
3. ✅ Import Postman collection
4. ✅ Test SSE với browser console
5. ✅ Upload một file trong chat

### Phase 3: Hiểu code (30 phút)

1. ✅ Đọc [PROJECT_STRUCTURE.md](PROJECT_STRUCTURE.md)
2. ✅ Review source code theo layers
3. ✅ Xem data flow diagrams
4. ✅ Hiểu reactive programming concepts

### Phase 4: Customize (1+ giờ)

1. ✅ Thêm một API endpoint mới
2. ✅ Customize Web UI
3. ✅ Thêm authentication
4. ✅ Deploy lên server

---

## 🎓 Học theo vai trò

### 🎨 Frontend Developer

**Focus on:**
- [API_EXAMPLES.md](API_EXAMPLES.md) - Đặc biệt SSE section
- [index.html](src/main/resources/static/index.html) - Web UI code
- Test SSE connection và real-time updates

**Quick Start:**
```javascript
// Connect to SSE
const eventSource = new EventSource('http://localhost:8080/api/sse/stream?userId=1');

// Listen for messages
eventSource.addEventListener('new_message', (e) => {
    const message = JSON.parse(e.data);
    console.log(message);
});
```

### 💻 Backend Developer

**Focus on:**
- [PROJECT_STRUCTURE.md](PROJECT_STRUCTURE.md) - Kiến trúc code
- Source code trong `src/main/java/com/chatapp/`
- R2DBC và reactive programming

**Key Files:**
- `SSEService.java` - SSE management
- `MessageService.java` - Business logic
- `FileStorageService.java` - Storage abstraction

### 🔧 DevOps Engineer

**Focus on:**
- [docker-compose.yml](docker-compose.yml) - Services
- [application.yml](src/main/resources/application.yml) - Config
- Deployment và scaling considerations

**Quick Commands:**
```bash
docker-compose up -d      # Start services
docker-compose ps         # Check status
docker-compose logs -f    # View logs
```

### 🧪 QA Engineer

**Focus on:**
- [API_EXAMPLES.md](API_EXAMPLES.md) - API specs
- [test-api.sh](test-api.sh) - Automated tests
- [postman-collection.json](postman-collection.json) - Manual tests

**Quick Test:**
```bash
chmod +x test-api.sh
./test-api.sh
```

### 📊 Product Manager / Analyst

**Focus on:**
- [README.md](README.md) - Features overview
- Web UI demo tại `http://localhost:8080`
- Feature capabilities và limitations

**Demo Flow:**
1. Create users
2. Create group
3. Add members
4. Send messages
5. Upload files
6. See real-time updates

---

## 🚦 Quick Decision Tree

```
BẠN ĐANG Ở ĐÂU?
│
├─ Chưa biết gì về project
│  └─→ Đọc START_HERE.md (file này) → Đọc QUICKSTART.md
│
├─ Đã chạy được app, muốn dùng APIs
│  └─→ Đọc API_EXAMPLES.md
│
├─ Muốn hiểu code structure
│  └─→ Đọc PROJECT_STRUCTURE.md
│
├─ Cần tài liệu đầy đủ
│  └─→ Đọc README.md
│
├─ Muốn navigate tất cả docs
│  └─→ Đọc INDEX.md
│
└─ Gặp lỗi
   └─→ QUICKSTART.md → Troubleshooting section
```

---

## 💡 Tips quan trọng

### ✅ DO:
- Đọc docs theo thứ tự: START_HERE → QUICKSTART → README
- Test code trong khi đọc
- Chạy `test-api.sh` để hiểu flow
- Xem source code sau khi đọc docs
- Dùng Web UI để visualize features

### ❌ DON'T:
- Skip QUICKSTART.md và nhảy vào code ngay
- Quên start PostgreSQL và MinIO
- Dùng Java cũ hơn 21
- Ignore error messages
- Test production với data thật

---

## 🎯 3 điều quan trọng nhất

### 1. 📖 Documentation Structure

```
START_HERE.md (entry point)
    ↓
INDEX.md (navigation hub)
    ↓
QUICKSTART.md → README.md → API_EXAMPLES.md → PROJECT_STRUCTURE.md
```

### 2. 🚀 Quick Start Command

```bash
# One command to start everything
docker-compose up -d && \
mvn clean package && \
java -jar target/realtime-chat-1.0.0.jar
```

### 3. 🌐 Key URLs

- **Application:** http://localhost:8080
- **MinIO Console:** http://localhost:9001 (minioadmin/minioadmin)
- **pgAdmin:** http://localhost:5050 (admin@admin.com/admin)
- **PostgreSQL:** localhost:5432 (postgres/postgres)

---

## 📞 Support & Help

### Gặp vấn đề?

1. Check [QUICKSTART.md](QUICKSTART.md) - Troubleshooting
2. Xem [API_EXAMPLES.md](API_EXAMPLES.md) cho ví dụ
3. Review [PROJECT_STRUCTURE.md](PROJECT_STRUCTURE.md) cho data flow
4. Google error message
5. Tạo issue với log details

### Muốn contribute?

1. Fork project
2. Đọc toàn bộ docs
3. Hiểu code structure
4. Make changes
5. Test thoroughly
6. Submit PR với docs updates

---

## ✨ Next Steps

Bạn đã đọc xong START_HERE.md! Giờ thì:

### Option 1: Chạy app ngay (Recommended for beginners)
→ **[QUICKSTART.md](QUICKSTART.md)**

### Option 2: Đọc tổng quan đầy đủ (Recommended for everyone)
→ **[README.md](README.md)**

### Option 3: Xem tất cả docs có gì (Recommended for navigation)
→ **[INDEX.md](INDEX.md)**

---

## 🎊 You're all set!

Bây giờ bạn đã biết:
- ✅ Project này làm gì
- ✅ Docs được organize như thế nào
- ✅ Nên bắt đầu từ đâu
- ✅ Làm sao tìm thông tin cần thiết

**Let's get started! 🚀**

---

*Happy Coding!* 💻

*Nếu bạn thấy project này hữu ích, đừng quên share với đồng nghiệp!*

---

**Quick Links:**
- [INDEX.md](INDEX.md) - Navigate all docs
- [QUICKSTART.md](QUICKSTART.md) - 5-minute setup
- [README.md](README.md) - Full documentation
- [API_EXAMPLES.md](API_EXAMPLES.md) - API usage guide
- [PROJECT_STRUCTURE.md](PROJECT_STRUCTURE.md) - Code walkthrough
