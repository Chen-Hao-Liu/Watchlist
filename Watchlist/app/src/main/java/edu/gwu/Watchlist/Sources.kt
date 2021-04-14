package edu.gwu.Watchlist
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Source(
    val mal_id: String,
    val url: String,
    val image_url: String,
    val title: String,
    val airing: String,
    val publishing: String,
    val synopsis: String,
    val type: String,
    val episodes: String,
    val chapters: String,
    val volumes: String,
    val score: String,
    val start_date: String,
    val end_date: String,
    val members: String,
    val rated: String,
    var userScore: String,
    var userReview: String
): Parcelable {
    constructor() : this("", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "")
}