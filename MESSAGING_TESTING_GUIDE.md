# Messaging System Integration Testing Guide

## Overview
This guide provides step-by-step instructions to test the complete messaging system integration between the Android app and PHP backend.

---

## Prerequisites

### Backend Setup (PHP)
1. **Database Tables Created:**
   - `users` - User accounts with role-based access
   - `alerts` - Emergency alerts
   - `conversations` - Message threads tied to alerts
   - `messages` - Individual messages

2. **PHP API Files Configured:**
   ```
   E:\XAMPP\htdocs\PHP\api\
   ├── conversations/
   │   ├── create.php   ✅ Fixed syntax errors
   │   └── list.php     ✅ Fixed syntax errors
   ├── messages/
   │   ├── send.php     ✅ Fixed syntax errors
   │   └── list.php     ✅ OK
   └── db_connect.php   (PDO database connection)
   ```

3. **Server Running:**
   - XAMPP Apache enabled
   - MySQL database accessible
   - PHP version >= 7.4

### Android App Setup
1. Network configuration updated to your IP:
   - Emulator: `http://10.0.2.2/PHP/api/`
   - Physical Device: `http://192.168.1.2/PHP/api/`

2. Dependencies installed:
   - Retrofit 2.9.0
   - Gson converter
   - OkHttp logging interceptor

---

## API Endpoints

### 1. Create Conversation
**Endpoint:** `POST /api/conversations/create`

**Request Body (JSON):**
```json
{
  "alert_id": 1,
  "user_id": 5
}
```

**Success Response (200):**
```json
{
  "success": true,
  "message": "Conversation created",
  "conversation": {
    "id": 12,
    "alert_id": 1,
    "created_by": 5,
    "created_at": "2025-12-22 14:30:00"
  }
}
```

**Error Response (400):**
```json
{
  "success": false,
  "message": "alert_id and user_id are required and must be positive integers"
}
```

---

### 2. Send Message
**Endpoint:** `POST /api/messages/send`

**Request Body (Form-Encoded):**
```
conversation_id=12&sender_id=5&content=Hello+responder
```

**Success Response (200):**
```json
{
  "success": true,
  "message": "Message sent",
  "data": {
    "id": 42,
    "conversation_id": 12,
    "sender_id": 5,
    "content": "Hello responder",
    "created_at": "2025-12-22 14:35:00"
  }
}
```

**Error Response (400):**
```json
{
  "success": false,
  "message": "conversation_id, sender_id and content are required"
}
```

---

### 3. Fetch Messages (Polling)
**Endpoint:** `GET /api/messages/list?conversation_id=12&last_message_id=0`

**Query Parameters:**
- `conversation_id` (required): ID of the conversation
- `last_message_id` (optional): Fetch only messages after this ID

**Success Response (200):**
```json
{
  "success": true,
  "message": "OK",
  "messages": [
    {
      "id": 40,
      "sender_id": 5,
      "content": "Help needed!",
      "created_at": "2025-12-22 14:30:00"
    },
    {
      "id": 41,
      "sender_id": 3,
      "content": "We are on the way",
      "created_at": "2025-12-22 14:31:00"
    }
  ]
}
```

---

### 4. List Conversations
**Endpoint:** `GET /api/conversations/list?alert_id=1&user_id=5&role=user`

**Query Parameters:**
- `alert_id` (required): Alert ID
- `user_id` (optional): User ID (required if not admin/responder)
- `role` (optional): User role (admin, responder, user)

---

## Testing Workflow

### Scenario 1: User Messages Alert Responder
1. **User clicks "Message Responder" button on Dashboard**
   - AlertId = 0 (special case for direct messaging)
   - UserId = Current authenticated user
   - Navigate to MessagingScreen

2. **Backend creates conversation (if not exists)**
   ```
   POST /api/conversations/create
   {"alert_id": 0, "user_id": 5}
   ```
   Response: conversation_id = 12

3. **User types and sends message**
   - Input: "I need help with the evacuation"
   - Button clicks calls sendMessage()

4. **Backend receives message**
   ```
   POST /api/messages/send
   conversation_id=12&sender_id=5&content=I+need+help+with+the+evacuation
   ```

5. **App polls for new messages every 3-5 seconds**
   ```
   GET /api/messages/list?conversation_id=12&last_message_id=42
   ```

