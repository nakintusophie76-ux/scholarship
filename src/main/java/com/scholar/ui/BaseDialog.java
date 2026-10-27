package com.scholar.ui;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Window;

/**
 * Shared shell for modal forms: branded header, card body and right aligned
 * button bar.
 */
public abstract class BaseDialog extends JDialog {

    private final JPanel center = new JPanel(new BorderLayout());
    private final JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
    private final int preferredWidth;

    protected BaseDialog(Component parent, String title, String subtitle, int width) {
        super(ownerOf(parent), title, ModalityType.APPLICATION_MODAL);
        this.preferredWidth = width;
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(false);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Theme.BG);
        root.add(header(title, subtitle), BorderLayout.NORTH);

        center.setOpaque(false);
        center.setBorder(BorderFactory.createEmptyBorder(18, 22, 6, 22));
        root.add(center, BorderLayout.CENTER);

        footer.setOpaque(false);
        footer.setBorder(BorderFactory.createEmptyBorder(8, 22, 20, 22));
        root.add(footer, BorderLayout.SOUTH);

        setContentPane(root);
        setSize(width, 560);
    }

    private static Window ownerOf(Component parent) {
        if (parent instanceof Window window) {
            return window;
        }
        return parent == null ? null : SwingUtilities.getWindowAncestor(parent);
    }

    private static JPanel header(String title, String subtitle) {
        JPanel header = new JPanel(new BorderLayout(12, 0));
        header.setBackground(Theme.CARD);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.BORDER),
                BorderFactory.createEmptyBorder(18, 22, 16, 22)));

        JLabel logo = new JLabel(Icons.logo(30));
        header.add(logo, BorderLayout.WEST);

        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        JLabel titleLabel = UiUtils.label(title, Font.BOLD, 19, Theme.INK);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel subtitleLabel = UiUtils.label(subtitle, Font.PLAIN, 12, Theme.MUTED);
        subtitleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        text.add(titleLabel);
        text.add(Box.createVerticalStrut(2));
        text.add(subtitleLabel);
        header.add(text, BorderLayout.CENTER);

        return header;
    }

    protected void setBody(JComponent body) {
        center.add(body, BorderLayout.CENTER);
    }

    protected void setButtons(JButton... buttons) {
        footer.removeAll();
        for (JButton button : buttons) {
            footer.add(button);
        }
        center.add(footer, BorderLayout.SOUTH);
    }

    protected void finish() {
        pack();
        setSize(Math.max(getWidth(), preferredWidth), getHeight());
        setLocationRelativeTo(getOwner());
    }
}
