package br.com.uepg.operacaoretrolaser.interactables;

import br.com.uepg.operacaoretrolaser.settings.SoundManager;
import br.com.uepg.operacaoretrolaser.states.PlayState;
import br.com.uepg.operacaoretrolaser.player.Player;

import java.awt.*;
import java.awt.geom.Point2D;

public class PerkMachine implements Interactable {
    private final float x, y;
    private final PerkType perk;
    private final int width = 32, height = 32;

    public PerkMachine(float x, float y, PerkType perk) {
        this.x = x;
        this.y = y;
        this.perk = perk;
    }

    @Override
    public boolean isNear(float playerX, float playerY, int pWidth, int pHeight) {
        float cx = x + width / 2f;
        float cy = y + height / 2f;
        float pcx = playerX + pWidth / 2f;
        float pcy = playerY + pHeight / 2f;
        return Point2D.distance(cx, cy, pcx, pcy) < 70;
    }

    public float getX() { return x; }
    public float getY() { return y; }

    @Override
    public void onInteract(PlayState state) {
        Player player = state.getPlayer();

        if (player.hasPerk(perk)) {
            return;
        }

        if (state.getDinheiro() >= perk.getCusto()) {
            if (player.getPerksAtivos().size() < 5) {
                state.gastarDinheiro(perk.getCusto());
                player.addPerk(perk);
                SoundManager.playSFX("comprar");
            } else {
                state.iniciarTrocaPerk(this.perk);
            }
        }
    }

    @Override
    public void draw(Graphics2D g2d) {
        g2d.setColor(new Color(40, 40, 50));
        g2d.fillRect((int)x, (int)y, width, height);
        g2d.setColor(new Color(20, 20, 25));
        g2d.drawRect((int)x, (int)y, width, height);

        g2d.setColor(getColorForPerk(perk));
        g2d.fillRect((int)x + 4, (int)y + 4, width - 8, height - 16);

        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("fonts/PressStart2P-Regular.ttf", Font.BOLD, 12));
        String sigla = perk.name().substring(0, 2);

        FontMetrics fm = g2d.getFontMetrics();
        int textX = (int)x + (width - fm.stringWidth(sigla)) / 2;
        g2d.drawString(sigla, textX, y + 16);
    }

    @Override
    public boolean isConsumed() {
        return false; // Máquinas nunca somem do mapa
    }

    @Override
    public void drawPrompt(Graphics2D g2d, Player player) {
        String texto = player.hasPerk(this.perk) ? "Já adquirido" :  "[F] " + perk.getNome() + " ($" + perk.getCusto() + ")";

        g2d.setFont(new Font("/fonts/PressStart2P-Regular.ttf", Font.BOLD, 10));
        FontMetrics fm = g2d.getFontMetrics();
        int textWidth = fm.stringWidth(texto);
        int textHeight = fm.getHeight();

        int drawX = (int) (this.x + this.width / 2f - textWidth / 2f);
        int drawY = (int) (this.y - 10);

        g2d.setColor(new Color(0, 0, 0, 180));
        g2d.fillRoundRect(drawX - 4, drawY - textHeight + 3, textWidth + 8, textHeight + 2, 5, 5);

        g2d.setColor(getColorForPerk(perk));
        g2d.drawRoundRect(drawX - 4, drawY - textHeight + 3, textWidth + 8, textHeight + 2, 5, 5);

        g2d.setColor(Color.WHITE);
        g2d.drawString(texto, drawX, drawY);
    }

    private Color getColorForPerk(PerkType type) {
        return switch (type) {
            case TANQUE -> new Color(220, 20, 60);       // Vermelho vivo
            case CHANCE_EXTRA -> new Color(0, 255, 255); // Ciano
            case PULMAO_ATLETA -> new Color(50, 205, 50);// Verde claro
            case GOLPE_DURO -> new Color(255, 140, 0);   // Laranja
            case VISAO_AGUIA -> new Color(255, 215, 0);  // Dourado/Amarelo
            case PISTOLEIRO -> new Color(255, 0, 255);   // Magenta
            case TIRO_DUPLO -> new Color(138, 43, 226);  // Roxo escuro
            case REPLICANTE -> new Color(192, 192, 192); // Prateado
        };
    }
}