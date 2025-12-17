<?php
/**
 * Log and Audit Trail for Sent Notifications Page
 * Track and audit all sent notifications for accountability and compliance
 */

$pageTitle = 'Log and Audit Trail';
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
       MAIN CONTENT - Log and Audit Trail
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
                            <span>Log and Audit Trail</span>
                        </li>
                    </ol>
                </nav>
                <h1>Log and Audit Trail for Sent Notifications</h1>
                <p>Comprehensive logging and audit trail system to track all sent notifications for accountability, compliance, and system monitoring.</p>
                <div class="info-box" style="background: #e3f2fd; border-left: 4px solid #2196f3; padding: 1rem; border-radius: 4px; margin-top: 1rem;">
                    <i class="fas fa-info-circle" style="color: #2196f3;"></i>
                    <strong>How to use:</strong> This page shows a complete record of all notifications sent. Use the filters to find specific notifications by date, channel, or status. Click "View" to see detailed information about any notification.
                </div>
            </div>
            
            <div class="sub-container">
                <div class="page-content">
                    <!-- Audit Statistics -->
                    <div class="module-card">
                        <div class="module-card-header">
                            <h2><i class="fas fa-chart-line"></i> Audit Statistics</h2>
                        </div>
                        <div>
                            <div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: 1.5rem;">
                                <div class="stat-card" style="text-align: center;">
                                    <div>
                                        <h3 style="font-size: 2.5rem; margin: 0; color: #4c8a89;" id="totalNotifications">0</h3>
                                        <p>Total Notifications Sent</p>
                                    </div>
                                </div>
                                <div class="stat-card" style="text-align: center;">
                                    <div>
                                        <h3 style="font-size: 2.5rem; margin: 0; color: var(--primary-color-1);" id="successfulNotifications">0</h3>
                                        <p>Successful</p>
                                    </div>
                                </div>
                                <div class="stat-card" style="text-align: center;">
                                    <div>
                                        <h3 style="font-size: 2.5rem; margin: 0; color: var(--primary-color-1);" id="failedNotifications">0</h3>
                                        <p>Failed</p>
                                    </div>
                                </div>
                                <div class="stat-card" style="text-align: center;">
                                    <div>
                                        <h3 style="font-size: 2.5rem; margin: 0; color: #4c8a89;" id="todayNotifications">0</h3>
                                        <p>Sent Today</p>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>

                    <!-- Filters -->
                    <div class="module-card">
                        <div class="module-card-header">
                            <h2><i class="fas fa-filter"></i> Filters</h2>
                        </div>
                        <div>
                            <form id="filterForm" style="display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: 1rem;">
                                <div class="form-group">
                                    <label for="filterDateFrom">Date From</label>
                                    <input type="date" id="filterDateFrom" name="date_from">
                                </div>
                                <div class="form-group">
                                    <label for="filterDateTo">Date To</label>
                                    <input type="date" id="filterDateTo" name="date_to">
                                </div>
                                <div class="form-group">
                                    <label for="filterChannel">Channel</label>
                                    <select id="filterChannel" name="channel">
                                        <option value="">All Channels</option>
                                        <option value="sms">SMS</option>
                                        <option value="email">Email</option>
                                        <option value="pa">PA System</option>
                                    </select>
                                </div>
                                <div class="form-group">
                                    <label for="filterStatus">Status</label>
                                    <select id="filterStatus" name="status">
                                        <option value="">All Status</option>
                                        <option value="success">Success</option>
                                        <option value="failed">Failed</option>
                                        <option value="pending">Pending</option>
                                    </select>
                                </div>
                                <div class="form-group" style="display: flex; align-items: flex-end;">
                                    <button type="button" class="btn btn-primary" onclick="applyFilters()">
                                        <i class="fas fa-search"></i> Apply Filters
                                    </button>
                                    <button type="button" class="btn btn-secondary" onclick="resetFilters()" style="margin-left: 0.5rem;">
                                        <i class="fas fa-redo"></i> Reset
                                    </button>
                                </div>
                            </form>
                        </div>
                    </div>

                    <!-- Audit Trail Table -->
                    <div class="module-card">
                        <div class="module-card-header" style="display: flex; justify-content: space-between; align-items: center;">
                            <h2><i class="fas fa-list"></i> Audit Trail</h2>
                            <button class="btn btn-primary" onclick="exportAuditTrail()">
                                <i class="fas fa-download"></i> Export
                            </button>
                        </div>
                        <div>
                            <table class="data-table" id="auditTrailTable">
                                <thead>
                                    <tr>
                                        <th>ID</th>
                                        <th>Timestamp</th>
                                        <th>Channel</th>
                                        <th>Recipient</th>
                                        <th>Message</th>
                                        <th>Status</th>
                                        <th>Sent By</th>
                                        <th>IP Address</th>
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

    <!-- View Details Modal -->
    <div id="detailsModal" class="modal" style="display: none;">
        <div class="modal-content" style="max-width: 700px;">
            <div class="modal-header">
                <h2>Notification Details</h2>
                <button class="modal-close" onclick="closeDetailsModal()">&times;</button>
            </div>
            <div class="modal-body" id="detailsContent">
                <!-- Details will be loaded here -->
            </div>
        </div>
    </div>

    <script>
        function loadAuditTrail() {
            const filters = getFilters();
            const queryParams = new URLSearchParams(filters).toString();
            
            fetch(`../api/audit-trail.php?action=list&${queryParams}`)
                .then(response => response.json())
                .then(data => {
                    const tbody = document.querySelector('#auditTrailTable tbody');
                    tbody.innerHTML = '';
                    
                    if (data.success && data.logs) {
                        data.logs.forEach(log => {
                            const row = document.createElement('tr');
                            row.innerHTML = `
                                <td>${log.id}</td>
                                <td>${log.timestamp}</td>
                                <td><span class="badge">${log.channel.toUpperCase()}</span></td>
                                <td>${log.recipient}</td>
                                <td>${log.message.substring(0, 50)}${log.message.length > 50 ? '...' : ''}</td>
                                <td><span class="badge ${log.status}">${log.status}</span></td>
                                <td>${log.sent_by || 'System'}</td>
                                <td>${log.ip_address || 'N/A'}</td>
                                <td>
                                    <button class="btn btn-sm btn-primary" onclick="viewDetails(${log.id})">
                                        <i class="fas fa-eye"></i>
                                    </button>
                                </td>
                            `;
                            tbody.appendChild(row);
                        });
                    }
                });
        }

        function loadStatistics() {
            fetch('../api/audit-trail.php?action=statistics')
                .then(response => response.json())
                .then(data => {
                    if (data.success) {
                        document.getElementById('totalNotifications').textContent = data.total || 0;
                        document.getElementById('successfulNotifications').textContent = data.successful || 0;
                        document.getElementById('failedNotifications').textContent = data.failed || 0;
                        document.getElementById('todayNotifications').textContent = data.today || 0;
                    }
                });
        }

        function getFilters() {
            return {
                date_from: document.getElementById('filterDateFrom').value,
                date_to: document.getElementById('filterDateTo').value,
                channel: document.getElementById('filterChannel').value,
                status: document.getElementById('filterStatus').value
            };
        }

        function applyFilters() {
            loadAuditTrail();
        }

        function resetFilters() {
            document.getElementById('filterForm').reset();
            loadAuditTrail();
        }

        function viewDetails(id) {
            fetch(`../api/audit-trail.php?action=get&id=${id}`)
                .then(response => response.json())
                .then(data => {
                    if (data.success && data.log) {
                        const log = data.log;
                        const content = document.getElementById('detailsContent');
                        content.innerHTML = `
                            <div style="display: grid; gap: 1rem;">
                                <div><strong>ID:</strong> ${log.id}</div>
                                <div><strong>Timestamp:</strong> ${log.timestamp}</div>
                                <div><strong>Channel:</strong> ${log.channel.toUpperCase()}</div>
                                <div><strong>Recipient:</strong> ${log.recipient}</div>
                                <div><strong>Message:</strong> ${log.message}</div>
                                <div><strong>Status:</strong> <span class="badge ${log.status}">${log.status}</span></div>
                                <div><strong>Sent By:</strong> ${log.sent_by || 'System'}</div>
                                <div><strong>IP Address:</strong> ${log.ip_address || 'N/A'}</div>
                                <div><strong>Response:</strong> ${log.response || 'N/A'}</div>
                                <div><strong>Error Message:</strong> ${log.error_message || 'None'}</div>
                            </div>
                        `;
                        document.getElementById('detailsModal').style.display = 'block';
                    }
                });
        }

        function closeDetailsModal() {
            document.getElementById('detailsModal').style.display = 'none';
        }

        function exportAuditTrail() {
            const filters = getFilters();
            const queryParams = new URLSearchParams({...filters, action: 'export'}).toString();
            window.location.href = `../api/audit-trail.php?${queryParams}`;
        }

        // Load data on page load
        document.addEventListener('DOMContentLoaded', function() {
            loadAuditTrail();
            loadStatistics();
        });
    </script>
</body>
</html>

