package me.dotteam.dotprod;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.Date;
import java.util.List;

import me.dotteam.dotprod.data.Hike;

/**
 * Created by foxtrot on 16/11/15.
 */
public class HikeArrayAdapter extends ArrayAdapter<Hike> {

    public HikeArrayAdapter(Context context, List<Hike> allHikes){
        super(context,R.layout.hike_brief,allHikes);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        //Using viewholder pattern to recycle everything correctly.
        if(convertView==null){
            LayoutInflater li = (LayoutInflater) super.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = li.inflate(R.layout.hike_brief,parent,false);
        }

        TextView hikeName = (TextView) convertView.findViewById(R.id.textView_HikeName);
        TextView hikeStart = (TextView) convertView.findViewById(R.id.textView_HikeDate);
        TextView hikeDuration = (TextView) convertView.findViewById(R.id.textView_HikeDuration);

        //TODO: Change to nickname or something
        Hike focused = super.getItem(position);
        hikeName.setText(Integer.toString(focused.getUniqueID()));
        hikeStart.setText(new Date(focused.startTime()).toString());
        hikeDuration.setText(focused.elapsedTime());

        return convertView;
    }
}
