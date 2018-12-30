package spencerspenst.psychopath;

class Globals {
    // Retrieving level information
    final static String LEVEL_NUM = "spencerspenst.psychopath.LEVEL_NUM";
    // TODO: change this so that there is an UNLOCKED_LEVEL and a LB_LEVEL
    // LB_LEVEL should only be incremented when the user beats the lowest unbeaten level
    // UNLOCKED_LEVEL is the same as LB_LEVEL, unless the user has unlocked all levels from the settings
    // resetting from the settings puts LB_LEVEL to 0 and UNLOCKED_LEVEL to 1

    // SharedPreferences constants
    final static String PREFS_NAME = "spencerspenst.psychopath.prefs";
    final static String CURRENT_LEVEL = "spencerspenst.psychopath.currentLevel";
    final static String STEP_COUNT = "spencerspenst.psychopath.stepCount";
    final static String RESTARTS = "spencerspenst.psychopath.restarts";

    // Miscellaneous constants
    final static int FIRST_LEVEL = 1;
    final static int TOTAL_LEVELS = 71;
}
