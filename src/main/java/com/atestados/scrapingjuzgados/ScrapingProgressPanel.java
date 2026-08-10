package com.atestados.scrapingjuzgados;

import com.formdev.flatlaf.FlatClientProperties;

import javax.swing.*;
import java.awt.*;

/**
 * Panel integrado de progreso (no modal) que se muestra en la parte inferior
 * del dashboard durante el scraping. Compacto: mensaje de estado centrado y
 * botón de cancelar. La animación vive en la tarjeta de estado (KPI).
 */
public class ScrapingProgressPanel extends JPanel implements ProgressReporter {

    private final JLabel messageLabel;
    private final JButton cancelButton;
    private final Runnable onCancel;
    private volatile boolean isCancelled = false;

    public ScrapingProgressPanel(Runnable onCancel) {
        this.onCancel = onCancel;
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        putClientProperty(FlatClientProperties.STYLE,
                "arc: 18; background: #FFFFFF; border: 1,1,1,1,#E5E7EB");
        setBorder(BorderFactory.createEmptyBorder(14, 20, 14, 20));

        messageLabel = new JLabel("Preparado", SwingConstants.CENTER);
        messageLabel.putClientProperty(FlatClientProperties.STYLE, "foreground: #6B7280; font: 13");
        messageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        cancelButton = new JButton("Cancelar");
        cancelButton.putClientProperty(FlatClientProperties.STYLE,
                "arc: 999; background: #FEE2E2; foreground: #B91C1C; borderWidth: 0; focusWidth: 0");
        cancelButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        cancelButton.addActionListener(e -> {
            isCancelled = true;
            if (onCancel != null) {
                onCancel.run();
            }
        });

        add(messageLabel);
        add(Box.createVerticalStrut(12));
        add(cancelButton);
    }

    @Override
    public void setMessage(String message) {
        SwingUtilities.invokeLater(() -> messageLabel.setText(message));
    }

    @Override
    public boolean isCancelled() {
        return isCancelled;
    }

    public void reset() {
        isCancelled = false;
        messageLabel.setText("Conectando...");
    }

    public void stop() {
    }
}
