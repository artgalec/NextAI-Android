package com.nexusai.techfeed

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Newspaper
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin

// ============================================================================
// МОДЕЛИ ДАННЫХ И ПАЛИТРЫ
// ============================================================================

data class NewsItem(
    val id: String,
    val brand: String,
    val platform: String,
    val title: String,
    val summary: String,
    val sourceLabel: String,
    val timeLabel: String,
    val timestampMillis: Long,
    val url: String,
    val isVeryFresh6h: Boolean
)

data class TechSpecRow(
    val label: String,
    val value: String
)

data class TechReviewArticle(
    val id: String,
    val deviceName: String,
    val brand: String,
    val category: String,
    val headline: String,
    val score: String,
    val leadSummary: String,
    val fullAnalysis: String,
    val pros: List<String>,
    val cons: List<String>,
    val specs: List<TechSpecRow>,
    val authorAgent: String,
    val publishedAt: String,
    val isUserOrdered: Boolean = false,
    val isAutoJunk: Boolean = false
)

data class UserAppSettings(
    val paletteId: String = "cyber_blue",
    val darkMode: String = "dark",
    val cardStyle: String = "outlined",
    val cornerRadius: Int = 16,
    val fontScale: Float = 1.0f,
    val showSummary: Boolean = true
)

data class AppPalette(
    val id: String,
    val name: String,
    val primaryDark: Color,
    val primaryLight: Color
)

val AppPalettes = listOf(
    AppPalette("cyber_blue", "Неон", Color(0xFF38BDF8), Color(0xFF0284C7)),
    AppPalette("emerald", "Изумруд", Color(0xFF10B981), Color(0xFF059669)),
    AppPalette("amber", "Янтарь", Color(0xFFF59E0B), Color(0xFFD97706)),
    AppPalette("violet", "Аметист", Color(0xFFA78BFA), Color(0xFF7C3AED)),
    AppPalette("rose", "Рубин", Color(0xFFF43F5E), Color(0xFFE11D48)),
    AppPalette("cyan", "Бирюза", Color(0xFF22D3EE), Color(0xFF0891B2))
)

enum class AgentLocationZone(val label: String) {
    DESK("За рабочим КП"),
    CHIEF_CHECK("На приёмке у Авроры"),
    DINING("На кухне"),
    LOUNGE("В комнате отдыха")
}

data class OfficeAgentProfile(
    val id: Int,
    val name: String,
    val role: String,
    val roomTitle: String,
    val accentColor: Color,
    val hairColor: Color,
    val isFemale: Boolean,
    val hasGlasses: Boolean,
    val isTopRow: Boolean,
    val deskChairPos: Offset,
    val roomDoorPos: Offset,
    val corridorEntryPos: Offset,
    val corridorLaneY: Float,
    val diningSeatPos: Offset,
    val loungeSeatPos: Offset
)

data class SoloTaskItem(
    val ticketNumber: Int,
    val badge: String,
    val title: String,
    val resultSummary: String,
    val startedAt: String,
    val completedAt: String = "",
    val chiefApproved: Boolean = false,
    val salaryDelta: Int = 0,
    val rejectReason: String = ""
)

data class AgentLiveStatus(
    val currentTask: SoloTaskItem,
    val progress: Float,
    val targetZone: AgentLocationZone,
    val breakSecondsRemaining: Float,
    val salaryBalance: Int,
    val rejectionsCount: Int,
    val reprimandsCount: Int,
    val completedHistory: List<SoloTaskItem>
)

data class ChiefAuditEntry(
    val id: String,
    val ticketNumber: Int,
    val agentName: String,
    val taskTitle: String,
    val verdict: String,
    val salaryDelta: Int,
    val hasReprimand: Boolean,
    val isApproved: Boolean,
    val checkedAt: String
)

data class DeputyEditEntry(
    val id: String,
    val reviewerName: String,
    val deviceName: String,
    val correctionDetail: String,
    val editedAt: String
)

data class PersistedOfficeState(
    val ticketCounter: Int,
    val agentStatuses: Map<Int, AgentLiveStatus>,
    val chiefAuditLog: List<ChiefAuditEntry>,
    val deputyEditLog: List<DeputyEditEntry>
)

// ============================================================================
// ЛОКАЛЬНОЕ ХРАНИЛИЩЕ (SHARED PREFERENCES + JSON)
// ============================================================================

object LocalCache {
    private const val PREF_NAME = "nexus_tech_feed_v5"
    private const val KEY_SETTINGS = "settings_json"
    private const val KEY_NEWS = "news_json"
    private const val KEY_REVIEWS = "reviews_json"
    private const val KEY_SAVED_IDS = "saved_ids_json"
    private const val KEY_DELETED_NEWS_IDS = "deleted_news_ids_json"
    private const val KEY_DELETED_REVIEW_IDS = "deleted_review_ids_json"
    private const val KEY_OFFICE_STATE = "office_state_json"

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    fun loadSettings(context: Context): UserAppSettings {
        val raw = prefs(context).getString(KEY_SETTINGS, null) ?: return UserAppSettings()
        return try {
            val o = JSONObject(raw)
            UserAppSettings(
                paletteId = o.optString("paletteId", "cyber_blue"),
                darkMode = o.optString("darkMode", "dark"),
                cardStyle = o.optString("cardStyle", "outlined"),
                cornerRadius = o.optInt("cornerRadius", 16),
                fontScale = o.optDouble("fontScale", 1.0).toFloat(),
                showSummary = o.optBoolean("showSummary", true)
            )
        } catch (_: Exception) {
            UserAppSettings()
        }
    }

    fun saveSettings(context: Context, settings: UserAppSettings) {
        val o = JSONObject().apply {
            put("paletteId", settings.paletteId)
            put("darkMode", settings.darkMode)
            put("cardStyle", settings.cardStyle)
            put("cornerRadius", settings.cornerRadius)
            put("fontScale", settings.fontScale.toDouble())
            put("showSummary", settings.showSummary)
        }
        prefs(context).edit().putString(KEY_SETTINGS, o.toString()).apply()
    }

