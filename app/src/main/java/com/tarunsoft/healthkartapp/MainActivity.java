

package com.tarunsoft.healthkartapp;

import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;

import com.android.volley.Cache;
import com.android.volley.Cache.Entry;
import com.android.volley.Request.Method;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.tarunsoft.actionbar.ActionBarActivity;
import com.tarunsoft.healthkartapp.adapter.SwipeListAdapter;
import com.tarunsoft.healthkartapp.app.AppController;
import com.tarunsoft.healthkartapp.modal.FeedItem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


/*
 * MainActivity extending ActionBarActivity for header custom view 
 * implementing OnRefreshListener listener to handle SwipeRefreshLayout refresh event 
 * to get latest product feeds 
 * 
 */


public class MainActivity extends ActionBarActivity implements SwipeRefreshLayout.OnRefreshListener {


    private String TAG = MainActivity.class.getSimpleName();
    private String searchtxt = "";


    private SwipeRefreshLayout swipeRefreshLayout;
    private ListView listView;
    private ProgressDialog dialog;
    private SwipeListAdapter adapter;
    private boolean calloutflag = false;
    private int mycount = 0;
    private List<FeedItem> feeditems;
    private String URL_FEED = "http://api.healthkart.com/api/search/results/?";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /*set main view to page */
        setContentView(R.layout.activity_main);

