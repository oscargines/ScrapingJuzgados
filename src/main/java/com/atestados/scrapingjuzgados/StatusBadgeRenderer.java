package com.atestados.scrapingjuzgados;

import com.formdev.flatlaf.FlatClientProperties;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

/**
 * Renderer de celdas que pinta el estado como una pastilla de color
 * (Completado verde, Procesando ámbar, Error rojo).
 */
public class StatusBadgeRenderer extends DefaultTableCellRenderer {

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean hasFocus, int row, int column) {
        JLabel label = (JLabel) super.getTableCellRendererComponent(
                table, value, isSelected, hasFocus, row, column);
        label.setOpaque(false);
        label.setHorizontalAlignment(SwingConstants.CENTER);
        label.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));

        Color bg;
        Color fg;
        switch (String.valueOf(value)) {
            case "Completado":
                bg = new Color(0xDCFCE7);
                fg = new Color(0x15803D);
                break;
            case "Procesando":
            case "En proceso":
            case "Pendiente":
                bg = new Color(0xFEF3C7);
                fg = new Color(0xB45309);
                break;
            case "Error":
            case "Fallido":
                bg = new Color(0xFEE2E2);
                fg = new Color(0xB91C1C);
                break;
            default:
                bg = new Color(0xF3F4F6);
                fg = new Color(0x4B5563);
                break;
        }

        label.putClientProperty(FlatClientProperties.STYLE,
                String.format("arc: 999; background: #%06X; foreground: #%06X; font: bold 11",
                        bg.getRGB() & 0xFFFFFF, fg.getRGB() & 0xFFFFFF));
        return label;
    }
}
