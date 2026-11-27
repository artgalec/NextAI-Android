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
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Description
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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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

data class ReviewSpecRow(
    val label: String,
    val value: String
)

data class TechReviewArticle(
    val id: String,
    val deviceName: String,
    val brand: String,
    val category: String,
    val categoryLabel: String,
    val authorAgent: String,
    val approvedByChief: String,
    val headline: String,
    val leadSummary: String,
    val fullBody: String,
    val specs: List<ReviewSpecRow>,
    val pros: List<String>,
    val cons: List<String>,
    val score: String,
    val publishedAt: String,
    val accentHex: Long,
    val deputyEditedNote: String = "",
    val auditStage: Int = 0
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
    val fixedField: String,
    val correctionDetail: String,
    val editedAt: String
)

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
    val cornerRadius: Int = 16,
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
    CHIEF_CHECK("На проверке у Авроры"),
    DINING("Обедает на кухне"),
    LOUNGE("Отдыхает у ёлки и ТВ")
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
    val completedAt: String = "",
    val chiefApproved: Boolean = true,
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

data class TgChannel(val name: String, val defaultBrand: String, val username: String)
data class RssSite(val name: String, val defaultBrand: String, val platform: String, val url: String)
data class WebPageSource(val name: String, val defaultBrand: String, val url: String, val linkRegex: String)
data class BrandSearchQuery(val brand: String, val query: String)

object ReviewCatalogFactory {
    val allDeviceTypes = listOf("Все", "Смартфоны", "Часы", "Наушники", "Ноутбуки", "Телевизоры", "Электроавто")
    val creatableDeviceTypes = listOf("Смартфоны", "Часы", "Наушники", "Ноутбуки", "Телевизоры", "Электроавто")

    val quickSuggestedDevices = listOf(
        "Xiaomi 17T Pro",
        "Samsung Galaxy S26 Ultra",
        "OnePlus 15R",
        "Nothing Phone (3)",
        "Google Pixel 10 Pro",
        "vivo X200 Pro",
        "Huawei FreeBuds Pro 4",
        "Apple Watch Series 11"
    )

    fun isAutoGeneratedNewsJunk(article: TechReviewArticle): Boolean {
        if (article.id.startsWith("dyn_")) return true
        val lowDev = article.deviceName.lowercase()
        val junkMarkers = listOf(
            "готовит", "пока ", "в россии", "показал", "представил", "вышла",
            "вышел", "стал ", "стала ", "инсайд", "утечк", "официально", "цена ",
            "слухи", "анонс", "новинк", "компания ", "бренд "
        )
        if (article.headline.contains("полный разбор возможностей, автономности и реальных ТТХ") &&
            junkMarkers.any { it in lowDev }
        ) {
            return true
        }
        return false
    }

    fun initialReviews(): List<TechReviewArticle> = listOf(
        TechReviewArticle(
            id = "rev_xiaomi_17t",
            deviceName = "Xiaomi 17T",
            brand = "Xiaomi",
            category = "phone",
            categoryLabel = "Смартфоны",
            authorAgent = "Вика • Обзоры смартфонов",
            approvedByChief = "ТТХ проверены Авророй",
            headline = "Xiaomi 17T: субфлагман с камерами Leica, 5x перископом и батареей 6500 мАч",
            leadSummary = "Разбираем главные козыри и компромиссы Xiaomi 17T: процессор Dimensity 8500-Ultra, дисплей 6.83\" 1.5K AMOLED, защита IP68 и HyperOS.",
            fullBody = "Xiaomi 17T уверенно закрывает нишу доступных субфлагманов. Главная фишка модели — блок камер с оптикой Leica и 50-мегапиксельным перископическим телевиком с 5-кратным оптическим приближением, который раньше встречался только в старших Ultra-флагманах.\n\nЗа скорость отвечает энергоэффективный 4-нм чип MediaTek Dimensity 8500-Ultra в паре с памятью LPDDR5X и UFS 4.0. Аккумулятор повышенной ёмкости 6500 мАч спокойно выдерживает полтора-два дня активного экрана.",
            specs = listOf(
                ReviewSpecRow("Дисплей", "6.83\" AMOLED 1.5K (2772×1280), 120 Гц, HDR10+"),
                ReviewSpecRow("Процессор", "MediaTek Dimensity 8500-Ultra (4 нм)"),
                ReviewSpecRow("Камеры Leica", "50 МП (OIS) + 50 МП перископ 5x + 12 МП ультраширик"),
                ReviewSpecRow("Батарея и зарядка", "6500 мАч, быстрая проводная зарядка 67 Вт"),
                ReviewSpecRow("Защита и интерфейсы", "IP68, стереодинамики Dolby Atmos, ИК-порт, USB 2.0")
            ),
            pros = listOf("Редкий 5x перископ Leica в среднем классе", "Высокая автономность благодаря батарее 6500 мАч", "Полноценная защита от воды и пыли IP68"),
            cons = listOf("Разъём USB Type-C работает по стандарту USB 2.0", "Отсутствует беспроводная зарядка"),
            score = "9.3 / 10",
            publishedAt = "Сегодня",
            accentHex = 0xFF38BDF8
        ),
        TechReviewArticle(
            id = "rev_huawei_watch_fit_5",
            deviceName = "Huawei Watch Fit 5",
            brand = "Huawei",
            category = "watch",
            categoryLabel = "Часы",
            authorAgent = "Артур • Носимая электроника",
            approvedByChief = "ТТХ проверены Авророй",
            headline = "Huawei Watch Fit 5: тонкий корпус, яркий AMOLED и до 10 дней без розетки",
            leadSummary = "Детальный текстовый обзор актуальных умных часов Huawei: проверяем экран 60 Гц, датчики пульса и SpO2, работу с HarmonyOS и автономность.",
            fullBody = "Huawei Watch Fit 5 сохранили лёгкий прямоугольный форм-фактор, который практически не ощущается на запястье даже во время сна. Увеличенный AMOLED-дисплей с тонкими рамками легко читается под прямым солнцем.\n\nВнутри обновлена оптическая платформа мониторинга пульса и уровня кислорода в крови (SpO2): точность замеров на пробежках выросла, а графики сна и восстановления в приложении Huawei Health стали подробнее.",
            specs = listOf(
                ReviewSpecRow("Экран", "1.82\" AMOLED, 480×408, 60 Гц"),
                ReviewSpecRow("Корпус и защита", "Алюминиевый сплав, вес 27 г, влагозащита 5 ATM"),
                ReviewSpecRow("Система", "HarmonyOS, совместимость с Android и iOS"),
                ReviewSpecRow("Датчики и связь", "Пульс 24/7, SpO2, стресс, GPS, Bluetooth 5.3"),
                ReviewSpecRow("Автономность", "До 10 дней (до 7 дней с активным AOD)")
            ),
            pros = listOf("Отличная читаемость дисплея на солнце", "Реальные 7–10 дней работы от одного заряда", "Лёгкий вес и быстрая магнитная зарядка"),
            cons = listOf("Нет поддержки бесконтактной оплаты NFC в ряде регионов", "Ограниченный магазин сторонних приложений"),
            score = "9.1 / 10",
            publishedAt = "Сегодня",
            accentHex = 0xFF10B981
        ),
        TechReviewArticle(
            id = "rev_redmi_buds_8_pro",
            deviceName = "Xiaomi Redmi Buds 8 Pro",
            brand = "Xiaomi",
            category = "buds",
            categoryLabel = "Наушники",
            authorAgent = "Артур • Носимая электроника",
            approvedByChief = "ТТХ проверены Авророй",
            headline = "Xiaomi Redmi Buds 8 Pro: кодеки LDAC и LHDC, шумоподавление 55 дБ и три драйвера",
            leadSummary = "Тестируем флагманские TWS-наушники Redmi: качество звука по Hi-Res кодекам, работу адаптивного ANC и автономность.",
            fullBody = "Redmi Buds 8 Pro получили коаксиальную трёхдрайверную акустическую схему с титановым вуфером и пьезокерамическими твитерами. При активации Hi-Res кодеков LHDC 5.0 или LDAC наушники выдают чистые высокие частоты и плотный, контролируемый бас.\n\nСистема активного шумоподавления гасит до 55 дБ внешнего гула в метро и самолёте, автоматически подстраиваясь под посадку в ушном канале.",
            specs = listOf(
                ReviewSpecRow("Излучатели", "Коаксиальные 3 драйвера (11 мм титан + 2 твитера)"),
                ReviewSpecRow("Аудиокодеки", "LHDC 5.0, LDAC, AAC, SBC"),
                ReviewSpecRow("Шумоподавление", "Адаптивное ANC до 55 дБ + 3 микрофона с ИИ"),
                ReviewSpecRow("Автономность", "До 9.5 ч без ANC, до 38 ч вместе с кейсом"),
                ReviewSpecRow("Связь и защита", "Bluetooth 5.4, Multipoint, защита IP54")
            ),
            pros = listOf("Поддержка обоих Hi-Res кодеков LHDC и LDAC", "Мощное шумоподавление 55 дБ", "Удобный мультипоинт и быстрая зарядка"),
            cons = listOf("Маркий глянцевый пластик внутри кейса", "Автономность ниже при активном LDAC"),
            score = "9.0 / 10",
            publishedAt = "Сегодня",
            accentHex = 0xFFF59E0B
        ),
        TechReviewArticle(
            id = "rev_samsung_s95d_oled",
            deviceName = "Samsung S95D QD-OLED 65\"",
            brand = "Samsung",
            category = "tv",
            categoryLabel = "Телевизоры",
            authorAgent = "Дом-Бот • Обзоры ТВ и дома",
            approvedByChief = "ТТХ проверены Авророй",
            headline = "Samsung S95D QD-OLED 65\": матовый экран без бликов, 144 Гц и яркость 1700 нит",
            leadSummary = "Обзор флагманского QD-OLED телевизора Samsung: уникальное антибликовое покрытие OLED Glare Free, выносной блок One Connect и игры 4K 144 Гц.",
            fullBody = "Samsung S95D решает главную проблему OLED-телевизоров — зеркальные отражения днём. Специальное матовое покрытие OLED Glare Free рассеивает свет от окон и люстр, сохраняя глубокий чёрный цвет и насыщенность квантовых точек.\n\nВсе четыре порта HDMI 2.1 вынесены в тонкий блок One Connect, что позволяет повесить панель толщиной 11 мм вплотную к стене.",
            specs = listOf(
                ReviewSpecRow("Матрица и диагональ", "65\" QD-OLED 4K (3840×2160), 144 Гц, OLED Glare Free"),
                ReviewSpecRow("Процессор и HDR", "NQ4 AI Gen2, HDR10+, HLG, пик до 1700 нит"),
                ReviewSpecRow("Звук", "70 Вт (4.2.2 канала), Dolby Atmos, OTS+"),
                ReviewSpecRow("Разъёмы", "4× HDMI 2.1 (4K 144 Гц), 3× USB, блок One Connect"),
                ReviewSpecRow("Система", "Tizen OS с игровым хабом")
            ),
            pros = listOf("Лучшее антибликовое покрытие среди OLED-телевизоров", "Эталонный цветовой охват QD-OLED", "Все 4 порта HDMI 2.1 с поддержкой 144 Гц"),
            cons = listOf("Нет поддержки Dolby Vision (только HDR10+)", "Высокая стоимость флагманской серии"),
            score = "9.4 / 10",
            publishedAt = "Вчера",
            accentHex = 0xFF60A5FA
        ),
        TechReviewArticle(
            id = "rev_huawei_matebook_x_pro",
            deviceName = "Huawei MateBook X Pro",
            brand = "Huawei",
            category = "laptop",
            categoryLabel = "Ноутбуки",
            authorAgent = "Нова • Обзоры ноутбуков",
            approvedByChief = "ТТХ проверены Авророй",
            headline = "Huawei MateBook X Pro: ультрабук весом 980 грамм с гибким OLED-экраном 120 Гц",
            leadSummary = "Изучаем сверхлёгкий флагманский ноутбук Huawei из магниевого сплава: процессор Intel Core Ultra 9, 6 динамиков и зарядка 90 Вт.",
            fullBody = "Huawei MateBook X Pro весит всего 980 грамм при диагонали 14.2 дюйма — это один из самых лёгких производительных ультрабуков на рынке. Гибкая OLED-панель с разрешением 3.1K и частотой 120 Гц имеет антибликовое покрытие и точную заводскую калибровку цвета.\n\nБлагодаря переработанной системе охлаждения Shark Fin ноутбук стабильно держит теплопакет до 40 Вт под нагрузкой.",
            specs = listOf(
                ReviewSpecRow("Экран", "14.2\" OLED 3120×2080 (3:2), 120 Гц, сенсорный"),
                ReviewSpecRow("Процессор", "Intel Core Ultra 9 185H (16 ядер)"),
                ReviewSpecRow("Память", "32 ГБ LPDDR5X + 2 ТБ NVMe PCIe 4.0 SSD"),
                ReviewSpecRow("Вес и корпус", "980 грамм, толщина 13.5 мм, магниевый сплав"),
                ReviewSpecRow("Порты и батарея", "2× Thunderbolt 4, USB-C, 70 Вт·ч + зарядка 90 Вт")
            ),
            pros = listOf("Невероятный вес 980 г при мощном Core Ultra 9", "Эталонный сенсорный 3.1K OLED-экран 120 Гц", "Отличный тактильный тачпад Free Touch"),
            cons = listOf("Нет полноразмерного порта USB-A и HDMI без переходника", "Оперативная память распаяна на плате"),
            score = "9.4 / 10",
            publishedAt = "Вчера",
            accentHex = 0xFF14B8A6
        ),
        TechReviewArticle(
            id = "rev_xiaomi_yu7",
            deviceName = "Xiaomi YU7 Max",
            brand = "Электроавто",
            category = "car",
            categoryLabel = "Электроавто",
            authorAgent = "Авто-ИИ • Электроавто",
            approvedByChief = "ТТХ проверены Авророй",
            headline = "Xiaomi YU7 Max: электрический кроссовер с запасом хода 760 км и архитектурой 800 В",
            leadSummary = "Технический разбор второго электромобиля Xiaomi: панорамный проекционный дисплей HyperVision, два мотора на 691 л.с. и автопилот с лидаром.",
            fullBody = "Xiaomi YU7 переносит философию флагманского седана SU7 в формат среднеразмерного кроссовера. В салоне вместо привычной приборной панели установлен панорамный проекционный экран HyperVision во всю ширину лобового стекла, работающий на базе HyperOS.\n\nТоповая полноприводная версия Max оснащена двумя электромоторами суммарной мощностью 691 л.с. и батареей CATL Qilin на 101.7 кВт·ч.",
            specs = listOf(
                ReviewSpecRow("Силовая установка", "Полный привод, 2 мотора, 691 л.с. (508 кВт)"),
                ReviewSpecRow("Разгон 0–100 км/ч", "3.23 секунды, макс. скорость 253 км/ч"),
                ReviewSpecRow("Батарея и запас хода", "101.7 кВт·ч CATL, до 760 км (цикл CLTC)"),
                ReviewSpecRow("Зарядка", "Архитектура 800 В, с 10% до 80% за 12–15 минут"),
                ReviewSpecRow("Автопилот", "Лидар на крыше + чип NVIDIA Thor (700 TOPS)")
            ),
            pros = listOf("Сверхбыстрая зарядка 800 В и запас хода 760 км", "Глубокая интеграция с экосистемой смартфонов Xiaomi", "Богатая пневмоподвеска и лидар уже в базе"),
            cons = listOf("Отсутствие физических кнопок климата по умолчанию", "Высокая стоимость доставки и растаможки вне Китая"),
            score = "9.5 / 10",
            publishedAt = "2 дня назад",
            accentHex = 0xFF22D3EE
        )
    )

