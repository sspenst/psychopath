package spencerspenst.psychopath;

import android.content.Context;
import android.widget.BaseAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ImageView;

class BlockAdapter extends BaseAdapter {
    private Context mContext;

    private final int columns;
    private final int rows;
    private final int blocks;
    private final String[][] type;

    BlockAdapter(Context c, int columns, int rows, String[][] type) {
        mContext = c;
        this.columns = columns;
        this.rows = rows;
        this.blocks = columns * rows;
        this.type = type.clone();
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

    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView;
        if (convertView == null) {
            imageView = new ImageView(mContext);
            if (columns > rows) imageView.setLayoutParams(new GridView.LayoutParams(Play.boardSize/columns, Play.boardSize/columns));
            else imageView.setLayoutParams(new GridView.LayoutParams(Play.boardSize/rows, Play.boardSize/rows));

            /* 0 - normal ground
             * 1 - unmovable block
             * 2 - movable block
             */
            switch(Integer.valueOf(type[position / columns][position % columns])) {
                case 0:
                    //textView.setBackgroundColor(Color.rgb(100, 119, 51));
                    imageView.setImageResource(R.drawable.ground);
                    break;
                case 1:
                    //textView.setBackgroundColor(Color.BLACK);
                    imageView.setImageResource(R.drawable.wall);
                    break;
                case 2:
                    //textView.setBackgroundColor(Color.rgb(215, 155, 155));
                    imageView.setImageResource(R.drawable.movable);
                    break;
            }
        } else {
            imageView = (ImageView) convertView;
        }

        return imageView;
    }
}