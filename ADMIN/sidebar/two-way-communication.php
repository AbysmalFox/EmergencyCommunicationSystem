<?php
/**
 * Two-Way Communication Interface Page
 * Manage interactive communication between administrators and citizens
 */

$pageTitle = 'Two-Way Communication Interface';
?>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title><?php echo htmlspecialchars($pageTitle); ?></title>
    <link rel="icon" type="image/x-icon" href="images/favicon.ico">
    <link rel="stylesheet" href="css/global.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css">
    <link rel="stylesheet" href="css/sidebar.css">
    <link rel="stylesheet" href="css/admin-header.css">
    <link rel="stylesheet" href="css/buttons.css">
    <link rel="stylesheet" href="css/forms.css">
    <link rel="stylesheet" href="css/hero.css">
    <link rel="stylesheet" href="css/sidebar-footer.css">
    <link rel="stylesheet" href="css/modules.css">
    <style>
        .communication-container {
            display: grid;
            grid-template-columns: 300px 1fr;
            gap: 1.5rem;
            height: calc(100vh - 300px);
        }
        .conversations-list {
            background: var(--card-bg);
            border-radius: 8px;
            padding: 1rem;
            overflow-y: auto;
        }
        .conversation-item {
            padding: 1rem;
            border-bottom: 1px solid var(--border-color);
            cursor: pointer;
            transition: background 0.2s;
        }
        .conversation-item:hover {
            background: var(--hover-bg);
        }
        .conversation-item.active {
            background: var(--primary-color);
            color: white;
        }
        .chat-window {
            display: flex;
            flex-direction: column;
            background: var(--card-bg);
            border-radius: 8px;
            overflow: hidden;
        }
        .chat-header {
            padding: 1rem;
            border-bottom: 1px solid var(--border-color);
            background: var(--primary-color);
            color: white;
        }
        .chat-messages {
            flex: 1;
            padding: 1rem;
            overflow-y: auto;
            display: flex;
            flex-direction: column;
            gap: 1rem;
        }
        .message {
            display: flex;
            gap: 0.5rem;
            max-width: 70%;
        }
        .message.sent {
            align-self: flex-end;
            flex-direction: row-reverse;
        }
        .message.received {
            align-self: flex-start;
        }
        .message-content {
            padding: 0.75rem 1rem;
            border-radius: 12px;
            background: var(--border-color);
        }
        .message.sent .message-content {
            background: var(--primary-color);
            color: white;
        }
        .chat-input {
            padding: 1rem;
            border-top: 1px solid var(--border-color);
            display: flex;
            gap: 0.5rem;
        }
        .chat-input input {
            flex: 1;
            padding: 0.75rem;
            border: 1px solid var(--border-color);
            border-radius: 8px;
        }
    </style>