    fun loadNews(context: Context): List<NewsItem> {
        val raw = prefs(context).getString(KEY_NEWS, null) ?: return emptyList()
        return try {
            val arr = JSONArray(raw)
            val list = mutableListOf<NewsItem>()
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                list.add(
                    NewsItem(
                        id = o.getString("id"),
                        brand = o.getString("brand"),
                        platform = o.getString("platform"),
                        title = o.getString("title"),
                        summary = o.optString("summary", ""),
                        sourceLabel = o.optString("sourceLabel", "Официальный канал"),
                        timeLabel = o.optString("timeLabel", "Сегодня"),
                        timestampMillis = o.optLong("timestampMillis", System.currentTimeMillis()),
                        url = o.optString("url", "https://mi.com"),
                        isVeryFresh6h = o.optBoolean("isVeryFresh6h", false)
                    )
                )
            }
            list
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun saveNews(context: Context, list: List<NewsItem>) {
        val arr = JSONArray()
        list.take(150).forEach { item ->
            arr.put(
                JSONObject().apply {
                    put("id", item.id)
                    put("brand", item.brand)
                    put("platform", item.platform)
                    put("title", item.title)
                    put("summary", item.summary)
                    put("sourceLabel", item.sourceLabel)
                    put("timeLabel", item.timeLabel)
                    put("timestampMillis", item.timestampMillis)
                    put("url", item.url)
                    put("isVeryFresh6h", item.isVeryFresh6h)
                }
            )
        }
        prefs(context).edit().putString(KEY_NEWS, arr.toString()).apply()
    }

    fun loadReviews(context: Context): List<TechReviewArticle> {
        val raw = prefs(context).getString(KEY_REVIEWS, null) ?: return emptyList()
        return try {
            val arr = JSONArray(raw)
            val result = mutableListOf<TechReviewArticle>()
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                val prosArr = o.optJSONArray("pros") ?: JSONArray()
                val consArr = o.optJSONArray("cons") ?: JSONArray()
                val specsArr = o.optJSONArray("specs") ?: JSONArray()
                val pros = List(prosArr.length()) { idx -> prosArr.getString(idx) }
                val cons = List(consArr.length()) { idx -> consArr.getString(idx) }
                val specs = List(specsArr.length()) { idx ->
                    val so = specsArr.getJSONObject(idx)
                    TechSpecRow(so.getString("label"), so.getString("value"))
                }
                result.add(
                    TechReviewArticle(
                        id = o.getString("id"),
                        deviceName = o.getString("deviceName"),
                        brand = o.getString("brand"),
                        category = o.getString("category"),
                        headline = o.getString("headline"),
                        score = o.getString("score"),
                        leadSummary = o.getString("leadSummary"),
                        fullAnalysis = o.getString("fullAnalysis"),
                        pros = pros,
                        cons = cons,
                        specs = specs,
                        authorAgent = o.optString("authorAgent", "Вика"),
                        publishedAt = o.optString("publishedAt", "Сегодня"),
                        isUserOrdered = o.optBoolean("isUserOrdered", false),
                        isAutoJunk = o.optBoolean("isAutoJunk", false)
                    )
                )
            }
            result
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun saveReviews(context: Context, list: List<TechReviewArticle>) {
        val arr = JSONArray()
        list.take(100).forEach { r ->
            val prosArr = JSONArray().apply { r.pros.forEach { put(it) } }
            val consArr = JSONArray().apply { r.cons.forEach { put(it) } }
            val specsArr = JSONArray().apply {
                r.specs.forEach { s ->
                    put(JSONObject().apply {
                        put("label", s.label)
                        put("value", s.value)
                    })
                }
            }
            arr.put(
                JSONObject().apply {
                    put("id", r.id)
                    put("deviceName", r.deviceName)
                    put("brand", r.brand)
                    put("category", r.category)
                    put("headline", r.headline)
                    put("score", r.score)
                    put("leadSummary", r.leadSummary)
                    put("fullAnalysis", r.fullAnalysis)
                    put("pros", prosArr)
                    put("cons", consArr)
                    put("specs", specsArr)
                    put("authorAgent", r.authorAgent)
                    put("publishedAt", r.publishedAt)
                    put("isUserOrdered", r.isUserOrdered)
                    put("isAutoJunk", r.isAutoJunk)
                }
            )
        }
        prefs(context).edit().putString(KEY_REVIEWS, arr.toString()).apply()
    }

    fun loadStringSet(context: Context, key: String): Set<String> {
        val raw = prefs(context).getString(key, null) ?: return emptySet()
        return try {
            val arr = JSONArray(raw)
            buildSet {
                for (i in 0 until arr.length()) add(arr.getString(i))
            }
        } catch (_: Exception) {
            emptySet()
        }
    }

    fun saveStringSet(context: Context, key: String, values: Set<String>) {
        val arr = JSONArray().apply { values.forEach { put(it) } }
        prefs(context).edit().putString(key, arr.toString()).apply()
    }

    fun loadSavedIds(context: Context) = loadStringSet(context, KEY_SAVED_IDS)
    fun saveSavedIds(context: Context, ids: Set<String>) = saveStringSet(context, KEY_SAVED_IDS, ids)

    fun loadDeletedNewsIds(context: Context) = loadStringSet(context, KEY_DELETED_NEWS_IDS)
    fun saveDeletedNewsIds(context: Context, ids: Set<String>) = saveStringSet(context, KEY_DELETED_NEWS_IDS, ids)

    fun loadDeletedReviewIds(context: Context) = loadStringSet(context, KEY_DELETED_REVIEW_IDS)
    fun saveDeletedReviewIds(context: Context, ids: Set<String>) = saveStringSet(context, KEY_DELETED_REVIEW_IDS, ids)

    fun saveOfficeState(
        context: Context,
        ticketCounter: Int,
        agentStatuses: Map<Int, AgentLiveStatus>,
        chiefAuditLog: List<ChiefAuditEntry>,
        deputyEditLog: List<DeputyEditEntry>
    ) {
        try {
            val statusesObj = JSONObject()
            agentStatuses.forEach { (id, st) ->
                val histArr = JSONArray()
                st.completedHistory.take(10).forEach { h ->
                    histArr.put(
                        JSONObject().apply {
                            put("ticketNumber", h.ticketNumber)
                            put("badge", h.badge)
                            put("title", h.title)
                            put("resultSummary", h.resultSummary)
                            put("startedAt", h.startedAt)
                            put("completedAt", h.completedAt)
                            put("chiefApproved", h.chiefApproved)
                            put("salaryDelta", h.salaryDelta)
                            put("rejectReason", h.rejectReason)
                        }
                    )
                }
                statusesObj.put(
                    id.toString(),
                    JSONObject().apply {
                        put("ticketNumber", st.currentTask.ticketNumber)
                        put("badge", st.currentTask.badge)
                        put("title", st.currentTask.title)
                        put("resultSummary", st.currentTask.resultSummary)
                        put("startedAt", st.currentTask.startedAt)
                        put("progress", st.progress.toDouble())
                        put("targetZone", st.targetZone.name)
                        put("breakSecondsRemaining", st.breakSecondsRemaining.toDouble())
                        put("salaryBalance", st.salaryBalance)
                        put("rejectionsCount", st.rejectionsCount)
                        put("reprimandsCount", st.reprimandsCount)
                        put("history", histArr)
                    }
                )
            }

            val chiefArr = JSONArray()
            chiefAuditLog.take(20).forEach { c ->
                chiefArr.put(
                    JSONObject().apply {
                        put("id", c.id)
                        put("ticketNumber", c.ticketNumber)
                        put("agentName", c.agentName)
                        put("taskTitle", c.taskTitle)
                        put("verdict", c.verdict)
                        put("salaryDelta", c.salaryDelta)
                        put("hasReprimand", c.hasReprimand)
                        put("isApproved", c.isApproved)
                        put("checkedAt", c.checkedAt)
                    }
                )
            }

            val depArr = JSONArray()
            deputyEditLog.take(20).forEach { d ->
                depArr.put(
                    JSONObject().apply {
                        put("id", d.id)
                        put("reviewerName", d.reviewerName)
                        put("deviceName", d.deviceName)
                        put("correctionDetail", d.correctionDetail)
                        put("editedAt", d.editedAt)
                    }
                )
            }

            val root = JSONObject().apply {
                put("ticketCounter", ticketCounter)
                put("agentStatuses", statusesObj)
                put("chiefAuditLog", chiefArr)
                put("deputyEditLog", depArr)
            }
            prefs(context).edit().putString(KEY_OFFICE_STATE, root.toString()).apply()
        } catch (_: Exception) {
        }
    }

    fun loadOfficeState(context: Context): PersistedOfficeState? {
        val raw = prefs(context).getString(KEY_OFFICE_STATE, null) ?: return null
        return try {
            val root = JSONObject(raw)
            val ticketCounter = root.optInt("ticketCounter", 120)
            val statusesObj = root.getJSONObject("agentStatuses")
            val map = mutableMapOf<Int, AgentLiveStatus>()
            val keys = statusesObj.keys()
            while (keys.hasNext()) {
                val k = keys.next()
                val id = k.toIntOrNull() ?: continue
                val o = statusesObj.getJSONObject(k)
                val zone = try {
                    AgentLocationZone.valueOf(o.optString("targetZone", AgentLocationZone.DESK.name))
                } catch (_: Exception) {
                    AgentLocationZone.DESK
                }
                val histArr = o.optJSONArray("history") ?: JSONArray()
                val history = List(histArr.length()) { idx ->
                    val ho = histArr.getJSONObject(idx)
                    SoloTaskItem(
                        ticketNumber = ho.optInt("ticketNumber", 100),
                        badge = ho.optString("badge", "ГОТОВО"),
                        title = ho.optString("title", ""),
                        resultSummary = ho.optString("resultSummary", ""),
                        startedAt = ho.optString("startedAt", ""),
                        completedAt = ho.optString("completedAt", ""),
                        chiefApproved = ho.optBoolean("chiefApproved", true),
                        salaryDelta = ho.optInt("salaryDelta", 0),
                        rejectReason = ho.optString("rejectReason", "")
                    )
                }
                map[id] = AgentLiveStatus(
                    currentTask = SoloTaskItem(
                        ticketNumber = o.optInt("ticketNumber", 100 + id),
                        badge = o.optString("badge", "В РАБОТЕ"),
                        title = o.optString("title", "Проверка спецификаций"),
                        resultSummary = o.optString("resultSummary", "Выполняется"),
                        startedAt = o.optString("startedAt", "12:00")
                    ),
                    progress = o.optDouble("progress", 0.3).toFloat(),
                    targetZone = zone,
                    breakSecondsRemaining = o.optDouble("breakSecondsRemaining", 0.0).toFloat(),
                    salaryBalance = o.optInt("salaryBalance", 5000),
                    rejectionsCount = o.optInt("rejectionsCount", 0),
                    reprimandsCount = o.optInt("reprimandsCount", 0),
                    completedHistory = history
                )
            }

            val chiefArr = root.optJSONArray("chiefAuditLog") ?: JSONArray()
            val chiefList = List(chiefArr.length()) { idx ->
                val co = chiefArr.getJSONObject(idx)
                ChiefAuditEntry(
                    id = co.getString("id"),
                    ticketNumber = co.optInt("ticketNumber", 100),
                    agentName = co.optString("agentName", "Вика"),
                    taskTitle = co.optString("taskTitle", ""),
                    verdict = co.optString("verdict", ""),
                    salaryDelta = co.optInt("salaryDelta", 0),
                    hasReprimand = co.optBoolean("hasReprimand", false),
                    isApproved = co.optBoolean("isApproved", true),
                    checkedAt = co.optString("checkedAt", "")
                )
            }

            val depArr = root.optJSONArray("deputyEditLog") ?: JSONArray()
            val depList = List(depArr.length()) { idx ->
                val doObj = depArr.getJSONObject(idx)
                DeputyEditEntry(
                    id = doObj.getString("id"),
                    reviewerName = doObj.optString("reviewerName", "Виктор"),
                    deviceName = doObj.optString("deviceName", ""),
                    correctionDetail = doObj.optString("correctionDetail", ""),
                    editedAt = doObj.optString("editedAt", "")
                )
            }

            PersistedOfficeState(
                ticketCounter = ticketCounter,
                agentStatuses = map,
                chiefAuditLog = chiefList,
                deputyEditLog = depList
            )
        } catch (_: Exception) {
            null
        }
    }

    fun clearAll(context: Context) {
        prefs(context).edit().clear().apply()
    }
}

// ============================================================================
// ПАРСЕР ОФИЦИАЛЬНЫХ КАНАЛОВ И РЕЗЕРВНАЯ ЛЕНТА
// ============================================================================

object NewsFeedFetcher {
    private data class ChannelSource(
        val brand: String,
        val platform: String,
        val sourceLabel: String,
        val webUrl: String
    )

    private val sources = listOf(
        ChannelSource("Xiaomi", "Telegram RU", "Xiaomi Russia", "https://t.me/s/xiaomi_ru_official"),
        ChannelSource("Samsung", "Telegram RU", "Samsung Russia", "https://t.me/s/SamsungRussia"),
        ChannelSource("Huawei", "Telegram RU", "Huawei Mobile", "https://t.me/s/huaweimobilerus"),
        ChannelSource("OPPO", "Telegram RU", "OPPO Россия", "https://t.me/s/opporussia"),
        ChannelSource("Nothing", "Telegram", "Nothing Tech", "https://t.me/s/nothingtech"),
        ChannelSource("Vivo", "Telegram RU", "vivo Россия", "https://t.me/s/vivorussia")
    )

    suspend fun fetchLatestNews(): List<NewsItem> = withContext(Dispatchers.IO) {
        val fetched = mutableListOf<NewsItem>()
        val now = System.currentTimeMillis()
        val sixHoursMs = 6L * 3600L * 1000L

        for (src in sources) {
            try {
                val conn = (URL(src.webUrl).openConnection() as HttpURLConnection).apply {
                    connectTimeout = 6500
                    readTimeout = 6500
                    setRequestProperty("User-Agent", "Mozilla/5.0 (Linux; Android 16; Xiaomi 17T)")
                }
                if (conn.responseCode == 200) {
                    val html = conn.inputStream.bufferedReader().use { it.readText() }
                    val parsed = parseTelegramWebHtml(html, src, now, sixHoursMs)
                    fetched.addAll(parsed)
                }
                conn.disconnect()
            } catch (_: Exception) {
            }
        }

        if (fetched.isEmpty()) {
            return@withContext fallbackSeedNews(now)
        }

        fetched.distinctBy { it.id }
            .sortedByDescending { it.timestampMillis }
            .take(80)
    }

    private fun parseTelegramWebHtml(
        html: String,
        src: ChannelSource,
        nowMillis: Long,
        sixHoursMs: Long
    ): List<NewsItem> {
        val results = mutableListOf<NewsItem>()
        val blocks = html.split("tgme_widget_message_wrap").drop(1).takeLast(6)
        val postRegex = Regex("""data-post="([^"]+)"""")
        val textRegex = Regex("""<div class="tgme_widget_message_text[^"]*"[^>]*>(.*?)</div>""", RegexOption.DOT_MATCHES_ALL)
        val timeRegex = Regex("""datetime="([^"]+)"""")

        for (block in blocks) {
            val postId = postRegex.find(block)?.groupValues?.getOrNull(1) ?: continue
            val rawTextHtml = textRegex.find(block)?.groupValues?.getOrNull(1) ?: continue
            val cleanText = rawTextHtml
                .replace(Regex("<br\\s*/?>", RegexOption.IGNORE_CASE), "\n")
                .replace(Regex("<[^>]+>"), "")
                .replace("&quot;", "\"")
                .replace("&amp;", "&")
                .replace("&lt;", "<")
                .replace("&gt;", ">")
                .replace("&#33;", "!")
                .replace("&#39;", "'")
                .trim()

            if (cleanText.length < 25) continue

            val lines = cleanText.lines().map { it.trim() }.filter { it.isNotBlank() }
            val title = lines.firstOrNull()?.take(115) ?: continue
            val summary = lines.drop(1).joinToString(" ").take(260)

            val isoTime = timeRegex.find(block)?.groupValues?.getOrNull(1)
            val ts = try {
                if (isoTime != null) Instant.parse(isoTime).toEpochMilli() else nowMillis
            } catch (_: Exception) {
                nowMillis
            }

            val zdt = Instant.ofEpochMilli(ts).atZone(ZoneId.systemDefault())
            val timeStr = zdt.format(DateTimeFormatter.ofPattern("dd.MM HH:mm"))

            results.add(
                NewsItem(
                    id = "tg_$postId",
                    brand = src.brand,
                    platform = src.platform,
                    title = title,
                    summary = summary,
                    sourceLabel = src.sourceLabel,
                    timeLabel = timeStr,
                    timestampMillis = ts,
                    url = "https://t.me/$postId",
                    isVeryFresh6h = (nowMillis - ts) in 0..sixHoursMs
                )
            )
        }
        return results
    }

    private fun fallbackSeedNews(now: Long): List<NewsItem> {
        val fmt = DateTimeFormatter.ofPattern("dd.MM HH:mm")
        val labelNow = Instant.ofEpochMilli(now).atZone(ZoneId.systemDefault()).format(fmt)
        return listOf(
            NewsItem(
                id = "seed_xiaomi_17t_hyperos",
                brand = "Xiaomi",
                platform = "HyperOS 3 • RU/CN",
                title = "Xiaomi выпустила обновление HyperOS 3.0.307 с оптимизацией камеры Leica и кодека LHDC",
                summary = "Обновление улучшает стабильность ночной съёмки на 5x перископе, снижает нагрев при зарядке и повышает стабильность Bluetooth-аудио.",
                sourceLabel = "Xiaomi Community",
                timeLabel = labelNow,
                timestampMillis = now - 30 * 60_000L,
                url = "https://new.c.mi.com/ru/",
                isVeryFresh6h = true
            ),
            NewsItem(
                id = "seed_huawei_watch_fit5",
                brand = "Huawei",
                platform = "HarmonyOS • Wearables",
                title = "Huawei Watch Fit 5 получили бета-обновление с расширенным мониторингом пульса и циферблатами",
                summary = "В новой сборке добавлены плавные виджеты тренировок, быстрый ответ на уведомления и оптимизирован расход батареи с включённым AOD.",
                sourceLabel = "Huawei Mobile",
                timeLabel = labelNow,
                timestampMillis = now - 95 * 60_000L,
                url = "https://consumer.huawei.com/ru/wearables/",
                isVeryFresh6h = true
            ),
            NewsItem(
                id = "seed_samsung_oneui",
                brand = "Samsung",
                platform = "One UI • Galaxy AI",
                title = "Samsung расширила функции локального ИИ и улучшила ШИМ-контроль на AMOLED-экранах флагманов",
                summary = "Новый режим защиты зрения снижает мерцание на низкой яркости, а встроенный переводчик работает полностью офлайн.",
                sourceLabel = "Samsung Newsroom",
                timeLabel = labelNow,
                timestampMillis = now - 180 * 60_000L,
                url = "https://news.samsung.com/ru/",
                isVeryFresh6h = true
            ),
            NewsItem(
                id = "seed_nothing_os",
                brand = "Nothing",
                platform = "Nothing OS • Glyph",
                title = "Nothing представила свежий патч безопасности и новые сценарии подсветки Glyph Interface",
                summary = "Теперь индикаторы на задней панели поддерживают прогресс-бар таймеров и интеграцию с системными уведомлениями.",
                sourceLabel = "Nothing Community",
                timeLabel = labelNow,
                timestampMillis = now - 7 * 3600_000L,
                url = "https://nothing.community/",
                isVeryFresh6h = false
            )
        )
    }
}

// ============================================================================
// КАТАЛОГ РЕДАКЦИОННЫХ ОБЗОРОВ И ДВОЙНОЙ АУДИТ ТТХ
// ============================================================================

object ReviewCatalogFactory {
    fun defaultReviews(): List<TechReviewArticle> = listOf(
        TechReviewArticle(
            id = "rev_xiaomi_17t",
            deviceName = "Xiaomi 17T",
            brand = "Xiaomi",
            category = "Смартфоны",
            headline = "Обзор Xiaomi 17T: субфлагман с 5x перископом Leica и батареей 6500 мА·ч",
            score = "9.1 / 10",
            leadSummary = "Сбалансированный субфлагман с оптикой Leica, ярким 1.5K AMOLED-дисплеем, защитой IP68 и рекордной автономностью на базе Dimensity 8500-Ultra.",
            fullAnalysis = "Xiaomi 17T делает ставку на автономность и телеобъектив: 50-мегапиксельный перископ с 5-кратным оптическим приближением выдаёт чёткие снимки днём и уверенно держит детализацию в сумерках. Экран 1.5K AMOLED с высокой частотой ШИМ комфортен для глаз, а аккумулятор на 6500 мА·ч легко выдерживает полтора-два дня активной работы. Компромиссы предсказуемы для класса: порт USB 2.0, отсутствие LTPO и беспроводной зарядки.",
            pros = listOf(
                "50 МП перископ-камера Leica с 5x оптическим зумом",
                "Аккумулятор 6500 мА·ч и защита корпуса по стандарту IP68",
                "Яркий 1.5K AMOLED-экран с высокочастотным ШИМ"
            ),
            cons = listOf(
                "Разъём USB Type-C стандарта USB 2.0",
                "Нет беспроводной зарядки и адаптивной герцовки LTPO"
            ),
            specs = listOf(
                TechSpecRow("Процессор", "MediaTek Dimensity 8500-Ultra (4 нм)"),
                TechSpecRow("Экран", "6.67\" 1.5K AMOLED, 120 Гц, HDR10+"),
                TechSpecRow("Камеры", "50 МП основная + 50 МП 5x перископ Leica + 12 МП ширик"),
                TechSpecRow("Батарея", "6500 мА·ч, быстрая проводная зарядка"),
                TechSpecRow("Аудио и связь", "Bluetooth 5.4, LHDC 5.0 / LDAC, Wi-Fi 7, IP68")
            ),
            authorAgent = "Вика",
            publishedAt = "Редакционный обзор"
        ),
        TechReviewArticle(
            id = "rev_huawei_watch_fit_5",
            deviceName = "Huawei Watch Fit 5",
            brand = "Huawei",
            category = "Носимая электроника",
            headline = "Обзор Huawei Watch Fit 5: лёгкий корпус, яркий AMOLED и неделя без розетки",
            score = "9.0 / 10",
            leadSummary = "Умные часы с тонким алюминиевым корпусом, точными датчиками пульса и SpO2, плавной HarmonyOS и автономностью до 7–10 дней.",
            fullAnalysis = "Huawei Watch Fit 5 сочетают компактный прямоугольный форм-фактор с крупным контрастным AMOLED-дисплеем, который отлично читается на ярком солнце. Обновлённые оптические сенсоры точно фиксируют пульс на интервальных тренировках, а встроенный GPS быстро ловит спутники без смартфона.",
            pros = listOf(
                "Яркий AMOLED-экран с тонкими рамками и режимом AOD",
                "До 7–10 дней автономной работы в смешанном сценарии",
                "Точный мониторинг пульса, сна и тренировок"
            ),
            cons = listOf(
                "Ограниченная установка сторонних приложений",
                "Нет бесконтактной оплаты во всех регионах"
            ),
            specs = listOf(
                TechSpecRow("Экран", "1.82\" AMOLED, 480×408, до 1500 нит"),
                TechSpecRow("ОС и связь", "HarmonyOS, Bluetooth 5.2, встроенный GNSS"),
                TechSpecRow("Автономность", "До 10 дней (до 7 дней с активным AOD)"),
                TechSpecRow("Влагозащита", "5 ATM (плавание в бассейне)")
            ),
            authorAgent = "Артур",
            publishedAt = "Редакционный обзор"
        ),
        TechReviewArticle(
            id = "rev_redmi_buds_8_pro",
            deviceName = "Xiaomi Redmi Buds 8 Pro",
            brand = "Xiaomi",
            category = "Носимая электроника",
            headline = "Обзор Xiaomi Redmi Buds 8 Pro: коаксиальные драйверы, ANC и поддержка LHDC / LDAC",
            score = "8.8 / 10",
            leadSummary = "Флагманские TWS-наушники линейки Redmi с гибридным шумоподавлением до 55 дБ и поддержкой кодеков высокого разрешения.",
            fullAnalysis = "Redmi Buds 8 Pro выделяются плотным контролем низких частот и чистыми высокими благодаря многодрайверной акустической схеме. Адаптивное шумоподавление эффективно отсекает гул транспорта, а поддержка кодеков высокого битрейта раскрывает потенциал на смартфонах Xiaomi.",
            pros = listOf(
                "Эффективное адаптивное шумоподавление до 55 дБ",
                "Поддержка аудиокодеков высокого разрешения (LHDC / LDAC)",
                "Удобная посадка и мультипоинт на два устройства"
            ),
            cons = listOf(
                "На высоком битрейте 990 кбит/с требуется стабильный радиоэфир",
                "Глянцевые вставки кейса собирают микроцарапины"
            ),
            specs = listOf(
                TechSpecRow("Акустика", "Коаксиальные двойные драйверы Hi-Res"),
                TechSpecRow("Шумоподавление", "Адаптивный ANC до 55 дБ + 3 микрофона"),
                TechSpecRow("Кодеки", "LHDC 5.0, LDAC, AAC, SBC"),
                TechSpecRow("Автономность", "До 9.5 ч от одного заряда, до 38 ч с кейсом")
            ),
            authorAgent = "Артур",
            publishedAt = "Редакционный обзор"
        )
    )

    fun createCustomReviewByUserOrder(
        rawDeviceName: String,
        selectedBrand: String,
        selectedCategoryLabel: String,
        userFocusNote: String,
        assignedAgentName: String
    ): TechReviewArticle {
        val cleanName = rawDeviceName.trim()
        val detectedBrand = if (selectedBrand != "Авто") selectedBrand else when {
            cleanName.contains("xiaomi", true) || cleanName.contains("redmi", true) || cleanName.contains("poco", true) -> "Xiaomi"
            cleanName.contains("samsung", true) || cleanName.contains("galaxy", true) -> "Samsung"
            cleanName.contains("huawei", true) -> "Huawei"
            cleanName.contains("oppo", true) -> "OPPO"
            cleanName.contains("nothing", true) -> "Nothing"
            cleanName.contains("vivo", true) -> "Vivo"
            cleanName.contains("realme", true) -> "Realme"
            else -> "Tech"
        }

        val detectedCat = if (selectedCategoryLabel != "Авто") selectedCategoryLabel else when {
            cleanName.contains("watch", true) || cleanName.contains("buds", true) -> "Носимая электроника"
            cleanName.contains("book", true) || cleanName.contains("laptop", true) -> "Ноутбуки"
            cleanName.contains("tv", true) -> "Умный дом и ТВ"
            else -> "Смартфоны"
        }

        val nowLabel = ZonedDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM HH:mm"))
        val focusParagraph = if (userFocusNote.isNotBlank()) {
            "Особый акцент по заданию редакции: ${userFocusNote.trim()}."
        } else {
            "Комплексная оценка экрана, автономности, охлаждения и стабильности прошивки."
        }

        return TechReviewArticle(
            id = "order_${System.currentTimeMillis()}",
            deviceName = cleanName,
            brand = detectedBrand,
            category = detectedCat,
            headline = "Обзор $cleanName: подробный разбор возможностей и реальных ТТХ",
            score = "9.0 / 10",
            leadSummary = "Специальный обзор $cleanName от агента $assignedAgentName. $focusParagraph",
            fullAnalysis = "Материал подготовлен по прямому приказу владельца редакции. $focusParagraph Устройство протестировано в повседневных сценариях: проверены стабильность беспроводных модулей, качество дисплея и автономность под нагрузкой.",
            pros = listOf(
                "Сбалансированные характеристики в своём сегменте",
                "Актуальные стандарты связи и быстрая зарядка",
                "Проверенные спецификации (аудит Виктора и Елены)"
            ),
            cons = listOf(
                "Требуется ручная настройка фоновых ограничений системы"
            ),
            specs = listOf(
                TechSpecRow("Модель", cleanName),
                TechSpecRow("Бренд", detectedBrand),
                TechSpecRow("Категория", detectedCat),
                TechSpecRow("Статус проверки", "Подтверждено редакцией ($nowLabel)")
            ),
            authorAgent = assignedAgentName,
            publishedAt = "По приказу • $nowLabel",
            isUserOrdered = true,
            isAutoJunk = false
        )
    }

    fun deputyVerifyAndFixArticle(
        articles: List<TechReviewArticle>,
        newsList: List<NewsItem>
    ): Pair<List<TechReviewArticle>, DeputyEditEntry?> {
        if (articles.isEmpty()) return articles to null
        val targetIdx = (System.currentTimeMillis() / 1000L).toInt().mod(articles.size)
        val target = articles[targetIdx]
        val nowStr = ZonedDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"))
        val updatedSpecs = target.specs.map { row ->
            if (row.label == "Статус проверки") {
                row.copy(value = "Сверено с даташитом в $nowStr")
            } else {
                row
            }
        }
        val updatedArticle = target.copy(specs = updatedSpecs)
        val updatedList = articles.toMutableList().apply { this[targetIdx] = updatedArticle }
        val log = DeputyEditEntry(
            id = "dep_${System.currentTimeMillis()}",
            reviewerName = if (targetIdx % 2 == 0) "Виктор" else "Елена",
            deviceName = target.deviceName,
            correctionDetail = "Сверены ТТХ и частотные спецификации (${newsList.size} источников в базе)",
            editedAt = nowStr
        )
        return updatedList to log
    }
}

// ============================================================================
// ТОЧКА ВХОДА И ГЛАВНЫЙ ЭКРАН ПРИЛОЖЕНИЯ
// ============================================================================

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NexusTechFeedApp()
        }
    }
}

@Composable
fun NexusTechFeedApp() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var settings by remember { mutableStateOf(LocalCache.loadSettings(context)) }
    var newsList by remember { mutableStateOf(LocalCache.loadNews(context)) }
    var reviewsList by remember {
        mutableStateOf(
            LocalCache.loadReviews(context).ifEmpty { ReviewCatalogFactory.defaultReviews() }
        )
    }
    var savedIds by remember { mutableStateOf(LocalCache.loadSavedIds(context)) }
    var deletedNewsIds by remember { mutableStateOf(LocalCache.loadDeletedNewsIds(context)) }
    var deletedReviewIds by remember { mutableStateOf(LocalCache.loadDeletedReviewIds(context)) }

