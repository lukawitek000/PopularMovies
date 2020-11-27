package com.lukasz.witkowski.android.moviestemple.database

import android.util.Log
import androidx.paging.PagingSource
import androidx.room.*
import com.lukasz.witkowski.android.moviestemple.models.*
import com.lukasz.witkowski.android.moviestemple.models.entities.*

@Dao
interface MovieDao {


    @Query("SELECT * FROM Movies ORDER BY movieId DESC")
    fun getAllMoviesPagingSource(): PagingSource<Int, MovieEntity>

    @Query("SELECT * FROM Movies")
    suspend fun getAllMovies(): List<MovieEntity>


    @Transaction
    @Query("SELECT * FROM Movies WHERE movieId = :id")
    suspend fun getMovieWithVideosAndReviews(id: Int): MovieAllInformation


    @Query("SELECT EXISTS (SELECT 1 FROM Movies WHERE movieId = :id)")
    suspend fun isMovieInDatabase(id: Int): Boolean



    @Transaction
    suspend fun insert(movie: Movie){

        movie.cast.forEach {
            insertMovieWithActor(MovieWithActor(movie.id, it.actorId))
            insertActor(it)
        }

        movie.genres.forEach {
            insertMovieWithGenres(MovieWithGenre(movie.id, it.genreId))
            insertGenres(it)
        }

        insertMovie(movie.toMovieEntity())
        val videos = movie.videos
        videos.forEach {
            val videoEntity = it.toVideoEntity()
            videoEntity.movieOwnerID = movie.id.toLong()
            insertVideo(videoEntity)
        }
        val reviews = movie.reviews
        reviews.forEach {
            val reviewEntity = it.toReviewEntity()
            reviewEntity.movieOwnerID = movie.id.toLong()
            insertReview(reviewEntity)
        }


    }

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertActor(actor: Actor)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertMovieWithActor(movieWithActor: MovieWithActor)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertGenres(genre: Genre)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertMovieWithGenres(movieWithGenres: MovieWithGenre)


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReview(review: ReviewEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVideo(video: VideoEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMovie(movie: MovieEntity)


    @Transaction
    suspend fun deleteMovieReviewAndVideo(movie: MovieEntity){
        Log.i("Dao", "movie to delete = $movie")
        deleteMovieById(movie.movieId)
        deleteReviewByMovieOwnerId(movie.movieId)
        deleteVideoByMovieOwnerId(movie.movieId)
    }

    @Query("DELETE FROM Movies WHERE movieId = :id")
    suspend fun deleteMovieById(id: Int)

    @Query("DELETE FROM Reviews WHERE movieOwnerID = :id")
    suspend fun deleteReviewByMovieOwnerId(id: Int)

    @Query("DELETE FROM Videos WHERE movieOwnerID = :id")
    suspend fun deleteVideoByMovieOwnerId(id: Int)


    @Transaction
    suspend fun deleteAll(){
        deleteAllReviews()
        deleteAllVideos()
        deleteAllMovies()
    }

    @Query("DELETE FROM Movies")
    suspend fun deleteAllMovies()

    @Query("DELETE FROM Reviews")
    suspend fun deleteAllReviews()

    @Query("DELETE FROM Videos")
    suspend fun deleteAllVideos()
}