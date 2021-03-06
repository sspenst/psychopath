package spencerspenst.psychopath;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class Main extends AppCompatActivity {
    private View mContentView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContentView = findViewById(R.id.fullscreen_content);
        setVisibility();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        // Window is fullscreen unless the user swipes
        // down from the top or bottom
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) setVisibility();
    }

    // TODO:
    // http://stackoverflow.com/questions/23047637/android-immersive-mode-and-backwards-compatability
    // so that immersive can be simulated on APIs lower than 19
    // TODO: make this function global because lots of other files do the same thing
    private void setVisibility() {
        mContentView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }

    public void play(View view) {
        SharedPreferences settings = getSharedPreferences(Globals.PREFS_NAME, 0);
        int currentLevel = settings.getInt(Globals.CURRENT_LEVEL, Globals.FIRST_LEVEL);
        Intent intent = new Intent(this, Play.class);
        intent.putExtra(Globals.LEVEL_NUM, currentLevel);
        startActivity(intent);
    }

    public void levelSelect(View view) {
        startActivity(new Intent(this, LevelSelect.class));
    }

    public void settings(View view) {
        startActivity(new Intent(this, Settings.class));
    }

    public void about(View view) {
        startActivity(new Intent(this, About.class));
    }
}
