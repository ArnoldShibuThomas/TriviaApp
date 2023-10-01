package com.arnold.triviaapp;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.commons.codec.binary.Base64;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class Questions extends AppCompatActivity {
    private TextView question;
    private Button b1;
    private Button b2;
    private Button b3;
    private Button b4;
    private TextView status;
    private Button nextQuestion;
    private int currentQuestion = 1;

    String categoryId;
    private String sessionKey;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_questions);
        // instantiate the variable
        question = findViewById(R.id.question);
        b1 = findViewById(R.id.option1);
        b2 = findViewById(R.id.option2);
        b3 = findViewById(R.id.option3);
        b4 = findViewById(R.id.option4);
        nextQuestion = findViewById(R.id.nextQuestion);
        // get the extra passed and set it as the category id
        categoryId = getIntent().getStringExtra("id");
        // check for null
        if(categoryId != null){
            // generate a session key for the user
            generateSessionKey();
            // get a question
            getQuestion();
        }
        else{
            Toast.makeText(this, "Nothing was given", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void getQuestion() {
        // getting a new volley request queue for making new requests
        RequestQueue volleyQueue = Volley.newRequestQueue(this);
        // url of the api through which we get api key
        String url = "https://opentdb.com/api.php?amount=1&category=" + categoryId+ "&encode=base64";
        // This will check if there is a session key
        if(sessionKey != null){
            // append the session key to append getting the same question
            url += "&token=" + sessionKey;
        }
        // since the response we get from the api is in JSON, we need to use `JsonObjectRequest` for parsing the request response
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                // when the HTTP request succeeds
                (Response.Listener<JSONObject>) response -> {
                    try {
                        // get the response code
                        int code = (int) response.get("response_code");
                        // check if the code is zero
                        if(code == 0){
                            // get the JSON Object of the question
                            JSONObject questionHere = (JSONObject) response.getJSONArray("results").get(0);
                            // get the string to decode
                            String qsDecoded = (String) questionHere.get("question");
                            // Decode data on other side, by processing encoded data
                            byte[] valueDecoded = Base64.decodeBase64(qsDecoded);
                            // set the text now
                            question.setText(new String(valueDecoded));
                            // get the questions
                        }
                        else{
                            Toast.makeText(this, "Session cannot be made", Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        Toast.makeText(this, "It looks like the servers may be down try again", Toast.LENGTH_SHORT).show();
                    }
                },
                // when the HTTP request fails
                (Response.ErrorListener) error -> {
                    // make a Toast telling the user that something went wrong
                    Toast.makeText(this, "Error Occurred during load", Toast.LENGTH_LONG).show();
                }
        );
        // to the Volley request queue
        volleyQueue.add(jsonObjectRequest);
    }

    private void generateSessionKey() {
        // getting a new volley request queue for making new requests
        RequestQueue volleyQueue = Volley.newRequestQueue(this);
        // url of the api through which we get api key
        String url = "https://opentdb.com/api_token.php?command=request";
        // since the response we get from the api is in JSON, we need to use `JsonObjectRequest` for parsing the request response
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                // when the HTTP request succeeds
                (Response.Listener<JSONObject>) response -> {
                    try {
                        // get the response code
                        int code = (int) response.get("response_code");
                        // check if the code is zero
                        if(code == 0){
                            // get the session key
                            sessionKey = (String) response.get("token");
                            // tell the user that we are good to go
                            Toast.makeText(this, "Session started..", Toast.LENGTH_SHORT).show();
                        }
                        else{
                            Toast.makeText(this, "Session cannot be made", Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        Toast.makeText(this, "It looks like the servers may be down try again", Toast.LENGTH_SHORT).show();
                    }
                },
                // when the HTTP request fails
                (Response.ErrorListener) error -> {
                    // make a Toast telling the user that something went wrong
                    Toast.makeText(this, "Error Occurred during load", Toast.LENGTH_LONG).show();
                }
        );
        // to the Volley request queue
        volleyQueue.add(jsonObjectRequest);
    }
}