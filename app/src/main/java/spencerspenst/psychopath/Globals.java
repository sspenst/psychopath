package spencerspenst.psychopath;

import android.app.Activity;
import android.content.Intent;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

class Globals {
    // Retrieving level information
    final static String LEVEL_NUM = "spencerspenst.psychopath.LEVEL_NUM";
    final static String LEVEL_NAME = "spencerspenst.psychopath.LEVEL_NAME";
    final static String LEVEL_AUTHOR = "spencerspenst.psychopath.LEVEL_AUTHOR";
    final static String LEVEL_STEPS = "spencerspenst.psychopath.LEVEL_STEPS";
    private final static int FILE_LINES_PER_LEVEL = 3;

    // SharedPreferences constants
    final static String PREFS_NAME = "MyPrefsFile";
    final static String CURRENT_LEVEL = "currentLevel";

    // Miscellaneous constants
    final static int FIRST_LEVEL = 1;
    final static int TOTAL_LEVELS = 71;

    /**
     * Creates an Intent with the information needed to start a level
     * @param activity the Activity that called this method
     * @param level the level number that is being loaded
     * @return Intent with all needed information
     */
    static Intent createPlayIntent(Activity activity, int level) {
        Intent intent = new Intent(activity, Play.class);
        intent.putExtra(LEVEL_NUM, level);

        // Get level information from level_data.txt based on which
        // button was pressed (based on the value of position)
        try {
            String str;
            int count = 0;
            InputStream ins = activity.getResources().openRawResource(R.raw.level_data);
            BufferedReader reader = new BufferedReader(new InputStreamReader(ins));
            while((str = reader.readLine()) != null) {
                if (count == (level - 1) * FILE_LINES_PER_LEVEL) {
                    intent.putExtra(LEVEL_NAME, str);
                    str = reader.readLine();
                    intent.putExtra(LEVEL_AUTHOR, str);
                    str = reader.readLine();
                    intent.putExtra(LEVEL_STEPS, str);
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
