package spencerspenst.psychopath;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class Play extends AppCompatActivity {

    private View mContentView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO: figure out how to do animations without freezing the phone
        //overridePendingTransition(R.anim.slide_in, 0);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);
        mContentView = findViewById(R.id.fullscreen_content);
        setVisibility();

        Intent intent = getIntent();

        TextView levelNum = (TextView) findViewById(R.id.level_num);
        String levelNumText = "Level: " + intent.getStringExtra(Globals.LEVEL_NUM);
        levelNum.setText(levelNumText);

        TextView levelName = (TextView) findViewById(R.id.level_name);
        String levelNameText = intent.getStringExtra(Globals.LEVEL_NAME);
        levelName.setText(levelNameText);

        TextView levelAuthor = (TextView) findViewById(R.id.level_author);
        String levelAuthorText = "by: " + intent.getStringExtra(Globals.LEVEL_AUTHOR);
        levelAuthor.setText(levelAuthorText);

        TextView levelSteps = (TextView) findViewById(R.id.level_steps);
        String levelStepsText = "0/" + intent.getStringExtra(Globals.LEVEL_STEPS);
        levelSteps.setText(levelStepsText);
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

    public void levelSelect(View view) {
        startActivity(new Intent(this, LevelSelect.class));
    }

    public void restart(View view) {
        // TODO: move this code somewhere else
        SharedPreferences settings = getSharedPreferences(Globals.PREFS_NAME, 0);
        int currentLevel = settings.getInt(Globals.CURRENT_LEVEL, Globals.FIRST_LEVEL);
        if (currentLevel < Globals.TOTAL_LEVELS) {
            SharedPreferences.Editor editor = settings.edit();
            editor.putInt(Globals.CURRENT_LEVEL, currentLevel + 1);
            editor.commit();
        }
    }

    /** TODO: should this method be in this class? maybe put in Globals?
     * Creates an Intent with the information needed to start a level
     *
     * @param fromClass the Activity that called this method
     * @param level the level number that is being loaded
     * @return Intent with all needed information
     */
    public static Intent createLevelIntent(Activity fromClass, int level) {
        Intent intent = new Intent(fromClass, Play.class);
        intent.putExtra(Globals.LEVEL_NUM, Integer.toString(level));

        // Get level information from level_data.txt based on which
        // button was pressed (based on the value of position)
        try {
            String str;
            int count = 0;
            InputStream ins = fromClass.getResources().openRawResource(R.raw.level_data);
            BufferedReader reader = new BufferedReader(new InputStreamReader(ins));
            while((str = reader.readLine()) != null) {
                if (count == (level - 1) * Globals.FILE_LINES_PER_LEVEL) {
                    intent.putExtra(Globals.LEVEL_NAME, str);
                    str = reader.readLine();
                    intent.putExtra(Globals.LEVEL_AUTHOR, str);
                    str = reader.readLine();
                    intent.putExtra(Globals.LEVEL_STEPS, str);
                    break;
                }
                count++;
            }
            reader.close();
            ins.close();
        } catch (IOException e){
            e.printStackTrace();
        }

        return intent;
    }
}
