package com.atestados.scrapingjuzgados;

import javax.swing.*;
import java.awt.*;

/**
 * Animación ligera tipo "slider" (barra indeterminada): un segmento verde
 * recorre una pista redondeada de un extremo a otro y vuelve. Se estira al
 * ancho de la tarjeta y mantiene una altura fija, sin romper las proporciones
 * del layout.
 */
public class LoadingSlider extends JComponent {

    private static final Color TRACK = new Color(0xE5E7EB);
    private static final Color BAR = new Color(0x22C55E);
    private static final double CYCLE_SECONDS = 2.2;

    private final Timer timer;
    private final long startNanos;
    private final int barHeight;

    public LoadingSlider(int barHeight) {
        this.barHeight = barHeight;
        setPreferredSize(new Dimension(200, barHeight));
        setMinimumSize(new Dimension(60, barHeight));
        setMaximumSize(new Dimension(Integer.MAX_VALUE, barHeight));
        setOpaque(false);
        startNanos = System.nanoTime();
        timer = new Timer(16, e -> repaint());
        timer.setCoalesce(true);
    }

    public void start() {
        if (!timer.isRunning()) {
            timer.start();
        }
    }

    public void stop() {
        timer.stop();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();
        int y = (h - barHeight) / 2;
        int arc = Math.max(2, barHeight / 2);

        g2d.setColor(TRACK);
        g2d.fillRoundRect(0, y, w, barHeight, arc, arc);

        double t = (System.nanoTime() - startNanos) / 1e9;
        double cycle = (t % CYCLE_SECONDS) / CYCLE_SECONDS;
        double p = 1 - Math.abs(2 * cycle - 1);

        int segW = Math.max(36, Math.max(1, w / 3));
        int x = (int) Math.round((w - segW) * p);

        g2d.setColor(BAR);
        g2d.fillRoundRect(x, y, segW, barHeight, arc, arc);

        g2d.dispose();
    }
}
