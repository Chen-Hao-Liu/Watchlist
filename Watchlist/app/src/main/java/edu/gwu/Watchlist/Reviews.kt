package edu.gwu.Watchlist
import java.io.Serializable

data class Review(
    val email : String,
    val score : String,
    val review : String
): Serializable {
    constructor() : this("", "", "")
}