package com.nexusai.techfeed

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Newspaper
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import org.jsoup.Jsoup
import org.jsoup.parser.Parser
import java.net.URI
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.concurrent.TimeUnit

data class NewsItem(
    val id: String,
    val title: String,
    val sourceLabel: String,
    val brand: String,
    val platform: String,
    val summary: String,
    val url: String,
    val timestamp: Long,
    val timeLabel: String
) {
    val isFresh24h: Boolean
        get() = (System.currentTimeMillis() - timestamp) <= 86_400_000L

    val isVeryFresh6h: Boolean
        get() = (System.currentTimeMillis() - timestamp) <= 21_600_000L
}

data class ThemePalette(
    val id: String,
    val name: String,
    val primaryLight: Color,
    val primaryDark: Color,
    val secondaryLight: Color,
    val secondaryDark: Color,
    val bgLight: Color,
    val bgDark: Color,
    val surfaceDark: Color
)

data class UserAppSettings(
    val paletteId: String = "emerald",
    val darkMode: String = "system",
    val cardStyle: String = "outlined",
    val cornerRadius: Int = 18,
    val fontScale: Float = 1.0f,
    val showSummary: Boolean = true
)

val AppPalettes = listOf(
    ThemePalette(
        id = "emerald",
        name = "Изумруд",
        primaryLight = Color(0xFF059669),
        primaryDark = Color(0xFF10B981),
        secondaryLight = Color(0xFFD1FAE5),
        secondaryDark = Color(0xFF064E3B),
        bgLight = Color(0xFFF0FDF4),
        bgDark = Color(0xFF06110E),
        surfaceDark = Color(0xFF0D1F1A)
    ),
    ThemePalette(
        id = "mint_neon",
        name = "Мятный неон",
        primaryLight = Color(0xFF0D9488),
        primaryDark = Color(0xFF2DD4BF),
        secondaryLight = Color(0xFFCCFBF1),
        secondaryDark = Color(0xFF115E59),
        bgLight = Color(0xFFF0FDFA),
        bgDark = Color(0xFF041211),
        surfaceDark = Color(0xFF0B2220)
    ),
    ThemePalette(
        id = "cyber_blue",
        name = "Кибер-синий",
        primaryLight = Color(0xFF2563EB),
        primaryDark = Color(0xFF60A5FA),
        secondaryLight = Color(0xFFDBEAFE),
        secondaryDark = Color(0xFF1E3A8A),
        bgLight = Color(0xFFF8FAFC),
        bgDark = Color(0xFF070B14),
        surfaceDark = Color(0xFF0F172A)
    ),
    ThemePalette(
        id = "violet_ai",
        name = "Фиолетовый AI",
        primaryLight = Color(0xFF7C3AED),
        primaryDark = Color(0xFFA78BFA),
        secondaryLight = Color(0xFFEDE9FE),
        secondaryDark = Color(0xFF4C1D95),
        bgLight = Color(0xFFFAF5FF),
        bgDark = Color(0xFF0D0817),
        surfaceDark = Color(0xFF19112B)
    ),
    ThemePalette(
        id = "amber_sunset",
        name = "Янтарный",
        primaryLight = Color(0xFFD97706),
        primaryDark = Color(0xFFFBBF24),
        secondaryLight = Color(0xFFFEF3C7),
        secondaryDark = Color(0xFF78350F),
        bgLight = Color(0xFFFFFBEB),
        bgDark = Color(0xFF140E05),
        surfaceDark = Color(0xFF24190A)
    ),
    ThemePalette(
        id = "ruby_pulse",
        name = "Рубиновый",
        primaryLight = Color(0xFFE11D48),
        primaryDark = Color(0xFFFB7185),
        secondaryLight = Color(0xFFFFE4E6),
        secondaryDark = Color(0xFF881337),
        bgLight = Color(0xFFFFF1F2),
        bgDark = Color(0xFF14070B),
        surfaceDark = Color(0xFF240E15)
    )
)

data class AiAgentNode(
    val id: Int,
    val name: String,
    val role: String,
    val roomTitle: String,
    val currentTask: String,
    val accentColor: Color,
    val rx: Float, // относительная координата X (0..1)
    val ry: Float  // относительная координата Y (0..1)
)

data class TgChannel(val name: String, val defaultBrand: String, val username: String)
data class RssSite(val name: String, val defaultBrand: String, val platform: String, val url: String)
data class WebPageSource(val name: String, val defaultBrand: String, val url: String, val linkRegex: String)
data class BrandSearchQuery(val brand: String, val query: String)

object NewsRepository {
    private val client = OkHttpClient.Builder()
        .connectTimeout(12, TimeUnit.SECONDS)
        .readTimeout(12, TimeUnit.SECONDS)
        .followRedirects(true)
        .build()

    private val tgChannels = listOf(
        TgChannel("Rozetked", "Общее", "rozetked"),
        TgChannel("Wylsacom Red", "Общее", "Wylsared"),
        TgChannel("Mobiltelefon.ru", "Общее", "mobiltelefonru"),
        TgChannel("Код Дурова", "Общее", "durovscode"),
        TgChannel("Лысый из МТ", "Общее", "lysyi_iz_mt"),
        TgChannel("Xiaomishka (инсайды)", "Xiaomi", "xiaomishka"),
        TgChannel("Xiaomi Россия", "Xiaomi", "xiaomirussia"),
        TgChannel("Xiaomi Community RU", "Xiaomi", "miui_ru"),
        TgChannel("Samsung Россия", "Samsung", "Samsung_Russia"),
        TgChannel("Huawei Mobile RU", "Huawei", "huaweimobilerus"),
        TgChannel("HONOR Россия", "HONOR", "honor_russia"),
        TgChannel("OPPO Россия", "OPPO", "opporussia"),
        TgChannel("vivo Россия", "vivo", "vivorussia"),
        TgChannel("realme Россия", "realme", "realme_russia")
    )

    private val rssSites = listOf(
        RssSite("4PDA", "Общее", "СМИ и блоги", "https://4pda.to/feed/"),
        RssSite("Rozetked", "Общее", "СМИ и блоги", "https://rozetked.me/rss"),
        RssSite("iXBT.com", "Общее", "СМИ и блоги", "https://www.ixbt.com/export/news.rss"),
        RssSite("Trashbox.ru", "Общее", "СМИ и блоги", "https://trashbox.ru/rss/all/"),
        RssSite("3DNews", "Общее", "СМИ и блоги", "https://3dnews.ru/news/rss/"),
        RssSite("Ferra.ru", "Общее", "СМИ и блоги", "https://www.ferra.ru/exports/rss.xml"),
        RssSite("Mobile-Review", "Общее", "СМИ и блоги", "https://mobile-review.com/all/feed/"),
        RssSite("Wylsacom Media", "Общее", "СМИ и блоги", "https://wylsa.com/feed/"),
        RssSite("Хабр — IT и гаджеты", "Общее", "СМИ и блоги", "https://habr.com/ru/rss/flows/develop/articles/?fl=ru"),
        RssSite("Лайфхакер — Технологии", "Общее", "СМИ и блоги", "https://lifehacker.ru/topics/technology/feed/"),
        RssSite("Kod.ru", "Общее", "СМИ и блоги", "https://kod.ru/rss/"),
        RssSite("Samsung Newsroom RU", "Samsung", "Оф. сайт", "https://news.samsung.com/ru/feed"),
        RssSite("Xiaomi Global Blog", "Xiaomi", "Оф. сайт", "https://blog.mi.com/en/feed/"),
        RssSite("Apple Newsroom", "Apple", "Оф. сайт", "https://www.apple.com/newsroom/rss-feed.rss")
    )

