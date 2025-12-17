<?php
/**
 * Two-Way Communication API
 * Handle interactive communication between administrators and citizens
 */

header('Content-Type: application/json; charset=utf-8');
require_once 'db_connect.php';

$action = $_GET['action'] ?? 'list';

if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $data = json_decode(file_get_contents('php://input'), true);
    $conversationId = $data['conversation_id'] ?? 0;
    $message = $data['message'] ?? '';
    $senderType = $data['sender_type'] ?? 'admin';
    
    if (empty($conversationId) || empty($message)) {
        echo json_encode(['success' => false, 'message' => 'Conversation ID and message are required.']);
        exit;
    }
    
    try {
        $stmt = $pdo->prepare("
            INSERT INTO messages (conversation_id, message, sender_type, sent_at)
            VALUES (?, ?, ?, NOW())
        ");
        $stmt->execute([$conversationId, $message, $senderType]);
        
        echo json_encode([
            'success' => true,
            'message' => 'Message sent successfully.',
            'message_id' => $pdo->lastInsertId()
        ]);
    } catch (PDOException $e) {
        error_log("Two-Way Communication Error: " . $e->getMessage());
        echo json_encode(['success' => false, 'message' => 'Database error occurred.']);
    }
} elseif ($action === 'list') {
    try {
        $stmt = $pdo->query("
            SELECT DISTINCT c.id, u.name as user_name, 
                   (SELECT message FROM messages WHERE conversation_id = c.id ORDER BY sent_at DESC LIMIT 1) as last_message,
                   (SELECT sent_at FROM messages WHERE conversation_id = c.id ORDER BY sent_at DESC LIMIT 1) as last_message_time
            FROM conversations c
            LEFT JOIN users u ON u.id = c.user_id
            ORDER BY last_message_time DESC
        ");
        $conversations = $stmt->fetchAll();
        
        echo json_encode([
            'success' => true,
            'conversations' => $conversations
        ]);
    } catch (PDOException $e) {
        error_log("List Conversations Error: " . $e->getMessage());
        echo json_encode(['success' => false, 'message' => 'Database error occurred.']);
    }
} elseif ($action === 'messages') {
    $conversationId = $_GET['conversation_id'] ?? 0;
    
    if (empty($conversationId)) {
        echo json_encode(['success' => false, 'message' => 'Conversation ID is required.']);
        exit;
    }
    
    try {
        $stmt = $pdo->prepare("
            SELECT id, message, sender_type, sent_at as timestamp
            FROM messages
            WHERE conversation_id = ?
            ORDER BY sent_at ASC
        ");
        $stmt->execute([$conversationId]);
        $messages = $stmt->fetchAll();
        
        echo json_encode([
            'success' => true,
            'messages' => $messages
        ]);
    } catch (PDOException $e) {
        error_log("Get Messages Error: " . $e->getMessage());
        echo json_encode(['success' => false, 'message' => 'Database error occurred.']);
    }
} else {
    echo json_encode(['success' => false, 'message' => 'Invalid action.']);
}
?>

