package com.nexusai.techfeed

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Check
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
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import kotlinx.coroutines.Dispatchers
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
import kotlin.math.hypot
import kotlin.math.sin

// ============================================================================
// МОДЕЛИ ДАННЫХ И ПОЛНОЦЕННЫЕ ЦВЕТОВЫЕ ПАЛИТРЫ
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
    val primaryLight: Color,
    val bgDarkTint: Color,
    val surfaceDarkTint: Color,
    val bgLightTint: Color
)

val AppPalettes = listOf(
    AppPalette(
        id = "cyber_blue",
        name = "Неон",
        primaryDark = Color(0xFF38BDF8),
        primaryLight = Color(0xFF0284C7),
        bgDarkTint = Color(0xFF060C18),
        surfaceDarkTint = Color(0xFF0D172A),
        bgLightTint = Color(0xFFEEF5FC)
    ),
    AppPalette(
        id = "emerald",
        name = "Изумруд",
        primaryDark = Color(0xFF10B981),
        primaryLight = Color(0xFF059669),
        bgDarkTint = Color(0xFF05120E),
        surfaceDarkTint = Color(0xFF0B1F19),
        bgLightTint = Color(0xFFECF9F4)
    ),
    AppPalette(
        id = "amber",
        name = "Янтарь",
        primaryDark = Color(0xFFF59E0B),
        primaryLight = Color(0xFFD97706),
        bgDarkTint = Color(0xFF130E05),
        surfaceDarkTint = Color(0xFF21180B),
        bgLightTint = Color(0xFFFEF8EC)
    ),
    AppPalette(
        id = "violet",
        name = "Аметист",
        primaryDark = Color(0xFFA78BFA),
        primaryLight = Color(0xFF7C3AED),
        bgDarkTint = Color(0xFF0E091A),
        surfaceDarkTint = Color(0xFF18112B),
        bgLightTint = Color(0xFFF4F0FE)
    ),
    AppPalette(
        id = "rose",
        name = "Рубин",
        primaryDark = Color(0xFFF43F5E),
        primaryLight = Color(0xFFE11D48),
        bgDarkTint = Color(0xFF15070C),
        surfaceDarkTint = Color(0xFF230E16),
        bgLightTint = Color(0xFFFEF0F3)
    ),
    AppPalette(
        id = "cyan",
        name = "Бирюза",
        primaryDark = Color(0xFF22D3EE),
        primaryLight = Color(0xFF0891B2),
        bgDarkTint = Color(0xFF051115),
        surfaceDarkTint = Color(0xFF0B1E24),
        bgLightTint = Color(0xFFECFAFD)
    )
)

