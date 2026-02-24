package com.example.emergencycommunicationsystem.ui.screens

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import com.example.emergencycommunicationsystem.ui.icons.AppIcons
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.emergencycommunicationsystem.R
import com.example.emergencycommunicationsystem.util.GoogleSignInHelper
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException

@Composable

fun LoginScreen(

    onLoginSuccess: (userId: Int, username: String, email: String, phone: String, token: String) -> Unit, // Corrected callback

    onSignUpClick: () -> Unit,

    onBackPressed: () -> Unit,

    viewModel: LoginViewModel = viewModel()

) {

    val localeContext = com.example.emergencycommunicationsystem.util.getLocaleContext()

    var emailOrPhone by remember { mutableStateOf("") }

    var password by remember { mutableStateOf("") }

    var errorMessage by remember { mutableStateOf<String?>(null) }



    val context = LocalContext.current

    val loginState by viewModel.loginState.collectAsState()

    

    // Google Sign-In launcher
    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        // Pass the intent data directly to the ViewModel to handle
        // The ViewModel uses GoogleSignInHelper to extract the account and handle errors
        viewModel.handleGoogleSignInResult(result.data)
    }



    LaunchedEffect(loginState) {

        when (val state = loginState) {

            is LoginState.Success -> {

                Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()

                viewModel.resetLoginState()

                // Pass all 5 arguments to the callback

                onLoginSuccess(state.userId, state.username, state.email, state.phone, state.token)

            }

            is LoginState.Error -> {

                errorMessage = state.message

                viewModel.resetLoginState()

            }

            else -> {}

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

            IconButton(onClick = onBackPressed, modifier = Modifier.align(Alignment.Start)) {

                Icon(AppIcons.ArrowBack, contentDescription = localeContext.getString(R.string.language_settings_back), tint = MaterialTheme.colorScheme.onBackground)

            }

            Icon(
                painter = androidx.compose.ui.res.painterResource(id = R.drawable.ic_tabler_user),
                contentDescription = localeContext.getString(R.string.login),
                modifier = Modifier.size(100.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(

                value = emailOrPhone,

                onValueChange = {

                    emailOrPhone = it

                    errorMessage = null

                },

                label = { Text(localeContext.getString(R.string.email_phone_label)) },

                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),

                modifier = Modifier.fillMaxWidth(),

                enabled = loginState !is LoginState.Loading,

                colors = OutlinedTextFieldDefaults.colors(

                    focusedTextColor = MaterialTheme.colorScheme.onBackground,

                    unfocusedTextColor = MaterialTheme.colorScheme.onBackground,

                    focusedContainerColor = Color.Transparent,

                    unfocusedContainerColor = Color.Transparent,

                    cursorColor = MaterialTheme.colorScheme.onBackground,

                    focusedLabelColor = MaterialTheme.colorScheme.onBackground,

                    unfocusedLabelColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),

                    focusedBorderColor = MaterialTheme.colorScheme.onBackground,

                    unfocusedBorderColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)

                )

            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(

                value = password,

                onValueChange = {

                    password = it

                    errorMessage = null

                },

                label = { Text(localeContext.getString(R.string.password_label)) },

                visualTransformation = PasswordVisualTransformation(),

                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),

                modifier = Modifier.fillMaxWidth(),

                enabled = loginState !is LoginState.Loading,

                colors = OutlinedTextFieldDefaults.colors(

                    focusedTextColor = MaterialTheme.colorScheme.onBackground,

                    unfocusedTextColor = MaterialTheme.colorScheme.onBackground,

                    focusedContainerColor = Color.Transparent,

                    unfocusedContainerColor = Color.Transparent,

                    cursorColor = MaterialTheme.colorScheme.onBackground,

                    focusedLabelColor = MaterialTheme.colorScheme.onBackground,

                    unfocusedLabelColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),

                    focusedBorderColor = MaterialTheme.colorScheme.onBackground,

                    unfocusedBorderColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)

                )

            )

            Spacer(modifier = Modifier.height(16.dp))



            AnimatedVisibility(

                visible = errorMessage != null,

                enter = fadeIn(),

                exit = fadeOut()

            ) {

                Text(

                    text = errorMessage ?: "",

                    color = MaterialTheme.colorScheme.error,

                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),

                    textAlign = TextAlign.Center

                )

            }



            Spacer(modifier = Modifier.height(8.dp))



            Button(

                onClick = {

                    viewModel.login(emailOrPhone, password)

                },

                modifier = Modifier.fillMaxWidth(),

                enabled = loginState !is LoginState.Loading,

                colors = ButtonDefaults.buttonColors(

                    containerColor = MaterialTheme.colorScheme.secondary,

                    contentColor = MaterialTheme.colorScheme.onSecondary

                )

            ) {

                if (loginState is LoginState.Loading) {

                    CircularProgressIndicator(

                        modifier = Modifier.size(20.dp),

                        color = MaterialTheme.colorScheme.onSecondary

                    )

                } else {

                    Text(localeContext.getString(R.string.login))

                }

            }

            

            Spacer(modifier = Modifier.height(16.dp))

            

            // Divider with "OR"

            Row(

                modifier = Modifier.fillMaxWidth(),

                verticalAlignment = Alignment.CenterVertically

            ) {

                Box(

                    modifier = Modifier

                        .weight(1f)

                        .height(1.dp)

                        .padding(horizontal = 8.dp)

                        .background(color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f))

                )

                Text(

                    text = localeContext.getString(R.string.or_label),

                    modifier = Modifier.padding(horizontal = 16.dp),

                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),

                    fontSize = 14.sp

                )

                Box(

                    modifier = Modifier

                        .weight(1f)

                        .height(1.dp)

                        .padding(horizontal = 8.dp)

                        .background(color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f))

                )

            }

            

            Spacer(modifier = Modifier.height(16.dp))

            

            // Google Sign-In Button

            OutlinedButton(

                onClick = {

                    val signInIntent = GoogleSignInHelper.getSignInIntent(context)

                    googleSignInLauncher.launch(signInIntent)

                },

                modifier = Modifier.fillMaxWidth(),

                enabled = loginState !is LoginState.Loading,

                colors = ButtonDefaults.outlinedButtonColors(

                    contentColor = MaterialTheme.colorScheme.onBackground

                ),

                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f))

            ) {

                Row(

                    verticalAlignment = Alignment.CenterVertically,

                    horizontalArrangement = Arrangement.Center

                ) {

                    // Google icon (using account circle as placeholder)

                                Icon(
                                    painter = androidx.compose.ui.res.painterResource(id = R.drawable.ic_google_logo),
                                    contentDescription = "Google",
                                    modifier = Modifier.size(20.dp),
                                    tint = Color.Unspecified
                                )
                    Spacer(modifier = Modifier.width(8.dp))

                    Text(

                        text = localeContext.getString(R.string.google_login_label),

                        fontWeight = FontWeight.Medium

                    )

                }

            }

            

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(

                onClick = onSignUpClick,

                enabled = loginState !is LoginState.Loading

            ) {

                Text(localeContext.getString(R.string.dont_have_account), color = MaterialTheme.colorScheme.primary)

            }

        }

    }

}
