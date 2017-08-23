package com.peterponterio.top10downloader;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.StringReader;
import java.util.ArrayList;

/**
 * Created by peterponterio on 8/7/17.
 */

public class ParseApplications {
    private static final String TAG = "ParseApplications";
    private ArrayList<FeedEntry> applications;

    //initialize Arraylist
    public ParseApplications() {
        this.applications = new ArrayList<>();
    }

    public ArrayList<FeedEntry> getApplications() {
        return applications;
    }

    //parse and manipulate data string
    public boolean parse(String xmlData) {
        boolean status = true;
        FeedEntry currentRecord = null;

        //make sure were looking at tags inside of the Entry tag of the rss feed
        boolean inEntry = false;

        //stores the value of the current tag
        String textValue = "";

        try {
            //sets up the java xml parser
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            XmlPullParser xpp = factory.newPullParser();
            
            //tell pull parser what to parse. 
            //pull parser needs string reader and string reader needs string
            xpp.setInput(new StringReader(xmlData));
            int eventType = xpp.getEventType();
            
            //check event and make sure we havent reached the end of the xml document
            while(eventType != XmlPullParser.END_DOCUMENT) {

                //get name of current tag
                String tagName = xpp.getName();
                switch (eventType) {
                    case XmlPullParser.START_TAG:
//                        Log.d(TAG, "parse: Starting tag for " + tagName);
                        //only checks inside entry tags
                        if("entry".equalsIgnoreCase(tagName)) {
                            inEntry = true;
                            currentRecord = new FeedEntry();
                        }
                        break;
                    case XmlPullParser.TEXT:
                        textValue = xpp.getText();
                        break;
                    case XmlPullParser.END_TAG:
//                        Log.d(TAG, "parse: Ending tag for " + tagName);
                        if(inEntry) {
                            //test the tag name, test for each object
                            //by putting string first for .equalsIgnoreCase, it guarantees there will never be null
                            if("entry".equalsIgnoreCase(tagName)) {
                                applications.add(currentRecord);
                                inEntry = false;
                            } else if("name".equalsIgnoreCase(tagName)) {
                                currentRecord.setName(textValue);
                            } else if("artist".equalsIgnoreCase(tagName)) {
                                currentRecord.setArtist(textValue);
                            } else if("releaseDate".equalsIgnoreCase(tagName)) {
                                currentRecord.setReleaseDate(textValue);
                            } else if("summary".equalsIgnoreCase(tagName)) {
                                currentRecord.setSummary(textValue);
                            } else if("image".equalsIgnoreCase(tagName)) {
                                currentRecord.setImageURL(textValue);
                            }
                        }
                        break;
                    default:
                        //Nothing else to do
                }
                //continue working through xml until next event is found, reloops
                eventType = xpp.next();
            }
//            for (FeedEntry app: applications) {
//                Log.d(TAG, "*******************");
//                Log.d(TAG, app.toString());
//            }

        } catch(Exception e) {
            status = false;
            e.printStackTrace();
        }

        return status;
    }
}
