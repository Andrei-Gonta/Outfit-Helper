package com.example.labproject.activities

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.ViewModelProvider
import com.example.labproject.R
import com.example.labproject.model.ClothingItem
import com.example.labproject.viewmodel.ClothingItemViewModel
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.UUID
import java.util.concurrent.TimeUnit

class RecognitionActivity : AppCompatActivity() {
    private lateinit var tvResult: TextView
    private lateinit var btnSelectImage: Button
    private lateinit var btnSave: Button
    private lateinit var imageView: ImageView
    private val PAT = "a204934077084d83afb2bb8d279bf079"
    private val apiUrl = "https://api.clarifai.com/v2/models/e0be3b9d6a454f0493ac3a30784001ff/outputs"

    private lateinit var clothingItemViewModel: ClothingItemViewModel
    private var currentDetectedItem: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recognition)

        clothingItemViewModel = ViewModelProvider(this)[ClothingItemViewModel::class.java]

        tvResult = findViewById(R.id.tvResult)
        btnSelectImage = findViewById(R.id.btnSelectImage)
        btnSave = findViewById(R.id.btnSave)
        imageView = findViewById(R.id.imageView)

        btnSelectImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            pickImage.launch(intent)
        }

        btnSave.setOnClickListener {
            currentDetectedItem?.let { itemName ->
                val clothingItem = ClothingItem(
                    id = UUID.randomUUID().toString(),
                    name = itemName
                )
                clothingItemViewModel.insertClothingItem(clothingItem)

                // Start DisplayClothesActivity and finish this activity
                val intent = Intent(this, DisplayClothesActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
    }

    private val pickImage = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                try {
                    val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri)
                    // Show the image
                    imageView.setImageBitmap(bitmap)
                    imageView.visibility = View.VISIBLE
                    // Hide the save button until we get a result
                    btnSave.visibility = View.GONE
                    tvResult.visibility = View.GONE
                    // Process the image
                    processImage(bitmap)
                } catch (e: Exception) {
                    tvResult.text = "Error loading image: ${e.message}"
                    tvResult.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun processImage(bitmap: Bitmap) {
        try {
            val maxDimension = 1024
            var scaledBitmap = bitmap
            if (bitmap.width > maxDimension || bitmap.height > maxDimension) {
                val scale = Math.min(
                    maxDimension.toFloat() / bitmap.width,
                    maxDimension.toFloat() / bitmap.height
                )
                scaledBitmap = Bitmap.createScaledBitmap(
                    bitmap,
                    (bitmap.width * scale).toInt(),
                    (bitmap.height * scale).toInt(),
                    true
                )
            }

            val byteArrayOutputStream = ByteArrayOutputStream()
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 85, byteArrayOutputStream)
            val imageBytes = byteArrayOutputStream.toByteArray()

            if (imageBytes.size > 10 * 1024 * 1024) {
                runOnUiThread {
                    tvResult.text = "Image is too large. Please select a smaller image."
                    tvResult.visibility = View.VISIBLE
                }
                return
            }

            val base64Image = Base64.encodeToString(imageBytes, Base64.NO_WRAP)

            val json = """
                {
                  "user_app_id": {
                    "user_id": "clarifai",
                    "app_id": "main"
                  },
                  "inputs": [
                    {
                      "data": {
                        "image": {
                          "base64": "$base64Image"
                        }
                      }
                    }
                  ]
                }
            """.trimIndent()

            val client = OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build()

            val request = Request.Builder()
                .url(apiUrl)
                .addHeader("Authorization", "Key $PAT")
                .addHeader("Content-Type", "application/json")
                .post(RequestBody.create("application/json".toMediaTypeOrNull(), json))
                .build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    runOnUiThread {
                        tvResult.text = "Network error: ${e.message}"
                        tvResult.visibility = View.VISIBLE
                    }
                }

                override fun onResponse(call: Call, response: Response) {
                    val body = response.body?.string()
                    runOnUiThread {
                        if (body != null) {
                            try {
                                val jsonObject = JSONObject(body)
                                Log.d("API_RESPONSE", body)
                                if (!response.isSuccessful) {
                                    tvResult.text = "API Error: ${jsonObject.optString("status", "Unknown error")}"
                                    tvResult.visibility = View.VISIBLE
                                    return@runOnUiThread
                                }
                                val result = extractConcepts(body)
                                tvResult.text = result.first
                                tvResult.visibility = View.VISIBLE

                                // Store the detected item name and show save button
                                currentDetectedItem = result.second
                                if (currentDetectedItem != null) {
                                    btnSave.visibility = View.VISIBLE
                                }
                            } catch (e: Exception) {
                                tvResult.text = "Error parsing response: ${e.message}\nResponse: $body"
                                tvResult.visibility = View.VISIBLE
                            }
                        } else {
                            tvResult.text = "Empty response from server"
                            tvResult.visibility = View.VISIBLE
                        }
                    }
                }
            })
        } catch (e: Exception) {
            runOnUiThread {
                tvResult.text = "Error processing image: ${e.message}"
                tvResult.visibility = View.VISIBLE
            }
        }
    }

    private fun extractConcepts(json: String): Pair<String, String?> {
        try {
            val root = JSONObject(json)

            if (root.has("status") && root.getJSONObject("status").has("code") && root.getJSONObject("status").getInt("code") != 10000) {
                return Pair("API Error: ${root.getJSONObject("status").optString("description", "Unknown error")}", null)
            }

            val outputs = root.optJSONArray("outputs")
            if (outputs == null || outputs.length() == 0) {
                return Pair("No outputs found in response", null)
            }

            val firstOutput = outputs.getJSONObject(0)
            val data = firstOutput.optJSONObject("data")
            if (data == null) {
                return Pair("No data found in response", null)
            }

            val concepts = data.optJSONArray("concepts")
            if (concepts == null || concepts.length() == 0) {
                return Pair("No concepts detected in the image", null)
            }

            // Get the highest confidence prediction
            var highestConcept: Pair<String, Double>? = null
            for (i in 0 until concepts.length()) {
                val concept = concepts.getJSONObject(i)
                val name = concept.getString("name")
                val value = concept.getDouble("value")
                if (highestConcept == null || value > highestConcept.second) {
                    highestConcept = Pair(name, value)
                }
            }

            return if (highestConcept != null) {
                Pair(
                    "I am ${String.format("%.1f", highestConcept.second * 100)}% sure this is a ${highestConcept.first}!",
                    highestConcept.first
                )
            } else {
                Pair("No confident predictions found", null)
            }
        } catch (e: Exception) {
            Log.e("JSON_PARSE_ERROR", "Error parsing JSON: ${e.message}", e)
            return Pair("Error parsing response: ${e.message}", null)
        }
    }
}