    var selectedTab by remember { mutableIntStateOf(0) }
    var isLoading by remember { mutableStateOf(false) }

    val activePalette = remember(settings.paletteId) {
        AppPalettes.find { it.id == settings.paletteId } ?: AppPalettes.first()
    }
    val systemDark = isSystemInDarkTheme()
    val useDark = when (settings.darkMode) {
        "light" -> false
        "dark", "amoled" -> true
        else -> systemDark
    }

    val colorScheme = if (useDark) {
        val bg = if (settings.darkMode == "amoled") Color(0xFF000000) else Color(0xFF09101D)
        val surf = if (settings.darkMode == "amoled") Color(0xFF0B0F17) else Color(0xFF111B2E)
        darkColorScheme(
            primary = activePalette.primaryDark,
            background = bg,
            surface = surf,
            onBackground = Color(0xFFF1F5F9),
            onSurface = Color(0xFFF1F5F9),
            secondaryContainer = activePalette.primaryDark.copy(alpha = 0.22f)
        )
    } else {
        lightColorScheme(
            primary = activePalette.primaryLight,
            background = Color(0xFFF4F7FB),
            surface = Color(0xFFFFFFFF),
            onBackground = Color(0xFF0F172A),
            onSurface = Color(0xFF0F172A),
            secondaryContainer = activePalette.primaryLight.copy(alpha = 0.16f)
        )
    }

    fun refreshFeed() {
        if (isLoading) return
        scope.launch {
            isLoading = true
            val fresh = NewsFeedFetcher.fetchLatestNews()
            val merged = (fresh + newsList)
                .distinctBy { it.id }
                .sortedByDescending { it.timestampMillis }
            newsList = merged
            LocalCache.saveNews(context, merged)
            isLoading = false
        }
    }

    LaunchedEffect(Unit) {
        if (newsList.isEmpty()) {
            refreshFeed()
        }
    }

    val visibleNews = remember(newsList, deletedNewsIds) {
        newsList.filterNot { it.id in deletedNewsIds }
    }
    val visibleReviews = remember(reviewsList, deletedReviewIds) {
        reviewsList.filterNot { it.id in deletedReviewIds }
    }
    val savedNews = remember(visibleNews, savedIds) {
        visibleNews.filter { it.id in savedIds }
    }

    MaterialTheme(colorScheme = colorScheme) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 2.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "NewsPulse AI",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = when (selectedTab) {
                                    0 -> "Официальные каналы • ${visibleNews.size} постов"
                                    1 -> "Редакционные обзоры • ${visibleReviews.size} статей"
                                    2 -> "Сохранённое • ${savedNews.size}"
                                    else -> "Новогодний ИИ-Дом и Настройки"
                                },
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
                            )
                        }

                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        } else {
                            IconButton(onClick = { refreshFeed() }) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Обновить",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            },
            bottomBar = {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface
                ) {
                    NavigationBarItem(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        icon = { Icon(Icons.Default.Newspaper, contentDescription = "Лента") },
                        label = { Text("Лента", fontSize = 11.sp) }
                    )
                    NavigationBarItem(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        icon = { Icon(Icons.Default.RateReview, contentDescription = "Обзоры") },
                        label = { Text("Обзоры", fontSize = 11.sp) }
                    )
                    NavigationBarItem(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        icon = { Icon(Icons.Filled.Bookmark, contentDescription = "Избранное") },
                        label = { Text("Избранное", fontSize = 11.sp) }
                    )
                    NavigationBarItem(
                        selected = selectedTab == 3,
                        onClick = { selectedTab = 3 },
                        icon = { Icon(Icons.Default.Hub, contentDescription = "ИИ-Дом") },
                        label = { Text("ИИ-Дом", fontSize = 11.sp) }
                    )
                }
            }
        ) { innerPadding ->
            when (selectedTab) {
                0 -> FeedScreen(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = innerPadding.calculateTopPadding()),
                    bottomPadding = innerPadding.calculateBottomPadding() + 12.dp,
                    news = visibleNews,
                    savedIds = savedIds,
                    settings = settings,
                    onToggleSave = { id ->
                        savedIds = if (id in savedIds) savedIds - id else savedIds + id
                        LocalCache.saveSavedIds(context, savedIds)
                    },
                    onDeletePost = { id ->
                        deletedNewsIds = deletedNewsIds + id
                        LocalCache.saveDeletedNewsIds(context, deletedNewsIds)
                        Toast.makeText(context, "Пост удалён (можно восстановить в ИИ-Доме)", Toast.LENGTH_SHORT).show()
                    }
                )

                1 -> ReviewsScreen(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = innerPadding.calculateTopPadding()),
                    bottomPadding = innerPadding.calculateBottomPadding() + 12.dp,
                    reviews = visibleReviews,
                    settings = settings,
                    onAddOrderedReview = { newRev ->
                        reviewsList = listOf(newRev) + reviewsList
                        LocalCache.saveReviews(context, reviewsList)
                    },
                    onDeleteReview = { id ->
                        deletedReviewIds = deletedReviewIds + id
                        LocalCache.saveDeletedReviewIds(context, deletedReviewIds)
                        Toast.makeText(context, "Обзор удалён", Toast.LENGTH_SHORT).show()
                    }
                )

                2 -> SavedScreen(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = innerPadding.calculateTopPadding()),
                    bottomPadding = innerPadding.calculateBottomPadding() + 12.dp,
                    savedNews = savedNews,
                    settings = settings,
                    onRemoveSaved = { id ->
                        savedIds = savedIds - id
                        LocalCache.saveSavedIds(context, savedIds)
                    },
                    onDeletePost = { id ->
                        deletedNewsIds = deletedNewsIds + id
                        LocalCache.saveDeletedNewsIds(context, deletedNewsIds)
                    }
                )

                3 -> SettingsScreen(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = innerPadding.calculateTopPadding()),
                    bottomPadding = innerPadding.calculateBottomPadding() + 12.dp,
                    settings = settings,
                    newsList = visibleNews,
                    existingReviews = visibleReviews,
                    deletedTotalCount = deletedNewsIds.size + deletedReviewIds.size,
                    isLoading = isLoading,
                    onSettingsChange = { updated ->
                        settings = updated
                        LocalCache.saveSettings(context, updated)
                    },
                    onTriggerScan = { refreshFeed() },
                    onNewReviewOrdered = { ordered ->
                        reviewsList = listOf(ordered) + reviewsList
                        LocalCache.saveReviews(context, reviewsList)
                    },
                    onReviewsSilentEdited = { edited ->
                        reviewsList = edited
                        LocalCache.saveReviews(context, edited)
                    },
                    onPurgeAutoJunk = {
                        reviewsList = reviewsList.filterNot { it.isAutoJunk }
                        LocalCache.saveReviews(context, reviewsList)
                    },
                    onRestoreDeleted = {
                        deletedNewsIds = emptySet()
                        deletedReviewIds = emptySet()
                        LocalCache.saveDeletedNewsIds(context, emptySet())
                        LocalCache.saveDeletedReviewIds(context, emptySet())
                        Toast.makeText(context, "Все удалённые посты и обзоры восстановлены", Toast.LENGTH_SHORT).show()
                    },
                    onClearCache = {
                        LocalCache.clearAll(context)
                        deletedNewsIds = emptySet()
                        deletedReviewIds = emptySet()
                        savedIds = emptySet()
                        reviewsList = ReviewCatalogFactory.defaultReviews()
                        refreshFeed()
                        Toast.makeText(context, "Кэш сброшен, лента обновляется", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }
    }
}

// ============================================================================
// ЭКРАНЫ: ЛЕНТА, ОБЗОРЫ, ИЗБРАННОЕ И ВСПОМОГАТЕЛЬНЫЕ ЧИПЫ
// ============================================================================

@Composable
fun CompactPillChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val primary = MaterialTheme.colorScheme.primary
    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(9.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(9.dp),
        color = if (selected) primary.copy(alpha = 0.2f)
        else MaterialTheme.colorScheme.background.copy(alpha = 0.75f),
        border = BorderStroke(
            width = if (selected) 1.1.dp else 0.8.dp,
            color = if (selected) primary else primary.copy(alpha = 0.28f)
        )
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            color = if (selected) primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
            maxLines = 1,
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp)
        )
    }
}

@Composable
fun StyledFilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val primary = MaterialTheme.colorScheme.primary
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(10.dp),
        color = if (selected) primary.copy(alpha = 0.2f)
        else MaterialTheme.colorScheme.background.copy(alpha = 0.72f),
        border = BorderStroke(
            width = if (selected) 1.2.dp else 0.8.dp,
            color = if (selected) primary else primary.copy(alpha = 0.25f)
        )
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 7.dp)
        ) {
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                color = if (selected) primary else MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun FeedScreen(
    modifier: Modifier = Modifier,
    bottomPadding: androidx.compose.ui.unit.Dp,
    news: List<NewsItem>,
    savedIds: Set<String>,
    settings: UserAppSettings,
    onToggleSave: (String) -> Unit,
    onDeletePost: (String) -> Unit
) {
    val context = LocalContext.current
    var selectedBrand by remember { mutableStateOf("Все") }
    var searchQuery by remember { mutableStateOf("") }
    var onlyFresh by remember { mutableStateOf(false) }

    val brands = listOf("Все", "Xiaomi", "Samsung", "Huawei", "OPPO", "Nothing", "Vivo")
    val filtered = remember(news, selectedBrand, searchQuery, onlyFresh) {
        news.filter { item ->
            val brandOk = selectedBrand == "Все" || item.brand.equals(selectedBrand, ignoreCase = true)
            val freshOk = !onlyFresh || item.isVeryFresh6h
            val queryOk = searchQuery.isBlank() ||
                item.title.contains(searchQuery, ignoreCase = true) ||
                item.summary.contains(searchQuery, ignoreCase = true) ||
                item.brand.contains(searchQuery, ignoreCase = true)
            brandOk && freshOk && queryOk
        }
    }

    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(start = 14.dp, end = 14.dp, top = 8.dp, bottom = bottomPadding),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Поиск по новостям и прошивкам...", fontSize = 13.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "Очистить")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    CompactPillChip(
                        label = "Свежие (6ч)",
                        selected = onlyFresh,
                        onClick = { onlyFresh = !onlyFresh }
                    )
                    brands.forEach { b ->
                        CompactPillChip(
                            label = b,
                            selected = selectedBrand == b,
                            onClick = { selectedBrand = b }
                        )
                    }
                }
            }
        }

        if (filtered.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 36.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Постов по выбранному фильтру пока нет",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
        } else {
            items(filtered, key = { it.id }) { item ->
                NewsCard(
                    item = item,
                    isSaved = item.id in savedIds,
                    settings = settings,
                    onOpen = { openCustomTab(context, item.url) },
                    onShare = { shareArticle(context, item) },
                    onDelete = { onDeletePost(item.id) },
                    onToggleSave = { onToggleSave(item.id) }
                )
            }
        }
    }
}

@Composable
fun ReviewsScreen(
    modifier: Modifier = Modifier,
    bottomPadding: androidx.compose.ui.unit.Dp,
    reviews: List<TechReviewArticle>,
    settings: UserAppSettings,
    onAddOrderedReview: (TechReviewArticle) -> Unit,
    onDeleteReview: (String) -> Unit
) {
    val context = LocalContext.current
    val primary = MaterialTheme.colorScheme.primary
    val scale = settings.fontScale

    var selectedCategory by remember { mutableStateOf("Все") }
    var openedArticle by remember { mutableStateOf<TechReviewArticle?>(null) }
    var showOrderDialog by remember { mutableStateOf(false) }
    var orderModel by remember { mutableStateOf("") }
    var orderNote by remember { mutableStateOf("") }

    val categories = listOf("Все", "Смартфоны", "Носимая электроника", "Ноутбуки", "Умный дом и ТВ")
    val filtered = remember(reviews, selectedCategory) {
        if (selectedCategory == "Все") reviews
        else reviews.filter { it.category.equals(selectedCategory, ignoreCase = true) }
    }

    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(start = 14.dp, end = 14.dp, top = 8.dp, bottom = bottomPadding),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Редакционные обзоры",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Button(
                        onClick = { showOrderDialog = true },
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Заказать обзор", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    categories.forEach { cat ->
                        CompactPillChip(
                            label = cat,
                            selected = selectedCategory == cat,
                            onClick = { selectedCategory = cat }
                        )
                    }
                }
            }
        }

        items(filtered, key = { it.id }) { article ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { openedArticle = article },
                shape = RoundedCornerShape(settings.cornerRadius.dp),
                border = BorderStroke(1.dp, primary.copy(alpha = 0.28f)),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(13.dp),
                    verticalArrangement = Arrangement.spacedBy(7.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = primary.copy(alpha = 0.16f)
                            ) {
                                Text(
                                    text = article.brand,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = primary,
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                )
                            }
                            Text(
                                text = "Автор: ${article.authorAgent}",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = Color(0xFFFBBF24),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(Modifier.width(3.dp))
                            Text(
                                text = article.score,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFFFBBF24)
                            )
                        }
                    }

                    Text(
                        text = article.headline,
                        fontSize = (15 * scale).sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Text(
                        text = article.leadSummary,
                        fontSize = (13 * scale).sp,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.76f)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = article.publishedAt,
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Surface(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { onDeleteReview(article.id) },
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFFF43F5E).copy(alpha = 0.12f)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.DeleteOutline,
                                        contentDescription = "Удалить обзор",
                                        tint = Color(0xFFF43F5E),
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }

                            Surface(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { shareReviewArticle(context, article) },
                                shape = RoundedCornerShape(8.dp),
                                color = primary.copy(alpha = 0.14f)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Share,
                                        contentDescription = "Поделиться",
                                        tint = primary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    openedArticle?.let { art ->
        AlertDialog(
            onDismissRequest = { openedArticle = null },
            title = {
                Text(
                    text = art.headline,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Автор: ${art.authorAgent} • Оценка: ${art.score}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = primary
                    )
                    Text(text = art.fullAnalysis, fontSize = 13.sp)
                    HorizontalDivider()
                    Text("Характеристики:", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    art.specs.forEach { sp ->
                        Text("• ${sp.label}: ${sp.value}", fontSize = 12.sp)
                    }
                    HorizontalDivider()
                    Text("Плюсы:", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
                    art.pros.forEach { p -> Text("+ $p", fontSize = 12.sp) }
                    Text("Минусы:", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFFF43F5E))
                    art.cons.forEach { c -> Text("− $c", fontSize = 12.sp) }
                }
            },
            confirmButton = {
                TextButton(onClick = { openedArticle = null }) {
                    Text("Закрыть")
                }
            },
            dismissButton = {
                TextButton(onClick = { shareReviewArticle(context, art) }) {
                    Text("Поделиться")
                }
            }
        )
    }

    if (showOrderDialog) {
        AlertDialog(
            onDismissRequest = { showOrderDialog = false },
            title = { Text("Заказать обзор агенту", fontSize = 16.sp, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = orderModel,
                        onValueChange = { orderModel = it },
                        label = { Text("Модель устройства") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = orderNote,
                        onValueChange = { orderNote = it },
                        label = { Text("Что проверить в первую очередь") },
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val name = orderModel.trim().ifBlank { "Xiaomi 17T Pro" }
                        val created = ReviewCatalogFactory.createCustomReviewByUserOrder(
                            rawDeviceName = name,
                            selectedBrand = "Авто",
                            selectedCategoryLabel = "Авто",
                            userFocusNote = orderNote,
                            assignedAgentName = "Вика"
                        )
                        onAddOrderedReview(created)
                        orderModel = ""
                        orderNote = ""
                        showOrderDialog = false
                    }
                ) {
                    Text("Создать обзор")
                }
            },
            dismissButton = {
                TextButton(onClick = { showOrderDialog = false }) {
                    Text("Отмена")
                }
            }
        )
    }
}

