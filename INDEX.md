# 📚 Documentation Index - Realtime Chat Application

Chào mừng đến với tài liệu của **Realtime Chat Application**! Đây là ứng dụng chat realtime sử dụng Spring Boot WebFlux, SSE, PostgreSQL (R2DBC), và MinIO.

## 🎯 Bắt đầu nhanh

**Chưa biết bắt đầu từ đâu?**

1. 📖 Đọc [QUICKSTART.md](QUICKSTART.md) - Chạy app trong 5 phút
2. 📖 Đọc [README.md](README.md) - Tài liệu đầy đủ
3. 📖 Xem [API_EXAMPLES.md](API_EXAMPLES.md) - Ví dụ sử dụng API
4. 📖 Tham khảo [PROJECT_STRUCTURE.md](PROJECT_STRUCTURE.md) - Hiểu cấu trúc code

---

## 📄 Danh sách tài liệu

### 1. [QUICKSTART.md](QUICKSTART.md) ⚡
**Mục đích:** Hướng dẫn chạy ứng dụng nhanh nhất có thể

**Nội dung:**
- Khởi động PostgreSQL và MinIO với Docker
- Build và run application
- Test với Web UI
- Test với cURL
- Troubleshooting nhanh

**Thời gian đọc:** 3-5 phút  
**Ai nên đọc:** Người mới bắt đầu, muốn test nhanh

---

### 2. [README.md](README.md) 📖
**Mục đích:** Tài liệu đầy đủ về project

**Nội dung:**
- Tính năng đầy đủ
- Yêu cầu hệ thống
- Cấu trúc project
- Hướng dẫn cài đặt chi tiết
- Configuration options
- API documentation overview
- Performance tuning
- Security considerations
- Further reading

**Thời gian đọc:** 15-20 phút  
**Ai nên đọc:** Tất cả mọi người

---

### 3. [API_EXAMPLES.md](API_EXAMPLES.md) 🌐
**Mục đích:** Ví dụ chi tiết về cách sử dụng APIs

**Nội dung:**
- User Management APIs + examples
- Group Management APIs + examples
- Messaging APIs + examples
- File Upload APIs + examples
- SSE connection examples (JavaScript)
- Complete workflow examples
- Error handling
- Tips and best practices

**Thời gian đọc:** 20-30 phút  
**Ai nên đọc:** Developers integrating với API

---

### 4. [PROJECT_STRUCTURE.md](PROJECT_STRUCTURE.md) 🏗️
**Mục đích:** Hiểu rõ cấu trúc code và kiến trúc

**Nội dung:**
- File và folder structure
- Số lượng files theo category
- Giải thích chi tiết từng component
- Database schema
- Data flow diagrams
- Technology stack
- Dependencies
- Performance tips

**Thời gian đọc:** 15-20 phút  
**Ai nên đọc:** Developers muốn đọc/modify code

---

## 🛠️ Các file khác

### [pom.xml](pom.xml)
Maven configuration với tất cả dependencies

### [docker-compose.yml](docker-compose.yml)
Docker Compose để chạy PostgreSQL, MinIO, và pgAdmin

### [.gitignore](.gitignore)
Git ignore rules cho Java/Maven project

### [test-api.sh](test-api.sh)
Bash script để test tất cả APIs tự động

### [postman-collection.json](postman-collection.json)
Postman collection để test APIs với GUI

### Configuration Files
- [application.yml](src/main/resources/application.yml) - Production config
- [application-dev.yml](src/main/resources/application-dev.yml) - Development config
- [schema.sql](src/main/resources/schema.sql) - Database schema

### Web UI
- [index.html](src/main/resources/static/index.html) - Web interface để test SSE và chat

---

## 🎓 Hướng dẫn học tập

### Level 1: Beginner (Chưa biết gì về project)
1. Đọc **QUICKSTART.md** để chạy app
2. Test với Web UI tại `http://localhost:8080`
3. Thử các API cơ bản với cURL hoặc Postman
4. Đọc qua **README.md** để hiểu overview

### Level 2: Intermediate (Đã chạy được app)
1. Đọc **API_EXAMPLES.md** để hiểu cách dùng APIs
2. Test các workflow examples
3. Thử tích hợp SSE vào JavaScript client
4. Xem **PROJECT_STRUCTURE.md** để hiểu code organization

### Level 3: Advanced (Muốn customize/extend)
1. Đọc kỹ **PROJECT_STRUCTURE.md**
2. Xem source code từng layer (Controller → Service → Repository)
3. Hiểu data flow và reactive programming với WebFlux
4. Customize features theo nhu cầu

---

## 🔍 Tìm kiếm nhanh

### Tôi muốn...

