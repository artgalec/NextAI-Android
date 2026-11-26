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
import androidx.compose.material.icons.filled.ArrowForward
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
import androidx.compose.ui.draw.shadow
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
import kotlin.math.atan2
import kotlin.math.cos
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

data class AiAgentNode(
    val id: Int,
    val name: String,
    val role: String,
    val roomTitle: String,
    val accentColor: Color,
    val rx: Float,
    val ry: Float,
    val deskAbove: Boolean
)

data class AgentTaskRecord(
    val ticketNumber: Int,
    val fromAgentId: Int,
    val toAgentId: Int,
    val badge: String,
    val title: String,
    val resultSummary: String,
    val createdAt: String,
    val completedAt: String = ""
)

data class AgentWorkState(
    val activeTask: AgentTaskRecord,
    val progress: Float,
    val completedTasks: List<AgentTaskRecord>
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
        // Instagram-ленты брендов через RSS-мосты
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

    // Фильтр: оставляем строго технику (смартфоны, ТВ, ноутбуки, ПК, умный дом, электрокары), отбрасываем космос и оффтоп
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

    // Компактная выезжающая панель фильтров (убирает громоздкие блоки с главной страницы)
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

                // Переключатель свежести
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

                // Источники (Платформы)
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

                // Бренды и категории техники
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
                            // Компактная кнопка поиска
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

                            // Компактная кнопка фильтров (бренды и источники)
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

                            // Кнопка обновления
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

                // Выдвижная строка поиска только по нажатию на кнопку лупы
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
                    bottom = innerPadding.calculateBottomPadding() + 16.dp
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
                    "Персонализация тем, шрифта, карточек и офис из 8 ИИ-агентов",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.65f)
                )
            }
        }

        // Интерактивный ИИ-Офис (8 агентов)
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
                                    "ИИ-Офис (8 агентов)",
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
                                "СМИ, Telegram, Instagram, ТВ, Ноутбуки, Электрокары",
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
                            "Войти",
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

private fun generateFreshAgentTask(
    ticketNumber: Int,
    fromId: Int,
    toId: Int,
    newsList: List<NewsItem>
): AgentTaskRecord {
    val nowStr = ZonedDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"))
    val sampleNews = if (newsList.isNotEmpty()) {
        newsList[(ticketNumber * 3) % newsList.size]
    } else null

    val brandPool = listOf("Xiaomi", "Samsung", "Huawei", "Apple", "HONOR", "OnePlus", "Google", "realme", "Электроавто", "Умный дом и ТВ")
    val brand = sampleNews?.brand?.takeIf { it != "Общее" } ?: brandPool[ticketNumber % brandPool.size]
    val source = sampleNews?.sourceLabel ?: listOf("4PDA", "Rozetked", "iXBT", "Telegram", "Instagram")[ticketNumber % 5]
    val headlineHint = sampleNews?.title?.take(42)?.let { "«$it...»" } ?: "новинкам $brand"

    return when (toId) {
        0 -> AgentTaskRecord(
            ticketNumber = ticketNumber,
            fromAgentId = fromId,
            toAgentId = 0,
            badge = "СМИ • $brand",
            title = "Просканировать RSS-ленты $source по смартфонам и гаджетам $brand ($headlineHint)",
            resultSummary = "Собрано ${3 + (ticketNumber % 5)} свежих статей о технике $brand из $source",
            createdAt = nowStr
        )
        1 -> AgentTaskRecord(
            ticketNumber = ticketNumber,
            fromAgentId = fromId,
            toAgentId = 1,
            badge = "TG • $brand",
            title = "Проверить утечки и посты про смартфоны $brand в Telegram-каналах ($headlineHint)",
            resultSummary = "Найдено ${2 + (ticketNumber % 4)} эксклюзивных поста в Telegram по теме $brand",
            createdAt = nowStr
        )
        2 -> AgentTaskRecord(
            ticketNumber = ticketNumber,
            fromAgentId = fromId,
            toAgentId = 2,
            badge = "Instagram • $brand",
            title = "Проверить официальные тизеры и рилсы $brand в Instagram по новинке $headlineHint",
            resultSummary = "Загружены свежие анонсы из Instagram-аккаунта $brand без репостов",
            createdAt = nowStr
        )
        3 -> AgentTaskRecord(
            ticketNumber = ticketNumber,
            fromAgentId = fromId,
            toAgentId = 3,
            badge = "Электрокары",
            title = "Собрать новости про электромобили Xiaomi SU7/YU7, Zeekr, BYD и автопилоты",
            resultSummary = "Отобрано ${2 + (ticketNumber % 3)} новых обзора по электрокарам и батареям",
            createdAt = nowStr
        )
        4 -> AgentTaskRecord(
            ticketNumber = ticketNumber,
            fromAgentId = fromId,
            toAgentId = 4,
            badge = "ТВ и Дом",
            title = "Найти анонсы OLED/Mini-LED телевизоров, роботов-пылесосов и устройств умного дома",
            resultSummary = "Добавлены свежие релизы телевизоров и гаджетов умного дома",
            createdAt = nowStr
        )
        5 -> AgentTaskRecord(
            ticketNumber = ticketNumber,
            fromAgentId = fromId,
            toAgentId = 5,
            badge = "Ноутбуки и ПК",
            title = "Проверить релизы ноутбуков MacBook, MateBook, MagicBook и новых видеокарт/процессоров",
            resultSummary = "Собраны характеристики и цены ${2 + (ticketNumber % 4)} новых ноутбуков",
            createdAt = nowStr
        )
        6 -> AgentTaskRecord(
            ticketNumber = ticketNumber,
            fromAgentId = fromId,
            toAgentId = 6,
            badge = "Анти-шум",
            title = "Отфильтровать космос, оффтоп и дубликаты по $brand в материале $headlineHint",
            resultSummary = "Отсеян нетехнический шум, оставлены только новости о железе и гаджетах",
            createdAt = nowStr
        )
        else -> AgentTaskRecord(
            ticketNumber = ticketNumber,
            fromAgentId = fromId,
            toAgentId = 7,
            badge = "Сводка • $brand",
            title = "Сформировать краткую суть новости $headlineHint для карточки в общей ленте",
            resultSummary = "Готова краткая выжимка и обновлена карточка $brand в ленте",
            createdAt = nowStr
        )
    }
}

