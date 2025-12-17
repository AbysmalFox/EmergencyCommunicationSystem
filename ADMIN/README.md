# Emergency Communication System - Admin Panel

## Overview
This is the administrative panel for the Emergency Communication System. It provides a user-friendly interface for managing emergency alerts, citizen subscriptions, and system integrations.

## Features

### 📊 Dashboard
- Real-time analytics and statistics
- Quick access to common actions
- Visual charts and graphs
- Recent activity feed

### 📢 Mass Notification System
- Send alerts via SMS, Email, and PA Systems
- Target specific subscriber groups
- Priority levels for urgent alerts

### 🏷️ Alert Categorization
- Organize alerts by type (Weather, Earthquake, Bomb Threat, etc.)
- Custom icons and colors
- Track alerts per category

### 💬 Two-Way Communication
- Real-time messaging with citizens
- Conversation management
- Quick response interface

### 🔌 Automated Warning Integration
- PAGASA (Weather) integration
- PHIVOLCS (Earthquake) integration
- Automatic alert synchronization

### 🌐 Multilingual Support
- Translate alerts to multiple languages
- Support for English, Filipino, Cebuano
- Automatic language detection

### 👥 Citizen Subscriptions
- Manage subscriber preferences
- Category-based subscriptions
- Notification channel preferences

### 📋 Audit Trail
- Complete log of all notifications
- Filter and search capabilities
- Export functionality

## Setup Instructions

### 1. Database Setup
Run the SQL schema file to create all necessary tables:
```sql
mysql -u root -p emergency_comm_db < api/database_schema.sql
```

Or import it through phpMyAdmin:
- Go to phpMyAdmin
- Select your database
- Click "Import"
- Choose `api/database_schema.sql`

### 2. Database Configuration
Edit `api/db_connect.php` and update your database credentials:
```php
$host = '127.0.0.1';
$db   = 'emergency_comm_db';
$user = 'root';
$pass = '';
```

### 3. Access the Admin Panel
1. Navigate to `ADMIN/sidebar/dashboard.php` in your browser
2. The dashboard will show analytics and quick actions
3. Use the sidebar to navigate between modules

## User Guide for Non-Technical Users

### Getting Started
1. **Dashboard**: Your main control center - shows all important statistics at a glance
2. **Hover over question marks (?)**: These provide helpful tooltips explaining each feature
3. **Quick Actions**: Use the colored buttons on the dashboard for common tasks

### Sending an Emergency Alert
1. Go to **Mass Notification System**
2. Click on the channel you want to use (SMS, Email, or PA System)
3. Type your message
4. Select who should receive it (All Subscribers or specific groups)
5. Choose the priority level
6. Click "Send Notification"

### Managing Subscribers
1. Go to **Citizen Subscriptions**
2. View all subscribers and their preferences
3. Click "View" to see or edit a subscriber's settings
4. Use the search box to find specific subscribers

### Responding to Citizen Messages
1. Go to **Two-Way Communication**
2. Click on a conversation from the left panel
3. Type your response
4. Click "Send"

### Checking Automated Warnings
1. Go to **Automated Warnings**
2. See the status of PAGASA and PHIVOLCS integrations
3. Toggle switches to enable/disable automatic warnings
4. View recent warnings from external sources

### Viewing System Activity
1. Go to **Audit Trail**
2. See all notifications that have been sent
3. Use filters to find specific notifications
4. Click "View" to see detailed information

## Tips for Non-Technical Users

- **Question Mark Icons**: Hover over any (?) icon to see helpful explanations
- **Info Boxes**: Blue boxes at the top of pages provide quick instructions
- **Color Coding**: 
  - Green = Success/Active
  - Red = Warning/Error
  - Blue = Information
  - Yellow = Pending
- **Breadcrumbs**: Use the navigation path at the top to see where you are
- **Search**: Most pages have search boxes to quickly find what you need

## Support

If you need help:
1. Check the tooltips (question mark icons)
2. Read the info boxes on each page
3. Review this README file

## File Structure

```
ADMIN/
├── api/                    # Backend API endpoints
│   ├── dashboard.php       # Dashboard analytics
│   ├── mass-notification.php
│   ├── alert-categories.php
│   ├── two-way-communication.php
│   ├── automated-warnings.php
│   ├── multilingual-alerts.php
│   ├── citizen-subscriptions.php
│   ├── audit-trail.php
│   └── database_schema.sql # Database setup
├── sidebar/               # Admin panel pages
│   ├── dashboard.php      # Main dashboard
│   ├── mass-notification.php
│   ├── alert-categorization.php
│   ├── two-way-communication.php
│   ├── automated-warnings.php
│   ├── multilingual-alerts.php
│   ├── citizen-subscriptions.php
│   ├── audit-trail.php
│   └── includes/          # Reusable components
└── README.md              # This file
```

## Notes

- All pages follow the same template structure for consistency
- The system is designed to be intuitive for non-technical users
- Help tooltips are available throughout the interface
- The dashboard auto-refreshes every 5 minutes

