package com.test.kumar.xingrepo;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by Kumar Saurabh on 1/17/2017.
 */

public class MainActivity extends AppCompatActivity {
    private ListView lv;

    private ArrayList<Repository> repoList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        repoList = new ArrayList<>();
        lv = (ListView) findViewById(R.id.list);
        if(isNetworkAvailable(this))
        {
            new GetContacts().execute();  // starting thr thread for Json download
        }
        else
        {
            Toast.makeText(this,"No internet connection",Toast.LENGTH_LONG).show();
        }
    }

    /**
     * This method checks if internetis there or not
     * @param context application contect
     * @return true for internet connection; false for no internet
     */

    public boolean isNetworkAvailable(final Context context) {
        final ConnectivityManager connectivityManager = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE));
        return connectivityManager.getActiveNetworkInfo() != null && connectivityManager.getActiveNetworkInfo().isConnected();
    }


    /**
     * this class is responsible for downloading the JSON on some other thread than UI thread
     */
    private class GetContacts extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Toast.makeText(MainActivity.this, "JSON data is being downloaded"
                    , Toast.LENGTH_LONG).show();
        }

        @Override
        protected Void doInBackground(Void... params) {

            HttpHandler sh = new HttpHandler();
            String url = "https://api.github.com/users/xing/repos";
            String jsonStr = sh.makeServiceCall(url);

            if (jsonStr != null) {
                try {
                    JSONArray jsonArray = new JSONArray(jsonStr); // gtting the json array
                    //loop through json array
                    for (int i = 0; i < jsonArray.length(); i++) {

                        JSONObject infoObj = jsonArray.getJSONObject(i);
                        String name = infoObj.getString("name");
                        String desc = infoObj.getString("description");
                        String htmlUrl = infoObj.getString("html_url");
                        boolean fork = infoObj.getBoolean("fork");

                        JSONObject ownerObj = infoObj.getJSONObject("owner");
                        String login = ownerObj.getString("login");
                        String ownerUrl = ownerObj.getString("html_url");

                        Repository repo = new Repository();

                        repo.setOwnerUrl(ownerUrl);
                        repo.setUrl(htmlUrl);
                        repo.setFork(fork);
                        repo.setName(name);
                        repo.setLogin(login);
                        repo.setDescription(desc);

                        repoList.add(repo);
                    }

                } catch (final Exception e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(),
                                    "Json parsing error:  main activity",
                                    Toast.LENGTH_LONG).show();
                        }
                    });
                }

            } else {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(),
                                "Couldn't get json from server. Check LogCat for possible errors!",
                                Toast.LENGTH_LONG).show();
                    }
                });
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            MyCustomAdapter adapter = new MyCustomAdapter(MainActivity.this, repoList);
            lv.setAdapter(adapter);

            /**
             * Seeting the event for long press
             */
            lv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                    onLongClick(lv, position);
                    return false;
                }
            });

        }

        /**
         * this method creates a alert dialog and askes user to deside which repo to go to
         * user can select normal repo or owner repo or cancel the dialog
         * @param lv list view on which the item is to be long pressed
         * @param position position of the item in the list
         */
        private void onLongClick(ListView lv, int position) {

            final Repository repo = (Repository) lv.getItemAtPosition(position);

            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
            alertDialogBuilder.setMessage("Choose desired Repo");
            alertDialogBuilder.setCancelable(true);

            alertDialogBuilder.setPositiveButton("Owner Url", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    openRepoPage(repo.getOwnerUrl());
                }
            });

            alertDialogBuilder.setNegativeButton("User Url", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    openRepoPage(repo.getUrl());
                }
            });

            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
        }

        /**
         * this method opens the browser with the desired uri for repo
         * @param targetUri detiunation webpage uri
         */
        private void openRepoPage(String targetUri) {
            Uri uri = Uri.parse(targetUri);
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);
        }

    }
}
