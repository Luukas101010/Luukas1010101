import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class RotationModClient implements KeyListener {

    private RotationGUI rotationGUI;

    public RotationModClient() {
        // Initialize RotationGUI here but don't show it yet
        rotationGUI = new RotationGUI();
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_INSERT) {
            // Show the RotationGUI when the Insert key is pressed
            rotationGUI.setVisible(true);
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {}

    @Override
    public void keyTyped(KeyEvent e) {}
}