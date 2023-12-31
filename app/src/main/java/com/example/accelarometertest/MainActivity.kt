package com.example.accelarometertest

import android.graphics.Color
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.MediaPlayer
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.CalendarContract.Colors
import android.widget.TextView
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.android.material.slider.LabelFormatter
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.math.sqrt

class MainActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var sensorManager : SensorManager
    private lateinit var beep : MediaPlayer

    private var currentAcc  = 0f
    private var lastAcc = 0f
    private var lastVals = arrayOfNulls<Float>(3)
    private var acc = 0f

    private lateinit var accView : TextView
    private lateinit var  valView : TextView
    private lateinit var diffView : TextView

    //CHART
    private lateinit var barChart : BarChart
    private lateinit var barData : BarData
    private lateinit var barDataSet : BarDataSet
    private var barEntries = ArrayList<BarEntry>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        accView = findViewById(R.id.accView)
        valView = findViewById(R.id.valView)
        diffView = findViewById(R.id.valDiffView)

        beep = MediaPlayer.create(this, R.raw.beep)
        setUpChartStuff()
        setUpSensorStuff()

    }

    //GETTING DATA
    private fun setUpSensorStuff(){
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)?.also {
             sensorManager.registerListener(
                 this,
                 it,
                 2,
                 2
             )
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if(event?.sensor?.type == Sensor.TYPE_ACCELEROMETER){
            val x = event.values[0].toFloat()
            val y = event.values[1].toFloat()
            val z = event.values[2].toFloat()

            val lastX = lastVals[0]
            val lastY = lastVals[1]
            val lastZ = lastVals[2]

            var xDiff = 0f
            var yDiff = 0f
            var zDiff = 0f

            currentAcc = sqrt(x*x + z*z + y*y).toDouble().toFloat()
            if(lastAcc != 0f){
                xDiff = abs(x - lastX!!)
                yDiff = abs(y - lastY!!)
                zDiff = abs(z - lastZ!!)
                currentAcc = sqrt(xDiff * xDiff + yDiff*yDiff + zDiff * zDiff)
                acc = currentAcc

                if(acc > 40){
                    beep.start()
                }

                barEntries[0] = BarEntry(0f, acc, "Acceleration")
                barEntries[1] = BarEntry(1f, xDiff, "X Acceleration")
                barEntries[2] = BarEntry(2f, yDiff, "Y Acceleration")
                barEntries[3] = BarEntry(3f, zDiff, "Z Acceleration")
                refreshChart()

            }

            lastVals[0] = x
            lastVals[1] = y
            lastVals[2] = z
            lastAcc = currentAcc



            accView.text = getString(R.string.accText, acc)
            valView.text = getString(R.string.valuesText, x, y, z)
            diffView.text = getString(R.string.valuesDiff, xDiff, yDiff, zDiff)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {

    }

//    CHARTING DATA
    private fun setUpChartStuff(){
        var xLabels = ArrayList<String>()
        xLabels.add("Acc")
        xLabels.add("xAcc")
        xLabels.add("yAcc")
        xLabels.add("zAcc")

        barChart = findViewById(R.id.barChart)
        barChart.axisRight.isEnabled = false
        setBarEntries()
        barDataSet = BarDataSet(barEntries, "")
        barDataSet.colors = ColorTemplate.COLORFUL_COLORS.toList()
        barChart.legend.textColor = Color.WHITE
        barChart.description.isEnabled = false

        val yAx = barChart.axisLeft
        yAx.axisMaximum = 50f
        yAx.axisMinimum = 0f
        yAx.axisLineWidth = 2f
        yAx.axisLineColor = Color.WHITE
        yAx.labelCount = 10
        yAx.textColor = Color.WHITE

        barData = BarData(barDataSet)
        barData.setValueTextSize(16f)
        barData.setDrawValues(false)
        barData.setValueTextColors(ColorTemplate.COLORFUL_COLORS.toList())


        barChart.data = barData
    }
    private fun refreshChart(){
        barDataSet = BarDataSet(barEntries, "Movement")
        barDataSet.colors = ColorTemplate.COLORFUL_COLORS.toList()
        barData = BarData(barDataSet)
        barData.setValueTextSize(16f)
        barData.setValueTextColors(ColorTemplate.COLORFUL_COLORS.toList())
        barChart.data = barData

        barChart.notifyDataSetChanged()
        barChart.invalidate()
    }
    private fun setBarEntries(){
        barEntries.add(BarEntry(0f, 45f))
        barEntries.add(BarEntry(1f, 45f))
        barEntries.add(BarEntry(2f, 45f))
        barEntries.add(BarEntry(3f,45f))
    }
}