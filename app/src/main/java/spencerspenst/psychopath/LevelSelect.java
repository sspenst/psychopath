package spencerspenst.psychopath;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.GridView;

public class LevelSelect extends AppCompatActivity {
    public static int height;
    public static int width;

    private LevelSelect thisClass = this;
    private View mContentView;
    private GridView gridView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_level_select);
        mContentView = findViewById(R.id.fullscreen_content);
        setVisibility();

        gridView = (GridView) findViewById(R.id.level_grid);
        gridView.setAdapter(new TextAdapter(this));

        // Play a level depending on the button pressed
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                SharedPreferences settings = getSharedPreferences(Globals.PREFS_NAME, 0);
                int currentLevel = settings.getInt(Globals.CURRENT_LEVEL, Globals.FIRST_LEVEL);
                if (position < currentLevel) {
                    startActivity(Globals.createPlayIntent(thisClass, position + 1));
                }
            }
        });

        // Wait until the entire layout has been made, then get the dimensions
        // of the gridView. Remake the gridView based on those dimensions.
        ViewTreeObserver viewTreeObserver = gridView.getViewTreeObserver();
        if (viewTreeObserver.isAlive()) {
            viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    gridView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    width = gridView.getWidth();
                    height = gridView.getHeight();
                    gridView.setAdapter(new TextAdapter(thisClass));
                }
            });
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        // Window is fullscreen unless the user swipes
        // down from the top or bottom
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) setVisibility();
    }

    private void setVisibility() {
        mContentView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }

    public void home(View view) {
        startActivity(new Intent(this, Main.class));
    }

    public void play(View view) {
        SharedPreferences settings = getSharedPreferences(Globals.PREFS_NAME, 0);
        int currentLevel = settings.getInt(Globals.CURRENT_LEVEL, Globals.FIRST_LEVEL);
        startActivity(Globals.createPlayIntent(this, currentLevel));
    }
}