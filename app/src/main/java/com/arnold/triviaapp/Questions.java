package com.arnold.triviaapp;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.commons.codec.binary.Base64;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;

import pl.droidsonroids.gif.GifImageView;


public class Questions extends AppCompatActivity {
    private TextView question;
    private Button b1;
    private Button b2;
    private Button b3;
    private Button b4;
    private TextView status;
    private Button nextQuestion;
    private int correctlyAnswered = 0;
    private int currentQuestion = 1;
    private String correctAnswer = null;
    private boolean answered = false;
    private boolean ended = false;
    String categoryId;
    private String sessionKey;
    private GifImageView feedbackImage;
    private final LoadingDialog loadingDialog = new LoadingDialog(Questions.this);
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // start loading
        loadingDialog.startLoadingDialog();
        // set context
        setContentView(R.layout.activity_questions);
        // generate a session key for the user
        generateSessionKey();
        // get the extra passed and set it as the category id
        categoryId = getIntent().getStringExtra("id");
        // get the image
        feedbackImage = findViewById(R.id.feedback);
        // instantiate the variable
        question = findViewById(R.id.question);
        b1 = findViewById(R.id.option1);
        b2 = findViewById(R.id.option2);
        b3 = findViewById(R.id.option3);
        b4 = findViewById(R.id.option4);
        // set the color of the buttons
        b1.setBackgroundColor(Color.LTGRAY);
        b2.setBackgroundColor(Color.LTGRAY);
        b3.setBackgroundColor(Color.LTGRAY);
        b4.setBackgroundColor(Color.LTGRAY);
        status = findViewById(R.id.statusMessage);
        nextQuestion = findViewById(R.id.nextQuestion);
        nextQuestion.setVisibility(View.INVISIBLE);
        // check for null
        if(categoryId != null){
            // get a question
            getQuestion();
        }
        else{
            Toast.makeText(this, "Nothing was given", Toast.LENGTH_SHORT).show();
            finish();
        }
        // create a status message
        status.setText("Question "+ currentQuestion +" out of 10");
        //create the onClicks for each button
        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                answerQuestion((String) b1.getText());
            }
        });
        b2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                answerQuestion((String) b2.getText());
            }
        });
        b3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                answerQuestion((String) b3.getText());
            }
        });
        b4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                answerQuestion((String) b4.getText());
            }
        });
        nextQuestion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // increment the current question
                currentQuestion++;
                if(currentQuestion <= 10) {
                    // start loading
                    loadingDialog.startLoadingDialog();
                    //set answered back to false
                    answered = false;
                    // Change the Header
                    status.setText("Question " + currentQuestion + " out of 10");
                    // set next question to invisible
                    nextQuestion.setVisibility(View.INVISIBLE);
                    // set the color of the buttons
                    b1.setBackgroundColor(Color.LTGRAY);
                    b2.setBackgroundColor(Color.LTGRAY);
                    b3.setBackgroundColor(Color.LTGRAY);
                    b4.setBackgroundColor(Color.LTGRAY);
                    // get a question
                    getQuestion();
                    // create a handler
                    Handler handler = new Handler();
                    // Delay
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            loadingDialog.dismissDialog();
                        }
                    }, 500);
                }
                else{
                    if(ended == false) {
                        // start loading
                        loadingDialog.startLoadingDialog();
                        // set every question thing to invisible
                        question.setVisibility(View.GONE);
                        b1.setVisibility(View.GONE);
                        b2.setVisibility(View.GONE);
                        b3.setVisibility(View.GONE);
                        b4.setVisibility(View.GONE);
                        status.setVisibility(View.VISIBLE);
                        status.setText("You answered " + correctlyAnswered + " Questions correctly out of 10!");
                        nextQuestion.setText("Play again!");
                        nextQuestion.setVisibility(View.VISIBLE);
                        ended = true;
                        // set the image view to visible
                        feedbackImage.setVisibility(View.VISIBLE);
                        if(correctlyAnswered <= 7){
                            feedbackImage.setImageResource(R.drawable.lose);
                        }
                        else{
                            feedbackImage.setImageResource(R.drawable.win);
                        }
                        // create a handler
                        Handler handler = new Handler();
                        // Delay
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                loadingDialog.dismissDialog();
                            }
                        }, 500);
                    }
                    else{
                        finish();
                    }
                }
            }
        });
        // create a handler
        Handler handler = new Handler();
        // Delay
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                loadingDialog.dismissDialog();
            }
        }, 600);
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
                            // get the questions type and decode it
                            qsDecoded = (String) questionHere.get("type");
                            String type = new String(valueDecoded = Base64.decodeBase64(qsDecoded));
                            // if the question is a multiple choice
                            if(type.equals("multiple")){
                                // make the other button visible
                                b3.setVisibility(View.VISIBLE);
                                b4.setVisibility(View.VISIBLE);
                                // get the correct answer
                                qsDecoded = (String) questionHere.get("correct_answer");
                                correctAnswer = new String(valueDecoded = Base64.decodeBase64(qsDecoded));
                               // store all the incorrect answers
                                ArrayList<String> incorrect_answer = new ArrayList<>();
                                // add correct answer to list of answers
                                incorrect_answer.add(correctAnswer);
                                int counter = 0;
                                while (questionHere.getJSONArray("incorrect_answers").length() != counter){
                                    // decode the incorrect
                                    qsDecoded = (String) questionHere.getJSONArray("incorrect_answers").get(counter);
                                    // add the incorrect answer
                                    incorrect_answer.add(new String(valueDecoded = Base64.decodeBase64(qsDecoded)));
                                    counter++;
                                }
                                //shuffle
                                Collections.shuffle(incorrect_answer);
                                // add the response to each button
                                b1.setText(incorrect_answer.get(0));
                                b2.setText(incorrect_answer.get(1));
                                b3.setText(incorrect_answer.get(2));
                                b4.setText(incorrect_answer.get(3));
                            }
                            // if the question is a true and false
                            else{
                                // only show true and false and make the other buttons disappear
                                b3.setVisibility(View.INVISIBLE);
                                b4.setVisibility(View.INVISIBLE);
                                b1.setText("True");
                                b2.setText("False");
                                // get the correct answer and decode it
                                qsDecoded = (String) questionHere.get("correct_answer");
                                correctAnswer = new String(valueDecoded = Base64.decodeBase64(qsDecoded));
                            }
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

    private void answerQuestion(String chosen){
        if(answered == false) {
            // set the answered Flag to true
            answered = true;
            // check to see if answer was correct or incorrect and toast as such
            if (chosen.equals(correctAnswer)) {
                correctlyAnswered++;
                Toast.makeText(this, "Correct!", Toast.LENGTH_SHORT).show();
            }
            else{
                Toast.makeText(this, "Incorrect", Toast.LENGTH_SHORT).show();
            }
            // show correct and wrong answers with colors red-wrong and green-right
            if(b1.getText().equals(correctAnswer)){
                b1.setBackgroundColor(Color.GREEN);
                b2.setBackgroundColor(Color.RED);
                b3.setBackgroundColor(Color.RED);
                b4.setBackgroundColor(Color.RED);
            }
            else if(b2.getText().equals(correctAnswer)){
                b1.setBackgroundColor(Color.RED);
                b2.setBackgroundColor(Color.GREEN);
                b3.setBackgroundColor(Color.RED);
                b4.setBackgroundColor(Color.RED);
            }
            else if(b3.getText().equals(correctAnswer)){
                b1.setBackgroundColor(Color.RED);
                b2.setBackgroundColor(Color.RED);
                b3.setBackgroundColor(Color.GREEN);
                b4.setBackgroundColor(Color.RED);
            }
            else{
                b1.setBackgroundColor(Color.RED);
                b2.setBackgroundColor(Color.RED);
                b3.setBackgroundColor(Color.RED);
                b4.setBackgroundColor(Color.GREEN);
            }
            // make next question visible
            nextQuestion.setVisibility(View.VISIBLE);
        }
    }
}