</head>
<body>
    <!-- Include Sidebar Component -->
    <?php include 'includes/sidebar.php'; ?>

    <!-- Include Admin Header Component -->
    <?php include 'includes/admin-header.php'; ?>
    
    <!-- ===================================
       MAIN CONTENT - Two-Way Communication
       =================================== -->
    <div class="main-content">
        <div class="main-container">
            <div class="title">
                <nav class="breadcrumb" aria-label="Breadcrumb">
                    <ol class="breadcrumb-list">
                        <li class="breadcrumb-item">
                            <a href="/" class="breadcrumb-link">
                                <span>Home</span>
                            </a>
                        </li>
                        <li class="breadcrumb-item">
                            <a href="/emergency-communication" class="breadcrumb-link">
                                <span>Emergency Communication</span>
                            </a>
                        </li>
                        <li class="breadcrumb-item active" aria-current="page">
                            <span>Two-Way Communication</span>
                        </li>
                    </ol>
                </nav>
                <h1>Two-Way Communication Interface</h1>
                <p>Interactive communication platform allowing administrators and citizens to exchange messages in real-time.</p>
                <div class="info-box" style="background: #e3f2fd; border-left: 4px solid #2196f3; padding: 1rem; border-radius: 4px; margin-top: 1rem;">
                    <i class="fas fa-info-circle" style="color: #2196f3;"></i>
                    <strong>How to use:</strong> Click on a conversation from the left panel to view messages. Type your response in the message box and click "Send" to reply to citizens.
                </div>
            </div>
            
            <div class="sub-container">
                <div class="page-content">
                    <div class="communication-container">
                        <!-- Conversations List -->
                        <div class="conversations-list">
                            <h3 style="margin-bottom: 1rem;">Conversations</h3>
                            <div id="conversationsList">
                                <!-- Conversations will be loaded here -->
                            </div>
                        </div>

                        <!-- Chat Window -->
                        <div class="chat-window">
                            <div class="chat-header">
                                <h3 id="chatUserName">Select a conversation</h3>
                                <small id="chatUserStatus"></small>
                            </div>
                            <div class="chat-messages" id="chatMessages">
                                <p style="text-align: center; color: var(--text-secondary); padding: 2rem;">
                                    Select a conversation to start messaging
                                </p>
                            </div>
                            <div class="chat-input">
                                <input type="text" id="messageInput" placeholder="Type a message..." disabled>
                                <button class="btn btn-primary" id="sendButton" disabled>
                                    <i class="fas fa-paper-plane"></i> Send
                                </button>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <script>
        let currentConversationId = null;

        function loadConversations() {
            fetch('../api/two-way-communication.php?action=list')
                .then(response => response.json())
                .then(data => {
                    const list = document.getElementById('conversationsList');
                    list.innerHTML = '';
                    
                    if (data.success && data.conversations) {
                        data.conversations.forEach(conv => {
                            const item = document.createElement('div');
                            item.className = 'conversation-item';
                            item.innerHTML = `
                                <strong>${conv.user_name}</strong>
                                <p style="margin: 0.5rem 0 0 0; font-size: 0.9rem; color: var(--text-secondary);">
                                    ${conv.last_message || 'No messages yet'}
                                </p>
                                <small style="color: var(--text-secondary);">${conv.last_message_time || ''}</small>
                            `;
                            item.addEventListener('click', function() {
                                openConversation(conv.id, conv.user_name, this);
                            });
                            list.appendChild(item);
                        });
                    }
                });
        }

        function openConversation(conversationId, userName, element) {
            currentConversationId = conversationId;
            document.getElementById('chatUserName').textContent = userName;
            document.getElementById('chatUserStatus').textContent = 'Online';
            document.getElementById('messageInput').disabled = false;
            document.getElementById('sendButton').disabled = false;
            
            // Update active conversation
            document.querySelectorAll('.conversation-item').forEach(item => {
                item.classList.remove('active');
            });
            if (element) {
                element.classList.add('active');
            }
            
            loadMessages(conversationId);
        }

        function loadMessages(conversationId) {
            fetch(`../api/two-way-communication.php?action=messages&conversation_id=${conversationId}`)
                .then(response => response.json())
                .then(data => {
                    const messagesDiv = document.getElementById('chatMessages');
                    messagesDiv.innerHTML = '';
                    
                    if (data.success && data.messages) {
                        data.messages.forEach(msg => {
                            const messageDiv = document.createElement('div');
                            messageDiv.className = `message ${msg.sender_type}`;
                            messageDiv.innerHTML = `
                                <div class="message-content">${msg.message}</div>
                                <small>${msg.timestamp}</small>
                            `;
                            messagesDiv.appendChild(messageDiv);
                        });
                        messagesDiv.scrollTop = messagesDiv.scrollHeight;
                    }
                });
        }

        document.getElementById('sendButton').addEventListener('click', sendMessage);
        document.getElementById('messageInput').addEventListener('keypress', function(e) {
            if (e.key === 'Enter' && !e.shiftKey) {
                e.preventDefault();
                sendMessage();
            }
        });

        function sendMessage() {
            const message = document.getElementById('messageInput').value.trim();
            if (!message || !currentConversationId) return;

            fetch('../api/two-way-communication.php', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({
                    conversation_id: currentConversationId,
                    message: message,
                    sender_type: 'admin'
                })
            })
            .then(response => response.json())
            .then(data => {
                if (data.success) {
                    document.getElementById('messageInput').value = '';
                    loadMessages(currentConversationId);
                } else {
                    alert('Error: ' + data.message);
                }
            });
        }

        // Load conversations on page load
        document.addEventListener('DOMContentLoaded', loadConversations);
        
        // Refresh conversations every 30 seconds
        setInterval(loadConversations, 30000);
    </script>
</body>
</html>

