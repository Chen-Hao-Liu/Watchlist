package edu.gwu.Watchlist

import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONObject

class SourceManager {
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

    // For each genre category, return the corresponding id based on anime/manga
    fun sort(type: String, genre: String): String{
        if(type == "anime"){
            when (genre) {
                "Action" -> return "1"
                "Adventure" -> return "2"
                "Cars" -> return "3"
                "Comedy" -> return "4"
                "Dementia" -> return "5"
                "Demons" -> return "6"
                "Mystery" -> return "7"
                "Drama" -> return "8"
                "Ecchi" -> return "9"
                "Fantasy" -> return "10"
                "Game" -> return "11"
                "Hentai" -> return "12"
                "Historical" -> return "13"
                "Horror" -> return "14"
                "Kids" -> return "15"
                "Magic" -> return "16"
                "Martial Arts" -> return "17"
                "Mecha" -> return "18"
                "Music" -> return "19"
                "Parody" -> return "20"
                "Samurai" -> return "21"
                "Romance" -> return "22"
                "School" -> return "23"
                "Sci Fi" -> return "24"
                "Shoujo" -> return "25"
                "Shoujo Ai" -> return "26"
                "Shounen" -> return "27"
                "Shounen Ai" -> return "28"
                "Space" -> return "29"
                "Sports" -> return "30"
                "Super Power" -> return "31"
                "Vampire" -> return "32"
                "Yaoi" -> return "33"
                "Yuri" -> return "34"
                "Harem" -> return "35"
                "Slice of Life" -> return "36"
                "Supernatural" -> return "37"
                "Military" -> return "38"
                "Police" -> return "39"
                "Psychological" -> return "40"
                "Thriller" -> return "41"
                "Seinen" -> return "42"
                "Josei" -> return "43"
                else -> return "0"
            }
        } else{
            when (genre) {
                "Action" -> return "1"
                "Adventure" -> return "2"
                "Cars" -> return "3"
                "Comedy" -> return "4"
                "Dementia" -> return "5"
                "Demons" -> return "6"
                "Mystery" -> return "7"
                "Drama" -> return "8"
                "Ecchi" -> return "9"
                "Fantasy" -> return "10"
                "Game" -> return "11"
                "Hentai" -> return "12"
                "Historical" -> return "13"
                "Horror" -> return "14"
                "Kids" -> return "15"
                "Magic" -> return "16"
                "Martial Arts" -> return "17"
                "Mecha" -> return "18"
                "Music" -> return "19"
                "Parody" -> return "20"
                "Samurai" -> return "21"
                "Romance" -> return "22"
                "School" -> return "23"
                "Sci Fi" -> return "24"
                "Shoujo" -> return "25"
                "Shoujo Ai" -> return "26"
                "Shounen" -> return "27"
                "Shounen Ai" -> return "28"
                "Space" -> return "29"
                "Sports" -> return "30"
                "Super Power" -> return "31"
                "Vampire" -> return "32"
                "Yaoi" -> return "33"
                "Yuri" -> return "34"
                "Harem" -> return "35"
                "Slice of Life" -> return "36"
                "Supernatural" -> return "37"
                "Military" -> return "38"
                "Police" -> return "39"
                "Psychological" -> return "40"
                "Seinen" -> return "41"
                "Josei" -> return "42"
                "Doujinshi" -> return "43"
                "Gender Bender" -> return "44"
                "Thriller" -> return "45"
                else -> return "0"
            }
        }
    }

    fun retrieveSources(searchTerm: String, media: String, type: String, status: String, orderBy: String, genre: String, rated: String, page: Int): List<Source> {
        // dynamically form URL based on what parameters are available
        var myUrl = "https://api.jikan.moe/v3/search/$media?q=$searchTerm&page=$page"
        if(type != "Select Type" && type != "Selectionner le genre"){
            val append = "&type=$type"
            myUrl += append
        }
        if(status != "Select Status" && status != "Selectionnez le statut"){
            val append = "&status=$status"
            myUrl += append
        }
        if(orderBy != "Select Order By" && orderBy != "Ordre par hauteur"){
            val append = "&order_by=$orderBy"
            myUrl += append
        }
        if(genre != "Select Genre" && genre != "Selectionnez le genre"){
            val num = sort(media, genre)
            val append = "&genre=$num"
            myUrl += append
        }
        if(rated != "Select Rating" && rated != "Selectionnez une note"){
            val append = "&rated=$rated"
            myUrl += append
        }

        Log.d("SourceManager URL: ", myUrl)
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
            val sources = json.getJSONArray("results")

            for (i in 0 until sources.length()) {
                val curr = sources.getJSONObject(i)
                // Since anime and manga share different parameters, first initialize shared parameters
                var mal_id = curr.getString("mal_id")
                var url = curr.getString("url")
                var image_url = curr.getString("image_url")
                var title = curr.getString("title")
                var airing = ""
                var publishing = ""
                var synopsis = curr.getString("synopsis")
                var type = curr.getString("type")
                var episodes = ""
                var chapters = ""
                var volumes = ""
                var score = curr.getString("score")
                var start_date = curr.getString("start_date")
                var end_date = curr.getString("end_date")
                var members = curr.getString("members")
                var rated = ""

                // insert manga or anime specific parameters
                if(media == "anime"){
                    airing = curr.getString("airing")
                    episodes = curr.getString("episodes")
                    rated = curr.getString("rated")
                }else{
                    publishing = curr.getString("publishing")
                    chapters = curr.getString("chapters")
                    volumes = curr.getString("volumes")
                }

                sourceList.add(
                    Source(
                        mal_id = mal_id,
                        url = url,
                        image_url = image_url,
                        title = title,
                        airing = airing,
                        publishing = publishing,
                        synopsis = synopsis,
                        type = type,
                        episodes = episodes,
                        chapters = chapters,
                        volumes = volumes,
                        score = score,
                        start_date = start_date,
                        end_date = end_date,
                        members = members,
                        rated = rated,
                        userScore = "N/A",
                        userReview = "",
                        rank = ""
                    )
                )
            }
        }

        return sourceList
    }
}