package com.leo.sensordemo

import android.content.Context.SENSOR_SERVICE
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.leo.sensordemo.databinding.ActivityMainBinding

/**
 * 1. 获取SensorManager对象
 *
 * 2. 获取Sensor对象
 *
 * 3. 注册Sensor对象
 *
 * 4. 重写onAccuracyChanged，onSensorChanged这两个方法
 *
 * 5. 注销Sensor对象
 */
class MainActivity : AppCompatActivity(), SensorEventListener {

    val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater);
    }

    val sensorManager: SensorManager by lazy {
        getSystemService(SENSOR_SERVICE) as SensorManager
    }
    //加速度传感器
    var accSensor :Sensor? = null
    // 陀螺仪传感器
    var gyroSensor :Sensor? = null
    //旋转矢量传感器
    var rotationVectorSensor :Sensor? = null
    //磁力传感器
    var magneticSensor :Sensor? = null

    val accValues = FloatArray(3);
    val magValues  = FloatArray(3);
    val r = FloatArray(9);
    val orientationValues = FloatArray(3);


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
//        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())

            Log.d("TAG", "onCreate: "+systemBars)
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initSensor()
        binding.btStart.setOnClickListener {
            if (binding.btStart.text == "start") {
                registerSensor()
                binding.btStart.text = "stop"
            } else {
                unregisterSensor()
                binding.btStart.text = "start"
            }
        }
    }
    fun initSensor() {
        val sensorList = sensorManager.getSensorList(Sensor.TYPE_ALL);
        val stringBuilder = StringBuilder()
        stringBuilder.append("传感器信息: \n")
        for (sensor in sensorList) {
            stringBuilder.append(sensor.name).append("\n")
        }
        binding.tvSensorName.text = stringBuilder.toString()

        accSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        gyroSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
        rotationVectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        magneticSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

    }

    fun registerSensor() {
        sensorManager.registerListener(this, accSensor, SensorManager.SENSOR_DELAY_GAME);
        sensorManager.registerListener(this, gyroSensor, SensorManager.SENSOR_DELAY_GAME);
        sensorManager.registerListener(this, rotationVectorSensor, SensorManager.SENSOR_DELAY_GAME);
        sensorManager.registerListener(this, magneticSensor, SensorManager.SENSOR_DELAY_GAME);
    }
    fun unregisterSensor() {
        sensorManager.unregisterListener(this)
    }

    override fun onResume() {
        super.onResume()
//        registerSensor()
    }

    override fun onPause() {
        super.onPause()
        if (binding.btStart.text == "stop") {
            binding.btStart.performClick()
        }
    }

    override fun onSensorChanged(event: SensorEvent) {
        val x = event.values[0].toFixed();
        val y = event.values[1].toFixed();
        val z = event.values[2].toFixed();
        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            accValues[0] = x
            accValues[1] = y
            accValues[2] = z
            binding.tvAccValueX.text = "x:${x.toFixed()}"
            binding.tvAccValueY.text = "y:${y.toFixed()}"
            binding.tvAccValueZ.text = "z:${z.toFixed()}"
        }else if (event.sensor.type == Sensor.TYPE_GYROSCOPE) {
            binding.tvGyroValueX.text = "x:${x.toFixed()}"
            binding.tvGyroValueY.text = "y:${y.toFixed()}"
            binding.tvGyroValueZ.text = "z:${z.toFixed()}"
        }else if (event.sensor.type == Sensor.TYPE_ROTATION_VECTOR) {
            binding.tvRotationVectorValueX.text = "x:${x.toFixed()}"
            binding.tvRotationVectorValueY.text = "y:${y.toFixed()}"
            binding.tvRotationVectorValueZ.text = "z:${z.toFixed()}"
        }else if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
            magValues[0] = x;
            magValues[1] = y;
            magValues[2] = z;
            SensorManager.getRotationMatrix(r, null, accValues, magValues);
            SensorManager.getOrientation(r,orientationValues);
            var azimuth = Math.toDegrees(orientationValues[0].toDouble());//航向角（Azimuth）：设备绕垂直轴旋转的角度，通常表示设备面向的方向 范围-180-180
            val pitch = Math.toDegrees(orientationValues[1].toDouble());//倾角（Pitch）：设备绕Y轴旋转的角度，通常表示设备向前或向后的倾斜
            val roll = Math.toDegrees(orientationValues[2].toDouble());//滚动角（Roll）：设备绕X轴旋转的角度，通常表示设备侧向的倾斜
            //获取磁场强度
            val magneticField = Math.sqrt((magValues[0] * magValues[0] + magValues[1] * magValues[1] + magValues[2] * magValues[2]).toDouble())
            binding.tvMagField.text = "磁场强度\n${magneticField.toFixed()}uT"
            binding.tvAzimuth.text = "航向角\n${azimuth.toFixed()}°"
            binding.tvPitch.text = "倾角\n${pitch.toFixed()}°"
            binding.tvRoll.text = "滚动角\n${roll.toFixed()}°"


            binding.tvMagValueX.text = "x:${x.toFixed()}"
            binding.tvMagValueY.text = "y:${y.toFixed()}"
            binding.tvMagValueZ.text = "z:${z.toFixed()}"
        }


    }

    /**
     * 当传感器的精度发生变化时会被调用。函数中的sensor参数表示发生变化的传感器对象，accuracy参数表示当前传感器的精度级别
     * SensorManager.SENSOR_STATUS_UNRELIABLE：数据不可靠，通常意味着传感器没有可用的数据或者数据可能不准确。
     * SensorManager.SENSOR_STATUS_ACCURACY_LOW：低精度，数据可能有较大的误差。
     * SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM：中等精度，数据有一定的准确性但可能仍有误差。
     * SensorManager.SENSOR_STATUS_ACCURACY_HIGH：高精度，数据较为准确。
     */
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {

    }

    fun Float.toFixed():Float{
        return Math.round(this * 1000)/1000F;
    }
    fun Double.toFixed():Double{
        return Math.round(this * 1000)/1000.0;
    }


}