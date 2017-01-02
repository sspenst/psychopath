package spencerspenst.psychopath;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
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
import java.util.Stack;

public class Play extends AppCompatActivity {
    private Play thisClass = this;
    private View mContentView;

    // Board variables
    private BlockAdapter blockAdapter;
    private GridView gridView;
    public static int boardSize;
    private static int blockSize;
    private int columns;
    private int rows;

    // Game variables
    private int levelNumber;
    private int totalSteps;
    private int steps;
    private int posX;
    private int posY;
    private String[][] type;
    private List<int[]> finishPositions;
    private List<ImageView> finishViews;
    private Stack<Undo> undoMoves = new Stack<>();

    /**
     * Represents the reverse of a move.
     * pull is true if a pink block was pushed with the original move.
     */
    private class Undo {
        int dx;
        int dy;
        boolean pull;

        Undo(int dx, int dy, boolean pull) {
            this.dx = dx;
            this.dy = dy;
            this.pull = pull;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);
        mContentView = findViewById(R.id.fullscreen_content);
        setVisibility();

        getLevelData();

        // Give instructions if you are on level 1, 2, or 10
        Intent intent = getIntent();
        levelNumber = intent.getIntExtra(Globals.LEVEL_NUM, 1);
        SharedPreferences settings = getSharedPreferences(Globals.PREFS_NAME, 0);
        int currentLevel = settings.getInt(Globals.CURRENT_LEVEL, Globals.FIRST_LEVEL);