    private val brandNewsFeeds = listOf(
        BrandSearchQuery("Xiaomi", "Xiaomi OR HyperOS OR Redmi OR POCO"),
        BrandSearchQuery("Samsung", "Samsung Galaxy OR One UI"),
        BrandSearchQuery("Apple", "Apple iPhone OR iOS"),
        BrandSearchQuery("Google", "Google Pixel OR Android"),
        BrandSearchQuery("Huawei", "Huawei OR HarmonyOS"),
        BrandSearchQuery("HONOR", "HONOR смартфон OR MagicOS"),
        BrandSearchQuery("OPPO", "OPPO смартфон OR ColorOS"),
        BrandSearchQuery("OnePlus", "OnePlus смартфон OR OxygenOS"),
        BrandSearchQuery("vivo", "vivo смартфон OR iQOO"),
        BrandSearchQuery("realme", "realme смартфон"),
        BrandSearchQuery("Nothing", "Nothing Phone OR CMF"),
        BrandSearchQuery("NVIDIA", "NVIDIA GeForce OR RTX"),
        BrandSearchQuery("AMD", "AMD Ryzen OR Radeon")
    )

    private val officialWebPages = listOf(
        WebPageSource("Xiaomi Россия (mi.com/ru)", "Xiaomi", "https://www.mi.com/ru/discover/", """/ru/discover/article\?id=\d+"""),
        WebPageSource("Xiaomi Community (c.mi.com)", "Xiaomi", "https://new.c.mi.com/ru/", """/ru/post/\d+"""),
        WebPageSource("HONOR Россия", "HONOR", "https://www.honor.com/ru/news/", """/ru/news/[^"#?]+/"""),
        WebPageSource("Huawei Россия", "Huawei", "https://consumer.huawei.com/ru/community/", """/ru/community/details/[^"#?]+""")
    )

    private fun detectBrand(text: String, fallback: String): String {
        if (fallback != "Общее") return fallback
        val lower = text.lowercase()
        return when {
            listOf("xiaomi", "hyperos", "redmi", "poco", "miui", "сяоми").any { it in lower } -> "Xiaomi"
            listOf("samsung", "galaxy", "one ui", "самсунг").any { it in lower } -> "Samsung"
            listOf("apple", "iphone", "ios ", "ipad", "macbook", "airpods").any { it in lower } -> "Apple"
            listOf("google", "pixel", "android ", "gemini").any { it in lower } -> "Google"
            listOf("huawei", "harmonyos", "mate ", "pura ", "watch fit").any { it in lower } -> "Huawei"
            listOf("honor", "magic os", "magicos").any { it in lower } -> "HONOR"
            listOf("oppo", "coloros", "find x", "reno").any { it in lower } -> "OPPO"
            listOf("oneplus", "oxygenos").any { it in lower } -> "OnePlus"
            listOf("vivo", "iqoo", "originos").any { it in lower } -> "vivo"
            listOf("realme", "реалми").any { it in lower } -> "realme"
            listOf("nothing", "cmf").any { it in lower } -> "Nothing"
            listOf("nvidia", "geforce", "rtx").any { it in lower } -> "NVIDIA"
            listOf("amd", "ryzen", "radeon").any { it in lower } -> "AMD"
            else -> "Общее"
        }
    }

