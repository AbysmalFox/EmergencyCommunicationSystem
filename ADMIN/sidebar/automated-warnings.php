<?php
/**
 * Automated Warning Integration Page
 * Integrate with external warning feeds like PAGASA and PHIVOLCS
 */

$pageTitle = 'Automated Warning Integration';
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
       MAIN CONTENT - Automated Warning Integration
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
                            <span>Automated Warning Integration</span>
                        </li>
                    </ol>
                </nav>
                <h1>Automated Warning Integration</h1>
                <p>Integrate with external warning feeds from PAGASA (weather) and PHIVOLCS (earthquake) for automated alert distribution.</p>
                <div class="info-box" style="background: #e3f2fd; border-left: 4px solid #2196f3; padding: 1rem; border-radius: 4px; margin-top: 1rem;">
                    <i class="fas fa-info-circle" style="color: #2196f3;"></i>
                    <strong>How to use:</strong> Toggle the switches to enable/disable automatic warnings from PAGASA (weather) and PHIVOLCS (earthquake). When enabled, warnings will automatically sync and can be sent to subscribers.
                </div>
            </div>
            
            <div class="sub-container">
                <div class="page-content">
                    <!-- Integration Status -->
                    <div class="module-card">
                        <div class="module-card-header">
                            <h2><i class="fas fa-plug"></i> Integration Status</h2>
                        </div>
                        <div>
                            <div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(300px, 1fr)); gap: 1.5rem;">
                                <div class="channel-card" id="pagasaCard">
                                    <div>
                                        <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 1rem;">
                                            <h3><i class="fas fa-cloud-sun" style="color: #4c8a89;"></i> PAGASA</h3>
                                            <span class="badge" id="pagasaStatus">Connecting...</span>
                                        </div>
                                        <p>Philippine Atmospheric, Geophysical and Astronomical Services Administration</p>
                                        <div style="margin-top: 1rem;">
                                            <label class="switch">
                                                <input type="checkbox" id="pagasaToggle" onchange="toggleIntegration('pagasa', this.checked)">
                                                <span class="slider"></span>
                                            </label>
                                            <span style="margin-left: 0.5rem;">Enable Integration</span>
                                        </div>
                                    </div>
                                </div>
                                <div class="channel-card" id="phivolcsCard">
                                    <div>
                                        <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 1rem;">
                                            <h3><i class="fas fa-mountain" style="color: #4c8a89;"></i> PHIVOLCS</h3>
                                            <span class="badge" id="phivolcsStatus">Connecting...</span>
                                        </div>
                                        <p>Philippine Institute of Volcanology and Seismology</p>
                                        <div style="margin-top: 1rem;">
                                            <label class="switch">
                                                <input type="checkbox" id="phivolcsToggle" onchange="toggleIntegration('phivolcs', this.checked)">
                                                <span class="slider"></span>
                                            </label>
                                            <span style="margin-left: 0.5rem;">Enable Integration</span>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>

                    <!-- Integration Settings -->
                    <div class="module-card">
                        <div class="module-card-header">
                            <h2><i class="fas fa-cog"></i> Integration Settings</h2>
                        </div>
                        <div>
                            <form id="integrationSettingsForm">
                                <div class="form-group">
                                    <label for="syncInterval">Sync Interval (minutes)</label>
                                    <input type="number" id="syncInterval" name="sync_interval" value="15" min="1" max="60">
                                    <small>How often to check for new warnings from external sources</small>
                                </div>
                                <div class="form-group">
                                    <label for="autoPublish">Auto-Publish Warnings</label>
                                    <label class="switch">
                                        <input type="checkbox" id="autoPublish" name="auto_publish">
                                        <span class="slider"></span>
                                    </label>
                                    <small>Automatically publish warnings from external sources without manual review</small>
                                </div>
                                <div class="form-group">
                                    <label for="notificationChannels">Notification Channels</label>
                                    <div style="display: flex; gap: 1rem; margin-top: 0.5rem;">
                                        <label><input type="checkbox" name="channels[]" value="sms" checked> SMS</label>
                                        <label><input type="checkbox" name="channels[]" value="email" checked> Email</label>
                                        <label><input type="checkbox" name="channels[]" value="pa"> PA System</label>
                                    </div>
                                </div>
                                <div class="form-actions">
                                    <button type="submit" class="btn btn-primary">
                                        <i class="fas fa-save"></i> Save Settings
                                    </button>
                                </div>
                            </form>
                        </div>
                    </div>

                    <!-- Recent Warnings -->
                    <div class="module-card">
                        <div class="module-card-header">
                            <h2><i class="fas fa-exclamation-triangle"></i> Recent Automated Warnings</h2>
                        </div>
                        <div>
                            <table class="data-table" id="warningsTable">
                                <thead>
                                    <tr>
                                        <th>ID</th>
                                        <th>Source</th>
                                        <th>Type</th>
                                        <th>Title</th>
                                        <th>Severity</th>
                                        <th>Status</th>
                                        <th>Received At</th>
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


    <script>
        function toggleIntegration(source, enabled) {
            fetch('../api/automated-warnings.php', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({
                    action: 'toggle',
                    source: source,
                    enabled: enabled
                })
            })
            .then(response => response.json())
            .then(data => {
                if (data.success) {
                    const statusElement = document.getElementById(source + 'Status');
                    statusElement.textContent = enabled ? 'Connected' : 'Disabled';
                    statusElement.className = 'badge ' + (enabled ? 'success' : 'secondary');
                } else {
                    alert('Error: ' + data.message);
                }
            });
        }

        function loadIntegrationStatus() {
            fetch('../api/automated-warnings.php?action=status')
                .then(response => response.json())
                .then(data => {
                    if (data.success) {
                        if (data.pagasa) {
                            document.getElementById('pagasaToggle').checked = data.pagasa.enabled;
                            document.getElementById('pagasaStatus').textContent = data.pagasa.enabled ? 'Connected' : 'Disabled';
                            document.getElementById('pagasaStatus').className = 'badge ' + (data.pagasa.enabled ? 'success' : 'secondary');
                        }
                        if (data.phivolcs) {
                            document.getElementById('phivolcsToggle').checked = data.phivolcs.enabled;
                            document.getElementById('phivolcsStatus').textContent = data.phivolcs.enabled ? 'Connected' : 'Disabled';
                            document.getElementById('phivolcsStatus').className = 'badge ' + (data.phivolcs.enabled ? 'success' : 'secondary');
                        }
                    }
                });
        }

        function loadWarnings() {
            fetch('../api/automated-warnings.php?action=warnings')
                .then(response => response.json())
                .then(data => {
                    const tbody = document.querySelector('#warningsTable tbody');
                    tbody.innerHTML = '';
                    
                    if (data.success && data.warnings) {
                        data.warnings.forEach(warning => {
                            const row = document.createElement('tr');
                            row.innerHTML = `
                                <td>${warning.id}</td>
                                <td><span class="badge">${warning.source.toUpperCase()}</span></td>
                                <td>${warning.type}</td>
                                <td>${warning.title}</td>
                                <td><span class="badge ${warning.severity}">${warning.severity}</span></td>
                                <td><span class="badge ${warning.status}">${warning.status}</span></td>
                                <td>${warning.received_at}</td>
                                <td>
                                    <button class="btn btn-sm btn-primary" onclick="viewWarning(${warning.id})">
                                        <i class="fas fa-eye"></i>
                                    </button>
                                    <button class="btn btn-sm btn-success" onclick="publishWarning(${warning.id})">
                                        <i class="fas fa-paper-plane"></i>
                                    </button>
                                </td>
                            `;
                            tbody.appendChild(row);
                        });
                    }
                });
        }

        document.getElementById('integrationSettingsForm').addEventListener('submit', function(e) {
            e.preventDefault();
            const formData = new FormData(this);
            
            fetch('../api/automated-warnings.php', {
                method: 'POST',
                body: formData
            })
            .then(response => response.json())
            .then(data => {
                if (data.success) {
                    alert('Settings saved successfully!');
                } else {
                    alert('Error: ' + data.message);
                }
            });
        });

        // Load data on page load
        document.addEventListener('DOMContentLoaded', function() {
            loadIntegrationStatus();
            loadWarnings();
        });
    </script>
</body>
</html>

