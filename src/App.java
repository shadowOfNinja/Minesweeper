import javax.swing.UIManager;

public class App {
    public static void main(String[] args) throws Exception {
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (Exception e) {
            e.printStackTrace();
        }
        Minesweeper minesweeper = new Minesweeper();
    }
}
