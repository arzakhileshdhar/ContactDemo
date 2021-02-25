package com.example.ajay.contacts_4;

import android.Manifest;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final int PERMISSIONS_REQUEST_READ_CONTACTS = 100;

    private ArrayList<String> mNames = new ArrayList<>();
    private ArrayList<String> mIDs = new ArrayList<>();
    private ArrayList<String> mNumbers = new ArrayList<>();
    private ArrayList<MyContact> contacts = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        showContacts();
    }


    private void loadData(){
        //      Retrieve names from phone's contact list and save in mNames
        getContactDataBefore();

//      Apply changes to phone's contact list
        new AsyncTask<String,String,String>(){

            @Override
            protected String doInBackground(String... params) {
                String name,number,id;
                for(int i=0;i<mIDs.size();i++){
//                    name = mNames.get(i);
                    id = mIDs.get(i);
                    number = mNumbers.get(i);
                  //  ContactsManager.addContact(MainActivity.this, new MyContact(id,number));
                    ContactsManager.updateMyContact(MainActivity.this, id);
                }
                return null;
            }

            @Override
            protected void onPostExecute(String s) {
                getContactDataAfter();
            }
        }.execute();

    }
    private void showContacts() {
        // Check the SDK version and whether the permission is already granted or not.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, PERMISSIONS_REQUEST_READ_CONTACTS);
            //After this point you wait for callback in onRequestPermissionsResult(int, String[], int[]) overriden method
        } else {
            loadData();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST_READ_CONTACTS) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission is granted
                loadData();
            } else {
                Toast.makeText(this, "Until you grant the permission, we canot display the names", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Method to fetch contact's from device
     */
    private void getContactDataBefore(){
        int i=0;

        // query all contact id's from device
        Cursor c1 = getContentResolver().query(ContactsContract.Contacts.CONTENT_URI,
                    new String[]{ContactsContract.Contacts._ID},null,null,null);

        if((c1 != null) && c1.moveToFirst()){

            // add contact id's to the mIDs list
            do{
                mIDs.add(c1.getString(c1.getColumnIndexOrThrow(ContactsContract.Contacts._ID)));

                // query all contact numbers corresponding to current id
                Cursor c2 = getContentResolver()
                        .query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                        new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER},
                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=?",
                        new String[]{mIDs.get(i)},null);

                if(c2 != null && c2.moveToFirst()){
                    // add contact number's to the mNumbers list
                    do{
                        mNumbers.add(c2.getString(c2
                                .getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER)));
                    }while (c2.moveToNext());
                    c2.close();
                }

                i++;
            }while (c1.moveToNext() && i<c1.getCount());

            c1.close();
        }
    }

    /**
     * Method to fetch contacts after updation (for logging purposes)
     */
    private void getContactDataAfter(){
        Cursor c = getContentResolver()
                .query(ContactsContract.Contacts.CONTENT_URI, null,null,null,null);

        List<String> RIds = new ArrayList<>();
        mIDs = new ArrayList<>();
        mNumbers = new ArrayList<>();
        int i=0;

        if(c != null && c.moveToFirst()){
            do{
                mIDs.add(c.getString(c
                        .getColumnIndexOrThrow(ContactsContract.Contacts._ID)));
                mNames.add(c.getString(c
                        .getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME)));

                Cursor c2 = getContentResolver()
                        .query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                        new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER},
                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=?",
                        new String[]{mIDs.get(i)},null);

                if(c2 != null && c2.moveToFirst()){
                    do{
                        mNumbers.add(c2.getString(c2
                                .getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER)));
                    }while (c2.moveToNext());
                    c2.close();
                }

                Cursor rawcontacts = getContentResolver()
                        .query(ContactsContract.RawContacts.CONTENT_URI,
                        new String[]{ContactsContract.RawContacts._ID},
                        ContactsContract.RawContacts.CONTACT_ID + "=?",
                        new String[]{mIDs.get(i)},null);

                if(rawcontacts != null && rawcontacts.moveToFirst()){
                    do{
                        RIds.add(rawcontacts.getString(rawcontacts
                                .getColumnIndexOrThrow(ContactsContract.RawContacts._ID)));
                    }while (rawcontacts.moveToNext());
                    rawcontacts.close();
                }

                Log.I(mNames.get(i) + ":");
                for(int j=0;j<RIds.size();j++){
                    Log.I(RIds.get(j));
                }

                Log.I(mIDs.get(i));

                if(mNumbers.size()>i)
                Log.I(mNumbers.get(i));

                i++;
            }while (c.moveToNext());
            c.close();
        }
    }
}