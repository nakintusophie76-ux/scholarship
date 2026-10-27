package com.scholar.ui;

import javax.swing.ImageIcon;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.util.function.Consumer;

/**
 * Small vector icons drawn with Java2D so the application does not depend on
 * image files or an icon font.
 */
public final class Icons {

    private Icons() {
    }

    public static ImageIcon dashboard(int size, Color color) {
        return draw(size, color, g -> {
            g.drawRoundRect(3, 3, 7, 7, 2, 2);
            g.drawRoundRect(14, 3, 7, 7, 2, 2);
            g.drawRoundRect(3, 14, 7, 7, 2, 2);
            g.drawRoundRect(14, 14, 7, 7, 2, 2);
        });
    }

    public static ImageIcon students(int size, Color color) {
        return draw(size, color, g -> {
            g.drawOval(8, 4, 8, 8);
            Path2D body = new Path2D.Double();
            body.moveTo(4, 21);
            body.curveTo(4, 15, 20, 15, 20, 21);
            g.draw(body);
        });
    }

    public static ImageIcon scholarship(int size, Color color) {
        return draw(size, color, capPainter(false));
    }

    public static ImageIcon applications(int size, Color color) {
        return draw(size, color, g -> {
            g.drawRoundRect(5, 3, 14, 18, 3, 3);
            g.drawLine(9, 9, 15, 9);
            g.drawLine(9, 13, 15, 13);
            g.drawLine(9, 17, 13, 17);
        });
    }

    public static ImageIcon reports(int size, Color color) {
        return draw(size, color, g -> {
            g.drawRoundRect(3, 13, 5, 8, 1, 1);
            g.drawRoundRect(10, 8, 5, 13, 1, 1);
            g.drawRoundRect(17, 3, 5, 18, 1, 1);
        });
    }

    public static ImageIcon key(int size, Color color) {
        return draw(size, color, g -> {
            g.drawOval(4, 5, 8, 8);
            g.drawLine(11, 12, 20, 21);
            g.drawLine(17, 18, 20, 15);
            g.drawLine(19, 20, 21, 18);
        });
    }

    public static ImageIcon logout(int size, Color color) {
        return draw(size, color, g -> {
            g.drawRoundRect(4, 4, 9, 16, 2, 2);
            g.drawLine(12, 12, 21, 12);
            g.drawLine(17, 8, 21, 12);
            g.drawLine(17, 16, 21, 12);
        });
    }

    public static ImageIcon award(int size, Color color) {
        return draw(size, color, g -> {
            g.drawOval(7, 3, 10, 10);
            g.drawLine(9, 12, 7, 21);
            g.drawLine(9, 12, 12, 15);
            g.drawLine(15, 12, 17, 21);
            g.drawLine(15, 12, 12, 15);
            g.drawOval(10, 6, 4, 4);
        });
    }

    public static ImageIcon clock(int size, Color color) {
        return draw(size, color, g -> {
            g.drawOval(3, 3, 18, 18);
            g.drawLine(12, 7, 12, 12);
            g.drawLine(12, 12, 16, 14);
        });
    }

    public static ImageIcon plus(int size, Color color) {
        return draw(size, color, g -> {
            g.drawLine(12, 5, 12, 19);
            g.drawLine(5, 12, 19, 12);
        });
    }

    public static ImageIcon search(int size, Color color) {
        return draw(size, color, g -> {
            g.drawOval(5, 5, 11, 11);
            g.drawLine(15, 15, 20, 20);
        });
    }

    public static ImageIcon refresh(int size, Color color) {
        return draw(size, color, g -> {
            g.drawArc(4, 4, 16, 16, 40, 280);
            g.drawLine(18, 3, 20, 7);
            g.drawLine(20, 7, 16, 8);
        });
    }

    public static ImageIcon check(int size, Color color) {
        return draw(size, color, g -> {
            g.drawLine(5, 12, 10, 17);
            g.drawLine(10, 17, 19, 7);
        });
    }

    /** Gradient rounded-square brand mark with a white graduation cap. */
    public static ImageIcon logo(int size) {
        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setPaint(new GradientPaint(0, 0, Theme.PRIMARY, size, size, new Color(0x8B5CF6)));
        g.fillRoundRect(0, 0, size, size, size / 4, size / 4);

        g.scale(size / 24.0, size / 24.0);
        g.setColor(Color.WHITE);
        g.setStroke(new BasicStroke(1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        capPainter(true).accept(g);
        g.dispose();
        return new ImageIcon(image);
    }

    private static Consumer<Graphics2D> capPainter(boolean fill) {
        return g -> {
            Path2D cap = new Path2D.Double();
            cap.moveTo(3, 9.5);
            cap.lineTo(12, 5);
            cap.lineTo(21, 9.5);
            cap.lineTo(12, 14);
            cap.closePath();
            if (fill) {
                g.fill(cap);
            } else {
                g.draw(cap);
            }
            g.drawLine(7, 12, 7, 16);
            g.drawLine(7, 16, 12, 18);
            g.drawLine(12, 18, 17, 16);
            g.drawLine(17, 12, 17, 16);
            g.drawLine(21, 10, 21, 15);
            g.drawOval(20, 14, 2, 2);
        };
    }

    private static ImageIcon draw(int size, Color color, Consumer<Graphics2D> painter) {
        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        g.scale(size / 24.0, size / 24.0);
        g.setColor(color);
        g.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        painter.accept(g);
        g.dispose();
        return new ImageIcon(image);
    }

    /** App window icon: the brand mark at 64px. */
    public static BufferedImage appIcon() {
        ImageIcon icon = logo(64);
        return (BufferedImage) icon.getImage();
    }

    /** Not used directly, keeps the font import meaningful if the mark changes. */
    static Font markFont() {
        return new Font("Segoe UI", Font.BOLD, 14);
    }
}
