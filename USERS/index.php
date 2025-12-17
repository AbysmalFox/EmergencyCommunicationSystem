<?php
// User landing page that reuses the admin header/footer styling assets
$assetBase = '../ADMIN/header/';
?>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>User Portal</title>
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
        <div class="hero-section" id="features">
            <div class="main-container">
                <div class="sub-container">
                    <h1>Emergency Communication System</h1>
                    <p>Stay informed with real-time alerts, multilingual support, and two-way communication across web, SMS, email, and PA systems.</p>
                    <div class="hero-buttons">
                        <a href="#modules" class="btn btn-primary">Explore Features</a>
                        <a href="#alerts" class="btn btn-secondary">See Alerts</a>
                    </div>
                </div>
            </div>
        </div>

        <div class="main-container">
            <div class="sub-container">
                <section id="modules" class="page-content">
                    <h2>User-Facing Capabilities</h2>
                    <p>Access all emergency communication tools in one place.</p>
                    <div class="cards-grid">
                        <div class="card">
                            <h3>Mass Notification</h3>
                            <p>Receive coordinated SMS, email, and PA announcements for critical events.</p>
                        </div>
                        <div class="card">
                            <h3>Alert Categorization</h3>
                            <p>Weather, earthquake, bomb threat, and more—clearly tagged so you know what to do.</p>
                        </div>
                        <div class="card">
                            <h3>Two-Way Communication</h3>
                            <p>Reply to alerts to confirm safety, request help, or share field updates.</p>
                        </div>
                        <div class="card">
                            <h3>Automated Warnings</h3>
                            <p>Integrated with PAGASA, PHIVOLCS, and other trusted feeds for early warnings.</p>
                        </div>
                        <div class="card">
                            <h3>Multilingual Support</h3>
                            <p>Alerts available in multiple languages for inclusive communication.</p>
                        </div>
                        <div class="card">
                            <h3>Subscriptions & Preferences</h3>
                            <p>Opt in by channel and category to receive only what matters to you.</p>
                        </div>
                        <div class="card">
                            <h3>Audit & Transparency</h3>
                            <p>Log of sent notifications for accountability and follow-up.</p>
                        </div>
                    </div>
                </section>

                <section id="alerts" class="page-content">
                    <h2>Live & Recent Alerts</h2>
                    <p>View real-time and historical alerts with categories and statuses.</p>
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

                <section id="profile" class="page-content">
                    <h2>Profile & Preferences</h2>
                    <p>Manage your contact methods, preferred languages, and alert categories.</p>
                    <div class="cards-grid">
                        <div class="card">
                            <h4>Contact Channels</h4>
                            <p>Update phone, email, and notification channels.</p>
                            <button class="btn btn-primary">Manage Channels</button>
                        </div>
                        <div class="card">
                            <h4>Alert Preferences</h4>
                            <p>Choose categories: Weather, Earthquake, Bomb Threat, Health, and more.</p>
                            <button class="btn btn-secondary">Edit Preferences</button>
                        </div>
                        <div class="card">
                            <h4>Languages</h4>
                            <p>Select your preferred language for alerts.</p>
                            <button class="btn btn-secondary">Set Language</button>
                        </div>
                    </div>
                </section>

                <section id="support" class="page-content">
                    <h2>Support & Resources</h2>
                    <p>Get guidance on responding to alerts and requesting assistance.</p>
                    <div class="cards-grid">
                        <div class="card">
                            <h4>How to Respond</h4>
                            <p>Step-by-step instructions for common alert types.</p>
                            <button class="btn btn-primary">View Guide</button>
                        </div>
                        <div class="card">
                            <h4>Contact Dispatch</h4>
                            <p>Reach emergency dispatch or your local incident commander.</p>
                            <button class="btn btn-secondary">Contact Now</button>
                        </div>
                        <div class="card">
                            <h4>Audit & History</h4>
                            <p>See what was sent and when for transparency.</p>
                            <button class="btn btn-secondary">Open Log</button>
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