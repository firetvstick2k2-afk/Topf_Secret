package de.topfsecret.app

import android.os.Bundle
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

private val Cream = Color(0xFFFFF8F0)
private val Paper = Color(0xFFFFFDF9)
private val Ink = Color(0xFF302019)
private val MutedInk = Color(0xFF806E62)
private val Copper = Color(0xFFE87524)
private val PaleCopper = Color(0xFFFFE6D2)
private val Sage = Color(0xFFE8EEE2)

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
        setContent { TopfSecretTheme { ActivationGate { TopfSecretApp(repository) } } }
    }
}

@Composable
private fun TopfSecretTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = Copper, onPrimary = Color.White, secondary = Color(0xFF8D4B2A),
            background = Cream, surface = Paper, onSurface = Ink, onBackground = Ink
        ),
        content = content
    )
}

@Composable
private fun TopfSecretApp(repository: RecipeRepository) {
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    var selectedRecipeId by rememberSaveable { mutableLongStateOf(0L) }
    var editingRecipeId by rememberSaveable { mutableLongStateOf(0L) }
    val recipes by repository.observeRecipes().collectAsState(initial = emptyList())
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
                0 -> HomeScreen(recipes.size, { selectedTab = 1 }, { selectedTab = 2 })
                1 -> {
                    if (editingRecipeId != 0L) {
                        val editing = recipes.firstOrNull { it.recipe.id == editingRecipeId }
                        RecipeEditorScreen(
                            recipe = editing,
                            onCancel = { editingRecipeId = 0L },
                            onSave = { entity, ingredients, steps ->
                                scope.launch {
                                    val newId = repository.saveRecipe(entity, ingredients, steps)
                                    editingRecipeId = 0L
                                    selectedRecipeId = newId
                                }
                            }
                        )
                    } else if (selectedRecipeId != 0L) {
                        val selected = recipes.firstOrNull { it.recipe.id == selectedRecipeId }
                        if (selected == null) {
                            RecipeListScreen(recipes, { editingRecipeId = -1L }, { selectedRecipeId = it })
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
                        RecipeListScreen(recipes, { editingRecipeId = -1L }, { selectedRecipeId = it })
                    }
                }
                2 -> PlaceholderScreen("GUT SORTIERT", "Kategorien", "Ob Frühstück, Hauptgericht oder etwas Süßes – bald findet ihr alles mit wenigen Fingertipps.", "🥕", "Die Kategorien werden als Nächstes mit euren gespeicherten Rezepten verbunden.")
                else -> PlaceholderScreen("FÜR SPÄTER", "Eure Favoriten", "Die Rezepte, die ihr immer wieder kochen möchtet, sammeln wir hier.", "♡", "Favoriten könnt ihr bereits in der Rezeptansicht markieren.")
            }
        }
    }
}

