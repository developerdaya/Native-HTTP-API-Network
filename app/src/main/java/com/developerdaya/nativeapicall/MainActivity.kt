package com.developerdaya.nativeapicall

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.*
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var postAdapter: PostsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Fetch posts
        fetchPosts()
    }

    private fun fetchPosts() {
        CoroutineScope(Dispatchers.IO).launch {
            val response = makeApiCall("https://jsonplaceholder.typicode.com/posts")
            val posts = parseJson(response)

            withContext(Dispatchers.Main) {
                postAdapter = PostsAdapter(posts)
                recyclerView.adapter = postAdapter
            }
        }
    }

    private suspend fun makeApiCall(urlString: String): String? {
        var result: String? = null
        val url = URL(urlString)
        val urlConnection: HttpURLConnection = url.openConnection() as HttpURLConnection

        try {
            urlConnection.requestMethod = "GET"
            urlConnection.connectTimeout = 5000
            urlConnection.readTimeout = 5000

            val responseCode = urlConnection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val stream = BufferedReader(InputStreamReader(urlConnection.inputStream))
                val sb = StringBuilder()
                var line: String?

                while (stream.readLine().also { line = it } != null) {
                    sb.append(line).append("\n")
                }
                stream.close()
                result = sb.toString()
            } else {
                result = "Error: $responseCode"
            }
        } catch (e: Exception) {
            e.printStackTrace()
            result = e.message
        } finally {
            urlConnection.disconnect()
        }

        return result
    }

    private fun parseJson(json: String?): List<Post> {
        val gson = Gson()
        val listType = object : TypeToken<List<Post>>() {}.type
        return gson.fromJson(json, listType)
    }
}
