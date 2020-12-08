package com.example.myproject

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.RecyclerView
import com.example.myproject.city.CitiesClickListener
import com.example.myproject.city.CitiesRecyclerAdapter
import com.example.myproject.city.City
import com.example.myproject.managers.downloadImageFromNetwork
import com.example.myproject.managers.makeRequest
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.result.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.util.*
import kotlin.collections.ArrayList


class MainActivity : AppCompatActivity(), CitiesClickListener, LocationListener {
    var citiesArray: MutableList<City> = ArrayList()

    lateinit var descriptionTextView: TextView
    lateinit var temperatureTextView: TextView
    lateinit var feelsLikeTextView: TextView
    lateinit var weatherIconImageView: ImageView
    lateinit var windTextView: TextView
    lateinit var humidityTextView: TextView
    lateinit var pressureTextView: TextView
    lateinit var mDrawerLayout: DrawerLayout
    lateinit var mRelativeLayout: RelativeLayout


    private lateinit var location: Location
    private lateinit var textView: TextView
    private lateinit var locationManager: LocationManager
    private lateinit var tvGpsLocation: TextView
    private val locationPermissionCode = 2


    var isGPSEnable = false
    var isNetworkEnable = false
    var locationCheck = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvGpsLocation = findViewById(R.id.gps_text_View)
        textView = findViewById(R.id.temperature_text_view)
        temperatureTextView = findViewById(R.id.temperature_text_view)
        feelsLikeTextView = findViewById(R.id.feels_like_text_view)
        weatherIconImageView = findViewById(R.id.weather_icon)
        descriptionTextView = findViewById(R.id.description_text_view)
        windTextView = findViewById(R.id.wind_text_view)
        humidityTextView = findViewById(R.id.humidity_text_view)
        pressureTextView = findViewById(R.id.pressure_text_view)

        mRelativeLayout = findViewById(R.id.content);
        mDrawerLayout = findViewById(R.id.drawerLayout)

