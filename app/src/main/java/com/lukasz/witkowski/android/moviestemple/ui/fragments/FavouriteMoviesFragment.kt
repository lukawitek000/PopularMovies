package com.lukasz.witkowski.android.moviestemple.ui.fragments

import android.os.Bundle
import android.view.*
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.paging.PagingData
import com.lukasz.witkowski.android.moviestemple.ui.MainActivity
import com.lukasz.witkowski.android.moviestemple.R
import com.lukasz.witkowski.android.moviestemple.ui.adapters.MoviesAdapter
import com.lukasz.witkowski.android.moviestemple.ui.dialogs.DeleteAllFavouriteMoviesDialogFragment
import com.lukasz.witkowski.android.moviestemple.models.Movie
import com.lukasz.witkowski.android.moviestemple.util.toMovie
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


class FavouriteMoviesFragment : BaseListMoviesFragment(), MoviesAdapter.MovieAdapterOnClickHandler{

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.movies_poster_list_layout, container, false)
        moviesAdapter = MoviesAdapter(this)
        setUpRecyclerView()
        retryOrRefreshList()
        initAdapter()
        setTextWhenMoviesAdapterIsEmpty(resources.getString(R.string.empty_favourite_movies_database_info))
        getFavouriteMovies()
        setHasOptionsMenu(true)
        return binding.root
    }

    private var job: Job? = null

    private fun getFavouriteMovies() {
        job?.cancel()
        job = lifecycleScope.launch {
            sharedViewModel.favouriteMovies.collectLatest { pagingData ->
                val movies: PagingData<Movie> = pagingData.mapSync {
                    it.toMovie()
                }
                moviesAdapter.submitData(movies)
            }
        }
    }


    override fun onMovieClick(movie: Movie) {
        sharedViewModel.selectMovie(movie)
        findNavController().navigate(R.id.action_favouriteMoviesFragment_to_detailInformationFragment)
        (activity as MainActivity).changeToolbarTitle(resources.getString(R.string.favourites_title))
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        return inflater.inflate(R.menu.favourite_fragment_menu, menu)
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean = when(item.itemId){
        R.id.delete_all_item -> {
            activity?.supportFragmentManager?.let { createAlertDialog().show(it, DeleteAllFavouriteMoviesDialogFragment.TAG) }
            true
        }
        else -> false
    }

    private fun createAlertDialog(): DeleteAllFavouriteMoviesDialogFragment {
        return DeleteAllFavouriteMoviesDialogFragment()
    }
}