# Project Structure - Realtime Chat Application

## 📂 Overview

Project gồm **27 Java classes** và nhiều file configuration, documentation.

```
realtime-chat-app/
├── 📄 pom.xml                          # Maven dependencies
├── 📄 docker-compose.yml               # PostgreSQL + MinIO services
├── 📄 .gitignore                       # Git ignore rules
├── 📄 README.md                        # Complete documentation
├── 📄 QUICKSTART.md                    # 5-minute quick start guide
├── 📄 API_EXAMPLES.md                  # Detailed API examples
├── 📄 PROJECT_STRUCTURE.md             # This file
├── 📄 postman-collection.json          # Postman collection
├── 📄 test-api.sh                      # API test script
│
├── 📁 src/main/
│   ├── 📁 java/com/chatapp/
│   │   │
│   │   ├── 📄 RealtimeChatApplication.java    # Main Spring Boot app
│   │   │
│   │   ├── 📁 config/                          # 🔧 Configuration
│   │   │   ├── MinioConfig.java               # MinIO setup
│   │   │   ├── R2dbcConfig.java               # R2DBC PostgreSQL
│   │   │   ├── StorageProperties.java         # Storage config properties
│   │   │   └── WebConfig.java                 # CORS & Codec config
│   │   │
│   │   ├── 📁 controller/                      # 🌐 REST Controllers (5 files)
│   │   │   ├── FileController.java            # Serve uploaded files
│   │   │   ├── GroupController.java           # Group management APIs
│   │   │   ├── MessageController.java         # Message APIs
│   │   │   ├── SSEController.java             # SSE streaming endpoint
│   │   │   └── UserController.java            # User management APIs
│   │   │
│   │   ├── 📁 dto/                             # 📦 Data Transfer Objects
│   │   │   └── DTOs.java                      # All request/response DTOs
│   │   │
│   │   ├── 📁 entity/                          # 🗄️ Database Entities (5 files)
│   │   │   ├── Group.java                     # Group entity
│   │   │   ├── GroupMember.java               # Group-User relationship
│   │   │   ├── Message.java                   # Message entity
│   │   │   ├── MessageAttachment.java         # File attachments
│   │   │   └── User.java                      # User entity
│   │   │
│   │   ├── 📁 exception/                       # ⚠️ Error Handling
│   │   │   └── GlobalExceptionHandler.java    # Global exception handler
│   │   │
│   │   ├── 📁 repository/                      # 🗃️ R2DBC Repositories (5 files)
│   │   │   ├── GroupMemberRepository.java
│   │   │   ├── GroupRepository.java
│   │   │   ├── MessageAttachmentRepository.java
│   │   │   ├── MessageRepository.java
│   │   │   └── UserRepository.java
│   │   │
│   │   └── 📁 service/                         # 💼 Business Logic (5 files)
│   │       ├── FileStorageService.java        # MinIO + Local storage (2 impls)
│   │       ├── GroupService.java              # Group operations
│   │       ├── MessageService.java            # Message operations
│   │       ├── SSEService.java                # SSE connection management
│   │       └── UserService.java               # User operations
│   │
│   └── 📁 resources/
│       ├── 📄 application.yml                  # Main configuration
│       ├── 📄 application-dev.yml              # Dev configuration
│       ├── 📄 schema.sql                       # Database schema
│       └── 📁 static/
│           └── 📄 index.html                   # Web UI client
│
└── 📁 src/test/                                # Tests (optional to add)
```

## 📊 File Count Summary

| Category | Count | Description |
|----------|-------|-------------|
| **Java Files** | 27 | Main application code |
| **Config Files** | 4 | Application & environment configs |
| **Controllers** | 5 | REST API endpoints |
| **Entities** | 5 | Database models |
| **Repositories** | 5 | Data access layer |
| **Services** | 5 | Business logic |
| **DTOs** | 1 file | Multiple DTO classes |
| **Documentation** | 4 | README, QUICKSTART, API_EXAMPLES, STRUCTURE |
| **Other** | 6 | pom.xml, docker-compose, scripts, etc. |

