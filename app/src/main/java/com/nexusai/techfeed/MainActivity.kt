package com.nexusai.techfeed

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Newspaper
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
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
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin

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

enum class AgentLocationZone(val label: String) {
    DESK("За рабочим столом"),
    DINING("Обедает в столовой"),
    LOUNGE("Отдыхает у ТВ")
}

data class OfficeAgentProfile(
    val id: Int,
    val name: String,
    val role: String,
    val roomTitle: String,
    val accentColor: Color,
    val hairColor: Color,
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
    val completedAt: String = ""
)

data class AgentLiveStatus(
    val currentTask: SoloTaskItem,
    val progress: Float,
    val targetZone: AgentLocationZone,
    val breakTicksRemaining: Int,
    val completedHistory: List<SoloTaskItem>
)

data class TgChannel(val name: String, val defaultBrand: String, val username: String)
data class RssSite(val name: String, val defaultBrand: String, val platform: String, val url: String)
data class WebPageSource(val name: String, val defaultBrand: String, val url: String, val linkRegex: String)
data class BrandSearchQuery(val brand: String, val query: String)

object NewsRepository {
    private val client = OkHttpClient.Builder()
        .connectTimeout(11, TimeUnit.SECONDS)
        .readTimeout(11, TimeUnit.SECONDS)
        .followRedirects(true)
        .build()

    private val tgChannels = listOf(
        TgChannel("Rozetked", "Общее", "rozetked"),
        TgChannel("Wylsacom Red", "Общее", "Wylsared"),
        TgChannel("Mobiltelefon.ru", "Общее", "mobiltelefonru"),
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
        RssSite("Kod.ru", "Общее", "СМИ и блоги", "https://kod.ru/rss/"),
        RssSite("Samsung Newsroom RU", "Samsung", "Оф. сайт", "https://news.samsung.com/ru/feed"),
        RssSite("Xiaomi Global Blog", "Xiaomi", "Оф. сайт", "https://blog.mi.com/en/feed/"),
        RssSite("Apple Newsroom", "Apple", "Оф. сайт", "https://www.apple.com/newsroom/rss-feed.rss"),
        RssSite("Instagram @xiaomi", "Xiaomi", "Instagram", "https://rsshub.app/instagram/user/xiaomi"),
        RssSite("Instagram @samsungmobile", "Samsung", "Instagram", "https://rsshub.app/instagram/user/samsungmobile"),
        RssSite("Instagram @nothing", "Nothing", "Instagram", "https://rsshub.app/instagram/user/nothing"),
        RssSite("Instagram @huaweimobile", "Huawei", "Instagram", "https://rsshub.app/instagram/user/huaweimobile"),
        RssSite("Instagram @oppo", "OPPO", "Instagram", "https://rsshub.app/instagram/user/oppo")
    )

    private val brandNewsFeeds = listOf(
        BrandSearchQuery("Xiaomi", "Xiaomi OR HyperOS OR Redmi OR POCO"),
        BrandSearchQuery("Samsung", "Samsung Galaxy OR One UI OR телевизор Samsung"),
        BrandSearchQuery("Apple", "Apple iPhone OR MacBook OR iPad"),
        BrandSearchQuery("Google", "Google Pixel OR Android"),
        BrandSearchQuery("Huawei", "Huawei OR HarmonyOS OR MateBook OR Watch"),
        BrandSearchQuery("HONOR", "HONOR смартфон OR MagicBook OR MagicOS"),
        BrandSearchQuery("OPPO", "OPPO смартфон OR ColorOS"),
        BrandSearchQuery("OnePlus", "OnePlus смартфон OR OxygenOS"),
        BrandSearchQuery("vivo", "vivo смартфон OR iQOO"),
        BrandSearchQuery("realme", "realme смартфон OR ноутбук"),
        BrandSearchQuery("Nothing", "Nothing Phone OR CMF"),
        BrandSearchQuery("Электроавто", "электромобиль Xiaomi SU7 OR Zeekr OR BYD OR Tesla OR Li Auto"),
        BrandSearchQuery("Умный дом и ТВ", "умный дом Aqara OR телевизор OLED Mini-LED")
    )

    private val officialWebPages = listOf(
        WebPageSource("Xiaomi Россия (mi.com/ru)", "Xiaomi", "https://www.mi.com/ru/discover/", """/ru/discover/article\?id=\d+"""),
        WebPageSource("Xiaomi Community (c.mi.com)", "Xiaomi", "https://new.c.mi.com/ru/", """/ru/post/\d+"""),
        WebPageSource("HONOR Россия", "HONOR", "https://www.honor.com/ru/news/", """/ru/news/[^"#?]+/"""),
        WebPageSource("Huawei Россия", "Huawei", "https://consumer.huawei.com/ru/community/", """/ru/community/details/[^"#?]+""")
    )

    private val bannedTopics = listOf(
        "космос", "ракета", "роскосмос", "nasa", "spacex", "орбит", "спутник ",
        "астероид", "телескоп", "галактик", "марс ", "луна ", "лунн", "мкс",
        "астроном", "космонавт", "звезд", "звёзд", "черная дыра", "чёрная дыра",
        "гороскоп", "рецепт", "политик", "выборы", "сериал", "фильм ", "трейлер фильма"
    )

    private val techKeywords = listOf(
        "смартфон", "телефон", "iphone", "galaxy", "xiaomi", "redmi", "poco",
        "huawei", "honor", "oppo", "oneplus", "vivo", "realme", "nothing", "pixel",
        "ноутбук", "macbook", "ultrabook", "планшет", "ipad", "телевизор", "oled",
        "qled", "mini-led", "монитор", "процессор", "видеокарт", "rtx", "ryzen",
        "snapdragon", "dimensity", "часы", "watch", "наушник", "buds", "airpods",
        "умный дом", "пылесос", "роутер", "wi-fi", "аккумулятор", "зарядк",
        "камера", "дисплей", "экран", "прошивк", "hyperos", "one ui", "android",
        "ios", "harmonyos", "электромобил", "электрокар", "zeekr", "byd", "tesla",
        "su7", "yu7", "li auto", "гаджет", "консол", "playstation", "nintendo", "xbox"
    )

    private fun isPureTechOrGadget(title: String, summary: String): Boolean {
        val text = "$title $summary".lowercase()
        if (bannedTopics.any { it in text }) return false
        return techKeywords.any { it in text }
    }

