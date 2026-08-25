package br.com.uepg.operacaoretrolaser.interactables;

import br.com.uepg.operacaoretrolaser.player.Player;
import br.com.uepg.operacaoretrolaser.settings.SoundManager;
import br.com.uepg.operacaoretrolaser.states.PlayState;
import br.com.uepg.operacaoretrolaser.weapons.Weapon;
import java.awt.*;

public class UpgradeStation implements Interactable {
    private final int x, y;

    public UpgradeStation(int x, int y) {
        this.x = x;
        this.y = y;
    }

    private int getCustoUpgrade(int nivelAtual) {
        return switch (nivelAtual) {
            case 0 -> 5000;   // Custo para ir do 0 para o 1
            case 1 -> 10000;  // Custo para ir do 1 para o 2 (Libera tiro secundário)
            case 2 -> 15000;  // Custo para ir do 2 para o 3
            default -> 0;
        };
    }

    @Override
    public boolean isNear(float playerX, float playerY, int width, int height) {
        float cx = x + 16, cy = y + 16;
        float pcx = playerX + width / 2f, pcy = playerY + height / 2f;
        return Math.hypot(cx - pcx, cy - pcy) < 70;
    }

    @Override
    public void onInteract(PlayState state) {
        Player player = state.getPlayer();
        Weapon armaAtual = player.getArmaAtual();

        if (armaAtual != null && armaAtual.getNivelUpgrade() < 3) {
            int custoAtual = getCustoUpgrade(armaAtual.getNivelUpgrade());

            if (state.getDinheiro() >= custoAtual) {
                state.gastarDinheiro(custoAtual);
                armaAtual.promoverUpgrade();
                SoundManager.playSFX("comprar");
            }
        }
    }

    @Override
    public void draw(Graphics2D g2d) {
        g2d.setColor(new Color(150, 0, 255));
        g2d.fillRect(x, y, 32, 32);

        g2d.setColor(Color.WHITE);
        g2d.drawRect(x, y, 32, 32);

        g2d.setFont(new Font("fonts/PressStart2P-Regular.ttf", Font.BOLD, 12));
        g2d.drawString("UP", x + 8, y + 21);
    }

    @Override
    public boolean isConsumed() { return false; }

    @Override
    public void drawPrompt(Graphics2D g2d, Player player) {
        Weapon armaAtual = player.getArmaAtual();
        String msg;

        if (armaAtual == null) {
            msg = "Nenhuma arma equipada";
        } else if (armaAtual.getNivelUpgrade() >= 3) {
            msg = "Arma no Nível Máximo!";
        } else {
            int custoAtual = getCustoUpgrade(armaAtual.getNivelUpgrade());
            msg = "[F] MELHORAR " + armaAtual.getNome() + " ($" + custoAtual + ")";
        }

        g2d.setColor(Color.WHITE);
        g2d.drawString(msg, x - 40, y - 10);
    }
}