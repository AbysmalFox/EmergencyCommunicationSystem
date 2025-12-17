<?php
/**
 * Citizen Subscription and Alert Preferences API
 * Manage citizen subscriptions and preferences
 */

header('Content-Type: application/json; charset=utf-8');
require_once 'db_connect.php';

$action = $_GET['action'] ?? 'list';

if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $subscriberId = $_POST['subscriber_id'] ?? 0;
    $categories = $_POST['categories'] ?? [];
    $channels = $_POST['channels'] ?? [];
    $preferredLanguage = $_POST['preferred_language'] ?? 'en';
    $status = $_POST['status'] ?? 'active';
    
    if (empty($subscriberId)) {
        echo json_encode(['success' => false, 'message' => 'Subscriber ID is required.']);
        exit;
    }
    
    try {
        $categoriesStr = is_array($categories) ? implode(',', $categories) : '';
        $channelsStr = is_array($channels) ? implode(',', $channels) : '';
        
        $stmt = $pdo->prepare("
            UPDATE subscriptions
            SET categories = ?, channels = ?, preferred_language = ?, status = ?, updated_at = NOW()
            WHERE id = ?
        ");
        $stmt->execute([$categoriesStr, $channelsStr, $preferredLanguage, $status, $subscriberId]);
        
        echo json_encode([
            'success' => true,
            'message' => 'Subscription updated successfully.'
        ]);
    } catch (PDOException $e) {
        error_log("Update Subscription Error: " . $e->getMessage());
        echo json_encode(['success' => false, 'message' => 'Database error occurred.']);
    }
} elseif ($_SERVER['REQUEST_METHOD'] === 'DELETE') {
    $data = json_decode(file_get_contents('php://input'), true);
    $id = $data['id'] ?? 0;
    
    if (empty($id)) {
        echo json_encode(['success' => false, 'message' => 'Subscription ID is required.']);
        exit;
    }
    
    try {
        $stmt = $pdo->prepare("DELETE FROM subscriptions WHERE id = ?");
        $stmt->execute([$id]);
        
        echo json_encode(['success' => true, 'message' => 'Subscription deleted successfully.']);
    } catch (PDOException $e) {
        error_log("Delete Subscription Error: " . $e->getMessage());
        echo json_encode(['success' => false, 'message' => 'Database error occurred.']);
    }
} elseif ($action === 'list') {
    try {
        $stmt = $pdo->query("
            SELECT s.*, u.name, u.email, u.phone,
                   SUBSTRING_INDEX(SUBSTRING_INDEX(s.categories, ',', numbers.n), ',', -1) as category
            FROM subscriptions s
            LEFT JOIN users u ON u.id = s.user_id
            WHERE s.status = 'active'
        ");
        $subscribers = $stmt->fetchAll();
        
        // Group by subscriber and combine categories/channels
        $grouped = [];
        foreach ($subscribers as $sub) {
            $id = $sub['id'];
            if (!isset($grouped[$id])) {
                $grouped[$id] = [
                    'id' => $sub['id'],
                    'name' => $sub['name'],
                    'email' => $sub['email'],
                    'phone' => $sub['phone'],
                    'categories' => $sub['categories'] ? explode(',', $sub['categories']) : [],
                    'channels' => $sub['channels'] ? explode(',', $sub['channels']) : [],
                    'language' => $sub['preferred_language'],
                    'status' => $sub['status']
                ];
            }
        }
        
        echo json_encode([
            'success' => true,
            'subscribers' => array_values($grouped)
        ]);
    } catch (PDOException $e) {
        error_log("List Subscribers Error: " . $e->getMessage());
        echo json_encode(['success' => false, 'message' => 'Database error occurred.']);
    }
} elseif ($action === 'get') {
    $id = $_GET['id'] ?? 0;
    
    try {
        $stmt = $pdo->prepare("
            SELECT s.*, u.name, u.email, u.phone
            FROM subscriptions s
            LEFT JOIN users u ON u.id = s.user_id
            WHERE s.id = ?
        ");
        $stmt->execute([$id]);
        $subscriber = $stmt->fetch();
        
        if ($subscriber) {
            $subscriber['categories'] = $subscriber['categories'] ? explode(',', $subscriber['categories']) : [];
            $subscriber['channels'] = $subscriber['channels'] ? explode(',', $subscriber['channels']) : [];
        }
        
        echo json_encode([
            'success' => true,
            'subscriber' => $subscriber
        ]);
    } catch (PDOException $e) {
        error_log("Get Subscriber Error: " . $e->getMessage());
        echo json_encode(['success' => false, 'message' => 'Database error occurred.']);
    }
} elseif ($action === 'statistics') {
    try {
        $total = $pdo->query("SELECT COUNT(*) FROM subscriptions")->fetchColumn();
        $active = $pdo->query("SELECT COUNT(*) FROM subscriptions WHERE status = 'active'")->fetchColumn();
        $weather = $pdo->query("SELECT COUNT(*) FROM subscriptions WHERE categories LIKE '%weather%' AND status = 'active'")->fetchColumn();
        $earthquake = $pdo->query("SELECT COUNT(*) FROM subscriptions WHERE categories LIKE '%earthquake%' AND status = 'active'")->fetchColumn();
        
        echo json_encode([
            'success' => true,
            'total' => $total,
            'active' => $active,
            'weather' => $weather,
            'earthquake' => $earthquake
        ]);
    } catch (PDOException $e) {
        error_log("Statistics Error: " . $e->getMessage());
        echo json_encode(['success' => false, 'message' => 'Database error occurred.']);
    }
} elseif ($action === 'export') {
    // Export functionality would go here
    header('Content-Type: text/csv');
    header('Content-Disposition: attachment; filename="subscribers.csv"');
    // CSV export implementation
    echo "ID,Name,Email,Phone,Categories,Channels,Language,Status\n";
    // ... more export code
    exit;
} else {
    echo json_encode(['success' => false, 'message' => 'Invalid action.']);
}
?>

