<?php
// User dashboard for emergency calling options (SIM and Internet/WiFi)
$assetBase = '../ADMIN/header/';
?>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Emergency Dashboard</title>
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
        <div class="hero-section" id="call-now">
            <div class="main-container">
                <div class="sub-container">
                    <h1>Call for Emergency</h1>
                    <p>Choose the best way to reach responders—via SIM (voice/SMS) or over Internet/WiFi (VoIP/chat).</p>
                    <div class="hero-buttons action-buttons">
                        <a href="tel:911" class="btn btn-primary"><i class="fas fa-phone"></i> Call via SIM (911)</a>
                        <a href="#internet-call" class="btn btn-secondary"><i class="fas fa-wifi"></i> Call via Internet/WiFi</a>
                    </div>
                </div>
            </div>
        </div>

        <div class="main-container">
            <div class="sub-container content-main">
                <section id="sim-call" class="page-content">
                    <h2>Call Using SIM (Voice/SMS)</h2>
                    <p>Use your mobile network for the fastest connection to responders.</p>
                    <div class="cards-grid">
                        <div class="card">
                            <h3>Voice Call (SIM)</h3>
                            <p>Dial national emergency 911 or your LGU hotline.</p>
                            <div class="action-buttons">
                                <a href="tel:911" class="btn btn-primary"><i class="fas fa-phone"></i> Call 911</a>
                                <a href="tel:+63123456789" class="btn btn-secondary"><i class="fas fa-phone-volume"></i> Call LGU Hotline</a>
                            </div>
                        </div>
                        <div class="card">
                            <h3>SMS (SIM)</h3>
                            <p>Text key details (location, incident type, injuries). Keep messages short and clear.</p>
                            <div class="action-buttons">
                                <a href="sms:+63123456789?body=Emergency%20at%20[location]%20-%20[type]%20-%20[injuries]" class="btn btn-primary"><i class="fas fa-comment-dots"></i> Text LGU</a>
                                <a href="sms:911?body=Emergency%20at%20[location]%20-%20[type]" class="btn btn-secondary"><i class="fas fa-exclamation-circle"></i> Text 911</a>
                            </div>
                        </div>
                    </div>
                </section>

                <section id="internet-call" class="page-content">
                    <h2>Call Using Internet/WiFi</h2>
                    <p>Use data or WiFi when cellular signal is weak. A VoIP/web-call endpoint can be integrated here.</p>
                    <div class="cards-grid">
                        <div class="card">
                            <h3>Web/VoIP Call</h3>
                            <p>Start a voice call over WiFi. (Hook this button to your VoIP/web-call service.)</p>
                            <button class="btn btn-primary" onclick="alert('Connect this to your VoIP/web-call flow');">
                                <i class="fas fa-headset"></i> Start Internet Call
                            </button>
                        </div>
                        <div class="card">
                            <h3>Two-Way Chat</h3>
                            <p>Send incident details and get dispatcher replies over data. (Integrate with your chat/two-way API.)</p>
                            <button class="btn btn-secondary" onclick="alert('Connect this to your chat/two-way comms flow');">
                                <i class="fas fa-comments"></i> Open Chat
                            </button>
                        </div>
                    </div>
                </section>

                <section id="contacts" class="page-content">
                    <h2>Quezon City Hotlines</h2>
                    <p>Official QCDRRMO and Quezon City emergency numbers. Save these for quick access.</p>
                    <div class="cards-grid">
                        <div class="card">
                            <h4>QCDRRMO Landline</h4>
                            <p>8927-5914 / 8928-4396</p>
                            <div class="action-buttons">
                                <a href="tel:0289275914" class="btn btn-primary">Call 8927-5914</a>
                                <a href="tel:0289284396" class="btn btn-secondary">Call 8928-4396</a>
                            </div>
                        </div>
                        <div class="card">
                            <h4>QC Emergency Hotline</h4>
                            <p>122</p>
                            <a href="tel:122" class="btn btn-primary">Call 122</a>
                        </div>
                        <div class="card">
                            <h4>Emergency Operations Center (EOC)</h4>
                            <p>0977-031-2892 (Globe)<br>0947-885-9929 (Smart)<br>8-988-4242 local 7245</p>
                            <div class="action-buttons">
                                <a href="tel:+639770312892" class="btn btn-primary">Call Globe</a>
                                <a href="tel:+639478859929" class="btn btn-secondary">Call Smart</a>
                                <a href="tel:0289884242,,7245" class="btn btn-secondary">Call Local 7245</a>
                            </div>
                        </div>
                        <div class="card">
                            <h4>EMS / Search and Rescue</h4>
                            <p>0947-884-7498 (Smart)<br>8928-4396</p>
                            <div class="action-buttons">
                                <a href="tel:+639478847498" class="btn btn-primary">Call EMS (Smart)</a>
                                <a href="tel:0289284396" class="btn btn-secondary">Call 8928-4396</a>
                            </div>
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
