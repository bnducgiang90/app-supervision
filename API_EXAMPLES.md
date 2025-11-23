# API Examples - Realtime Chat Application

Tài liệu này cung cấp các ví dụ chi tiết về cách sử dụng APIs của ứng dụng chat.

## Table of Contents

- [User Management](#user-management)
- [Group Management](#group-management)
- [Messaging](#messaging)
- [File Upload](#file-upload)
- [Server-Sent Events (SSE)](#server-sent-events-sse)
- [Complete Workflow Examples](#complete-workflow-examples)

---

## User Management

### 1. Create a New User

**Request:**
```bash
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{
    "username": "johndoe",
    "displayName": "John Doe",
    "avatarUrl": "https://example.com/avatars/johndoe.jpg"
  }'
```

**Response:**
```json
{
  "id": 1,
  "username": "johndoe",
  "displayName": "John Doe",
  "avatarUrl": "https://example.com/avatars/johndoe.jpg",
  "status": "ONLINE",
  "createdAt": "2024-01-15T10:30:00"
}
```

### 2. Get All Users

**Request:**
```bash
curl http://localhost:8080/api/users
```

**Response:**
```json
[
  {
    "id": 1,
    "username": "johndoe",
    "displayName": "John Doe",
    "status": "ONLINE",
    "createdAt": "2024-01-15T10:30:00"
  },
  {
    "id": 2,
    "username": "janedoe",
    "displayName": "Jane Doe",
    "status": "OFFLINE",
    "createdAt": "2024-01-15T11:00:00"
  }
]
```

### 3. Get User by ID

**Request:**
```bash
curl http://localhost:8080/api/users/1
```

### 4. Get User by Username

**Request:**
```bash
curl http://localhost:8080/api/users/username/johndoe
```

### 5. Update User Status

**Request:**
```bash
curl -X PUT "http://localhost:8080/api/users/1/status?status=AWAY"
```

**Available statuses:** `ONLINE`, `OFFLINE`, `AWAY`, `BUSY`

---

## Group Management

### 1. Create a Group

**Request:**
```bash
curl -X POST http://localhost:8080/api/groups \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Engineering Team",
    "description": "Main engineering discussion group",
    "avatarUrl": "https://example.com/groups/engineering.jpg",
    "createdBy": 1
  }'
```

**Response:**
```json
{
  "id": 1,
  "name": "Engineering Team",
  "description": "Main engineering discussion group",
  "avatarUrl": "https://example.com/groups/engineering.jpg",
  "createdBy": 1,
  "memberCount": 1,
  "createdAt": "2024-01-15T12:00:00",
  "updatedAt": "2024-01-15T12:00:00"
}
```

### 2. Add Member to Group

**Request:**
```bash
curl -X POST http://localhost:8080/api/groups/1/members \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 2,
    "role": "MEMBER"
  }'
```

**Available roles:** `ADMIN`, `MODERATOR`, `MEMBER`

**Response:** `201 Created` (no body)

### 3. Get Group by ID

**Request:**
```bash
curl http://localhost:8080/api/groups/1
```

### 4. Get User's Groups

**Request:**
```bash
curl http://localhost:8080/api/groups/user/1
```

**Response:**
```json
[
  {
    "id": 1,
    "name": "Engineering Team",
    "description": "Main engineering discussion group",
    "memberCount": 5,
    "createdAt": "2024-01-15T12:00:00"
  },
  {
    "id": 2,
    "name": "Project Alpha",
    "description": "Project Alpha team",
    "memberCount": 3,
    "createdAt": "2024-01-15T13:00:00"
  }
]
```

### 5. Get Group Members

**Request:**
```bash
curl http://localhost:8080/api/groups/1/members
```

**Response:**
```json
[
  {
    "id": 1,
    "username": "johndoe",
    "displayName": "John Doe",
    "avatarUrl": "https://example.com/avatars/johndoe.jpg",
    "status": "ONLINE"
  },
  {
    "id": 2,
    "username": "janedoe",
    "displayName": "Jane Doe",
    "status": "ONLINE"
  }
]
```

### 6. Remove Member from Group

**Request:**
```bash
curl -X DELETE http://localhost:8080/api/groups/1/members/2
```

**Response:** `204 No Content`

---

## Messaging

### 1. Send Text Message

**Request:**
```bash
curl -X POST http://localhost:8080/api/messages \
  -H "Content-Type: application/json" \
  -d '{
    "groupId": 1,
    "senderId": 1,
    "content": "Hello everyone! How is the project going?",
    "messageType": "TEXT"
  }'
```

**Response:**
```json
{
  "id": 1,
  "groupId": 1,
  "senderId": 1,
  "senderName": "John Doe",
  "senderAvatar": "https://example.com/avatars/johndoe.jpg",
  "content": "Hello everyone! How is the project going?",
  "messageType": "TEXT",
  "attachments": [],
  "createdAt": "2024-01-15T14:30:00"
}
```

### 2. Get Group Messages (with Pagination)

**Request:**
```bash
# Get first page (50 messages)
curl "http://localhost:8080/api/messages/group/1?page=0&size=50"

# Get second page
curl "http://localhost:8080/api/messages/group/1?page=1&size=50"

# Get all recent messages (default)
curl http://localhost:8080/api/messages/group/1
```

**Response:**
```json
[
  {
    "id": 1,
    "groupId": 1,
    "senderId": 1,
    "senderName": "John Doe",
    "content": "Hello everyone!",
    "messageType": "TEXT",
    "attachments": [],
    "createdAt": "2024-01-15T14:30:00"
  },
  {
    "id": 2,
    "groupId": 1,
    "senderId": 2,
    "senderName": "Jane Doe",
    "content": "Hi John! All good here.",
    "messageType": "TEXT",
    "attachments": [],
    "createdAt": "2024-01-15T14:31:00"
  }
]
```

### 3. Get Single Message

**Request:**
```bash
curl http://localhost:8080/api/messages/1
```

---

## File Upload

### 1. Send Message with Single Image

**Request:**
```bash
curl -X POST http://localhost:8080/api/messages/upload \
  -F 'message={"groupId":1,"senderId":1,"content":"Check out this screenshot!","messageType":"IMAGE"}' \
  -F 'files=@/path/to/screenshot.png'
```

### 2. Send Message with Multiple Files

**Request:**
```bash
curl -X POST http://localhost:8080/api/messages/upload \
  -F 'message={"groupId":1,"senderId":1,"content":"Project photos","messageType":"IMAGE"}' \
  -F 'files=@/path/to/photo1.jpg' \
  -F 'files=@/path/to/photo2.jpg' \
  -F 'files=@/path/to/photo3.jpg'
```

### 3. Send Video Message

**Request:**
```bash
curl -X POST http://localhost:8080/api/messages/upload \
  -F 'message={"groupId":1,"senderId":1,"content":"Demo video","messageType":"VIDEO"}' \
  -F 'files=@/path/to/demo.mp4'
```

**Response với attachments:**
```json
{
  "id": 5,
  "groupId": 1,
  "senderId": 1,
  "senderName": "John Doe",
  "content": "Check out this screenshot!",
  "messageType": "IMAGE",
  "attachments": [
    {
      "id": 1,
      "fileName": "screenshot.png",
      "fileType": "image/png",
      "fileSize": 245678,
      "fileUrl": "http://localhost:8080/api/files/messages/5/abc-123.png",
      "thumbnailUrl": null
    }
  ],
  "createdAt": "2024-01-15T15:00:00"
}
```

---

## Server-Sent Events (SSE)

### 1. Connect to SSE Stream (JavaScript)

```javascript
// Establish SSE connection
const eventSource = new EventSource('http://localhost:8080/api/sse/stream?userId=1');

// Handle connection event
eventSource.addEventListener('connected', (event) => {
    const data = JSON.parse(event.data);
    console.log('Connected to chat:', data);
});

// Handle new message events
eventSource.addEventListener('new_message', (event) => {
    const message = JSON.parse(event.data);
    console.log('New message received:', message.data);
    
    // Display message in UI
    displayMessage(message.data);
});

// Handle member added events
eventSource.addEventListener('member_added', (event) => {
    const data = JSON.parse(event.data);
    console.log('New member added to group:', data.data);
});

// Handle member removed events
eventSource.addEventListener('member_removed', (event) => {
    const data = JSON.parse(event.data);
    console.log('Member removed from group:', data.data);
});

// Handle group created events
eventSource.addEventListener('group_created', (event) => {
    const data = JSON.parse(event.data);
    console.log('New group created:', data.data);
});

// Handle heartbeat
eventSource.addEventListener('heartbeat', (event) => {
    console.log('Heartbeat:', event.data);
});

// Handle errors
eventSource.onerror = (error) => {
    console.error('SSE Error:', error);
    // Reconnect logic here
};

// Close connection
// eventSource.close();
```

### 2. SSE Event Types

| Event Type | Description | Data Structure |
|------------|-------------|----------------|
| `connected` | Initial connection established | `{message, userId}` |
| `new_message` | New message sent to group | `MessageResponse` object |
| `member_added` | Member added to group | `GroupResponse` object |
| `member_removed` | Member removed from group | `{groupId, userId}` |
| `group_created` | New group created | `GroupResponse` object |
| `heartbeat` | Keep-alive signal | `{timestamp}` |

### 3. Check SSE Status

**Get overall status:**
```bash
curl http://localhost:8080/api/sse/status
```

**Response:**
```json
{
  "connectedUsers": 5,
  "status": "running"
}
```

**Check specific user:**
```bash
curl http://localhost:8080/api/sse/status/1
```

**Response:**
```json
{
  "userId": 1,
  "connected": true
}
```

### 4. Disconnect User (Admin)

```bash
curl -X DELETE http://localhost:8080/api/sse/disconnect/1
```

---

## Complete Workflow Examples

### Example 1: Create a Team Chat

```bash
# Step 1: Create users
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{"username":"alice","displayName":"Alice Smith"}'

curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{"username":"bob","displayName":"Bob Johnson"}'

curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{"username":"charlie","displayName":"Charlie Brown"}'

# Step 2: Create group
curl -X POST http://localhost:8080/api/groups \
  -H "Content-Type: application/json" \
  -d '{"name":"Product Team","description":"Product discussions","createdBy":1}'

# Step 3: Add members
curl -X POST http://localhost:8080/api/groups/1/members \
  -H "Content-Type: application/json" \
  -d '{"userId":2,"role":"MEMBER"}'

curl -X POST http://localhost:8080/api/groups/1/members \
  -H "Content-Type: application/json" \
  -d '{"userId":3,"role":"MEMBER"}'

# Step 4: Send welcome message
curl -X POST http://localhost:8080/api/messages \
  -H "Content-Type: application/json" \
  -d '{"groupId":1,"senderId":1,"content":"Welcome to the team!","messageType":"TEXT"}'
```

### Example 2: Share Project Files

```bash
# Step 1: Send message with design files
curl -X POST http://localhost:8080/api/messages/upload \
  -F 'message={"groupId":1,"senderId":1,"content":"Latest designs","messageType":"IMAGE"}' \
  -F 'files=@designs/mockup1.png' \
  -F 'files=@designs/mockup2.png'

# Step 2: Send project video
curl -X POST http://localhost:8080/api/messages/upload \
  -F 'message={"groupId":1,"senderId":1,"content":"Demo video","messageType":"VIDEO"}' \
  -F 'files=@videos/demo.mp4'

# Step 3: Get all messages to see files
curl http://localhost:8080/api/messages/group/1
```

### Example 3: Real-time Chat Session

```javascript
// User 1 connects
const user1SSE = new EventSource('http://localhost:8080/api/sse/stream?userId=1');
user1SSE.addEventListener('new_message', (e) => {
    console.log('User 1 received:', JSON.parse(e.data));
});

// User 2 connects
const user2SSE = new EventSource('http://localhost:8080/api/sse/stream?userId=2');
user2SSE.addEventListener('new_message', (e) => {
    console.log('User 2 received:', JSON.parse(e.data));
});

// User 1 sends message - User 2 will receive via SSE
fetch('http://localhost:8080/api/messages', {
    method: 'POST',
    headers: {'Content-Type': 'application/json'},
    body: JSON.stringify({
        groupId: 1,
        senderId: 1,
        content: 'Hey Bob, are you there?',
        messageType: 'TEXT'
    })
});

// User 2 replies - User 1 will receive via SSE
fetch('http://localhost:8080/api/messages', {
    method: 'POST',
    headers: {'Content-Type': 'application/json'},
    body: JSON.stringify({
        groupId: 1,
        senderId: 2,
        content: 'Yes! What's up?',
        messageType: 'TEXT'
    })
});
```

---

## Error Handling

### Common Error Responses

**400 Bad Request - Validation Error:**
```json
{
  "timestamp": "2024-01-15T16:00:00",
  "status": 400,
  "error": "Validation Failed",
  "fieldErrors": {
    "username": "Username is required",
    "displayName": "Display name is required"
  }
}
```

**404 Not Found:**
```json
{
  "timestamp": "2024-01-15T16:00:00",
  "status": 404,
  "error": "Not Found",
  "message": "User not found"
}
```

**500 Internal Server Error:**
```json
{
  "timestamp": "2024-01-15T16:00:00",
  "status": 500,
  "error": "Internal Server Error",
  "message": "An unexpected error occurred"
}
```

---

## Tips and Best Practices

1. **Always connect to SSE before sending messages** to receive real-time updates
2. **Use pagination** for message history to avoid loading too much data
3. **Handle SSE reconnection** in case of network issues
4. **Validate file types and sizes** before uploading
5. **Close SSE connections** when not needed to free up resources
6. **Use appropriate message types** (TEXT, IMAGE, VIDEO, FILE) for better organization

---

## Testing Tools

- **cURL**: Command-line testing (shown in examples above)
- **Postman**: Import `postman-collection.json` for GUI testing
- **Browser DevTools**: Monitor SSE events in Network tab
- **Web UI**: Use built-in web interface at `http://localhost:8080`

---

For more information, see [README.md](README.md) and [QUICKSTART.md](QUICKSTART.md).
