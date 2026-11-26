package de.topfsecret.app

import android.os.Bundle
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.content.Context
import androidx.core.content.FileProvider
import java.io.File
import java.util.UUID
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.topfsecret.app.data.RecipeRepository
import de.topfsecret.app.data.local.RecipeEntity
import de.topfsecret.app.data.local.RecipeWithDetails
import de.topfsecret.app.data.local.TopfSecretDatabase
import kotlinx.coroutines.launch

private data class AppPalette(val cream: Color, val paper: Color, val ink: Color, val mutedInk: Color, val copper: Color, val paleCopper: Color, val sage: Color)
private val LightPalette = AppPalette(Color(0xFFFFF8F0), Color(0xFFFFFDF9), Color(0xFF302019), Color(0xFF806E62), Color(0xFFE87524), Color(0xFFFFE6D2), Color(0xFFE8EEE2))
private val DarkPalette = AppPalette(Color(0xFF171310), Color(0xFF25201B), Color(0xFFF4E9DF), Color(0xFFB9A99C), Color(0xFFFF9A55), Color(0xFF493022), Color(0xFF303A30))
private val LocalPalette = compositionLocalOf { LightPalette }
private val Cream: Color @Composable get() = LocalPalette.current.cream
private val Paper: Color @Composable get() = LocalPalette.current.paper
private val Ink: Color @Composable get() = LocalPalette.current.ink
private val MutedInk: Color @Composable get() = LocalPalette.current.mutedInk
private val Copper: Color @Composable get() = LocalPalette.current.copper
private val PaleCopper: Color @Composable get() = LocalPalette.current.paleCopper
private val Sage: Color @Composable get() = LocalPalette.current.sage

private data class AppDestination(val label: String, val symbol: String)
private val destinations = listOf(
    AppDestination("Start", "⌂"),
    AppDestination("Rezepte", "▤"),
    AppDestination("Kategorien", "▦"),
    AppDestination("Favoriten", "♡")
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val repository = RecipeRepository(TopfSecretDatabase.getInstance(applicationContext).recipeDao())
        val preferences = getSharedPreferences("topf_secret_settings", MODE_PRIVATE)
        val appVersion = packageManager.getPackageInfo(packageName, 0).versionName ?: "Unbekannt"
        setContent {
            var darkMode by rememberSaveable { mutableStateOf(preferences.getBoolean("dark_mode", false)) }
            TopfSecretTheme(darkMode) {
                ActivationGate {
                    TopfSecretApp(repository, darkMode, appVersion) { enabled ->
                        darkMode = enabled
                        preferences.edit().putBoolean("dark_mode", enabled).apply()
                    }
                }
            }
        }
    }
}

@Composable
private fun TopfSecretTheme(darkMode: Boolean, content: @Composable () -> Unit) {
    val palette = if (darkMode) DarkPalette else LightPalette
    val scheme = if (darkMode) darkColorScheme(primary = palette.copper, secondary = Color(0xFFFFB879), background = palette.cream, surface = palette.paper, onSurface = palette.ink, onBackground = palette.ink)
    else lightColorScheme(primary = palette.copper, onPrimary = Color.White, secondary = Color(0xFF8D4B2A), background = palette.cream, surface = palette.paper, onSurface = palette.ink, onBackground = palette.ink)
    CompositionLocalProvider(LocalPalette provides palette) { MaterialTheme(colorScheme = scheme, content = content) }
}