    private fun fetchUrl(url: String): String? {
        return try {
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "Mozilla/5.0 (Linux; Android 15; Xiaomi 17T) AppleWebKit/537.36 Chrome/130.0.0.0 Mobile Safari/537.36")
                .header("Accept-Language", "ru-RU,ru;q=0.9,en;q=0.5")
                .header("Cache-Control", "no-cache")
                .build()
            client.newCall(request).execute().use { resp ->
                if (resp.isSuccessful) resp.body?.string() else null
            }
        } catch (_: Exception) {
            null
        }
    }

    private fun formatTimestamp(millis: Long): String {
        val zone = ZoneId.systemDefault()
        val postTime = Instant.ofEpochMilli(millis).atZone(zone)
        val now = ZonedDateTime.now(zone)
        return when {
            postTime.toLocalDate() == now.toLocalDate() ->
                "Сегодня, ${postTime.format(DateTimeFormatter.ofPattern("HH:mm"))}"
            postTime.toLocalDate() == now.minusDays(1).toLocalDate() ->
                "Вчера, ${postTime.format(DateTimeFormatter.ofPattern("HH:mm"))}"
            else ->
                postTime.format(DateTimeFormatter.ofPattern("dd.MM в HH:mm"))
        }
    }

    private fun parseRssDate(dateStr: String): Long {
        if (dateStr.isBlank()) return System.currentTimeMillis()
        try {
            return Instant.parse(dateStr).toEpochMilli()
        } catch (_: Exception) {}
        val patterns = listOf(
            "EEE, dd MMM yyyy HH:mm:ss Z",
            "EEE, dd MMM yyyy HH:mm:ss z",
            "yyyy-MM-dd'T'HH:mm:ssXXX",
            "yyyy-MM-dd'T'HH:mm:ss'Z'",
            "yyyy-MM-dd HH:mm:ss"
        )
        for (p in patterns) {
            try {
                val sdf = SimpleDateFormat(p, Locale.ENGLISH)
                sdf.parse(dateStr.trim())?.let { return it.time }
            } catch (_: Exception) {}
        }
        return System.currentTimeMillis()
    }

    private fun makeSummary(raw: String, maxLen: Int = 220): String {
        val clean = Jsoup.parse(raw).text().replace(Regex("\\s+"), " ").trim()
        if (clean.isEmpty()) return "Нажмите «Читать полностью», чтобы открыть статью в источнике."
        return if (clean.length <= maxLen) clean else clean.take(maxLen).substringBeforeLast(" ") + "..."
    }

    private fun fetchTelegram(ch: TgChannel): List<NewsItem> {
        val html = fetchUrl("https://t.me/s/${ch.username}?t=${System.currentTimeMillis()}") ?: return emptyList()
        if (!html.contains("subscribers") && !html.contains("подписчик")) return emptyList()

        val doc = Jsoup.parse(html)
        val wraps = doc.select("div.tgme_widget_message_wrap")
        val list = mutableListOf<NewsItem>()

        for (wrap in wraps) {
            val msgDiv = wrap.selectFirst("div.tgme_widget_message") ?: continue
            val postId = msgDiv.attr("data-post")
            if (postId.isBlank()) continue
            val timeAttr = wrap.selectFirst("time[datetime]")?.attr("datetime") ?: continue
            val textEl = wrap.selectFirst("div.tgme_widget_message_text") ?: continue
            val text = textEl.text().replace(Regex("\\s+"), " ").trim()
            if (text.length < 20) continue

            val ts = try {
                Instant.parse(timeAttr).toEpochMilli()
            } catch (_: Exception) {
                System.currentTimeMillis()
            }
            val title = if (text.length > 90) text.take(90).substringBeforeLast(" ") + "..." else text
            val detected = detectBrand(text, ch.defaultBrand)

            list.add(
                NewsItem(
                    id = "tg_$postId",
                    title = title,
                    sourceLabel = ch.name,
                    brand = detected,
                    platform = "Telegram",
                    summary = makeSummary(text),
                    url = "https://t.me/$postId",
                    timestamp = ts,
                    timeLabel = formatTimestamp(ts)
                )
            )
        }
        return list.sortedByDescending { it.timestamp }.take(6)
    }

    private fun fetchRss(site: RssSite): List<NewsItem> {
        val xml = fetchUrl(site.url) ?: return emptyList()
        val doc = Jsoup.parse(xml, "", Parser.xmlParser())
        val entries = doc.select("item").ifEmpty { doc.select("entry") }
        val list = mutableListOf<NewsItem>()

        for (entry in entries.take(7)) {
            val title = Jsoup.parse(entry.selectFirst("title")?.text() ?: "").text().trim()
            if (title.isBlank()) continue
            val link = entry.selectFirst("link")?.text()?.trim()?.ifBlank {
                entry.selectFirst("link")?.attr("href") ?: ""
            } ?: continue
            if (link.isBlank()) continue

            val desc = entry.selectFirst("description")?.text()
                ?: entry.selectFirst("summary")?.text()
                ?: entry.selectFirst("content")?.text() ?: ""
            val dateStr = entry.selectFirst("pubDate")?.text()
                ?: entry.selectFirst("updated")?.text()
                ?: entry.selectFirst("published")?.text() ?: ""
            val ts = parseRssDate(dateStr)
            val summary = makeSummary(desc)
            val detected = detectBrand("$title $summary", site.defaultBrand)

            list.add(
                NewsItem(
                    id = "rss_${link.hashCode()}",
                    title = title,
                    sourceLabel = site.name,
                    brand = detected,
                    platform = site.platform,
                    summary = summary,
                    url = link,
                    timestamp = ts,
                    timeLabel = formatTimestamp(ts)
                )
            )
        }
        return list
    }

    private fun fetchBrandGoogleNews(bq: BrandSearchQuery): List<NewsItem> {
        val encoded = URLEncoder.encode(bq.query, "UTF-8")
        val url = "https://news.google.com/rss/search?q=$encoded&hl=ru&gl=RU&ceid=RU:ru"
        val xml = fetchUrl(url) ?: return emptyList()
        val doc = Jsoup.parse(xml, "", Parser.xmlParser())
        val items = doc.select("item")
        val list = mutableListOf<NewsItem>()

        for (item in items.take(5)) {
            val rawTitle = Jsoup.parse(item.selectFirst("title")?.text() ?: "").text().trim()
            if (rawTitle.isBlank()) continue
            val sourceName = item.selectFirst("source")?.text()?.trim()?.ifBlank { "Новости СМИ" } ?: "Новости СМИ"
            val cleanTitle = rawTitle.substringBeforeLast(" - ").trim().ifBlank { rawTitle }
            val link = item.selectFirst("link")?.text()?.trim() ?: continue
            val dateStr = item.selectFirst("pubDate")?.text() ?: ""
            val ts = parseRssDate(dateStr)

            list.add(
                NewsItem(
                    id = "gn_${bq.brand}_${link.hashCode()}",
                    title = cleanTitle,
                    sourceLabel = sourceName,
                    brand = bq.brand,
                    platform = "СМИ и блоги",
                    summary = "Материал издания «$sourceName» по теме ${bq.brand}. Нажмите «Читать полностью» для перехода к статье.",
                    url = link,
                    timestamp = ts,
                    timeLabel = formatTimestamp(ts)
                )
            )
        }
        return list
    }

    private fun fetchWebPage(wp: WebPageSource): List<NewsItem> {
        val html = fetchUrl(wp.url) ?: return emptyList()
        val doc = Jsoup.parse(html, wp.url)
        val regex = Regex(wp.linkRegex)

        return doc.select("a[href]")
            .map { it to it.absUrl("href") }
            .filter { (_, absUrl) -> absUrl.isNotBlank() && absUrl != wp.url && regex.containsMatchIn(absUrl) }
            .distinctBy { (_, absUrl) -> absUrl }
            .take(5)
            .mapIndexed { idx, (a, absUrl) ->
                var title = a.text().replace(Regex("\\s+"), " ").trim()
                if (title.length < 10) {
                    val slug = URI(absUrl).path.trimEnd('/').substringAfterLast('/')
                        .replace(Regex("^\\d+-"), "")
                        .replace("-", " ")
                        .trim()
                    title = if (slug.length > 8) slug.replaceFirstChar { it.uppercase() } else "Публикация на ${wp.name}"
                }
                val ts = System.currentTimeMillis() - (idx * 25 + 15) * 60_000L
                NewsItem(
                    id = "web_${absUrl.hashCode()}",
                    title = title.take(95),
                    sourceLabel = wp.name,
                    brand = detectBrand(title, wp.defaultBrand),
                    platform = "Оф. сайт",
                    summary = "Официальный материал на портале ${wp.name}. Нажмите «Читать полностью» для просмотра.",
                    url = absUrl,
                    timestamp = ts,
                    timeLabel = "На сайте"
                )
            }
    }

    suspend fun loadAllNews(): List<NewsItem> = coroutineScope {
        val jobs = mutableListOf<Deferred<List<NewsItem>>>()
        rssSites.forEach { site -> jobs.add(async(Dispatchers.IO) { fetchRss(site) }) }
        tgChannels.forEach { ch -> jobs.add(async(Dispatchers.IO) { fetchTelegram(ch) }) }
        officialWebPages.forEach { wp -> jobs.add(async(Dispatchers.IO) { fetchWebPage(wp) }) }
        brandNewsFeeds.forEach { bq -> jobs.add(async(Dispatchers.IO) { fetchBrandGoogleNews(bq) }) }

        val all = jobs.awaitAll().flatten()
        val seenUrls = mutableSetOf<String>()
        val seenTitles = mutableSetOf<String>()
        all.filter { item ->
            val normTitle = item.title.lowercase().take(55)
            seenUrls.add(item.url) && seenTitles.add(normTitle)
        }.sortedByDescending { it.timestamp }
    }
}