enum class AgentLocationZone(val label: String) {
    DESK("Работа за ПК"),
    CHIEF_CHECK("Сдача отчёта Авроре"),
    DINING("Обед на кухне"),
    LOUNGE("Отдых у ТВ")
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
    val isManagement: Boolean,
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
    val breakTotalSeconds: Float,
    val mealPaidForCurrentVisit: Boolean,
    val totalMealSpent: Int,
    val lastMealReceipt: String,
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
    private const val PREF_NAME = "nexus_tech_feed_v9"
    private const val KEY_SETTINGS = "settings_json"
    private const val KEY_NEWS = "news_json"
    private const val KEY_REVIEWS = "reviews_json"
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
                    put(
                        JSONObject().apply {
                            put("label", s.label)
                            put("value", s.value)
                        }
                    )
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
                        put("breakTotalSeconds", st.breakTotalSeconds.toDouble())
                        put("mealPaidForCurrentVisit", st.mealPaidForCurrentVisit)
                        put("totalMealSpent", st.totalMealSpent)
                        put("lastMealReceipt", st.lastMealReceipt)
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
                    breakTotalSeconds = o.optDouble("breakTotalSeconds", 15.0).toFloat(),
                    mealPaidForCurrentVisit = o.optBoolean("mealPaidForCurrentVisit", false),
                    totalMealSpent = o.optInt("totalMealSpent", 0),
                    lastMealReceipt = o.optString("lastMealReceipt", "Обедов ещё не было"),
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
            headline = "Xiaomi 17T: субфлагман с 5x перископом Leica и батареей 6500 мА·ч",
            score = "9.1",
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
            publishedAt = "Редакция • Сегодня"
        ),
        TechReviewArticle(
            id = "rev_huawei_watch_fit_5",
            deviceName = "Huawei Watch Fit 5",
            brand = "Huawei",
            category = "Носимая электроника",
            headline = "Huawei Watch Fit 5: лёгкий корпус, яркий AMOLED и неделя без розетки",
            score = "9.0",
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
            publishedAt = "Редакция • Вчера"
        ),
        TechReviewArticle(
            id = "rev_redmi_buds_8_pro",
            deviceName = "Xiaomi Redmi Buds 8 Pro",
            brand = "Xiaomi",
            category = "Носимая электроника",
            headline = "Xiaomi Redmi Buds 8 Pro: коаксиальные драйверы, ANC и поддержка LHDC / LDAC",
            score = "8.8",
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
            publishedAt = "Редакция • 2 дня назад"
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
            headline = "$cleanName: подробный разбор возможностей и реальных ТТХ",
            score = "9.0",
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
            publishedAt = "Приказ • $nowLabel",
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
// ТОЧКА ВХОДА И ГЛАВНЫЙ ЭКРАН ПРИЛОЖЕНИЯ (4 РАЗДЕЛЬНЫХ ВКЛАДКИ БЕЗ ИЗБРАННОГО)
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
fun SectionBlockCard(
    title: String,
    subtitle: String? = null,
    accentColor: Color = MaterialTheme.colorScheme.primary,
    cornerRadius: Int = 16,
    modifier: Modifier = Modifier,
    headerAction: (@Composable () -> Unit)? = null,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(cornerRadius.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .width(4.dp)
                            .height(22.dp)
                            .clip(CircleShape)
                            .background(accentColor)
                    )
                    Column {
                        Text(
                            text = title,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (!subtitle.isNullOrBlank()) {
                            Text(
                                text = subtitle,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
                headerAction?.invoke()
            }
            content()
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
    var deletedNewsIds by remember { mutableStateOf(LocalCache.loadDeletedNewsIds(context)) }
    var deletedReviewIds by remember { mutableStateOf(LocalCache.loadDeletedReviewIds(context)) }

    // 0 = Лента, 1 = Обзоры, 2 = ИИ-Дом, 3 = Настройки (Избранное убрано)
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

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !useDark
            }
        }
    }

    val colorScheme = if (useDark) {
        val bg = if (settings.darkMode == "amoled") Color(0xFF000000) else activePalette.bgDarkTint
        val surf = if (settings.darkMode == "amoled") Color(0xFF0A0D14) else activePalette.surfaceDarkTint
        val surfContainer = if (settings.darkMode == "amoled") Color(0xFF121622) else activePalette.primaryDark.copy(alpha = 0.12f)
        darkColorScheme(
            primary = activePalette.primaryDark,
            onPrimary = Color(0xFF04101A),
            primaryContainer = activePalette.primaryDark.copy(alpha = 0.22f),
            onPrimaryContainer = activePalette.primaryDark,
            secondary = activePalette.primaryDark,
            secondaryContainer = surfContainer,
            onSecondaryContainer = Color(0xFFF8FAFC),
            background = bg,
            onBackground = Color(0xFFF8FAFC),
            surface = surf,
            onSurface = Color(0xFFF8FAFC),
            surfaceVariant = surfContainer,
            onSurfaceVariant = Color(0xFF94A3B8),
            outline = activePalette.primaryDark.copy(alpha = 0.45f),
            outlineVariant = activePalette.primaryDark.copy(alpha = 0.24f)
        )
    } else {
        val bg = activePalette.bgLightTint
        val surf = Color(0xFFFFFFFF)
        val surfContainer = activePalette.primaryLight.copy(alpha = 0.10f)
        lightColorScheme(
            primary = activePalette.primaryLight,
            onPrimary = Color.White,
            primaryContainer = activePalette.primaryLight.copy(alpha = 0.16f),
            onPrimaryContainer = activePalette.primaryLight,
            secondary = activePalette.primaryLight,
            secondaryContainer = surfContainer,
            onSecondaryContainer = Color(0xFF0F172A),
            background = bg,
            onBackground = Color(0xFF0F172A),
            surface = surf,
            onSurface = Color(0xFF0F172A),
            surfaceVariant = surfContainer,
            onSurfaceVariant = Color(0xFF475569),
            outline = activePalette.primaryLight.copy(alpha = 0.45f),
            outlineVariant = activePalette.primaryLight.copy(alpha = 0.22f)
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

    MaterialTheme(colorScheme = colorScheme) {
        val primary = MaterialTheme.colorScheme.primary
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    shadowElevation = 2.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(primary.copy(alpha = 0.16f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Column {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = "NewsPulse AI",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = primary.copy(alpha = 0.16f)
                                    ) {
                                        Text(
                                            text = activePalette.name.uppercase(),
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = primary,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = when (selectedTab) {
                                        0 -> "Лента новостей • ${visibleNews.size} публикаций"
                                        1 -> "Журнал обзоров • ${visibleReviews.size} статей"
                                        2 -> "ИИ-Дом • 8 агентов в работе"
                                        else -> "Настройки интерфейса и кэша"
                                    },
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Surface(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .clickable(enabled = !isLoading) { refreshFeed() },
                            shape = RoundedCornerShape(10.dp),
                            color = primary.copy(alpha = 0.15f)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                if (isLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        strokeWidth = 2.dp,
                                        color = primary
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = "Обновить",
                                        tint = primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            },
            bottomBar = {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    shadowElevation = 4.dp
                ) {
                    NavigationBar(
                        containerColor = Color.Transparent,
                        tonalElevation = 0.dp
                    ) {
                        val navColors = NavigationBarItemDefaults.colors(
                            selectedIconColor = primary,
                            selectedTextColor = primary,
                            indicatorColor = primary.copy(alpha = 0.16f),
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        NavigationBarItem(
                            selected = selectedTab == 0,
                            onClick = { selectedTab = 0 },
                            colors = navColors,
                            icon = { Icon(Icons.Default.Newspaper, contentDescription = "Лента") },
                            label = { Text("Лента", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) }
                        )
                        NavigationBarItem(
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            colors = navColors,
                            icon = { Icon(Icons.Default.RateReview, contentDescription = "Обзоры") },
                            label = { Text("Обзоры", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) }
                        )
                        NavigationBarItem(
                            selected = selectedTab == 2,
                            onClick = { selectedTab = 2 },
                            colors = navColors,
                            icon = { Icon(Icons.Default.Hub, contentDescription = "ИИ-Дом") },
                            label = { Text("ИИ-Дом", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) }
                        )
                        NavigationBarItem(
                            selected = selectedTab == 3,
                            onClick = { selectedTab = 3 },
                            colors = navColors,
                            icon = { Icon(Icons.Default.Settings, contentDescription = "Настройки") },
                            label = { Text("Настройки", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) }
                        )
                    }
                }
            }
        ) { innerPadding ->
            when (selectedTab) {
                0 -> FeedScreen(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = innerPadding.calculateTopPadding()),
                    bottomPadding = innerPadding.calculateBottomPadding() + 14.dp,
                    news = visibleNews,
                    settings = settings,
                    onDeletePost = { id ->
                        deletedNewsIds = deletedNewsIds + id
                        LocalCache.saveDeletedNewsIds(context, deletedNewsIds)
                        Toast.makeText(context, "Пост скрыт (можно вернуть в Настройках)", Toast.LENGTH_SHORT).show()
                    }
                )

                1 -> ReviewsScreen(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = innerPadding.calculateTopPadding()),
                    bottomPadding = innerPadding.calculateBottomPadding() + 14.dp,
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

                2 -> AiHouseScreen(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = innerPadding.calculateTopPadding()),
                    bottomPadding = innerPadding.calculateBottomPadding() + 14.dp,
                    settings = settings,
                    newsList = visibleNews,
                    existingReviews = visibleReviews,
                    onNewReviewOrdered = { ordered ->
                        reviewsList = listOf(ordered) + reviewsList
                        LocalCache.saveReviews(context, reviewsList)
                    },
                    onReviewsSilentEdited = { edited ->
                        reviewsList = edited
                        LocalCache.saveReviews(context, edited)
                    }
                )

                3 -> SettingsScreen(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = innerPadding.calculateTopPadding()),
                    bottomPadding = innerPadding.calculateBottomPadding() + 14.dp,
                    settings = settings,
                    deletedTotalCount = deletedNewsIds.size + deletedReviewIds.size,
                    isLoading = isLoading,
                    onSettingsChange = { updated ->
                        settings = updated
                        LocalCache.saveSettings(context, updated)
                    },
                    onTriggerScan = { refreshFeed() },
                    onPurgeAutoJunk = {
                        reviewsList = reviewsList.filterNot { it.isAutoJunk }
                        LocalCache.saveReviews(context, reviewsList)
                    },
                    onRestoreDeleted = {
                        deletedNewsIds = emptySet()
                        deletedReviewIds = emptySet()
                        LocalCache.saveDeletedNewsIds(context, emptySet())
                        LocalCache.saveDeletedReviewIds(context, emptySet())
                        Toast.makeText(context, "Все скрытые публикации восстановлены", Toast.LENGTH_SHORT).show()
                    },
                    onClearCache = {
                        LocalCache.clearAll(context)
                        deletedNewsIds = emptySet()
                        deletedReviewIds = emptySet()
                        reviewsList = ReviewCatalogFactory.defaultReviews()
                        refreshFeed()
                        Toast.makeText(context, "Кэш очищен, лента обновляется", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }
    }
}

// ============================================================================
// КОМПАКТНЫЕ ЧИПЫ И ЭКРАН ЛЕНТЫ (КОМПАКТНЫЙ ПОИСК + СКЛАДНОЙ ФИЛЬТР)
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
        color = if (selected) primary else MaterialTheme.colorScheme.surfaceVariant,
        border = if (selected) null else BorderStroke(0.8.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (selected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(11.dp)
                )
            }
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )
        }
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
        color = if (selected) primary else MaterialTheme.colorScheme.surfaceVariant,
        border = if (selected) null else BorderStroke(0.8.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)
        ) {
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FeedScreen(
    modifier: Modifier = Modifier,
    bottomPadding: Dp,
    news: List<NewsItem>,
    settings: UserAppSettings,
    onDeletePost: (String) -> Unit
) {
    val context = LocalContext.current
    val primary = MaterialTheme.colorScheme.primary
    var selectedBrand by remember { mutableStateOf("Все") }
    var searchQuery by remember { mutableStateOf("") }
    var onlyFresh by remember { mutableStateOf(false) }
    var filtersExpanded by remember { mutableStateOf(false) }

    val activeFilterCount = (if (selectedBrand != "Все") 1 else 0) + (if (onlyFresh) 1 else 0)
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
        contentPadding = PaddingValues(start = 12.dp, end = 12.dp, top = 8.dp, bottom = bottomPadding),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                shadowElevation = 1.dp
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            modifier = Modifier.weight(1f),
                            textStyle = TextStyle(fontSize = 13.sp),
                            placeholder = {
                                Text(
                                    text = "Поиск по новостям...",
                                    fontSize = 12.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = null,
                                    tint = primary,
                                    modifier = Modifier.size(17.dp)
                                )
                            },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(
                                        onClick = { searchQuery = "" },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.Default.Close, contentDescription = "Очистить", modifier = Modifier.size(15.dp))
                                    }
                                }
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(11.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                                focusedBorderColor = primary,
                                unfocusedBorderColor = Color.Transparent
                            )
                        )

                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(11.dp))
                                .clickable { filtersExpanded = !filtersExpanded },
                            shape = RoundedCornerShape(11.dp),
                            color = if (filtersExpanded || activeFilterCount > 0) primary else MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Tune,
                                    contentDescription = "Фильтры",
                                    tint = if (filtersExpanded || activeFilterCount > 0) MaterialTheme.colorScheme.onPrimary else primary,
                                    modifier = Modifier.size(16.dp)
                                )
                                if (activeFilterCount > 0) {
                                    Text(
                                        text = "$activeFilterCount",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = MaterialTheme.colorScheme.onPrimary
                                    )
                                }
                            }
                        }
                    }

                    AnimatedVisibility(
                        visible = filtersExpanded,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        Column(
                            modifier = Modifier.padding(bottom = 4.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Только свежие (за 6 ч)",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Switch(
                                    checked = onlyFresh,
                                    onCheckedChange = { onlyFresh = it },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                                        checkedTrackColor = primary
                                    )
                                )
                            }

                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                brands.forEach { b ->
                                    CompactPillChip(
                                        label = b,
                                        selected = selectedBrand == b,
                                        onClick = { selectedBrand = b }
                                    )
                                }
                                if (activeFilterCount > 0) {
                                    CompactPillChip(
                                        label = "✕ Сброс",
                                        selected = false,
                                        onClick = {
                                            selectedBrand = "Все"
                                            onlyFresh = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        if (filtered.isEmpty()) {
            item {
                SectionBlockCard(
                    title = "Ничего не найдено",
                    subtitle = "Измените поисковый запрос или сбросьте фильтры",
                    accentColor = primary,
                    cornerRadius = settings.cornerRadius
                ) {
                    OutlinedButton(
                        onClick = {
                            selectedBrand = "Все"
                            onlyFresh = false
                            searchQuery = ""
                        },
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Сбросить фильтры", fontSize = 12.sp)
                    }
                }
            }
        } else {
            items(filtered, key = { it.id }) { item ->
                NewsCard(
                    item = item,
                    settings = settings,
                    onOpen = { openCustomTab(context, item.url) },
                    onShare = { shareArticle(context, item) },
                    onDelete = { onDeletePost(item.id) }
                )
            }
        }
    }
}

// ============================================================================
// ЭКРАН ОБЗОРОВ С ЯРКИМИ КАРТОЧКАМИ И КОМПАКТНЫМ ФИЛЬТРОМ
// ============================================================================

private fun brandAccentColor(brand: String, fallback: Color): Color = when (brand.lowercase()) {
    "xiaomi" -> Color(0xFFFF6900)
    "huawei" -> Color(0xFFE11D48)
    "samsung" -> Color(0xFF3B82F6)
    "oppo" -> Color(0xFF10B981)
    "nothing" -> Color(0xFFA78BFA)
    "vivo" -> Color(0xFF06B6D4)
    else -> fallback
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ReviewsScreen(
    modifier: Modifier = Modifier,
    bottomPadding: Dp,
    reviews: List<TechReviewArticle>,
    settings: UserAppSettings,
    onAddOrderedReview: (TechReviewArticle) -> Unit,
    onDeleteReview: (String) -> Unit
) {
    val context = LocalContext.current
    val primary = MaterialTheme.colorScheme.primary
    val scale = settings.fontScale

    var selectedCategory by remember { mutableStateOf("Все") }
    var filtersExpanded by remember { mutableStateOf(false) }
    var openedArticle by remember { mutableStateOf<TechReviewArticle?>(null) }
    var showOrderDialog by remember { mutableStateOf(false) }
    var orderModel by remember { mutableStateOf("") }
    var orderNote by remember { mutableStateOf("") }

    val hasActiveFilter = selectedCategory != "Все"
    val categories = listOf("Все", "Смартфоны", "Носимая электроника", "Ноутбуки", "Умный дом и ТВ")
    val filtered = remember(reviews, selectedCategory) {
        if (selectedCategory == "Все") reviews
        else reviews.filter { it.category.equals(selectedCategory, ignoreCase = true) }
    }

    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(start = 12.dp, end = 12.dp, top = 8.dp, bottom = bottomPadding),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 6.dp)
                        ) {
                            Text(
                                text = "Редакционные тесты",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (hasActiveFilter) selectedCategory else "Все категории • ${filtered.size} шт.",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { filtersExpanded = !filtersExpanded },
                                shape = RoundedCornerShape(10.dp),
                                color = if (filtersExpanded || hasActiveFilter) primary else MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Box(
                                    modifier = Modifier.padding(8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Tune,
                                        contentDescription = "Фильтр категорий",
                                        tint = if (filtersExpanded || hasActiveFilter) MaterialTheme.colorScheme.onPrimary else primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }

                            Button(
                                onClick = { showOrderDialog = true },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                ),
                                contentPadding = PaddingValues(horizontal = 11.dp, vertical = 7.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(15.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Заказать", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    AnimatedVisibility(
                        visible = filtersExpanded,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
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
                }
            }
        }

        items(filtered, key = { it.id }) { article ->
            val cardAccent = brandAccentColor(article.brand, primary)
            val cardShape = RoundedCornerShape(settings.cornerRadius.dp)

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(cardShape)
                    .clickable { openedArticle = article },
                shape = cardShape,
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.3.dp, cardAccent.copy(alpha = 0.5f)),
                shadowElevation = 3.dp
            ) {
                Column {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        cardAccent.copy(alpha = 0.28f),
                                        primary.copy(alpha = 0.14f)
                                    )
                                )
                            )
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(end = 10.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = cardAccent
                                    ) {
                                        Text(
                                            text = article.brand.uppercase(),
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color.White,
                                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                        )
                                    }
                                    Text(
                                        text = article.category,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.78f),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Text(
                                    text = article.deviceName,
                                    fontSize = (17 * scale).sp,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = cardAccent,
                                shadowElevation = 2.dp
                            ) {
                                Column(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Star,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Spacer(Modifier.width(2.dp))
                                        Text(
                                            text = article.score,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Black,
                                            color = Color.White
                                        )
                                    }
                                    Text(
                                        text = "ИЗ 10",
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White.copy(alpha = 0.9f)
                                    )
                                }
                            }
                        }
                    }

                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = article.headline,
                            fontSize = (14 * scale).sp,
                            fontWeight = FontWeight.Bold,
                            lineHeight = (19 * scale).sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Text(
                            text = article.leadSummary,
                            fontSize = (12.5 * scale).sp,
                            lineHeight = (18 * scale).sp,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        if (article.pros.isNotEmpty()) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFF10B981).copy(alpha = 0.12f),
                                border = BorderStroke(0.8.dp, Color(0xFF10B981).copy(alpha = 0.35f))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = Color(0xFF10B981),
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = article.pros.first(),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
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
                                text = "Автор: ${article.authorAgent} • ${article.publishedAt}",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(end = 8.dp)
                            )

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    modifier = Modifier
                                        .size(30.dp)
                                        .clip(RoundedCornerShape(9.dp))
                                        .clickable { onDeleteReview(article.id) },
                                    shape = RoundedCornerShape(9.dp),
                                    color = Color(0xFFF43F5E).copy(alpha = 0.14f)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.DeleteOutline,
                                            contentDescription = "Удалить",
                                            tint = Color(0xFFF43F5E),
                                            modifier = Modifier.size(15.dp)
                                        )
                                    }
                                }

                                Surface(
                                    modifier = Modifier
                                        .size(30.dp)
                                        .clip(RoundedCornerShape(9.dp))
                                        .clickable { shareReviewArticle(context, article) },
                                    shape = RoundedCornerShape(9.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.Share,
                                            contentDescription = "Поделиться",
                                            tint = MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.size(15.dp)
                                        )
                                    }
                                }

                                Surface(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(9.dp))
                                        .clickable { openedArticle = article },
                                    shape = RoundedCornerShape(9.dp),
                                    color = cardAccent
                                ) {
                                    Text(
                                        text = "Разбор →",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
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
            shape = RoundedCornerShape(20.dp),
            title = {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = primary.copy(alpha = 0.16f)
                        ) {
                            Text(
                                text = art.brand,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = primary,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                            )
                        }
                        Text(
                            text = "Оценка: ${art.score} / 10",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFFF59E0B)
                        )
                    }
                    Text(
                        text = art.headline,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(text = art.fullAnalysis, fontSize = 13.sp, lineHeight = 19.sp)
                    HorizontalDivider()
                    Text("Ключевые характеристики:", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    art.specs.forEach { sp ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 9.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = sp.label,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.weight(0.42f)
                                )
                                Text(
                                    text = sp.value,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.weight(0.58f)
                                )
                            }
                        }
                    }
                    HorizontalDivider()
                    Text("Преимущества:", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
                    art.pros.forEach { p -> Text("+ $p", fontSize = 12.sp) }
                    Text("Компромиссы:", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFFF43F5E))
                    art.cons.forEach { c -> Text("− $c", fontSize = 12.sp) }
                }
            },
            confirmButton = {
                Button(onClick = { openedArticle = null }, shape = RoundedCornerShape(10.dp)) {
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
            shape = RoundedCornerShape(20.dp),
            title = { Text("Заказать обзор агенту", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = orderModel,
                        onValueChange = { orderModel = it },
                        label = { Text("Модель устройства") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = orderNote,
                        onValueChange = { orderNote = it },
                        label = { Text("На что сделать акцент") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    shape = RoundedCornerShape(10.dp),
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

// ============================================================================
// КАРТОЧКИ НОВОСТЕЙ В ЛЕНТЕ
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
        appendLine("Оценка редакции: ${article.score} / 10")
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
    settings: UserAppSettings,
    onOpen: () -> Unit,
    onShare: () -> Unit,
    onDelete: () -> Unit
) {
    val primary = MaterialTheme.colorScheme.primary
    val scale = settings.fontScale
    val shape = RoundedCornerShape(settings.cornerRadius.dp)

    val containerColor = when (settings.cardStyle) {
        "filled" -> MaterialTheme.colorScheme.secondaryContainer
        else -> MaterialTheme.colorScheme.surface
    }
    val borderStroke = when (settings.cardStyle) {
        "elevated" -> BorderStroke(0.8.dp, MaterialTheme.colorScheme.outlineVariant)
        "filled" -> BorderStroke(1.dp, primary.copy(alpha = 0.35f))
        else -> BorderStroke(1.1.dp, MaterialTheme.colorScheme.outlineVariant)
    }
    val elevation = if (settings.cardStyle == "elevated") {
        CardDefaults.cardElevation(defaultElevation = 4.dp)
    } else {
        CardDefaults.cardElevation(defaultElevation = 1.dp)
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
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = primary.copy(alpha = 0.16f)
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
                    Text(
                        text = item.platform,
                        fontSize = (10 * scale).sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
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
                    color = MaterialTheme.colorScheme.onSurfaceVariant
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
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp)
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        modifier = Modifier
                            .size(30.dp)
                            .clip(RoundedCornerShape(9.dp))
                            .clickable { onDelete() },
                        shape = RoundedCornerShape(9.dp),
                        color = Color(0xFFF43F5E).copy(alpha = 0.14f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = "Скрыть пост",
                                tint = Color(0xFFF43F5E),
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }

                    Surface(
                        modifier = Modifier
                            .size(30.dp)
                            .clip(RoundedCornerShape(9.dp))
                            .clickable { onShare() },
                        shape = RoundedCornerShape(9.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Поделиться",
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }

                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(9.dp))
                            .clickable { onOpen() },
                        shape = RoundedCornerShape(9.dp),
                        color = primary
                    ) {
                        Text(
                            text = "Читать ↗",
                            fontSize = (11 * scale).sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onPrimary,
                            maxLines = 1,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }
    }
}

// ============================================================================
// ОТДЕЛЬНЫЙ ЭКРАН «ИИ-ДОМ» С ПОШАГОВЫМ ГРАФОМ ПУТЕВЫХ ТОЧЕК (БЕЗ ЗАСТРЕВАНИЯ)
// ============================================================================

private val DefaultOfficeAgents = listOf(
    OfficeAgentProfile(
        id = 0,
        name = "Аврора",
        role = "Директор редакции",
        roomTitle = "★ Директор",
        accentColor = Color(0xFFFBBF24),
        hairColor = Color(0xFFE11D48),
        isFemale = true,
        hasGlasses = false,
        isTopRow = true,
        isManagement = true,
        deskChairPos = Offset(0.125f, 0.165f),
        roomDoorPos = Offset(0.125f, 0.265f),
        corridorEntryPos = Offset(0.125f, 0.315f),
        corridorLaneY = 0.315f,
        diningSeatPos = Offset(0.115f, 0.415f),
        loungeSeatPos = Offset(0.765f, 0.405f)
    ),
    OfficeAgentProfile(
        id = 1,
        name = "Виктор",
        role = "Замруководителя (1-й аудит)",
        roomTitle = "Зам. директора",
        accentColor = Color(0xFFF59E0B),
        hairColor = Color(0xFF475569),
        isFemale = false,
        hasGlasses = true,
        isTopRow = true,
        isManagement = true,
        deskChairPos = Offset(0.375f, 0.165f),
        roomDoorPos = Offset(0.375f, 0.265f),
        corridorEntryPos = Offset(0.375f, 0.315f),
        corridorLaneY = 0.315f,
        diningSeatPos = Offset(0.115f, 0.475f),
        loungeSeatPos = Offset(0.765f, 0.465f)
    ),
    OfficeAgentProfile(
        id = 2,
        name = "Елена",
        role = "Фактчекинг ТТХ (2-й этап)",
        roomTitle = "Аудит ТТХ",
        accentColor = Color(0xFFEC4899),
        hairColor = Color(0xFF9333EA),
        isFemale = true,
        hasGlasses = true,
        isTopRow = true,
        isManagement = true,
        deskChairPos = Offset(0.625f, 0.165f),
        roomDoorPos = Offset(0.625f, 0.265f),
        corridorEntryPos = Offset(0.625f, 0.315f),
        corridorLaneY = 0.315f,
        diningSeatPos = Offset(0.115f, 0.535f),
        loungeSeatPos = Offset(0.765f, 0.525f)
    ),
    OfficeAgentProfile(
        id = 3,
        name = "Вика",
        role = "Обзоры смартфонов",
        roomTitle = "Смартфоны",
        accentColor = Color(0xFF38BDF8),
        hairColor = Color(0xFFF43F5E),
        isFemale = true,
        hasGlasses = false,
        isTopRow = true,
        isManagement = false,
        deskChairPos = Offset(0.875f, 0.165f),
        roomDoorPos = Offset(0.875f, 0.265f),
        corridorEntryPos = Offset(0.875f, 0.315f),
        corridorLaneY = 0.315f,
        diningSeatPos = Offset(0.115f, 0.595f),
        loungeSeatPos = Offset(0.765f, 0.585f)
    ),
    OfficeAgentProfile(
        id = 4,
        name = "Артур",
        role = "Носимая электроника",
        roomTitle = "Часы и аудио",
        accentColor = Color(0xFF10B981),
        hairColor = Color(0xFF1E293B),
        isFemale = false,
        hasGlasses = false,
        isTopRow = false,
        isManagement = false,
        deskChairPos = Offset(0.125f, 0.825f),
        roomDoorPos = Offset(0.125f, 0.725f),
        corridorEntryPos = Offset(0.125f, 0.675f),
        corridorLaneY = 0.675f,
        diningSeatPos = Offset(0.115f, 0.435f),
        loungeSeatPos = Offset(0.835f, 0.415f)
    ),
    OfficeAgentProfile(
        id = 5,
        name = "Нова",
        role = "Обзоры ноутбуков",
        roomTitle = "Ноутбуки",
        accentColor = Color(0xFFA78BFA),
        hairColor = Color(0xFFFBBF24),
        isFemale = true,
        hasGlasses = false,
        isTopRow = false,
        isManagement = false,
        deskChairPos = Offset(0.375f, 0.825f),
        roomDoorPos = Offset(0.375f, 0.725f),
        corridorEntryPos = Offset(0.375f, 0.675f),
        corridorLaneY = 0.675f,
        diningSeatPos = Offset(0.115f, 0.495f),
        loungeSeatPos = Offset(0.835f, 0.475f)
    ),
    OfficeAgentProfile(
        id = 6,
        name = "Дом-Бот",
        role = "Обзоры ТВ и дома",
        roomTitle = "Умный дом",
        accentColor = Color(0xFF60A5FA),
        hairColor = Color(0xFF38BDF8),
        isFemale = false,
        hasGlasses = false,
        isTopRow = false,
        isManagement = false,
        deskChairPos = Offset(0.625f, 0.825f),
        roomDoorPos = Offset(0.625f, 0.725f),
        corridorEntryPos = Offset(0.625f, 0.675f),
        corridorLaneY = 0.675f,
        diningSeatPos = Offset(0.115f, 0.555f),
        loungeSeatPos = Offset(0.835f, 0.535f)
    ),
    OfficeAgentProfile(
        id = 7,
        name = "Авто-ИИ",
        role = "Электроавто",
        roomTitle = "Электрокары",
        accentColor = Color(0xFF22D3EE),
        hairColor = Color(0xFF0F172A),
        isFemale = false,
        hasGlasses = false,
        isTopRow = false,
        isManagement = false,
        deskChairPos = Offset(0.875f, 0.825f),
        roomDoorPos = Offset(0.875f, 0.725f),
        corridorEntryPos = Offset(0.875f, 0.675f),
        corridorLaneY = 0.675f,
        diningSeatPos = Offset(0.115f, 0.610f),
        loungeSeatPos = Offset(0.835f, 0.590f)
    )
)

private val KitchenMenuCatalog = listOf(
    "Сэндвич + Эспрессо" to 140,
    "Паста Карбонара + Чай" to 220,
    "Круассан + Капучино" to 160,
    "Бизнес-ланч + Сок" to 260
)

private val AutonomousTaskTemplates = mapOf(
    0 to listOf(
        "Приёмка отчётов и начисление премий отделу" to "Проверены тикеты сотрудников, начислены бонусы",
        "Контроль стандартов качества редакции" to "Утверждён план обзоров и проверок на смену"
    ),
    1 to listOf(
        "1-й этап аудита ТТХ опубликованных обзоров" to "Сверены стандарты Wi-Fi, Bluetooth и мощности ЗУ",
        "Контроль фактов по официальным даташитам брендов" to "Неточностей в карточках устройств не обнаружено"
    ),
    2 to listOf(
        "2-й глубокий этап перепроверки спецификаций" to "Перепроверены частота ШИМ, плотность PPI и аудиокодеки",
        "Финальная валидация таблиц характеристик" to "Все значения соответствуют официальным спецификациям"
    ),
    3 to listOf(
        "Сверка матриц AMOLED и ШИМ флагманов 2026" to "Проверены пиковая яркость и частота ШИМ в 6 обзорах",
        "Анализ перископ-модулей Leica и ZEISS" to "Подтверждены фокусные расстояния и светосила f/2.6"
    ),
    4 to listOf(
        "Тест автономности Huawei Watch Fit 5 с AOD" to "Зафиксировано 7.5 суток при активном мониторинге SpO2",
        "Проверка битрейта LHDC 5.0 и LDAC на TWS" to "Подтверждена стабильность 900–1000 кбит/с без обрывов"
    ),
    5 to listOf(
        "Замер теплопакета Intel Core Ultra в ультрабуках" to "Стабильные 38–40 Вт под длительной нагрузкой",
        "Проверка цветового охвата 3.1K OLED панелей" to "100% DCI-P3, заводская дельта E < 0.8"
    ),
    6 to listOf(
        "Тест антибликового слоя QD-OLED Glare Free" to "Отражения от дневного света снижены на 78%",
        "Проверка пропускной способности портов HDMI 2.1" to "Подтверждены 4K 144 Гц + VRR на всех 4 разъёмах"
    ),
    7 to listOf(
        "Расчёт кривой зарядки 800В батареи CATL Qilin" to "С 10% до 80% за 13.5 минут на станции 360 кВт",
        "Проверка работы лидара и автопилота NVIDIA Thor" to "Дальность распознавания препятствий до 250 метров"
    )
)

private fun buildCorridorRouteQueue(
    start: Offset,
    profile: OfficeAgentProfile,
    targetZone: AgentLocationZone
): List<Offset> {
    val topLaneY = 0.315f
    val botLaneY = 0.675f
    val leftHallX = 0.255f
    val rightHallX = 0.655f
    val centerSpineX = 0.455f

    val kitchenDoorOuter = Offset(leftHallX, 0.495f)
    val kitchenDoorInner = Offset(0.175f, 0.495f)
    val loungeDoorOuter = Offset(rightHallX, 0.495f)
    val loungeDoorInner = Offset(0.725f, 0.495f)
    val auroraDoorOuter = Offset(0.125f, topLaneY)
    val auroraDoorInner = Offset(0.125f, 0.265f)
    val chiefCheckSpot = Offset(0.125f, 0.225f)

    val pts = mutableListOf<Offset>()

    val currentLaneY: Float = when {
        start.y < 0.28f -> {
            val doorX = if (abs(start.x - 0.125f) < 0.11f) 0.125f else profile.roomDoorPos.x
            pts.add(Offset(doorX, 0.265f))
            pts.add(Offset(doorX, topLaneY))
            topLaneY
        }
        start.y > 0.71f -> {
            pts.add(profile.roomDoorPos)
            pts.add(profile.corridorEntryPos)
            botLaneY
        }
        start.x < 0.20f -> {
            pts.add(kitchenDoorInner)
            pts.add(kitchenDoorOuter)
            pts.add(Offset(leftHallX, topLaneY))
            topLaneY
        }
        start.x > 0.70f -> {
            pts.add(loungeDoorInner)
            pts.add(loungeDoorOuter)
            pts.add(Offset(rightHallX, topLaneY))
            topLaneY
        }
        else -> {
            val nearestLane = if (start.y < 0.5f) topLaneY else botLaneY
            pts.add(Offset(start.x, nearestLane))
            nearestLane
        }
    }

    when (targetZone) {
        AgentLocationZone.DESK -> {
            val destLaneY = if (profile.isTopRow) topLaneY else botLaneY
            if (abs(currentLaneY - destLaneY) > 0.01f) {
                pts.add(Offset(centerSpineX, currentLaneY))
                pts.add(Offset(centerSpineX, destLaneY))
            }
            pts.add(profile.corridorEntryPos)
            pts.add(profile.roomDoorPos)
            pts.add(profile.deskChairPos)
        }

        AgentLocationZone.CHIEF_CHECK -> {
            if (abs(currentLaneY - topLaneY) > 0.01f) {
                pts.add(Offset(centerSpineX, currentLaneY))
                pts.add(Offset(centerSpineX, topLaneY))
            }
            pts.add(auroraDoorOuter)
            pts.add(auroraDoorInner)
            pts.add(chiefCheckSpot)
        }

        AgentLocationZone.DINING -> {
            pts.add(Offset(leftHallX, currentLaneY))
            pts.add(kitchenDoorOuter)
            pts.add(kitchenDoorInner)
            pts.add(profile.diningSeatPos)
        }

        AgentLocationZone.LOUNGE -> {
            pts.add(Offset(rightHallX, currentLaneY))
            pts.add(loungeDoorOuter)
            pts.add(loungeDoorInner)
            pts.add(profile.loungeSeatPos)
        }
    }

    val cleaned = mutableListOf<Offset>()
    for (p in pts) {
        if (cleaned.isEmpty() || hypot(cleaned.last().x - p.x, cleaned.last().y - p.y) > 0.008f) {
            cleaned.add(p)
        }
    }
    return cleaned
}

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
            progress = 0.25f + (ag.id * 0.09f) % 0.45f,
            targetZone = AgentLocationZone.DESK,
            breakSecondsRemaining = 0f,
            breakTotalSeconds = 15f,
            mealPaidForCurrentVisit = false,
            totalMealSpent = 0,
            lastMealReceipt = "Ещё не обедал(а)",
            salaryBalance = 5200 + ag.id * 150,
            rejectionsCount = 0,
            reprimandsCount = 0,
            completedHistory = emptyList()
        )
    }
}

@Composable
fun AiHouseScreen(
    modifier: Modifier = Modifier,
    bottomPadding: Dp,
    settings: UserAppSettings,
    newsList: List<NewsItem>,
    existingReviews: List<TechReviewArticle>,
    onNewReviewOrdered: (TechReviewArticle) -> Unit,
    onReviewsSilentEdited: (List<TechReviewArticle>) -> Unit
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
        mutableStateOf>(DefaultOfficeAgents.associate { it.id to it.deskChairPos })
    }
    var agentWaypoints by remember {
        mutableStateOf>>(emptyMap())
    }
    var agentRouteTargets by remember {
        mutableStateOf>(emptyMap())
    }
    var agentWalkPhases by remember {
        mutableStateOf>(DefaultOfficeAgents.associate { it.id to (it.id * 0.13f) % 1f })
    }
    var smoothWorkPhase by remember { mutableFloatStateOf(0f) }

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
        var lastFrameNanos = 0L
        var saveTimerSec = 0f

        while (isActive && isOfficeActive) {
            withFrameNanos { frameNanos ->
                val dt = if (lastFrameNanos == 0L) {
                    0.016f
                } else {
                    ((frameNanos - lastFrameNanos) / 1_000_000_000f).coerceIn(0.001f, 0.05f)
                }
                lastFrameNanos = frameNanos
                saveTimerSec += dt
                smoothWorkPhase = (smoothWorkPhase + dt * 0.55f) % 1f

                val nowTime = ZonedDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"))
                val updatedStatuses = HashMap<Int, AgentLiveStatus>(agentStatuses)
                val updatedPositions = HashMap<Int, Offset>(DefaultOfficeAgents.size)
                val updatedWaypoints = HashMap<Int, List<Offset>>(agentWaypoints)
                val updatedRouteTargets = HashMap<Int, AgentLocationZone>(agentRouteTargets)
                val updatedWalkPhases = HashMap<Int, Float>(DefaultOfficeAgents.size)
                var newChiefEntries = chiefAuditLog
                var newDeputyEntries = deputyEditLog
                var nextTicket = ticketCounter

                val moveSpeedPerSec = 0.135f
                val workSpeedPerSec = 0.135f

                DefaultOfficeAgents.forEach { profile ->
                    val status = updatedStatuses[profile.id] ?: return@forEach
                    val curPos: Offset = agentPositions[profile.id] ?: profile.deskChairPos
                    val prevWalk: Float = agentWalkPhases[profile.id] ?: 0f

                    val finalDestination: Offset = when (status.targetZone) {
                        AgentLocationZone.DESK -> profile.deskChairPos
                        AgentLocationZone.CHIEF_CHECK -> Offset(0.125f, 0.225f)
                        AgentLocationZone.DINING -> profile.diningSeatPos
                        AgentLocationZone.LOUNGE -> profile.loungeSeatPos
                    }

                    var queue: List<Offset> = updatedWaypoints[profile.id].orEmpty()
                    if (updatedRouteTargets[profile.id] != status.targetZone) {
                        queue = buildCorridorRouteQueue(curPos, profile, status.targetZone)
                        updatedRouteTargets[profile.id] = status.targetZone
                        updatedWaypoints[profile.id] = queue
                    }

                    val distToFinal = hypot(finalDestination.x - curPos.x, finalDestination.y - curPos.y)
                    if (queue.isNotEmpty() || distToFinal > 0.005f) {
                        val nextWaypoint: Offset = queue.firstOrNull() ?: finalDestination
                        val wDist = hypot(nextWaypoint.x - curPos.x, nextWaypoint.y - curPos.y)
                        val step = moveSpeedPerSec * dt

                        if (wDist <= step) {
                            updatedPositions[profile.id] = nextWaypoint
                            if (queue.isNotEmpty()) {
                                updatedWaypoints[profile.id] = queue.drop(1)
                            }
                        } else {
                            updatedPositions[profile.id] = Offset(
                                x = curPos.x + ((nextWaypoint.x - curPos.x) / wDist) * step,
                                y = curPos.y + ((nextWaypoint.y - curPos.y) / wDist) * step
                            )
                        }
                        updatedWalkPhases[profile.id] = (prevWalk + dt * 2.2f) % 1f
                    } else {
                        updatedPositions[profile.id] = finalDestination
                        updatedWaypoints[profile.id] = emptyList()
                        updatedWalkPhases[profile.id] = (prevWalk + dt * 1.2f) % 1f

                        when (status.targetZone) {
                            AgentLocationZone.DESK -> {
                                val nextProg = (status.progress + workSpeedPerSec * dt).coerceAtMost(1f)
                                if (nextProg < 1f) {
                                    updatedStatuses[profile.id] = status.copy(progress = nextProg)
                                } else {
                                    if (profile.id == 0) {
                                        nextTicket++
                                        val pool = AutonomousTaskTemplates[0]!!
                                        val nextTaskTpl = pool[nextTicket % pool.size]
                                        val doneTask = status.currentTask.copy(
                                            completedAt = nowTime,
                                            chiefApproved = true,
                                            salaryDelta = 450
                                        )
                                        val goEat = (nextTicket % 2 == 0)
                                        updatedStatuses[profile.id] = status.copy(
                                            currentTask = SoloTaskItem(
                                                ticketNumber = nextTicket,
                                                badge = "ДИРЕКТОР",
                                                title = nextTaskTpl.first,
                                                resultSummary = nextTaskTpl.second,
                                                startedAt = nowTime
                                            ),
                                            progress = 0f,
                                            targetZone = if (goEat) AgentLocationZone.DINING else AgentLocationZone.LOUNGE,
                                            breakSecondsRemaining = 14f,
                                            breakTotalSeconds = 14f,
                                            mealPaidForCurrentVisit = false,
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
                                val delta = if (approved) 380 else -150
                                val nextReprimands = if (approved) status.reprimandsCount else status.reprimandsCount + 1
                                val isReattestation = !approved && nextReprimands >= 3
                                val finalReprimands = if (isReattestation) 0 else nextReprimands

                                val verdictText = when {
                                    approved -> "Принято Авророй: премия +380 ₽"
                                    isReattestation -> "3-й выговор! Переаттестация (−150 ₽)"
                                    else -> "Выговор от Авроры ($nextReprimands/3): доработать (−150 ₽)"
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

                                if (approved && (profile.id == 1 || profile.id == 2)) {
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

                                val nextZone = when {
                                    !approved -> AgentLocationZone.DESK
                                    nextTicket % 3 == 1 -> AgentLocationZone.DINING
                                    nextTicket % 3 == 2 -> AgentLocationZone.LOUNGE
                                    else -> AgentLocationZone.DESK
                                }
                                val breakTime = if (nextZone == AgentLocationZone.DESK) 0f else 15f

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
                                    breakSecondsRemaining = breakTime,
                                    breakTotalSeconds = breakTime.coerceAtLeast(1f),
                                    mealPaidForCurrentVisit = false,
                                    salaryBalance = (status.salaryBalance + delta).coerceAtLeast(0),
                                    rejectionsCount = status.rejectionsCount + if (approved) 0 else 1,
                                    reprimandsCount = finalReprimands,
                                    completedHistory = (listOf(doneTask) + status.completedHistory).take(12)
                                )
                            }

                            AgentLocationZone.DINING -> {
                                var curSt = status
                                if (!curSt.mealPaidForCurrentVisit) {
                                    val dish = KitchenMenuCatalog[(profile.id + nextTicket) % KitchenMenuCatalog.size]
                                    val dishName = dish.first
                                    val dishCost = dish.second
                                    val newBalance = (curSt.salaryBalance - dishCost).coerceAtLeast(0)
                                    curSt = curSt.copy(
                                        mealPaidForCurrentVisit = true,
                                        salaryBalance = newBalance,
                                        totalMealSpent = curSt.totalMealSpent + dishCost,
                                        lastMealReceipt = "$dishName (−$dishCost ₽ в $nowTime)"
                                    )
                                }

                                val left = curSt.breakSecondsRemaining - dt
                                if (left > 0f) {
                                    updatedStatuses[profile.id] = curSt.copy(breakSecondsRemaining = left)
                                } else {
                                    updatedStatuses[profile.id] = curSt.copy(
                                        targetZone = AgentLocationZone.LOUNGE,
                                        breakSecondsRemaining = 10f,
                                        breakTotalSeconds = 10f,
                                        mealPaidForCurrentVisit = false
                                    )
                                }
                            }

                            AgentLocationZone.LOUNGE -> {
                                val left = status.breakSecondsRemaining - dt
                                if (left > 0f) {
                                    updatedStatuses[profile.id] = status.copy(breakSecondsRemaining = left)
                                } else {
                                    updatedStatuses[profile.id] = status.copy(
                                        targetZone = AgentLocationZone.DESK,
                                        breakSecondsRemaining = 0f,
                                        mealPaidForCurrentVisit = false
                                    )
                                }
                            }
                        }
                    }
                }

                ticketCounter = nextTicket
                agentStatuses = updatedStatuses
                agentPositions = updatedPositions
                agentWaypoints = updatedWaypoints
                agentRouteTargets = updatedRouteTargets
                agentWalkPhases = updatedWalkPhases
                chiefAuditLog = newChiefEntries
                deputyEditLog = newDeputyEntries

                if (saveTimerSec >= 4.0f) {
                    saveTimerSec = 0f
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

    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(start = 12.dp, end = 12.dp, top = 8.dp, bottom = bottomPadding),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            SectionBlockCard(
                title = "Карта ИИ-Дома",
                subtitle = "Руководство сверху • Кухня слева • Коридорные дорожки",
                accentColor = primary,
                cornerRadius = settings.cornerRadius,
                headerAction = {
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { isOfficeActive = !isOfficeActive },
                        shape = RoundedCornerShape(10.dp),
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
                                text = if (isOfficeActive) "Активен" else "Пауза",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isOfficeActive) Color(0xFF10B981) else Color(0xFFF59E0B)
                            )
                        }
                    }
                }
            ) {
                AiNewYearHouseInteractiveMap(
                    agents = DefaultOfficeAgents,
                    positions = agentPositions,
                    walkPhases = agentWalkPhases,
                    statuses = agentStatuses,
                    selectedAgentId = selectedAgentId,
                    primaryColor = primary,
                    garlandPhase = garlandPhase,
                    snowPhase = snowPhase,
                    liveWorkPhase = smoothWorkPhase,
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
                            else MaterialTheme.colorScheme.surfaceVariant,
                            border = BorderStroke(
                                1.1.dp,
                                if (isSel) ag.accentColor else MaterialTheme.colorScheme.outlineVariant
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 6.dp),
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
            }
        }

        item {
            val selectedProfile = DefaultOfficeAgents.first { it.id == selectedAgentId }
            val selectedStatus = agentStatuses[selectedAgentId]
            if (selectedStatus != null) {
                val approvedCount = selectedStatus.completedHistory.count { it.chiefApproved }
                val scheduleProgress = when (selectedStatus.targetZone) {
                    AgentLocationZone.DESK -> selectedStatus.progress.coerceIn(0f, 1f)
                    AgentLocationZone.CHIEF_CHECK -> 1f
                    AgentLocationZone.DINING, AgentLocationZone.LOUNGE -> {
                        val total = selectedStatus.breakTotalSeconds.coerceAtLeast(1f)
                        (1f - (selectedStatus.breakSecondsRemaining / total)).coerceIn(0f, 1f)
                    }
                }
                val scheduleLabel = when (selectedStatus.targetZone) {
                    AgentLocationZone.DESK -> "Работа за ПК • ${(scheduleProgress * 100).toInt()}%"
                    AgentLocationZone.CHIEF_CHECK -> "Сдача отчёта Авроре"
                    AgentLocationZone.DINING -> "Обед на кухне • ${selectedStatus.breakSecondsRemaining.toInt()} сек"
                    AgentLocationZone.LOUNGE -> "Отдых у ТВ • ${selectedStatus.breakSecondsRemaining.toInt()} сек"
                }

                SectionBlockCard(
                    title = "${selectedProfile.name} • ${selectedProfile.role}",
                    subtitle = scheduleLabel,
                    accentColor = selectedProfile.accentColor,
                    cornerRadius = settings.cornerRadius,
                    headerAction = {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF10B981).copy(alpha = 0.16f)
                        ) {
                            Text(
                                text = "${selectedStatus.salaryBalance} ₽",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF10B981),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF10B981).copy(alpha = 0.12f)
                        ) {
                            Text(
                                text = "Премий: $approvedCount",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF10B981),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                            )
                        }
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFFBBF24).copy(alpha = 0.14f)
                        ) {
                            Text(
                                text = "Еда: −${selectedStatus.totalMealSpent} ₽",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFF59E0B),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                            )
                        }
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            color = if (selectedStatus.reprimandsCount > 0) Color(0xFFF43F5E).copy(alpha = 0.14f)
                            else MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                text = "Выг.: ${selectedStatus.reprimandsCount}/3",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (selectedStatus.reprimandsCount > 0) Color(0xFFF43F5E)
                                else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(9.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 9.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Restaurant,
                                contentDescription = null,
                                tint = Color(0xFFF59E0B),
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "Чек кухни: ${selectedStatus.lastMealReceipt}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Text(
                        text = "Тикет #${selectedStatus.currentTask.ticketNumber}: ${selectedStatus.currentTask.title}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    LinearProgressIndicator(
                        progress = { scheduleProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(CircleShape),
                        color = selectedProfile.accentColor,
                        trackColor = selectedProfile.accentColor.copy(alpha = 0.18f)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                agentStatuses = agentStatuses.toMutableMap().apply {
                                    this[selectedProfile.id] = selectedStatus.copy(
                                        targetZone = AgentLocationZone.DINING,
                                        breakSecondsRemaining = 16f,
                                        breakTotalSeconds = 16f,
                                        mealPaidForCurrentVisit = false
                                    )
                                }
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("На обед (платно)", fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                        }

                        OutlinedButton(
                            onClick = {
                                agentStatuses = agentStatuses.toMutableMap().apply {
                                    this[selectedProfile.id] = selectedStatus.copy(
                                        targetZone = AgentLocationZone.LOUNGE,
                                        breakSecondsRemaining = 14f,
                                        breakTotalSeconds = 14f
                                    )
                                }
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Отдохнуть у ТВ", fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                        }
                    }
                }
            }
        }

        item {
            SectionBlockCard(
                title = "Управление отделом и журналы",
                subtitle = "Прямые приказы агентам, отчёты Авроры и правки ТТХ",
                accentColor = primary,
                cornerRadius = settings.cornerRadius
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    CompactPillChip(
                        label = "Приказы",
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
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
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
                                    "Отправить на обед"
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
                                    label = { Text("Модель устройства (напр. Xiaomi 17T Pro)", fontSize = 11.sp) },
                                    singleLine = true,
                                    shape = RoundedCornerShape(11.dp)
                                )
                                OutlinedTextField(
                                    value = cmdNoteInput,
                                    onValueChange = { cmdNoteInput = it },
                                    modifier = Modifier.fillMaxWidth(),
                                    label = { Text("Акцент обзора (необязательно)", fontSize = 11.sp) },
                                    singleLine = true,
                                    shape = RoundedCornerShape(11.dp)
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
                                                "${targetAgent.name} подготовил(а) обзор ${created.deviceName}",
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
                                                        breakSecondsRemaining = 16f,
                                                        breakTotalSeconds = 16f,
                                                        mealPaidForCurrentVisit = false
                                                    )
                                                }
                                                Toast.makeText(
                                                    context,
                                                    "${targetAgent.name} идёт по дорожке на кухню",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        }
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(11.dp)
                            ) {
                                Icon(Icons.Default.AdminPanelSettings, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    text = "Отправить приказ: ${DefaultOfficeAgents[cmdAgentIndex].name}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    1 -> {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            if (chiefAuditLog.isEmpty()) {
                                Text(
                                    text = "Журнал приёмки задач Директором Авророй пока пуст.",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            } else {
                                chiefAuditLog.take(6).forEach { entry ->
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(9.dp),
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
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
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
                                    text = "Правки ТТХ от Виктора и Елены пока не зафиксированы.",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            } else {
                                deputyEditLog.take(6).forEach { dep ->
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(9.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.EditNote,
                                                contentDescription = null,
                                                tint = primary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(Modifier.width(8.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = "${dep.reviewerName} → ${dep.deviceName} (${dep.editedAt})",
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = primary
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
}

// ============================================================================
// ОТДЕЛЬНЫЙ ЭКРАН «НАСТРОЙКИ» — ВСЁ РАЗНЕСЕНО ПО САМОСТОЯТЕЛЬНЫМ БЛОКАМ
// ============================================================================

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    bottomPadding: Dp,
    settings: UserAppSettings,
    deletedTotalCount: Int,
    isLoading: Boolean,
    onSettingsChange: (UserAppSettings) -> Unit,
    onTriggerScan: () -> Unit,
    onPurgeAutoJunk: () -> Unit,
    onRestoreDeleted: () -> Unit,
    onClearCache: () -> Unit
) {
    val context = LocalContext.current
    val primary = MaterialTheme.colorScheme.primary

    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(start = 12.dp, end = 12.dp, top = 8.dp, bottom = bottomPadding),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            SectionBlockCard(
                title = "Цветовая палитра",
                subtitle = "Меняет акцент, фон и обводки всего приложения",
                accentColor = primary,
                cornerRadius = settings.cornerRadius
            ) {
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
                                color = if (isSel) pal.primaryDark.copy(alpha = 0.22f)
                                else MaterialTheme.colorScheme.surfaceVariant,
                                border = BorderStroke(
                                    width = if (isSel) 1.5.dp else 0.8.dp,
                                    color = if (isSel) pal.primaryDark else MaterialTheme.colorScheme.outlineVariant
                                )
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(14.dp)
                                            .clip(CircleShape)
                                            .background(pal.primaryDark)
                                    )
                                    Spacer(Modifier.width(7.dp))
                                    Text(
                                        text = pal.name,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSel) FontWeight.ExtraBold else FontWeight.Medium,
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
        }

        item {
            SectionBlockCard(
                title = "Тема и оформление карточек",
                subtitle = "Светлый/тёмный режим и внешний вид блоков",
                accentColor = primary,
                cornerRadius = settings.cornerRadius
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Режим экрана:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
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
                        text = "Стиль карточек ленты:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
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
            }
        }

        item {
            SectionBlockCard(
                title = "Шрифт и геометрия",
                subtitle = "Размер текста, радиус углов и показ анонсов",
                accentColor = primary,
                cornerRadius = settings.cornerRadius
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Масштаб текста:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
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

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Скругление карточек:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
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
                        steps = 3,
                        colors = SliderDefaults.colors(thumbColor = primary, activeTrackColor = primary)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Краткий анонс в новостях",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp)
                    )
                    Switch(
                        checked = settings.showSummary,
                        onCheckedChange = { onSettingsChange(settings.copy(showSummary = it)) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                            checkedTrackColor = primary
                        )
                    )
                }
            }
        }

        item {
            SectionBlockCard(
                title = "Данные и синхронизация",
                subtitle = "Восстановление скрытых постов и очистка кэша",
                accentColor = primary,
                cornerRadius = settings.cornerRadius
            ) {
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
                        Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = "Вернуть ($deletedTotalCount)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    OutlinedButton(
                        onClick = {
                            onPurgeAutoJunk()
                            Toast.makeText(context, "Авто-мусор очищен", Toast.LENGTH_SHORT).show()
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
}

// ============================================================================
// ИНТЕРАКТИВНЫЙ ХОЛСТ ИИ-ДОМА
// ============================================================================

@Composable
private fun AiNewYearHouseInteractiveMap(
    agents: List<OfficeAgentProfile>,
    positions: Map<Int, Offset>,
    walkPhases: Map<Int, Float>,
    statuses: Map<Int, AgentLiveStatus>,
    selectedAgentId: Int,
    primaryColor: Color,
    garlandPhase: Float,
    snowPhase: Float,
    liveWorkPhase: Float,
    onSelectAgent: (Int) -> Unit
) {
    val density = LocalDensity.current
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .height(385.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFF060B15))
            .border(1.3.dp, primaryColor.copy(alpha = 0.45f), RoundedCornerShape(14.dp))
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
        val boxWPx = with(density) { boxW.toPx() }
        val boxHPx = with(density) { boxH.toPx() }
        val badgeHalfWPx = with(density) { 26.dp.toPx() }
        val badgeOffsetYPx = with(density) { 29.dp.toPx() }
        val minXPx = with(density) { 2.dp.toPx() }
        val maxXPx = with(density) { (boxW - 54.dp).toPx() }
        val minYPx = with(density) { 13.dp.toPx() }
        val maxYPx = with(density) { (boxH - 28.dp).toPx() }

        Canvas(modifier = Modifier.fillMaxSize()) {
            drawAiNewYearHouseFloorplan(
                agents = agents,
                positions = positions,
                walkPhases = walkPhases,
                statuses = statuses,
                selectedId = selectedAgentId,
                garlandPhase = garlandPhase,
                snowPhase = snowPhase,
                liveWorkPhase = liveWorkPhase
            )
        }

        for (col in 0 until 4) {
            val ag = agents[col]
            val roomCenterX = boxW * ((col + 0.5f) / 4f)
            Box(
                modifier = Modifier
                    .offset(x = roomCenterX - 40.dp, y = 4.dp)
                    .width(80.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = ag.roomTitle,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (ag.isManagement) Color(0xFFFBBF24) else ag.accentColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        for (col in 0 until 4) {
            val ag = agents[col + 4]
            val roomCenterX = boxW * ((col + 0.5f) / 4f)
            Box(
                modifier = Modifier
                    .offset(x = roomCenterX - 40.dp, y = boxH - 14.dp)
                    .width(80.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = ag.roomTitle,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = ag.accentColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Text(
            text = "КУХНЯ • КАССА",
            fontSize = 6.5.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFFFBBF24).copy(alpha = 0.92f),
            modifier = Modifier.offset(x = boxW * 0.02f, y = boxH * 0.345f)
        )
        Text(
            text = "ОТДЫХ • ТВ",
            fontSize = 7.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFF38BDF8).copy(alpha = 0.92f),
            modifier = Modifier.offset(x = boxW * 0.745f, y = boxH * 0.345f)
        )

        agents.forEach { ag ->
            val pos = positions[ag.id] ?: ag.deskChairPos
            val isSel = ag.id == selectedAgentId
            val tx = (boxWPx * pos.x - badgeHalfWPx).coerceIn(minXPx, maxXPx)
            val ty = (boxHPx * pos.y - badgeOffsetYPx).coerceIn(minYPx, maxYPx)

            Surface(
                modifier = Modifier
                    .graphicsLayer {
                        translationX = tx
                        translationY = ty
                    }
                    .width(52.dp),
                shape = RoundedCornerShape(5.dp),
                color = if (isSel) ag.accentColor else Color(0xFF09111E).copy(alpha = 0.9f),
                border = BorderStroke(
                    width = if (isSel) 1.1.dp else 0.7.dp,
                    color = ag.accentColor.copy(alpha = if (isSel) 1f else 0.75f)
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
    walkPhases: Map<Int, Float>,
    statuses: Map<Int, AgentLiveStatus>,
    selectedId: Int,
    garlandPhase: Float,
    snowPhase: Float,
    liveWorkPhase: Float
) {
    val w = size.width
    val h = size.height

    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(Color(0xFF0B1424), Color(0xFF101D33), Color(0xFF0B1424)),
            startY = h * 0.265f,
            endY = h * 0.735f
        ),
        topLeft = Offset(0f, h * 0.265f),
        size = Size(w, h * 0.47f)
    )

    val topLaneY = h * 0.315f
    val botLaneY = h * 0.675f
    val leftHallX = w * 0.255f
    val rightHallX = w * 0.655f
    val centerSpineX = w * 0.455f
    val pathColor = Color(0xFF38BDF8).copy(alpha = 0.14f)
    val pathStroke = Color(0xFF38BDF8).copy(alpha = 0.32f)

    drawRoundRect(
        color = pathColor,
        topLeft = Offset(w * 0.08f, topLaneY - 7.dp.toPx()),
        size = Size(w * 0.84f, 14.dp.toPx()),
        cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
    )
    drawRoundRect(
        color = pathColor,
        topLeft = Offset(w * 0.08f, botLaneY - 7.dp.toPx()),
        size = Size(w * 0.84f, 14.dp.toPx()),
        cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
    )
    listOf(leftHallX, centerSpineX, rightHallX).forEach { vx ->
        drawRoundRect(
            color = pathColor,
            topLeft = Offset(vx - 7.dp.toPx(), topLaneY - 5.dp.toPx()),
            size = Size(14.dp.toPx(), (botLaneY - topLaneY) + 10.dp.toPx()),
            cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
        )
    }
    val dashEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f), 0f)
    drawLine(pathStroke, Offset(w * 0.125f, topLaneY), Offset(w * 0.875f, topLaneY), 1.dp.toPx(), pathEffect = dashEffect)
    drawLine(pathStroke, Offset(w * 0.125f, botLaneY), Offset(w * 0.875f, botLaneY), 1.dp.toPx(), pathEffect = dashEffect)
    drawLine(pathStroke, Offset(leftHallX, topLaneY), Offset(leftHallX, botLaneY), 1.dp.toPx(), pathEffect = dashEffect)
    drawLine(pathStroke, Offset(centerSpineX, topLaneY), Offset(centerSpineX, botLaneY), 1.dp.toPx(), pathEffect = dashEffect)
    drawLine(pathStroke, Offset(rightHallX, topLaneY), Offset(rightHallX, botLaneY), 1.dp.toPx(), pathEffect = dashEffect)

    val colWidth = w / 4f
    val bulbColors = listOf(Color(0xFFF43F5E), Color(0xFFFBBF24), Color(0xFF38BDF8), Color(0xFF10B981))

    for (col in 0 until 4) {
        val x0 = col * colWidth + 2.5.dp.toPx()
        val roomW = colWidth - 5.dp.toPx()

        val topAg = agents[col]
        val topSt = statuses[topAg.id]
        val isTopSel = selectedId == topAg.id
        val topRoomH = h * 0.258f

        drawRoundRect(
            brush = Brush.verticalGradient(
                colors = if (topAg.isManagement) listOf(Color(0xFF241E18), Color(0xFF151C2E))
                else listOf(Color(0xFF172845), Color(0xFF101C33)),
                startY = 2.dp.toPx(),
                endY = 2.dp.toPx() + topRoomH
            ),
            topLeft = Offset(x0, 2.dp.toPx()),
            size = Size(roomW, topRoomH),
            cornerRadius = CornerRadius(9.dp.toPx(), 9.dp.toPx())
        )
        drawRoundRect(
            color = if (topAg.isManagement) {
                Color(0xFFFBBF24).copy(alpha = if (isTopSel) 0.95f else 0.52f)
            } else {
                topAg.accentColor.copy(alpha = if (isTopSel) 0.92f else 0.38f)
            },
            topLeft = Offset(x0, 2.dp.toPx()),
            size = Size(roomW, topRoomH),
            cornerRadius = CornerRadius(9.dp.toPx(), 9.dp.toPx()),
            style = Stroke(width = if (isTopSel || topAg.isManagement) 1.7.dp.toPx() else 1.dp.toPx())
        )
        drawRect(
            color = Color(0xFF0E1A2E),
            topLeft = Offset(x0 + roomW * 0.35f, topRoomH - 1.dp.toPx()),
            size = Size(roomW * 0.30f, 9.dp.toPx())
        )
        drawLine(
            color = pathStroke,
            start = Offset(x0 + roomW * 0.5f, topRoomH),
            end = Offset(x0 + roomW * 0.5f, topLaneY),
            strokeWidth = 1.dp.toPx(),
            pathEffect = dashEffect
        )

        drawFestiveWreath(center = Offset(x0 + roomW * 0.14f, 18.dp.toPx()), radius = 4.dp.toPx())
        for (b in 0 until 5) {
            val bx = x0 + roomW * (0.25f + b * 0.15f)
            val bAlpha = 0.45f + 0.55f * abs(sin(garlandPhase + b * 0.9f + col))
            drawCircle(
                color = bulbColors[(b + col) % bulbColors.size].copy(alpha = bAlpha),
                radius = 1.5.dp.toPx(),
                center = Offset(bx, 14.dp.toPx())
            )
        }

        drawWorkstationWithLampAndChair(
            deskLeft = x0 + roomW * 0.16f,
            deskTop = h * 0.068f,
            deskWidth = roomW * 0.68f,
            deskHeight = h * 0.046f,
            chairCenter = Offset(topAg.deskChairPos.x * w, topAg.deskChairPos.y * h),
            accentColor = topAg.accentColor,
            progress = topSt?.progress ?: 0.5f,
            liveWorkPhase = liveWorkPhase + col * 0.21f,
            isTopRow = true
        )

        val botAg = agents[col + 4]
        val botSt = statuses[botAg.id]
        val isBotSel = selectedId == botAg.id
        val botRoomTop = h * 0.728f
        val botRoomH = h * 0.258f

        drawRoundRect(
            brush = Brush.verticalGradient(
                colors = listOf(Color(0xFF101C33), Color(0xFF172845)),
                startY = botRoomTop,
                endY = botRoomTop + botRoomH
            ),
            topLeft = Offset(x0, botRoomTop),
            size = Size(roomW, botRoomH),
            cornerRadius = CornerRadius(9.dp.toPx(), 9.dp.toPx())
        )
        drawRoundRect(
            color = botAg.accentColor.copy(alpha = if (isBotSel) 0.92f else 0.38f),
            topLeft = Offset(x0, botRoomTop),
            size = Size(roomW, botRoomH),
            cornerRadius = CornerRadius(9.dp.toPx(), 9.dp.toPx()),
            style = Stroke(width = if (isBotSel) 1.7.dp.toPx() else 1.dp.toPx())
        )
        drawRect(
            color = Color(0xFF0E1A2E),
            topLeft = Offset(x0 + roomW * 0.35f, botRoomTop - 7.dp.toPx()),
            size = Size(roomW * 0.30f, 9.dp.toPx())
        )
        drawLine(
            color = pathStroke,
            start = Offset(x0 + roomW * 0.5f, botRoomTop),
            end = Offset(x0 + roomW * 0.5f, botLaneY),
            strokeWidth = 1.dp.toPx(),
            pathEffect = dashEffect
        )

        drawFestiveWreath(center = Offset(x0 + roomW * 0.86f, botRoomTop + 14.dp.toPx()), radius = 4.dp.toPx())
        for (b in 0 until 5) {
            val bx = x0 + roomW * (0.12f + b * 0.15f)
            val bAlpha = 0.45f + 0.55f * abs(sin(garlandPhase + b * 0.9f + col + 2f))
            drawCircle(
                color = bulbColors[(b + col + 1) % bulbColors.size].copy(alpha = bAlpha),
                radius = 1.5.dp.toPx(),
                center = Offset(bx, botRoomTop + botRoomH - 13.dp.toPx())
            )
        }

        drawWorkstationWithLampAndChair(
            deskLeft = x0 + roomW * 0.16f,
            deskTop = botRoomTop + botRoomH * 0.56f,
            deskWidth = roomW * 0.68f,
            deskHeight = h * 0.046f,
            chairCenter = Offset(botAg.deskChairPos.x * w, botAg.deskChairPos.y * h),
            accentColor = botAg.accentColor,
            progress = botSt?.progress ?: 0.5f,
            liveWorkPhase = liveWorkPhase + (col + 4) * 0.19f,
            isTopRow = false
        )
    }

    val kitchenLeft = 3.dp.toPx()
    val kitchenTop = h * 0.335f
    val kitchenW = w * 0.185f
    val kitchenH = h * 0.32f

    drawRoundRect(
        color = Color(0xFF16253E),
        topLeft = Offset(kitchenLeft, kitchenTop),
        size = Size(kitchenW, kitchenH),
        cornerRadius = CornerRadius(9.dp.toPx(), 9.dp.toPx())
    )
    drawRoundRect(
        color = Color(0xFFFBBF24).copy(alpha = 0.42f),
        topLeft = Offset(kitchenLeft, kitchenTop),
        size = Size(kitchenW, kitchenH),
        cornerRadius = CornerRadius(9.dp.toPx(), 9.dp.toPx()),
        style = Stroke(width = 1.2.dp.toPx())
    )
    drawRect(
        color = Color(0xFF0F1B30),
        topLeft = Offset(kitchenLeft + kitchenW - 2.dp.toPx(), kitchenTop + kitchenH * 0.36f),
        size = Size(6.dp.toPx(), kitchenH * 0.28f)
    )
    drawLine(
        color = pathStroke,
        start = Offset(kitchenLeft + kitchenW, kitchenTop + kitchenH * 0.5f),
        end = Offset(leftHallX, kitchenTop + kitchenH * 0.5f),
        strokeWidth = 1.dp.toPx(),
        pathEffect = dashEffect
    )

    val counterW = 15.dp.toPx()
    drawRoundRect(
        color = Color(0xFF334155),
        topLeft = Offset(kitchenLeft + 2.dp.toPx(), kitchenTop + 14.dp.toPx()),
        size = Size(counterW, kitchenH - 20.dp.toPx()),
        cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx())
    )

    val foodStartY = kitchenTop + 19.dp.toPx()
    drawCircle(color = Color.White, radius = 4.2.dp.toPx(), center = Offset(kitchenLeft + 9.5.dp.toPx(), foodStartY))
    drawCircle(color = Color(0xFFEA580C), radius = 2.8.dp.toPx(), center = Offset(kitchenLeft + 9.5.dp.toPx(), foodStartY))
    drawCircle(color = Color(0xFF22C55E), radius = 1.1.dp.toPx(), center = Offset(kitchenLeft + 8.8.dp.toPx(), foodStartY - 0.6.dp.toPx()))

    val food2Y = foodStartY + 14.dp.toPx()
    drawCircle(color = Color.White, radius = 4.2.dp.toPx(), center = Offset(kitchenLeft + 9.5.dp.toPx(), food2Y))
    drawRoundRect(
        color = Color(0xFFFBBF24),
        topLeft = Offset(kitchenLeft + 6.8.dp.toPx(), food2Y - 2.dp.toPx()),
        size = Size(5.4.dp.toPx(), 4.dp.toPx()),
        cornerRadius = CornerRadius(1.2.dp.toPx(), 1.2.dp.toPx())
    )

    val food3Y = food2Y + 14.dp.toPx()
    drawCircle(color = Color.White, radius = 4.2.dp.toPx(), center = Offset(kitchenLeft + 9.5.dp.toPx(), food3Y))
    drawCircle(color = Color(0xFF10B981), radius = 2.7.dp.toPx(), center = Offset(kitchenLeft + 9.5.dp.toPx(), food3Y))
    drawCircle(color = Color(0xFFF43F5E), radius = 1.0.dp.toPx(), center = Offset(kitchenLeft + 10.2.dp.toPx(), food3Y + 0.6.dp.toPx()))

    val coffeeY = food3Y + 13.dp.toPx()
    drawRoundRect(
        color = Color(0xFF090D16),
        topLeft = Offset(kitchenLeft + 3.5.dp.toPx(), coffeeY),
        size = Size(12.dp.toPx(), 11.dp.toPx()),
        cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
    )
    drawCircle(color = Color(0xFF10B981), radius = 1.5.dp.toPx(), center = Offset(kitchenLeft + 12.5.dp.toPx(), coffeeY + 3.dp.toPx()))
    val steamRise = (liveWorkPhase * 7.dp.toPx())
    drawCircle(
        color = Color.White.copy(alpha = (1f - liveWorkPhase) * 0.55f),
        radius = 1.8.dp.toPx(),
        center = Offset(kitchenLeft + 17.dp.toPx() + sin(liveWorkPhase * 6.28f) * 1.5f, coffeeY + 4.dp.toPx() - steamRise)
    )

    val posY = coffeeY + 14.dp.toPx()
    drawRoundRect(
        color = Color(0xFF1E293B),
        topLeft = Offset(kitchenLeft + 3.5.dp.toPx(), posY),
        size = Size(12.dp.toPx(), 11.dp.toPx()),
        cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
    )
    drawRect(
        color = Color(0xFF10B981),
        topLeft = Offset(kitchenLeft + 5.5.dp.toPx(), posY + 2.dp.toPx()),
        size = Size(8.dp.toPx(), 4.5.dp.toPx())
    )

    val loungeLeft = w * 0.705f
    val loungeTop = h * 0.335f
    val loungeW = w - loungeLeft - 3.dp.toPx()
    val loungeH = h * 0.32f

    drawRoundRect(
        color = Color(0xFF142138),
        topLeft = Offset(loungeLeft, loungeTop),
        size = Size(loungeW, loungeH),
        cornerRadius = CornerRadius(9.dp.toPx(), 9.dp.toPx())
    )
    drawRoundRect(
        color = Color(0xFF38BDF8).copy(alpha = 0.36f),
        topLeft = Offset(loungeLeft, loungeTop),
        size = Size(loungeW, loungeH),
        cornerRadius = CornerRadius(9.dp.toPx(), 9.dp.toPx()),
        style = Stroke(width = 1.1.dp.toPx())
    )
    drawRect(
        color = Color(0xFF0F1B30),
        topLeft = Offset(loungeLeft - 3.dp.toPx(), loungeTop + loungeH * 0.36f),
        size = Size(6.dp.toPx(), loungeH * 0.28f)
    )
    drawLine(
        color = pathStroke,
        start = Offset(rightHallX, loungeTop + loungeH * 0.5f),
        end = Offset(loungeLeft, loungeTop + loungeH * 0.5f),
        strokeWidth = 1.dp.toPx(),
        pathEffect = dashEffect
    )

    val sofaX = loungeLeft + 9.dp.toPx()
    val sofaY = loungeTop + 18.dp.toPx()
    val sofaW = 18.dp.toPx()
    val sofaH = loungeH - 28.dp.toPx()
    drawRoundRect(
        color = Color(0xFF1E293B),
        topLeft = Offset(sofaX, sofaY),
        size = Size(6.dp.toPx(), sofaH),
        cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx())
    )
    drawRoundRect(
        color = Color(0xFF334155),
        topLeft = Offset(sofaX + 5.dp.toPx(), sofaY + 2.dp.toPx()),
        size = Size(sofaW - 5.dp.toPx(), sofaH - 4.dp.toPx()),
        cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
    )
    drawRoundRect(
        color = Color(0xFFF43F5E),
        topLeft = Offset(sofaX + 6.dp.toPx(), sofaY + 6.dp.toPx()),
        size = Size(6.dp.toPx(), 8.dp.toPx()),
        cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
    )
    drawRoundRect(
        color = Color(0xFFFBBF24),
        topLeft = Offset(sofaX + 6.dp.toPx(), sofaY + sofaH - 14.dp.toPx()),
        size = Size(6.dp.toPx(), 8.dp.toPx()),
        cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
    )

    val tvScreenW = 16.dp.toPx()
    val tvTop = loungeTop + 14.dp.toPx()
    val tvH = loungeH - 20.dp.toPx()
    val tvLeft = loungeLeft + loungeW - tvScreenW - 3.dp.toPx()

    drawRect(
        brush = Brush.horizontalGradient(
            colors = listOf(Color.Transparent, Color(0xFF38BDF8).copy(alpha = 0.24f)),
            startX = tvLeft - 20.dp.toPx(),
            endX = tvLeft
        ),
        topLeft = Offset(tvLeft - 20.dp.toPx(), tvTop),
        size = Size(20.dp.toPx(), tvH)
    )
    drawRoundRect(
        color = Color(0xFF090D16),
        topLeft = Offset(tvLeft, tvTop),
        size = Size(tvScreenW, tvH),
        cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx())
    )
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
    for (bar in 0 until 5) {
        val barH = scrW * (0.28f + 0.58f * abs(sin(liveWorkPhase * 6.283f + bar * 1.1f)))
        val by = scrTop + 5.dp.toPx() + bar * ((scrH - 16.dp.toPx()) / 5f)
        drawRect(
            color = if (bar % 2 == 0) Color(0xFF38BDF8) else Color(0xFFFBBF24),
            topLeft = Offset(scrLeft + scrW - barH - 1.dp.toPx(), by),
            size = Size(barH, 3.dp.toPx())
        )
    }

    drawDetailedPineTree(
        centerX = w * 0.555f,
        topY = h * 0.355f,
        bottomY = h * 0.635f,
        garlandPhase = garlandPhase
    )

    agents.forEach { ag ->
        val pos = positions[ag.id] ?: ag.deskChairPos
        val walkPh = walkPhases[ag.id] ?: 0f
        val cx = pos.x * w
        val cy = pos.y * h
        val isSel = ag.id == selectedId
        val st = statuses[ag.id]

        val isAtDesk = hypot(pos.x - ag.deskChairPos.x, pos.y - ag.deskChairPos.y) < 0.025f &&
            st?.targetZone == AgentLocationZone.DESK
        val isWalking = !isAtDesk &&
            (st?.targetZone != AgentLocationZone.DINING || hypot(pos.x - ag.diningSeatPos.x, pos.y - ag.diningSeatPos.y) > 0.025f) &&
            (st?.targetZone != AgentLocationZone.LOUNGE || hypot(pos.x - ag.loungeSeatPos.x, pos.y - ag.loungeSeatPos.y) > 0.025f) &&
            (st?.targetZone != AgentLocationZone.CHIEF_CHECK || hypot(pos.x - 0.125f, pos.y - 0.225f) > 0.025f)

        drawDetailedAgentCharacter(
            cx = cx,
            cy = cy,
            profile = ag,
            status = st,
            isSelected = isSel,
            isWalking = isWalking,
            isTypingAtDesk = isAtDesk,
            walkPhase = walkPh
        )
    }

    for (s in 0 until 24) {
        val seedX = ((s * 73 + 19) % 100) / 100f
        val drift = sin(snowPhase * 6.28318f + s) * 6.dp.toPx()
        val syFrac = ((s * 37) / 100f + snowPhase) % 1f
        val sx = seedX * w + drift
        val sy = syFrac * h
        drawCircle(
            color = Color.White.copy(alpha = if (s % 3 == 0) 0.35f else 0.18f),
            radius = if (s % 4 == 0) 1.6.dp.toPx() else 1.1.dp.toPx(),
            center = Offset(sx, sy)
        )
    }
}

private fun DrawScope.drawFestiveWreath(center: Offset, radius: Float) {
    drawCircle(
        color = Color(0xFF059669),
        radius = radius,
        center = center,
        style = Stroke(width = 2.dp.toPx())
    )
    drawCircle(
        color = Color(0xFF10B981),
        radius = radius * 0.85f,
        center = center,
        style = Stroke(width = 0.9.dp.toPx())
    )
    drawCircle(
        color = Color(0xFFF43F5E),
        radius = 1.5.dp.toPx(),
        center = Offset(center.x, center.y - radius)
    )
}

private fun DrawScope.drawWorkstationWithLampAndChair(
    deskLeft: Float,
    deskTop: Float,
    deskWidth: Float,
    deskHeight: Float,
    chairCenter: Offset,
    accentColor: Color,
    progress: Float,
    liveWorkPhase: Float,
    isTopRow: Boolean
) {
    val seatW = 14.dp.toPx()
    val seatH = 10.dp.toPx()
    drawRoundRect(
        color = Color(0xFF1E293B),
        topLeft = Offset(chairCenter.x - seatW * 0.5f, chairCenter.y - seatH * 0.35f),
        size = Size(seatW, seatH),
        cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx())
    )
    drawRoundRect(
        color = Color(0xFF475569),
        topLeft = Offset(chairCenter.x - seatW * 0.5f - 2.dp.toPx(), chairCenter.y - seatH * 0.25f),
        size = Size(2.2.dp.toPx(), seatH * 0.8f),
        cornerRadius = CornerRadius(1.dp.toPx(), 1.dp.toPx())
    )
    drawRoundRect(
        color = Color(0xFF475569),
        topLeft = Offset(chairCenter.x + seatW * 0.5f - 0.2.dp.toPx(), chairCenter.y - seatH * 0.25f),
        size = Size(2.2.dp.toPx(), seatH * 0.8f),
        cornerRadius = CornerRadius(1.dp.toPx(), 1.dp.toPx())
    )
    val backrestY = if (isTopRow) chairCenter.y + seatH * 0.45f else chairCenter.y - seatH * 0.72f
    drawRoundRect(
        color = Color(0xFF334155),
        topLeft = Offset(chairCenter.x - seatW * 0.52f, backrestY),
        size = Size(seatW * 1.04f, 4.2.dp.toPx()),
        cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
    )
    drawRoundRect(
        color = accentColor.copy(alpha = 0.65f),
        topLeft = Offset(chairCenter.x - seatW * 0.52f, backrestY),
        size = Size(seatW * 1.04f, 4.2.dp.toPx()),
        cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx()),
        style = Stroke(width = 0.8.dp.toPx())
    )

    drawRoundRect(
        color = Color(0xFF2B3F63),
        topLeft = Offset(deskLeft, deskTop),
        size = Size(deskWidth, deskHeight),
        cornerRadius = CornerRadius(3.5.dp.toPx(), 3.5.dp.toPx())
    )
    drawRoundRect(
        color = Color.White.copy(alpha = 0.14f),
        topLeft = Offset(deskLeft, deskTop),
        size = Size(deskWidth, deskHeight),
        cornerRadius = CornerRadius(3.5.dp.toPx(), 3.5.dp.toPx()),
        style = Stroke(width = 0.7.dp.toPx())
    )

    val lampBaseX = deskLeft + 5.5.dp.toPx()
    val lampBaseY = deskTop + deskHeight * 0.52f
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color(0xFFFDE047).copy(alpha = 0.32f), Color.Transparent),
            center = Offset(lampBaseX + 5.dp.toPx(), lampBaseY),
            radius = 12.dp.toPx()
        ),
        radius = 12.dp.toPx(),
        center = Offset(lampBaseX + 5.dp.toPx(), lampBaseY)
    )
    drawCircle(
        color = Color(0xFF94A3B8),
        radius = 2.4.dp.toPx(),
        center = Offset(lampBaseX, lampBaseY + 2.dp.toPx())
    )
    drawLine(
        color = Color(0xFFCBD5E1),
        start = Offset(lampBaseX, lampBaseY + 2.dp.toPx()),
        end = Offset(lampBaseX + 1.5.dp.toPx(), lampBaseY - 5.5.dp.toPx()),
        strokeWidth = 1.2.dp.toPx(),
        cap = StrokeCap.Round
    )
    drawRoundRect(
        color = Color(0xFFF59E0B),
        topLeft = Offset(lampBaseX - 0.5.dp.toPx(), lampBaseY - 7.5.dp.toPx()),
        size = Size(5.5.dp.toPx(), 3.2.dp.toPx()),
        cornerRadius = CornerRadius(1.5.dp.toPx(), 1.5.dp.toPx())
    )
    drawCircle(
        color = Color(0xFFFEF08A),
        radius = 1.4.dp.toPx(),
        center = Offset(lampBaseX + 3.5.dp.toPx(), lampBaseY - 4.3.dp.toPx())
    )

    val monW = deskWidth * 0.52f
    val monH = 12.dp.toPx()
    val monLeft = deskLeft + (deskWidth - monW) * 0.58f
    val monTop = if (isTopRow) deskTop - 4.dp.toPx() else deskTop - 1.dp.toPx()

    drawRoundRect(
        color = Color(0xFF070D19),
        topLeft = Offset(monLeft, monTop),
        size = Size(monW, monH),
        cornerRadius = CornerRadius(2.5.dp.toPx(), 2.5.dp.toPx())
    )
    val scrX = monLeft + 1.5.dp.toPx()
    val scrY = monTop + 1.5.dp.toPx()
    val scrW = monW - 3.dp.toPx()
    val scrH = monH - 3.dp.toPx()
    drawRect(
        color = Color(0xFF0E223D),
        topLeft = Offset(scrX, scrY),
        size = Size(scrW, scrH)
    )

    for (ln in 0 until 3) {
        val animFrac = ((liveWorkPhase + ln * 0.33f) % 1f)
        val lineW = scrW * (0.35f + 0.55f * abs(sin(animFrac * PI.toFloat())))
        val ly = scrY + 1.8.dp.toPx() + ln * 2.2.dp.toPx()
        drawLine(
            color = if (ln == 0) accentColor else Color(0xFF93C5FD).copy(alpha = 0.85f),
            start = Offset(scrX + 1.5.dp.toPx(), ly),
            end = Offset(scrX + 1.5.dp.toPx() + lineW, ly),
            strokeWidth = 1.1.dp.toPx(),
            cap = StrokeCap.Round
        )
    }
    val barY = scrY + scrH - 1.8.dp.toPx()
    drawLine(
        color = Color.White.copy(alpha = 0.18f),
        start = Offset(scrX + 1.5.dp.toPx(), barY),
        end = Offset(scrX + scrW - 1.5.dp.toPx(), barY),
        strokeWidth = 1.3.dp.toPx()
    )
    drawLine(
        color = Color(0xFF10B981),
        start = Offset(scrX + 1.5.dp.toPx(), barY),
        end = Offset(scrX + 1.5.dp.toPx() + (scrW - 3.dp.toPx()) * progress.coerceIn(0.1f, 1f), barY),
        strokeWidth = 1.3.dp.toPx()
    )

    val kbY = if (isTopRow) deskTop + deskHeight - 4.2.dp.toPx() else deskTop + 1.4.dp.toPx()
    drawRoundRect(
        color = Color(0xFF1E293B),
        topLeft = Offset(monLeft + 3.dp.toPx(), kbY),
        size = Size(monW - 6.dp.toPx(), 2.8.dp.toPx()),
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

    drawOval(
        color = Color.Black.copy(alpha = 0.45f),
        topLeft = Offset(centerX - 22.dp.toPx(), bottomY - 3.dp.toPx()),
        size = Size(44.dp.toPx(), 8.dp.toPx())
    )

    drawRect(
        color = Color(0xFF78350F),
        topLeft = Offset(centerX - 3.2.dp.toPx(), bottomY - 7.dp.toPx()),
        size = Size(6.4.dp.toPx(), 8.dp.toPx())
    )
    drawRoundRect(
        color = Color(0xFFDC2626),
        topLeft = Offset(centerX - 5.5.dp.toPx(), bottomY - 3.dp.toPx()),
        size = Size(11.dp.toPx(), 5.5.dp.toPx()),
        cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
    )

    val tiers = listOf(
        Triple(topY, topY + totalH * 0.34f, 10.dp.toPx()),
        Triple(topY + totalH * 0.18f, topY + totalH * 0.56f, 15.dp.toPx()),
        Triple(topY + totalH * 0.38f, topY + totalH * 0.77f, 19.5.dp.toPx()),
        Triple(topY + totalH * 0.58f, bottomY - 5.dp.toPx(), 24.dp.toPx())
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

        val needleCount = 10 + idx * 4
        for (n in 0..needleCount) {
            val frac = n.toFloat() / needleCount.toFloat()
            val ny = ty0 + (ty1 - ty0) * frac
            val spanX = halfW * frac
            drawLine(
                color = if (n % 2 == 0) Color(0xFF34D399) else Color(0xFF047857),
                start = Offset(centerX - spanX + 1.5.dp.toPx(), ny),
                end = Offset(centerX - spanX - 2.5.dp.toPx(), ny + 2.dp.toPx()),
                strokeWidth = 0.9.dp.toPx(),
                cap = StrokeCap.Round
            )
            drawLine(
                color = if (n % 2 == 0) Color(0xFF34D399) else Color(0xFF047857),
                start = Offset(centerX + spanX - 1.5.dp.toPx(), ny),
                end = Offset(centerX + spanX + 2.5.dp.toPx(), ny + 2.dp.toPx()),
                strokeWidth = 0.9.dp.toPx(),
                cap = StrokeCap.Round
            )
        }
    }

    val baubles = listOf(
        Triple(Offset(-3.5.dp.toPx(), totalH * 0.22f), Color(0xFFF43F5E), 2.3.dp.toPx()),
        Triple(Offset(4.5.dp.toPx(), totalH * 0.30f), Color(0xFF38BDF8), 2.3.dp.toPx()),
        Triple(Offset(-8.dp.toPx(), totalH * 0.44f), Color(0xFFFBBF24), 2.6.dp.toPx()),
        Triple(Offset(7.5.dp.toPx(), totalH * 0.50f), Color(0xFFA78BFA), 2.6.dp.toPx()),
        Triple(Offset(-12.dp.toPx(), totalH * 0.72f), Color(0xFFF43F5E), 2.8.dp.toPx()),
        Triple(Offset(11.dp.toPx(), totalH * 0.76f), Color(0xFFFBBF24), 2.8.dp.toPx())
    )
    baubles.forEachIndexed { idx, (offset, col, rad) ->
        val bCenter = Offset(centerX + offset.x, topY + offset.y)
        val glow = 0.65f + 0.35f * abs(sin(garlandPhase + idx))
        drawCircle(color = col.copy(alpha = glow), radius = rad, center = bCenter)
        drawCircle(
            color = Color.White.copy(alpha = 0.85f),
            radius = rad * 0.34f,
            center = Offset(bCenter.x - rad * 0.32f, bCenter.y - rad * 0.32f)
        )
    }

    val starCenter = Offset(centerX, topY - 2.dp.toPx())
    drawCircle(color = Color(0xFFFBBF24).copy(alpha = 0.28f), radius = 7.dp.toPx(), center = starCenter)
    drawCircle(color = Color(0xFFFDE047), radius = 3.8.dp.toPx(), center = starCenter)
}

private fun DrawScope.drawDetailedAgentCharacter(
    cx: Float,
    cy: Float,
    profile: OfficeAgentProfile,
    status: AgentLiveStatus?,
    isSelected: Boolean,
    isWalking: Boolean,
    isTypingAtDesk: Boolean,
    walkPhase: Float
) {
    if (isSelected) {
        drawCircle(
            color = profile.accentColor.copy(alpha = 0.22f),
            radius = 16.dp.toPx(),
            center = Offset(cx, cy + 2.dp.toPx())
        )
        drawCircle(
            color = profile.accentColor,
            radius = 16.dp.toPx(),
            center = Offset(cx, cy + 2.dp.toPx()),
            style = Stroke(
                width = 1.4.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 4f), 0f)
            )
        )
    }

    drawOval(
        color = Color.Black.copy(alpha = 0.45f),
        topLeft = Offset(cx - 8.dp.toPx(), cy + 12.dp.toPx()),
        size = Size(16.dp.toPx(), 4.5.dp.toPx())
    )

    val radAngle = walkPhase * 6.28318f
    val stride = if (isWalking) sin(radAngle) * 2.8.dp.toPx() else 0f
    val bodyBob = if (isWalking) abs(sin(radAngle)) * 1.1.dp.toPx() else 0f
    val typeBounce = if (isTypingAtDesk) sin(radAngle * 2f) * 1.1.dp.toPx() else 0f
    val yBody = cy - bodyBob

    drawLine(
        color = Color(0xFF1E293B),
        start = Offset(cx - 2.6.dp.toPx(), yBody + 6.dp.toPx()),
        end = Offset(cx - 3.dp.toPx(), cy + 12.dp.toPx() + stride),
        strokeWidth = 2.6.dp.toPx(),
        cap = StrokeCap.Round
    )
    drawLine(
        color = Color(0xFF1E293B),
        start = Offset(cx + 2.6.dp.toPx(), yBody + 6.dp.toPx()),
        end = Offset(cx + 3.dp.toPx(), cy + 12.dp.toPx() - stride),
        strokeWidth = 2.6.dp.toPx(),
        cap = StrokeCap.Round
    )
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

    drawRoundRect(
        color = profile.accentColor,
        topLeft = Offset(cx - 6.2.dp.toPx(), yBody - 1.5.dp.toPx()),
        size = Size(12.4.dp.toPx(), 9.dp.toPx()),
        cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
    )
    val collar = Path().apply {
        moveTo(cx - 2.2.dp.toPx(), yBody - 1.5.dp.toPx())
        lineTo(cx, yBody + 2.dp.toPx())
        lineTo(cx + 2.2.dp.toPx(), yBody - 1.5.dp.toPx())
        close()
    }
    drawPath(path = collar, color = Color.White.copy(alpha = 0.9f))

    val leftHandY = if (isTypingAtDesk) {
        (if (profile.isTopRow) yBody - 3.dp.toPx() else yBody + 4.dp.toPx()) + typeBounce
    } else {
        yBody + 4.dp.toPx() - stride * 0.7f
    }
    val rightHandY = if (isTypingAtDesk) {
        (if (profile.isTopRow) yBody - 3.dp.toPx() else yBody + 4.dp.toPx()) - typeBounce
    } else {
        yBody + 4.dp.toPx() + stride * 0.7f
    }
    drawLine(
        color = profile.accentColor,
        start = Offset(cx - 6.dp.toPx(), yBody + 0.5.dp.toPx()),
        end = Offset(cx - 8.dp.toPx(), leftHandY),
        strokeWidth = 2.2.dp.toPx(),
        cap = StrokeCap.Round
    )
    drawLine(
        color = profile.accentColor,
        start = Offset(cx + 6.dp.toPx(), yBody + 0.5.dp.toPx()),
        end = Offset(cx + 8.dp.toPx(), rightHandY),
        strokeWidth = 2.2.dp.toPx(),
        cap = StrokeCap.Round
    )
    drawCircle(color = Color(0xFFFDE68A), radius = 1.4.dp.toPx(), center = Offset(cx - 8.dp.toPx(), leftHandY))
    drawCircle(color = Color(0xFFFDE68A), radius = 1.4.dp.toPx(), center = Offset(cx + 8.dp.toPx(), rightHandY))

    drawRect(
        color = Color(0xFFFCD34D),
        topLeft = Offset(cx - 1.5.dp.toPx(), yBody - 3.dp.toPx()),
        size = Size(3.dp.toPx(), 2.dp.toPx())
    )
    val headCenter = Offset(cx, yBody - 7.dp.toPx())
    if (profile.isFemale) {
        drawRoundRect(
            color = profile.hairColor,
            topLeft = Offset(cx - 6.dp.toPx(), yBody - 11.5.dp.toPx()),
            size = Size(12.dp.toPx(), 9.5.dp.toPx()),
            cornerRadius = CornerRadius(5.dp.toPx(), 5.dp.toPx())
        )
    }
    drawCircle(
        color = Color(0xFFFDE68A),
        radius = 4.8.dp.toPx(),
        center = headCenter
    )
    drawArc(
        color = profile.hairColor,
        startAngle = 185f,
        sweepAngle = 170f,
        useCenter = true,
        topLeft = Offset(cx - 4.9.dp.toPx(), yBody - 12.dp.toPx()),
        size = Size(9.8.dp.toPx(), 6.8.dp.toPx())
    )
    drawCircle(color = Color(0xFF0F172A), radius = 0.9.dp.toPx(), center = Offset(cx - 1.7.dp.toPx(), yBody - 6.8.dp.toPx()))
    drawCircle(color = Color(0xFF0F172A), radius = 0.9.dp.toPx(), center = Offset(cx + 1.7.dp.toPx(), yBody - 6.8.dp.toPx()))
    drawArc(
        color = Color(0xFF9F1239),
        startAngle = 20f,
        sweepAngle = 140f,
        useCenter = false,
        topLeft = Offset(cx - 1.6.dp.toPx(), yBody - 6.2.dp.toPx()),
        size = Size(3.2.dp.toPx(), 2.2.dp.toPx()),
        style = Stroke(width = 0.8.dp.toPx())
    )
    if (profile.hasGlasses) {
        drawCircle(
            color = Color(0xFF38BDF8),
            radius = 1.6.dp.toPx(),
            center = Offset(cx - 1.7.dp.toPx(), yBody - 6.8.dp.toPx()),
            style = Stroke(width = 0.6.dp.toPx())
        )
        drawCircle(
            color = Color(0xFF38BDF8),
            radius = 1.6.dp.toPx(),
            center = Offset(cx + 1.7.dp.toPx(), yBody - 6.8.dp.toPx()),
            style = Stroke(width = 0.6.dp.toPx())
        )
    }
    if (profile.id == 0) {
        drawCircle(
            color = Color(0xFFFBBF24),
            radius = 1.8.dp.toPx(),
            center = Offset(cx, yBody - 12.6.dp.toPx())
        )
    }

    val statusDotColor = when (status?.targetZone) {
        AgentLocationZone.CHIEF_CHECK -> Color(0xFF38BDF8)
        AgentLocationZone.DINING, AgentLocationZone.LOUNGE -> Color(0xFFFBBF24)
        else -> Color(0xFF10B981)
    }
    drawCircle(
        color = statusDotColor,
        radius = 2.4.dp.toPx(),
        center = Offset(cx + 7.5.dp.toPx(), yBody - 9.dp.toPx())
    )

    if ((status?.reprimandsCount ?: 0) > 0) {
        drawCircle(
            color = Color(0xFFF43F5E),
            radius = 2.4.dp.toPx(),
            center = Offset(cx - 7.5.dp.toPx(), yBody - 9.dp.toPx())
        )
    }
}
