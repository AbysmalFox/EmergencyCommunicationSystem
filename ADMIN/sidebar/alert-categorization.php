<?php
/**
 * Alert Categorization Page
 * Manage alert categories: Weather, Earthquake, Bomb Threat, etc.
 */

$pageTitle = 'Alert Categorization';
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
       MAIN CONTENT - Alert Categorization
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
                            <span>Alert Categorization</span>
                        </li>
                    </ol>
                </nav>
                <h1>Alert Categorization</h1>
                <p>Organize and manage alert categories for effective emergency communication. Categories include Weather, Earthquake, Bomb Threat, and more.</p>
                <div class="info-box" style="background: #e3f2fd; border-left: 4px solid #2196f3; padding: 1rem; border-radius: 4px; margin-top: 1rem;">
                    <i class="fas fa-info-circle" style="color: #2196f3;"></i>
                    <strong>How to use:</strong> Create categories to organize your alerts. Each category can have its own icon and color. Citizens can subscribe to specific categories they want to receive.
                </div>
            </div>
            
            <div class="sub-container">
                <div class="page-content">
                    <!-- Add New Category -->
                    <div class="module-card">
                        <div class="module-card-header">
                            <h2><i class="fas fa-plus-circle"></i> Add New Category</h2>
                        </div>
                        <div>
                            <form id="categoryForm">
                                <div class="form-row">
                                    <div class="form-group">
                                        <label for="categoryName">Category Name *</label>
                                        <input type="text" id="categoryName" name="name" required>
                                    </div>
                                    <div class="form-group">
                                        <label for="categoryIcon">Icon</label>
                                        <select id="categoryIcon" name="icon">
                                            <option value="fa-cloud-rain">Weather</option>
                                            <option value="fa-mountain">Earthquake</option>
                                            <option value="fa-bomb">Bomb Threat</option>
                                            <option value="fa-fire">Fire</option>
                                            <option value="fa-tornado">Tornado</option>
                                            <option value="fa-exclamation-triangle">General Alert</option>
                                        </select>
                                    </div>
                                </div>
                                <div class="form-group">
                                    <label for="categoryDescription">Description</label>
                                    <textarea id="categoryDescription" name="description" rows="3"></textarea>
                                </div>
                                <div class="form-group">
                                    <label for="categoryColor">Color</label>
                                    <input type="color" id="categoryColor" name="color" value="#4c8a89">
                                </div>
                                <div class="form-actions">
                                    <button type="submit" class="btn btn-primary">
                                        <i class="fas fa-save"></i> Add Category
                                    </button>
                                </div>
                            </form>
                        </div>
                    </div>

                    <!-- Categories List -->
                    <div class="module-card">
                        <div class="module-card-header">
                            <h2><i class="fas fa-list"></i> Alert Categories</h2>
                        </div>
                        <div>
                            <table class="data-table" id="categoriesTable">
                                <thead>
                                    <tr>
                                        <th>ID</th>
                                        <th>Name</th>
                                        <th>Icon</th>
                                        <th>Description</th>
                                        <th>Color</th>
                                        <th>Alerts Count</th>
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
        document.getElementById('categoryForm').addEventListener('submit', function(e) {
            e.preventDefault();
            const formData = new FormData(this);
            
            fetch('../api/alert-categories.php', {
                method: 'POST',
                body: formData
            })
            .then(response => response.json())
            .then(data => {
                if (data.success) {
                    alert('Category added successfully!');
                    this.reset();
                    loadCategories();
                } else {
                    alert('Error: ' + data.message);
                }
            })
            .catch(error => {
                console.error('Error:', error);
                alert('An error occurred while adding the category.');
            });
        });

        function loadCategories() {
            fetch('../api/alert-categories.php?action=list')
                .then(response => response.json())
                .then(data => {
                    const tbody = document.querySelector('#categoriesTable tbody');
                    tbody.innerHTML = '';
                    
                    if (data.success && data.categories) {
                        data.categories.forEach(cat => {
                            const row = document.createElement('tr');
                            row.innerHTML = `
                                <td>${cat.id}</td>
                                <td><strong>${cat.name}</strong></td>
                                <td><i class="fas ${cat.icon}"></i></td>
                                <td>${cat.description || 'N/A'}</td>
                                <td><span style="display: inline-block; width: 30px; height: 30px; background: ${cat.color}; border-radius: 4px;"></span></td>
                                <td>${cat.alerts_count || 0}</td>
                                <td>
                                    <button class="btn btn-sm btn-primary" onclick="editCategory(${cat.id})">
                                        <i class="fas fa-edit"></i>
                                    </button>
                                    <button class="btn btn-sm btn-danger" onclick="deleteCategory(${cat.id})">
                                        <i class="fas fa-trash"></i>
                                    </button>
                                </td>
                            `;
                            tbody.appendChild(row);
                        });
                    }
                });
        }

        function editCategory(id) {
            // Implementation for editing category
            alert('Edit category ' + id);
        }

        function deleteCategory(id) {
            if (confirm('Are you sure you want to delete this category?')) {
                fetch('../api/alert-categories.php', {
                    method: 'DELETE',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify({id: id})
                })
                .then(response => response.json())
                .then(data => {
                    if (data.success) {
                        alert('Category deleted successfully!');
                        loadCategories();
                    } else {
                        alert('Error: ' + data.message);
                    }
                });
            }
        }

        // Load categories on page load
        document.addEventListener('DOMContentLoaded', loadCategories);
    </script>
</body>
</html>

