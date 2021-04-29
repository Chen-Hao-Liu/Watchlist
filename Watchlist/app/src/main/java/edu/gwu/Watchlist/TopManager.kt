package edu.gwu.Watchlist

import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONObject

class TopManager {
    // The OkHttpClient will facilitate the complexities of networking with the server
    val okHttpClient: OkHttpClient

    // An init block allows us to do extra logic during class initialization
    init {
        val builder = OkHttpClient.Builder()
        // Set our networking client up to log all requests & responses to console
        val logging = HttpLoggingInterceptor()
        logging.level = HttpLoggingInterceptor.Level.BODY
        builder.addInterceptor(logging)
        okHttpClient = builder.build()
    }

    fun retrieveSources(media: String, subType: String, page: Int): List<Source> {
        // dynamically form URL based on what parameters are available
        var myUrl = "https://api.jikan.moe/v3/top/$media/$page"

        if(subType != "general" && subType != "en general"){
            myUrl += "/$subType"
        }

        Log.d("TopManager URL: ", myUrl)
        // Either search by category or search by all
        val request =
            Request.Builder()
                .get()
                .url(myUrl)
                .build()

        val response: Response = okHttpClient.newCall(request).execute()
        val sourceList = mutableListOf<Source>()
        val responseBody = response.body?.string()

        if (response.isSuccessful && !responseBody.isNullOrBlank()){
            // Parse JSON body into Source objects
            val json = JSONObject(responseBody)
            val sources = json.getJSONArray("top")

            for (i in 0 until sources.length()) {
                val curr = sources.getJSONObject(i)
                // Since anime and manga share different parameters, first initialize shared parameters
                var mal_id = curr.getString("mal_id")
                var rank = curr.getString("rank")
                var title = curr.getString("title")
                var url = curr.getString("url")
                var image_url = curr.getString("image_url")
                var type = curr.getString("type")
                var start_date = curr.getString("start_date")
                var end_date = curr.getString("end_date")
                var members = curr.getString("members")
                var score = curr.getString("score")
                var volumes = ""
                var episodes = ""

                // insert manga or anime specific parameters
                if(media == "manga"){
                    volumes = curr.getString("volumes")
                }else{
                    episodes = curr.getString("episodes")
                }

                sourceList.add(
                    Source(
                        mal_id = mal_id,
                        url = url,
                        image_url = image_url,
                        title = title,
                        airing = "",
                        publishing = "",
                        synopsis = "",
                        type = type,
                        episodes = episodes,
                        chapters = "",
                        volumes = volumes,
                        score = score,
                        start_date = start_date,
                        end_date = end_date,
                        members = members,
                        rated = "",
                        userScore = "N/A",
                        userReview = "",
                        rank = rank
                    )
                )
            }
        }

        return sourceList
    }
}