@Composable
fun SavedScreen(
    modifier: Modifier = Modifier,
    bottomPadding: androidx.compose.ui.unit.Dp,
    savedNews: List<NewsItem>,
    settings: UserAppSettings,
    onRemoveSaved: (String) -> Unit,
    onDeletePost: (String) -> Unit
) {
    val context = LocalContext.current
    if (savedNews.isEmpty()) {
        Box(
            modifier = modifier,
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "В избранном пока пусто — нажмите на закладку в ленте",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    } else {
        LazyColumn(
            modifier = modifier,
            contentPadding = PaddingValues(start = 14.dp, end = 14.dp, top = 8.dp, bottom = bottomPadding),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(savedNews, key = { it.id }) { item ->
                NewsCard(
                    item = item,
                    isSaved = true,
                    settings = settings,
                    onOpen = { openCustomTab(context, item.url) },
                    onShare = { shareArticle(context, item) },
                    onDelete = { onDeletePost(item.id) },
                    onToggleSave = { onRemoveSaved(item.id) }
                )
            }
        }
    }
}

// ============================================================================
// УТИЛИТЫ КАРТОЧЕК, НОВОГОДНИЙ ИИ-ДОМ И НАСТРОЙКИ
// ============================================================================

fun openCustomTab(context: Context, url: String) {
    try {
        val customTabsIntent = CustomTabsIntent.Builder()
            .setShowTitle(true)
            .build()
        customTabsIntent.launchUrl(context, Uri.parse(url))
    } catch (_: Exception) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (_: Exception) {
            Toast.makeText(context, "Не удалось открыть ссылку", Toast.LENGTH_SHORT).show()
        }
    }
}

fun shareArticle(context: Context, item: NewsItem) {
    val text = "${item.title}\n\nИсточник: ${item.sourceLabel} (${item.brand})\n${item.url}"
    val sendIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, item.title)
        putExtra(Intent.EXTRA_TEXT, text)
    }
    val chooser = Intent.createChooser(sendIntent, "Поделиться новостью").apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    context.startActivity(chooser)
}

fun shareReviewArticle(context: Context, article: TechReviewArticle) {
    val specsText = article.specs.joinToString("\n") { "• ${it.label}: ${it.value}" }
    val text = buildString {
        appendLine(article.headline)
        appendLine("Оценка редакции: ${article.score}")
        appendLine()
        appendLine(article.leadSummary)
        appendLine()
        appendLine("Основные ТТХ:")
        appendLine(specsText)
    }
    val sendIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, article.headline)
        putExtra(Intent.EXTRA_TEXT, text)
    }
    val chooser = Intent.createChooser(sendIntent, "Поделиться обзором").apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    context.startActivity(chooser)
}

