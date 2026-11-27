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
    val auditStage: Int = 0 // 0 = не перепроверялось, 1 = сверено Виктором, 2 = глубокий фактчекинг Елены завершён
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
            id = "uniq_oneplus_nord_buds_4_pro",
            deviceName = "OnePlus Nord Buds 4 Pro",
            brand = "OnePlus",
            category = "buds",
            categoryLabel = "Наушники",
            authorAgent = "Артур • Носимая электроника",
            approvedByChief = "ТТХ проверены Авророй",
            headline = "OnePlus Nord Buds 4 Pro: кодек LHDC 5.0, шумоподавление 55 дБ и драйверы 12.4 мм",
            leadSummary = "Детальный тест доступных флагманских TWS от OnePlus: поддержка Hi-Res аудио LHDC 5.0, глубокий бас BassWave и 44 часа автономности.",
            fullBody = "OnePlus Nord Buds 4 Pro получили крупные 12.4-мм титанизированные драйверы и редкую для своего класса поддержку Hi-Res кодека LHDC 5.0. Адаптивное шумоподавление анализирует форму ушного канала и отсекает до 55 дБ низкочастотного гула.",
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
            fullBody = "Xiaomi TV S Mini LED 75 сочетает большую 75-дюймовую диагональ с матрицей на 512 зон локальной подсветки и пиковой яркостью 1200 нит. В ночных сценах фильмов ореолы вокруг ярких объектов сведены к минимуму.",
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
            fullBody = "Galaxy Watch Ultra созданы для экстремальных нагрузок: корпус выполнен из титана Grade 4, экран прикрыт сапфировым стеклом, а влагозащита достигает 10 ATM.",
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
            fullBody = "HONOR MagicBook Art 14 выделяется уникальной съёмной веб-камерой на магнитах, которая хранится в боковом слоте корпуса и при необходимости крепится на верхнюю крышку.",
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
            id = "uniq_zeekr_7x",
            deviceName = "Zeekr 7X Performance",
            brand = "Электроавто",
            category = "car",
            categoryLabel = "Электроавто",
            authorAgent = "Авто-ИИ • Электроавто",
            approvedByChief = "ТТХ проверены Авророй",
            headline = "Zeekr 7X Performance: семейный электрокроссовер 800 В с разгоном за 3.8 секунды",
            leadSummary = "Разбираем характеристики электрокроссовера Zeekr 7X: батарея Golden Brick с зарядкой за 10.5 минут, пневмоподвеска и лидар.",
            fullBody = "Zeekr 7X построен на 800-вольтовой платформе SEA и поддерживает сверхбыструю зарядку 5.5C, пополняя заряд с 10% до 80% всего за 10.5 минут на мощных станциях.",
            specs = listOf(
                ReviewSpecRow("Силовая установка", "Полный привод, 2 мотора, 646 л.с. (475 кВт)"),
                ReviewSpecRow("Динамика", "Разгон 0–100 км/ч за 3.8 сек"),
                ReviewSpecRow("Батарея и запас", "100 кВт·ч CATL Qilin, до 705 км (CLTC)"),
                ReviewSpecRow("Зарядка", "Архитектура 800 В, быстрая зарядка до 480 кВт"),
                ReviewSpecRow("Салон и подвеска", "Пневмоподвеска, экран 16\" Mini-LED, автопилот Zeekr AD")
            ),
            pros = listOf("Рекордная скорость зарядки на архитектуре 800 В", "Просторный салон и комфортная пневмоподвеска"),
            cons = listOf("Отсутствие официальной гарантии в ряде стран"),
            score = "9.4 / 10",
            publishedAt = "Сегодня",
            accentHex = 0xFF22D3EE
        ),
        TechReviewArticle(
            id = "uniq_macbook_pro_14_m4",
            deviceName = "Apple MacBook Pro 14 M4 Pro",
            brand = "Apple",
            category = "laptop",
            categoryLabel = "Ноутбуки",
            authorAgent = "Нова • Обзоры ноутбуков",
            approvedByChief = "ТТХ проверены Авророй",
            headline = "Apple MacBook Pro 14 M4 Pro: порты Thunderbolt 5, нанотекстурный экран и 22 часа работы",
            leadSummary = "Обзор обновлённого профессионального ноутбука Apple на чипе M4 Pro с 24 ГБ объединённой памяти в базе.",
            fullBody = "MacBook Pro 14 на чипе M4 Pro получил три порта Thunderbolt 5 с пропускной способностью до 120 Гбит/с, яркость SDR до 1000 нит на улице и опциональное матовое нанотекстурное стекло.",
            specs = listOf(
                ReviewSpecRow("Экран", "14.2\" Liquid Retina XDR Mini-LED, 120 Гц, 1600 нит"),
                ReviewSpecRow("Процессор", "Apple M4 Pro (14 ядер CPU, 20 ядер GPU)"),
                ReviewSpecRow("Память", "24 ГБ объединённой памяти + 1 ТБ SSD"),
                ReviewSpecRow("Порты", "3× Thunderbolt 5, HDMI 2.1, SDXC, MagSafe 3"),
                ReviewSpecRow("Автономность", "До 22 часов воспроизведения видео")
            ),
            pros = listOf("Мощнейший чип M4 Pro и порты Thunderbolt 5", "Эталонная автономность до 22 часов"),
            cons = listOf("Высокая цена конфигураций с большим объёмом SSD"),
            score = "9.5 / 10",
            publishedAt = "Сегодня",
            accentHex = 0xFFA78BFA
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
            id = "uniq_lg_oled_g4_65",
            deviceName = "LG OLED evo G4 65\"",
            brand = "Умный дом и ТВ",
            category = "tv",
            categoryLabel = "Телевизоры",
            authorAgent = "Дом-Бот • Обзоры ТВ и дома",
            approvedByChief = "ТТХ проверены Авророй",
            headline = "LG OLED evo G4 65\": процессор Alpha 11 AI, микролинзы MLA и 144 Гц для игр",
            leadSummary = "Обзор флагманского OLED-телевизора LG с настенным креплением без зазора и четырьмя портами HDMI 2.1.",
            fullBody = "LG OLED G4 оснащён панелью с массивом микролинз второго поколения (MLA) и процессором Alpha 11 AI, обеспечивающим высокую пиковую яркость и идеальную совместимость с Dolby Vision и G-Sync.",
            specs = listOf(
                ReviewSpecRow("Матрица", "65\" WOLED MLA 4K (3840×2160), 144 Гц"),
                ReviewSpecRow("Процессор и HDR", "Alpha 11 AI 4K, Dolby Vision, HDR10, HLG"),
                ReviewSpecRow("Игровые функции", "4× HDMI 2.1 (48 Гбит/с), G-Sync, FreeSync, VRR 144 Гц"),
                ReviewSpecRow("Звук", "60 Вт (4.2 канала), Dolby Atmos, AI Sound Pro"),
                ReviewSpecRow("Система", "webOS 24 с 5-летней программой обновлений")
            ),
            pros = listOf("4 полноценных порта HDMI 2.1 на 48 Гбит/с", "Поддержка Dolby Vision и 144 Гц"),
            cons = listOf("Настольная подставка приобретается отдельно (в комплекте настенный кронштейн)"),
            score = "9.5 / 10",
            publishedAt = "Сегодня",
            accentHex = 0xFFF59E0B
        ),
        TechReviewArticle(
            id = "uniq_oppo_find_x8_pro",
            deviceName = "OPPO Find X8 Pro",
            brand = "OPPO",
            category = "phone",
            categoryLabel = "Смартфоны",
            authorAgent = "Вика • Обзоры смартфонов",
            approvedByChief = "ТТХ проверены Авророй",
            headline = "OPPO Find X8 Pro: два перископических телевика Hasselblad и кнопка камеры Quick Button",
            leadSummary = "Обзор флагмана OPPO с четырьмя камерами по 50 МП, процессором Dimensity 9400 и батареей 5910 мАч.",
            fullBody = "OPPO Find X8 Pro выделяется сразу двумя перископическими телеобъективами (3x и 6x) с цветопередачей Hasselblad и сенсорной кнопкой быстрого запуска камеры на боковой грани.",
            specs = listOf(
                ReviewSpecRow("Экран", "6.78\" LTPO AMOLED 1.5K, 1–120 Гц, Dolby Vision"),
                ReviewSpecRow("Процессор", "MediaTek Dimensity 9400 (3 нм)"),
                ReviewSpecRow("Камеры Hasselblad", "4× 50 МП (основная + 3x перископ + 6x перископ + ширик)"),
                ReviewSpecRow("Батарея", "5910 мАч, проводная 80 Вт / беспроводная 50 Вт"),
                ReviewSpecRow("Защита", "IP68 / IP69, переключатель режимов Alert Slider")
            ),
            pros = listOf("Два оптических перископа 3x и 6x", "Удобная кнопка камеры и физический переключатель звука"),
            cons = listOf("Разъём USB-C работает на скорости USB 3.2 Gen 1"),
            score = "9.3 / 10",
            publishedAt = "Сегодня",
            accentHex = 0xFF10B981
        ),
        TechReviewArticle(
            id = "uniq_realme_gt_7_pro",
            deviceName = "realme GT 7 Pro",
            brand = "realme",
            category = "phone",
            categoryLabel = "Смартфоны",
            authorAgent = "Вика • Обзоры смартфонов",
            approvedByChief = "ТТХ проверены Авророй",
            headline = "realme GT 7 Pro: Snapdragon 8 Elite, батарея 6500 мАч и зарядка 120 Вт за 37 минут",
            leadSummary = "Тестируем доступный флагман realme с ультразвуковым сканером отпечатков и режимом подводной съёмки IP69.",
            fullBody = "realme GT 7 Pro предлагает флагманский чип Snapdragon 8 Elite, экран Samsung Eco² OLED Plus без поляризатора и аккумулятор 6500 мАч с зарядкой 120 Вт.",
            specs = listOf(
                ReviewSpecRow("Экран", "6.78\" Eco² OLED Plus 1.5K, 1–120 Гц, 2000 нит"),
                ReviewSpecRow("Процессор", "Qualcomm Snapdragon 8 Elite (3 нм)"),
                ReviewSpecRow("Камеры", "50 МП Sony IMX906 (OIS) + 50 МП перископ 3x + 8 МП ширик"),
                ReviewSpecRow("Батарея", "6500 мАч Titan Battery, быстрая зарядка 120 Вт"),
                ReviewSpecRow("Защита", "IP68 / IP69 с режимом подводной фотосъёмки")
            ),
            pros = listOf("Молниеносная зарядка 120 Вт и батарея 6500 мАч", "Ультразвуковой сканер отпечатков работает даже мокрыми пальцами"),
            cons = listOf("Ультраширокоугольный модуль на 8 МП без автофокуса"),
            score = "9.2 / 10",
            publishedAt = "Сегодня",
            accentHex = 0xFFF59E0B
        )
    )

    fun generateUniqueNextArticle(existingNames: Set<String>, newsList: List<NewsItem>): TechReviewArticle {
        val nowStr = ZonedDateTime.now().format(DateTimeFormatter.ofPattern("Сегодня, HH:mm"))

        val fromPool = curatedDevicesPool.firstOrNull { candidate ->
            !existingNames.contains(candidate.deviceName.trim().lowercase())
        }
        if (fromPool != null) {
            return fromPool.copy(
                id = "uniq_${fromPool.id}_${System.currentTimeMillis()}",
                publishedAt = nowStr
            )
        }

        val dynamicDeviceTemplates = listOf(
            Triple("Xiaomi 17 Ultra", "Xiaomi", "phone"),
            Triple("POCO F8 Pro", "Xiaomi", "phone"),
            Triple("Redmi K90 Pro", "Xiaomi", "phone"),
            Triple("Xiaomi Watch S5", "Xiaomi", "watch"),
            Triple("Samsung Galaxy Z Fold 7", "Samsung", "phone"),
            Triple("Samsung Galaxy Buds 4 Pro", "Samsung", "buds"),
            Triple("Samsung Galaxy Book 5 Pro", "Samsung", "laptop"),
            Triple("Huawei Pura 80 Ultra", "Huawei", "phone"),
            Triple("Huawei Watch GT 6 Pro", "Huawei", "watch"),
            Triple("HONOR Magic 8 Pro", "HONOR", "phone"),
            Triple("HONOR Watch 5 Ultra", "HONOR", "watch"),
            Triple("OnePlus 15", "OnePlus", "phone"),
            Triple("OnePlus Watch 3", "OnePlus", "watch"),
            Triple("Google Pixel Watch 4", "Google", "watch"),
            Triple("Apple AirPods Pro 3", "Apple", "buds"),
            Triple("Apple iPad Pro M5 13\"", "Apple", "laptop"),
            Triple("vivo Watch GT 2", "vivo", "watch"),
            Triple("OPPO Watch X2", "OPPO", "watch"),
            Triple("realme Buds Air 7 Pro", "realme", "buds"),
            Triple("CMF Headphone Pro", "Nothing", "buds"),
            Triple("BYD Sealion 07 EV", "Электроавто", "car"),
            Triple("Li Auto L9 Ultra", "Электроавто", "car"),
            Triple("Tesla Model Y Juniper", "Электроавто", "car"),
            Triple("Sony Bravia 9 Mini-LED 75\"", "Умный дом и ТВ", "tv"),
            Triple("TCL C855 QD-Mini LED 65\"", "Умный дом и ТВ", "tv"),
            Triple("ASUS Zenbook S 16 OLED", "HONOR", "laptop")
        )

        val chosen = dynamicDeviceTemplates.firstOrNull { (devName, _, _) ->
            !existingNames.contains(devName.trim().lowercase())
        } ?: run {
            val idx = existingNames.size + 1
            val brandList = listOf("Xiaomi", "Samsung", "Huawei", "HONOR", "OnePlus", "Apple", "Google", "OPPO", "vivo", "realme")
            val b = brandList[idx % brandList.size]
            val matchingNews = newsList.firstOrNull { it.brand == b }
            val modelSuffix = matchingNews?.title?.split(" ")?.take(3)?.joinToString(" ")?.replace(Regex("[^A-Za-zА-Яа-я0-9 ]"), "")?.trim()
                ?.takeIf { it.length in 4..24 } ?: "$b Flagship Edition #$idx"
            Triple(modelSuffix, b, "phone")
        }

        val (devName, brand, cat) = chosen
        val catLabel = when (cat) {
            "watch" -> "Часы"
            "buds" -> "Наушники"
            "laptop" -> "Ноутбуки"
            "tv" -> "Телевизоры"
            "car" -> "Электроавто"
            else -> "Смартфоны"
        }
        val author = when (cat) {
            "watch", "buds" -> "Артур • Носимая электроника"
            "laptop" -> "Нова • Обзоры ноутбуков"
            "tv" -> "Дом-Бот • Обзоры ТВ и дома"
            "car" -> "Авто-ИИ • Электроавто"
            else -> "Вика • Обзоры смартфонов"
        }
        val specs = when (cat) {
            "watch" -> listOf(
                ReviewSpecRow("Экран", "1.5\" LTPO AMOLED, 466×466, сапфировое стекло, 2200 нит"),
                ReviewSpecRow("Корпус и защита", "Титановый безель, 5 ATM + IP69"),
                ReviewSpecRow("Навигация и связь", "Двухчастотный GPS, Bluetooth 5.4, NFC"),
                ReviewSpecRow("Датчики", "Пульс, SpO2, ЭКГ, мониторинг сна"),
                ReviewSpecRow("Автономность", "До 12 дней в стандартном режиме")
            )
            "buds" -> listOf(
                ReviewSpecRow("Излучатели", "Двойные коаксиальные 11 мм + микропланарный твитер"),
                ReviewSpecRow("Аудиокодеки", "LHDC 5.0, LDAC, AAC, SBC"),
                ReviewSpecRow("Шумоподавление", "Адаптивное ANC до 54 дБ + 3 микрофона"),
                ReviewSpecRow("Автономность", "До 9 ч без ANC, до 40 ч с кейсом"),
                ReviewSpecRow("Связь и защита", "Bluetooth 5.4, Multipoint, IP55")
            )
            "laptop" -> listOf(
                ReviewSpecRow("Экран", "14.5\" OLED 3K (3072×1920), 120 Гц, 100% DCI-P3"),
                ReviewSpecRow("Процессор", "Флагманский мобильный чип нового поколения (16 ядер)"),
                ReviewSpecRow("Память", "32 ГБ LPDDR5X + 1 ТБ NVMe PCIe 4.0 SSD"),
                ReviewSpecRow("Корпус и вес", "Алюминиево-магниевый корпус, 1.19 кг"),
                ReviewSpecRow("Батарея и порты", "75 Вт·ч, зарядка 100 Вт, 2× Thunderbolt 4 / USB4")
            )
            "tv" -> listOf(
                ReviewSpecRow("Матрица", "65\" QD-Mini LED 4K (3840×2160), 144 Гц"),
                ReviewSpecRow("Яркость и HDR", "Пиковая яркость до 2000 нит, Dolby Vision IQ, HDR10+"),
                ReviewSpecRow("Звук", "60 Вт (2.1.2 канала), Dolby Atmos"),
                ReviewSpecRow("Интерфейсы", "4× HDMI 2.1 (4K 144 Гц, VRR), Wi-Fi 6"),
                ReviewSpecRow("Платформа", "Smart TV с голосовым управлением")
            )
            "car" -> listOf(
                ReviewSpecRow("Силовая установка", "Полный привод AWD, два электромотора, 585 л.с."),
                ReviewSpecRow("Динамика", "Разгон 0–100 км/ч за 3.9 сек"),
                ReviewSpecRow("Батарея", "100 кВт·ч, запас хода до 720 км (CLTC)"),
                ReviewSpecRow("Зарядка", "Архитектура 800 В, 10–80% за 14 минут"),
                ReviewSpecRow("Ассистенты", "Лидар + камеры кругового обзора 360°")
            )
            else -> listOf(
                ReviewSpecRow("Дисплей", "6.78\" LTPO AMOLED 1.5K, 1–120 Гц, Dolby Vision"),
                ReviewSpecRow("Процессор", "Флагманский 3-нм чипсет, LPDDR5X, UFS 4.1"),
                ReviewSpecRow("Камеры", "50 МП (OIS) + 50 МП перископ + 50 МП ультраширик"),
                ReviewSpecRow("Батарея и зарядка", "6200 мАч Silicon-Carbon, зарядка 90 Вт"),
                ReviewSpecRow("Защита", "IP68 / IP69, стереодинамики, Wi-Fi 7")
            )
        }

        val accents = listOf(0xFF10B981, 0xFF38BDF8, 0xFFF59E0B, 0xFFA78BFA, 0xFF22D3EE, 0xFFF43F5E)
        val accentHex = accents[existingNames.size % accents.size]

        return TechReviewArticle(
            id = "dyn_${System.currentTimeMillis()}_${existingNames.size}",
            deviceName = devName,
            brand = brand,
            category = cat,
            categoryLabel = catLabel,
            authorAgent = author,
            approvedByChief = "ТТХ проверены Авророй",
            headline = "$devName: полный разбор возможностей, автономности и реальных ТТХ",
            leadSummary = "Детальный обзор новинки $devName от $brand: проверяем экран, производительность, камеры/акустику и автономность.",
            fullBody = "$devName стал одной из самых обсуждаемых новинок в линейке $brand. Инженеры сделали упор на сбалансированные характеристики, высокую энергоэффективность и современные беспроводные интерфейсы.\n\nПо итогам тестирования $devName уверенно справляется с ежедневными нагрузками и выделяется продуманной эргономикой.",
            specs = specs,
            pros = listOf("Сбалансированные характеристики и современный экран", "Высокая автономность и быстрая зарядка", "Актуальные беспроводные стандарты и защита корпуса"),
            cons = listOf("Высокая стартовая цена на старте продаж", "Комплектация зависит от региона поставки"),
            score = "9.2 / 10",
            publishedAt = nowStr,
            accentHex = accentHex
        )
    }

    // Двойная тщательная перепроверка уже опубликованных статей двумя специалистами (Виктор + Елена) без повторной публикации!
    fun deputyVerifyAndFixArticle(
        articles: List<TechReviewArticle>,
        newsList: List<NewsItem>
    ): Pair<List<TechReviewArticle>, DeputyEditEntry?> {
        if (articles.isEmpty()) return articles to null
        val nowStr = ZonedDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"))

        // Ищем статью со стадией проверки < 2 (сначала этап 1 от Виктора, затем глубокий аудит этапа 2 от Елены)
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
            "OnePlus Nord Buds 4 Pro" to ("Кодеки" to "LHDC 5.0 (до 96 кГц / 24 бит), AAC, SBC"),
            "Samsung Galaxy S26 Ultra" to ("Батарея" to "5000 мАч, проводная 45 Вт / беспроводная Qi2 25 Вт"),
            "Huawei MateBook X Pro" to ("Порты и батарея" to "2× Thunderbolt 4, 1× USB-C, 70 Вт·ч + адаптер 90 Вт GaN")
        )

        val stage2DeepRefinements: Map<String, Pair<String, String>> = mapOf(
            "Xiaomi 17T" to ("Дисплей" to "6.83\" AMOLED 1.5K (2772×1280), 120 Гц, ШИМ 3840 Гц, HDR10+, Dolby Vision"),
            "Huawei Watch Fit 5" to ("Экран" to "1.82\" AMOLED, 480×408 (347 PPI), 60 Гц, пик 1500 нит"),
            "Xiaomi Redmi Buds 8 Pro" to ("Шумоподавление" to "Адаптивное ANC до 55 дБ (4 кГц) + 3 микрофона с ветрозащитой"),
            "OnePlus 15R" to ("Экран" to "6.83\" плоский LTPS AMOLED 1.5K (2800×1272), 165 Гц, 10-бит"),
            "OnePlus Nord Buds 4 Pro" to ("Автономность" to "До 12 ч без ANC (AAC), до 44 ч с кейсом, 10 мин = 4 ч")
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
                "2-й этап сверки: уточнены точные цифры в пункте «$fieldLabel» по $matchingNewsHint (без дубля)"
            } else {
                "1-й этап сверки: исправлен пункт «$fieldLabel» по данным $matchingNewsHint (без дубля)"
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
                "2-й этап сверки: перепроверен параметр «$fixedFieldLabel» и исключены дубли"
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
        val raw = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY_REVIEWS, null)
        if (raw.isNullOrBlank()) {
            val initial = ReviewCatalogFactory.initialReviews()
            saveReviews(context, initial)
            return initial
        }
        return try {
            val arr = JSONArray(raw)
            if (arr.length() == 0) return ReviewCatalogFactory.initialReviews()
            List(arr.length()) { i ->
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
        } catch (_: Exception) {
            ReviewCatalogFactory.initialReviews()
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

    var newsList by remember { mutableStateOf(LocalCache.getCached(context)) }
    var savedList by remember { mutableStateOf(LocalCache.getSaved(context)) }
    var reviewArticles by remember { mutableStateOf(LocalCache.loadReviews(context)) }
    var openedReviewId by remember { mutableStateOf<String?>(null) }

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

    val openedReview = remember(reviewArticles, openedReviewId) {
        openedReviewId?.let { id -> reviewArticles.find { it.id == id } }
    }

    if (openedReview != null) {
        ReviewDetailDialog(
            article = openedReview,
            onShare = { shareReviewArticle(context, openedReview) },
            onDismiss = { openedReviewId = null }
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
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .clickable {
                                    val existing = reviewArticles.map { it.deviceName.trim().lowercase() }.toSet()
                                    val newRev = ReviewCatalogFactory.generateUniqueNextArticle(existing, newsList)
                                    val updated = listOf(newRev) + reviewArticles
                                    reviewArticles = updated
                                    LocalCache.saveReviews(context, updated)
                                    Toast.makeText(
                                        context,
                                        "Аврора утвердила и сохранила обзор: ${newRev.deviceName}",
                                        Toast.LENGTH_SHORT
                                    ).show()
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
                            val updated = listOf(approvedArticle) + reviewArticles
                            reviewArticles = updated
                            LocalCache.saveReviews(context, updated)
                        }
                    },
                    onReviewsSilentEdited = { editedList ->
                        reviewArticles = editedList
                        LocalCache.saveReviews(context, editedList)
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
    onReviewsSilentEdited: (List<TechReviewArticle>) -> Unit,
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
            onReviewsSilentEdited = onReviewsSilentEdited,
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
                    "Персонализация тем, шрифта и новогодний ИИ-Дом с сохранением",
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
                                "Кабинет ТТХ (Виктор + Елена), узкая кухня слева, пешеходные дорожки и снег",
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

// Маршрутизация строго по размеченным пешеходным дорожкам офиса!
private fun buildWalkwayPath(
    fromPos: Offset,
    ag: OfficeAgentProfile,
    destination: AgentLocationZone
): List<Offset> {
    val points = mutableListOf<Offset>()
    val isInsideTopRoom = fromPos.y < 0.32f
    val isInsideBotRoom = fromPos.y > 0.68f

    // 1. Выход из текущего кабинета на основную пешеходную магистраль
    if (isInsideTopRoom) {
        if (fromPos.x < 0.16f) {
            points.add(Offset(0.08f, 0.32f))
            points.add(Offset(0.08f, 0.35f))
        } else {
            points.add(ag.roomDoorPos)
            points.add(Offset(ag.roomDoorPos.x, 0.35f))
        }
    } else if (isInsideBotRoom) {
        points.add(ag.roomDoorPos)
        points.add(Offset(ag.roomDoorPos.x, 0.65f))
    } else {
        // Если уже в центральном холле — выходим на ближайшую дорожку
        if (fromPos.x < 0.20f) {
            points.add(Offset(0.21f, fromPos.y))
        }
    }

    val curRef = points.lastOrNull() ?: fromPos
    val topTrackY = 0.35f
    val botTrackY = 0.65f
    val verticalAxisX = 0.21f // Главная вертикальная пешеходная дорожка между верхней и нижней магистралью

    fun navigateToTrack(targetTrackY: Float) {
        if (abs(curRef.y - targetTrackY) > 0.05f) {
            points.add(Offset(verticalAxisX, curRef.y))
            points.add(Offset(verticalAxisX, targetTrackY))
        }
    }

    when (destination) {
        AgentLocationZone.DESK -> {
            val targetTrackY = if (ag.isTopRow) topTrackY else botTrackY
            navigateToTrack(targetTrackY)
            points.add(Offset(ag.roomDoorPos.x, targetTrackY))
            points.add(ag.roomDoorPos)
            points.add(ag.deskChairPos)
        }
        AgentLocationZone.CHIEF_CHECK -> {
            navigateToTrack(topTrackY)
            points.add(Offset(0.08f, topTrackY))
            points.add(Offset(0.08f, 0.32f))
            points.add(Offset(0.085f, 0.25f))
        }
        AgentLocationZone.DINING -> {
            // Идём по вертикальной дорожке x=0.21 к входу в узкую кухню слева
            points.add(Offset(verticalAxisX, curRef.y))
            points.add(Offset(verticalAxisX, ag.diningSeatPos.y))
            points.add(ag.diningSeatPos)
        }
        AgentLocationZone.LOUNGE -> {
            // Идём к правой лаунж-зоне у ёлки по верхней/нижней дорожке
            val laneY = if (curRef.y > 0.50f) botTrackY else topTrackY
            points.add(Offset(ag.loungeSeatPos.x, laneY))
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
    onReviewsSilentEdited: (List<TechReviewArticle>) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val primary = MaterialTheme.colorScheme.primary
    val currentReviewsState by rememberUpdatedState(existingReviews)

    // Расширенный кабинет проверки ТТХ (x: 0.16..0.38) с двумя сотрудниками (Виктор и Елена),
    // 4 кабинета верхнего ряда (x: 0.38..1.00), 4 кабинета нижнего ряда (x: 0.00..1.00),
    // узкая кухня у левой стенки (x: 0.00..0.16) и пешеходные дорожки!
    val agents = remember {
        listOf(
            OfficeAgentProfile(
                id = 0, name = "Алиса", role = "СМИ", roomTitle = "Кабинет 1 • СМИ",
                accentColor = Color(0xFF10B981), hairColor = Color(0xFF78350F), isTopRow = true,
                deskChairPos = Offset(0.457f, 0.19f), roomDoorPos = Offset(0.457f, 0.32f), corridorEntryPos = Offset(0.457f, 0.35f),
                corridorLaneY = 0.35f,
                diningSeatPos = Offset(0.115f, 0.43f), loungeSeatPos = Offset(0.70f, 0.54f)
            ),
            OfficeAgentProfile(
                id = 1, name = "Макс", role = "TG", roomTitle = "Кабинет 2 • TG",
                accentColor = Color(0xFF38BDF8), hairColor = Color(0xFF1E293B), isTopRow = true,
                deskChairPos = Offset(0.612f, 0.19f), roomDoorPos = Offset(0.612f, 0.32f), corridorEntryPos = Offset(0.612f, 0.35f),
                corridorLaneY = 0.35f,
                diningSeatPos = Offset(0.115f, 0.48f), loungeSeatPos = Offset(0.77f, 0.54f)
            ),
            OfficeAgentProfile(
                id = 2, name = "Инста", role = "IG", roomTitle = "Кабинет 3 • IG",
                accentColor = Color(0xFFF43F5E), hairColor = Color(0xFF9F1239), isTopRow = true,
                deskChairPos = Offset(0.767f, 0.19f), roomDoorPos = Offset(0.767f, 0.32f), corridorEntryPos = Offset(0.767f, 0.35f),
                corridorLaneY = 0.35f,
                diningSeatPos = Offset(0.115f, 0.53f), loungeSeatPos = Offset(0.84f, 0.54f)
            ),
            OfficeAgentProfile(
                id = 3, name = "Артур", role = "Часы/TWS", roomTitle = "Кабинет 4 • Обзоры",
                accentColor = Color(0xFF34D399), hairColor = Color(0xFF0F172A), isTopRow = true,
                deskChairPos = Offset(0.922f, 0.19f), roomDoorPos = Offset(0.922f, 0.32f), corridorEntryPos = Offset(0.922f, 0.35f),
                corridorLaneY = 0.35f,
                diningSeatPos = Offset(0.115f, 0.57f), loungeSeatPos = Offset(0.91f, 0.54f)
            ),
            OfficeAgentProfile(
                id = 4, name = "Авто-ИИ", role = "Авто", roomTitle = "Кабинет 5 • Авто",
                accentColor = Color(0xFF22D3EE), hairColor = Color(0xFF0F172A), isTopRow = false,
                deskChairPos = Offset(0.125f, 0.81f), roomDoorPos = Offset(0.125f, 0.68f), corridorEntryPos = Offset(0.125f, 0.65f),
                corridorLaneY = 0.65f,
                diningSeatPos = Offset(0.115f, 0.45f), loungeSeatPos = Offset(0.72f, 0.54f)
            ),
            OfficeAgentProfile(
                id = 5, name = "Дом-Бот", role = "ТВ/Дом", roomTitle = "Кабинет 6 • ТВ/Дом",
                accentColor = Color(0xFFF59E0B), hairColor = Color(0xFF451A03), isTopRow = false,
                deskChairPos = Offset(0.375f, 0.81f), roomDoorPos = Offset(0.375f, 0.68f), corridorEntryPos = Offset(0.375f, 0.65f),
                corridorLaneY = 0.65f,
                diningSeatPos = Offset(0.115f, 0.50f), loungeSeatPos = Offset(0.79f, 0.54f)
            ),
            OfficeAgentProfile(
                id = 6, name = "Нова", role = "ПК", roomTitle = "Кабинет 7 • ПК",
                accentColor = Color(0xFFA78BFA), hairColor = Color(0xFF312E81), isTopRow = false,
                deskChairPos = Offset(0.625f, 0.81f), roomDoorPos = Offset(0.625f, 0.68f), corridorEntryPos = Offset(0.625f, 0.65f),
                corridorLaneY = 0.65f,
                diningSeatPos = Offset(0.115f, 0.55f), loungeSeatPos = Offset(0.86f, 0.54f)
            ),
            OfficeAgentProfile(
                id = 7, name = "Вика", role = "Смарт", roomTitle = "Кабинет 8 • Обзоры",
                accentColor = Color(0xFFFB7185), hairColor = Color(0xFF701A75), isTopRow = false,
                deskChairPos = Offset(0.875f, 0.81f), roomDoorPos = Offset(0.875f, 0.68f), corridorEntryPos = Offset(0.875f, 0.65f),
                corridorLaneY = 0.65f,
                diningSeatPos = Offset(0.115f, 0.58f), loungeSeatPos = Offset(0.93f, 0.54f)
            )
        )
    }

    val loadedOffice = remember { LocalCache.loadOfficeState(context) }

    var ticketCounter by remember { mutableIntStateOf(loadedOffice?.ticketCounter ?: 410) }
    var selectedAgentId by remember { mutableIntStateOf(7) }

    var isChiefHistoryExpanded by remember { mutableStateOf(false) }
    var isDeputyHistoryExpanded by remember { mutableStateOf(false) }
    var isAgentHistoryExpanded by remember { mutableStateOf(false) }

    val chiefAuditLog = remember {
        mutableStateListOf<ChiefAuditEntry>().apply {
            if (loadedOffice != null && loadedOffice.chiefAuditLog.isNotEmpty()) {
                addAll(loadedOffice.chiefAuditLog)
            } else {
                add(
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
                    )
                )
                add(
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
                    )
                )
            }
        }
    }

    val deputyEditLog = remember {
        mutableStateListOf<DeputyEditEntry>().apply {
            if (loadedOffice != null && loadedOffice.deputyEditLog.isNotEmpty()) {
                addAll(loadedOffice.deputyEditLog)
            } else {
                add(
                    DeputyEditEntry(
                        id = "dep_init_1",
                        reviewerName = "Виктор + Елена",
                        deviceName = "Xiaomi 17T",
                        fixedField = "Защита и интерфейсы",
                        correctionDetail = "Уточнены интерфейсы Wi-Fi 7, Bluetooth 6.0 и USB 2.0 без дублирования статьи",
                        editedAt = ZonedDateTime.now().minusMinutes(2).format(DateTimeFormatter.ofPattern("HH:mm:ss"))
                    )
                )
            }
        }
    }

    val agentStatuses = remember {
        mutableStateMapOf<Int, AgentLiveStatus>().apply {
            val savedMap = loadedOffice?.agentStatuses
            agents.forEachIndexed { idx, ag ->
                val savedSt = savedMap?.get(ag.id)
                if (savedSt != null) {
                    put(ag.id, savedSt)
                } else {
                    val startSalary = when (ag.id) {
                        7 -> 18500
                        3 -> 17000
                        6 -> 12000
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
                            completedHistory = listOf(pastTask1)
                        )
                    )
                }
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
                        AgentLocationZone.CHIEF_CHECK -> Offset(0.085f, 0.25f)
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

    DisposableEffect(Unit) {
        onDispose {
            LocalCache.saveOfficeState(
                context = context,
                ticketCounter = ticketCounter,
                agentStatuses = agentStatuses.toMap(),
                chiefAuditLog = chiefAuditLog.toList(),
                deputyEditLog = deputyEditLog.toList()
            )
        }
    }

    LaunchedEffect(isScanning, newsList.size) {
        var lastNanos = System.nanoTime()
        var deputyAuditTimerSec = 8f
        var autoPersistTimerSec = 4f

        while (true) {
            delay(16L)
            val nowNanos = System.nanoTime()
            val dtSec = ((nowNanos - lastNanos) / 1_000_000_000f).coerceIn(0.005f, 0.05f)
            lastNanos = nowNanos

            autoPersistTimerSec -= dtSec
            if (autoPersistTimerSec <= 0f) {
                autoPersistTimerSec = 4f
                LocalCache.saveOfficeState(
                    context = context,
                    ticketCounter = ticketCounter,
                    agentStatuses = agentStatuses.toMap(),
                    chiefAuditLog = chiefAuditLog.toList(),
                    deputyEditLog = deputyEditLog.toList()
                )
            }

            // Двухэтапная тщательная проверка ТТХ в кабинете проверки (Виктор + Елена) без дублирования статей
            deputyAuditTimerSec -= dtSec
            if (deputyAuditTimerSec <= 0f) {
                deputyAuditTimerSec = if (isScanning) 11f else 18f
                val (updatedArticles, editEntry) = ReviewCatalogFactory.deputyVerifyAndFixArticle(
                    articles = currentReviewsState,
                    newsList = newsList
                )
                if (editEntry != null) {
                    onReviewsSilentEdited(updatedArticles)
                    deputyEditLog.add(0, editEntry)
                    if (deputyEditLog.size > 24) {
                        deputyEditLog.removeAt(deputyEditLog.lastIndex)
                    }
                    LocalCache.saveOfficeState(
                        context = context,
                        ticketCounter = ticketCounter,
                        agentStatuses = agentStatuses.toMap(),
                        chiefAuditLog = chiefAuditLog.toList(),
                        deputyEditLog = deputyEditLog.toList()
                    )
                }
            }

            val workDurationSec = if (isScanning) 42f else 85f
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

                                if (ag.id in listOf(3, 4, 5, 6, 7)) {
                                    val existingNames = currentReviewsState.map { it.deviceName.trim().lowercase() }.toSet()
                                    val uniqueReview = ReviewCatalogFactory.generateUniqueNextArticle(existingNames, newsList)
                                    onNewReviewApproved(uniqueReview)
                                }
                            }
                            if (chiefAuditLog.size > 24) {
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
                                    completedHistory = (listOf(finishedTask) + status.completedHistory).take(12)
                                )
                                agentPathQueues[ag.id] = buildWalkwayPath(curPos, ag, chosenZone)
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
                                    completedHistory = (listOf(finishedTask) + status.completedHistory).take(12)
                                )
                                agentPathQueues[ag.id] = buildWalkwayPath(curPos, ag, AgentLocationZone.DESK)
                            }

                            LocalCache.saveOfficeState(
                                context = context,
                                ticketCounter = ticketCounter,
                                agentStatuses = agentStatuses.toMap(),
                                chiefAuditLog = chiefAuditLog.toList(),
                                deputyEditLog = deputyEditLog.toList()
                            )
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
                            agentPathQueues[ag.id] = buildWalkwayPath(curPos, ag, AgentLocationZone.DESK)
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
                            agentPathQueues[ag.id] = buildWalkwayPath(curPos, ag, AgentLocationZone.CHIEF_CHECK)
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
    val snowFallPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 6500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "snowFall"
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
                            "Отдел ТТХ (Виктор + Елена), узкая кухня слева, дорожки и снег",
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

                // 1. Карточка Директора Авроры
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
                                    text = "Директор Аврора (Приёмка и премии)",
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

                // 2. Карточка отдела проверки ТТХ (Виктор + Елена: двухэтапный аудит без дублей!)
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFF38BDF8).copy(alpha = 0.10f),
                    border = BorderStroke(1.dp, Color(0xFF38BDF8).copy(alpha = 0.45f))
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { isDeputyHistoryExpanded = !isDeputyHistoryExpanded },
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier.weight(1f).padding(end = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.EditNote,
                                    contentDescription = null,
                                    tint = Color(0xFF38BDF8),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    text = "Отдел ТТХ: Виктор + Елена (Двойная сверка)",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF38BDF8),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Правок: ${deputyEditLog.size}",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                                )
                                Spacer(Modifier.width(4.dp))
                                Icon(
                                    imageVector = if (isDeputyHistoryExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                    contentDescription = "Журнал правок ТТХ",
                                    tint = Color(0xFF38BDF8),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        AnimatedVisibility(visible = isDeputyHistoryExpanded) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 4.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                HorizontalDivider(color = Color(0xFF38BDF8).copy(alpha = 0.25f))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Двухэтапная проверка опубликованных ТТХ без дублей:",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                        modifier = Modifier.weight(1f)
                                    )
                                    TextButton(
                                        onClick = {
                                            val (updatedList, entry) = ReviewCatalogFactory.deputyVerifyAndFixArticle(
                                                articles = currentReviewsState,
                                                newsList = newsList
                                            )
                                            if (entry != null) {
                                                onReviewsSilentEdited(updatedList)
                                                deputyEditLog.add(0, entry)
                                                Toast.makeText(
                                                    context,
                                                    "${entry.reviewerName} обновил(а) ТТХ для ${entry.deviceName}",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        },
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Text("Перепроверить", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF38BDF8))
                                    }
                                }
                                deputyEditLog.forEach { dep ->
                                    Column(modifier = Modifier.fillMaxWidth()) {
                                        Text(
                                            text = "[${dep.editedAt}] ${dep.reviewerName} • ${dep.deviceName} (${dep.fixedField})",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = dep.correctionDetail,
                                            fontSize = 10.sp,
                                            color = Color(0xFF38BDF8)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // 3. Просторный ИИ-Дом: кабинет ТТХ с 2 людьми (Виктор + Елена), узкая кухня у левой стенки, пешеходные дорожки, современная ёлка и снег!
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(455.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(Color(0xFF0F172A))
                        .border(1.4.dp, Color(0xFF10B981).copy(alpha = 0.55f), RoundedCornerShape(18.dp))
                        .pointerInput(Unit) {
                            detectTapGestures { tap ->
                                val w = size.width.toFloat()
                                val h = size.height.toFloat()
                                if (tap.x < w * 0.16f && tap.y < h * 0.32f) {
                                    isChiefHistoryExpanded = !isChiefHistoryExpanded
                                    return@detectTapGestures
                                }
                                if (tap.x in (w * 0.16f)..(w * 0.38f) && tap.y < h * 0.32f) {
                                    isDeputyHistoryExpanded = !isDeputyHistoryExpanded
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

                        val topRoomsBottom = h * 0.32f
                        val centerRoomsTop = h * 0.39f
                        val centerRoomsBottom = h * 0.61f
                        val botRoomsTop = h * 0.68f

                        // Общая зона холла и коридоров
                        drawRect(
                            color = Color(0xFF111827),
                            topLeft = Offset(0f, topRoomsBottom),
                            size = Size(w, botRoomsTop - topRoomsBottom)
                        )

                        // ПЕШЕХОДНЫЕ ДОРОЖКИ ПО ВСЕМУ ОФИСУ (с покрытием, бордюрами и центральной разметкой)
                        val roadWidth = 16.dp.toPx()
                        val roadFill = Color(0xFF1E293B)
                        val roadBorder = Color(0xFF334155)
                        val dashColor = Color(0xFF64748B).copy(alpha = 0.55f)
                        val dashEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f), 0f)

                        val topRoadY = h * 0.35f
                        val botRoadY = h * 0.65f
                        val vertRoadX = w * 0.21f
                        val rightVertRoadX = w * 0.78f

                        // Верхняя горизонтальная дорожка (от Авроры вдоль всех верхних кабинетов)
                        drawRect(
                            color = roadFill,
                            topLeft = Offset(w * 0.05f, topRoadY - roadWidth / 2f),
                            size = Size(w * 0.91f, roadWidth)
                        )
                        drawLine(roadBorder, Offset(w * 0.05f, topRoadY - roadWidth / 2f), Offset(w * 0.96f, topRoadY - roadWidth / 2f), 1.dp.toPx())
                        drawLine(roadBorder, Offset(w * 0.05f, topRoadY + roadWidth / 2f), Offset(w * 0.96f, topRoadY + roadWidth / 2f), 1.dp.toPx())
                        drawLine(dashColor, Offset(w * 0.06f, topRoadY), Offset(w * 0.95f, topRoadY), 1.2.dp.toPx(), pathEffect = dashEffect)

                        // Нижняя горизонтальная дорожка (вдоль всех нижних кабинетов)
                        drawRect(
                            color = roadFill,
                            topLeft = Offset(w * 0.08f, botRoadY - roadWidth / 2f),
                            size = Size(w * 0.85f, roadWidth)
                        )
                        drawLine(roadBorder, Offset(w * 0.08f, botRoadY - roadWidth / 2f), Offset(w * 0.93f, botRoadY - roadWidth / 2f), 1.dp.toPx())
                        drawLine(roadBorder, Offset(w * 0.08f, botRoadY + roadWidth / 2f), Offset(w * 0.93f, botRoadY + roadWidth / 2f), 1.dp.toPx())
                        drawLine(dashColor, Offset(w * 0.09f, botRoadY), Offset(w * 0.92f, botRoadY), 1.2.dp.toPx(), pathEffect = dashEffect)

                        // Главная вертикальная пешеходная дорожка (мимо узкой кухни слева между верхней и нижней магистралью)
                        drawRect(
                            color = roadFill,
                            topLeft = Offset(vertRoadX - roadWidth / 2f, topRoadY),
                            size = Size(roadWidth, botRoadY - topRoadY)
                        )
                        drawLine(roadBorder, Offset(vertRoadX - roadWidth / 2f, topRoadY + roadWidth / 2f), Offset(vertRoadX - roadWidth / 2f, botRoadY - roadWidth / 2f), 1.dp.toPx())
                        drawLine(roadBorder, Offset(vertRoadX + roadWidth / 2f, topRoadY + roadWidth / 2f), Offset(vertRoadX + roadWidth / 2f, botRoadY - roadWidth / 2f), 1.dp.toPx())
                        drawLine(dashColor, Offset(vertRoadX, topRoadY), Offset(vertRoadX, botRoadY), 1.2.dp.toPx(), pathEffect = dashEffect)

                        // Правая прогулочная дорожка (к ёлке и лаунж-зоне)
                        drawRect(
                            color = roadFill,
                            topLeft = Offset(rightVertRoadX - roadWidth / 2f, topRoadY),
                            size = Size(roadWidth, botRoadY - topRoadY)
                        )
                        drawLine(dashColor, Offset(rightVertRoadX, topRoadY), Offset(rightVertRoadX, botRoadY), 1.2.dp.toPx(), pathEffect = dashEffect)

                        // Ответвления дорожек к дверям кабинетов и к узкой кухне
                        drawRect(color = roadFill, topLeft = Offset(w * 0.08f - 6.dp.toPx(), topRoomsBottom), size = Size(12.dp.toPx(), topRoadY - topRoomsBottom))
                        drawRect(color = roadFill, topLeft = Offset(w * 0.27f - 6.dp.toPx(), topRoomsBottom), size = Size(12.dp.toPx(), topRoadY - topRoomsBottom))
                        drawRect(color = roadFill, topLeft = Offset(w * 0.14f, h * 0.49f), size = Size(vertRoadX - w * 0.14f, 11.dp.toPx()))

                        val wallColor = Color(0xFF475569)
                        val wallThickness = 2.2.dp.toPx()
                        val doorHalf = 10.dp.toPx()

                        // 1. Кабинет Директора Авроры (x: 0.00..0.16, y: 0.00..0.32)
                        val chiefW = w * 0.16f
                        drawRect(
                            color = Color(0xFFFBBF24).copy(alpha = 0.13f),
                            topLeft = Offset(2.dp.toPx(), 2.dp.toPx()),
                            size = Size(chiefW - 3.dp.toPx(), topRoomsBottom - 4.dp.toPx())
                        )
                        drawLine(Color(0xFFFBBF24), Offset(chiefW, 0f), Offset(chiefW, topRoomsBottom), wallThickness)
                        val chiefDoorX = chiefW * 0.5f
                        drawLine(Color(0xFFFBBF24), Offset(0f, topRoomsBottom), Offset(chiefDoorX - doorHalf, topRoomsBottom), wallThickness)
                        drawLine(Color(0xFFFBBF24), Offset(chiefDoorX + doorHalf, topRoomsBottom), Offset(chiefW, topRoomsBottom), wallThickness)

                        val avrChair = Offset(w * 0.08f, h * 0.19f)
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

                        // 2. Расширенный кабинет проверки ТТХ (x: 0.16..0.38, y: 0.00..0.32) — ДВА СОТРУДНИКА (Виктор + Елена)
                        val deputyLeft = chiefW
                        val deputyRight = w * 0.38f
                        val deputyW = deputyRight - deputyLeft
                        drawRect(
                            color = Color(0xFF38BDF8).copy(alpha = 0.13f),
                            topLeft = Offset(deputyLeft + 2.dp.toPx(), 2.dp.toPx()),
                            size = Size(deputyW - 3.dp.toPx(), topRoomsBottom - 4.dp.toPx())
                        )
                        drawLine(Color(0xFF38BDF8), Offset(deputyRight, 0f), Offset(deputyRight, topRoomsBottom), wallThickness)
                        val deputyDoorX = deputyLeft + deputyW * 0.5f
                        drawLine(Color(0xFF38BDF8), Offset(deputyLeft, topRoomsBottom), Offset(deputyDoorX - doorHalf, topRoomsBottom), wallThickness)
                        drawLine(Color(0xFF38BDF8), Offset(deputyDoorX + doorHalf, topRoomsBottom), Offset(deputyRight, topRoomsBottom), wallThickness)

                        // Первый специалист ТТХ — Виктор (левый стол в кабинете ТТХ)
                        val vicChair = Offset(w * 0.218f, h * 0.19f)
                        drawDetailedWorkstation(
                            chairAnchor = vicChair,
                            isTopRow = true,
                            accentColor = Color(0xFF38BDF8),
                            isActiveWork = true,
                            progress = 1.0f,
                            scanPhase = screenScanPhase
                        )
                        drawDetailedHumanAgent(
                            center = vicChair,
                            isTopRow = true,
                            zone = AgentLocationZone.DESK,
                            isWalking = false,
                            shirtColor = Color(0xFF38BDF8),
                            hairColor = Color(0xFF1E293B),
                            walkPhase = 0f,
                            typePhase = -typingHands,
                            isSelected = false
                        )

                        // Второй специалист ТТХ — Елена (правый стол в кабинете ТТХ)
                        val elenaChair = Offset(w * 0.322f, h * 0.19f)
                        drawDetailedWorkstation(
                            chairAnchor = elenaChair,
                            isTopRow = true,
                            accentColor = Color(0xFF2DD4BF),
                            isActiveWork = true,
                            progress = 1.0f,
                            scanPhase = (screenScanPhase + 0.5f) % 1f
                        )
                        drawDetailedHumanAgent(
                            center = elenaChair,
                            isTopRow = true,
                            zone = AgentLocationZone.DESK,
                            isWalking = false,
                            shirtColor = Color(0xFF2DD4BF),
                            hairColor = Color(0xFF701A75),
                            walkPhase = 0f,
                            typePhase = typingHands,
                            isSelected = false
                        )

                        // 3. 4 кабинета агентов верхнего ряда (x: 0.38..1.00, y: 0.00..0.32)
                        val topAgentRoomW = (w * 0.62f) / 4f
                        for (col in 0..3) {
                            val left = deputyRight + col * topAgentRoomW
                            val right = left + topAgentRoomW
                            val doorCenter = left + topAgentRoomW / 2f
                            val ag = agents[col]
                            val isSel = ag.id == selectedAgentId

                            drawRect(
                                color = ag.accentColor.copy(alpha = if (isSel) 0.15f else 0.05f),
                                topLeft = Offset(left + 2.dp.toPx(), 2.dp.toPx()),
                                size = Size(topAgentRoomW - 3.dp.toPx(), topRoomsBottom - 4.dp.toPx())
                            )
                            drawLine(wallColor, Offset(right, 0f), Offset(right, topRoomsBottom), wallThickness)
                            drawLine(wallColor, Offset(left, topRoomsBottom), Offset(doorCenter - doorHalf, topRoomsBottom), wallThickness)
                            drawLine(wallColor, Offset(doorCenter + doorHalf, topRoomsBottom), Offset(right, topRoomsBottom), wallThickness)
                            drawLine(ag.accentColor.copy(alpha = 0.55f), Offset(doorCenter - doorHalf, topRoomsBottom), Offset(doorCenter + doorHalf, topRoomsBottom), 1.4.dp.toPx())
                        }

                        // Новогодние гирлянды вдоль коридоров
                        val bulbColors = listOf(
                            Color(0xFFEF4444),
                            Color(0xFFFBBF24),
                            Color(0xFF10B981),
                            Color(0xFF38BDF8),
                            Color(0xFFA855F7)
                        )
                        val bulbCount = 26
                        for (b in 0..bulbCount) {
                            val bx = w * (b.toFloat() / bulbCount)
                            val c1 = bulbColors[b % bulbColors.size]
                            val c2 = bulbColors[(b + 2) % bulbColors.size]
                            val isEven = b % 2 == 0
                            val glowAlpha = if (isEven) 0.45f + 0.55f * garlandBlink else 1.0f - 0.55f * garlandBlink

                            drawCircle(c1.copy(alpha = glowAlpha * 0.35f), 4.2.dp.toPx(), Offset(bx, topRoomsBottom + 2.dp.toPx()))
                            drawCircle(c1.copy(alpha = glowAlpha), 2.0.dp.toPx(), Offset(bx, topRoomsBottom + 2.dp.toPx()))

                            drawCircle(c2.copy(alpha = glowAlpha * 0.35f), 4.2.dp.toPx(), Offset(bx, botRoomsTop - 2.dp.toPx()))
                            drawCircle(c2.copy(alpha = glowAlpha), 2.0.dp.toPx(), Offset(bx, botRoomsTop - 2.dp.toPx()))
                        }

                        // 4. УЗКАЯ КУХНЯ ВПЛОТНУЮ К ЛЕВОЙ СТЕНКЕ (x: 0.00..0.155, y: 0.39..0.61)
                        val kitchenRect = Offset(0f, centerRoomsTop)
                        val kitchenSize = Size(w * 0.155f, centerRoomsBottom - centerRoomsTop)
                        drawRoundRect(
                            color = Color(0xFF451A03).copy(alpha = 0.52f),
                            topLeft = kitchenRect,
                            size = kitchenSize,
                            cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx())
                        )
                        drawRoundRect(
                            color = Color(0xFFF59E0B).copy(alpha = 0.55f),
                            topLeft = kitchenRect,
                            size = kitchenSize,
                            cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx()),
                            style = Stroke(width = 1.2.dp.toPx())
                        )
                        // Узкий кухонный гарнитур и барная стойка вдоль самой левой стенки
                        drawRoundRect(
                            color = Color(0xFF78350F),
                            topLeft = Offset(3.dp.toPx(), kitchenRect.y + 8.dp.toPx()),
                            size = Size(10.dp.toPx(), kitchenSize.height - 16.dp.toPx()),
                            cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx())
                        )
                        // Кофемашина и микроволновка на столешнице слева
                        drawRect(
                            color = Color(0xFFCBD5E1),
                            topLeft = Offset(4.5.dp.toPx(), kitchenRect.y + 14.dp.toPx()),
                            size = Size(7.dp.toPx(), 9.dp.toPx())
                        )
                        drawRect(
                            color = Color(0xFF38BDF8),
                            topLeft = Offset(4.5.dp.toPx(), kitchenRect.y + 30.dp.toPx()),
                            size = Size(7.dp.toPx(), 11.dp.toPx())
                        )

                        // 5. СОВРЕМЕННАЯ ДИЗАЙНЕРСКАЯ ЁЛКА С ИГОЛОЧКАМИ в свободном центре (x: 0.46, y: 0.50)
                        val treeX = w * 0.46f
                        val treeTopY = centerRoomsTop - 4.dp.toPx()
                        val treeBottomY = centerRoomsBottom - 8.dp.toPx()
                        val treeHeight = treeBottomY - treeTopY

                        drawCircle(
                            color = Color(0xFF10B981).copy(alpha = 0.14f),
                            radius = 36.dp.toPx(),
                            center = Offset(treeX, treeTopY + treeHeight * 0.55f)
                        )

                        drawRoundRect(
                            color = Color(0xFF78350F),
                            topLeft = Offset(treeX - 3.dp.toPx(), treeBottomY - 2.dp.toPx()),
                            size = Size(6.dp.toPx(), 8.dp.toPx()),
                            cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
                        )

                        for (tier in 0..3) {
                            val tierTop = treeTopY + tier * (treeHeight * 0.21f)
                            val tierBot = tierTop + treeHeight * 0.36f
                            val halfWidth = (9 + tier * 5.5f).dp.toPx()
                            val path = Path().apply {
                                moveTo(treeX, tierTop)
                                lineTo(treeX - halfWidth, tierBot)
                                lineTo(treeX, tierBot - 3.dp.toPx())
                                lineTo(treeX + halfWidth, tierBot)
                                close()
                            }
                            drawPath(
                                path = path,
                                brush = Brush.verticalGradient(
                                    colors = listOf(Color(0xFF34D399), Color(0xFF047857)),
                                    startY = tierTop,
                                    endY = tierBot
                                )
                            )
                            drawPath(path, color = Color(0xFF6EE7B7).copy(alpha = 0.7f), style = Stroke(width = 0.9.dp.toPx()))

                            val needlesCount = 6 + tier * 2
                            for (n in 1..needlesCount) {
                                val frac = n.toFloat() / (needlesCount + 1)
                                val lx = treeX - halfWidth * frac
                                val rx = treeX + halfWidth * frac
                                val ny = tierTop + (tierBot - tierTop) * frac
                                drawLine(
                                    color = Color(0xFFA7F3D0).copy(alpha = 0.85f),
                                    start = Offset(lx, ny),
                                    end = Offset(lx - 2.6.dp.toPx(), ny + 1.8.dp.toPx()),
                                    strokeWidth = 0.9.dp.toPx(),
                                    cap = StrokeCap.Round
                                )
                                drawLine(
                                    color = Color(0xFFA7F3D0).copy(alpha = 0.85f),
                                    start = Offset(rx, ny),
                                    end = Offset(rx + 2.6.dp.toPx(), ny + 1.8.dp.toPx()),
                                    strokeWidth = 0.9.dp.toPx(),
                                    cap = StrokeCap.Round
                                )
                            }
                        }

                        drawLine(
                            color = Color(0xFFFDE047).copy(alpha = 0.75f),
                            start = Offset(treeX - 5.dp.toPx(), treeTopY + treeHeight * 0.25f),
                            end = Offset(treeX + 8.dp.toPx(), treeTopY + treeHeight * 0.36f),
                            strokeWidth = 1.2.dp.toPx(),
                            cap = StrokeCap.Round
                        )
                        drawLine(
                            color = Color(0xFF38BDF8).copy(alpha = 0.75f),
                            start = Offset(treeX + 10.dp.toPx(), treeTopY + treeHeight * 0.52f),
                            end = Offset(treeX - 13.dp.toPx(), treeTopY + treeHeight * 0.66f),
                            strokeWidth = 1.2.dp.toPx(),
                            cap = StrokeCap.Round
                        )
                        drawLine(
                            color = Color(0xFFF43F5E).copy(alpha = 0.75f),
                            start = Offset(treeX - 14.dp.toPx(), treeTopY + treeHeight * 0.78f),
                            end = Offset(treeX + 17.dp.toPx(), treeTopY + treeHeight * 0.90f),
                            strokeWidth = 1.2.dp.toPx(),
                            cap = StrokeCap.Round
                        )

                        drawCircle(
                            color = Color(0xFFFBBF24).copy(alpha = 0.45f + 0.45f * garlandBlink),
                            radius = 7.dp.toPx(),
                            center = Offset(treeX, treeTopY - 2.dp.toPx())
                        )
                        drawLine(
                            color = Color(0xFFFEF08A),
                            start = Offset(treeX, treeTopY - 7.dp.toPx()),
                            end = Offset(treeX, treeTopY + 3.dp.toPx()),
                            strokeWidth = 1.6.dp.toPx(),
                            cap = StrokeCap.Round
                        )
                        drawLine(
                            color = Color(0xFFFEF08A),
                            start = Offset(treeX - 5.dp.toPx(), treeTopY - 2.dp.toPx()),
                            end = Offset(treeX + 5.dp.toPx(), treeTopY - 2.dp.toPx()),
                            strokeWidth = 1.6.dp.toPx(),
                            cap = StrokeCap.Round
                        )

                        val ornaments = listOf(
                            Triple(treeX - 4.dp.toPx(), treeTopY + treeHeight * 0.28f, Color(0xFFEF4444)),
                            Triple(treeX + 5.dp.toPx(), treeTopY + treeHeight * 0.38f, Color(0xFFFBBF24)),
                            Triple(treeX - 8.dp.toPx(), treeTopY + treeHeight * 0.55f, Color(0xFF38BDF8)),
                            Triple(treeX + 3.dp.toPx(), treeTopY + treeHeight * 0.62f, Color(0xFFF43F5E)),
                            Triple(treeX + 11.dp.toPx(), treeTopY + treeHeight * 0.78f, Color(0xFFFBBF24)),
                            Triple(treeX - 12.dp.toPx(), treeTopY + treeHeight * 0.84f, Color(0xFFA855F7))
                        )
                        ornaments.forEachIndexed { idx, (ox, oy, oColor) ->
                            val blink = if (idx % 2 == 0) 0.5f + 0.5f * garlandBlink else 1.0f - 0.5f * garlandBlink
                            drawCircle(oColor.copy(alpha = blink), 2.3.dp.toPx(), Offset(ox, oy))
                        }
                        drawRoundRect(
                            color = Color(0xFFEF4444),
                            topLeft = Offset(treeX - 14.dp.toPx(), treeBottomY + 1.dp.toPx()),
                            size = Size(8.dp.toPx(), 6.dp.toPx()),
                            cornerRadius = CornerRadius(1.5.dp.toPx(), 1.5.dp.toPx())
                        )
                        drawRoundRect(
                            color = Color(0xFF38BDF8),
                            topLeft = Offset(treeX + 6.dp.toPx(), treeBottomY + 1.dp.toPx()),
                            size = Size(8.dp.toPx(), 6.dp.toPx()),
                            cornerRadius = CornerRadius(1.5.dp.toPx(), 1.5.dp.toPx())
                        )

                        // 6. Компактная Комната отдыха справа (x: 0.66..0.97, y: 0.39..0.61)
                        val loungeRect = Offset(w * 0.66f, centerRoomsTop)
                        val loungeSize = Size(w * 0.31f, centerRoomsBottom - centerRoomsTop)
                        drawRoundRect(
                            color = Color(0xFF1E1B4B).copy(alpha = 0.48f),
                            topLeft = loungeRect,
                            size = loungeSize,
                            cornerRadius = CornerRadius(11.dp.toPx(), 11.dp.toPx())
                        )
                        drawRoundRect(
                            color = Color(0xFF818CF8).copy(alpha = 0.45f),
                            topLeft = loungeRect,
                            size = loungeSize,
                            cornerRadius = CornerRadius(11.dp.toPx(), 11.dp.toPx()),
                            style = Stroke(width = 1.2.dp.toPx())
                        )
                        val tvLeft = loungeRect.x + loungeSize.width * 0.16f
                        val tvWidth = loungeSize.width * 0.68f
                        val tvTop = loungeRect.y + 6.dp.toPx()
                        drawRoundRect(
                            color = Color(0xFF090D16),
                            topLeft = Offset(tvLeft, tvTop),
                            size = Size(tvWidth, 6.dp.toPx()),
                            cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
                        )
                        drawLine(
                            color = Color(0xFF38BDF8),
                            start = Offset(tvLeft + 3.dp.toPx(), tvTop + 6.dp.toPx()),
                            end = Offset(tvLeft + tvWidth - 3.dp.toPx(), tvTop + 6.dp.toPx()),
                            strokeWidth = 2.2.dp.toPx()
                        )
                        drawRoundRect(
                            color = Color(0xFF4338CA),
                            topLeft = Offset(loungeRect.x + 10.dp.toPx(), loungeRect.y + loungeSize.height * 0.54f),
                            size = Size(loungeSize.width - 20.dp.toPx(), 16.dp.toPx()),
                            cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                        )

                        // 7. 4 просторных кабинета нижнего ряда (y: 0.68..1.00)
                        val botColWidth = w / 4f
                        for (col in 0..3) {
                            val left = col * botColWidth
                            val right = left + botColWidth
                            val doorCenter = left + botColWidth / 2f
                            val botAgent = agents[col + 4]
                            val isBotSel = botAgent.id == selectedAgentId

                            drawRect(
                                color = botAgent.accentColor.copy(alpha = if (isBotSel) 0.15f else 0.05f),
                                topLeft = Offset(left + 2.dp.toPx(), botRoomsTop + 2.dp.toPx()),
                                size = Size(botColWidth - 3.dp.toPx(), h - botRoomsTop - 4.dp.toPx())
                            )
                            drawLine(wallColor, Offset(right, botRoomsTop), Offset(right, h), wallThickness)
                            drawLine(wallColor, Offset(left, botRoomsTop), Offset(doorCenter - doorHalf, botRoomsTop), wallThickness)
                            drawLine(wallColor, Offset(doorCenter + doorHalf, botRoomsTop), Offset(right, botRoomsTop), wallThickness)
                            drawLine(botAgent.accentColor.copy(alpha = 0.55f), Offset(doorCenter - doorHalf, botRoomsTop), Offset(doorCenter + doorHalf, botRoomsTop), 1.4.dp.toPx())
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

                        // 8. ЖИВОЙ ПАДАЮЩИЙ СНЕГ И СНЕЖИНКИ по всему офису
                        val snowFlakeCount = 34
                        for (s in 0 until snowFlakeCount) {
                            val baseX = ((s * 37) % 100) / 100f
                            val speedMult = 1f + (s % 3) * 0.35f
                            val yNorm = ((s * 0.13f) + snowFallPhase * speedMult) % 1f
                            val sway = sin((snowFallPhase * 6.28f) + s) * 0.015f
                            val sx = w * (baseX + sway).coerceIn(0.01f, 0.99f)
                            val sy = h * yNorm
                            val radius = if (s % 4 == 0) 2.4.dp.toPx() else 1.5.dp.toPx()

                            drawCircle(
                                color = Color.White.copy(alpha = if (s % 4 == 0) 0.78f else 0.52f),
                                radius = radius,
                                center = Offset(sx, sy)
                            )
                            if (s % 4 == 0) {
                                val arm = 3.2.dp.toPx()
                                drawLine(
                                    color = Color.White.copy(alpha = 0.65f),
                                    start = Offset(sx - arm, sy),
                                    end = Offset(sx + arm, sy),
                                    strokeWidth = 0.8.dp.toPx()
                                )
                                drawLine(
                                    color = Color.White.copy(alpha = 0.65f),
                                    start = Offset(sx, sy - arm),
                                    end = Offset(sx, sy + arm),
                                    strokeWidth = 0.8.dp.toPx()
                                )
                            }
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
                            modifier = Modifier.weight(0.16f),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("Аврора", fontSize = 8.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFFFBBF24), maxLines = 1)
                            Text("Директор", fontSize = 6.5.sp, color = Color.White.copy(alpha = 0.65f), maxLines = 1)
                        }
                        Column(
                            modifier = Modifier.weight(0.22f),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("Виктор + Елена", fontSize = 8.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF38BDF8), maxLines = 1)
                            Text("Проверка ТТХ (×2)", fontSize = 6.5.sp, color = Color.White.copy(alpha = 0.65f), maxLines = 1)
                        }
                        agents.take(4).forEach { ag ->
                            Column(
                                modifier = Modifier.weight(0.155f),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = ag.name,
                                    fontSize = 7.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (ag.id == selectedAgentId) ag.accentColor else Color.White.copy(alpha = 0.85f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = ag.role,
                                    fontSize = 6.sp,
                                    color = Color.White.copy(alpha = 0.45f),
                                    maxLines = 1
                                )
                            }
                        }
                    }

                    // Подпись узкой кухни у левой стенки
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .fillMaxWidth(0.155f)
                            .padding(start = 14.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Кухня", fontSize = 7.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFBBF24), maxLines = 1)
                            Text("Обед", fontSize = 6.sp, color = Color.White.copy(alpha = 0.6f), maxLines = 1)
                        }
                    }

                    // Подпись у современной ёлки
                    Box(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .offset(x = (-14).dp, y = 40.dp)
                    ) {
                        Text(
                            text = "2027",
                            fontSize = 7.5.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF34D399),
                            maxLines = 1
                        )
                    }

                    // Подпись лаунж-зоны справа
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .padding(end = 22.dp, bottom = 36.dp)
                    ) {
                        Text("ТВ и диван", fontSize = 7.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFFA5B4FC))
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 4.dp)
                    ) {
                        agents.drop(4).forEach { ag ->
                            Column(
                                modifier = Modifier.weight(1f),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = ag.name,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (ag.id == selectedAgentId) ag.accentColor else Color.White.copy(alpha = 0.85f),
                                    maxLines = 1
                                )
                                Text(
                                    text = ag.role,
                                    fontSize = 6.5.sp,
                                    color = Color.White.copy(alpha = 0.45f),
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }

                // 4. Инспектор выбранного сотрудника
                if (inspectedStatus != null) {
                    val pct = (inspectedStatus.progress * 100).toInt()
                    val isWalking = agentWalkingFlags[inspectedAgent.id] ?: false
                    val statusBadgeText = when {
                        isWalking -> "Идёт по дорожке..."
                        inspectedStatus.targetZone == AgentLocationZone.CHIEF_CHECK -> "Сдаёт статью Авроре"
                        inspectedStatus.targetZone == AgentLocationZone.DINING -> "Обедает (${inspectedStatus.breakSecondsRemaining.toInt()} с)"
                        inspectedStatus.targetZone == AgentLocationZone.LOUNGE -> "У ёлки (${inspectedStatus.breakSecondsRemaining.toInt()} с)"
                        else -> "В работе • $pct%"
                    }

                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = inspectedAgent.accentColor.copy(alpha = 0.11f),
                        border = BorderStroke(1.dp, inspectedAgent.accentColor.copy(alpha = 0.4f))
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
                                Row(
                                    modifier = Modifier.weight(1f).padding(end = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .clip(CircleShape)
                                            .background(inspectedAgent.accentColor)
                                    )
                                    Spacer(Modifier.width(7.dp))
                                    Text(
                                        text = "${inspectedAgent.name} (${inspectedAgent.roomTitle})",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = inspectedAgent.accentColor.copy(alpha = 0.2f)
                                ) {
                                    Text(
                                        text = statusBadgeText,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = inspectedAgent.accentColor,
                                        maxLines = 1,
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
                                    shape = RoundedCornerShape(9.dp),
                                    color = Color(0xFF10B981).copy(alpha = 0.14f)
                                ) {
                                    Text(
                                        text = "Баланс: ${inspectedStatus.salaryBalance} ₽",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF10B981),
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(vertical = 5.dp, horizontal = 4.dp)
                                    )
                                }
                                Surface(
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(9.dp),
                                    color = Color(0xFFF59E0B).copy(alpha = 0.14f)
                                ) {
                                    Text(
                                        text = "Отклонено: ${inspectedStatus.rejectionsCount}",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFF59E0B),
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(vertical = 5.dp, horizontal = 4.dp)
                                    )
                                }
                                Surface(
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(9.dp),
                                    color = Color(0xFFF43F5E).copy(alpha = 0.14f)
                                ) {
                                    Text(
                                        text = "Выговоров: ${inspectedStatus.reprimandsCount}",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFF43F5E),
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(vertical = 5.dp, horizontal = 4.dp)
                                    )
                                }
                            }

                            Text(
                                text = "Задача #${inspectedStatus.currentTask.ticketNumber}: ${inspectedStatus.currentTask.title}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            LinearProgressIndicator(
                                progress = { inspectedStatus.progress.coerceIn(0f, 1f) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(5.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = inspectedAgent.accentColor,
                                trackColor = inspectedAgent.accentColor.copy(alpha = 0.18f)
                            )

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { isAgentHistoryExpanded = !isAgentHistoryExpanded }
                                    .padding(top = 2.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.History,
                                        contentDescription = null,
                                        tint = inspectedAgent.accentColor,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(Modifier.width(5.dp))
                                    Text(
                                        text = "Архив задач сотрудника (${inspectedStatus.completedHistory.size})",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = inspectedAgent.accentColor
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
                                    verticalArrangement = Arrangement.spacedBy(5.dp)
                                ) {
                                    HorizontalDivider(color = inspectedAgent.accentColor.copy(alpha = 0.25f))
                                    inspectedStatus.completedHistory.forEach { done ->
                                        Column(modifier = Modifier.fillMaxWidth()) {
                                            Text(
                                                text = "[${done.completedAt}] #${done.ticketNumber} • ${done.title}",
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f)
                                            )
                                            Text(
                                                text = if (done.chiefApproved)
                                                    "Принято Авророй (+${done.salaryDelta} ₽)"
                                                else done.rejectReason,
                                                fontSize = 10.sp,
                                                color = if (done.chiefApproved) Color(0xFF10B981) else Color(0xFFF43F5E)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onRunScan,
                        enabled = !isScanning,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(5.dp))
                        Text(
                            if (isScanning) "Агенты ускорены..." else "Ускорить смену",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Свернуть", fontSize = 12.sp)
                    }
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
    val dir = if (isTopRow) 1f else -1f
    val deskCenter = Offset(chairAnchor.x, chairAnchor.y + dir * 16.dp.toPx())
    val deskW = 34.dp.toPx()
    val deskH = 14.dp.toPx()

    drawRoundRect(
        color = Color(0xFF1E293B),
        topLeft = Offset(deskCenter.x - deskW / 2f, deskCenter.y - deskH / 2f),
        size = Size(deskW, deskH),
        cornerRadius = CornerRadius(3.5.dp.toPx(), 3.5.dp.toPx())
    )
    drawRoundRect(
        color = accentColor.copy(alpha = if (isActiveWork) 0.65f else 0.25f),
        topLeft = Offset(deskCenter.x - deskW / 2f, deskCenter.y - deskH / 2f),
        size = Size(deskW, deskH),
        cornerRadius = CornerRadius(3.5.dp.toPx(), 3.5.dp.toPx()),
        style = Stroke(width = 1.dp.toPx())
    )

    val monCenter = Offset(deskCenter.x, deskCenter.y + dir * 2.dp.toPx())
    val monW = 17.dp.toPx()
    val monH = 7.5.dp.toPx()
    drawRoundRect(
        color = Color(0xFF090D16),
        topLeft = Offset(monCenter.x - monW / 2f, monCenter.y - monH / 2f),
        size = Size(monW, monH),
        cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
    )
    val screenColor = if (isActiveWork) accentColor else Color(0xFF334155)
    drawRoundRect(
        color = screenColor.copy(alpha = if (isActiveWork) 0.9f else 0.45f),
        topLeft = Offset(monCenter.x - monW / 2f + 1.2.dp.toPx(), monCenter.y - monH / 2f + 1.2.dp.toPx()),
        size = Size(monW - 2.4.dp.toPx(), monH - 2.4.dp.toPx()),
        cornerRadius = CornerRadius(1.2.dp.toPx(), 1.2.dp.toPx())
    )

    if (isActiveWork) {
        val barWidth = (monW - 4.dp.toPx()) * progress.coerceIn(0.1f, 1f)
        val lineY = monCenter.y - 1.dp.toPx() + ((scanPhase * 2f) % 2.dp.toPx())
        drawLine(
            color = Color.White.copy(alpha = 0.9f),
            start = Offset(monCenter.x - monW / 2f + 2.dp.toPx(), lineY),
            end = Offset(monCenter.x - monW / 2f + 2.dp.toPx() + barWidth, lineY),
            strokeWidth = 1.2.dp.toPx(),
            cap = StrokeCap.Round
        )
    }

    val kbCenter = Offset(deskCenter.x - 1.5.dp.toPx(), deskCenter.y - dir * 3.dp.toPx())
    drawRoundRect(
        color = Color(0xFF475569),
        topLeft = Offset(kbCenter.x - 6.dp.toPx(), kbCenter.y - 1.6.dp.toPx()),
        size = Size(12.dp.toPx(), 3.2.dp.toPx()),
        cornerRadius = CornerRadius(1.dp.toPx(), 1.dp.toPx())
    )

    drawCircle(
        color = Color(0xFF92400E),
        radius = 2.dp.toPx(),
        center = Offset(deskCenter.x + 12.dp.toPx(), deskCenter.y - dir * 2.dp.toPx())
    )
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
    val skinColor = Color(0xFFFDE68A)
    val pantsColor = Color(0xFF1E293B)

    if (isSelected) {
        drawCircle(
            color = shirtColor.copy(alpha = 0.25f),
            radius = 15.dp.toPx(),
            center = center
        )
        drawCircle(
            color = Color.White,
            radius = 15.dp.toPx(),
            center = center,
            style = Stroke(width = 1.3.dp.toPx())
        )
    }

    if (isWalking) {
        val leftLegOffset = walkPhase * 3.2.dp.toPx()
        val rightLegOffset = -walkPhase * 3.2.dp.toPx()
        drawLine(
            color = pantsColor,
            start = Offset(center.x - 2.2.dp.toPx(), center.y + 3.dp.toPx()),
            end = Offset(center.x - 2.5.dp.toPx() + leftLegOffset, center.y + 9.dp.toPx()),
            strokeWidth = 2.4.dp.toPx(),
            cap = StrokeCap.Round
        )
        drawLine(
            color = pantsColor,
            start = Offset(center.x + 2.2.dp.toPx(), center.y + 3.dp.toPx()),
            end = Offset(center.x + 2.5.dp.toPx() + rightLegOffset, center.y + 9.dp.toPx()),
            strokeWidth = 2.4.dp.toPx(),
            cap = StrokeCap.Round
        )
    } else if (zone == AgentLocationZone.DESK) {
        val backDir = if (isTopRow) -1f else 1f
        drawRoundRect(
            color = Color(0xFF334155),
            topLeft = Offset(center.x - 6.dp.toPx(), center.y + backDir * 3.8.dp.toPx() - 2.dp.toPx()),
            size = Size(12.dp.toPx(), 4.dp.toPx()),
            cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
        )
    }

    drawRoundRect(
        color = shirtColor,
        topLeft = Offset(center.x - 5.5.dp.toPx(), center.y - 3.dp.toPx()),
        size = Size(11.dp.toPx(), 7.dp.toPx()),
        cornerRadius = CornerRadius(3.5.dp.toPx(), 3.5.dp.toPx())
    )

    if (!isWalking && zone == AgentLocationZone.DESK) {
        val reachDir = if (isTopRow) 1f else -1f
        val leftReach = reachDir * (5.dp.toPx() + typePhase * 1.4.dp.toPx())
        val rightReach = reachDir * (5.dp.toPx() - typePhase * 1.4.dp.toPx())
        drawLine(
            color = skinColor,
            start = Offset(center.x - 4.5.dp.toPx(), center.y + reachDir * 1.dp.toPx()),
            end = Offset(center.x - 3.dp.toPx(), center.y + leftReach),
            strokeWidth = 1.8.dp.toPx(),
            cap = StrokeCap.Round
        )
        drawLine(
            color = skinColor,
            start = Offset(center.x + 4.5.dp.toPx(), center.y + reachDir * 1.dp.toPx()),
            end = Offset(center.x + 3.dp.toPx(), center.y + rightReach),
            strokeWidth = 1.8.dp.toPx(),
            cap = StrokeCap.Round
        )
    } else {
        val armSwing = if (isWalking) -walkPhase * 2.5.dp.toPx() else 0f
        drawLine(
            color = skinColor,
            start = Offset(center.x - 5.5.dp.toPx(), center.y - 1.dp.toPx()),
            end = Offset(center.x - 6.8.dp.toPx(), center.y + 3.5.dp.toPx() + armSwing),
            strokeWidth = 1.8.dp.toPx(),
            cap = StrokeCap.Round
        )
        drawLine(
            color = skinColor,
            start = Offset(center.x + 5.5.dp.toPx(), center.y - 1.dp.toPx()),
            end = Offset(center.x + 6.8.dp.toPx(), center.y + 3.5.dp.toPx() - armSwing),
            strokeWidth = 1.8.dp.toPx(),
            cap = StrokeCap.Round
        )
    }

    val headCenter = Offset(center.x, center.y - 6.2.dp.toPx())
    drawCircle(color = skinColor, radius = 4.2.dp.toPx(), center = headCenter)
    drawArc(
        color = hairColor,
        startAngle = 180f,
        sweepAngle = 180f,
        useCenter = true,
        topLeft = Offset(headCenter.x - 4.3.dp.toPx(), headCenter.y - 4.4.dp.toPx()),
        size = Size(8.6.dp.toPx(), 6.dp.toPx())
    )

    val showEyes = isWalking || zone != AgentLocationZone.DESK || !isTopRow
    if (showEyes) {
        drawCircle(Color(0xFF0F172A), 0.75.dp.toPx(), Offset(headCenter.x - 1.4.dp.toPx(), headCenter.y + 0.3.dp.toPx()))
        drawCircle(Color(0xFF0F172A), 0.75.dp.toPx(), Offset(headCenter.x + 1.4.dp.toPx(), headCenter.y + 0.3.dp.toPx()))
    }
}

@Composable
fun SettingsSectionCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.16f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(10.dp))
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
    val isCompact = settings.cardStyle == "compact"
    val scale = settings.fontScale

    val badgeColor = when (item.platform) {
        "Telegram" -> Color(0xFF0284C7)
        "Instagram" -> Color(0xFFD946EF)
        "Оф. сайт" -> primary
        else -> Color(0xFF059669)
    }

    val containerColor = when (settings.cardStyle) {
        "filled" -> primary.copy(alpha = 0.08f)
        else -> MaterialTheme.colorScheme.surface
    }

    val borderStroke = when (settings.cardStyle) {
        "filled" -> null
        else -> BorderStroke(1.dp, primary.copy(alpha = 0.18f))
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpen() },
        shape = RoundedCornerShape(settings.cornerRadius.dp),
        border = borderStroke,
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Column(
            modifier = Modifier.padding(if (isCompact) 11.dp else 13.dp),
            verticalArrangement = Arrangement.spacedBy(if (isCompact) 5.dp else 7.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f).padding(end = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = badgeColor.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = item.brand,
                            color = badgeColor,
                            fontSize = (10 * scale).sp,
                            fontWeight = FontWeight.ExtraBold,
                            maxLines = 1,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "${item.sourceLabel} • ${item.platform}",
                        fontSize = (10 * scale).sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (item.isVeryFresh6h) {
                        Surface(
                            shape = RoundedCornerShape(5.dp),
                            color = primary.copy(alpha = 0.18f)
                        ) {
                            Text(
                                text = "СВЕЖЕЕ",
                                fontSize = (9 * scale).sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = primary,
                                maxLines = 1,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                            )
                        }
                        Spacer(Modifier.width(5.dp))
                    }
                    Text(
                        text = item.timeLabel,
                        fontSize = (10 * scale).sp,
                        maxLines = 1,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)
                    )
                }
            }

            Text(
                text = item.title,
                fontSize = (if (isCompact) 14f else 15.5f).times(scale).sp,
                fontWeight = FontWeight.Bold,
                lineHeight = (if (isCompact) 19f else 21f).times(scale).sp,
                color = MaterialTheme.colorScheme.onSurface
            )

            if (settings.showSummary && !isCompact) {
                Text(
                    text = item.summary,
                    fontSize = (13 * scale).sp,
                    lineHeight = (18 * scale).sp,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.78f)
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
                    color = primary.copy(alpha = 0.12f),
                    border = BorderStroke(0.8.dp, primary.copy(alpha = 0.35f))
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
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onShare,
                        modifier = Modifier.size(30.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Поделиться",
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    IconButton(
                        onClick = onToggleSave,
                        modifier = Modifier.size(30.dp)
                    ) {
                        Icon(
                            imageVector = if (isSaved) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                            contentDescription = "Сохранить",
                            tint = if (isSaved) primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

private fun openCustomTab(context: Context, url: String) {
    try {
        val customTabs = CustomTabsIntent.Builder()
            .setShowTitle(true)
            .build()
        customTabs.launchUrl(context, Uri.parse(url))
    } catch (_: Exception) {
        try {
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        } catch (_: Exception) {
            Toast.makeText(context, "Не удалось открыть ссылку", Toast.LENGTH_SHORT).show()
        }
    }
}

private fun shareArticle(context: Context, item: NewsItem) {
    val shareText = "${item.title}\n\n${item.summary}\n\nИсточник (${item.sourceLabel}): ${item.url}"
    val sendIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, item.title)
        putExtra(Intent.EXTRA_TEXT, shareText)
    }
    context.startActivity(Intent.createChooser(sendIntent, "Поделиться новостью"))
}

private fun shareReviewArticle(context: Context, article: TechReviewArticle) {
    val specsText = article.specs.joinToString("\n") { "• ${it.label}: ${it.value}" }
    val shareText = "${article.headline}\nОценка: ${article.score}\n\n${article.leadSummary}\n\nОсновные ТТХ:\n$specsText\n\nПодготовлено в NewsPulse AI"
    val sendIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, article.headline)
        putExtra(Intent.EXTRA_TEXT, shareText)
    }
    context.startActivity(Intent.createChooser(sendIntent, "Поделиться обзором"))
}
