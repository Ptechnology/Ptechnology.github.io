package com.example.ui

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.game.GameMode
import com.example.game.GameState
import com.example.game.GameViewModel
import com.example.game.ScreenState
import com.example.game.WordUtils
import com.example.ui.theme.BibleCrimson
import com.example.ui.theme.BibleGoldAccent
import com.example.ui.theme.BibleGoldLight
import com.example.ui.theme.BibleNavyPrimary
import com.example.ui.theme.BibleParchmentBorder

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun GamePlayScreen(
    viewModel: GameViewModel,
    gameState: GameState
) {
    var wordGuessInput by remember { mutableStateOf("") }
    var showQuitDialog by remember { mutableStateOf(false) }
    var showSkipDialog by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()
    val context = LocalContext.current

    val currentWord = gameState.currentWord
    val wordString = currentWord?.word ?: ""
    val isInputEnabled = !gameState.isTimeExpired && gameState.timeRemaining > 0 && !gameState.isGameOver

    BackHandler {
        showQuitDialog = true
    }

    // Modal de Feedback de Resposta (Acerto, Erro, Fim de Jogo, Tempo Esgotado)
    gameState.answerFeedback?.let { feedback ->
        AppFeedbackDialog(
            feedback = feedback,
            isGameOver = gameState.isGameOver,
            correctWord = currentWord?.word,
            onDismiss = {
                wordGuessInput = ""
                viewModel.dismissFeedbackAndNextWord()
            }
        )
    }

    // Modal de Confirmação para Desistir
    if (showQuitDialog) {
        AppConfirmationDialog(
            title = "Deseja sair da partida?",
            message = "Sua pontuação atual (${gameState.score} pts) será registrada no ranking do aplicativo.",
            confirmText = "Sair e Salvar",
            dismissText = "Continuar Jogando",
            onConfirm = {
                showQuitDialog = false
                viewModel.navigateTo(ScreenState.RANKINGS)
            },
            onDismiss = { showQuitDialog = false }
        )
    }

    // Modal de Confirmação para Pular Palavra
    if (showSkipDialog) {
        AppConfirmationDialog(
            title = "Pular esta pergunta?",
            message = "Pular a palavra custará 1 vida das suas vidas restantes.",
            confirmText = "Pular (-1 vida)",
            dismissText = "Tentar memorizar",
            onConfirm = {
                showSkipDialog = false
                viewModel.skipWordWithPenalty()
            },
            onDismiss = { showSkipDialog = false }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Palavras Bíblicas",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            if (gameState.gameMode == GameMode.MULTIPLAYER && gameState.currentTeam != null) {
                                Text(
                                    text = "Vez de: ${gameState.currentTeam?.name} (Rodada ${gameState.currentRoundNumber})",
                                    style = MaterialTheme.typography.bodySmall.copy(color = BibleGoldLight)
                                )
                            } else {
                                Text(
                                    text = "Palavra #${gameState.currentWordIndex + 1} de ${gameState.allWords.size}",
                                    style = MaterialTheme.typography.bodySmall.copy(color = BibleGoldLight)
                                )
                            }
                        }

                        // Pontuação Atual
                        Card(
                            colors = CardDefaults.cardColors(containerColor = BibleGoldAccent),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "${gameState.score} pts",
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                fontSize = 14.sp
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { showQuitDialog = true }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Desistir da partida"
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 680.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
            // Barra Superior de Status: Vidas (Corações) e Timer (60s)
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Vidas (3 a 5 vidas, com bônus de +1 vida a cada 20 acertos)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Vidas: ",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            (1..5).forEach { index ->
                                Icon(
                                    imageVector = if (index <= gameState.lives) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                    contentDescription = null,
                                    tint = if (index <= gameState.lives) BibleCrimson else Color.Gray.copy(alpha = 0.4f),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                            }
                        }

                        // Acertos e Sequência
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_bible),
                                contentDescription = null,
                                tint = BibleNavyPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Acertos: ${gameState.correctAnswersCount}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Timer de 60s
                    val progress = (gameState.timeRemaining / 60f).coerceIn(0f, 1f)
                    val timerColor = when {
                        gameState.timeRemaining > 25 -> Color(0xFF16A34A)
                        gameState.timeRemaining > 10 -> BibleGoldAccent
                        else -> BibleCrimson
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Timer,
                                contentDescription = null,
                                tint = timerColor,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (gameState.timeRemaining > 0) {
                                    "${gameState.timeRemaining}s restantes"
                                } else {
                                    "0s - Tempo Esgotado! (Incorreto)"
                                },
                                fontWeight = FontWeight.Bold,
                                color = timerColor,
                                fontSize = 13.sp
                            )
                        }
                        Text(
                            text = if (gameState.timeRemaining > 0) "+10 + bônus tempo" else "Esgotado",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = timerColor,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Card da Pergunta / Dica (Coluna B da Planilha com textos bíblicos)
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, BibleParchmentBorder, RoundedCornerShape(18.dp))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_scroll),
                                contentDescription = null,
                                tint = BibleGoldAccent,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Dica das Escrituras",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = BibleNavyPrimary
                                )
                            )
                        }

                        if (currentWord?.category?.isNotBlank() == true) {
                            Box(
                                modifier = Modifier
                                    .background(
                                        color = BibleNavyPrimary.copy(alpha = 0.1f),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = currentWord.category,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = BibleNavyPrimary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Texto Completo da Dica da Coluna B
                    Text(
                        text = currentWord?.clue ?: "Carregando dica bíblica...",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontSize = 16.sp,
                            lineHeight = 22.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Exibição da Palavra Oculta (Masked Word)
            val letterCount = wordString.count { it.isLetter() }
            val revealedRuleText = when {
                letterCount <= 5 -> "$letterCount letras (1 revelada)"
                letterCount <= 10 -> "$letterCount letras (2 reveladas)"
                else -> "$letterCount letras (3 reveladas)"
            }

            Text(
                text = "PALAVRA OCULTA • $revealedRuleText",
                style = MaterialTheme.typography.labelSmall.copy(
                    letterSpacing = 1.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Letras da Palavra em Blocos Estilizados
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.Center,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                for (i in wordString.indices) {
                    val ch = wordString[i]
                    if (!ch.isLetter()) {
                        Box(
                            modifier = Modifier
                                .size(32.dp, 44.dp)
                                .padding(horizontal = 2.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = ch.toString(),
                                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    } else {
                        val normCh = WordUtils.normalizeText(ch.toString()).firstOrNull() ?: ch.uppercaseChar()
                        val isRevealed = i in gameState.revealedIndices || normCh in gameState.userGuessedChars

                        Box(
                            modifier = Modifier
                                .padding(horizontal = 3.dp)
                                .size(36.dp, 46.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (isRevealed) BibleNavyPrimary else MaterialTheme.colorScheme.surfaceVariant
                                )
                                .border(
                                    width = 1.5.dp,
                                    color = if (isRevealed) BibleGoldAccent else BibleParchmentBorder,
                                    shape = RoundedCornerShape(8.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (isRevealed) ch.toString() else "",
                                color = if (isRevealed) Color.White else MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 20.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Campo para informar a palavra correta
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Informe a palavra correta:",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = wordGuessInput,
                            onValueChange = { wordGuessInput = it.uppercase() },
                            placeholder = { Text("Digite a palavra") },
                            singleLine = true,
                            enabled = isInputEnabled,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("guess_word_input"),
                            shape = RoundedCornerShape(10.dp)
                        )

                        Button(
                            onClick = {
                                if (wordGuessInput.isNotBlank()) {
                                    viewModel.submitFullWordGuess(wordGuessInput)
                                }
                            },
                            enabled = isInputEnabled && wordGuessInput.isNotBlank(),
                            colors = ButtonDefaults.buttonColors(containerColor = BibleNavyPrimary),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .height(52.dp)
                                .testTag("confirm_guess_button")
                        ) {
                            Icon(imageVector = Icons.Default.Check, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Confirmar", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Teclado Virtual QWERTY para Palpite de Letra Avulsa
            Text(
                text = "Ou toque em uma letra:",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(6.dp))

            val qwertyRows = listOf(
                listOf('Q', 'W', 'E', 'R', 'T', 'Y', 'U', 'I', 'O', 'P'),
                listOf('A', 'S', 'D', 'F', 'G', 'H', 'J', 'K', 'L'),
                listOf('Z', 'X', 'C', 'V', 'B', 'N', 'M')
            )

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                qwertyRows.forEach { rowLetters ->
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        rowLetters.forEach { letter ->
                            val isUsed = gameState.userGuessedChars.contains(letter)
                            Box(
                                modifier = Modifier
                                    .padding(horizontal = 2.dp)
                                    .size(width = 33.dp, height = 40.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(
                                        if (isUsed || !isInputEnabled) Color.LightGray.copy(alpha = 0.4f) else MaterialTheme.colorScheme.surface
                                    )
                                    .border(
                                        width = 1.dp,
                                        color = if (isUsed || !isInputEnabled) Color.Transparent else BibleParchmentBorder,
                                        shape = RoundedCornerShape(6.dp)
                                    )
                                    .clickable(enabled = !isUsed && isInputEnabled) {
                                        viewModel.guessLetter(letter)
                                    }
                                    .testTag("key_$letter"),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = letter.toString(),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = if (isUsed || !isInputEnabled) Color.Gray else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Botão para Pular Palavra (Custa 1 vida)
            OutlinedButton(
                onClick = { showSkipDialog = true },
                enabled = isInputEnabled,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = BibleCrimson),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("skip_word_button")
            ) {
                Icon(imageVector = Icons.Default.SkipNext, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Pular esta pergunta (-1 vida)", fontWeight = FontWeight.SemiBold)
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Botão com link para acessar a Bíblia online no navegador padrão
            OutlinedButton(
                onClick = {
                    val bibleIntent = Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://www.jw.org/pt/biblioteca/biblia/nwt/livros/")
                    )
                    context.startActivity(bibleIntent)
                },
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = BibleNavyPrimary
                ),
                border = BorderStroke(1.dp, BibleGoldAccent.copy(alpha = 0.6f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("open_online_bible_button")
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_bible),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = BibleNavyPrimary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Acessar Bíblia no JW.ORG",
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                    contentDescription = "Abrir navegador",
                    modifier = Modifier.size(16.dp),
                    tint = BibleGoldAccent
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
