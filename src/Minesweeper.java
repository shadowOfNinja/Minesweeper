import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Random;
import javax.swing.*;

public class Minesweeper {
    final int MAX_WINDOW_WIDTH = 800;
    final int MAX_WINDOW_HEIGHT = 800;

    private class MineTile extends JButton {
        int r;
        int c;

        MineTile(int r, int c) {
            this.r = r;
            this.c = c;
        }
    }

    int numRows = 8;
    int numCols = numRows;
    int tileSize = Math.min(MAX_WINDOW_WIDTH / numCols, MAX_WINDOW_HEIGHT / numRows);
    int boardWidth = numCols * tileSize;
    int boardHeight = numRows * tileSize;

    double textScale; // Scale for text size

    Image mineIcon;
    Image flagIcon;
    Image flaggedMineIcon;
    
    JFrame frame = new JFrame("Minesweeper");
    JPanel textPanel = new JPanel();
    JPanel boardPanel = new JPanel();

    int minecount = 10; // Number of mines
    int flagCount = 0; // Count of flags placed
    MineTile[][] board = new MineTile[numRows][numCols];
    ArrayList<MineTile> mineList = new ArrayList<>();
    Random random = new Random();

    int tilesClicked = 0; //goal is to click all tiles without mines
    boolean gameOver = false;

    JLabel mineCountLabel = new JLabel();
    JButton resetButton = new JButton(); // You can use an icon or emoji here
    JLabel timerLabel = new JLabel("00:00");

    Timer timer;
    int elapsedSeconds = 0;

