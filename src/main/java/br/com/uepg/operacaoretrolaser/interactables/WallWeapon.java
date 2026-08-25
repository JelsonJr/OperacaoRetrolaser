package br.com.uepg.operacaoretrolaser.interactables;

import br.com.uepg.operacaoretrolaser.player.Player;
import br.com.uepg.operacaoretrolaser.settings.SoundManager;
import br.com.uepg.operacaoretrolaser.states.PlayState;
import br.com.uepg.operacaoretrolaser.weapons.Weapon;

import java.awt.*;

public class WallWeapon implements Interactable {
    private final int x, y;
    private final Weapon arma;
    private final int custo;

    public WallWeapon(int x, int y, Weapon arma, int custo) {
        this.x = x;
        this.y = y;
        this.arma = arma;
        this.custo = custo;
    }

    @Override
    public boolean isNear(float playerX, float playerY, int width, int height) {
        float cx = x + 16, cy = y + 16;
        float pcx = playerX + width / 2f, pcy = playerY + height / 2f;
        return Math.hypot(cx - pcx, cy - pcy) < 60;
    }

    @Override
    public void onInteract(PlayState state) {
        if (state.getDinheiro() >= custo && !state.getPlayer().hasWeapon(arma)) {
            state.gastarDinheiro(custo);
            state.getPlayer().darArma(arma);
            SoundManager.playSFX("comprar");
        }
    }

    @Override
    public void draw(Graphics2D g2d) {
        float pulse = (float) (Math.sin(System.currentTimeMillis() * 0.005) * 0.5 + 0.5);
        int alphaGlow = (int) (80 + pulse * 80);

        g2d.setColor(new Color(0, 255, 255, alphaGlow));
        g2d.fillRoundRect(x + 2, y + 2, 28, 28, 8, 8);

        g2d.setColor(Color.WHITE);
        g2d.drawRoundRect(x + 2, y + 2, 28, 28, 8, 8);

        g2d.setColor(Color.DARK_GRAY);
        g2d.fillRect(x + 8, y + 12, 14, 6); // Corpo principal da arma
        g2d.fillRect(x + 8, y + 18, 4, 6);  // Cabo/Grip
        g2d.fillRect(x + 22, y + 14, 6, 2); // Cano

        g2d.setColor(Color.CYAN);
        g2d.fillRect(x + 12, y + 14, 4, 2);
    }

    @Override
    public boolean isConsumed() {
        return false;
    }

    @Override
    public void drawPrompt(Graphics2D g2d, Player player) {
        String msg = player.hasWeapon(arma) ? arma.getNome() + " (já adquirida)" : "[F] COMPRAR " + arma.getNome() + " ($" + custo + ")";
        g2d.setColor(Color.WHITE);
        g2d.drawString(msg, x - 20, y - 10);
    }
}