    private fun detectBrand(text: String, fallback: String): String {
        if (fallback != "Общее") return fallback
        val lower = text.lowercase()
        return when {
            listOf("xiaomi", "hyperos", "redmi", "poco", "miui", "сяоми").any { it in lower } -> "Xiaomi"
            listOf("samsung", "galaxy", "one ui", "самсунг").any { it in lower } -> "Samsung"
            listOf("apple", "iphone", "ios ", "ipad", "macbook", "airpods").any { it in lower } -> "Apple"
            listOf("google", "pixel", "android ").any { it in lower } -> "Google"
            listOf("huawei", "harmonyos", "mate ", "pura ", "watch fit", "matebook").any { it in lower } -> "Huawei"
            listOf("honor", "magic os", "magicos", "magicbook").any { it in lower } -> "HONOR"
            listOf("oppo", "coloros", "find x", "reno").any { it in lower } -> "OPPO"
            listOf("oneplus", "oxygenos").any { it in lower } -> "OnePlus"
            listOf("vivo", "iqoo", "originos").any { it in lower } -> "vivo"
            listOf("realme", "реалми").any { it in lower } -> "realme"
            listOf("nothing", "cmf").any { it in lower } -> "Nothing"
            listOf("электромобил", "электрокар", "zeekr", "byd", "tesla", "su7", "yu7", "li auto").any { it in lower } -> "Электроавто"
            listOf("умный дом", "aqara", "телевизор", "oled", "qled", "mini-led", "пылесос").any { it in lower } -> "Умный дом и ТВ"
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

    private fun makeSummary(raw: String, maxLen: Int = 210): String {
        val clean = Jsoup.parse(raw).text().replace(Regex("\\s+"), " ").trim()
        if (clean.isEmpty()) return "Нажмите «Читать полностью», чтобы открыть материал в источнике."
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
            if (text.length < 22) continue

            val title = if (text.length > 90) text.take(90).substringBeforeLast(" ") + "..." else text
            val summary = makeSummary(text)
            if (!isPureTechOrGadget(title, summary)) continue

            val ts = try {
                Instant.parse(timeAttr).toEpochMilli()
            } catch (_: Exception) {
                System.currentTimeMillis()
            }
            val detected = detectBrand(text, ch.defaultBrand)

            list.add(
                NewsItem(
                    id = "tg_$postId",
                    title = title,
                    sourceLabel = ch.name,
                    brand = detected,
                    platform = "Telegram",
                    summary = summary,
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

        for (entry in entries.take(8)) {
            val title = Jsoup.parse(entry.selectFirst("title")?.text() ?: "").text().trim()
            if (title.isBlank()) continue
            val link = entry.selectFirst("link")?.text()?.trim()?.ifBlank {
                entry.selectFirst("link")?.attr("href") ?: ""
            } ?: continue
            if (link.isBlank()) continue

            val desc = entry.selectFirst("description")?.text()
                ?: entry.selectFirst("summary")?.text()
                ?: entry.selectFirst("content")?.text() ?: ""
            val summary = makeSummary(desc)
            if (!isPureTechOrGadget(title, summary)) continue

            val dateStr = entry.selectFirst("pubDate")?.text()
                ?: entry.selectFirst("updated")?.text()
                ?: entry.selectFirst("published")?.text() ?: ""
            val ts = parseRssDate(dateStr)
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
            val summary = "Материал издания «$sourceName» по теме ${bq.brand}."
            if (!isPureTechOrGadget(cleanTitle, summary)) continue

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
                    summary = summary,
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
            .mapIndexedNotNull { idx, (a, absUrl) ->
                var title = a.text().replace(Regex("\\s+"), " ").trim()
                if (title.length < 10) {
                    val slug = URI(absUrl).path.trimEnd('/').substringAfterLast('/')
                        .replace(Regex("^\\d+-"), "")
                        .replace("-", " ")
                        .trim()
                    title = if (slug.length > 8) slug.replaceFirstChar { it.uppercase() } else "Публикация на ${wp.name}"
                }
                val summary = "Официальный материал на портале ${wp.name}."
                if (!isPureTechOrGadget(title, summary)) return@mapIndexedNotNull null

                val ts = System.currentTimeMillis() - (idx * 25 + 15) * 60_000L
                NewsItem(
                    id = "web_${absUrl.hashCode()}",
                    title = title.take(95),
                    sourceLabel = wp.name,
                    brand = detectBrand(title, wp.defaultBrand),
                    platform = "Оф. сайт",
                    summary = summary,
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
    private const val KEY_CACHE = "cached_news_ru_v4"
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
    var lastUpdatedText by remember { mutableStateOf(if (newsList.isNotEmpty()) "${newsList.size} новостей о технике" else "Загрузка...") }

    var selectedTab by remember { mutableIntStateOf(0) }
    var searchQuery by remember { mutableStateOf("") }
    var isSearchExpanded by remember { mutableStateOf(false) }
    var showFilterSheet by remember { mutableStateOf(false) }

    var freshOnly by remember { mutableStateOf(false) }
    var selectedPlatform by remember { mutableStateOf("Все") }
    var selectedBrand by remember { mutableStateOf("Все") }

    val brands = listOf(
        "Все", "Xiaomi", "Samsung", "Apple", "Google", "Huawei",
        "HONOR", "OPPO", "OnePlus", "vivo", "realme", "Nothing",
        "Электроавто", "Умный дом и ТВ", "Общее"
    )
    val platforms = listOf("Все", "СМИ и блоги", "Telegram", "Instagram", "Оф. сайт")

    val hasActiveFilters = freshOnly || selectedPlatform != "Все" || selectedBrand != "Все"

    fun refreshFeed() {
        if (isLoading) return
        isLoading = true
        lastUpdatedText = "Собираем новости по технике..."
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

    if (showFilterSheet) {
        ModalBottomSheet(
            onDismissRequest = { showFilterSheet = false },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Фильтры ленты",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (hasActiveFilters) {
                        TextButton(onClick = {
                            freshOnly = false
                            selectedPlatform = "Все"
                            selectedBrand = "Все"
                        }) {
                            Text("Сбросить всё", fontSize = 12.sp)
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Только свежие за 24 часа",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Switch(
                        checked = freshOnly,
                        onCheckedChange = { freshOnly = it }
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Откуда брать новости",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
                    )
                    platforms.chunked(3).forEach { rowPlats ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            rowPlats.forEach { plat ->
                                StyledFilterChip(
                                    label = plat,
                                    selected = selectedPlatform == plat,
                                    onClick = { selectedPlatform = plat },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            repeat(3 - rowPlats.size) {
                                Spacer(Modifier.weight(1f))
                            }
                        }
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Бренды и категории техники",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
                    )
                    brands.chunked(3).forEach { rowBrands ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            rowBrands.forEach { brand ->
                                val count = brandCounts[brand] ?: 0
                                val label = if (brand == "Все") "Все ($count)" else "$brand ($count)"
                                StyledFilterChip(
                                    label = label,
                                    selected = selectedBrand == brand,
                                    onClick = { selectedBrand = brand },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            repeat(3 - rowBrands.size) {
                                Spacer(Modifier.weight(1f))
                            }
                        }
                    }
                }

                Button(
                    onClick = { showFilterSheet = false },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp, bottom = 18.dp),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(
                        "Показать (${displayedItems.size})",
                        fontWeight = FontWeight.Bold
                    )
                }
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
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.14f),
                                MaterialTheme.colorScheme.background
                            )
                        )
                    )
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary)
                            )
                            Spacer(Modifier.width(7.dp))
                            Text(
                                "NewsPulse AI",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                        val subInfo = buildString {
                            append(lastUpdatedText)
                            if (selectedBrand != "Все") append(" • $selectedBrand")
                            if (selectedPlatform != "Все") append(" • $selectedPlatform")
                        }
                        Text(
                            text = subInfo,
                            fontSize = 11.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.65f)
                        )
                    }

                    if (selectedTab == 0) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            FilledTonalIconButton(
                                onClick = {
                                    isSearchExpanded = !isSearchExpanded
                                    if (!isSearchExpanded) searchQuery = ""
                                },
                                modifier = Modifier.size(38.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(
                                    imageVector = if (isSearchExpanded) Icons.Default.Close else Icons.Default.Search,
                                    contentDescription = "Поиск",
                                    tint = if (isSearchExpanded || searchQuery.isNotEmpty()) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            Box {
                                FilledTonalIconButton(
                                    onClick = { showFilterSheet = true },
                                    modifier = Modifier.size(38.dp),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Tune,
                                        contentDescription = "Фильтры",
                                        tint = if (hasActiveFilters) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                if (hasActiveFilters) {
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .padding(4.dp)
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.primary)
                                    )
                                }
                            }

                            Surface(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable(enabled = !isLoading) { refreshFeed() },
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.primary
                            ) {
                                Box(
                                    modifier = Modifier.size(38.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isLoading) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(16.dp),
                                            strokeWidth = 2.dp,
                                            color = MaterialTheme.colorScheme.onPrimary
                                        )
                                    } else {
                                        Icon(
                                            Icons.Default.Refresh,
                                            contentDescription = "Обновить",
                                            tint = MaterialTheme.colorScheme.onPrimary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                AnimatedVisibility(visible = selectedTab == 0 && isSearchExpanded) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.28f))
                    ) {
                        TextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = {
                                Text(
                                    "Найти: Xiaomi 17T, OLED ТВ, ноутбук, электрокар...",
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                )
                            },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { searchQuery = "" }) {
                                        Icon(
                                            Icons.Default.Close,
                                            contentDescription = null,
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
                                unfocusedIndicatorColor = Color.Transparent
                            )
                        )
                    }
                }
            }
        },
        bottomBar = {
            FullWidthBottomNavBar(
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
                bottomPadding = innerPadding.calculateBottomPadding() + 12.dp,
                settings = settings,
                newsList = newsList,
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
                    top = 4.dp,
                    bottom = innerPadding.calculateBottomPadding() + 12.dp
                ),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (displayedItems.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 72.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (isLoading) "Собираем свежие новости о технике и гаджетах..."
                                else if (selectedTab == 1) "В избранном пока пусто"
                                else "По выбранным фильтрам новостей нет",
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
fun FullWidthBottomNavBar(
    selectedTab: Int,
    savedCount: Int,
    onSelectTab: (Int) -> Unit
) {
    val primary = MaterialTheme.colorScheme.primary
    val surface = MaterialTheme.colorScheme.surface

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = surface,
        shadowElevation = 6.dp
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(0.8.dp)
                    .background(primary.copy(alpha = 0.22f))
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .height(42.dp)
                    .padding(horizontal = 6.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                FullWidthNavTab(
                    modifier = Modifier.weight(1f),
                    selected = selectedTab == 0,
                    icon = Icons.Outlined.Newspaper,
                    label = "Лента",
                    onClick = { onSelectTab(0) }
                )
                FullWidthNavTab(
                    modifier = Modifier.weight(1f),
                    selected = selectedTab == 1,
                    icon = if (selectedTab == 1) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                    label = if (savedCount > 0) "Избранное ($savedCount)" else "Избранное",
                    onClick = { onSelectTab(1) }
                )
                FullWidthNavTab(
                    modifier = Modifier.weight(1f),
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
fun FullWidthNavTab(
    modifier: Modifier = Modifier,
    selected: Boolean,
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    val primary = MaterialTheme.colorScheme.primary
    val bgTint by animateColorAsState(
        targetValue = if (selected) primary.copy(alpha = 0.14f) else Color.Transparent,
        label = "tabBgCompact"
    )
    val contentColor by animateColorAsState(
        targetValue = if (selected) primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
        label = "tabColorCompact"
    )

    Row(
        modifier = modifier
            .fillMaxHeight()
            .padding(vertical = 4.dp, horizontal = 3.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(bgTint)
            .clickable { onClick() },
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = contentColor,
            modifier = Modifier.size(16.dp)
        )
        Spacer(Modifier.width(5.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            color = contentColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
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
    val bgColor by animateColorAsState(
        targetValue = if (selected) primary else MaterialTheme.colorScheme.background.copy(alpha = 0.7f),
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
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                color = textColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    bottomPadding: androidx.compose.ui.unit.Dp,
    settings: UserAppSettings,
    newsList: List<NewsItem>,
    isLoading: Boolean,
    onSettingsChange: (UserAppSettings) -> Unit,
    onTriggerScan: () -> Unit,
    onClearCache: () -> Unit
) {
    var showAiOfficeDialog by remember { mutableStateOf(false) }

    if (showAiOfficeDialog) {
        AiOfficeLiveDialog(
            newsList = newsList,
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
                    "Персонализация тем, шрифта, карточек и ИИ-кабинет",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.65f)
                )
            }
        }

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
                                    "ИИ-Кабинет (6 агентов)",
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
                                "Кабинеты с мониторами, коридоры, столовая и комната отдыха",
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

private fun createSoloAgentTask(
    ticketNumber: Int,
    agentId: Int,
    newsList: List<NewsItem>
): SoloTaskItem {
    val nowStr = ZonedDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"))
    val sampleNews = if (newsList.isNotEmpty()) {
        newsList[(ticketNumber * 5 + agentId) % newsList.size]
    } else null

    val brandPool = listOf("Xiaomi", "Samsung", "Huawei", "Apple", "HONOR", "OnePlus", "Google", "realme", "Электроавто", "Умный дом и ТВ")
    val brand = sampleNews?.brand?.takeIf { it != "Общее" } ?: brandPool[(ticketNumber + agentId) % brandPool.size]
    val source = sampleNews?.sourceLabel ?: listOf("4PDA", "Rozetked", "iXBT", "Telegram", "Instagram")[(ticketNumber + agentId) % 5]
    val headlineHint = sampleNews?.title?.take(44)?.let { "«$it...»" } ?: "новинкам $brand"

    return when (agentId) {
        0 -> SoloTaskItem(
            ticketNumber = ticketNumber,
            badge = "СМИ и RSS • $brand",
            title = "Парсинг RSS-лент $source: разбор обзоров и характеристик $brand ($headlineHint)",
            resultSummary = "Обработано ${3 + (ticketNumber % 4)} свежих материала $source по бренду $brand",
            startedAt = nowStr
        )
        1 -> SoloTaskItem(
            ticketNumber = ticketNumber,
            badge = "Telegram • $brand",
            title = "Мониторинг Telegram-каналов: проверка инсайдов и прошивок $brand ($headlineHint)",
            resultSummary = "Извлечено ${2 + (ticketNumber % 4)} поста из Telegram по теме $brand",
            startedAt = nowStr
        )
        2 -> SoloTaskItem(
            ticketNumber = ticketNumber,
            badge = "Instagram • $brand",
            title = "Проверка официального Instagram $brand: анонсы, тизеры и фото новинок",
            resultSummary = "Проверены свежие публикации Instagram-профиля $brand",
            startedAt = nowStr
        )
        3 -> SoloTaskItem(
            ticketNumber = ticketNumber,
            badge = "Электроавто",
            title = "Анализ новостей по электрокарам Xiaomi SU7/YU7, Zeekr, BYD и батареям",
            resultSummary = "Обновлена сводка по электромобилям и новым автопилотам",
            startedAt = nowStr
        )
        4 -> SoloTaskItem(
            ticketNumber = ticketNumber,
            badge = "ТВ и Умный дом",
            title = "Сбор релизов OLED/Mini-LED телевизоров, роботов-пылесосов и датчиков Aqara",
            resultSummary = "Добавлены новые анонсы телевизоров и устройств умного дома",
            startedAt = nowStr
        )
        else -> SoloTaskItem(
            ticketNumber = ticketNumber,
            badge = "Ноутбуки и Фильтр",
            title = "Проверка релизов ноутбуков MacBook/MateBook и очистка ленты от нетехнического шума",
            resultSummary = "Отфильтрованы дубликаты и проверены характеристики новых ноутбуков",
            startedAt = nowStr
        )
    }
}

private fun buildCleanPath(
    fromPos: Offset,
    ag: OfficeAgentProfile,
    destination: AgentLocationZone
): List<Offset> {
    val points = mutableListOf<Offset>()
    val isCurrentlyInsideRoom = (ag.isTopRow && fromPos.y < 0.32f) || (!ag.isTopRow && fromPos.y > 0.68f)

    // Если агент внутри своего кабинета — сначала строго через центр дверного проёма в коридор
    if (isCurrentlyInsideRoom) {
        points.add(ag.roomDoorPos)
        points.add(ag.corridorEntryPos)
    }

    when (destination) {
        AgentLocationZone.DESK -> {
            // Идём по своей полосе коридора к входу в свой кабинет и строго через центр двери к креслу
            points.add(Offset(fromPos.x, ag.corridorLaneY))
            points.add(Offset(ag.corridorEntryPos.x, ag.corridorLaneY))
            points.add(ag.corridorEntryPos)
            points.add(ag.roomDoorPos)
            points.add(ag.deskChairPos)
        }
        AgentLocationZone.DINING -> {
            // Идём по своей полосе коридора в центральный проход (x = 0.50), затем к своему стулу в столовой
            points.add(Offset(ag.corridorEntryPos.x, ag.corridorLaneY))
            points.add(Offset(0.50f, ag.corridorLaneY))
            points.add(Offset(0.50f, 0.50f))
            points.add(Offset(ag.diningSeatPos.x, 0.50f))
            points.add(ag.diningSeatPos)
        }
        AgentLocationZone.LOUNGE -> {
            // Идём по своей полосе коридора в центральный проход (x = 0.50), затем к своему месту на диване у ТВ
            points.add(Offset(ag.corridorEntryPos.x, ag.corridorLaneY))
            points.add(Offset(0.50f, ag.corridorLaneY))
            points.add(Offset(0.50f, 0.50f))
            points.add(Offset(ag.loungeSeatPos.x, 0.50f))
            points.add(ag.loungeSeatPos)
        }
    }

    // Убираем дублирующиеся или уже пройденные стартовые точки
    val cleaned = mutableListOf<Offset>()
    for (pt in points) {
        val prev = cleaned.lastOrNull() ?: fromPos
        if (hypot(pt.x - prev.x, pt.y - prev.y) > 0.004f) {
            cleaned.add(pt)
        }
    }
    return cleaned
}

@Composable
fun AiOfficeLiveDialog(
    newsList: List<NewsItem>,
    isScanning: Boolean,
    onRunScan: () -> Unit,
    onDismiss: () -> Unit
) {
    val primary = MaterialTheme.colorScheme.primary

    // 6 индивидуальных кабинетов + персональные полосы в коридоре (corridorLaneY), чтобы агенты не сталкивались
    val agents = remember {
        listOf(
            OfficeAgentProfile(
                id = 0, name = "Алиса", role = "СМИ и RSS", roomTitle = "Кабинет 1 • СМИ",
                accentColor = Color(0xFF10B981), hairColor = Color(0xFF78350F), isTopRow = true,
                deskChairPos = Offset(0.17f, 0.21f), roomDoorPos = Offset(0.17f, 0.32f), corridorEntryPos = Offset(0.17f, 0.365f),
                corridorLaneY = 0.365f,
                diningSeatPos = Offset(0.11f, 0.57f), loungeSeatPos = Offset(0.65f, 0.59f)
            ),
            OfficeAgentProfile(
                id = 1, name = "Макс", role = "Telegram", roomTitle = "Кабинет 2 • TG",
                accentColor = Color(0xFF38BDF8), hairColor = Color(0xFF1E293B), isTopRow = true,
                deskChairPos = Offset(0.50f, 0.21f), roomDoorPos = Offset(0.50f, 0.32f), corridorEntryPos = Offset(0.50f, 0.365f),
                corridorLaneY = 0.405f,
                diningSeatPos = Offset(0.22f, 0.57f), loungeSeatPos = Offset(0.75f, 0.59f)
            ),
            OfficeAgentProfile(
                id = 2, name = "Инста", role = "Instagram", roomTitle = "Кабинет 3 • IG",
                accentColor = Color(0xFFF43F5E), hairColor = Color(0xFF9F1239), isTopRow = true,
                deskChairPos = Offset(0.83f, 0.21f), roomDoorPos = Offset(0.83f, 0.32f), corridorEntryPos = Offset(0.83f, 0.365f),
                corridorLaneY = 0.445f,
                diningSeatPos = Offset(0.33f, 0.57f), loungeSeatPos = Offset(0.85f, 0.59f)
            ),
            OfficeAgentProfile(
                id = 3, name = "Авто-ИИ", role = "Электроавто", roomTitle = "Кабинет 4 • Авто",
                accentColor = Color(0xFF22D3EE), hairColor = Color(0xFF0F172A), isTopRow = false,
                deskChairPos = Offset(0.17f, 0.79f), roomDoorPos = Offset(0.17f, 0.68f), corridorEntryPos = Offset(0.17f, 0.635f),
                corridorLaneY = 0.635f,
                diningSeatPos = Offset(0.14f, 0.57f), loungeSeatPos = Offset(0.68f, 0.59f)
            ),
            OfficeAgentProfile(
                id = 4, name = "Дом-Бот", role = "ТВ и Дом", roomTitle = "Кабинет 5 • ТВ/Дом",
                accentColor = Color(0xFFF59E0B), hairColor = Color(0xFF451A03), isTopRow = false,
                deskChairPos = Offset(0.50f, 0.79f), roomDoorPos = Offset(0.50f, 0.68f), corridorEntryPos = Offset(0.50f, 0.635f),
                corridorLaneY = 0.595f,
                diningSeatPos = Offset(0.25f, 0.57f), loungeSeatPos = Offset(0.78f, 0.59f)
            ),
            OfficeAgentProfile(
                id = 5, name = "Нова", role = "Ноутбуки/ПК", roomTitle = "Кабинет 6 • ПК",
                accentColor = Color(0xFFA78BFA), hairColor = Color(0xFF312E81), isTopRow = false,
                deskChairPos = Offset(0.83f, 0.79f), roomDoorPos = Offset(0.83f, 0.68f), corridorEntryPos = Offset(0.83f, 0.635f),
                corridorLaneY = 0.555f,
                diningSeatPos = Offset(0.36f, 0.57f), loungeSeatPos = Offset(0.88f, 0.59f)
            )
        )
    }

    var ticketCounter by remember { mutableIntStateOf(115) }
    var selectedAgentId by remember { mutableIntStateOf(0) }

    val agentStatuses = remember {
        mutableStateMapOf<Int, AgentLiveStatus>().apply {
            agents.forEachIndexed { idx, ag ->
                val pastTask = createSoloAgentTask(
                    ticketNumber = 100 + idx,
                    agentId = ag.id,
                    newsList = newsList
                ).copy(completedAt = ZonedDateTime.now().minusMinutes(2).format(DateTimeFormatter.ofPattern("HH:mm:ss")))

                val activeTask = createSoloAgentTask(
                    ticketNumber = 108 + idx,
                    agentId = ag.id,
                    newsList = newsList
                )
                val startZone = when (idx) {
                    2 -> AgentLocationZone.LOUNGE
                    4 -> AgentLocationZone.DINING
                    else -> AgentLocationZone.DESK
                }
                put(
                    ag.id,
                    AgentLiveStatus(
                        currentTask = activeTask,
                        progress = 0.18f + (idx * 0.11f),
                        targetZone = startZone,
                        breakTicksRemaining = if (startZone != AgentLocationZone.DESK) 40 else 0,
                        completedHistory = listOf(pastTask)
                    )
                )
            }
        }
    }

    val agentPositions = remember {
        mutableStateMapOf<Int, Offset>().apply {
            agents.forEach { ag ->
                val zone = agentStatuses[ag.id]?.targetZone ?: AgentLocationZone.DESK
                put(
                    ag.id,
                    when (zone) {
                        AgentLocationZone.DESK -> ag.deskChairPos
                        AgentLocationZone.DINING -> ag.diningSeatPos
                        AgentLocationZone.LOUNGE -> ag.loungeSeatPos
                    }
                )
            }
        }
    }

    // Персональная очередь маршрутных точек для каждого агента — пройденные точки удаляются!
    val agentPathQueues = remember {
        mutableStateMapOf<Int, List<Offset>>().apply {
            agents.forEach { put(it.id, emptyList()) }
        }
    }

    val agentWalkingFlags = remember {
        mutableStateMapOf<Int, Boolean>().apply {
            agents.forEach { put(it.id, false) }
        }
    }

    LaunchedEffect(isScanning, newsList.size) {
        while (true) {
            delay(75L)
            val progressStep = if (isScanning) 0.0085f else 0.0042f

            for (ag in agents) {
                val status = agentStatuses[ag.id] ?: continue
                val curPos = agentPositions[ag.id] ?: ag.deskChairPos
                val queue = agentPathQueues[ag.id].orEmpty()

                // Если есть точки в маршруте — шагаем строго по ним, удаляя пройденные
                if (queue.isNotEmpty()) {
                    val targetWp = queue.first()
                    val dx = targetWp.x - curPos.x
                    val dy = targetWp.y - curPos.y
                    val dist = hypot(dx, dy)
                    val stepSize = 0.014f

                    if (dist <= stepSize) {
                        agentPositions[ag.id] = targetWp
                        val remaining = queue.drop(1)
                        agentPathQueues[ag.id] = remaining
                        agentWalkingFlags[ag.id] = remaining.isNotEmpty()
                    } else {
                        agentPositions[ag.id] = Offset(
                            curPos.x + (dx / dist) * stepSize,
                            curPos.y + (dy / dist) * stepSize
                        )
                        agentWalkingFlags[ag.id] = true
                    }
                    continue
                }

                agentWalkingFlags[ag.id] = false

                // Агент дошёл до места и выполняет действие (отдыхает или работает за монитором)
                if (status.targetZone != AgentLocationZone.DESK) {
                    if (status.breakTicksRemaining > 1) {
                        agentStatuses[ag.id] = status.copy(breakTicksRemaining = status.breakTicksRemaining - 1)
                    } else {
                        ticketCounter += 1
                        val nextTask = createSoloAgentTask(
                            ticketNumber = ticketCounter,
                            agentId = ag.id,
                            newsList = newsList
                        )
                        agentStatuses[ag.id] = status.copy(
                            currentTask = nextTask,
                            progress = 0.02f,
                            targetZone = AgentLocationZone.DESK,
                            breakTicksRemaining = 0
                        )
                        // Строим маршрут от столовой/лаунжа обратно в свой кабинет через дверь
                        agentPathQueues[ag.id] = buildCleanPath(curPos, ag, AgentLocationZone.DESK)
                    }
                } else {
                    val nextProg = status.progress + progressStep
                    if (nextProg >= 1.0f) {
                        val doneTime = ZonedDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"))
                        val finishedTask = status.currentTask.copy(completedAt = doneTime)

                        val restingCount = agentStatuses.values.count { it.targetZone != AgentLocationZone.DESK }
                        val takeBreak = !isScanning && restingCount < 2 && ((ticketCounter + ag.id) % 3 == 0)

                        if (takeBreak) {
                            val chosenZone = if ((ticketCounter + ag.id) % 2 == 0) AgentLocationZone.DINING else AgentLocationZone.LOUNGE
                            agentStatuses[ag.id] = AgentLiveStatus(
                                currentTask = status.currentTask,
                                progress = 1.0f,
                                targetZone = chosenZone,
                                breakTicksRemaining = 110,
                                completedHistory = (listOf(finishedTask) + status.completedHistory).take(6)
                            )
                            // Строим маршрут из кабинета через дверь в столовую или комнату отдыха
                            agentPathQueues[ag.id] = buildCleanPath(curPos, ag, chosenZone)
                        } else {
                            ticketCounter += 1
                            val nextTask = createSoloAgentTask(
                                ticketNumber = ticketCounter,
                                agentId = ag.id,
                                newsList = newsList
                            )
                            agentStatuses[ag.id] = AgentLiveStatus(
                                currentTask = nextTask,
                                progress = 0.02f,
                                targetZone = AgentLocationZone.DESK,
                                breakTicksRemaining = 0,
                                completedHistory = (listOf(finishedTask) + status.completedHistory).take(6)
                            )
                        }
                    } else {
                        agentStatuses[ag.id] = status.copy(progress = nextProg)
                    }
                }
            }
        }
    }

    val inspectedAgent = agents[selectedAgentId]
    val inspectedStatus = agentStatuses[selectedAgentId]

    val infiniteTransition = rememberInfiniteTransition(label = "officeAnimations")
    val walkSwing by infiniteTransition.animateFloat(
        initialValue = -1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 280, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "legSwing"
    )
    val typingHands by infiniteTransition.animateFloat(
        initialValue = -1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 360, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "handsType"
    )
    val screenScanPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "monitorLines"
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 12.dp),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.2.dp, primary.copy(alpha = 0.35f))
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "ИИ-Кабинет",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(Modifier.width(8.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFF10B981).copy(alpha = 0.18f)
                            ) {
                                Text(
                                    text = if (isScanning) "СБОР НОВОСТЕЙ" else "РАБОЧИЙ РЕЖИМ",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF10B981),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            "Нажмите на любого сотрудника или кабинет, чтобы посмотреть экран",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Закрыть")
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(410.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(Color(0xFF111827))
                        .border(1.4.dp, primary.copy(alpha = 0.35f), RoundedCornerShape(18.dp))
                        .pointerInput(Unit) {
                            detectTapGestures { tap ->
                                val w = size.width.toFloat()
                                val h = size.height.toFloat()
                                val tapped = agents.minByOrNull { ag ->
                                    val pos = agentPositions[ag.id] ?: ag.deskChairPos
                                    val dx = tap.x - pos.x * w
                                    val dy = tap.y - pos.y * h
                                    dx * dx + dy * dy
                                }
                                if (tapped != null) {
                                    selectedAgentId = tapped.id
                                }
                            }
                        }
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val w = size.width
                        val h = size.height

                        drawRect(color = Color(0xFF1E2433), size = size)

                        val hallTop = h * 0.32f
                        val hallBottom = h * 0.68f
                        drawRect(
                            color = Color(0xFF151C28),
                            topLeft = Offset(0f, hallTop),
                            size = Size(w, hallBottom - hallTop)
                        )

                        val tileStep = 18.dp.toPx()
                        var gx = tileStep
                        while (gx < w) {
                            drawLine(Color.White.copy(alpha = 0.04f), Offset(gx, hallTop), Offset(gx, hallBottom), 1f)
                            gx += tileStep
                        }
                        var gy = hallTop + tileStep
                        while (gy < hallBottom) {
                            drawLine(Color.White.copy(alpha = 0.04f), Offset(0f, gy), Offset(w, gy), 1f)
                            gy += tileStep
                        }

                        drawLine(
                            color = Color(0xFF38BDF8).copy(alpha = 0.16f),
                            start = Offset(w * 0.06f, h * 0.50f),
                            end = Offset(w * 0.94f, h * 0.50f),
                            strokeWidth = 1.5.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 10f), 0f)
                        )

                        val wallColor = Color(0xFF475569)
                        val wallThickness = 3.dp.toPx()
                        val colWidth = w / 3f

                        for (col in 0..2) {
                            val left = col * colWidth
                            val right = (col + 1) * colWidth
                            val doorCenter = left + colWidth / 2f
                            val doorHalf = 19.dp.toPx()

                            val topAgent = agents[col]
                            val isTopSel = topAgent.id == selectedAgentId
                            drawRect(
                                color = topAgent.accentColor.copy(alpha = if (isTopSel) 0.13f else 0.04f),
                                topLeft = Offset(left + 2.dp.toPx(), 2.dp.toPx()),
                                size = Size(colWidth - 4.dp.toPx(), hallTop - 4.dp.toPx())
                            )
                            drawRect(
                                color = Color(0xFF38BDF8).copy(alpha = 0.45f),
                                topLeft = Offset(left + colWidth * 0.25f, 1.dp.toPx()),
                                size = Size(colWidth * 0.50f, 3.5.dp.toPx())
                            )
                            drawLine(wallColor, Offset(right, 0f), Offset(right, hallTop), wallThickness)
                            drawLine(wallColor, Offset(left, hallTop), Offset(doorCenter - doorHalf, hallTop), wallThickness)
                            drawLine(wallColor, Offset(doorCenter + doorHalf, hallTop), Offset(right, hallTop), wallThickness)
                            drawLine(topAgent.accentColor.copy(alpha = 0.45f), Offset(doorCenter - doorHalf, hallTop), Offset(doorCenter + doorHalf, hallTop), 1.5.dp.toPx())

                            val botAgent = agents[col + 3]
                            val isBotSel = botAgent.id == selectedAgentId
                            drawRect(
                                color = botAgent.accentColor.copy(alpha = if (isBotSel) 0.13f else 0.04f),
                                topLeft = Offset(left + 2.dp.toPx(), hallBottom + 2.dp.toPx()),
                                size = Size(colWidth - 4.dp.toPx(), h - hallBottom - 4.dp.toPx())
                            )
                            drawRect(
                                color = Color(0xFF38BDF8).copy(alpha = 0.45f),
                                topLeft = Offset(left + colWidth * 0.25f, h - 4.5.dp.toPx()),
                                size = Size(colWidth * 0.50f, 3.5.dp.toPx())
                            )
                            drawLine(wallColor, Offset(right, hallBottom), Offset(right, h), wallThickness)
                            drawLine(wallColor, Offset(left, hallBottom), Offset(doorCenter - doorHalf, hallBottom), wallThickness)
                            drawLine(wallColor, Offset(doorCenter + doorHalf, hallBottom), Offset(right, hallBottom), wallThickness)
                            drawLine(botAgent.accentColor.copy(alpha = 0.45f), Offset(doorCenter - doorHalf, hallBottom), Offset(doorCenter + doorHalf, hallBottom), 1.5.dp.toPx())
                        }

                        // Зона столовой слева (x: 0.03..0.42, y: 0.43..0.64) — проход сверху свободен
                        val diningRect = Offset(w * 0.03f, h * 0.43f)
                        val diningSize = Size(w * 0.39f, h * 0.21f)
                        drawRoundRect(
                            color = Color(0xFF451A03).copy(alpha = 0.38f),
                            topLeft = diningRect,
                            size = diningSize,
                            cornerRadius = CornerRadius(10.dp.toPx(), 10.dp.toPx())
                        )
                        drawRoundRect(
                            color = Color(0xFFF59E0B).copy(alpha = 0.32f),
                            topLeft = diningRect,
                            size = diningSize,
                            cornerRadius = CornerRadius(10.dp.toPx(), 10.dp.toPx()),
                            style = Stroke(width = 1.dp.toPx())
                        )
                        drawRoundRect(
                            color = Color(0xFF1E293B),
                            topLeft = Offset(diningRect.x + 5.dp.toPx(), diningRect.y + 8.dp.toPx()),
                            size = Size(12.dp.toPx(), diningSize.height - 16.dp.toPx()),
                            cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx())
                        )
                        drawCircle(Color(0xFFEF4444), 2.5.dp.toPx(), Offset(diningRect.x + 11.dp.toPx(), diningRect.y + 18.dp.toPx()))
                        drawCircle(Color(0xFF10B981), 2.5.dp.toPx(), Offset(diningRect.x + 11.dp.toPx(), diningRect.y + 28.dp.toPx()))

                        val dTableX = diningRect.x + 24.dp.toPx()
                        val dTableY = diningRect.y + diningSize.height * 0.24f
                        val dTableW = diningSize.width - 32.dp.toPx()
                        val dTableH = 16.dp.toPx()
                        drawRoundRect(
                            color = Color(0xFF92400E),
                            topLeft = Offset(dTableX, dTableY),
                            size = Size(dTableW, dTableH),
                            cornerRadius = CornerRadius(5.dp.toPx(), 5.dp.toPx())
                        )
                        drawCircle(Color.White.copy(alpha = 0.85f), 2.8.dp.toPx(), Offset(dTableX + dTableW * 0.22f, dTableY + dTableH * 0.5f))
                        drawCircle(Color.White.copy(alpha = 0.85f), 2.8.dp.toPx(), Offset(dTableX + dTableW * 0.52f, dTableY + dTableH * 0.5f))
                        drawCircle(Color.White.copy(alpha = 0.85f), 2.8.dp.toPx(), Offset(dTableX + dTableW * 0.82f, dTableY + dTableH * 0.5f))

                        // Комната отдыха справа (x: 0.58..0.97, y: 0.43..0.64) — проход сверху свободен
                        val loungeRect = Offset(w * 0.58f, h * 0.43f)
                        val loungeSize = Size(w * 0.39f, h * 0.21f)
                        drawRoundRect(
                            color = Color(0xFF1E1B4B).copy(alpha = 0.42f),
                            topLeft = loungeRect,
                            size = loungeSize,
                            cornerRadius = CornerRadius(10.dp.toPx(), 10.dp.toPx())
                        )
                        drawRoundRect(
                            color = Color(0xFF818CF8).copy(alpha = 0.32f),
                            topLeft = loungeRect,
                            size = loungeSize,
                            cornerRadius = CornerRadius(10.dp.toPx(), 10.dp.toPx()),
                            style = Stroke(width = 1.dp.toPx())
                        )
                        val tvLeft = loungeRect.x + loungeSize.width * 0.18f
                        val tvWidth = loungeSize.width * 0.64f
                        val tvTop = loungeRect.y + 6.dp.toPx()
                        drawRoundRect(
                            color = Color(0xFF090D16),
                            topLeft = Offset(tvLeft, tvTop),
                            size = Size(tvWidth, 7.dp.toPx()),
                            cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
                        )
                        drawLine(
                            color = Color(0xFF38BDF8),
                            start = Offset(tvLeft + 3.dp.toPx(), tvTop + 7.dp.toPx()),
                            end = Offset(tvLeft + tvWidth - 3.dp.toPx(), tvTop + 7.dp.toPx()),
                            strokeWidth = 2.5.dp.toPx()
                        )
                        drawRoundRect(
                            color = Color(0xFF334155),
                            topLeft = Offset(loungeRect.x + loungeSize.width * 0.32f, loungeRect.y + loungeSize.height * 0.32f),
                            size = Size(loungeSize.width * 0.36f, 8.dp.toPx()),
                            cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx())
                        )
                        drawRoundRect(
                            color = Color(0xFF4338CA),
                            topLeft = Offset(loungeRect.x + 12.dp.toPx(), loungeRect.y + loungeSize.height * 0.62f),
                            size = Size(loungeSize.width - 24.dp.toPx(), 17.dp.toPx()),
                            cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                        )

                        agents.forEach { ag ->
                            val status = agentStatuses[ag.id]
                            val isAtDesk = status?.targetZone == AgentLocationZone.DESK && !(agentWalkingFlags[ag.id] ?: false)
                            drawDetailedWorkstation(
                                chairAnchor = Offset(ag.deskChairPos.x * w, ag.deskChairPos.y * h),
                                isTopRow = ag.isTopRow,
                                accentColor = ag.accentColor,
                                isActiveWork = isAtDesk,
                                progress = status?.progress ?: 0.5f,
                                scanPhase = screenScanPhase
                            )
                        }

                        agents.forEach { ag ->
                            val pos = agentPositions[ag.id] ?: ag.deskChairPos
                            val isWalking = agentWalkingFlags[ag.id] ?: false
                            val status = agentStatuses[ag.id]
                            val zone = status?.targetZone ?: AgentLocationZone.DESK

                            drawDetailedHumanAgent(
                                center = Offset(pos.x * w, pos.y * h),
                                isTopRow = ag.isTopRow,
                                zone = zone,
                                isWalking = isWalking,
                                shirtColor = ag.accentColor,
                                hairColor = ag.hairColor,
                                walkPhase = walkSwing,
                                typePhase = typingHands,
                                isSelected = ag.id == selectedAgentId
                            )
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.TopCenter)
                            .padding(top = 6.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        agents.take(3).forEach { ag ->
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(ag.name, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, color = ag.accentColor)
                                Text(ag.role, fontSize = 8.sp, color = Color.White.copy(alpha = 0.72f))
                            }
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.Center)
                            .padding(horizontal = 18.dp, vertical = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "Столовая",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFCD34D),
                            modifier = Modifier.padding(top = 56.dp)
                        )
                        Text(
                            "Комната отдыха • ТВ",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFA5B4FC),
                            modifier = Modifier.padding(top = 56.dp)
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 6.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        agents.drop(3).forEach { ag ->
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(ag.name, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, color = ag.accentColor)
                                Text(ag.role, fontSize = 8.sp, color = Color.White.copy(alpha = 0.72f))
                            }
                        }
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    agents.chunked(3).forEach { rowAgents ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            rowAgents.forEach { ag ->
                                val isSel = selectedAgentId == ag.id
                                val st = agentStatuses[ag.id]
                                val isWalking = agentWalkingFlags[ag.id] ?: false
                                val stateLabel = when {
                                    isWalking -> "Идёт по коридору"
                                    st?.targetZone == AgentLocationZone.DINING -> "В столовой"
                                    st?.targetZone == AgentLocationZone.LOUNGE -> "У телевизора"
                                    else -> "За ПК • ${((st?.progress ?: 0f) * 100).toInt()}%"
                                }
                                Surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(10.dp))
                                        .clickable { selectedAgentId = ag.id },
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isSel) ag.accentColor.copy(alpha = 0.2f)
                                    else MaterialTheme.colorScheme.background.copy(alpha = 0.65f),
                                    border = BorderStroke(
                                        width = if (isSel) 1.4.dp else 1.dp,
                                        color = if (isSel) ag.accentColor else ag.accentColor.copy(alpha = 0.25f)
                                    )
                                ) {
                                    Column(
                                        modifier = Modifier.padding(vertical = 6.dp, horizontal = 4.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = ag.name,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSel) ag.accentColor else MaterialTheme.colorScheme.onSurface,
                                            maxLines = 1
                                        )
                                        Text(
                                            text = stateLabel,
                                            fontSize = 9.sp,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.68f),
                                            maxLines = 1
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                if (inspectedStatus != null) {
                    val isWalking = agentWalkingFlags[selectedAgentId] ?: false
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = inspectedAgent.accentColor.copy(alpha = 0.1f),
                        border = BorderStroke(1.dp, inspectedAgent.accentColor.copy(alpha = 0.45f))
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${inspectedAgent.name} • ${inspectedAgent.roomTitle}",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = inspectedAgent.accentColor
                                )
                                Text(
                                    text = when {
                                        isWalking -> "Идёт по коридору"
                                        inspectedStatus.targetZone != AgentLocationZone.DESK -> inspectedStatus.targetZone.label
                                        else -> "Задача #${inspectedStatus.currentTask.ticketNumber} • ${(inspectedStatus.progress * 100).toInt()}%"
                                    },
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = inspectedAgent.accentColor
                                )
                            }

                            Text(
                                text = when {
                                    isWalking -> "Направляется по коридору: ${inspectedStatus.targetZone.label.lowercase()}."
                                    inspectedStatus.targetZone == AgentLocationZone.DINING -> "Перерыв на обед: пьёт кофе в столовой перед следующей проверкой источников."
                                    inspectedStatus.targetZone == AgentLocationZone.LOUNGE -> "Отдых в комнате отдыха: смотрит техно-канал по телевизору."
                                    else -> "На экране монитора: ${inspectedStatus.currentTask.title}"
                                },
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.92f)
                            )

                            if (inspectedStatus.targetZone == AgentLocationZone.DESK && !isWalking) {
                                LinearProgressIndicator(
                                    progress = { inspectedStatus.progress.coerceIn(0f, 1f) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(CircleShape),
                                    color = inspectedAgent.accentColor,
                                    trackColor = inspectedAgent.accentColor.copy(alpha = 0.2f)
                                )
                            }

                            HorizontalDivider(color = inspectedAgent.accentColor.copy(alpha = 0.2f))

                            Text(
                                text = "Самостоятельно выполнено (${inspectedStatus.completedHistory.size}):",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                            )

                            inspectedStatus.completedHistory.take(3).forEach { done ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.Top,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = Color(0xFF10B981),
                                        modifier = Modifier
                                            .size(14.dp)
                                            .padding(top = 2.dp)
                                    )
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "#${done.ticketNumber} ${done.resultSummary}",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.88f)
                                        )
                                        Text(
                                            text = "Завершено в ${done.completedAt}",
                                            fontSize = 9.sp,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

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
                        text = if (isScanning) "Агенты собирают свежие новости..." else "Обновить новости по всем кабинетам",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

private fun DrawScope.drawDetailedWorkstation(
    chairAnchor: Offset,
    isTopRow: Boolean,
    accentColor: Color,
    isActiveWork: Boolean,
    progress: Float,
    scanPhase: Float
) {
    val dir = if (isTopRow) -1f else 1f
    val deskW = 58.dp.toPx()
    val deskH = 22.dp.toPx()
    val deskCenterY = chairAnchor.y + dir * 18.dp.toPx()

    drawCircle(
        color = Color(0xFF334155),
        radius = 8.5.dp.toPx(),
        center = chairAnchor
    )
    drawRoundRect(
        color = Color(0xFF475569),
        topLeft = Offset(chairAnchor.x - 9.dp.toPx(), chairAnchor.y - dir * 6.dp.toPx() - 2.dp.toPx()),
        size = Size(18.dp.toPx(), 4.dp.toPx()),
        cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
    )

    val plantX = chairAnchor.x + 33.dp.toPx()
    val plantY = deskCenterY + dir * 2.dp.toPx()
    drawCircle(Color(0xFF78350F), 4.5.dp.toPx(), Offset(plantX, plantY))
    drawCircle(Color(0xFF059669), 5.5.dp.toPx(), Offset(plantX - 1.5.dp.toPx(), plantY - 1.5.dp.toPx()))
    drawCircle(Color(0xFF10B981), 4.dp.toPx(), Offset(plantX + 2.dp.toPx(), plantY + 1.dp.toPx()))

    val deskTopLeft = Offset(chairAnchor.x - deskW / 2f, deskCenterY - deskH / 2f)
    drawRoundRect(
        color = Color(0xFF273142),
        topLeft = deskTopLeft,
        size = Size(deskW, deskH),
        cornerRadius = CornerRadius(5.dp.toPx(), 5.dp.toPx())
    )
    drawRoundRect(
        color = Color(0xFF64748B).copy(alpha = 0.55f),
        topLeft = deskTopLeft,
        size = Size(deskW, deskH),
        cornerRadius = CornerRadius(5.dp.toPx(), 5.dp.toPx()),
        style = Stroke(width = 1.dp.toPx())
    )

    drawRoundRect(
        color = Color(0xFF0F172A),
        topLeft = Offset(chairAnchor.x + deskW / 2f - 10.dp.toPx(), deskCenterY - 7.dp.toPx()),
        size = Size(7.dp.toPx(), 14.dp.toPx()),
        cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
    )
    drawCircle(
        color = if (isActiveWork) Color(0xFF10B981) else Color(0xFFF59E0B),
        radius = 1.4.dp.toPx(),
        center = Offset(chairAnchor.x + deskW / 2f - 6.5.dp.toPx(), deskCenterY - 3.dp.toPx())
    )

    val kbY = deskCenterY - dir * 5.5.dp.toPx()
    drawRoundRect(
        color = Color(0xFF0F172A),
        topLeft = Offset(chairAnchor.x - 13.dp.toPx(), kbY - 2.2.dp.toPx()),
        size = Size(20.dp.toPx(), 4.4.dp.toPx()),
        cornerRadius = CornerRadius(1.5.dp.toPx(), 1.5.dp.toPx())
    )
    drawRoundRect(
        color = Color(0xFF94A3B8),
        topLeft = Offset(chairAnchor.x + 10.dp.toPx(), kbY - 1.8.dp.toPx()),
        size = Size(3.5.dp.toPx(), 4.5.dp.toPx()),
        cornerRadius = CornerRadius(1.5.dp.toPx(), 1.5.dp.toPx())
    )

    val monW = 30.dp.toPx()
    val monH = 11.dp.toPx()
    val monCenterY = deskCenterY + dir * 4.5.dp.toPx()
    val monTopLeft = Offset(chairAnchor.x - monW / 2f - 2.dp.toPx(), monCenterY - monH / 2f)

    drawRoundRect(
        color = Color(0xFF090D16),
        topLeft = monTopLeft,
        size = Size(monW, monH),
        cornerRadius = CornerRadius(2.5.dp.toPx(), 2.5.dp.toPx())
    )
    drawRoundRect(
        color = if (isActiveWork) accentColor else Color(0xFF475569),
        topLeft = monTopLeft,
        size = Size(monW, monH),
        cornerRadius = CornerRadius(2.5.dp.toPx(), 2.5.dp.toPx()),
        style = Stroke(width = 1.dp.toPx())
    )

    val scrLeft = monTopLeft.x + 2.dp.toPx()
    val scrTop = monTopLeft.y + 2.dp.toPx()
    val scrW = monW - 4.dp.toPx()
    val scrH = monH - 4.dp.toPx()
    drawRect(
        color = if (isActiveWork) accentColor.copy(alpha = 0.16f) else Color(0xFF1E293B),
        topLeft = Offset(scrLeft, scrTop),
        size = Size(scrW, scrH)
    )

    if (isActiveWork) {
        val line1W = scrW * (0.35f + 0.25f * sin(scanPhase * 6.28f).let { abs(it) })
        val line2W = scrW * (0.28f + 0.30f * cos(scanPhase * 6.28f).let { abs(it) })
        drawLine(
            color = accentColor,
            start = Offset(scrLeft + 1.5.dp.toPx(), scrTop + 2.dp.toPx()),
            end = Offset(scrLeft + 1.5.dp.toPx() + line1W, scrTop + 2.dp.toPx()),
            strokeWidth = 1.3.dp.toPx(),
            cap = StrokeCap.Round
        )
        drawLine(
            color = Color.White.copy(alpha = 0.85f),
            start = Offset(scrLeft + 1.5.dp.toPx(), scrTop + 4.2.dp.toPx()),
            end = Offset(scrLeft + 1.5.dp.toPx() + line2W, scrTop + 4.2.dp.toPx()),
            strokeWidth = 1.3.dp.toPx(),
            cap = StrokeCap.Round
        )
        drawLine(
            color = Color(0xFF34D399),
            start = Offset(scrLeft + 1.5.dp.toPx(), scrTop + scrH - 1.2.dp.toPx()),
            end = Offset(scrLeft + 1.5.dp.toPx() + (scrW - 3.dp.toPx()) * progress.coerceIn(0.1f, 1f), scrTop + scrH - 1.2.dp.toPx()),
            strokeWidth = 1.4.dp.toPx(),
            cap = StrokeCap.Round
        )
    } else {
        drawCircle(
            color = Color(0xFFF59E0B).copy(alpha = 0.75f),
            radius = 1.6.dp.toPx(),
            center = Offset(scrLeft + scrW / 2f, scrTop + scrH / 2f)
        )
    }
}

private fun DrawScope.drawDetailedHumanAgent(
    center: Offset,
    isTopRow: Boolean,
    zone: AgentLocationZone,
    isWalking: Boolean,
    shirtColor: Color,
    hairColor: Color,
    walkPhase: Float,
    typePhase: Float,
    isSelected: Boolean
) {
    if (isSelected) {
        drawCircle(
            color = shirtColor.copy(alpha = 0.24f),
            radius = 18.dp.toPx(),
            center = center
        )
        drawCircle(
            color = shirtColor.copy(alpha = 0.75f),
            radius = 18.dp.toPx(),
            center = center,
            style = Stroke(width = 1.2.dp.toPx())
        )
    }

    val skinColor = Color(0xFFFDE68A)
    val pantsColor = Color(0xFF1E293B)
    val shoeColor = Color(0xFF090D16)

    if (isWalking) {
        val leftStep = walkPhase * 4.5.dp.toPx()
        val rightStep = -walkPhase * 4.5.dp.toPx()

        drawLine(
            color = pantsColor,
            start = Offset(center.x - 3.dp.toPx(), center.y + 4.dp.toPx()),
            end = Offset(center.x - 3.5.dp.toPx(), center.y + 11.dp.toPx() + leftStep),
            strokeWidth = 3.2.dp.toPx(),
            cap = StrokeCap.Round
        )
        drawCircle(shoeColor, 2.dp.toPx(), Offset(center.x - 3.5.dp.toPx(), center.y + 11.5.dp.toPx() + leftStep))

        drawLine(
            color = pantsColor,
            start = Offset(center.x + 3.dp.toPx(), center.y + 4.dp.toPx()),
            end = Offset(center.x + 3.5.dp.toPx(), center.y + 11.dp.toPx() + rightStep),
            strokeWidth = 3.2.dp.toPx(),
            cap = StrokeCap.Round
        )
        drawCircle(shoeColor, 2.dp.toPx(), Offset(center.x + 3.5.dp.toPx(), center.y + 11.5.dp.toPx() + rightStep))
    } else if (zone == AgentLocationZone.LOUNGE) {
        drawLine(
            color = pantsColor,
            start = Offset(center.x - 3.5.dp.toPx(), center.y + 3.dp.toPx()),
            end = Offset(center.x - 3.5.dp.toPx(), center.y + 8.dp.toPx()),
            strokeWidth = 3.2.dp.toPx(),
            cap = StrokeCap.Round
        )
        drawLine(
            color = pantsColor,
            start = Offset(center.x + 3.5.dp.toPx(), center.y + 3.dp.toPx()),
            end = Offset(center.x + 3.5.dp.toPx(), center.y + 8.dp.toPx()),
            strokeWidth = 3.2.dp.toPx(),
            cap = StrokeCap.Round
        )
    } else if (zone == AgentLocationZone.DINING) {
        drawLine(
            color = pantsColor,
            start = Offset(center.x - 3.dp.toPx(), center.y + 4.dp.toPx()),
            end = Offset(center.x - 3.dp.toPx(), center.y + 10.dp.toPx()),
            strokeWidth = 3.dp.toPx(),
            cap = StrokeCap.Round
        )
        drawLine(
            color = pantsColor,
            start = Offset(center.x + 3.dp.toPx(), center.y + 4.dp.toPx()),
            end = Offset(center.x + 3.dp.toPx(), center.y + 10.dp.toPx()),
            strokeWidth = 3.dp.toPx(),
            cap = StrokeCap.Round
        )
    }

    drawRoundRect(
        color = shirtColor,
        topLeft = Offset(center.x - 7.5.dp.toPx(), center.y - 4.5.dp.toPx()),
        size = Size(15.dp.toPx(), 10.dp.toPx()),
        cornerRadius = CornerRadius(4.5.dp.toPx(), 4.5.dp.toPx())
    )

    if (zone == AgentLocationZone.DESK && !isWalking) {
        val dir = if (isTopRow) -1f else 1f
        val kbReachY = center.y + dir * 12.5.dp.toPx()
        val shift = typePhase * 1.6.dp.toPx()
        drawLine(
            color = skinColor,
            start = Offset(center.x - 6.5.dp.toPx(), center.y + dir * 2.dp.toPx()),
            end = Offset(center.x - 4.dp.toPx(), kbReachY + shift),
            strokeWidth = 2.6.dp.toPx(),
            cap = StrokeCap.Round
        )
        drawLine(
            color = skinColor,
            start = Offset(center.x + 6.5.dp.toPx(), center.y + dir * 2.dp.toPx()),
            end = Offset(center.x + 4.dp.toPx(), kbReachY - shift),
            strokeWidth = 2.6.dp.toPx(),
            cap = StrokeCap.Round
        )
    } else if (isWalking) {
        val armSwing = -walkPhase * 3.dp.toPx()
        drawLine(
            color = skinColor,
            start = Offset(center.x - 7.5.dp.toPx(), center.y - 1.dp.toPx()),
            end = Offset(center.x - 8.5.dp.toPx(), center.y + 4.dp.toPx() + armSwing),
            strokeWidth = 2.5.dp.toPx(),
            cap = StrokeCap.Round
        )
        drawLine(
            color = skinColor,
            start = Offset(center.x + 7.5.dp.toPx(), center.y - 1.dp.toPx()),
            end = Offset(center.x + 8.5.dp.toPx(), center.y + 4.dp.toPx() - armSwing),
            strokeWidth = 2.5.dp.toPx(),
            cap = StrokeCap.Round
        )
    } else if (zone == AgentLocationZone.DINING) {
        drawLine(
            color = skinColor,
            start = Offset(center.x + 6.dp.toPx(), center.y),
            end = Offset(center.x + 9.5.dp.toPx(), center.y - 3.dp.toPx()),
            strokeWidth = 2.5.dp.toPx(),
            cap = StrokeCap.Round
        )
        drawCircle(Color(0xFFFBBF24), 2.4.dp.toPx(), Offset(center.x + 10.dp.toPx(), center.y - 3.5.dp.toPx()))
    }

    val headCenter = Offset(center.x, center.y - 7.dp.toPx())
    drawCircle(
        color = skinColor,
        radius = 5.2.dp.toPx(),
        center = headCenter
    )
    drawArc(
        color = hairColor,
        startAngle = 180f,
        sweepAngle = 180f,
        useCenter = true,
        topLeft = Offset(headCenter.x - 5.2.dp.toPx(), headCenter.y - 5.2.dp.toPx()),
        size = Size(10.4.dp.toPx(), 10.4.dp.toPx())
    )
    val lookingBack = zone == AgentLocationZone.DESK && isTopRow && !isWalking
    if (!lookingBack) {
        drawCircle(Color(0xFF0F172A), 0.9.dp.toPx(), Offset(headCenter.x - 1.8.dp.toPx(), headCenter.y + 0.5.dp.toPx()))
        drawCircle(Color(0xFF0F172A), 0.9.dp.toPx(), Offset(headCenter.x + 1.8.dp.toPx(), headCenter.y + 0.5.dp.toPx()))
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
        "Instagram" -> Color(0xFFF43F5E)
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