@Composable
private fun TopfSecretApp(repository: RecipeRepository, darkMode: Boolean, appVersion: String, onDarkModeChange: (Boolean) -> Unit) {
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    var selectedRecipeId by rememberSaveable { mutableLongStateOf(0L) }
    var editingRecipeId by rememberSaveable { mutableLongStateOf(0L) }
    var selectedCategory by rememberSaveable { mutableStateOf("") }
    val recipes by repository.observeRecipes().collectAsState(initial = emptyList())
    val categories = recipes.map { it.recipe.category.trim() }.filter(String::isNotBlank).distinct().sortedBy { it.lowercase() }
    val scope = rememberCoroutineScope()

    Scaffold(
        containerColor = Cream,
        bottomBar = {
            NavigationBar(containerColor = Paper, contentColor = Ink) {
                destinations.forEachIndexed { index, destination ->
                    NavigationBarItem(
                        selected = selectedTab == index,
                        onClick = {
                            selectedTab = index
                            selectedRecipeId = 0L
                            editingRecipeId = 0L
                            if (index != 1) selectedCategory = ""
                        },
                        icon = { Text(destination.symbol, fontSize = 23.sp) },
                        label = { Text(destination.label) }
                    )
                }
            }
        }
    ) { innerPadding ->
        Surface(Modifier.fillMaxSize().padding(innerPadding), color = Cream) {
            when (selectedTab) {
                0 -> HomeScreen(recipes.size, darkMode, appVersion, onDarkModeChange, { selectedTab = 1 }, { selectedTab = 2 })
                1 -> {
                    if (editingRecipeId != 0L) {
                        val editing = recipes.firstOrNull { it.recipe.id == editingRecipeId }
                        RecipeEditorScreen(
                            recipe = editing,
                            onCancel = { editingRecipeId = 0L },
                            onSave = { entity, ingredients, steps ->
                                scope.launch {
                                    val newId = repository.saveRecipe(entity, ingredients, steps)
                                    if (editing != null && editing.recipe.imageUri != entity.imageUri) {
                                        deleteManagedRecipeImage(applicationContext, editing.recipe.imageUri)
                                    }
                                    editingRecipeId = 0L
                                    selectedRecipeId = newId
                                }
                            }
                        )
                    } else if (selectedRecipeId != 0L) {
                        val selected = recipes.firstOrNull { it.recipe.id == selectedRecipeId }
                        if (selected == null) {
                            RecipeListScreen(recipes, selectedCategory, { selectedCategory = "" }, { editingRecipeId = -1L }, { selectedRecipeId = it })
                        } else {
                            RecipeDetailScreen(
                                recipe = selected,
                                onBack = { selectedRecipeId = 0L },
                                onEdit = { editingRecipeId = selected.recipe.id },
                                onFavorite = {
                                    scope.launch { repository.setFavorite(selected.recipe, !selected.recipe.isFavorite) }
                                },
                                onDelete = {
                                    scope.launch {
                                        repository.deleteRecipe(selected.recipe.id)
                                        selectedRecipeId = 0L
                                    }
                                }
                            )
                        }
                    } else {
                        RecipeListScreen(recipes, selectedCategory, { selectedCategory = "" }, { editingRecipeId = -1L }, { selectedRecipeId = it })
                    }
                }
                2 -> CategoryScreen(categories, recipes, onCategoryClick = { category ->
                    selectedCategory = category
                    selectedTab = 1
                    selectedRecipeId = 0L
                    editingRecipeId = 0L
                })
                else -> FavoriteScreen(
                    recipes = recipes.filter { it.recipe.isFavorite },
                    onOpen = { recipeId ->
                        selectedRecipeId = recipeId
                        selectedTab = 1
                    }
                )
            }
        }
    }
}

