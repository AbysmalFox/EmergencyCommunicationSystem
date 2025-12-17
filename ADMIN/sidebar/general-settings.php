<?php
/**
 * General Settings Page
 * Manage system settings including dark mode
 */

$pageTitle = 'General Settings';
?>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title><?php echo htmlspecialchars($pageTitle); ?></title>
    <link rel="icon" type="image/x-icon" href="images/favicon.ico">
    <link rel="stylesheet" href="css/global.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css">
    <link rel="stylesheet" href="css/sidebar.css">
    <link rel="stylesheet" href="css/admin-header.css">
    <link rel="stylesheet" href="css/buttons.css">
    <link rel="stylesheet" href="css/forms.css">
    <link rel="stylesheet" href="css/hero.css">
    <link rel="stylesheet" href="css/sidebar-footer.css">
    <link rel="stylesheet" href="css/modules.css">
    <style>
        .settings-section h2 {
            margin-bottom: 1.5rem;
            color: var(--text-color-1);
            display: flex;
            align-items: center;
            gap: 0.5rem;
        }
        .setting-item {
            display: flex;
            justify-content: space-between;
            align-items: center;
            padding: 1.5rem 0;
            border-bottom: 1px solid var(--border-color-1);
        }
        .setting-item:last-child {
            border-bottom: none;
        }
        .setting-info {
            flex: 1;
        }
        .setting-title {
            font-weight: 600;
            margin-bottom: 0.5rem;
            color: var(--text-color-1);
        }
        .setting-description {
            font-size: 0.9rem;
            color: var(--text-secondary-1);
        }
        .theme-options {
            display: flex;
            gap: 1rem;
            flex-wrap: wrap;
        }
        .theme-option {
            flex: 1;
            min-width: 120px;
            padding: 1rem;
            border: 2px solid var(--border-color-1);
            border-radius: 8px;
            text-align: center;
            cursor: pointer;
            transition: all 0.2s;
            background: var(--card-bg-1);
        }
        .theme-option:hover {
            border-color: var(--primary-color-1);
            transform: translateY(-2px);
        }
        .theme-option.active {
            border-color: var(--primary-color-1);
            background: var(--primary-color-1);
            color: white;
        }
        .theme-option i {
            font-size: 1.5rem;
            margin-bottom: 0.5rem;
            display: block;
        }
        .switch {
            position: relative;
            display: inline-block;
            width: 60px;
            height: 34px;
        }
        .switch input {
            opacity: 0;
            width: 0;
            height: 0;
        }
        .slider {
            position: absolute;
            cursor: pointer;
            top: 0;
            left: 0;
            right: 0;
            bottom: 0;
            background-color: #ccc;
            transition: .4s;
            border-radius: 34px;
        }
        .slider:before {
            position: absolute;
            content: "";
            height: 26px;
            width: 26px;
            left: 4px;
            bottom: 4px;
            background-color: white;
            transition: .4s;
            border-radius: 50%;
        }
        input:checked + .slider {
            background-color: var(--primary-color-1);
        }
        input:checked + .slider:before {
            transform: translateX(26px);
        }
        .info-box {
            background: #e3f2fd;
            border-left: 4px solid #2196f3;
            padding: 1rem;
            border-radius: 4px;
            margin-bottom: 1.5rem;
        }
        [data-theme="dark"] .info-box {
            background: rgba(33, 150, 243, 0.1);
            border-left-color: #2196f3;
        }
        .info-box i {
            color: #2196f3;
            margin-right: 0.5rem;
        }
    </style>
