<?php
/**
 * Log and Audit Trail API
 * Track and audit all sent notifications
 */

header('Content-Type: application/json; charset=utf-8');
require_once 'db_connect.php';

$action = $_GET['action'] ?? 'list';

if ($action === 'list') {
    $dateFrom = $_GET['date_from'] ?? '';
    $dateTo = $_GET['date_to'] ?? '';
    $channel = $_GET['channel'] ?? '';
    $status = $_GET['status'] ?? '';
    
    try {
        $query = "SELECT id, channel, message, recipient, status, sent_at as timestamp, sent_by, ip_address
                  FROM notification_logs WHERE 1=1";
        $params = [];
        
        if (!empty($dateFrom)) {
            $query .= " AND DATE(sent_at) >= ?";
            $params[] = $dateFrom;
        }
        if (!empty($dateTo)) {
            $query .= " AND DATE(sent_at) <= ?";
            $params[] = $dateTo;
        }
        if (!empty($channel)) {
            $query .= " AND channel = ?";
            $params[] = $channel;
        }
        if (!empty($status)) {
            $query .= " AND status = ?";
            $params[] = $status;
        }
        
        $query .= " ORDER BY sent_at DESC LIMIT 500";
        
        $stmt = $pdo->prepare($query);
        $stmt->execute($params);
        $logs = $stmt->fetchAll();
        
        echo json_encode([
            'success' => true,
            'logs' => $logs
        ]);
    } catch (PDOException $e) {
        error_log("List Audit Trail Error: " . $e->getMessage());
        echo json_encode(['success' => false, 'message' => 'Database error occurred.']);
    }
} elseif ($action === 'get') {
    $id = $_GET['id'] ?? 0;
    
    try {
        $stmt = $pdo->prepare("
            SELECT * FROM notification_logs WHERE id = ?
        ");
        $stmt->execute([$id]);
        $log = $stmt->fetch();
        
        echo json_encode([
            'success' => true,
            'log' => $log
        ]);
    } catch (PDOException $e) {
        error_log("Get Log Error: " . $e->getMessage());
        echo json_encode(['success' => false, 'message' => 'Database error occurred.']);
    }
} elseif ($action === 'statistics') {
    try {
        $total = $pdo->query("SELECT COUNT(*) FROM notification_logs")->fetchColumn();
        $successful = $pdo->query("SELECT COUNT(*) FROM notification_logs WHERE status = 'success'")->fetchColumn();
        $failed = $pdo->query("SELECT COUNT(*) FROM notification_logs WHERE status = 'failed'")->fetchColumn();
        $today = $pdo->query("SELECT COUNT(*) FROM notification_logs WHERE DATE(sent_at) = CURDATE()")->fetchColumn();
        
        echo json_encode([
            'success' => true,
            'total' => $total,
            'successful' => $successful,
            'failed' => $failed,
            'today' => $today
        ]);
    } catch (PDOException $e) {
        error_log("Statistics Error: " . $e->getMessage());
        echo json_encode(['success' => false, 'message' => 'Database error occurred.']);
    }
} elseif ($action === 'export') {
    // Export functionality
    header('Content-Type: text/csv');
    header('Content-Disposition: attachment; filename="audit_trail_' . date('Y-m-d') . '.csv"');
    
    $dateFrom = $_GET['date_from'] ?? '';
    $dateTo = $_GET['date_to'] ?? '';
    $channel = $_GET['channel'] ?? '';
    $status = $_GET['status'] ?? '';
    
    try {
        $query = "SELECT id, channel, message, recipient, status, sent_at, sent_by, ip_address
                  FROM notification_logs WHERE 1=1";
        $params = [];
        
        if (!empty($dateFrom)) {
            $query .= " AND DATE(sent_at) >= ?";
            $params[] = $dateFrom;
        }
        if (!empty($dateTo)) {
            $query .= " AND DATE(sent_at) <= ?";
            $params[] = $dateTo;
        }
        if (!empty($channel)) {
            $query .= " AND channel = ?";
            $params[] = $channel;
        }
        if (!empty($status)) {
            $query .= " AND status = ?";
            $params[] = $status;
        }
        
        $query .= " ORDER BY sent_at DESC";
        
        $stmt = $pdo->prepare($query);
        $stmt->execute($params);
        
        $output = fopen('php://output', 'w');
        fputcsv($output, ['ID', 'Channel', 'Message', 'Recipient', 'Status', 'Sent At', 'Sent By', 'IP Address']);
        
        while ($row = $stmt->fetch(PDO::FETCH_ASSOC)) {
            fputcsv($output, $row);
        }
        
        fclose($output);
    } catch (PDOException $e) {
        error_log("Export Error: " . $e->getMessage());
        echo "Error exporting data.";
    }
    exit;
} else {
    echo json_encode(['success' => false, 'message' => 'Invalid action.']);
}
?>

