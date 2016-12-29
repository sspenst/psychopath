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
import java.util.ArrayList;
import java.util.List;

public class Play extends AppCompatActivity {
    private Play thisClass = this;
    private View mContentView;

    // Board variables
    private GridView gridView;
    public static int boardSize;
    private static int blockSize;
    private int columns;
    private int rows;

    // Game variables
    private int totalSteps;
    private int steps;
    private int posX;
    private int posY;
    private String[][] type;
    private List<int[]> finishPositions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO: figure out how to do animations without freezing the phone
        //overridePendingTransition(R.anim.slide_in, 0);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);
        mContentView = findViewById(R.id.fullscreen_content);
        setVisibility();

        getLevelData();

        // Give instructions if you are on level 1, 2, or 10
        Intent intent = getIntent();
        int levelNumber = intent.getIntExtra(Globals.LEVEL_NUM, 1);
        SharedPreferences settings = getSharedPreferences(Globals.PREFS_NAME, 0);
        int currentLevel = settings.getInt(Globals.CURRENT_LEVEL, Globals.FIRST_LEVEL);

        if (levelNumber == 1 && currentLevel == 1) {
            // TODO: what does the winning block look like?
            directionAlert("The objective of this game is to get to the winning " +
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

        gridView = (GridView) findViewById(R.id.level_grid);
        gridView.setAdapter(new BlockAdapter(this, columns, rows, type));

        // Wait until the entire layout has been made, then get the dimensions of the GridView.
        // Remake the RelativeLayout and GridView based on those dimensions.
        ViewTreeObserver viewTreeObserver = gridView.getViewTreeObserver();
        if (viewTreeObserver.isAlive()) {
            viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    // Get the recently-computed board size
                    gridView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    gridView.setNumColumns(columns);
                    boardSize = gridView.getWidth();
                    blockSize = columns > rows ? boardSize/columns : boardSize/rows;
                    // Dynamically adjust height of the boardSquare area to be the same as the width
                    RelativeLayout boardSquare = (RelativeLayout) findViewById(R.id.board_square);
                    boardSquare.getLayoutParams().height = boardSize;
                    // Update the width or height of the board depending on which is smaller
                    RelativeLayout board = (RelativeLayout) findViewById(R.id.board);
                    if (rows > columns) {
                        board.getLayoutParams().width = boardSize * columns / rows;
                    } else {
                        board.getLayoutParams().height = boardSize * rows / columns;
                    }
                    board.invalidate();

                    draw(true);

                    // Create and add all finishes to the board
                    for (int[] finishPosition : finishPositions) {
                        ImageView finish = new ImageView(thisClass);
                        finish.setImageResource(R.drawable.finish);
                        finish.setX(blockSize*finishPosition[0]);
                        finish.setY(blockSize*finishPosition[1]);
                        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(blockSize, blockSize);
                        RelativeLayout rl = (RelativeLayout) findViewById(R.id.board);
                        rl.addView(finish, lp);
                    }
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

    /**
     * Get all information about the level from the appropriate
     * file and format it into proper data structures.
     */
    private void getLevelData() {
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
             * <name>, <author>, <steps>, <columns>, <rows>, <block layout>
             * Block layout will always have <height> lines of text, and <width> integers
             * within the line of text.
             */
            BufferedReader reader = new BufferedReader(new InputStreamReader(ins));

            TextView levelName = (TextView) findViewById(R.id.level_name);
            levelName.setText(reader.readLine());

            String levelAuthorText = "by: " + reader.readLine();
            TextView levelAuthor = (TextView) findViewById(R.id.level_author);
            levelAuthor.setText(levelAuthorText);

            steps = 0;
            totalSteps = Integer.valueOf(reader.readLine());
            String levelStepsText = steps + "/" + totalSteps;
            TextView levelSteps = (TextView) findViewById(R.id.level_steps);
            levelSteps.setText(levelStepsText);

            columns = Integer.valueOf(reader.readLine());
            rows = Integer.valueOf(reader.readLine());
            type = new String[rows][columns];
            finishPositions = new ArrayList<int[]>();
            for (int i = 0; i < rows; i++) {
                String[] text = reader.readLine().split(" ");
                for (int j = 0; j < columns; j++) {
                    // Store "0" instead of "3" or "4", and store the start and
                    // finish positions in their appropriate data structures
                    switch(text[j]) {
                        case "3":
                            int[] finishPosition = {j, i};
                            finishPositions.add(finishPosition);
                            type[i][j] = "0";
                            break;
                        case "4":
                            posX = j;
                            posY = i;
                            type[i][j] = "0";
                            break;
                        default:
                            type[i][j] = text[j];
                    }
                }
            }
            reader.close();
            ins.close();
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    /**
     * Draw the grid and all of its components.
     * @param newGrid true if the grid needs to be updated
     */
    private void draw(boolean newGrid) {
        // Remake the GridView
        gridView.setAdapter(new BlockAdapter(this, columns, rows, type));

        // Update the player location and size depending on which
        ImageView player = (ImageView) findViewById(R.id.player);
        player.setX((blockSize)*posX);
        player.setY((blockSize)*posY);
        player.getLayoutParams().width = blockSize;
        player.getLayoutParams().height = blockSize;
    }

    private void checkWin() {
        for (int[] finishPosition : finishPositions) {
            if (finishPosition[0] == posX && finishPosition[1] == posY) {
                // TODO: alert
            }
        }
    }

    /**
     * Move the player.
     * @param x the change in x position
     * @param y the change in y position
     */
    private void move(int x, int y) {
        int newPosX = posX + x;
        int newPosY = posY + y;
        if (newPosX < 0 || newPosX == columns || newPosY < 0 || newPosY == rows) return;
        switch(type[newPosY][newPosX]) {
            case "0":
                posX = newPosX;
                posY = newPosY;
                incrementSteps();
                draw(false);
                checkWin();
                break;
            case "1":
                break;
            case "2":
                // Check if there is space for the block to move
                if (type[newPosY + y][newPosX + x].equals("0")) {
                    posX = newPosX;
                    posY = newPosY;
                    type[newPosY][newPosX] = "0";
                    type[newPosY + y][newPosX + x] = "2";
                    incrementSteps();
                    draw(true);
                    checkWin();
                }
                break;
        }
    }

    private void incrementSteps() {
        steps++;
        String levelStepsText = steps + "/" + totalSteps;
        TextView levelSteps = (TextView) findViewById(R.id.level_steps);
        levelSteps.setText(levelStepsText);
    }

    public void left(View view) {
        move(-1, 0);
    }

    public void up (View view) {
        move(0, -1);
    }

    public void down (View view) {
        move(0, 1);
    }

    public void right (View view) {
        move(1, 0);
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
