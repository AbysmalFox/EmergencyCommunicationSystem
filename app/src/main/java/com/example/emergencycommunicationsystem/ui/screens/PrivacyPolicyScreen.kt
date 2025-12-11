package com.example.emergencycommunicationsystem.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyPolicyScreen(onBackPressed: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Privacy Policy") },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) {
        paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            item {
                Text("Privacy Policy for AlertaraQc", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Last Updated: December 11, 2025", style = MaterialTheme.typography.bodySmall)
                Spacer(modifier = Modifier.height(16.dp))
                Text("This Privacy Policy describes how AlertaraQc (“we”, “our”, or “the system”) collects, uses, processes, and protects personal information when users access our emergency communication platform. By using AlertaraQc, you agree to the practices described in this policy.")
                Spacer(modifier = Modifier.height(16.dp))
                PolicySection("1. Information We Collect", "1.1 Personal Information", "When users register or use certain features, we may collect: Full name, Email address, Contact number, Username and password (securely hashed and stored on our server), User role (citizen, responder, employee, administrator)")
                PolicySection(null, "1.2 Location Data", "The system may request your location to: Send location-based emergency alerts, Allow users to report incidents with accurate coordinates, Assist responders during emergencies. Location data is collected only with your permission and only when necessary for service functionality.")
                PolicySection(null, "1.3 Incident and Report Data", "Information submitted during emergency reports may include: Photos or uploaded files, Description of incidents, Time, date, and coordinates of report, Type of emergency")
                PolicySection(null, "1.4 Device and Technical Data", "We may automatically collect: IP address, Browser or device type, Operating system, Access logs, System interactions for troubleshooting and security purposes")
                PolicySection("2. How We Use Your Information", null, "Collected information may be used to: Provide secure login and user authentication, Deliver real-time emergency notifications, Support communication between users and responders, Process and store emergency reports, Improve system stability, performance, and security, Prevent fraud, abuse, or unauthorized access, Maintain accurate records for emergency response purposes. We do not sell, trade, or share personal data with third-party advertisers.")
                PolicySection("3. Data Storage and Security", null, "AlertaraQc uses a PHP backend with a secured database hosted on a server. We apply multiple security measures, including: Password hashing, Encrypted communication (HTTPS), Role-based access controls, Regular security audits, Restricted server access. Despite using industry-standard protections, no online system can guarantee absolute security. We continuously work to safeguard your data.")
                PolicySection("4. Sharing of Information", null, "We may share limited data only under the following conditions: With authorized emergency responders and personnel, When required by law, court order, or government regulation, To address fraud, misconduct, or threats to public safety, With trusted service providers for hosting or system maintenance, bound by confidentiality agreements. We do not share information for marketing or advertising purposes.")
                PolicySection("5. Location Permissions", null, "Location access is optional but required for functions such as: Submitting accurate emergency reports, Receiving localized alerts, Allowing responders to locate individuals in need. You may disable location permissions at any time, though certain features may not work.")
                PolicySection("6. Data Retention", null, "We retain data only as long as it is necessary to: Provide system services, Maintain accurate emergency records, Comply with legal and operational requirements. Users may request deletion of their account and associated data, subject to verification.")
                PolicySection("7. Children’s Privacy", null, "AlertaraQc is not intended for children under 13. We do not knowingly collect personal information from children. If such data is identified, it will be deleted promptly.")
                PolicySection("8. Your Rights", null, "Depending on your region, you may have the right to: Access your personal information, Update or correct your data, Request account deletion, Withdraw consent for location use, Contact us for privacy inquiries")
                PolicySection("9. Changes to This Privacy Policy", null, "We may update this policy from time to time. Any changes will be posted on our official website or application. Continued use of AlertaraQc indicates acceptance of the updated policy.")
                PolicySection("10. Contact Information", null, "For questions or concerns regarding this Privacy Policy, contact us at: Email: [Insert official email address] Organization / Developer: AlertaraQc Development Team")

            }
        }
    }
}

@Composable
fun PolicySection(title: String?, subtitle: String?, content: String) {
    Column(modifier = Modifier.padding(bottom = 16.dp)) {
        title?.let {
            Text(it, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
        }
        subtitle?.let {
            Text(it, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(4.dp))
        }
        Text(content)
    }
}