    Minesweeper() {
        frame.setSize(boardWidth, boardHeight);
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        mineCountLabel.setFont(new Font("Arial", Font.BOLD, 25));
        mineCountLabel.setHorizontalAlignment(JLabel.CENTER);
        mineCountLabel.setText("Mines: " + minecount);

        resetButton.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 25));
        resetButton.setText("😊"); // Reset button with an emoji
        resetButton.setFocusable(false);
        resetButton.addActionListener(e -> resetGame());

        // Timer setup (start when game starts)
        timer = new Timer(1000, e -> {
            elapsedSeconds++;
            int minutes = elapsedSeconds / 60;
            int seconds = elapsedSeconds % 60;
            timerLabel.setText(String.format("%02d:%02d", minutes, seconds));
        });

        timerLabel.setFont(new Font("Arial", Font.BOLD, 25));
        timerLabel.setHorizontalAlignment(JLabel.CENTER);

        // Use a GridBagLayout for flexible arrangement
        textPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 10, 0, 10);
        gbc.gridy = 0;

        gbc.gridx = 0;
        textPanel.add(mineCountLabel, gbc);

        gbc.gridx = 1;
        textPanel.add(resetButton, gbc);

        gbc.gridx = 2;
        textPanel.add(timerLabel, gbc);

        frame.add(textPanel, BorderLayout.NORTH);

        boardPanel.setLayout(new GridLayout(numRows, numCols));
        frame.add(boardPanel);

        updateTextScale(); // Initialize text scale based on default difficulty

        //load images
        mineIcon = new ImageIcon(getClass().getResource("./Mine.png")).getImage().getScaledInstance(tileSize - 10, tileSize - 10, Image.SCALE_SMOOTH);
        flagIcon = new ImageIcon(getClass().getResource("./Flag.png")).getImage().getScaledInstance(tileSize - 10, tileSize - 10, Image.SCALE_SMOOTH);
        flaggedMineIcon = new ImageIcon(getClass().getResource("./FlaggedMine.png")).getImage().getScaledInstance(tileSize - 10, tileSize - 10, Image.SCALE_SMOOTH);

        for (int r = 0; r < numRows; r++) {
            for (int c = 0; c < numCols; c++) {
                MineTile tile = createAndAddTile(r, c);
                board[r][c] = tile;
                boardPanel.add(tile);
            }
        }
        frame.setAlwaysOnTop(true);
        frame.setVisible(true);

        // Create menu bar
        JMenuBar menuBar = new JMenuBar();
        JMenu gameMenu = new JMenu("Game");

        JMenuItem beginnerItem = new JMenuItem("Beginner (8x8, 10 mines)");
        JMenuItem intermediateItem = new JMenuItem("Intermediate (16x16, 40 mines)");
        JMenuItem expertItem = new JMenuItem("Expert (24x24, 99 mines)");

        gameMenu.add(beginnerItem);
        gameMenu.add(intermediateItem);
        gameMenu.add(expertItem);
        menuBar.add(gameMenu);
        frame.setJMenuBar(menuBar);

        // Add action listeners for difficulty changes
        beginnerItem.addActionListener(e -> setDifficulty(8, 8, 10));
        intermediateItem.addActionListener(e -> setDifficulty(16, 16, 40));
        expertItem.addActionListener(e -> setDifficulty(24, 24, 99));

        setMines();
    }

    void setMines() {
        mineList = new ArrayList<MineTile>();

        int mineLeft = minecount;
        while (mineLeft > 0) {
            int r = random.nextInt(numRows);
            int c = random.nextInt(numCols);
            MineTile tile = board[r][c];
            if (!mineList.contains(tile)) {
                mineList.add(tile);
                mineLeft--;
            }
        }

    }

    void revealMines() {
        for (MineTile mine : mineList) {
            if (mine.getIcon() != null) {
                mine.setIcon(new ImageIcon(flaggedMineIcon)); // Show flag icon for mines
            }
            else {
                mine.setText(""); // Clear text
                mine.setIcon(new ImageIcon(mineIcon));
            }
        }
        gameOver = true;
        resetButton.setText("😢"); // Change reset button to a sad face
        mineCountLabel.setText("You Lost!"); // Update mine count label to indicate a loss
        timer.stop();
    }

    void checkMine(int r, int c) {
        if (r < 0 || r >= numRows || c < 0 || c >= numCols) {
            return;
        }
        
        MineTile tile = board[r][c];
        if (!tile.isEnabled()) {
            return; // Tile already checked
        }

        // If the tile is flagged, remove the flag and update the count before disabling
        if (tile.getIcon() != null) {
            tile.setIcon(null);
            flagCount = Math.max(0, flagCount - 1);
            mineCountLabel.setText("Mines: " + (minecount - flagCount));
        }

        tile.setEnabled(false);
        tilesClicked++;

        int minesFound = 0;

        //top 3
        minesFound += countMine(r-1, c-1); // top left
        minesFound += countMine(r-1, c);   // top
        minesFound += countMine(r-1, c+1); // top right

        //left and right
        minesFound += countMine(r, c-1);   // left
        minesFound += countMine(r, c+1);   // right

        //bottom 3
        minesFound += countMine(r+1, c-1); // bottom left
        minesFound += countMine(r+1, c);   // bottom
        minesFound += countMine(r+1, c+1); // bottom right

        if (minesFound > 0) {
            tile.setFont(new Font("Arial", Font.BOLD, (int)(tileSize / textScale))); // Font scales with tile size
            switch (minesFound) {
                case 1:
                    tile.setForeground(Color.BLUE);
                    break;
                case 2:
                    tile.setForeground(Color.GREEN);
                    break;
                case 3:
                    tile.setForeground(Color.RED);
                    break;
                case 4:
                    tile.setForeground(new Color(128, 0, 128)); // Purple
                    break;
                case 5:
                    tile.setForeground(new Color(128, 0, 0)); // Maroon
                    break;
                case 6:
                    tile.setForeground(new Color(0, 128, 128)); // Teal
                    break;
                case 7:
                    tile.setForeground(Color.BLACK);
                    break;
                case 8:
                    tile.setForeground(Color.GRAY);
                    break;
                default:
                    System.out.println("Unexpected mine count: " + minesFound);
                    break;
            }
            tile.setText(Integer.toString(minesFound));
        } else {
            tile.setText(""); // No mines around, leave it empty
            if (tile.getIcon() != null) {
                tile.setEnabled(true);
                tile.setIcon(null);
                // Decrement flagCount and update label
                flagCount = Math.max(0, flagCount - 1);
                mineCountLabel.setText("Mines: " + (minecount - flagCount));
                tile.setEnabled(false);
            }
            
            //top 3
            checkMine(r-1, c-1); // top left
            checkMine(r-1, c);   // top
            checkMine(r-1, c+1); // top right

            //left and right
            checkMine(r, c-1);   // left
            checkMine(r, c+1);   // right

            //bottom 3
            checkMine(r+1, c-1); // bottom left
            checkMine(r+1, c);   // bottom
            checkMine(r+1, c+1); // bottom right
        }

        if (tilesClicked == (numRows * numCols) - mineList.size()) {
            gameOver = true;
            timer.stop();
            resetButton.setText("😎"); // Change reset button to a celebration face
            mineCountLabel.setText("You Win!"); // Update mine count label to indicate win
        }
    }

    int countMine(int r, int c) {
        if (r < 0 || r >= numRows || c < 0 || c >= numCols) {
            return 0;
        }
        if (mineList.contains(board[r][c])) {
            return 1;
        }
        return 0;
    }

    void resetGame() {
        // Stop and reset timer
        if (timer != null) {
            timer.stop();
        }
        elapsedSeconds = 0;
        timerLabel.setText("00:00");

        // Reset game state
        tilesClicked = 0;
        flagCount = 0;
        gameOver = false;
        mineCountLabel.setText("Mines: " + minecount);

        // Remove all icons and reset all tiles
        for (int r = 0; r < numRows; r++) {
            for (int c = 0; c < numCols; c++) {
                MineTile tile = board[r][c];
                tile.setEnabled(true);
                tile.setText("");
                tile.setIcon(null);
                tile.setFont(new Font("Arial", Font.BOLD, (int)(tileSize / textScale))); // Font scales with tile size
                tile.setForeground(Color.BLACK);
            }
        }

        // Reset mines
        setMines();

        resetButton.setText("😊");
    } 

    void setDifficulty(int rows, int cols, int mines) {
        // Stop timer if running
        if (timer != null) timer.stop();

        // Update settings
        numRows = rows;
        numCols = cols;
        minecount = mines;

        updateTextScale(); // <-- Add this line

        tileSize = Math.min(MAX_WINDOW_WIDTH / cols, MAX_WINDOW_HEIGHT / rows);
        boardWidth = cols * tileSize;
        boardHeight = rows * tileSize;
        frame.setSize(boardWidth, boardHeight);

        // Reload images with new tileSize
        mineIcon = new ImageIcon(getClass().getResource("./Mine.png")).getImage().getScaledInstance(tileSize - 10, tileSize - 10, Image.SCALE_SMOOTH);
        flagIcon = new ImageIcon(getClass().getResource("./Flag.png")).getImage().getScaledInstance(tileSize - 10, tileSize - 10, Image.SCALE_SMOOTH);
        flaggedMineIcon = new ImageIcon(getClass().getResource("./FlaggedMine.png")).getImage().getScaledInstance(tileSize - 10, tileSize - 10, Image.SCALE_SMOOTH);

        // Remove old board
        frame.remove(boardPanel);

        // Create new board panel and tiles
        boardPanel = new JPanel(new GridLayout(numRows, numCols));
        board = new MineTile[numRows][numCols];
        for (int r = 0; r < numRows; r++) {
            for (int c = 0; c < numCols; c++) {
                MineTile tile = new MineTile(r, c);
                board[r][c] = tile;
                tile.setFocusable(false);
                tile.setMargin(new Insets(0,0,0,0));
                tile.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 45));
                tile.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mousePressed(MouseEvent e) {
                        if (gameOver) {
                            return; // Ignore clicks if the game is over
                        }
                        if (e.getButton() == MouseEvent.BUTTON1) {
                            resetButton.setText("😨"); // Change reset button to a worried face
                        }
                    }

                    @Override
                    public void mouseReleased(MouseEvent e) {
                        if (gameOver) {
                            return; // Ignore clicks if the game is over
                        }
                        resetButton.setText("😊");
                        MineTile tile = (MineTile) e.getSource();
                        
                        if (e.getButton() == MouseEvent.BUTTON1) {
                            // Left click action
                            if (timer.isRunning() == false) {
                                // Start the timer when the first tile is clicked
                                timer = new Timer(1000, new ActionListener() {
                                    @Override
                                    public void actionPerformed(ActionEvent e) {
                                        elapsedSeconds++;
                                        int minutes = elapsedSeconds / 60;
                                        int seconds = elapsedSeconds % 60;
                                        timerLabel.setText(String.format("%02d:%02d", minutes, seconds));
                                    }
                                });
                                timer.start();
                            }

                            if (tile.getText() == "") {
                                if (mineList.contains(tile)) {
                                    revealMines();
                                }
                                else {
                                    checkMine(tile.r, tile.c);
                                }
                            }
                        } else if (e.getButton() == MouseEvent.BUTTON3) {
                            // Right click action
                            if (tile.getIcon() == null && tile.getText().equals("") && flagCount < 10 && tile.isEnabled()) {
                                tile.setIcon(new ImageIcon(flagIcon)); // Example action
                                flagCount++;
                                //mineCountLabel.setText("Mines: " + (minecount - flagCount));
                            } else if (tile.getIcon() != null && tile.isEnabled()) {
                                tile.setIcon(null); // Remove flag
                                tile.setText("?");
                                flagCount--;
                                //mineCountLabel.setText("Mines: " + (minecount - flagCount));
                            } else if (tile.getText().equals("?") && tile.isEnabled()) {
                                tile.setText(""); // Remove question mark
                                tile.setIcon(null); // Remove flag
                            }

                            // Clamp value and update label
                            flagCount = Math.max(0, Math.min(flagCount, minecount));
                            mineCountLabel.setText("Mines: " + (minecount - flagCount));
                        }
                    }
                });
                boardPanel.add(tile);
            }
        }
        frame.add(boardPanel);
        frame.revalidate();
        frame.repaint();

        // Reset game state
        resetGame();
    }

    // Add this method inside your Minesweeper class:
    private MineTile createAndAddTile(int r, int c) {
        MineTile tile = new MineTile(r, c);
        tile.setPreferredSize(new Dimension(tileSize, tileSize));
        tile.setFont(new Font("Arial", Font.BOLD, (int)(tileSize / textScale))); // Font scales with tile size
        tile.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (gameOver) {
                    return; // Ignore clicks if the game is over
                }
                if (e.getButton() == MouseEvent.BUTTON1) {
                    resetButton.setText("😨"); // Change reset button to a worried face
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (gameOver) {
                    return; // Ignore clicks if the game is over
                }
                resetButton.setText("😊");
                MineTile tile = (MineTile) e.getSource();

                if (e.getButton() == MouseEvent.BUTTON1) {
                    // Left click action
                    if (timer.isRunning() == false) {
                        timer = new Timer(1000, new ActionListener() {
                            @Override
                            public void actionPerformed(ActionEvent e) {
                                elapsedSeconds++;
                                int minutes = elapsedSeconds / 60;
                                int seconds = elapsedSeconds % 60;
                                timerLabel.setText(String.format("%02d:%02d", minutes, seconds));
                            }
                        });
                        timer.start();
                    }

                    if (tile.getText().equals("")) {
                        if (mineList.contains(tile)) {
                            revealMines();
                        } else {
                            checkMine(tile.r, tile.c);
                        }
                    }
                } else if (e.getButton() == MouseEvent.BUTTON3) {
                    // Right click action
                    if (tile.getIcon() == null && tile.getText().equals("") && flagCount < minecount && tile.isEnabled()) {
                        tile.setIcon(new ImageIcon(flagIcon));
                        flagCount++;
                    } else if (tile.getIcon() != null && tile.isEnabled()) {
                        tile.setIcon(null); // Remove flag
                        tile.setText("?");
                        flagCount--;
                    } else if (tile.getText().equals("?") && tile.isEnabled()) {
                        tile.setText(""); // Remove question mark
                        tile.setIcon(null); // Remove flag
                    }

                    // Clamp value and update label
                    flagCount = Math.max(0, Math.min(flagCount, minecount));
                    mineCountLabel.setText("Mines: " + (minecount - flagCount));
                }
            }
        });
        return tile;
    }

    // Add this method to your Minesweeper class:
    private void updateTextScale() {
        // Adjust these thresholds and scales as needed for your UI
        int maxDim = Math.max(numRows, numCols);
        if (maxDim <= 8) {
            textScale = 2; // Beginner
        } else if (maxDim <= 16) {
            textScale = 3; // Intermediate
        } else {
            textScale = 4; // Expert
        }
        System.out.println("Text scale set to: " + textScale);
    }
}

//To DO
//1) Change text colours based on the number of mines around - DONE
//   Also, replace use of emojis with icon images for better aesthetics - DONE
//2) Add a timer - DONE
//3) Add a reset button / reactive button like the original - DONE
//4) Make the mine count decrease when a flag is placed - DONE
//5) In game over state, add a new icon for a mine that was flagged - DONE
//6) Fix bug where a flagged tile that is later cleared removes the flag and corrects the flag count - DONE
//7) Add UI for different difficulty levels - DONE