object LocalCache {
    private const val PREFS = "newspulse_ai_prefs"
    private const val KEY_SAVED = "saved_news"
    private const val KEY_CACHE = "cached_news_ru_v3"
    private const val KEY_SETTINGS = "user_app_settings_v2"

    fun saveList(context: Context, key: String, items: List<NewsItem>) {
        val arr = JSONArray()
        items.forEach { item ->
            val obj = JSONObject().apply {
                put("id", item.id)
                put("title", item.title)
                put("sourceLabel", item.sourceLabel)
                put("brand", item.brand)
                put("platform", item.platform)
                put("summary", item.summary)
                put("url", item.url)
                put("timestamp", item.timestamp)
                put("timeLabel", item.timeLabel)
            }
            arr.put(obj)
        }
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putString(key, arr.toString()).apply()
    }

    fun loadList(context: Context, key: String): List<NewsItem> {
        val raw = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(key, null) ?: return emptyList()
        return try {
            val arr = JSONArray(raw)
            List(arr.length()) { i ->
                val o = arr.getJSONObject(i)
                NewsItem(
                    id = o.getString("id"),
                    title = o.getString("title"),
                    sourceLabel = o.getString("sourceLabel"),
                    brand = o.getString("brand"),
                    platform = o.getString("platform"),
                    summary = o.getString("summary"),
                    url = o.getString("url"),
                    timestamp = o.getLong("timestamp"),
                    timeLabel = o.getString("timeLabel")
                )
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun saveSettings(context: Context, config: UserAppSettings) {
        val obj = JSONObject().apply {
            put("paletteId", config.paletteId)
            put("darkMode", config.darkMode)
            put("cardStyle", config.cardStyle)
            put("cornerRadius", config.cornerRadius)
            put("fontScale", config.fontScale.toDouble())
            put("showSummary", config.showSummary)
        }
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putString(KEY_SETTINGS, obj.toString()).apply()
    }

    fun loadSettings(context: Context): UserAppSettings {
        val raw = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY_SETTINGS, null) ?: return UserAppSettings()
        return try {
            val o = JSONObject(raw)
            UserAppSettings(
                paletteId = o.optString("paletteId", "emerald"),
                darkMode = o.optString("darkMode", "system"),
                cardStyle = o.optString("cardStyle", "outlined"),
                cornerRadius = o.optInt("cornerRadius", 18),
                fontScale = o.optDouble("fontScale", 1.0).toFloat(),
                showSummary = o.optBoolean("showSummary", true)
            )
        } catch (_: Exception) {
            UserAppSettings()
        }
    }

    fun getSaved(context: Context) = loadList(context, KEY_SAVED)
    fun setSaved(context: Context, items: List<NewsItem>) = saveList(context, KEY_SAVED, items)
    fun getCached(context: Context) = loadList(context, KEY_CACHE)
    fun setCached(context: Context, items: List<NewsItem>) = saveList(context, KEY_CACHE, items)
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val context = LocalContext.current
            var appSettings by remember { mutableStateOf(LocalCache.loadSettings(context)) }
            val systemDark = isSystemInDarkTheme()

            val isDark = when (appSettings.darkMode) {
                "light" -> false
                "dark", "amoled" -> true
                else -> systemDark
            }
            val isAmoled = appSettings.darkMode == "amoled"
            val palette = AppPalettes.find { it.id == appSettings.paletteId } ?: AppPalettes.first()

            val primaryColor by animateColorAsState(
                targetValue = if (isDark) palette.primaryDark else palette.primaryLight,
                label = "primary"
            )
            val bgColor by animateColorAsState(
                targetValue = when {
                    isAmoled -> Color(0xFF000000)
                    isDark -> palette.bgDark
                    else -> palette.bgLight
                },
                label = "bg"
            )
            val surfaceColor by animateColorAsState(
                targetValue = when {
                    isAmoled -> Color(0xFF0B100E)
                    isDark -> palette.surfaceDark
                    else -> Color.White
                },
                label = "surface"
            )

            val colors = if (isDark) {
                darkColorScheme(
                    primary = primaryColor,
                    onPrimary = Color(0xFF031711),
                    secondaryContainer = palette.secondaryDark,
                    onSecondaryContainer = Color.White,
                    background = bgColor,
                    surface = surfaceColor,
                    onBackground = Color(0xFFF1F5F9),
                    onSurface = Color(0xFFF1F5F9)
                )
            } else {
                lightColorScheme(
                    primary = primaryColor,
                    onPrimary = Color.White,
                    secondaryContainer = palette.secondaryLight,
                    onSecondaryContainer = Color(0xFF062C22),
                    background = bgColor,
                    surface = surfaceColor,
                    onBackground = Color(0xFF0F172A),
                    onSurface = Color(0xFF0F172A)
                )
            }

            MaterialTheme(colorScheme = colors) {
                NewsPulseAppScreen(
                    settings = appSettings,
                    onSettingsChange = { updated ->
                        appSettings = updated
                        LocalCache.saveSettings(context, updated)
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewsPulseAppScreen(
    settings: UserAppSettings,
    onSettingsChange: (UserAppSettings) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var newsList by remember { mutableStateOf(LocalCache.getCached(context)) }
    var savedList by remember { mutableStateOf(LocalCache.getSaved(context)) }
    var isLoading by remember { mutableStateOf(false) }
    var lastUpdatedText by remember { mutableStateOf(if (newsList.isNotEmpty()) "Из кеша" else "Загрузка...") }

    var selectedTab by remember { mutableIntStateOf(0) }
    var searchQuery by remember { mutableStateOf("") }
    var freshOnly by remember { mutableStateOf(false) }
    var selectedPlatform by remember { mutableStateOf("Все") }
    var selectedBrand by remember { mutableStateOf("Все") }

    val brands = listOf(
        "Все", "Xiaomi", "Samsung", "Apple", "Google", "Huawei",
        "HONOR", "OPPO", "OnePlus", "vivo", "realme", "Nothing",
        "NVIDIA", "AMD", "Общее"
    )
    val platforms = listOf("Все", "СМИ и блоги", "Telegram", "Оф. сайт")

    fun refreshFeed() {
        if (isLoading) return
        isLoading = true
        lastUpdatedText = "Собираем новости по всем брендам..."
        scope.launch {
            val fresh = NewsRepository.loadAllNews()
            if (fresh.isNotEmpty()) {
                newsList = fresh
                LocalCache.setCached(context, fresh)
                val nowStr = ZonedDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm"))
                lastUpdatedText = "Обновлено в $nowStr • ${fresh.size} материалов"
            } else {
                lastUpdatedText = "Не удалось обновить (проверьте сеть)"
            }
            isLoading = false
        }
    }

    LaunchedEffect(Unit) {
        refreshFeed()
    }

    val brandCounts = remember(newsList) {
        brands.associateWith { b ->
            if (b == "Все") newsList.size else newsList.count { it.brand == b }
        }
    }

    val displayedItems = remember(newsList, savedList, selectedTab, searchQuery, freshOnly, selectedPlatform, selectedBrand) {
        if (selectedTab == 1) {
            savedList
        } else {
            val q = searchQuery.trim().lowercase()
            newsList.filter { item ->
                val matchFresh = !freshOnly || item.isFresh24h
                val matchPlat = selectedPlatform == "Все" || item.platform == selectedPlatform
                val matchBrand = selectedBrand == "Все" || item.brand == selectedBrand
                val matchQuery = q.isEmpty() ||
                    item.title.lowercase().contains(q) ||
                    item.summary.lowercase().contains(q) ||
                    item.sourceLabel.lowercase().contains(q) ||
                    item.brand.lowercase().contains(q)
                matchFresh && matchPlat && matchBrand && matchQuery
            }
        }
    }

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.16f),
                                MaterialTheme.colorScheme.background
                            )
                        )
                    )
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(9.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "NewsPulse AI",
                                fontSize = 21.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                        Text(
                            lastUpdatedText,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.65f)
                        )
                    }

                    if (selectedTab != 2) {
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .clickable(enabled = !isLoading) { refreshFeed() },
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.primary
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (isLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(15.dp),
                                        strokeWidth = 2.dp,
                                        color = MaterialTheme.colorScheme.onPrimary
                                    )
                                } else {
                                    Icon(
                                        Icons.Default.Refresh,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.size(15.dp)
                                    )
                                }
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    "Обновить",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                        }
                    }
                }
            }
        },
        bottomBar = {
            FloatingCapsuleNavBar(
                selectedTab = selectedTab,
                savedCount = savedList.size,
                onSelectTab = { selectedTab = it }
            )
        }
    ) { innerPadding ->
        if (selectedTab == 2) {
            SettingsScreen(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = innerPadding.calculateTopPadding()),
                bottomPadding = innerPadding.calculateBottomPadding() + 16.dp,
                settings = settings,
                newsCount = newsList.size,
                isLoading = isLoading,
                onSettingsChange = onSettingsChange,
                onTriggerScan = { refreshFeed() },
                onClearCache = {
                    newsList = emptyList()
                    LocalCache.setCached(context, emptyList())
                    refreshFeed()
                }
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = innerPadding.calculateTopPadding()),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    top = 6.dp,
                    bottom = innerPadding.calculateBottomPadding() + 16.dp
                ),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (selectedTab == 0) {
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                StatPill(
                                    modifier = Modifier.weight(1f),
                                    title = "Всего",
                                    value = "${newsList.size}"
                                )
                                StatPill(
                                    modifier = Modifier.weight(1f),
                                    title = "За 24ч",
                                    value = "${newsList.count { it.isFresh24h }}"
                                )
                                StatPill(
                                    modifier = Modifier.weight(1f),
                                    title = "Избранное",
                                    value = "${savedList.size}"
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = MaterialTheme.colorScheme.surface,
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.22f))
                            ) {
                                TextField(
                                    value = searchQuery,
                                    onValueChange = { searchQuery = it },
                                    modifier = Modifier.fillMaxWidth(),
                                    placeholder = {
                                        Text(
                                            "Поиск: HyperOS, Xiaomi 17T, Galaxy, ИИ...",
                                            fontSize = 13.sp,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                        )
                                    },
                                    leadingIcon = {
                                        Icon(
                                            Icons.Default.Search,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(19.dp)
                                        )
                                    },
                                    trailingIcon = {
                                        if (searchQuery.isNotEmpty()) {
                                            IconButton(onClick = { searchQuery = "" }) {
                                                Icon(
                                                    Icons.Default.Close,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        }
                                    },
                                    singleLine = true,
                                    colors = TextFieldDefaults.colors(
                                        focusedContainerColor = Color.Transparent,
                                        unfocusedContainerColor = Color.Transparent,
                                        focusedIndicatorColor = Color.Transparent,
                                        unfocusedIndicatorColor = Color.Transparent,
                                        disabledIndicatorColor = Color.Transparent
                                    )
                                )
                            }

                            Row(
                                modifier = Modifier.horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                StyledFilterChip(
                                    label = "За 24ч",
                                    selected = freshOnly,
                                    onClick = { freshOnly = !freshOnly }
                                )
                                platforms.forEach { plat ->
                                    StyledFilterChip(
                                        label = plat,
                                        selected = selectedPlatform == plat,
                                        onClick = { selectedPlatform = plat }
                                    )
                                }
                            }

                            Row(
                                modifier = Modifier.horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                brands.forEach { brand ->
                                    val count = brandCounts[brand] ?: 0
                                    val labelText = if (brand == "Все") "Все ($count)" else "$brand ($count)"
                                    StyledFilterChip(
                                        label = labelText,
                                        selected = selectedBrand == brand,
                                        onClick = { selectedBrand = brand }
                                    )
                                }
                            }
                        }
                    }
                }

                if (displayedItems.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 64.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (isLoading) "Загружаем свежие новости по всем брендам..."
                                else if (selectedTab == 1) "В избранном пока пусто"
                                else "По этому фильтру новостей нет — попробуй выбрать платформу «Все»",
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.65f),
                                fontSize = 14.sp
                            )
                        }
                    }
                } else {
                    items(displayedItems, key = { it.url }) { item ->
                        val isSaved = savedList.any { it.url == item.url }
                        NewsCard(
                            item = item,
                            isSaved = isSaved,
                            settings = settings,
                            onOpen = { openCustomTab(context, item.url) },
                            onShare = { shareArticle(context, item) },
                            onToggleSave = {
                                val updated = if (isSaved) {
                                    savedList.filterNot { it.url == item.url }
                                } else {
                                    listOf(item) + savedList
                                }
                                savedList = updated
                                LocalCache.setSaved(context, updated)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FloatingCapsuleNavBar(
    selectedTab: Int,
    savedCount: Int,
    onSelectTab: (Int) -> Unit
) {
    val primary = MaterialTheme.colorScheme.primary
    val surface = MaterialTheme.colorScheme.surface

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .shadow(
                    elevation = 10.dp,
                    shape = RoundedCornerShape(28.dp),
                    ambientColor = primary.copy(alpha = 0.25f),
                    spotColor = primary.copy(alpha = 0.25f)
                ),
            shape = RoundedCornerShape(28.dp),
            color = surface.copy(alpha = 0.96f),
            border = BorderStroke(1.dp, primary.copy(alpha = 0.24f))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CapsuleNavItem(
                    selected = selectedTab == 0,
                    icon = Icons.Outlined.Newspaper,
                    label = "Лента",
                    onClick = { onSelectTab(0) }
                )
                CapsuleNavItem(
                    selected = selectedTab == 1,
                    icon = if (selectedTab == 1) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                    label = if (savedCount > 0) "Избранное ($savedCount)" else "Избранное",
                    onClick = { onSelectTab(1) }
                )
                CapsuleNavItem(
                    selected = selectedTab == 2,
                    icon = if (selectedTab == 2) Icons.Filled.Settings else Icons.Outlined.Settings,
                    label = "Настройки",
                    onClick = { onSelectTab(2) }
                )
            }
        }
    }
}

@Composable
fun CapsuleNavItem(
    selected: Boolean,
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    val primary = MaterialTheme.colorScheme.primary
    val bgColor by animateColorAsState(
        targetValue = if (selected) primary.copy(alpha = 0.16f) else Color.Transparent,
        label = "navBg"
    )
    val contentColor by animateColorAsState(
        targetValue = if (selected) primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
        label = "navContent"
    )

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(22.dp))
            .background(bgColor)
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = contentColor,
            modifier = Modifier.size(19.dp)
        )
        if (selected) {
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = contentColor,
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
    val bgColor by animateColorAsState(
        targetValue = if (selected) primary else MaterialTheme.colorScheme.surface,
        label = "chipBg"
    )
    val textColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
        label = "chipText"
    )
    val borderColor = if (selected) primary else primary.copy(alpha = 0.2f)

    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        color = bgColor,
        border = BorderStroke(1.dp, borderColor)
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 13.dp, vertical = 7.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                color = textColor,
                maxLines = 1
            )
        }
    }
}

