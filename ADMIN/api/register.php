<?php
// register.php
header('Content-Type: application/json');

// Include database connection
require_once 'db_connect.php';

// Get raw POST data
$data = json_decode(file_get_contents('php://input'), true);

// --- CRITICAL PHP SERVER-SIDE VALIDATION ---
// 1. Check if required fields are *set* in the incoming JSON
if (!isset($data['name']) || !isset($data['email']) || !isset($data['password'])) {
    echo json_encode(["success" => false, "message" => "Missing required JSON fields (name, email, password)."]);
    exit();
}

// 2. Trim whitespace and then check if fields are *empty*
$name = trim($data['name']);
$email = trim($data['email']);
$plainPassword = $data['password']; // Password will be hashed, no trim for hashing input

// Now, check if any of the *trimmed* values are empty
if (empty($name) || empty($email) || empty($plainPassword)) {
    echo json_encode(["success" => false, "message" => "All fields must be filled."]);
    exit();
}

// Basic email format validation (more robust regex could be used)
if (!filter_var($email, FILTER_VALIDATE_EMAIL)) {
    echo json_encode(["success" => false, "message" => "Invalid email format."]);
    exit();
}

// Password length validation (match client-side if possible)
if (strlen($plainPassword) < 6) {
    echo json_encode(["success" => false, "message" => "Password must be at least 6 characters long."]);
    exit();
}

// Check if email already exists
try {
    $stmt = $pdo->prepare("SELECT COUNT(*) FROM users WHERE email = ?");
    $stmt->execute([$email]);
    if ($stmt->fetchColumn() > 0) {
        echo json_encode(["success" => false, "message" => "Email already registered."]);
        exit();
    }
} catch (\PDOException $e) {
    error_log("Email check error: " . $e->getMessage());
    echo json_encode(["success" => false, "message" => "Server error during email check."]);
    exit();
}


// Hash the password
$hashedPassword = password_hash($plainPassword, PASSWORD_DEFAULT);

try {
    // Insert new user into the database
    $stmt = $pdo->prepare("INSERT INTO users (name, email, password) VALUES (?, ?, ?)");
    if ($stmt->execute([$name, $email, $hashedPassword])) {
        echo json_encode(["success" => true, "message" => "Registration successful!"]);
    } else {
        echo json_encode(["success" => false, "message" => "Registration failed. Database insert error."]);
    }

} catch (\PDOException $e) {
    // Log the error for debugging. Do not expose SQL errors directly to the client.
    error_log("User registration error: " . $e->getMessage());
    echo json_encode(["success" => false, "message" => "An error occurred during registration."]);
}

?>
