package com.atestados.scrapingjuzgados;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Arc2D;
import javax.swing.plaf.basic.BasicProgressBarUI;

/**
 * Diálogo de progreso personalizado con indicador circular animado.
 */
public class ProgressDialog extends JDialog implements ProgressReporter {
    private JLabel messageLabel = null;
    private JProgressBar progressBar = null;
    private volatile boolean isCancelled = false;

    /**
     * Constructor del diálogo de progreso.
     *
     * @param parent Ventana padre (puede ser null)
     * @param title  Título del diálogo
     */
    public ProgressDialog(JFrame parent, String title) {
        super(parent, title, true);
        initComponents();
    }

    private void initComponents() {
        setSize(300, 150);
        setLocationRelativeTo(getParent());
        setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        setResizable(false);

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        messageLabel = new JLabel("Procesando, por favor espere...", SwingConstants.CENTER);
        messageLabel.setFont(new Font("Arial", Font.PLAIN, 14));

        progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        progressBar.setStringPainted(false);

        // Personalizar la barra de progreso con animación circular
        progressBar.setUI(new BasicProgressBarUI() {
            @Override
            protected Color getSelectionBackground() {
                return Color.WHITE;
            }

            @Override
            protected Color getSelectionForeground() {
                return Color.WHITE;
            }

            @Override
            public void paintIndeterminate(Graphics g, JComponent c) {
                if (isCancelled) return; // Detener animación si se cancela

                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                Insets insets = progressBar.getInsets();
                int width = progressBar.getWidth() - (insets.right + insets.left);
                int height = progressBar.getHeight() - (insets.top + insets.bottom);
                int size = Math.min(width, height);

                int x = insets.left + (width - size) / 2;
                int y = insets.top + (height - size) / 2;

                g2d.setColor(new Color(220, 220, 220));
                g2d.fillOval(x, y, size, size);

                g2d.setColor(new Color(0, 120, 215));
                int arcSize = size - 4;
                int arcX = x + 2;
                int arcY = y + 2;

                int arcStart = (int) (System.currentTimeMillis() / 10 % 360);
                g2d.setStroke(new BasicStroke(3f));
                g2d.draw(new Arc2D.Double(arcX, arcY, arcSize, arcSize, arcStart, 90, Arc2D.OPEN));
            }
        });

        JButton cancelButton = new JButton("Cancelar");
        cancelButton.addActionListener(e -> cancel());

        JPanel centerPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.anchor = GridBagConstraints.CENTER;

        centerPanel.add(messageLabel, gbc);
        centerPanel.add(Box.createVerticalStrut(10), gbc);
        centerPanel.add(progressBar, gbc);
        centerPanel.add(Box.createVerticalStrut(10), gbc);
        centerPanel.add(cancelButton, gbc);

        panel.add(centerPanel, BorderLayout.CENTER);
        add(panel);

        // Manejar cierre de ventana
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                cancel();
            }
        });
    }

    /**
     * Actualiza el mensaje del diálogo en el EDT.
     *
     * @param message Nuevo mensaje
     */
    @Override
    public void setMessage(String message) {
        SwingUtilities.invokeLater(() -> messageLabel.setText(message));
    }

    /**
     * Muestra el diálogo.
     */
    public void showDialog() {
        SwingUtilities.invokeLater(() -> setVisible(true));
    }

    /**
     * Cierra el diálogo en el EDT.
     */
    public void closeDialog() {
        SwingUtilities.invokeLater(() -> {
            setVisible(false);
            dispose();
        });
    }

    /**
     * Cancela la tarea asociada.
     */
    public void cancel() {
        isCancelled = true;
        closeDialog();
    }

    /**
     * Verifica si la tarea fue cancelada.
     *
     * @return true si la tarea fue cancelada
     */
    @Override
    public boolean isCancelled() {
        return isCancelled;
    }
}