package com.example.studyspace.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.abs
import kotlin.math.sqrt

// ====================================================
// SensorManagerHelper - Sensor Framework Integration
// Lifecycle-aware sensor manager untuk Accelerometer & Light
// Mencegah memory leak dengan unregister saat tidak dipakai
// ====================================================
class SensorManagerHelper(context: Context) : DefaultLifecycleObserver {

    // Android SensorManager untuk akses hardware sensor
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    // Referensi ke sensor hardware
    private val accelerometer: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val lightSensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)

    // ---- StateFlow untuk data accelerometer ----
    private val _accelerometerData = MutableStateFlow(AccelerometerData())
    val accelerometerData: StateFlow<AccelerometerData> = _accelerometerData.asStateFlow()

    // ---- StateFlow untuk data light sensor ----
    private val _lightData = MutableStateFlow(LightData())
    val lightData: StateFlow<LightData> = _lightData.asStateFlow()

    // ---- StateFlow untuk status ketersediaan sensor ----
    private val _sensorAvailability = MutableStateFlow(
        SensorAvailability(
            hasAccelerometer = accelerometer != null,
            hasLightSensor = lightSensor != null
        )
    )
    val sensorAvailability: StateFlow<SensorAvailability> = _sensorAvailability.asStateFlow()

    // ---- Tracking variabel untuk sesi belajar ----
    private var movementCount = 0
    private var lightReadings = mutableListOf<Float>()
    private var isMonitoring = false

    // ---- Low-pass filter untuk smoothing accelerometer ----
    // Mengurangi noise agar pergerakan kecil tidak dihitung
    private var lastX = 0f
    private var lastY = 0f
    private var lastZ = 0f
    private val movementThreshold = 1.5f // Minimum delta untuk dihitung sebagai gerakan
    private val alpha = 0.8f // Filter coefficient (0=no filter, 1=max filter)

    // ---- Sensor Event Listener ----
    private val sensorListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {
            when (event.sensor.type) {
                Sensor.TYPE_ACCELEROMETER -> handleAccelerometerEvent(event)
                Sensor.TYPE_LIGHT -> handleLightEvent(event)
            }
        }

        override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
            // Tidak diperlukan untuk implementasi ini
        }
    }

    /**
     * Menangani event accelerometer dengan low-pass filter
     * untuk mengurangi false positive dari noise sensor
     */
    private fun handleAccelerometerEvent(event: SensorEvent) {
        val x = event.values[0]
        val y = event.values[1]
        val z = event.values[2]

        // Hitung magnitude perubahan dari pembacaan sebelumnya
        val deltaX = abs(x - lastX)
        val deltaY = abs(y - lastY)
        val deltaZ = abs(z - lastZ)

        // Hitung total delta movement
        val totalDelta = sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ)

        // Hanya hitung sebagai gerakan jika melewati threshold
        if (totalDelta > movementThreshold) {
            movementCount++
        }

        // Update data dengan low-pass filter
        // Alpha tinggi = lebih halus tapi kurang responsif
        lastX = alpha * lastX + (1 - alpha) * x
        lastY = alpha * lastY + (1 - alpha) * y
        lastZ = alpha * lastZ + (1 - alpha) * z

        // Update state
        _accelerometerData.value = AccelerometerData(
            x = x,
            y = y,
            z = z,
            magnitude = sqrt(x * x + y * y + z * z),
            movementCount = movementCount,
            isMoving = totalDelta > movementThreshold
        )
    }

    /**
     * Menangani event light sensor
     * Menyimpan pembacaan untuk statistik sesi
     */
    private fun handleLightEvent(event: SensorEvent) {
        val lux = event.values[0]

        // Simpan pembacaan untuk perhitungan rata-rata sesi
        if (isMonitoring) {
            lightReadings.add(lux)
            // Batasi jumlah pembacaan yang disimpan (max 1000)
            if (lightReadings.size > 1000) {
                lightReadings.removeAt(0)
            }
        }

        // Tentukan kategori pencahayaan
        val category = getLightCategory(lux)

        _lightData.value = LightData(
            lux = lux,
            category = category,
            recommendation = getLightRecommendation(lux)
        )
    }

    /**
     * Menentukan kategori pencahayaan berdasarkan nilai lux
     */
    private fun getLightCategory(lux: Float): LightCategory {
        return when {
            lux < 10 -> LightCategory.VERY_DARK
            lux < 50 -> LightCategory.DARK
            lux < 200 -> LightCategory.DIM
            lux < 500 -> LightCategory.MODERATE
            lux < 1000 -> LightCategory.BRIGHT
            else -> LightCategory.VERY_BRIGHT
        }
    }

    /**
     * Memberikan saran berdasarkan kondisi pencahayaan
     */
    private fun getLightRecommendation(lux: Float): String {
        return when {
            lux < 50 -> "Pencahayaan sangat rendah. Tambahkan lampu agar tidak melelahkan mata."
            lux < 200 -> "Pencahayaan kurang ideal. Disarankan minimal 200 lux untuk membaca."
            lux < 500 -> "Pencahayaan cukup baik untuk belajar."
            lux < 2000 -> "Pencahayaan ideal untuk belajar dan membaca."
            else -> "Pencahayaan sangat terang. Pastikan tidak menyilaukan mata."
        }
    }

    // ---- Public Methods ----

    /**
     * Mulai monitoring sensor
     * Dipanggil ketika pengguna membuka layar Environment atau memulai sesi
     */
    fun startMonitoring() {
        isMonitoring = true
        resetSessionData()
        registerSensors()
    }

    /**
     * Hentikan monitoring sensor
     * Dipanggil ketika meninggalkan layar atau sesi selesai
     */
    fun stopMonitoring() {
        isMonitoring = false
        unregisterSensors()
    }

    /**
     * Reset data sesi (dipanggil saat memulai sesi baru)
     */
    fun resetSessionData() {
        movementCount = 0
        lightReadings.clear()
        lastX = 0f
        lastY = 0f
        lastZ = 0f
        _accelerometerData.value = AccelerometerData()
    }

    /**
     * Mendapatkan ringkasan sensor selama sesi
     * Dipanggil ketika sesi berakhir untuk disimpan ke database
     */
    fun getSessionSummary(): SensorSessionSummary {
        val avgLight = if (lightReadings.isNotEmpty()) {
            lightReadings.average().toFloat()
        } else 0f

        val minLight = lightReadings.minOrNull() ?: 0f
        val maxLight = lightReadings.maxOrNull() ?: 0f

        return SensorSessionSummary(
            averageLight = avgLight,
            minLight = minLight,
            maxLight = maxLight,
            movementCount = movementCount
        )
    }

    /**
     * Mendaftarkan listener sensor
     * Menggunakan SENSOR_DELAY_NORMAL untuk hemat baterai
     */
    private fun registerSensors() {
        accelerometer?.let {
            sensorManager.registerListener(
                sensorListener,
                it,
                SensorManager.SENSOR_DELAY_NORMAL
            )
        }
        lightSensor?.let {
            sensorManager.registerListener(
                sensorListener,
                it,
                SensorManager.SENSOR_DELAY_NORMAL
            )
        }
    }

    /**
     * Melepas registrasi listener sensor
     * PENTING: Selalu dipanggil untuk mencegah memory leak dan boros baterai
     */
    private fun unregisterSensors() {
        sensorManager.unregisterListener(sensorListener)
    }

    // ---- Lifecycle Methods ----

    /** Dipanggil saat Activity/Fragment resume - sensor tidak otomatis aktif */
    override fun onResume(owner: LifecycleOwner) {
        // Sensor hanya aktif jika diminta secara eksplisit via startMonitoring()
    }

    /** Dipanggil saat Activity/Fragment pause - hentikan sensor */
    override fun onPause(owner: LifecycleOwner) {
        if (isMonitoring) {
            unregisterSensors()
        }
    }

    /** Dipanggil saat Activity/Fragment resume setelah pause */
    fun resumeIfMonitoring() {
        if (isMonitoring) {
            registerSensors()
        }
    }

    /** Cleanup saat tidak lagi dibutuhkan */
    fun cleanup() {
        stopMonitoring()
    }
}

