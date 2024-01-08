/*----------------------------------------------------------------
 *  Authors:  K. Walsh and Daniel Chavez
 *  Email:    kwalsh@holycross.edu and dachav26@g.holycross.edu
 *  Written:  7/13/2015
 *  Edited:   12/08/2022 
 * 
 *  Minesweeper game. This class implements the game window and most
 *  of the game logic.
 *----------------------------------------------------------------*/

import GUI.*;

/**
 * A <i>Game</i> object manages all information about a minesweeper game as it
 * is being played and displayed on the screen. This includes information about
 * all of the cells (this is stored in a 2-D array of Cell objects), how many
 * flags have been planted, how many mines have been deployed, etc. Game extends
 * Window so it can be drawn on the screen. It also extends EventListener, so it
 * can respond to user interaction.
 */
public class Game extends Window implements EventListener {

    /**
     * Number of cells tall the game board will be.
     */
    public static final int NUM_ROWS = 20;

    /**
     * Number of cells wide the game board will be.
     */
    public static final int NUM_COLS = 30;

    // Example game screen layout:
    // +---------------------------------------------------------+
    // |      M A R G I N = 50                                   |
    // | M  + - - - - - - - - - - - - - - - - - - - - - - - + M  |
    // | A  |                                               | A  |
    // | R  |                                               | R  |
    // | G  |                Grid of Cells                  | G  |
    // | I  |                                               | I  |
    // | N  |                                               | N  |
    // | =  |       600 = NUM_COLS * Cell.SIZE wide         | =  |
    // | 50 |                      by                       | 50 |
    // |    |       400 = NUM_ROWS * Cell.SIZE tall         |    |
    // |    |                                               |    |
    // |    |                                               |    |
    // |    |                                               |    |
    // |    + - - - - - - - - - - - - - - - - - - - - - - - +    |
    // |            SPACE     S   SPACE   S    SPACE             |
    // |    + - - - - - - - + P + - - - + P + - - - - - - - +    |
    // |    |    Status     | A | Timer | A |     Help      |    |
    // |    |       Box     | C |       | C |      Box      |    |
    // |    + - - - - - - - + E + - - - + E + - - - - - - - +    |
    // |     M A R G I N = 50                                    |
    // +-- ------------------------------------------------------+

    /**
     * Width of the game window, in pixels.
     * Equal to 2*MARGIN + GRID_WIDTH
     * or 2*MARGIN + 2*SPACE + StatusBox.WIDTH, Timer.WIDTH, HelpBox.WIDTH,
     * whichever is larger.
     */
    public static final int WIDTH = 700;

    /**
     * Height of the game window, in pixels.
     * Equal to 2*MARGIN + SPACE
     *     + GRID_HEIGHT
     *     + max(StatusBox.Height, Timer.HEIGHT, HelpBox.HEIGHT)
     */
    public static final int HEIGHT = 600; 

    /**
     * Width of the grid part of the window, in pixels.
     * Equal to NUM_COLS * Cell.SIZE.
     */
    public static final int GRID_WIDTH = NUM_COLS * Cell.SIZE;

    /**
     * Height of the grid part of the window, in pixels.
     * Equal to NUM_ROWS * Cell.SIZE.
     */
    public static final int GRID_HEIGHT = NUM_ROWS * Cell.SIZE;

    /**
     * Margin around the edges of the canvas.
     */
    private static final int MARGIN = 50;

    /**
     * Space between elements on the canvas.
     */
    private static final int SPACE = 25;

    // A 2-D array of Cell objects to keep track of the board state.
    private Cell[][] cells = new Cell[NUM_ROWS][NUM_COLS];

    private int numMines = 0;    // number of mines deployed
    private int numRevealed = 0; // number of cells revealed so far

    // Whether or not the game has been won.
    private boolean gameWon = false;

    // Whether or not the game has been lost
    private boolean gameLost = false;

    // Name of the user playing the game.
    private String username;

    // The difficulty level of the game, used for tracking top scores.
    private String difficulty;

    // The status box that appears in the top left.
    private StatusBox status;

    // The timer that appears in the top middle.
    private Timer timer;

