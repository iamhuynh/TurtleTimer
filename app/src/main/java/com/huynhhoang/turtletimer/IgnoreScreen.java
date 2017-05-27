package com.huynhhoang.turtletimer;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

import static android.R.id.input;
import static android.widget.Toast.makeText;

/**
 * Created by huynh on 5/24/17.
 */

public class IgnoreScreen extends Activity {

    ArrayList<String> ignoreContactList = new ArrayList<String>();
    Button buttonAddIgnoreNumber;
    EditText editTextIgnoreNumber;
    ListView listViewIgnoreContacts;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ignore_layout);

        buttonAddIgnoreNumber = (Button) findViewById(R.id.buttonAddIgnoreNumber);
        editTextIgnoreNumber = (EditText) findViewById(R.id.editTextIgnoreNumber);
        listViewIgnoreContacts = (ListView) findViewById(R.id.listViewIgnoreContacts);



        buttonAddIgnoreNumber.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String getInput = editTextIgnoreNumber.getText().toString();
                getInput = stripNonDigits(getInput);

                if(getInput.length() != 10) {
                    Toast toast = Toast.makeText(getBaseContext(), "Input must be 10 digits long.", Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                }
                else {
                    getInput = "(" + getInput.charAt(0) + getInput.charAt(1) + getInput.charAt(2) + ") " +
                            getInput.charAt(3) + getInput.charAt(4) + getInput.charAt(5) + "-" +
                            getInput.charAt(6) + getInput.charAt(7) + getInput.charAt(8) + getInput.charAt(9);

                    if (ignoreContactList.contains(getInput)) {
                        Toast toast = Toast.makeText(getBaseContext(), getInput + " is already in the list", Toast.LENGTH_LONG);
                        toast.setGravity(Gravity.CENTER, 0, 0);
                        toast.show();
                    }

                    else {
                        ignoreContactList.add(getInput);
                        ArrayAdapter<String> adapter = new ArrayAdapter<String>(IgnoreScreen.this, android.R.layout.simple_expandable_list_item_1, ignoreContactList);
                        listViewIgnoreContacts.setAdapter(adapter);
                        Toast toast = Toast.makeText(getBaseContext(), getInput + " will be ignored", Toast.LENGTH_LONG);
                        toast.setGravity(Gravity.CENTER, 0, 0);
                        toast.show();
                        editTextIgnoreNumber.setText("");
                    }
                }
            }
        });

        Intent activityThatCalled = getIntent();

    }


    /**
     * Helper class to strip digits from a string
     * Used for user input
     */
    public static String stripNonDigits(final CharSequence input){
        final StringBuilder sb = new StringBuilder(input.length());
        for(int i = 0; i < input.length(); i++){
            final char c = input.charAt(i);
            if(c > 47 && c < 58){
                sb.append(c);
            }
        }
        return sb.toString();
    }




    /**
     * method to switch screens to contact list
     */

    public void onGoToMainScreen(View view) {

        Intent getMainScreenIntent = new Intent(this, MainActivity.class);

        final int result = 1;

        getMainScreenIntent.putExtra("CallingActivity", "MainActivity");

        startActivityForResult(getMainScreenIntent, result);


    }

    /**
     * method to switch screens to contact list
     */

    public void onGoToContactList(View view) {

        Intent getContactScreenIntent = new Intent(this, ContactScreen.class);

        final int result = 1;

        getContactScreenIntent.putExtra("CallingActivity", "MainActivity");

        startActivityForResult(getContactScreenIntent, result);


    }
}
