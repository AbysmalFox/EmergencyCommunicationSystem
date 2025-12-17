<?php
// profile_data.php
header('Content-Type: application/json'); // Set header for JSON response

// Include database connection
require_once 'db_connect.php';

// Get raw POST data
$data = json_decode(file_get_contents('php://input'), true);

// In a real application, you would validate the token sent from the client
// For this example, we'll just check for user_id directly.
if (!isset($data['user_id'])) {
    echo json_encode(["success" => false, "message" => "User ID is required."]);
    exit();
}

$userId = $data['user_id'];

try {
    // Fetch user data by ID using a prepared statement
    $stmt = $pdo->prepare("SELECT id, username, email, created_at FROM users WHERE id = ?");
    $stmt->execute([$userId]);
    $user = $stmt->fetch();

    if ($user) {
        echo json_encode([
            "success" => true,
            "message" => "Profile data fetched successfully.",
            "user" => [
                "id" => $user['id'],
                "username" => $user['username'],
                "email" => $user['email'],
                "created_at" => $user['created_at']
            ]
        ]);
    } else {
        echo json_encode(["success" => false, "message" => "User not found."]);
    }

} catch (\PDOException $e) {
    // Log the error for debugging
    error_log("Profile data fetch error: " . $e->getMessage());
    echo json_encode(["success" => false, "message" => "An error occurred while fetching profile data."]);
}
?>