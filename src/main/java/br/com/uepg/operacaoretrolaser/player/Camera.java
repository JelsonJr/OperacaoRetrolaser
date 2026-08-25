package br.com.uepg.operacaoretrolaser.player;

import br.com.uepg.operacaoretrolaser.ui.GameMap;
import br.com.uepg.operacaoretrolaser.GamePanel;

public class Camera {
    private float x, y;

    public Camera(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public void tick(Player player) {
        float targetX = player.getX() - (GamePanel.WIDTH / 2f) + (player.getWidth() / 2f);
        float targetY = player.getY() - (GamePanel.HEIGHT / 2f) + (player.getHeight() / 2f);

        // Interpolação suave (Lerp)
        x += (targetX - x) * 0.20f;
        y += (targetY - y) * 0.20f;

        // Se a diferença for minúscula, trava o valor exato
        if (Math.abs(targetX - x) < 0.1f) x = targetX;
        if (Math.abs(targetY - y) < 0.1f) y = targetY;

        // Limites do mapa
        if (x < 0) x = 0;
        if (y < 0) y = 0;

        int mapWidthMax = GameMap.COLS * GameMap.TILE_SIZE - GamePanel.WIDTH;
        int mapHeightMax = GameMap.ROWS * GameMap.TILE_SIZE - GamePanel.HEIGHT;

        if (x > mapWidthMax) x = mapWidthMax;
        if (y > mapHeightMax) y = mapHeightMax;
    }

    public float getX() { return x; }
    public float getY() { return y; }
}