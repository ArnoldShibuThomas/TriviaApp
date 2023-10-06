package com.arnold.triviaapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {
    // This will  hold the links to categories
    private String categoriesApi = "https://opentdb.com/api_category.php";
    // create a listview
    private ListView categories;
    // create an arraylist of them
    private Button refresh;
    // get the search bar
    private EditText searchBar;
    // create a hash map
    private HashMap<String,Integer> items = new HashMap<String,Integer>();
    // This will store the finalize list
    private ArrayList<String> searchableList= new ArrayList<>();

    private ArrayList<String> categoriesFound;

    private final LoadingDialog loadingDialog = new LoadingDialog(MainActivity.this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // start loading
        loadingDialog.startLoadingDialog();
        // initialize variables
        categories = findViewById(R.id.categories);
        refresh = findViewById(R.id.refreshButton);
        searchBar = findViewById(R.id.findItem);
        categoriesFound = new ArrayList<>();
        // on the below line we are initializing the adapter for our list view.
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, categoriesFound);
        // on below line we are setting adapter for our list view.
        categories.setAdapter(adapter);
        // get all of the categories
        getCategories(adapter);
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                loadingDialog.dismissDialog();
            }
        }, 500);
        // create a Text-watcher on the search bard
        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // if the user types in something search for what they typed
                if (s.toString().trim().length() != 0) {
                    searchForWord(s.toString(), adapter);
                }
                else{
                    // If the user makes the the search empty bring back everything
                    refresh(adapter);
                }
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // if the user types in something search for what they typed
                searchForWord(s.toString().trim(), adapter);
            }

            @Override
            public void afterTextChanged(Editable s) {
                // if the user types in something search for what they typed
                if (s.toString().trim().length() != 0) {
                    searchForWord(s.toString().trim(), adapter);
                }
                else{
                    // If the user makes the the search empty bring back everything
                    refresh(adapter);
                }
            }
        });
        // make the refresh button look up categories again
        refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // start loading
                loadingDialog.startLoadingDialog();
                // clear the search
                searchBar.getText().clear();
                // refresh the list of items to have everything again
                refresh(adapter);
                // make user wait
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        loadingDialog.dismissDialog();
                    }
                }, 500);
            }
        });
        //handle the click events in the list view
        categories.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // go to the question page with the id of the selected option
                int idOfCategory = items.get(categoriesFound.get(position));
                // create a new intent
                Intent intent = new Intent(MainActivity.this,Questions.class);
                // give the new intent the id the user has chosen as a string
                intent.putExtra("id","" + idOfCategory);
                // start the new activity
                startActivity(intent);
            }
        });
    }

    // This is the refresh method which will reset the list view to all the list items
    private void refresh(ArrayAdapter<String> adapter) {
        // clear the list
        categoriesFound.clear();
        // insert everything back into list
        for(int i = 0; i <searchableList.size(); i++){
            categoriesFound.add(searchableList.get(i));
        }
        // notify the adapter of the change
        adapter.notifyDataSetChanged();
    }

    // This wil get the categories the user ca select from
    private void getCategories(ArrayAdapter<String> adapter) {
        // getting a new volley request queue for making new requests
        RequestQueue volleyQueue = Volley.newRequestQueue(MainActivity.this);
        // url of the api through which we get the categories
        String url = categoriesApi;
        // clear the list
        categoriesFound.clear();

        // since the response we get from the api is in JSON, we need to use `JsonObjectRequest` for parsing the request response
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                // when the HTTP request succeeds
                (Response.Listener<JSONObject>) response -> {
                    try {
                        // clear hashmap
                        items = new HashMap<String,Integer>();
                        // lets index all the categories
                        int i = 0;
                        // get the json object
                        JSONArray trivia_categories = response.getJSONArray("trivia_categories");
                        // loop through and get all of the  categories
                        while(i < trivia_categories.length()){
                            // get one category and add it to the triavia list
                            JSONObject here = (JSONObject) trivia_categories.get(i);
                            // add the category name
                            categoriesFound.add((String) here.get("name"));
                            // only update if this is the first time
                            // this will add it to the global list
                            searchableList.add((String) here.get("name"));
                            // Store key values in hashmaps
                            items.put((String) here.get("name"),(Integer) here.get("id"));
                            // increment i
                            i++;
                        }
                        // notify the adapter of the chane
                        adapter.notifyDataSetChanged();
                    } catch (JSONException e) {
                        Toast.makeText(this, "It looks like the servers may be down try again", Toast.LENGTH_SHORT).show();
                    }
                },
                // when the HTTP request fails
                (Response.ErrorListener) error -> {
                    // make a Toast telling the user that something went wrong
                    Toast.makeText(MainActivity.this, "Error Occurred during load", Toast.LENGTH_LONG).show();
                }
        );
        // to the Volley request queue
        volleyQueue.add(jsonObjectRequest);
    }

    // This will be the basis for a simple search
    private  void searchForWord(String wordOrPhrase, ArrayAdapter<String> adapter){
        // clear the categories found
        categoriesFound.clear();
        // look for all the categories containing the String the user is searching for
        for(int i = 0; i < searchableList.size(); i++){
            if(searchableList.get(i).contains(wordOrPhrase)){
                // if the String contains the substring the user is searching for then do this
                categoriesFound.add(searchableList.get(i));
            }
        }
        // change the data to match
        adapter.notifyDataSetChanged();
    }
}