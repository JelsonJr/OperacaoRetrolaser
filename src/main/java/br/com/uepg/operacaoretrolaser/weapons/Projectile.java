package br.com.uepg.operacaoretrolaser.weapons;

import br.com.uepg.operacaoretrolaser.ui.GameMap;
import java.awt.*;

public class Projectile {
    private final float angle;
    private float x, y;
    private float distanceTraveled = 0;
    private boolean active = true;

    private final int dano;
    private final boolean isEspecial;
    private final float alcanceMaximo;

    public Projectile(float startX, float startY, float targetX, float targetY, int dano, boolean isEspecial, float alcanceMaximo) {
        this.x = startX;
        this.y = startY;
        this.dano = dano;
        this.isEspecial = isEspecial;
        this.alcanceMaximo = alcanceMaximo;

        this.angle = (float) Math.atan2(targetY - startY, targetX - startX);
    }

    public float getX() { return x; }
    public float getY() { return y; }
    public boolean isActive() { return active; }
    public int getDano() { return dano; }
    public void desativar() { this.active = false; }

    public void update(GameMap map) {
        if (!active) return;

        final float speed = isEspecial ? 20f : 15f;
        float nextX = x + (float) Math.cos(angle) * speed;
        float nextY = y + (float) Math.sin(angle) * speed;

        if (!map.isFree(nextX, nextY, 2, 2)) {
            active = false;
            return;
        }

        x = nextX;
        y = nextY;
        distanceTraveled += speed;

        if (distanceTraveled >= alcanceMaximo) {
            active = false;
        }
    }

    public void draw(Graphics2D g2d) {
        if (!active) return;

        int length = isEspecial ? 16 : 12;
        int tailX = (int) (x - Math.cos(angle) * length);
        int tailY = (int) (y - Math.sin(angle) * length);

        var cor = isEspecial ? new Color(180, 50, 255) : new Color(255, 204, 0);
        Stroke oldStroke = g2d.getStroke();

        g2d.setColor(cor);
        g2d.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2d.drawLine(tailX, tailY, (int) x, (int) y);

        g2d.setStroke(oldStroke);
    }

    // inicialmente obrigatório no projeto
    private void desenharLinhaBresenham(Graphics2D g2d, int x1, int y1, int x2, int y2, Color color) {
        g2d.setColor(color);
        int dx = Math.abs(x2 - x1);
        int dy = Math.abs(y2 - y1);
        int sx = x1 < x2 ? 1 : -1;
        int sy = y1 < y2 ? 1 : -1;
        int err = dx - dy;

        while (true) {
            g2d.fillRect(x1, y1, 2, 2);
            if (x1 == x2 && y1 == y2) break;
            int e2 = 2 * err;
            if (e2 > -dy) { err -= dy; x1 += sx; }
            if (e2 < dx) { err += dx; y1 += sy; }
        }
    }
}