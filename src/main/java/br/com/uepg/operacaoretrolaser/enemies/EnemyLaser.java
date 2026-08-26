package br.com.uepg.operacaoretrolaser.enemies;

import br.com.uepg.operacaoretrolaser.player.Player;
import br.com.uepg.operacaoretrolaser.ui.GameMap;

import java.awt.*;

public class EnemyLaser {
    private final float vx, vy;
    private float x, y;
    private float distanceTraveled = 0;
    private boolean active = true;

    public EnemyLaser(float startX, float startY, float targetX, float targetY) {
        this.x = startX;
        this.y = startY;

        // Calcula o ângulo em direção ao jogador
        float angle = (float) Math.atan2(targetY - startY, targetX - startX);
        float speed = 7.0f;
        this.vx = (float) Math.cos(angle) * speed;
        this.vy = (float) Math.sin(angle) * speed;
    }

    public void update(GameMap map, Player player) {
        if (!active) return;
        
        x += vx;
        y += vy;
        distanceTraveled = (float) (distanceTraveled + Math.hypot(vx, vy));

        if (!map.isFree(x, y, 2, 2)) {
            active = false;
            return;
        }

        float MAX_DISTANCE = 20 * GameMap.TILE_SIZE;

        if (distanceTraveled >= MAX_DISTANCE) {
            active = false;
            return;
        }

        // Checa colisão com o Player (simples verificação de caixa/bounding box)
        if (x >= player.getX() && x <= player.getX() + player.getWidth() &&
                y >= player.getY() && y <= player.getY() + player.getHeight()) {
            player.tomarDano(1);
            active = false;
        }
    }

    public void draw(Graphics2D g2d) {
        g2d.setColor(Color.RED);
        g2d.fillOval((int) x - 3, (int) y - 3, 6, 6);
    }

    public boolean isActive() {
        return active;
    }
}