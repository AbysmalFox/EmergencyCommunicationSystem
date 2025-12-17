<?php
/**
 * Mass Notification System Page
 * Manage SMS, Email, and PA (Public Address) Systems for broad communication
 */

$pageTitle = 'Mass Notification System';
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
    <link rel="stylesheet" href="css/datatables.css">
    <link rel="stylesheet" href="css/hero.css">
    <link rel="stylesheet" href="css/sidebar-footer.css">
    <link rel="stylesheet" href="css/modules.css">
</head>
<body>
    <!-- Include Sidebar Component -->
    <?php include 'includes/sidebar.php'; ?>

    <!-- Include Admin Header Component -->
    <?php include 'includes/admin-header.php'; ?>
    
    <!-- ===================================
       MAIN CONTENT - Mass Notification System
       =================================== -->
    <div class="main-content">
        <div class="main-container">
            <div class="title">
                <nav class="breadcrumb" aria-label="Breadcrumb">
                    <ol class="breadcrumb-list">
                        <li class="breadcrumb-item">
                            <a href="/" class="breadcrumb-link">
                                <span>Home</span>
                            </a>
                        </li>
                        <li class="breadcrumb-item">
                            <a href="/emergency-communication" class="breadcrumb-link">
                                <span>Emergency Communication</span>
                            </a>
                        </li>
                        <li class="breadcrumb-item active" aria-current="page">
                            <span>Mass Notification System</span>
                        </li>
                    </ol>
                </nav>
                <h1>Mass Notification System</h1>
                <p>Send notifications via SMS, Email, and Public Address (PA) Systems to reach citizens during emergencies.</p>
                <div class="info-box" style="background: #e3f2fd; border-left: 4px solid #2196f3; padding: 1rem; border-radius: 4px; margin-top: 1rem;">
                    <i class="fas fa-info-circle" style="color: #2196f3;"></i>
                    <strong>How to use:</strong> Click on any notification channel (SMS, Email, or PA System) to send an emergency alert. You can send to all subscribers or select specific groups.
                </div>
            </div>
            
            <div class="sub-container">
                <div class="page-content">
                    <!-- Notification Channels -->
                    <div class="module-card">
                        <div class="module-card-header">
                            <h2><i class="fas fa-broadcast-tower"></i> Notification Channels</h2>
                        </div>
                        <div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(300px, 1fr)); gap: 1.5rem;">
                            <div class="channel-card">
                                <h3><i class="fas fa-sms" style="color: var(--primary-color-1);"></i> SMS</h3>
                                <p>Send text messages to mobile subscribers</p>
                                <button class="btn btn-primary" onclick="openChannelModal('sms')">
                                    <i class="fas fa-paper-plane"></i> Send SMS
                                </button>
                            </div>
                            <div class="channel-card">
                                <h3><i class="fas fa-envelope" style="color: var(--primary-color-1);"></i> Email</h3>
                                <p>Send email notifications to registered users</p>
                                <button class="btn btn-primary" onclick="openChannelModal('email')">
                                    <i class="fas fa-paper-plane"></i> Send Email
                                </button>
                            </div>
                            <div class="channel-card">
                                <h3><i class="fas fa-bullhorn" style="color: var(--primary-color-1);"></i> PA System</h3>
                                <p>Broadcast announcements via Public Address systems</p>
                                <button class="btn btn-primary" onclick="openChannelModal('pa')">
                                    <i class="fas fa-paper-plane"></i> Broadcast PA
                                </button>
                            </div>
                        </div>
                    </div>

                    <!-- Recent Notifications -->
                    <div class="module-card">
                        <div class="module-card-header">
                            <h2><i class="fas fa-history"></i> Recent Notifications</h2>
                        </div>
                        <div>
                            <table class="data-table" id="notificationsTable">
                                <thead>
                                    <tr>
                                        <th>ID</th>
                                        <th>Channel</th>
                                        <th>Message</th>
                                        <th>Recipients</th>
                                        <th>Status</th>
                                        <th>Sent At</th>
                                        <th>Actions</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <!-- Data will be loaded via API -->
                                </tbody>
                            </table>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <!-- Notification Modal -->
    <div id="notificationModal" class="modal" style="display: none;">
        <div class="modal-content" style="max-width: 600px;">
            <div class="modal-header">
                <h2 id="modalTitle">Send Notification</h2>
                <button class="modal-close" onclick="closeNotificationModal()">&times;</button>
            </div>
            <div class="modal-body">
                <form id="notificationForm">
                    <input type="hidden" id="channelType" name="channel">
                    <div class="form-group">
                        <label for="message">Message *</label>
                        <textarea id="message" name="message" rows="5" required></textarea>
                    </div>
                    <div class="form-group">
                        <label for="recipients">Recipients *</label>
                        <select id="recipients" name="recipients" multiple required>
                            <option value="all">All Subscribers</option>
                            <option value="weather">Weather Alert Subscribers</option>
                            <option value="earthquake">Earthquake Alert Subscribers</option>
                            <option value="bomb">Bomb Threat Subscribers</option>
                        </select>
                        <small>Hold Ctrl/Cmd to select multiple</small>
                    </div>
                    <div class="form-group">
                        <label for="priority">Priority</label>
                        <select id="priority" name="priority">
                            <option value="low">Low</option>
                            <option value="medium" selected>Medium</option>
                            <option value="high">High</option>
                            <option value="critical">Critical</option>
                        </select>
                    </div>
                    <div class="form-actions">
                        <button type="button" class="btn btn-secondary" onclick="closeNotificationModal()">Cancel</button>
                        <button type="submit" class="btn btn-primary">
                            <i class="fas fa-paper-plane"></i> Send Notification
                        </button>
                    </div>
                </form>
            </div>
        </div>
    </div>

    <script>
        function openChannelModal(channel) {
            const modal = document.getElementById('notificationModal');
            const channelType = document.getElementById('channelType');
            const modalTitle = document.getElementById('modalTitle');
            
            channelType.value = channel;
            modalTitle.textContent = `Send ${channel.toUpperCase()} Notification`;
            modal.style.display = 'block';
        }

        function closeNotificationModal() {
            document.getElementById('notificationModal').style.display = 'none';
            document.getElementById('notificationForm').reset();
        }

        document.getElementById('notificationForm').addEventListener('submit', function(e) {
            e.preventDefault();
            const formData = new FormData(this);
            
            fetch('../api/mass-notification.php', {
                method: 'POST',
                body: formData
            })
            .then(response => response.json())
            .then(data => {
                if (data.success) {
                    alert('Notification sent successfully!');
                    closeNotificationModal();
                    loadNotifications();
                } else {
                    alert('Error: ' + data.message);
                }
            })
            .catch(error => {
                console.error('Error:', error);
                alert('An error occurred while sending the notification.');
            });
        });

        function loadNotifications() {
            fetch('../api/mass-notification.php?action=list')
                .then(response => response.json())
                .then(data => {
                    const tbody = document.querySelector('#notificationsTable tbody');
                    tbody.innerHTML = '';
                    
                    if (data.success && data.notifications) {
                        data.notifications.forEach(notif => {
                            const row = document.createElement('tr');
                            row.innerHTML = `
                                <td>${notif.id}</td>
                                <td><span class="badge">${notif.channel.toUpperCase()}</span></td>
                                <td>${notif.message.substring(0, 50)}...</td>
                                <td>${notif.recipients}</td>
                                <td><span class="badge ${notif.status}">${notif.status}</span></td>
                                <td>${notif.sent_at}</td>
                                <td>
                                    <button class="btn btn-sm btn-secondary" onclick="viewNotification(${notif.id})">
                                        <i class="fas fa-eye"></i>
                                    </button>
                                </td>
                            `;
                            tbody.appendChild(row);
                        });
                    }
                });
        }

        // Load notifications on page load
        document.addEventListener('DOMContentLoaded', loadNotifications);
    </script>
</body>
</html>

