package br.com.uepg.operacaoretrolaser.ui;

import java.awt.*;

public class NeonButton {
    private final Rectangle rect;
    private final String text;
    private final Color neonColor;
    private boolean isHovered = false;

    public NeonButton(int x, int y, int width, int height, String text, Color neonColor) {
        this.rect = new Rectangle(x, y, width, height);
        this.text = text;
        this.neonColor = neonColor;
    }

    public void setHovered(boolean hovered) {
        this.isHovered = hovered;
    }

    public boolean contains(Point p) {
        return rect.contains(p);
    }

    public void draw(Graphics2D g2d, Font font) {
        if (isHovered) {
            g2d.setColor(new Color(neonColor.getRed(), neonColor.getGreen(), neonColor.getBlue(), 80));
            g2d.fillRect(rect.x, rect.y, rect.width, rect.height);
        } else {
            g2d.setColor(new Color(0, 0, 0, 150));
            g2d.fillRect(rect.x, rect.y, rect.width, rect.height);
        }

        // Efeito Neon
        if (isHovered) {
            g2d.setStroke(new BasicStroke(6));
            g2d.setColor(new Color(neonColor.getRed(), neonColor.getGreen(), neonColor.getBlue(), 50));
            g2d.drawRect(rect.x, rect.y, rect.width, rect.height);
        }

        // Borda principal
        g2d.setStroke(new BasicStroke(2));
        g2d.setColor(isHovered ? Color.WHITE : neonColor);
        g2d.drawRect(rect.x, rect.y, rect.width, rect.height);

        g2d.setFont(font);
        FontMetrics fm = g2d.getFontMetrics();
        int textX = rect.x + (rect.width - fm.stringWidth(text)) / 2;
        int textY = rect.y + ((rect.height - fm.getHeight()) / 2) + fm.getAscent();

        // Sombra do texto para contraste
        g2d.setColor(Color.BLACK);
        g2d.drawString(text, textX + 2, textY + 2);
        g2d.setColor(isHovered ? Color.WHITE : neonColor);
        g2d.drawString(text, textX, textY);
    }
}