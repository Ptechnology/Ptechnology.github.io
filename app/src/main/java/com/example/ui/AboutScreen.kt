package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Rule
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.game.GameViewModel
import com.example.game.ScreenState
import com.example.ui.theme.BibleGoldAccent
import com.example.ui.theme.BibleGoldLight
import com.example.ui.theme.BibleNavyDark
import com.example.ui.theme.BibleNavyPrimary
import com.example.ui.theme.BibleOlive
import com.example.ui.theme.BibleParchmentBorder

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(
    viewModel: GameViewModel
) {
    val scrollState = rememberScrollState()

    BackHandler {
        viewModel.navigateTo(ScreenState.HOME)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_bible),
                            contentDescription = "Bíblia Sagrada",
                            tint = BibleGoldLight,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Sobre o Aplicativo", fontWeight = FontWeight.Bold)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateTo(ScreenState.HOME) }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Voltar"
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
            // Emblema Central de Bíblia Sagrada (Sem cruz)
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = BibleNavyDark),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.5.dp, BibleGoldAccent, RoundedCornerShape(20.dp))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(BibleNavyPrimary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_bible),
                            contentDescription = "Escrituras Sagradas",
                            tint = BibleGoldLight,
                            modifier = Modifier.size(44.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Jogo das Palavras Bíblicas",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        ),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Versão 1.0",
                        style = MaterialTheme.typography.bodySmall.copy(color = BibleGoldLight)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Card com as Informações e Textos Oficiais Requisitados
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, BibleParchmentBorder, RoundedCornerShape(16.dp))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Desenvolvedor e Data
                    InfoItemRow(
                        icon = Icons.Default.Person,
                        title = "Desenvolvimento",
                        content = "Aplicativo desenvolvido por Marcelo Palladino, em 27/09/2026."
                    )

                    // Objetivo do Aplicativo
                    InfoItemRow(
                        icon = Icons.Default.Info,
                        title = "Objetivo",
                        content = "Memorizar palavras bíblicas, aumentando seu conhecimento da Bíblia Sagrada."
                    )

                    // Fontes e Tradução da Bíblia Sagrada
                    InfoItemRow(
                        icon = Icons.Default.MenuBook,
                        title = "Base Bíblica e Fontes",
                        content = "Todas as palavras têm como base a Tradução do Novo Mundo da Bíblia Sagrada, versão de 2015 em Português do Brasil, e as questões foram extraídas das séries de \"Palavras Cruzadas\" de números da revista Despertai! das Testemunhas de Jeová."
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Resumo das Regras do Jogo
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, BibleParchmentBorder, RoundedCornerShape(16.dp))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Rule,
                            contentDescription = null,
                            tint = BibleOlive,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Regras do Jogo",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "• Letras Ocultas: palavras de até 5 caracteres revelam 1 letra; de até 10 caracteres revelam 2 letras; mais de 10 caracteres revelam 3 letras.\n\n" +
                                "• Tempo Limite: 60 segundos por tentativa. O término do tempo impede novas tentativas e considera a resposta incorreta (-1 vida).\n\n" +
                                "• Pontuação: acerto dentro do tempo rende 10 pontos + os segundos restantes de bônus.\n\n" +
                                "• Vidas: o jogador inicia com 3 vidas. A cada 20 acertos, ganha 1 vida extra (até o limite de 5 vidas).\n\n" +
                                "• Sem repetições: as palavras não são repetidas durante um mesmo jogo; o jogo termina ao serem utilizadas todas as palavras cadastradas.\n\n" +
                                "• Multi-jogador: código alfanumérico de 5 caracteres para convidar amigos, com divisão automática em até 4 equipes de 5 componentes cada. As questões são alternadas entre as equipes para todos responderem o mesmo número de perguntas.\n\n" +
                                "• Ranking: rankings registrados na tela 'Rankings', exibindo o TOP-10 e também a posição conquistada no último jogo, mesmo não estando entre o 10 melhores.",
                        style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 20.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = { viewModel.navigateTo(ScreenState.HOME) },
                colors = ButtonDefaults.buttonColors(containerColor = BibleNavyPrimary),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("about_back_home_button")
            ) {
                Text("Voltar à Tela Inicial", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun InfoItemRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    content: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(BibleNavyPrimary.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = BibleNavyPrimary,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = BibleNavyPrimary
                )
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = content,
                style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 20.sp),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