@Composable
fun StatPill(modifier: Modifier = Modifier, title: String, value: String) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.18f))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                fontSize = 16.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = title,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
            )
        }
    }
}

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    bottomPadding: androidx.compose.ui.unit.Dp,
    settings: UserAppSettings,
    newsCount: Int,
    isLoading: Boolean,
    onSettingsChange: (UserAppSettings) -> Unit,
    onTriggerScan: () -> Unit,
    onClearCache: () -> Unit
) {
    var showAiOfficeDialog by remember { mutableStateOf(false) }

    if (showAiOfficeDialog) {
        AiOfficeLiveDialog(
            newsCount = newsCount,
            isScanning = isLoading,
            onRunScan = onTriggerScan,
            onDismiss = { showAiOfficeDialog = false }
        )
    }

    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = 8.dp,
            bottom = bottomPadding
        ),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column {
                Text(
                    "Настройки",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    "Персонализация тем, шрифта, карточек и мониторинг ИИ-офиса",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.65f)
                )
            }
        }

        // Интерактивный ИИ-Офис
        item {
            val primary = MaterialTheme.colorScheme.primary
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .clickable { showAiOfficeDialog = true },
                shape = RoundedCornerShape(18.dp),
                color = primary.copy(alpha = 0.12f),
                border = BorderStroke(1.2.dp, primary.copy(alpha = 0.45f))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Hub,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    "ИИ-Офис (Live-карта)",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF10B981))
                                )
                            }
                            Text(
                                "4 агента онлайн • Нажмите, чтобы открыть план офиса",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }
                    }
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = primary
                    ) {
                        Text(
                            "Открыть",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }

        // Раздел 1: Темы и цветовая схема
        item {
            SettingsSectionCard(title = "Тема и режим экрана") {
                val modes = listOf(
                    "system" to "Авто",
                    "light" to "Светлая",
                    "dark" to "Тёмная",
                    "amoled" to "AMOLED"
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    modes.forEach { (id, label) ->
                        StyledFilterChip(
                            label = label,
                            selected = settings.darkMode == id,
                            onClick = { onSettingsChange(settings.copy(darkMode = id)) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(Modifier.height(14.dp))
                Text(
                    "Цветовая палитра",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                Spacer(Modifier.height(8.dp))

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    AppPalettes.chunked(2).forEach { rowPalettes ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            rowPalettes.forEach { pal ->
                                val isSelected = settings.paletteId == pal.id
                                Surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(14.dp))
                                        .clickable { onSettingsChange(settings.copy(paletteId = pal.id)) },
                                    shape = RoundedCornerShape(14.dp),
                                    color = if (isSelected) {
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.16f)
                                    } else {
                                        MaterialTheme.colorScheme.background.copy(alpha = 0.65f)
                                    },
                                    border = BorderStroke(
                                        width = if (isSelected) 1.5.dp else 1.dp,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.primary.copy(alpha = 0.16f)
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier.padding(11.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .size(20.dp)
                                                    .clip(CircleShape)
                                                    .background(pal.primaryDark)
                                                    .border(1.5.dp, Color.White.copy(alpha = 0.5f), CircleShape)
                                            )
                                            Spacer(Modifier.width(8.dp))
                                            Text(
                                                pal.name,
                                                fontSize = 12.sp,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                            )
                                        }
                                        if (isSelected) {
                                            Icon(
                                                Icons.Default.Check,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(16.dp)
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

        // Раздел 2: Размер текста и чтение
        item {
            SettingsSectionCard(title = "Текст и отображение ленты") {
                Text(
                    "Размер шрифта",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                Spacer(Modifier.height(8.dp))
                val sizes = listOf(
                    0.90f to "Мелкий",
                    1.0f to "Стандарт",
                    1.12f to "Крупный",
                    1.22f to "Макси"
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    sizes.forEach { (scale, label) ->
                        StyledFilterChip(
                            label = label,
                            selected = settings.fontScale == scale,
                            onClick = { onSettingsChange(settings.copy(fontScale = scale)) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(Modifier.height(14.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Показывать краткую суть",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            "Отключите для компактного просмотра только заголовков",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                    Switch(
                        checked = settings.showSummary,
                        onCheckedChange = { onSettingsChange(settings.copy(showSummary = it)) }
                    )
                }
            }
        }

        // Раздел 3: Форма и стиль карточек
        item {
            SettingsSectionCard(title = "Вид карточек и углы") {
                val styles = listOf(
                    "outlined" to "Контурный",
                    "filled" to "Заливка",
                    "compact" to "Компакт"
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    styles.forEach { (id, label) ->
                        StyledFilterChip(
                            label = label,
                            selected = settings.cardStyle == id,
                            onClick = { onSettingsChange(settings.copy(cardStyle = id)) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))
                val corners = listOf(
                    12 to "Строгие (12dp)",
                    18 to "Средние (18dp)",
                    26 to "Плавные (26dp)"
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    corners.forEach { (radius, label) ->
                        StyledFilterChip(
                            label = label,
                            selected = settings.cornerRadius == radius,
                            onClick = { onSettingsChange(settings.copy(cornerRadius = radius)) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // Предпросмотр карточки
        item {
            Text(
                "Предпросмотр карточки",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.75f)
            )
            Spacer(Modifier.height(8.dp))
            NewsCard(
                item = NewsItem(
                    id = "preview",
                    title = "Xiaomi представила новое поколение интерфейса HyperOS с ИИ-функциями",
                    sourceLabel = "Rozetked",
                    brand = "Xiaomi",
                    platform = "СМИ и блоги",
                    summary = "Обновление приносит ускоренную анимацию, улучшенную автономность и интеллектуальную сводку новостей.",
                    url = "https://rozetked.me",
                    timestamp = System.currentTimeMillis(),
                    timeLabel = "Только что"
                ),
                isSaved = true,
                settings = settings,
                onOpen = {},
                onShare = {},
                onToggleSave = {}
            )
        }

        // Кнопки обслуживания
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = { onSettingsChange(UserAppSettings()) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f))
                ) {
                    Text("Сбросить тему", fontSize = 12.sp)
                }
                OutlinedButton(
                    onClick = onClearCache,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f))
                ) {
                    Icon(Icons.Default.DeleteOutline, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Обновить кеш", fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun AiOfficeLiveDialog(
    newsCount: Int,
    isScanning: Boolean,
    onRunScan: () -> Unit,
    onDismiss: () -> Unit
) {
    val primary = MaterialTheme.colorScheme.primary
    val agents = remember(newsCount, isScanning) {
        listOf(
            AiAgentNode(
                id = 0,
                name = "Алиса-Парсер",
                role = "Сканер СМИ и RSS",
                roomTitle = "Отдел Веб-потоков",
                currentTask = if (isScanning) "Парсит 4PDA, Rozetked, iXBT и 3DNews..." else "Мониторит 14 техно-порталов в фоновом режиме",
                accentColor = Color(0xFF10B981),
                rx = 0.24f,
                ry = 0.25f
            ),
            AiAgentNode(
                id = 1,
                name = "Макс-Инсайд",
                role = "Монитор Telegram",
                roomTitle = "Сектор Каналов",
                currentTask = if (isScanning) "Читает посты Rozetked, Wylsacom, Xiaomishka..." else "Отслеживает 14 официальных и инсайдерских каналов",
                accentColor = Color(0xFF38BDF8),
                rx = 0.76f,
                ry = 0.25f
            ),
            AiAgentNode(
                id = 2,
                name = "Нова-ИИ",
                role = "Классификатор брендов",
                roomTitle = "Лаборатория тегов",
                currentTask = "Разметила $newsCount новостей по 13 брендам без дублей",
                accentColor = Color(0xFFA78BFA),
                rx = 0.24f,
                ry = 0.75f
            ),
            AiAgentNode(
                id = 3,
                name = "Пульс-Ред",
                role = "Редактор сводок",
                roomTitle = "Студия дайджестов",
                currentTask = "Сформировал краткую суть и подготовил карточки к чтению",
                accentColor = Color(0xFFFBBF24),
                rx = 0.76f,
                ry = 0.75f
            )
        )
    }

    var selectedAgentId by remember { mutableIntStateOf(0) }
    val activeAgent = agents.first { it.id == selectedAgentId }

    val infiniteTransition = rememberInfiniteTransition(label = "officeAnim")
    val packetProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "packet"
    )
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 24.dp),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.2.dp, primary.copy(alpha = 0.35f))
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Шапка окна
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "Офис ИИ-агентов",
                                fontSize = 19.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(Modifier.width(8.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFF10B981).copy(alpha = 0.18f)
                            ) {
                                Text(
                                    text = if (isScanning) "АКТИВНЫЙ РЕЙД" else "LIVE",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF10B981),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            "Нажмите на любого агента на схеме офиса",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Закрыть")
                    }
                }

                // Интерактивная карта офиса
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(270.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(Color(0xFF071310))
                        .border(1.dp, primary.copy(alpha = 0.3f), RoundedCornerShape(18.dp))
                        .pointerInput(Unit) {
                            detectTapGestures { tapOffset ->
                                val w = size.width.toFloat()
                                val h = size.height.toFloat()
                                val clicked = agents.minByOrNull { ag ->
                                    val dx = tapOffset.x - ag.rx * w
                                    val dy = tapOffset.y - ag.ry * h
                                    dx * dx + dy * dy
                                }
                                if (clicked != null) {
                                    selectedAgentId = clicked.id
                                }
                            }
                        }
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val w = size.width
                        val h = size.height
                        val center = Offset(w / 2f, h / 2f)

                        // Сетка пола офиса
                        val gridStep = 28.dp.toPx()
                        var gx = gridStep
                        while (gx < w) {
                            drawLine(
                                color = primary.copy(alpha = 0.07f),
                                start = Offset(gx, 0f),
                                end = Offset(gx, h),
                                strokeWidth = 1f
                            )
                            gx += gridStep
                        }
                        var gy = gridStep
                        while (gy < h) {
                            drawLine(
                                color = primary.copy(alpha = 0.07f),
                                start = Offset(0f, gy),
                                end = Offset(w, gy),
                                strokeWidth = 1f
                            )
                            gy += gridStep
                        }

                        // 4 кабинета-зоны по углам
                        val roomW = w * 0.41f
                        val roomH = h * 0.38f
                        val pad = w * 0.04f
                        val roomCoords = listOf(
                            Offset(pad, pad),
                            Offset(w - pad - roomW, pad),
                            Offset(pad, h - pad - roomH),
                            Offset(w - pad - roomW, h - pad - roomH)
                        )

                        roomCoords.forEachIndexed { idx, topLeft ->
                            val ag = agents[idx]
                            val isSel = ag.id == selectedAgentId
                            drawRoundRect(
                                color = ag.accentColor.copy(alpha = if (isSel) 0.16f else 0.07f),
                                topLeft = topLeft,
                                size = Size(roomW, roomH),
                                cornerRadius = CornerRadius(14.dp.toPx(), 14.dp.toPx())
                            )
                            drawRoundRect(
                                color = ag.accentColor.copy(alpha = if (isSel) 0.85f else 0.3f),
                                topLeft = topLeft,
                                size = Size(roomW, roomH),
                                cornerRadius = CornerRadius(14.dp.toPx(), 14.dp.toPx()),
                                style = Stroke(width = if (isSel) 2.dp.toPx() else 1.dp.toPx())
                            )
                        }

                        // Линии связи от каждого агента к центральному ядру NewsPulse AI
                        val dash = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                        agents.forEach { ag ->
                            val startPos = Offset(ag.rx * w, ag.ry * h)
                            drawLine(
                                color = ag.accentColor.copy(alpha = 0.45f),
                                start = startPos,
                                end = center,
                                strokeWidth = 2.dp.toPx(),
                                pathEffect = dash
                            )

                            // Бегущий пакет данных к серверу
                            val px = startPos.x + (center.x - startPos.x) * packetProgress
                            val py = startPos.y + (center.y - startPos.y) * packetProgress
                            drawCircle(
                                color = ag.accentColor,
                                radius = 4.dp.toPx(),
                                center = Offset(px, py)
                            )
                        }

                        // Центральный серверный хаб NewsPulse AI
                        drawCircle(
                            color = primary.copy(alpha = 0.22f),
                            radius = 28.dp.toPx() * pulseScale,
                            center = center
                        )
                        drawCircle(
                            color = Color(0xFF0D261F),
                            radius = 24.dp.toPx(),
                            center = center
                        )
                        drawCircle(
                            color = primary,
                            radius = 24.dp.toPx(),
                            center = center,
                            style = Stroke(width = 2.dp.toPx())
                        )

                        // Аватары ИИ-агентов за рабочими столами
                        agents.forEach { ag ->
                            val pos = Offset(ag.rx * w, ag.ry * h)
                            val isSel = ag.id == selectedAgentId

                            if (isSel) {
                                drawCircle(
                                    color = ag.accentColor.copy(alpha = 0.28f),
                                    radius = 22.dp.toPx() * pulseScale,
                                    center = pos
                                )
                            }
                            drawCircle(
                                color = ag.accentColor,
                                radius = 15.dp.toPx(),
                                center = pos
                            )
                            drawCircle(
                                color = Color(0xFF071310),
                                radius = 6.dp.toPx(),
                                center = pos
                            )
                        }
                    }

                    // Подписи кабинетов и агентов поверх схемы
                    agents.forEach { ag ->
                        val align = when (ag.id) {
                            0 -> Alignment.TopStart
                            1 -> Alignment.TopEnd
                            2 -> Alignment.BottomStart
                            else -> Alignment.BottomEnd
                        }
                        Column(
                            modifier = Modifier
                                .align(align)
                                .padding(horizontal = 16.dp, vertical = 10.dp),
                            horizontalAlignment = if (ag.id % 2 == 1) Alignment.End else Alignment.Start
                        ) {
                            Text(
                                text = ag.roomTitle,
                                fontSize = 9.sp,
                                color = Color.White.copy(alpha = 0.55f)
                            )
                            Text(
                                text = ag.name,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = ag.accentColor
                            )
                        }
                    }

                    // Метка центрального хаба
                    Text(
                        text = "AI HUB",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = primary,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                // Карточка выбранного агента с текущим статусом
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = activeAgent.accentColor.copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, activeAgent.accentColor.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${activeAgent.name} • ${activeAgent.role}",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = activeAgent.accentColor
                            )
                            Text(
                                text = "В сети",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF10B981)
                            )
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = activeAgent.currentTask,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.88f)
                        )
                    }
                }

                // Кнопка запуска командного сбора новостей прямо из офиса
                Button(
                    onClick = onRunScan,
                    enabled = !isScanning,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = if (isScanning) "Агенты собирают свежие новости..." else "Отправить агентов на сбор новостей",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun SettingsSectionCard(title: String, content: @Composable () -> Unit) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.18f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
fun NewsCard(
    item: NewsItem,
    isSaved: Boolean,
    settings: UserAppSettings,
    onOpen: () -> Unit,
    onShare: () -> Unit,
    onToggleSave: () -> Unit
) {
    val primary = MaterialTheme.colorScheme.primary
    val accentColor = when (item.platform) {
        "СМИ и блоги" -> primary
        "Оф. сайт" -> Color(0xFF10B981)
        else -> Color(0xFF38BDF8)
    }

    val shape = RoundedCornerShape(settings.cornerRadius.dp)
    val cardBorder = if (settings.cardStyle == "outlined") {
        BorderStroke(1.dp, primary.copy(alpha = 0.22f))
    } else null

    val cardContainerColor = if (settings.cardStyle == "filled") {
        primary.copy(alpha = 0.08f)
    } else {
        MaterialTheme.colorScheme.surface
    }

    val scale = settings.fontScale

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpen() },
        shape = shape,
        border = cardBorder,
        colors = CardDefaults.cardColors(containerColor = cardContainerColor)
    ) {
        Column(modifier = Modifier.padding(if (settings.cardStyle == "compact") 12.dp else 16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = accentColor.copy(alpha = 0.16f)
                    ) {
                        Text(
                            text = "${item.platform} • ${item.sourceLabel}",
                            color = accentColor,
                            fontSize = (11 * scale).sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    if (item.isVeryFresh6h) {
                        Spacer(Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = primary.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = "NEW",
                                color = primary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                Spacer(Modifier.width(8.dp))
                Text(
                    text = item.timeLabel,
                    fontSize = (11 * scale).sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }

            Spacer(Modifier.height(10.dp))
            Text(
                text = item.title,
                fontSize = (16 * scale).sp,
                fontWeight = FontWeight.Bold,
                lineHeight = (22 * scale).sp,
                color = MaterialTheme.colorScheme.onSurface
            )

            if (settings.showSummary && settings.cardStyle != "compact") {
                Spacer(Modifier.height(10.dp))
                Surface(
                    shape = RoundedCornerShape((settings.cornerRadius - 4).coerceAtLeast(8).dp),
                    color = MaterialTheme.colorScheme.background.copy(alpha = 0.72f)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Суть новости",
                                fontSize = (11 * scale).sp,
                                fontWeight = FontWeight.SemiBold,
                                color = accentColor
                            )
                            Text(
                                text = item.brand,
                                fontSize = (11 * scale).sp,
                                fontWeight = FontWeight.Bold,
                                color = primary
                            )
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = item.summary,
                            fontSize = (13 * scale).sp,
                            lineHeight = (18 * scale).sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f)
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onOpen,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text("Читать полностью ↗", fontSize = (13 * scale).sp, fontWeight = FontWeight.Bold)
                }
                FilledTonalIconButton(
                    onClick = onShare,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Поделиться",
                        modifier = Modifier.size(18.dp)
                    )
                }
                FilledTonalIconButton(
                    onClick = onToggleSave,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = if (isSaved) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                        contentDescription = "В избранное",
                        tint = if (isSaved) primary else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

fun openCustomTab(context: Context, url: String) {
    try {
        CustomTabsIntent.Builder()
            .setShowTitle(true)
            .build()
            .launchUrl(context, Uri.parse(url))
    } catch (_: Exception) {
    }
}

fun shareArticle(context: Context, item: NewsItem) {
    try {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, item.title)
            putExtra(Intent.EXTRA_TEXT, "${item.title}\n\n${item.url}")
        }
        context.startActivity(Intent.createChooser(intent, "Поделиться новостью"))
    } catch (_: Exception) {
    }
}
