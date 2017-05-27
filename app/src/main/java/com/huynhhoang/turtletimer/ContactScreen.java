package com.huynhhoang.turtletimer;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;
import android.app.AlertDialog.Builder;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static android.R.attr.button;
import static android.R.attr.id;
import static android.R.id.list;
import static android.icu.lang.UCharacter.GraphemeClusterBreak.L;

/**
 * Created by huynh on 5/24/17.
 */

public class ContactScreen extends Activity {

    // Request code for READ_CONTACTS. It can be any number > 0.
    private static final int PERMISSIONS_REQUEST_READ_CONTACTS = 100;
    final CharSequence myList[] = { "Tea", "Coffee", "Milk" };
    private Button buttonLoadContacts;
    private Button buttonImportPopup;
    private ListView listMainContacts;

    private ArrayList<Integer> mSelectedItems = new ArrayList<>();


    AlertDialog mDialog = null;
    /**
     * This becomes false when "Select All" is selected while deselecting some other item on list.
     */
    boolean selectAll = true;
    /**
     * Number of items in array list and eventually in ListView
     */
    int sizeOfList;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.contact_layout);

        listMainContacts = (ListView) findViewById(R.id.listMainContacts);
        buttonLoadContacts = (Button) findViewById(R.id.buttonLoadContacts);
        buttonImportPopup = (Button) findViewById(R.id.buttonImportPopup);

        buttonImportPopup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buttonLoadContactsOntoPop();
            }
        });




        buttonLoadContacts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                buttonLoadContacts();
            }
        });

    }

    public void buttonLoadContacts() {
        showContacts();
    }

    public void buttonLoadContactsOntoPop() {
        mDialog = onCreateDialog(null);
        mDialog.show();

        // we get the ListView from already shown dialog
        final ListView listView = mDialog.getListView();
        // ListView Item Click Listener that enables "Select all" choice
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                boolean isChecked = listView.isItemChecked(position);
                if (position == 0) {
                    if(selectAll) {
                        for (int i = 1; i < sizeOfList; i++) { // we start with first element after "Select all" choice
                            if (isChecked && !listView.isItemChecked(i)) {
                                listView.performItemClick(listView, i, 0);
                                mSelectedItems.add(i);
                                Log.d("this is my array", "arr: " + mSelectedItems.toString());
                            }
                            else if (!isChecked && listView.isItemChecked(i)) {
                                listView.performItemClick(listView, i, 0);
                                if (mSelectedItems.contains(i)) {
                                    mSelectedItems.remove(Integer.valueOf(i));
                                    Log.d("this is my array", "cray: " + mSelectedItems.toString());
                                }

//
                            }
                        }
                    }
                } else if (position != 0) {
                    if (!isChecked && listView.isItemChecked(0)) {
                        // if other item is unselected while "Select all" is selected, unselect "Select all"
                        // false, performItemClick, true is a must in order for this code to work
                        selectAll = false;
                        listView.performItemClick(listView, 0, 0);
                        selectAll = true;
                    }

                    if(isChecked && !listView.isItemChecked(0)) {
                        mSelectedItems.add(position);
                        Log.d("queso", "queso: " + mSelectedItems.toString());
                    }
                    else if (!isChecked && !listView.isItemChecked(0)) {
                        if (mSelectedItems.contains(position)) {
                            mSelectedItems.remove(Integer.valueOf(position));
                            Log.d("this is my array", "burrito: " + mSelectedItems.toString());
                        }

                    }
                }


            }
        });

    }

    /**
     * Show the contacts in the ListView.
     */
    private void showContacts() {
        // Check the SDK version and whether the permission is already granted or not.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, PERMISSIONS_REQUEST_READ_CONTACTS);
            //After this point you wait for callback in onRequestPermissionsResult(int, String[], int[]) overriden method
        } else {
            // Android version is lesser than 6.0 or the permission is already granted.
            List<String> contacts = getContactNames();
            //ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, contacts);
            //listMainContacts.setAdapter(adapter);
        }
    }

    /**
     * test contacts
     */
    private void getContactListToDisplay() {
        // Check the SDK version and whether the permission is already granted or not.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, PERMISSIONS_REQUEST_READ_CONTACTS);
            //After this point you wait for callback in onRequestPermissionsResult(int, String[], int[]) overriden method
        } else {
            // Android version is lesser than 6.0 or the permission is already granted.
            List<String> contacts = getContactNames();
            //ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, contacts);
            //listMainContacts.setAdapter(adapter);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST_READ_CONTACTS) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission is granted
                showContacts();
            } else {
                Toast.makeText(this, "Until you grant the permission, we canot display the names", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Read the name of all the contacts.
     *
     * @return a list of names.
     */
    private List<String> getContactNames() {
        List<String> contacts = new ArrayList<>();
        contacts.add("Select All");
        // Get the ContentResolver
        ContentResolver cr = getContentResolver();
        // Get the Cursor of all the contacts
        Cursor cursor = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);

        // Move the cursor to first. Also check whether the cursor is empty or not.
        if (cursor.moveToFirst()) {
            // Iterate through the cursor
            do {
                // Get the contacts name
                String id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
                int hasPhoneNumber = Integer.parseInt(cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)));

                if (hasPhoneNumber > 0) {
                    Cursor cursor2 = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                            new String[]{id}, null);

                    while (cursor2.moveToNext()) {
                        String displayName = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                        String phoneNumber = cursor2.getString(cursor2.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                        String addElement = displayName + " :: " + phoneNumber;
                        contacts.add(addElement);
                    }

                }


            } while (cursor.moveToNext());
        }
        // Close the curosor
        cursor.close();

        return contacts;
    }


    public AlertDialog onCreateDialog(Bundle savedInstanceState){

        List<String> contactList = new ArrayList<String>();

        // Check the SDK version and whether the permission is already granted or not.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, PERMISSIONS_REQUEST_READ_CONTACTS);
            //After this point you wait for callback in onRequestPermissionsResult(int, String[], int[]) overriden method
        } else {
            // Android version is lesser than 6.0 or the permission is already granted.
            contactList = getContactNames();
            //ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, contacts);
            //listMainContacts.setAdapter(adapter);
        }

        CharSequence[] csContactList = contactList.toArray(new CharSequence[contactList.size()]);
        //save the size of the contact list to sizeOfList
        sizeOfList = contactList.size();
        final boolean bl[] = new boolean[sizeOfList];
        final AlertDialog.Builder builder = new AlertDialog.Builder(ContactScreen.this);

        // set the dialog title
        builder.setTitle("Choose your contacts")

                // specify the list array, the items to be selected by default (null for none),
                // and the listener through which to receive call backs when items are selected
                .setMultiChoiceItems(csContactList, null, null)
                // Set the action buttons
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        // User clicked OK, so save something here

                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {}
                });
        return builder.create();

    }

}
