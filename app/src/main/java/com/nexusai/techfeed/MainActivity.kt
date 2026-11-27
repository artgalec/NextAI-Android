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

data class ReviewSpecRow(
    val label: String,
    val value: String
)

data class TechReviewArticle(
    val id: String,
    val deviceName: String,
    val brand: String,
    val category: String, // "phone", "watch", "buds", "laptop", "tv", "car"
    val categoryLabel: String, // "Смартфоны", "Часы", "Наушники", "Ноутбуки", "Телевизоры", "Электроавто"
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
    val accentHex: Long
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
    DINING("Обедает в столовой"),
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
            fullBody = "Huawei Watch Fit 5 сохранили лёгкий прямоугольный форм-фактор, который практически не ощущается на запястье даже во время сна. Увеличенный AMOLED-дисплей с тонкими рамками легко читается под прямым солнцем.\n\nВнутри обновлена оптическая платформа мониторинга пульса и уровня кислорода в крови (SpO2): точность замеров на пробежках выросла, а графики сна и восстановления в приложении Huawei Health стали подробнее. Поддерживаются звонки по Bluetooth и более 100 спортивных режимов с GPS.",
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
            authorAgent = "Вика • Обзоры техники и авто",
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

    // Строгий каталог уникальных устройств: каждое устройство выпускается ТОЛЬКО ОДИН РАЗ, без ревизий и дублей!
    private val uniqueNewDevicesCatalog = listOf(
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
            fullBody = "OnePlus 15R делает ставку на максимальную плавность интерфейса и рекордную автономность. Смартфон оснащён 6.83-дюймовым плоским LTPS AMOLED-экраном с разрешением 1.5K и частотой обновления до 165 Гц.\n\nВнутри установлен процессор Qualcomm Snapdragon 8 Gen 5 с испарительной камерой площадью 9140 мм², а кремний-углеродный аккумулятор ёмкостью 7400 мАч обеспечивает до двух суток активной работы при поддержке проводной зарядки SuperVOOC 80 Вт.",
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
            id = "uniq_oneplus_nord_buds_4_pro",
            deviceName = "OnePlus Nord Buds 4 Pro",
            brand = "OnePlus",
            category = "buds",
            categoryLabel = "Наушники",
            authorAgent = "Артур • Носимая электроника",
            approvedByChief = "ТТХ проверены Авророй",
            headline = "OnePlus Nord Buds 4 Pro: кодек LHDC 5.0, шумоподавление 55 дБ и драйверы 12.4 мм",
            leadSummary = "Детальный тест доступных флагманских TWS от OnePlus: поддержка Hi-Res аудио LHDC 5.0, глубокий бас BassWave и 44 часа автономности.",
            fullBody = "OnePlus Nord Buds 4 Pro получили крупные 12.4-мм титанизированные драйверы и редкую для своего класса поддержку Hi-Res кодека LHDC 5.0. Адаптивное шумоподавление анализирует форму ушного канала и отсекает до 55 дБ низкочастотного гула.\n\nБлагодаря алгоритму BassWave 2.0 наушники не заваливают середину даже на максимальном уровне баса, а трёхмикрофонная система чисто передаёт голос на ветру.",
            specs = listOf(
                ReviewSpecRow("Драйверы", "12.4 мм динамические с титановым напылением"),
                ReviewSpecRow("Кодеки", "LHDC 5.0, AAC, SBC"),
                ReviewSpecRow("Шумоподавление", "Адаптивное ANC до 55 дБ + режим прозрачности"),
                ReviewSpecRow("Автономность", "До 12 ч от одного заряда, до 44 ч с кейсом"),
                ReviewSpecRow("Связь и защита", "Bluetooth 5.4, Google Fast Pair, IP55")
            ),
            pros = listOf("Поддержка LHDC 5.0 и мощное ANC 55 дБ", "До 44 часов работы с зарядным футляром", "Удобная посадка и защита от пота IP55"),
            cons = listOf("Нет беспроводной зарядки кейса", "Полная настройка эквалайзера только в приложении HeyMelody"),
            score = "9.1 / 10",
            publishedAt = "Сегодня",
            accentHex = 0xFFF43F5E
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
            fullBody = "Apple Watch Series 11 получили обновлённую широкоугольную OLED-матрицу LTPO3, которая остаётся на 40% ярче при взгляде под углом, а в режиме Always-On отображает секундную стрелку в реальном времени.\n\nТонкий корпус толщиной 9.7 мм стал ещё удобнее под манжетой рубашки, а быстрая зарядка набирает 80% ёмкости всего за 30 минут.",
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
        ),
        TechReviewArticle(
            id = "uniq_xiaomi_tv_s_mini_led",
            deviceName = "Xiaomi TV S Mini LED 75\"",
            brand = "Xiaomi",
            category = "tv",
            categoryLabel = "Телевизоры",
            authorAgent = "Дом-Бот • Обзоры ТВ и дома",
            approvedByChief = "ТТХ проверены Авророй",
            headline = "Xiaomi TV S Mini LED 75\": 512 зон локального затемнения, 144 Гц и яркость 1200 нит",
            leadSummary = "Тестируем 75-дюймовый Mini-LED телевизор Xiaomi: контрастность в тёмных сценах, работу 144 Гц с консолями и оболочку Google TV.",
            fullBody = "Xiaomi TV S Mini LED 75 сочетает большую 75-дюймовую диагональ с матрицей на 512 зон локальной подсветки и пиковой яркостью 1200 нит. В ночных сценах фильмов ореолы вокруг ярких объектов сведены к минимуму.\n\nНаличие двух полноценных портов HDMI 2.1 с поддержкой 4K 144 Гц, VRR и FreeSync Premium делает телевизор отличным монитором для современных игровых приставок.",
            specs = listOf(
                ReviewSpecRow("Экран", "75\" VA Mini-LED 4K (3840×2160), 144 Гц, 512 зон"),
                ReviewSpecRow("Яркость и HDR", "До 1200 нит, Dolby Vision IQ, HDR10+, IMAX Enhanced"),
                ReviewSpecRow("Процессор и память", "Quad Cortex-A73, 3 ГБ ОЗУ + 32 ГБ ПЗУ"),
                ReviewSpecRow("Звук и порты", "30 Вт Dolby Atmos, 2× HDMI 2.1 (144 Гц), Wi-Fi 6"),
                ReviewSpecRow("Платформа", "Google TV с голосовым поиском и Chromecast")
            ),
            pros = listOf("Отличное соотношение цены, 75\" диагонали и Mini-LED", "Поддержка 4K 144 Гц и Dolby Vision IQ", "Шустрый интерфейс Google TV с Wi-Fi 6"),
            cons = listOf("Углы обзора уступают OLED-панелям", "Штатную акустику для кино лучше дополнить саундбаром"),
            score = "9.2 / 10",
            publishedAt = "Сегодня",
            accentHex = 0xFF10B981
        ),
        TechReviewArticle(
            id = "uniq_samsung_galaxy_watch_ultra",
            deviceName = "Samsung Galaxy Watch Ultra",
            brand = "Samsung",
            category = "watch",
            categoryLabel = "Часы",
            authorAgent = "Артур • Носимая электроника",
            approvedByChief = "ТТХ проверены Авророй",
            headline = "Samsung Galaxy Watch Ultra: титановый корпус, 3000 нит и двухчастотный GPS",
            leadSummary = "Обзор защищённых флагманских часов Samsung для спорта и туризма: 3-нм процессор Exynos W1000 и сапфировое стекло.",
            fullBody = "Galaxy Watch Ultra созданы для экстремальных нагрузок: корпус выполнен из титана Grade 4, экран прикрыт сапфировым стеклом, а влагозащита достигает 10 ATM.\n\nНовый 5-ядерный чип Exynos W1000 по техпроцессу 3 нм обеспечил мгновенный отклик One UI Watch.",
            specs = listOf(
                ReviewSpecRow("Дисплей", "1.5\" Super AMOLED, 480×480, сапфир, 3000 нит"),
                ReviewSpecRow("Чипсет и память", "Exynos W1000 (3 нм), 2 ГБ ОЗУ + 32 ГБ ПЗУ"),
                ReviewSpecRow("Корпус и защита", "Титан Grade 4, 47 мм, 10 ATM + IP68"),
                ReviewSpecRow("Навигация", "Двухчастотный GPS (L1+L5), LTE/eSIM, NFC"),
                ReviewSpecRow("Батарея", "590 мАч, до 60–100 часов работы")
            ),
            pros = listOf("Рекордная яркость 3000 нит и сапфировое стекло", "Быстрый 3-нм процессор Exynos W1000"),
            cons = listOf("Крупный корпус 47 мм", "Часть медицинских функций требует смартфон Galaxy"),
            score = "9.2 / 10",
            publishedAt = "Сегодня",
            accentHex = 0xFF3B82F6
        ),
        TechReviewArticle(
            id = "uniq_honor_magicbook_art_14",
            deviceName = "HONOR MagicBook Art 14",
            brand = "HONOR",
            category = "laptop",
            categoryLabel = "Ноутбуки",
            authorAgent = "Нова • Обзоры ноутбуков",
            approvedByChief = "ТТХ проверены Авророй",
            headline = "HONOR MagicBook Art 14: съёмная магнитная веб-камера, вес 1.03 кг и OLED 3.1K",
            leadSummary = "Тестируем ультратонкий ноутбук HONOR с рекордно тонкими рамками 2.2 мм и спрятанной в корпусе камерой.",
            fullBody = "HONOR MagicBook Art 14 выделяется уникальной съёмной веб-камерой на магнитах, которая хранится в боковом слоте корпуса и при необходимости крепится на верхнюю крышку.\n\nБлагодаря этому экран занимает 97% площади крышки, а частота ШИМ 4320 Гц полностью исключает усталость глаз.",
            specs = listOf(
                ReviewSpecRow("Экран", "14.6\" OLED 3120×2080, 120 Гц, ШИМ 4320 Гц"),
                ReviewSpecRow("Процессор", "Intel Core Ultra 7 155H, графика Intel Arc"),
                ReviewSpecRow("Память", "32 ГБ LPDDR5X + 1 ТБ SSD"),
                ReviewSpecRow("Корпус", "Магниевый сплав, вес 1.03 кг, толщина 11.5 мм"),
                ReviewSpecRow("Звук и батарея", "6 динамиков Spatial Audio, 60 Вт·ч")
            ),
            pros = listOf("Безрамочный 14.6\" OLED-экран без вырезов", "Оригинальная магнитная камера для приватности"),
            cons = listOf("Невысокая автономность под максимальной нагрузкой"),
            score = "9.3 / 10",
            publishedAt = "Сегодня",
            accentHex = 0xFF06B6D4
        )
    )

    // Возвращает ТОЛЬКО уникальное устройство, которого ещё нет в списке! Никаких ревизий и повторов!
    fun generateUniqueNextArticle(existingNames: Set<String>): TechReviewArticle? {
        val nowStr = ZonedDateTime.now().format(DateTimeFormatter.ofPattern("Сегодня, HH:mm"))
        val nextUnique = uniqueNewDevicesCatalog.firstOrNull { candidate ->
            !existingNames.contains(candidate.deviceName.trim().lowercase())
        } ?: return null

        return nextUnique.copy(
            id = "uniq_${nextUnique.id}_${System.currentTimeMillis()}",
            publishedAt = nowStr
        )
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

object LocalCache {
    private const val PREFS = "newspulse_ai_prefs"
    private const val KEY_SAVED = "saved_news"
    private const val KEY_CACHE = "cached_news_ru_v7"
    private const val KEY_SETTINGS = "user_app_settings_v4"

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

    var newsList by remember { mutableStateOf(LocalCache.getCached(context)) }
    var savedList by remember { mutableStateOf(LocalCache.getSaved(context)) }
    var reviewArticles by remember { mutableStateOf(ReviewCatalogFactory.initialReviews()) }
    var openedReview by remember { mutableStateOf<TechReviewArticle?>(null) }

    var isLoading by remember { mutableStateOf(false) }
    var lastUpdatedText by remember { mutableStateOf(if (newsList.isNotEmpty()) "${newsList.size} новостей о технике" else "Загрузка...") }

    // 0 = Лента, 1 = Обзоры, 2 = Избранное, 3 = Настройки
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
        if (selectedTab == 2) {
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

    if (openedReview != null) {
        ReviewDetailDialog(
            article = openedReview!!,
            onShare = { shareReviewArticle(context, openedReview!!) },
            onDismiss = { openedReview = null }
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
                            1 -> "Уникальные обзоры и ТТХ • ${reviewArticles.size} шт."
                            2 -> "Сохранённые статьи • ${savedList.size}"
                            3 -> "Темы оформления и новогодний ИИ-Дом"
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
                        // Выпускает ТОЛЬКО уникальную модель (без дублей и ревизий)
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .clickable {
                                    val existing = reviewArticles.map { it.deviceName.trim().lowercase() }.toSet()
                                    val newRev = ReviewCatalogFactory.generateUniqueNextArticle(existing)
                                    if (newRev != null) {
                                        reviewArticles = listOf(newRev) + reviewArticles
                                        Toast.makeText(
                                            context,
                                            "Аврора утвердила обзор: ${newRev.deviceName}",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    } else {
                                        Toast.makeText(
                                            context,
                                            "Все уникальные новинки уже опубликованы (дубли запрещены)",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                },
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
                                    "+ Новый обзор",
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
                val reviewBrands = listOf("Все", "Xiaomi", "Huawei", "Samsung", "Apple", "OnePlus", "HONOR", "Электроавто")
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
                                onOpen = { openedReview = article },
                                onShare = { shareReviewArticle(context, article) }
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
                    isLoading = isLoading,
                    onSettingsChange = onSettingsChange,
                    onTriggerScan = { refreshFeed() },
                    onNewReviewApproved = { approvedArticle ->
                        val exists = reviewArticles.any { it.deviceName.trim().equals(approvedArticle.deviceName.trim(), ignoreCase = true) }
                        if (!exists) {
                            reviewArticles = listOf(approvedArticle) + reviewArticles
                        }
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
    onShare: () -> Unit
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

            // Нижняя панель с кнопкой «Поделиться» и компактной кнопкой «Читать ↗»
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${article.approvedByChief} • ${article.specs.size} ТТХ",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
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
                            text = "${article.approvedByChief} • ${article.score}",
                            fontSize = 10.sp,
                            color = Color(0xFF10B981),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
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
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
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

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    bottomPadding: androidx.compose.ui.unit.Dp,
    settings: UserAppSettings,
    newsList: List<NewsItem>,
    existingReviews: List<TechReviewArticle>,
    isLoading: Boolean,
    onSettingsChange: (UserAppSettings) -> Unit,
    onTriggerScan: () -> Unit,
    onNewReviewApproved: (TechReviewArticle) -> Unit,
    onClearCache: () -> Unit
) {
    var showAiOfficeDialog by remember { mutableStateOf(false) }

    if (showAiOfficeDialog) {
        AiOfficeLiveDialog(
            newsList = newsList,
            existingReviews = existingReviews,
            isScanning = isLoading,
            onRunScan = onTriggerScan,
            onNewReviewApproved = onNewReviewApproved,
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
                    "Персонализация тем, шрифта и новогодний ИИ-Дом с премиями",
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
                        modifier = Modifier.weight(1f).padding(end = 8.dp),
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
                                    "Новогодний ИИ-Дом • Штат",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
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
                                "Ёлка, огонёчки, фактчекинг ТТХ от Авроры, зарплаты, премии и выговоры",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
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
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                maxLines = 1
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
                    Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
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
                    16 to "Средние (16dp)",
                    24 to "Плавные (24dp)"
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
    val headlineHint = sampleNews?.title?.take(40)?.let { "«$it...»" } ?: "новинкам $brand"

    return when (agentId) {
        0 -> SoloTaskItem(
            ticketNumber = ticketNumber,
            badge = "СМИ и RSS • $brand",
            title = "Сверка ТТХ и парсинг RSS $source по устройствам $brand ($headlineHint)",
            resultSummary = "Обработано ${3 + (ticketNumber % 4)} материалов $source по бренду $brand",
            startedAt = nowStr
        )
        1 -> SoloTaskItem(
            ticketNumber = ticketNumber,
            badge = "Telegram • $brand",
            title = "Проверка официальных каналов Telegram по новинкам $brand ($headlineHint)",
            resultSummary = "Извлечено ${2 + (ticketNumber % 4)} поста из Telegram по теме $brand",
            startedAt = nowStr
        )
        2 -> SoloTaskItem(
            ticketNumber = ticketNumber,
            badge = "Instagram • $brand",
            title = "Проверка тизеров в Instagram $brand: дизайн, цвета и даты анонсов",
            resultSummary = "Проверены публикации Instagram-профиля $brand",
            startedAt = nowStr
        )
        3 -> SoloTaskItem(
            ticketNumber = ticketNumber,
            badge = "Обзор часов и TWS",
            title = "Подготовка уникального обзора часов/наушников с точными ТТХ для Авроры",
            resultSummary = "Подготовлен обзор носимой электроники с проверкой ТТХ",
            startedAt = nowStr
        )
        4 -> SoloTaskItem(
            ticketNumber = ticketNumber,
            badge = "Электроавто",
            title = "Фактчекинг ТТХ электрокаров Xiaomi SU7/YU7, Zeekr, BYD и батарей CATL",
            resultSummary = "Обновлена сводка по электроавто и автопилотам",
            startedAt = nowStr
        )
        5 -> SoloTaskItem(
            ticketNumber = ticketNumber,
            badge = "ТВ и Умный дом",
            title = "Проверка матриц OLED/Mini-LED телевизоров и устройств умного дома",
            resultSummary = "Добавлены материалы по телевизорам и умному дому",
            startedAt = nowStr
        )
        6 -> SoloTaskItem(
            ticketNumber = ticketNumber,
            badge = "Ноутбуки и ПК",
            title = "Сверка характеристик ультрабуков MateBook/MagicBook/MacBook без дублей",
            resultSummary = "Проверены характеристики новых ноутбуков",
            startedAt = nowStr
        )
        else -> SoloTaskItem(
            ticketNumber = ticketNumber,
            badge = "Обзор смартфонов",
            title = "Подготовка уникального обзора смартфона ($brand) с фактчекингом ТТХ",
            resultSummary = "Подготовлен обзор смартфона $brand с проверкой ТТХ",
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
    val isCurrentlyInsideRoom = (ag.isTopRow && fromPos.y < 0.27f) || (!ag.isTopRow && fromPos.y > 0.73f)

    if (isCurrentlyInsideRoom) {
        points.add(ag.roomDoorPos)
        points.add(ag.corridorEntryPos)
    }

    val chiefDoorPos = Offset(0.10f, 0.27f)
    val chiefVisitPos = Offset(0.11f, 0.21f)

    when (destination) {
        AgentLocationZone.DESK -> {
            if (fromPos.y < 0.27f && fromPos.x < 0.21f) {
                points.add(chiefDoorPos)
                points.add(Offset(0.10f, ag.corridorLaneY))
            }
            points.add(Offset(fromPos.x, ag.corridorLaneY))
            points.add(Offset(ag.corridorEntryPos.x, ag.corridorLaneY))
            points.add(ag.corridorEntryPos)
            points.add(ag.roomDoorPos)
            points.add(ag.deskChairPos)
        }
        AgentLocationZone.CHIEF_CHECK -> {
            points.add(Offset(ag.corridorEntryPos.x, ag.corridorLaneY))
            points.add(Offset(0.10f, ag.corridorLaneY))
            points.add(chiefDoorPos)
            points.add(chiefVisitPos)
        }
        AgentLocationZone.DINING -> {
            if (fromPos.y < 0.27f && fromPos.x < 0.21f) {
                points.add(chiefDoorPos)
                points.add(Offset(0.10f, 0.31f))
            }
            points.add(Offset(ag.diningSeatPos.x, 0.31f))
            points.add(ag.diningSeatPos)
        }
        AgentLocationZone.LOUNGE -> {
            if (fromPos.y < 0.27f && fromPos.x < 0.21f) {
                points.add(chiefDoorPos)
                points.add(Offset(0.10f, 0.31f))
            }
            points.add(Offset(ag.loungeSeatPos.x, 0.31f))
            points.add(ag.loungeSeatPos)
        }
    }

    val cleaned = mutableListOf<Offset>()
    for (pt in points) {
        val prev = cleaned.lastOrNull() ?: fromPos
        if (hypot(pt.x - prev.x, pt.y - prev.y) > 0.003f) {
            cleaned.add(pt)
        }
    }
    return cleaned
}

@Composable
fun AiOfficeLiveDialog(
    newsList: List<NewsItem>,
    existingReviews: List<TechReviewArticle>,
    isScanning: Boolean,
    onRunScan: () -> Unit,
    onNewReviewApproved: (TechReviewArticle) -> Unit,
    onDismiss: () -> Unit
) {
    val primary = MaterialTheme.colorScheme.primary

    val agents = remember {
        listOf(
            OfficeAgentProfile(
                id = 0, name = "Алиса", role = "СМИ", roomTitle = "Кабинет 1 • СМИ",
                accentColor = Color(0xFF10B981), hairColor = Color(0xFF78350F), isTopRow = true,
                deskChairPos = Offset(0.30f, 0.17f), roomDoorPos = Offset(0.30f, 0.27f), corridorEntryPos = Offset(0.30f, 0.30f),
                corridorLaneY = 0.295f,
                diningSeatPos = Offset(0.12f, 0.56f), loungeSeatPos = Offset(0.62f, 0.58f)
            ),
            OfficeAgentProfile(
                id = 1, name = "Макс", role = "TG", roomTitle = "Кабинет 2 • TG",
                accentColor = Color(0xFF38BDF8), hairColor = Color(0xFF1E293B), isTopRow = true,
                deskChairPos = Offset(0.50f, 0.17f), roomDoorPos = Offset(0.50f, 0.27f), corridorEntryPos = Offset(0.50f, 0.30f),
                corridorLaneY = 0.31f,
                diningSeatPos = Offset(0.21f, 0.56f), loungeSeatPos = Offset(0.71f, 0.58f)
            ),
            OfficeAgentProfile(
                id = 2, name = "Инста", role = "IG", roomTitle = "Кабинет 3 • IG",
                accentColor = Color(0xFFF43F5E), hairColor = Color(0xFF9F1239), isTopRow = true,
                deskChairPos = Offset(0.70f, 0.17f), roomDoorPos = Offset(0.70f, 0.27f), corridorEntryPos = Offset(0.70f, 0.30f),
                corridorLaneY = 0.325f,
                diningSeatPos = Offset(0.30f, 0.56f), loungeSeatPos = Offset(0.80f, 0.58f)
            ),
            OfficeAgentProfile(
                id = 3, name = "Артур", role = "Часы/TWS", roomTitle = "Кабинет 4 • Обзоры",
                accentColor = Color(0xFF34D399), hairColor = Color(0xFF0F172A), isTopRow = true,
                deskChairPos = Offset(0.90f, 0.17f), roomDoorPos = Offset(0.90f, 0.27f), corridorEntryPos = Offset(0.90f, 0.30f),
                corridorLaneY = 0.34f,
                diningSeatPos = Offset(0.38f, 0.56f), loungeSeatPos = Offset(0.89f, 0.58f)
            ),
            OfficeAgentProfile(
                id = 4, name = "Авто-ИИ", role = "Авто", roomTitle = "Кабинет 5 • Авто",
                accentColor = Color(0xFF22D3EE), hairColor = Color(0xFF0F172A), isTopRow = false,
                deskChairPos = Offset(0.125f, 0.83f), roomDoorPos = Offset(0.125f, 0.73f), corridorEntryPos = Offset(0.125f, 0.70f),
                corridorLaneY = 0.705f,
                diningSeatPos = Offset(0.14f, 0.56f), loungeSeatPos = Offset(0.64f, 0.58f)
            ),
            OfficeAgentProfile(
                id = 5, name = "Дом-Бот", role = "ТВ/Дом", roomTitle = "Кабинет 6 • ТВ/Дом",
                accentColor = Color(0xFFF59E0B), hairColor = Color(0xFF451A03), isTopRow = false,
                deskChairPos = Offset(0.375f, 0.83f), roomDoorPos = Offset(0.375f, 0.73f), corridorEntryPos = Offset(0.375f, 0.70f),
                corridorLaneY = 0.695f,
                diningSeatPos = Offset(0.23f, 0.56f), loungeSeatPos = Offset(0.73f, 0.58f)
            ),
            OfficeAgentProfile(
                id = 6, name = "Нова", role = "ПК", roomTitle = "Кабинет 7 • ПК",
                accentColor = Color(0xFFA78BFA), hairColor = Color(0xFF312E81), isTopRow = false,
                deskChairPos = Offset(0.625f, 0.83f), roomDoorPos = Offset(0.625f, 0.73f), corridorEntryPos = Offset(0.625f, 0.70f),
                corridorLaneY = 0.685f,
                diningSeatPos = Offset(0.32f, 0.56f), loungeSeatPos = Offset(0.82f, 0.58f)
            ),
            OfficeAgentProfile(
                id = 7, name = "Вика", role = "Смарт", roomTitle = "Кабинет 8 • Обзоры",
                accentColor = Color(0xFFFB7185), hairColor = Color(0xFF701A75), isTopRow = false,
                deskChairPos = Offset(0.875f, 0.83f), roomDoorPos = Offset(0.875f, 0.73f), corridorEntryPos = Offset(0.875f, 0.70f),
                corridorLaneY = 0.675f,
                diningSeatPos = Offset(0.40f, 0.56f), loungeSeatPos = Offset(0.90f, 0.58f)
            )
        )
    }

    var ticketCounter by remember { mutableIntStateOf(410) }
    var selectedAgentId by remember { mutableIntStateOf(7) }

    var isChiefHistoryExpanded by remember { mutableStateOf(false) }
    var isAgentHistoryExpanded by remember { mutableStateOf(false) }

    val chiefAuditLog = remember {
        mutableStateListOf(
            ChiefAuditEntry(
                id = "aud_1",
                ticketNumber = 409,
                agentName = "Вика",
                taskTitle = "Обзор Xiaomi 17T (6.83\" AMOLED, Dimensity 8500-Ultra, 6500 мАч)",
                verdict = "Подтверждено: ТТХ точные, дублей нет • Премия +2 500 ₽",
                salaryDelta = 2500,
                hasReprimand = false,
                isApproved = true,
                checkedAt = ZonedDateTime.now().minusMinutes(1).format(DateTimeFormatter.ofPattern("HH:mm:ss"))
            ),
            ChiefAuditEntry(
                id = "aud_2",
                ticketNumber = 408,
                agentName = "Макс",
                taskTitle = "Повторная статья «Xiaomi 17T — Ревизия #2»",
                verdict = "Отклонено: дубль уже выпущенной модели запрещён • 0 ₽",
                salaryDelta = 0,
                hasReprimand = false,
                isApproved = false,
                checkedAt = ZonedDateTime.now().minusMinutes(3).format(DateTimeFormatter.ofPattern("HH:mm:ss"))
            ),
            ChiefAuditEntry(
                id = "aud_3",
                ticketNumber = 407,
                agentName = "Артур",
                taskTitle = "Обзор Huawei Watch Fit 5 (1.82\" AMOLED, 5 ATM, до 10 дней)",
                verdict = "Подтверждено: характеристики проверены • Премия +2 000 ₽",
                salaryDelta = 2000,
                hasReprimand = false,
                isApproved = true,
                checkedAt = ZonedDateTime.now().minusMinutes(5).format(DateTimeFormatter.ofPattern("HH:mm:ss"))
            ),
            ChiefAuditEntry(
                id = "aud_4",
                ticketNumber = 406,
                agentName = "Нова",
                taskTitle = "Черновик ноутбука с ошибкой в типе процессора и портов",
                verdict = "Отклонено за неточность ТТХ (2-е нарушение) • Выговор и депремирование -2 000 ₽",
                salaryDelta = -2000,
                hasReprimand = true,
                isApproved = false,
                checkedAt = ZonedDateTime.now().minusMinutes(8).format(DateTimeFormatter.ofPattern("HH:mm:ss"))
            )
        )
    }

    val agentStatuses = remember {
        mutableStateMapOf<Int, AgentLiveStatus>().apply {
            agents.forEachIndexed { idx, ag ->
                val startSalary = when (ag.id) {
                    7 -> 18500
                    3 -> 17000
                    6 -> 12000 // У Новы был выговор и депремирование
                    else -> 15000 + (idx * 500)
                }
                val startRejects = if (ag.id == 6) 2 else if (ag.id == 1) 1 else 0
                val startReprimands = if (ag.id == 6) 1 else 0

                val pastTask1 = createSoloAgentTask(
                    ticketNumber = 380 + idx,
                    agentId = ag.id,
                    newsList = newsList
                ).copy(
                    completedAt = ZonedDateTime.now().minusMinutes(4).format(DateTimeFormatter.ofPattern("HH:mm:ss")),
                    chiefApproved = true,
                    salaryDelta = 2000
                )
                val pastTask2 = createSoloAgentTask(
                    ticketNumber = 370 + idx,
                    agentId = ag.id,
                    newsList = newsList
                ).copy(
                    completedAt = ZonedDateTime.now().minusMinutes(9).format(DateTimeFormatter.ofPattern("HH:mm:ss")),
                    chiefApproved = (ag.id != 6 && ag.id != 1),
                    salaryDelta = if (ag.id == 6) -2000 else if (ag.id == 1) 0 else 1500,
                    rejectReason = if (ag.id == 6) "Ошибка в ТТХ • Выговор и штраф -2 000 ₽" else if (ag.id == 1) "Повторная статья отклонена (0 ₽)" else ""
                )

                val activeTask = createSoloAgentTask(
                    ticketNumber = 400 + idx,
                    agentId = ag.id,
                    newsList = newsList
                )
                val startZone = if (idx == 5) AgentLocationZone.DINING else AgentLocationZone.DESK
                put(
                    ag.id,
                    AgentLiveStatus(
                        currentTask = activeTask,
                        progress = 0.25f + (idx * 0.08f),
                        targetZone = startZone,
                        breakSecondsRemaining = if (startZone != AgentLocationZone.DESK) 85f else 0f,
                        salaryBalance = startSalary,
                        rejectionsCount = startRejects,
                        reprimandsCount = startReprimands,
                        completedHistory = listOf(pastTask1, pastTask2)
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
                        AgentLocationZone.CHIEF_CHECK -> Offset(0.11f, 0.21f)
                        AgentLocationZone.DINING -> ag.diningSeatPos
                        AgentLocationZone.LOUNGE -> ag.loungeSeatPos
                    }
                )
            }
        }
    }

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
        var lastNanos = System.nanoTime()
        while (true) {
            delay(16L)
            val nowNanos = System.nanoTime()
            val dtSec = ((nowNanos - lastNanos) / 1_000_000_000f).coerceIn(0.005f, 0.05f)
            lastNanos = nowNanos

            val workDurationSec = if (isScanning) 45f else 90f
            val progressStep = dtSec / workDurationSec
            val walkStep = 0.16f * dtSec

            for (ag in agents) {
                val status = agentStatuses[ag.id] ?: continue
                val curPos = agentPositions[ag.id] ?: ag.deskChairPos
                val queue = agentPathQueues[ag.id].orEmpty()

                if (queue.isNotEmpty()) {
                    val targetWp = queue.first()
                    val dx = targetWp.x - curPos.x
                    val dy = targetWp.y - curPos.y
                    val dist = hypot(dx, dy)

                    if (dist <= walkStep) {
                        agentPositions[ag.id] = targetWp
                        val remaining = queue.drop(1)
                        agentPathQueues[ag.id] = remaining
                        agentWalkingFlags[ag.id] = remaining.isNotEmpty()
                    } else {
                        agentPositions[ag.id] = Offset(
                            curPos.x + (dx / dist) * walkStep,
                            curPos.y + (dy / dist) * walkStep
                        )
                        agentWalkingFlags[ag.id] = true
                    }
                    continue
                }

                agentWalkingFlags[ag.id] = false

                when (status.targetZone) {
                    AgentLocationZone.CHIEF_CHECK -> {
                        val remSec = status.breakSecondsRemaining - dtSec
                        if (remSec > 0f) {
                            agentStatuses[ag.id] = status.copy(breakSecondsRemaining = remSec)
                        } else {
                            val nowTime = ZonedDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"))
                            // Строгий фактчекинг ТТХ и проверка дублей у Авроры:
                            val isRejectedByChief = (status.currentTask.ticketNumber % 5 == 0)

                            var updatedSalary = status.salaryBalance
                            var updatedRejections = status.rejectionsCount
                            var updatedReprimands = status.reprimandsCount
                            val deltaSalary: Int
                            val issuedReprimand: Boolean
                            val reasonText: String

                            if (isRejectedByChief) {
                                updatedRejections += 1
                                if (updatedRejections >= 2 && updatedRejections % 2 == 0) {
                                    // Депремирование и официальный выговор за систематические ошибки в ТТХ или дубли
                                    deltaSalary = -2000
                                    updatedSalary = (updatedSalary - 2000).coerceAtLeast(0)
                                    updatedReprimands += 1
                                    issuedReprimand = true
                                    reasonText = "Отклонено (ошибка ТТХ/дубль) • Выговор #$updatedReprimands и депремирование -2 000 ₽"
                                } else {
                                    deltaSalary = 0
                                    issuedReprimand = false
                                    reasonText = "Отклонено (неточность в ТТХ или повтор темы) • Премия 0 ₽"
                                }

                                chiefAuditLog.add(
                                    0,
                                    ChiefAuditEntry(
                                        id = "aud_${System.currentTimeMillis()}_${ag.id}",
                                        ticketNumber = status.currentTask.ticketNumber,
                                        agentName = ag.name,
                                        taskTitle = status.currentTask.title,
                                        verdict = reasonText,
                                        salaryDelta = deltaSalary,
                                        hasReprimand = issuedReprimand,
                                        isApproved = false,
                                        checkedAt = nowTime
                                    )
                                )
                            } else {
                                // Аврора сама определяет размер премии за качественный материал с точными ТТХ
                                val bonusOptions = listOf(1500, 2000, 2500, 3000)
                                deltaSalary = bonusOptions[(status.currentTask.ticketNumber + ag.id) % bonusOptions.size]
                                updatedSalary += deltaSalary
                                issuedReprimand = false
                                reasonText = "Подтверждено: ТТХ сверены, дублей нет • Премия +$deltaSalary ₽"

                                chiefAuditLog.add(
                                    0,
                                    ChiefAuditEntry(
                                        id = "aud_${System.currentTimeMillis()}_${ag.id}",
                                        ticketNumber = status.currentTask.ticketNumber,
                                        agentName = ag.name,
                                        taskTitle = status.currentTask.title,
                                        verdict = reasonText,
                                        salaryDelta = deltaSalary,
                                        hasReprimand = false,
                                        isApproved = true,
                                        checkedAt = nowTime
                                    )
                                )

                                // Выпускаем обзор ТОЛЬКО если это абсолютно уникальное устройство (без ревизий!)
                                if (ag.id == 3 || ag.id == 7) {
                                    val existingNames = existingReviews.map { it.deviceName.trim().lowercase() }.toSet()
                                    val uniqueReview = ReviewCatalogFactory.generateUniqueNextArticle(existingNames)
                                    if (uniqueReview != null) {
                                        onNewReviewApproved(uniqueReview)
                                    }
                                }
                            }
                            if (chiefAuditLog.size > 18) {
                                chiefAuditLog.removeAt(chiefAuditLog.lastIndex)
                            }

                            val finishedTask = status.currentTask.copy(
                                completedAt = nowTime,
                                chiefApproved = !isRejectedByChief,
                                salaryDelta = deltaSalary,
                                rejectReason = if (isRejectedByChief) reasonText else ""
                            )

                            val restingCount = agentStatuses.values.count {
                                it.targetZone == AgentLocationZone.DINING || it.targetZone == AgentLocationZone.LOUNGE
                            }
                            val takeBreak = !isScanning && restingCount < 2 && ((ticketCounter + ag.id) % 3 == 0)

                            if (takeBreak) {
                                val chosenZone = if ((ticketCounter + ag.id) % 2 == 0) AgentLocationZone.DINING else AgentLocationZone.LOUNGE
                                agentStatuses[ag.id] = AgentLiveStatus(
                                    currentTask = status.currentTask,
                                    progress = 1.0f,
                                    targetZone = chosenZone,
                                    breakSecondsRemaining = 85f,
                                    salaryBalance = updatedSalary,
                                    rejectionsCount = updatedRejections,
                                    reprimandsCount = updatedReprimands,
                                    completedHistory = (listOf(finishedTask) + status.completedHistory).take(10)
                                )
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
                                    progress = 0.01f,
                                    targetZone = AgentLocationZone.DESK,
                                    breakSecondsRemaining = 0f,
                                    salaryBalance = updatedSalary,
                                    rejectionsCount = updatedRejections,
                                    reprimandsCount = updatedReprimands,
                                    completedHistory = (listOf(finishedTask) + status.completedHistory).take(10)
                                )
                                agentPathQueues[ag.id] = buildCleanPath(curPos, ag, AgentLocationZone.DESK)
                            }
                        }
                    }
                    AgentLocationZone.DINING, AgentLocationZone.LOUNGE -> {
                        val remSec = status.breakSecondsRemaining - dtSec
                        if (remSec > 0f) {
                            agentStatuses[ag.id] = status.copy(breakSecondsRemaining = remSec)
                        } else {
                            ticketCounter += 1
                            val nextTask = createSoloAgentTask(
                                ticketNumber = ticketCounter,
                                agentId = ag.id,
                                newsList = newsList
                            )
                            agentStatuses[ag.id] = status.copy(
                                currentTask = nextTask,
                                progress = 0.01f,
                                targetZone = AgentLocationZone.DESK,
                                breakSecondsRemaining = 0f
                            )
                            agentPathQueues[ag.id] = buildCleanPath(curPos, ag, AgentLocationZone.DESK)
                        }
                    }
                    AgentLocationZone.DESK -> {
                        val nextProg = status.progress + progressStep
                        if (nextProg >= 1.0f) {
                            agentStatuses[ag.id] = status.copy(
                                progress = 1.0f,
                                targetZone = AgentLocationZone.CHIEF_CHECK,
                                breakSecondsRemaining = 6f
                            )
                            agentPathQueues[ag.id] = buildCleanPath(curPos, ag, AgentLocationZone.CHIEF_CHECK)
                        } else {
                            agentStatuses[ag.id] = status.copy(progress = nextProg)
                        }
                    }
                }
            }
        }
    }

    val inspectedAgent = agents[selectedAgentId]
    val inspectedStatus = agentStatuses[selectedAgentId]
    val approvedCount = chiefAuditLog.count { it.isApproved }
    val rejectedCount = chiefAuditLog.count { !it.isApproved }

    val infiniteTransition = rememberInfiniteTransition(label = "officeAnimations")
    val walkSwing by infiniteTransition.animateFloat(
        initialValue = -1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 320, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "legSwing"
    )
    val typingHands by infiniteTransition.animateFloat(
        initialValue = -1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 380, easing = FastOutSlowInEasing),
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
    val garlandBlink by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "newYearLights"
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
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                        Text(
                            "Новогодний ИИ-Дом • Штат",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            "Ёлка, гирлянды, фактчекинг ТТХ, премии и выговоры от Авроры",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Закрыть")
                    }
                }

                // Карточка Директора Авроры со скрываемым журналом проверок ТТХ, премий и выговоров
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFFFBBF24).copy(alpha = 0.10f),
                    border = BorderStroke(1.dp, Color(0xFFFBBF24).copy(alpha = 0.45f))
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { isChiefHistoryExpanded = !isChiefHistoryExpanded },
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier.weight(1f).padding(end = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AdminPanelSettings,
                                    contentDescription = null,
                                    tint = Color(0xFFFBBF24),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    text = "Директор Аврора (Фактчекинг и премии)",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFFFBBF24),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Принято: $approvedCount • Откл: $rejectedCount",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                                )
                                Spacer(Modifier.width(4.dp))
                                Icon(
                                    imageVector = if (isChiefHistoryExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                    contentDescription = "Журнал проверок",
                                    tint = Color(0xFFFBBF24),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        AnimatedVisibility(visible = isChiefHistoryExpanded) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 4.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                HorizontalDivider(color = Color(0xFFFBBF24).copy(alpha = 0.25f))
                                Text(
                                    text = "Журнал решений Авроры (${chiefAuditLog.size}):",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                )
                                chiefAuditLog.forEach { log ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.Top,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (log.isApproved) Icons.Default.CheckCircle else if (log.hasReprimand) Icons.Default.Warning else Icons.Default.Cancel,
                                            contentDescription = null,
                                            tint = if (log.isApproved) Color(0xFF10B981) else Color(0xFFF43F5E),
                                            modifier = Modifier.size(14.dp).padding(top = 2.dp)
                                        )
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "[${log.checkedAt}] #${log.ticketNumber} ${log.agentName}: ${log.taskTitle}",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = log.verdict,
                                                fontSize = 10.sp,
                                                color = if (log.isApproved) Color(0xFF10B981) else Color(0xFFF43F5E)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Новогодний план ИИ-Дома: кабинет Авроры, 8 кабинетов, столовая, комната отдыха и большая ёлка в центре!
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(435.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(Color(0xFF0F172A))
                        .border(1.4.dp, Color(0xFF10B981).copy(alpha = 0.55f), RoundedCornerShape(18.dp))
                        .pointerInput(Unit) {
                            detectTapGestures { tap ->
                                val w = size.width.toFloat()
                                val h = size.height.toFloat()
                                if (tap.x < w * 0.20f && tap.y < h * 0.27f) {
                                    isChiefHistoryExpanded = !isChiefHistoryExpanded
                                    return@detectTapGestures
                                }
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

                        drawRect(color = Color(0xFF182032), size = size)

                        val topRoomsBottom = h * 0.27f
                        val centerRoomsTop = h * 0.36f
                        val centerRoomsBottom = h * 0.64f
                        val botRoomsTop = h * 0.73f

                        // Коридоры
                        drawRect(
                            color = Color(0xFF111827),
                            topLeft = Offset(0f, topRoomsBottom),
                            size = Size(w, botRoomsTop - topRoomsBottom)
                        )

                        // Новогодние снежинки в коридорах
                        val snowCoords = listOf(
                            Offset(0.05f, 0.32f), Offset(0.18f, 0.33f), Offset(0.49f, 0.31f),
                            Offset(0.84f, 0.32f), Offset(0.95f, 0.34f), Offset(0.08f, 0.68f),
                            Offset(0.49f, 0.69f), Offset(0.76f, 0.68f), Offset(0.93f, 0.69f)
                        )
                        snowCoords.forEachIndexed { idx, sc ->
                            val alphaSnow = if (idx % 2 == 0) 0.35f + 0.35f * garlandBlink else 0.70f - 0.35f * garlandBlink
                            drawCircle(
                                color = Color.White.copy(alpha = alphaSnow),
                                radius = 1.6.dp.toPx(),
                                center = Offset(w * sc.x, h * sc.y)
                            )
                        }

                        val wallColor = Color(0xFF475569)
                        val wallThickness = 2.4.dp.toPx()

                        // 1. Отдельный кабинет Директора Авроры (x: 0.00..0.20, y: 0.00..0.27)
                        val chiefRoomW = w * 0.20f
                        drawRect(
                            color = Color(0xFFFBBF24).copy(alpha = 0.14f),
                            topLeft = Offset(2.dp.toPx(), 2.dp.toPx()),
                            size = Size(chiefRoomW - 4.dp.toPx(), topRoomsBottom - 4.dp.toPx())
                        )
                        drawLine(Color(0xFFFBBF24), Offset(chiefRoomW, 0f), Offset(chiefRoomW, topRoomsBottom), wallThickness)
                        val chiefDoorX = chiefRoomW * 0.5f
                        val doorHalf = 13.dp.toPx()
                        drawLine(Color(0xFFFBBF24), Offset(0f, topRoomsBottom), Offset(chiefDoorX - doorHalf, topRoomsBottom), wallThickness)
                        drawLine(Color(0xFFFBBF24), Offset(chiefDoorX + doorHalf, topRoomsBottom), Offset(chiefRoomW, topRoomsBottom), wallThickness)

                        val avrChair = Offset(w * 0.08f, h * 0.155f)
                        drawDetailedWorkstation(
                            chairAnchor = avrChair,
                            isTopRow = true,
                            accentColor = Color(0xFFFBBF24),
                            isActiveWork = true,
                            progress = 1.0f,
                            scanPhase = screenScanPhase
                        )
                        drawDetailedHumanAgent(
                            center = avrChair,
                            isTopRow = true,
                            zone = AgentLocationZone.DESK,
                            isWalking = false,
                            shirtColor = Color(0xFFFBBF24),
                            hairColor = Color(0xFF78350F),
                            walkPhase = 0f,
                            typePhase = typingHands,
                            isSelected = false
                        )

                        // 2. 4 кабинета агентов справа от кабинета Авроры
                        val topAgentRoomW = (w * 0.80f) / 4f
                        for (col in 0..3) {
                            val left = chiefRoomW + col * topAgentRoomW
                            val right = left + topAgentRoomW
                            val doorCenter = left + topAgentRoomW / 2f
                            val ag = agents[col]
                            val isSel = ag.id == selectedAgentId

                            drawRect(
                                color = ag.accentColor.copy(alpha = if (isSel) 0.14f else 0.04f),
                                topLeft = Offset(left + 2.dp.toPx(), 2.dp.toPx()),
                                size = Size(topAgentRoomW - 4.dp.toPx(), topRoomsBottom - 4.dp.toPx())
                            )
                            drawLine(wallColor, Offset(right, 0f), Offset(right, topRoomsBottom), wallThickness)
                            drawLine(wallColor, Offset(left, topRoomsBottom), Offset(doorCenter - doorHalf, topRoomsBottom), wallThickness)
                            drawLine(wallColor, Offset(doorCenter + doorHalf, topRoomsBottom), Offset(right, topRoomsBottom), wallThickness)
                            drawLine(ag.accentColor.copy(alpha = 0.5f), Offset(doorCenter - doorHalf, topRoomsBottom), Offset(doorCenter + doorHalf, topRoomsBottom), 1.4.dp.toPx())
                        }

                        // Новогодние гирлянды с разноцветными огонёчками вдоль верхней и нижней стен коридора
                        val bulbColors = listOf(
                            Color(0xFFEF4444), // Красный
                            Color(0xFFFBBF24), // Золотой
                            Color(0xFF10B981), // Изумрудный
                            Color(0xFF38BDF8), // Голубой
                            Color(0xFFA855F7)  // Фиолетовый
                        )
                        val bulbCount = 24
                        for (b in 0..bulbCount) {
                            val bx = w * (b.toFloat() / bulbCount)
                            val c1 = bulbColors[b % bulbColors.size]
                            val c2 = bulbColors[(b + 2) % bulbColors.size]
                            val isEven = b % 2 == 0
                            val glowAlpha = if (isEven) 0.45f + 0.55f * garlandBlink else 1.0f - 0.55f * garlandBlink

                            // Верхняя гирлянда
                            drawCircle(c1.copy(alpha = glowAlpha * 0.35f), 4.5.dp.toPx(), Offset(bx, topRoomsBottom + 2.dp.toPx()))
                            drawCircle(c1.copy(alpha = glowAlpha), 2.1.dp.toPx(), Offset(bx, topRoomsBottom + 2.dp.toPx()))

                            // Нижняя гирлянда
                            drawCircle(c2.copy(alpha = glowAlpha * 0.35f), 4.5.dp.toPx(), Offset(bx, botRoomsTop - 2.dp.toPx()))
                            drawCircle(c2.copy(alpha = glowAlpha), 2.1.dp.toPx(), Offset(bx, botRoomsTop - 2.dp.toPx()))
                        }

                        // 3. Просторная Столовая слева (x: 0.03..0.44, y: 0.36..0.64)
                        val diningRect = Offset(w * 0.03f, centerRoomsTop)
                        val diningSize = Size(w * 0.41f, centerRoomsBottom - centerRoomsTop)
                        drawRoundRect(
                            color = Color(0xFF451A03).copy(alpha = 0.45f),
                            topLeft = diningRect,
                            size = diningSize,
                            cornerRadius = CornerRadius(12.dp.toPx(), 12.dp.toPx())
                        )
                        drawRoundRect(
                            color = Color(0xFFF59E0B).copy(alpha = 0.45f),
                            topLeft = diningRect,
                            size = diningSize,
                            cornerRadius = CornerRadius(12.dp.toPx(), 12.dp.toPx()),
                            style = Stroke(width = 1.2.dp.toPx())
                        )
                        drawRoundRect(
                            color = Color(0xFF1E293B),
                            topLeft = Offset(diningRect.x + 6.dp.toPx(), diningRect.y + 8.dp.toPx()),
                            size = Size(13.dp.toPx(), diningSize.height - 16.dp.toPx()),
                            cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                        )
                        val dTableX = diningRect.x + 25.dp.toPx()
                        val dTableY = diningRect.y + diningSize.height * 0.30f
                        val dTableW = diningSize.width - 33.dp.toPx()
                        val dTableH = 18.dp.toPx()
                        drawRoundRect(
                            color = Color(0xFF92400E),
                            topLeft = Offset(dTableX, dTableY),
                            size = Size(dTableW, dTableH),
                            cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                        )
                        for (p in 1..4) {
                            drawCircle(
                                color = Color.White.copy(alpha = 0.85f),
                                radius = 2.8.dp.toPx(),
                                center = Offset(dTableX + dTableW * (p / 5f), dTableY + dTableH * 0.5f)
                            )
                        }

                        // 4. Нарядная НОВОГОДНЯЯ ЁЛКА с подарками и звездой по центру холла (x: 0.50, y: 0.50)
                        val treeX = w * 0.50f
                        val treeTopY = centerRoomsTop + 8.dp.toPx()
                        val treeBottomY = centerRoomsBottom - 16.dp.toPx()
                        val treeHeight = treeBottomY - treeTopY

                        // Ствол
                        drawRoundRect(
                            color = Color(0xFF78350F),
                            topLeft = Offset(treeX - 3.5.dp.toPx(), treeBottomY - 2.dp.toPx()),
                            size = Size(7.dp.toPx(), 9.dp.toPx()),
                            cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
                        )

                        // 3 пушистых яруса хвои
                        for (tier in 0..2) {
                            val tierTop = treeTopY + tier * (treeHeight * 0.25f)
                            val tierBot = tierTop + treeHeight * 0.45f
                            val halfWidth = (10 + tier * 6).dp.toPx()
                            val path = Path().apply {
                                moveTo(treeX, tierTop)
                                lineTo(treeX - halfWidth, tierBot)
                                lineTo(treeX + halfWidth, tierBot)
                                close()
                            }
                            drawPath(path, color = Color(0xFF059669))
                            drawPath(path, color = Color(0xFF34D399), style = Stroke(width = 1.dp.toPx()))
                        }

                        // Золотая звезда на макушке
                        drawCircle(
                            color = Color(0xFFFBBF24).copy(alpha = 0.4f + 0.4f * garlandBlink),
                            radius = 6.5.dp.toPx(),
                            center = Offset(treeX, treeTopY - 1.dp.toPx())
                        )
                        drawCircle(
                            color = Color(0xFFFDE047),
                            radius = 3.2.dp.toPx(),
                            center = Offset(treeX, treeTopY - 1.dp.toPx())
                        )

                        // Разноцветные ёлочные шары и огонёчки на ёлке
                        val ornaments = listOf(
                            Triple(treeX - 4.dp.toPx(), treeTopY + treeHeight * 0.30f, Color(0xFFEF4444)),
                            Triple(treeX + 5.dp.toPx(), treeTopY + treeHeight * 0.38f, Color(0xFFFBBF24)),
                            Triple(treeX - 7.dp.toPx(), treeTopY + treeHeight * 0.58f, Color(0xFF38BDF8)),
                            Triple(treeX + 2.dp.toPx(), treeTopY + treeHeight * 0.64f, Color(0xFFF43F5E)),
                            Triple(treeX + 9.dp.toPx(), treeTopY + treeHeight * 0.80f, Color(0xFFFBBF24)),
                            Triple(treeX - 10.dp.toPx(), treeTopY + treeHeight * 0.84f, Color(0xFFA855F7))
                        )
                        ornaments.forEachIndexed { idx, (ox, oy, oColor) ->
                            val blink = if (idx % 2 == 0) 0.5f + 0.5f * garlandBlink else 1.0f - 0.5f * garlandBlink
                            drawCircle(oColor.copy(alpha = blink), 2.3.dp.toPx(), Offset(ox, oy))
                        }

                        // Коробки с новогодними подарками под ёлкой
                        drawRoundRect(
                            color = Color(0xFFEF4444),
                            topLeft = Offset(treeX - 13.dp.toPx(), treeBottomY + 2.dp.toPx()),
                            size = Size(8.dp.toPx(), 7.dp.toPx()),
                            cornerRadius = CornerRadius(1.5.dp.toPx(), 1.5.dp.toPx())
                        )
                        drawRoundRect(
                            color = Color(0xFF38BDF8),
                            topLeft = Offset(treeX + 5.dp.toPx(), treeBottomY + 2.dp.toPx()),
                            size = Size(8.dp.toPx(), 7.dp.toPx()),
                            cornerRadius = CornerRadius(1.5.dp.toPx(), 1.5.dp.toPx())
                        )

                        // 5. Просторная Комната отдыха с ТВ справа (x: 0.56..0.97, y: 0.36..0.64)
                        val loungeRect = Offset(w * 0.56f, centerRoomsTop)
                        val loungeSize = Size(w * 0.41f, centerRoomsBottom - centerRoomsTop)
                        drawRoundRect(
                            color = Color(0xFF1E1B4B).copy(alpha = 0.48f),
                            topLeft = loungeRect,
                            size = loungeSize,
                            cornerRadius = CornerRadius(12.dp.toPx(), 12.dp.toPx())
                        )
                        drawRoundRect(
                            color = Color(0xFF818CF8).copy(alpha = 0.45f),
                            topLeft = loungeRect,
                            size = loungeSize,
                            cornerRadius = CornerRadius(12.dp.toPx(), 12.dp.toPx()),
                            style = Stroke(width = 1.2.dp.toPx())
                        )
                        val tvLeft = loungeRect.x + loungeSize.width * 0.16f
                        val tvWidth = loungeSize.width * 0.68f
                        val tvTop = loungeRect.y + 7.dp.toPx()
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
                            strokeWidth = 2.4.dp.toPx()
                        )
                        drawRoundRect(
                            color = Color(0xFF4338CA),
                            topLeft = Offset(loungeRect.x + 12.dp.toPx(), loungeRect.y + loungeSize.height * 0.52f),
                            size = Size(loungeSize.width - 24.dp.toPx(), 20.dp.toPx()),
                            cornerRadius = CornerRadius(7.dp.toPx(), 7.dp.toPx())
                        )

                        // 6. 4 кабинета нижнего ряда
                        val botColWidth = w / 4f
                        for (col in 0..3) {
                            val left = col * botColWidth
                            val right = left + botColWidth
                            val doorCenter = left + botColWidth / 2f
                            val botAgent = agents[col + 4]
                            val isBotSel = botAgent.id == selectedAgentId

                            drawRect(
                                color = botAgent.accentColor.copy(alpha = if (isBotSel) 0.14f else 0.04f),
                                topLeft = Offset(left + 2.dp.toPx(), botRoomsTop + 2.dp.toPx()),
                                size = Size(botColWidth - 4.dp.toPx(), h - botRoomsTop - 4.dp.toPx())
                            )
                            drawLine(wallColor, Offset(right, botRoomsTop), Offset(right, h), wallThickness)
                            drawLine(wallColor, Offset(left, botRoomsTop), Offset(doorCenter - doorHalf, botRoomsTop), wallThickness)
                            drawLine(wallColor, Offset(doorCenter + doorHalf, botRoomsTop), Offset(right, botRoomsTop), wallThickness)
                            drawLine(botAgent.accentColor.copy(alpha = 0.5f), Offset(doorCenter - doorHalf, botRoomsTop), Offset(doorCenter + doorHalf, botRoomsTop), 1.4.dp.toPx())
                        }

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
                            .padding(top = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier.weight(0.20f),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("Аврора", fontSize = 9.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFFFBBF24), maxLines = 1)
                            Text("Директор", fontSize = 7.sp, color = Color(0xFFFDE68A), maxLines = 1)
                        }
                        agents.take(4).forEach { ag ->
                            Column(
                                modifier = Modifier.weight(0.20f),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(ag.name, fontSize = 9.sp, fontWeight = FontWeight.ExtraBold, color = ag.accentColor, maxLines = 1)
                                Text(ag.role, fontSize = 7.sp, color = Color.White.copy(alpha = 0.72f), maxLines = 1)
                            }
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.Center)
                            .padding(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "Столовая и кофе",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFFFCD34D),
                            modifier = Modifier.padding(top = 88.dp)
                        )
                        Text(
                            "2027",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF34D399),
                            modifier = Modifier.padding(top = 88.dp)
                        )
                        Text(
                            "Лаунж • OLED ТВ",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFFA5B4FC),
                            modifier = Modifier.padding(top = 88.dp)
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 4.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        agents.drop(4).forEach { ag ->
                            Column(
                                modifier = Modifier.weight(1f),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(ag.name, fontSize = 9.sp, fontWeight = FontWeight.ExtraBold, color = ag.accentColor, maxLines = 1)
                                Text(ag.role, fontSize = 7.sp, color = Color.White.copy(alpha = 0.72f), maxLines = 1)
                            }
                        }
                    }
                }

                // 2 ряда по 4 карточки сотрудников с отображением зарплаты и статуса
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    agents.chunked(4).forEach { rowAgents ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            rowAgents.forEach { ag ->
                                val isSel = selectedAgentId == ag.id
                                val st = agentStatuses[ag.id]
                                val salText = "${st?.salaryBalance ?: 15000} ₽"
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
                                            text = salText,
                                            fontSize = 8.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color(0xFF10B981),
                                            maxLines = 1
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Карточка выбранного сотрудника с балансом зарплаты, выговорами и скрываемой историей задач
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
                                    color = inspectedAgent.accentColor,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f).padding(end = 8.dp)
                                )
                                Surface(
                                    shape = RoundedCornerShape(7.dp),
                                    color = Color(0xFF10B981).copy(alpha = 0.18f)
                                ) {
                                    Text(
                                        text = "Баланс: ${inspectedStatus.salaryBalance} ₽",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color(0xFF10B981),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Отклонено: ${inspectedStatus.rejectionsCount} • Выговоры: ${inspectedStatus.reprimandsCount}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (inspectedStatus.reprimandsCount > 0) Color(0xFFF43F5E)
                                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f)
                                )
                                Text(
                                    text = when {
                                        isWalking -> "Идёт по коридору"
                                        inspectedStatus.targetZone == AgentLocationZone.CHIEF_CHECK -> "На проверке у Авроры"
                                        inspectedStatus.targetZone != AgentLocationZone.DESK ->
                                            "Перерыв: ${inspectedStatus.breakSecondsRemaining.toInt()}с"
                                        else -> "Задача #${inspectedStatus.currentTask.ticketNumber} (${(inspectedStatus.progress * 100).toInt()}%)"
                                    },
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = inspectedAgent.accentColor
                                )
                            }

                            Text(
                                text = when {
                                    isWalking -> "Направляется: ${inspectedStatus.targetZone.label.lowercase()}."
                                    inspectedStatus.targetZone == AgentLocationZone.CHIEF_CHECK ->
                                        "Аврора проверяет точность ТТХ и уникальность устройства."
                                    inspectedStatus.targetZone == AgentLocationZone.DINING ->
                                        "Обедает в столовой (осталось ${inspectedStatus.breakSecondsRemaining.toInt()} сек)."
                                    inspectedStatus.targetZone == AgentLocationZone.LOUNGE ->
                                        "Отдыхает у новогодней ёлки и ТВ (осталось ${inspectedStatus.breakSecondsRemaining.toInt()} сек)."
                                    else -> "В работе: ${inspectedStatus.currentTask.title}"
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

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { isAgentHistoryExpanded = !isAgentHistoryExpanded }
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.History,
                                        contentDescription = null,
                                        tint = inspectedAgent.accentColor,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(Modifier.width(6.dp))
                                    Text(
                                        text = "История задач и начислений (${inspectedStatus.completedHistory.size})",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f)
                                    )
                                }
                                Icon(
                                    imageVector = if (isAgentHistoryExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                    contentDescription = "Показать историю",
                                    tint = inspectedAgent.accentColor,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            AnimatedVisibility(visible = isAgentHistoryExpanded) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 4.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    inspectedStatus.completedHistory.forEach { done ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.Top,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Icon(
                                                imageVector = if (done.chiefApproved) Icons.Default.CheckCircle else Icons.Default.Cancel,
                                                contentDescription = null,
                                                tint = if (done.chiefApproved) Color(0xFF10B981) else Color(0xFFF43F5E),
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
                                                    text = if (done.chiefApproved)
                                                        "Подтверждено в ${done.completedAt} • Премия +${done.salaryDelta} ₽"
                                                    else
                                                        "${done.rejectReason} (${done.completedAt})",
                                                    fontSize = 9.sp,
                                                    color = if (done.chiefApproved) Color(0xFF10B981) else Color(0xFFF43F5E)
                                                )
                                            }
                                        }
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
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(17.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = if (isScanning) "Агенты собирают свежие новости..." else "Запустить общий сбор и фактчекинг",
                        fontSize = 12.sp,
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
    val deskW = 42.dp.toPx()
    val deskH = 18.dp.toPx()
    val deskCenterY = chairAnchor.y + dir * 15.dp.toPx()

    drawCircle(
        color = Color(0xFF334155),
        radius = 7.dp.toPx(),
        center = chairAnchor
    )
    drawRoundRect(
        color = Color(0xFF475569),
        topLeft = Offset(chairAnchor.x - 7.dp.toPx(), chairAnchor.y - dir * 5.dp.toPx() - 2.dp.toPx()),
        size = Size(14.dp.toPx(), 3.dp.toPx()),
        cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
    )

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

    val kbY = deskCenterY - dir * 4.5.dp.toPx()
    drawRoundRect(
        color = Color(0xFF0F172A),
        topLeft = Offset(chairAnchor.x - 10.dp.toPx(), kbY - 1.8.dp.toPx()),
        size = Size(16.dp.toPx(), 3.6.dp.toPx()),
        cornerRadius = CornerRadius(1.5.dp.toPx(), 1.5.dp.toPx())
    )

    val monW = 24.dp.toPx()
    val monH = 9.dp.toPx()
    val monCenterY = deskCenterY + dir * 3.5.dp.toPx()
    val monTopLeft = Offset(chairAnchor.x - monW / 2f, monCenterY - monH / 2f)

    drawRoundRect(
        color = Color(0xFF090D16),
        topLeft = monTopLeft,
        size = Size(monW, monH),
        cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
    )
    drawRoundRect(
        color = if (isActiveWork) accentColor else Color(0xFF475569),
        topLeft = monTopLeft,
        size = Size(monW, monH),
        cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx()),
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
            start = Offset(scrLeft + 1.5.dp.toPx(), scrTop + 1.5.dp.toPx()),
            end = Offset(scrLeft + 1.5.dp.toPx() + line1W, scrTop + 1.5.dp.toPx()),
            strokeWidth = 1.1.dp.toPx(),
            cap = StrokeCap.Round
        )
        drawLine(
            color = Color.White.copy(alpha = 0.85f),
            start = Offset(scrLeft + 1.5.dp.toPx(), scrTop + 3.2.dp.toPx()),
            end = Offset(scrLeft + 1.5.dp.toPx() + line2W, scrTop + 3.2.dp.toPx()),
            strokeWidth = 1.1.dp.toPx(),
            cap = StrokeCap.Round
        )
        drawLine(
            color = Color(0xFF34D399),
            start = Offset(scrLeft + 1.5.dp.toPx(), scrTop + scrH - 1.dp.toPx()),
            end = Offset(scrLeft + 1.5.dp.toPx() + (scrW - 3.dp.toPx()) * progress.coerceIn(0.1f, 1f), scrTop + scrH - 1.dp.toPx()),
            strokeWidth = 1.2.dp.toPx(),
            cap = StrokeCap.Round
        )
    } else {
        drawCircle(
            color = Color(0xFFF59E0B).copy(alpha = 0.75f),
            radius = 1.4.dp.toPx(),
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
            radius = 15.dp.toPx(),
            center = center
        )
        drawCircle(
            color = shirtColor.copy(alpha = 0.75f),
            radius = 15.dp.toPx(),
            center = center,
            style = Stroke(width = 1.2.dp.toPx())
        )
    }

    val skinColor = Color(0xFFFDE68A)
    val pantsColor = Color(0xFF1E293B)
    val shoeColor = Color(0xFF090D16)

    if (isWalking) {
        val leftStep = walkPhase * 3.8.dp.toPx()
        val rightStep = -walkPhase * 3.8.dp.toPx()

        drawLine(
            color = pantsColor,
            start = Offset(center.x - 2.6.dp.toPx(), center.y + 3.dp.toPx()),
            end = Offset(center.x - 2.8.dp.toPx(), center.y + 9.dp.toPx() + leftStep),
            strokeWidth = 2.8.dp.toPx(),
            cap = StrokeCap.Round
        )
        drawCircle(shoeColor, 1.7.dp.toPx(), Offset(center.x - 2.8.dp.toPx(), center.y + 9.5.dp.toPx() + leftStep))

        drawLine(
            color = pantsColor,
            start = Offset(center.x + 2.6.dp.toPx(), center.y + 3.dp.toPx()),
            end = Offset(center.x + 2.8.dp.toPx(), center.y + 9.dp.toPx() + rightStep),
            strokeWidth = 2.8.dp.toPx(),
            cap = StrokeCap.Round
        )
        drawCircle(shoeColor, 1.7.dp.toPx(), Offset(center.x + 2.8.dp.toPx(), center.y + 9.5.dp.toPx() + rightStep))
    } else if (zone == AgentLocationZone.LOUNGE || zone == AgentLocationZone.DINING) {
        drawLine(
            color = pantsColor,
            start = Offset(center.x - 2.8.dp.toPx(), center.y + 3.dp.toPx()),
            end = Offset(center.x - 2.8.dp.toPx(), center.y + 7.5.dp.toPx()),
            strokeWidth = 2.8.dp.toPx(),
            cap = StrokeCap.Round
        )
        drawLine(
            color = pantsColor,
            start = Offset(center.x + 2.8.dp.toPx(), center.y + 3.dp.toPx()),
            end = Offset(center.x + 2.8.dp.toPx(), center.y + 7.5.dp.toPx()),
            strokeWidth = 2.8.dp.toPx(),
            cap = StrokeCap.Round
        )
    }

    drawRoundRect(
        color = shirtColor,
        topLeft = Offset(center.x - 6.5.dp.toPx(), center.y - 3.8.dp.toPx()),
        size = Size(13.dp.toPx(), 8.5.dp.toPx()),
        cornerRadius = CornerRadius(3.8.dp.toPx(), 3.8.dp.toPx())
    )

    if (zone == AgentLocationZone.DESK && !isWalking) {
        val dir = if (isTopRow) -1f else 1f
        val kbReachY = center.y + dir * 10.5.dp.toPx()
        val shift = typePhase * 1.4.dp.toPx()
        drawLine(
            color = skinColor,
            start = Offset(center.x - 5.5.dp.toPx(), center.y + dir * 1.8.dp.toPx()),
            end = Offset(center.x - 3.2.dp.toPx(), kbReachY + shift),
            strokeWidth = 2.2.dp.toPx(),
            cap = StrokeCap.Round
        )
        drawLine(
            color = skinColor,
            start = Offset(center.x + 5.5.dp.toPx(), center.y + dir * 1.8.dp.toPx()),
            end = Offset(center.x + 3.2.dp.toPx(), kbReachY - shift),
            strokeWidth = 2.2.dp.toPx(),
            cap = StrokeCap.Round
        )
    } else if (isWalking) {
        val armSwing = -walkPhase * 2.5.dp.toPx()
        drawLine(
            color = skinColor,
            start = Offset(center.x - 6.5.dp.toPx(), center.y - 1.dp.toPx()),
            end = Offset(center.x - 7.5.dp.toPx(), center.y + 3.dp.toPx() + armSwing),
            strokeWidth = 2.1.dp.toPx(),
            cap = StrokeCap.Round
        )
        drawLine(
            color = skinColor,
            start = Offset(center.x + 6.5.dp.toPx(), center.y - 1.dp.toPx()),
            end = Offset(center.x + 7.5.dp.toPx(), center.y + 3.dp.toPx() - armSwing),
            strokeWidth = 2.1.dp.toPx(),
            cap = StrokeCap.Round
        )
    } else if (zone == AgentLocationZone.DINING) {
        drawLine(
            color = skinColor,
            start = Offset(center.x + 5.dp.toPx(), center.y),
            end = Offset(center.x + 8.dp.toPx(), center.y - 2.2.dp.toPx()),
            strokeWidth = 2.1.dp.toPx(),
            cap = StrokeCap.Round
        )
        drawCircle(Color(0xFFFBBF24), 2.dp.toPx(), Offset(center.x + 8.5.dp.toPx(), center.y - 2.6.dp.toPx()))
    }

    val headCenter = Offset(center.x, center.y - 6.dp.toPx())
    drawCircle(
        color = skinColor,
        radius = 4.5.dp.toPx(),
        center = headCenter
    )
    drawArc(
        color = hairColor,
        startAngle = 180f,
        sweepAngle = 180f,
        useCenter = true,
        topLeft = Offset(headCenter.x - 4.5.dp.toPx(), headCenter.y - 4.5.dp.toPx()),
        size = Size(9.dp.toPx(), 9.dp.toPx())
    )
    val lookingBack = zone == AgentLocationZone.DESK && isTopRow && !isWalking
    if (!lookingBack) {
        drawCircle(Color(0xFF0F172A), 0.8.dp.toPx(), Offset(headCenter.x - 1.5.dp.toPx(), headCenter.y + 0.5.dp.toPx()))
        drawCircle(Color(0xFF0F172A), 0.8.dp.toPx(), Offset(headCenter.x + 1.5.dp.toPx(), headCenter.y + 0.5.dp.toPx()))
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
        BorderStroke(0.8.dp, primary.copy(alpha = 0.18f))
    } else null

    val cardContainerColor = if (settings.cardStyle == "filled") {
        primary.copy(alpha = 0.06f)
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
        Column(
            modifier = Modifier.padding(if (settings.cardStyle == "compact") 11.dp else 13.dp),
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
                    Text(
                        text = "${item.sourceLabel} • ${item.brand}",
                        color = accentColor,
                        fontSize = (11 * scale).sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (item.isVeryFresh6h) {
                        Spacer(Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(5.dp),
                            color = primary.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "NEW",
                                color = primary,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.ExtraBold,
                                maxLines = 1,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                            )
                        }
                    }
                }
                Text(
                    text = item.timeLabel,
                    fontSize = (10 * scale).sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                    maxLines = 1
                )
            }

            Text(
                text = item.title,
                fontSize = (15 * scale).sp,
                fontWeight = FontWeight.Bold,
                lineHeight = (20 * scale).sp,
                color = MaterialTheme.colorScheme.onSurface
            )

            if (settings.showSummary && settings.cardStyle != "compact") {
                Text(
                    text = item.summary,
                    fontSize = (13 * scale).sp,
                    lineHeight = (18 * scale).sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.80f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onOpen() },
                    shape = RoundedCornerShape(8.dp),
                    color = primary.copy(alpha = 0.13f),
                    border = BorderStroke(0.8.dp, primary.copy(alpha = 0.38f))
                ) {
                    Text(
                        text = "Читать ↗",
                        fontSize = (11 * scale).sp,
                        fontWeight = FontWeight.Bold,
                        color = primary,
                        maxLines = 1,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onShare() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Поделиться",
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
                            modifier = Modifier.size(15.dp)
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onToggleSave() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isSaved) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                            contentDescription = "В избранное",
                            tint = if (isSaved) primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
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

fun shareReviewArticle(context: Context, article: TechReviewArticle) {
    try {
        val specsText = article.specs.joinToString("\n") { "• ${it.label}: ${it.value}" }
        val shareBody = buildString {
            append("${article.headline}\n")
            append("Оценка: ${article.score} (${article.approvedByChief})\n\n")
            append("${article.leadSummary}\n\n")
            append("Характеристики (ТТХ):\n$specsText")
        }
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, article.headline)
            putExtra(Intent.EXTRA_TEXT, shareBody)
        }
        context.startActivity(Intent.createChooser(intent, "Поделиться обзором"))
    } catch (_: Exception) {
    }
}
