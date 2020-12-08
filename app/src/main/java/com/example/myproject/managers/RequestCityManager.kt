package com.example.myproject.managers

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.lang.StringBuilder
import java.net.URL
import javax.net.ssl.HttpsURLConnection

suspend fun makeRequest(cityName: String): String {
    return withContext(Dispatchers.IO) {
        var buffer: BufferedReader? = null

        try{
            val url = URL("https://api.openweathermap.org/data/2.5/weather?q=$cityName&appid=44ea5bfdf77b5ba5b06bd622ec304f68&lang=ru")

            val httpsURLConnection = url.openConnection() as HttpsURLConnection
            httpsURLConnection.requestMethod = "GET"
            httpsURLConnection.readTimeout = 10000
            httpsURLConnection.connect()

            buffer = BufferedReader(InputStreamReader(httpsURLConnection.inputStream))

            val builder = StringBuilder()
            var line: String? = null

            while (true) {
                line = buffer.readLine() ?: break
                builder.append(line).append("")
            }

            builder.toString()

        } catch (exc: Exception) {
            buffer?.close()
            exc.message.toString()
        } finally {
            buffer?.close()
        }
    }
}
