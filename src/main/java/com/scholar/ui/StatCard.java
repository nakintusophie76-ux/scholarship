package com.scholar.ui;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagLayout;
import java.awt.RenderingHints;

/**
 * Dashboard KPI card: tinted icon bubble, label, value and supporting note.
 */
public final class StatCard extends RoundedPanel {

    private final JLabel valueLabel;
    private final JLabel noteLabel;

    public StatCard(String title, String note, ImageIcon icon, Color tint) {
        setLayout(new BorderLayout(16, 0));
        setBorder(BorderFactory.createEmptyBorder(18, 20, 20, 20));

        JPanel bubble = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(tint);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        bubble.setOpaque(false);
        bubble.setPreferredSize(new Dimension(48, 48));
        bubble.add(new JLabel(icon));
        add(bubble, BorderLayout.WEST);

        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));

        JLabel titleLabel = UiUtils.label(title.toUpperCase(), Font.BOLD, 11, Theme.MUTED);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        valueLabel = UiUtils.label("0", Font.BOLD, 26, Theme.INK);
        valueLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        noteLabel = UiUtils.label(note, Font.PLAIN, 12, Theme.MUTED);
        noteLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        text.add(Box.createVerticalGlue());
        text.add(titleLabel);
        text.add(Box.createVerticalStrut(2));
        text.add(valueLabel);
        text.add(Box.createVerticalStrut(2));
        text.add(noteLabel);
        text.add(Box.createVerticalGlue());
        add(text, BorderLayout.CENTER);

        setPreferredSize(new Dimension(240, 112));
    }

    public void setValue(String value) {
        valueLabel.setText(value);
    }

    public void setNote(String note) {
        noteLabel.setText(note);
    }
}
