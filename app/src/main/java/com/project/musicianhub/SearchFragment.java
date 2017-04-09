package com.project.musicianhub;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import com.project.musicianhub.adapter.SearchAdapter;
import com.project.musicianhub.model.Search;
import com.project.musicianhub.service.SearchServiceImpl;
import com.project.musicianhub.util.SearchCustomTouchListener;
import com.project.musicianhub.util.SessionManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


/**
 * Search Fragment class which controls the search tasks of the user
 *
 * @author Akash Rajbanshi
 * @since 1.0
 */
public class SearchFragment extends Fragment {

    //recycler view
    RecyclerView recyclerView;
    //search adapter
    SearchAdapter searchAdapter;
    //search list
    private List<Search> searchList;
    //search service
    SearchServiceImpl searchService;
    //search edit text
    EditText searchEditText;
    //user id
    int id;
    //clear search text view
    TextView clearSearchTxtView;
    //empty search textview
    TextView emptySearch;

    public SearchFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        final View view = inflater.inflate(R.layout.fragment_search, container, false);

        emptySearch = (TextView) view.findViewById(R.id.empty_search);

        //UI initialization of the search fragment
        clearSearchTxtView = (TextView) view.findViewById(R.id.clear_search_history_textView);

        //session initialization and getting the session data
        SessionManager session = new SessionManager(view.getContext());
        final HashMap<String, Integer> sessionUserId = session.getUserDetails();
        final int sesUserId = sessionUserId.get(SessionManager.KEY_ID);

        //initialize the recylcer view
        recyclerView = (RecyclerView) view.findViewById(R.id.search_recycler_view);
        searchList = new ArrayList<>();
        searchAdapter = new SearchAdapter(view.getContext(), searchList);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(view.getContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(searchAdapter);

        //hide the clear search textview if the search list is empty
        if (searchList.size() == 0)
            clearSearchTxtView.setVisibility(View.GONE);

        //get the search history for the user
        searchService = new SearchServiceImpl();
        searchService.getSearchHistory(searchList, searchAdapter, view.getContext(), sesUserId, clearSearchTxtView, emptySearch, recyclerView);

        //sets the custom on item touch listener on recycler view
        recyclerView.addOnItemTouchListener(new SearchCustomTouchListener(view.getContext(), recyclerView, new SearchFragment.ClickListener() {
            @Override
            public void onClick(View view, int position) {
                //gets the searchlist position and adds it to search history
                Search search = searchList.get(position);
                searchService.addSearchHistory(view.getContext(), search);
                //starts the music post activity showing single post view
                Intent intent = new Intent(view.getContext(), MusicPostActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra("musicId", search.getMusicId());
                startActivity(intent);
            }

            @Override
            public void onLongClick(View view, int position) {

            }
        }));


        searchEditText = (EditText) view.findViewById(R.id.clearable_edit);
        searchEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                //performs search once the search button has been pressed in soft keyboard
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    performSearch();
                    return true;
                }
                return false;
            }
        });

        //searches for the music according to the text change in the search edit text
        searchEditText.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!searchEditText.getText().toString().equals("")) {
                    //directly searches for the music without any other tasks such as hiding of soft keyboard
                    performSearchWithCurrentContext();
                } else {
                    //gets the search history once the edit text is empty
                    searchService.getSearchHistory(searchList, searchAdapter, view.getContext(), sesUserId, clearSearchTxtView, emptySearch, recyclerView);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                searchList.clear();
                searchAdapter.notifyDataSetChanged();
            }


        });

        //asks the user for the confirmation for clearing the search history
        clearSearchTxtView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(view.getContext())
                        .setMessage("Are you sure you want to clear the search history?")
                        .setCancelable(false)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                searchService.deleteSearchHistory(searchList, searchAdapter, view.getContext(), clearSearchTxtView, emptySearch, recyclerView);
                            }
                        })
                        .setNegativeButton("No", null)
                        .show();
            }
        });


        return view;
    }

    /**
     * Performs Search once the soft keyboard is hidden
     *
     * @author Akash Rajbashi
     * @since 1.0
     */
    private void performSearch() {
        searchEditText.clearFocus();
        InputMethodManager in = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        in.hideSoftInputFromWindow(searchEditText.getWindowToken(), 0);
        performSearchWithCurrentContext();

    }

    /**
     * Performs search using the search edit text value
     *
     * @author Akash Rajbashi
     * @since 1.0
     */
    private void performSearchWithCurrentContext() {
        searchService.searchMusic(searchList, searchAdapter, this.getContext(), searchEditText.getText().toString(), emptySearch, recyclerView);
    }

    /**
     * Interface for the reclycler view touch events
     *
     * @author Akash Rajbashi
     * @since 1.0
     */
    public interface ClickListener {
        void onClick(View view, int position);

        void onLongClick(View view, int position);
    }
}
