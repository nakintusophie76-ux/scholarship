package com.scholar.ui;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

/**
 * White rounded panel with a subtle shadow, used for all content cards.
 */
public class RoundedPanel extends JPanel {

    private Color fillColor = Theme.CARD;
    private Color borderColor = Theme.BORDER;
    private int arc = 18;
    private boolean shadow = true;

    public RoundedPanel() {
        setOpaque(false);
    }

    public RoundedPanel fill(Color color) {
        this.fillColor = color;
        return this;
    }

    public RoundedPanel borderColor(Color color) {
        this.borderColor = color;
        return this;
    }

    public RoundedPanel arc(int arc) {
        this.arc = arc;
        return this;
    }

    public RoundedPanel shadow(boolean shadow) {
        this.shadow = shadow;
        return this;
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int width = getWidth() - 2;
        int height = getHeight() - 3;
        if (width <= 0 || height <= 0) {
            g2.dispose();
            return;
        }

        if (shadow) {
            g2.setColor(new Color(15, 23, 42, 14));
            g2.fillRoundRect(1, 2, width, height, arc, arc);
        }

        g2.setColor(fillColor);
        g2.fillRoundRect(0, 0, width, height, arc, arc);
        if (borderColor != null) {
            g2.setColor(borderColor);
            g2.drawRoundRect(0, 0, width, height, arc, arc);
        }
        g2.dispose();
        super.paintComponent(g);
    }
}
