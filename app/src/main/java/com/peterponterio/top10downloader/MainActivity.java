package com.peterponterio.top10downloader;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private ListView listApps;

    //changed limit value of url to %d
    //specifies an integer value that will be replaced by an actual value using the string.format method
    private String feedURL = "http://ax.itunes.apple.com/WebObjects/MZStoreServices.woa/ws/RSS/topfreeapplications/limit=%d/xml";
    private int feedLimit = 10;

    private String feedCachedURL = "INVALIDATED";
    public static final String STATE_URL = "feedURL";
    public static final String STATE_LIMIT = "feedLimit";

    public static final String LISTVIEW_STATE = "LISTVIEW_STATE";
    private Parcelable mListState = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        listApps = (ListView) findViewById(R.id.xmlListView);


        //checks for a non null bundle. If bundle is null, application is starting for the first time
        //rotating the device DOES NOT create a null bundle
        //if bundle is not null(meaning there is a save state), load savedInstanceState
        if(savedInstanceState != null) {
            feedURL = savedInstanceState.getString(STATE_URL);
            feedLimit = savedInstanceState.getInt(STATE_LIMIT);
        }

        //using string.format, were passing the feedURL string and passing a parameter of feedLimit
        //feedLimit value will replace the %d in the feedURL
        downloadURL(String.format(feedURL, feedLimit));
    }



    //called when its time to inflate the activities menu
    //create the menu objects from the xml file
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.feeds_menu, menu);
        if(feedLimit == 10) {
            menu.findItem(R.id.menu10).setChecked(true);
        } else {
            menu.findItem(R.id.menu25).setChecked(true);
        }
        return true;
    }




    //specify what the menu items do
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch(id) {
            case R.id.menuFree:
                feedURL = "http://ax.itunes.apple.com/WebObjects/MZStoreServices.woa/ws/RSS/topfreeapplications/limit=%d/xml";
                break;
            case R.id.menuPaid:
                feedURL = "http://ax.itunes.apple.com/WebObjects/MZStoreServices.woa/ws/RSS/toppaidapplications/limit=%d/xml";
                break;
            case R.id.menuSongs:
                feedURL = "http://ax.itunes.apple.com/WebObjects/MZStoreServices.woa/ws/RSS/topsongs/limit=%d/xml";
                break;
            case R.id.menu10:
            case R.id.menu25:
                //if top10 or top25 is checked, check it, change value of feedLimit accordingly(35-10=25, 35-10=25)
                //only changed value if the menu item(top10 or top25) wasnt already checked
                if(!item.isChecked()) {
                    item.setChecked(true);
                    feedLimit = 35 - feedLimit;
                    Log.d(TAG, "onOptionsItemSelected: " + item.getTitle() + " setting feedLimit to " + feedLimit);
                } else {
                    Log.d(TAG, "onOptionsItemSelected: " + item.getTitle() + " feedLimit unchanged");
                }
                break;
            case R.id.menuRefresh:
                //resets the cached URL so downloadURL runs again
                feedCachedURL = "INVALIDATED";
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        //using string.format, were passing the feedURL string and passing a parameter of feedLimit
        //feedLimit value will replace the %d in the feedURL
        downloadURL(String.format(feedURL, feedLimit));
        return true;
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString(STATE_URL, feedURL);
        outState.putInt(STATE_LIMIT, feedLimit);

        //saves the position of the scroll(point where you stopped scrolling)
        mListState = listApps.onSaveInstanceState();
        outState.putParcelable(LISTVIEW_STATE, mListState);


        super.onSaveInstanceState(outState);
    }


    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        //retrieves the scroll position
        mListState = savedInstanceState.getParcelable(LISTVIEW_STATE);
    }





    private void downloadURL(String feedURL) {
        //check the URL its given against the stored value, our cachedURL
        //if theyre the same, theres no need to dowload the data again
        //if theyre different, downloads the data the stores the downloaded URL in the feedCachedURL, ready to be compared next time
        if(!feedURL.equalsIgnoreCase(feedCachedURL)) {
            Log.d(TAG, "downloadURL: starting AsyncTask");
            DownloadData downloadData = new DownloadData();
            downloadData.execute(feedURL);
            feedCachedURL = feedURL;
            Log.d(TAG, "downloadURL: done");
        } else {
            Log.d(TAG, "downloadURL: URL not changed");
        }
    }
    











    //void is in place of a progress bar option
    //<Params, Progress, result>
    //async task runs in background
    private class DownloadData extends AsyncTask<String, Void, String> {

        private static final String TAG = "DownloadData";


        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
//            Log.d(TAG, "onPostExecute: parameter is " + s);
            ParseApplications parseApplications = new ParseApplications();
            parseApplications.parse(s);

            //created new array adapter object
            //first parameter is context
            //second parameter is the resource containing the text view that the array adapter will use to put the data into
            //third parameter is the list of objects to display

            //library adapter
//            ArrayAdapter<FeedEntry> arrayAdapter = new ArrayAdapter<FeedEntry>(MainActivity.this, R.layout.list_item, parseApplications.getApplications());
//            listApps.setAdapter(arrayAdapter);

            //custom adapter
            FeedAdapter<FeedEntry> feedAdapter = new FeedAdapter<>(MainActivity.this, R.layout.list_record, parseApplications.getApplications());
            listApps.setAdapter(feedAdapter);


            //checks if the list state is null
            //if it is, no position was saved, therefore nothing will be restored
            //if not null, restore the scroll position
            Log.d(TAG, "onPostExecute: restoring position " + mListState);
            if(mListState != null) {
                listApps.onRestoreInstanceState(mListState);
                mListState = null;
            }
        }









        @Override
        protected String doInBackground(String... params) {
            Log.d(TAG, "doInBackground: starts with " + params[0]);
            String rssFeed = downloadXML(params[0]);
            if (rssFeed == null) {
                Log.e(TAG, "doInBackground: Error downloading");
            }
            return rssFeed;
        }









        private String downloadXML(String urlPath) {
            //more efficient then concatenating strings
            StringBuilder xmlResult = new StringBuilder();

            //try block to catch exception
            try {
                URL url = new URL(urlPath);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                int response = connection.getResponseCode();
                Log.d(TAG, "downloadXML: The response code was " + response);
                InputStream inputStream = connection.getInputStream();
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader reader = new BufferedReader(inputStreamReader);
                //3 previous lines can be replaced with ^^^
                //BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

                int charsRead;
                //stores 500 characters (arbitrary)
                char[] inputBuffer = new char[500];
                //keeps going until end of streams reach
                while (true) {
                    charsRead = reader.read(inputBuffer);
                    //end of stream of data
                    if (charsRead < 0) {
                        break;
                    }
                    //hold and count number of characters read from the stream
                    if (charsRead > 0) {
                        xmlResult.append(String.copyValueOf(inputBuffer, 0, charsRead));
                    }
                }
                //close reader when theres nothing left to read
                reader.close();

                return xmlResult.toString(); //converts to string
            } catch (MalformedURLException e) {
                Log.e(TAG, "downloadXML: Invalid URL " + e.getMessage());
            } catch (IOException e) {
                Log.e(TAG, "downloadXML: IO Exception reading data " + e.getMessage());
            } catch (SecurityException e) {
                Log.e(TAG, "downloadXML: Security Exception. Needs permission? " + e.getMessage());
            }

            return null;
        }


    }
}

