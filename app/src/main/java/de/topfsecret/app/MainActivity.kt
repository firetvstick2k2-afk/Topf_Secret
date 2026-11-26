package de.topfsecret.app

import android.os.Bundle
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions
import com.google.mlkit.vision.barcode.common.Barcode
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.content.Context
import android.webkit.WebView
import android.webkit.WebViewClient
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import androidx.compose.ui.viewinterop.AndroidView
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
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import kotlinx.coroutines.delay
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.topfsecret.app.data.RecipeRepository
import de.topfsecret.app.data.local.RecipeEntity
import de.topfsecret.app.data.local.IngredientEntity
import de.topfsecret.app.data.local.LocalFoodProductDao
import de.topfsecret.app.data.local.LocalFoodProductEntity
import de.topfsecret.app.nutrition.OpenFoodFactsClient
import de.topfsecret.app.nutrition.OpenFoodFactsProduct
import de.topfsecret.app.nutrition.IngredientMeasureConverter
import de.topfsecret.app.nutrition.NutritionPer100
import de.topfsecret.app.nutrition.sumNutrition
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
    AppDestination("Favoriten", "♡"),
    AppDestination("Entdecken", "✦")
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val appContext = applicationContext
        val database = TopfSecretDatabase.getInstance(appContext)
        val repository = RecipeRepository(database.recipeDao())
        val productCatalog = database.localFoodProductDao()
        val blsFoodDao = database.blsFoodDao()
        val preferences = getSharedPreferences("topf_secret_settings", MODE_PRIVATE)
        val appVersion = packageManager.getPackageInfo(packageName, 0).versionName ?: "Unbekannt"
        setContent {
            var darkMode by rememberSaveable { mutableStateOf(preferences.getBoolean("dark_mode", false)) }
            var showSplash by rememberSaveable { mutableStateOf(true) }
            LaunchedEffect(Unit) {
                delay(3_100)
                showSplash = false
            }
            TopfSecretTheme(darkMode) {
                if (showSplash) {
                    TopfSecretSplash(onFinished = { showSplash = false })
                } else {
                    ActivationGate {
                        TopfSecretApp(appContext, repository, productCatalog, blsFoodDao, darkMode, appVersion) { enabled ->
                            darkMode = enabled
                            preferences.edit().putBoolean("dark_mode", enabled).apply()
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TopfSecretSplash(onFinished: () -> Unit) {
    val potScale = remember { Animatable(0.72f) }
    val lidTilt = remember { Animatable(-22f) }
    val steamAlpha = remember { Animatable(0f) }
    val steamRise = remember { Animatable(18f) }
    var showTitle by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        potScale.animateTo(1f, tween(520))
        delay(180)
        lidTilt.animateTo(13f, tween(420))
        steamAlpha.animateTo(1f, tween(260))
        steamRise.animateTo(-8f, tween(520))
        delay(220)
        lidTilt.animateTo(0f, tween(380))
        showTitle = true
        delay(1_050)
        onFinished()
    }

    Column(
        modifier = Modifier.fillMaxSize().background(Cream),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier.size(width = 270.dp, height = 230.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "♨",
                color = Copper.copy(alpha = steamAlpha.value),
                fontSize = 54.sp,
                modifier = Modifier.align(Alignment.TopCenter).offset(y = steamRise.value.dp)
            )
            Box(
                modifier = Modifier.offset(y = 34.dp).graphicsLayer {
                    scaleX = potScale.value
                    scaleY = potScale.value
                },
                contentAlignment = Alignment.Center
            ) {
                Row(
                    modifier = Modifier.width(230.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Box(Modifier.size(34.dp, 44.dp).border(7.dp, Color(0xFF9B4C25), RoundedCornerShape(14.dp)))
                    Spacer(Modifier.width(162.dp))
                    Box(Modifier.size(34.dp, 44.dp).border(7.dp, Color(0xFF9B4C25), RoundedCornerShape(14.dp)))
                }
                Box(
                    modifier = Modifier.size(194.dp, 132.dp).background(Color(0xFFE87524), RoundedCornerShape(42.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(Modifier.fillMaxSize()) {
                        val eyeY = size.height * 0.39f
                        drawCircle(Color(0xFF302019), radius = 6.5.dp.toPx(), center = Offset(size.width * 0.36f, eyeY))
                        drawCircle(Color(0xFF302019), radius = 6.5.dp.toPx(), center = Offset(size.width * 0.64f, eyeY))
                        drawArc(
                            color = Color(0xFF302019),
                            startAngle = 0f,
                            sweepAngle = 180f,
                            useCenter = false,
                            topLeft = Offset(size.width * 0.37f, size.height * 0.48f),
                            size = Size(size.width * 0.26f, size.height * 0.25f),
                            style = Stroke(width = 4.dp.toPx())
                        )
                    }
                }
                Box(
                    modifier = Modifier.offset(y = (-76).dp).graphicsLayer { rotationZ = lidTilt.value }
                        .size(210.dp, 19.dp)
                        .background(Color(0xFF9B4C25), RoundedCornerShape(50))
                )
                Box(
                    modifier = Modifier.offset(y = (-91).dp).size(34.dp, 17.dp)
                        .background(Color(0xFF9B4C25), RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                )
            }
        }
        Spacer(Modifier.height(2.dp))
        Text(
            "TOPF SECRET",
            color = Copper,
            fontSize = 17.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 4.sp
        )
        Spacer(Modifier.height(8.dp))
        Text(
            if (showTitle) "Pssst … hier wird's lecker! 😉" else "Deine Rezepte. Euer Geheimnis.",
            color = MutedInk,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center
        )
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
private fun TopfSecretApp(context: Context, repository: RecipeRepository, productCatalog: LocalFoodProductDao, blsFoodDao: de.topfsecret.app.data.local.BlsFoodDao, darkMode: Boolean, appVersion: String, onDarkModeChange: (Boolean) -> Unit) {
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    var selectedRecipeId by rememberSaveable { mutableLongStateOf(0L) }
    var editingRecipeId by rememberSaveable { mutableLongStateOf(0L) }
    var selectedCategory by rememberSaveable { mutableStateOf("") }
    var importStartUrl by rememberSaveable { mutableStateOf("") }
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
                0 -> HomeScreen(recipes.size, darkMode, appVersion, onDarkModeChange, { selectedTab = 1 }, { selectedTab = 2 }, { selectedTab = 5 })
                1 -> {
                    if (editingRecipeId == -2L) {
                        OnlineRecipeImportScreen(
                            initialUrl = importStartUrl,
                            onCancel = { editingRecipeId = 0L; importStartUrl = "" },
                            onSave = { imported ->
                                scope.launch {
                                    val entity = RecipeEntity(
                                        title = imported.title,
                                        description = imported.description,
                                        category = imported.category.ifBlank { "Importiert" },
                                        servings = imported.servings,
                                        preparationMinutes = imported.preparationMinutes,
                                        cookingMinutes = imported.cookingMinutes,
                                        sourceUrl = imported.sourceUrl,
                                        imageUri = imported.imageUrl?.let { downloadRecipeImage(context, it, imported.sourceUrl) },
                                        caloriesPerServing = imported.caloriesPerServing,
                                        carbohydratesPerServing = imported.carbohydratesPerServing,
                                        sugarsPerServing = imported.sugarsPerServing,
                                        fiberPerServing = imported.fiberPerServing,
                                        proteinPerServing = imported.proteinPerServing,
                                        fatPerServing = imported.fatPerServing,
                                        saturatedFatPerServing = imported.saturatedFatPerServing,
                                        saltPerServing = imported.saltPerServing
                                    )
                                    de.topfsecret.app.nutrition.BlsDatasetSeeder.ensureSeeded(context, blsFoodDao)
                                    val importedNutrition = mutableMapOf<String, OpenFoodFactsProduct>()
                                    imported.ingredients.forEach { line ->
                                        val ingredientName = Regex("""^\d+(?:[.,]\d+)?\s*(?:kg|g|mg|l|ml|cl|dl|EL|TL|Stk\.?|Stück)?\s+(.+)$""", RegexOption.IGNORE_CASE)
                                            .matchEntire(line.trim())?.groupValues?.get(1)?.trim() ?: line.trim()
                                        val local = productCatalog.search(ingredientName).firstOrNull { it.name.equals(ingredientName, true) }
                                        if (local != null) {
                                            importedNutrition[ingredientName] = OpenFoodFactsProduct(local.barcode, local.name, local.brand,
                                                local.caloriesKcalPer100, local.carbohydratesGPer100, local.proteinGPer100, local.fatGPer100,
                                                local.sourceUrl, local.sugarsGPer100, local.fiberGPer100, local.saltGPer100,
                                                local.saturatedFatGPer100, local.nutritionBasis)
                                        } else {
                                            val bls = blsFoodDao.search(ingredientName).firstOrNull { it.germanName.equals(ingredientName, true) }
                                            if (bls != null) importedNutrition[ingredientName] = OpenFoodFactsProduct(
                                                "BLS:${bls.code}", bls.germanName, "BLS 4.0", bls.kcalPer100,
                                                bls.carbohydratesGPer100, bls.proteinGPer100, bls.fatGPer100,
                                                "https://blsdb.de/", bls.blsNumber("SUGAR"), bls.blsNumber("FIBT"),
                                                bls.blsNumber("NACL"), bls.blsNumber("FASAT")
                                            )
                                        }
                                    }
                                    val newId = repository.saveRecipe(entity, imported.ingredients, imported.steps, importedNutrition)
                                    editingRecipeId = 0L
                                    importStartUrl = ""
                                    selectedRecipeId = newId
                                }
                            }
                        )
                    } else if (editingRecipeId != 0L) {
                        val editing = recipes.firstOrNull { it.recipe.id == editingRecipeId }
                        RecipeEditorScreen(
                            recipe = editing,
                            productCatalog = productCatalog,
                            blsFoodDao = blsFoodDao,
                            onCancel = { editingRecipeId = 0L },
                            onSave = { entity, ingredients, steps, nutritionByIngredientName ->
                                scope.launch {
                                    val newId = repository.saveRecipe(entity, ingredients, steps, nutritionByIngredientName)
                                    if (editing != null && editing.recipe.imageUri != entity.imageUri) {
                                        deleteManagedRecipeImage(context, editing.recipe.imageUri)
                                    }
                                    editingRecipeId = 0L
                                    selectedRecipeId = newId
                                }
                            }
                        )
                    } else if (selectedRecipeId != 0L) {
                        val selected = recipes.firstOrNull { it.recipe.id == selectedRecipeId }
                        if (selected == null) {
                            RecipeListScreen(recipes, selectedCategory, { selectedCategory = "" }, { editingRecipeId = -1L }, { selectedRecipeId = it }, { editingRecipeId = -2L })
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
                        RecipeListScreen(recipes, selectedCategory, { selectedCategory = "" }, { editingRecipeId = -1L }, { selectedRecipeId = it }, { editingRecipeId = -2L })
                    }
                }
                2 -> CategoryScreen(categories, recipes, onCategoryClick = { category ->
                    selectedCategory = category
                    selectedTab = 1
                    selectedRecipeId = 0L
                    editingRecipeId = 0L
                })
                3 -> FavoriteScreen(
                    recipes = recipes.filter { it.recipe.isFavorite },
                    onOpen = { recipeId ->
                        selectedRecipeId = recipeId
                        selectedTab = 1
                    }
                )
                4 -> RecipeDiscoveryScreen(
                    onImport = { pageUrl ->
                        importStartUrl = pageUrl
                        editingRecipeId = -2L
                        selectedTab = 1
                    }
                )
                else -> AcknowledgementsScreen()
            }
        }
    }
}

@Composable
private fun HomeScreen(recipeCount: Int, darkMode: Boolean, appVersion: String, onDarkModeChange: (Boolean) -> Unit, onRecipesClick: () -> Unit, onCategoriesClick: () -> Unit, onSourcesClick: () -> Unit) {
    var showInfoDialog by rememberSaveable { mutableStateOf(false) }
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 22.dp, vertical = 18.dp),
        horizontalAlignment = Alignment.Start
    ) {
        var menuExpanded by remember { mutableStateOf(false) }
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Box {
                IconButton(onClick = { menuExpanded = true }) { Text("☰", color = Copper, fontSize = 25.sp) }
                DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                    DropdownMenuItem(text = { Text("Start") }, onClick = { menuExpanded = false })
                    DropdownMenuItem(text = { Text("Rezepte") }, onClick = { menuExpanded = false; onRecipesClick() })
                    DropdownMenuItem(text = { Text("Kategorien") }, onClick = { menuExpanded = false; onCategoriesClick() })
                    DropdownMenuItem(text = { Text("Quellen & Danksagungen") }, onClick = { menuExpanded = false; onSourcesClick() })
                    DropdownMenuItem(text = { Text("Über Topf Secret") }, onClick = { menuExpanded = false; showInfoDialog = true })
                }
            }
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
private fun RecipeDiscoveryScreen(onImport: (String) -> Unit) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var query by rememberSaveable { mutableStateOf("") }
    var selectedSource by rememberSaveable { mutableStateOf("TheMealDB") }
    var selectedCategory by rememberSaveable { mutableStateOf("Für dich") }
    var meals by remember { mutableStateOf<List<DiscoveryRecipe>>(emptyList()) }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf("") }
    val categories = listOf("Für dich", "Chicken", "Beef", "Seafood", "Vegetarian", "Pasta", "Dessert", "Starter")
    fun load(category: String, search: String = "") {
        selectedSource = "TheMealDB"
        selectedCategory = category
        loading = true
        error = ""
        scope.launch {
            runCatching {
                when {
                    search.isNotBlank() -> RecipeDiscoveryClient.search(search)
                    category == "Für dich" -> RecipeDiscoveryClient.popular()
                    else -> RecipeDiscoveryClient.byCategory(category)
                }
            }.onSuccess { meals = it }
                .onFailure { error = it.message ?: "Rezepte konnten nicht geladen werden." }
            loading = false
        }
    }
    fun openChefkochSearch() {
        val target = if (query.isBlank()) "https://www.chefkoch.de/" else
            "https://www.chefkoch.de/rs/suche.php?q=${Uri.encode(query.trim())}"
        runCatching {
            context.startActivity(android.content.Intent(android.content.Intent.ACTION_VIEW, Uri.parse(target)))
        }.onFailure { error = "Chefkoch konnte nicht geöffnet werden." }
    }
    LaunchedEffect(Unit) { load("Für dich") }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(bottom = 20.dp)) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp)) {
            Text("TOPF SECRET ORIGINALS", color = Copper, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
            Text("Entdecke dein nächstes Lieblingsgericht", color = Ink, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text("Rezepte entdecken, auswählen und in dein Kochbuch übernehmen.", color = MutedInk, style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(selected = selectedSource == "TheMealDB", onClick = { if (selectedSource != "TheMealDB") load("Für dich") }, label = { Text("TheMealDB") })
                FilterChip(selected = selectedSource == "Chefkoch", onClick = { selectedSource = "Chefkoch"; error = "" }, label = { Text("Chefkoch") })
            }
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text(if (selectedSource == "Chefkoch") "Auf Chefkoch suchen" else "Rezept suchen") },
                trailingIcon = {
                    TextButton(
                        onClick = { if (selectedSource == "Chefkoch") openChefkochSearch() else load(selectedCategory, query.trim()) },
                        enabled = selectedSource == "Chefkoch" || query.isNotBlank()
                    ) { Text("Suchen") }
                }
            )
        }
        if (selectedSource == "Chefkoch") {
            Card(
                Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Paper)
            ) {
                Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("CHEFKOCH", color = Copper, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp)
                    Text("Rezepte auf Chefkoch finden", color = Ink, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text("Die Suche öffnet die offizielle Chefkoch-Seite. Wenn du ein Rezept gefunden hast, kopiere dessen Rezeptlink und importiere ihn in Topf Secret über „Online-Rezept über Link importieren“. Die Rezeptvorschau und Speicherung laufen dann wie gewohnt in Topf Secret.", color = MutedInk)
                    Button(onClick = { openChefkochSearch() }, modifier = Modifier.fillMaxWidth()) {
                        Text(if (query.isBlank()) "Chefkoch öffnen" else "Chefkoch-Suche öffnen")
                    }
                    Text("Chefkoch-Rezeptinhalte werden nicht automatisch ausgelesen. So bleiben wir bei der offiziellen Suche und dem vorhandenen Linkimport.", color = MutedInk, style = MaterialTheme.typography.bodySmall)
                }
            }
        } else {
            Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                categories.forEach { category ->
                    FilterChip(selected = selectedCategory == category && query.isBlank(), onClick = { query = ""; load(category) }, label = { Text(category) })
                }
            }
            Spacer(Modifier.height(14.dp))
            if (selectedCategory == "Für dich" && query.isBlank() && meals.isNotEmpty()) {
                val hero = meals.first()
                Box(Modifier.fillMaxWidth().padding(horizontal = 14.dp).height(220.dp)) {
                    RecipePhoto(hero.imageUrl, Modifier.fillMaxSize())
                    Column(Modifier.align(Alignment.BottomStart).fillMaxWidth().padding(14.dp)) {
                        Text("JETZT ENTDECKEN", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text(hero.title, color = Color.White, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                        Button(onClick = { onImport("themealdb://${hero.id}") }) { Text("Rezept ansehen & übernehmen") }
                    }
                }
                Spacer(Modifier.height(18.dp))
            }
            Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(if (query.isNotBlank()) "Suchergebnisse" else selectedCategory, color = Ink, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(Modifier.weight(1f))
                if (loading) CircularProgressIndicator(Modifier.size(22.dp), strokeWidth = 2.dp)
            }
            if (error.isNotBlank()) Text(error, color = Color(0xFFB3261E), modifier = Modifier.padding(16.dp))
            if (!loading && meals.isEmpty() && error.isBlank()) Text("Keine Rezepte gefunden. Probiere einen anderen Suchbegriff.", color = MutedInk, modifier = Modifier.padding(16.dp))
            if (meals.isNotEmpty()) {
                Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    meals.forEach { meal ->
                        Card(Modifier.width(178.dp), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Paper)) {
                            Column {
                                RecipePhoto(meal.imageUrl, Modifier.fillMaxWidth().height(132.dp))
                                Column(Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                                    Text(meal.title, color = Ink, fontWeight = FontWeight.Bold, maxLines = 2, minLines = 2)
                                    Text(listOf(meal.category, meal.area).filter(String::isNotBlank).joinToString(" · ").ifBlank { "Rezeptidee" }, color = MutedInk, style = MaterialTheme.typography.labelSmall, maxLines = 1)
                                    TextButton(onClick = { onImport("themealdb://${meal.id}") }, modifier = Modifier.fillMaxWidth()) { Text("Ansehen & übernehmen") }
                                }
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(18.dp))
            Text("Weitere Ideen", color = Ink, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 16.dp))
            Text("Die Rezeptauswahl wird aus TheMealDB geladen. Chefkoch kann über die offizielle Suche und den Linkimport genutzt werden.", color = MutedInk, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp))
        }
    }
}
@Composable
private fun RecipeBrowserScreen(onImport: (String) -> Unit) {
    var address by rememberSaveable { mutableStateOf("https://www.chefkoch.de/") }
    var currentUrl by rememberSaveable { mutableStateOf("https://www.chefkoch.de/") }
    var pageTitle by rememberSaveable { mutableStateOf("Chefkoch") }
    var canGoBack by remember { mutableStateOf(false) }
    var canGoForward by remember { mutableStateOf(false) }
    var webView by remember { mutableStateOf<WebView?>(null) }
    var pageError by remember { mutableStateOf("") }
    Column(Modifier.fillMaxSize().padding(horizontal = 12.dp, vertical = 8.dp)) {
        Text("REZEPT-BROWSER", color = Copper, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
        Text("Finde dein nächstes Lieblingsgericht", color = Ink, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            OutlinedTextField(
                value = address,
                onValueChange = { address = it },
                modifier = Modifier.weight(1f),
                singleLine = true,
                label = { Text("Webadresse oder Suchbegriff") }
            )
            Button(onClick = {
                val entered = address.trim()
                val target = when {
                    entered.startsWith("http://", true) || entered.startsWith("https://", true) -> entered
                    entered.contains(".") && !entered.contains(" ") -> "https://$entered"
                    else -> "https://www.google.com/search?q=" + Uri.encode(entered)
                }
                webView?.loadUrl(target)
            }) { Text("Los") }
        }
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = { webView?.goBack() }, enabled = canGoBack) { Text("‹ Zurück") }
            TextButton(onClick = { webView?.goForward() }, enabled = canGoForward) { Text("Vorwärts ›") }
            TextButton(onClick = { webView?.reload() }) { Text("↻") }
            Spacer(Modifier.weight(1f))
            TextButton(onClick = { webView?.loadUrl("https://www.chefkoch.de/"); address = "https://www.chefkoch.de/" }) { Text("Chefkoch") }
        }
        if (pageError.isNotBlank()) Text(pageError, color = Color(0xFFB3261E), style = MaterialTheme.typography.bodySmall)
        AndroidView(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            factory = { context ->
                WebView(context).apply {
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    settings.allowFileAccess = false
                    settings.allowContentAccess = false
                    settings.javaScriptCanOpenWindowsAutomatically = false
                    settings.setSupportMultipleWindows(false)
                    webViewClient = object : WebViewClient() {
                        override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                            val scheme = request.url.scheme?.lowercase()
                            return scheme != "https" && scheme != "http"
                        }
                        override fun onPageStarted(view: WebView, url: String?, favicon: Bitmap?) {
                            super.onPageStarted(view, url, favicon)
                            if (!url.isNullOrBlank()) { currentUrl = url; address = url }
                            pageError = ""
                        }
                        override fun onPageFinished(view: WebView, url: String?) {
                            super.onPageFinished(view, url)
                            if (!url.isNullOrBlank()) { currentUrl = url; address = url }
                            pageTitle = view.title ?: "Rezeptseite"
                            canGoBack = view.canGoBack()
                            canGoForward = view.canGoForward()
                        }
                        override fun onReceivedError(view: WebView, request: WebResourceRequest, error: android.webkit.WebResourceError) {
                            if (request.isForMainFrame) pageError = "Diese Seite konnte nicht geladen werden."
                        }
                    }
                    webChromeClient = WebChromeClient()
                    webView = this
                    loadUrl(currentUrl)
                }
            },
            update = { view -> webView = view }
        )
        Spacer(Modifier.height(8.dp))
        Button(
            onClick = { onImport(currentUrl) },
            enabled = currentUrl.startsWith("https://", true) || currentUrl.startsWith("http://", true),
            modifier = Modifier.fillMaxWidth()
        ) { Text("＋ Dieses Rezept übernehmen") }
        Text("Gehe auf eine Rezeptseite und öffne das gewünschte Gericht. Mit dem Knopf startest du den bekannten Import mit Kategorie-, Nährwert- und Bildauswahl.", color = MutedInk, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun OnlineRecipeImportScreen(initialUrl: String = "", onCancel: () -> Unit, onSave: (ImportedRecipe) -> Unit) {
    var url by rememberSaveable { mutableStateOf(initialUrl) }
    var imported by remember { mutableStateOf<ImportedRecipe?>(null) }
    var selectedCategory by rememberSaveable { mutableStateOf("") }
    var selectedImageUrl by rememberSaveable { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf("") }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    LaunchedEffect(initialUrl) {
        if (initialUrl.startsWith("themealdb://") && imported == null && !loading) {
            loading = true
            error = ""
            runCatching { RecipeDiscoveryClient.details(initialUrl.removePrefix("themealdb://")) }
                .onSuccess { imported = it; selectedCategory = it.category.ifBlank { "Importiert" }; selectedImageUrl = it.imageUrl }
                .onFailure { error = it.message ?: "Das Rezept konnte nicht geladen werden." }
            loading = false
        }
    }
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        TextButton(onClick = onCancel) { Text("‹ Zurück zu den Rezepten", color = Copper) }
        Text("ONLINE-REZEPT", color = Copper, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
        Text(if (initialUrl.startsWith("themealdb://")) "Rezept ansehen & übernehmen" else "Rezept per Link übernehmen", color = Ink, fontSize = 27.sp, fontWeight = FontWeight.Bold)
        Text(if (initialUrl.startsWith("themealdb://")) "Das vollständige Rezept wird direkt aus TheMealDB geladen. Prüfe Zutaten und Zubereitung und wähle vor dem Speichern deine Kategorie." else "Füge den Link einer Rezeptseite ein. Topf Secret sucht nach strukturierten Rezeptangaben (Schema.org) und zeigt dir die gefundenen Daten vor dem Speichern.", color = MutedInk)
        OutlinedTextField(
            value = url,
            onValueChange = { url = it; imported = null; error = "" },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Rezept-Link") },
            placeholder = { Text("https://…") },
            singleLine = true
        )
        Button(
            onClick = {
                loading = true
                error = ""
                imported = null
                scope.launch {
                    runCatching { if (url.startsWith("themealdb://")) RecipeDiscoveryClient.details(url.removePrefix("themealdb://")) else OnlineRecipeImporter.importFromUrl(url) }
                        .onSuccess { imported = it; selectedCategory = it.category.ifBlank { "Importiert" }; selectedImageUrl = it.imageUrl }
                        .onFailure { error = it.message ?: "Der Rezeptlink konnte nicht gelesen werden." }
                    loading = false
                }
            },
            enabled = url.isNotBlank() && !loading,
            modifier = Modifier.fillMaxWidth()
        ) { Text(if (loading) "Rezept wird gesucht…" else "Rezeptdaten laden") }
        if (error.isNotBlank()) {
            Text(error, color = Color(0xFFB3261E))
            Text("Tipp: Manche Webseiten stellen keine maschinenlesbaren Rezeptdaten bereit. Dann kannst du das Rezept weiterhin manuell anlegen.", color = MutedInk, style = MaterialTheme.typography.bodySmall)
        }
        imported?.let { recipe ->
            Card(colors = CardDefaults.cardColors(containerColor = Paper), shape = RoundedCornerShape(18.dp)) {
                Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("VORSCHAU", color = Copper, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                    Text(recipe.title, color = Ink, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
                    Text("BILDAUSWAHL", color = Copper, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp)
                    if (recipe.imageUrls.isNotEmpty()) {
                        Text("Ich habe ${recipe.imageUrls.size} mögliche Bilder auf der Seite gefunden. Wähle das passende Rezeptbild aus:", color = MutedInk, style = MaterialTheme.typography.bodySmall)
                        recipe.imageUrls.forEachIndexed { index, imageUrl ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = if (selectedImageUrl == imageUrl) PaleCopper else Paper),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Column(Modifier.fillMaxWidth().padding(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    RecipePhoto(imageUrl, Modifier.fillMaxWidth().height(145.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("Bild ${index + 1}", color = Ink, style = MaterialTheme.typography.bodySmall)
                                        TextButton(onClick = { selectedImageUrl = imageUrl }) {
                                            Text(if (selectedImageUrl == imageUrl) "✓ Ausgewählt" else "Dieses Bild wählen", color = Copper)
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        Text("Auf der Rezeptseite wurden keine geeigneten Bilder gefunden. Du kannst das Rezept trotzdem ohne Bild übernehmen.", color = MutedInk, style = MaterialTheme.typography.bodySmall)
                    }
                    if (recipe.description.isNotBlank()) Text(recipe.description, color = MutedInk)
                    OutlinedTextField(
                        value = selectedCategory,
                        onValueChange = { selectedCategory = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Kategorie für dieses Rezept") },
                        placeholder = { Text("z. B. Suppen, Hauptgerichte, Desserts") },
                        singleLine = true
                    )
                    Text("${recipe.servings} Portionen · Vorbereitung: ${recipe.preparationMinutes?.let { "$it Min." } ?: "–"} · Kochzeit: ${recipe.cookingMinutes?.let { "$it Min." } ?: "–"}", color = Ink)
                    val importedNutritionPreview = listOfNotNull(
                        recipe.caloriesPerServing?.let { "Energie: ${it.prettyNumber()} kcal" },
                        recipe.carbohydratesPerServing?.let { "Kohlenhydrate: ${it.prettyNumber()} g" },
                        recipe.sugarsPerServing?.let { "Zucker: ${it.prettyNumber()} g" },
                        recipe.fiberPerServing?.let { "Ballaststoffe: ${it.prettyNumber()} g" },
                        recipe.proteinPerServing?.let { "Eiweiß: ${it.prettyNumber()} g" },
                        recipe.fatPerServing?.let { "Fett: ${it.prettyNumber()} g" },
                        recipe.saturatedFatPerServing?.let { "Gesättigte Fettsäuren: ${it.prettyNumber()} g" },
                        recipe.saltPerServing?.let { "Salz: ${it.prettyNumber()} g" }
                    )
                    if (importedNutritionPreview.isNotEmpty()) {
                        Text("Nährwerte laut Rezeptquelle (pro Portion)", color = Ink, fontWeight = FontWeight.Bold)
                        importedNutritionPreview.forEach { Text(it, color = Ink, style = MaterialTheme.typography.bodySmall) }
                    } else {
                        Text("Die Quelle stellt keine strukturierten Nährwertangaben bereit. Die Zutatenberechnung in Topf Secret kann später Werte ergänzen.", color = MutedInk, style = MaterialTheme.typography.bodySmall)
                    }
                    Text("Zutaten (${recipe.ingredients.size})", color = Ink, fontWeight = FontWeight.Bold)
                    recipe.ingredients.forEach { Text("• $it", color = Ink, style = MaterialTheme.typography.bodyMedium) }
                    Text("Zubereitung (${recipe.steps.size} Schritte)", color = Ink, fontWeight = FontWeight.Bold)
                    recipe.steps.forEachIndexed { index, step -> Text("${index + 1}. $step", color = Ink, style = MaterialTheme.typography.bodyMedium) }
                    Text("Quelle: ${recipe.sourceUrl}", color = MutedInk, style = MaterialTheme.typography.bodySmall)
                    Text("Das ausgewählte Bild wird beim Speichern lokal abgelegt. Die Rezeptseite bleibt als Quelle hinterlegt.", color = MutedInk, style = MaterialTheme.typography.bodySmall)
                    Button(onClick = { onSave(recipe.copy(category = selectedCategory.trim().ifBlank { "Importiert" }, imageUrl = selectedImageUrl)) }, modifier = Modifier.fillMaxWidth()) { Text("In meine Rezepte übernehmen") }
                }
            }
        }
        Text("Hinweis: Der Import funktioniert bei Webseiten, die Rezeptdaten strukturiert veröffentlichen. Nicht jede Rezeptseite unterstützt das. Es werden nur Rezept-Metadaten übernommen, keine komplette Webseite.", color = MutedInk, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun RecipeListScreen(
    recipes: List<RecipeWithDetails>,
    selectedCategory: String,
    onClearCategory: () -> Unit,
    onAdd: () -> Unit,
    onOpen: (Long) -> Unit,
    onImportOnline: () -> Unit
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
            OutlinedButton(onClick = onImportOnline, modifier = Modifier.fillMaxWidth()) {
                Text("🌐 Online-Rezept über Link importieren")
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
private fun RecipeEditorScreen(recipe: RecipeWithDetails?, productCatalog: LocalFoodProductDao, blsFoodDao: de.topfsecret.app.data.local.BlsFoodDao, onCancel: () -> Unit, onSave: (RecipeEntity, List<String>, List<String>, Map<String, OpenFoodFactsProduct>) -> Unit) {
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
    var ingredientsField by rememberSaveable(original?.id, stateSaver = TextFieldValue.Saver) { mutableStateOf(TextFieldValue(recipe?.ingredients?.sortedBy { it.position }?.joinToString("\n") { listOf(it.amount, it.name).filter(String::isNotBlank).joinToString(" ") }.orEmpty())) }
    val ingredients = ingredientsField.text
    var steps by rememberSaveable(original?.id) { mutableStateOf(recipe?.steps?.sortedBy { it.position }?.joinToString("\n") { it.instruction }.orEmpty()) }
    var error by rememberSaveable(original?.id) { mutableStateOf(false) }
    var imageUri by rememberSaveable(original?.id) { mutableStateOf(original?.imageUri.orEmpty()) }
    var pendingCameraPath by rememberSaveable(original?.id) { mutableStateOf("") }
    var imageError by rememberSaveable(original?.id) { mutableStateOf(false) }
    var showOnlineImageSearch by rememberSaveable(original?.id) { mutableStateOf(false) }
    var showNutritionDetails by rememberSaveable(original?.id) { mutableStateOf(false) }
    val nutritionByIngredientName = remember(original?.id) {
        mutableStateMapOf<String, OpenFoodFactsProduct>().apply {
            recipe?.ingredients?.forEach { item ->
                if (item.barcode != null) {
                    put(item.name, OpenFoodFactsProduct(
                        barcode = item.barcode,
                        name = item.name,
                        brand = null,
                        caloriesKcalPer100 = item.caloriesPer100,
                        carbohydratesGPer100 = item.carbohydratesPer100,
                        proteinGPer100 = item.proteinPer100,
                        fatGPer100 = item.fatPer100,
                        sourceUrl = item.nutritionSource ?: "https://world.openfoodfacts.org/",
                        sugarsGPer100 = item.sugarsPer100,
                        fiberGPer100 = item.fiberPer100,
                        saltGPer100 = item.saltPer100,
                        saturatedFatGPer100 = item.saturatedFatPer100,
                        nutritionBasis = item.nutritionBasis
                    ))
                }
            }
        }
    }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var scannedProduct by remember { mutableStateOf<OpenFoodFactsProduct?>(null) }
    var barcodeError by remember { mutableStateOf("") }
    var barcodeLoading by remember { mutableStateOf(false) }
    var showProductSearch by remember { mutableStateOf(false) }
    var showCustomIngredientDialog by remember { mutableStateOf(false) }
    var customIngredientName by remember { mutableStateOf("") }
    var customIngredientBrand by remember { mutableStateOf("") }
    var customIngredientBasis by remember { mutableStateOf("100g") }
    var customKcal by remember { mutableStateOf("") }
    var customCarbs by remember { mutableStateOf("") }
    var customSugar by remember { mutableStateOf("") }
    var customFiber by remember { mutableStateOf("") }
    var customProtein by remember { mutableStateOf("") }
    var customFat by remember { mutableStateOf("") }
    var customSatFat by remember { mutableStateOf("") }
    var customSalt by remember { mutableStateOf("") }
    var productSearchQuery by remember { mutableStateOf("") }
    var productSearchResults by remember { mutableStateOf<List<OpenFoodFactsProduct>>(emptyList()) }
    var productSearchLoading by remember { mutableStateOf(false) }
    var productSearchMessage by remember { mutableStateOf("") }
    var editingProduct by remember { mutableStateOf(false) }
    var editedBarcode by remember { mutableStateOf("") }
    var editedName by remember { mutableStateOf("") }
    var editedBrand by remember { mutableStateOf("") }
    var editedKcal by remember { mutableStateOf("") }
    var editedCarbs by remember { mutableStateOf("") }
    var editedProtein by remember { mutableStateOf("") }
    var editedFat by remember { mutableStateOf("") }
    val nutritionSnapshot = nutritionByIngredientName.toMap()
    val nutritionEvaluation = remember(ingredients, nutritionSnapshot) {
        val lines = ingredients.lines().map(String::trim).filter(String::isNotEmpty)
        val calculated = mutableListOf<NutritionPer100.Totals>()
        val issues = mutableListOf<Pair<String, String>>()
        val knownUnits = setOf("mg", "g", "gramm", "kg", "ml", "cl", "dl", "l", "liter", "litre", "tl", "teelöffel", "el", "esslöffel", "tasse", "tassen", "stück", "stk", "stk.", "prise", "pck", "pck.", "packung", "bund", "dose", "becher")
        lines.forEach { line ->
            val match = Regex("""^(\d+(?:[.,]\d+)?)(?:\s*([\p{L}.]+))?(?:\s+(.+))?$""", RegexOption.IGNORE_CASE).matchEntire(line)
            if (match == null) { issues += line to "Die Mengenangabe konnte nicht erkannt werden."; return@forEach }
            val quantity = match.groupValues[1].replace(',', '.').toDoubleOrNull()
            val second = match.groupValues[2].trim()
            val third = match.groupValues[3].trim()
            val hasUnit = second.lowercase(java.util.Locale.GERMAN) in knownUnits
            val unit = if (hasUnit) second else "Stück"
            val name = if (hasUnit) third else listOf(second, third).filter(String::isNotBlank).joinToString(" ")
            if (name.isBlank() || quantity == null) { issues += line to "Bitte Menge, Einheit und Zutat prüfen."; return@forEach }
            val product = nutritionSnapshot[name] ?: nutritionSnapshot.entries.singleOrNull { (key, _) ->
                key.contains(name, ignoreCase = true) || name.contains(key, ignoreCase = true)
            }?.value
            if (product == null) { issues += line to "Für diese Zutat liegen keine Nährwertdaten vor."; return@forEach }
            val nutrition = NutritionPer100(product.caloriesKcalPer100, product.carbohydratesGPer100, product.proteinGPer100, product.fatGPer100, if (product.nutritionBasis == "100ml") IngredientMeasureConverter.Basis.MILLILITRES else IngredientMeasureConverter.Basis.GRAMS, product.sugarsGPer100, product.fiberGPer100, product.saltGPer100, product.saturatedFatGPer100)
            val result = nutrition.forAmount(quantity, unit, name)
            if (result == null) issues += line to if (unit.lowercase(java.util.Locale.GERMAN) in setOf("stück", "stk", "stk.")) "Aufgrund der Mengenangabe nicht berechenbar: Stückgewicht fehlt." else "Die Einheit '$unit' hat kein hinterlegtes Referenzgewicht und kann deshalb nicht berechnet werden."
            else if (listOf(product.caloriesKcalPer100, product.carbohydratesGPer100, product.proteinGPer100, product.fatGPer100).all { it == null }) issues += line to "Für diese Zutat fehlen verwertbare Grundnährwerte."
            else calculated += result
        }
        calculated.sumNutrition().copy(
            uncalculatedIngredients = issues.size,
            sugarsComplete = calculated.all { it.sugarsComplete } && issues.isEmpty(),
            fiberComplete = calculated.all { it.fiberComplete } && issues.isEmpty(),
            saltComplete = calculated.all { it.saltComplete } && issues.isEmpty(),
            saturatedFatComplete = calculated.all { it.saturatedFatComplete } && issues.isEmpty()
        ) to issues
    }
    val calculatedNutrition = nutritionEvaluation.first
    val uncalculatedReasons = nutritionEvaluation.second
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
        Text("Zutaten", color = Ink, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            OutlinedButton(enabled = !barcodeLoading, modifier = Modifier.weight(1f), onClick = {
                barcodeError = ""
                barcodeLoading = true
                val options = GmsBarcodeScannerOptions.Builder()
                    .setBarcodeFormats(Barcode.FORMAT_EAN_13, Barcode.FORMAT_EAN_8, Barcode.FORMAT_UPC_A, Barcode.FORMAT_UPC_E)
                    .enableAutoZoom().build()
                GmsBarcodeScanning.getClient(context, options).startScan()
                    .addOnSuccessListener { barcode ->
                        val code = barcode.rawValue
                        if (code.isNullOrBlank()) {
                            barcodeError = "Kein Barcode erkannt. Du kannst das Produkt stattdessen suchen."
                            barcodeLoading = false
                            productSearchQuery = ""
                            productSearchMessage = barcodeError
                            showProductSearch = true
                        } else scope.launch {
                            val saved = productCatalog.findByBarcode(code)
                            if (saved != null) {
                                scannedProduct = OpenFoodFactsProduct(saved.barcode, saved.name, saved.brand, saved.caloriesKcalPer100, saved.carbohydratesGPer100, saved.proteinGPer100, saved.fatGPer100, saved.sourceUrl, saved.sugarsGPer100, saved.fiberGPer100, saved.saltGPer100, saved.saturatedFatGPer100, saved.nutritionBasis)
                                barcodeLoading = false
                            } else {
                                OpenFoodFactsClient.lookupBarcode(code).fold(
                                    onSuccess = { barcodeLoading = false; if (it.hasNutrition) scannedProduct = it else { productSearchQuery = it.name; productSearchMessage = "Produkt erkannt, aber es fehlen Nährwerte."; showProductSearch = true } },
                                    onFailure = { barcodeError = it.message ?: "Produkt konnte nicht geladen werden."; barcodeLoading = false; productSearchQuery = ""; productSearchMessage = barcodeError; showProductSearch = true }
                                )
                            }
                        }
                    }
                    .addOnCanceledListener { barcodeLoading = false }
                    .addOnFailureListener { error -> barcodeError = error.message ?: "Der Barcode-Scanner konnte nicht gestartet werden."; barcodeLoading = false; productSearchQuery = ""; productSearchMessage = barcodeError; showProductSearch = true }
            }) { Text(if (barcodeLoading) "Scanne…" else "Barcode scannen") }
            OutlinedButton(modifier = Modifier.weight(1f), onClick = {
                productSearchQuery = ""; productSearchResults = emptyList(); productSearchMessage = ""; showProductSearch = true
            }) { Text("Produkt suchen") }
            OutlinedButton(modifier = Modifier.weight(1f), onClick = { showCustomIngredientDialog = true }) { Text("Zutat hinzufügen") }
        }
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(value = ingredientsField, onValueChange = { ingredientsField = it.copy(text = it.text.replace("\\\\n", "\n")) }, modifier = Modifier.fillMaxWidth(), label = { Text("Zutaten – eine pro Zeile") }, placeholder = { Text("z. B. 500 g Kartoffeln\n1 Zwiebel") }, minLines = 4)
        if (barcodeError.isNotBlank()) Text(barcodeError, color = Color(0xFFB3261E), style = MaterialTheme.typography.bodySmall)
        Spacer(Modifier.height(10.dp))
        OutlinedTextField(steps, { steps = it }, Modifier.fillMaxWidth(), label = { Text("Zubereitung – ein Schritt pro Zeile") }, placeholder = { Text("Kartoffeln schälen und würfeln.\nZwiebel glasig anbraten.") }, minLines = 4)
        Spacer(Modifier.height(16.dp))
        TextButton(onClick = { showNutritionDetails = !showNutritionDetails }, modifier = Modifier.fillMaxWidth()) {
            Text(if (showNutritionDetails) "Nährwerte ausblenden  ⌃" else "Nährwerte anzeigen  ⌄", color = Copper)
        }
        if (showNutritionDetails) {
            if (ingredients.lines().any { it.isNotBlank() }) {
                Card(colors = CardDefaults.cardColors(containerColor = PaleCopper), shape = RoundedCornerShape(14.dp)) {
                    Column(Modifier.fillMaxWidth().padding(12.dp)) {
                        Text("Nährwertangaben", color = Ink, fontWeight = FontWeight.Bold)
                        val portionCount = servings.toIntOrNull()?.coerceAtLeast(1) ?: 2
                        if (calculatedNutrition.calculatedIngredients > 0) {
                            Text(if (uncalculatedReasons.isEmpty()) "Gesamtgericht" else "Bisher berechenbare Zutaten (Teilmenge)", color = Copper, fontWeight = FontWeight.SemiBold)
                            NutritionValueLines(calculatedNutrition)
                            Spacer(Modifier.height(6.dp))
                            Text("Pro Portion" + if (uncalculatedReasons.isEmpty()) "" else " (Teilmenge)", color = Copper, fontWeight = FontWeight.SemiBold)
                            NutritionValueLines(calculatedNutrition.copy(caloriesKcal = calculatedNutrition.caloriesKcal / portionCount, carbohydratesG = calculatedNutrition.carbohydratesG / portionCount, proteinG = calculatedNutrition.proteinG / portionCount, fatG = calculatedNutrition.fatG / portionCount, sugarsG = calculatedNutrition.sugarsG / portionCount, fiberG = calculatedNutrition.fiberG / portionCount, saltG = calculatedNutrition.saltG / portionCount, saturatedFatG = calculatedNutrition.saturatedFatG / portionCount))
                        } else {
                            Text("Noch keine Zutat mit passender Mengenangabe berechenbar.", color = MutedInk, style = MaterialTheme.typography.bodySmall)
                        }
                        Text(calculatedNutrition.calculatedIngredients.toString() + " von " + (calculatedNutrition.calculatedIngredients + calculatedNutrition.uncalculatedIngredients) + " Zutaten berechnet" + if (calculatedNutrition.estimatedMeasures) " · Küchenmaße/Dichte teilweise geschätzt" else "", color = MutedInk, style = MaterialTheme.typography.labelSmall)
                        if (uncalculatedReasons.isNotEmpty()) {
                            Spacer(Modifier.height(8.dp))
                            Text("Nicht berechnete Zutaten", color = Ink, fontWeight = FontWeight.SemiBold)
                            uncalculatedReasons.forEach { (line, reason) ->
                                Text("• $line", color = Ink, style = MaterialTheme.typography.bodySmall)
                                Text(reason, color = MutedInk, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
                Spacer(Modifier.height(10.dp))
            }
            Text("Manuelle Nährwertangaben (optional)", color = Ink, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(kcal, { kcal = it }, Modifier.fillMaxWidth(), label = { Text("Kalorien (kcal)") }, singleLine = true)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(carbs, { carbs = it }, Modifier.weight(1f), label = { Text("Kohlenhydrate (g)") }, singleLine = true)
                OutlinedTextField(protein, { protein = it }, Modifier.weight(1f), label = { Text("Eiweiß (g)") }, singleLine = true)
            }
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(fat, { fat = it }, Modifier.fillMaxWidth(), label = { Text("Fett (g)") }, singleLine = true)
    
        }
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
                        caloriesPerServing = if (calculatedNutrition.calculatedIngredients > 0) { if (uncalculatedReasons.isEmpty()) calculatedNutrition.caloriesKcal / (servings.toIntOrNull()?.coerceAtLeast(1) ?: 2) else null } else kcal.toDecimalOrNull(),
                        carbohydratesPerServing = if (calculatedNutrition.calculatedIngredients > 0) { if (uncalculatedReasons.isEmpty()) calculatedNutrition.carbohydratesG / (servings.toIntOrNull()?.coerceAtLeast(1) ?: 2) else null } else carbs.toDecimalOrNull(),
                        proteinPerServing = if (calculatedNutrition.calculatedIngredients > 0) { if (uncalculatedReasons.isEmpty()) calculatedNutrition.proteinG / (servings.toIntOrNull()?.coerceAtLeast(1) ?: 2) else null } else protein.toDecimalOrNull(),
                        fatPerServing = if (calculatedNutrition.calculatedIngredients > 0) { if (uncalculatedReasons.isEmpty()) calculatedNutrition.fatG / (servings.toIntOrNull()?.coerceAtLeast(1) ?: 2) else null } else fat.toDecimalOrNull(),
                        sugarsPerServing = if (calculatedNutrition.calculatedIngredients > 0 && calculatedNutrition.sugarsComplete && uncalculatedReasons.isEmpty()) calculatedNutrition.sugarsG / (servings.toIntOrNull()?.coerceAtLeast(1) ?: 2) else null,
                        fiberPerServing = if (calculatedNutrition.calculatedIngredients > 0 && calculatedNutrition.fiberComplete && uncalculatedReasons.isEmpty()) calculatedNutrition.fiberG / (servings.toIntOrNull()?.coerceAtLeast(1) ?: 2) else null,
                        saltPerServing = if (calculatedNutrition.calculatedIngredients > 0 && calculatedNutrition.saltComplete && uncalculatedReasons.isEmpty()) calculatedNutrition.saltG / (servings.toIntOrNull()?.coerceAtLeast(1) ?: 2) else null,
                        saturatedFatPerServing = if (calculatedNutrition.calculatedIngredients > 0 && calculatedNutrition.saturatedFatComplete && uncalculatedReasons.isEmpty()) calculatedNutrition.saturatedFatG / (servings.toIntOrNull()?.coerceAtLeast(1) ?: 2) else null,
                        updatedAt = System.currentTimeMillis()
                    )
                    onSave(entity, ingredients.lines(), steps.lines(), nutritionByIngredientName.toMap())
                }
            }) { Text("Rezept speichern") }
        }
    }
    if (showProductSearch) {
        AlertDialog(
            onDismissRequest = { if (!productSearchLoading) showProductSearch = false },
            title = { Text("Produkt suchen") },
            text = {
                Column(Modifier.heightIn(max = 420.dp).verticalScroll(rememberScrollState())) {
                    Text("Suche nach Lebensmittel oder Marke, z. B. Semmelknödel oder Gut & Günstig.", color = MutedInk)
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(value = productSearchQuery, onValueChange = { productSearchQuery = it }, modifier = Modifier.fillMaxWidth(), label = { Text("Suchbegriff") }, singleLine = true)
                    Spacer(Modifier.height(6.dp))
                    Button(enabled = productSearchQuery.trim().length >= 2 && !productSearchLoading, onClick = {
                        productSearchLoading = true
                        productSearchMessage = ""
                        scope.launch {
                            runCatching {
                                de.topfsecret.app.nutrition.BlsDatasetSeeder.ensureSeeded(context, blsFoodDao)
                                blsFoodDao.search(productSearchQuery.trim()).map { item ->
                                    OpenFoodFactsProduct(
                                        barcode = "BLS:${item.code}", name = item.germanName, brand = "BLS 4.0",
                                        caloriesKcalPer100 = item.kcalPer100, carbohydratesGPer100 = item.carbohydratesGPer100,
                                        proteinGPer100 = item.proteinGPer100, fatGPer100 = item.fatGPer100, sourceUrl = "https://blsdb.de/",
                                        sugarsGPer100 = item.blsNumber("SUGAR"), fiberGPer100 = item.blsNumber("FIBT"),
                                        saltGPer100 = item.blsNumber("NACL"), saturatedFatGPer100 = item.blsNumber("FASAT")
                                    )
                                }
                            }.onSuccess { blsProducts ->
                                val localResults = productCatalog.search(productSearchQuery).map {
                                    OpenFoodFactsProduct(it.barcode, it.name, it.brand, it.caloriesKcalPer100, it.carbohydratesGPer100, it.proteinGPer100, it.fatGPer100, it.sourceUrl, it.sugarsGPer100, it.fiberGPer100, it.saltGPer100, it.saturatedFatGPer100, it.nutritionBasis)
                                }
                                val customResults = localResults.filter { it.barcode.startsWith("CUSTOM:") }
                                val otherLocalResults = localResults.filterNot { it.barcode.startsWith("CUSTOM:") }
                                OpenFoodFactsClient.searchProducts(productSearchQuery).fold(
                                    onSuccess = { remote ->
                                        val combined = customResults + blsProducts + otherLocalResults + remote
                                        productSearchResults = combined.distinctBy { it.barcode }.take(40)
                                        productSearchMessage = if (productSearchResults.isEmpty()) "Keine passenden Produkte oder BLS-Lebensmittel gefunden." else "Eigene Zutaten zuerst · danach BLS 4.0, lokale und Online-Produkte."
                                        productSearchLoading = false
                                    },
                                    onFailure = { error ->
                                        productSearchResults = (customResults + blsProducts + otherLocalResults).distinctBy { it.barcode }.take(40)
                                        productSearchMessage = if (productSearchResults.isNotEmpty()) "Offline-Treffer aus BLS 4.0 und lokalem Katalog. Online-Suche nicht erreichbar." else (error.message ?: "Die Suche ist fehlgeschlagen.")
                                        productSearchLoading = false
                                    }
                                )
                            }.onFailure { error ->
                                val localResults = productCatalog.search(productSearchQuery).map {
                                    OpenFoodFactsProduct(it.barcode, it.name, it.brand, it.caloriesKcalPer100, it.carbohydratesGPer100, it.proteinGPer100, it.fatGPer100, it.sourceUrl, it.sugarsGPer100, it.fiberGPer100, it.saltGPer100, it.saturatedFatGPer100, it.nutritionBasis)
                                }
                                productSearchResults = localResults.sortedByDescending { it.barcode.startsWith("CUSTOM:") }
                                productSearchMessage = "BLS konnte nicht geladen werden: ${error.message ?: "unbekannter Fehler"}"
                                productSearchLoading = false
                            }
                        }
                    }) { Text(if (productSearchLoading) "Suche…" else "Suchen") }
                    if (productSearchMessage.isNotBlank()) {
                        Spacer(Modifier.height(6.dp))
                        Text(productSearchMessage, color = MutedInk, style = MaterialTheme.typography.bodySmall)
                    }
                    productSearchResults.forEach { item ->
                        TextButton(modifier = Modifier.fillMaxWidth(), onClick = {
                            scannedProduct = item
                            showProductSearch = false
                        }) {
                            Column(Modifier.fillMaxWidth()) {
                                Text(item.name, color = Ink, fontWeight = FontWeight.SemiBold)
                                Text(listOfNotNull(item.brand, if (item.hasNutrition) "Nährwerte vorhanden" else "ohne Nährwerte").joinToString(" · "), color = MutedInk, style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(enabled = productSearchQuery.isNotBlank() && !productSearchLoading, onClick = {
                    val name = productSearchQuery.trim()
                    val existing = ingredients.lines().map(String::trim).filter(String::isNotBlank)
                    if (existing.none { it.endsWith(name, ignoreCase = true) }) {
                        val updated = (existing + name).joinToString("\n") + "\n"
                        ingredientsField = TextFieldValue(updated, androidx.compose.ui.text.TextRange(updated.length))
                    }
                    showProductSearch = false
                }) { Text("Zutat ohne Produktdaten übernehmen") }
            },
            dismissButton = { TextButton(onClick = { showProductSearch = false }) { Text("Schließen") } }
        )
    }
    if (showCustomIngredientDialog) {
        AlertDialog(
            onDismissRequest = { showCustomIngredientDialog = false },
            title = { Text("Eigene Zutat hinzufügen") },
            text = {
                Column(Modifier.heightIn(max = 520.dp).verticalScroll(rememberScrollState())) {
                    Text("Speichert die Zutat dauerhaft auf diesem Gerät. Du findest sie später wieder unter „Produkt suchen“.", color = MutedInk, style = MaterialTheme.typography.bodySmall)
                    OutlinedTextField(customIngredientName, { customIngredientName = it }, Modifier.fillMaxWidth(), label = { Text("Name der Zutat *") }, singleLine = true)
                    OutlinedTextField(customIngredientBrand, { customIngredientBrand = it }, Modifier.fillMaxWidth(), label = { Text("Marke / Hersteller (optional)") }, singleLine = true)
                    Spacer(Modifier.height(8.dp))
                    Text("Nährwerte beziehen sich auf:", color = Ink, fontWeight = FontWeight.SemiBold)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(selected = customIngredientBasis == "100g", onClick = { customIngredientBasis = "100g" }, label = { Text("100 g") })
                        FilterChip(selected = customIngredientBasis == "100ml", onClick = { customIngredientBasis = "100ml" }, label = { Text("100 ml") })
                    }
                    Text("Bitte Werte von der Verpackung je 100 g bzw. 100 ml übernehmen. Bei festen Lebensmitteln meist 100 g, bei Getränken meist 100 ml. Fehlende Angaben leer lassen.", color = MutedInk, style = MaterialTheme.typography.bodySmall)
                    OutlinedTextField(customKcal, { customKcal = it }, Modifier.fillMaxWidth(), label = { Text("Energie (kcal)") }, singleLine = true)
                    OutlinedTextField(customCarbs, { customCarbs = it }, Modifier.fillMaxWidth(), label = { Text("Kohlenhydrate (g)") }, singleLine = true)
                    OutlinedTextField(customSugar, { customSugar = it }, Modifier.fillMaxWidth(), label = { Text("davon Zucker (g)") }, singleLine = true)
                    OutlinedTextField(customFiber, { customFiber = it }, Modifier.fillMaxWidth(), label = { Text("Ballaststoffe (g)") }, singleLine = true)
                    OutlinedTextField(customProtein, { customProtein = it }, Modifier.fillMaxWidth(), label = { Text("Eiweiß (g)") }, singleLine = true)
                    OutlinedTextField(customFat, { customFat = it }, Modifier.fillMaxWidth(), label = { Text("Fett (g)") }, singleLine = true)
                    OutlinedTextField(customSatFat, { customSatFat = it }, Modifier.fillMaxWidth(), label = { Text("davon gesättigte Fettsäuren (g)") }, singleLine = true)
                    OutlinedTextField(customSalt, { customSalt = it }, Modifier.fillMaxWidth(), label = { Text("Salz (g)") }, singleLine = true)
                }
            },
            confirmButton = {
                TextButton(enabled = customIngredientName.isNotBlank(), onClick = {
                    val name = customIngredientName.trim()
                    val code = "CUSTOM:" + UUID.randomUUID().toString()
                    val source = "local://custom"
                    val product = OpenFoodFactsProduct(code, name, customIngredientBrand.trim().ifBlank { null },
                        customKcal.toDecimalOrNull(), customCarbs.toDecimalOrNull(), customProtein.toDecimalOrNull(), customFat.toDecimalOrNull(),
                        source, customSugar.toDecimalOrNull(), customFiber.toDecimalOrNull(), customSalt.toDecimalOrNull(), customSatFat.toDecimalOrNull(), customIngredientBasis)
                    scope.launch {
                        productCatalog.save(LocalFoodProductEntity(code, name, product.brand, product.caloriesKcalPer100, product.carbohydratesGPer100, product.proteinGPer100, product.fatGPer100, source, "Eigene Zutat", "USER_CREATED",
                            sugarsGPer100 = product.sugarsGPer100, fiberGPer100 = product.fiberGPer100, saltGPer100 = product.saltGPer100, saturatedFatGPer100 = product.saturatedFatGPer100, nutritionBasis = product.nutritionBasis))
                        nutritionByIngredientName[name] = product
                        val unit = if (customIngredientBasis == "100ml") "100 ml " else "100 g "
                        val updated = (ingredients.lines().map(String::trim).filter(String::isNotBlank) + unit + name).joinToString("\n") + "\n"
                        ingredientsField = TextFieldValue(updated, androidx.compose.ui.text.TextRange(updated.length))
                        showCustomIngredientDialog = false
                        customIngredientName = ""; customIngredientBrand = ""; customKcal = ""; customCarbs = ""; customSugar = ""; customFiber = ""; customProtein = ""; customFat = ""; customSatFat = ""; customSalt = ""; customIngredientBasis = "100g"
                    }
                }) { Text("Speichern & übernehmen") }
            },
            dismissButton = { TextButton(onClick = { showCustomIngredientDialog = false }) { Text("Abbrechen") } }
        )
    }
    scannedProduct?.let { product ->
        AlertDialog(
            onDismissRequest = { scannedProduct = null },
            title = { Text("Produkt gefunden") },
            text = {
                Column(Modifier.heightIn(max = 440.dp).verticalScroll(rememberScrollState())) {
                    if (editingProduct) {
                        OutlinedTextField(editedBarcode, { editedBarcode = it.filter(Char::isDigit).take(14) }, Modifier.fillMaxWidth(), label = { Text("Barcode") }, singleLine = true)
                        OutlinedTextField(editedName, { editedName = it }, Modifier.fillMaxWidth(), label = { Text("Produktname") }, singleLine = true)
                        OutlinedTextField(editedBrand, { editedBrand = it }, Modifier.fillMaxWidth(), label = { Text("Marke") }, singleLine = true)
                        OutlinedTextField(editedKcal, { editedKcal = it }, Modifier.fillMaxWidth(), label = { Text("kcal je 100 g") }, singleLine = true)
                        OutlinedTextField(editedCarbs, { editedCarbs = it }, Modifier.fillMaxWidth(), label = { Text("Kohlenhydrate g je 100 g") }, singleLine = true)
                        OutlinedTextField(editedProtein, { editedProtein = it }, Modifier.fillMaxWidth(), label = { Text("Eiweiß g je 100 g") }, singleLine = true)
                        OutlinedTextField(editedFat, { editedFat = it }, Modifier.fillMaxWidth(), label = { Text("Fett g je 100 g") }, singleLine = true)
                        Text("Manuell korrigierte Werte werden als selbst geprüft lokal gespeichert.", color = MutedInk, style = MaterialTheme.typography.bodySmall)
                    } else {
                        Text(product.name, fontWeight = FontWeight.Bold)
                        product.brand?.takeIf(String::isNotBlank)?.let { Text(it, color = MutedInk) }
                        Spacer(Modifier.height(8.dp))
                        Text("Nährwerte je " + if (product.nutritionBasis == "100ml") "100 ml" else "100 g")
                        Text("Energie: " + (product.caloriesKcalPer100?.let { it.prettyNumber() + " kcal" } ?: "nicht vorhanden"))
                        Text("Kohlenhydrate: " + (product.carbohydratesGPer100?.let { it.prettyNumber() + " g" } ?: "nicht vorhanden"))
                        Text("Eiweiß: " + (product.proteinGPer100?.let { it.prettyNumber() + " g" } ?: "nicht vorhanden"))
                        Text("Fett: " + (product.fatGPer100?.let { it.prettyNumber() + " g" } ?: "nicht vorhanden"))
                        Text("davon Zucker: " + (product.sugarsGPer100?.let { it.prettyNumber() + " g" } ?: "nicht vorhanden"))
                        Text("Ballaststoffe: " + (product.fiberGPer100?.let { it.prettyNumber() + " g" } ?: "nicht vorhanden"))
                        Text("Salz: " + (product.saltGPer100?.let { it.prettyNumber() + " g" } ?: "nicht vorhanden"))
                        Text("davon gesättigte Fettsäuren: " + (product.saturatedFatGPer100?.let { it.prettyNumber() + " g" } ?: "nicht vorhanden"))
                        Spacer(Modifier.height(6.dp))
                        Text(if (product.barcode.startsWith("BLS:")) "Quelle: Bundeslebensmittelschlüssel (BLS) 4.0 · Durchschnittswerte je 100 g." else if (product.barcode.startsWith("CUSTOM:")) "Quelle: selbst eingetragen · eigene Angaben prüfen." else "Quelle: Open Food Facts · vor Übernahme bitte Verpackung prüfen.", color = MutedInk, style = MaterialTheme.typography.bodySmall)
                    }
                }
            },
            confirmButton = {
                if (editingProduct) {
                    TextButton(onClick = {
                        val correctedCode = editedBarcode.trim()
                        val correctedName = editedName.trim()
                        if (correctedCode.matches(Regex("[0-9]{8,14}")) && correctedName.isNotBlank()) {
                            scope.launch {
                                if (correctedCode != product.barcode) productCatalog.deleteByBarcode(product.barcode)
                                val corrected = OpenFoodFactsProduct(correctedCode, correctedName, editedBrand.trim().ifBlank { null }, editedKcal.toDecimalOrNull(), editedCarbs.toDecimalOrNull(), editedProtein.toDecimalOrNull(), editedFat.toDecimalOrNull(), product.sourceUrl, product.sugarsGPer100, product.fiberGPer100, product.saltGPer100, product.saturatedFatGPer100)
                                productCatalog.save(LocalFoodProductEntity(corrected.barcode, corrected.name, corrected.brand, corrected.caloriesKcalPer100, corrected.carbohydratesGPer100, corrected.proteinGPer100, corrected.fatGPer100, corrected.sourceUrl, "Manuell geprüft", "VERIFIED_LOCAL", sugarsGPer100 = corrected.sugarsGPer100, fiberGPer100 = corrected.fiberGPer100, saltGPer100 = corrected.saltGPer100, saturatedFatGPer100 = corrected.saturatedFatGPer100, nutritionBasis = corrected.nutritionBasis))
                                scannedProduct = corrected
                                editingProduct = false
                            }
                        }
                    }) { Text("Korrektur speichern") }
                } else {
                    TextButton(onClick = {
                        scope.launch {
                            if (!product.barcode.startsWith("BLS:") && !product.barcode.startsWith("CUSTOM:")) {
                                val existing = productCatalog.findByBarcode(product.barcode)
                                val quality = if (existing?.quality == "VERIFIED_LOCAL") "VERIFIED_LOCAL" else "UNVERIFIED"
                                val sourceName = existing?.sourceName ?: "Open Food Facts"
                                productCatalog.save(LocalFoodProductEntity(product.barcode, product.name, product.brand, product.caloriesKcalPer100, product.carbohydratesGPer100, product.proteinGPer100, product.fatGPer100, product.sourceUrl, sourceName, quality, sugarsGPer100 = product.sugarsGPer100, fiberGPer100 = product.fiberGPer100, saltGPer100 = product.saltGPer100, saturatedFatGPer100 = product.saturatedFatGPer100, nutritionBasis = product.nutritionBasis))
                            }
                            nutritionByIngredientName[product.name] = product
                            if (ingredients.lines().none { it.contains(product.name, ignoreCase = true) }) {
                                val updated = listOf(ingredients.trimEnd(), (if (product.nutritionBasis == "100ml") "100 ml " else "100 g ") + product.name).filter(String::isNotBlank).joinToString("\n") + "\n"
                                ingredientsField = TextFieldValue(updated, androidx.compose.ui.text.TextRange(updated.length))
                            }
                            scannedProduct = null
                        }
                    }) { Text("Bestätigen & als Zutat übernehmen") }
                }
            },
            dismissButton = {
                Row {
                    TextButton(onClick = {
                        editedBarcode = product.barcode
                        editedName = product.name
                        editedBrand = product.brand.orEmpty()
                        editedKcal = product.caloriesKcalPer100?.toString().orEmpty()
                        editedCarbs = product.carbohydratesGPer100?.toString().orEmpty()
                        editedProtein = product.proteinGPer100?.toString().orEmpty()
                        editedFat = product.fatGPer100?.toString().orEmpty()
                        editingProduct = true
                    }) { Text("Angaben korrigieren") }
                    TextButton(onClick = { scannedProduct = null; editingProduct = false }) { Text("Abbrechen") }
                }
            }
        )
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
    var showNutritionDetails by rememberSaveable(recipe.recipe.id) { mutableStateOf(false) }
    val savedNutrition = remember(recipe.ingredients, recipe.recipe.servings) {
        val calculated = mutableListOf<NutritionPer100.Totals>()
        var uncalculated = 0
        recipe.ingredients.forEach { ingredient ->
            val amount = IngredientMeasureConverter.parseAmount(ingredient.amount)
            if (amount == null) {
                uncalculated++
            } else {
                val nutrition = NutritionPer100(
                    ingredient.caloriesPer100, ingredient.carbohydratesPer100,
                    ingredient.proteinPer100, ingredient.fatPer100,
                    if (ingredient.nutritionBasis == "100ml") IngredientMeasureConverter.Basis.MILLILITRES else IngredientMeasureConverter.Basis.GRAMS, ingredient.sugarsPer100,
                    ingredient.fiberPer100, ingredient.saltPer100, ingredient.saturatedFatPer100
                )
                val result = nutrition.forAmount(amount.first, amount.second, ingredient.name)
                if (result == null || listOf(ingredient.caloriesPer100, ingredient.carbohydratesPer100, ingredient.proteinPer100, ingredient.fatPer100).all { it == null }) {
                    uncalculated++
                } else calculated += result
            }
        }
        calculated.sumNutrition().copy(
            uncalculatedIngredients = uncalculated,
            sugarsComplete = calculated.all { it.sugarsComplete } && uncalculated == 0,
            fiberComplete = calculated.all { it.fiberComplete } && uncalculated == 0,
            saltComplete = calculated.all { it.saltComplete } && uncalculated == 0,
            saturatedFatComplete = calculated.all { it.saturatedFatComplete } && uncalculated == 0
        )
    }
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
            Column(Modifier.padding(vertical = 3.dp)) {
                Text("• " + line, color = Ink)
                if (!it.barcode.isNullOrBlank()) {
                    Text((if (it.barcode.startsWith("BLS:")) "BLS-Code: " + it.barcode.removePrefix("BLS:") + " · Quelle: BLS 4.0" else "Barcode: " + it.barcode + " · Nährwertquelle: Open Food Facts"), color = MutedInk, style = MaterialTheme.typography.labelSmall)
                }
            }
        }
        Spacer(Modifier.height(22.dp))
        Text("Zubereitung", color = Ink, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        if (recipe.steps.isEmpty()) Text("Noch keine Zubereitungsschritte eingetragen.", color = MutedInk)
        else recipe.steps.sortedBy { it.position }.forEachIndexed { index, step -> Text((index + 1).toString() + ". " + step.instruction, color = Ink, modifier = Modifier.padding(vertical = 5.dp)) }
        Spacer(Modifier.height(16.dp))
        TextButton(onClick = { showNutritionDetails = !showNutritionDetails }, modifier = Modifier.fillMaxWidth()) {
            Text(if (showNutritionDetails) "Nährwerte ausblenden  ⌃" else "Nährwerte anzeigen  ⌄", color = Copper)
        }
        if (showNutritionDetails) {
            if (savedNutrition.calculatedIngredients > 0) {
                Spacer(Modifier.height(12.dp))
                Card(colors = CardDefaults.cardColors(containerColor = PaleCopper), shape = RoundedCornerShape(16.dp)) {
                    Column(Modifier.fillMaxWidth().padding(14.dp)) {
                        Text("Nährwertangaben", color = Ink, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(5.dp))
                        Text(if (savedNutrition.uncalculatedIngredients == 0) "Gesamtgericht" else "Berechenbare Zutaten (Teilmenge)", color = Copper, fontWeight = FontWeight.SemiBold)
                        NutritionValueLines(savedNutrition)
                        Spacer(Modifier.height(4.dp))
                        Text("Pro Portion", color = Copper, fontWeight = FontWeight.SemiBold)
                        val servings = recipe.recipe.servings.coerceAtLeast(1).toDouble()
                        NutritionValueLines(savedNutrition.copy(
                            caloriesKcal = savedNutrition.caloriesKcal / servings,
                            carbohydratesG = savedNutrition.carbohydratesG / servings,
                            proteinG = savedNutrition.proteinG / servings,
                            fatG = savedNutrition.fatG / servings,
                            sugarsG = savedNutrition.sugarsG / servings,
                            fiberG = savedNutrition.fiberG / servings,
                            saltG = savedNutrition.saltG / servings,
                            saturatedFatG = savedNutrition.saturatedFatG / servings
                        ))
                        Text(if (savedNutrition.uncalculatedIngredients == 0) "Angaben sind Richtwerte." else "${savedNutrition.uncalculatedIngredients} Zutat(en) nicht berechenbar; Werte zeigen nur die berechenbare Teilmenge.", color = MutedInk, style = MaterialTheme.typography.labelSmall)
                    }
                }
            } else {
                val manualLabels = listOfNotNull(
                    recipe.recipe.caloriesPerServing?.let { "⚡ " + it.prettyNumber() + " kcal" },
                    recipe.recipe.carbohydratesPerServing?.let { "Kohlenhydrate " + it.prettyNumber() + " g" },
                    recipe.recipe.sugarsPerServing?.let { "davon Zucker " + it.prettyNumber() + " g" },
                    recipe.recipe.fiberPerServing?.let { "Ballaststoffe " + it.prettyNumber() + " g" },
                    recipe.recipe.proteinPerServing?.let { "Eiweiß " + it.prettyNumber() + " g" },
                    recipe.recipe.fatPerServing?.let { "Fett " + it.prettyNumber() + " g" },
                    recipe.recipe.saturatedFatPerServing?.let { "davon gesättigte Fettsäuren " + it.prettyNumber() + " g" },
                    recipe.recipe.saltPerServing?.let { "Salz " + it.prettyNumber() + " g" }
                )
                if (manualLabels.isNotEmpty()) {
                    Spacer(Modifier.height(12.dp))
                    Card(colors = CardDefaults.cardColors(containerColor = PaleCopper), shape = RoundedCornerShape(16.dp)) {
                        Column(Modifier.fillMaxWidth().padding(14.dp)) {
                            Text(if (recipe.recipe.sourceUrl.isNullOrBlank()) "Manuelle Nährwertangaben" else "Nährwertangaben aus Rezeptquelle", color = Ink, fontWeight = FontWeight.Bold)
                            manualLabels.forEach { Text(it, color = Ink, style = MaterialTheme.typography.bodyMedium) }
                            Text("Angaben sind Richtwerte.", color = MutedInk, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
            val sourceNutritionLabels = listOfNotNull(
                recipe.recipe.caloriesPerServing?.let { "Energie " + it.prettyNumber() + " kcal" },
                recipe.recipe.carbohydratesPerServing?.let { "Kohlenhydrate " + it.prettyNumber() + " g" },
                recipe.recipe.sugarsPerServing?.let { "davon Zucker " + it.prettyNumber() + " g" },
                recipe.recipe.fiberPerServing?.let { "Ballaststoffe " + it.prettyNumber() + " g" },
                recipe.recipe.proteinPerServing?.let { "Eiweiß " + it.prettyNumber() + " g" },
                recipe.recipe.fatPerServing?.let { "Fett " + it.prettyNumber() + " g" },
                recipe.recipe.saturatedFatPerServing?.let { "davon gesättigte Fettsäuren " + it.prettyNumber() + " g" },
                recipe.recipe.saltPerServing?.let { "Salz " + it.prettyNumber() + " g" }
            )
            if (savedNutrition.calculatedIngredients > 0 && sourceNutritionLabels.isNotEmpty()) {
                Spacer(Modifier.height(10.dp))
                Card(colors = CardDefaults.cardColors(containerColor = PaleCopper), shape = RoundedCornerShape(16.dp)) {
                    Column(Modifier.fillMaxWidth().padding(14.dp)) {
                        Text("Zusätzliche Nährwerte aus der Rezeptquelle (pro Portion)", color = Ink, fontWeight = FontWeight.Bold)
                        sourceNutritionLabels.forEach { Text(it, color = Ink, style = MaterialTheme.typography.bodyMedium) }
                        Text("Originalangaben der Rezeptseite; getrennt von der Zutatenberechnung.", color = MutedInk, style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
    
        }
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

private fun de.topfsecret.app.data.local.BlsFoodEntity.blsNumber(code: String): Double? {
    val raw = runCatching { org.json.JSONObject(nutrientsJson).optJSONArray(code)?.opt(0) }.getOrNull() ?: return null
    val text = raw.toString()
    if (text == "null" || text.startsWith("<") || text == "-") return null
    return text.replace(',', '.').toDoubleOrNull()?.takeIf { it.isFinite() && it >= 0.0 }
}

@Composable
private fun NutritionValueLines(values: NutritionPer100.Totals) {
    val rows = listOf(
        "Energie" to "${values.caloriesKcal.prettyNumber()} kcal",
        "Kohlenhydrate" to "${values.carbohydratesG.prettyNumber()} g",
        "davon Zucker" to if (values.sugarsComplete) "${values.sugarsG.prettyNumber()} g" else "nicht vollständig verfügbar",
        "Ballaststoffe" to if (values.fiberComplete) "${values.fiberG.prettyNumber()} g" else "nicht vollständig verfügbar",
        "Eiweiß" to "${values.proteinG.prettyNumber()} g",
        "Fett" to "${values.fatG.prettyNumber()} g",
        "davon gesättigte Fettsäuren" to if (values.saturatedFatComplete) "${values.saturatedFatG.prettyNumber()} g" else "nicht vollständig verfügbar",
        "Salz" to if (values.saltComplete) "${values.saltG.prettyNumber()} g" else "nicht vollständig verfügbar"
    )
    rows.forEach { (label, value) -> Text("$label: $value", color = Ink, style = MaterialTheme.typography.bodySmall) }
}

private fun Double.prettyNumber(): String =
    java.text.DecimalFormat("0.##", java.text.DecimalFormatSymbols.getInstance(java.util.Locale.GERMAN)).format(this)


@Composable
private fun RecipePhoto(uri: String?, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val bitmap by produceState<Bitmap?>(initialValue = null, uri) {
        value = withContext(Dispatchers.IO) {
            uri?.let { value ->
                runCatching {
                    if (value.startsWith("https://", true) || value.startsWith("http://", true)) {
                        val connection = java.net.URL(value).openConnection().apply {
                            connectTimeout = 10_000
                            readTimeout = 12_000
                        }
                        connection.getInputStream().use { BitmapFactory.decodeStream(it) }
                    } else {
                        context.contentResolver.openInputStream(Uri.parse(value))?.use { stream ->
                            BitmapFactory.decodeStream(stream)
                        }
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

private suspend fun downloadRecipeImage(context: Context, imageUrl: String, sourceUrl: String): String? =
    withContext(Dispatchers.IO) {
        runCatching {
            val parsed = java.net.URL(imageUrl)
            require(parsed.protocol == "https" || parsed.protocol == "http") { "Ungültiges Bildformat" }
            val connection = (parsed.openConnection() as java.net.HttpURLConnection).apply {
                instanceFollowRedirects = true
                connectTimeout = 10_000
                readTimeout = 15_000
                setRequestProperty("User-Agent", "TopfSecret/0.7.1 (recipe image import)")
            }
            try {
                connection.connect()
                require(connection.responseCode in 200..299) { "Rezeptbild konnte nicht geladen werden" }
                require(connection.contentType?.startsWith("image/") == true) { "Die Bildquelle liefert keine Bilddatei" }
                require(connection.contentLengthLong <= 8L * 1024L * 1024L || connection.contentLengthLong < 0) { "Rezeptbild ist zu groß" }
                val directory = File(context.filesDir, "recipe_images").apply { mkdirs() }
                val destination = File(directory, "online-${UUID.randomUUID()}.img")
                connection.inputStream.use { input ->
                    destination.outputStream().use { output ->
                        val buffer = ByteArray(8192)
                        var total = 0L
                        while (true) {
                            val count = input.read(buffer)
                            if (count < 0) break
                            total += count
                            require(total <= 8L * 1024L * 1024L) { "Rezeptbild ist zu groß" }
                            output.write(buffer, 0, count)
                        }
                    }
                }
                File(directory, destination.name + ".source.txt").writeText("Bildquelle: $imageUrl\nRezeptquelle: $sourceUrl\n")
                Uri.fromFile(destination).toString()
            } finally {
                connection.disconnect()
            }
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


@Composable
private fun AcknowledgementsScreen() {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(22.dp)) {
        Text("TRANSPARENZ & DANKE", color = Copper, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
        Spacer(Modifier.height(8.dp))
        Text("Quellen & Danksagungen", color = Ink, fontSize = 27.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(10.dp))
        Text("Topf Secret nutzt offene Daten und freie Projekte. Wir bedanken uns bei den Einrichtungen, Entwicklerinnen und Entwicklern sowie allen Mitwirkenden, die solche Ressourcen möglich machen.", color = MutedInk, style = MaterialTheme.typography.bodyLarge, lineHeight = 24.sp)
        Spacer(Modifier.height(20.dp))
        Card(shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = Paper)) {
            Column(Modifier.padding(18.dp)) {
                Text("Bundeslebensmittelschlüssel (BLS) 4.0", color = Ink, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                Text("Herausgeber: Max Rubner-Institut – Bundesforschungsinstitut für Ernährung und Lebensmittel.", color = MutedInk)
                Spacer(Modifier.height(8.dp))
                Text("Max Rubner-Institut (2025): Bundeslebensmittelschlüssel (BLS), Version 4.0 – Deutsche Nährstoffdatenbank. Karlsruhe. DOI: 10.25826/Data20251217-134202-0", color = Ink, style = MaterialTheme.typography.bodySmall)
                Spacer(Modifier.height(8.dp))
                Text("Lizenz: Creative Commons Namensnennung 4.0 International (CC BY 4.0). Änderungen und Aufbereitungen werden kenntlich gemacht.", color = MutedInk, style = MaterialTheme.typography.bodySmall)
                Spacer(Modifier.height(8.dp))
                Text("Website: blsdb.de · Lizenz: creativecommons.org/licenses/by/4.0/", color = Copper, style = MaterialTheme.typography.bodySmall)
                Spacer(Modifier.height(10.dp))
                Text("Das Max Rubner-Institut ist nicht Betreiber, Herausgeber oder Unterstützer der App Topf Secret und hat diese App nicht geprüft oder freigegeben.", color = MutedInk, style = MaterialTheme.typography.bodySmall)
            }
        }
        Spacer(Modifier.height(12.dp))
        Card(shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = Paper)) {
            Column(Modifier.padding(18.dp)) {
                Text("Open Food Facts", color = Ink, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                Text("Produktinformationen können aus der gemeinschaftlich gepflegten Datenbank Open Food Facts stammen. Datenbanklizenz: Open Database License (ODbL) 1.0. Quellen- und Lizenzhinweise werden bei der jeweiligen Datenverwendung beachtet.", color = MutedInk, style = MaterialTheme.typography.bodySmall)
                Spacer(Modifier.height(8.dp))
                Text("Website: openfoodfacts.org · Lizenz: opendatacommons.org/licenses/odbl/1-0/", color = Copper, style = MaterialTheme.typography.bodySmall)
            }
        }
        Spacer(Modifier.height(12.dp))
        Card(shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = Paper)) {
            Column(Modifier.padding(18.dp)) {
                Text("Wikimedia Commons", color = Ink, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                Text("Für die Gerichtebildersuche werden Medien von Wikimedia Commons verwendet. Lizenz und Urheberangaben unterscheiden sich je Datei und werden deshalb direkt beim jeweiligen Bild angezeigt und als Quellenbeleg gespeichert.", color = MutedInk, style = MaterialTheme.typography.bodySmall)
                Spacer(Modifier.height(8.dp))
                Text("Website: commons.wikimedia.org", color = Copper, style = MaterialTheme.typography.bodySmall)
            }
        }
        Spacer(Modifier.height(12.dp))
        Card(shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = Paper)) {
            Column(Modifier.padding(18.dp)) {
                Text("Weitere Mitwirkende", color = Ink, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                Text("Diese Seite wird ergänzt, sobald weitere Datenquellen, Bibliotheken oder Personen zur Entwicklung beitragen und einer namentlichen Nennung zugestimmt haben.", color = MutedInk, style = MaterialTheme.typography.bodySmall)
            }
        }
        Spacer(Modifier.height(20.dp))
        Text("Topf Secret ist ein unabhängiges Projekt. Die Nennung einer Quelle bedeutet keine Empfehlung oder Unterstützung durch den jeweiligen Rechteinhaber.", color = MutedInk, style = MaterialTheme.typography.bodySmall)
    }
}