@Composable
fun NewsCard(
    item: NewsItem,
    isSaved: Boolean,
    settings: UserAppSettings,
    onOpen: () -> Unit,
    onShare: () -> Unit,
    onDelete: () -> Unit,
    onToggleSave: () -> Unit
) {
    val primary = MaterialTheme.colorScheme.primary
    val scale = settings.fontScale
    val shape = RoundedCornerShape(settings.cornerRadius.dp)

    val containerColor = when (settings.cardStyle) {
        "filled" -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.35f)
        else -> MaterialTheme.colorScheme.surface
    }
    val borderStroke = when (settings.cardStyle) {
        "elevated" -> BorderStroke(0.5.dp, primary.copy(alpha = 0.14f))
        "filled" -> BorderStroke(0.8.dp, primary.copy(alpha = 0.22f))
        else -> BorderStroke(1.dp, primary.copy(alpha = 0.24f))
    }
    val elevation = if (settings.cardStyle == "elevated") {
        CardDefaults.cardElevation(defaultElevation = 3.dp)
    } else {
        CardDefaults.cardElevation(defaultElevation = 0.dp)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpen() },
        shape = shape,
        border = borderStroke,
        elevation = elevation,
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Column(
            modifier = Modifier.padding(13.dp),
            verticalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f).padding(end = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = primary.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = item.brand,
                            fontSize = (10 * scale).sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = primary,
                            maxLines = 1,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.07f)
                    ) {
                        Text(
                            text = item.platform,
                            fontSize = (10 * scale).sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                if (item.isVeryFresh6h) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFF10B981).copy(alpha = 0.16f)
                    ) {
                        Text(
                            text = "СВЕЖЕЕ",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF10B981),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Text(
                text = item.title,
                fontSize = (15 * scale).sp,
                fontWeight = FontWeight.Bold,
                lineHeight = (20 * scale).sp,
                color = MaterialTheme.colorScheme.onSurface
            )

            if (settings.showSummary && item.summary.isNotBlank()) {
                Text(
                    text = item.summary,
                    fontSize = (13 * scale).sp,
                    lineHeight = (18 * scale).sp,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.76f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${item.sourceLabel} • ${item.timeLabel}",
                    fontSize = (11 * scale).sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.58f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f).padding(end = 8.dp)
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onDelete() },
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFF43F5E).copy(alpha = 0.12f),
                        border = BorderStroke(0.8.dp, Color(0xFFF43F5E).copy(alpha = 0.35f))
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = "Удалить пост",
                                tint = Color(0xFFF43F5E),
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }

                    Surface(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onShare() },
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.background.copy(alpha = 0.8f),
                        border = BorderStroke(0.8.dp, primary.copy(alpha = 0.25f))
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Поделиться",
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }

                    Surface(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onToggleSave() },
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSaved) primary.copy(alpha = 0.18f)
                        else MaterialTheme.colorScheme.background.copy(alpha = 0.8f),
                        border = BorderStroke(0.8.dp, primary.copy(alpha = if (isSaved) 0.55f else 0.25f))
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (isSaved) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                                contentDescription = "В избранное",
                                tint = if (isSaved) primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }

                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onOpen() },
                        shape = RoundedCornerShape(8.dp),
                        color = primary.copy(alpha = 0.15f),
                        border = BorderStroke(0.9.dp, primary.copy(alpha = 0.42f))
                    ) {
                        Text(
                            text = "Читать ↗",
                            fontSize = (11 * scale).sp,
                            fontWeight = FontWeight.Bold,
                            color = primary,
                            maxLines = 1,
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

private val DefaultOfficeAgents = listOf(
    OfficeAgentProfile(
        id = 0,
        name = "Вика",
        role = "Обзоры смартфонов",
        roomTitle = "Каб. 1 • Смартфоны",
        accentColor = Color(0xFF38BDF8),
        hairColor = Color(0xFFF43F5E),
        isFemale = true,
        hasGlasses = false,
        isTopRow = true,
        deskChairPos = Offset(0.125f, 0.175f),
        roomDoorPos = Offset(0.125f, 0.295f),
        corridorEntryPos = Offset(0.125f, 0.355f),
        corridorLaneY = 0.375f,
        diningSeatPos = Offset(0.145f, 0.455f),
        loungeSeatPos = Offset(0.735f, 0.445f)
    ),
    OfficeAgentProfile(
        id = 1,
        name = "Артур",
        role = "Носимая электроника",
        roomTitle = "Каб. 2 • Часы и аудио",
        accentColor = Color(0xFF10B981),
        hairColor = Color(0xFF1E293B),
        isFemale = false,
        hasGlasses = false,
        isTopRow = true,
        deskChairPos = Offset(0.375f, 0.175f),
        roomDoorPos = Offset(0.375f, 0.295f),
        corridorEntryPos = Offset(0.375f, 0.355f),
        corridorLaneY = 0.395f,
        diningSeatPos = Offset(0.215f, 0.455f),
        loungeSeatPos = Offset(0.735f, 0.505f)
    ),
    OfficeAgentProfile(
        id = 2,
        name = "Нова",
        role = "Обзоры ноутбуков",
        roomTitle = "Каб. 3 • Ноутбуки",
        accentColor = Color(0xFFA78BFA),
        hairColor = Color(0xFFFBBF24),
        isFemale = true,
        hasGlasses = false,
        isTopRow = true,
        deskChairPos = Offset(0.625f, 0.175f),
        roomDoorPos = Offset(0.625f, 0.295f),
        corridorEntryPos = Offset(0.625f, 0.355f),
        corridorLaneY = 0.375f,
        diningSeatPos = Offset(0.145f, 0.555f),
        loungeSeatPos = Offset(0.735f, 0.565f)
    ),
    OfficeAgentProfile(
        id = 3,
        name = "Дом-Бот",
        role = "Обзоры ТВ и дома",
        roomTitle = "Каб. 4 • Умный дом",
        accentColor = Color(0xFF60A5FA),
        hairColor = Color(0xFF38BDF8),
        isFemale = false,
        hasGlasses = false,
        isTopRow = true,
        deskChairPos = Offset(0.875f, 0.175f),
        roomDoorPos = Offset(0.875f, 0.295f),
        corridorEntryPos = Offset(0.875f, 0.355f),
        corridorLaneY = 0.405f,
        diningSeatPos = Offset(0.215f, 0.555f),
        loungeSeatPos = Offset(0.805f, 0.445f)
    ),
    OfficeAgentProfile(
        id = 4,
        name = "Авто-ИИ",
        role = "Электроавто",
        roomTitle = "Каб. 5 • Электрокары",
        accentColor = Color(0xFF22D3EE),
        hairColor = Color(0xFF0F172A),
        isFemale = false,
        hasGlasses = false,
        isTopRow = false,
        deskChairPos = Offset(0.125f, 0.825f),
        roomDoorPos = Offset(0.125f, 0.685f),
        corridorEntryPos = Offset(0.125f, 0.625f),
        corridorLaneY = 0.605f,
        diningSeatPos = Offset(0.18f, 0.435f),
        loungeSeatPos = Offset(0.805f, 0.565f)
    ),
    OfficeAgentProfile(
        id = 5,
        name = "Виктор",
        role = "Замруководителя (1-й аудит)",
        roomTitle = "Каб. 6 • Зам. директора",
        accentColor = Color(0xFFF59E0B),
        hairColor = Color(0xFF475569),
        isFemale = false,
        hasGlasses = true,
        isTopRow = false,
        deskChairPos = Offset(0.375f, 0.825f),
        roomDoorPos = Offset(0.375f, 0.685f),
        corridorEntryPos = Offset(0.375f, 0.625f),
        corridorLaneY = 0.595f,
        diningSeatPos = Offset(0.18f, 0.585f),
        loungeSeatPos = Offset(0.765f, 0.435f)
    ),
    OfficeAgentProfile(
        id = 6,
        name = "Елена",
        role = "Фактчекинг ТТХ (2-й этап)",
        roomTitle = "Каб. 7 • Аудит ТТХ",
        accentColor = Color(0xFFEC4899),
        hairColor = Color(0xFF9333EA),
        isFemale = true,
        hasGlasses = true,
        isTopRow = false,
        deskChairPos = Offset(0.625f, 0.825f),
        roomDoorPos = Offset(0.625f, 0.685f),
        corridorEntryPos = Offset(0.625f, 0.625f),
        corridorLaneY = 0.605f,
        diningSeatPos = Offset(0.115f, 0.505f),
        loungeSeatPos = Offset(0.765f, 0.575f)
    ),
    OfficeAgentProfile(
        id = 7,
        name = "Аврора",
        role = "Директор редакции",
        roomTitle = "Каб. 8 • Директор",
        accentColor = Color(0xFF10B981),
        hairColor = Color(0xFFE11D48),
        isFemale = true,
        hasGlasses = false,
        isTopRow = false,
        deskChairPos = Offset(0.875f, 0.825f),
        roomDoorPos = Offset(0.875f, 0.685f),
        corridorEntryPos = Offset(0.875f, 0.625f),
        corridorLaneY = 0.585f,
        diningSeatPos = Offset(0.245f, 0.505f),
        loungeSeatPos = Offset(0.835f, 0.505f)
    )
)

private val AutonomousTaskTemplates = mapOf(
    0 to listOf(
        "Сверка матриц AMOLED и ШИМ флагманов 2026" to "Проверены пиковая яркость и частота ШИМ в 6 обзорах",
        "Анализ перископ-модулей Leica и ZEISS" to "Подтверждены фокусные расстояния и светосила f/2.6",
        "Проверка версий HyperOS и One UI в базе" to "Обновлены индексы региональных прошивок RU/CN"
    ),
    1 to listOf(
        "Тест автономности Huawei Watch Fit 5 с AOD" to "Зафиксировано 7.5 суток при активном мониторинге SpO2",
        "Проверка битрейта LHDC 5.0 и LDAC на TWS" to "Подтверждена стабильность 900–1000 кбит/с без обрывов",
        "Сравнение точности GPS-треков умных часов" to "Отклонение двухчастотного модуля менее 1.8 м"
    ),
    2 to listOf(
        "Замер теплопакета Intel Core Ultra в ультрабуках" to "Стабильные 38–40 Вт под длительной нагрузкой",
        "Проверка цветового охвата 3.1K OLED панелей" to "100% DCI-P3, заводская дельта E < 0.8"
    ),
    3 to listOf(
        "Тест антибликового слоя QD-OLED Glare Free" to "Отражения от дневного света снижены на 78%",
        "Проверка пропускной способности портов HDMI 2.1" to "Подтверждены 4K 144 Гц + VRR на всех 4 разъёмах"
    ),
    4 to listOf(
        "Расчёт кривой зарядки 800В батареи CATL Qilin" to "С 10% до 80% за 13.5 минут на станции 360 кВт",
        "Проверка работы лидара и автопилота NVIDIA Thor" to "Дальность распознавания препятствий до 250 метров"
    ),
    5 to listOf(
        "1-й этап аудита ТТХ опубликованных обзоров" to "Сверены стандарты Wi-Fi, Bluetooth и мощности ЗУ",
        "Контроль фактов по официальным даташитам брендов" to "Неточностей в карточках устройств не обнаружено"
    ),
    6 to listOf(
        "2-й глубокий этап перепроверки спецификаций" to "Перепроверены частота ШИМ, плотность PPI и аудиокодеки",
        "Финальная валидация таблиц характеристик" to "Все значения соответствуют официальным спецификациям"
    ),
    7 to listOf(
        "Приёмка отчётов и начисление премий отделу" to "Проверены тикеты сотрудников, начислены бонусы",
        "Контроль стандартов качества редакции" to "Самовольная генерация из новостей заблокирована"
    )
)

private fun buildInitialAgentStatuses(): Map<Int, AgentLiveStatus> {
    val nowStr = ZonedDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm"))
    return DefaultOfficeAgents.associate { ag ->
        val tpl = AutonomousTaskTemplates[ag.id]?.firstOrNull()
            ?: ("Мониторинг и проверка ТТХ" to "Спецификации проверены")
        ag.id to AgentLiveStatus(
            currentTask = SoloTaskItem(
                ticketNumber = 100 + ag.id,
                badge = "В РАБОТЕ",
                title = tpl.first,
                resultSummary = tpl.second,
                startedAt = nowStr
            ),
            progress = 0.28f + (ag.id * 0.08f) % 0.5f,
            targetZone = AgentLocationZone.DESK,
            breakSecondsRemaining = 0f,
            salaryBalance = 4800 + ag.id * 200,
            rejectionsCount = 0,
            reprimandsCount = 0,
            completedHistory = emptyList()
        )
    }
}

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    bottomPadding: androidx.compose.ui.unit.Dp,
    settings: UserAppSettings,
    newsList: List<NewsItem>,
    existingReviews: List<TechReviewArticle>,
    deletedTotalCount: Int,
    isLoading: Boolean,
    onSettingsChange: (UserAppSettings) -> Unit,
    onTriggerScan: () -> Unit,
    onNewReviewOrdered: (TechReviewArticle) -> Unit,
    onReviewsSilentEdited: (List<TechReviewArticle>) -> Unit,
    onPurgeAutoJunk: () -> Unit,
    onRestoreDeleted: () -> Unit,
    onClearCache: () -> Unit
) {
    val context = LocalContext.current
    val primary = MaterialTheme.colorScheme.primary

    val persisted = remember { LocalCache.loadOfficeState(context) }
    var ticketCounter by remember { mutableIntStateOf(persisted?.ticketCounter ?: 120) }
    var agentStatuses by remember {
        mutableStateOf(
            persisted?.agentStatuses?.takeIf { it.size == DefaultOfficeAgents.size }
                ?: buildInitialAgentStatuses()
        )
    }
    var chiefAuditLog by remember { mutableStateOf(persisted?.chiefAuditLog ?: emptyList()) }
    var deputyEditLog by remember { mutableStateOf(persisted?.deputyEditLog ?: emptyList()) }

    var agentPositions by remember {
        mutableStateOf(DefaultOfficeAgents.associate { it.id to it.deskChairPos })
    }
    var selectedAgentId by remember { mutableIntStateOf(0) }
    var isOfficeActive by remember { mutableStateOf(true) }
    var officeSubTab by remember { mutableIntStateOf(0) }

    var cmdAgentIndex by remember { mutableIntStateOf(0) }
    var cmdActionType by remember { mutableIntStateOf(0) }
    var cmdDeviceInput by remember { mutableStateOf("") }
    var cmdNoteInput by remember { mutableStateOf("") }

    val latestReviewsState by rememberUpdatedState(existingReviews)
    val latestNewsState by rememberUpdatedState(newsList)

    LaunchedEffect(isOfficeActive) {
        var saveTick = 0
        while (isActive && isOfficeActive) {
            delay(160L)
            val nowTime = ZonedDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"))
            val updatedStatuses = agentStatuses.toMutableMap()
            val updatedPositions = agentPositions.toMutableMap()
            var newChiefEntries = chiefAuditLog
            var newDeputyEntries = deputyEditLog
            var nextTicket = ticketCounter

            DefaultOfficeAgents.forEach { profile ->
                val status = updatedStatuses[profile.id] ?: return@forEach
                val curPos = updatedPositions[profile.id] ?: profile.deskChairPos

                val targetPoint = when (status.targetZone) {
                    AgentLocationZone.DESK -> profile.deskChairPos
                    AgentLocationZone.CHIEF_CHECK -> Offset(0.84f, 0.76f)
                    AgentLocationZone.DINING -> profile.diningSeatPos
                    AgentLocationZone.LOUNGE -> profile.loungeSeatPos
                }

                val dist = hypot(targetPoint.x - curPos.x, targetPoint.y - curPos.y)
                if (dist > 0.012f) {
                    val waypoint = when {
                        abs(curPos.y - profile.deskChairPos.y) < 0.05f &&
                            abs(curPos.x - profile.deskChairPos.x) < 0.05f &&
                            status.targetZone != AgentLocationZone.DESK -> profile.roomDoorPos

                        abs(curPos.y - profile.roomDoorPos.y) < 0.03f &&
                            status.targetZone != AgentLocationZone.DESK -> profile.corridorEntryPos

                        status.targetZone == AgentLocationZone.DESK &&
                            abs(curPos.x - profile.corridorEntryPos.x) > 0.03f &&
                            curPos.y > 0.31f && curPos.y < 0.67f ->
                            Offset(profile.corridorEntryPos.x, profile.corridorLaneY)

                        status.targetZone == AgentLocationZone.DESK &&
                            abs(curPos.x - profile.corridorEntryPos.x) <= 0.03f &&
                            abs(curPos.y - profile.roomDoorPos.y) > 0.03f -> profile.roomDoorPos

                        else -> targetPoint
                    }

                    val wDist = hypot(waypoint.x - curPos.x, waypoint.y - curPos.y).coerceAtLeast(0.001f)
                    val step = 0.014f
                    val nextPos = if (wDist <= step) {
                        waypoint
                    } else {
                        Offset(
                            x = curPos.x + ((waypoint.x - curPos.x) / wDist) * step,
                            y = curPos.y + ((waypoint.y - curPos.y) / wDist) * step
                        )
                    }
                    updatedPositions[profile.id] = nextPos
                } else {
                    updatedPositions[profile.id] = targetPoint
                    when (status.targetZone) {
                        AgentLocationZone.DESK -> {
                            val nextProg = (status.progress + 0.024f).coerceAtMost(1f)
                            if (nextProg < 1f) {
                                updatedStatuses[profile.id] = status.copy(progress = nextProg)
                            } else {
                                if (profile.id == 7) {
                                    nextTicket++
                                    val pool = AutonomousTaskTemplates[7]!!
                                    val nextTaskTpl = pool[nextTicket % pool.size]
                                    val doneTask = status.currentTask.copy(
                                        completedAt = nowTime,
                                        chiefApproved = true,
                                        salaryDelta = 450
                                    )
                                    updatedStatuses[profile.id] = status.copy(
                                        currentTask = SoloTaskItem(
                                            ticketNumber = nextTicket,
                                            badge = "ДИРЕКТОР",
                                            title = nextTaskTpl.first,
                                            resultSummary = nextTaskTpl.second,
                                            startedAt = nowTime
                                        ),
                                        progress = 0f,
                                        salaryBalance = status.salaryBalance + 450,
                                        completedHistory = (listOf(doneTask) + status.completedHistory).take(12)
                                    )
                                } else {
                                    updatedStatuses[profile.id] = status.copy(
                                        progress = 1f,
                                        targetZone = AgentLocationZone.CHIEF_CHECK
                                    )
                                }
                            }
                        }

                        AgentLocationZone.CHIEF_CHECK -> {
                            val approved = (nextTicket % 5) != 0
                            val delta = if (approved) 350 else -150
                            val nextReprimands = if (approved) status.reprimandsCount else status.reprimandsCount + 1
                            val isReattestation = !approved && nextReprimands >= 3
                            val finalReprimands = if (isReattestation) 0 else nextReprimands

                            val verdictText = when {
                                approved -> "Принято Авророй: премия +350 ₽"
                                isReattestation -> "3-й выговор! Аврора назначила переаттестацию (−150 ₽)"
                                else -> "Выговор от Авроры ($nextReprimands/3): доработать тикет (−150 ₽)"
                            }

                            val auditEntry = ChiefAuditEntry(
                                id = "chk_${System.currentTimeMillis()}_${profile.id}",
                                ticketNumber = status.currentTask.ticketNumber,
                                agentName = profile.name,
                                taskTitle = status.currentTask.title,
                                verdict = verdictText,
                                salaryDelta = delta,
                                hasReprimand = !approved,
                                isApproved = approved,
                                checkedAt = nowTime
                            )
                            newChiefEntries = (listOf(auditEntry) + newChiefEntries).take(25)

                            if (approved && (profile.id == 5 || profile.id == 6)) {
                                val (editedList, depLog) = ReviewCatalogFactory.deputyVerifyAndFixArticle(
                                    articles = latestReviewsState,
                                    newsList = latestNewsState
                                )
                                if (depLog != null) {
                                    onReviewsSilentEdited(editedList)
                                    newDeputyEntries = (listOf(depLog) + newDeputyEntries).take(25)
                                }
                            }

                            val doneTask = status.currentTask.copy(
                                completedAt = nowTime,
                                chiefApproved = approved,
                                salaryDelta = delta,
                                rejectReason = if (approved) "" else "Выговор от Авроры"
                            )

                            nextTicket++
                            val pool = AutonomousTaskTemplates[profile.id]
                                ?: listOf("Проверка спецификаций" to "Выполнено")
                            val nextTpl = pool[nextTicket % pool.size]
                            val takeBreak = approved && (nextTicket % 3 == 0)
                            val nextZone = if (takeBreak) {
                                if (profile.id % 2 == 0) AgentLocationZone.DINING else AgentLocationZone.LOUNGE
                            } else {
                                AgentLocationZone.DESK
                            }

                            updatedStatuses[profile.id] = status.copy(
                                currentTask = SoloTaskItem(
                                    ticketNumber = nextTicket,
                                    badge = if (approved) "В РАБОТЕ" else "ДОРАБОТКА",
                                    title = nextTpl.first,
                                    resultSummary = nextTpl.second,
                                    startedAt = nowTime
                                ),
                                progress = 0f,
                                targetZone = nextZone,
                                breakSecondsRemaining = if (takeBreak) 14f else 0f,
                                salaryBalance = (status.salaryBalance + delta).coerceAtLeast(0),
                                rejectionsCount = status.rejectionsCount + if (approved) 0 else 1,
                                reprimandsCount = finalReprimands,
                                completedHistory = (listOf(doneTask) + status.completedHistory).take(12)
                            )
                        }

                        AgentLocationZone.DINING, AgentLocationZone.LOUNGE -> {
                            val left = status.breakSecondsRemaining - 0.16f
                            if (left > 0f) {
                                updatedStatuses[profile.id] = status.copy(breakSecondsRemaining = left)
                            } else {
                                updatedStatuses[profile.id] = status.copy(
                                    targetZone = AgentLocationZone.DESK,
                                    breakSecondsRemaining = 0f
                                )
                            }
                        }
                    }
                }
            }

            ticketCounter = nextTicket
            agentStatuses = updatedStatuses
            agentPositions = updatedPositions
            chiefAuditLog = newChiefEntries
            deputyEditLog = newDeputyEntries

            saveTick++
            if (saveTick % 25 == 0) {
                LocalCache.saveOfficeState(
                    context = context,
                    ticketCounter = ticketCounter,
                    agentStatuses = agentStatuses,
                    chiefAuditLog = chiefAuditLog,
                    deputyEditLog = deputyEditLog
                )
            }
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "newYearHouseAnim")
    val garlandPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.28318f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "garlandPhase"
    )
    val snowPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 6500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "snowPhase"
    )
    val liveWorkPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "liveWorkPhase"
    )

    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(
            start = 14.dp,
            end = 14.dp,
            top = 6.dp,
            bottom = bottomPadding
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                border = BorderStroke(1.2.dp, primary.copy(alpha = 0.4f)),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(13.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Hub,
                                    contentDescription = null,
                                    tint = primary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    text = "Новогодний ИИ-Дом (8 агентов)",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Text(
                                text = "Личные кабинеты • Кухня • Лаунж с ТВ • Живая работа на КП",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
                            )
                        }

                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(9.dp))
                                .clickable { isOfficeActive = !isOfficeActive },
                            shape = RoundedCornerShape(9.dp),
                            color = if (isOfficeActive) Color(0xFF10B981).copy(alpha = 0.16f)
                            else Color(0xFFF59E0B).copy(alpha = 0.16f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (isOfficeActive) Icons.Default.CheckCircle else Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    tint = if (isOfficeActive) Color(0xFF10B981) else Color(0xFFF59E0B),
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    text = if (isOfficeActive) "Живой" else "Пауза",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isOfficeActive) Color(0xFF10B981) else Color(0xFFF59E0B)
                                )
                            }
                        }
                    }

                    AiNewYearHouseInteractiveMap(
                        agents = DefaultOfficeAgents,
                        positions = agentPositions,
                        statuses = agentStatuses,
                        selectedAgentId = selectedAgentId,
                        primaryColor = primary,
                        garlandPhase = garlandPhase,
                        snowPhase = snowPhase,
                        liveWorkPhase = liveWorkPhase,
                        onSelectAgent = { clickedId ->
                            selectedAgentId = clickedId
                            cmdAgentIndex = clickedId
                        }
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        DefaultOfficeAgents.forEach { ag ->
                            val isSel = ag.id == selectedAgentId
                            val st = agentStatuses[ag.id]
                            Surface(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable {
                                        selectedAgentId = ag.id
                                        cmdAgentIndex = ag.id
                                    },
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSel) ag.accentColor.copy(alpha = 0.22f)
                                else MaterialTheme.colorScheme.background.copy(alpha = 0.72f),
                                border = BorderStroke(
                                    1.dp,
                                    if (isSel) ag.accentColor else ag.accentColor.copy(alpha = 0.28f)
                                )
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(ag.accentColor)
                                    )
                                    Spacer(Modifier.width(5.dp))
                                    Text(
                                        text = ag.name,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSel) FontWeight.ExtraBold else FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    if ((st?.reprimandsCount ?: 0) > 0) {
                                        Spacer(Modifier.width(4.dp))
                                        Text(
                                            text = "⚠${st?.reprimandsCount}",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFF59E0B)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    val selectedProfile = DefaultOfficeAgents.first { it.id == selectedAgentId }
                    val selectedStatus = agentStatuses[selectedAgentId]
                    if (selectedStatus != null) {
                        val approvedCount = selectedStatus.completedHistory.count { it.chiefApproved }
                        Surface(
                            shape = RoundedCornerShape(13.dp),
                            color = MaterialTheme.colorScheme.background.copy(alpha = 0.82f),
                            border = BorderStroke(1.dp, selectedProfile.accentColor.copy(alpha = 0.48f))
                        ) {
                            Column(
                                modifier = Modifier.padding(10.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f).padding(end = 6.dp)) {
                                        Text(
                                            text = "${selectedProfile.name} • ${selectedProfile.role}",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = selectedProfile.accentColor,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = "${selectedProfile.roomTitle} • ${selectedStatus.targetZone.label}",
                                            fontSize = 10.sp,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
                                        )
                                    }
                                    Surface(
                                        shape = RoundedCornerShape(7.dp),
                                        color = Color(0xFF10B981).copy(alpha = 0.16f)
                                    ) {
                                        Text(
                                            text = "${selectedStatus.salaryBalance} ₽",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color(0xFF10B981),
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Surface(
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(7.dp),
                                        color = Color(0xFF10B981).copy(alpha = 0.11f)
                                    ) {
                                        Text(
                                            text = "Премий: $approvedCount",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF10B981),
                                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp)
                                        )
                                    }
                                    Surface(
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(7.dp),
                                        color = if (selectedStatus.reprimandsCount > 0) {
                                            Color(0xFFF43F5E).copy(alpha = 0.14f)
                                        } else {
                                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f)
                                        }
                                    ) {
                                        Text(
                                            text = "Выговоры: ${selectedStatus.reprimandsCount}/3",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (selectedStatus.reprimandsCount > 0) Color(0xFFF43F5E)
                                            else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f),
                                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp)
                                        )
                                    }
                                    Surface(
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(7.dp),
                                        color = selectedProfile.accentColor.copy(alpha = 0.12f)
                                    ) {
                                        Text(
                                            text = "Тикет #${selectedStatus.currentTask.ticketNumber}",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = selectedProfile.accentColor,
                                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp)
                                        )
                                    }
                                }

                                Text(
                                    text = "На экране КП: ${selectedStatus.currentTask.title}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )

                                LinearProgressIndicator(
                                    progress = { selectedStatus.progress.coerceIn(0f, 1f) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(5.dp)
                                        .clip(CircleShape),
                                    color = selectedProfile.accentColor,
                                    trackColor = selectedProfile.accentColor.copy(alpha = 0.18f)
                                )

                                if (selectedProfile.id != 7) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Surface(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(8.dp))
                                                .clickable {
                                                    agentStatuses = agentStatuses.toMutableMap().apply {
                                                        this[selectedProfile.id] = selectedStatus.copy(
                                                            progress = 1f,
                                                            targetZone = AgentLocationZone.CHIEF_CHECK
                                                        )
                                                    }
                                                },
                                            shape = RoundedCornerShape(8.dp),
                                            color = Color(0xFF38BDF8).copy(alpha = 0.14f),
                                            border = BorderStroke(0.8.dp, Color(0xFF38BDF8).copy(alpha = 0.4f))
                                        ) {
                                            Text(
                                                text = "На приёмку к Авроре",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF38BDF8),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                                            )
                                        }

                                        Surface(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(8.dp))
                                                .clickable {
                                                    agentStatuses = agentStatuses.toMutableMap().apply {
                                                        this[selectedProfile.id] = selectedStatus.copy(
                                                            targetZone = if (selectedProfile.id % 2 == 0) {
                                                                AgentLocationZone.DINING
                                                            } else {
                                                                AgentLocationZone.LOUNGE
                                                            },
                                                            breakSecondsRemaining = 15f
                                                        )
                                                    }
                                                },
                                            shape = RoundedCornerShape(8.dp),
                                            color = Color(0xFFF59E0B).copy(alpha = 0.14f),
                                            border = BorderStroke(0.8.dp, Color(0xFFF59E0B).copy(alpha = 0.4f))
                                        ) {
                                            Text(
                                                text = "Отправить на отдых",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFFF59E0B),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                                            )
                                        }
                                    }
                                }

                                if (selectedStatus.completedHistory.isNotEmpty()) {
                                    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                        Text(
                                            text = "Последние закрытые тикеты:",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                        )
                                        selectedStatus.completedHistory.take(2).forEach { done ->
                                            val deltaSign = if (done.salaryDelta >= 0) "+${done.salaryDelta}" else "${done.salaryDelta}"
                                            Text(
                                                text = "• #${done.ticketNumber} ${done.title} ($deltaSign ₽)",
                                                fontSize = 10.sp,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                                color = if (done.chiefApproved) Color(0xFF10B981) else Color(0xFFF43F5E)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        CompactPillChip(
                            label = "Командный центр",
                            selected = officeSubTab == 0,
                            onClick = { officeSubTab = 0 }
                        )
                        CompactPillChip(
                            label = "Аврора (${chiefAuditLog.size})",
                            selected = officeSubTab == 1,
                            onClick = { officeSubTab = 1 }
                        )
                        CompactPillChip(
                            label = "Аудит ТТХ (${deputyEditLog.size})",
                            selected = officeSubTab == 2,
                            onClick = { officeSubTab = 2 }
                        )
                    }

                    when (officeSubTab) {
                        0 -> {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = primary.copy(alpha = 0.08f),
                                border = BorderStroke(1.dp, primary.copy(alpha = 0.28f))
                            ) {
                                Column(
                                    modifier = Modifier.padding(10.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.AdminPanelSettings,
                                            contentDescription = null,
                                            tint = primary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(Modifier.width(6.dp))
                                        Text(
                                            text = "Командный центр владельца",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }

                                    Text(
                                        text = "Выберите сотрудника и отдайте прямой приказ:",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                    )

                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .horizontalScroll(rememberScrollState()),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        DefaultOfficeAgents.forEachIndexed { index, ag ->
                                            CompactPillChip(
                                                label = ag.name,
                                                selected = cmdAgentIndex == index,
                                                onClick = {
                                                    cmdAgentIndex = index
                                                    selectedAgentId = ag.id
                                                }
                                            )
                                        }
                                    }

                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .horizontalScroll(rememberScrollState()),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        listOf(
                                            "Обзор устройства",
                                            "Сверить ТТХ",
                                            "Отправить на кофе"
                                        ).forEachIndexed { idx, actLabel ->
                                            CompactPillChip(
                                                label = actLabel,
                                                selected = cmdActionType == idx,
                                                onClick = { cmdActionType = idx }
                                            )
                                        }
                                    }

                                    if (cmdActionType == 0) {
                                        OutlinedTextField(
                                            value = cmdDeviceInput,
                                            onValueChange = { cmdDeviceInput = it },
                                            modifier = Modifier.fillMaxWidth(),
                                            label = { Text("Устройство для обзора (напр. Xiaomi 17T Pro)", fontSize = 11.sp) },
                                            singleLine = true,
                                            shape = RoundedCornerShape(10.dp)
                                        )
                                        OutlinedTextField(
                                            value = cmdNoteInput,
                                            onValueChange = { cmdNoteInput = it },
                                            modifier = Modifier.fillMaxWidth(),
                                            label = { Text("На что сделать акцент (необязательно)", fontSize = 11.sp) },
                                            singleLine = true,
                                            shape = RoundedCornerShape(10.dp)
                                        )
                                    }

                                    Button(
                                        onClick = {
                                            val targetAgent = DefaultOfficeAgents[cmdAgentIndex]
                                            val nowStr = ZonedDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"))
                                            ticketCounter += 1

                                            when (cmdActionType) {
                                                0 -> {
                                                    val model = cmdDeviceInput.trim().ifBlank { "Xiaomi 17T Pro" }
                                                    val created = ReviewCatalogFactory.createCustomReviewByUserOrder(
                                                        rawDeviceName = model,
                                                        selectedBrand = "Авто",
                                                        selectedCategoryLabel = "Авто",
                                                        userFocusNote = cmdNoteInput,
                                                        assignedAgentName = targetAgent.name
                                                    )
                                                    onNewReviewOrdered(created)

                                                    val curSt = agentStatuses[targetAgent.id]
                                                    if (curSt != null) {
                                                        val doneOrder = SoloTaskItem(
                                                            ticketNumber = ticketCounter,
                                                            badge = "ПРИКАЗ",
                                                            title = "Обзор по приказу: ${created.deviceName}",
                                                            resultSummary = "Опубликован в разделе «Обзоры»",
                                                            startedAt = nowStr,
                                                            completedAt = nowStr,
                                                            chiefApproved = true,
                                                            salaryDelta = 500
                                                        )
                                                        agentStatuses = agentStatuses.toMutableMap().apply {
                                                            this[targetAgent.id] = curSt.copy(
                                                                currentTask = doneOrder,
                                                                progress = 1f,
                                                                targetZone = AgentLocationZone.DESK,
                                                                salaryBalance = curSt.salaryBalance + 500,
                                                                completedHistory = (listOf(doneOrder) + curSt.completedHistory).take(12)
                                                            )
                                                        }
                                                    }
                                                    cmdDeviceInput = ""
                                                    cmdNoteInput = ""
                                                    Toast.makeText(
                                                        context,
                                                        "${targetAgent.name} выполнил(а) приказ: обзор ${created.deviceName}",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                                1 -> {
                                                    val (editedList, depLog) = ReviewCatalogFactory.deputyVerifyAndFixArticle(
                                                        articles = existingReviews,
                                                        newsList = newsList
                                                    )
                                                    if (depLog != null) {
                                                        onReviewsSilentEdited(editedList)
                                                        deputyEditLog = (listOf(depLog.copy(reviewerName = targetAgent.name)) + deputyEditLog).take(25)
                                                        Toast.makeText(
                                                            context,
                                                            "${targetAgent.name} проверил(а) ТТХ: ${depLog.deviceName}",
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                    }
                                                }
                                                2 -> {
                                                    val curSt = agentStatuses[targetAgent.id]
                                                    if (curSt != null) {
                                                        agentStatuses = agentStatuses.toMutableMap().apply {
                                                            this[targetAgent.id] = curSt.copy(
                                                                targetZone = AgentLocationZone.DINING,
                                                                breakSecondsRemaining = 18f
                                                            )
                                                        }
                                                        Toast.makeText(
                                                            context,
                                                            "${targetAgent.name} отправлен(а) на кухню за кофе",
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                    }
                                                }
                                            }
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(15.dp))
                                        Spacer(Modifier.width(6.dp))
                                        Text(
                                            text = "Отправить приказ агенту ${DefaultOfficeAgents[cmdAgentIndex].name}",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }

                        1 -> {
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                if (chiefAuditLog.isEmpty()) {
                                    Text(
                                        text = "Журнал приёмки задач Директором Авророй пока пуст — дождитесь завершения цикла агента.",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
                                    )
                                } else {
                                    chiefAuditLog.take(6).forEach { entry ->
                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = MaterialTheme.colorScheme.background.copy(alpha = 0.72f)
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(8.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    imageVector = if (entry.isApproved) Icons.Default.CheckCircle else Icons.Default.Warning,
                                                    contentDescription = null,
                                                    tint = if (entry.isApproved) Color(0xFF10B981) else Color(0xFFF59E0B),
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(Modifier.width(8.dp))
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        text = "#${entry.ticketNumber} ${entry.agentName} • ${entry.checkedAt}",
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
                                                    )
                                                    Text(
                                                        text = entry.verdict,
                                                        fontSize = 11.sp,
                                                        color = MaterialTheme.colorScheme.onSurface
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        2 -> {
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                if (deputyEditLog.isEmpty()) {
                                    Text(
                                        text = "Замруководителя Виктор и Елена пока не вносили правок в ТТХ.",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
                                    )
                                } else {
                                    deputyEditLog.take(6).forEach { dep ->
                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = MaterialTheme.colorScheme.background.copy(alpha = 0.72f)
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(8.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.EditNote,
                                                    contentDescription = null,
                                                    tint = Color(0xFF38BDF8),
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(Modifier.width(8.dp))
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        text = "${dep.reviewerName} → ${dep.deviceName} (${dep.editedAt})",
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color(0xFF38BDF8)
                                                    )
                                                    Text(
                                                        text = dep.correctionDetail,
                                                        fontSize = 11.sp,
                                                        color = MaterialTheme.colorScheme.onSurface
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                border = BorderStroke(1.dp, primary.copy(alpha = 0.25f)),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = null,
                            tint = primary,
                            modifier = Modifier.size(17.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = "Управление контентом и памятью",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = onRestoreDeleted,
                            enabled = deletedTotalCount > 0,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(11.dp)
                        ) {
                            Text(
                                text = "Восстановить удалённое ($deletedTotalCount)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        OutlinedButton(
                            onClick = {
                                onPurgeAutoJunk()
                                Toast.makeText(context, "Авто-обзоры из новостей очищены", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(11.dp)
                        ) {
                            Text(
                                text = "Очистить мусор",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onTriggerScan,
                            enabled = !isLoading,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(11.dp)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(Modifier.width(5.dp))
                            Text("Обновить ленту", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = onClearCache,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(11.dp)
                        ) {
                            Icon(Icons.Default.Cancel, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(Modifier.width(5.dp))
                            Text("Сбросить кэш", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                border = BorderStroke(1.dp, primary.copy(alpha = 0.25f)),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Внешний вид и тема",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "Цветовая акцентуация:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f)
                        )
                        AppPalettes.chunked(3).forEach { rowPalettes ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                rowPalettes.forEach { pal ->
                                    val isSel = settings.paletteId == pal.id
                                    Surface(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(11.dp))
                                            .clickable {
                                                onSettingsChange(settings.copy(paletteId = pal.id))
                                            },
                                        shape = RoundedCornerShape(11.dp),
                                        color = if (isSel) pal.primaryDark.copy(alpha = 0.18f)
                                        else MaterialTheme.colorScheme.background.copy(alpha = 0.7f),
                                        border = BorderStroke(
                                            1.2.dp,
                                            if (isSel) pal.primaryDark else pal.primaryDark.copy(alpha = 0.25f)
                                        )
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(12.dp)
                                                    .clip(CircleShape)
                                                    .background(pal.primaryDark)
                                            )
                                            Spacer(Modifier.width(6.dp))
                                            Text(
                                                text = pal.name,
                                                fontSize = 11.sp,
                                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "Режим экрана:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f)
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf(
                                "system" to "Авто",
                                "light" to "Светлая",
                                "dark" to "Тёмная",
                                "amoled" to "AMOLED"
                            ).forEach { (modeKey, modeTitle) ->
                                StyledFilterChip(
                                    label = modeTitle,
                                    selected = settings.darkMode == modeKey,
                                    onClick = { onSettingsChange(settings.copy(darkMode = modeKey)) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "Стиль карточек:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f)
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf(
                                "outlined" to "Контур",
                                "elevated" to "Тень",
                                "filled" to "Заливка"
                            ).forEach { (stKey, stTitle) ->
                                StyledFilterChip(
                                    label = stTitle,
                                    selected = settings.cardStyle == stKey,
                                    onClick = { onSettingsChange(settings.copy(cardStyle = stKey)) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Скругление карточек:",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f)
                            )
                            Text(
                                text = "${settings.cornerRadius} dp",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = primary
                            )
                        }
                        Slider(
                            value = settings.cornerRadius.toFloat(),
                            onValueChange = {
                                onSettingsChange(settings.copy(cornerRadius = it.toInt()))
                            },
                            valueRange = 8f..24f,
                            steps = 3
                        )
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "Масштаб шрифта:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f)
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf(
                                0.9f to "Компакт",
                                1.0f to "Обычный",
                                1.1f to "Крупный",
                                1.2f to "Макси"
                            ).forEach { (sc, scTitle) ->
                                StyledFilterChip(
                                    label = scTitle,
                                    selected = abs(settings.fontScale - sc) < 0.03f,
                                    onClick = { onSettingsChange(settings.copy(fontScale = sc)) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Показывать краткий анонс в новостях",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f).padding(end = 8.dp)
                        )
                        Switch(
                            checked = settings.showSummary,
                            onCheckedChange = { onSettingsChange(settings.copy(showSummary = it)) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AiNewYearHouseInteractiveMap(
    agents: List<OfficeAgentProfile>,
    positions: Map<Int, Offset>,
    statuses: Map<Int, AgentLiveStatus>,
    selectedAgentId: Int,
    primaryColor: Color,
    garlandPhase: Float,
    snowPhase: Float,
    liveWorkPhase: Float,
    onSelectAgent: (Int) -> Unit
) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .height(370.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFF060B15))
            .border(1.2.dp, primaryColor.copy(alpha = 0.45f), RoundedCornerShape(14.dp))
            .pointerInput(Unit) {
                detectTapGestures { tapOffset ->
                    val w = size.width.toFloat().coerceAtLeast(1f)
                    val h = size.height.toFloat().coerceAtLeast(1f)
                    val normTap = Offset(tapOffset.x / w, tapOffset.y / h)
                    val nearest = agents.minByOrNull { ag ->
                        val p = positions[ag.id] ?: ag.deskChairPos
                        hypot(p.x - normTap.x, p.y - normTap.y)
                    }
                    if (nearest != null) {
                        onSelectAgent(nearest.id)
                    }
                }
            }
    ) {
        val boxW = maxWidth
        val boxH = maxHeight

        Canvas(modifier = Modifier.fillMaxSize()) {
            drawAiNewYearHouseFloorplan(
                agents = agents,
                positions = positions,
                statuses = statuses,
                selectedId = selectedAgentId,
                garlandPhase = garlandPhase,
                snowPhase = snowPhase,
                liveWorkPhase = liveWorkPhase
            )
        }

        // Подписи верхних 4 личных кабинетов
        for (col in 0 until 4) {
            val ag = agents[col]
            val roomCenterX = boxW * ((col + 0.5f) / 4f)
            Box(
                modifier = Modifier
                    .offset(x = roomCenterX - 38.dp, y = 4.dp)
                    .width(76.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${col + 1}. ${ag.name}",
                    fontSize = 8.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = ag.accentColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        // Подписи нижних 4 личных кабинетов
        for (col in 0 until 4) {
            val ag = agents[col + 4]
            val roomCenterX = boxW * ((col + 0.5f) / 4f)
            Box(
                modifier = Modifier
                    .offset(x = roomCenterX - 40.dp, y = boxH - 15.dp)
                    .width(80.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (ag.id == 7) "★ Директор" else "${col + 5}. ${ag.name}",
                    fontSize = 8.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (ag.id == 7) Color(0xFFFBBF24) else ag.accentColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        // Подписи Кухни у стены и Комнаты отдыха с ТВ
        Text(
            text = "КУХНЯ",
            fontSize = 7.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFFFBBF24).copy(alpha = 0.9f),
            modifier = Modifier.offset(x = boxW * 0.085f, y = boxH * 0.34f)
        )
        Text(
            text = "ОТДЫХ • ТВ",
            fontSize = 7.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFF38BDF8).copy(alpha = 0.9f),
            modifier = Modifier.offset(x = boxW * 0.695f, y = boxH * 0.34f)
        )

        // Плавающие бейджи с именами над каждым из 8 человечков
        agents.forEach { ag ->
            val pos = positions[ag.id] ?: ag.deskChairPos
            val isSel = ag.id == selectedAgentId
            val tagX = (boxW * pos.x - 26.dp).coerceIn(2.dp, boxW - 54.dp)
            val tagY = (boxH * pos.y - 29.dp).coerceIn(13.dp, boxH - 28.dp)

            Surface(
                modifier = Modifier
                    .offset(x = tagX, y = tagY)
                    .width(52.dp),
                shape = RoundedCornerShape(5.dp),
                color = if (isSel) ag.accentColor else Color(0xFF09111E).copy(alpha = 0.88f),
                border = BorderStroke(
                    width = if (isSel) 1.1.dp else 0.6.dp,
                    color = ag.accentColor.copy(alpha = if (isSel) 1f else 0.7f)
                )
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = ag.name,
                        fontSize = 7.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isSel) Color(0xFF06101D) else Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(horizontal = 3.dp, vertical = 1.dp)
                    )
                }
            }
        }
    }
}

private fun DrawScope.drawAiNewYearHouseFloorplan(
    agents: List<OfficeAgentProfile>,
    positions: Map<Int, Offset>,
    statuses: Map<Int, AgentLiveStatus>,
    selectedId: Int,
    garlandPhase: Float,
    snowPhase: Float,
    liveWorkPhase: Float
) {
    val w = size.width
    val h = size.height

    // 1. Тёплый паркетный пол центрального холла с мягким градиентом
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(Color(0xFF0E192D), Color(0xFF13223B), Color(0xFF0E192D)),
            startY = h * 0.28f,
            endY = h * 0.72f
        ),
        topLeft = Offset(0f, h * 0.28f),
        size = Size(w, h * 0.44f)
    )
    // Тонкие линии паркета в холле
    for (pRow in 1..6) {
        val py = h * (0.28f + pRow * 0.062f)
        drawLine(
            color = Color.White.copy(alpha = 0.03f),
            start = Offset(0f, py),
            end = Offset(w, py),
            strokeWidth = 0.8.dp.toPx()
        )
    }

    // 2. ВОСЕМЬ ЛИЧНЫХ КАБИНЕТОВ С НОВОГОДНИМ ДЕКОРОМ И ЖИВЫМИ ЭКРАНАМИ КП
    val colWidth = w / 4f
    val bulbColors = listOf(Color(0xFFF43F5E), Color(0xFFFBBF24), Color(0xFF38BDF8), Color(0xFF10B981))

    for (col in 0 until 4) {
        val x0 = col * colWidth + 2.5.dp.toPx()
        val roomW = colWidth - 5.dp.toPx()

        // --- ВЕРХНИЙ ЛИЧНЫЙ КАБИНЕТ ---
        val topAg = agents[col]
        val topSt = statuses[topAg.id]
        val isTopSel = selectedId == topAg.id
        val topRoomH = h * 0.265f

        drawRoundRect(
            brush = Brush.verticalGradient(
                colors = listOf(Color(0xFF172845), Color(0xFF101C33)),
                startY = 2.dp.toPx(),
                endY = 2.dp.toPx() + topRoomH
            ),
            topLeft = Offset(x0, 2.dp.toPx()),
            size = Size(roomW, topRoomH),
            cornerRadius = CornerRadius(9.dp.toPx(), 9.dp.toPx())
        )
        drawRoundRect(
            color = topAg.accentColor.copy(alpha = if (isTopSel) 0.92f else 0.36f),
            topLeft = Offset(x0, 2.dp.toPx()),
            size = Size(roomW, topRoomH),
            cornerRadius = CornerRadius(9.dp.toPx(), 9.dp.toPx()),
            style = Stroke(width = if (isTopSel) 1.8.dp.toPx() else 1.dp.toPx())
        )
        // Дверной проём в холл
        drawRect(
            color = Color(0xFF101D33),
            topLeft = Offset(x0 + roomW * 0.36f, topRoomH),
            size = Size(roomW * 0.28f, 6.dp.toPx())
        )
        // Новогодний венок с красным бантом на стене кабинета
        drawFestiveWreath(center = Offset(x0 + roomW * 0.14f, 19.dp.toPx()), radius = 4.2.dp.toPx())
        // Новогодняя гирлянда под потолком кабинета
        for (b in 0 until 5) {
            val bx = x0 + roomW * (0.25f + b * 0.15f)
            val bAlpha = 0.45f + 0.55f * abs(sin(garlandPhase + b * 0.9f + col))
            drawCircle(
                color = bulbColors[(b + col) % bulbColors.size].copy(alpha = bAlpha),
                radius = 1.6.dp.toPx(),
                center = Offset(bx, 15.dp.toPx())
            )
        }

        // Рабочий стол + КП работника с живым отображением работы (верхний ряд)
        drawWorkstationWithLivePc(
            deskLeft = x0 + roomW * 0.18f,
            deskTop = h * 0.068f,
            deskWidth = roomW * 0.64f,
            deskHeight = h * 0.048f,
            accentColor = topAg.accentColor,
            progress = topSt?.progress ?: 0.5f,
            liveWorkPhase = liveWorkPhase + col * 0.21f,
            isTopRow = true
        )

        // --- НИЖНИЙ ЛИЧНЫЙ КАБИНЕТ ---
        val botAg = agents[col + 4]
        val botSt = statuses[botAg.id]
        val isBotSel = selectedId == botAg.id
        val isDirector = botAg.id == 7
        val botRoomTop = h * 0.715f
        val botRoomH = h * 0.265f

        drawRoundRect(
            brush = Brush.verticalGradient(
                colors = if (isDirector) listOf(Color(0xFF1E2638), Color(0xFF282218))
                else listOf(Color(0xFF101C33), Color(0xFF172845)),
                startY = botRoomTop,
                endY = botRoomTop + botRoomH
            ),
            topLeft = Offset(x0, botRoomTop),
            size = Size(roomW, botRoomH),
            cornerRadius = CornerRadius(9.dp.toPx(), 9.dp.toPx())
        )
        drawRoundRect(
            color = if (isDirector) {
                Color(0xFFFBBF24).copy(alpha = if (isBotSel) 0.95f else 0.58f)
            } else {
                botAg.accentColor.copy(alpha = if (isBotSel) 0.92f else 0.36f)
            },
            topLeft = Offset(x0, botRoomTop),
            size = Size(roomW, botRoomH),
            cornerRadius = CornerRadius(9.dp.toPx(), 9.dp.toPx()),
            style = Stroke(width = if (isBotSel || isDirector) 1.8.dp.toPx() else 1.dp.toPx())
        )
        // Дверной проём нижнего кабинета
        drawRect(
            color = Color(0xFF101D33),
            topLeft = Offset(x0 + roomW * 0.36f, botRoomTop - 4.dp.toPx()),
            size = Size(roomW * 0.28f, 6.dp.toPx())
        )
        // Новогодний венок и гирлянда в нижнем кабинете
        drawFestiveWreath(center = Offset(x0 + roomW * 0.86f, botRoomTop + 14.dp.toPx()), radius = 4.2.dp.toPx())
        for (b in 0 until 5) {
            val bx = x0 + roomW * (0.12f + b * 0.15f)
            val bAlpha = 0.45f + 0.55f * abs(sin(garlandPhase + b * 0.9f + col + 2f))
            drawCircle(
                color = bulbColors[(b + col + 1) % bulbColors.size].copy(alpha = bAlpha),
                radius = 1.6.dp.toPx(),
                center = Offset(bx, botRoomTop + botRoomH - 14.dp.toPx())
            )
        }

        // Рабочий стол + КП работника с живым отображением работы (нижний ряд)
        drawWorkstationWithLivePc(
            deskLeft = x0 + roomW * 0.18f,
            deskTop = botRoomTop + botRoomH * 0.56f,
            deskWidth = roomW * 0.64f,
            deskHeight = h * 0.048f,
            accentColor = if (isDirector) Color(0xFFFBBF24) else botAg.accentColor,
            progress = botSt?.progress ?: 0.5f,
            liveWorkPhase = liveWorkPhase + (col + 4) * 0.19f,
            isTopRow = false
        )
    }

    // 3. НЕБОЛЬШАЯ КУХНЯ У ЛЕВОЙ СТЕНЫ
    val kitchenLeft = 3.dp.toPx()
    val kitchenTop = h * 0.33f
    val kitchenW = w * 0.255f
    val kitchenH = h * 0.34f

    // Плиточный пол кухни
    drawRoundRect(
        color = Color(0xFF16253E),
        topLeft = Offset(kitchenLeft, kitchenTop),
        size = Size(kitchenW, kitchenH),
        cornerRadius = CornerRadius(10.dp.toPx(), 10.dp.toPx())
    )
    drawRoundRect(
        color = Color(0xFFFBBF24).copy(alpha = 0.28f),
        topLeft = Offset(kitchenLeft, kitchenTop),
        size = Size(kitchenW, kitchenH),
        cornerRadius = CornerRadius(10.dp.toPx(), 10.dp.toPx()),
        style = Stroke(width = 1.dp.toPx())
    )
    // Кухонный гарнитур (столешница вплотную к левой стене)
    val counterW = 14.dp.toPx()
    drawRoundRect(
        color = Color(0xFF334155),
        topLeft = Offset(kitchenLeft + 2.dp.toPx(), kitchenTop + 14.dp.toPx()),
        size = Size(counterW, kitchenH - 22.dp.toPx()),
        cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx())
    )
    // Мойка с краном
    drawRect(
        color = Color(0xFF64748B),
        topLeft = Offset(kitchenLeft + 4.dp.toPx(), kitchenTop + 20.dp.toPx()),
        size = Size(9.dp.toPx(), 11.dp.toPx())
    )
    drawCircle(
        color = Color(0xFF38BDF8),
        radius = 2.dp.toPx(),
        center = Offset(kitchenLeft + 8.5.dp.toPx(), kitchenTop + 25.5.dp.toPx())
    )
    // Кофемашина на столешнице + поднимающийся пар
    val coffeeY = kitchenTop + 42.dp.toPx()
    drawRoundRect(
        color = Color(0xFF0F172A),
        topLeft = Offset(kitchenLeft + 4.dp.toPx(), coffeeY),
        size = Size(10.dp.toPx(), 13.dp.toPx()),
        cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
    )
    drawCircle(
        color = Color(0xFFEF4444),
        radius = 1.4.dp.toPx(),
        center = Offset(kitchenLeft + 11.dp.toPx(), coffeeY + 3.dp.toPx())
    )
    // Анимированный пар от свежего кофе
    val steamRise = (liveWorkPhase * 8.dp.toPx())
    drawCircle(
        color = Color.White.copy(alpha = (1f - liveWorkPhase) * 0.55f),
        radius = 2.dp.toPx(),
        center = Offset(kitchenLeft + 15.dp.toPx() + sin(liveWorkPhase * 6.28f) * 2f, coffeeY + 4.dp.toPx() - steamRise)
    )
    // Небольшой обеденный стол со стульями у кухни
    drawRoundRect(
        color = Color(0xFFB45309),
        topLeft = Offset(kitchenLeft + counterW + 12.dp.toPx(), kitchenTop + kitchenH * 0.34f),
        size = Size(18.dp.toPx(), kitchenH * 0.34f),
        cornerRadius = CornerRadius(5.dp.toPx(), 5.dp.toPx())
    )

    // 4. КОМНАТА ОТДЫХА СПРАВА С ДИВАНОМ И ТВ НА ВСЮ СТЕНУ (С ЖИВЫМ КОНТЕНТОМ)
    val loungeLeft = w * 0.655f
    val loungeTop = h * 0.33f
    val loungeW = w - loungeLeft - 3.dp.toPx()
    val loungeH = h * 0.34f

    drawRoundRect(
        color = Color(0xFF142138),
        topLeft = Offset(loungeLeft, loungeTop),
        size = Size(loungeW, loungeH),
        cornerRadius = CornerRadius(10.dp.toPx(), 10.dp.toPx())
    )
    drawRoundRect(
        color = Color(0xFF38BDF8).copy(alpha = 0.32f),
        topLeft = Offset(loungeLeft, loungeTop),
        size = Size(loungeW, loungeH),
        cornerRadius = CornerRadius(10.dp.toPx(), 10.dp.toPx()),
        style = Stroke(width = 1.dp.toPx())
    )

    // Мягкий угловой/прямой диван с новогодними подушками
    val sofaX = loungeLeft + 10.dp.toPx()
    val sofaY = loungeTop + 20.dp.toPx()
    val sofaW = 19.dp.toPx()
    val sofaH = loungeH - 32.dp.toPx()
    // Спинка дивана
    drawRoundRect(
        color = Color(0xFF1E293B),
        topLeft = Offset(sofaX, sofaY),
        size = Size(6.dp.toPx(), sofaH),
        cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx())
    )
    // Сиденье дивана
    drawRoundRect(
        color = Color(0xFF334155),
        topLeft = Offset(sofaX + 5.dp.toPx(), sofaY + 2.dp.toPx()),
        size = Size(sofaW - 5.dp.toPx(), sofaH - 4.dp.toPx()),
        cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
    )
    // Новогодние декоративные подушки (красная и золотая)
    drawRoundRect(
        color = Color(0xFFF43F5E),
        topLeft = Offset(sofaX + 6.dp.toPx(), sofaY + 6.dp.toPx()),
        size = Size(6.dp.toPx(), 8.dp.toPx()),
        cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
    )
    drawRoundRect(
        color = Color(0xFFFBBF24),
        topLeft = Offset(sofaX + 6.dp.toPx(), sofaY + sofaH - 15.dp.toPx()),
        size = Size(6.dp.toPx(), 8.dp.toPx()),
        cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
    )

    // ТВ НА ВСЮ ПРАВУЮ СТЕНУ С ОТОБРАЖЕНИЕМ КОНТЕНТА И AMBILIGHT-ПОДСВЕТКОЙ
    val tvScreenW = 17.dp.toPx()
    val tvTop = loungeTop + 14.dp.toPx()
    val tvH = loungeH - 22.dp.toPx()
    val tvLeft = loungeLeft + loungeW - tvScreenW - 3.dp.toPx()

    // Ambilight свечение от ТВ на пол комнаты отдыха
    drawRect(
        brush = Brush.horizontalGradient(
            colors = listOf(Color.Transparent, Color(0xFF38BDF8).copy(alpha = 0.24f)),
            startX = tvLeft - 24.dp.toPx(),
            endX = tvLeft
        ),
        topLeft = Offset(tvLeft - 24.dp.toPx(), tvTop),
        size = Size(24.dp.toPx(), tvH)
    )
    // Корпус панорамного ТВ на всю стену
    drawRoundRect(
        color = Color(0xFF090D16),
        topLeft = Offset(tvLeft, tvTop),
        size = Size(tvScreenW, tvH),
        cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx())
    )
    // Экран ТВ с контентом (эфир новостей + живые графики + бегущая строка)
    val scrLeft = tvLeft + 1.8.dp.toPx()
    val scrTop = tvTop + 2.dp.toPx()
    val scrW = tvScreenW - 4.5.dp.toPx()
    val scrH = tvH - 4.dp.toPx()
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(Color(0xFF0284C7), Color(0xFF1E1B4B)),
            startY = scrTop,
            endY = scrTop + scrH
        ),
        topLeft = Offset(scrLeft, scrTop),
        size = Size(scrW, scrH)
    )
    // Контент на ТВ: анимированные столбцы аналитики / эквалайзера
    for (bar in 0 until 5) {
        val barH = scrW * (0.28f + 0.58f * abs(sin(liveWorkPhase * 6.283f + bar * 1.1f)))
        val by = scrTop + 5.dp.toPx() + bar * ((scrH - 18.dp.toPx()) / 5f)
        drawRect(
            color = if (bar % 2 == 0) Color(0xFF38BDF8) else Color(0xFFFBBF24),
            topLeft = Offset(scrLeft + scrW - barH - 1.dp.toPx(), by),
            size = Size(barH, 3.dp.toPx())
        )
    }
    // Красная плашка «LIVE» / бегущая строка внизу экрана ТВ
    drawRect(
        color = Color(0xFFEF4444),
        topLeft = Offset(scrLeft, scrTop + scrH - 7.dp.toPx()),
        size = Size(scrW, 6.dp.toPx())
    )

    // 5. ПУШИСТАЯ НОВОГОДНЯЯ ЁЛКА С ИГОЛКАМИ, МИШУРОЙ И ШАРАМИ ПО ЦЕНТРУ
    drawDetailedPineTree(
        centerX = w * 0.455f,
        topY = h * 0.345f,
        bottomY = h * 0.645f,
        garlandPhase = garlandPhase
    )

    // 6. МЕРЦАЮЩИЕ ГИРЛЯНДЫ ВДОЛЬ СТЕНОК КОРИДОРА
    for (i in 0 until 18) {
        val bx = w * (0.03f + i * 0.055f)
        val pulseTop = 0.42f + 0.58f * abs(sin(garlandPhase + i * 0.65f))
        val pulseBot = 0.42f + 0.58f * abs(sin(garlandPhase + i * 0.65f + 1.57f))
        drawCircle(
            color = bulbColors[i % bulbColors.size].copy(alpha = pulseTop),
            radius = 2.2.dp.toPx(),
            center = Offset(bx, h * 0.292f)
        )
        drawCircle(
            color = bulbColors[(i + 2) % bulbColors.size].copy(alpha = pulseBot),
            radius = 2.2.dp.toPx(),
            center = Offset(bx, h * 0.708f)
        )
    }

    // 7. ХОРОШО ПРОРИСОВАННЫЕ ЧЕЛОВЕЧКИ (АГЕНТЫ) С АНИМАЦИЕЙ ХОДЬБЫ И РАБОТЫ ЗА КП
    agents.forEach { ag ->
        val pos = positions[ag.id] ?: ag.deskChairPos
        val cx = pos.x * w
        val cy = pos.y * h
        val isSel = ag.id == selectedId
        val st = statuses[ag.id]

        val isAtDesk = hypot(pos.x - ag.deskChairPos.x, pos.y - ag.deskChairPos.y) < 0.025f &&
            st?.targetZone == AgentLocationZone.DESK
        val isWalking = !isAtDesk &&
            (st?.targetZone != AgentLocationZone.DINING || hypot(pos.x - ag.diningSeatPos.x, pos.y - ag.diningSeatPos.y) > 0.025f) &&
            (st?.targetZone != AgentLocationZone.LOUNGE || hypot(pos.x - ag.loungeSeatPos.x, pos.y - ag.loungeSeatPos.y) > 0.025f)

        drawDetailedAgentCharacter(
            cx = cx,
            cy = cy,
            profile = ag,
            status = st,
            isSelected = isSel,
            isWalking = isWalking,
            isTypingAtDesk = isAtDesk,
            animPhase = liveWorkPhase + ag.id * 0.37f
        )
    }

    // 8. МЯГКИЙ НОВОГОДНИЙ СНЕГОПАД
    for (s in 0 until 26) {
        val seedX = ((s * 73 + 19) % 100) / 100f
        val drift = sin(snowPhase * 6.28318f + s) * 6.dp.toPx()
        val syFrac = ((s * 37) / 100f + snowPhase) % 1f
        val sx = seedX * w + drift
        val sy = syFrac * h
        drawCircle(
            color = Color.White.copy(alpha = if (s % 3 == 0) 0.38f else 0.20f),
            radius = if (s % 4 == 0) 1.7.dp.toPx() else 1.1.dp.toPx(),
            center = Offset(sx, sy)
        )
    }
}

private fun DrawScope.drawFestiveWreath(center: Offset, radius: Float) {
    drawCircle(
        color = Color(0xFF059669),
        radius = radius,
        center = center,
        style = Stroke(width = 2.2.dp.toPx())
    )
    drawCircle(
        color = Color(0xFF10B981),
        radius = radius * 0.85f,
        center = center,
        style = Stroke(width = 1.dp.toPx())
    )
    // Красный бант и ягодки
    drawCircle(
        color = Color(0xFFF43F5E),
        radius = 1.6.dp.toPx(),
        center = Offset(center.x, center.y - radius)
    )
    drawCircle(
        color = Color(0xFFFBBF24),
        radius = 1.dp.toPx(),
        center = Offset(center.x + radius, center.y)
    )
}

private fun DrawScope.drawWorkstationWithLivePc(
    deskLeft: Float,
    deskTop: Float,
    deskWidth: Float,
    deskHeight: Float,
    accentColor: Color,
    progress: Float,
    liveWorkPhase: Float,
    isTopRow: Boolean
) {
    // Столешница
    drawRoundRect(
        color = Color(0xFF2B3F63),
        topLeft = Offset(deskLeft, deskTop),
        size = Size(deskWidth, deskHeight),
        cornerRadius = CornerRadius(3.5.dp.toPx(), 3.5.dp.toPx())
    )
    drawRoundRect(
        color = Color.White.copy(alpha = 0.12f),
        topLeft = Offset(deskLeft, deskTop),
        size = Size(deskWidth, deskHeight),
        cornerRadius = CornerRadius(3.5.dp.toPx(), 3.5.dp.toPx()),
        style = Stroke(width = 0.6.dp.toPx())
    )

    // Монитор КП (расположен на столе, экран обращён так, чтобы был виден процесс работы)
    val monW = deskWidth * 0.56f
    val monH = 12.5.dp.toPx()
    val monLeft = deskLeft + (deskWidth - monW) * 0.5f
    val monTop = if (isTopRow) deskTop - 4.dp.toPx() else deskTop - 1.dp.toPx()

    // Мягкое свечение от монитора на стол
    drawCircle(
        color = accentColor.copy(alpha = 0.22f),
        radius = monW * 0.65f,
        center = Offset(monLeft + monW * 0.5f, monTop + monH * 0.5f)
    )

    // Рамка монитора
    drawRoundRect(
        color = Color(0xFF070D19),
        topLeft = Offset(monLeft, monTop),
        size = Size(monW, monH),
        cornerRadius = CornerRadius(2.5.dp.toPx(), 2.5.dp.toPx())
    )
    // Активный экран КП
    val scrX = monLeft + 1.5.dp.toPx()
    val scrY = monTop + 1.5.dp.toPx()
    val scrW = monW - 3.dp.toPx()
    val scrH = monH - 3.dp.toPx()
    drawRect(
        color = Color(0xFF0E223D),
        topLeft = Offset(scrX, scrY),
        size = Size(scrW, scrH)
    )

    // Живой контент на КП: бегущие строки кода/аналитики + прогресс-бар задачи
    val lineCount = 3
    for (ln in 0 until lineCount) {
        val animFrac = ((liveWorkPhase + ln * 0.33f) % 1f)
        val lineW = scrW * (0.35f + 0.55f * abs(sin(animFrac * PI.toFloat())))
        val ly = scrY + 1.8.dp.toPx() + ln * 2.3.dp.toPx()
        drawLine(
            color = if (ln == 0) accentColor else Color(0xFF93C5FD).copy(alpha = 0.85f),
            start = Offset(scrX + 1.5.dp.toPx(), ly),
            end = Offset(scrX + 1.5.dp.toPx() + lineW, ly),
            strokeWidth = 1.2.dp.toPx(),
            cap = StrokeCap.Round
        )
    }
    // Мини-шкала прогресса тикета внизу экрана КП
    val barY = scrY + scrH - 1.8.dp.toPx()
    drawLine(
        color = Color.White.copy(alpha = 0.18f),
        start = Offset(scrX + 1.5.dp.toPx(), barY),
        end = Offset(scrX + scrW - 1.5.dp.toPx(), barY),
        strokeWidth = 1.4.dp.toPx()
    )
    drawLine(
        color = Color(0xFF10B981),
        start = Offset(scrX + 1.5.dp.toPx(), barY),
        end = Offset(scrX + 1.5.dp.toPx() + (scrW - 3.dp.toPx()) * progress.coerceIn(0.1f, 1f), barY),
        strokeWidth = 1.4.dp.toPx()
    )

    // Клавиатура рядом с монитором
    val kbY = if (isTopRow) deskTop + deskHeight - 4.5.dp.toPx() else deskTop + 1.5.dp.toPx()
    drawRoundRect(
        color = Color(0xFF1E293B),
        topLeft = Offset(monLeft + 3.dp.toPx(), kbY),
        size = Size(monW - 6.dp.toPx(), 3.dp.toPx()),
        cornerRadius = CornerRadius(1.dp.toPx(), 1.dp.toPx())
    )
}

private fun DrawScope.drawDetailedPineTree(
    centerX: Float,
    topY: Float,
    bottomY: Float,
    garlandPhase: Float
) {
    val totalH = bottomY - topY

    // Тень под ёлкой
    drawOval(
        color = Color.Black.copy(alpha = 0.45f),
        topLeft = Offset(centerX - 24.dp.toPx(), bottomY - 3.dp.toPx()),
        size = Size(48.dp.toPx(), 9.dp.toPx())
    )

    // Ствол и нарядный горшок
    drawRect(
        color = Color(0xFF78350F),
        topLeft = Offset(centerX - 3.5.dp.toPx(), bottomY - 8.dp.toPx()),
        size = Size(7.dp.toPx(), 9.dp.toPx())
    )
    drawRoundRect(
        color = Color(0xFFDC2626),
        topLeft = Offset(centerX - 6.dp.toPx(), bottomY - 3.dp.toPx()),
        size = Size(12.dp.toPx(), 6.dp.toPx()),
        cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
    )

    // 4 пушистых яруса хвои + прорисовка отдельных иголок по краям и внутри
    val tiers = listOf(
        Triple(topY, topY + totalH * 0.34f, 11.dp.toPx()),
        Triple(topY + totalH * 0.18f, topY + totalH * 0.56f, 16.dp.toPx()),
        Triple(topY + totalH * 0.38f, topY + totalH * 0.77f, 21.dp.toPx()),
        Triple(topY + totalH * 0.58f, bottomY - 6.dp.toPx(), 26.dp.toPx())
    )

    tiers.forEachIndexed { idx, (ty0, ty1, halfW) ->
        val p = Path().apply {
            moveTo(centerX, ty0)
            lineTo(centerX - halfW, ty1)
            lineTo(centerX, ty1 - 3.dp.toPx())
            lineTo(centerX + halfW, ty1)
            close()
        }
        drawPath(
            path = p,
            brush = Brush.verticalGradient(
                colors = listOf(Color(0xFF10B981), Color(0xFF065F46)),
                startY = ty0,
                endY = ty1
            )
        )

        // Прорисовка хвойных иголок вдоль левой, правой и нижней кромок каждого яруса
        val needleCount = 12 + idx * 4
        for (n in 0..needleCount) {
            val frac = n.toFloat() / needleCount.toFloat()
            val ny = ty0 + (ty1 - ty0) * frac
            val spanX = halfW * frac

            // Левые иголки
            drawLine(
                color = if (n % 2 == 0) Color(0xFF34D399) else Color(0xFF047857),
                start = Offset(centerX - spanX + 1.5.dp.toPx(), ny),
                end = Offset(centerX - spanX - 2.8.dp.toPx(), ny + 2.2.dp.toPx()),
                strokeWidth = 1.dp.toPx(),
                cap = StrokeCap.Round
            )
            // Правые иголки
            drawLine(
                color = if (n % 2 == 0) Color(0xFF34D399) else Color(0xFF047857),
                start = Offset(centerX + spanX - 1.5.dp.toPx(), ny),
                end = Offset(centerX + spanX + 2.8.dp.toPx(), ny + 2.2.dp.toPx()),
                strokeWidth = 1.dp.toPx(),
                cap = StrokeCap.Round
            )
            // Внутренние фактурные иголки
            if (n % 2 == 1 && frac > 0.25f) {
                val innerX = centerX + (if (n % 4 == 1) -spanX * 0.42f else spanX * 0.42f)
                drawLine(
                    color = Color(0xFF34D399).copy(alpha = 0.7f),
                    start = Offset(innerX, ny - 2.dp.toPx()),
                    end = Offset(innerX + (if (innerX < centerX) -2.dp.toPx() else 2.dp.toPx()), ny + 2.dp.toPx()),
                    strokeWidth = 0.9.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }
        }
    }

    // Золотая гирлянда-мишура по диагонали
    drawLine(
        color = Color(0xFFFBBF24).copy(alpha = 0.75f),
        start = Offset(centerX - 7.dp.toPx(), topY + totalH * 0.25f),
        end = Offset(centerX + 12.dp.toPx(), topY + totalH * 0.44f),
        strokeWidth = 1.5.dp.toPx(),
        cap = StrokeCap.Round
    )
    drawLine(
        color = Color(0xFFFBBF24).copy(alpha = 0.75f),
        start = Offset(centerX - 14.dp.toPx(), topY + totalH * 0.52f),
        end = Offset(centerX + 18.dp.toPx(), topY + totalH * 0.72f),
        strokeWidth = 1.5.dp.toPx(),
        cap = StrokeCap.Round
    )

    // Стеклянные ёлочные шары с тенью и белым бликом
    val baubles = listOf(
        Triple(Offset(-4.dp.toPx(), totalH * 0.22f), Color(0xFFF43F5E), 2.5.dp.toPx()),
        Triple(Offset(5.dp.toPx(), totalH * 0.30f), Color(0xFF38BDF8), 2.5.dp.toPx()),
        Triple(Offset(-9.dp.toPx(), totalH * 0.44f), Color(0xFFFBBF24), 2.8.dp.toPx()),
        Triple(Offset(8.dp.toPx(), totalH * 0.50f), Color(0xFFA78BFA), 2.8.dp.toPx()),
        Triple(Offset(-2.dp.toPx(), totalH * 0.60f), Color(0xFF38BDF8), 2.6.dp.toPx()),
        Triple(Offset(-14.dp.toPx(), totalH * 0.72f), Color(0xFFF43F5E), 3.0.dp.toPx()),
        Triple(Offset(13.dp.toPx(), totalH * 0.76f), Color(0xFFFBBF24), 3.0.dp.toPx()),
        Triple(Offset(0.dp.toPx(), totalH * 0.82f), Color(0xFFEC4899), 2.7.dp.toPx())
    )
    baubles.forEachIndexed { idx, (offset, col, rad) ->
        val bCenter = Offset(centerX + offset.x, topY + offset.y)
        val glow = 0.65f + 0.35f * abs(sin(garlandPhase + idx))
        drawCircle(
            color = col.copy(alpha = glow),
            radius = rad,
            center = bCenter
        )
        // Блик на шарике
        drawCircle(
            color = Color.White.copy(alpha = 0.85f),
            radius = rad * 0.34f,
            center = Offset(bCenter.x - rad * 0.32f, bCenter.y - rad * 0.32f)
        )
    }

    // Сияющая звезда на макушке
    val starCenter = Offset(centerX, topY - 2.dp.toPx())
    drawCircle(
        color = Color(0xFFFBBF24).copy(alpha = 0.28f),
        radius = 8.dp.toPx(),
        center = starCenter
    )
    drawCircle(
        color = Color(0xFFFDE047),
        radius = 4.2.dp.toPx(),
        center = starCenter
    )

    // Подарки с лентами под ёлкой
    drawRoundRect(
        color = Color(0xFFF43F5E),
        topLeft = Offset(centerX - 18.dp.toPx(), bottomY - 4.dp.toPx()),
        size = Size(9.dp.toPx(), 7.dp.toPx()),
        cornerRadius = CornerRadius(1.5.dp.toPx(), 1.5.dp.toPx())
    )
    drawLine(
        color = Color(0xFFFBBF24),
        start = Offset(centerX - 13.5.dp.toPx(), bottomY - 4.dp.toPx()),
        end = Offset(centerX - 13.5.dp.toPx(), bottomY + 3.dp.toPx()),
        strokeWidth = 1.3.dp.toPx()
    )
    drawRoundRect(
        color = Color(0xFF38BDF8),
        topLeft = Offset(centerX + 9.dp.toPx(), bottomY - 4.dp.toPx()),
        size = Size(9.dp.toPx(), 7.dp.toPx()),
        cornerRadius = CornerRadius(1.5.dp.toPx(), 1.5.dp.toPx())
    )
    drawLine(
        color = Color.White,
        start = Offset(centerX + 13.5.dp.toPx(), bottomY - 4.dp.toPx()),
        end = Offset(centerX + 13.5.dp.toPx(), bottomY + 3.dp.toPx()),
        strokeWidth = 1.3.dp.toPx()
    )
}

private fun DrawScope.drawDetailedAgentCharacter(
    cx: Float,
    cy: Float,
    profile: OfficeAgentProfile,
    status: AgentLiveStatus?,
    isSelected: Boolean,
    isWalking: Boolean,
    isTypingAtDesk: Boolean,
    animPhase: Float
) {
    // Выделение выбранного агента пунктирным кольцом
    if (isSelected) {
        drawCircle(
            color = profile.accentColor.copy(alpha = 0.22f),
            radius = 17.dp.toPx(),
            center = Offset(cx, cy + 2.dp.toPx())
        )
        drawCircle(
            color = profile.accentColor,
            radius = 17.dp.toPx(),
            center = Offset(cx, cy + 2.dp.toPx()),
            style = Stroke(
                width = 1.4.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 4f), 0f)
            )
        )
    }

    // Тень под ногами
    drawOval(
        color = Color.Black.copy(alpha = 0.45f),
        topLeft = Offset(cx - 8.dp.toPx(), cy + 12.dp.toPx()),
        size = Size(16.dp.toPx(), 4.5.dp.toPx())
    )

    val stride = if (isWalking) sin(animPhase * 12.566f) * 2.6.dp.toPx() else 0f
    val typeBounce = if (isTypingAtDesk) sin(animPhase * 18.84f) * 1.1.dp.toPx() else 0f

    // Ноги (брюки) и обувь
    drawLine(
        color = Color(0xFF1E293B),
        start = Offset(cx - 2.6.dp.toPx(), cy + 6.dp.toPx()),
        end = Offset(cx - 3.dp.toPx(), cy + 12.dp.toPx() + stride),
        strokeWidth = 2.6.dp.toPx(),
        cap = StrokeCap.Round
    )
    drawLine(
        color = Color(0xFF1E293B),
        start = Offset(cx + 2.6.dp.toPx(), cy + 6.dp.toPx()),
        end = Offset(cx + 3.dp.toPx(), cy + 12.dp.toPx() - stride),
        strokeWidth = 2.6.dp.toPx(),
        cap = StrokeCap.Round
    )
    // Ботинки
    drawCircle(
        color = Color(0xFF090D16),
        radius = 1.8.dp.toPx(),
        center = Offset(cx - 3.2.dp.toPx(), cy + 12.8.dp.toPx() + stride)
    )
    drawCircle(
        color = Color(0xFF090D16),
        radius = 1.8.dp.toPx(),
        center = Offset(cx + 3.2.dp.toPx(), cy + 12.8.dp.toPx() - stride)
    )

    // Туловище / пиджак сотрудника
    drawRoundRect(
        color = profile.accentColor,
        topLeft = Offset(cx - 6.2.dp.toPx(), cy - 1.5.dp.toPx()),
        size = Size(12.4.dp.toPx(), 9.dp.toPx()),
        cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
    )
    // Белый воротничок рубашки
    val collar = Path().apply {
        moveTo(cx - 2.2.dp.toPx(), cy - 1.5.dp.toPx())
        lineTo(cx, cy + 2.dp.toPx())
        lineTo(cx + 2.2.dp.toPx(), cy - 1.5.dp.toPx())
        close()
    }
    drawPath(path = collar, color = Color.White.copy(alpha = 0.9f))

    // Руки (подвижные при ходьбе или печатающие за КП)
    val leftHandY = if (isTypingAtDesk) {
        (if (profile.isTopRow) cy - 3.dp.toPx() else cy + 4.dp.toPx()) + typeBounce
    } else {
        cy + 4.dp.toPx() - stride * 0.7f
    }
    val rightHandY = if (isTypingAtDesk) {
        (if (profile.isTopRow) cy - 3.dp.toPx() else cy + 4.dp.toPx()) - typeBounce
    } else {
        cy + 4.dp.toPx() + stride * 0.7f
    }
    drawLine(
        color = profile.accentColor,
        start = Offset(cx - 6.dp.toPx(), cy + 0.5.dp.toPx()),
        end = Offset(cx - 8.dp.toPx(), leftHandY),
        strokeWidth = 2.2.dp.toPx(),
        cap = StrokeCap.Round
    )
    drawLine(
        color = profile.accentColor,
        start = Offset(cx + 6.dp.toPx(), cy + 0.5.dp.toPx()),
        end = Offset(cx + 8.dp.toPx(), rightHandY),
        strokeWidth = 2.2.dp.toPx(),
        cap = StrokeCap.Round
    )
    // Кисти рук
    drawCircle(color = Color(0xFFFDE68A), radius = 1.4.dp.toPx(), center = Offset(cx - 8.dp.toPx(), leftHandY))
    drawCircle(color = Color(0xFFFDE68A), radius = 1.4.dp.toPx(), center = Offset(cx + 8.dp.toPx(), rightHandY))

    // Шея и голова
    drawRect(
        color = Color(0xFFFCD34D),
        topLeft = Offset(cx - 1.5.dp.toPx(), cy - 3.dp.toPx()),
        size = Size(3.dp.toPx(), 2.dp.toPx())
    )
    val headCenter = Offset(cx, cy - 7.dp.toPx())
    // Длинные волосы сзади для девушек-агентов
    if (profile.isFemale) {
        drawRoundRect(
            color = profile.hairColor,
            topLeft = Offset(cx - 6.dp.toPx(), cy - 11.5.dp.toPx()),
            size = Size(12.dp.toPx(), 9.5.dp.toPx()),
            cornerRadius = CornerRadius(5.dp.toPx(), 5.dp.toPx())
        )
    }
    // Лицо
    drawCircle(
        color = Color(0xFFFDE68A),
        radius = 4.8.dp.toPx(),
        center = headCenter
    )
    // Чёлка / верхняя причёска
    drawArc(
        color = profile.hairColor,
        startAngle = 185f,
        sweepAngle = 170f,
        useCenter = true,
        topLeft = Offset(cx - 4.9.dp.toPx(), cy - 12.dp.toPx()),
        size = Size(9.8.dp.toPx(), 6.8.dp.toPx())
    )
    // Глаза
    drawCircle(color = Color(0xFF0F172A), radius = 0.9.dp.toPx(), center = Offset(cx - 1.7.dp.toPx(), cy - 6.8.dp.toPx()))
    drawCircle(color = Color(0xFF0F172A), radius = 0.9.dp.toPx(), center = Offset(cx + 1.7.dp.toPx(), cy - 6.8.dp.toPx()))
    // Улыбка
    drawArc(
        color = Color(0xFF9F1239),
        startAngle = 20f,
        sweepAngle = 140f,
        useCenter = false,
        topLeft = Offset(cx - 1.6.dp.toPx(), cy - 6.2.dp.toPx()),
        size = Size(3.2.dp.toPx(), 2.2.dp.toPx()),
        style = Stroke(width = 0.8.dp.toPx())
    )
    // Очки для аудиторов ТТХ (Виктор и Елена)
    if (profile.hasGlasses) {
        drawCircle(
            color = Color(0xFF38BDF8),
            radius = 1.6.dp.toPx(),
            center = Offset(cx - 1.7.dp.toPx(), cy - 6.8.dp.toPx()),
            style = Stroke(width = 0.6.dp.toPx())
        )
        drawCircle(
            color = Color(0xFF38BDF8),
            radius = 1.6.dp.toPx(),
            center = Offset(cx + 1.7.dp.toPx(), cy - 6.8.dp.toPx()),
            style = Stroke(width = 0.6.dp.toPx())
        )
    }
    // Золотая диадема/корона у Директора Авроры
    if (profile.id == 7) {
        drawCircle(
            color = Color(0xFFFBBF24),
            radius = 1.8.dp.toPx(),
            center = Offset(cx, cy - 12.6.dp.toPx())
        )
    }

    // Индикатор статуса (зелёный = работает за КП, голубой = на проверке у Авроры, жёлтый = отдых)
    val statusDotColor = when (status?.targetZone) {
        AgentLocationZone.CHIEF_CHECK -> Color(0xFF38BDF8)
        AgentLocationZone.DINING, AgentLocationZone.LOUNGE -> Color(0xFFFBBF24)
        else -> Color(0xFF10B981)
    }
    drawCircle(
        color = statusDotColor,
        radius = 2.4.dp.toPx(),
        center = Offset(cx + 7.5.dp.toPx(), cy - 9.dp.toPx())
    )

    if ((status?.reprimandsCount ?: 0) > 0) {
        drawCircle(
            color = Color(0xFFF43F5E),
            radius = 2.4.dp.toPx(),
            center = Offset(cx - 7.5.dp.toPx(), cy - 9.dp.toPx())
        )
    }
}