		/*get the list and add SwipeRefreshLayout as adapter to list  */
        listView = (ListView) findViewById(R.id.list);
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);

		/*create Array list object to store json data item to list */
        feeditems = new ArrayList<FeedItem>();
		/*assign list to adaptor*/
        adapter = new SwipeListAdapter(this, feeditems);
        listView.setAdapter(adapter);

		/*Initially set setOnRefreshListener to true while loading data will show progress in action  and customize progress view 
		 * by using some custom colors 
		 * */

        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setColorSchemeResources(R.color.gray,
                R.color.green,
                R.color.red,
                R.color.yello);

		/*OnScrollListener is added to list  to check if we are on the bottom of page or not 
		 * if page bottom reached fire call to API with change in parameter
		 * increase page no count by 1 
		 * @fire rest call 
		 * @change the url 
		 */
        listView.setOnScrollListener(new OnScrollListener() {

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }
			/*
			 * onScroll event will be fired while page scroll 
			 * track the event till page bottom is reached and again fire rest call
			 * */

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem,
                                 int visibleItemCount, int totalItemCount) {

                mycount++;
                //reset page counter
                if (mycount > 40) {
                    mycount = 1;
                }

                //increase page no count
                if ((firstVisibleItem + visibleItemCount) == totalItemCount) {


                    calloutflag = false;
                    swipeRefreshLayout.setRefreshing(true);
                    //if we are searching for something then fire call for it
                    if (searchtxt != null && searchtxt.length() > 0) {
                        System.out.println("searchtxt coming with leangth" + searchtxt.length());
                        fetchFeeds(getAPIURL(searchtxt, mycount));
                    } else {
                        // if we have empty search txt then we fire default call
                        //fire call by just increasing page no count

                        fetchFeeds(getAPIURL("protin", mycount));
                    }

                }
            }


        });


        /**
         * Showing Swipe Refresh animation on activity create
         * As animation won't start on onCreate, post runnable is used
         */
        swipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {

                swipeRefreshLayout.setRefreshing(true);
                dialog = new ProgressDialog(MainActivity.this);
                dialog.setMessage("Loading data...");
                dialog.show();
                fetchFeeds(getAPIURL("protin", 1));
                // new AsyncListViewLoader().execute("http://api.androidhive.info/feed/feed.json");
            }
        });

    }

    /**
     * This method is called when swipe refresh is pulled down
     * this is just to fire same call again to get updated data
     * it will get data back from cache if already exist
     */
    @Override
    public void onRefresh()

    {
        swipeRefreshLayout.setRefreshing(true);
        calloutflag = false;

        if (searchtxt != null && searchtxt.length() > 0) {
            System.out.println("searchtxt coming with leangth" + searchtxt.length());
            fetchFeeds(getAPIURL(searchtxt, mycount));
        } else {

            fetchFeeds(getAPIURL("protin", mycount));
        }
    }

    private void fetchFeeds(String url) {

        Cache cache = AppController.getInstance().getRequestQueue().getCache();
        Entry entry = cache.get(url);
        if (entry != null) {
            // fetch the data from cache if avilable
            System.out.println("getting data from cache..");
            try {
                String data = new String(entry.data, "UTF-8");
                try {
                    parseJsonFeed(new JSONObject(data));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

        } else {
            // making fresh  request and getting json
            JsonObjectRequest jsonReq = new JsonObjectRequest(Method.GET,
                    url, null, new Response.Listener<JSONObject>() {

                @Override
                public void onResponse(JSONObject response) {
                    VolleyLog.d(TAG, "Response: " + response.toString());
                    if (response != null) {
                        // fire call for json parse
                        parseJsonFeed(response);
                    }
                }
            }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {
                    VolleyLog.d(TAG, "Error: " + error.getMessage());
                }
            });

            // Adding request to volley request queue
            AppController.getInstance().addToRequestQueue(jsonReq);
        }
    }

	/*caching  mechanism for HTTP REST  call 
	 * using party API to store REST API calls in cache 
	 * @ AppController singleton class to return cached  resource
	 * @ if cache data is available for REST URL call then use same and don't fire call  to REST API
	 * 
	 */

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();

        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setIconifiedByDefault(false);

        SearchView.OnQueryTextListener textChangeListener = new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextChange(String newText) {
                // this is your adapter that will be filtered

                System.out.println("on text chnge text: " + newText);
                return true;
            }

            @Override
            public boolean onQueryTextSubmit(String query) {
                // this is your adapter that will be filtered
                // start progress animation
                //@reset the page counter on new txt serch
                // calloutflag to check if we need to flush the list items or not

                swipeRefreshLayout.setRefreshing(true);
                mycount = 1;
                System.out.println("on query submit: " + query.toString());
                searchtxt = query.toString();
                calloutflag = true;

                fetchFeeds(getAPIURL(searchtxt, mycount));
                return true;
            }
        };
        searchView.setOnQueryTextListener(textChangeListener);

        return super.onCreateOptionsMenu(menu);

    }

	/*added  searchManager for search of different products using API call*
	 * @see com.tarunsoft.actionbar.ActionBarActivity#onCreateOptionsMenu(android.view.Menu)
	 * @ onQueryTextSubmit when user click on submit after entering txt data 
	 * @ Now fire a call and fetch json data based on search item
	 */

    private String getAPIURL(String query, int pagecount) {
        return URL_FEED + "txtQ=" + query + "&pageNo=" + pagecount + "&perPage=5&st=1";

    }

    private void parseJsonFeed(JSONObject response) {
        try {

            if (feeditems.size() > 0 && feeditems != null && calloutflag == true) {
                feeditems.clear();

            }

            JSONObject jsonobj = (JSONObject) response.getJSONObject("results");
            JSONArray feedArray = jsonobj.getJSONArray("variants");

            for (int i = 0; i < feedArray.length(); i++) {
                JSONObject feedObj = (JSONObject) feedArray.get(i);


                JSONObject imgobject = (JSONObject) feedObj.getJSONObject("m_img");

                FeedItem item = new FeedItem();
                item.setId(feedObj.getInt("id"));
                item.setName(feedObj.getString("nm"));


                // Image might be null sometimes
                String image = imgobject.isNull("s_link") ? null : imgobject.getString("s_link");
                item.setImage(image);
                item.setStatus(feedObj.getString("catName"));
                item.setProfilePic(imgobject.getString("t_link"));
                item.setBrandName(feedObj.getString("brName"));
                item.setBrandInstName(feedObj.getString("brIntName"));
                item.setBrandCategory(feedObj.getString("catName"));

                // url might be null sometimes
                String feedUrl = feedObj.isNull("url") ? null : feedObj
                        .getString("url");
                if (dialog != null) {
                    dialog.dismiss();
                }
                item.setUrl(feedUrl);


                feeditems.add(item);
            }

            // notify data changes to list adapater
            swipeRefreshLayout.setRefreshing(false);
            adapter.notifyDataSetChanged();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

	/*@parse JSON data using getJSONObject to get JSONObject and getJSONArray to retrieve array 
	 * @ flush list item if its new request 
	 * @ fetch json data and add it to feeditems
	 * @ once data received set progress animation to false @swipeRefreshLayout 
	 **/

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Toast.makeText(this, "tapped...", Toast.LENGTH_SHORT).show();
                break;
            case R.id.menu_refresh:
                Toast.makeText(this, "refreshing...", Toast.LENGTH_SHORT)
                        .show();
                break;
            case R.id.menu_share:
                Toast.makeText(this, "Tapped share", Toast.LENGTH_SHORT).show();
                break;
            case R.id.menu_about:
                Intent myintent3 = new Intent();
                myintent3.setAction("android.intent.action.ABOUT");
                startActivity(myintent3);
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Fetching product json by making http call to API
     *
     * @AsyncListViewLoader to fecth data from async call
     */


    private class AsyncListViewLoader extends AsyncTask<String, Void, List<FeedItem>> {
        private final ProgressDialog dialog = new ProgressDialog(MainActivity.this);

        @Override
        protected void onPostExecute(List<FeedItem> result) {
            super.onPostExecute(result);
            dialog.dismiss();
            swipeRefreshLayout.setRefreshing(false);
            adapter = new SwipeListAdapter(MainActivity.this, result);
            listView.setAdapter(adapter);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog.setMessage("Downloading Movies...");
            dialog.show();
        }

        @Override
        protected List<FeedItem> doInBackground(String... params) {
            List<FeedItem> result = new ArrayList<FeedItem>();

            try {
                URL u = new URL(params[0]);

                HttpURLConnection conn = (HttpURLConnection) u.openConnection();
                conn.setRequestMethod("GET");

                conn.connect();
                InputStream is = conn.getInputStream();

                // Read the stream
                byte[] b = new byte[1024];
                ByteArrayOutputStream baos = new ByteArrayOutputStream();

                while (is.read(b) != -1)
                    baos.write(b);
                String JSONResp = new String(baos.toByteArray());
                JSONObject jsonobj = new JSONObject(JSONResp);

                try {
                    JSONArray feedArray = jsonobj.getJSONArray("feed");

                    for (int i = 0; i < feedArray.length(); i++) {
                        JSONObject feedObj = (JSONObject) feedArray.get(i);

                        FeedItem item = new FeedItem();
                        item.setId(feedObj.getInt("id"));
                        // Image might be null sometimes
                        String image = feedObj.isNull("image") ? null : feedObj
                                .getString("image");
                        item.setImage(image);
                        item.setStatus(feedObj.getString("status"));

                        item.setBrandName(feedObj.getString("brName"));
                        item.setBrandInstName(feedObj.getString("brIntName"));
                        item.setBrandCategory(feedObj.getString("catName"));
                        // url might be null sometimes
                        String feedUrl = feedObj.isNull("url") ? null : feedObj
                                .getString("url");
                        item.setUrl(feedUrl);

                        feeditems.add(item);
                    }
                    // notify data changes to list adapater
                    swipeRefreshLayout.setRefreshing(false);
                    adapter.notifyDataSetChanged();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return result;
            } catch (Throwable t) {
                t.printStackTrace();
            }
            return null;
        }

    }


}


