# Realtime Chat Application với Spring Boot WebFlux và SSE

Ứng dụng chat realtime sử dụng Server-Sent Events (SSE) để truyền message giữa các client, hỗ trợ group chat với upload ảnh/video, metadata lưu trong PostgreSQL (R2DBC), file lưu trên MinIO hoặc local storage.

## 🚀 Tính năng

- ✅ **Realtime messaging** với Server-Sent Events (SSE)
- ✅ **Group chat** với quản lý members
- ✅ **Upload files** (ảnh, video, file thông thường)
- ✅ **Dual storage**: MinIO hoặc Local File System (cấu hình động)
- ✅ **R2DBC** với PostgreSQL cho reactive database access
- ✅ **Spring Boot WebFlux** - Non-blocking, reactive architecture
- ✅ **Java 21** với Virtual Threads support
- ✅ **REST APIs** đầy đủ cho tất cả operations
- ✅ **Web UI** để test SSE và chat features

## 📋 Yêu cầu hệ thống

- Java 21+
- Maven 3.8+
- PostgreSQL 14+
- MinIO (optional, nếu dùng MinIO storage)
- Docker & Docker Compose (optional, để chạy services)

## 🏗️ Cấu trúc Project

```
realtime-chat-app/
├── src/
│   ├── main/
│   │   ├── java/com/chatapp/
│   │   │   ├── config/          # Configurations
│   │   │   │   ├── MinioConfig.java
│   │   │   │   ├── R2dbcConfig.java
│   │   │   │   ├── StorageProperties.java
│   │   │   │   └── WebConfig.java
│   │   │   ├── controller/      # REST Controllers
│   │   │   │   ├── FileController.java
│   │   │   │   ├── GroupController.java
│   │   │   │   ├── MessageController.java
│   │   │   │   ├── SSEController.java
│   │   │   │   └── UserController.java
│   │   │   ├── dto/             # Data Transfer Objects
│   │   │   │   └── DTOs.java
│   │   │   ├── entity/          # Database Entities
│   │   │   │   ├── Group.java
│   │   │   │   ├── GroupMember.java
│   │   │   │   ├── Message.java
│   │   │   │   ├── MessageAttachment.java
│   │   │   │   └── User.java
│   │   │   ├── repository/      # R2DBC Repositories
│   │   │   │   ├── GroupMemberRepository.java
│   │   │   │   ├── GroupRepository.java
│   │   │   │   ├── MessageAttachmentRepository.java
│   │   │   │   ├── MessageRepository.java
│   │   │   │   └── UserRepository.java
│   │   │   ├── service/         # Business Logic
│   │   │   │   ├── FileStorageService.java
│   │   │   │   ├── GroupService.java
│   │   │   │   ├── MessageService.java
│   │   │   │   ├── SSEService.java
│   │   │   │   └── UserService.java
│   │   │   └── RealtimeChatApplication.java
│   │   └── resources/
│   │       ├── application.yml
│   │       ├── schema.sql
│   │       └── static/
│   │           └── index.html   # Web UI client
│   └── test/
└── pom.xml
```

## 🔧 Cài đặt và Chạy

### Option 1: Chạy với Docker Compose (Khuyến nghị)

1. **Tạo file `docker-compose.yml`:**

```yaml
version: '3.8'

services:
  postgres:
    image: postgres:16
    environment:
      POSTGRES_DB: chatdb
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: postgres
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data

  minio:
    image: minio/minio:latest
    command: server /data --console-address ":9001"
    environment:
      MINIO_ROOT_USER: minioadmin
      MINIO_ROOT_PASSWORD: minioadmin
    ports:
      - "9000:9000"
      - "9001:9001"
    volumes:
      - minio_data:/data

volumes:
  postgres_data:
  minio_data:
```

2. **Khởi động services:**

```bash
docker-compose up -d
```

3. **Build và chạy application:**

```bash
mvn clean package
java -jar target/realtime-chat-1.0.0.jar
```

### Option 2: Chạy Manual

1. **Cài đặt PostgreSQL:**

```bash
# Ubuntu/Debian
sudo apt install postgresql postgresql-contrib

# macOS
brew install postgresql

# Tạo database
psql -U postgres
CREATE DATABASE chatdb;
```

2. **Cài đặt MinIO (Optional):**

```bash
# Download và chạy MinIO
wget https://dl.min.io/server/minio/release/linux-amd64/minio
chmod +x minio
./minio server /mnt/data --console-address ":9001"
```

3. **Cấu hình application.yml:**

Chỉnh sửa `src/main/resources/application.yml` để match với setup của bạn.

4. **Build và chạy:**

```bash
mvn clean package
java -jar target/realtime-chat-1.0.0.jar
```

## ⚙️ Configuration

### Storage Configuration

**Dùng MinIO:**

```yaml
storage:
  type: minio
  minio:
    endpoint: http://localhost:9000
    access-key: minioadmin
    secret-key: minioadmin
    bucket: chat-files
```

**Dùng Local Storage:**

```yaml
storage:
  type: local
  local:
    upload-dir: ./uploads
```

### Database Configuration

```yaml
spring:
  r2dbc:
    url: r2dbc:postgresql://localhost:5432/chatdb
    username: postgres
    password: postgres
```

## 📚 API Documentation

### User APIs

**Tạo User:**
```bash
POST /api/users
Content-Type: application/json

{
  "username": "john_doe",
  "displayName": "John Doe",
  "avatarUrl": "https://example.com/avatar.jpg"
}
```

**Lấy thông tin User:**
```bash
GET /api/users/{id}
GET /api/users/username/{username}
GET /api/users
```

**Cập nhật status:**
```bash
PUT /api/users/{id}/status?status=ONLINE
```