@Composable
fun AiOfficeLiveDialog(
    newsList: List<NewsItem>,
    isScanning: Boolean,
    onRunScan: () -> Unit,
    onDismiss: () -> Unit
) {
    val primary = MaterialTheme.colorScheme.primary

    // 8 полноценных ИИ-агентов в 2 ряда (по 4 рабочих места)
    val agents = remember {
        listOf(
            AiAgentNode(0, "Алиса", "СМИ", "Отдел СМИ", Color(0xFF10B981), 0.14f, 0.25f, true),
            AiAgentNode(1, "Макс", "Telegram", "Сектор TG", Color(0xFF38BDF8), 0.38f, 0.25f, true),
            AiAgentNode(2, "Инста", "Instagram", "Сектор IG", Color(0xFFF43F5E), 0.62f, 0.25f, true),
            AiAgentNode(3, "Авто-ИИ", "Электрокары", "Авто-лаб", Color(0xFF22D3EE), 0.86f, 0.25f, true),
            AiAgentNode(4, "Дом-Бот", "ТВ и Дом", "Умный дом", Color(0xFFF59E0B), 0.14f, 0.76f, false),
            AiAgentNode(5, "Ноут-ИИ", "Ноутбуки", "ПК-сектор", Color(0xFF818CF8), 0.38f, 0.76f, false),
            AiAgentNode(6, "Нова", "Фильтр", "Анти-шум", Color(0xFFA78BFA), 0.62f, 0.76f, false),
            AiAgentNode(7, "Пульс", "Редактор", "Дайджест", Color(0xFF34D399), 0.86f, 0.76f, false)
        )
    }

    var ticketSeq by remember { mutableIntStateOf(210) }
    var selectedAgentId by remember { mutableIntStateOf(0) }
    var activeDispatch by remember {
        mutableStateOf(
            generateFreshAgentTask(ticketNumber = 209, fromId = 2, toId = 6, newsList = newsList)
        )
    }

    val agentWorkStates = remember {
        mutableStateMapOf<Int, AgentWorkState>().apply {
            agents.forEachIndexed { idx, ag ->
                val initialDone = generateFreshAgentTask(
                    ticketNumber = 200 + idx,
                    fromId = (idx + 5) % 8,
                    toId = ag.id,
                    newsList = newsList
                ).copy(completedAt = ZonedDateTime.now().minusMinutes(1).format(DateTimeFormatter.ofPattern("HH:mm:ss")))

                val initialCurrent = generateFreshAgentTask(
                    ticketNumber = 208 + idx,
                    fromId = (idx + 1) % 8,
                    toId = ag.id,
                    newsList = newsList
                )
                put(
                    ag.id,
                    AgentWorkState(
                        activeTask = initialCurrent,
                        progress = 0.15f + (idx * 0.10f),
                        completedTasks = listOf(initialDone)
                    )
                )
            }
        }
    }

    LaunchedEffect(isScanning, newsList.size) {
        while (true) {
            delay(if (isScanning) 320L else 520L)
            val stepIncrement = if (isScanning) 0.19f else 0.11f

            for (agId in 0..7) {
                val current = agentWorkStates[agId] ?: continue
                val nextProgress = current.progress + stepIncrement
                if (nextProgress >= 1.0f) {
                    val finishedTime = ZonedDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"))
                    val completedItem = current.activeTask.copy(completedAt = finishedTime)

                    val nextReceiverId = (agId + 1 + (ticketSeq % 7)) % 8
                    ticketSeq += 1
                    val newAssignedTask = generateFreshAgentTask(
                        ticketNumber = ticketSeq,
                        fromId = agId,
                        toId = nextReceiverId,
                        newsList = newsList
                    )
                    activeDispatch = newAssignedTask

                    ticketSeq += 1
                    val nextSelfTask = generateFreshAgentTask(
                        ticketNumber = ticketSeq,
                        fromId = (agId + 3) % 8,
                        toId = agId,
                        newsList = newsList
                    )

                    agentWorkStates[agId] = AgentWorkState(
                        activeTask = nextSelfTask,
                        progress = 0.05f,
                        completedTasks = (listOf(completedItem) + current.completedTasks).take(6)
                    )
                } else {
                    agentWorkStates[agId] = current.copy(progress = nextProgress)
                }
            }
        }
    }

    val senderAgent = agents[activeDispatch.fromAgentId]
    val receiverAgent = agents[activeDispatch.toAgentId]
    val inspectedAgent = agents[selectedAgentId]
    val inspectedWork = agentWorkStates[selectedAgentId]

    val infiniteTransition = rememberInfiniteTransition(label = "officeWork8")
    val typingPhase by infiniteTransition.animateFloat(
        initialValue = -1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 320, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "typing8"
    )
    val packetTravel by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "packetTravel8"
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 14.dp),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.2.dp, primary.copy(alpha = 0.35f))
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
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
                                "Офис (8 ИИ-агентов)",
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
                                    text = if (isScanning) "АВРАЛ • 8/8" else "ВСЕ 8 В РАБОТЕ",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF10B981),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            "Нажмите на любого из 8 сотрудников, чтобы увидеть его задачи",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Закрыть")
                    }
                }

                // Схема офиса с 8 рабочими местами, мебелью и человечками
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(Color(0xFF0A1412))
                        .border(1.2.dp, primary.copy(alpha = 0.32f), RoundedCornerShape(18.dp))
                        .pointerInput(Unit) {
                            detectTapGestures { tap ->
                                val w = size.width.toFloat()
                                val h = size.height.toFloat()
                                val tapped = agents.minByOrNull { ag ->
                                    val dx = tap.x - ag.rx * w
                                    val dy = tap.y - ag.ry * h
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

                        val tile = 24.dp.toPx()
                        var x = tile
                        while (x < w) {
                            drawLine(Color.White.copy(alpha = 0.04f), Offset(x, 0f), Offset(x, h), 1f)
                            x += tile
                        }
                        var y = tile
                        while (y < h) {
                            drawLine(Color.White.copy(alpha = 0.04f), Offset(0f, y), Offset(w, y), 1f)
                            y += tile
                        }

                        // 8 рабочих зон (2 ряда по 4)
                        val zoneW = w * 0.22f
                        val zoneH = h * 0.37f
                        agents.forEach { ag ->
                            val isSelected = ag.id == selectedAgentId
                            val topLeft = Offset(ag.rx * w - zoneW / 2f, ag.ry * h - zoneH / 2f)
                            drawRoundRect(
                                color = ag.accentColor.copy(alpha = if (isSelected) 0.18f else 0.06f),
                                topLeft = topLeft,
                                size = Size(zoneW, zoneH),
                                cornerRadius = CornerRadius(10.dp.toPx(), 10.dp.toPx())
                            )
                            drawRoundRect(
                                color = ag.accentColor.copy(alpha = if (isSelected) 0.9f else 0.28f),
                                topLeft = topLeft,
                                size = Size(zoneW, zoneH),
                                cornerRadius = CornerRadius(10.dp.toPx(), 10.dp.toPx()),
                                style = Stroke(width = if (isSelected) 1.8.dp.toPx() else 1.dp.toPx())
                            )
                        }

                        // Центральный коридор: диван и кулер
                        val cx = w / 2f
                        val cy = h / 2f
                        drawRoundRect(
                            color = primary.copy(alpha = 0.1f),
                            topLeft = Offset(cx - 56.dp.toPx(), cy - 16.dp.toPx()),
                            size = Size(112.dp.toPx(), 32.dp.toPx()),
                            cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx())
                        )

                        // Столы, кресла, мониторы и 8 человечков
                        agents.forEach { ag ->
                            val px = ag.rx * w
                            val py = ag.ry * h
                            val isSelected = ag.id == selectedAgentId
                            val handShift = typingPhase * 1.8.dp.toPx()

                            drawCompactWorkstationAndPerson(
                                personPos = Offset(px, py),
                                deskAbove = ag.deskAbove,
                                shirtColor = ag.accentColor,
                                handOffset = handShift,
                                isHighlighted = isSelected
                            )
                        }

                        // Стрелка передачи новой задачи между сотрудниками
                        val start = Offset(senderAgent.rx * w, senderAgent.ry * h)
                        val end = Offset(receiverAgent.rx * w, receiverAgent.ry * h)
                        drawLine(
                            color = senderAgent.accentColor.copy(alpha = 0.8f),
                            start = start,
                            end = end,
                            strokeWidth = 2.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f), 0f)
                        )

                        val angle = atan2(end.y - start.y, end.x - start.x)
                        val arrowLen = 9.dp.toPx()
                        val tip = Offset(
                            end.x - 16.dp.toPx() * cos(angle),
                            end.y - 16.dp.toPx() * sin(angle)
                        )
                        val arrowPath = Path().apply {
                            moveTo(tip.x, tip.y)
                            lineTo(
                                tip.x - arrowLen * cos(angle - 0.45f),
                                tip.y - arrowLen * sin(angle - 0.45f)
                            )
                            lineTo(
                                tip.x - arrowLen * cos(angle + 0.45f),
                                tip.y - arrowLen * sin(angle + 0.45f)
                            )
                            close()
                        }
                        drawPath(arrowPath, color = senderAgent.accentColor)

                        val packetX = start.x + (end.x - start.x) * packetTravel
                        val packetY = start.y + (end.y - start.y) * packetTravel
                        drawCircle(
                            color = Color.White,
                            radius = 5.dp.toPx(),
                            center = Offset(packetX, packetY)
                        )
                        drawCircle(
                            color = senderAgent.accentColor,
                            radius = 3.dp.toPx(),
                            center = Offset(packetX, packetY)
                        )
                    }

                    // Подписи имён 8 агентов у каждого стола
                    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                        val boxW = maxWidth
                        val boxH = maxHeight
                        agents.forEach { ag ->
                            val offsetX = (boxW * ag.rx) - 34.dp
                            val offsetY = if (ag.deskAbove) (boxH * ag.ry) + 18.dp else (boxH * ag.ry) - 34.dp
                            Column(
                                modifier = Modifier
                                    .offset(x = offsetX.coerceIn(4.dp, boxW - 72.dp), y = offsetY)
                                    .width(68.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = ag.name,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = ag.accentColor,
                                    maxLines = 1
                                )
                                Text(
                                    text = ag.role,
                                    fontSize = 8.sp,
                                    color = Color.White.copy(alpha = 0.65f),
                                    maxLines = 1
                                )
                            }
                        }
                    }

                    // Всплывающий бабл последней переданной задачи в центре
                    Surface(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(horizontal = 16.dp),
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFF0F2620).copy(alpha = 0.95f),
                        border = BorderStroke(1.dp, senderAgent.accentColor.copy(alpha = 0.75f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Text(
                                text = senderAgent.name,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = senderAgent.accentColor
                            )
                            Icon(
                                imageVector = Icons.Default.ArrowForward,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(11.dp)
                            )
                            Text(
                                text = "${receiverAgent.name}: #${activeDispatch.ticketNumber} ${activeDispatch.badge}",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                        }
                    }
                }

                // Сетка выбора любого из 8 агентов (2 ряда по 4)
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    agents.chunked(4).forEach { rowAgents ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            rowAgents.forEach { ag ->
                                val isSel = selectedAgentId == ag.id
                                val doneCount = agentWorkStates[ag.id]?.completedTasks?.size ?: 0
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
                                        modifier = Modifier.padding(vertical = 5.dp, horizontal = 3.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = ag.name,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSel) ag.accentColor else MaterialTheme.colorScheme.onSurface,
                                            maxLines = 1
                                        )
                                        Text(
                                            text = "✓ $doneCount",
                                            fontSize = 9.sp,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Досье выбранного агента: чем занят сейчас + история выполненных задач
                if (inspectedWork != null) {
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
                                    text = "${inspectedAgent.name} • ${inspectedAgent.role}",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = inspectedAgent.accentColor
                                )
                                Text(
                                    text = "#${inspectedWork.activeTask.ticketNumber} • ${(inspectedWork.progress * 100).toInt()}%",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = inspectedAgent.accentColor
                                )
                            }

                            Text(
                                text = "В работе: ${inspectedWork.activeTask.title}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.92f)
                            )

                            LinearProgressIndicator(
                                progress = { inspectedWork.progress.coerceIn(0f, 1f) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(CircleShape),
                                color = inspectedAgent.accentColor,
                                trackColor = inspectedAgent.accentColor.copy(alpha = 0.2f)
                            )

                            HorizontalDivider(color = inspectedAgent.accentColor.copy(alpha = 0.2f))

                            Text(
                                text = "Выполненные задачи (${inspectedWork.completedTasks.size}):",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                            )

                            inspectedWork.completedTasks.take(3).forEach { done ->
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
                                            text = "Выполнено в ${done.completedAt}",
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
                        text = if (isScanning) "Все 8 агентов собирают новости..." else "Запустить общий сбор новостей (8 агентов)",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

private fun DrawScope.drawCompactWorkstationAndPerson(
    personPos: Offset,
    deskAbove: Boolean,
    shirtColor: Color,
    handOffset: Float,
    isHighlighted: Boolean
) {
    val dir = if (deskAbove) -1f else 1f
    val deskW = 40.dp.toPx()
    val deskH = 16.dp.toPx()
    val deskCenterY = personPos.y + dir * 16.dp.toPx()

    // Кресло
    val chairCenterY = personPos.y - dir * 8.dp.toPx()
    drawRoundRect(
        color = Color(0xFF334155),
        topLeft = Offset(personPos.x - 9.dp.toPx(), chairCenterY - 4.dp.toPx()),
        size = Size(18.dp.toPx(), 8.dp.toPx()),
        cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx())
    )

    // Стол
    drawRoundRect(
        color = Color(0xFF1E293B),
        topLeft = Offset(personPos.x - deskW / 2f, deskCenterY - deskH / 2f),
        size = Size(deskW, deskH),
        cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
    )
    drawRoundRect(
        color = Color(0xFF475569),
        topLeft = Offset(personPos.x - deskW / 2f, deskCenterY - deskH / 2f),
        size = Size(deskW, deskH),
        cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx()),
        style = Stroke(width = 1.dp.toPx())
    )

    // Монитор
    val monitorY = deskCenterY + dir * 2.dp.toPx()
    drawLine(
        color = shirtColor,
        start = Offset(personPos.x - 10.dp.toPx(), monitorY),
        end = Offset(personPos.x + 10.dp.toPx(), monitorY),
        strokeWidth = 2.5.dp.toPx(),
        cap = StrokeCap.Round
    )

    val kbY = deskCenterY - dir * 4.dp.toPx()

    if (isHighlighted) {
        drawCircle(
            color = shirtColor.copy(alpha = 0.25f),
            radius = 16.dp.toPx(),
            center = personPos
        )
    }

    // Плечи человечка
    drawRoundRect(
        color = shirtColor,
        topLeft = Offset(personPos.x - 9.dp.toPx(), personPos.y - 5.dp.toPx()),
        size = Size(18.dp.toPx(), 10.dp.toPx()),
        cornerRadius = CornerRadius(5.dp.toPx(), 5.dp.toPx())
    )

    // Руки
    drawLine(
        color = Color(0xFFFDE68A),
        start = Offset(personPos.x - 7.dp.toPx(), personPos.y + dir * 3.dp.toPx()),
        end = Offset(personPos.x - 4.dp.toPx(), kbY + handOffset),
        strokeWidth = 2.5.dp.toPx(),
        cap = StrokeCap.Round
    )
    drawLine(
        color = Color(0xFFFDE68A),
        start = Offset(personPos.x + 7.dp.toPx(), personPos.y + dir * 3.dp.toPx()),
        end = Offset(personPos.x + 4.dp.toPx(), kbY - handOffset),
        strokeWidth = 2.5.dp.toPx(),
        cap = StrokeCap.Round
    )

    // Голова
    drawCircle(
        color = Color(0xFFFDE68A),
        radius = 5.5.dp.toPx(),
        center = personPos
    )
    drawArc(
        color = Color(0xFF1E293B),
        startAngle = if (deskAbove) 0f else 180f,
        sweepAngle = 180f,
        useCenter = true,
        topLeft = Offset(personPos.x - 5.5.dp.toPx(), personPos.y - 5.5.dp.toPx()),
        size = Size(11.dp.toPx(), 11.dp.toPx())
    )
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