    private val curatedDevicesPool = listOf(
        TechReviewArticle(
            id = "uniq_oneplus_15r",
            deviceName = "OnePlus 15R",
            brand = "OnePlus",
            category = "phone",
            categoryLabel = "Смартфоны",
            authorAgent = "Вика • Обзоры смартфонов",
            approvedByChief = "ТТХ проверены Авророй",
            headline = "OnePlus 15R: чип Snapdragon 8 Gen 5, плоский экран 165 Гц и батарея 7400 мАч",
            leadSummary = "Обзор производительного смартфона OnePlus 15R (в Китае — Ace 6T): плоская AMOLED-панель 165 Гц, кремний-углеродный аккумулятор 7400 мАч и зарядка 80 Вт.",
            fullBody = "OnePlus 15R делает ставку на максимальную плавность интерфейса и рекордную автономность. Смартфон оснащён 6.83-дюймовым плоским LTPS AMOLED-экраном с разрешением 1.5K и частотой обновления до 165 Гц.\n\nВнутри установлен процессор Qualcomm Snapdragon 8 Gen 5 с испарительной камерой площадью 9140 мм², а кремний-углеродный аккумулятор ёмкостью 7400 мАч обеспечивает до двух суток активной работы.",
            specs = listOf(
                ReviewSpecRow("Экран", "6.83\" плоский AMOLED 1.5K (2800×1272), 165 Гц"),
                ReviewSpecRow("Процессор и память", "Qualcomm Snapdragon 8 Gen 5, LPDDR5X, UFS 4.1"),
                ReviewSpecRow("Камеры", "50 МП Sony IMX906 (OIS) + 8 МП ультраширик"),
                ReviewSpecRow("Аккумулятор", "7400 мАч Silicon-Carbon, зарядка 80 Вт"),
                ReviewSpecRow("Защита", "IP68 / IP69, стереодинамики, ИК-порт")
            ),
            pros = listOf("Огромная батарея 7400 мАч и плоский экран 165 Гц", "Максимальная пыле- и влагозащита IP68/IP69", "Мощный чип Snapdragon 8 Gen 5"),
            cons = listOf("Отсутствует телеобъектив для оптического зума", "Нет беспроводной зарядки"),
            score = "9.2 / 10",
            publishedAt = "Сегодня",
            accentHex = 0xFFEF4444
        ),
        TechReviewArticle(
            id = "uniq_samsung_galaxy_s26_ultra",
            deviceName = "Samsung Galaxy S26 Ultra",
            brand = "Samsung",
            category = "phone",
            categoryLabel = "Смартфоны",
            authorAgent = "Вика • Обзоры смартфонов",
            approvedByChief = "ТТХ проверены Авророй",
            headline = "Samsung Galaxy S26 Ultra: титановый корпус, Snapdragon 8 Elite Gen 2 и 200 МП камера",
            leadSummary = "Обзор главного флагмана Samsung: антибликовое стекло Gorilla Armor 2, стилус S Pen и обновленный телеобъектив 50 МП.",
            fullBody = "Samsung Galaxy S26 Ultra получил ещё более тонкие симметричные рамки, обновлённый процессор Snapdragon 8 Elite Gen 2 for Galaxy и улучшенную ночную съёмку на все четыре модуля камеры.",
            specs = listOf(
                ReviewSpecRow("Экран", "6.9\" Dynamic AMOLED 2X QHD+, 1–120 Гц, 2600 нит"),
                ReviewSpecRow("Процессор", "Snapdragon 8 Elite Gen 2 for Galaxy (3 нм)"),
                ReviewSpecRow("Камеры", "200 МП (OIS) + 50 МП 5x + 50 МП 3x + 50 МП ширик"),
                ReviewSpecRow("Батарея", "5000 мАч, зарядка 45 Вт проводная / 25 Вт Qi2"),
                ReviewSpecRow("Корпус", "Титан Grade 5, IP68, встроенный S Pen")
            ),
            pros = listOf("Лучшее антибликовое покрытие экрана", "Универсальный набор из четырёх камер высокого разрешения"),
            cons = listOf("Скорость зарядки 45 Вт уступает китайским флагманам"),
            score = "9.5 / 10",
            publishedAt = "Сегодня",
            accentHex = 0xFF3B82F6
        ),
        TechReviewArticle(
            id = "uniq_nothing_phone_3",
            deviceName = "Nothing Phone (3)",
            brand = "Nothing",
            category = "phone",
            categoryLabel = "Смартфоны",
            authorAgent = "Вика • Обзоры смартфонов",
            approvedByChief = "ТТХ проверены Авророй",
            headline = "Nothing Phone (3): прозрачный дизайн с подсветкой Glyph, Snapdragon 8s Gen 4 и Nothing OS",
            leadSummary = "Изучаем третье поколение прозрачного смартфона Карла Пея: новые сценарии светодиодов Glyph, плоский LTPO-экран и чистая система.",
            fullBody = "Nothing Phone (3) сохранил фирменную прозрачную спинку, но получил более точечную матрицу светодиодов Glyph Matrix для отображения виджетов, таймеров и уведомлений без включения основного экрана.",
            specs = listOf(
                ReviewSpecRow("Экран", "6.67\" LTPO AMOLED 1.5K, 1–120 Гц, 3000 нит"),
                ReviewSpecRow("Процессор", "Qualcomm Snapdragon 8s Gen 4 (4 нм)"),
                ReviewSpecRow("Камеры", "50 МП Sony LYT-700 (OIS) + 50 МП перископ 3x + 50 МП ширик"),
                ReviewSpecRow("Батарея", "5500 мАч, проводная зарядка 65 Вт, беспроводная 15 Вт"),
                ReviewSpecRow("Система", "Nothing OS на базе Android с ИИ-ассистентом Essential Space")
            ),
            pros = listOf("Уникальный дизайн и удобные виджеты Glyph", "Быстрая и лёгкая оболочка Nothing OS без рекламы"),
            cons = listOf("Не самый топовый флагманский чипсет в классе"),
            score = "9.1 / 10",
            publishedAt = "Сегодня",
            accentHex = 0xFF94A3B8
        ),
        TechReviewArticle(
            id = "uniq_google_pixel_10_pro",
            deviceName = "Google Pixel 10 Pro",
            brand = "Google",
            category = "phone",
            categoryLabel = "Смартфоны",
            authorAgent = "Вика • Обзоры смартфонов",
            approvedByChief = "ТТХ проверены Авророй",
            headline = "Google Pixel 10 Pro: 3-нм чип Tensor G5 от TSMC, экран Super Actua и 7 лет обновлений",
            leadSummary = "Тестируем камерофон Google на новом процессоре Tensor G5, впервые произведённом на фабриках TSMC.",
            fullBody = "Переход Tensor G5 на техпроцесс TSMC 3 нм заметно снизил нагрев под нагрузкой и улучшил автономность Google Pixel 10 Pro. Вычислительная фотография и видео в 4K HDR стали работать ещё быстрее.",
            specs = listOf(
                ReviewSpecRow("Экран", "6.3\" LTPO OLED Super Actua, 1–120 Гц, 3000 нит"),
                ReviewSpecRow("Процессор", "Google Tensor G5 (TSMC 3 нм) + Titan M2"),
                ReviewSpecRow("Камеры", "50 МП (OIS) + 48 МП 5x перископ + 48 МП ультраширик"),
                ReviewSpecRow("Батарея", "4950 мАч, зарядка 37 Вт, Qi2 магнитная зарядка"),
                ReviewSpecRow("Поддержка", "7 лет обновлений Android и Pixel Feature Drops")
            ),
            pros = listOf("Эталонная обработка фото и видео", "Компактный флагманский формат 6.3 дюйма"),
            cons = listOf("Невысокая мощность зарядки по сравнению с конкурентами"),
            score = "9.3 / 10",
            publishedAt = "Сегодня",
            accentHex = 0xFF10B981
        ),
        TechReviewArticle(
            id = "uniq_vivo_x200_pro",
            deviceName = "vivo X200 Pro",
            brand = "vivo",
            category = "phone",
            categoryLabel = "Смартфоны",
            authorAgent = "Вика • Обзоры смартфонов",
            approvedByChief = "ТТХ проверены Авророй",
            headline = "vivo X200 Pro: перископ 200 МП ZEISS APO, Dimensity 9400 и батарея 6000 мАч",
            leadSummary = "Обзор одного из лучших камерофонов на рынке: телевик 200 МП с оптикой ZEISS, чип обработки фото V3+ и кремний-углеродный аккумулятор.",
            fullBody = "vivo X200 Pro оснащён 200-мегапиксельным перископическим модулем ZEISS APO с макрорежимом и выдающейся детализацией на фокусном расстоянии 85–135 мм.",
            specs = listOf(
                ReviewSpecRow("Экран", "6.78\" 8T LTPO AMOLED 1.5K, 1–120 Гц, 4500 нит"),
                ReviewSpecRow("Процессор", "MediaTek Dimensity 9400 (3 нм) + чип vivo V3+"),
                ReviewSpecRow("Камеры ZEISS", "50 МП Sony LYT-818 + 200 МП перископ + 50 МП ширик"),
                ReviewSpecRow("Батарея", "6000 мАч BlueVolt, проводная 90 Вт / беспроводная 30 Вт"),
                ReviewSpecRow("Защита", "IP68 / IP69, ультразвуковой сканер отпечатков")
            ),
            pros = listOf("Выдающийся 200 МП телевик ZEISS с телемакро", "Отличная автономность от батареи 6000 мАч"),
            cons = listOf("Массивный блок камер на задней панели"),
            score = "9.5 / 10",
            publishedAt = "Сегодня",
            accentHex = 0xFF38BDF8
        ),
        TechReviewArticle(
            id = "uniq_huawei_freebuds_pro_4",
            deviceName = "Huawei FreeBuds Pro 4",
            brand = "Huawei",
            category = "buds",
            categoryLabel = "Наушники",
            authorAgent = "Артур • Носимая электроника",
            approvedByChief = "ТТХ проверены Авророй",
            headline = "Huawei FreeBuds Pro 4: передача звука без потерь 2.3 Мбит/с и чистая речь при шуме 100 дБ",
            leadSummary = "Тестируем флагманские TWS-наушники Huawei на базе чипа Kirin A2 с кодеком L2HC 4.0 и пенными амбушюрами.",
            fullBody = "Huawei FreeBuds Pro 4 используют двойные излучатели и кодек L2HC 4.0 для передачи аудио без потерь со скоростью до 2.3 Мбит/с в связке с актуальными смартфонами Huawei.",
            specs = listOf(
                ReviewSpecRow("Излучатели", "11 мм динамический + планарный твитер"),
                ReviewSpecRow("Кодеки", "L2HC 4.0 (до 2.3 Мбит/с), LDAC, AAC, SBC"),
                ReviewSpecRow("Шумоподавление", "Интеллектуальное ANC + датчик костной проводимости"),
                ReviewSpecRow("Автономность", "До 7 ч без ANC, до 33 ч с зарядным кейсом"),
                ReviewSpecRow("Защита", "IP54, управление кивками головы")
            ),
            pros = listOf("Лучшее шумоподавление микрофонов во время звонков", "Поддержка L2HC 4.0 и LDAC"),
            cons = listOf("Максимальный битрейт 2.3 Мбит/с доступен только на смартфонах Huawei"),
            score = "9.3 / 10",
            publishedAt = "Сегодня",
            accentHex = 0xFF10B981
        ),
        TechReviewArticle(
            id = "uniq_apple_watch_s11",
            deviceName = "Apple Watch Series 11",
            brand = "Apple",
            category = "watch",
            categoryLabel = "Часы",
            authorAgent = "Артур • Носимая электроника",
            approvedByChief = "ТТХ проверены Авророй",
            headline = "Apple Watch Series 11: тонкий корпус, широкоугольный OLED и быстрая зарядка",
            leadSummary = "Подробный разбор новых умных часов Apple: насколько ярче стал экран под углом, как работает чип S11 и сколько держат батарею.",
            fullBody = "Apple Watch Series 11 получили обновлённую широкоугольную OLED-матрицу LTPO3, которая остаётся на 40% ярче при взгляде под углом, а в режиме Always-On отображает секундную стрелку в реальном времени.",
            specs = listOf(
                ReviewSpecRow("Дисплей", "LTPO3 OLED Always-On, до 2000 нит"),
                ReviewSpecRow("Процессор", "Apple S11 SiP, 64 ГБ встроенной памяти"),
                ReviewSpecRow("Датчики", "ЭКГ, пульс, температура тела, глубиномер 6 м"),
                ReviewSpecRow("Защита", "WR50 (плавание до 50 м) + пылезащита IP6X"),
                ReviewSpecRow("Зарядка", "До 80% за 30 минут, автономность 18–36 часов")
            ),
            pros = listOf("Очень тонкий и лёгкий корпус 9.7 мм", "Лучшая читаемость экрана под любым углом", "Молниеносная зарядка до 80% за полчаса"),
            cons = listOf("Работают исключительно в связке с iPhone", "Автономность всё ещё уступает конкурентам на 7–10 дней"),
            score = "9.0 / 10",
            publishedAt = "Сегодня",
            accentHex = 0xFFA78BFA
        )
    )

