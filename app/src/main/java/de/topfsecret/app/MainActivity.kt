package de.topfsecret.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Cream = Color(0xFFFFF8F0)
private val Paper = Color(0xFFFFFDF9)
private val Ink = Color(0xFF302019)
private val MutedInk = Color(0xFF806E62)
private val Copper = Color(0xFFE87524)
private val PaleCopper = Color(0xFFFFE6D2)
private val Sage = Color(0xFFE8EEE2)

private data class AppDestination(
    val label: String,
    val symbol: String
)

private val destinations = listOf(
    AppDestination("Start", "⌂"),
    AppDestination("Rezepte", "▤"),
    AppDestination("Kategorien", "▦"),
    AppDestination("Favoriten", "♡")
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TopfSecretTheme {
                TopfSecretApp()
            }
        }
    }
}

@Composable
private fun TopfSecretTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = Copper,
            onPrimary = Color.White,
            secondary = Color(0xFF8D4B2A),
            background = Cream,
            surface = Paper,
            onSurface = Ink,
            onBackground = Ink
        ),
        typography = MaterialTheme.typography,
        content = content
    )
}

@Composable
private fun TopfSecretApp() {
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }

    Scaffold(
        containerColor = Cream,
        bottomBar = {
            NavigationBar(
                containerColor = Paper,
                contentColor = Ink
            ) {
                destinations.forEachIndexed { index, destination ->
                    NavigationBarItem(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        icon = {
                            Text(
                                text = destination.symbol,
                                fontSize = 23.sp,
                                fontWeight = FontWeight.Medium
                            )
                        },
                        label = { Text(destination.label) }
                    )
                }
            }
        }
    ) { innerPadding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            color = Cream
        ) {
            when (selectedTab) {
                0 -> HomeScreen(
                    onRecipesClick = { selectedTab = 1 },
                    onCategoriesClick = { selectedTab = 2 }
                )
                1 -> PlaceholderScreen(
                    eyebrow = "DEINE SAMMLUNG",
                    title = "Alle Rezepte",
                    description = "Hier bekommt jedes eurer Lieblingsgerichte seinen eigenen Platz.",
                    symbol = "📖",
                    detail = "Die Rezeptverwaltung bauen wir als Nächstes."
                )
                2 -> PlaceholderScreen(
                    eyebrow = "GUT SORTIERT",
                    title = "Kategorien",
                    description = "Ob Frühstück, Hauptgericht oder etwas Süßes – bald findet ihr alles mit wenigen Fingertipps.",
                    symbol = "🥕",
                    detail = "Die Kategorien werden mit der Rezeptdatenbank verknüpft."
                )
                else -> PlaceholderScreen(
                    eyebrow = "FÜR SPÄTER",
                    title = "Eure Favoriten",
                    description = "Die Rezepte, die ihr immer wieder kochen möchtet, sammeln wir hier.",
                    symbol = "♡",
                    detail = "Favoriten könnt ihr markieren, sobald Rezepte angelegt werden."
                )
            }
        }
    }
}

@Composable
private fun HomeScreen(
    onRecipesClick: () -> Unit,
    onCategoriesClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 22.dp, vertical = 18.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(R.drawable.topf_secret_logo),
                contentDescription = "Topf Secret",
                modifier = Modifier.size(76.dp)
            )
            Spacer(Modifier.width(14.dp))
            Column {
                Text(
                    text = "TOPF SECRET",
                    color = Copper,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )
                Text(
                    text = "Euer Kochbuch",
                    color = Ink,
                    fontSize = 23.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Deine Rezepte. Eure Küche.",
                    color = MutedInk,
                    fontSize = 13.sp
                )
            }
        }

        Spacer(Modifier.height(28.dp))

        Text(
            text = "Schön, dass ihr da seid! 👋",
            color = MutedInk,
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(Modifier.height(7.dp))
        Text(
            text = "Was kochen wir heute?",
            color = Ink,
            fontSize = 29.sp,
            lineHeight = 35.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(10.dp))
        Text(
            text = "Eure Rezeptsammlung für gemeinsame Lieblingsmomente wächst hier Schritt für Schritt.",
            color = MutedInk,
            style = MaterialTheme.typography.bodyLarge,
            lineHeight = 24.sp
        )

        Spacer(Modifier.height(24.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Ink)
        ) {
            Column(modifier = Modifier.padding(22.dp)) {
                Text(
                    text = "GUTEN APPETIT!",
                    color = Color(0xFFFFB879),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Eure Küche.
Eure Regeln.",
                    color = Color.White,
                    fontSize = 27.sp,
                    lineHeight = 32.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Ein Ort für die Rezepte, die euch schmecken und verbinden.",
                    color = Color(0xFFFFE8D8),
                    style = MaterialTheme.typography.bodyMedium,
                    lineHeight = 21.sp
                )
            }
        }

        Spacer(Modifier.height(26.dp))
        Text(
            text = "Auf einen Blick",
            color = Ink,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(13.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            HomeActionCard(
                modifier = Modifier.weight(1f),
                symbol = "📖",
                title = "Rezepte",
                subtitle = "Eure Sammlung",
                background = PaleCopper,
                onClick = onRecipesClick
            )
            HomeActionCard(
                modifier = Modifier.weight(1f),
                symbol = "🥕",
                title = "Kategorien",
                subtitle = "Alles im Blick",
                background = Sage,
                onClick = onCategoriesClick
            )
        }

        Spacer(Modifier.height(22.dp))
        Text(
            text = "Hier entsteht euer digitales Kochbuch.",
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            color = MutedInk,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
private fun HomeActionCard(
    modifier: Modifier = Modifier,
    symbol: String,
    title: String,
    subtitle: String,
    background: Color,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(text = symbol, fontSize = 27.sp)
            Spacer(Modifier.height(12.dp))
            Text(
                text = title,
                color = Ink,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(Modifier.height(3.dp))
            Text(
                text = subtitle,
                color = MutedInk,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun PlaceholderScreen(
    eyebrow: String,
    title: String,
    description: String,
    symbol: String,
    detail: String
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = eyebrow,
            color = Copper,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.sp
        )
        Spacer(Modifier.height(18.dp))
        Text(text = symbol, fontSize = 58.sp)
        Spacer(Modifier.height(14.dp))
        Text(
            text = title,
            color = Ink,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(10.dp))
        Text(
            text = description,
            color = MutedInk,
            style = MaterialTheme.typography.bodyLarge,
            lineHeight = 25.sp,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(22.dp))
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = PaleCopper)
        ) {
            Text(
                text = detail,
                modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp),
                color = Ink,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
        }
    }
}
