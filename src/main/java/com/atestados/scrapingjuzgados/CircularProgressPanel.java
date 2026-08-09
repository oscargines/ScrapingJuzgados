package com.atestados.scrapingjuzgados;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Arc2D;

/**
 * Panel con indicador de progreso circular animado y mensaje de estado.
 * Se integra directamente en la ventana principal.
 */
public class CircularProgressPanel extends JPanel implements ProgressReporter {

    private volatile boolean running = false;
    private volatile boolean cancelled = false;
    private String message = "";
    private int progress = -1; // -1 = indeterminado
    private Timer animationTimer;
    private int arcAngle = 0;

    public CircularProgressPanel() {
        setOpaque(false);
        setPreferredSize(new Dimension(0, 60));
        setVisible(false);
    }

    public void start(String initialMessage) {
        this.message = initialMessage;
        this.cancelled = false;
        this.running = true;
        setVisible(true);

        if (animationTimer == null) {
            animationTimer = new Timer(50, e -> {
                arcAngle = (arcAngle + 6) % 360;
                repaint();
            });
        }
        animationTimer.start();
    }

    public void stop() {
        running = false;
        if (animationTimer != null) {
            animationTimer.stop();
        }
        setVisible(false);
        repaint();
    }

    @Override
    public void setMessage(String message) {
        this.message = message;
        repaint();
    }

    public void setProgress(int progress) {
        this.progress = Math.max(-1, Math.min(100, progress));
        repaint();
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    public void cancel() {
        cancelled = true;
        stop();
    }

    @Override
    protected void paintComponent(Graphics g) {
        if (!running) return;

        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int panelWidth = getWidth();
        int panelHeight = getHeight();

        // Dibujar círculo de progreso
        int circleSize = Math.min(panelHeight - 10, 45);
        int circleX = 15;
        int circleY = (panelHeight - circleSize) / 2;

        // Fondo del círculo
        g2d.setColor(new Color(230, 230, 230));
        g2d.fillOval(circleX, circleY, circleSize, circleSize);

        // Arco animado
        g2d.setColor(new Color(0, 120, 215));
        g2d.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        int arcSize = circleSize - 6;
        int arcX = circleX + 3;
        int arcY = circleY + 3;

        if (progress >= 0) {
            // Progreso determinado: arco proporcional
            int sweep = (int) (360.0 * progress / 100.0);
            g2d.draw(new Arc2D.Double(arcX, arcY, arcSize, arcSize, 90, -sweep, Arc2D.OPEN));
        } else {
            // Indeterminado: arco rotatorio
            g2d.draw(new Arc2D.Double(arcX, arcY, arcSize, arcSize, arcAngle, 90, Arc2D.OPEN));
        }

        // Texto del porcentaje si es determinado
        if (progress >= 0) {
            g2d.setColor(new Color(60, 60, 60));
            g2d.setFont(new Font("Arial", Font.BOLD, 10));
            String pct = progress + "%";
            FontMetrics fm = g2d.getFontMetrics();
            int textX = circleX + (circleSize - fm.stringWidth(pct)) / 2;
            int textY = circleY + (circleSize + fm.getAscent() - fm.getDescent()) / 2;
            g2d.drawString(pct, textX, textY);
        }

        // Mensaje de estado
        g2d.setColor(new Color(50, 50, 50));
        g2d.setFont(new Font("Arial", Font.PLAIN, 13));
        int textX = circleX + circleSize + 12;
        int maxWidth = panelWidth - textX - 10;

        if (message != null && !message.isEmpty()) {
            // Truncar mensaje si no cabe
            String drawText = message;
            FontMetrics fm = g2d.getFontMetrics();
            if (fm.stringWidth(drawText) > maxWidth && maxWidth > 0) {
                while (fm.stringWidth(drawText + "...") > maxWidth && drawText.length() > 0) {
                    drawText = drawText.substring(0, drawText.length() - 1);
                }
                drawText += "...";
            }
            int textY = (panelHeight + fm.getAscent() - fm.getDescent()) / 2;
            g2d.drawString(drawText, textX, textY);
        }
    }
}
