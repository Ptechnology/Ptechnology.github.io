package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.RankingEntry
import com.example.game.GameState
import com.example.game.GameViewModel
import com.example.game.ScreenState
import com.example.ui.theme.BibleGoldAccent
import com.example.ui.theme.BibleGoldDark
import com.example.ui.theme.BibleGoldLight
import com.example.ui.theme.BibleNavyDark
import com.example.ui.theme.BibleNavyPrimary
import com.example.ui.theme.BibleParchmentBorder

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RankingsScreen(
    viewModel: GameViewModel,
    gameState: GameState
) {
    val topRankings by viewModel.topRankings.collectAsState()
    val allRankings by viewModel.allRankings.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Top 10, 1: Todos

    BackHandler {
        viewModel.navigateTo(ScreenState.HOME)
    }

    // Calcula a posição do jogador atual no ranking geral
    val recentRankingId = gameState.lastSavedRankingId
    val currentPosition = if (recentRankingId != null) {
        val idx = allRankings.indexOfFirst { it.id.toLong() == recentRankingId }
        if (idx >= 0) idx + 1 else gameState.lastPlayerPosition
    } else gameState.lastPlayerPosition

    val currentRankingEntry = allRankings.find { it.id.toLong() == recentRankingId }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = null,
                            tint = BibleGoldLight,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Rankings Bíblicos", fontWeight = FontWeight.Bold)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateTo(ScreenState.HOME) }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Início"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BibleNavyPrimary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(4.dp))

                // Card de Destaque da Posição do Jogador Atual
                // REGRA: "Caso o jogador tenha perdido o jogo, sua posição deve aparecer na tela de rankings,
                // mesmo que não tenha ficado entre o top 10, mostrando sua posição, seu nome e sua pontuação no jogo atual."
                if (currentPosition != null) {
                    val playerName = currentRankingEntry?.playerName ?: gameState.playerName.ifBlank { "Você" }
                    val playerScore = currentRankingEntry?.score ?: gameState.score
                    val isTop10 = currentPosition <= 10

                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isTop10) Color(0xFFFEF3C7) else BibleNavyDark
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                width = 2.dp,
                                color = BibleGoldAccent,
                                shape = RoundedCornerShape(16.dp)
                            )
                            .testTag("current_player_rank_card")
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(CircleShape)
                                            .background(BibleGoldAccent),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "#$currentPosition",
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color.White,
                                            fontSize = 16.sp
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = "Sua Posição no Jogo Atual",
                                            style = MaterialTheme.typography.labelMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = if (isTop10) BibleGoldDark else BibleGoldLight
                                            )
                                        )
                                        Text(
                                            text = playerName,
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = if (isTop10) BibleNavyDark else Color.White
                                            )
                                        )
                                    }
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "$playerScore pts",
                                        style = MaterialTheme.typography.headlineSmall.copy(
                                            fontWeight = FontWeight.ExtraBold,
                                            color = if (isTop10) BibleNavyDark else BibleGoldLight
                                        )
                                    )
                                    Text(
                                        text = if (isTop10) "No Top 10!" else "Registrado na planilha",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = if (isTop10) Color(0xFF92400E) else Color.LightGray
                                        )
                                    )
                                }
                            }

                            if (!isTop10) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Mesmo fora do Top 10, sua posição (#$currentPosition) foi salva no banco de dados da planilha Google!",
                                    style = MaterialTheme.typography.bodySmall.copy(color = Color.White.copy(alpha = 0.85f))
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            // Abas de visualização: Top 10 e Todos os Jogos
            item {
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = BibleNavyPrimary,
                    modifier = Modifier.clip(RoundedCornerShape(12.dp))
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("Top 10 Melhores", fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("Todos os Rankings (${allRankings.size})", fontWeight = FontWeight.Bold) }
                    )
                }
            }

            val listToDisplay = if (selectedTab == 0) topRankings else allRankings

            if (listToDisplay.isEmpty()) {
                item {
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_scroll),
                                contentDescription = null,
                                tint = BibleGoldAccent,
                                modifier = Modifier.size(40.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Nenhuma pontuação gravada ainda",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = "Jogue uma partida solo ou multi-jogador para registrar seu nome no ranking!",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                itemsIndexed(listToDisplay) { index, entry ->
                    val rankNumber = index + 1
                    val isCurrentPlayerEntry = entry.id.toLong() == recentRankingId

                    RankingItemCard(
                        rankNumber = rankNumber,
                        entry = entry,
                        isCurrentPlayer = isCurrentPlayerEntry
                    )
                }
            }

            // Botões de Ação Inferiores
            item {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = { viewModel.navigateTo(ScreenState.HOME) },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .defaultMinSize(minHeight = 56.dp)
                            .testTag("rankings_home_button"),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Home, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Tela Inicial", fontWeight = FontWeight.SemiBold)
                    }

                    Button(
                        onClick = { viewModel.startGame() },
                        colors = ButtonDefaults.buttonColors(containerColor = BibleNavyPrimary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .defaultMinSize(minHeight = 56.dp)
                            .testTag("rankings_play_again_button"),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Jogar Novamente", fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun RankingItemCard(
    rankNumber: Int,
    entry: RankingEntry,
    isCurrentPlayer: Boolean
) {
    val (badgeBg, badgeTextColor) = when (rankNumber) {
        1 -> Pair(Color(0xFFFBBF24), Color(0xFF78350F)) // Ouro
        2 -> Pair(Color(0xFF94A3B8), Color(0xFF1E293B)) // Prata
        3 -> Pair(Color(0xFFD97706), Color.White)        // Bronze
        else -> Pair(Color(0xFFE2E8F0), Color(0xFF334155))
    }

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isCurrentPlayer) Color(0xFFEFF6FF) else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isCurrentPlayer) 3.dp else 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = if (isCurrentPlayer) 2.dp else 1.dp,
                color = if (isCurrentPlayer) BibleNavyPrimary else BibleParchmentBorder,
                shape = RoundedCornerShape(12.dp)
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Distintivo de Posição
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(badgeBg),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "#$rankNumber",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = badgeTextColor
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Informações do Registro conforme planilha:
            // Data, hora, nome, pontuação, tipo de jogo, equipe, jogadores, posição
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = entry.playerName,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleSmall
                    )
                    if (isCurrentPlayer) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .background(BibleNavyPrimary, RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "Você",
                                fontSize = 10.sp,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "${entry.gameMode} • ${entry.correctWords} acertos",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (entry.teamName != "-" && entry.teamName.isNotBlank()) {
                        Text(
                            text = "(${entry.teamName})",
                            style = MaterialTheme.typography.bodySmall,
                            color = BibleGoldDark
                        )
                    }
                }

                if (entry.playerNames != "-" && entry.playerNames.isNotBlank() && entry.gameMode.contains("Multi", ignoreCase = true)) {
                    Text(
                        text = "Jogadores: ${entry.playerNames}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Text(
                    text = "${entry.date} ${entry.time}",
                    fontSize = 10.sp,
                    color = Color.Gray
                )
            }

            // Pontuação
            Text(
                text = "${entry.score} pts",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = BibleNavyPrimary
                )
            )
        }
    }
}