6. **Responder replies via web admin panel**
   - Message stored in database
   - Next poll retrieves it

7. **App displays response in chat UI**
   - Sender name shown
   - Timestamp displayed
   - Message content visible

---

### Scenario 2: User Messages Specific Alert
1. **User goes to Alerts screen**
2. **User sees alert: "Typhoon Warning"**
3. **User clicks "Message" button on alert card**
   - AlertId = 1 (from alert)
   - Navigate to MessagingScreen with alert context

4. **Same flow as Scenario 1, but with alertId=1**
   - Creates conversation for that specific alert
   - Responders can see all conversations for that alert

---

## Manual Testing Checklist

### ✅ Android App Tests

- [ ] Dashboard loads without crashes
- [ ] "Message Responder" button appears below action grid
- [ ] Clicking button navigates to MessagingScreen
- [ ] MessagingScreen shows loading indicator while fetching messages
- [ ] User can type message in text field
- [ ] Send button enabled only when text is not empty
- [ ] Sent message appears in chat immediately
- [ ] Messages poll updates every 5 seconds
- [ ] New responder messages appear in real-time
- [ ] Back button returns to dashboard
- [ ] Alerts screen loads with "Message" button on each alert
- [ ] Clicking alert message button navigates to messaging with alert context
- [ ] Network errors display user-friendly error message

### ✅ Backend Tests

Use Postman or curl to test:

**Create Conversation:**
```bash
curl -X POST http://192.168.1.2/PHP/api/conversations/create \
  -H "Content-Type: application/json" \
  -d '{"alert_id":0,"user_id":5}'
```

**Send Message:**
```bash
curl -X POST http://192.168.1.2/PHP/api/messages/send \
  -d "conversation_id=12&sender_id=5&content=Test%20message"
```

**Fetch Messages:**
```bash
curl http://192.168.1.2/PHP/api/messages/list?conversation_id=12&last_message_id=0
```

### ✅ Database Tests

Check database entries:
```sql
-- View conversations
SELECT * FROM conversations WHERE alert_id IN (0, 1);

-- View messages
SELECT * FROM messages WHERE conversation_id = 12;

-- Check timestamps
SELECT id, sender_id, message, sent_at FROM messages ORDER BY sent_at DESC LIMIT 10;
```

---

## Common Issues & Solutions

### Issue 1: 404 Not Found on API Calls
**Cause:** Incorrect base URL or endpoint path
**Solution:** 
- Verify IP address in RetrofitClient.kt
- Check PHP file location matches endpoint
- Ensure Apache is running

### Issue 2: Empty Messages List
**Cause:** Conversation doesn't exist or no messages sent
**Solution:**
- Create conversation first via create.php
- Check conversation_id is correct
- Verify sender_id exists in users table

### Issue 3: Message Not Sending
**Cause:** Validation error or DB connection issue
**Solution:**
- Ensure conversation_id > 0
- Ensure sender_id > 0
- Ensure content is not empty
- Check db_connect.php credentials

### Issue 4: Slow Polling Response
**Cause:** Polling interval too short or network lag
**Solution:**
- Increase polling interval to 5 seconds
- Check network connectivity
- Monitor server logs for slow queries

---

## Performance Optimization Tips

1. **Polling Strategy:**
   - Start with 5-second interval
   - Increase to 10 seconds if no new messages
   - Decrease to 2 seconds when user is actively typing

2. **Database Queries:**
   - Add index on `messages.conversation_id`
   - Add index on `conversations.alert_id`
   - Regular cleanup of old conversations (optional)

3. **Network:**
   - Compress JSON responses
   - Implement response caching
   - Use gzip encoding

---

## Success Criteria

✅ **All tests pass when:**
1. User can send messages from dashboard
2. User can send messages from alert-specific conversation
3. Messages appear immediately on send
4. Responder messages are polled and displayed
5. No database errors in logs
6. API responses under 1 second
7. App doesn't crash on network errors

---

## Next Steps

1. Test with real user accounts
2. Load test with multiple conversations
3. Implement message read receipts
4. Add typing indicators
5. Implement push notifications (optional)
6. Add message search functionality
7. Implement conversation archiving

---

**Last Updated:** December 22, 2025  
**Status:** Ready for Testing

