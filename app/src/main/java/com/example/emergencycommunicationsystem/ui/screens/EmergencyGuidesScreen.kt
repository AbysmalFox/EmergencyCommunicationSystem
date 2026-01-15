package com.example.emergencycommunicationsystem.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.emergencycommunicationsystem.data.EmergencyGuidesData
import com.example.emergencycommunicationsystem.data.UserPrefs
import com.example.emergencycommunicationsystem.data.models.EmergencyCategory
import com.example.emergencycommunicationsystem.data.models.EmergencyGuide
import com.example.emergencycommunicationsystem.ui.icons.AppIcons
import com.example.emergencycommunicationsystem.util.TranslationService
import com.example.emergencycommunicationsystem.util.getLocaleContext

/**
 * Format category name for display (English version - will be translated)
 */
private fun formatCategoryNameEn(category: EmergencyCategory): String {
    return when (category) {
        EmergencyCategory.MEDICAL -> "Medical"
        EmergencyCategory.NATURAL_DISASTER -> "Natural Disaster"
        EmergencyCategory.CRIME -> "Crime"
        EmergencyCategory.ACCIDENT -> "Accident"
        EmergencyCategory.FIRE -> "Fire"
        EmergencyCategory.WEATHER -> "Weather"
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmergencyGuidesScreen(
    onBackPressed: () -> Unit,
    onGuideClick: (String) -> Unit
) {
    val localeContext = getLocaleContext()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    // Get current language preference
    val currentLanguage by UserPrefs.getLanguage(context).collectAsState(initial = "en")
    
    // Translated UI strings
    var translatedTitle by remember { mutableStateOf("Emergency Guides") }
    var translatedPlaceholder by remember { mutableStateOf("Search emergency guides...") }
    var translatedAll by remember { mutableStateOf("All") }
    var translatedNoGuides by remember { mutableStateOf("No guides found") }
    
    // Translate UI strings
    LaunchedEffect(currentLanguage) {
        if (currentLanguage != "en") {
            coroutineScope.launch {
                translatedTitle = TranslationService.translate("Emergency Guides", currentLanguage)
                translatedPlaceholder = TranslationService.translate("Search emergency guides...", currentLanguage)
                translatedAll = TranslationService.translate("All", currentLanguage)
                translatedNoGuides = TranslationService.translate("No guides found", currentLanguage)
            }
        } else {
            translatedTitle = "Emergency Guides"
            translatedPlaceholder = "Search emergency guides..."
            translatedAll = "All"
            translatedNoGuides = "No guides found"
        }
    }
    
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<EmergencyCategory?>(null) }
    
    val guides = remember(searchQuery, selectedCategory) {
        val filtered = if (selectedCategory != null) {
            EmergencyGuidesData.getGuidesByCategory(selectedCategory!!)
        } else {
            EmergencyGuidesData.allGuides
        }
        
        if (searchQuery.isBlank()) {
            filtered
        } else {
            EmergencyGuidesData.searchGuides(searchQuery).filter { it in filtered }
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                placeholder = { Text(translatedPlaceholder) },
                leadingIcon = {
                    Icon(
                        imageVector = AppIcons.Info,
                        contentDescription = "Search"
                    )
                },
                singleLine = true
            )
            
            // Category Filter Chips
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .padding(horizontal = 16.dp)
            ) {
                item {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // All category chip
                        CategoryChip(
                            label = translatedAll,
                            isSelected = selectedCategory == null,
                            onClick = { selectedCategory = null }
                        )
                        
                        // Category chips - only show categories that have guides
                        EmergencyCategory.values()
                            .filter { category ->
                                EmergencyGuidesData.getGuidesByCategory(category).isNotEmpty()
                            }
                            .forEach { category ->
                                TranslatedCategoryChip(
                                    category = category,
                                    isSelected = selectedCategory == category,
                                    onClick = { selectedCategory = category },
                                    currentLanguage = currentLanguage
                                )
                            }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Guides List
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (guides.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = translatedNoGuides,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    items(guides) { guide ->
                        EmergencyGuideItem(
                            guide = guide,
                            onClick = { onGuideClick(guide.id) },
                            currentLanguage = currentLanguage
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .background(
                color = if (isSelected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                },
                shape = RoundedCornerShape(20.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) {
                MaterialTheme.colorScheme.onPrimary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            }
        )
    }
}

@Composable
fun TranslatedCategoryChip(
    category: EmergencyCategory,
    isSelected: Boolean,
    onClick: () -> Unit,
    currentLanguage: String
) {
    val categoryNameEn = formatCategoryNameEn(category)
    var translatedLabel by remember { mutableStateOf(categoryNameEn) }
    val coroutineScope = rememberCoroutineScope()
    
    LaunchedEffect(categoryNameEn, currentLanguage) {
        if (currentLanguage != "en") {
            coroutineScope.launch {
                translatedLabel = TranslationService.translate(categoryNameEn, currentLanguage)
            }
        } else {
            translatedLabel = categoryNameEn
        }
    }
    
    CategoryChip(
        label = translatedLabel,
        isSelected = isSelected,
        onClick = onClick
    )
}

@Composable
fun EmergencyGuideItem(
    guide: EmergencyGuide,
    onClick: () -> Unit,
    currentLanguage: String
) {
    val coroutineScope = rememberCoroutineScope()
    
    // Translated guide content
    var translatedTitle by remember { mutableStateOf(guide.title) }
    var translatedDescription by remember { mutableStateOf(guide.description) }
    var translatedTipsText by remember { mutableStateOf("${guide.tips.size} tips available") }
    
    // Translate guide content
    LaunchedEffect(guide.id, currentLanguage) {
        if (currentLanguage != "en") {
            coroutineScope.launch {
                translatedTitle = TranslationService.translate(guide.title, currentLanguage)
                translatedDescription = TranslationService.translate(guide.description, currentLanguage)
                val tipsText = "${guide.tips.size} tips available"
                translatedTipsText = TranslationService.translate(tipsText, currentLanguage)
            }
        } else {
            translatedTitle = guide.title
            translatedDescription = guide.description
            translatedTipsText = "${guide.tips.size} tips available"
        }
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon/Emoji
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = guide.icon,
                    fontSize = 28.sp
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Content
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = translatedTitle,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = translatedDescription,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = translatedTipsText,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            
            // Chevron
            Icon(
                imageVector = AppIcons.ChevronRight,
                contentDescription = "View Details",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
