package com.example.planer.ui.components

import android.content.Context
import android.graphics.Outline
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.text.TextUtils
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.res.ResourcesCompat
import com.example.planer.R
import com.example.planer.data.Ads
import com.example.planer.ui.theme.LocalP
import com.example.planer.ui.theme.PColors
import com.yandex.mobile.ads.common.AdBindingResult
import com.yandex.mobile.ads.common.ImpressionData
import com.yandex.mobile.ads.nativeads.MediaView
import com.yandex.mobile.ads.nativeads.NativeAd
import com.yandex.mobile.ads.nativeads.NativeAdEventListener
import com.yandex.mobile.ads.nativeads.NativeAdView
import com.yandex.mobile.ads.nativeads.NativeAdViewBinder

/**
 * Нативная реклама в стиле приложения: белая (в тёмной теме — тёмная) карточка
 * со скруглением 28dp, шрифт Manrope, сиреневая кнопка-таблетка.
 * Пока объявление не загрузилось, место не занимается — ничего не «прыгает» пустым.
 *
 * [key] — место показа ("today", "schedule", "health"): у каждого своё объявление.
 * [compact] — карточка вдвое меньше (для главного экрана), в том же стиле.
 */
@Composable
fun NativeAdCard(key: String, modifier: Modifier = Modifier, compact: Boolean = false) {
    val ctx = LocalContext.current
    val c = LocalP.current
    val slot = remember(key) { Ads.slot(key) }
    LaunchedEffect(key, Ads.ready) { Ads.loadNative(ctx, slot) }

    val ad = slot.ad ?: return
    var failed by remember(ad) { mutableStateOf(false) }
    if (failed) return

    AndroidView(
        factory = { context -> AdCard(context, compact).root },
        modifier = modifier.fillMaxWidth(),
        update = { view ->
            val card = view.tag as AdCard
            card.paint(c)
            if (card.bound !== ad && !card.bind(ad)) failed = true
        }
    )
}

/** Разметка объявления. Все обязательные элементы (заголовок, домен, «Реклама»,
 *  возраст, предупреждение, меню жалобы, медиа) лежат внутри NativeAdView. */