**...chạy app nhanh nhất?**
→ [QUICKSTART.md](QUICKSTART.md)

**...hiểu app làm gì?**
→ [README.md](README.md) - Features section

**...biết cách gọi API?**
→ [API_EXAMPLES.md](API_EXAMPLES.md)

**...tích hợp SSE?**
→ [API_EXAMPLES.md](API_EXAMPLES.md) - SSE section

**...upload file?**
→ [API_EXAMPLES.md](API_EXAMPLES.md) - File Upload section

**...hiểu code structure?**
→ [PROJECT_STRUCTURE.md](PROJECT_STRUCTURE.md)

**...config MinIO?**
→ [README.md](README.md) - Configuration section

**...test APIs?**
→ Run `./test-api.sh` hoặc import `postman-collection.json`

**...deploy production?**
→ [README.md](README.md) - Security Notes section

**...troubleshoot lỗi?**
→ [QUICKSTART.md](QUICKSTART.md) - Troubleshooting section

---

## 💡 Tips cho việc đọc docs

1. **Đọc theo thứ tự:** QUICKSTART → README → API_EXAMPLES → PROJECT_STRUCTURE
2. **Hands-on learning:** Chạy code trong khi đọc docs
3. **Bookmark:** Lưu link tới API_EXAMPLES.md để reference nhanh
4. **Test ngay:** Mỗi khi đọc một API, test luôn với cURL hoặc Postman
5. **Code review:** Sau khi đọc docs, xem source code để hiểu sâu hơn

---

## 🎯 Use Cases theo vai trò

### Product Manager / Business Analyst
→ Đọc **README.md** để hiểu features và capabilities

### Frontend Developer
→ Tập trung vào **API_EXAMPLES.md**, đặc biệt SSE section

### Backend Developer
→ Đọc tất cả, chú ý **PROJECT_STRUCTURE.md** và source code

### DevOps Engineer
→ **docker-compose.yml**, **README.md** (Configuration), và deployment notes

### QA Engineer
→ **API_EXAMPLES.md**, **test-api.sh**, và **postman-collection.json**

---

## 📊 Project Statistics

- **Total Java Files:** 27
- **Lines of Code:** ~3,000+ (excluding comments)
- **API Endpoints:** 20+
- **Database Tables:** 5
- **Documentation Pages:** 5 (including this)
- **Technologies:** 10+

---

## 🔗 External Resources

### Spring Boot & WebFlux
- [Spring WebFlux Docs](https://docs.spring.io/spring-framework/docs/current/reference/html/web-reactive.html)
- [Project Reactor](https://projectreactor.io/)

### R2DBC
- [R2DBC Documentation](https://r2dbc.io/)
- [R2DBC PostgreSQL](https://github.com/pgjdbc/r2dbc-postgresql)

### Server-Sent Events
- [MDN SSE Guide](https://developer.mozilla.org/en-US/docs/Web/API/Server-sent_events)
- [HTML5 SSE Spec](https://html.spec.whatwg.org/multipage/server-sent-events.html)

### MinIO
- [MinIO Documentation](https://min.io/docs/)
- [MinIO Java SDK](https://github.com/minio/minio-java)

---

## 📮 Feedback & Support

Nếu bạn có câu hỏi hoặc gặp vấn đề:

1. Kiểm tra **QUICKSTART.md** - Troubleshooting section
2. Xem lại **API_EXAMPLES.md** cho ví dụ cụ thể
3. Review **PROJECT_STRUCTURE.md** để hiểu data flow
4. Tạo issue với thông tin chi tiết

---

## 📝 Cập nhật tài liệu

Docs này được tạo cùng với code. Nếu bạn modify code, nhớ update docs tương ứng:

- Thêm API mới → Update **API_EXAMPLES.md**
- Thêm config option → Update **README.md**
- Thay đổi structure → Update **PROJECT_STRUCTURE.md**
- Thêm feature → Update tất cả docs liên quan

---

## ✅ Checklist cho người mới

- [ ] Đọc **QUICKSTART.md**
- [ ] Start services với Docker Compose
- [ ] Build và run application
- [ ] Test Web UI tại http://localhost:8080
- [ ] Chạy `./test-api.sh`
- [ ] Import Postman collection
- [ ] Đọc **README.md**
- [ ] Đọc **API_EXAMPLES.md**
- [ ] Review **PROJECT_STRUCTURE.md**
- [ ] Xem source code
- [ ] Thử customize một feature
- [ ] Deploy lên server test

---

**Happy Coding! 🚀**

Nếu bạn thấy project này hữu ích, đừng quên để lại feedback!

---

*Last Updated: 2024-01-15*  
*Version: 1.0.0*
