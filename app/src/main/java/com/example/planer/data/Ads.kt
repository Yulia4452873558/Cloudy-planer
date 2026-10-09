package com.example.planer.data

import android.app.Activity
import android.content.Context
import android.content.pm.ApplicationInfo
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.yandex.mobile.ads.common.AdError
import com.yandex.mobile.ads.common.AdRequest
import com.yandex.mobile.ads.common.AdRequestError
import com.yandex.mobile.ads.common.ImpressionData
import com.yandex.mobile.ads.common.YandexAds
import com.yandex.mobile.ads.interstitial.InterstitialAd
import com.yandex.mobile.ads.interstitial.InterstitialAdEventListener
import com.yandex.mobile.ads.interstitial.InterstitialAdLoadListener
import com.yandex.mobile.ads.interstitial.InterstitialAdLoader
import com.yandex.mobile.ads.nativeads.NativeAd
import com.yandex.mobile.ads.nativeads.NativeAdLoadListener
import com.yandex.mobile.ads.nativeads.NativeAdLoader

/**
 * Реклама Яндекса (Yandex Mobile Ads SDK 8).
 *
 * - Межстраничная: после создания задачи, когда их количество кратно трём.
 * - Нативная: карточка в стиле приложения на «Сегодня», «Расписании» и «Здоровье».
 *
 * В отладочной сборке (Run из Android Studio) используются демо-блоки Яндекса —
 * так реклама видна сразу, а показы не засчитываются как накрутка.
 * В релизной сборке — настоящие блоки.
 */
object Ads {
    private const val INTERSTITIAL_ID = "R-M-20203104-3"
    private const val NATIVE_ID = "R-M-20203104-2"
    private const val DEMO_INTERSTITIAL_ID = "demo-interstitial-yandex"
    private const val DEMO_NATIVE_ID = "demo-native-content-yandex"

    /** Не повторяем неудачную загрузку бесконечно. */
    private const val MAX_FAILURES = 3
    /** Через сколько нативную рекламу можно обновить при возврате на экран. */
    private const val NATIVE_REFRESH_MS = 90_000L

    /** SDK готов к загрузке рекламы. */
    var ready by mutableStateOf(false)
        private set
    private var initStarted = false

    private fun debug(ctx: Context) = (ctx.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
    private fun interstitialId(ctx: Context) = if (debug(ctx)) DEMO_INTERSTITIAL_ID else INTERSTITIAL_ID
    private fun nativeId(ctx: Context) = if (debug(ctx)) DEMO_NATIVE_ID else NATIVE_ID

    /** Вызывается один раз при запуске (MainActivity.onCreate). */
    fun init(ctx: Context) {
        if (initStarted) return
        initStarted = true
        val app = ctx.applicationContext
        YandexAds.initialize(app) {
            ready = true
            preloadInterstitial(app)
        }
    }

    // ---------- Межстраничная ----------

    private var interstitialLoader: InterstitialAdLoader? = null
    private var interstitial: InterstitialAd? = null
    private var interstitialLoading = false
    private var interstitialFailures = 0

    /** Загружаем заранее, чтобы показать мгновенно, без ожидания. */
    fun preloadInterstitial(ctx: Context) {
        if (!ready || interstitial != null || interstitialLoading || interstitialFailures >= MAX_FAILURES) return
        val app = ctx.applicationContext
        val loader = interstitialLoader ?: InterstitialAdLoader(app).also { interstitialLoader = it }
        interstitialLoading = true
        loader.loadAd(
            AdRequest.Builder(interstitialId(app)).build(),
            object : InterstitialAdLoadListener {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialLoading = false
                    interstitialFailures = 0
                    interstitial = ad
                }

                override fun onAdFailedToLoad(error: AdRequestError) {
                    interstitialLoading = false
                    interstitialFailures++
                }
            }
        )
    }

    /** Показать межстраничную рекламу, если она уже загружена. */
    fun showInterstitial(activity: Activity) {
        val ad = interstitial
        if (ad == null) {
            interstitialFailures = 0
            preloadInterstitial(activity)
            return
        }
        interstitial = null
        val app = activity.applicationContext
        ad.setAdEventListener(object : InterstitialAdEventListener {
            override fun onAdShown() {}

            override fun onAdFailedToShow(adError: AdError) {
                ad.setAdEventListener(null)
                preloadInterstitial(app)
            }

            override fun onAdDismissed() {
                ad.setAdEventListener(null)
                preloadInterstitial(app)
            }

            override fun onAdClicked() {}

            override fun onAdImpression(impressionData: ImpressionData?) {}
        })
        ad.show(activity)
    }

    // ---------- Нативная ----------

    /** Отдельное объявление для каждого места: одно объявление нельзя показывать в двух местах сразу. */
    class NativeSlot {
        var ad by mutableStateOf<NativeAd?>(null)
            internal set
        internal var loadedAt = 0L
        internal var loading = false
        internal var failures = 0
        internal var loader: NativeAdLoader? = null
    }

    private val slots = HashMap<String, NativeSlot>()

    fun slot(key: String): NativeSlot = slots.getOrPut(key) { NativeSlot() }

    /** Загрузить объявление для места [slot] (или обновить, если старое висит давно). */
    fun loadNative(ctx: Context, slot: NativeSlot) {
        if (!ready || slot.loading || slot.failures >= MAX_FAILURES) return
        if (slot.ad != null && System.currentTimeMillis() - slot.loadedAt < NATIVE_REFRESH_MS) return
        val loader = NativeAdLoader(ctx)
        slot.loader = loader // держим ссылку, пока идёт загрузка
        slot.loading = true
        loader.loadAd(
            AdRequest.Builder(nativeId(ctx)).build(),
            object : NativeAdLoadListener {
                override fun onAdLoaded(nativeAd: NativeAd) {
                    slot.loading = false
                    slot.loader = null
                    slot.failures = 0
                    slot.loadedAt = System.currentTimeMillis()
                    slot.ad = nativeAd
                }

                override fun onAdFailedToLoad(error: AdRequestError) {
                    slot.loading = false
                    slot.loader = null
                    slot.failures++
                }
            }
        )
    }
}
