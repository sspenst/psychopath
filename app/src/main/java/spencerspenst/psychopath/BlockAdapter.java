package spencerspenst.psychopath;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.Gravity;
import android.widget.BaseAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

class BlockAdapter extends BaseAdapter {
    private Context mContext;

    // TODO: change these depending on level properties?
    private static final int columns = 20;
    private static final int rows = 20;
    private static final int blocks = rows * columns;

    BlockAdapter(Context c) {
        mContext = c;
    }

    public int getCount() {
        return blocks;
    }

    public Object getItem(int position) {
        return null;
    }

    public long getItemId(int position) {
        return 0;
    }

    // create a new TextView for each item referenced by the Adapter
    public View getView(int position, View convertView, ViewGroup parent) {
        TextView textView;
        if (convertView == null) {
            textView = new TextView(mContext);
            textView.setLayoutParams(new GridView.LayoutParams(Play.width/columns, Play.height/rows));
            if (position % 3 == 0) textView.setBackgroundColor(Color.BLACK);
            else if (position % 3 == 1) textView.setBackgroundColor(Color.rgb(215, 155, 155));
            else textView.setBackgroundColor(Color.rgb(100, 119, 51));
            // Set text color based on levels beaten
            //SharedPreferences settings = mContext.getSharedPreferences(Globals.PREFS_NAME, 0);
            //int currentLevel = settings.getInt(Globals.CURRENT_LEVEL, Globals.FIRST_LEVEL);
            //if (position < currentLevel) textView.setTextColor(Color.BLACK);
            //else textView.setTextColor(Color.argb(30, 0, 0, 0));
        } else {
            textView = (TextView) convertView;
        }

        //textView.setText(Integer.toString(position + 1));
        return textView;

        /* OLD IMAGEVIEW CODE TODO: IS IT USEFUL?
        ImageView imageView;
        if (convertView == null) {
            // if it's not recycled, initialize some attributes
            imageView = new ImageView(mContext);
            imageView.setLayoutParams(new GridView.LayoutParams(LevelSelect.width/6, LevelSelect.height/12));
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setPadding(8, 8, 8, 8);
        } else {
            imageView = (ImageView) convertView;
        }

        imageView.setImageResource(mThumbIds[position]);
        return imageView;*/
    }
}