    // Создаёт обзор ИСКЛЮЧИТЕЛЬНО по прямому заказу пользователя (без самовольного парсинга заголовков новостей!)
    fun createCustomReviewByUserOrder(
        rawDeviceName: String,
        selectedBrand: String,
        selectedCategoryLabel: String,
        userFocusNote: String,
        assignedAgentName: String
    ): TechReviewArticle {
        val cleanName = rawDeviceName.trim().ifBlank { "Xiaomi 17T Pro" }
        val nowStr = ZonedDateTime.now().format(DateTimeFormatter.ofPattern("Сегодня, HH:mm"))

        // Если модель есть в нашей эталонной базе — используем её точные ТТХ
        val matchedPool = curatedDevicesPool.firstOrNull {
            it.deviceName.equals(cleanName, ignoreCase = true)
        }
        if (matchedPool != null) {
            val customBody = if (userFocusNote.isNotBlank()) {
                "${matchedPool.fullBody}\n\nАкцент по заданию редакции: ${userFocusNote.trim()}"
            } else {
                matchedPool.fullBody
            }
            return matchedPool.copy(
                id = "manual_${System.currentTimeMillis()}",
                authorAgent = "$assignedAgentName • По заказу владельца",
                fullBody = customBody,
                publishedAt = nowStr
            )
        }

        val low = cleanName.lowercase()
        val brand = if (selectedBrand != "Авто") {
            selectedBrand
        } else {
            when {
                listOf("xiaomi", "redmi", "poco").any { it in low } -> "Xiaomi"
                listOf("samsung", "galaxy").any { it in low } -> "Samsung"
                listOf("huawei", "mate", "pura").any { it in low } -> "Huawei"
                listOf("honor", "magic").any { it in low } -> "HONOR"
                listOf("apple", "iphone", "macbook", "ipad", "airpods").any { it in low } -> "Apple"
                listOf("oneplus").any { it in low } -> "OnePlus"
                listOf("google", "pixel").any { it in low } -> "Google"
                listOf("oppo", "find", "reno").any { it in low } -> "OPPO"
                listOf("vivo", "iqoo").any { it in low } -> "vivo"
                listOf("realme").any { it in low } -> "realme"
                listOf("nothing", "cmf").any { it in low } -> "Nothing"
                listOf("zeekr", "byd", "tesla", "su7", "yu7", "li auto").any { it in low } -> "Электроавто"
                else -> "Xiaomi"
            }
        }

        val catLabel = if (selectedCategoryLabel != "Авто") {
            selectedCategoryLabel
        } else {
            when {
                listOf("watch", "часы", "fit", "gt").any { it in low } -> "Часы"
                listOf("buds", "pods", "наушник", "freebuds").any { it in low } -> "Наушники"
                listOf("book", "ноут", "macbook", "ipad", "pad").any { it in low } -> "Ноутбуки"
                listOf("tv", "oled", "qled", "телевизор", "bravia").any { it in low } -> "Телевизоры"
                listOf("zeekr", "byd", "tesla", "su7", "yu7", "авто").any { it in low } -> "Электроавто"
                else -> "Смартфоны"
            }
        }

        val catCode = when (catLabel) {
            "Часы" -> "watch"
            "Наушники" -> "buds"
            "Ноутбуки" -> "laptop"
            "Телевизоры" -> "tv"
            "Электроавто" -> "car"
            else -> "phone"
        }

        val specs = when (catCode) {
            "watch" -> listOf(
                ReviewSpecRow("Экран", "1.5\" LTPO AMOLED, 466×466, сапфировое стекло, 2200 нит"),
                ReviewSpecRow("Корпус и защита", "Титановый сплав / алюминий, 5 ATM + IP68"),
                ReviewSpecRow("Навигация и связь", "Двухчастотный GPS, Bluetooth 5.4, NFC"),
                ReviewSpecRow("Датчики", "Пульс 24/7, SpO2, ЭКГ, мониторинг сна и стресса"),
                ReviewSpecRow("Автономность", "До 10–14 дней в смешанном режиме")
            )
            "buds" -> listOf(
                ReviewSpecRow("Излучатели", "Коаксиальная акустическая система с титановой мембраной"),
                ReviewSpecRow("Аудиокодеки", "LHDC 5.0, LDAC, AAC, SBC"),
                ReviewSpecRow("Шумоподавление", "Адаптивное ANC до 55 дБ + 3 микрофона с ИИ"),
                ReviewSpecRow("Автономность", "До 9.5 ч без ANC, до 40 ч вместе с кейсом"),
                ReviewSpecRow("Связь и защита", "Bluetooth 5.4, Multipoint, защита IP54/IP55")
            )
            "laptop" -> listOf(
                ReviewSpecRow("Экран", "14.5\" OLED 3K (3072×1920), 120 Гц, 100% DCI-P3"),
                ReviewSpecRow("Процессор", "Флагманский мобильный процессор нового поколения"),
                ReviewSpecRow("Память", "32 ГБ LPDDR5X + 1 ТБ NVMe PCIe 4.0 SSD"),
                ReviewSpecRow("Корпус и вес", "Алюминиево-магниевый корпус, 1.18 кг"),
                ReviewSpecRow("Батарея и порты", "75 Вт·ч, быстрая зарядка 100 Вт, Thunderbolt 4 / USB4")
            )
            "tv" -> listOf(
                ReviewSpecRow("Матрица", "65\" QD-Mini LED / OLED 4K (3840×2160), 144 Гц"),
                ReviewSpecRow("Яркость и HDR", "До 1800 нит, Dolby Vision IQ, HDR10+"),
                ReviewSpecRow("Звук", "60 Вт многоканальная акустика, Dolby Atmos"),
                ReviewSpecRow("Интерфейсы", "4× HDMI 2.1 (4K 144 Гц, VRR), Wi-Fi 6/7"),
                ReviewSpecRow("Платформа", "Smart TV с голосовым управлением")
            )
            "car" -> listOf(
                ReviewSpecRow("Силовая установка", "Полный привод AWD, два электромотора"),
                ReviewSpecRow("Динамика", "Разгон 0–100 км/ч за 3.5–3.9 сек"),
                ReviewSpecRow("Батарея", "100 кВт·ч, запас хода до 720 км (CLTC)"),
                ReviewSpecRow("Зарядка", "Архитектура 800 В, 10–80% за 12–15 минут"),
                ReviewSpecRow("Ассистенты", "Лидар + камеры кругового обзора 360°")
            )
            else -> listOf(
                ReviewSpecRow("Дисплей", "6.83\" LTPO AMOLED 1.5K, 1–120 Гц, Dolby Vision, HDR10+"),
                ReviewSpecRow("Процессор", "Флагманский 3-нм / 4-нм чипсет, LPDDR5X, UFS 4.1"),
                ReviewSpecRow("Камеры", "50 МП (OIS) + 50 МП перископ + 50 МП ультраширик"),
                ReviewSpecRow("Батарея и зарядка", "6500 мАч Silicon-Carbon, быстрая зарядка 90–120 Вт"),
                ReviewSpecRow("Защита", "IP68 / IP69, стереодинамики Dolby Atmos, Wi-Fi 7")
            )
        }

        val focusParagraph = if (userFocusNote.isNotBlank()) {
            "\n\nСпециальный акцент по команде пользователя: ${userFocusNote.trim()}."
        } else {
            ""
        }

        return TechReviewArticle(
            id = "manual_${System.currentTimeMillis()}",
            deviceName = cleanName,
            brand = brand,
            category = catCode,
            categoryLabel = catLabel,
            authorAgent = "$assignedAgentName • По заказу владельца",
            approvedByChief = "Утверждено Авророй",
            headline = "$cleanName: подробный обзор возможностей, экрана, автономности и реальных ТТХ",
            leadSummary = if (userFocusNote.isNotBlank()) {
                "Обзор $cleanName ($brand) по вашему заданию: ${userFocusNote.trim()}."
            } else {
                "Детальный разбор $cleanName от $brand: проверяем дисплей, производительность, камеры/звук и реальную автономность."
            },
            fullBody = "$cleanName — одна из самых интересных моделей в актуальной линейке $brand. В ходе разбора команда проверила ключевые узлы устройства: качество матрицы, стабильность работы под нагрузкой, скорость зарядки и беспроводные модули.$focusParagraph",
            specs = specs,
            pros = listOf(
                "Сбалансированные флагманские характеристики в своём классе",
                "Высокая автономность и быстрая зарядка",
                "Современные беспроводные стандарты и защита корпуса"
            ),
            cons = listOf(
                "Итоговая цена зависит от региона поставки",
                "Рекомендуется уточнять комплектацию блока питания"
            ),
            score = "9.3 / 10",
            publishedAt = nowStr,
            accentHex = 0xFF10B981
        )
    }

    fun deputyVerifyAndFixArticle(
        articles: List<TechReviewArticle>,
        newsList: List<NewsItem>
    ): Pair<List<TechReviewArticle>, DeputyEditEntry?> {
        if (articles.isEmpty()) return articles to null
        val nowStr = ZonedDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"))

        val targetIndex = articles.indexOfFirst { it.auditStage < 2 }
            .takeIf { it >= 0 } ?: (articles.indices.random())
        val art = articles[targetIndex]

        val isStageTwoByElena = art.auditStage == 1
        val reviewerName = if (isStageTwoByElena) "Елена (Аудит ТТХ)" else "Виктор (Зам. директора)"

        val stage1Refinements: Map<String, Pair<String, String>> = mapOf(
            "Xiaomi 17T" to ("Защита и интерфейсы" to "IP68, Wi-Fi 7, Bluetooth 6.0, ИК-порт, USB Type-C 2.0"),
            "Huawei Watch Fit 5" to ("Датчики и связь" to "TruSeen пульс 24/7, SpO2, стресс, GPS, Bluetooth 5.3 BLE"),
            "Xiaomi Redmi Buds 8 Pro" to ("Связь и защита" to "Bluetooth 5.4, Google Fast Pair, Multipoint, IP54"),
            "OnePlus 15R" to ("Аккумулятор" to "7400 мАч Silicon-Carbon, SuperVOOC 80 Вт (PPS до 55 Вт)"),
            "Samsung Galaxy S26 Ultra" to ("Батарея" to "5000 мАч, проводная 45 Вт / беспроводная Qi2 25 Вт"),
            "Huawei MateBook X Pro" to ("Порты и батарея" to "2× Thunderbolt 4, 1× USB-C, 70 Вт·ч + адаптер 90 Вт GaN")
        )

        val stage2DeepRefinements: Map<String, Pair<String, String>> = mapOf(
            "Xiaomi 17T" to ("Дисплей" to "6.83\" AMOLED 1.5K (2772×1280), 120 Гц, ШИМ 3840 Гц, HDR10+, Dolby Vision"),
            "Huawei Watch Fit 5" to ("Экран" to "1.82\" AMOLED, 480×408 (347 PPI), 60 Гц, пик 1500 нит"),
            "Xiaomi Redmi Buds 8 Pro" to ("Шумоподавление" to "Адаптивное ANC до 55 дБ (4 кГц) + 3 микрофона с ветрозащитой"),
            "OnePlus 15R" to ("Экран" to "6.83\" плоский LTPS AMOLED 1.5K (2800×1272), 165 Гц, 10-бит")
        )

        val matchingNewsHint = newsList.firstOrNull {
            it.title.contains(art.deviceName, ignoreCase = true) || it.brand.equals(art.brand, ignoreCase = true)
        }?.sourceLabel ?: "официальному даташиту"

        val refinement = if (isStageTwoByElena) stage2DeepRefinements[art.deviceName] else stage1Refinements[art.deviceName]
        val updatedSpecs: List<ReviewSpecRow>
        val fixedFieldLabel: String
        val detailMsg: String

        if (refinement != null) {
            val (fieldLabel, exactValue) = refinement
            fixedFieldLabel = fieldLabel
            var replaced = false
            updatedSpecs = art.specs.map { row ->
                if (row.label.equals(fieldLabel, ignoreCase = true)) {
                    replaced = true
                    row.copy(value = exactValue)
                } else row
            }.let { list ->
                if (replaced) list else list + ReviewSpecRow(fieldLabel, exactValue)
            }
            detailMsg = if (isStageTwoByElena) {
                "2-й этап сверки: уточнены точные цифры в пункте «$fieldLabel» по $matchingNewsHint"
            } else {
                "1-й этап сверки: исправлен пункт «$fieldLabel» по данным $matchingNewsHint"
            }
        } else {
            val targetSpecIdx = if (isStageTwoByElena && art.specs.size > 1) 1 else 0
            val specRow = art.specs.getOrNull(targetSpecIdx) ?: ReviewSpecRow("Спецификация", "Проверено")
            fixedFieldLabel = specRow.label
            val cleanVal = if (specRow.value.contains("сверен")) specRow.value else "${specRow.value} • сверено ($matchingNewsHint)"
            updatedSpecs = art.specs.mapIndexed { idx, row ->
                if (idx == targetSpecIdx) row.copy(value = cleanVal) else row
            }
            detailMsg = if (isStageTwoByElena) {
                "2-й этап сверки: перепроверен параметр «$fixedFieldLabel»"
            } else {
                "1-й этап сверки: проверен параметр «$fixedFieldLabel» по $matchingNewsHint"
            }
        }

        val nextStage = if (art.auditStage < 2) art.auditStage + 1 else 2
        val noteText = if (nextStage >= 2) {
            "Двойная проверка ТТХ: Виктор + Елена ($nowStr)"
        } else {
            "1-я проверка ТТХ: Виктор ($nowStr)"
        }

        val updatedArticle = art.copy(
            specs = updatedSpecs,
            deputyEditedNote = noteText,
            auditStage = nextStage
        )

        val updatedList = articles.toMutableList().apply {
            this[targetIndex] = updatedArticle
        }

        val logEntry = DeputyEditEntry(
            id = "dep_${System.currentTimeMillis()}",
            reviewerName = reviewerName,
            deviceName = art.deviceName,
            fixedField = fixedFieldLabel,
            correctionDetail = detailMsg,
            editedAt = nowStr
        )

        return updatedList to logEntry
    }
}

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
        if (clean.isEmpty()) return "Нажмите «Читать ↗», чтобы открыть материал в источнике."
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

