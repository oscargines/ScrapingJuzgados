package com.atestados.scrapingjuzgados;

import com.formdev.flatlaf.FlatClientProperties;

import javax.swing.*;
import java.awt.*;

/**
 * Tarjeta de métrica (KPI) estilo dashboard SaaS: label gris pequeño arriba,
 * valor grande en el centro y delta verde con flecha abajo.
 * Soporta componente animado opcional (ej. LoadingSlider) debajo del valor.
 */
public class StatCard extends JPanel {

    private final JLabel valueLbl;
    private final JLabel deltaLbl;
    private JComponent animatedComponent;

    public StatCard(String label, String value, String delta) {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        putClientProperty(FlatClientProperties.STYLE,
                "arc: 18; background: #FFFFFF; border: 1,1,1,1,#E5E7EB");
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel labelLbl = new JLabel(label);
        labelLbl.putClientProperty(FlatClientProperties.STYLE, "foreground: #6B7280; font: 13");
        labelLbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        valueLbl = new JLabel(value);
        valueLbl.putClientProperty(FlatClientProperties.STYLE, "foreground: #111827; font: bold 30");
        valueLbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        deltaLbl = new JLabel(delta);
        deltaLbl.putClientProperty(FlatClientProperties.STYLE, "foreground: #16A34A; font: 12");
        deltaLbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        add(labelLbl);
        add(Box.createVerticalStrut(10));
        add(valueLbl);
        add(Box.createVerticalStrut(8));
        add(deltaLbl);
    }

    public void setValue(String value) {
        valueLbl.setText(value);
    }

    public void setDelta(String delta) {
        deltaLbl.setText(delta);
    }

    public void setAnimatedComponent(JComponent comp) {
        if (animatedComponent != null) {
            remove(animatedComponent);
        }
        animatedComponent = comp;
        if (comp != null) {
            comp.setAlignmentX(Component.LEFT_ALIGNMENT);
            add(Box.createVerticalStrut(8));
            add(comp);
        }
        revalidate();
        repaint();
    }

    public void clearAnimatedComponent() {
        setAnimatedComponent(null);
    }
}
