<?php
// Shared footer snippet for user pages (extracted from index/dashboard)
?>
<footer class="footer">
    <div class="main-container">
        <div class="sub-container">
            <div class="footer-container">
                <div class="footer-main">
                    <div class="footer-brand">
                        <a href="index.php" class="footer-logo">
                            <img src="<?= $assetBase ?>images/logo.svg" alt="" class="logo-img">
                        </a>
                        <p class="footer-description">
                            Building modern web applications with clean code, responsive design, and user-friendly interfaces.
                        </p>
                        <div class="footer-social">
                            <a href="#" class="social-link" aria-label="Facebook"><i class="fab fa-facebook-f"></i></a>
                            <a href="#" class="social-link" aria-label="Twitter"><i class="fab fa-twitter"></i></a>
                            <a href="#" class="social-link" aria-label="LinkedIn"><i class="fab fa-linkedin-in"></i></a>
                            <a href="#" class="social-link" aria-label="GitHub"><i class="fab fa-github"></i></a>
                        </div>
                    </div>

                    <div class="footer-column">
                        <h4>Navigation</h4>
                        <ul class="footer-links">
                            <li><a href="home.php" class="footer-link">Home</a></li>
                            <li><a href="alerts.php" class="footer-link">Alerts</a></li>
                            <li><a href="profile.php" class="footer-link">Profile</a></li>
                            <li><a href="support.php" class="footer-link">Support</a></li>
                            <li><a href="emergency-call.php" class="footer-link">Emergency Call</a></li>
                        </ul>
                    </div>

                    <div class="footer-column">
                        <h4>Resources</h4>
                        <ul class="footer-links">
                            <li><a href="#" class="footer-link">Documentation</a></li>
                            <li><a href="#" class="footer-link">FAQ</a></li>
                        </ul>
                    </div>

                    <div class="footer-column">
                        <h4>Company</h4>
                        <ul class="footer-links">
                            <li><a href="#" class="footer-link">About Us</a></li>
                            <li><a href="#" class="footer-link">Privacy Policy</a></li>
                            <li><a href="#" class="footer-link">Terms of Service</a></li>
                        </ul>
                    </div>
                </div>

                <div class="footer-bottom">
                    <div class="footer-copyright">
                        <p>&copy; <?= date('Y'); ?> LGU #4. All rights reserved.</p>
                    </div>
                    <div class="footer-legal">
                        <a href="#" class="footer-link">Privacy Policy</a>
                        <a href="#" class="footer-link">Terms of Service</a>
                        <a href="#" class="footer-link">Cookie Policy</a>
                    </div>
                    <div class="theme-toggle">
                        <button class="theme-toggle-btn" data-theme="system" aria-label="System theme"><i class="fas fa-desktop"></i></button>
                        <button class="theme-toggle-btn" data-theme="light" aria-label="Light theme"><i class="fas fa-sun"></i></button>
                        <button class="theme-toggle-btn" data-theme="dark" aria-label="Dark theme"><i class="fas fa-moon"></i></button>
                    </div>
                </div>
            </div>
        </div>
    </div>
</footer>

