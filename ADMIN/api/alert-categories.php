<?php
/**
 * Alert Categories API
 * Manage alert categories (Weather, Earthquake, Bomb Threat, etc.)
 */

header('Content-Type: application/json; charset=utf-8');
require_once 'db_connect.php';

$action = $_GET['action'] ?? 'list';

if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $name = $_POST['name'] ?? '';
    $icon = $_POST['icon'] ?? 'fa-exclamation-triangle';
    $description = $_POST['description'] ?? '';
    $color = $_POST['color'] ?? '#4c8a89';
    
    if (empty($name)) {
        echo json_encode(['success' => false, 'message' => 'Category name is required.']);
        exit;
    }
    
    try {
        $stmt = $pdo->prepare("
            INSERT INTO alert_categories (name, icon, description, color, created_at)
            VALUES (?, ?, ?, ?, NOW())
        ");
        $stmt->execute([$name, $icon, $description, $color]);
        
        echo json_encode([
            'success' => true,
            'message' => 'Category added successfully.',
            'category_id' => $pdo->lastInsertId()
        ]);
    } catch (PDOException $e) {
        error_log("Alert Category Error: " . $e->getMessage());
        echo json_encode(['success' => false, 'message' => 'Database error occurred.']);
    }
} elseif ($_SERVER['REQUEST_METHOD'] === 'DELETE') {
    $data = json_decode(file_get_contents('php://input'), true);
    $id = $data['id'] ?? 0;
    
    if (empty($id)) {
        echo json_encode(['success' => false, 'message' => 'Category ID is required.']);
        exit;
    }
    
    try {
        $stmt = $pdo->prepare("DELETE FROM alert_categories WHERE id = ?");
        $stmt->execute([$id]);
        
        echo json_encode(['success' => true, 'message' => 'Category deleted successfully.']);
    } catch (PDOException $e) {
        error_log("Delete Category Error: " . $e->getMessage());
        echo json_encode(['success' => false, 'message' => 'Database error occurred.']);
    }
} elseif ($action === 'list') {
    try {
        $stmt = $pdo->query("
            SELECT c.*, COUNT(a.id) as alerts_count
            FROM alert_categories c
            LEFT JOIN alerts a ON a.category_id = c.id
            GROUP BY c.id
            ORDER BY c.name
        ");
        $categories = $stmt->fetchAll();
        
        echo json_encode([
            'success' => true,
            'categories' => $categories
        ]);
    } catch (PDOException $e) {
        error_log("List Categories Error: " . $e->getMessage());
        echo json_encode(['success' => false, 'message' => 'Database error occurred.']);
    }
} else {
    echo json_encode(['success' => false, 'message' => 'Invalid action.']);
}
?>

