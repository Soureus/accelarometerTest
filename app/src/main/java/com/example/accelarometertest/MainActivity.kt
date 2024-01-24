package com.example.accelarometertest

import android.content.Intent
import android.graphics.Color
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.MediaPlayer
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.CalendarContract.Colors
import android.widget.Button
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
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.File
import java.io.FileOutputStream
import java.io.FileReader
import java.io.FileWriter
import java.io.IOException
import java.lang.System.out
import java.math.BigDecimal
import java.math.RoundingMode
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
    private lateinit var textOutput : TextView
    private lateinit var outputButton : Button

    //CHART
    private lateinit var barChart : BarChart
    private lateinit var barData : BarData
    private lateinit var barDataSet : BarDataSet
    private var barEntries = ArrayList<BarEntry>()

    private lateinit var bw : BufferedWriter
    private lateinit var br : BufferedReader
    private lateinit var file : File

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        accView = findViewById(R.id.accView)
        valView = findViewById(R.id.valView)
        diffView = findViewById(R.id.valDiffView)
        outputButton = findViewById(R.id.fileOpenBtn)

        file = File(filesDir, "Test.txt");
        try {
            file.createNewFile();
        }catch (e : IOException){
            e.printStackTrace()
        }

        bw = BufferedWriter(FileWriter(file))

        outputButton.setOnClickListener {
            val intent = Intent(this, FileActivity::class.java)
            startActivity(intent)
            //textOutput = findViewById(R.id.fileOutput)

            //val text = br.readLine()
            //textOutput.text = text
        }

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
                 1,
                 1
             )
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if(event?.sensor?.type == Sensor.TYPE_ACCELEROMETER){
            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]

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

                    val xDiffDec = BigDecimal(xDiff.toDouble()).setScale(3, RoundingMode.HALF_EVEN)
                    val yDiffDec = BigDecimal(yDiff.toDouble()).setScale(3, RoundingMode.HALF_EVEN)
                    val zDiffDec = BigDecimal(zDiff.toDouble()).setScale(3, RoundingMode.HALF_EVEN)
                    val accDec = BigDecimal(acc.toDouble()).setScale(3, RoundingMode.HALF_EVEN)
                    val output = "$xDiffDec|$yDiffDec|$zDiffDec|$accDec"

                    FileOutputStream(file, true).bufferedWriter().use { out ->
                        out.appendLine(output)
                    }


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