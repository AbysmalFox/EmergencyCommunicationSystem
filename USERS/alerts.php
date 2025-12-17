<?php
$assetBase = '../ADMIN/header/';
$current = 'alerts.php';
?>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Alerts</title>
    <link rel="icon" type="image/x-icon" href="<?= $assetBase ?>images/favicon.ico">
    <link rel="stylesheet" href="<?= $assetBase ?>css/global.css">
    <link rel="stylesheet" href="<?= $assetBase ?>css/buttons.css">
    <link rel="stylesheet" href="<?= $assetBase ?>css/hero.css">
    <link rel="stylesheet" href="../ADMIN/sidebar/css/global.css">
    <link rel="stylesheet" href="../ADMIN/sidebar/css/sidebar.css">
    <link rel="stylesheet" href="../ADMIN/sidebar/css/content.css">
    <link rel="stylesheet" href="../ADMIN/sidebar/css/buttons.css">
    <link rel="stylesheet" href="css/user.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/sweetalert2@11/dist/sweetalert2.min.css">
</head>
<body>
    <?php include 'includes/sidebar.php'; ?>

    <button class="sidebar-toggle-btn" aria-label="Toggle menu" onclick="window.sidebarToggle()">
        <i class="fas fa-bars"></i>
    </button>

    <main class="main-content">
        <div class="hero-section" id="alerts">
            <div class="main-container">
                <div class="sub-container">
                    <h1>Live & Recent Alerts</h1>
                    <p>View and respond to critical alerts with clear categories and actions.</p>
                </div>
            </div>
        </div>

        <div class="main-container">
            <div class="sub-container content-main">
                <section class="page-content">
                    <h2>Active Alerts</h2>
                    <div class="cards-grid">
                        <div class="card">
                            <h4>Weather Advisory</h4>
                            <p>Rainfall alert from PAGASA. Stay indoors if possible.</p>
                            <button class="btn btn-primary">Acknowledge</button>
                        </div>
                        <div class="card">
                            <h4>Earthquake Update</h4>
                            <p>Aftershock notice from PHIVOLCS. Expect minor tremors.</p>
                            <button class="btn btn-secondary">View Details</button>
                        </div>
                    </div>
                </section>
            </div>
        </div>
    </main>

    <?php include 'includes/footer-snippet.php'; ?>

    <script src="https://cdn.jsdelivr.net/npm/sweetalert2@11"></script>
    <script src="<?= $assetBase ?>js/mobile-menu.js"></script>
    <script src="<?= $assetBase ?>js/theme-toggle.js"></script>
</body>
</html>

