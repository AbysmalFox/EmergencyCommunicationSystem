<?php
/**
 * Emergency Communication System Dashboard
 * User-friendly analytics dashboard for non-technical administrators
 */

$pageTitle = 'Dashboard';
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
    <link rel="stylesheet" href="css/hero.css">
    <link rel="stylesheet" href="css/sidebar-footer.css">
    <link rel="stylesheet" href="css/modules.css">
    <style>
        .dashboard-grid {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
            gap: 1.5rem;
            margin-bottom: 2rem;
        }
        .stat-card.weather { border-left-color: #3498db; }
        .stat-card.earthquake { border-left-color: #e74c3c; }
        .stat-card.subscribers { border-left-color: #2ecc71; }
        .stat-card.notifications { border-left-color: #f39c12; }
        .stat-card.success { border-left-color: #27ae60; }
        .stat-card.pending { border-left-color: #95a5a6; }
        .stat-value {
            font-size: 2.5rem;
            font-weight: bold;
            color: var(--primary-color-1);
            margin: 0.5rem 0;
        }
        .stat-label {
            color: var(--text-secondary-1);
            font-size: 0.9rem;
            margin-bottom: 0.5rem;
        }
        .stat-change {
            font-size: 0.85rem;
            display: flex;
            align-items: center;
            gap: 0.25rem;
        }
        .stat-change.positive { color: #27ae60; }
        .stat-change.negative { color: #e74c3c; }
        .chart-container {
            background: var(--card-bg-1);
            border-radius: 12px;
            padding: 1.5rem;
            margin-bottom: 2rem;
            box-shadow: 0 2px 8px rgba(0,0,0,0.1);
            border: 1px solid var(--border-color-1);
        }
        
        [data-theme="dark"] .chart-container {
            box-shadow: 0 2px 8px rgba(0,0,0,0.3);
        }
        .chart-title {
            font-size: 1.2rem;
            font-weight: 600;
            margin-bottom: 1rem;
            color: var(--text-color-1);
        }
        .quick-actions {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
            gap: 1rem;
            margin-bottom: 2rem;
        }
        .quick-action-btn {
            background: var(--card-bg-1);
            border: 2px solid var(--border-color-1);
            border-radius: 8px;
            padding: 1.5rem;
            text-align: center;
            text-decoration: none;
            color: var(--text-color-1);
            transition: all 0.2s;
            display: flex;
            flex-direction: column;
            align-items: center;
            gap: 0.5rem;
        }
        .quick-action-btn:hover {
            border-color: var(--primary-color-1);
            background: var(--primary-color-1);
            color: white;
            transform: translateY(-2px);
        }
        
        [data-theme="dark"] .quick-action-btn {
            border-color: var(--border-color-1);
        }
        .quick-action-btn i {
            font-size: 2rem;
            margin-bottom: 0.5rem;
        }
        .recent-activity {
            background: var(--card-bg-1);
            border-radius: 12px;
            padding: 1.5rem;
            box-shadow: 0 2px 8px rgba(0,0,0,0.1);
            border: 1px solid var(--border-color-1);
        }
        
        [data-theme="dark"] .recent-activity {
            box-shadow: 0 2px 8px rgba(0,0,0,0.3);
        }
        .activity-item {
            padding: 1rem;
            border-bottom: 1px solid var(--border-color-1);
            display: flex;
            align-items: center;
            gap: 1rem;
        }
        .activity-item:last-child {
            border-bottom: none;
        }
        .activity-icon {
            width: 40px;
            height: 40px;
            border-radius: 50%;
            display: flex;
            align-items: center;
            justify-content: center;
            background: var(--primary-color-1);
            color: white;
        }
        .activity-content {
            flex: 1;
        }
        .activity-title {
            font-weight: 600;
            margin-bottom: 0.25rem;
        }
        .activity-time {
            font-size: 0.85rem;
            color: var(--text-secondary-1);
        }
        .help-tooltip {
            position: relative;
            display: inline-block;
            cursor: help;
            color: var(--text-secondary-1);
            margin-left: 0.5rem;
        }
        .help-tooltip:hover .tooltip-text {
            visibility: visible;
            opacity: 1;
        }
        .tooltip-text {
            visibility: hidden;
            width: 200px;
            background-color: var(--dark-color-1);
            color: white;
            text-align: center;
            border-radius: 6px;
            padding: 0.5rem;
            position: absolute;
            z-index: 1;
            bottom: 125%;
            left: 50%;
            margin-left: -100px;
            opacity: 0;
            transition: opacity 0.3s;
            font-size: 0.85rem;
        }
        .tooltip-text::after {
            content: "";
            position: absolute;
            top: 100%;
            left: 50%;
            margin-left: -5px;
            border-width: 5px;
            border-style: solid;
            border-color: var(--dark-color-1) transparent transparent transparent;
        }
    </style>
</head>
<body>
    <!-- Include Sidebar Component -->
    <?php include 'includes/sidebar.php'; ?>

    <!-- Include Admin Header Component -->
    <?php include 'includes/admin-header.php'; ?>
    
    <!-- ===================================
       MAIN CONTENT - Dashboard
       =================================== -->
    <div class="main-content">
        <div class="main-container">
            <div class="title">
                <h1>Dashboard <span class="help-tooltip">
                    <i class="fas fa-question-circle"></i>
                    <span class="tooltip-text">Your main control center. Here you can see all important statistics and quickly access key features.</span>
                </span></h1>
                <p>Welcome back! Here's an overview of your Emergency Communication System.</p>
            </div>
            
            <div class="sub-container">
                <div class="page-content">
                    <!-- Info Box for New Users -->
                    <div class="info-box">
                        <i class="fas fa-info-circle"></i>
                        <strong>Getting Started:</strong> This dashboard shows you everything at a glance. Click on any card or button to learn more about that feature.
                    </div>

                    <!-- Key Statistics -->
                    <div class="dashboard-grid">
                        <div class="stat-card subscribers">
                            <div class="stat-label">
                                <i class="fas fa-users"></i> Total Subscribers
                                <span class="help-tooltip">
                                    <i class="fas fa-question-circle"></i>
                                    <span class="tooltip-text">Total number of citizens registered to receive emergency alerts</span>
                                </span>
                            </div>
                            <div class="stat-value" id="totalSubscribers">0</div>
                            <div class="stat-change positive">
                                <i class="fas fa-arrow-up"></i>
                                <span id="subscriberChange">+0 this week</span>
                            </div>
                        </div>

                        <div class="stat-card notifications">
                            <div class="stat-label">
                                <i class="fas fa-bell"></i> Notifications Sent Today
                                <span class="help-tooltip">
                                    <i class="fas fa-question-circle"></i>
                                    <span class="tooltip-text">Number of emergency alerts sent today through all channels (SMS, Email, PA)</span>
                                </span>
                            </div>
                            <div class="stat-value" id="notificationsToday">0</div>
                            <div class="stat-change positive">
                                <i class="fas fa-check-circle"></i>
                                <span id="notificationStatus">All delivered</span>
                            </div>
                        </div>

                        <div class="stat-card success">
                            <div class="stat-label">
                                <i class="fas fa-check-circle"></i> Success Rate
                                <span class="help-tooltip">
                                    <i class="fas fa-question-circle"></i>
                                    <span class="tooltip-text">Percentage of notifications successfully delivered to recipients</span>
                                </span>
                            </div>
                            <div class="stat-value" id="successRate">0%</div>
                            <div class="stat-change positive">
                                <i class="fas fa-arrow-up"></i>
                                <span>Excellent</span>
                            </div>
                        </div>

                        <div class="stat-card weather">
                            <div class="stat-label">
                                <i class="fas fa-cloud-rain"></i> Weather Alerts
                                <span class="help-tooltip">
                                    <i class="fas fa-question-circle"></i>
                                    <span class="tooltip-text">Active weather-related alerts from PAGASA</span>
                                </span>
                            </div>
                            <div class="stat-value" id="weatherAlerts">0</div>
                            <div class="stat-change" id="weatherStatus">
                                <i class="fas fa-info-circle"></i>
                                <span>No active alerts</span>
                            </div>
                        </div>

                        <div class="stat-card earthquake">
                            <div class="stat-label">
                                <i class="fas fa-mountain"></i> Earthquake Alerts
                                <span class="help-tooltip">
                                    <i class="fas fa-question-circle"></i>
                                    <span class="tooltip-text">Active earthquake warnings from PHIVOLCS</span>
                                </span>
                            </div>
                            <div class="stat-value" id="earthquakeAlerts">0</div>
                            <div class="stat-change" id="earthquakeStatus">
                                <i class="fas fa-info-circle"></i>
                                <span>No active alerts</span>
                            </div>
                        </div>

                        <div class="stat-card pending">
                            <div class="stat-label">
                                <i class="fas fa-clock"></i> Pending Messages
                                <span class="help-tooltip">
                                    <i class="fas fa-question-circle"></i>
                                    <span class="tooltip-text">Messages from citizens waiting for your response</span>
                                </span>
                            </div>
                            <div class="stat-value" id="pendingMessages">0</div>
                            <div class="stat-change">
                                <i class="fas fa-comments"></i>
                                <span><a href="two-way-communication.php" style="color: var(--primary-color-1);">View conversations</a></span>
                            </div>
                        </div>
                    </div>

                    <!-- Quick Actions -->
                    <div class="chart-container">
                        <div class="chart-title">
                            <i class="fas fa-bolt"></i> Quick Actions
                            <span class="help-tooltip">
                                <i class="fas fa-question-circle"></i>
                                <span class="tooltip-text">Fast access to the most commonly used features</span>
                            </span>
                        </div>
                        <div class="quick-actions">
                            <a href="mass-notification.php" class="quick-action-btn">
                                <i class="fas fa-paper-plane"></i>
                                <strong>Send Alert</strong>
                                <small>Send emergency notification</small>
                            </a>
                            <a href="two-way-communication.php" class="quick-action-btn">
                                <i class="fas fa-comments"></i>
                                <strong>View Messages</strong>
                                <small>Check citizen messages</small>
                            </a>
                            <a href="citizen-subscriptions.php" class="quick-action-btn">
                                <i class="fas fa-users"></i>
                                <strong>Manage Subscribers</strong>
                                <small>View and edit subscriptions</small>
                            </a>
                            <a href="automated-warnings.php" class="quick-action-btn">
                                <i class="fas fa-plug"></i>
                                <strong>Check Integrations</strong>
                                <small>PAGASA & PHIVOLCS status</small>
                            </a>
                        </div>
                    </div>

                    <!-- Charts Row -->
                    <div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(400px, 1fr)); gap: 1.5rem; margin-bottom: 2rem;">
                        <!-- Notifications Chart -->
                        <div class="chart-container">
                            <div class="chart-title">
                                <i class="fas fa-chart-line"></i> Notifications This Week
                                <span class="help-tooltip">
                                    <i class="fas fa-question-circle"></i>
                                    <span class="tooltip-text">Daily breakdown of notifications sent over the past 7 days</span>
                                </span>
                            </div>
                            <canvas id="notificationsChart" height="200"></canvas>
                        </div>

                        <!-- Channel Distribution -->
                        <div class="chart-container">
                            <div class="chart-title">
                                <i class="fas fa-chart-pie"></i> Notification Channels
                                <span class="help-tooltip">
                                    <i class="fas fa-question-circle"></i>
                                    <span class="tooltip-text">Distribution of notifications by channel (SMS, Email, PA System)</span>
                                </span>
                            </div>
                            <canvas id="channelsChart" height="200"></canvas>
                        </div>
                    </div>

                    <!-- Recent Activity -->
                    <div class="recent-activity">
                        <div class="chart-title">
                            <i class="fas fa-history"></i> Recent Activity
                            <span class="help-tooltip">
                                <i class="fas fa-question-circle"></i>
                                <span class="tooltip-text">Latest actions and events in your system</span>
                            </span>
                        </div>
                        <div id="recentActivity">
                            <!-- Activity items will be loaded here -->
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <script src="https://cdn.jsdelivr.net/npm/chart.js"></script>
    <script>
        // Load dashboard data
        function loadDashboardData() {
            fetch('../api/dashboard.php')
                .then(response => response.json())
                .then(data => {
                    if (data.success) {
                        // Update statistics
                        document.getElementById('totalSubscribers').textContent = data.stats.total_subscribers || 0;
                        document.getElementById('subscriberChange').textContent = `+${data.stats.subscriber_change || 0} this week`;
                        document.getElementById('notificationsToday').textContent = data.stats.notifications_today || 0;
                        document.getElementById('successRate').textContent = (data.stats.success_rate || 0) + '%';
                        document.getElementById('weatherAlerts').textContent = data.stats.weather_alerts || 0;
                        document.getElementById('earthquakeAlerts').textContent = data.stats.earthquake_alerts || 0;
                        document.getElementById('pendingMessages').textContent = data.stats.pending_messages || 0;

                        // Update weather status
                        if (data.stats.weather_alerts > 0) {
                            document.getElementById('weatherStatus').innerHTML = '<i class="fas fa-exclamation-triangle"></i> <span style="color: #e74c3c;">Active alerts</span>';
                        }

                        // Update earthquake status
                        if (data.stats.earthquake_alerts > 0) {
                            document.getElementById('earthquakeStatus').innerHTML = '<i class="fas fa-exclamation-triangle"></i> <span style="color: #e74c3c;">Active alerts</span>';
                        }

                        // Load charts
                        if (data.charts) {
                            loadNotificationsChart(data.charts.notifications);
                            loadChannelsChart(data.charts.channels);
                        }

                        // Load recent activity
                        if (data.activity) {
                            loadRecentActivity(data.activity);
                        }
                    }
                })
                .catch(error => {
                    console.error('Error loading dashboard data:', error);
                });
        }

        function loadNotificationsChart(data) {
            const ctx = document.getElementById('notificationsChart').getContext('2d');
            new Chart(ctx, {
                type: 'line',
                data: {
                    labels: data.labels || ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun'],
                    datasets: [{
                        label: 'Notifications',
                        data: data.values || [0, 0, 0, 0, 0, 0, 0],
                        borderColor: '#4c8a89',
                        backgroundColor: 'rgba(76, 138, 137, 0.1)',
                        tension: 0.4,
                        fill: true
                    }]
                },
                options: {
                    responsive: true,
                    maintainAspectRatio: false,
                    plugins: {
                        legend: {
                            display: false
                        }
                    },
                    scales: {
                        y: {
                            beginAtZero: true
                        }
                    }
                }
            });
        }

        function loadChannelsChart(data) {
            const ctx = document.getElementById('channelsChart').getContext('2d');
            new Chart(ctx, {
                type: 'doughnut',
                data: {
                    labels: data.labels || ['SMS', 'Email', 'PA System'],
                    datasets: [{
                        data: data.values || [0, 0, 0],
                        backgroundColor: ['#3498db', '#2ecc71', '#f39c12']
                    }]
                },
                options: {
                    responsive: true,
                    maintainAspectRatio: false,
                    plugins: {
                        legend: {
                            position: 'bottom'
                        }
                    }
                }
            });
        }

        function loadRecentActivity(activities) {
            const container = document.getElementById('recentActivity');
            container.innerHTML = '';

            if (!activities || activities.length === 0) {
                container.innerHTML = '<div class="activity-item"><p style="color: var(--text-secondary-1);">No recent activity</p></div>';
                return;
            }

            activities.forEach(activity => {
                const item = document.createElement('div');
                item.className = 'activity-item';
                
                const iconClass = {
                    'notification': 'fa-bell',
                    'message': 'fa-comments',
                    'subscription': 'fa-user-plus',
                    'alert': 'fa-exclamation-triangle',
                    'integration': 'fa-plug'
                }[activity.type] || 'fa-circle';

                item.innerHTML = `
                    <div class="activity-icon">
                        <i class="fas ${iconClass}"></i>
                    </div>
                    <div class="activity-content">
                        <div class="activity-title">${activity.title}</div>
                        <div class="activity-time">${activity.time}</div>
                    </div>
                `;
                container.appendChild(item);
            });
        }

        // Load data on page load
        document.addEventListener('DOMContentLoaded', loadDashboardData);

        // Refresh data every 5 minutes
        setInterval(loadDashboardData, 300000);
    </script>
</body>
</html>