private class AdCard(ctx: Context, private val compact: Boolean) {
    private val dm = ctx.resources.displayMetrics
    private fun dp(v: Int) = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, v.toFloat(), dm).toInt()
    private fun dpf(v: Int) = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, v.toFloat(), dm)

    private val bold: Typeface? = font(ctx, R.font.manrope_bold)
    private val extra: Typeface? = font(ctx, R.font.manrope_extrabold)
    private val semi: Typeface? = font(ctx, R.font.manrope_semibold)
    private val medium: Typeface? = font(ctx, R.font.manrope_medium)

    val root = NativeAdView(ctx)

    private val icon = ImageView(ctx).apply {
        scaleType = ImageView.ScaleType.CENTER_CROP
        rounded(dpf(14))
    }
    private val title = text(ctx, 15f, extra).apply { maxLines = 2; ellipsize = TextUtils.TruncateAt.END }
    private val favicon = ImageView(ctx).apply { scaleType = ImageView.ScaleType.FIT_CENTER }
    private val domain = text(ctx, 12f, semi).apply { maxLines = 1; ellipsize = TextUtils.TruncateAt.END }
    private val feedback = ImageView(ctx).apply {
        setImageResource(R.drawable.ic_ad_feedback)
        scaleType = ImageView.ScaleType.CENTER
        contentDescription = "Пожаловаться на рекламу"
    }
    private val sponsored = chip(ctx)
    private val age = chip(ctx)
    private val body = text(ctx, 13f, medium).apply { maxLines = 4; ellipsize = TextUtils.TruncateAt.END; setLineSpacing(0f, 1.15f) }
    private val media = MediaView(ctx).apply { rounded(dpf(20)) }
    private val price = text(ctx, 15f, extra)
    private val cta = text(ctx, 14f, bold).apply {
        gravity = Gravity.CENTER
        maxLines = 1
        setPadding(dp(20), dp(10), dp(20), dp(10))
        minHeight = dp(44)
    }
    private val warning = text(ctx, 11f, medium)

    var bound: NativeAd? = null
        private set

    init {
        if (compact) buildCompact(ctx) else buildFull(ctx)
        root.tag = this
    }

    private fun lp(w: Int, h: Int) = LinearLayout.LayoutParams(w, h)
    private val wrap get() = ViewGroup.LayoutParams.WRAP_CONTENT
    private val match get() = ViewGroup.LayoutParams.MATCH_PARENT

    /** Компактная карточка для главного экрана: тот же стиль, что у полной,
     *  но вдвое ниже — без описания, медиа пониже, метки в одной строке с доменом. */
    private fun buildCompact(ctx: Context) {
        title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
        title.maxLines = 1
        price.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f)
        warning.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10f)
        cta.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f)
        cta.setPadding(dp(16), dp(7), dp(16), dp(7))
        cta.minHeight = dp(34)
        sponsored.setPadding(dp(8), dp(3), dp(8), dp(3))
        age.setPadding(dp(8), dp(3), dp(8), dp(3))
        icon.rounded(dpf(12))
        media.rounded(dpf(18))

        val col = LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(14), dp(12), dp(14), dp(12))
        }

        // Шапка: иконка · заголовок + метки/домен · меню жалобы
        val head = LinearLayout(ctx).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        head.addView(icon, lp(dp(36), dp(36)).apply { marginEnd = dp(10) })
        val names = LinearLayout(ctx).apply { orientation = LinearLayout.VERTICAL }
        names.addView(title, lp(match, wrap))
        val sub = LinearLayout(ctx).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        sub.addView(sponsored, lp(wrap, wrap).apply { marginEnd = dp(4) })
        sub.addView(age, lp(wrap, wrap).apply { marginEnd = dp(6) })
        sub.addView(favicon, lp(dp(12), dp(12)).apply { marginEnd = dp(4) })
        sub.addView(domain, lp(0, wrap).apply { weight = 1f })
        names.addView(sub, lp(match, wrap).apply { topMargin = dp(3) })
        head.addView(names, lp(0, wrap).apply { weight = 1f })
        head.addView(feedback, lp(dp(32), dp(32)).apply { marginStart = dp(2) })
        col.addView(head, lp(match, wrap))

        col.addView(media, lp(match, dp(96)).apply { topMargin = dp(10) })

        val foot = LinearLayout(ctx).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        foot.addView(price, lp(0, wrap).apply { weight = 1f })
        foot.addView(cta, lp(wrap, wrap))
        col.addView(foot, lp(match, wrap).apply { topMargin = dp(8) })
        col.addView(warning, lp(match, wrap).apply { topMargin = dp(6) })

        root.addView(col, ViewGroup.LayoutParams(match, wrap))
        root.rounded(dpf(28))
    }

    /** Полная карточка — для «Расписания» и «Здоровья». */
    private fun buildFull(ctx: Context) {
        val col = LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(16), dp(16), dp(16))
        }

        // Шапка: иконка · заголовок/домен · меню жалобы
        val head = LinearLayout(ctx).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        head.addView(icon, LinearLayout.LayoutParams(dp(44), dp(44)).apply { marginEnd = dp(12) })
        val names = LinearLayout(ctx).apply { orientation = LinearLayout.VERTICAL }
        names.addView(title, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))
        val domainRow = LinearLayout(ctx).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        domainRow.addView(favicon, LinearLayout.LayoutParams(dp(14), dp(14)).apply { marginEnd = dp(6) })
        domainRow.addView(domain, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
        names.addView(domainRow, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { topMargin = dp(2) })
        head.addView(names, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
        head.addView(feedback, LinearLayout.LayoutParams(dp(36), dp(36)).apply { marginStart = dp(4) })
        col.addView(head)

        // Метки «Реклама» и возраст
        val tags = LinearLayout(ctx).apply { orientation = LinearLayout.HORIZONTAL }
        tags.addView(sponsored, LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { marginEnd = dp(6) })
        tags.addView(age)
        col.addView(tags, LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { topMargin = dp(10) })

        col.addView(body, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { topMargin = dp(10) })
        col.addView(media, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { topMargin = dp(12) })

        // Цена и кнопка
        val foot = LinearLayout(ctx).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        foot.addView(price, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
        foot.addView(cta, LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT))
        col.addView(foot, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { topMargin = dp(12) })

        col.addView(warning, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { topMargin = dp(10) })

        root.addView(col, ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))
        root.rounded(dpf(28))
    }

    /** Цвета темы приложения (светлая / тёмная). */
    fun paint(c: PColors) {
        root.background = GradientDrawable().apply {
            cornerRadius = dpf(28)
            setColor(c.card.toArgb())
        }
        val chipBg = c.chip
        icon.setBackgroundColor(chipBg.toArgb())
        title.setTextColor(c.ink.toArgb())
        domain.setTextColor(c.muted.toArgb())
        body.setTextColor(c.muted.toArgb())
        price.setTextColor(c.ink.toArgb())
        warning.setTextColor(c.faint.toArgb())
        feedback.setColorFilter(c.faint.toArgb())
        for (t in listOf(sponsored, age)) {
            t.setTextColor(c.accentDeep.toArgb())
            t.background = pill(chipBg, dpf(12))
        }
        cta.setTextColor(c.onAccent.toArgb())
        cta.background = pill(c.accentDeep, dpf(22))
    }

    /** Привязка объявления. false — не удалось, карточку прячем. */
    fun bind(ad: NativeAd): Boolean {
        val binder = NativeAdViewBinder.Builder(root)
            .setAgeView(age)
            .apply { if (!compact) setBodyView(body) }
            .setCallToActionView(cta)
            .setDomainView(domain)
            .setFaviconView(favicon)
            .setFeedbackView(feedback)
            .setIconView(icon)
            .setMediaView(media)
            .setPriceView(price)
            .setSponsoredView(sponsored)
            .setTitleView(title)
            .setWarningView(warning)
            .build()
        val result = ad.bindNativeAd(binder)
        if (result is AdBindingResult.Failure) return false
        bound = ad
        ad.setNativeAdEventListener(object : NativeAdEventListener {
            override fun onAdClicked() {}
            override fun onImpression(data: ImpressionData?) {}
        })
        // Пустые элементы прячем, чтобы не было пустых «таблеток» и отступов.
        val a = ad.adAssets
        icon.visible(a.icon != null)
        favicon.visible(a.favicon != null)
        domain.visible(a.domain != null)
        age.visible(a.age != null)
        sponsored.visible(a.sponsored != null)
        body.visible(a.body != null)
        media.visible(a.media != null || a.image != null)
        price.visible(a.price != null)
        cta.visible(a.callToAction != null)
        warning.visible(a.warning != null)
        return true
    }

    private fun text(ctx: Context, sp: Float, face: Typeface?) = TextView(ctx).apply {
        setTextSize(TypedValue.COMPLEX_UNIT_SP, sp)
        if (face != null) typeface = face
        includeFontPadding = false
    }

    private fun chip(ctx: Context) = text(ctx, 11f, bold).apply {
        setPadding(dp(10), dp(5), dp(10), dp(5))
        maxLines = 1
    }

    private fun pill(color: Color, radius: Float) = GradientDrawable().apply {
        cornerRadius = radius
        setColor(color.toArgb())
    }

    private fun font(ctx: Context, id: Int): Typeface? = try {
        ResourcesCompat.getFont(ctx, id)
    } catch (e: Exception) {
        null
    }
}

private fun View.visible(on: Boolean) {
    visibility = if (on) View.VISIBLE else View.GONE
}

/** Скругление углов без обрезки содержимого более чем на края. */
private fun View.rounded(radius: Float) {
    outlineProvider = object : ViewOutlineProvider() {
        override fun getOutline(view: View, outline: Outline) {
            outline.setRoundRect(0, 0, view.width, view.height, radius)
        }
    }
    clipToOutline = true
}