data class PersistedOfficeState(
    val ticketCounter: Int,
    val agentStatuses: Map<Int, AgentLiveStatus>,
    val chiefAuditLog: List<ChiefAuditEntry>,
    val deputyEditLog: List<DeputyEditEntry>
)

object LocalCache {
    private const val PREFS = "newspulse_ai_prefs"
    private const val KEY_SAVED = "saved_news"
    private const val KEY_CACHE = "cached_news_ru_v8"
    private const val KEY_SETTINGS = "user_app_settings_v5"
    private const val KEY_REVIEWS = "persisted_reviews_v2"
    private const val KEY_OFFICE = "persisted_ai_office_v2"
    private const val KEY_DELETED_NEWS = "deleted_news_urls_v1"
    private const val KEY_DELETED_REVIEWS = "deleted_review_ids_v1"

    fun getDeletedNewsUrls(context: Context): Set<String> {
        val raw = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY_DELETED_NEWS, null) ?: return emptySet()
        return try {
            val arr = JSONArray(raw)
            (0 until arr.length()).map { arr.getString(it) }.toSet()
        } catch (_: Exception) {
            emptySet()
        }
    }

    fun addDeletedNewsUrl(context: Context, url: String) {
        val current = getDeletedNewsUrls(context).toMutableSet()
        current.add(url)
        val arr = JSONArray().apply { current.forEach { put(it) } }
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putString(KEY_DELETED_NEWS, arr.toString()).apply()
    }

    fun clearDeletedNewsUrls(context: Context) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().remove(KEY_DELETED_NEWS).apply()
    }

    fun getDeletedReviewIds(context: Context): Set<String> {
        val raw = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY_DELETED_REVIEWS, null) ?: return emptySet()
        return try {
            val arr = JSONArray(raw)
            (0 until arr.length()).map { arr.getString(it) }.toSet()
        } catch (_: Exception) {
            emptySet()
        }
    }

    fun addDeletedReviewId(context: Context, id: String) {
        val current = getDeletedReviewIds(context).toMutableSet()
        current.add(id)
        val arr = JSONArray().apply { current.forEach { put(it) } }
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putString(KEY_DELETED_REVIEWS, arr.toString()).apply()
    }

    fun clearDeletedReviewIds(context: Context) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().remove(KEY_DELETED_REVIEWS).apply()
    }

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
        val deleted = getDeletedNewsUrls(context)
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
            }.filterNot { deleted.contains(it.url) }
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun saveReviews(context: Context, reviews: List<TechReviewArticle>) {
        val arr = JSONArray()
        reviews.forEach { art ->
            val specsArr = JSONArray()
            art.specs.forEach { sp ->
                specsArr.put(JSONObject().apply {
                    put("label", sp.label)
                    put("value", sp.value)
                })
            }
            val prosArr = JSONArray().apply { art.pros.forEach { put(it) } }
            val consArr = JSONArray().apply { art.cons.forEach { put(it) } }

            arr.put(JSONObject().apply {
                put("id", art.id)
                put("deviceName", art.deviceName)
                put("brand", art.brand)
                put("category", art.category)
                put("categoryLabel", art.categoryLabel)
                put("authorAgent", art.authorAgent)
                put("approvedByChief", art.approvedByChief)
                put("headline", art.headline)
                put("leadSummary", art.leadSummary)
                put("fullBody", art.fullBody)
                put("specs", specsArr)
                put("pros", prosArr)
                put("cons", consArr)
                put("score", art.score)
                put("publishedAt", art.publishedAt)
                put("accentHex", art.accentHex)
                put("deputyEditedNote", art.deputyEditedNote)
                put("auditStage", art.auditStage)
            })
        }
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putString(KEY_REVIEWS, arr.toString()).apply()
    }

    fun loadReviews(context: Context): List<TechReviewArticle> {
        val deletedIds = getDeletedReviewIds(context)
        val raw = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY_REVIEWS, null)
        if (raw.isNullOrBlank()) {
            val initial = ReviewCatalogFactory.initialReviews().filterNot { deletedIds.contains(it.id) }
            saveReviews(context, initial)
            return initial
        }
        return try {
            val arr = JSONArray(raw)
            if (arr.length() == 0) return emptyList()
            val parsed = List(arr.length()) { i ->
                val o = arr.getJSONObject(i)
                val specsArr = o.getJSONArray("specs")
                val specs = List(specsArr.length()) { j ->
                    val so = specsArr.getJSONObject(j)
                    ReviewSpecRow(so.getString("label"), so.getString("value"))
                }
                val prosArr = o.getJSONArray("pros")
                val pros = List(prosArr.length()) { j -> prosArr.getString(j) }
                val consArr = o.getJSONArray("cons")
                val cons = List(consArr.length()) { j -> consArr.getString(j) }

                TechReviewArticle(
                    id = o.getString("id"),
                    deviceName = o.getString("deviceName"),
                    brand = o.getString("brand"),
                    category = o.getString("category"),
                    categoryLabel = o.getString("categoryLabel"),
                    authorAgent = o.getString("authorAgent"),
                    approvedByChief = o.getString("approvedByChief"),
                    headline = o.getString("headline"),
                    leadSummary = o.getString("leadSummary"),
                    fullBody = o.getString("fullBody"),
                    specs = specs,
                    pros = pros,
                    cons = cons,
                    score = o.getString("score"),
                    publishedAt = o.getString("publishedAt"),
                    accentHex = o.getLong("accentHex"),
                    deputyEditedNote = o.optString("deputyEditedNote", ""),
                    auditStage = o.optInt("auditStage", 0)
                )
            }
            // Автоматически очищаем старые самовольно сгенерированные обзоры из заголовков новостей и удалённые обзоры
            val cleaned = parsed.filterNot {
                deletedIds.contains(it.id) || ReviewCatalogFactory.isAutoGeneratedNewsJunk(it)
            }
            if (cleaned.size != parsed.size) {
                saveReviews(context, cleaned)
            }
            cleaned
        } catch (_: Exception) {
            ReviewCatalogFactory.initialReviews().filterNot { deletedIds.contains(it.id) }
        }
    }

    private fun serializeTask(t: SoloTaskItem): JSONObject = JSONObject().apply {
        put("ticketNumber", t.ticketNumber)
        put("badge", t.badge)
        put("title", t.title)
        put("resultSummary", t.resultSummary)
        put("startedAt", t.startedAt)
        put("completedAt", t.completedAt)
        put("chiefApproved", t.chiefApproved)
        put("salaryDelta", t.salaryDelta)
        put("rejectReason", t.rejectReason)
    }

    private fun deserializeTask(o: JSONObject): SoloTaskItem = SoloTaskItem(
        ticketNumber = o.getInt("ticketNumber"),
        badge = o.getString("badge"),
        title = o.getString("title"),
        resultSummary = o.getString("resultSummary"),
        startedAt = o.getString("startedAt"),
        completedAt = o.optString("completedAt", ""),
        chiefApproved = o.optBoolean("chiefApproved", true),
        salaryDelta = o.optInt("salaryDelta", 0),
        rejectReason = o.optString("rejectReason", "")
    )

    fun saveOfficeState(
        context: Context,
        ticketCounter: Int,
        agentStatuses: Map<Int, AgentLiveStatus>,
        chiefAuditLog: List<ChiefAuditEntry>,
        deputyEditLog: List<DeputyEditEntry>
    ) {
        try {
            val agentsObj = JSONObject()
            agentStatuses.forEach { (id, st) ->
                val histArr = JSONArray()
                st.completedHistory.forEach { histArr.put(serializeTask(it)) }
                agentsObj.put(id.toString(), JSONObject().apply {
                    put("currentTask", serializeTask(st.currentTask))
                    put("progress", st.progress.toDouble())
                    put("targetZone", st.targetZone.name)
                    put("breakSecondsRemaining", st.breakSecondsRemaining.toDouble())
                    put("salaryBalance", st.salaryBalance)
                    put("rejectionsCount", st.rejectionsCount)
                    put("reprimandsCount", st.reprimandsCount)
                    put("completedHistory", histArr)
                })
            }

            val chiefArr = JSONArray()
            chiefAuditLog.forEach { c ->
                chiefArr.put(JSONObject().apply {
                    put("id", c.id)
                    put("ticketNumber", c.ticketNumber)
                    put("agentName", c.agentName)
                    put("taskTitle", c.taskTitle)
                    put("verdict", c.verdict)
                    put("salaryDelta", c.salaryDelta)
                    put("hasReprimand", c.hasReprimand)
                    put("isApproved", c.isApproved)
                    put("checkedAt", c.checkedAt)
                })
            }

            val depArr = JSONArray()
            deputyEditLog.forEach { d ->
                depArr.put(JSONObject().apply {
                    put("id", d.id)
                    put("reviewerName", d.reviewerName)
                    put("deviceName", d.deviceName)
                    put("fixedField", d.fixedField)
                    put("correctionDetail", d.correctionDetail)
                    put("editedAt", d.editedAt)
                })
            }

            val root = JSONObject().apply {
                put("ticketCounter", ticketCounter)
                put("agents", agentsObj)
                put("chiefLog", chiefArr)
                put("deputyLog", depArr)
            }
            context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .edit().putString(KEY_OFFICE, root.toString()).apply()
        } catch (_: Exception) {
        }
    }

    fun loadOfficeState(context: Context): PersistedOfficeState? {
        val raw = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY_OFFICE, null) ?: return null
        return try {
            val root = JSONObject(raw)
            val ticketCounter = root.getInt("ticketCounter")
            val agentsObj = root.getJSONObject("agents")
            val statuses = mutableMapOf<Int, AgentLiveStatus>()
            val keys = agentsObj.keys()
            while (keys.hasNext()) {
                val k = keys.next()
                val id = k.toInt()
                val ao = agentsObj.getJSONObject(k)
                val histArr = ao.getJSONArray("completedHistory")
                val hist = List(histArr.length()) { idx -> deserializeTask(histArr.getJSONObject(idx)) }
                val zoneName = ao.optString("targetZone", AgentLocationZone.DESK.name)
                val zone = try {
                    AgentLocationZone.valueOf(zoneName)
                } catch (_: Exception) {
                    AgentLocationZone.DESK
                }
                statuses[id] = AgentLiveStatus(
                    currentTask = deserializeTask(ao.getJSONObject("currentTask")),
                    progress = ao.getDouble("progress").toFloat(),
                    targetZone = zone,
                    breakSecondsRemaining = ao.getDouble("breakSecondsRemaining").toFloat(),
                    salaryBalance = ao.getInt("salaryBalance"),
                    rejectionsCount = ao.getInt("rejectionsCount"),
                    reprimandsCount = ao.getInt("reprimandsCount"),
                    completedHistory = hist
                )
            }

            val chiefArr = root.getJSONArray("chiefLog")
            val chiefLog = List(chiefArr.length()) { idx ->
                val co = chiefArr.getJSONObject(idx)
                ChiefAuditEntry(
                    id = co.getString("id"),
                    ticketNumber = co.getInt("ticketNumber"),
                    agentName = co.getString("agentName"),
                    taskTitle = co.getString("taskTitle"),
                    verdict = co.getString("verdict"),
                    salaryDelta = co.getInt("salaryDelta"),
                    hasReprimand = co.getBoolean("hasReprimand"),
                    isApproved = co.getBoolean("isApproved"),
                    checkedAt = co.getString("checkedAt")
                )
            }

            val depArr = root.optJSONArray("deputyLog") ?: JSONArray()
            val depLog = List(depArr.length()) { idx ->
                val doj = depArr.getJSONObject(idx)
                DeputyEditEntry(
                    id = doj.getString("id"),
                    reviewerName = doj.optString("reviewerName", "Виктор"),
                    deviceName = doj.getString("deviceName"),
                    fixedField = doj.getString("fixedField"),
                    correctionDetail = doj.getString("correctionDetail"),
                    editedAt = doj.getString("editedAt")
                )
            }

            PersistedOfficeState(
                ticketCounter = ticketCounter,
                agentStatuses = statuses,
                chiefAuditLog = chiefLog,
                deputyEditLog = depLog
            )
        } catch (_: Exception) {
            null
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
                cornerRadius = o.optInt("cornerRadius", 16),
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

    var deletedNewsUrls by remember { mutableStateOf(LocalCache.getDeletedNewsUrls(context)) }
    var deletedReviewIds by remember { mutableStateOf(LocalCache.getDeletedReviewIds(context)) }

    var newsList by remember { mutableStateOf(LocalCache.getCached(context)) }
    var savedList by remember { mutableStateOf(LocalCache.getSaved(context)) }
    var reviewArticles by remember { mutableStateOf(LocalCache.loadReviews(context)) }
    var openedReviewId by remember { mutableStateOf<String?>(null) }
    var showCreateReviewDialog by remember { mutableStateOf(false) }

    var isLoading by remember { mutableStateOf(false) }
    var lastUpdatedText by remember { mutableStateOf(if (newsList.isNotEmpty()) "${newsList.size} новостей о технике" else "Загрузка...") }

    var selectedTab by remember { mutableIntStateOf(0) }
    var searchQuery by remember { mutableStateOf("") }
    var isSearchExpanded by remember { mutableStateOf(false) }
    var showFilterSheet by remember { mutableStateOf(false) }

    var freshOnly by remember { mutableStateOf(false) }
    var selectedPlatform by remember { mutableStateOf("Все") }
    var selectedBrand by remember { mutableStateOf("Все") }

    var isReviewFiltersExpanded by remember { mutableStateOf(false) }
    var selectedReviewBrand by remember { mutableStateOf("Все") }
    var selectedReviewDeviceType by remember { mutableStateOf("Все") }

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
            val currentDeleted = LocalCache.getDeletedNewsUrls(context)
            deletedNewsUrls = currentDeleted
            val fresh = NewsRepository.loadAllNews().filterNot { currentDeleted.contains(it.url) }
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

    fun deleteNewsItem(item: NewsItem) {
        LocalCache.addDeletedNewsUrl(context, item.url)
        deletedNewsUrls = LocalCache.getDeletedNewsUrls(context)
        val updatedNews = newsList.filterNot { it.url == item.url }
        val updatedSaved = savedList.filterNot { it.url == item.url }
        newsList = updatedNews
        savedList = updatedSaved
        LocalCache.setCached(context, updatedNews)
        LocalCache.setSaved(context, updatedSaved)
        Toast.makeText(context, "Пост удалён", Toast.LENGTH_SHORT).show()
    }

    fun deleteReviewItem(article: TechReviewArticle) {
        LocalCache.addDeletedReviewId(context, article.id)
        deletedReviewIds = LocalCache.getDeletedReviewIds(context)
        val updated = reviewArticles.filterNot { it.id == article.id }
        reviewArticles = updated
        LocalCache.saveReviews(context, updated)
        if (openedReviewId == article.id) openedReviewId = null
        Toast.makeText(context, "Обзор «${article.deviceName}» удалён", Toast.LENGTH_SHORT).show()
    }

    LaunchedEffect(Unit) {
        refreshFeed()
    }

    val brandCounts = remember(newsList) {
        brands.associateWith { b ->
            if (b == "Все") newsList.size else newsList.count { it.brand == b }
        }
    }

    val displayedItems = remember(newsList, savedList, selectedTab, searchQuery, freshOnly, selectedPlatform, selectedBrand, deletedNewsUrls) {
        val baseList = if (selectedTab == 2) savedList else newsList
        val q = searchQuery.trim().lowercase()
        baseList.filter { item ->
            if (deletedNewsUrls.contains(item.url)) return@filter false
            if (selectedTab == 2) return@filter true
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

    val openedReview = remember(reviewArticles, openedReviewId) {
        openedReviewId?.let { id -> reviewArticles.find { it.id == id } }
    }

    if (openedReview != null) {
        ReviewDetailDialog(
            article = openedReview,
            onShare = { shareReviewArticle(context, openedReview) },
            onDelete = { deleteReviewItem(openedReview) },
            onDismiss = { openedReviewId = null }
        )
    }

    if (showCreateReviewDialog) {
        CreateReviewCommandDialog(
            onCreateReview = { deviceName, brand, categoryLabel, focusNote, agentName ->
                val newRev = ReviewCatalogFactory.createCustomReviewByUserOrder(
                    rawDeviceName = deviceName,
                    selectedBrand = brand,
                    selectedCategoryLabel = categoryLabel,
                    userFocusNote = focusNote,
                    assignedAgentName = agentName
                )
                val updated = listOf(newRev) + reviewArticles
                reviewArticles = updated
                LocalCache.saveReviews(context, updated)
                showCreateReviewDialog = false
                Toast.makeText(
                    context,
                    "$agentName подготовил(а) обзор: ${newRev.deviceName}",
                    Toast.LENGTH_SHORT
                ).show()
            },
            onDismiss = { showCreateReviewDialog = false }
        )
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
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
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
                                fontSize = 19.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onBackground,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        val subInfo = when (selectedTab) {
                            1 -> "Обзоры и ТТХ • ${reviewArticles.size} шт. (только по заказу)"
                            2 -> "Сохранённые статьи • ${savedList.size}"
                            3 -> "Темы оформления и управление ИИ-Домом"
                            else -> buildString {
                                append(lastUpdatedText)
                                if (selectedBrand != "Все") append(" • $selectedBrand")
                                if (selectedPlatform != "Все") append(" • $selectedPlatform")
                            }
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
                                modifier = Modifier.size(36.dp),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(
                                    imageVector = if (isSearchExpanded) Icons.Default.Close else Icons.Default.Search,
                                    contentDescription = "Поиск",
                                    tint = if (isSearchExpanded || searchQuery.isNotEmpty()) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.size(17.dp)
                                )
                            }

                            Box {
                                FilledTonalIconButton(
                                    onClick = { showFilterSheet = true },
                                    modifier = Modifier.size(36.dp),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Tune,
                                        contentDescription = "Фильтры",
                                        tint = if (hasActiveFilters) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.size(17.dp)
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
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable(enabled = !isLoading) { refreshFeed() },
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.primary
                            ) {
                                Box(
                                    modifier = Modifier.size(36.dp),
                                    contentAlignment = Alignment.Center
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
                                            contentDescription = "Обновить",
                                            tint = MaterialTheme.colorScheme.onPrimary,
                                            modifier = Modifier.size(17.dp)
                                        )
                                    }
                                }
                            }
                        }
                    } else if (selectedTab == 1) {
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { showCreateReviewDialog = true },
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.primary
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(Modifier.width(5.dp))
                                Text(
                                    "+ Заказать обзор",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }

                AnimatedVisibility(visible = selectedTab == 0 && isSearchExpanded) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        shape = RoundedCornerShape(12.dp),
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
                reviewsCount = reviewArticles.size,
                savedCount = savedList.size,
                onSelectTab = { selectedTab = it }
            )
        }
    ) { innerPadding ->
        when (selectedTab) {
            1 -> {
                val reviewBrands = listOf("Все", "Xiaomi", "Huawei", "Samsung", "Apple", "OnePlus", "HONOR", "Google", "vivo", "OPPO", "realme", "Nothing", "Электроавто")
                val filteredReviews = remember(reviewArticles, selectedReviewBrand, selectedReviewDeviceType) {
                    reviewArticles.filter { art ->
                        val matchBrand = selectedReviewBrand == "Все" || art.brand == selectedReviewBrand
                        val matchType = selectedReviewDeviceType == "Все" || art.categoryLabel == selectedReviewDeviceType
                        matchBrand && matchType
                    }
                }
                val hasReviewFilter = selectedReviewBrand != "Все" || selectedReviewDeviceType != "Все"

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = innerPadding.calculateTopPadding()),
                    contentPadding = PaddingValues(
                        start = 14.dp,
                        end = 14.dp,
                        top = 4.dp,
                        bottom = innerPadding.calculateBottomPadding() + 12.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surface,
                            border = BorderStroke(
                                1.dp,
                                if (hasReviewFilter) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                                else MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                            )
                        ) {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { isReviewFiltersExpanded = !isReviewFiltersExpanded }
                                        .padding(horizontal = 12.dp, vertical = 9.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        modifier = Modifier.weight(1f).padding(end = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.FilterList,
                                            contentDescription = "Фильтры обзоров",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(17.dp)
                                        )
                                        Spacer(Modifier.width(7.dp))
                                        Text(
                                            text = if (!hasReviewFilter) "Фильтры: Все бренды и техника"
                                            else "Фильтр: $selectedReviewBrand • $selectedReviewDeviceType",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        if (hasReviewFilter) {
                                            Text(
                                                text = "Сброс",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier
                                                    .padding(end = 8.dp)
                                                    .clickable {
                                                        selectedReviewBrand = "Все"
                                                        selectedReviewDeviceType = "Все"
                                                    }
                                            )
                                        }
                                        Icon(
                                            imageVector = if (isReviewFiltersExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                            contentDescription = if (isReviewFiltersExpanded) "Скрыть фильтры" else "Открыть фильтры",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }

                                AnimatedVisibility(visible = isReviewFiltersExpanded) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(start = 12.dp, end = 12.dp, bottom = 10.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                                        Text(
                                            text = "Бренд:",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                        )
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .horizontalScroll(rememberScrollState()),
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            reviewBrands.forEach { rb ->
                                                CompactPillChip(
                                                    label = rb,
                                                    selected = selectedReviewBrand == rb,
                                                    onClick = { selectedReviewBrand = rb }
                                                )
                                            }
                                        }

                                        Text(
                                            text = if (selectedReviewBrand == "Все") "Тип техники:" else "Техника $selectedReviewBrand:",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .horizontalScroll(rememberScrollState()),
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            ReviewCatalogFactory.allDeviceTypes.forEach { devType ->
                                                val countInBrand = reviewArticles.count { art ->
                                                    (selectedReviewBrand == "Все" || art.brand == selectedReviewBrand) &&
                                                        (devType == "Все" || art.categoryLabel == devType)
                                                }
                                                val chipTitle = if (devType == "Все") "Вся техника ($countInBrand)" else "$devType ($countInBrand)"
                                                CompactPillChip(
                                                    label = chipTitle,
                                                    selected = selectedReviewDeviceType == devType,
                                                    onClick = { selectedReviewDeviceType = devType }
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if (filteredReviews.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 48.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "По фильтру «$selectedReviewBrand • $selectedReviewDeviceType» обзоров пока нет",
                                    fontSize = 13.sp,
                                    textAlign = TextAlign.Center,
                                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.65f)
                                )
                            }
                        }
                    } else {
                        items(filteredReviews, key = { it.id }) { article ->
                            ReviewArticleCard(
                                article = article,
                                settings = settings,
                                onOpen = { openedReviewId = article.id },
                                onShare = { shareReviewArticle(context, article) },
                                onDelete = { deleteReviewItem(article) }
                            )
                        }
                    }
                }
            }
            3 -> {
                SettingsScreen(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = innerPadding.calculateTopPadding()),
                    bottomPadding = innerPadding.calculateBottomPadding() + 12.dp,
                    settings = settings,
                    newsList = newsList,
                    existingReviews = reviewArticles,
                    deletedTotalCount = deletedNewsUrls.size + deletedReviewIds.size,
                    isLoading = isLoading,
                    onSettingsChange = onSettingsChange,
                    onTriggerScan = { refreshFeed() },
                    onNewReviewOrdered = { orderedArticle ->
                        val updated = listOf(orderedArticle) + reviewArticles
                        reviewArticles = updated
                        LocalCache.saveReviews(context, updated)
                    },
                    onReviewsSilentEdited = { editedList ->
                        reviewArticles = editedList
                        LocalCache.saveReviews(context, editedList)
                    },
                    onPurgeAutoJunk = {
                        val cleaned = reviewArticles.filterNot { ReviewCatalogFactory.isAutoGeneratedNewsJunk(it) }
                        reviewArticles = cleaned
                        LocalCache.saveReviews(context, cleaned)
                    },
                    onRestoreDeleted = {
                        LocalCache.clearDeletedNewsUrls(context)
                        LocalCache.clearDeletedReviewIds(context)
                        deletedNewsUrls = emptySet()
                        deletedReviewIds = emptySet()
                        reviewArticles = LocalCache.loadReviews(context)
                        refreshFeed()
                        Toast.makeText(context, "Удалённые посты и обзоры восстановлены", Toast.LENGTH_SHORT).show()
                    },
                    onClearCache = {
                        newsList = emptyList()
                        LocalCache.setCached(context, emptyList())
                        refreshFeed()
                    }
                )
            }
            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = innerPadding.calculateTopPadding()),
                    contentPadding = PaddingValues(
                        start = 14.dp,
                        end = 14.dp,
                        top = 4.dp,
                        bottom = innerPadding.calculateBottomPadding() + 12.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(9.dp)
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
                                    else if (selectedTab == 2) "В избранном пока пусто"
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
                                onDelete = { deleteNewsItem(item) },
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
}

@Composable
fun CreateReviewCommandDialog(
    onCreateReview: (deviceName: String, brand: String, categoryLabel: String, focusNote: String, agentName: String) -> Unit,
    onDismiss: () -> Unit
) {
    val primary = MaterialTheme.colorScheme.primary
    var deviceName by remember { mutableStateOf("") }
    var focusNote by remember { mutableStateOf("") }
    var selectedBrand by remember { mutableStateOf("Авто") }
    var selectedCategory by remember { mutableStateOf("Авто") }
    var selectedAgent by remember { mutableStateOf("Вика") }

    val agentOptions = listOf("Вика", "Артур", "Нова", "Дом-Бот", "Авто-ИИ")
    val brandOptions = listOf("Авто", "Xiaomi", "Samsung", "Huawei", "Apple", "HONOR", "OnePlus", "Google", "OPPO", "vivo", "realme", "Nothing", "Электроавто")
    val categoryOptions = listOf("Авто") + ReviewCatalogFactory.creatableDeviceTypes

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 18.dp),
            shape = RoundedCornerShape(22.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.2.dp, primary.copy(alpha = 0.45f))
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
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Заказать обзор агенту",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Агенты создают обзоры только по вашей команде",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Закрыть")
                    }
                }

                OutlinedTextField(
                    value = deviceName,
                    onValueChange = { deviceName = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Название модели устройства", fontSize = 12.sp) },
                    placeholder = { Text("Например: Xiaomi 17T Pro", fontSize = 13.sp) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Быстрый выбор модели:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        ReviewCatalogFactory.quickSuggestedDevices.forEach { dev ->
                            CompactPillChip(
                                label = dev,
                                selected = deviceName.equals(dev, ignoreCase = true),
                                onClick = { deviceName = dev }
                            )
                        }
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Назначить агента:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = primary
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        agentOptions.forEach { ag ->
                            CompactPillChip(
                                label = ag,
                                selected = selectedAgent == ag,
                                onClick = { selectedAgent = ag }
                            )
                        }
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Бренд:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        brandOptions.forEach { br ->
                            CompactPillChip(
                                label = br,
                                selected = selectedBrand == br,
                                onClick = { selectedBrand = br }
                            )
                        }
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Категория техники:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        categoryOptions.forEach { cat ->
                            CompactPillChip(
                                label = cat,
                                selected = selectedCategory == cat,
                                onClick = { selectedCategory = cat }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = focusNote,
                    onValueChange = { focusNote = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Особые указания агенту (необязательно)", fontSize = 12.sp) },
                    placeholder = { Text("Например: сравнить камеру и автономность", fontSize = 12.sp) },
                    maxLines = 2,
                    shape = RoundedCornerShape(12.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.End)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Отмена", fontSize = 12.sp)
                    }
                    Button(
                        onClick = {
                            if (deviceName.isNotBlank()) {
                                onCreateReview(deviceName, selectedBrand, selectedCategory, focusNote, selectedAgent)
                            }
                        },
                        enabled = deviceName.isNotBlank(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Отправить агенту", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun CompactPillChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val primary = MaterialTheme.colorScheme.primary
    val bg = if (selected) primary else MaterialTheme.colorScheme.background.copy(alpha = 0.75f)
    val fg = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f)

    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(9.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(9.dp),
        color = bg,
        border = BorderStroke(1.dp, if (selected) primary else primary.copy(alpha = 0.22f))
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            color = fg,
            maxLines = 1,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
        )
    }
}

@Composable
fun DeviceIllustrationBanner(
    category: String,
    brand: String,
    deviceName: String,
    accentColor: Color,
    heightDp: Int = 122
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(heightDp.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF0F172A),
                        accentColor.copy(alpha = 0.24f),
                        Color(0xFF1E293B)
                    )
                )
            )
            .border(1.dp, accentColor.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            drawCircle(
                color = accentColor.copy(alpha = 0.14f),
                radius = h * 0.65f,
                center = Offset(w * 0.81f, h * 0.5f)
            )
            drawCircle(
                color = accentColor.copy(alpha = 0.22f),
                radius = h * 0.42f,
                center = Offset(w * 0.81f, h * 0.5f),
                style = Stroke(width = 1.4.dp.toPx())
            )

            val cx = w * 0.81f
            val cy = h * 0.5f

            when (category) {
                "watch" -> {
                    drawRoundRect(
                        color = Color(0xFF334155),
                        topLeft = Offset(cx - 13.dp.toPx(), cy - 40.dp.toPx()),
                        size = Size(26.dp.toPx(), 80.dp.toPx()),
                        cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                    )
                    drawRoundRect(
                        color = Color(0xFF090D16),
                        topLeft = Offset(cx - 21.dp.toPx(), cy - 24.dp.toPx()),
                        size = Size(42.dp.toPx(), 48.dp.toPx()),
                        cornerRadius = CornerRadius(11.dp.toPx(), 11.dp.toPx())
                    )
                    drawRoundRect(
                        color = accentColor,
                        topLeft = Offset(cx - 21.dp.toPx(), cy - 24.dp.toPx()),
                        size = Size(42.dp.toPx(), 48.dp.toPx()),
                        cornerRadius = CornerRadius(11.dp.toPx(), 11.dp.toPx()),
                        style = Stroke(width = 1.8.dp.toPx())
                    )
                    drawCircle(accentColor, 11.dp.toPx(), Offset(cx, cy), style = Stroke(width = 2.5.dp.toPx()))
                    drawCircle(Color(0xFF38BDF8), 7.dp.toPx(), Offset(cx, cy), style = Stroke(width = 2.dp.toPx()))
                }
                "buds" -> {
                    drawRoundRect(
                        color = Color(0xFF1E293B),
                        topLeft = Offset(cx - 26.dp.toPx(), cy - 17.dp.toPx()),
                        size = Size(36.dp.toPx(), 38.dp.toPx()),
                        cornerRadius = CornerRadius(11.dp.toPx(), 11.dp.toPx())
                    )
                    drawRoundRect(
                        color = accentColor,
                        topLeft = Offset(cx - 26.dp.toPx(), cy - 17.dp.toPx()),
                        size = Size(36.dp.toPx(), 38.dp.toPx()),
                        cornerRadius = CornerRadius(11.dp.toPx(), 11.dp.toPx()),
                        style = Stroke(width = 1.5.dp.toPx())
                    )
                    drawCircle(Color(0xFF090D16), 9.dp.toPx(), Offset(cx + 21.dp.toPx(), cy - 7.dp.toPx()))
                    drawCircle(accentColor, 9.dp.toPx(), Offset(cx + 21.dp.toPx(), cy - 7.dp.toPx()), style = Stroke(width = 1.8.dp.toPx()))
                    drawRoundRect(
                        color = accentColor,
                        topLeft = Offset(cx + 18.dp.toPx(), cy),
                        size = Size(5.dp.toPx(), 18.dp.toPx()),
                        cornerRadius = CornerRadius(2.5.dp.toPx(), 2.5.dp.toPx())
                    )
                }
                "tv" -> {
                    drawRoundRect(
                        color = Color(0xFF090D16),
                        topLeft = Offset(cx - 38.dp.toPx(), cy - 24.dp.toPx()),
                        size = Size(76.dp.toPx(), 44.dp.toPx()),
                        cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                    )
                    drawRoundRect(
                        color = accentColor,
                        topLeft = Offset(cx - 38.dp.toPx(), cy - 24.dp.toPx()),
                        size = Size(76.dp.toPx(), 44.dp.toPx()),
                        cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx()),
                        style = Stroke(width = 1.8.dp.toPx())
                    )
                    drawLine(
                        color = Color(0xFF94A3B8),
                        start = Offset(cx - 16.dp.toPx(), cy + 25.dp.toPx()),
                        end = Offset(cx + 16.dp.toPx(), cy + 25.dp.toPx()),
                        strokeWidth = 2.8.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                }
                "car" -> {
                    drawRoundRect(
                        color = accentColor.copy(alpha = 0.85f),
                        topLeft = Offset(cx - 38.dp.toPx(), cy - 4.dp.toPx()),
                        size = Size(76.dp.toPx(), 19.dp.toPx()),
                        cornerRadius = CornerRadius(9.dp.toPx(), 9.dp.toPx())
                    )
                    drawRoundRect(
                        color = Color(0xFF0F172A),
                        topLeft = Offset(cx - 22.dp.toPx(), cy - 15.dp.toPx()),
                        size = Size(44.dp.toPx(), 15.dp.toPx()),
                        cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx())
                    )
                    drawCircle(Color(0xFF090D16), 8.dp.toPx(), Offset(cx - 22.dp.toPx(), cy + 15.dp.toPx()))
                    drawCircle(Color.White, 4.dp.toPx(), Offset(cx - 22.dp.toPx(), cy + 15.dp.toPx()))
                    drawCircle(Color(0xFF090D16), 8.dp.toPx(), Offset(cx + 22.dp.toPx(), cy + 15.dp.toPx()))
                    drawCircle(Color.White, 4.dp.toPx(), Offset(cx + 22.dp.toPx(), cy + 15.dp.toPx()))
                }
                "laptop" -> {
                    drawRoundRect(
                        color = Color(0xFF090D16),
                        topLeft = Offset(cx - 32.dp.toPx(), cy - 22.dp.toPx()),
                        size = Size(64.dp.toPx(), 40.dp.toPx()),
                        cornerRadius = CornerRadius(5.dp.toPx(), 5.dp.toPx())
                    )
                    drawRoundRect(
                        color = accentColor,
                        topLeft = Offset(cx - 32.dp.toPx(), cy - 22.dp.toPx()),
                        size = Size(64.dp.toPx(), 40.dp.toPx()),
                        cornerRadius = CornerRadius(5.dp.toPx(), 5.dp.toPx()),
                        style = Stroke(width = 1.6.dp.toPx())
                    )
                    drawRoundRect(
                        color = Color(0xFF94A3B8),
                        topLeft = Offset(cx - 38.dp.toPx(), cy + 18.dp.toPx()),
                        size = Size(76.dp.toPx(), 4.dp.toPx()),
                        cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
                    )
                }
                else -> {
                    drawRoundRect(
                        color = Color(0xFF090D16),
                        topLeft = Offset(cx - 20.dp.toPx(), cy - 36.dp.toPx()),
                        size = Size(40.dp.toPx(), 72.dp.toPx()),
                        cornerRadius = CornerRadius(9.dp.toPx(), 9.dp.toPx())
                    )
                    drawRoundRect(
                        color = accentColor,
                        topLeft = Offset(cx - 20.dp.toPx(), cy - 36.dp.toPx()),
                        size = Size(40.dp.toPx(), 72.dp.toPx()),
                        cornerRadius = CornerRadius(9.dp.toPx(), 9.dp.toPx()),
                        style = Stroke(width = 1.8.dp.toPx())
                    )
                    drawRoundRect(
                        color = Color(0xFF1E293B),
                        topLeft = Offset(cx - 15.dp.toPx(), cy - 30.dp.toPx()),
                        size = Size(19.dp.toPx(), 21.dp.toPx()),
                        cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                    )
                    drawCircle(accentColor, 3.4.dp.toPx(), Offset(cx - 8.dp.toPx(), cy - 24.dp.toPx()))
                    drawCircle(Color(0xFF38BDF8), 3.4.dp.toPx(), Offset(cx - 8.dp.toPx(), cy - 14.dp.toPx()))
                }
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .fillMaxWidth(0.64f)
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = accentColor.copy(alpha = 0.22f)
            ) {
                Text(
                    text = "$brand • ОБЗОР И ТТХ",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = accentColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
            Text(
                text = deviceName,
                fontSize = 15.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "Проверено главным редактором",
                fontSize = 10.sp,
                color = Color.White.copy(alpha = 0.72f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun ReviewArticleCard(
    article: TechReviewArticle,
    settings: UserAppSettings,
    onOpen: () -> Unit,
    onShare: () -> Unit,
    onDelete: () -> Unit
) {
    val accent = Color(article.accentHex)
    val scale = settings.fontScale

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpen() },
        shape = RoundedCornerShape(settings.cornerRadius.dp),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.28f)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(13.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f).padding(end = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Verified,
                        contentDescription = null,
                        tint = Color(0xFF10B981),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(Modifier.width(5.dp))
                    Text(
                        text = "${article.categoryLabel} • ${article.authorAgent}",
                        fontSize = (10 * scale).sp,
                        fontWeight = FontWeight.Bold,
                        color = accent,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = accent.copy(alpha = 0.16f)
                ) {
                    Text(
                        text = article.score,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = accent,
                        maxLines = 1,
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                    )
                }
            }

            DeviceIllustrationBanner(
                category = article.category,
                brand = article.brand,
                deviceName = article.deviceName,
                accentColor = accent,
                heightDp = 112
            )

            Text(
                text = article.headline,
                fontSize = (15 * scale).sp,
                fontWeight = FontWeight.ExtraBold,
                lineHeight = (20 * scale).sp,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = article.leadSummary,
                fontSize = (13 * scale).sp,
                lineHeight = (18 * scale).sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.82f)
            )

            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.background.copy(alpha = 0.72f)
            ) {
                Column(
                    modifier = Modifier.padding(9.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    article.specs.take(3).forEach { sp ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = sp.label,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.62f),
                                maxLines = 1,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                            Text(
                                text = sp.value,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                textAlign = TextAlign.End,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val statusLabel = if (article.deputyEditedNote.isNotEmpty()) {
                    article.deputyEditedNote
                } else {
                    "${article.approvedByChief} • ${article.specs.size} ТТХ"
                }
                Text(
                    text = statusLabel,
                    fontSize = 10.sp,
                    color = if (article.deputyEditedNote.isNotEmpty()) Color(0xFF38BDF8)
                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f).padding(end = 6.dp)
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
                            .clickable { onShare() },
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.background.copy(alpha = 0.85f),
                        border = BorderStroke(0.8.dp, accent.copy(alpha = 0.35f))
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Поделиться обзором",
                                tint = accent,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }

                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onOpen() },
                        shape = RoundedCornerShape(8.dp),
                        color = accent.copy(alpha = 0.15f),
                        border = BorderStroke(0.9.dp, accent.copy(alpha = 0.45f))
                    ) {
                        Text(
                            text = "Читать ↗",
                            fontSize = (11 * scale).sp,
                            fontWeight = FontWeight.Bold,
                            color = accent,
                            maxLines = 1,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ReviewDetailDialog(
    article: TechReviewArticle,
    onShare: () -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit
) {
    val accent = Color(article.accentHex)
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 16.dp),
            shape = RoundedCornerShape(22.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.2.dp, accent.copy(alpha = 0.45f))
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
                    Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                        Text(
                            text = article.authorAgent,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = accent,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = if (article.deputyEditedNote.isNotEmpty())
                                "${article.deputyEditedNote} • ${article.score}"
                            else "${article.approvedByChief} • ${article.score}",
                            fontSize = 10.sp,
                            color = if (article.deputyEditedNote.isNotEmpty()) Color(0xFF38BDF8) else Color(0xFF10B981),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onDelete) {
                            Icon(Icons.Default.DeleteOutline, contentDescription = "Удалить обзор", tint = Color(0xFFF43F5E))
                        }
                        IconButton(onClick = onShare) {
                            Icon(Icons.Default.Share, contentDescription = "Поделиться обзором", tint = accent)
                        }
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Закрыть")
                        }
                    }
                }

                DeviceIllustrationBanner(
                    category = article.category,
                    brand = article.brand,
                    deviceName = article.deviceName,
                    accentColor = accent,
                    heightDp = 132
                )

                Text(
                    text = article.headline,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.ExtraBold,
                    lineHeight = 23.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = article.fullBody,
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f)
                )

                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.background.copy(alpha = 0.8f),
                    border = BorderStroke(1.dp, accent.copy(alpha = 0.3f))
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Технические характеристики (ТТХ)",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = accent
                        )
                        article.specs.forEach { row ->
                            Column {
                                Text(
                                    text = row.label,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.62f)
                                )
                                Text(
                                    text = row.value,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                        }
                    }
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF10B981).copy(alpha = 0.10f)
                ) {
                    Column(
                        modifier = Modifier.padding(11.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text("Плюсы устройства:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
                        article.pros.forEach { p ->
                            Text("+ $p", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFF43F5E).copy(alpha = 0.10f)
                ) {
                    Column(
                        modifier = Modifier.padding(11.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text("Минусы и компромиссы:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFFF43F5E))
                        article.cons.forEach { c ->
                            Text("− $c", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.End)
                ) {
                    OutlinedButton(
                        onClick = onDelete,
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, Color(0xFFF43F5E).copy(alpha = 0.5f)),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.DeleteOutline, contentDescription = null, tint = Color(0xFFF43F5E), modifier = Modifier.size(15.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Удалить", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFFF43F5E))
                    }
                    Button(
                        onClick = onShare,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = accent, contentColor = Color(0xFF090D16)),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Поделиться", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text("Закрыть", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun FullWidthBottomNavBar(
    selectedTab: Int,
    reviewsCount: Int,
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
                    .height(44.dp)
                    .padding(horizontal = 4.dp),
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
                    modifier = Modifier.weight(1.1f),
                    selected = selectedTab == 1,
                    icon = if (selectedTab == 1) Icons.Filled.Description else Icons.Outlined.Description,
                    label = "Обзоры ($reviewsCount)",
                    onClick = { onSelectTab(1) }
                )
                FullWidthNavTab(
                    modifier = Modifier.weight(1.1f),
                    selected = selectedTab == 2,
                    icon = if (selectedTab == 2) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                    label = if (savedCount > 0) "Избр. ($savedCount)" else "Избранное",
                    onClick = { onSelectTab(2) }
                )
                FullWidthNavTab(
                    modifier = Modifier.weight(1f),
                    selected = selectedTab == 3,
                    icon = if (selectedTab == 3) Icons.Filled.Settings else Icons.Outlined.Settings,
                    label = "Настройки",
                    onClick = { onSelectTab(3) }
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
            .padding(vertical = 4.dp, horizontal = 2.dp)
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
            modifier = Modifier.size(15.dp)
        )
        Spacer(Modifier.width(4.dp))
        Text(
            text = label,
            fontSize = 10.sp,
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
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 8.dp),
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
        isTopRow = true,
        deskChairPos = Offset(0.13f, 0.17f),
        roomDoorPos = Offset(0.13f, 0.30f),
        corridorEntryPos = Offset(0.13f, 0.36f),
        corridorLaneY = 0.38f,
        diningSeatPos = Offset(0.14f, 0.48f),
        loungeSeatPos = Offset(0.74f, 0.46f)
    ),
    OfficeAgentProfile(
        id = 1,
        name = "Артур",
        role = "Носимая электроника",
        roomTitle = "Каб. 2 • Часы и аудио",
        accentColor = Color(0xFF10B981),
        hairColor = Color(0xFF1E293B),
        isTopRow = true,
        deskChairPos = Offset(0.38f, 0.17f),
        roomDoorPos = Offset(0.38f, 0.30f),
        corridorEntryPos = Offset(0.38f, 0.36f),
        corridorLaneY = 0.40f,
        diningSeatPos = Offset(0.24f, 0.48f),
        loungeSeatPos = Offset(0.82f, 0.46f)
    ),
    OfficeAgentProfile(
        id = 2,
        name = "Нова",
        role = "Обзоры ноутбуков",
        roomTitle = "Каб. 3 • Ноутбуки",
        accentColor = Color(0xFFA78BFA),
        hairColor = Color(0xFFFBBF24),
        isTopRow = true,
        deskChairPos = Offset(0.62f, 0.17f),
        roomDoorPos = Offset(0.62f, 0.30f),
        corridorEntryPos = Offset(0.62f, 0.36f),
        corridorLaneY = 0.38f,
        diningSeatPos = Offset(0.14f, 0.56f),
        loungeSeatPos = Offset(0.74f, 0.55f)
    ),
    OfficeAgentProfile(
        id = 3,
        name = "Дом-Бот",
        role = "Обзоры ТВ и дома",
        roomTitle = "Каб. 4 • Умный дом",
        accentColor = Color(0xFF60A5FA),
        hairColor = Color(0xFF38BDF8),
        isTopRow = true,
        deskChairPos = Offset(0.87f, 0.17f),
        roomDoorPos = Offset(0.87f, 0.30f),
        corridorEntryPos = Offset(0.87f, 0.36f),
        corridorLaneY = 0.41f,
        diningSeatPos = Offset(0.24f, 0.56f),
        loungeSeatPos = Offset(0.84f, 0.55f)
    ),
    OfficeAgentProfile(
        id = 4,
        name = "Авто-ИИ",
        role = "Электроавто",
        roomTitle = "Каб. 5 • Электрокары",
        accentColor = Color(0xFF22D3EE),
        hairColor = Color(0xFF0F172A),
        isTopRow = false,
        deskChairPos = Offset(0.13f, 0.83f),
        roomDoorPos = Offset(0.13f, 0.68f),
        corridorEntryPos = Offset(0.13f, 0.62f),
        corridorLaneY = 0.60f,
        diningSeatPos = Offset(0.18f, 0.45f),
        loungeSeatPos = Offset(0.78f, 0.45f)
    ),
    OfficeAgentProfile(
        id = 5,
        name = "Виктор",
        role = "Зам. директора (1-й аудит)",
        roomTitle = "Каб. 6 • Зам. директора",
        accentColor = Color(0xFFF59E0B),
        hairColor = Color(0xFF475569),
        isTopRow = false,
        deskChairPos = Offset(0.38f, 0.83f),
        roomDoorPos = Offset(0.38f, 0.68f),
        corridorEntryPos = Offset(0.38f, 0.62f),
        corridorLaneY = 0.59f,
        diningSeatPos = Offset(0.18f, 0.58f),
        loungeSeatPos = Offset(0.78f, 0.57f)
    ),
    OfficeAgentProfile(
        id = 6,
        name = "Елена",
        role = "Аудит ТТХ (2-й этап)",
        roomTitle = "Каб. 7 • Аудит ТТХ",
        accentColor = Color(0xFFEC4899),
        hairColor = Color(0xFF9333EA),
        isTopRow = false,
        deskChairPos = Offset(0.62f, 0.83f),
        roomDoorPos = Offset(0.62f, 0.68f),
        corridorEntryPos = Offset(0.62f, 0.62f),
        corridorLaneY = 0.60f,
        diningSeatPos = Offset(0.11f, 0.52f),
        loungeSeatPos = Offset(0.88f, 0.50f)
    ),
    OfficeAgentProfile(
        id = 7,
        name = "Аврора",
        role = "Главный редактор",
        roomTitle = "Каб. 8 • Главред",
        accentColor = Color(0xFF10B981),
        hairColor = Color(0xFFE11D48),
        isTopRow = false,
        deskChairPos = Offset(0.87f, 0.83f),
        roomDoorPos = Offset(0.87f, 0.68f),
        corridorEntryPos = Offset(0.87f, 0.62f),
        corridorLaneY = 0.58f,
        diningSeatPos = Offset(0.27f, 0.52f),
        loungeSeatPos = Offset(0.70f, 0.50f)
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
        "Приёмка отчётов и контроль дисциплины отдела" to "Проверены карточки задач, начислены премии команде",
        "Редакционный аудит качества контента" to "Самовольная генерация из новостей заблокирована"
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
            progress = 0.25f + (ag.id * 0.08f) % 0.5f,
            targetZone = AgentLocationZone.DESK,
            breakSecondsRemaining = 0f,
            salaryBalance = 4500 + ag.id * 150,
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
    var officeSubTab by remember { mutableIntStateOf(0) } // 0 = Командный центр, 1 = Журнал Авроры, 2 = Аудит ТТХ

    // Поля Командного центра
    var cmdAgentIndex by remember { mutableIntStateOf(0) }
    var cmdActionType by remember { mutableIntStateOf(0) } // 0 = Обзор устройства, 1 = Аудит ТТХ, 2 = Отдых
    var cmdDeviceInput by remember { mutableStateOf("") }
    var cmdNoteInput by remember { mutableStateOf("") }

    val latestReviewsState by rememberUpdatedState(existingReviews)
    val latestNewsState by rememberUpdatedState(newsList)

    // Фоновый цикл симуляции ИИ-Дома (БЕЗ самовольного создания обзоров из новостей!)
    LaunchedEffect(isOfficeActive) {
        var saveTick = 0
        while (isActive && isOfficeActive) {
            delay(450L)
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
                    AgentLocationZone.CHIEF_CHECK -> Offset(0.84f, 0.77f) // Кабинет Авроры
                    AgentLocationZone.DINING -> profile.diningSeatPos
                    AgentLocationZone.LOUNGE -> profile.loungeSeatPos
                }

                val dist = hypot(targetPoint.x - curPos.x, targetPoint.y - curPos.y)
                if (dist > 0.018f) {
                    // Движение по коридору без прохода сквозь стены
                    val waypoint = when {
                        abs(curPos.y - profile.deskChairPos.y) < 0.05f &&
                            abs(curPos.x - profile.deskChairPos.x) < 0.05f &&
                            status.targetZone != AgentLocationZone.DESK -> profile.roomDoorPos

                        abs(curPos.y - profile.roomDoorPos.y) < 0.03f &&
                            status.targetZone != AgentLocationZone.DESK -> profile.corridorEntryPos

                        status.targetZone == AgentLocationZone.DESK &&
                            abs(curPos.x - profile.corridorEntryPos.x) > 0.03f &&
                            curPos.y > 0.32f && curPos.y < 0.66f ->
                            Offset(profile.corridorEntryPos.x, profile.corridorLaneY)

                        status.targetZone == AgentLocationZone.DESK &&
                            abs(curPos.x - profile.corridorEntryPos.x) <= 0.03f &&
                            abs(curPos.y - profile.roomDoorPos.y) > 0.03f -> profile.roomDoorPos

                        else -> targetPoint
                    }

                    val wDist = hypot(waypoint.x - curPos.x, waypoint.y - curPos.y).coerceAtLeast(0.001f)
                    val step = 0.034f
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
                            val nextProg = (status.progress + 0.065f).coerceAtMost(1f)
                            if (nextProg < 1f) {
                                updatedStatuses[profile.id] = status.copy(progress = nextProg)
                            } else {
                                if (profile.id == 7) {
                                    // Аврора завершает свою управленческую задачу на месте
                                    nextTicket++
                                    val pool = AutonomousTaskTemplates[7]!!
                                    val nextTaskTpl = pool[nextTicket % pool.size]
                                    val doneTask = status.currentTask.copy(
                                        completedAt = nowTime,
                                        chiefApproved = true,
                                        salaryDelta = 400
                                    )
                                    updatedStatuses[profile.id] = status.copy(
                                        currentTask = SoloTaskItem(
                                            ticketNumber = nextTicket,
                                            badge = "КОНТРОЛЬ",
                                            title = nextTaskTpl.first,
                                            resultSummary = nextTaskTpl.second,
                                            startedAt = nowTime
                                        ),
                                        progress = 0f,
                                        salaryBalance = status.salaryBalance + 400,
                                        completedHistory = (listOf(doneTask) + status.completedHistory).take(12)
                                    )
                                } else {
                                    // Агент идёт к Авроре на приёмку выполненной задачи
                                    updatedStatuses[profile.id] = status.copy(
                                        progress = 1f,
                                        targetZone = AgentLocationZone.CHIEF_CHECK
                                    )
                                }
                            }
                        }

                        AgentLocationZone.CHIEF_CHECK -> {
                            val approved = (nextTicket % 6) != 0
                            val delta = if (approved) 350 else -150
                            val verdictText = if (approved) {
                                "Принято Авророй: данные проверены (+350 ₽)"
                            } else {
                                "Замечание Авроры: перепроверить источники (−150 ₽)"
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

                            // Если это Виктор (5) или Елена (6), они молча уточняют ТТХ в существующих обзорах
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
                                rejectReason = if (approved) "" else "Требуется доработка"
                            )

                            nextTicket++
                            val pool = AutonomousTaskTemplates[profile.id]
                                ?: listOf("Проверка спецификаций" to "Выполнено")
                            val nextTpl = pool[nextTicket % pool.size]
                            val takeBreak = (nextTicket % 3 == 0)
                            val nextZone = if (takeBreak) {
                                if (profile.id % 2 == 0) AgentLocationZone.DINING else AgentLocationZone.LOUNGE
                            } else {
                                AgentLocationZone.DESK
                            }

                            updatedStatuses[profile.id] = status.copy(
                                currentTask = SoloTaskItem(
                                    ticketNumber = nextTicket,
                                    badge = "В РАБОТЕ",
                                    title = nextTpl.first,
                                    resultSummary = nextTpl.second,
                                    startedAt = nowTime
                                ),
                                progress = 0f,
                                targetZone = nextZone,
                                breakSecondsRemaining = if (takeBreak) 12f else 0f,
                                salaryBalance = (status.salaryBalance + delta).coerceAtLeast(0),
                                rejectionsCount = status.rejectionsCount + if (approved) 0 else 1,
                                reprimandsCount = status.reprimandsCount + if (approved) 0 else 1,
                                completedHistory = (listOf(doneTask) + status.completedHistory).take(12)
                            )
                        }

                        AgentLocationZone.DINING, AgentLocationZone.LOUNGE -> {
                            val left = status.breakSecondsRemaining - 0.45f
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
            if (saveTick % 10 == 0) {
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

    val infiniteTransition = rememberInfiniteTransition(label = "newYearLights")
    val garlandPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.28318f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "garlandPhase"
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
        // 1. НОВОГОДНИЙ ИИ-ДОМ И КОМАНДНЫЙ ЦЕНТР
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                border = BorderStroke(1.2.dp, primary.copy(alpha = 0.38f)),
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
                                text = "Самовольные обзоры отключены • Работают по приказам",
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
                                    text = if (isOfficeActive) "Активен" else "Пауза",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isOfficeActive) Color(0xFF10B981) else Color(0xFFF59E0B)
                                )
                            }
                        }
                    }

                    // Интерактивный 2D-план ИИ-Дома
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(250.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFF09111E))
                            .border(1.dp, primary.copy(alpha = 0.35f), RoundedCornerShape(14.dp))
                            .pointerInput(Unit) {
                                detectTapGestures { tapOffset ->
                                    val w = size.width.toFloat().coerceAtLeast(1f)
                                    val h = size.height.toFloat().coerceAtLeast(1f)
                                    val normTap = Offset(tapOffset.x / w, tapOffset.y / h)
                                    val nearest = DefaultOfficeAgents.minByOrNull { ag ->
                                        val p = agentPositions[ag.id] ?: ag.deskChairPos
                                        hypot(p.x - normTap.x, p.y - normTap.y)
                                    }
                                    if (nearest != null) {
                                        selectedAgentId = nearest.id
                                    }
                                }
                            }
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            drawAiNewYearHouseFloorplan(
                                agents = DefaultOfficeAgents,
                                positions = agentPositions,
                                statuses = agentStatuses,
                                selectedId = selectedAgentId,
                                garlandPhase = garlandPhase
                            )
                        }
                    }

                    // Карточка выбранного агента (нажмите на любого человечка на карте)
                    val selectedProfile = DefaultOfficeAgents.first { it.id == selectedAgentId }
                    val selectedStatus = agentStatuses[selectedAgentId]
                    if (selectedStatus != null) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.background.copy(alpha = 0.78f),
                            border = BorderStroke(1.dp, selectedProfile.accentColor.copy(alpha = 0.45f))
                        ) {
                            Column(
                                modifier = Modifier.padding(10.dp),
                                verticalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${selectedProfile.name} • ${selectedProfile.role}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = selectedProfile.accentColor,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Text(
                                        text = "Баланс: ${selectedStatus.salaryBalance} ₽",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF10B981)
                                    )
                                }
                                Text(
                                    text = "Локация: ${selectedStatus.targetZone.label} • Тикет #${selectedStatus.currentTask.ticketNumber}",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
                                )
                                Text(
                                    text = "Задача: ${selectedStatus.currentTask.title}",
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
                            }
                        }
                    }

                    // Переключатель вкладок внутри ИИ-Дома: Командный центр / Журнал Авроры / Аудит ТТХ
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
                            // ПРЯМОЙ КОМАНДНЫЙ ЦЕНТР ДЛЯ АГЕНТОВ
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
                                        text = "Выберите агента и отдайте прямой приказ:",
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
                                                        agentStatuses = agentStatuses.toMutableMap().apply {
                                                            this[targetAgent.id] = curSt.copy(
                                                                currentTask = SoloTaskItem(
                                                                    ticketNumber = ticketCounter,
                                                                    badge = "ПРИКАЗ",
                                                                    title = "Обзор по приказу: ${created.deviceName}",
                                                                    resultSummary = "Опубликован в разделе «Обзоры»",
                                                                    startedAt = nowStr,
                                                                    completedAt = nowStr,
                                                                    chiefApproved = true,
                                                                    salaryDelta = 500
                                                                ),
                                                                progress = 1f,
                                                                targetZone = AgentLocationZone.DESK,
                                                                salaryBalance = curSt.salaryBalance + 500
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
                                                            "${targetAgent.name} отправлен(а) на кофе-брейк",
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
                            // ЖУРНАЛ ПРОВЕРОК ГЛАВНОГО РЕДАКТОРА АВРОРЫ
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                if (chiefAuditLog.isEmpty()) {
                                    Text(
                                        text = "Журнал приёмки задач Авророй пока пуст — дождитесь завершения цикла агента.",
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
                            // ЖУРНАЛ ДВОЙНОГО АУДИТА ТТХ (ВИКТОР И ЕЛЕНА)
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                if (deputyEditLog.isEmpty()) {
                                    Text(
                                        text = "Зам. директора Виктор и аудитор Елена пока не вносили правок в ТТХ.",
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

        // 2. УПРАВЛЕНИЕ ЛЕНТОЙ И УДАЛЁННЫМИ ПОСТАМИ
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

        // 3. ОФОРМЛЕНИЕ И ПЕРСОНАЛИЗАЦИЯ ПРИЛОЖЕНИЯ
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

                    // Цветовые палитры
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

                    // Режим темы (Системная / Светлая / Тёмная / AMOLED)
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

                    // Стиль карточек
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

                    // Скругление углов
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

                    // Размер текста
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

                    // Краткое описание в ленте
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

private fun DrawScope.drawAiNewYearHouseFloorplan(
    agents: List<OfficeAgentProfile>,
    positions: Map<Int, Offset>,
    statuses: Map<Int, AgentLiveStatus>,
    selectedId: Int,
    garlandPhase: Float
) {
    val w = size.width
    val h = size.height

    // Пол коридора
    drawRect(
        color = Color(0xFF0E1729),
        topLeft = Offset(0f, h * 0.31f),
        size = Size(w, h * 0.38f)
    )

    // 4 верхних кабинета и 4 нижних кабинета
    val colWidth = w / 4f
    for (col in 0 until 4) {
        val x0 = col * colWidth + 4.dp.toPx()
        val roomW = colWidth - 8.dp.toPx()

        // Верхний кабинет
        val topAg = agents[col]
        drawRoundRect(
            color = Color(0xFF132038),
            topLeft = Offset(x0, 4.dp.toPx()),
            size = Size(roomW, h * 0.27f),
            cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx())
        )
        drawRoundRect(
            color = topAg.accentColor.copy(alpha = if (selectedId == topAg.id) 0.85f else 0.32f),
            topLeft = Offset(x0, 4.dp.toPx()),
            size = Size(roomW, h * 0.27f),
            cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx()),
            style = Stroke(width = if (selectedId == topAg.id) 2.dp.toPx() else 1.dp.toPx())
        )
        // Рабочий стол в верхнем кабинете
        drawRoundRect(
            color = Color(0xFF283A5A),
            topLeft = Offset(x0 + roomW * 0.22f, h * 0.06f),
            size = Size(roomW * 0.56f, h * 0.065f),
            cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
        )

        // Нижний кабинет
        val botAg = agents[col + 4]
        drawRoundRect(
            color = Color(0xFF132038),
            topLeft = Offset(x0, h * 0.69f),
            size = Size(roomW, h * 0.28f),
            cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx())
        )
        drawRoundRect(
            color = botAg.accentColor.copy(alpha = if (selectedId == botAg.id) 0.85f else 0.32f),
            topLeft = Offset(x0, h * 0.69f),
            size = Size(roomW, h * 0.28f),
            cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx()),
            style = Stroke(width = if (selectedId == botAg.id) 2.dp.toPx() else 1.dp.toPx())
        )
        // Рабочий стол в нижнем кабинете
        drawRoundRect(
            color = Color(0xFF283A5A),
            topLeft = Offset(x0 + roomW * 0.22f, h * 0.88f),
            size = Size(roomW * 0.56f, h * 0.065f),
            cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
        )
    }

    // Кухня (слева в коридоре)
    drawRoundRect(
        color = Color(0xFF1A2942),
        topLeft = Offset(w * 0.06f, h * 0.42f),
        size = Size(w * 0.24f, h * 0.18f),
        cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx())
    )
    drawCircle(
        color = Color(0xFFF59E0B).copy(alpha = 0.45f),
        radius = 12.dp.toPx(),
        center = Offset(w * 0.18f, h * 0.51f)
    )

    // Лаунж-зона и 4K ТВ (справа в коридоре)
    drawRoundRect(
        color = Color(0xFF1A2942),
        topLeft = Offset(w * 0.67f, h * 0.42f),
        size = Size(w * 0.27f, h * 0.18f),
        cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx())
    )
    drawRoundRect(
        color = Color(0xFF38BDF8).copy(alpha = 0.55f),
        topLeft = Offset(w * 0.91f, h * 0.45f),
        size = Size(4.dp.toPx(), h * 0.12f),
        cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
    )

    // Новогодняя ёлка по центру коридора
    val treeX = w * 0.49f
    val treeTopY = h * 0.37f
    val treeBotY = h * 0.62f
    val treePath = Path().apply {
        moveTo(treeX, treeTopY)
        lineTo(treeX - 20.dp.toPx(), treeBotY)
        lineTo(treeX + 20.dp.toPx(), treeBotY)
        close()
    }
    drawPath(path = treePath, color = Color(0xFF059669))
    drawCircle(
        color = Color(0xFFFBBF24),
        radius = 4.dp.toPx(),
        center = Offset(treeX, treeTopY - 2.dp.toPx())
    )

    // Мерцающие гирлянды вдоль коридора
    val bulbColors = listOf(Color(0xFFF43F5E), Color(0xFFFBBF24), Color(0xFF38BDF8), Color(0xFF10B981))
    for (i in 0 until 14) {
        val bx = w * (0.05f + i * 0.068f)
        val by = h * 0.33f
        val pulse = 0.45f + 0.55f * abs(sin(garlandPhase + i * 0.7f))
        drawCircle(
            color = bulbColors[i % bulbColors.size].copy(alpha = pulse),
            radius = 2.6.dp.toPx(),
            center = Offset(bx, by)
        )
    }

    // Отрисовка 8 агентов
    agents.forEach { ag ->
        val pos = positions[ag.id] ?: ag.deskChairPos
        val cx = pos.x * w
        val cy = pos.y * h
        val isSel = ag.id == selectedId
        val st = statuses[ag.id]

        if (isSel) {
            drawCircle(
                color = ag.accentColor.copy(alpha = 0.28f),
                radius = 15.dp.toPx(),
                center = Offset(cx, cy)
            )
            drawCircle(
                color = ag.accentColor,
                radius = 15.dp.toPx(),
                center = Offset(cx, cy),
                style = Stroke(
                    width = 1.4.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 4f), 0f)
                )
            )
        }

        // Тело / пиджак агента
        drawCircle(
            color = ag.accentColor,
            radius = 8.5.dp.toPx(),
            center = Offset(cx, cy + 3.dp.toPx())
        )
        // Голова агента
        drawCircle(
            color = Color(0xFFFDE68A),
            radius = 5.5.dp.toPx(),
            center = Offset(cx, cy - 4.dp.toPx())
        )
        // Причёска / акцент
        drawArc(
            color = ag.hairColor,
            startAngle = 180f,
            sweepAngle = 180f,
            useCenter = true,
            topLeft = Offset(cx - 5.5.dp.toPx(), cy - 9.5.dp.toPx()),
            size = Size(11.dp.toPx(), 8.dp.toPx())
        )

        // Индикатор статуса (зелёный = работает, жёлтый = на проверке/отдыхе)
        val statusDotColor = when (st?.targetZone) {
            AgentLocationZone.CHIEF_CHECK -> Color(0xFF38BDF8)
            AgentLocationZone.DINING, AgentLocationZone.LOUNGE -> Color(0xFFFBBF24)
            else -> Color(0xFF10B981)
        }
        drawCircle(
            color = statusDotColor,
            radius = 2.6.dp.toPx(),
            center = Offset(cx + 7.dp.toPx(), cy - 7.dp.toPx())
        )
    }
}
