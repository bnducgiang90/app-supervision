# Hướng dẫn Deploy lên Render.com

## Bước 1: Chuẩn bị Database

1. Tạo PostgreSQL database trên Render.com:
   - Vào Dashboard → New → PostgreSQL
   - Chọn plan phù hợp (Free tier có giới hạn)
   - Lưu lại thông tin kết nối:
     - Internal Database URL
     - Database Name
     - Username
     - Password

## Bước 2: Tạo Web Service

1. Vào Dashboard → New → Web Service
2. Kết nối repository GitHub/GitLab của bạn
3. Cấu hình:
   - **Name**: `realtime-chat-app`
   - **Environment**: `Docker`
   - **Dockerfile Path**: `./Dockerfile`
   - **Docker Context**: `.`
   - **Region**: Chọn gần bạn nhất (Singapore, Oregon, Frankfurt...)
   - **Branch**: `main` hoặc branch bạn muốn deploy

## Bước 3: Cấu hình Environment Variables

Thêm các biến môi trường sau trong Render.com Dashboard:

### Database Configuration
```
SPRING_PROFILES_ACTIVE=production
SPRING_R2DBC_URL=r2dbc:postgresql://your-db-host:5432/your-db-name
SPRING_R2DBC_USERNAME=your-db-username
SPRING_R2DBC_PASSWORD=your-db-password
```

### JWT Configuration
```
JWT_SECRET=your-very-secure-256-bit-secret-key-here-change-this
JWT_EXPIRATION=86400000
```

### Storage Configuration
```
STORAGE_TYPE=local
STORAGE_LOCAL_UPLOAD_DIR=/app/uploads
```

### Server Configuration
```
PORT=8080
SERVER_PORT=8080
```

### Logging (Optional)
```
LOGGING_LEVEL_ROOT=INFO
LOGGING_LEVEL_COM_CHATAPP=INFO
```

## Bước 4: Chạy Database Migration

Sau khi deploy, bạn cần chạy SQL schema để tạo tables:

1. Vào PostgreSQL database trên Render.com
2. Mở PostgreSQL Shell hoặc dùng psql
3. Chạy file `src/main/resources/schema.sql`
4. (Nếu có) Chạy file `src/main/resources/migration_add_group_code.sql`

Hoặc bạn có thể tạo một endpoint admin để chạy migration tự động.

## Bước 5: Health Check

Render.com sẽ tự động check health tại:
- Health Check Path: `/api/users/login` (GET request)

## Lưu ý quan trọng

### 1. Persistent Storage
- Render.com **không persist** files trong container
- Nếu dùng `STORAGE_TYPE=local`, files sẽ mất khi container restart
- **Giải pháp**: 
  - Dùng MinIO/S3 (recommended)
  - Hoặc dùng Render Disk (paid feature)

### 2. Database Connection
- Dùng **Internal Database URL** cho kết nối từ cùng network
- Format: `r2dbc:postgresql://host:5432/dbname`

### 3. Port Configuration
- Render.com tự động set `PORT` env var
- Application sẽ đọc từ `application-production.yml`

### 4. Build Time
- Lần đầu build có thể mất 5-10 phút
- Các lần sau sẽ nhanh hơn nhờ Docker layer caching

### 5. Free Tier Limitations
- Container sleep sau 15 phút không có traffic
- Có thể dùng service như UptimeRobot để ping định kỳ

## Troubleshooting

### Container không start
- Check logs trong Render.com Dashboard
- Kiểm tra environment variables đã set đúng chưa
- Kiểm tra database connection

### Database connection failed
- Kiểm tra Internal Database URL
- Đảm bảo database đã được tạo
- Kiểm tra username/password

### Files không persist
- Chuyển sang dùng MinIO/S3
- Hoặc upgrade lên plan có persistent disk

## Production Checklist

- [ ] Database đã được tạo và migrate schema
- [ ] Environment variables đã được set
- [ ] JWT_SECRET đã được thay đổi thành secret mạnh
- [ ] Storage type đã được cấu hình (local hoặc minio)
- [ ] Health check endpoint hoạt động
- [ ] Test API endpoints
- [ ] Test SSE realtime connection
- [ ] Test file upload/download

## Support

Nếu gặp vấn đề, check:
1. Render.com logs
2. Application logs trong container
3. Database connection status
4. Environment variables

