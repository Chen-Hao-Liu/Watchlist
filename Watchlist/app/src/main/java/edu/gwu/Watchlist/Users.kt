package edu.gwu.Watchlist
import java.io.Serializable

data class User(
    val uid : String,
    val email : String
): Serializable {
    constructor() : this("", "")
}