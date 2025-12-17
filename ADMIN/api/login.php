<?php
// login.php

// --- TEMPORARY DEBUGGING (Enable only while testing) ---
error_reporting(E_ALL);
ini_set('display_errors', 1);
// --------------------------------------------------------

// Return JSON response
header('Content-Type: application/json');

// Include DB connection (contains $pdo)
require_once 'db_connect.php';

// Get raw JSON input from the app
$data = json_decode(file_get_contents('php://input'), true);

// Validate required fields
if (!isset($data['email']) || !isset($data['password'])) {
    echo json_encode(["success" => false, "message" => "Missing email or password."]);
    exit();
}

$email = trim($data['email']);
$plainPassword = $data['password'];

if (empty($email) || empty($plainPassword)) {
    echo json_encode(["success" => false, "message" => "Email and password are required."]);
    exit();
}

try {
    // Your database uses columns: id, name, email, password
    $stmt = $pdo->prepare("SELECT id, name, email, password FROM users WHERE email = ?");
    
    if (!$stmt) {
        $errorInfo = $pdo->errorInfo();
        error_log("PDO Prepare Error: " . $errorInfo[2]);
        echo json_encode([
            "success" => false,
            "message" => "Database prepare error: " . $errorInfo[2]
        ]);
        exit();
    }

    $stmt->execute([$email]);
    $user = $stmt->fetch();

    // Verify password using the DB 'password' column
    if ($user && password_verify($plainPassword, $user['password'])) {

        // Generate simple session token
        $token = bin2hex(random_bytes(16));

        echo json_encode([
            "success" => true,
            "message" => "Login successful!",
            "user_id" => $user['id'],
            "username" => $user['name'], // Map DB 'name' → Android "username"
            "email" => $user['email'],
            "token" => $token
        ]);
    } else {
        echo json_encode(["success" => false, "message" => "Invalid email or password."]);
    }

} catch (PDOException $e) {
    error_log("Login PDO Exception: " . $e->getMessage());
    echo json_encode([
        "success" => false, 
        "message" => "Database error: " . $e->getMessage()
    ]);
} catch (Exception $e) {
    error_log("Login General Exception: " . $e->getMessage());
    echo json_encode([
        "success" => false, 
        "message" => "Server error: " . $e->getMessage()
    ]);
}
?>
