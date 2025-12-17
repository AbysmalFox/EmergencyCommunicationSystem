<?php
/**
 * Multilingual Support for Alerts API
 * Manage alert translations
 */

header('Content-Type: application/json; charset=utf-8');
require_once 'db_connect.php';

$action = $_GET['action'] ?? 'list';

if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $alertId = $_POST['alert_id'] ?? 0;
    $targetLanguage = $_POST['target_language'] ?? '';
    $translatedTitle = $_POST['translated_title'] ?? '';
    $translatedContent = $_POST['translated_content'] ?? '';
    
    if (empty($alertId) || empty($targetLanguage) || empty($translatedTitle) || empty($translatedContent)) {
        echo json_encode(['success' => false, 'message' => 'All fields are required.']);
        exit;
    }
    
    try {
        $stmt = $pdo->prepare("
            INSERT INTO alert_translations (alert_id, target_language, translated_title, translated_content, status, translated_at)
            VALUES (?, ?, ?, ?, 'active', NOW())
            ON DUPLICATE KEY UPDATE translated_title = ?, translated_content = ?, translated_at = NOW()
        ");
        $stmt->execute([$alertId, $targetLanguage, $translatedTitle, $translatedContent, $translatedTitle, $translatedContent]);
        
        echo json_encode([
            'success' => true,
            'message' => 'Translation saved successfully.',
            'translation_id' => $pdo->lastInsertId()
        ]);
    } catch (PDOException $e) {
        error_log("Multilingual Alert Error: " . $e->getMessage());
        echo json_encode(['success' => false, 'message' => 'Database error occurred.']);
    }
} elseif ($_SERVER['REQUEST_METHOD'] === 'DELETE') {
    $data = json_decode(file_get_contents('php://input'), true);
    $id = $data['id'] ?? 0;
    
    if (empty($id)) {
        echo json_encode(['success' => false, 'message' => 'Translation ID is required.']);
        exit;
    }
    
    try {
        $stmt = $pdo->prepare("DELETE FROM alert_translations WHERE id = ?");
        $stmt->execute([$id]);
        
        echo json_encode(['success' => true, 'message' => 'Translation deleted successfully.']);
    } catch (PDOException $e) {
        error_log("Delete Translation Error: " . $e->getMessage());
        echo json_encode(['success' => false, 'message' => 'Database error occurred.']);
    }
} elseif ($action === 'list') {
    try {
        $stmt = $pdo->query("
            SELECT t.*, a.title as original_title, a.message as original_content, 'en' as original_language
            FROM alert_translations t
            LEFT JOIN alerts a ON a.id = t.alert_id
            ORDER BY t.translated_at DESC
        ");
        $translations = $stmt->fetchAll();
        
        echo json_encode([
            'success' => true,
            'translations' => $translations
        ]);
    } catch (PDOException $e) {
        error_log("List Translations Error: " . $e->getMessage());
        echo json_encode(['success' => false, 'message' => 'Database error occurred.']);
    }
} else {
    echo json_encode(['success' => false, 'message' => 'Invalid action.']);
}
?>

