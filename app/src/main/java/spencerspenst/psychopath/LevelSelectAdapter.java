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
import android.widget.TextView;

class LevelSelectAdapter extends BaseAdapter {
    private Context mContext;

    private static final int columns = 6;
    private static final int rows = 12;
    private static final int buttons = rows * columns;

    LevelSelectAdapter(Context c) {
        mContext = c;
    }

    public int getCount() {
        // TODO: add a bonus button?
        return buttons - 1;
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
            textView.setLayoutParams(new GridView.LayoutParams(LevelSelect.width/columns, LevelSelect.height/rows));
            textView.setTypeface(Typeface.SANS_SERIF, Typeface.BOLD);
            textView.setGravity(Gravity.CENTER);
            textView.setTextSize(20);

            // Set text color based on levels beaten
            SharedPreferences settings = mContext.getSharedPreferences(Globals.PREFS_NAME, 0);
            int currentLevel = settings.getInt(Globals.CURRENT_LEVEL, Globals.FIRST_LEVEL);
            if (position < currentLevel) textView.setTextColor(Color.BLACK);
            else textView.setTextColor(Color.argb(30, 0, 0, 0));
        } else {
            textView = (TextView) convertView;
        }

        textView.setText(Integer.toString(position + 1));
        return textView;
    }
}