@Composable
private fun HomeScreen(recipeCount: Int, onRecipesClick: () -> Unit, onCategoriesClick: () -> Unit) {
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 22.dp, vertical = 18.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Image(painterResource(R.drawable.topf_secret_logo), "Topf Secret", Modifier.size(76.dp))
            Spacer(Modifier.width(14.dp))
            Column {
                Text("TOPF SECRET", color = Copper, fontSize = 13.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                Text("Euer Kochbuch", color = Ink, fontSize = 23.sp, fontWeight = FontWeight.Bold)
                Text("Deine Rezepte. Eure Küche.", color = MutedInk, fontSize = 13.sp)
            }
        }
        Spacer(Modifier.height(28.dp))
        Text("Schön, dass ihr da seid! 👋", color = MutedInk, style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(7.dp))
        Text("Was kochen wir heute?", color = Ink, fontSize = 29.sp, lineHeight = 35.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(10.dp))
        Text("Eure Rezeptsammlung für gemeinsame Lieblingsmomente wächst hier Schritt für Schritt.", color = MutedInk, style = MaterialTheme.typography.bodyLarge, lineHeight = 24.sp)
        Spacer(Modifier.height(24.dp))
        Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = Ink)) {
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
private fun RecipeListScreen(recipes: List<RecipeWithDetails>, onAdd: () -> Unit, onOpen: (Long) -> Unit) {
    Scaffold(
        containerColor = Cream,
        floatingActionButton = {
            FloatingActionButton(onClick = onAdd, containerColor = Copper, contentColor = Color.White) { Text("+", fontSize = 28.sp) }
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(20.dp)) {
            Text("DEINE SAMMLUNG", color = Copper, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
            Spacer(Modifier.height(6.dp))
            Text("Alle Rezepte", color = Ink, fontSize = 28.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(16.dp))
            if (recipes.isEmpty()) {
                Card(shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = Paper)) {
                    Column(Modifier.fillMaxWidth().padding(22.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🍲", fontSize = 48.sp)
                        Spacer(Modifier.height(10.dp))
                        Text("Der Topf ist noch leer", color = Ink, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(6.dp))
                        Text("Legt euer erstes Lieblingsrezept an. Über das Plus geht's los!", color = MutedInk, textAlign = TextAlign.Center)
                    }
                }
            } else recipes.forEach { item ->
                Card(onClick = { onOpen(item.recipe.id) }, modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp), shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = Paper)) {
                    Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(if (item.recipe.isFavorite) "❤️" else "🍽️", fontSize = 27.sp)
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
private fun RecipeEditorScreen(recipe: RecipeWithDetails?, onCancel: () -> Unit, onSave: (RecipeEntity, List<String>, List<String>) -> Unit) {
    val original = recipe?.recipe
    var title by rememberSaveable(original?.id) { mutableStateOf(original?.title.orEmpty()) }
    var description by rememberSaveable(original?.id) { mutableStateOf(original?.description.orEmpty()) }
    var category by rememberSaveable(original?.id) { mutableStateOf(original?.category.orEmpty()) }
    var servings by rememberSaveable(original?.id) { mutableStateOf((original?.servings ?: 2).toString()) }
    var ingredients by rememberSaveable(original?.id) { mutableStateOf(recipe?.ingredients?.sortedBy { it.position }?.joinToString("\n") { it.name }.orEmpty()) }
    var steps by rememberSaveable(original?.id) { mutableStateOf(recipe?.steps?.sortedBy { it.position }?.joinToString("\n") { it.instruction }.orEmpty()) }
    var error by rememberSaveable(original?.id) { mutableStateOf(false) }

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
        OutlinedTextField(description, { description = it }, Modifier.fillMaxWidth(), label = { Text("Kurze Beschreibung") }, minLines = 2)
        Spacer(Modifier.height(10.dp))
        OutlinedTextField(ingredients, { ingredients = it }, Modifier.fillMaxWidth(), label = { Text("Zutaten – eine pro Zeile") }, placeholder = { Text("z. B. 500 g Kartoffeln\n1 Zwiebel") }, minLines = 4)
        Spacer(Modifier.height(10.dp))
        OutlinedTextField(steps, { steps = it }, Modifier.fillMaxWidth(), label = { Text("Zubereitung – ein Schritt pro Zeile") }, placeholder = { Text("Kartoffeln schälen und würfeln.\nZwiebel glasig anbraten.") }, minLines = 4)
        if (error) {
            Spacer(Modifier.height(6.dp))
            Text("Bitte gib dem Rezept mindestens einen Namen.", color = Color(0xFFB3261E))
        }
        Spacer(Modifier.height(18.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            TextButton(onClick = onCancel) { Text("Abbrechen", color = MutedInk) }
            Button(onClick = {
                if (title.isBlank()) error = true
                else {
                    val entity = (original ?: RecipeEntity(title = title.trim())).copy(
                        title = title.trim(), description = description.trim(), category = category.trim(),
                        servings = servings.toIntOrNull()?.coerceAtLeast(1) ?: 2,
                        updatedAt = System.currentTimeMillis()
                    )
                    onSave(entity, ingredients.lines(), steps.lines())
                }
            }) { Text("Rezept speichern") }
        }
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
        Spacer(Modifier.height(8.dp))
        Text(recipe.recipe.servings.toString() + " Portionen", color = MutedInk)
        if (recipe.recipe.description.isNotBlank()) {
            Spacer(Modifier.height(14.dp))
            Text(recipe.recipe.description, color = Ink, style = MaterialTheme.typography.bodyLarge)
        }
        Spacer(Modifier.height(22.dp))
        Text("Zutaten", color = Ink, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        if (recipe.ingredients.isEmpty()) Text("Noch keine Zutaten eingetragen.", color = MutedInk)
        else recipe.ingredients.sortedBy { it.position }.forEach { Text("• " + it.name, color = Ink, modifier = Modifier.padding(vertical = 3.dp)) }
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
