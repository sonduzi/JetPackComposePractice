package com.example.level

import android.app.Application
import android.content.Context
import android.content.pm.ActivityInfo
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.*
import com.example.level.ui.theme.JetpackComposePracticeTheme

class MainActivity : ComponentActivity() {
    private val viewModel by viewModels<MainViewModel>()
    override fun onCreate(savedInstanceState: Bundle?) {
        // 화면이 꺼지지 않게 하기
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        // 화면이 가로 모드로 고정되게 하기
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        super.onCreate(savedInstanceState)
        lifecycle.addObserver(viewModel)
        setContent {
            TiltScreen(x = viewModel.x.value, y = viewModel.y.value)
        }
    }
}

class MainViewModel(application: Application) : AndroidViewModel(application),
    SensorEventListener, LifecycleEventObserver {
    private val _x = mutableStateOf(0f)
    val x : State<Float> = _x

    private val _y = mutableStateOf(0f)
    val y : State<Float> = _y

    private val sensorManager by lazy {
        application.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    }

    fun registerSensor() {
        sensorManager.registerListener(
            this,
            sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
            SensorManager.SENSOR_DELAY_NORMAL,
        )
    }

    fun unregisterSensor() {
        sensorManager.unregisterListener(
            this
        )
    }

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        if (event == Lifecycle.Event.ON_RESUME) {
            registerSensor()
        } else if (event == Lifecycle.Event.ON_PAUSE) {
            unregisterSensor()
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            _x.value = event.values[0]
            _y.value = event.values[1]
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    }
}

@Composable
fun TiltScreen(x: Float, y: Float) {
    val yCoord = x * 20
    val xCoord = y * 20

    Canvas(modifier = Modifier.fillMaxSize()) {
        val centerX = size.width / 2
        val centerY = size.height / 2

        // 바깥 원
        drawCircle(
            color = Color.Black,
            radius = 100f,
            center = Offset(centerX, centerY),
            style = Stroke(),
        )
        // 녹색 원
        drawCircle(
            color = Color.Green,
            radius = 100f,
            center = Offset(xCoord + centerX , yCoord +centerY),
        )
        // 가운데 십자가
        drawLine(
            color = Color.Black,
            start = Offset(centerX - 20, centerY),
            end = Offset(centerX + 20, centerY),
        )
        drawLine(
            color = Color.Black,
            start = Offset(centerX, centerY - 20),
            end = Offset(centerX, centerY+ 20),
        )
    }
}

@Preview(showBackground = true)
@Composable
fun Preview() {
    TiltScreen(x = 30f, y = 20f)
}