    // The help box that appears in the top right.
    private HelpBox help;

    /**
     * Constructor: Initializes a new game, but does not deploy any mines, plant
     * any flags, etc. The difficulty is either "easy", "medium", or "hard", and
     * will be used to load the proper top scores file. Name is used as the
     * user's name.
     */
    public Game(String name, String difficulty) {
        super("Minesweeper!", WIDTH, HEIGHT);

        this.username = name;
        this.difficulty = difficulty;

        // Create the background
        setBackgroundColor(Canvas.DARK_GRAY);

        // Create a border around the grid
        Box border = new Box(MARGIN-1.5, MARGIN-1.5, GRID_WIDTH+3, GRID_HEIGHT+3);
        border.setBackgroundColor(null);
        border.setBorderColor(Canvas.BLACK);
        add(border);

        // Create the info boxes
        help = new HelpBox(
                WIDTH - MARGIN - HelpBox.WIDTH,
                HEIGHT - MARGIN - HelpBox.HEIGHT);
        add(help);

        timer = new Timer ( //Creates new Timer and Timer box widget for the game
            MARGIN + MARGIN / 2 + StatusBox.WIDTH, HEIGHT - MARGIN - Timer.HEIGHT);
        add(timer);

       status = new StatusBox ( //Creates new "Status Box" which shows the number of cells remaining and number of mines deployed
            this, MARGIN, HEIGHT - MARGIN - StatusBox.HEIGHT);
        add(status); 

        for(int row = 0; row < NUM_ROWS; row++) { //Creates a grid of cells (30 x 20) and takes the cells from Cell.java
            for (int col = 0; col < NUM_COLS; col++) {
                cells[row][col] = new Cell(MARGIN + Cell.SIZE * col, MARGIN + Cell.SIZE * row);
                add(cells[row][col]);
            }
        }
    }

    /**
     * Get the number of mines that are deployed.
     */
    public int getNumMinesDeployed() {
        return numMines;
    }

    /**
     * Get the number of hidden cells remaining to be revealed.
     */
    public int getNumCellsRemaining() {
        return NUM_ROWS * NUM_COLS - numRevealed;
    }

    /**
     * Deploy the given number of mines. This gets called once during game
     * setup. The game doesn't actually begin officially until the user clicks
     * a cell, so the timer should not start yet.
     */
    public void deployMines(int mines) { //Deploys number of mines within the grid of cells in random positions
        int n = 0;
        while (n < mines) {
            int r = StdRandom.uniform(0, NUM_ROWS);
            int c = StdRandom.uniform(0, NUM_COLS);

            if (!cells[r][c].isMine()) {
                cells[r][c].makeMine();
                numMines++; //Increment number of mines for status box
                n++;
 
               if(r > 0) {
                   cells[r-1][c].incrementNeighborMineCount();
                }
                if (r + 1 < NUM_ROWS) {
                    cells[r+1][c].incrementNeighborMineCount();
                }
                if (c+1 < NUM_COLS) {
                    cells[r][c+1].incrementNeighborMineCount();
                }
                if(r+1 < NUM_ROWS && c+1 < NUM_COLS ) {
                    cells[r+1][c+1].incrementNeighborMineCount();
                }
                if (r > 0 && c + 1 < NUM_COLS) {
                    cells[r-1][c+1].incrementNeighborMineCount();
                }
                if (c > 0) { 
                    cells[r][c-1].incrementNeighborMineCount();
                }
                if (r > 0 && c > 0) { 
                    cells[r-1][c-1].incrementNeighborMineCount();
                }                
                if(r + 1 < NUM_ROWS && c > 0) {
                    cells[r+1][c-1].incrementNeighborMineCount();
                }
            }
        }
    }

    public void showAllMines() { //Shows all of the mines at the end of the game whether you won or lost 
        if (gameLost || gameWon) {
            for (int i = 0; i < NUM_ROWS; i++) {
                for (int j = 0; j < NUM_COLS; j++) {
                    if (cells[i][j].isMine()) {
                        cells[i][j].reveal();
                    }
                }   
            }
        }
    }