## 🔑 Key Components Explained

### 1. Configuration Layer (`config/`)

**MinioConfig.java**
- Khởi tạo MinIO client
- Auto-create bucket nếu cần
- Chỉ load khi `storage.type=minio`

**R2dbcConfig.java**
- Configure R2DBC connection pool
- Initialize database schema tự động

**StorageProperties.java**
- Bind properties từ application.yml
- Support cả MinIO và Local storage

**WebConfig.java**
- CORS configuration
- Codec max-in-memory-size settings

### 2. Controller Layer (`controller/`)

**UserController.java**
- `POST /api/users` - Create user
- `GET /api/users/{id}` - Get user
- `GET /api/users` - List all users
- `PUT /api/users/{id}/status` - Update status

**GroupController.java**
- `POST /api/groups` - Create group
- `GET /api/groups/{id}` - Get group
- `POST /api/groups/{groupId}/members` - Add member
- `DELETE /api/groups/{groupId}/members/{userId}` - Remove member
- `GET /api/groups/{groupId}/members` - List members

**MessageController.java**
- `POST /api/messages` - Send text message
- `POST /api/messages/upload` - Send with files
- `GET /api/messages/group/{groupId}` - Get history
- `GET /api/messages/{id}` - Get single message

**SSEController.java**
- `GET /api/sse/stream?userId={id}` - SSE connection
- `GET /api/sse/status` - Connection status
- `DELETE /api/sse/disconnect/{userId}` - Disconnect

**FileController.java**
- `GET /api/files/**` - Serve uploaded files (local storage only)

### 3. Service Layer (`service/`)

**FileStorageService.java**
- Interface với 2 implementations:
  - `MinioFileStorageService` - MinIO storage
  - `LocalFileStorageService` - Local file system
- Upload, download, delete files

**SSEService.java**
- Quản lý SSE connections (Map<UserId, Sink>)
- Broadcast messages tới users/groups
- Heartbeat để keep-alive connections

**UserService.java**
- CRUD operations cho users
- Update user status

**GroupService.java**
- Group CRUD operations
- Member management
- Send notifications via SSE

**MessageService.java**
- Send messages (text + attachments)
- File upload handling
- Message history với pagination

### 4. Repository Layer (`repository/`)

Tất cả extends `R2dbcRepository<T, Long>`:
- Reactive database access
- Custom queries với `@Query`
- Automatic CRUD operations

### 5. Entity Layer (`entity/`)

**User.java**
- id, username, displayName, avatarUrl, status
- Enum: UserStatus (ONLINE, OFFLINE, AWAY, BUSY)

**Group.java**
- id, name, description, avatarUrl, createdBy

**GroupMember.java**
- id, groupId, userId, role, joinedAt
- Enum: MemberRole (ADMIN, MODERATOR, MEMBER)

**Message.java**
- id, groupId, senderId, content, messageType
- Enum: MessageType (TEXT, IMAGE, VIDEO, FILE)

**MessageAttachment.java**
- id, messageId, fileName, fileType, fileSize, fileUrl, storagePath

### 6. DTO Layer (`dto/`)

**Request DTOs:**
- CreateUserRequest
- CreateGroupRequest
- AddMemberRequest
- SendMessageRequest

**Response DTOs:**
- UserResponse
- GroupResponse
- MessageResponse
- AttachmentResponse
- SSEMessage
- FileUploadResponse

## 🗄️ Database Schema

**5 Tables:**
1. `users` - User information
2. `groups` - Group information
3. `group_members` - Many-to-many relationship
4. `messages` - Chat messages
5. `message_attachments` - File metadata

**Relationships:**
- User 1:N Groups (creator)
- User N:M Groups (via group_members)
- Message N:1 Group
- Message N:1 User (sender)
- Message 1:N Attachments

## 🔄 Data Flow

### Sending a Message:

```
1. Client → POST /api/messages
2. MessageController.sendMessage()
3. MessageService.sendMessage()
   ├─→ Validate user is in group
   ├─→ Save message to DB (R2DBC)
   ├─→ Build MessageResponse
   └─→ SSEService.sendToGroup() → Broadcast to connected users
4. Return MessageResponse
5. All connected users receive via SSE
```