// ====================================================
// Data Classes untuk Sensor
// ====================================================

/** Data dari Accelerometer */
data class AccelerometerData(
    val x: Float = 0f,
    val y: Float = 0f,
    val z: Float = 0f,
    val magnitude: Float = 0f,
    val movementCount: Int = 0,
    val isMoving: Boolean = false
)

/** Data dari Light Sensor */
data class LightData(
    val lux: Float = 0f,
    val category: LightCategory = LightCategory.MODERATE,
    val recommendation: String = ""
)

/** Kategori tingkat pencahayaan */
enum class LightCategory(val displayName: String, val colorHex: String) {
    VERY_DARK("Sangat Gelap", "#FF6B6B"),
    DARK("Gelap", "#FACC15"),
    DIM("Redup", "#FB923C"),
    MODERATE("Cukup", "#4ADE80"),
    BRIGHT("Terang", "#5B8CFF"),
    VERY_BRIGHT("Sangat Terang", "#9275FF")
}

/** Ketersediaan sensor di perangkat */
data class SensorAvailability(
    val hasAccelerometer: Boolean = false,
    val hasLightSensor: Boolean = false
)

/** Ringkasan sensor selama sesi belajar */
data class SensorSessionSummary(
    val averageLight: Float = 0f,
    val minLight: Float = 0f,
    val maxLight: Float = 0f,
    val movementCount: Int = 0
)
