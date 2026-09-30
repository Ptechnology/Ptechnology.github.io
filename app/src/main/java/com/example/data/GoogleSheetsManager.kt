package com.example.data

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.StringReader
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class GoogleSheetsManager(private val context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("google_sheets_config", Context.MODE_PRIVATE)

    companion object {
        const val DEFAULT_SHEET_ID = "1S7Mx8QfuN6IsHLBV7YlHlsg6oZ67koWwvgmP9WrfS40"
        const val DEFAULT_APPS_SCRIPT_URL = "https://script.google.com/macros/s/AKfycbwirtSIoveHtF07Mj6x_XiitW3xLGPimowItnNpIWcZ8MlBOJQM-MDTAX1dJG72WKgNpA/exec"
        const val DEFAULT_API_KEY = "BIBLE_WORDS_APP_KEY_2026"

        private const val KEY_SHEET_ID = "sheet_id"
        private const val KEY_APPS_SCRIPT_URL = "apps_script_url"
        private const val KEY_API_KEY = "api_key"
        private const val KEY_LAST_SYNC_COUNT = "last_sync_count"
        private const val KEY_LAST_SYNC_TIME = "last_sync_time"
        private const val KEY_LAST_SYNC_ERROR = "last_sync_error"

        val APPS_SCRIPT_TEMPLATE = """
// ===============================================================
// SCRIPT DE INTEGRAÇÃO COM CHAVE DE API EXCLUSIVA DO APP
// Cole este código em: Extensões > Apps Script na sua planilha Google
// Depois clique em: Implantar > Nova Implantação > Tipo: Aplicativo da Web
// Configure: 'Executar como: Eu' e 'Quem pode acessar: Qualquer pessoa'
// ===============================================================

var APP_API_KEY = "$DEFAULT_API_KEY"; // Chave de segurança exclusiva do aplicativo

function doGet(e) {
  var key = (e && e.parameter && e.parameter.apiKey) ? e.parameter.apiKey : "";
  if (key !== APP_API_KEY) {
    return ContentService.createTextOutput(JSON.stringify({
      success: false,
      error: "Acesso negado: Chave de API inválida."
    })).setMimeType(ContentService.MimeType.JSON);
  }

  var action = (e && e.parameter && e.parameter.action) ? e.parameter.action : "getWords";
  var ss = SpreadsheetApp.getActiveSpreadsheet();

  if (action === "getWords") {
    var sheet = ss.getSheetByName("Palavras");
    if (!sheet) {
      return ContentService.createTextOutput(JSON.stringify({
        success: false,
        error: "Aba 'Palavras' não foi encontrada nesta planilha."
      })).setMimeType(ContentService.MimeType.JSON);
    }

    var data = sheet.getDataRange().getValues();
    var words = [];
    for (var i = 0; i < data.length; i++) {
      var row = data[i];
      var word = String(row[0] || "").trim().toUpperCase();
      var clue = String(row[1] || "").trim();

      // Pula linha de cabeçalho
      if (i === 0 && (word === "PALAVRA" || word === "PALAVRAS" || word === "RESPOSTA" || word === "COLUNA A")) {
        continue;
      }
      if (word && clue) {
        words.push({ word: word, clue: clue });
      }
    }

    return ContentService.createTextOutput(JSON.stringify({
      success: true,
      count: words.length,
      words: words
    })).setMimeType(ContentService.MimeType.JSON);
  }

  return ContentService.createTextOutput(JSON.stringify({ status: "ok" }))
    .setMimeType(ContentService.MimeType.JSON);
}

function doPost(e) {
  try {
    var contents = e.postData.contents;
    var data = JSON.parse(contents);

    if (!data || data.apiKey !== APP_API_KEY) {
      return ContentService.createTextOutput(JSON.stringify({
        success: false,
        error: "Acesso negado: Chave de API inválida."
      })).setMimeType(ContentService.MimeType.JSON);
    }

    var ss = SpreadsheetApp.getActiveSpreadsheet();

    // 1. Gravação na aba "Rankings"
    if (data.action === "saveRanking") {
      var sheet = ss.getSheetByName("Rankings");
      if (!sheet) {
        sheet = ss.insertSheet("Rankings");
        sheet.appendRow(["Data", "Hora", "Nome", "Pontuação", "Tipo de Jogo", "Nome da Equipe", "Nomes dos Jogadores", "Posição no Ranking"]);
      }
      sheet.appendRow([
        data.date || "",
        data.time || "",
        data.name || "",
        data.score || 0,
        data.gameMode || "",
        data.teamName || "",
        data.playerNames || "",
        data.rankPosition || 0
      ]);
      return ContentService.createTextOutput(JSON.stringify({ success: true }))
        .setMimeType(ContentService.MimeType.JSON);
    }

    // 2. Gravação na aba "Sistema"
    if (data.action === "saveSystemLog") {
      var sheet = ss.getSheetByName("Sistema");
      if (!sheet) {
        sheet = ss.insertSheet("Sistema");
        sheet.appendRow(["ID da Partida", "Data e Hora", "Tipo de Jogo", "Jogadores e Equipes", "Equipe Vencedora", "Perguntas e Respostas dadas", "Pontuação Final"]);
      }
      sheet.appendRow([
        data.gameId || "",
        data.date || "",
        data.gameMode || "",
        data.playersAndTeams || "",
        data.winningTeam || "",
        data.questionsAndAnswers || "",
        data.finalScore || 0
      ]);
      return ContentService.createTextOutput(JSON.stringify({ success: true }))
        .setMimeType(ContentService.MimeType.JSON);
    }

    return ContentService.createTextOutput(JSON.stringify({ error: "Ação não reconhecida" }))
      .setMimeType(ContentService.MimeType.JSON);
  } catch (err) {
    return ContentService.createTextOutput(JSON.stringify({ error: err.toString() }))
      .setMimeType(ContentService.MimeType.JSON);
  }
}
""".trimIndent()
    }

    var sheetId: String
        get() = prefs.getString(KEY_SHEET_ID, DEFAULT_SHEET_ID)?.ifBlank { DEFAULT_SHEET_ID } ?: DEFAULT_SHEET_ID
        set(value) {
            val clean = value.trim()
            val finalId = if (clean.isBlank()) DEFAULT_SHEET_ID else clean
            prefs.edit().putString(KEY_SHEET_ID, finalId).apply()
        }

    var appsScriptWebhookUrl: String
        get() = prefs.getString(KEY_APPS_SCRIPT_URL, DEFAULT_APPS_SCRIPT_URL)?.ifBlank { DEFAULT_APPS_SCRIPT_URL } ?: DEFAULT_APPS_SCRIPT_URL
        set(value) = prefs.edit().putString(KEY_APPS_SCRIPT_URL, value.trim()).apply()

    var apiKey: String
        get() = prefs.getString(KEY_API_KEY, DEFAULT_API_KEY) ?: DEFAULT_API_KEY
        set(value) = prefs.edit().putString(KEY_API_KEY, value.trim()).apply()

    var lastSyncCount: Int
        get() = prefs.getInt(KEY_LAST_SYNC_COUNT, 0)
        set(value) = prefs.edit().putInt(KEY_LAST_SYNC_COUNT, value).apply()

    var lastSyncTime: String
        get() = prefs.getString(KEY_LAST_SYNC_TIME, "") ?: ""
        set(value) = prefs.edit().putString(KEY_LAST_SYNC_TIME, value).apply()

    var lastSyncError: String?
        get() = prefs.getString(KEY_LAST_SYNC_ERROR, null)
        set(value) = prefs.edit().putString(KEY_LAST_SYNC_ERROR, value).apply()

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .followRedirects(true)
        .build()

    /**
     * Consulta e faz download das palavras da aba "Palavras" da planilha Google.
     * Palavra (resposta) na Coluna A e Dica (questão) na Coluna B.
     */
    suspend fun fetchWordsFromSheet(): Result<List<BibleWord>> = withContext(Dispatchers.IO) {
        val currentSheetId = sheetId
        val scriptUrl = appsScriptWebhookUrl
        val currentKey = apiKey

        // 1. Se houver Webhook do Apps Script configurado, consulta autenticando com a Chave de API
        if (scriptUrl.isNotBlank()) {
            try {
                val separator = if (scriptUrl.contains("?")) "&" else "?"
                val fullUrl = "$scriptUrl${separator}action=getWords&apiKey=$currentKey"
                val request = Request.Builder().url(fullUrl).get().build()
                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    val bodyStr = response.body?.string() ?: ""
                    val json = JSONObject(bodyStr)
                    if (json.optBoolean("success", false)) {
                        val wordsArray = json.optJSONArray("words") ?: JSONArray()
                        val list = mutableListOf<BibleWord>()
                        for (i in 0 until wordsArray.length()) {
                            val item = wordsArray.getJSONObject(i)
                            val word = item.optString("word").trim().uppercase()
                            val clue = item.optString("clue").trim()
                            if (word.isNotBlank() && clue.isNotBlank()) {
                                list.add(BibleWord(word = word, clue = clue, reference = extractReference(clue)))
                            }
                        }
                        if (list.isNotEmpty()) {
                            saveSyncSuccess(list.size)
                            return@withContext Result.success(list)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.w("GoogleSheetsManager", "Tentativa via Apps Script com API Key falhou: ${e.message}")
            }
        }

        // 2. Consulta via exportação direta CSV da aba "Palavras"
        val exportUrls = listOf(
            "https://docs.google.com/spreadsheets/d/$currentSheetId/gviz/tq?tqx=out:csv&sheet=Palavras",
            "https://docs.google.com/spreadsheets/d/$currentSheetId/export?format=csv&sheet=Palavras"
        )

        var isRestricted = false

        for (url in exportUrls) {
            try {
                val request = Request.Builder()
                    .url(url)
                    .header("User-Agent", "Mozilla/5.0 (Android; Mobile)")
                    .build()
                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    val body = response.body?.string() ?: ""
                    if (body.contains("Sign in to your Google Account") || body.contains("accounts.google.com") || body.contains("<!DOCTYPE html>")) {
                        isRestricted = true
                    } else {
                        val parsed = parseWordsCsv(body)
                        if (parsed.isNotEmpty()) {
                            saveSyncSuccess(parsed.size)
                            return@withContext Result.success(parsed)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.w("GoogleSheetsManager", "Tentativa de download da URL $url falhou: ${e.message}")
            }
        }

        val errorMessage = if (isRestricted) {
            "A planilha Google está em modo restrito. No Google Drive, clique em Compartilhar > Acesso Geral: altere para 'Qualquer pessoa com o link' (como Leitor), ou configure o Web App com a Chave de API."
        } else {
            "Não foi possível ler as palavras da aba 'Palavras'. Verifique a conexão com a internet ou se a aba se chama exatamente 'Palavras'."
        }

        saveSyncError(errorMessage)
        Result.failure(Exception(errorMessage))
    }

    /**
     * Registra o ranking na aba "Rankings" da planilha Google com verificação de API Key
     * Colunas: Data, Hora, Nome, Pontuação, Tipo de Jogo, Nome da Equipe, Nomes dos Jogadores, Posição no Ranking
     */
    suspend fun postRankingToSheet(entry: RankingEntry): Result<Boolean> = withContext(Dispatchers.IO) {
        val webhook = appsScriptWebhookUrl
        if (webhook.isBlank()) {
            return@withContext Result.failure(
                Exception("URL do Google Apps Script não configurada para gravação na aba 'Rankings'.")
            )
        }

        try {
            val json = JSONObject().apply {
                put("apiKey", apiKey)
                put("action", "saveRanking")
                put("date", entry.date)
                put("time", entry.time)
                put("name", entry.playerName)
                put("score", entry.score)
                put("gameMode", entry.gameMode)
                put("teamName", entry.teamName)
                put("playerNames", entry.playerNames)
                put("rankPosition", entry.rankPosition)
            }
            val body = json.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
            val request = Request.Builder().url(webhook).post(body).build()
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                Result.success(true)
            } else {
                Result.failure(Exception("Erro HTTP ${response.code} ao gravar na aba Rankings"))
            }
        } catch (e: Exception) {
            Log.e("GoogleSheetsManager", "Erro ao registrar ranking na planilha: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * Registra a partida na aba "Sistema" da planilha Google com verificação de API Key
     * Colunas: ID da partida, Data e Hora, Tipo de jogo, Jogadores e Equipes, Equipe vencedora, Perguntas e respostas dadas, Pontuação final
     */
    suspend fun postSystemLogToSheet(log: SystemGameLog): Result<Boolean> = withContext(Dispatchers.IO) {
        val webhook = appsScriptWebhookUrl
        if (webhook.isBlank()) {
            return@withContext Result.failure(
                Exception("URL do Google Apps Script não configurada para gravação na aba 'Sistema'.")
            )
        }

        try {
            val json = JSONObject().apply {
                put("apiKey", apiKey)
                put("action", "saveSystemLog")
                put("gameId", log.gameId)
                put("date", log.date)
                put("gameMode", log.gameMode)
                put("playersAndTeams", log.playersAndTeams)
                put("winningTeam", log.winningTeam)
                put("questionsAndAnswers", log.questionsAndAnswers)
                put("finalScore", log.finalScore)
            }
            val body = json.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
            val request = Request.Builder().url(webhook).post(body).build()
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                Result.success(true)
            } else {
                Result.failure(Exception("Erro HTTP ${response.code} ao gravar na aba Sistema"))
            }
        } catch (e: Exception) {
            Log.e("GoogleSheetsManager", "Erro ao registrar partida na aba Sistema: ${e.message}")
            Result.failure(e)
        }
    }

    private fun saveSyncSuccess(count: Int) {
        val nowStr = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())
        lastSyncCount = count
        lastSyncTime = nowStr
        lastSyncError = null
    }

    private fun saveSyncError(error: String) {
        lastSyncError = error
    }

    private fun parseWordsCsv(csv: String): List<BibleWord> {
        val result = mutableListOf<BibleWord>()
        val reader = BufferedReader(StringReader(csv))
        var lineNum = 0

        reader.forEachLine { line ->
            lineNum++
            if (line.isNotBlank()) {
                val cols = parseCsvLine(line)
                if (cols.size >= 2) {
                    val word = cols[0].trim().removeSurrounding("\"").uppercase()
                    val clue = cols[1].trim().removeSurrounding("\"")

                    if (lineNum == 1 && (word.equals("PALAVRA", true) || word.equals("PALAVRAS", true) || word.equals("RESPOSTA", true) || word.equals("COLUNA A", true))) {
                        return@forEachLine
                    }

                    if (word.isNotBlank() && clue.isNotBlank()) {
                        val ref = extractReference(clue)
                        result.add(
                            BibleWord(
                                word = word,
                                clue = clue,
                                reference = ref
                            )
                        )
                    }
                }
            }
        }
        return result
    }

    private fun extractReference(clue: String): String {
        val regex = "\\(([^)]+)\\)".toRegex()
        val match = regex.findAll(clue).lastOrNull()
        return match?.groupValues?.get(1) ?: ""
    }

    private fun parseCsvLine(line: String): List<String> {
        val tokens = mutableListOf<String>()
        val sb = StringBuilder()
        var inQuotes = false

        for (ch in line) {
            when {
                ch == '\"' -> inQuotes = !inQuotes
                ch == ',' && !inQuotes -> {
                    tokens.add(sb.toString())
                    sb.clear()
                }
                else -> sb.append(ch)
            }
        }
        tokens.add(sb.toString())
        return tokens
    }
}
