package com.project.musicianhub.service;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.project.musicianhub.adapter.SearchAdapter;
import com.project.musicianhub.model.Search;
import com.project.musicianhub.singleton.Singleton;
import com.project.musicianhub.util.SessionManager;
import com.project.musicianhub.util.VolleyUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Search Service implementation class
 *
 * @author Akash Rajbanshi
 * @since 1.0
 */

public class SearchServiceImpl implements ISearchService {

    private static final String SEARCH_URL = "http://192.168.0.13:8080/musicianhub/webapi/music/search/";
    private static final String SEARCH_HISTORY_URL = "http://192.168.0.13:8080/musicianhub/webapi/users/";

    /**
     * adds the search history to the list
     *
     * @param context current context
     * @param search  search object
     */
    @Override
    public void addSearchHistory(Context context, Search search) {
        String url = SEARCH_HISTORY_URL + search.getUserId() + "/searchHistory/createSearchHistory";
        JSONObject params = new JSONObject();
        try {
            JSONObject musicParam = new JSONObject();
            musicParam.put("id", search.getMusicId());
            params.put("music", musicParam);
        } catch (JSONException e) {
            e.printStackTrace();
        }


        JsonObjectRequest postRequest = new JsonObjectRequest(Request.Method.POST, url, params,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                    }
                },
                new Response.ErrorListener()

                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                    }
                });
        VolleyUtil.setRetryPolicyForVolley(postRequest);
        Singleton.getmInstance(context.getApplicationContext()).addToRequestQueue(postRequest);
    }

    /**
     * search music and updates the search list
     *
     * @param searchList    search list
     * @param searchAdapter search adapter
     * @param context       current context
     * @param searchText    search text
     * @param emptySearch   empty search textview
     * @param recyclerView  recyclerview object
     */
    @Override
    public void searchMusic(final List<Search> searchList, final SearchAdapter searchAdapter, final Context context, String searchText, final TextView emptySearch, final RecyclerView recyclerView) {

        final StringRequest stringRequest = new StringRequest(Request.Method.GET, SEARCH_URL + searchText, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONArray jsonArray = new JSONArray(response);
                    int length = jsonArray.length();
                    if (length != 0) {
                        searchList.clear();
                        searchAdapter.notifyDataSetChanged();
                        for (int i = 0; i < jsonArray.length(); i++) {

                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            int id = jsonObject.getInt("id");
                            String title = jsonObject.getString("title");
                            String imagePath = jsonObject.getString("album_art_path");


                            JSONObject userObj = jsonObject.getJSONObject("user");


                            String userName = userObj.getString("name");

                            SessionManager session = new SessionManager(context.getApplicationContext());
                            final HashMap<String, Integer> sessionUserId = session.getUserDetails();
                            final int sesUserId = sessionUserId.get(SessionManager.KEY_ID);

                            Search search = new Search(title, userName, imagePath, id);
                            search.setUserId(sesUserId);
                            searchList.add(search);
                            searchAdapter.notifyDataSetChanged();
                        }
                    } else {
                        searchList.clear();
                        searchAdapter.notifyDataSetChanged();
                    }

                    if (searchList != null) {
                        if (searchList.size() > 0) {
                            emptySearch.setVisibility(View.GONE);
                            recyclerView.setVisibility(View.VISIBLE);
                        } else {
                            emptySearch.setVisibility(View.VISIBLE);
                            recyclerView.setVisibility(View.GONE);
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        });
        VolleyUtil.setRetryPolicyForVolley(stringRequest);
        Singleton.getmInstance(context.getApplicationContext()).addToRequestQueue(stringRequest);
    }

    /**
     * gets the search history list
     *
     * @param searchList         search list
     * @param searchAdapter      search adapter
     * @param context            current context
     * @param id                 user id
     * @param clearSearchTxtView clear search text view
     * @param recyclerView       recyclerview object
     */
    @Override
    public void getSearchHistory(final List<Search> searchList, final SearchAdapter searchAdapter, final Context context, int id, final TextView clearSearchTxtView, final TextView emptySearch, final RecyclerView recyclerView) {
        String url = SEARCH_HISTORY_URL + id + "/searchHistory/allSearchHistory";
        final StringRequest stringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONArray jsonArray = new JSONArray(response);
                    int length = jsonArray.length();
                    if (length != 0) {
                        if (searchList != null) {
                            searchList.clear();
                            searchAdapter.notifyDataSetChanged();
                        }
                        for (int i = 0; i < jsonArray.length(); i++) {

                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            int id = jsonObject.getInt("id");
                            String title = jsonObject.getString("title");
                            String imagePath = jsonObject.getString("album_art_path");


                            JSONObject userObj = jsonObject.getJSONObject("user");


                            String userName = userObj.getString("name");


                            SessionManager session = new SessionManager(context.getApplicationContext());
                            final HashMap<String, Integer> sessionUserId = session.getUserDetails();
                            final int sesUserId = sessionUserId.get(SessionManager.KEY_ID);

                            Search search = new Search(title, userName, imagePath, id);
                            search.setUserId(sesUserId);
                            searchList.add(search);
                            searchAdapter.notifyDataSetChanged();
                            clearSearchTxtView.setVisibility(View.VISIBLE);
                        }
                    } else {
                        searchList.clear();
                        searchAdapter.notifyDataSetChanged();
                    }

                    if (searchList != null) {
                        if (searchList.size() > 0) {
                            emptySearch.setVisibility(View.GONE);
                            recyclerView.setVisibility(View.VISIBLE);
                        } else {
                            emptySearch.setVisibility(View.VISIBLE);
                            recyclerView.setVisibility(View.GONE);
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        });
        VolleyUtil.setRetryPolicyForVolley(stringRequest);
        Singleton.getmInstance(context.getApplicationContext()).addToRequestQueue(stringRequest);
    }

    /**
     * deletes the search history list
     *
     * @param searchList         search list
     * @param searchAdapter      search adapter
     * @param context            current context
     * @param clearSearchTxtView clear search text view
     * @param recyclerView       recylcer text view
     */
    @Override
    public void deleteSearchHistory(final List<Search> searchList, final SearchAdapter searchAdapter, final Context context, final TextView clearSearchTxtView, final TextView emptySearch, final RecyclerView recyclerView) {

        SessionManager session = new SessionManager(context.getApplicationContext());
        final HashMap<String, Integer> sessionUserId = session.getUserDetails();
        final int sesUserId = sessionUserId.get(SessionManager.KEY_ID);

        String url = SEARCH_HISTORY_URL + sesUserId + "/searchHistory/clearSearchHistory";
        final StringRequest stringRequest = new StringRequest(Request.Method.DELETE, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                //clears the search list once the search history is deleted
                searchList.clear();
                searchAdapter.notifyDataSetChanged();
                clearSearchTxtView.setVisibility(View.GONE);

                emptySearch.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        }) {

            /**
             * Passing some request headers
             */
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Content-Type", "application/json; charset=utf-8");
                return headers;
            }


        };
        VolleyUtil.setRetryPolicyForVolley(stringRequest);
        Singleton.getmInstance(context.getApplicationContext()).addToRequestQueue(stringRequest);
    }
}
