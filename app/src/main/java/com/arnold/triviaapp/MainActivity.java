package com.arnold.triviaapp;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Adapter;
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
    private HashMap<Integer,String> items = new HashMap<Integer,String>();
    // This will store the finalize list
    private ArrayList<String> searchableList= new ArrayList<>();

    private ArrayList<String> categoriesFound;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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
        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if (s.toString().trim().length() != 0) {
                    searchForWord(s.toString(), adapter);
                }
                else{
                    refresh(adapter);
                }
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchForWord(s.toString().trim(), adapter);
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().trim().length() != 0) {
                    searchForWord(s.toString().trim(), adapter);
                }
                else{
                    refresh(adapter);
                }
            }
        });
        // make the refresh button look up categories again
        refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchBar.getText().clear();
                refresh(adapter);

            }
        });
    }

    private void refresh(ArrayAdapter<String> adapter) {
        categoriesFound.clear();
        for(int i = 0; i <searchableList.size(); i++){
            categoriesFound.add(searchableList.get(i));
        }
        adapter.notifyDataSetChanged();
    }

    // This wil get the categories the user ca select from
    private void getCategories(ArrayAdapter<String> adapter) {
        // getting a new volley request queue for making new requests
        RequestQueue volleyQueue = Volley.newRequestQueue(MainActivity.this);
        // url of the api through which we get random dog images
        String url = categoriesApi;
        // clear the list
        categoriesFound.clear();

        // since the response we get from the api is in JSON, we need to use `JsonObjectRequest` for parsing the request response
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                // when the HTTP request succeeds
                (Response.Listener<JSONObject>) response -> {
                    try {
                        // clear hashmap
                        items = new HashMap<Integer,String>();
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
                            items.put((Integer) here.get("id"), (String) here.get("name"));
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
        categoriesFound.clear();
        for(int i = 0; i < searchableList.size(); i++){
            if(searchableList.get(i).contains(wordOrPhrase)){
                categoriesFound.add(searchableList.get(i));
            }
        }
        System.out.print(categoriesFound.size());
        // change the data to match
        adapter.notifyDataSetChanged();
    }
}