### File Upload:

```
1. Client → POST /api/messages/upload (multipart)
2. MessageController.sendMessageWithFiles()
3. MessageService.sendMessageWithAttachments()
   ├─→ Save message to DB
   ├─→ For each file:
   │   ├─→ FileStorageService.uploadFile()
   │   │   ├─→ [MinIO] Upload to MinIO bucket
   │   │   └─→ [Local] Save to local directory
   │   └─→ Save attachment metadata to DB
   └─→ SSEService broadcast
4. Return MessageResponse with attachments
```

### SSE Connection:

```
1. Client → EventSource('GET /api/sse/stream?userId=1')
2. SSEController.streamEvents()
3. SSEService.subscribe(userId)
   ├─→ Create Flux<ServerSentEvent>
   ├─→ Store user sink in Map
   ├─→ Send 'connected' event
   └─→ Merge with heartbeat Flux
4. Keep connection alive
5. Receive events: new_message, member_added, etc.
```

## 🛠️ Technology Stack

| Layer | Technology |
|-------|-----------|
| Language | Java 21 |
| Framework | Spring Boot 3.2 + WebFlux |
| Database | PostgreSQL 16 |
| Database Driver | R2DBC PostgreSQL |
| Object Storage | MinIO / Local FS |
| Real-time | Server-Sent Events (SSE) |
| Build Tool | Maven |
| Container | Docker Compose |

## 📝 Configuration Files

**application.yml** - Production config
- R2DBC connection
- MinIO settings
- SSE timeout
- Storage type

**application-dev.yml** - Development config
- Debug logging
- Longer timeouts
- Local storage default

**schema.sql** - Database initialization
- CREATE TABLE statements
- Indexes
- Sample data

## 🚀 Running the Application

**Option 1: Maven**
```bash
mvn spring-boot:run
```

**Option 2: JAR**
```bash
mvn clean package
java -jar target/realtime-chat-1.0.0.jar
```

**Option 3: With profile**
```bash
java -jar target/realtime-chat-1.0.0.jar --spring.profiles.active=dev
```

## 🧪 Testing

**Web UI:**
- Open `http://localhost:8080`
- Test SSE + messaging in browser

**cURL:**
- Run `./test-api.sh` for automated tests
- Or use individual cURL commands

**Postman:**
- Import `postman-collection.json`
- Test all APIs with GUI

## 📦 Dependencies

**Core:**
- spring-boot-starter-webflux
- spring-boot-starter-data-r2dbc
- r2dbc-postgresql

**Storage:**
- minio (8.5.7)

**Utilities:**
- lombok
- jackson-datatype-jsr310
- spring-boot-starter-validation

## 🔐 Security Considerations

**Current implementation:**
- ✅ Basic validation
- ✅ Exception handling
- ✅ CORS enabled
- ❌ No authentication
- ❌ No authorization

**For production, add:**
- JWT/OAuth2 authentication
- Role-based access control
- API rate limiting
- File upload validation
- HTTPS/WSS
- Input sanitization

## 🎯 Features Implemented

- ✅ User management
- ✅ Group chat
- ✅ Real-time messaging (SSE)
- ✅ File upload (images/videos)
- ✅ Message history with pagination
- ✅ Dual storage (MinIO/Local)
- ✅ R2DBC reactive database
- ✅ Exception handling
- ✅ Web UI client
- ✅ API documentation

## 📈 Performance Tips

**R2DBC Pool:**
- Adjust initial-size và max-size based on load
- Monitor connection leaks

**SSE Connections:**
- Set appropriate timeouts
- Implement reconnection logic
- Clean up disconnected users

**File Upload:**
- Limit file size
- Validate file types
- Use streaming for large files

**Database:**
- Use indexes on frequently queried columns
- Implement caching for hot data
- Use pagination for large result sets

---

**For detailed API usage, see [API_EXAMPLES.md](API_EXAMPLES.md)**

**For quick start, see [QUICKSTART.md](QUICKSTART.md)**
