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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.emergencycommunicationsystem.R
import com.example.emergencycommunicationsystem.util.LocationUtils
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable

fun SignUpScreen(

    state: SignUpState,

    onSignUpClick: (String, String, String, String, String, Boolean, Double?, Double?, String?) -> Unit,

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

    var locationPermissionGranted by remember { mutableStateOf(false) }

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

                        imageVector = AppIcons.CheckCircle,

                        contentDescription = "Success",

                        tint = Color(0xFF4CAF50), // A nice green color

                        modifier = Modifier.size(100.dp)

                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(localeContext.getString(R.string.registration_success), style = MaterialTheme.typography.headlineSmall)

                    Text(localeContext.getString(R.string.redirecting_to_login), style = MaterialTheme.typography.bodyLarge)

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

                        Icon(AppIcons.ArrowBack, contentDescription = localeContext.getString(R.string.language_settings_back))

                    }

                    Text(localeContext.getString(R.string.create_account), style = MaterialTheme.typography.headlineLarge, color = MaterialTheme.colorScheme.onBackground)

                    Spacer(modifier = Modifier.height(32.dp))

                    OutlinedTextField(value = fullName, onValueChange = { fullName = it }, label = { Text(localeContext.getString(R.string.full_name)) }, modifier = Modifier.fillMaxWidth())

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text(localeContext.getString(R.string.email_address)) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email), modifier = Modifier.fillMaxWidth())

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(

                        value = phoneNumber,

                        onValueChange = { newNumber ->

                            if (newNumber.all { char -> char.isDigit() } && newNumber.length <= 10) {

                                phoneNumber = newNumber

                            }

                        },

                        label = { Text(localeContext.getString(R.string.phone_number)) },

                        leadingIcon = {

                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(start = 8.dp)) {

                                Text("+63", style = MaterialTheme.typography.bodyLarge)

                                Spacer(modifier = Modifier.width(4.dp))

                            }

                        },

                        placeholder = { Text("9123456789") },

                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),

                        modifier = Modifier.fillMaxWidth()

                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text(localeContext.getString(R.string.password_label)) }, visualTransformation = PasswordVisualTransformation(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password), modifier = Modifier.fillMaxWidth())

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(value = confirmPassword, onValueChange = { confirmPassword = it }, label = { Text(localeContext.getString(R.string.confirm_password_label)) }, visualTransformation = PasswordVisualTransformation(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password), modifier = Modifier.fillMaxWidth())

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {

                        Checkbox(checked = locationPermissionGranted, onCheckedChange = { locationPermissionGranted = it })

                        Text(localeContext.getString(R.string.location_permission_consent), style = MaterialTheme.typography.bodyMedium)

                    }

                    Spacer(modifier = Modifier.height(16.dp))



                    if (state is SignUpState.Loading) {

                        CircularProgressIndicator()

                    } else if (state is SignUpState.Error) {

                        Text(state.message, color = MaterialTheme.colorScheme.error)

                        Spacer(modifier = Modifier.height(16.dp))

                    }



                    Button(

                        onClick = {

                            scope.launch {

                                if (locationPermissionGranted) {

                                    // You would have a way to get the current location here.

                                    // For now, we'll use a placeholder. You would replace this with a call to your location provider.

                                    latitude = 14.5995

                                    longitude = 120.9842

                                    address = LocationUtils.getAddressFromCoordinates(context, latitude!!, longitude!!)

                                }

                                onSignUpClick(fullName, email, "+63$phoneNumber", password, confirmPassword, locationPermissionGranted, latitude, longitude, address)

                            }

                        },

                        modifier = Modifier.fillMaxWidth(),

                        enabled = state !is SignUpState.Loading

                    ) {

                        Text(localeContext.getString(R.string.signup))

                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    TextButton(onClick = onLoginClick, enabled = state !is SignUpState.Loading) {

                        Text(localeContext.getString(R.string.already_have_account), color = MaterialTheme.colorScheme.primary)

                    }

                }

            }

        }

    }

}
