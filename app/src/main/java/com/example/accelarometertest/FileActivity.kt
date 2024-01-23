package com.example.accelarometertest

import android.os.Bundle
import android.os.PersistableBundle
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.io.BufferedReader
import java.io.File
import java.io.FileReader

class FileActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_file)
        setupTableRows()
    }

    private fun setupTableRows(){
        val file = File(filesDir, "Test.txt");
        val br = BufferedReader(FileReader(file))
        val it = br.lineSequence().iterator()

        val tl = findViewById<TableLayout>(R.id.dataTable)
        val tsl = findViewById<LinearLayout>(R.id.TestLayout)

        var line: String?
        while(br.readLine().also { line = it } !=null){
            val values = line?.split("|")
            val tr = TableRow(this)
            tr.layoutParams = TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT)
            val xtv = TextView(this)
            val ytv = TextView(this)
            val ztv = TextView(this)
            val atv = TextView(this)

            xtv.text = values?.get(0)
            ytv.text = values?.get(1)
            ztv.text = values?.get(2)
            atv.text = values?.get(3)

            tr.addView(xtv)
            tr.addView(ytv)
            tr.addView(ztv)
            tr.addView(atv)


            tl.addView(tr)

        }
    }

}