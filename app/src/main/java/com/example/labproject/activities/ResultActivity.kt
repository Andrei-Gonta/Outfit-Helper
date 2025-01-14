package com.example.labproject.activities

import android.annotation.SuppressLint
import android.health.connect.datatypes.units.Temperature
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.semantics.text
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.labproject.R

class ResultActivity : AppCompatActivity() {
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_result)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val  message_top= findViewById<TextView>(R.id.message_top)
        val  message_bottom= findViewById<TextView>(R.id.message_bottom)
        val  message_bottom2= findViewById<TextView>(R.id.message_bottom2)


        val temp_value = intent.getIntExtra("Temprature", 0)

        val hat: ImageView = findViewById(R.id.hat)
        val jacket: ImageView = findViewById(R.id.jacket)
        val jeans: ImageView = findViewById(R.id.jeans)
        val boots: ImageView = findViewById(R.id.boots)



        if(temp_value < 10)
        {
            hat.setImageResource(R.drawable.hat)
            jacket.setImageResource(R.drawable.blue_jacket)
            jeans.setImageResource(R.drawable.pants2)
            boots.setImageResource(R.drawable.boots)
            message_top.text = "This is your outfit!"
            message_bottom.text = "There is a high chance of precipitation, don't forget to take an umbrella with you!"
            message_bottom2.text = "Have a wonderful day!"

        }
        else
        {
            message_top.text = "Another outfit"
        }



    }
}