package spencerspenst.psychopath;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

public class Settings extends AppCompatActivity {
    private View mContentView;
    private Settings thisClass = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        mContentView = findViewById(R.id.fullscreen_content);
        setVisibility();

        // Get stats from SharedPreferences
        SharedPreferences settings = getSharedPreferences(Globals.PREFS_NAME, 0);
        int stepCount = settings.getInt(Globals.STEP_COUNT, 0);
        int restarts = settings.getInt(Globals.RESTARTS, 0);

        // Add the stats to the layout
        LinearLayout statContainer = (LinearLayout) findViewById(R.id.stat_container);
        TextView stepCountView = new TextView(this);
        stepCountView.setText("Total steps taken: " + stepCount);
        stepCountView.setTextColor(Color.WHITE);
        stepCountView.setGravity(TextView.TEXT_ALIGNMENT_CENTER);
        statContainer.addView(stepCountView);

        TextView restartsView = new TextView(this);
        restartsView.setText("Total levels restarted: " + restarts);
        restartsView.setTextColor(Color.WHITE);
        restartsView.setGravity(TextView.TEXT_ALIGNMENT_CENTER);
        statContainer.addView(restartsView);
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

    public void resetProgress(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("Reset Progress");
        builder.setMessage("Are you sure you want to reset your progress?\n" +
                "NOTE: This will reset all statistics as well.");

        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                SharedPreferences settings = getSharedPreferences(Globals.PREFS_NAME, 0);
                SharedPreferences.Editor editor = settings.edit();
                editor.putInt(Globals.CURRENT_LEVEL, Globals.FIRST_LEVEL);
                editor.putInt(Globals.STEP_COUNT, 0);
                editor.putInt(Globals.RESTARTS, 0);
                editor.apply();
                startActivity(new Intent(thisClass, Settings.class));
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User cancelled the dialog
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void unlock(View view) {
        // TODO: remove this option if there is going to be a game center leaderboards thing
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("Unlock All Levels");
        builder.setMessage("Are you sure you want to unlock all levels?");

        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                SharedPreferences settings = getSharedPreferences(Globals.PREFS_NAME, 0);
                SharedPreferences.Editor editor = settings.edit();
                editor.putInt(Globals.CURRENT_LEVEL, Globals.TOTAL_LEVELS);
                editor.apply();
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User cancelled the dialog
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }
}