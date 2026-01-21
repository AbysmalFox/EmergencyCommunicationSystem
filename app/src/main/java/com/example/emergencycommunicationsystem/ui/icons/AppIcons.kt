package com.example.emergencycommunicationsystem.ui.icons

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Centralized icon mapping
 * Using Material Icons Extended with some Tabler icons as vector drawables
 * Tabler icons can be added manually by converting SVG to vector drawables
 */
object AppIcons {
    // Navigation Icons
    val Home: ImageVector = Icons.Filled.Home
    val Alerts: ImageVector = Icons.Filled.Notifications
    val Profile: ImageVector = Icons.Filled.AccountCircle
    val Map: ImageVector = Icons.Filled.Map
    
    // Action Icons
    val EmergencyCall: ImageVector = Icons.Filled.Call
    val ReportIncident: ImageVector = Icons.Filled.Warning
    val Safe: ImageVector = Icons.Filled.CheckCircle
    val Message: ImageVector = Icons.AutoMirrored.Filled.Message
    val Chat: ImageVector = Icons.AutoMirrored.Filled.Chat
    
    // Alert Category Icons
    val Weather: ImageVector = Icons.Filled.Cloud
    val Earthquake: ImageVector = Icons.Filled.Warning
    // Fire icon - using Tabler flame icon from vector drawable
    // Note: For vector drawables, we'll use painterResource in composables
    val Fire: ImageVector = Icons.Filled.LocalFireDepartment // Fallback, will use Tabler icon via painterResource
    val Health: ImageVector = Icons.Filled.LocalHospital
    val Info: ImageVector = Icons.Filled.Info
    val Security: ImageVector = Icons.Filled.Security
    val Water: ImageVector = Icons.Filled.WaterDrop
    val Flood: ImageVector = Icons.Filled.WaterDrop
    
    // Common UI Icons
    val Location: ImageVector = Icons.Filled.LocationOn
    val MyLocation: ImageVector = Icons.Filled.MyLocation
    val CheckCircle: ImageVector = Icons.Filled.CheckCircle
    val Warning: ImageVector = Icons.Filled.Warning
    val Error: ImageVector = Icons.Filled.Error
    val ChevronRight: ImageVector = Icons.Filled.ChevronRight
    val NotificationsOff: ImageVector = Icons.Filled.NotificationsOff
    
    // Weather Icons
    val Thermostat: ImageVector = Icons.Filled.Thermostat
    val Visibility: ImageVector = Icons.Filled.Visibility
    val Wind: ImageVector = Icons.Filled.Air
    val Humidity: ImageVector = Icons.Filled.WaterDrop
    
    // Other Icons
    val Person: ImageVector = Icons.Filled.Person
    val AccountCircle: ImageVector = Icons.Filled.AccountCircle
    
    // Additional Icons
    val Language: ImageVector = Icons.Filled.Language
    val ArrowBack: ImageVector = Icons.AutoMirrored.Filled.ArrowBack
    val AddPhoto: ImageVector = Icons.Filled.AddAPhoto
    val LocalFireDepartment: ImageVector = Icons.Filled.LocalFireDepartment
    val MedicalServices: ImageVector = Icons.Filled.MedicalServices
    val LocalPolice: ImageVector = Icons.Filled.LocalPolice
    val Send: ImageVector = Icons.AutoMirrored.Filled.Send
    val MicOff: ImageVector = Icons.Filled.MicOff
    val Dialpad: ImageVector = Icons.Filled.Dialpad
    val CallEnd: ImageVector = Icons.Filled.CallEnd
    val Traffic: ImageVector = Icons.Filled.Traffic
    val Shield: ImageVector = Icons.Filled.Shield
    val LocalHospital: ImageVector = Icons.Filled.LocalHospital
    
    // Theme Icons
    val Settings: ImageVector = Icons.Filled.Settings
    val LightMode: ImageVector = Icons.Filled.LightMode
    val DarkMode: ImageVector = Icons.Filled.DarkMode
}
