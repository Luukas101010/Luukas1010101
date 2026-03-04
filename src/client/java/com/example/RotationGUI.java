import javax.swing.*;
import java.awt.*;

public class RotationGUI extends JFrame {
    private RotationPanel rotationPanel;

    public RotationGUI() {
        setTitle("Rotation Speed Controller");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(500, 600);
        setLocationRelativeTo(null);
        setResizable(false);

        // Main panel
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());

        // Rotation display panel
        rotationPanel = new RotationPanel();
        mainPanel.add(rotationPanel, BorderLayout.CENTER);

        // Control panel with slider
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.Y_AXIS));
        controlPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel sliderLabel = new JLabel("Rotation Speed:");
        sliderLabel.setFont(new Font("Arial", Font.BOLD, 14));

        JSlider speedSlider = new JSlider(JSlider.HORIZONTAL, 0, 100, 0);
        speedSlider.setMajorTickSpacing(10);
        speedSlider.setMinorTickSpacing(1);
        speedSlider.setPaintTicks(true);
        speedSlider.setPaintLabels(true);
        speedSlider.addChangeListener(e -> {
            int speed = speedSlider.getValue();
            rotationPanel.setRotationSpeed(speed);
        });

        controlPanel.add(sliderLabel);
        controlPanel.add(Box.createVerticalStrut(10));
        controlPanel.add(speedSlider);

        mainPanel.add(controlPanel, BorderLayout.SOUTH);

        add(mainPanel);
        setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new RotationGUI());
    }
}

class RotationPanel extends JPanel {
    private double angle = 0;
    private int rotationSpeed = 0;
    private Timer timer;

    public RotationPanel() {
        setBackground(Color.WHITE);
        timer = new Timer(30, e -> {
            angle += rotationSpeed * 0.1;
            repaint();
        });
        timer.start();
    }

    public void setRotationSpeed(int speed) {
        this.rotationSpeed = speed;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int centerX = getWidth() / 2;
        int centerY = getHeight() / 2;
        int size = 80;

        // Save the current transform
        var originalTransform = g2d.getTransform();

        // Translate to center, rotate, then translate back
        g2d.translate(centerX, centerY);
        g2d.rotate(Math.toRadians(angle));

        // Draw a rotating square
        g2d.setColor(new Color(70, 130, 180));
        g2d.fillRect(-size / 2, -size / 2, size, size);

        // Draw a circle on top
        g2d.setColor(Color.RED);
        g2d.fillOval(-20, -20, 40, 40);

        // Restore the original transform
        g2d.setTransform(originalTransform);

        // Draw speed info
        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("Arial", Font.PLAIN, 12));
        g2d.drawString("Speed: " + rotationSpeed, 20, 20);
        g2d.drawString("Angle: " + String.format("%.1f°", angle % 360), 20, 40);
    }
}