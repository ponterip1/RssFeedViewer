package com.peterponterio.top10downloader;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by peterponterio on 8/7/17.
 */

public class FeedAdapter<T extends FeedEntry> extends ArrayAdapter {
    private static final String TAG = "FeedAdapter";
    private final int layoutResource;

    //creates all the view objects described in the xml
    private final LayoutInflater layoutInflater;
    private List<T> applications;

    public FeedAdapter(@NonNull Context context, @LayoutRes int resource, List<T> applications) {
        super(context, resource);
        this.layoutResource = resource;
        this.layoutInflater = LayoutInflater.from(context);
        this.applications = applications;
    }

    @Override
    public int getCount() {
        return applications.size();
    }

    //called everytime the adapter wants another item to display
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        /*  We need a viewHolder variable to hold the viewHolder object
         *  Then if convertView is null we are inflating it as before but then we
         *  also add the creation of a new viewHolder object and we store that in the convertView tag using the set tag method
         *  Now if we have been given an existing view back by the listView, then we retrieve ViewHolder from its tag
         *  using the get method. And that tag is an object and thats why we have to cast it as a viewHolder.
         *  We then retrieve the application record from the list and sets its values into the widgets that are stored in the
         *  viewHolder. So find view by ID's only called when a new view has to be inflated, otherwse the widgets have already been found
         *  and references to them are stored in the viewHolder
         * **/

        ViewHolder viewHolder;

        if (convertView == null) {
            Log.d(TAG, "getView: called with null convertView");
            convertView = layoutInflater.inflate(layoutResource, parent, false);

            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else {
            Log.d(TAG, "getView: provided a convertView");
            viewHolder = (ViewHolder) convertView.getTag();
        }


        //retrieves the object from the position of the item it needs to display
        T currentApp = applications.get(position);

        //set the fields into the view
        viewHolder.tvName.setText(currentApp.getName());
        viewHolder.tvArtist.setText(currentApp.getArtist());
        viewHolder.tvSummary.setText(currentApp.getSummary());
        return convertView;
    }

    private class ViewHolder {
        final TextView tvName;
        final TextView tvArtist;
        final TextView tvSummary;

        ViewHolder(View v) {
            this.tvName = (TextView) v.findViewById(R.id.tvName);
            this.tvArtist = (TextView) v.findViewById(R.id.tvArtist);
            this.tvSummary = (TextView) v.findViewById(R.id.tvSummary);

        }
    }
}
