package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.game.GameMode
import com.example.game.GameState
import com.example.game.GameViewModel
import com.example.game.ScreenState
import com.example.ui.theme.BibleGoldAccent
import com.example.ui.theme.BibleGoldLight
import com.example.ui.theme.BibleNavyDark
import com.example.ui.theme.BibleNavyPrimary
import com.example.ui.theme.BibleParchmentBorder

@Composable
fun HomeScreen(
    viewModel: GameViewModel,
    gameState: GameState
) {
    val scrollState = rememberScrollState()
    var nameError by remember { mutableStateOf(false) }
    var joinRoomCode by remember { mutableStateOf("") }
    var codeError by remember { mutableStateOf(false) }
    var showJoinInput by remember { mutableStateOf(false) }

    // Consulta a planilha cada vez que a tela inicial for acessada para atualizar a relação de palavras
    LaunchedEffect(Unit) {
        viewModel.refreshWordsFromSpreadsheet()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState)
            .padding(bottom = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Banner e Cabeçalho Decorativo com Bíblia Sagrada
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.banner_game_wood_1790775278891),
                contentDescription = "Palavras Bíblicas",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            // Gradiente suave sobre o banner
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                BibleNavyDark.copy(alpha = 0.85f)
                            )
                        )
                    )
            )

            // Título e Ícone de Bíblia Sagrada
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_bible),
                        contentDescription = "Bíblia Sagrada",
                        tint = BibleGoldLight,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Jogo das Palavras Bíblicas",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        ),
                        textAlign = TextAlign.Center
                    )
                }

                Text(
                    text = "Memorização baseada na Tradução do Novo Mundo (2015)",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = BibleGoldLight.copy(alpha = 0.9f)
                    ),
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Card Principal com Configurações da Partida
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Campo Obrigatório de Nome do Jogador
                Text(
                    text = "Nome do Jogador (Obrigatório)",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )

                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = gameState.playerName,
                    onValueChange = {
                        viewModel.setPlayerName(it)
                        if (it.isNotBlank()) nameError = false
                    },
                    placeholder = { Text("Digite seu nome ou apelido") },
                    singleLine = true,
                    isError = nameError,
                    supportingText = {
                        if (nameError) {
                            Text("O nome do jogador é obrigatório para continuar.", color = MaterialTheme.colorScheme.error)
                        }
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = BibleNavyPrimary
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("player_name_input"),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Escolha do Modo de Jogo: Solo ou Multi-jogador
                Text(
                    text = "Modo de Jogo",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Botão Modo Solo
                    ModeSelectCard(
                        title = "Solo",
                        description = "3 vidas • 60s",
                        icon = Icons.Default.Person,
                        isSelected = gameState.gameMode == GameMode.SOLO,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("mode_solo_button"),
                        onClick = { viewModel.setGameMode(GameMode.SOLO) }
                    )

                    // Botão Modo Multi-jogador
                    ModeSelectCard(
                        title = "Multi-jogador",
                        description = "Equipes e código",
                        icon = Icons.Default.Group,
                        isSelected = gameState.gameMode == GameMode.MULTIPLAYER,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("mode_multiplayer_button"),
                        onClick = { viewModel.setGameMode(GameMode.MULTIPLAYER) }
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Seção Dinâmica dependendo do Modo Escolhido
                if (gameState.gameMode == GameMode.SOLO) {
                    Button(
                        onClick = {
                            if (gameState.playerName.isBlank()) {
                                nameError = true
                            } else {
                                viewModel.startGame()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BibleNavyPrimary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("start_solo_game_button")
                    ) {
                        Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Começar Partida Solo",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                } else {
                    // Modo Multi-jogador: Criar ou Entrar em Sala
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Botão Criar Sala (Gera código de 5 dígitos alfanuméricos)
                        Button(
                            onClick = {
                                if (gameState.playerName.isBlank()) {
                                    nameError = true
                                } else {
                                    viewModel.createMultiplayerRoom()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = BibleNavyPrimary),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("create_room_button")
                        ) {
                            Icon(painter = painterResource(id = R.drawable.ic_scroll), contentDescription = null, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Criar Nova Sala (Gerar Código)", fontWeight = FontWeight.Bold)
                        }

                        // Botão Alternar para Entrar em Sala Existente
                        OutlinedButton(
                            onClick = { showJoinInput = !showJoinInput },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("join_room_toggle_button")
                        ) {
                            Text(if (showJoinInput) "Ocultar Campo de Código" else "Tenho um Código de Sala")
                        }

                        AnimatedVisibility(visible = showJoinInput) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .padding(12.dp)
                            ) {
                                Text(
                                    text = "Digite o código alfanumérico de 5 caracteres:",
                                    style = MaterialTheme.typography.bodySmall
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedTextField(
                                        value = joinRoomCode,
                                        onValueChange = {
                                            if (it.length <= 5) joinRoomCode = it.uppercase()
                                            codeError = false
                                        },
                                        placeholder = { Text("Ex: B7K9X") },
                                        singleLine = true,
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("room_code_input"),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    Button(
                                        onClick = {
                                            if (gameState.playerName.isBlank()) {
                                                nameError = true
                                            } else if (joinRoomCode.length < 5) {
                                                codeError = true
                                            } else {
                                                viewModel.setRoomCode(joinRoomCode)
                                                viewModel.joinMultiplayerRoom(joinRoomCode)
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = BibleGoldAccent),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.testTag("join_room_confirm_button")
                                    ) {
                                        Text("Entrar", fontWeight = FontWeight.Bold, color = Color.White)
                                    }
                                }
                                if (codeError) {
                                    Text(
                                        text = "O código deve conter 5 caracteres alfanuméricos.",
                                        color = MaterialTheme.colorScheme.error,
                                        style = MaterialTheme.typography.bodySmall,
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Seção de Ações Secundárias: Rankings e Sobre
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Botão Rankings
            Card(
                modifier = Modifier
                    .weight(1f)
                    .clickable { viewModel.navigateTo(ScreenState.RANKINGS) }
                    .testTag("nav_rankings_button"),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFEF3C7)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Leaderboard,
                            contentDescription = "Rankings",
                            tint = BibleGoldAccent
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Rankings",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleSmall
                        )
                        Text(
                            text = "Top 10 e sua posição",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Botão Sobre
            Card(
                modifier = Modifier
                    .weight(1f)
                    .clickable { viewModel.navigateTo(ScreenState.ABOUT) }
                    .testTag("nav_about_button"),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFDBEAFE)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_bible),
                            contentDescription = "Sobre",
                            tint = BibleNavyPrimary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Sobre",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleSmall
                        )
                        Text(
                            text = "Créditos e fontes",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ModeSelectCard(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .clickable { onClick() }
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) BibleNavyPrimary else BibleParchmentBorder,
                shape = RoundedCornerShape(12.dp)
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFFEFF6FF) else MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) BibleNavyPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = title,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) BibleNavyPrimary else MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = description,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