</head>
<body>
    <!-- Include Sidebar Component -->
    <?php include 'includes/sidebar.php'; ?>

    <!-- Include Admin Header Component -->
    <?php include 'includes/admin-header.php'; ?>
    
    <!-- ===================================
       MAIN CONTENT - General Settings
       =================================== -->
    <div class="main-content">
        <div class="main-container">
            <div class="title">
                <nav class="breadcrumb" aria-label="Breadcrumb">
                    <ol class="breadcrumb-list">
                        <li class="breadcrumb-item">
                            <a href="dashboard.php" class="breadcrumb-link">
                                <span>Home</span>
                            </a>
                        </li>
                        <li class="breadcrumb-item active" aria-current="page">
                            <span>General Settings</span>
                        </li>
                    </ol>
                </nav>
                <h1>General Settings</h1>
                <p>Manage your system preferences and appearance settings.</p>
            </div>
            
            <div class="sub-container">
                <div class="page-content">
                    <div class="info-box">
                        <i class="fas fa-info-circle"></i>
                        <strong>Tip:</strong> Your theme preference is saved automatically and will be remembered when you return.
                    </div>

                    <!-- Appearance Settings -->
                    <div class="module-card">
                        <div class="module-card-header">
                            <h2><i class="fas fa-palette"></i> Appearance</h2>
                        </div>
                        <div>
                            <div class="setting-item">
                                <div class="setting-info">
                                    <div class="setting-title">Theme</div>
                                    <div class="setting-description">Choose your preferred color theme. System theme follows your device settings.</div>
                                </div>
                                <div class="theme-options">
                                    <div class="theme-option" data-theme="light" onclick="setTheme('light')">
                                        <i class="fas fa-sun"></i>
                                        <div>Light</div>
                                    </div>
                                    <div class="theme-option" data-theme="dark" onclick="setTheme('dark')">
                                        <i class="fas fa-moon"></i>
                                        <div>Dark</div>
                                    </div>
                                    <div class="theme-option" data-theme="system" onclick="setTheme('system')">
                                        <i class="fas fa-desktop"></i>
                                        <div>System</div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>

                    <!-- System Settings -->
                    <div class="module-card">
                        <div class="module-card-header">
                            <h2><i class="fas fa-cog"></i> System</h2>
                        </div>
                        <div>
                            <div class="setting-item">
                                <div class="setting-info">
                                    <div class="setting-title">Auto-refresh Dashboard</div>
                                    <div class="setting-description">Automatically refresh dashboard data every 5 minutes</div>
                                </div>
                                <label class="switch">
                                    <input type="checkbox" id="autoRefresh" checked>
                                    <span class="slider"></span>
                                </label>
                            </div>

                            <div class="setting-item">
                                <div class="setting-info">
                                    <div class="setting-title">Email Notifications</div>
                                    <div class="setting-description">Receive email notifications for important system events</div>
                                </div>
                                <label class="switch">
                                    <input type="checkbox" id="emailNotifications" checked>
                                    <span class="slider"></span>
                                </label>
                            </div>

                            <div class="setting-item">
                                <div class="setting-info">
                                    <div class="setting-title">Sound Alerts</div>
                                    <div class="setting-description">Play sound when new messages or alerts arrive</div>
                                </div>
                                <label class="switch">
                                    <input type="checkbox" id="soundAlerts">
                                    <span class="slider"></span>
                                </label>
                            </div>
                        </div>
                    </div>

                    <!-- Notification Settings -->
                    <div class="module-card">
                        <div class="module-card-header">
                            <h2><i class="fas fa-bell"></i> Notifications</h2>
                        </div>
                        <div>
                            <div class="setting-item">
                                <div class="setting-info">
                                    <div class="setting-title">Show Notification Badges</div>
                                    <div class="setting-description">Display notification count badges in the header</div>
                                </div>
                                <label class="switch">
                                    <input type="checkbox" id="showBadges" checked>
                                    <span class="slider"></span>
                                </label>
                            </div>

                            <div class="setting-item">
                                <div class="setting-info">
                                    <div class="setting-title">Desktop Notifications</div>
                                    <div class="setting-description">Show browser notifications for new alerts</div>
                                </div>
                                <label class="switch">
                                    <input type="checkbox" id="desktopNotifications">
                                    <span class="slider"></span>
                                </label>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <script>
        // Load saved settings
        function loadSettings() {
            const savedTheme = localStorage.getItem('theme') || 'system';
            updateThemeButtons(savedTheme);
            
            // Load other settings
            document.getElementById('autoRefresh').checked = localStorage.getItem('autoRefresh') !== 'false';
            document.getElementById('emailNotifications').checked = localStorage.getItem('emailNotifications') !== 'false';
            document.getElementById('soundAlerts').checked = localStorage.getItem('soundAlerts') === 'true';
            document.getElementById('showBadges').checked = localStorage.getItem('showBadges') !== 'false';
            document.getElementById('desktopNotifications').checked = localStorage.getItem('desktopNotifications') === 'true';
        }

        function setTheme(theme) {
            const html = document.documentElement;
            html.setAttribute('data-theme', theme);
            localStorage.setItem('theme', theme);
            updateThemeButtons(theme);
            
            // Update header buttons if they exist
            const lightBtn = document.getElementById('lightModeBtn');
            const darkBtn = document.getElementById('darkModeBtn');
            if (lightBtn && darkBtn) {
                if (theme === 'dark') {
                    lightBtn.classList.remove('active');
                    darkBtn.classList.add('active');
                } else if (theme === 'light') {
                    lightBtn.classList.add('active');
                    darkBtn.classList.remove('active');
                } else {
                    // System theme - detect current
                    const currentTheme = html.getAttribute('data-theme');
                    if (currentTheme === 'dark') {
                        lightBtn.classList.remove('active');
                        darkBtn.classList.add('active');
                    } else {
                        lightBtn.classList.add('active');
                        darkBtn.classList.remove('active');
                    }
                }
            }
            
            if (theme === 'system') {
                applySystemTheme();
            }
        }

        function updateThemeButtons(theme) {
            document.querySelectorAll('.theme-option').forEach(option => {
                option.classList.remove('active');
                if (option.getAttribute('data-theme') === theme) {
                    option.classList.add('active');
                }
            });
        }

        function applySystemTheme() {
            const prefersDark = window.matchMedia('(prefers-color-scheme: dark)').matches;
            document.documentElement.setAttribute('data-theme', prefersDark ? 'dark' : 'light');
        }

        // Listen for system theme changes
        window.matchMedia('(prefers-color-scheme: dark)').addEventListener('change', () => {
            if (localStorage.getItem('theme') === 'system') {
                applySystemTheme();
            }
        });

        // Listen for theme changes from header buttons
        document.addEventListener('DOMContentLoaded', function() {
            loadSettings();
            const savedTheme = localStorage.getItem('theme') || 'light';
            
            // Sync with header buttons
            const lightBtn = document.getElementById('lightModeBtn');
            const darkBtn = document.getElementById('darkModeBtn');
            if (lightBtn && darkBtn) {
                const currentTheme = document.documentElement.getAttribute('data-theme') || savedTheme;
                if (currentTheme === 'dark') {
                    lightBtn.classList.remove('active');
                    darkBtn.classList.add('active');
                } else {
                    lightBtn.classList.add('active');
                    darkBtn.classList.remove('active');
                }
            }
            
            if (savedTheme === 'system') {
                applySystemTheme();
            }
            
            // Save settings - moved inside DOMContentLoaded to ensure elements exist
            const autoRefresh = document.getElementById('autoRefresh');
            const emailNotifications = document.getElementById('emailNotifications');
            const soundAlerts = document.getElementById('soundAlerts');
            const showBadges = document.getElementById('showBadges');
            const desktopNotifications = document.getElementById('desktopNotifications');
            
            if (autoRefresh) {
                autoRefresh.addEventListener('change', function() {
                    localStorage.setItem('autoRefresh', this.checked);
                });
            }
            
            if (emailNotifications) {
                emailNotifications.addEventListener('change', function() {
                    localStorage.setItem('emailNotifications', this.checked);
                });
            }
            
            if (soundAlerts) {
                soundAlerts.addEventListener('change', function() {
                    localStorage.setItem('soundAlerts', this.checked);
                });
            }
            
            if (showBadges) {
                showBadges.addEventListener('change', function() {
                    localStorage.setItem('showBadges', this.checked);
                });
            }
            
            if (desktopNotifications) {
                desktopNotifications.addEventListener('change', function() {
                    localStorage.setItem('desktopNotifications', this.checked);
                });
            }
        });
        
        // Listen for theme changes from header
        window.addEventListener('storage', function(e) {
            if (e.key === 'theme') {
                const newTheme = e.newValue || 'light';
                updateThemeButtons(newTheme);
            }
        });
    </script>
</body>
</html>
