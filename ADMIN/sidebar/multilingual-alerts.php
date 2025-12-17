<?php
/**
 * Multilingual Support for Alerts Page
 * Manage alert translations and multilingual content
 */

$pageTitle = 'Multilingual Support for Alerts';
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
       MAIN CONTENT - Multilingual Support
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
                            <span>Multilingual Support</span>
                        </li>
                    </ol>
                </nav>
                <h1>Multilingual Support for Alerts</h1>
                <p>Manage alert translations to ensure alerts can be delivered in multiple languages for better accessibility.</p>
                <div class="info-box" style="background: #e3f2fd; border-left: 4px solid #2196f3; padding: 1rem; border-radius: 4px; margin-top: 1rem;">
                    <i class="fas fa-info-circle" style="color: #2196f3;"></i>
                    <strong>How to use:</strong> Select an alert, choose the target language, and provide the translated title and content. The system will automatically send alerts in the subscriber's preferred language.
                </div>
            </div>
            
            <div class="sub-container">
                <div class="page-content">
                    <!-- Supported Languages -->
                    <div class="module-card">
                        <div class="module-card-header">
                            <h2><i class="fas fa-language"></i> Supported Languages</h2>
                        </div>
                        <div>
                            <div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: 1rem;">
                                <div class="channel-card">
                                    <div>
                                        <h3><i class="fas fa-flag"></i> English</h3>
                                        <p>Status: <span class="badge success">Active</span></p>
                                        <button class="btn btn-sm btn-primary" onclick="manageLanguage('en')">
                                            <i class="fas fa-cog"></i> Manage
                                        </button>
                                    </div>
                                </div>
                                <div class="channel-card">
                                    <div>
                                        <h3><i class="fas fa-flag"></i> Filipino (Tagalog)</h3>
                                        <p>Status: <span class="badge success">Active</span></p>
                                        <button class="btn btn-sm btn-primary" onclick="manageLanguage('tl')">
                                            <i class="fas fa-cog"></i> Manage
                                        </button>
                                    </div>
                                </div>
                                <div class="channel-card">
                                    <div>
                                        <h3><i class="fas fa-flag"></i> Cebuano</h3>
                                        <p>Status: <span class="badge success">Active</span></p>
                                        <button class="btn btn-sm btn-primary" onclick="manageLanguage('ceb')">
                                            <i class="fas fa-cog"></i> Manage
                                        </button>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>

                    <!-- Translate Alert -->
                    <div class="module-card">
                        <div class="module-card-header">
                            <h2><i class="fas fa-language"></i> Translate Alert</h2>
                        </div>
                        <div>
                            <form id="translationForm">
                                <div class="form-group">
                                    <label for="alertSelect">Select Alert</label>
                                    <select id="alertSelect" name="alert_id" required>
                                        <option value="">-- Select an alert --</option>
                                        <!-- Options will be loaded via API -->
                                    </select>
                                </div>
                                <div class="form-group">
                                    <label for="targetLanguage">Target Language</label>
                                    <select id="targetLanguage" name="target_language" required>
                                        <option value="en">English</option>
                                        <option value="tl">Filipino (Tagalog)</option>
                                        <option value="ceb">Cebuano</option>
                                    </select>
                                </div>
                                <div class="form-group">
                                    <label for="translatedTitle">Translated Title *</label>
                                    <input type="text" id="translatedTitle" name="translated_title" required>
                                </div>
                                <div class="form-group">
                                    <label for="translatedContent">Translated Content *</label>
                                    <textarea id="translatedContent" name="translated_content" rows="6" required></textarea>
                                </div>
                                <div class="form-actions">
                                    <button type="submit" class="btn btn-primary">
                                        <i class="fas fa-save"></i> Save Translation
                                    </button>
                                </div>
                            </form>
                        </div>
                    </div>

                    <!-- Translation History -->
                    <div class="module-card">
                        <div class="module-card-header">
                            <h2><i class="fas fa-history"></i> Translation History</h2>
                        </div>
                        <div>
                            <table class="data-table" id="translationsTable">
                                <thead>
                                    <tr>
                                        <th>Alert ID</th>
                                        <th>Original Language</th>
                                        <th>Translated Language</th>
                                        <th>Title</th>
                                        <th>Status</th>
                                        <th>Translated At</th>
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
        function loadAlerts() {
            fetch('../api/alerts.php')
                .then(response => response.json())
                .then(data => {
                    const select = document.getElementById('alertSelect');
                    select.innerHTML = '<option value="">-- Select an alert --</option>';
                    
                    if (Array.isArray(data)) {
                        data.forEach(alert => {
                            const option = document.createElement('option');
                            option.value = alert.id;
                            option.textContent = alert.title;
                            select.appendChild(option);
                        });
                    }
                });
        }

        document.getElementById('alertSelect').addEventListener('change', function() {
            const alertId = this.value;
            if (alertId) {
                fetch(`../api/alerts.php?id=${alertId}`)
                    .then(response => response.json())
                    .then(data => {
                        if (data && data.length > 0) {
                            const alert = data[0];
                            document.getElementById('translatedTitle').placeholder = alert.title;
                            document.getElementById('translatedContent').placeholder = alert.message || alert.content;
                        }
                    });
            }
        });

        document.getElementById('translationForm').addEventListener('submit', function(e) {
            e.preventDefault();
            const formData = new FormData(this);
            
            fetch('../api/multilingual-alerts.php', {
                method: 'POST',
                body: formData
            })
            .then(response => response.json())
            .then(data => {
                if (data.success) {
                    alert('Translation saved successfully!');
                    this.reset();
                    loadTranslations();
                } else {
                    alert('Error: ' + data.message);
                }
            })
            .catch(error => {
                console.error('Error:', error);
                alert('An error occurred while saving the translation.');
            });
        });

        function loadTranslations() {
            fetch('../api/multilingual-alerts.php?action=list')
                .then(response => response.json())
                .then(data => {
                    const tbody = document.querySelector('#translationsTable tbody');
                    tbody.innerHTML = '';
                    
                    if (data.success && data.translations) {
                        data.translations.forEach(trans => {
                            const row = document.createElement('tr');
                            row.innerHTML = `
                                <td>${trans.alert_id}</td>
                                <td>${trans.original_language}</td>
                                <td>${trans.translated_language}</td>
                                <td>${trans.translated_title}</td>
                                <td><span class="badge ${trans.status}">${trans.status}</span></td>
                                <td>${trans.translated_at}</td>
                                <td>
                                    <button class="btn btn-sm btn-primary" onclick="editTranslation(${trans.id})">
                                        <i class="fas fa-edit"></i>
                                    </button>
                                    <button class="btn btn-sm btn-danger" onclick="deleteTranslation(${trans.id})">
                                        <i class="fas fa-trash"></i>
                                    </button>
                                </td>
                            `;
                            tbody.appendChild(row);
                        });
                    }
                });
        }

        function manageLanguage(langCode) {
            alert('Manage language: ' + langCode);
        }

        function editTranslation(id) {
            alert('Edit translation ' + id);
        }

        function deleteTranslation(id) {
            if (confirm('Are you sure you want to delete this translation?')) {
                fetch('../api/multilingual-alerts.php', {
                    method: 'DELETE',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify({id: id})
                })
                .then(response => response.json())
                .then(data => {
                    if (data.success) {
                        alert('Translation deleted successfully!');
                        loadTranslations();
                    } else {
                        alert('Error: ' + data.message);
                    }
                });
            }
        }

        // Load data on page load
        document.addEventListener('DOMContentLoaded', function() {
            loadAlerts();
            loadTranslations();
        });
    </script>
</body>
</html>

