<?php
/**
 * Reusable Sidebar Component
 * Include this file in your pages where you want a sidebar: <?php include 'sidebar/sidebar.php'; ?>
 * 
 * Features:
 * - Responsive design with mobile toggle
 * - Admin-style navigation
 * - Collapsible sections
 * - Dark mode support
 * - Multiple layout options
 */
?>

<!-- Sidebar Component -->
<aside class="sidebar" id="sidebar">
    <div class="sidebar-header">
        <div class="sidebar-brand">
            <div class="brand-logo">
                <img src="images/logo.svg" alt="" class="logo-img">
            </div>
        </div>
    </div>
    
    <div class="sidebar-content">
        <!-- Navigation Menu -->
        <nav class="sidebar-nav">
            <!-- Admin Section -->
            <div class="sidebar-section">
                <h3 class="sidebar-section-title">Admin</h3>
                <ul class="sidebar-menu">
                    <li class="sidebar-menu-item">
                        <a href="dashboard.php" class="sidebar-link <?php echo basename($_SERVER['PHP_SELF']) == 'dashboard.php' ? 'active' : ''; ?>">
                            <i class="fas fa-home" style="margin-right: 0.5rem;"></i>
                            <span>Dashboard</span>
                        </a>
                    </li>
                    
                    <li class="sidebar-menu-item">
                        <a href="users.php" class="sidebar-link <?php echo basename($_SERVER['PHP_SELF']) == 'users.php' ? 'active' : ''; ?>">
                            <i class="fas fa-users" style="margin-right: 0.5rem;"></i>
                            <span>Users</span>
                        </a>
                    </li>
                    
                    <li class="sidebar-menu-item">
                        <a href="general-settings.php" class="sidebar-link <?php echo basename($_SERVER['PHP_SELF']) == 'general-settings.php' ? 'active' : ''; ?>">
                            <i class="fas fa-cog" style="margin-right: 0.5rem;"></i>
                            <span>Settings</span>
                        </a>
                    </li>
                </ul>
            </div>
            
            <!-- Emergency Communication System Section -->
            <div class="sidebar-section">
                <h3 class="sidebar-section-title">Emergency Communication</h3>
                <ul class="sidebar-menu">
                    <li class="sidebar-menu-item">
                        <a href="mass-notification.php" class="sidebar-link <?php echo basename($_SERVER['PHP_SELF']) == 'mass-notification.php' ? 'active' : ''; ?>">
                            <i class="fas fa-broadcast-tower" style="margin-right: 0.5rem;"></i>
                            <span>Mass Notification</span>
                        </a>
                    </li>
                    
                    <li class="sidebar-menu-item">
                        <a href="alert-categorization.php" class="sidebar-link <?php echo basename($_SERVER['PHP_SELF']) == 'alert-categorization.php' ? 'active' : ''; ?>">
                            <i class="fas fa-tags" style="margin-right: 0.5rem;"></i>
                            <span>Alert Categorization</span>
                        </a>
                    </li>
                    
                    <li class="sidebar-menu-item">
                        <a href="two-way-communication.php" class="sidebar-link <?php echo basename($_SERVER['PHP_SELF']) == 'two-way-communication.php' ? 'active' : ''; ?>">
                            <i class="fas fa-comments" style="margin-right: 0.5rem;"></i>
                            <span>Two-Way Communication</span>
                        </a>
                    </li>
                    
                    <li class="sidebar-menu-item">
                        <a href="automated-warnings.php" class="sidebar-link <?php echo basename($_SERVER['PHP_SELF']) == 'automated-warnings.php' ? 'active' : ''; ?>">
                            <i class="fas fa-plug" style="margin-right: 0.5rem;"></i>
                            <span>Automated Warnings</span>
                        </a>
                    </li>
                    
                    <li class="sidebar-menu-item">
                        <a href="multilingual-alerts.php" class="sidebar-link <?php echo basename($_SERVER['PHP_SELF']) == 'multilingual-alerts.php' ? 'active' : ''; ?>">
                            <i class="fas fa-language" style="margin-right: 0.5rem;"></i>
                            <span>Multilingual Support</span>
                        </a>
                    </li>
                    
                    <li class="sidebar-menu-item">
                        <a href="citizen-subscriptions.php" class="sidebar-link <?php echo basename($_SERVER['PHP_SELF']) == 'citizen-subscriptions.php' ? 'active' : ''; ?>">
                            <i class="fas fa-users" style="margin-right: 0.5rem;"></i>
                            <span>Citizen Subscriptions</span>
                        </a>
                    </li>
                    
                    <li class="sidebar-menu-item">
                        <a href="audit-trail.php" class="sidebar-link <?php echo basename($_SERVER['PHP_SELF']) == 'audit-trail.php' ? 'active' : ''; ?>">
                            <i class="fas fa-history" style="margin-right: 0.5rem;"></i>
                            <span>Audit Trail</span>
                        </a>
                    </li>
                </ul>
            </div>
            
        </nav>
    </div>
