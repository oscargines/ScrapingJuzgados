package com.atestados.scrapingjuzgados;

import com.formdev.flatlaf.FlatClientProperties;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Sidebar de navegación estilo dashboard SaaS: fondo blanco, ítem activo con
 * fondo gris claro y botón primario verde "+ Nuevo scraping".
 */
public class SidebarPanel extends JPanel {

    public static final String OPTION_ORGANOS = "Órganos Judiciales";
    public static final String OPTION_TODOS = "Todos los Juzgados";
    public static final String OPTION_DEMARCACION = "Demarcaciones";

    private static final String STYLE_ACTIVE =
            "arc: 12; background: #F3F4F6; hoverBackground: #E5E7EB; foreground: #111827; font: bold 13; borderWidth: 0; focusWidth: 0";
    private static final String STYLE_INACTIVE =
            "arc: 12; background: null; hoverBackground: #F3F4F6; foreground: #374151; borderWidth: 0; focusWidth: 0";

    private final List<JButton> navButtons = new ArrayList<>();
    private JButton newScrapeButton;
    private Consumer<String> onSelect;
    private Runnable onNewScrape;

    public SidebarPanel() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(Color.WHITE);
        setPreferredSize(new Dimension(240, 0));
        setBorder(BorderFactory.createEmptyBorder(20, 16, 20, 16));

        JLabel logo = new JLabel("SCRAPING");
        logo.putClientProperty(FlatClientProperties.STYLE, "font: bold 16; foreground: #111827");
        logo.setAlignmentX(Component.LEFT_ALIGNMENT);
        add(logo);
        add(Box.createVerticalStrut(2));

        JLabel sub = new JLabel("Directorio del Poder Judicial");
        sub.putClientProperty(FlatClientProperties.STYLE, "foreground: #6B7280; font: 11");
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);
        add(sub);
        add(Box.createVerticalStrut(20));

        newScrapeButton = buildButton("＋ Nuevo scraping", true);
        newScrapeButton.addActionListener(e -> {
            if (onNewScrape != null) {
                onNewScrape.run();
            }
        });
        add(newScrapeButton);
        add(Box.createVerticalStrut(20));

        JLabel section = new JLabel("SCRAPING");
        section.putClientProperty(FlatClientProperties.STYLE, "foreground: #9CA3AF; font: bold 11");
        section.setAlignmentX(Component.LEFT_ALIGNMENT);
        add(section);
        add(Box.createVerticalStrut(6));

        addNavItem(OPTION_ORGANOS, true);
        addNavItem(OPTION_TODOS, false);
        addNavItem(OPTION_DEMARCACION, false);

        add(Box.createVerticalGlue());

        JLabel version = new JLabel("Scraping v1.0");
        version.putClientProperty(FlatClientProperties.STYLE, "foreground: #6B7280; font: 12");
        version.setAlignmentX(Component.LEFT_ALIGNMENT);
        add(version);
    }

    private JButton buildButton(String text, boolean primary) {
        JButton button = new JButton(text);
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        button.putClientProperty(FlatClientProperties.STYLE, primary
                ? "arc: 12; background: #22C55E; foreground: #FFFFFF; borderWidth: 0; focusWidth: 0"
                : STYLE_INACTIVE);
        return button;
    }

    private void addNavItem(String key, boolean active) {
        JButton button = buildButton(key, false);
        if (active) {
            button.putClientProperty(FlatClientProperties.STYLE, STYLE_ACTIVE);
        }
        button.addActionListener(e -> {
            if (isEnabled()) {
                setActive(key);
                if (onSelect != null) {
                    onSelect.accept(key);
                }
            }
        });
        navButtons.add(button);
        add(button);
        add(Box.createVerticalStrut(4));
    }

    public void setOnSelect(Consumer<String> onSelect) {
        this.onSelect = onSelect;
    }

    public void setOnNewScrape(Runnable onNewScrape) {
        this.onNewScrape = onNewScrape;
    }

    public void setActive(String key) {
        for (JButton b : navButtons) {
            boolean isActive = b.getText().equals(key);
            b.putClientProperty(FlatClientProperties.STYLE, isActive ? STYLE_ACTIVE : STYLE_INACTIVE);
            b.repaint();
            b.revalidate();
        }
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        newScrapeButton.setEnabled(enabled);
        for (JButton b : navButtons) {
            b.setEnabled(enabled);
        }
    }
}
