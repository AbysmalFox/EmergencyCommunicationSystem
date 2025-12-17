<?php
$assetBase = '../ADMIN/header/';
?>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>User Sign Up</title>
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
        <div class="main-container">
            <div class="sub-container content-main">
                <section class="page-content">
                    <h2>Create an Account</h2>
                    <p>Sign up to receive alerts, manage your preferences, and access emergency tools.</p>
                    <form class="auth-form">
                        <div class="form-group">
                            <label for="full_name">Full Name</label>
                            <input type="text" id="full_name" name="full_name" placeholder="Juan Dela Cruz" required>
                        </div>
                        <div class="form-group">
                            <label for="email">Email</label>
                            <input type="email" id="email" name="email" placeholder="you@example.com" required>
                        </div>
                        <div class="form-group">
                            <label for="phone">Mobile Number</label>
                            <input type="tel" id="phone" name="phone" placeholder="+63 9XX XXX XXXX" required>
                        </div>
                        <div class="form-group form-group-half">
                            <div>
                                <label for="barangay">Barangay</label>
                                <input type="text" id="barangay" name="barangay" placeholder="e.g. Barangay Central" required>
                            </div>
                            <div>
                                <label for="house_number">House / Unit No.</label>
                                <input type="text" id="house_number" name="house_number" placeholder="e.g. #123" required>
                            </div>
                        </div>
                        <div class="form-group">
                            <label for="address">Complete Address</label>
                            <textarea id="address" name="address" rows="3" placeholder="Complete address in Quezon City" required></textarea>
                        </div>
                        <div class="form-group">
                            <label for="password">Password</label>
                            <input type="password" id="password" name="password" placeholder="Create a password" required>
                        </div>
                        <button type="submit" class="btn btn-primary">Sign Up</button>
                        <p class="auth-switch">
                            Already have an account?
                            <a href="login.php">Login</a>
                        </p>
                    </form>
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


