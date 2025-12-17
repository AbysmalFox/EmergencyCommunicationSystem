<?php
// Dedicated Home page for the user portal
$assetBase = '../ADMIN/header/';
?>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>User Home</title>
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
        <div class="hero-section home-hero" id="features">
            <div class="main-container">
                <div class="sub-container">
                    <h1><strong>QUEZON CITY EMERGENCY COMMUNICATION PORTAL</strong></h1>
                    <p class="hero-subtitle">
                        Quezon City Hall, Kalayaan Avenue, Diliman, Quezon City
                    </p>
                    <p class="hero-subtitle">
                        <strong>Mission:</strong>
                        To operationalize an effective, efficient, and inclusive DRRM system dedicated to Resilience-building in Quezon City communities.
                    </p>
                    <p class="hero-subtitle">
                        <strong>Vision:</strong>
                        A global mode of excellence in Disaster Risk Reduction and Management for its cohesive DRRM system fostering a Sustainable, Future-ready, and Resilient Quezon City.
                    </p>
                </div>
            </div>
        </div>

        <div class="main-container">
            <div class="sub-container home-emergency-cta">
                <a href="emergency-call.php" class="btn btn-primary emergency-call-btn">
                    <i class="fas fa-phone"></i>
                    <span>Call for Emergency</span>
                </a>
            </div>
        </div>

        <div class="main-container">
            <div class="sub-container">
                <section class="page-content">
                    <h2>About Us</h2>
                    <p>
                        The Quezon City Emergency Communication Portal connects residents, responders, and the local government
                        through reliable, multi-channel emergency alerts and communication tools.
                        Our goal is to help you receive critical information quickly and safely during disasters, incidents,
                        and city-wide emergencies.
                    </p>
                </section>

                <section class="page-content">
                    <h2>Services</h2>
                    <div class="cards-grid">
                        <div class="card">
                            <h3>Mass Notifications</h3>
                            <p>City-wide alerts sent via SMS, email, and online channels for urgent incidents and advisories.</p>
                        </div>
                        <div class="card">
                            <h3>Two-Way Communication</h3>
                            <p>Residents can report incidents, request assistance, and send updates back to responders.</p>
                        </div>
                        <div class="card">
                            <h3>Automated Hazard Feeds</h3>
                            <p>Integrated updates from agencies such as PAGASA and PHIVOLCS for weather and seismic events.</p>
                        </div>
                        <div class="card">
                            <h3>Multilingual Alerts</h3>
                            <p>Important messages can be delivered in multiple languages to reach more communities.</p>
                        </div>
                    </div>
                </section>

                <section class="page-content">
                    <h2>Guide: How to Call for Emergency</h2>
                    <ol class="emergency-guide-list">
                        <li><strong>Stay calm and move to a safe place.</strong> Ensure you are away from immediate danger before calling.</li>
                        <li><strong>Use the “Call for Emergency” button.</strong> Click the button above or dial the hotlines on the Emergency Call page.</li>
                        <li><strong>Prepare key details.</strong> Be ready to state your exact location, type of emergency, number of people involved, and visible hazards.</li>
                        <li><strong>Follow instructions.</strong> Listen carefully to the dispatcher and follow their guidance while waiting for responders.</li>
                        <li><strong>Keep lines open.</strong> Stay on the call or keep your phone available in case responders need more information.</li>
                    </ol>
                </section>
            </div>
        </div>
    </main>

    <?php include 'includes/footer-snippet.php'; ?>

    <script src="https://cdn.jsdelivr.net/npm/sweetalert2@11"></script>
    <script src="<?= $assetBase ?>js/mobile-menu.js"></script>
    <script src="<?= $assetBase ?>js/theme-toggle.js"></script>
    <script>
        // Simple language preference modal & search
        document.addEventListener('DOMContentLoaded', function () {
            const existingPreference = localStorage.getItem('preferredLanguage');
            const languages = [
                'English',
                'Filipino',
                'Cebuano',
                'Ilocano',
                'Kapampangan',
                'Bicolano',
                'Waray'
            ];

            function showLanguageModal() {
                const wrapper = document.createElement('div');
                wrapper.className = 'language-modal-backdrop';
                wrapper.innerHTML = `
                    <div class="language-modal">
                        <h2>Select Language</h2>
                        <p>Please choose your preferred language for alerts and content.</p>
                        <div class="language-buttons">
                            <button data-lang="en" class="btn btn-primary">English</button>
                            <button data-lang="fil" class="btn btn-secondary">Filipino</button>
                        </div>
                        <div class="language-search">
                            <input type="text" id="languageSearchInput" placeholder="Search language...">
                            <ul id="languageSuggestions"></ul>
                        </div>
                    </div>
                `;
                document.body.appendChild(wrapper);

                const suggestionsList = wrapper.querySelector('#languageSuggestions');
                const searchInput = wrapper.querySelector('#languageSearchInput');

                function setLanguage(code, label) {
                    localStorage.setItem('preferredLanguage', code);
                    localStorage.setItem('preferredLanguageLabel', label || code);
                    document.documentElement.setAttribute('data-lang', code);
                    wrapper.remove();
                }

                wrapper.querySelectorAll('.language-buttons button').forEach(btn => {
                    btn.addEventListener('click', () => {
                        const code = btn.getAttribute('data-lang');
                        const label = btn.textContent.trim();
                        setLanguage(code, label);
                    });
                });

                wrapper.addEventListener('click', (e) => {
                    if (e.target === wrapper) {
                        wrapper.remove();
                    }
                });

                if (searchInput && suggestionsList) {
                    searchInput.addEventListener('input', () => {
                        const q = searchInput.value.trim().toLowerCase();
                        suggestionsList.innerHTML = '';
                        if (!q) return;
                        const matches = languages.filter(lang =>
                            lang.toLowerCase().includes(q)
                        );
                        matches.forEach(lang => {
                            const li = document.createElement('li');
                            li.textContent = lang;
                            li.addEventListener('click', () => {
                                const code = lang.toLowerCase().startsWith('fil') ? 'fil' : 'en';
                                setLanguage(code, lang);
                            });
                            suggestionsList.appendChild(li);
                        });
                    });
                }
            }

            // Always show the language modal on load, but apply saved preference if available
            if (existingPreference) {
                document.documentElement.setAttribute('data-lang', existingPreference);
            }
            showLanguageModal();
        });
    </script>
</body>
</html>


