package com.example.emergencycommunicationsystem.ui.screens

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.emergencycommunicationsystem.data.EmergencyGuidesData
import com.example.emergencycommunicationsystem.data.UserPrefs
import com.example.emergencycommunicationsystem.data.models.EmergencyGuide
import com.example.emergencycommunicationsystem.data.models.TipPriority
import com.example.emergencycommunicationsystem.ui.icons.AppIcons
import com.example.emergencycommunicationsystem.util.TranslationService

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmergencyGuideDetailScreen(
    guideId: String,
    onBackPressed: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val currentLanguage by UserPrefs.getLanguage(context).collectAsState(initial = "en")
    
    val guide = EmergencyGuidesData.getGuideById(guideId)
    
    // Translated UI strings
    var translatedNotFoundTitle by remember { mutableStateOf("Guide Not Found") }
    var translatedNotFoundMessage by remember { mutableStateOf("Emergency guide not found") }
    var translatedWhatToDo by remember { mutableStateOf("What to Do:") }
    var translatedRemember by remember { mutableStateOf("💡 Remember") }
    var translatedFooterNote by remember { mutableStateOf("In any emergency, call your local emergency services (911) immediately. These tips are general guidance and should not replace professional medical or emergency advice.") }
    
    // Translate UI strings
    LaunchedEffect(currentLanguage) {
        if (currentLanguage != "en") {
            coroutineScope.launch {
                translatedNotFoundTitle = TranslationService.translate("Guide Not Found", currentLanguage)
                translatedNotFoundMessage = TranslationService.translate("Emergency guide not found", currentLanguage)
                translatedWhatToDo = TranslationService.translate("What to Do:", currentLanguage)
                translatedRemember = TranslationService.translate("💡 Remember", currentLanguage)
                translatedFooterNote = TranslationService.translate("In any emergency, call your local emergency services (911) immediately. These tips are general guidance and should not replace professional medical or emergency advice.", currentLanguage)
            }
        } else {
            translatedNotFoundTitle = "Guide Not Found"
            translatedNotFoundMessage = "Emergency guide not found"
            translatedWhatToDo = "What to Do:"
            translatedRemember = "💡 Remember"
            translatedFooterNote = "In any emergency, call your local emergency services (911) immediately. These tips are general guidance and should not replace professional medical or emergency advice."
        }
    }
    
    if (guide == null) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(translatedNotFoundTitle) },
                    navigationIcon = {
                        IconButton(onClick = onBackPressed) {
                            Icon(
                                imageVector = AppIcons.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    }
                )
            }
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(it),
                contentAlignment = Alignment.Center
            ) {
                Text(translatedNotFoundMessage)
            }
        }
        return
    }
    
    // Translated guide content
    var translatedTitle by remember { mutableStateOf(guide.title) }
    var translatedDescription by remember { mutableStateOf(guide.description) }
    
    // Translate guide content
    LaunchedEffect(guide.id, currentLanguage) {
        if (currentLanguage != "en") {
            coroutineScope.launch {
                translatedTitle = TranslationService.translate(guide.title, currentLanguage)
                translatedDescription = TranslationService.translate(guide.description, currentLanguage)
            }
        } else {
            translatedTitle = guide.title
            translatedDescription = guide.description
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(translatedTitle) },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(
                            imageVector = AppIcons.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    shape = RoundedCornerShape(16.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = guide.icon,
                                fontSize = 36.sp
                            )
                        }
                        
                        Spacer(modifier = Modifier.width(16.dp))
                        
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = translatedTitle,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = translatedDescription,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
            
            // Tips Section
            item {
                Text(
                    text = translatedWhatToDo,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            
            itemsIndexed(guide.tips) { index, tip ->
                EmergencyTipCard(
                    tip = tip,
                    number = index + 1,
                    currentLanguage = currentLanguage
                )
            }
            
            // Footer Note
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = translatedRemember,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = translatedFooterNote,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EmergencyTipCard(
    tip: com.example.emergencycommunicationsystem.data.models.EmergencyTip,
    number: Int,
    currentLanguage: String
) {
    val coroutineScope = rememberCoroutineScope()
    val priorityColor = when (tip.priority) {
        TipPriority.CRITICAL -> MaterialTheme.colorScheme.error
        TipPriority.HIGH -> MaterialTheme.colorScheme.errorContainer
        TipPriority.NORMAL -> MaterialTheme.colorScheme.primary
    }
    
    // Translated tip content
    var translatedTitle by remember { mutableStateOf(tip.title) }
    var translatedDescription by remember { mutableStateOf(tip.description) }
    var translatedPriority by remember { mutableStateOf(tip.priority.name) }
    
    // Translate tip content
    LaunchedEffect(tip.title, tip.description, tip.priority.name, currentLanguage) {
        if (currentLanguage != "en") {
            coroutineScope.launch {
                translatedTitle = TranslationService.translate(tip.title, currentLanguage)
                translatedDescription = TranslationService.translate(tip.description, currentLanguage)
                translatedPriority = TranslationService.translate(tip.priority.name, currentLanguage)
            }
        } else {
            translatedTitle = tip.title
            translatedDescription = tip.description
            translatedPriority = tip.priority.name
        }
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Number Badge
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(
                        color = priorityColor.copy(alpha = 0.2f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = number.toString(),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = priorityColor
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Content
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = translatedTitle,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                    
                    // Priority Badge
                    Surface(
                        color = priorityColor.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = translatedPriority,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = priorityColor,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = translatedDescription,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 20.sp
                )
            }
        }
    }
}
