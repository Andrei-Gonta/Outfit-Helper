package com.example.labproject.activities

import android.health.connect.datatypes.units.Temperature
import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.semantics.text
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.labproject.R

class ResultActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_result)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val resultTextView = findViewById<TextView>(R.id.resultTextView)

        val temp_value = intent.getIntExtra("Temprature", 0)

        if(temp_value < 10)
        {
            resultTextView.text = "Hot"
        }
        else
        {
            resultTextView.text = "Cold"
        }



    }
}