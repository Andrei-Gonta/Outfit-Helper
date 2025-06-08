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
import kotlinx.coroutines.delay
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

        // Initialize both task and clothing item lists
        taskViewModel.getTaskList(true, "starttime")
        clothingItemViewModel.getClothingItems(true, "name")

        android.util.Log.d("ResultActivity", "ViewModels initialized and lists requested")

        val messageTop = findViewById<TextView>(R.id.message_top)
        val messageBottom = findViewById<TextView>(R.id.message_bottom)
        val messageBottom2 = findViewById<TextView>(R.id.message_bottom2)
        val promptText = findViewById<TextView>(R.id.prompt_text)

        // Get data passed from previous activity
        val tempValue = intent.getIntExtra("Temperature", 0)
        val weatherDescription = intent.getStringExtra("WeatherDescription") ?: ""
        val windSpeed = intent.getFloatExtra("WindSpeed", 0f)
        val rainChance = intent.getIntExtra("RainChance", 0)

        android.util.Log.d("ResultActivity", "Starting to fetch tasks and clothing items...")

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
                android.util.Log.d("ResultActivity", "Today's date: $today")

                // Wait a bit for the data to be loaded
                delay(500)

                // Get the initial task data
                val taskStateFlow = taskViewModel.taskStateFlow.first()
                android.util.Log.d("ResultActivity", "Task state flow status: ${taskStateFlow.status}")

                val taskData = taskStateFlow.data
                android.util.Log.d("ResultActivity", "Task data available: ${taskData != null}")

                // Get the first list of tasks if available
                val allTasks = taskData?.first()
                android.util.Log.d("ResultActivity", "All tasks count: ${allTasks?.size}")
                android.util.Log.d("ResultActivity", "All tasks: ${allTasks?.map {
                    "${it.title} (${SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(it.starttime)} - ${SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(it.endtime)})"
                }}")

                // Filter tasks for today
                val tasks = allTasks?.filter { task ->
                    val taskDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(task.starttime)
                    val isToday = taskDate == today
                    android.util.Log.d("ResultActivity", "Task: ${task.title}, Date: $taskDate, Today: $today, IsToday: $isToday")
                    isToday
                } ?: emptyList()

                android.util.Log.d("ResultActivity", "Today's tasks count: ${tasks.size}")
                android.util.Log.d("ResultActivity", "Today's tasks: ${tasks.map {
                    "${it.title} (${SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(it.starttime)} - ${SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(it.endtime)})"
                }}")

                // Get clothing items
                val clothingItemStateFlow = clothingItemViewModel.clothingItemStateFlow.first()
                android.util.Log.d("ResultActivity", "Clothing item state flow status: ${clothingItemStateFlow.status}")

                val clothingItemsData = clothingItemStateFlow.data
                android.util.Log.d("ResultActivity", "Clothing items data available: ${clothingItemsData != null}")

                val clothingItems = clothingItemsData?.first() ?: emptyList()
                android.util.Log.d("ResultActivity", "Clothing items count: ${clothingItems.size}")
                android.util.Log.d("ResultActivity", "Clothing items: ${clothingItems.map { it.name }}")

                // Build and display the prompt
                val prompt = buildPrompt(tempValue, weatherDescription, windSpeed, rainChance, tasks, clothingItems)
                android.util.Log.d("ResultActivity", "Built prompt: $prompt")
                promptText.text = prompt

                // For now, keep the simple outfit logic
                if(tempValue < 10 || rainChance > 50) {
                    hat.setImageResource(R.drawable.hat)
                    jacket.setImageResource(R.drawable.blue_jacket)
                    jeans.setImageResource(R.drawable.pants2)
                    boots.setImageResource(R.drawable.boots)
                    messageTop.text = "This is your outfit!"
                    messageBottom.text = if (rainChance > 50) {
                        "There is a ${rainChance}% chance of rain, don't forget to take an umbrella with you!"
                    } else {
                        "It's cold outside, dress warmly!"
                    }
                    messageBottom2.text = "Have a wonderful day!"
                } else {
                    messageTop.text = "Another outfit"
                }
            } catch (e: Exception) {
                android.util.Log.e("ResultActivity", "Error loading data", e)
                messageTop.text = "Error loading data: ${e.message}"
            }
        }
    }

    private fun buildPrompt(
        temperature: Int,
        weatherDescription: String,
        windSpeed: Float,
        rainChance: Int,
        tasks: List<Task>,
        clothingItems: List<ClothingItem>
    ): String {
        android.util.Log.d("ResultActivity", "Building prompt with ${tasks.size} tasks")

        // Get task descriptions from database with time information
        val taskDescriptions = if (tasks.isNotEmpty()) {
            tasks.joinToString(", ") { task ->
                val startTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(task.starttime)
                val endTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(task.endtime)
                "${task.title} ($startTime - $endTime)".also {
                    android.util.Log.d("ResultActivity", "Task description: $it")
                }
            }
        } else {
            "nicio activitate".also {
                android.util.Log.d("ResultActivity", "No tasks found")
            }
        }

        // Get clothing items description
        val availableClothes = if (clothingItems.isNotEmpty()) {
            clothingItems.joinToString(", ") { it.name }
        } else {
            "nicio haină"
        }

        return "Sugerează un outfit potrivit pentru o zi cu ${temperature}°C, " +
                "cu ${weatherDescription.lowercase()}, vânt de ${windSpeed}m/s și șansă de ploaie de ${rainChance}%. " +
                "Utilizatorul are următoarele activități: ${taskDescriptions}, " +
                "iar hainele disponibile sunt: ${availableClothes}"
    }
}