### Group APIs

**Tạo Group:**
```bash
POST /api/groups
Content-Type: application/json

{
  "name": "Team Chat",
  "description": "Team discussion group",
  "createdBy": 1
}
```

**Thêm member vào Group:**
```bash
POST /api/groups/{groupId}/members
Content-Type: application/json

{
  "userId": 2,
  "role": "MEMBER"
}
```

**Lấy danh sách Groups của User:**
```bash
GET /api/groups/user/{userId}
```

**Lấy members của Group:**
```bash
GET /api/groups/{groupId}/members
```

### Message APIs

**Gửi text message:**
```bash
POST /api/messages
Content-Type: application/json

{
  "groupId": 1,
  "senderId": 1,
  "content": "Hello everyone!",
  "messageType": "TEXT"
}
```

**Gửi message với files:**
```bash
POST /api/messages/upload
Content-Type: multipart/form-data

message: {
  "groupId": 1,
  "senderId": 1,
  "content": "Check out these photos!",
  "messageType": "IMAGE"
}
files: [file1.jpg, file2.png]
```

**Lấy lịch sử messages:**
```bash
GET /api/messages/group/{groupId}?page=0&size=50
```

### SSE APIs

**Connect tới SSE stream:**
```javascript
const eventSource = new EventSource('http://localhost:8080/api/sse/stream?userId=1');

eventSource.addEventListener('new_message', (event) => {
  const message = JSON.parse(event.data);
  console.log('New message:', message);
});

eventSource.addEventListener('member_added', (event) => {
  const data = JSON.parse(event.data);
  console.log('New member added:', data);
});
```

**Check connection status:**
```bash
GET /api/sse/status
GET /api/sse/status/{userId}
```

## 🌐 Web UI

Mở trình duyệt và truy cập: `http://localhost:8080`

**Hướng dẫn sử dụng Web UI:**

1. Nhập User ID của bạn
2. Click "Connect" để kết nối SSE stream
3. Nhập Group ID
4. Click "Load Messages" để load tin nhắn
5. Gửi message hoặc attach files

## 🧪 Testing với cURL

**Test flow hoàn chỉnh:**

```bash
# 1. Tạo users
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{"username":"alice","displayName":"Alice"}'

curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{"username":"bob","displayName":"Bob"}'

# 2. Tạo group
curl -X POST http://localhost:8080/api/groups \
  -H "Content-Type: application/json" \
  -d '{"name":"Test Group","description":"Testing","createdBy":1}'

# 3. Thêm member vào group
curl -X POST http://localhost:8080/api/groups/1/members \
  -H "Content-Type: application/json" \
  -d '{"userId":2,"role":"MEMBER"}'

# 4. Gửi message
curl -X POST http://localhost:8080/api/messages \
  -H "Content-Type: application/json" \
  -d '{"groupId":1,"senderId":1,"content":"Hello!","messageType":"TEXT"}'

# 5. Upload image
curl -X POST http://localhost:8080/api/messages/upload \
  -F 'message={"groupId":1,"senderId":1,"content":"Check this out!","messageType":"IMAGE"}' \
  -F 'files=@/path/to/image.jpg'

# 6. Lấy messages
curl http://localhost:8080/api/messages/group/1
```

## 📊 Database Schema

Application tự động tạo các tables khi khởi động:

- **users**: Thông tin users
- **groups**: Thông tin groups
- **group_members**: Mapping users và groups
- **messages**: Tin nhắn
- **message_attachments**: File attachments

## 🔐 Security Notes

**Trong production, cần:**

1. Implement authentication/authorization (JWT, OAuth2)
2. Validate file uploads (size, type, virus scanning)
3. Rate limiting cho APIs
4. HTTPS cho SSE connections
5. Secure MinIO/storage access
6. Input validation và sanitization
7. SQL injection protection (R2DBC parameterized queries)

## 🐛 Troubleshooting

**SSE connection bị disconnect:**
- Check network/firewall settings
- Verify SSE timeout configuration
- Check browser console for errors

**File upload lỗi:**
- Check max file size configuration
- Verify storage permissions
- Check MinIO bucket configuration

**Database connection errors:**
- Verify PostgreSQL is running
- Check connection string
- Verify database exists

**R2DBC errors:**
- Ensure R2DBC driver version compatibility
- Check schema initialization

## 📝 Development Tips

1. **Enable debug logging:**
```yaml
logging:
  level:
    com.chatapp: DEBUG
```

2. **Hot reload với Spring DevTools:**
Add to pom.xml:
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-devtools</artifactId>
    <scope>runtime</scope>
    <optional>true</optional>
</dependency>
```

3. **Test SSE với Browser DevTools:**
- Open Network tab
- Filter for EventStream
- Monitor SSE events

## 🚀 Performance Tuning

**R2DBC Connection Pool:**
```yaml
spring:
  r2dbc:
    pool:
      initial-size: 10
      max-size: 50
      max-idle-time: 30m
```

**WebFlux Memory:**
```yaml
spring:
  codec:
    max-in-memory-size: 20MB
```

## 📖 Further Reading

- [Spring WebFlux Documentation](https://docs.spring.io/spring-framework/docs/current/reference/html/web-reactive.html)
- [Server-Sent Events (SSE)](https://developer.mozilla.org/en-US/docs/Web/API/Server-sent_events)
- [R2DBC Documentation](https://r2dbc.io/)
- [MinIO Documentation](https://min.io/docs/)
- [Project Reactor](https://projectreactor.io/)

## 📧 Support

Nếu có vấn đề, tạo issue hoặc liên hệ qua email.

## 📄 License

MIT License - Free to use and modify.

---

**Happy Chatting! 🎉**
