package com.lukasz.witkowski.android.moviestemple

import android.app.Application
import android.util.Log
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.lukasz.witkowski.android.moviestemple.api.*
import com.lukasz.witkowski.android.moviestemple.database.FavouriteMovieDatabase
import com.lukasz.witkowski.android.moviestemple.models.*
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.Flow

class MainRepository(application: Application) {


    companion object {
        private val LOCK = Any()
        @Volatile
        private var instance: MainRepository? = null
        fun getInstance(application: Application): MainRepository {
            return instance ?: synchronized(LOCK){
                MainRepository(application)
            }
        }
    }



    private val database = FavouriteMovieDatabase.getInstance(application.applicationContext)


    val favouriteMovies = Pager(
            config = PagingConfig(
                    pageSize = 20,
                    enablePlaceholders = false
            ),
            pagingSourceFactory = { database!!.movieDao().getAllMoviesPagingSource() }
    ).flow




    suspend fun getDetailInformation(movie: Movie): Movie{
        return withContext(IO) {
            if (isMovieInDatabase(movie.id)) {
                Log.i("Database debug", "movie details from database ")
                getMovieDetailsFromDatabase(movie.id)
            } else {
                Log.i("Database debug", "movie details from api ")
                getMovieDetailsFromApi(movie)
            }
        }
    }

    private suspend fun getMovieDetailsFromApi(movie: Movie): Movie {
        return withContext(IO) {
            val response = TMDBApi.retrofitService.getMovieDetailsVideosReviewsById(movieId = movie.id)
            Log.i("MainRepository", "detail response credits = ${response.credits}")
            movie.reviews  = response.reviews.results.map {
                it.toReview()
            }
            movie.videos = response.videos.results.map {
                it.toVideo()
            }
            movie.genres = response.genres
            val filmMakers: MutableList<Actor> = response.credits.cast.map {
                it.toActor()
            } as MutableList<Actor>
            val director = response.credits.crew.filter {
                it.job == "Director"
            }
            val writer = response.credits.crew.filter {
                it.job == "Writer"
            }
            val producer = response.credits.crew.filter {
                it.job == "Producer"
            }
            Log.i("MainRepo", "\ndirector = $director \n writer = $writer \n producer = $producer")
            /*filmMakers.addAll(response.credits.crew.map {
                it.toActor()
            })*/
            movie.directors = director.map {
                it.toDirector()
            }
            movie.writers = writer.map {
                it.toWriter()
            }

            movie.cast = filmMakers
            Log.i("MainRepo", "whole detail info $movie")

            movie
        }
    }

    private suspend fun getMovieDetailsFromDatabase(id: Int): Movie {
        return withContext(IO){
            val databaseEntity = database?.movieDao()?.getMovieWithVideosAndReviews(id)
            Log.i("Database debug", "movie details ${databaseEntity}")
            databaseEntity!!.toMovie()
        }
    }

    private suspend fun isMovieInDatabase(id: Int): Boolean {
        return withContext(IO) {
            database?.movieDao()?.isMovieInDatabase(id) ?: false
        }
    }


     fun isMovieInFavourites(id: Int): Boolean {
        return runBlocking {
            isMovieInDatabase(id)
        }
     }



    suspend fun deleteMovieFromDatabase(movie: Movie){
        withContext(IO){
            database!!.movieDao().deleteMovieReviewAndVideo(movie.toMovieEntity())
        }
    }


    suspend fun insertMovieToDatabase(movie: Movie){
        withContext(IO){
            database?.movieDao()?.insert(movie)
        }
    }





    private suspend fun getFavouriteMoviesId(): List<Int> {
        return withContext(IO){
            val listOfIds: List<Int> =  database!!.movieDao().getAllMovies().map {
                it.movieId
            }
            listOfIds
        }
    }

     fun getRecommendationsBasedOnFavouriteMovies(): Flow<PagingData<Movie>>{
         return runBlocking {
             val listOfIds: List<Int> = getFavouriteMoviesId()
             Pager(
                     config = PagingConfig(
                             pageSize = TMDB_PAGE_SIZE,
                             enablePlaceholders = false,
                             initialLoadSize = 2 * TMDB_PAGE_SIZE,
                             prefetchDistance = TMDB_PAGE_SIZE
                     ),
                     pagingSourceFactory = { MoviesPagingSource(RECOMMENDATIONS_QUERY, listOfIds) }
             ).flow
         }
    }



    suspend fun deleteAllFavouriteMovies(){
        withContext(IO){
            database?.movieDao()?.deleteAll()
        }
    }

    fun getPagingDataMovies(query: String): Flow<PagingData<Movie>> {
        return Pager(
                config = PagingConfig(
                        pageSize = TMDB_PAGE_SIZE,
                        enablePlaceholders = false,
                        initialLoadSize = 2 * TMDB_PAGE_SIZE,
                        prefetchDistance = TMDB_PAGE_SIZE
                ),
                pagingSourceFactory = { MoviesPagingSource(query) }
        ).flow
    }


}