</aside>

<!-- Sidebar Overlay for mobile -->
<div class="sidebar-overlay" id="sidebarOverlay"></div>

<script>
document.addEventListener('DOMContentLoaded', function() {
    // Sidebar functionality
    const sidebar = document.getElementById('sidebar');
    const sidebarOverlay = document.getElementById('sidebarOverlay');
    
    // Toggle sidebar
    function toggleSidebar() {
        sidebar.classList.toggle('sidebar-open');
        sidebarOverlay.classList.toggle('sidebar-overlay-open');
        document.body.classList.toggle('sidebar-open');
    }
    
    // Close sidebar
    function closeSidebar() {
        sidebar.classList.remove('sidebar-open');
        sidebarOverlay.classList.remove('sidebar-overlay-open');
        document.body.classList.remove('sidebar-open');
    }

    // Expose functions globally so other scripts
    // can trigger the sidebar without duplicating logic.
    window.sidebarToggle = toggleSidebar;
    window.sidebarClose = closeSidebar;
    
    // Close sidebar when clicking overlay
    if (sidebarOverlay) {
        sidebarOverlay.addEventListener('click', closeSidebar);
    }
    
    // Close sidebar on Escape key
    document.addEventListener('keydown', function(e) {
        if (e.key === 'Escape' && sidebar.classList.contains('sidebar-open')) {
            closeSidebar();
        }
    });
    
    // Handle submenu toggles
    const submenuToggles = document.querySelectorAll('.sidebar-submenu-toggle');
    submenuToggles.forEach(toggle => {
        toggle.addEventListener('click', function(e) {
            e.preventDefault();
            const submenu = this.nextElementSibling;
            const icon = this.querySelector('.submenu-icon');
            
            if (submenu) {
                const isOpen = submenu.classList.contains('sidebar-submenu-open');
                submenu.classList.toggle('sidebar-submenu-open');
                this.classList.toggle('active', !isOpen);
                
                // Toggle icon based on new state
                if (icon) {
                    if (submenu.classList.contains('sidebar-submenu-open')) {
                        // Now open - show up chevron
                        icon.classList.remove('fa-chevron-down');
                        icon.classList.add('fa-chevron-up');
                    } else {
                        // Now closed - show down chevron
                        icon.classList.remove('fa-chevron-up');
                        icon.classList.add('fa-chevron-down');
                    }
                }
            }
        });
    });
    
    // Auto-open submenu if it contains active item
    const activeLinks = document.querySelectorAll('.sidebar-submenu .sidebar-link.active');
    activeLinks.forEach(activeLink => {
        const submenu = activeLink.closest('.sidebar-submenu');
        const toggle = submenu ? submenu.previousElementSibling : null;
        
        if (submenu && toggle && toggle.classList.contains('sidebar-submenu-toggle')) {
            submenu.classList.add('sidebar-submenu-open');
            toggle.classList.add('active');
            
            const icon = toggle.querySelector('.submenu-icon');
            if (icon) {
                icon.classList.remove('fa-chevron-down');
                icon.classList.add('fa-chevron-up');
            }
        }
    });
});
</script>
