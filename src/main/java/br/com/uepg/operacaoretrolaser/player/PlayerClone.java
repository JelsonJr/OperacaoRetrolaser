package br.com.uepg.operacaoretrolaser.player;
import java.awt.*;

public class PlayerClone {
    private final float x, y;
    private int vida = 4;

    public PlayerClone(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public void receberDano(int dano) {
        this.vida -= dano;
    }

    public boolean isAlive() {
        return vida > 0;
    }

    public void draw(Graphics2D g2d) {
        if (!isAlive()) return;

        // Efeito de pulsação holográfica
        float pulse = (float) (Math.sin(System.currentTimeMillis() * 0.005) * 0.5 + 0.5);
        int alpha = (int) (100 + pulse * 80); // Transparência dinâmica

        // Corpo do Clone (Cor Ciano)
        g2d.setColor(new Color(0, 255, 255, alpha));
        g2d.fillOval((int) x + 8, (int) y, 16, 16); // Cabeça
        g2d.fillRoundRect((int) x + 6, (int) y + 16, 20, 16, 5, 5); // Torso

        // Efeito de Scanline (linha subindo e descendo)
        g2d.setColor(new Color(255, 255, 255, alpha + 50));
        int scanY = (int) ((System.currentTimeMillis() * 0.05) % 32);
        g2d.drawLine((int) x, (int) y + scanY, (int) x + 32, (int) y + scanY);

        // Indicador de vida do clone
        g2d.setColor(Color.RED);
        g2d.fillRect((int) x, (int) y - 8, 32, 4);
        g2d.setColor(Color.CYAN);
        g2d.fillRect((int) x, (int) y - 8, (int) (32 * (vida / 5.0f)), 4);
    }

    public float getX() { return x; }
    public float getY() { return y; }
}