@Composable
private fun HomeScreen(recipeCount: Int, darkMode: Boolean, appVersion: String, onDarkModeChange: (Boolean) -> Unit, onRecipesClick: () -> Unit, onCategoriesClick: () -> Unit) {
    var showInfoDialog by rememberSaveable { mutableStateOf(false) }
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 22.dp, vertical = 18.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Image(painterResource(R.drawable.topf_secret_logo), "Topf Secret", Modifier.size(76.dp))
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text("TOPF SECRET", color = Copper, fontSize = 13.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                Text("Euer Kochbuch", color = Ink, fontSize = 23.sp, fontWeight = FontWeight.Bold)
                Text("Deine Rezepte. Eure Küche.", color = MutedInk, fontSize = 13.sp)
            }
            IconButton(onClick = { showInfoDialog = true }) {
                Text("ⓘ", color = Copper, fontSize = 27.sp, fontWeight = FontWeight.Bold)
            }
            IconButton(onClick = { onDarkModeChange(!darkMode) }) {
                Text(if (darkMode) "☀️" else "🌙", fontSize = 23.sp)
            }
        }
        Spacer(Modifier.height(28.dp))
        if (showInfoDialog) {
            AlertDialog(
                onDismissRequest = { showInfoDialog = false },
                confirmButton = {
                    TextButton(onClick = { showInfoDialog = false }) { Text("Schließen", color = Copper) }
                },
                icon = {
                    Image(painterResource(R.drawable.topf_secret_logo), "Topf Secret Logo", Modifier.size(104.dp))
                },
                title = {
                    Text("Topf Secret by\nAndre Schlüter", color = Ink, textAlign = TextAlign.Center)
                },
                text = {
                    Text("Version $appVersion", color = MutedInk, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                },
                containerColor = Paper
            )
        }
        Text("Schön, dass ihr da seid! 👋", color = MutedInk, style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(7.dp))
        Text("Was kochen wir heute?", color = Ink, fontSize = 29.sp, lineHeight = 35.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(10.dp))
        Text("Eure Rezeptsammlung für gemeinsame Lieblingsmomente wächst hier Schritt für Schritt.", color = MutedInk, style = MaterialTheme.typography.bodyLarge, lineHeight = 24.sp)
        Spacer(Modifier.height(24.dp))
        Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = if (darkMode) Color(0xFF493022) else Color(0xFF302019))) {
            Column(Modifier.padding(22.dp)) {
                Text("GUTEN APPETIT!", color = Color(0xFFFFB879), fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                Spacer(Modifier.height(8.dp))
                Text("Eure Küche.\nEure Regeln.", color = Color.White, fontSize = 27.sp, lineHeight = 32.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Text("Ein Ort für die Rezepte, die euch schmecken und verbinden.", color = Color(0xFFFFE8D8), lineHeight = 21.sp)
            }
        }
        Spacer(Modifier.height(26.dp))
        Text("Auf einen Blick", color = Ink, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(13.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            HomeActionCard(Modifier.weight(1f), "📖", "Rezepte", "$recipeCount gespeichert", PaleCopper, onRecipesClick)
            HomeActionCard(Modifier.weight(1f), "🥕", "Kategorien", "Alles im Blick", Sage, onCategoriesClick)
        }
        Spacer(Modifier.height(22.dp))
        Spacer(Modifier.height(18.dp))

        Text("Hier entsteht euer digitales Kochbuch.", Modifier.fillMaxWidth(), color = MutedInk, textAlign = TextAlign.Center, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun HomeActionCard(modifier: Modifier, symbol: String, title: String, subtitle: String, background: Color, onClick: () -> Unit) {
    Card(onClick = onClick, modifier = modifier, shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = background)) {
        Column(Modifier.fillMaxWidth().padding(16.dp)) {
            Text(symbol, fontSize = 27.sp)
            Spacer(Modifier.height(12.dp))
            Text(title, color = Ink, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(3.dp))
            Text(subtitle, color = MutedInk, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun RecipeListScreen(
    recipes: List<RecipeWithDetails>,
    selectedCategory: String,
    onClearCategory: () -> Unit,
    onAdd: () -> Unit,
    onOpen: (Long) -> Unit
) {
    var searchText by rememberSaveable { mutableStateOf("") }
    val visibleRecipes = recipes.filter { item ->
        val categoryMatches = selectedCategory.isBlank() || item.recipe.category.equals(selectedCategory, ignoreCase = true)
        val query = searchText.trim()
        val searchMatches = query.isBlank() ||
            item.recipe.title.contains(query, ignoreCase = true) ||
            item.recipe.category.contains(query, ignoreCase = true) ||
            item.recipe.description.contains(query, ignoreCase = true) ||
            item.ingredients.any { it.name.contains(query, ignoreCase = true) }
        categoryMatches && searchMatches
    }
    Scaffold(
        containerColor = Cream,
        floatingActionButton = {
            FloatingActionButton(onClick = onAdd, containerColor = Copper, contentColor = Color.White) { Text("+", fontSize = 28.sp) }
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(20.dp)) {
            Text("DEINE SAMMLUNG", color = Copper, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
            Spacer(Modifier.height(6.dp))
            Text(if (selectedCategory.isBlank()) "Alle Rezepte" else selectedCategory, color = Ink, fontSize = 28.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = searchText,
                onValueChange = { searchText = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text("Rezepte suchen") },
                placeholder = { Text("Name, Kategorie oder Zutat") },
                trailingIcon = {
                    if (searchText.isNotEmpty()) IconButton(onClick = { searchText = "" }) { Text("✕") }
                }
            )
            if (selectedCategory.isNotBlank()) {
                Spacer(Modifier.height(8.dp))
                TextButton(onClick = onClearCategory) { Text("Alle Kategorien anzeigen", color = Copper) }
            }
            Spacer(Modifier.height(12.dp))
            if (visibleRecipes.isEmpty()) {
                Card(shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = Paper)) {
                    Column(Modifier.fillMaxWidth().padding(22.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(if (recipes.isEmpty()) "🍲" else "🔎", fontSize = 48.sp)
                        Spacer(Modifier.height(10.dp))
                        Text(
                            when {
                                recipes.isEmpty() -> "Der Topf ist noch leer"
                                selectedCategory.isNotBlank() -> "Keine Rezepte in dieser Kategorie"
                                else -> "Nichts gefunden"
                            },
                            color = Ink, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            if (recipes.isEmpty()) "Legt euer erstes Lieblingsrezept an. Über das Plus geht's los!"
                            else "Ändert euren Suchbegriff oder wählt eine andere Kategorie.",
                            color = MutedInk, textAlign = TextAlign.Center
                        )
                    }
                }
            } else visibleRecipes.forEach { item ->
                Card(onClick = { onOpen(item.recipe.id) }, modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp), shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = Paper)) {
                    Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        if (!item.recipe.imageUri.isNullOrBlank()) {
                            RecipePhoto(item.recipe.imageUri, Modifier.size(58.dp))
                        } else {
                            Text(if (item.recipe.isFavorite) "❤️" else "🍽️", fontSize = 27.sp)
                        }
                        Spacer(Modifier.width(14.dp))
                        Column(Modifier.weight(1f)) {
                            Text(item.recipe.title, color = Ink, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                            Text(item.recipe.category + " · " + item.recipe.servings + " Portionen", color = MutedInk, style = MaterialTheme.typography.bodySmall)
                        }
                        Text("›", color = Copper, fontSize = 28.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryScreen(
    categories: List<String>,
    recipes: List<RecipeWithDetails>,
    onCategoryClick: (String) -> Unit
) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp)) {
        Text("GUT SORTIERT", color = Copper, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
        Spacer(Modifier.height(6.dp))
        Text("Kategorien", color = Ink, fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text("Wählt eine Kategorie und findet die passenden Rezepte.", color = MutedInk, style = MaterialTheme.typography.bodyLarge)
        Spacer(Modifier.height(18.dp))
        if (categories.isEmpty()) {
            Card(shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = Paper)) {
                Column(Modifier.fillMaxWidth().padding(22.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🥕", fontSize = 48.sp)
                    Spacer(Modifier.height(10.dp))
                    Text("Noch keine Kategorien", color = Ink, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(6.dp))
                    Text("Gebt euren Rezepten beim Anlegen eine Kategorie, zum Beispiel Hauptgericht, Dessert oder Frühstück.", color = MutedInk, textAlign = TextAlign.Center)
                }
            }
        } else categories.forEach { category ->
            val count = recipes.count { it.recipe.category.equals(category, ignoreCase = true) }
            Card(onClick = { onCategoryClick(category) }, modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp), shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = Paper)) {
                Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("🥕", fontSize = 25.sp)
                    Spacer(Modifier.width(14.dp))
                    Column(Modifier.weight(1f)) {
                        Text(category, color = Ink, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        Text("$count " + if (count == 1) "Rezept" else "Rezepte", color = MutedInk, style = MaterialTheme.typography.bodySmall)
                    }
                    Text("›", color = Copper, fontSize = 28.sp)
                }
            }
        }
    }
}

@Composable
private fun FavoriteScreen(
    recipes: List<RecipeWithDetails>,
    onOpen: (Long) -> Unit
) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp)) {
        Text("DEINE LIEBLINGE", color = Copper, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
        Spacer(Modifier.height(6.dp))
        Text("Eure Favoriten", color = Ink, fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text("Hier findet ihr eure Rezepte, die ihr besonders gern mögt.", color = MutedInk, style = MaterialTheme.typography.bodyLarge)
        Spacer(Modifier.height(18.dp))
        if (recipes.isEmpty()) {
            Card(shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = Paper)) {
                Column(Modifier.fillMaxWidth().padding(22.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("♡", color = Copper, fontSize = 52.sp)
                    Spacer(Modifier.height(10.dp))
                    Text("Noch keine Favoriten", color = Ink, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(6.dp))
                    Text("Öffnet ein Rezept und tippt auf „Favorit“, um es hier zu sammeln.", color = MutedInk, textAlign = TextAlign.Center)
                }
            }
        } else recipes.forEach { item ->
            Card(
                onClick = { onOpen(item.recipe.id) },
                modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Paper)
            ) {
                Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("❤️", fontSize = 27.sp)
                    Spacer(Modifier.width(14.dp))
                    Column(Modifier.weight(1f)) {
                        Text(item.recipe.title, color = Ink, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        Text(item.recipe.category.ifBlank { "Ohne Kategorie" } + " · " + item.recipe.servings + " Portionen", color = MutedInk, style = MaterialTheme.typography.bodySmall)
                    }
                    Text("›", color = Copper, fontSize = 28.sp)
                }
            }
        }
    }
}

@Composable
private fun RecipeEditorScreen(recipe: RecipeWithDetails?, onCancel: () -> Unit, onSave: (RecipeEntity, List<String>, List<String>) -> Unit) {
    val original = recipe?.recipe
    var title by rememberSaveable(original?.id) { mutableStateOf(original?.title.orEmpty()) }
    var description by rememberSaveable(original?.id) { mutableStateOf(original?.description.orEmpty()) }
    var category by rememberSaveable(original?.id) { mutableStateOf(original?.category.orEmpty()) }
    var servings by rememberSaveable(original?.id) { mutableStateOf((original?.servings ?: 2).toString()) }
    var prepMinutes by rememberSaveable(original?.id) { mutableStateOf(original?.preparationMinutes?.toString().orEmpty()) }
    var cookMinutes by rememberSaveable(original?.id) { mutableStateOf(original?.cookingMinutes?.toString().orEmpty()) }
    var kcal by rememberSaveable(original?.id) { mutableStateOf(original?.caloriesPerServing?.toString().orEmpty()) }
    var carbs by rememberSaveable(original?.id) { mutableStateOf(original?.carbohydratesPerServing?.toString().orEmpty()) }
    var protein by rememberSaveable(original?.id) { mutableStateOf(original?.proteinPerServing?.toString().orEmpty()) }
    var fat by rememberSaveable(original?.id) { mutableStateOf(original?.fatPerServing?.toString().orEmpty()) }
    var ingredients by rememberSaveable(original?.id) { mutableStateOf(recipe?.ingredients?.sortedBy { it.position }?.joinToString("\n") { listOf(it.amount, it.name).filter(String::isNotBlank).joinToString(" ") }.orEmpty()) }
    var steps by rememberSaveable(original?.id) { mutableStateOf(recipe?.steps?.sortedBy { it.position }?.joinToString("\n") { it.instruction }.orEmpty()) }
    var error by rememberSaveable(original?.id) { mutableStateOf(false) }
    var imageUri by rememberSaveable(original?.id) { mutableStateOf(original?.imageUri.orEmpty()) }
    var pendingCameraPath by rememberSaveable(original?.id) { mutableStateOf("") }
    var imageError by rememberSaveable(original?.id) { mutableStateOf(false) }
    var showOnlineImageSearch by rememberSaveable(original?.id) { mutableStateOf(false) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let { selected ->
            scope.launch {
                val imported = importRecipeImage(context, selected)
                if (imported != null) {
                    if (imageUri != original?.imageUri) deleteManagedRecipeImage(context, imageUri)
                    imageUri = imported
                    imageError = false
                } else imageError = true
            }
        }
    }
    val cameraCapture = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success && pendingCameraPath.isNotBlank()) {
            if (imageUri != original?.imageUri) deleteManagedRecipeImage(context, imageUri)
            imageUri = Uri.fromFile(File(pendingCameraPath)).toString()
            imageError = false
        } else {
            pendingCameraPath.takeIf(String::isNotBlank)?.let { File(it).delete() }
        }
        pendingCameraPath = ""
    }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp)) {
        Text(if (original == null) "NEUES LIEBLINGSREZEPT" else "REZEPT BEARBEITEN", color = Copper, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
        Spacer(Modifier.height(6.dp))
        Text(if (original == null) "Was kommt in den Topf?" else "Rezept ändern", color = Ink, fontSize = 26.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(title, { title = it; error = false }, Modifier.fillMaxWidth(), label = { Text("Rezeptname *") }, singleLine = true)
        Spacer(Modifier.height(10.dp))
        OutlinedTextField(category, { category = it }, Modifier.fillMaxWidth(), label = { Text("Kategorie (z. B. Hauptgericht)") }, singleLine = true)
        Spacer(Modifier.height(10.dp))
        OutlinedTextField(servings, { servings = it.filter(Char::isDigit) }, Modifier.fillMaxWidth(), label = { Text("Portionen") }, singleLine = true)
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedTextField(prepMinutes, { prepMinutes = it.filter(Char::isDigit) }, Modifier.weight(1f), label = { Text("Vorbereitung (Min.)") }, singleLine = true)
            OutlinedTextField(cookMinutes, { cookMinutes = it.filter(Char::isDigit) }, Modifier.weight(1f), label = { Text("Kochzeit (Min.)") }, singleLine = true)
        }
        Spacer(Modifier.height(10.dp))
        OutlinedTextField(description, { description = it }, Modifier.fillMaxWidth(), label = { Text("Kurze Beschreibung") }, minLines = 2)
        Spacer(Modifier.height(14.dp))
        Text("Rezeptbild", color = Ink, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(6.dp))
        if (imageUri.isNotBlank()) {
            RecipePhoto(imageUri, Modifier.fillMaxWidth().height(190.dp))
            Spacer(Modifier.height(6.dp))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = { imagePicker.launch(arrayOf("image/*")) }) {
                Text("Galerie")
            }
            OutlinedButton(onClick = {
                runCatching {
                    val directory = File(context.filesDir, "recipe_images").apply { mkdirs() }
                    val photo = File(directory, "camera-${UUID.randomUUID()}.jpg")
                    val contentUri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", photo)
                    pendingCameraPath = photo.absolutePath
                    cameraCapture.launch(contentUri)
                }.onFailure { imageError = true }
            }) { Text("Kamera") }
            OutlinedButton(onClick = { showOnlineImageSearch = true }) { Text("Online suchen") }
        }
        if (imageUri.isNotBlank()) {
            TextButton(onClick = { imageUri = "" }) { Text("Rezeptbild entfernen", color = Color(0xFFB3261E)) }
        }
        if (imageError) {
            Spacer(Modifier.height(4.dp))
            Text("Das Bild konnte nicht übernommen werden. Bitte erneut versuchen.", color = Color(0xFFB3261E), style = MaterialTheme.typography.bodySmall)
        }
        Spacer(Modifier.height(10.dp))
        OutlinedTextField(ingredients, { ingredients = it }, Modifier.fillMaxWidth(), label = { Text("Zutaten – eine pro Zeile") }, placeholder = { Text("z. B. 500 g Kartoffeln\n1 Zwiebel") }, minLines = 4)
        Spacer(Modifier.height(10.dp))
        OutlinedTextField(steps, { steps = it }, Modifier.fillMaxWidth(), label = { Text("Zubereitung – ein Schritt pro Zeile") }, placeholder = { Text("Kartoffeln schälen und würfeln.\nZwiebel glasig anbraten.") }, minLines = 4)
        Spacer(Modifier.height(16.dp))
        Text("Nährwerte pro Portion (optional)", color = Ink, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(kcal, { kcal = it }, Modifier.fillMaxWidth(), label = { Text("Kalorien (kcal)") }, singleLine = true)
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(carbs, { carbs = it }, Modifier.weight(1f), label = { Text("Kohlenhydrate (g)") }, singleLine = true)
            OutlinedTextField(protein, { protein = it }, Modifier.weight(1f), label = { Text("Eiweiß (g)") }, singleLine = true)
        }
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(fat, { fat = it }, Modifier.fillMaxWidth(), label = { Text("Fett (g)") }, singleLine = true)
        if (error) {
            Spacer(Modifier.height(6.dp))
            Text("Bitte gib dem Rezept mindestens einen Namen.", color = Color(0xFFB3261E))
        }
        Spacer(Modifier.height(18.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            TextButton(onClick = { if (imageUri != original?.imageUri) deleteManagedRecipeImage(context, imageUri); onCancel() }) { Text("Abbrechen", color = MutedInk) }
            Button(onClick = {
                if (title.isBlank()) error = true
                else {
                    val entity = (original ?: RecipeEntity(title = title.trim())).copy(
                        title = title.trim(), description = description.trim(), category = category.trim(),
                        servings = servings.toIntOrNull()?.coerceAtLeast(1) ?: 2,
                        preparationMinutes = prepMinutes.toIntOrNull(),
                        cookingMinutes = cookMinutes.toIntOrNull(),
                        imageUri = imageUri.ifBlank { null },
                        caloriesPerServing = kcal.toDecimalOrNull(),
                        carbohydratesPerServing = carbs.toDecimalOrNull(),
                        proteinPerServing = protein.toDecimalOrNull(),
                        fatPerServing = fat.toDecimalOrNull(),
                        updatedAt = System.currentTimeMillis()
                    )
                    onSave(entity, ingredients.lines(), steps.lines())
                }
            }) { Text("Rezept speichern") }
        }
    }
    if (showOnlineImageSearch) {
        OnlineDishImageSearch(
            initialQuery = title.ifBlank { original?.title.orEmpty() },
            onDismiss = { showOnlineImageSearch = false },
            onImageSelected = { selectedUri ->
                if (imageUri != original?.imageUri) deleteManagedRecipeImage(context, imageUri)
                imageUri = selectedUri
                imageError = false
                showOnlineImageSearch = false
            }
        )
    }
}

@Composable
private fun RecipeDetailScreen(recipe: RecipeWithDetails, onBack: () -> Unit, onEdit: () -> Unit, onFavorite: () -> Unit, onDelete: () -> Unit) {
    var confirmDelete by rememberSaveable(recipe.recipe.id) { mutableStateOf(false) }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp)) {
        TextButton(onClick = onBack) { Text("‹ Zurück zu den Rezepten", color = Copper) }
        Spacer(Modifier.height(8.dp))
        Text(recipe.recipe.category.uppercase().ifBlank { "EUER REZEPT" }, color = Copper, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
        Spacer(Modifier.height(6.dp))
        Text(recipe.recipe.title, color = Ink, fontSize = 30.sp, lineHeight = 36.sp, fontWeight = FontWeight.Bold)
        if (!recipe.recipe.imageUri.isNullOrBlank()) {
            Spacer(Modifier.height(14.dp))
            RecipePhoto(recipe.recipe.imageUri, Modifier.fillMaxWidth().height(230.dp))
        }
        Spacer(Modifier.height(8.dp))
        Text(recipe.recipe.servings.toString() + " Portionen", color = MutedInk)
        val timeLabels = listOfNotNull(
            recipe.recipe.preparationMinutes?.let { "Vorbereitung: " + it + " Min." },
            recipe.recipe.cookingMinutes?.let { "Kochzeit: " + it + " Min." }
        )
        if (timeLabels.isNotEmpty()) {
            Spacer(Modifier.height(8.dp))
            Text(timeLabels.joinToString("  ·  "), color = MutedInk)
        }
        val nutritionLabels = listOfNotNull(
            recipe.recipe.caloriesPerServing?.let { "⚡ " + it.prettyNumber() + " kcal" },
            recipe.recipe.carbohydratesPerServing?.let { "Kohlenhydrate " + it.prettyNumber() + " g" },
            recipe.recipe.proteinPerServing?.let { "Eiweiß " + it.prettyNumber() + " g" },
            recipe.recipe.fatPerServing?.let { "Fett " + it.prettyNumber() + " g" }
        )
        if (nutritionLabels.isNotEmpty()) {
            Spacer(Modifier.height(12.dp))
            Card(colors = CardDefaults.cardColors(containerColor = PaleCopper), shape = RoundedCornerShape(16.dp)) {
                Column(Modifier.fillMaxWidth().padding(14.dp)) {
                    Text("Nährwerte pro Portion", color = Ink, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(5.dp))
                    nutritionLabels.forEach { Text(it, color = Ink, style = MaterialTheme.typography.bodyMedium) }
                    Text("Angaben sind Richtwerte.", color = MutedInk, style = MaterialTheme.typography.labelSmall)
                }
            }
        }
        if (recipe.recipe.description.isNotBlank()) {
            Spacer(Modifier.height(14.dp))
            Text(recipe.recipe.description, color = Ink, style = MaterialTheme.typography.bodyLarge)
        }
        Spacer(Modifier.height(22.dp))
        Text("Zutaten", color = Ink, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        if (recipe.ingredients.isEmpty()) Text("Noch keine Zutaten eingetragen.", color = MutedInk)
        else recipe.ingredients.sortedBy { it.position }.forEach {
            val line = listOf(it.amount, it.name).filter(String::isNotBlank).joinToString(" ")
            Text("• " + line, color = Ink, modifier = Modifier.padding(vertical = 3.dp))
        }
        Spacer(Modifier.height(22.dp))
        Text("Zubereitung", color = Ink, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        if (recipe.steps.isEmpty()) Text("Noch keine Zubereitungsschritte eingetragen.", color = MutedInk)
        else recipe.steps.sortedBy { it.position }.forEachIndexed { index, step -> Text((index + 1).toString() + ". " + step.instruction, color = Ink, modifier = Modifier.padding(vertical = 5.dp)) }
        Spacer(Modifier.height(24.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = onEdit) { Text("Bearbeiten") }
            TextButton(onClick = onFavorite) { Text(if (recipe.recipe.isFavorite) "♥ Favorit" else "♡ Favorit", color = Copper) }
            TextButton(onClick = { confirmDelete = true }) { Text("Löschen", color = Color(0xFFB3261E)) }
        }
    }
    if (confirmDelete) AlertDialog(
        onDismissRequest = { confirmDelete = false },
        title = { Text("Rezept löschen?") },
        text = { Text("„" + recipe.recipe.title + "“ wird dauerhaft von diesem Gerät entfernt.") },
        confirmButton = { TextButton(onClick = { confirmDelete = false; onDelete() }) { Text("Löschen", color = Color(0xFFB3261E)) } },
        dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text("Abbrechen") } }
    )
}

@Composable
private fun PlaceholderScreen(eyebrow: String, title: String, description: String, symbol: String, detail: String) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Text(eyebrow, color = Copper, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
        Spacer(Modifier.height(18.dp))
        Text(symbol, fontSize = 58.sp)
        Spacer(Modifier.height(14.dp))
        Text(title, color = Ink, fontSize = 28.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        Spacer(Modifier.height(10.dp))
        Text(description, color = MutedInk, style = MaterialTheme.typography.bodyLarge, lineHeight = 25.sp, textAlign = TextAlign.Center)
        Spacer(Modifier.height(22.dp))
        Card(shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = PaleCopper)) {
            Text(detail, Modifier.padding(horizontal = 18.dp, vertical = 14.dp), color = Ink, style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center)
        }
    }
}


private fun String.toDecimalOrNull(): Double? =
    trim().replace(',', '.').takeIf(String::isNotBlank)?.toDoubleOrNull()

private fun Double.prettyNumber(): String =
    if (this % 1.0 == 0.0) toInt().toString() else toString().replace('.', ',')


@Composable
private fun RecipePhoto(uri: String?, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val bitmap by produceState<Bitmap?>(initialValue = null, uri) {
        value = withContext(Dispatchers.IO) {
            uri?.let { value ->
                runCatching {
                    context.contentResolver.openInputStream(Uri.parse(value))?.use { stream ->
                        BitmapFactory.decodeStream(stream)
                    }
                }.getOrNull()
            }
        }
    }
    if (bitmap != null) {
        Image(
            bitmap = bitmap!!.asImageBitmap(),
            contentDescription = "Rezeptbild",
            modifier = modifier,
            contentScale = ContentScale.Crop
        )
    }
}


private suspend fun importRecipeImage(context: Context, source: Uri): String? =
    withContext(Dispatchers.IO) {
        runCatching {
            val directory = File(context.filesDir, "recipe_images").apply { mkdirs() }
            val destination = File(directory, "gallery-${UUID.randomUUID()}.img")
            context.contentResolver.openInputStream(source)?.use { input ->
                destination.outputStream().use { output -> input.copyTo(output) }
            } ?: return@withContext null
            Uri.fromFile(destination).toString()
        }.getOrNull()
    }

private fun deleteManagedRecipeImage(context: Context, storedUri: String?) {
    if (storedUri.isNullOrBlank()) return
    runCatching {
        val file = File(Uri.parse(storedUri).path ?: return)
        val imageDirectory = File(context.filesDir, "recipe_images").canonicalFile
        val candidate = file.canonicalFile
        if (candidate.parentFile == imageDirectory) {
            candidate.delete()
            File(imageDirectory, candidate.name + ".source.txt").delete()
        }
    }
}
