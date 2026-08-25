package br.com.uepg.operacaoretrolaser.interactables;

import br.com.uepg.operacaoretrolaser.player.Player;
import br.com.uepg.operacaoretrolaser.settings.SoundManager;
import br.com.uepg.operacaoretrolaser.ui.GameMap;
import br.com.uepg.operacaoretrolaser.states.PlayState;

import java.awt.*;
import java.util.List;

public class Door implements Interactable {
    private final List<Point> tiles;
    private final int cost;
    private boolean open = false;
    private int animationTick = 0;

    public Door(List<Point> tiles, int cost) {
        this.tiles = tiles;
        this.cost = cost;
    }

    @Override
    public void drawPrompt(Graphics2D g2d, Player player) {
        float playerX = player.getX();
        float playerY = player.getY();
        int pWidth = player.getWidth();
        int pHeight = player.getHeight();

        if (open || tiles.isEmpty()) return;

        if (isNear(playerX, playerY, pWidth, pHeight)) {
            Point p = tiles.get(tiles.size() / 2); // Pega o bloco central da porta
            int worldX = p.x * GameMap.TILE_SIZE + GameMap.TILE_SIZE / 2;
            int worldY = p.y * GameMap.TILE_SIZE - 10; // Desenha acima da porta

            String msg = "Custo: " + cost + " [F]";
            g2d.setFont(new Font("fonts/PressStart2P-Regular.ttf", Font.BOLD, 14));
            int width = g2d.getFontMetrics().stringWidth(msg);

            g2d.setColor(new Color(0, 0, 0, 180));
            g2d.fillRect(worldX - width / 2 - 5, worldY - 15, width + 10, 20);

            g2d.setColor(new Color(102, 252, 241));
            g2d.drawString(msg, worldX - width / 2, worldY);
        }
    }

    @Override
    public boolean isNear(float playerX, float playerY, int width, int height) {
        float playerCenterX = playerX + width / 2f;
        float playerCenterY = playerY + height / 2f;

        for (Point p : tiles) {
            float tileCenterX = p.x * GameMap.TILE_SIZE + (GameMap.TILE_SIZE / 2f);
            float tileCenterY = p.y * GameMap.TILE_SIZE + (GameMap.TILE_SIZE / 2f);

            if (Math.hypot(tileCenterX - playerCenterX, tileCenterY - playerCenterY) <= 75) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onInteract(PlayState state) {
        if (!open && state.getDinheiro() >= cost) {
            state.gastarDinheiro(cost);
            open = true;
            state.getMap().liberarTilesDaPorta(tiles);
            SoundManager.playSFX("porta");
        }
    }

    @Override
    public void draw(Graphics2D g2d) {
        if (open || tiles.isEmpty()) return;

        animationTick++;

        for (Point p : tiles) {
            int px = p.x * GameMap.TILE_SIZE;
            int py = p.y * GameMap.TILE_SIZE;

            g2d.setColor(new Color(20, 20, 25));
            g2d.fillRect(px, py, GameMap.TILE_SIZE, GameMap.TILE_SIZE);

            g2d.setColor(new Color(60, 65, 75));
            g2d.fillRect(px + 4, py + 12, GameMap.TILE_SIZE - 8, 8);
            g2d.setColor(new Color(90, 95, 110));
            g2d.drawRect(px + 4, py + 12, GameMap.TILE_SIZE - 8, 8);

            int alpha = 150 + (int)(Math.sin(animationTick * 0.1) * 50);

            g2d.setColor(new Color(255, 50, 50, alpha)); // Vermelho Neon Translúcido
            g2d.fillRect(px + 8, py, GameMap.TILE_SIZE - 16, GameMap.TILE_SIZE);

            g2d.setColor(new Color(255, 150, 150, 220));
            g2d.drawLine(px + GameMap.TILE_SIZE / 2 - 2, py, px + GameMap.TILE_SIZE / 2 - 2, py + GameMap.TILE_SIZE);
            g2d.drawLine(px + GameMap.TILE_SIZE / 2 + 2, py, px + GameMap.TILE_SIZE / 2 + 2, py + GameMap.TILE_SIZE);

            if (animationTick % 10 < 5) {
                g2d.setColor(Color.WHITE);
                g2d.fillRect(px + 12, py + (animationTick % GameMap.TILE_SIZE), 2, 2);
                g2d.fillRect(px + 18, py + ((animationTick + 15) % GameMap.TILE_SIZE), 2, 2);
            }
        }
    }

    @Override
    public boolean isConsumed() {
        return open;
    }
}