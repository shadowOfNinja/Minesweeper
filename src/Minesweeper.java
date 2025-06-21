
import java.awt.*;
import java.awt.event.*;
import java.lang.reflect.Array;
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

    Minesweeper() {
        frame.setSize(boardWidth, boardHeight);
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        textLabel.setFont(new Font("Arial", Font.BOLD, 25));
        textLabel.setHorizontalAlignment(JLabel.CENTER);
        textLabel.setText("Minesweeper: " + Integer.toString(minecount));
        textLabel.setOpaque(true);

        textPanel.setLayout(new BorderLayout());
        textPanel.add(textLabel);
        frame.add(textPanel, BorderLayout.NORTH);

        boardPanel.setLayout(new GridLayout(numRows, numCols));
        frame.add(boardPanel);

        for (int r = 0; r < numRows; r++) {
            for (int c = 0; c < numCols; c++) {
                MineTile tile = new MineTile(r, c);
                board[r][c] = tile;
                
                tile.setFocusable(false);
                tile.setMargin(new Insets(0,0,0,0));
                tile.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 45));
                //tile.setText("😀");
                tile.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mousePressed(MouseEvent e) {
                        if (gameOver) {
                            return; // Ignore clicks if the game is over
                        }
                        MineTile tile = (MineTile) e.getSource();
                        
                        if (e.getButton() == MouseEvent.BUTTON1) {
                            // Left click action
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
                            if (tile.getText() == "" && tile.isEnabled()) {
                                tile.setText("🚩"); // Example action
                            } else if (tile.getText() == "🚩") {
                                tile.setText(""); // Remove flag
                            }
                        }
                    }
                });
                boardPanel.add(tile);
            }
        }
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
            mine.setText("💣");
        }
        gameOver = true;
        textLabel.setText("Game Over!");
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
}
