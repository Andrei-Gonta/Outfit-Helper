package com.example.labproject.activities

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.example.labproject.R
import com.example.labproject.model.ClothingItem
import com.example.labproject.model.Task
import com.example.labproject.viewmodel.ClothingItemViewModel
import com.example.labproject.viewmodel.TaskViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ResultActivity : AppCompatActivity() {

    private lateinit var taskViewModel: TaskViewModel
    private lateinit var clothingItemViewModel: ClothingItemViewModel

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

        taskViewModel = ViewModelProvider(this)[TaskViewModel::class.java]
        clothingItemViewModel = ViewModelProvider(this)[ClothingItemViewModel::class.java]

        val messageTop = findViewById<TextView>(R.id.message_top)
        val messageBottom = findViewById<TextView>(R.id.message_bottom)
        val messageBottom2 = findViewById<TextView>(R.id.message_bottom2)
        val promptText = findViewById<TextView>(R.id.prompt_text)

        // Get data passed from previous activity
        val tempValue = intent.getIntExtra("Temperature", 0)
        val weatherDescription = intent.getStringExtra("WeatherDescription") ?: ""
        val windSpeed = intent.getFloatExtra("WindSpeed", 0f)
        val humidity = intent.getIntExtra("Humidity", 0)

        // Initialize views for outfit display
        val hat: ImageView = findViewById(R.id.hat)
        val jacket: ImageView = findViewById(R.id.jacket)
        val jeans: ImageView = findViewById(R.id.jeans)
        val boots: ImageView = findViewById(R.id.boots)

        // Fetch tasks and clothing items
        CoroutineScope(Dispatchers.Main).launch {
            try {
                // Get today's date
                val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

                // Fetch tasks and clothing items
                val tasks = taskViewModel.taskStateFlow.first().data?.first()?.filter { task ->
                    val taskDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(task.starttime)
                    taskDate == today
                } ?: emptyList()

                val clothingItems = clothingItemViewModel.clothingItemStateFlow.first().data?.first() ?: emptyList()

                // Build the prompt
                val prompt = buildPrompt(tempValue, weatherDescription, windSpeed, humidity, tasks, clothingItems)

                // Display the prompt
                promptText.text = prompt

                // For now, keep the simple outfit logic
                if(tempValue < 10) {
                    hat.setImageResource(R.drawable.hat)
                    jacket.setImageResource(R.drawable.blue_jacket)
                    jeans.setImageResource(R.drawable.pants2)
                    boots.setImageResource(R.drawable.boots)
                    messageTop.text = "This is your outfit!"
                    messageBottom.text = "There is a high chance of precipitation, don't forget to take an umbrella with you!"
                    messageBottom2.text = "Have a wonderful day!"
                } else {
                    messageTop.text = "Another outfit"
                }
            } catch (e: Exception) {
                messageTop.text = "Error loading data: ${e.message}"
            }
        }
    }

    private fun buildPrompt(
        temperature: Int,
        weatherDescription: String,
        windSpeed: Float,
        humidity: Int,
        tasks: List<Task>,
        clothingItems: List<ClothingItem>
    ): String {
        // Get task descriptions from database with time information
        val taskDescriptions = tasks.joinToString(", ") { task ->
            "${task.title} (${SimpleDateFormat("HH:mm", Locale.getDefault()).format(task.starttime)} - ${SimpleDateFormat("HH:mm", Locale.getDefault()).format(task.endtime)})"
        }

        // Get clothing items from intent
        val availableClothes = intent.getStringArrayListExtra("ClothingItems")?.joinToString(", ") ?: ""

        return "Sugerează un outfit potrivit pentru o zi cu ${temperature}°C, " +
                "cu ${weatherDescription.lowercase()}, vânt de ${windSpeed}m/s și umiditate de ${humidity}%. " +
                "Utilizatorul are următoarele activități: ${if (taskDescriptions.isNotEmpty()) taskDescriptions else "nicio activitate"}, " +
                "iar hainele disponibile sunt: ${if (availableClothes.isNotEmpty()) availableClothes else "nicio haină"}"
    }
}