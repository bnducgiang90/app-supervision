# 🚀 QUICKSTART - Realtime Chat Application

Hướng dẫn nhanh để chạy ứng dụng chat realtime trong 5 phút.

## Bước 1: Khởi động Services (30 giây)

```bash
# Clone hoặc cd vào project
cd realtime-chat-app

# Start PostgreSQL và MinIO với Docker Compose
docker-compose up -d

# Kiểm tra services đã chạy
docker-compose ps
```

**Output mong đợi:**
```
NAME                IMAGE               STATUS
chat-postgres       postgres:16-alpine  Up
chat-minio          minio/minio:latest  Up
```

## Bước 2: Build và Run Application (2 phút)

```bash
# Build application
mvn clean package -DskipTests

# Run application
java -jar target/realtime-chat-1.0.0.jar
```

**Application sẽ chạy ở:** `http://localhost:8080`

## Bước 3: Test với Web UI (1 phút)

1. Mở browser tại: `http://localhost:8080`
2. Nhập User ID: `1`
3. Click "Connect"
4. Nhập Group ID: `1`
5. Click "Load Messages"
6. Gửi message đầu tiên!

## Bước 4: Test với cURL (1 phút)

Mở terminal mới và chạy:

```bash
# 1. Tạo user
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{"username":"alice","displayName":"Alice"}'

# 2. Tạo group
curl -X POST http://localhost:8080/api/groups \
  -H "Content-Type: application/json" \
  -d '{"name":"Test Group","createdBy":1}'

# 3. Gửi message
curl -X POST http://localhost:8080/api/messages \
  -H "Content-Type: application/json" \
  -d '{"groupId":1,"senderId":1,"content":"Hello!","messageType":"TEXT"}'

# 4. Lấy messages
curl http://localhost:8080/api/messages/group/1
```

## Bước 5 (Optional): Test Script tự động

```bash
# Cấp quyền execute cho script
chmod +x test-api.sh

# Chạy test script
./test-api.sh
```

## 🎯 Các URL quan trọng

| Service | URL | Credentials |
|---------|-----|-------------|
| Application | http://localhost:8080 | - |
| MinIO Console | http://localhost:9001 | minioadmin / minioadmin |
| pgAdmin | http://localhost:5050 | admin@admin.com / admin |
| PostgreSQL | localhost:5432 | postgres / postgres |

## 🔧 Cấu hình nhanh

**Chuyển sang Local Storage (không cần MinIO):**

Edit `application.yml`:
```yaml
storage:
  type: local  # Thay vì minio
```

**Thay đổi Port:**

Edit `application.yml`:
```yaml
server:
  port: 9090  # Thay vì 8080
```

## 🐛 Troubleshooting nhanh

**Lỗi "Port 8080 already in use":**
```bash
# Tìm và kill process
lsof -ti:8080 | xargs kill -9
```

**Lỗi kết nối PostgreSQL:**
```bash
# Restart PostgreSQL
docker-compose restart postgres
```

**Lỗi MinIO:**
```bash
# Restart MinIO
docker-compose restart minio
```

**Database không khởi tạo:**
```bash
# Drop và recreate database
docker-compose exec postgres psql -U postgres -c "DROP DATABASE chatdb;"
docker-compose exec postgres psql -U postgres -c "CREATE DATABASE chatdb;"
```

## 📱 Test SSE với JavaScript

Mở browser console và chạy:

```javascript
// Connect tới SSE
const eventSource = new EventSource('http://localhost:8080/api/sse/stream?userId=1');

// Listen cho new messages
eventSource.addEventListener('new_message', (event) => {
    const message = JSON.parse(event.data);
    console.log('New message:', message);
});

// Listen cho connection
eventSource.addEventListener('connected', (event) => {
    console.log('Connected:', event.data);
});

// Gửi test message (trong tab khác hoặc terminal)
fetch('http://localhost:8080/api/messages', {
    method: 'POST',
    headers: {'Content-Type': 'application/json'},
    body: JSON.stringify({
        groupId: 1,
        senderId: 1,
        content: 'Test from browser!',
        messageType: 'TEXT'
    })
});
```

## 🎉 Xong!

Bây giờ bạn có:
- ✅ Realtime chat với SSE
- ✅ Group management
- ✅ File upload (images/videos)
- ✅ PostgreSQL database
- ✅ MinIO object storage
- ✅ Web UI để test

## 📚 Next Steps

- Đọc [README.md](README.md) để biết chi tiết về APIs
- Import [postman-collection.json](postman-collection.json) vào Postman
- Xem source code để customize
- Deploy lên production

## 🔗 Useful Commands

```bash
# Stop all services
docker-compose down

# View logs
docker-compose logs -f

# Clean everything
docker-compose down -v
rm -rf target/ uploads/

# Rebuild application
mvn clean package -DskipTests

# Run with different profile
java -jar target/realtime-chat-1.0.0.jar --spring.profiles.active=dev
```

---

**Need help?** Check [README.md](README.md) or create an issue.