        if (levelNumber == 1 && currentLevel == 1) {
            directionAlert("The objective of this game is to get to the finish " +
                    "block in the shortest amount of steps.\n\nThe total number of steps you " +
                    "have is listed on the bottom of the screen. If you go over " +
                    "this number of steps you will lose and the level will restart.");
        } else if (levelNumber == 2 && currentLevel == 2) {
            directionAlert("You cannot move through black blocks, only around them.");
        } else if (levelNumber == 10 && currentLevel == 10) {
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

                    // Remake the GridView
                    blockAdapter = new BlockAdapter(thisClass, columns, rows, type);
                    gridView.setAdapter(blockAdapter);

                    // LayoutParams for a block
                    RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(blockSize, blockSize);

                    // Create and add all finishes to the board
                    finishViews = new ArrayList<>();
                    for (int[] finishPosition : finishPositions) {
                        ImageView finish = new ImageView(thisClass);
                        finish.setImageResource(R.drawable.finish);
                        finish.setX(blockSize*finishPosition[0]);
                        finish.setY(blockSize*finishPosition[1]);
                        board.addView(finish, lp);
                        finishViews.add(finish);
                    }

                    // Create the player so that it appears above the finish
                    ImageView player = new ImageView(thisClass);
                    player.setId(R.id.player);
                    player.setImageResource(R.drawable.ball);
                    player.setX(blockSize*posX);
                    player.setY(blockSize*posY);
                    board.addView(player, lp);
                }
            });
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
        builder.setCancelable(false);
        builder.create().show();
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
            levelSteps.setTextColor(Color.WHITE);

            columns = Integer.valueOf(reader.readLine());
            rows = Integer.valueOf(reader.readLine());
            type = new String[rows][columns];
            finishPositions = new ArrayList<>();
            for (int i = 0; i < rows; i++) {
                //String[] text = reader.readLine().split(" ");
                String line = reader.readLine();
                for (int j = 0; j < columns; j++) {
                    // Store "0" instead of "3" or "4", and store the start and
                    // finish positions in their appropriate data structures
                    switch(line.substring(j,j+1)) {
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
                            type[i][j] = line.substring(j,j+1);
                    }
                }
            }

            reader.close();
            ins.close();
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    // Update the position of the player on the screen
    private void updatePlayer() {
        ImageView player = (ImageView) findViewById(R.id.player);
        player.setX((blockSize)*posX);
        player.setY((blockSize)*posY);
    }

    // Passing true increments steps, false decrements
    private void updateSteps(boolean increment) {
        if (increment) steps++;
        else steps--;

        String levelStepsText = steps + "/" + totalSteps;
        TextView levelSteps = (TextView) findViewById(R.id.level_steps);
        levelSteps.setText(levelStepsText);

        if (steps > totalSteps) levelSteps.setTextColor(Color.RED);
        else levelSteps.setTextColor(Color.WHITE);
    }

    // Display an Alert if the player is on one of the finish blocks
    private void checkWin() {
        for (int[] finishPosition : finishPositions) {
            if (finishPosition[0] == posX && finishPosition[1] == posY) {
                if (steps == totalSteps) {
                    // Increment level beaten count
                    SharedPreferences settings = getSharedPreferences(Globals.PREFS_NAME, 0);
                    final int currentLevel = settings.getInt(Globals.CURRENT_LEVEL, Globals.FIRST_LEVEL);
                    if (levelNumber == currentLevel && currentLevel < Globals.TOTAL_LEVELS) {
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putInt(Globals.CURRENT_LEVEL, currentLevel + 1);
                        editor.apply();
                    }

                    TextView levelName = (TextView) findViewById(R.id.level_name);

                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setMessage("Congratulations, you beat \"" + levelName.getText() + "\"!");
                    builder.setPositiveButton("Next Level", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            Intent intent = new Intent(thisClass, Play.class);
                            intent.putExtra(Globals.LEVEL_NUM, levelNumber + 1);
                            startActivity(intent);
                        }
                    });
                    builder.setNegativeButton("Level Select", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            startActivity(new Intent(thisClass, LevelSelect.class));
                        }
                    });
                    builder.setCancelable(false);
                    builder.create().show();
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setMessage("You completed the level with " + (steps - totalSteps) + " extra steps.");
                    builder.setPositiveButton("Restart", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            restart(null);
                        }
                    });
                    builder.setNegativeButton("Continue", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // Keep playing the level
                        }
                    });
                    builder.setCancelable(false);
                    builder.create().show();
                } // TODO: should something happen if the user has steps < totalSteps?
            }
        }
    }

    // Return true if a point is within the bounds of the board.
    private boolean validPoint(int x, int y) {
        return x >= 0 && x < columns && y >= 0 && y < rows;
    }

    /**
     * Move the player.
     * @param dx the change in x position
     * @param dy the change in y position
     */
    private void move(int dx, int dy) {
        int newPosX = posX + dx;
        int newPosY = posY + dy;
        if (!validPoint(newPosX, newPosY)) return;
        boolean moveMade = false;

        switch(type[newPosY][newPosX]) {
            case "0":
                undoMoves.push(new Undo(-dx, -dy, false));
                moveMade = true;
                posX = newPosX;
                posY = newPosY;
                break;
            case "1":
                break;
            case "2":
                // Check if there is space for the block to move
                int pushPosX = newPosX + dx;
                int pushPosY = newPosY + dy;
                if (!validPoint(pushPosX, pushPosY)) return;
                if (type[pushPosY][pushPosX].equals("0")) {
                    undoMoves.push(new Undo(-dx, -dy, true));
                    moveMade = true;
                    posX = newPosX;
                    posY = newPosY;
                    type[newPosY][newPosX] = "0";
                    type[pushPosY][pushPosX] = "2";
                    // Update the two blocks that changed
                    GridView levelGrid = (GridView) findViewById(R.id.level_grid);
                    ImageView normalBlock = (ImageView) levelGrid.getChildAt(newPosY * columns + newPosX);
                    normalBlock.setImageResource(R.drawable.ground);
                    ImageView movableBlock = (ImageView) levelGrid.getChildAt(pushPosY * columns + pushPosX);
                    movableBlock.setImageResource(R.drawable.movable);

                    // Change visibility of finish blocks if movable blocks are covering them
                    for (int i = 0; i < finishPositions.size(); i++) {
                        if (finishPositions.get(i)[0] == newPosX && finishPositions.get(i)[1] == newPosY) {
                            finishViews.get(i).setVisibility(View.VISIBLE);
                        } else if (finishPositions.get(i)[0] == pushPosX && finishPositions.get(i)[1] == pushPosY) {
                            finishViews.get(i).setVisibility(View.INVISIBLE);
                        }
                    }
                }
                break;
        }

        if (moveMade) {
            updatePlayer();
            updateSteps(true);
            checkWin();
            incSteps();
        }
    }

    // Increment the total step count
    private void incSteps() {
        SharedPreferences settings = getSharedPreferences(Globals.PREFS_NAME, 0);
        int stepCount = settings.getInt(Globals.STEP_COUNT, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt(Globals.STEP_COUNT, stepCount + 1);
        editor.apply();
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

    public void levelSelect(View view) {
        // TODO: is there any cleanup that needs to be done when you leave Play.class?
        startActivity(new Intent(this, LevelSelect.class));
    }

    public void restart(View view) {
        undoMoves.clear();
        getLevelData();
        blockAdapter = new BlockAdapter(this, columns, rows, type);
        gridView.setAdapter(blockAdapter);
        updatePlayer();

        for (ImageView finishView : finishViews) {
            finishView.setVisibility(View.VISIBLE);
        }

        // Increment the number of restarts
        SharedPreferences settings = getSharedPreferences(Globals.PREFS_NAME, 0);
        int restarts = settings.getInt(Globals.RESTARTS, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt(Globals.RESTARTS, restarts + 1);
        editor.apply();
    }

    public void undo(View view) {
        if (!undoMoves.isEmpty()) {
            Undo undoMove = undoMoves.pop();

            // Find the current position of the movable block
            int movableX = posX - undoMove.dx;
            int movableY = posY - undoMove.dy;

            // Put the movable block at the current position
            if (undoMove.pull) {
                GridView levelGrid = (GridView) findViewById(R.id.level_grid);
                ImageView normalBlock = (ImageView) levelGrid.getChildAt(movableY * columns + movableX);
                normalBlock.setImageResource(R.drawable.ground);
                type[movableY][movableX] = "0";
                ImageView movableBlock = (ImageView) levelGrid.getChildAt(posY * columns + posX);
                movableBlock.setImageResource(R.drawable.movable);
                type[posY][posX] = "2";

                // Change visibility of finish blocks if movable blocks are covering them
                for (int i = 0; i < finishPositions.size(); i++) {
                    if (finishPositions.get(i)[0] == movableX && finishPositions.get(i)[1] == movableY) {
                        finishViews.get(i).setVisibility(View.VISIBLE);
                    } else if (finishPositions.get(i)[0] == posX && finishPositions.get(i)[1] == posY) {
                        finishViews.get(i).setVisibility(View.INVISIBLE);
                    }
                }
            }

            // Undo the move
            posX += undoMove.dx;
            posY += undoMove.dy;
            updatePlayer();
            updateSteps(false);
            checkWin();
            incSteps();
        }
    }
}
