package br.com.uepg.operacaoretrolaser.ui;

import java.awt.*;

public class FloatingText {
    private final double x;
    private double y;
    private final String text;
    private final Color color;
    private int life;
    private final int maxLife;
    private final double speedY;
    private final boolean isUI;

    public FloatingText(double x, double y, String text, Color color, int life, double speedY, boolean isUI) {
        this.x = x;
        this.y = y;
        this.text = text;
        this.color = color;
        this.life = life;
        this.maxLife = life;
        this.speedY = speedY;
        this.isUI = isUI;
    }

    public boolean isUI() { return isUI; }

    public boolean update() {
        y -= speedY;
        life--;

        return life <= 0;
    }

    public void draw(Graphics2D g2d) {
        int alpha = (int) (255 * ((double) life / maxLife));
        alpha = Math.clamp(alpha, 0, 255);

        g2d.setFont(new Font("/fonts/PressStart2P-Regular.ttf", Font.BOLD, 16));
        g2d.setColor(new Color(0, 0, 0, alpha));
        g2d.drawString(text, (int) x + 1, (int) y + 1);

        g2d.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha));
        g2d.drawString(text, (int) x, (int) y);
    }
}