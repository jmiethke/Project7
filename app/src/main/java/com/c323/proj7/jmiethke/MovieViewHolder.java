package com.c323.proj7.jmiethke;

import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Generic ViewHolder class used for MovieAdapter.
 */
public class MovieViewHolder {

    ImageView itemImage;
    TextView movieName;
    MovieViewHolder(View v) {
        itemImage = v.findViewById(R.id.iv_list_item);
        movieName = v.findViewById(R.id.tv_list_item);
    }
}
