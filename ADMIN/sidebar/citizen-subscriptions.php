<?php
/**
 * Citizen Subscription and Alert Preferences Page
 * Manage citizen subscriptions and their alert preferences
 */

$pageTitle = 'Citizen Subscription and Alert Preferences';
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
       MAIN CONTENT - Citizen Subscriptions
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
                            <span>Citizen Subscriptions</span>
                        </li>
                    </ol>
                </nav>
                <h1>Citizen Subscription and Alert Preferences</h1>
                <p>Manage citizen subscriptions and allow citizens to customize their alert preferences for personalized emergency notifications.</p>
                <div class="info-box" style="background: #e3f2fd; border-left: 4px solid #2196f3; padding: 1rem; border-radius: 4px; margin-top: 1rem;">
                    <i class="fas fa-info-circle" style="color: #2196f3;"></i>
                    <strong>How to use:</strong> View all subscribers and their preferences. Click "View" to see or edit a subscriber's alert categories, notification channels, and preferred language. Use the search box to find specific subscribers.
                </div>
            </div>
            
            <div class="sub-container">
                <div class="page-content">
                    <!-- Subscription Statistics -->
                    <div class="module-card">
                        <div class="module-card-header">
                            <h2><i class="fas fa-chart-bar"></i> Subscription Statistics</h2>
                        </div>
                        <div>
                            <div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: 1.5rem;">
                                <div class="stat-card" style="text-align: center;">
                                    <div>
                                        <h3 style="font-size: 2.5rem; margin: 0; color: #4c8a89;" id="totalSubscribers">0</h3>
                                        <p>Total Subscribers</p>
                                    </div>
                                </div>
                                <div class="stat-card" style="text-align: center;">
                                    <div>
                                        <h3 style="font-size: 2.5rem; margin: 0; color: var(--primary-color-1);" id="activeSubscribers">0</h3>
                                        <p>Active Subscribers</p>
                                    </div>
                                </div>
                                <div class="stat-card" style="text-align: center;">
                                    <div>
                                        <h3 style="font-size: 2.5rem; margin: 0; color: var(--primary-color-1);" id="weatherSubscribers">0</h3>
                                        <p>Weather Alert Subscribers</p>
                                    </div>
                                </div>
                                <div class="stat-card" style="text-align: center;">
                                    <div>
                                        <h3 style="font-size: 2.5rem; margin: 0; color: #4c8a89;" id="earthquakeSubscribers">0</h3>
                                        <p>Earthquake Alert Subscribers</p>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>

                    <!-- Subscribers List -->
                    <div class="module-card">
                        <div class="module-card-header" style="display: flex; justify-content: space-between; align-items: center;">
                            <h2><i class="fas fa-users"></i> Subscribers</h2>
                            <button class="btn btn-primary" onclick="exportSubscribers()">
                                <i class="fas fa-download"></i> Export
                            </button>
                        </div>
                        <div>
                            <div style="margin-bottom: 1rem;">
                                <input type="text" id="searchSubscribers" placeholder="Search subscribers..." style="width: 100%; padding: 0.75rem;">
                            </div>
                            <table class="data-table" id="subscribersTable">
                                <thead>
                                    <tr>
                                        <th>ID</th>
                                        <th>Name</th>
                                        <th>Email</th>
                                        <th>Phone</th>
                                        <th>Subscribed Categories</th>
                                        <th>Notification Channels</th>
                                        <th>Language</th>
                                        <th>Status</th>
                                        <th>Actions</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <!-- Data will be loaded via API -->
                                </tbody>
                            </table>
                        </div>
                    </div>

                    <!-- View/Edit Subscription Modal -->
                    <div id="subscriptionModal" class="modal" style="display: none;">
                        <div class="modal-content" style="max-width: 600px;">
                            <div class="modal-header">
                                <h2 id="modalTitle">Subscription Details</h2>
                                <button class="modal-close" onclick="closeSubscriptionModal()">&times;</button>
                            </div>
                            <div class="modal-body">
                                <form id="subscriptionForm">
                                    <input type="hidden" id="subscriberId" name="subscriber_id">
                                    <div class="form-group">
                                        <label>Subscribed Categories</label>
                                        <div style="display: flex; flex-direction: column; gap: 0.5rem; margin-top: 0.5rem;">
                                            <label><input type="checkbox" name="categories[]" value="weather"> Weather Alerts</label>
                                            <label><input type="checkbox" name="categories[]" value="earthquake"> Earthquake Alerts</label>
                                            <label><input type="checkbox" name="categories[]" value="bomb"> Bomb Threat Alerts</label>
                                            <label><input type="checkbox" name="categories[]" value="fire"> Fire Alerts</label>
                                            <label><input type="checkbox" name="categories[]" value="general"> General Alerts</label>
                                        </div>
                                    </div>
                                    <div class="form-group">
                                        <label>Notification Channels</label>
                                        <div style="display: flex; flex-direction: column; gap: 0.5rem; margin-top: 0.5rem;">
                                            <label><input type="checkbox" name="channels[]" value="sms"> SMS</label>
                                            <label><input type="checkbox" name="channels[]" value="email"> Email</label>
                                            <label><input type="checkbox" name="channels[]" value="push"> Push Notification</label>
                                        </div>
                                    </div>
                                    <div class="form-group">
                                        <label for="preferredLanguage">Preferred Language</label>
                                        <select id="preferredLanguage" name="preferred_language">
                                            <option value="en">English</option>
                                            <option value="tl">Filipino (Tagalog)</option>
                                            <option value="ceb">Cebuano</option>
                                        </select>
                                    </div>
                                    <div class="form-group">
                                        <label for="subscriptionStatus">Status</label>
                                        <select id="subscriptionStatus" name="status">
                                            <option value="active">Active</option>
                                            <option value="inactive">Inactive</option>
                                            <option value="suspended">Suspended</option>
                                        </select>
                                    </div>
                                    <div class="form-actions">
                                        <button type="button" class="btn btn-secondary" onclick="closeSubscriptionModal()">Cancel</button>
                                        <button type="submit" class="btn btn-primary">
                                            <i class="fas fa-save"></i> Save Changes
                                        </button>
                                    </div>
                                </form>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <script>
        function loadSubscribers() {
            fetch('../api/citizen-subscriptions.php?action=list')
                .then(response => response.json())
                .then(data => {
                    const tbody = document.querySelector('#subscribersTable tbody');
                    tbody.innerHTML = '';
                    
                    if (data.success && data.subscribers) {
                        data.subscribers.forEach(sub => {
                            const row = document.createElement('tr');
                            row.innerHTML = `
                                <td>${sub.id}</td>
                                <td>${sub.name}</td>
                                <td>${sub.email}</td>
                                <td>${sub.phone || 'N/A'}</td>
                                <td>${sub.categories.join(', ') || 'None'}</td>
                                <td>${sub.channels.join(', ') || 'None'}</td>
                                <td>${sub.language || 'en'}</td>
                                <td><span class="badge ${sub.status}">${sub.status}</span></td>
                                <td>
                                    <button class="btn btn-sm btn-primary" onclick="viewSubscription(${sub.id})">
                                        <i class="fas fa-eye"></i>
                                    </button>
                                    <button class="btn btn-sm btn-danger" onclick="deleteSubscription(${sub.id})">
                                        <i class="fas fa-trash"></i>
                                    </button>
                                </td>
                            `;
                            tbody.appendChild(row);
                        });
                    }
                });
        }

        function loadStatistics() {
            fetch('../api/citizen-subscriptions.php?action=statistics')
                .then(response => response.json())
                .then(data => {
                    if (data.success) {
                        document.getElementById('totalSubscribers').textContent = data.total || 0;
                        document.getElementById('activeSubscribers').textContent = data.active || 0;
                        document.getElementById('weatherSubscribers').textContent = data.weather || 0;
                        document.getElementById('earthquakeSubscribers').textContent = data.earthquake || 0;
                    }
                });
        }

        function viewSubscription(id) {
            fetch(`../api/citizen-subscriptions.php?action=get&id=${id}`)
                .then(response => response.json())
                .then(data => {
                    if (data.success && data.subscriber) {
                        const sub = data.subscriber;
                        document.getElementById('subscriberId').value = sub.id;
                        
                        // Set categories
                        document.querySelectorAll('input[name="categories[]"]').forEach(cb => {
                            cb.checked = sub.categories.includes(cb.value);
                        });
                        
                        // Set channels
                        document.querySelectorAll('input[name="channels[]"]').forEach(cb => {
                            cb.checked = sub.channels.includes(cb.value);
                        });
                        
                        document.getElementById('preferredLanguage').value = sub.language || 'en';
                        document.getElementById('subscriptionStatus').value = sub.status || 'active';
                        
                        document.getElementById('subscriptionModal').style.display = 'block';
                    }
                });
        }

        function closeSubscriptionModal() {
            document.getElementById('subscriptionModal').style.display = 'none';
            document.getElementById('subscriptionForm').reset();
        }

        document.getElementById('subscriptionForm').addEventListener('submit', function(e) {
            e.preventDefault();
            const formData = new FormData(this);
            
            fetch('../api/citizen-subscriptions.php', {
                method: 'POST',
                body: formData
            })
            .then(response => response.json())
            .then(data => {
                if (data.success) {
                    alert('Subscription updated successfully!');
                    closeSubscriptionModal();
                    loadSubscribers();
                    loadStatistics();
                } else {
                    alert('Error: ' + data.message);
                }
            });
        });

        function deleteSubscription(id) {
            if (confirm('Are you sure you want to delete this subscription?')) {
                fetch('../api/citizen-subscriptions.php', {
                    method: 'DELETE',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify({id: id})
                })
                .then(response => response.json())
                .then(data => {
                    if (data.success) {
                        alert('Subscription deleted successfully!');
                        loadSubscribers();
                        loadStatistics();
                    } else {
                        alert('Error: ' + data.message);
                    }
                });
            }
        }

        function exportSubscribers() {
            window.location.href = '../api/citizen-subscriptions.php?action=export';
        }

        // Search functionality
        document.getElementById('searchSubscribers').addEventListener('input', function(e) {
            const searchTerm = e.target.value.toLowerCase();
            const rows = document.querySelectorAll('#subscribersTable tbody tr');
            rows.forEach(row => {
                const text = row.textContent.toLowerCase();
                row.style.display = text.includes(searchTerm) ? '' : 'none';
            });
        });

        // Load data on page load
        document.addEventListener('DOMContentLoaded', function() {
            loadSubscribers();
            loadStatistics();
        });
    </script>
</body>
</html>

