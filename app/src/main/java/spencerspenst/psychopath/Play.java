package spencerspenst.psychopath;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class Play extends AppCompatActivity {
    public static int width;
    public static int height;

    private Play thisClass = this;
    private View mContentView;
    private GridView gridView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO: figure out how to do animations without freezing the phone
        //overridePendingTransition(R.anim.slide_in, 0);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);
        mContentView = findViewById(R.id.fullscreen_content);
        setVisibility();

        gridView = (GridView) findViewById(R.id.level_grid);
        gridView.setAdapter(new BlockAdapter(this));

        // Wait until the entire layout has been made, then get the dimensions of the GridView.
        // Remake the RelativeLayout and GridView based on those dimensions.
        ViewTreeObserver viewTreeObserver = gridView.getViewTreeObserver();
        if (viewTreeObserver.isAlive()) {
            viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    gridView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    width = gridView.getWidth();
                    height = width;
                    // Update the RelativeLayout first
                    RelativeLayout rl = (RelativeLayout) findViewById(R.id.board);
                    rl.getLayoutParams().height = height;
                    rl.invalidate();
                    // Update the Player size
                    ImageView player = (ImageView) findViewById(R.id.player);
                    player.setX((width/20)*2);
                    player.setY((height/20)*5);
                    player.getLayoutParams().width = width/20;
                    player.getLayoutParams().height = height/20;
                    // Remake the GridView
                    gridView.setAdapter(new BlockAdapter(thisClass));
                }
            });
        }

        setLevelData();

        // Give instructions if you are on level 1, 2, or 10
        Intent intent = getIntent();
        int levelNumber = intent.getIntExtra(Globals.LEVEL_NUM, 1);
        SharedPreferences settings = getSharedPreferences(Globals.PREFS_NAME, 0);
        int currentLevel = settings.getInt(Globals.CURRENT_LEVEL, Globals.FIRST_LEVEL);

        if (levelNumber == 1 && currentLevel == 1) {
            // TODO: what does the winning block look like?
            directionAlert("The Objective of this game is to get to the winning " +
                    "block in the shortest amount of steps.\n\nThe total number of steps you " +
                    "have is listed on the bottom of the screen. If you go over " +
                    "this number of steps you will lose and the level will restart.");
        } else if (levelNumber == 2 && currentLevel == 2) {
            // TODO: are the blocks always black?
            directionAlert("You cannot move through black blocks, only around them.");
        } else if (levelNumber == 10 && currentLevel == 10) {
            // TODO: are they always pink?
            directionAlert("Pink blocks can be moved in any direction. You cannot stack " +
                    "them, nor can you push two blocks at once.");
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

    private void setLevelData() {
        // Set level information
        Intent intent = getIntent();
        int levelNumber = intent.getIntExtra(Globals.LEVEL_NUM, 1);

        TextView levelNum = (TextView) findViewById(R.id.level_num);
        String levelNumText = "Level: " + levelNumber;
        levelNum.setText(levelNumText);

        // Get level information from level<levelNumber>.txt
        try {
            // Get the ID of the raw resource based on the level number
            Resources res = getResources();
            int rawID = res.getIdentifier("level" + Integer.toString(levelNumber), "raw", getPackageName());
            InputStream ins = getResources().openRawResource(rawID);

            /* File will contain lines of information in the following manner:
             * <name>, <author>, <steps>, <width>, <height>, <block layout>
             * Block layout will always have <height> lines of text, and <width>
             * integers within the line of text. The integers will be one of the following:
             * 0 - normal ground
             * 1 - unmovable block
             * 2 - movable block
             * 3 - finish
             * 4 - start
             */
            BufferedReader reader = new BufferedReader(new InputStreamReader(ins));

            TextView levelName = (TextView) findViewById(R.id.level_name);
            levelName.setText(reader.readLine());

            TextView levelAuthor = (TextView) findViewById(R.id.level_author);
            String levelAuthorText = "by: " + reader.readLine();
            levelAuthor.setText(levelAuthorText);

            TextView levelSteps = (TextView) findViewById(R.id.level_steps);
            String levelStepsText = "0/" + reader.readLine();
            levelSteps.setText(levelStepsText);

            // TODO: read other info
            reader.close();
            ins.close();
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    private void directionAlert(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("DIRECTIONS:");
        builder.setMessage(message);
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // Acknowledge instructions
            }
        });
        builder.create().show();
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
}
