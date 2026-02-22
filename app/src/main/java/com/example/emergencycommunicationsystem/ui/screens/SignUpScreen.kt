package com.example.emergencycommunicationsystem.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import com.example.emergencycommunicationsystem.ui.icons.AppIcons
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.emergencycommunicationsystem.R
import com.example.emergencycommunicationsystem.util.LocationUtils
import android.util.Patterns
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SignUpScreen(
    state: SignUpState,
    otpState: SignUpOtpState,
    onSignUpClick: (String, String, String, String, String, Boolean, Double?, Double?, String?) -> Unit,
    onEmailChanged: (String) -> Unit,
    onSendOtpClick: (String) -> Unit,
    onVerifyOtpClick: (String, String) -> Unit,
    onLoginClick: () -> Unit,
    onBackPressed: () -> Unit,
    onRegistrationSuccess: () -> Unit // New callback for redirection
) {
    val localeContext = com.example.emergencycommunicationsystem.util.getLocaleContext()
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var otpCode by remember { mutableStateOf("") }
    var locationPermissionGranted by remember { mutableStateOf(false) }
    var currentStep by rememberSaveable { mutableStateOf(1) }
    var stepError by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var latitude by remember { mutableStateOf<Double?>(null) }
    var longitude by remember { mutableStateOf<Double?>(null) }
    var address by remember { mutableStateOf<String?>(null) }

    // Handle the redirection after a delay
    LaunchedEffect(state) {
        if (state is SignUpState.Success) {
            delay(2000) // Keep the success message on screen for 2 seconds
            onRegistrationSuccess()
        }
    }

    Scaffold(containerColor = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            AnimatedVisibility(
                visible = state is SignUpState.Success,
                enter = fadeIn(animationSpec = tween(500)),
                exit = fadeOut(animationSpec = tween(500))
            ) {
                // --- Success State UI ---
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Icon(
                        painter = androidx.compose.ui.res.painterResource(id = R.drawable.ic_tabler_shield_check),
                        contentDescription = "Success",
                        tint = Color(0xFF4CAF50), // A nice green color
                        modifier = Modifier.size(100.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(localeContext.getString(R.string.registration_success), style = MaterialTheme.typography.headlineSmall, color = Color.White)
                    Text(localeContext.getString(R.string.redirecting_to_login), style = MaterialTheme.typography.bodyLarge, color = Color.White.copy(alpha = 0.8f))
                }
            }

            AnimatedVisibility(
                visible = state !is SignUpState.Success,
                enter = fadeIn(animationSpec = tween(500)),
                exit = fadeOut(animationSpec = tween(500))
            ) {
                // --- Form UI ---
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    IconButton(onClick = onBackPressed, modifier = Modifier.align(Alignment.Start)) {
                        Icon(AppIcons.ArrowBack, contentDescription = localeContext.getString(R.string.language_settings_back), tint = Color.White)
                    }
                    Text(localeContext.getString(R.string.create_account), style = MaterialTheme.typography.headlineLarge, color = Color.White)
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    val textFieldColors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedLabelColor = Color.White,
                        unfocusedLabelColor = Color.White.copy(alpha = 0.7f),
                        focusedBorderColor = Color.White,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
                        cursorColor = Color.White,
                        focusedPlaceholderColor = Color.White.copy(alpha = 0.5f),
                        unfocusedPlaceholderColor = Color.White.copy(alpha = 0.5f),
                        focusedPrefixColor = Color.White,
                        unfocusedPrefixColor = Color.White
                    )

                    Text(
                        text = if (currentStep == 1) "Step 1 of 2: Account Details" else "Step 2 of 2: Gmail OTP Verification",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.85f),
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    if (currentStep == 1) {
                        OutlinedTextField(
                            value = fullName, 
                            onValueChange = { fullName = it }, 
                            label = { Text(localeContext.getString(R.string.full_name)) }, 
                            modifier = Modifier.fillMaxWidth(),
                            colors = textFieldColors
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedTextField(
                            value = email, 
                            onValueChange = { 
                                email = it
                                onEmailChanged(it)
                                stepError = null
                            }, 
                            label = { Text(localeContext.getString(R.string.email_address)) }, 
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email), 
                            modifier = Modifier.fillMaxWidth(),
                            colors = textFieldColors
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedTextField(
                            value = phoneNumber,
                            onValueChange = { newNumber ->
                                if (newNumber.all { char -> char.isDigit() } && newNumber.length <= 10) {
                                    phoneNumber = newNumber
                                }
                            },
                            label = { Text(localeContext.getString(R.string.phone_number)) },
                            prefix = {
                                Text("+63 ", color = Color.White)
                            },
                            placeholder = { Text("9123456789") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            modifier = Modifier.fillMaxWidth(),
                            colors = textFieldColors
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedTextField(
                            value = password, 
                            onValueChange = { password = it }, 
                            label = { Text(localeContext.getString(R.string.password_label)) }, 
                            visualTransformation = PasswordVisualTransformation(), 
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password), 
                            modifier = Modifier.fillMaxWidth(),
                            colors = textFieldColors
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedTextField(
                            value = confirmPassword, 
                            onValueChange = { confirmPassword = it }, 
                            label = { Text(localeContext.getString(R.string.confirm_password_label)) }, 
                            visualTransformation = PasswordVisualTransformation(), 
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password), 
                            modifier = Modifier.fillMaxWidth(),
                            colors = textFieldColors
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = locationPermissionGranted, 
                                onCheckedChange = { locationPermissionGranted = it },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = MaterialTheme.colorScheme.primary,
                                    uncheckedColor = Color.White.copy(alpha = 0.6f),
                                    checkmarkColor = Color.White
                                )
                            )
                            Text(localeContext.getString(R.string.location_permission_consent), style = MaterialTheme.typography.bodyMedium, color = Color.White)
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    } else {
                        OutlinedTextField(
                            value = email,
                            onValueChange = { },
                            readOnly = true,
                            label = { Text(localeContext.getString(R.string.email_address)) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = textFieldColors
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { onSendOtpClick(email) },
                            enabled = state !is SignUpState.Loading && !otpState.isSending && otpState.canResendInSeconds == 0,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White,
                                contentColor = Color(0xFF2D5A57)
                            )
                        ) {
                            Text(
                                if (otpState.canResendInSeconds > 0) {
                                    "Resend OTP (${otpState.canResendInSeconds}s)"
                                } else if (otpState.isSending) {
                                    "Sending OTP..."
                                } else {
                                    "Send OTP to Gmail"
                                }
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = otpCode,
                            onValueChange = { newCode ->
                                if (newCode.all { it.isDigit() } && newCode.length <= 6) {
                                    otpCode = newCode
                                }
                            },
                            label = { Text("Gmail OTP Code") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(),
                            colors = textFieldColors
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { onVerifyOtpClick(email, otpCode) },
                            enabled = state !is SignUpState.Loading && otpState.isSent && !otpState.isVerified,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (otpState.isVerified) Color(0xFF4CAF50) else Color.White,
                                contentColor = if (otpState.isVerified) Color.White else Color(0xFF2D5A57)
                            )
                        ) {
                            Text(if (otpState.isVerified) "Gmail Verified" else "Verify OTP")
                        }
                        otpState.message?.let { message ->
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = message,
                                color = if (otpState.isVerified) Color(0xFF81C784) else MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    if (state is SignUpState.Loading) {
                        CircularProgressIndicator(color = Color.White)
                    } else if (state is SignUpState.Error) {
                        Text(state.message, color = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                    stepError?.let { message ->
                        Text(message, color = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    if (currentStep == 1) {
                        Button(
                            onClick = {
                                stepError = null
                                when {
                                    fullName.isBlank() || email.isBlank() || phoneNumber.isBlank() || password.isBlank() || confirmPassword.isBlank() ->
                                        stepError = "All fields are required."
                                    !Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches() || !email.trim().endsWith("@gmail.com", ignoreCase = true) ->
                                        stepError = "Please enter a valid Gmail address."
                                    password != confirmPassword ->
                                        stepError = "Passwords do not match."
                                    password.length < 8 ->
                                        stepError = "Password must be at least 8 characters long."
                                    else -> currentStep = 2
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = state !is SignUpState.Loading,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White,
                                contentColor = Color(0xFF2D5A57)
                            )
                        ) {
                            Text("Continue to OTP Verification")
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    } else {
                        OutlinedButton(
                            onClick = { currentStep = 1 },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = state !is SignUpState.Loading,
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                        ) {
                            Text("Back to Details")
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = {
                                scope.launch {
                                    if (locationPermissionGranted) {
                                        latitude = 14.5995
                                        longitude = 120.9842
                                        address = LocationUtils.getAddressFromCoordinates(context, latitude!!, longitude!!)
                                    }
                                    onSignUpClick(fullName, email, "+63$phoneNumber", password, confirmPassword, locationPermissionGranted, latitude, longitude, address)
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = state !is SignUpState.Loading && otpState.isVerified,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White,
                                contentColor = Color(0xFF2D5A57) // Dark teal matching background
                            )
                        ) {
                            Text(localeContext.getString(R.string.signup))
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                    TextButton(onClick = onLoginClick, enabled = state !is SignUpState.Loading) {
                        Text(localeContext.getString(R.string.already_have_account), color = Color.White)
                    }
                }
            }
        }
    }
}
