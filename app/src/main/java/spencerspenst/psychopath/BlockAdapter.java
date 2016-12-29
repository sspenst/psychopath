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
    private final int columns;
    private final int rows;
    private final int blocks;
    private final String[][] type;

    // TODO: take rows and columns as a parameter
    BlockAdapter(Context c, int columns, int rows, String[][] type) {
        mContext = c;
        this.columns = columns;
        this.rows = rows;
        this.blocks = columns * rows;
        // TODO: probably should do some defensive copying here
        this.type = type;
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
            if (columns > rows) textView.setLayoutParams(new GridView.LayoutParams(Play.boardSize/columns, Play.boardSize/columns));
            else textView.setLayoutParams(new GridView.LayoutParams(Play.boardSize/rows, Play.boardSize/rows));
            /* 0 - normal ground
             * 1 - unmovable block
             * 2 - movable block
             * 3 - finish
             * 4 - start
             */
            switch(Integer.valueOf(type[position / columns][position % columns])) {
                case 0:
                case 4:
                    textView.setBackgroundColor(Color.rgb(100, 119, 51));
                    break;
                case 1:
                    textView.setBackgroundColor(Color.BLACK);
                    break;
                case 2:
                    textView.setBackgroundColor(Color.rgb(215, 155, 155));
                    break;
                case 3:
                    textView.setBackgroundColor(Color.RED);
                    break;
            }
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