package com.example.emergencycommunicationsystem.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
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
    var translatedPlaceholder by remember { mutableStateOf("Search guides...") }
    var translatedAll by remember { mutableStateOf("All") }
    var translatedNoGuides by remember { mutableStateOf("No guides found") }
    
    // Translate UI strings
    LaunchedEffect(currentLanguage) {
        if (currentLanguage != "en") {
            coroutineScope.launch {
                translatedTitle = TranslationService.translate("Emergency Guides", currentLanguage)
                translatedPlaceholder = TranslationService.translate("Search guides...", currentLanguage)
                translatedAll = TranslationService.translate("All", currentLanguage)
                translatedNoGuides = TranslationService.translate("No guides found", currentLanguage)
            }
        } else {
            translatedTitle = "Emergency Guides"
            translatedPlaceholder = "Search guides..."
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
    
    // The main container background should be dark (background from theme)
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background) // Dark background
            .windowInsetsPadding(WindowInsets.statusBars)
    ) {
        // Top Bar Area
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBackPressed,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = AppIcons.ArrowBack,
                    contentDescription = "Back",
                    modifier = Modifier.size(24.dp),
                    tint = Color.White // Back arrow should be white to be visible
                )
            }
            Text(
                text = translatedTitle,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White, // Text "Emergency Guides" set to White
                modifier = Modifier.padding(start = 12.dp)
            )
        }

        // Search Bar (White background for input area)
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            placeholder = { 
                Text(
                    text = translatedPlaceholder,
                    fontSize = 14.sp
                ) 
            },
            leadingIcon = {
                Icon(
                    imageVector = AppIcons.Info,
                    contentDescription = "Search",
                    modifier = Modifier.size(20.dp)
                )
            },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                unfocusedPlaceholderColor = Color.Gray,
                focusedPlaceholderColor = Color.Gray,
                unfocusedTextColor = Color.Black,
                focusedTextColor = Color.Black
            ),
            textStyle = MaterialTheme.typography.bodyLarge.copy(fontSize = 15.sp)
        )
        
        // Category Filter Chips
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                CategoryChip(
                    label = translatedAll,
                    isSelected = selectedCategory == null,
                    onClick = { selectedCategory = null }
                )
            }
            
            items(EmergencyCategory.values().filter { category ->
                EmergencyGuidesData.getGuidesByCategory(category).isNotEmpty()
            }) { category ->
                TranslatedCategoryChip(
                    category = category,
                    isSelected = selectedCategory == category,
                    onClick = { selectedCategory = category },
                    currentLanguage = currentLanguage
                )
            }
        }
        
        Spacer(modifier = Modifier.height(4.dp))

        // Guides List
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                start = 16.dp, 
                top = 4.dp, 
                end = 16.dp, 
                bottom = 16.dp
            ),
            verticalArrangement = Arrangement.spacedBy(10.dp)
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
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 14.sp
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
                    MaterialTheme.colorScheme.secondary // Use secondary for active chips
                } else {
                    Color.White.copy(alpha = 0.15f) // Subtle white for inactive on dark bg
                },
                shape = RoundedCornerShape(20.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = Color.White // Text on dark background should be white
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
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = Color.White // Guide cards should be white as per theme
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon/Emoji Box
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        color = Color(0xFFE0F2F1), // Light teal background for icon
                        shape = RoundedCornerShape(10.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = guide.icon,
                    fontSize = 24.sp
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Content
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = translatedTitle,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black, // Dark text on white card
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = translatedDescription,
                    fontSize = 13.sp,
                    color = Color.DarkGray, // Dark gray text on white card
                    maxLines = 2
                )
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            
            // Chevron
            Icon(
                imageVector = AppIcons.ChevronRight,
                contentDescription = null,
                tint = Color.Gray,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