        val actionBarDrawerToggle: ActionBarDrawerToggle =
            object : ActionBarDrawerToggle(this, mDrawerLayout, R.string.open, R.string.close) {
                private val scaleFactor = 6f
                override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
                    super.onDrawerSlide(drawerView, slideOffset)
                    val slideX: Float = drawerView.getWidth() * slideOffset
                    mRelativeLayout.setTranslationX(slideX)
                    mRelativeLayout.setScaleX(1 - slideOffset / scaleFactor)
                    mRelativeLayout.setScaleY(1 - slideOffset / scaleFactor)
                }
            }

        mDrawerLayout.setScrimColor(Color.TRANSPARENT)
        mDrawerLayout.setDrawerElevation(0f)
        mDrawerLayout.addDrawerListener(actionBarDrawerToggle)




        findViewById<Button>(R.id.current_location).setOnClickListener {
            locationCheck = true
            getLocation()
            mDrawerLayout.closeDrawer(GravityCompat.START)
        }

        findViewById<ImageButton>(R.id.menu_drawer).setOnClickListener {
            mDrawerLayout.openDrawer(GravityCompat.START)
        }

        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        getLocation()

        initList()
        initRecycler()
    }


    private fun initList() {
        citiesArray.add(City("Москва"))
        citiesArray.add(City("Лондон"))
        citiesArray.add(City("Берлин"))
        citiesArray.add(City("Париж"))
        citiesArray.add(City("Токио"))
        citiesArray.add(City("Казань"))
        citiesArray.add(City("Иваново"))
        citiesArray.add(City("Вашингтон"))
        citiesArray.add(City("Минск"))
        citiesArray.add(City("Киев"))
        citiesArray.add(City("Киров"))
        citiesArray.add(City("Самара"))
    }

    private fun initRecycler() {
        val recyclerView: RecyclerView = findViewById(R.id.recycler_city_view)
        val adapter = CitiesRecyclerAdapter(this, citiesArray, this)
        recyclerView.adapter = adapter
    }

    override fun onCitiesClickListener(position: Int) {
        requestCityWeather(citiesArray[position].name)
        tvGpsLocation.text = citiesArray[position].name
        mDrawerLayout.closeDrawer(GravityCompat.START)
    }

    private fun requestCityWeather(cityName: String) {
        GlobalScope.launch {
            val result = makeRequest(cityName)
            withContext(Dispatchers.Main) { parseJson(result) }
        }
    }


    private fun getReverseGeoCode() {
        val gcd = Geocoder(this, Locale.getDefault())
        val addresses = gcd.getFromLocation(location.latitude, location.longitude, 1)
        if (addresses.size > 0) {
            tvGpsLocation.text = addresses[0].locality
        }
    }

    private fun getLocation() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if ((ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED)
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ),
                    locationPermissionCode
                )
            } else {
                requestLocation()
            }
        } else {
            requestLocation()
        }
    }

    @SuppressLint("MissingPermission")
    fun requestLocation() {
        isGPSEnable = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        isNetworkEnable = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

        when {
            isNetworkEnable -> {
                locationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER,
                    1000,
                    0F,
                    this
                )
            }
            isGPSEnable -> {
                locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    1000,
                    0F,
                    this
                );
            }
            else -> {
                Toast.makeText(this, "Providers Denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onProviderEnabled(provider: String) {
        super.onProviderEnabled(provider)
        tvGpsLocation.text = provider
    }

    override fun onProviderDisabled(provider: String) {
        super.onProviderDisabled(provider)
        tvGpsLocation.text = provider
    }

    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}

    override fun onLocationChanged(location: Location) {
        if (locationCheck) {
            this.location = location
            request(location)
            getReverseGeoCode()
            locationCheck = false
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == locationPermissionCode) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLocation()
            } else {
                onCitiesClickListener(0)
            }
        }
    }

    private fun request(location: Location) {

        val url: String =
            "https://api.openweathermap.org/data/2.5/weather?lat=${location.latitude}&lon=${location.longitude}&appid=44ea5bfdf77b5ba5b06bd622ec304f68&lang=ru"

        url.httpGet().responseString { _, _, result ->
            when (result) {
                is Result.Failure -> {
                    textView.post { textView.text = result.getException().message }
                }

                is Result.Success -> {
                    textView.post { parseJson(result.get()) }
                }
            }
        }
    }

    private fun parseJson(string: String) {
        val json = JSONObject(string)
        val weather = json.getJSONArray("weather")

        for (x in 0 until 1) {
            val jsonObject = weather.getJSONObject(x)

            val description = jsonObject.optString("description", "")
            val weatherIcon = jsonObject.optString("icon", "")
            descriptionTextView.text = description.toUpperCase()
            requestIcon(weatherIcon)
        }

        val main = json.getJSONObject("main")
        val temperature = main.optInt("temp", 0)
        val feelsLike = main.optInt("feels_like", 0)
        val humidity = main.optInt("humidity", 0)
        val pressure = main.optInt("pressure", 0)

        val wind = json.getJSONObject("wind")
        val speed = wind.optDouble("speed", 0.0)

        temperatureTextView.text = (temperature - 273).toString() + "°C"
        feelsLikeTextView.text = "Ощущается как : " + (feelsLike - 273).toString() + "°C"
        windTextView.text = "Ветер: ${speed} м/с"
        humidityTextView.text = "Влажность: ${humidity}%"
        pressureTextView.text = "Давление: ${pressure} гПа"


    }

    private fun requestIcon(weatherIconString: String) {
        Thread {
            val icon = downloadImageFromNetwork(
                "https://openweathermap.org/img/wn/$weatherIconString@4x.png"
            )
            weatherIconImageView.post { weatherIconImageView.setImageBitmap(icon) }
        }.start()
    }
}