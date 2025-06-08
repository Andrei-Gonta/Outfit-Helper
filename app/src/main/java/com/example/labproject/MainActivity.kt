package com.example.labproject

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast

import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityCompat

import com.example.labproject.activities.DisplayClothesActivity
import com.example.labproject.activities.TaskActivity
import com.example.labproject.activities.TaskSimpleActivity
import com.example.labproject.adapter.RecycleViewAdapter
import com.example.labproject.adapter.ForecastAdapter
import com.example.labproject.utils.RetrofitInstance
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.Task
import com.example.labproject.databinding.ActivityMainBinding
import com.example.labproject.databinding.BottomSheetLayoutBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.mkrdeveloper.weatherappexample.data.forecastModels.ForecastData
import com.squareup.picasso.Picasso
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.recyclerview.widget.LinearLayoutManager

@Suppress("DEPRECATION")
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private lateinit var sheetLayoutBinding: BottomSheetLayoutBinding

    private lateinit var dialog: BottomSheetDialog

    private var city: String = "timisoara"

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    private lateinit var forecastAdapter: ForecastAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        sheetLayoutBinding = BottomSheetLayoutBinding.inflate(layoutInflater)
        dialog = BottomSheetDialog(this, R.style.BottomSheetTheme)
        dialog.setContentView(sheetLayoutBinding.root)
        setContentView(binding.root)

        // Initialize forecast RecyclerView
        forecastAdapter = ForecastAdapter()
        binding.rvForecast.apply {
            layoutManager = LinearLayoutManager(this@MainActivity, LinearLayoutManager.HORIZONTAL, false)
            adapter = forecastAdapter
        }

        val temperature = binding.tvTemp.text.toString()

        val temp_value = temperature
        binding.btnDisplayWaredrobe.setOnClickListener {
            val intent = Intent(this, DisplayClothesActivity::class.java)
                .putExtra("Temperature", binding.tvTemp.text.toString().replace("°C", "").toInt())
                .putExtra("WeatherDescription", binding.tvStatus.text.toString())

            startActivity(intent)
        }

        binding.btnDisplaySchedule.setOnClickListener {
            val intent = Intent(this, TaskActivity::class.java)
                .putExtra("Temperature", binding.tvTemp.text.toString().replace("°C", "").toInt())
                .putExtra("WeatherDescription", binding.tvStatus.text.toString())

            startActivity(intent)
        }

        binding.btnStart.setOnClickListener {
            val intent = Intent(this, TaskActivity::class.java)
                .putExtra("Temperature", binding.tvTemp.text.toString().replace("°C", "").toInt())
                .putExtra("WeatherDescription", binding.tvStatus.text.toString())

            startActivity(intent)
        }

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        binding.searchView.setOnQueryTextListener(object: SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (query!= null){
                    city = query
                }
                getCurrentWeather(city)
                return true
            }
            override fun onQueryTextChange(newText: String?): Boolean {
                return false
            }
        })
        getCurrentWeather(city)
        binding.tvLocation.setOnClickListener {
            fetchLocation()
        }

    }

    private fun fetchLocation() {
        val task: Task<Location> = fusedLocationProviderClient.lastLocation
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),101
            )
            return
        }

        task.addOnSuccessListener {
            val geocoder= Geocoder(this, Locale.getDefault())

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
                geocoder.getFromLocation(it.latitude,it.longitude,1, object: Geocoder.GeocodeListener{
                    override fun onGeocode(addresses: MutableList<Address>) {
                        city = addresses[0].locality
                    }
                })
            }else{
                val address = geocoder.getFromLocation(it.latitude,it.longitude,1) as List<Address>
                city = address[0].locality
            }
            getCurrentWeather(city)
        }
    }


    @SuppressLint("SetTextI18n")
    @OptIn(DelicateCoroutinesApi::class)
    private fun getCurrentWeather(city: String) {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                // Get current weather
                val weatherResponse = RetrofitInstance.api.getCurrentWeather(
                    city,
                    "metric",
                    applicationContext.getString(R.string.api_key)
                )

                // Get forecast
                val forecastResponse = RetrofitInstance.api.getForecast(
                    city,
                    "metric",
                    applicationContext.getString(R.string.api_key)
                )

                if (weatherResponse.isSuccessful && weatherResponse.body() != null &&
                    forecastResponse.isSuccessful && forecastResponse.body() != null) {
                    withContext(Dispatchers.Main) {
                        // Update current weather UI
                        val data = weatherResponse.body()!!
                        val iconId = data.weather[0].icon
                        val imgUrl = "https://openweathermap.org/img/wn/$iconId@4x.png"

                        Picasso.get().load(imgUrl).into(binding.imgWeather)

                        binding.apply {
                            tvStatus.text = data.weather[0].description
                            tvLocation.text = "${data.name}\n${data.sys.country}"
                            tvTemp.text = "${data.main.temp.toInt()}°C"
                            tvFeelsLike.text = "Feels like: ${data.main.feels_like.toInt()}°C"
                            tvMinTemp.text = "Min temp: ${data.main.temp_min.toInt()}°C"
                            tvMaxTemp.text = "Max temp: ${data.main.temp_max.toInt()}°C"
                            tvUpdateTime.text = "Last Update: ${
                                dateFormatConverter(
                                    data.dt.toLong()
                                )
                            }"
                        }

                        // Update forecast UI
                        val forecastData = forecastResponse.body()!!
                        forecastAdapter.updateForecast(forecastData.list.take(24)) // Show next 24 hours
                    }
                }
            } catch (e: IOException) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(applicationContext, "app error ${e.message}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: HttpException) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(applicationContext, "http error ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }




    private fun dateFormatConverter(date: Long): String {

        return SimpleDateFormat(
            "hh:mm a",
            Locale.ENGLISH
        ).format(Date(date * 1000))
    }
}