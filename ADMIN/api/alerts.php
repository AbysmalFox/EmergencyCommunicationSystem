php
<?php
header('Content-Type: application/json; charset=utf-8');

// Example static payload for testing. Replace with DB queries.
$alerts = [
  [
    "id" => 1,
    "title" => "Flood Watch",
    "message" => "Heavy rain expected in low-lying areas. Exercise caution.",
    "category" => "Weather",
    "status" => "active",
    "timestamp" => date('c')
  ],
  [
    "id" => 2,
    "title" => "Road Closure",
    "message" => "Main St closed due to landslide. Use alternate routes.",
    "category" => "Traffic",
    "status" => "active",
    "timestamp" => date('c')
  ]
];

echo json_encode($alerts);