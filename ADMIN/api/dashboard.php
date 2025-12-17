<?php
/**
 * Dashboard Analytics API
 * Provides analytics data for the dashboard
 */

header('Content-Type: application/json; charset=utf-8');
require_once 'db_connect.php';

try {
    // Get total subscribers
    $totalSubscribers = $pdo->query("SELECT COUNT(*) FROM subscriptions WHERE status = 'active'")->fetchColumn();
    
    // Get subscribers change this week
    $weekAgo = date('Y-m-d', strtotime('-7 days'));
    $subscriberChange = $pdo->query("
        SELECT COUNT(*) FROM subscriptions 
        WHERE status = 'active' AND created_at >= '$weekAgo'
    ")->fetchColumn();
    
    // Get notifications sent today
    $today = date('Y-m-d');
    $notificationsToday = $pdo->query("
        SELECT COUNT(*) FROM notification_logs 
        WHERE DATE(sent_at) = '$today'
    ")->fetchColumn();
    
    // Get success rate
    $totalNotifications = $pdo->query("SELECT COUNT(*) FROM notification_logs")->fetchColumn();
    $successfulNotifications = $pdo->query("
        SELECT COUNT(*) FROM notification_logs WHERE status = 'success'
    ")->fetchColumn();
    $successRate = $totalNotifications > 0 ? round(($successfulNotifications / $totalNotifications) * 100) : 100;
    
    // Get active weather alerts
    $weatherAlerts = $pdo->query("
        SELECT COUNT(*) FROM automated_warnings 
        WHERE source = 'pagasa' AND status = 'published'
    ")->fetchColumn();
    
    // Get active earthquake alerts
    $earthquakeAlerts = $pdo->query("
        SELECT COUNT(*) FROM automated_warnings 
        WHERE source = 'phivolcs' AND status = 'published'
    ")->fetchColumn();
    
    // Get pending messages
    $pendingMessages = $pdo->query("
        SELECT COUNT(DISTINCT conversation_id) FROM messages 
        WHERE sender_type = 'citizen' AND read_at IS NULL
    ")->fetchColumn();
    
    // Get notifications chart data (last 7 days)
    $notificationsData = [];
    $labels = [];
    $values = [];
    for ($i = 6; $i >= 0; $i--) {
        $date = date('Y-m-d', strtotime("-$i days"));
        $dayName = date('D', strtotime("-$i days"));
        $count = $pdo->query("
            SELECT COUNT(*) FROM notification_logs 
            WHERE DATE(sent_at) = '$date'
        ")->fetchColumn();
        $labels[] = $dayName;
        $values[] = (int)$count;
    }
    
    // Get channels distribution
    $smsCount = $pdo->query("SELECT COUNT(*) FROM notification_logs WHERE channel = 'sms'")->fetchColumn();
    $emailCount = $pdo->query("SELECT COUNT(*) FROM notification_logs WHERE channel = 'email'")->fetchColumn();
    $paCount = $pdo->query("SELECT COUNT(*) FROM notification_logs WHERE channel = 'pa'")->fetchColumn();
    
    // Get recent activity
    $recentActivity = [];
    
    // Recent notifications
    $stmt = $pdo->query("
        SELECT CONCAT('Notification sent via ', channel) as title, 
               sent_at as time, 'notification' as type
        FROM notification_logs 
        ORDER BY sent_at DESC 
        LIMIT 5
    ");
    $notifications = $stmt->fetchAll();
    foreach ($notifications as $notif) {
        $recentActivity[] = [
            'title' => $notif['title'],
            'time' => timeAgo($notif['time']),
            'type' => 'notification'
        ];
    }
    
    // Recent messages
    $stmt = $pdo->query("
        SELECT CONCAT('New message from citizen') as title, 
               sent_at as time, 'message' as type
        FROM messages 
        WHERE sender_type = 'citizen'
        ORDER BY sent_at DESC 
        LIMIT 3
    ");
    $messages = $stmt->fetchAll();
    foreach ($messages as $msg) {
        $recentActivity[] = [
            'title' => $msg['title'],
            'time' => timeAgo($msg['time']),
            'type' => 'message'
        ];
    }
    
    // Sort by time and limit to 10
    usort($recentActivity, function($a, $b) {
        return strtotime($b['time']) - strtotime($a['time']);
    });
    $recentActivity = array_slice($recentActivity, 0, 10);
    
    echo json_encode([
        'success' => true,
        'stats' => [
            'total_subscribers' => (int)$totalSubscribers,
            'subscriber_change' => (int)$subscriberChange,
            'notifications_today' => (int)$notificationsToday,
            'success_rate' => (int)$successRate,
            'weather_alerts' => (int)$weatherAlerts,
            'earthquake_alerts' => (int)$earthquakeAlerts,
            'pending_messages' => (int)$pendingMessages
        ],
        'charts' => [
            'notifications' => [
                'labels' => $labels,
                'values' => $values
            ],
            'channels' => [
                'labels' => ['SMS', 'Email', 'PA System'],
                'values' => [(int)$smsCount, (int)$emailCount, (int)$paCount]
            ]
        ],
        'activity' => $recentActivity
    ]);
    
} catch (PDOException $e) {
    error_log("Dashboard API Error: " . $e->getMessage());
    echo json_encode([
        'success' => false,
        'message' => 'Error loading dashboard data',
        'stats' => [
            'total_subscribers' => 0,
            'subscriber_change' => 0,
            'notifications_today' => 0,
            'success_rate' => 0,
            'weather_alerts' => 0,
            'earthquake_alerts' => 0,
            'pending_messages' => 0
        ],
        'charts' => [
            'notifications' => [
                'labels' => ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun'],
                'values' => [0, 0, 0, 0, 0, 0, 0]
            ],
            'channels' => [
                'labels' => ['SMS', 'Email', 'PA System'],
                'values' => [0, 0, 0]
            ]
        ],
        'activity' => []
    ]);
}

function timeAgo($datetime) {
    $timestamp = strtotime($datetime);
    $diff = time() - $timestamp;
    
    if ($diff < 60) {
        return 'Just now';
    } elseif ($diff < 3600) {
        $mins = floor($diff / 60);
        return $mins . ' minute' . ($mins > 1 ? 's' : '') . ' ago';
    } elseif ($diff < 86400) {
        $hours = floor($diff / 3600);
        return $hours . ' hour' . ($hours > 1 ? 's' : '') . ' ago';
    } elseif ($diff < 604800) {
        $days = floor($diff / 86400);
        return $days . ' day' . ($days > 1 ? 's' : '') . ' ago';
    } else {
        return date('M d, Y', $timestamp);
    }
}
?>

