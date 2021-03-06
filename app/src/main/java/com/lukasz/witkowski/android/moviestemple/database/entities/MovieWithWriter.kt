package com.lukasz.witkowski.android.moviestemple.database.entities

import androidx.room.Entity


@Entity(primaryKeys = ["movieId", "writerId"])
data class MovieWithWriter(
     var movieId: Int,
     var writerId: Int
)