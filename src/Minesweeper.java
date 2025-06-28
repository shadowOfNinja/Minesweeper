import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Random;
import javax.swing.*;

public class Minesweeper {

    private class MineTile extends JButton {
        int r;
        int c;

        MineTile(int r, int c) {
            this.r = r;
            this.c = c;
        }
    }

    int tileSize = 70;
    int numRows = 8;
    int numCols = numRows;
    int boardWidth = numCols * tileSize;
    int boardHeight = numRows * tileSize;

    Image mineIcon;
    Image flagIcon;
    
    JFrame frame = new JFrame("Minesweeper");
    JLabel textLabel = new JLabel();
    JPanel textPanel = new JPanel();
    JPanel boardPanel = new JPanel();

    int minecount = 10; // Number of mines
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

        //load images
        mineIcon = new ImageIcon(getClass().getResource("./Mine.png")).getImage().getScaledInstance(45, 45, Image.SCALE_SMOOTH);
        flagIcon = new ImageIcon(getClass().getResource("./Flag.png")).getImage().getScaledInstance(45, 45, Image.SCALE_SMOOTH);

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
                            if (tile.getIcon() == null && tile.isEnabled()) {
                                tile.setIcon(new ImageIcon(flagIcon)); // Example action
                            } else if (tile.getIcon() != null) {
                                tile.setIcon(null); // Remove flag
                            }
                        }
                    }
                });
                boardPanel.add(tile);
            }
        }
        frame.setAlwaysOnTop(true);
        frame.setVisible(true);

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
            mine.setIcon(new ImageIcon(mineIcon));
        }
        gameOver = true;
        textLabel.setText("Game Over!");
        resetButton.setText("😢"); // Change reset button to a sad face
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
            tile.setFont(new Font("Arial", Font.BOLD, 45));
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
            textLabel.setText("Mines Cleared!");
            gameOver = true;
            timer.stop();
            resetButton.setText("😎"); // Change reset button to a celebration face
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
        gameOver = false;
        mineCountLabel.setText("Mines: " + minecount);

        // Remove all icons and reset all tiles
        for (int r = 0; r < numRows; r++) {
            for (int c = 0; c < numCols; c++) {
                MineTile tile = board[r][c];
                tile.setEnabled(true);
                tile.setText("");
                tile.setIcon(null);
                tile.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 45));
                tile.setForeground(Color.BLACK);
            }
        }

        // Reset mines
        setMines();

        resetButton.setText("😊");

        // Clear any status text
        textLabel.setText("");
    } 
}

//To DO
//1) Change text colours based on the number of mines around - DONE
//   Also, replace use of emojis with icon images for better aesthetics - DONE
//2) Add a timer - DONE
//3) Add a reset button / reactive button like the original - DONE
//4) Make the mine count decrease when a flag is placed
