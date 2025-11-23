# Docker Deployment Guide

## Files đã tạo

1. **Dockerfile** - Multi-stage build cho Spring Boot app
2. **.dockerignore** - Giảm build context size
3. **application-production.yml** - Production configuration với env vars
4. **render.yaml** - Blueprint cho Render.com (optional)
5. **DEPLOY.md** - Hướng dẫn chi tiết deploy

## Test build local

```bash
# Build image
docker build -t realtime-chat-app:latest .

# Run container
docker run -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=production \
  -e SPRING_R2DBC_URL=r2dbc:postgresql://host.docker.internal:5432/chatdb \
  -e SPRING_R2DBC_USERNAME=postgres \
  -e SPRING_R2DBC_PASSWORD=yourpassword \
  -e JWT_SECRET=your-secret-key \
  realtime-chat-app:latest
```

## Deploy lên Render.com

1. Push code lên GitHub/GitLab
2. Tạo Web Service trên Render.com
3. Connect repository
4. Set environment variables (xem DEPLOY.md)
5. Deploy!

## Image size

- Build stage: ~800MB (temporary)
- Final image: ~200MB (alpine-based)

## Notes

- Port được đọc từ `PORT` env var (Render.com tự set)
- Production profile tự động được kích hoạt
- Non-root user được sử dụng cho security
- Health check endpoint: `/api/users/login`

