<?php
$assetBase = '../ADMIN/header/';
$current = 'profile.php';
?>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Profile & Preferences</title>
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
        <div class="hero-section" id="profile">
            <div class="main-container">
                <div class="sub-container">
                    <h1>Profile & Preferences</h1>
                    <p>Manage your contact methods, preferred languages, and alert categories.</p>
                </div>
            </div>
        </div>

        <div class="main-container">
            <div class="sub-container content-main">
                <section class="page-content">
                    <h2>Your Settings</h2>
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
                    </div>
                </section>

                <section class="page-content">
                    <h2>Language Settings</h2>
                    <p>Choose your preferred language. This will be used for alerts and interface text where available.</p>
                    <form class="auth-form" id="languageSettingsForm">
                        <div class="form-group">
                            <label>Preferred Language</label>
                            <div class="form-group-half">
                                <label>
                                    <input type="radio" name="preferred_language" value="en">
                                    English
                                </label>
                                <label>
                                    <input type="radio" name="preferred_language" value="fil">
                                    Filipino
                                </label>
                            </div>
                        </div>
                        <button type="button" class="btn btn-primary" id="saveLanguageBtn">Save Language</button>
                    </form>
                </section>
            </div>
        </div>
    </main>

    <?php include 'includes/footer-snippet.php'; ?>

    <script src="https://cdn.jsdelivr.net/npm/sweetalert2@11"></script>
    <script src="<?= $assetBase ?>js/mobile-menu.js"></script>
    <script src="<?= $assetBase ?>js/theme-toggle.js"></script>
    <script>
        document.addEventListener('DOMContentLoaded', function () {
            const savedLang = localStorage.getItem('preferredLanguage') || 'en';
            const radios = document.querySelectorAll('input[name="preferred_language"]');
            const saveBtn = document.getElementById('saveLanguageBtn');

            radios.forEach(r => {
                if (r.value === savedLang) {
                    r.checked = true;
                }
            });

            if (saveBtn) {
                saveBtn.addEventListener('click', function () {
                    const selected = document.querySelector('input[name="preferred_language"]:checked');
                    if (!selected) return;
                    const lang = selected.value;
                    localStorage.setItem('preferredLanguage', lang);
                    document.documentElement.setAttribute('data-lang', lang);
                    Swal.fire({
                        icon: 'success',
                        title: 'Language updated',
                        text: 'Your preferred language has been saved.',
                        timer: 1500,
                        showConfirmButton: false
                    });
                });
            }
        });
    </script>
</body>
</html>

