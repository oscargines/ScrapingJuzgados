package com.atestados.scrapingjuzgados;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Arc2D;

/**
 * Panel con indicador de progreso circular animado y mensaje de estado.
 * Siempre está en el layout; solo pinta contenido cuando está activo.
 */
public class CircularProgressPanel extends JPanel implements ProgressReporter {

    private volatile boolean running = false;
    private volatile boolean cancelled = false;
    private String message = "";
    private int progress = -1;
    private Timer animationTimer;
    private int arcAngle = 0;

    public CircularProgressPanel() {
        setOpaque(false);
        setMinimumSize(new Dimension(0, 55));
        setPreferredSize(new Dimension(1000, 55));
        setMaximumSize(new Dimension(Integer.MAX_VALUE, 55));
    }

    public void start(String initialMessage) {
        this.message = initialMessage;
        this.cancelled = false;
        this.running = true;
        this.progress = -1;

        if (animationTimer == null) {
            animationTimer = new Timer(50, e -> {
                arcAngle = (arcAngle + 6) % 360;
                repaint();
            });
            animationTimer.setCoalesce(true);
        }
        animationTimer.start();
        repaint();
    }

    public void stop() {
        running = false;
        if (animationTimer != null) {
            animationTimer.stop();
        }
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
    public Dimension getPreferredSize() {
        return new Dimension(getParent() != null ? getParent().getWidth() : 1000, 55);
    }

    @Override
    protected void paintComponent(Graphics g) {
        if (!running) return;

        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();

        // Fondo semitransparente
        g2d.setColor(new Color(240, 240, 240, 200));
        g2d.fillRect(0, 0, w, h);

        // Línea separadora superior
        g2d.setColor(new Color(0, 120, 215));
        g2d.fillRect(0, 0, w, 2);

        // Círculo de progreso
        int circleSize = Math.min(h - 16, 38);
        int circleX = 20;
        int circleY = (h - circleSize) / 2;

        g2d.setColor(new Color(220, 220, 220));
        g2d.fillOval(circleX, circleY, circleSize, circleSize);

        g2d.setColor(new Color(0, 120, 215));
        g2d.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        int arcSize = circleSize - 6;
        int arcX = circleX + 3;
        int arcY = circleY + 3;

        if (progress >= 0) {
            int sweep = (int) (360.0 * progress / 100.0);
            g2d.draw(new Arc2D.Double(arcX, arcY, arcSize, arcSize, 90, -sweep, Arc2D.OPEN));
        } else {
            g2d.draw(new Arc2D.Double(arcX, arcY, arcSize, arcSize, arcAngle, 90, Arc2D.OPEN));
        }

        if (progress >= 0) {
            g2d.setColor(new Color(60, 60, 60));
            g2d.setFont(new Font("Arial", Font.BOLD, 10));
            String pct = progress + "%";
            FontMetrics fm = g2d.getFontMetrics();
            int textX = circleX + (circleSize - fm.stringWidth(pct)) / 2;
            int textY = circleY + (circleSize + fm.getAscent() - fm.getDescent()) / 2;
            g2d.drawString(pct, textX, textY);
        }

        // Mensaje
        g2d.setColor(new Color(50, 50, 50));
        g2d.setFont(new Font("SansSerif", Font.PLAIN, 13));
        int textX = circleX + circleSize + 12;
        int maxWidth = w - textX - 10;

        if (message != null && !message.isEmpty() && maxWidth > 0) {
            String drawText = message;
            FontMetrics fm = g2d.getFontMetrics();
            if (fm.stringWidth(drawText) > maxWidth) {
                while (fm.stringWidth(drawText + "...") > maxWidth && drawText.length() > 0) {
                    drawText = drawText.substring(0, drawText.length() - 1);
                }
                drawText += "...";
            }
            int textY = (h + fm.getAscent() - fm.getDescent()) / 2;
            g2d.drawString(drawText, textX, textY);
        }
    }
}
