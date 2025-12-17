<?php
// db_connect.php
// Creates a PDO instance in $pdo. Adjust credentials as needed for your XAMPP setup.

// Report all errors to the log, but do NOT display them to the client
error_reporting(E_ALL);
ini_set('display_errors', 0);

$host = '127.0.0.1';
$db   = 'emergency_comm_db';
$user = 'root';
$pass = '';
$charset = 'utf8mb4';

$dsn = "mysql:host=$host;dbname=$db;charset=$charset";
$options = [
    PDO::ATTR_ERRMODE            => PDO::ERRMODE_EXCEPTION,
    PDO::ATTR_DEFAULT_FETCH_MODE => PDO::FETCH_ASSOC,
    PDO::ATTR_EMULATE_PREPARES   => false,
];

try {
    $pdo = new PDO($dsn, $user, $pass, $options);
} catch (PDOException $e) {
    error_log('DB Connection failed: ' . $e->getMessage());
    http_response_code(500);
    echo json_encode(['success' => false, 'message' => 'Database connection failed.']);
    exit();
}

?>