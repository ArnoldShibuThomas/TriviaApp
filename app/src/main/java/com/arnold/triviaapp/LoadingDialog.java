package com.arnold.triviaapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.view.LayoutInflater;

public class LoadingDialog {
    // declare two components
    private Activity activity;
    private AlertDialog alertDialog;

    // CConstructor
    LoadingDialog(Activity myActivity){
        activity = myActivity;
    }

    void startLoadingDialog(){
        // create the builder
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        // get the inflation
        LayoutInflater inflater = activity.getLayoutInflater();
        // set the view to the loading
        builder.setView(inflater.inflate(R.layout.loading, null));
        // avoid user clicking off
        builder.setCancelable(false);

        //create the dialog
        alertDialog = builder.create();
        // show it
        alertDialog.show();
    }

    void dismissDialog(){
        // dismmiss the dialog
        alertDialog.dismiss();
    }
}