    public void autoRevealCells(int row, int col) { //Recursion part of the game in which it will reveal all cells until there are neighboring cells nearby
        if(row < NUM_ROWS && col < NUM_COLS && row >= 0 && col >= 0) { //Makes sure it is within bounds of the grid
            if (!cells[row][col].coastIsClear() && !(cells[row][col].isRevealed())) { //Reveals one cell if their are neighboring mines nearby
                cells[row][col].reveal();
                numRevealed++;
                return;
            } else if (cells[row][col].coastIsClear() && !(cells[row][col].isRevealed())) { //Reveals all cells nearby until to hits nearby mines, then it stops
                cells[row][col].reveal();
                numRevealed++;
                
                autoRevealCells(row + 1, col + 1);
                autoRevealCells(row + 1, col);
                autoRevealCells(row + 1, col - 1);
                autoRevealCells(row, col + 1);
                autoRevealCells(row, col - 1);
                autoRevealCells(row - 1, col + 1);
                autoRevealCells(row - 1, col);
                autoRevealCells(row - 1, col - 1);
            }
        }
    }

    /**
     * Respond to a mouse click. This function will be called each time the user
     * clicks on the game window. The x, y parameters indicate the screen
     * coordinates where the user has clicked, and the button parameter
     * indicates which mouse button was clicked (either "left", "middle", or
     * "right"). The function should update the game state according to what the
     * user has clicked.
     * @param x the x coordinate where the user clicked, in pixels.
     * @param y the y coordinate where the user clicked, in pixels.
     * @param button either "left", "middle", or "right".
     */

    public void mouseClicked(double x, double y, String button) {
        // User clicked the mouse, see what they want to do.
        // If game is over, then ignore the mouse click.
        
        if (gameWon || gameLost) {
            return;
        }
        timer.startCounting();
    
        // If the user middle-clicked, ignore it.
        if (!button.equals("left") && !button.equals("right"))
            return;

        // If the user clicked outside of the game grid, ignore it.
        if (x < MARGIN || y < MARGIN
                || x >= MARGIN + GRID_WIDTH || y >= MARGIN + GRID_HEIGHT) {
                return;
        }

        // Calculate which cell the user clicked.
        int row = (int)((y - MARGIN) / Cell.SIZE);
        int col = (int)((x - MARGIN) / Cell.SIZE);

        if (!cells[row][col].isMine() && !cells[row][col].isRevealed()) { //If the cells are not a mine and it is not revealed, reveal them by calling autoRevealCells() function 
            autoRevealCells(row, col);
            StdOut.printf("You clicked row %d column %d with button %s\n", row, col, button);
            
            if (numRevealed == (NUM_ROWS * NUM_COLS) - getNumMinesDeployed()) { //Checks if the number of mines and the number of cells are the same and if they are, then the game ends and you've WON
                cells[row][col].reveal();
                gameWon = true;
                timer.stopCounting();
                showAllMines();
                StdOut.println("Yay! You have won the game!" );
            }
        } else if(cells[row][col].isMine() && !cells[row][col].isRevealed()) { //Checks if the user clicked is a mine and if it is, then reveal the mine and end the game with GAME LOST
            cells[row][col].reveal();
            gameLost = true;
            timer.stopCounting();
            showAllMines();
            StdOut.println("Sorry you have lost the game...");
        }
    }

    /**
     * Respond to key presses. This function will be called each time the user
     * presses a key. The parameter indicates the character the user pressed.
     * The function should update the game state according to what character the
     * user has pressed. 
     * @param c the character that was typed.
     */
    public void keyTyped(char c)
    {
        // User pressed a key, see what they want to do.
        switch (c) {
            case 'q': 
            case 'Q': 
                hide(); // user wants to quit
                break;
            default:
                break; // anything else is ignored
        }
    }

    /**
     * Paint the background for this window on the canvas. Don't call this
     * directly, it is called by the GUI system automatically. This function
     * should draw something on the canvas, if you like. Or the background can
     * be blank.
     * @param canvas the canvas on which to draw.
     */
    public void repaintWindowBackground(GUI.Canvas canvas) {
        setBackgroundColor(canvas.MAGENTA);
    }
}