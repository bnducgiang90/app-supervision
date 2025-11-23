#!/bin/bash

# Test script for Realtime Chat Application APIs
# Usage: ./test-api.sh

BASE_URL="http://localhost:8080"
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${YELLOW}========================================${NC}"
echo -e "${YELLOW}Realtime Chat Application - API Tests${NC}"
echo -e "${YELLOW}========================================${NC}\n"

# Function to test API endpoint
test_endpoint() {
    local method=$1
    local endpoint=$2
    local data=$3
    local description=$4
    
    echo -e "${YELLOW}Testing: ${description}${NC}"
    echo -e "Endpoint: ${method} ${endpoint}"
    
    if [ -z "$data" ]; then
        response=$(curl -s -w "\n%{http_code}" -X ${method} "${BASE_URL}${endpoint}")
    else
        response=$(curl -s -w "\n%{http_code}" -X ${method} "${BASE_URL}${endpoint}" \
            -H "Content-Type: application/json" \
            -d "${data}")
    fi
    
    http_code=$(echo "$response" | tail -n1)
    body=$(echo "$response" | sed '$d')
    
    if [[ $http_code -ge 200 && $http_code -lt 300 ]]; then
        echo -e "${GREEN}✓ Success (${http_code})${NC}"
        echo -e "Response: ${body}\n"
    else
        echo -e "${RED}✗ Failed (${http_code})${NC}"
        echo -e "Response: ${body}\n"
    fi
}

# Test 1: Create Users
echo -e "${YELLOW}========= User Management Tests =========${NC}\n"

test_endpoint "POST" "/api/users" \
    '{"username":"alice","displayName":"Alice Smith","avatarUrl":"https://example.com/alice.jpg"}' \
    "Create User: Alice"

test_endpoint "POST" "/api/users" \
    '{"username":"bob","displayName":"Bob Johnson","avatarUrl":"https://example.com/bob.jpg"}' \
    "Create User: Bob"

test_endpoint "POST" "/api/users" \
    '{"username":"charlie","displayName":"Charlie Brown"}' \
    "Create User: Charlie"

sleep 1

# Test 2: Get Users
test_endpoint "GET" "/api/users" "" \
    "Get All Users"

test_endpoint "GET" "/api/users/1" "" \
    "Get User by ID (1)"

test_endpoint "GET" "/api/users/username/alice" "" \
    "Get User by Username (alice)"

# Test 3: Update User Status
test_endpoint "PUT" "/api/users/1/status?status=ONLINE" "" \
    "Update User Status to ONLINE"

echo -e "\n${YELLOW}========= Group Management Tests =========${NC}\n"

# Test 4: Create Groups
test_endpoint "POST" "/api/groups" \
    '{"name":"Project Team","description":"Main project discussion","createdBy":1}' \
    "Create Group: Project Team"

test_endpoint "POST" "/api/groups" \
    '{"name":"Random Chat","description":"Random discussions","createdBy":1}' \
    "Create Group: Random Chat"

sleep 1

# Test 5: Add Members to Group
test_endpoint "POST" "/api/groups/1/members" \
    '{"userId":2,"role":"MEMBER"}' \
    "Add Bob to Project Team"

test_endpoint "POST" "/api/groups/1/members" \
    '{"userId":3,"role":"MEMBER"}' \
    "Add Charlie to Project Team"

sleep 1

# Test 6: Get Groups
test_endpoint "GET" "/api/groups/1" "" \
    "Get Group by ID (1)"

test_endpoint "GET" "/api/groups/user/1" "" \
    "Get User's Groups (User 1)"

test_endpoint "GET" "/api/groups/1/members" "" \
    "Get Group Members (Group 1)"

echo -e "\n${YELLOW}========= Message Tests =========${NC}\n"

# Test 7: Send Messages
test_endpoint "POST" "/api/messages" \
    '{"groupId":1,"senderId":1,"content":"Hello everyone!","messageType":"TEXT"}' \
    "Send Message from Alice"

test_endpoint "POST" "/api/messages" \
    '{"groupId":1,"senderId":2,"content":"Hi Alice! How are you?","messageType":"TEXT"}' \
    "Send Message from Bob"

test_endpoint "POST" "/api/messages" \
    '{"groupId":1,"senderId":3,"content":"Hey guys, what are we working on?","messageType":"TEXT"}' \
    "Send Message from Charlie"

sleep 1

# Test 8: Get Messages
test_endpoint "GET" "/api/messages/group/1?page=0&size=10" "" \
    "Get Group Messages (Group 1)"

test_endpoint "GET" "/api/messages/1" "" \
    "Get Message by ID (1)"

echo -e "\n${YELLOW}========= SSE Status Tests =========${NC}\n"

# Test 9: SSE Status
test_endpoint "GET" "/api/sse/status" "" \
    "Get SSE Connection Status"

test_endpoint "GET" "/api/sse/status/1" "" \
    "Get User SSE Status (User 1)"

echo -e "\n${YELLOW}========================================${NC}"
echo -e "${GREEN}API Tests Completed!${NC}"
echo -e "${YELLOW}========================================${NC}\n"

echo -e "To test SSE streaming, open your browser and go to:"
echo -e "${GREEN}http://localhost:8080${NC}\n"

echo -e "Or use this JavaScript code in browser console:"
echo -e "${YELLOW}const eventSource = new EventSource('http://localhost:8080/api/sse/stream?userId=1');${NC}"
echo -e "${YELLOW}eventSource.addEventListener('new_message', (e) => console.log(JSON.parse(e.data)));${NC}\n"
