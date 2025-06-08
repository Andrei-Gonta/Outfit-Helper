package com.example.labproject.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.labproject.databinding.ItemForecastBinding
import com.mkrdeveloper.weatherappexample.data.forecastModels.ForecastData
import com.squareup.picasso.Picasso
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.math.roundToInt

class ForecastAdapter : RecyclerView.Adapter<ForecastAdapter.ForecastViewHolder>() {
    private var forecastList = listOf<ForecastData>()

    class ForecastViewHolder(val binding: ItemForecastBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ForecastViewHolder {
        return ForecastViewHolder(
            ItemForecastBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ForecastViewHolder, position: Int) {
        val forecast = forecastList[position]
        holder.binding.apply {
            // Format time
            val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val outputFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
            val date = inputFormat.parse(forecast.dt_txt)
            tvTime.text = outputFormat.format(date!!)

            // Set temperature
            tvTemp.text = "${forecast.main.temp.toInt()}°C"

            // Set rain chance (convert probability to percentage)
            tvRainChance.text = "${(forecast.pop * 100).roundToInt()}%"

            // Load weather icon
            val iconCode = forecast.weather[0].icon
            val iconUrl = "https://openweathermap.org/img/w/$iconCode.png"
            Picasso.get().load(iconUrl).into(imgWeather)
        }
    }

    override fun getItemCount() = forecastList.size

    fun updateForecast(newForecast: List<ForecastData>) {
        forecastList = newForecast
        notifyDataSetChanged()
    }
}