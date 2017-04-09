package com.project.musicianhub.service;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.widget.TextView;

import com.project.musicianhub.adapter.SearchAdapter;
import com.project.musicianhub.model.Search;

import java.util.List;

/**
 * Search Service interface
 *
 * @author Akash Rajbanshi
 * @since 1.0
 */

public interface ISearchService {

    /**
     * adds the search history to the list
     *
     * @param context
     * @param search
     * @author Akash Rajbanshi
     * @since 1.0
     */
    public void addSearchHistory(Context context, Search search);

    /**
     * search music and updates the search list
     *
     * @param searchList
     * @param searchAdapter
     * @param context
     * @param searchText
     * @param emptySearch
     *@param recyclerView @author Akash Rajbanshi
     * @since 1.0
     */
    public void searchMusic(List<Search> searchList, SearchAdapter searchAdapter, Context context, String searchText, TextView emptySearch, RecyclerView recyclerView);

    /**
     * gets the search history list
     *
     * @param searchList
     * @param searchAdapter
     * @param context
     * @param id
     * @param clearSearchTxtView
     *@param recyclerView @author Akash Rajbanshi
     * @since 1.0
     */
    public void getSearchHistory(List<Search> searchList, SearchAdapter searchAdapter, Context context, int id, TextView clearSearchTxtView, final TextView emptySearch, RecyclerView recyclerView);

    /**
     * deletes the search history list
     *
     * @param searchList
     * @param searchAdapter
     * @param context
     * @param clearSearchTxtView
     *@param recyclerView @author Akash Rajbanshi
     * @since 1.0
     */
    public void deleteSearchHistory(List<Search> searchList, SearchAdapter searchAdapter, Context context, TextView clearSearchTxtView, final TextView emptySearch, RecyclerView recyclerView);


}
