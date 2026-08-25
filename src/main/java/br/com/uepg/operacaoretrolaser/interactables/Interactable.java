package br.com.uepg.operacaoretrolaser.interactables;

import br.com.uepg.operacaoretrolaser.player.Player;
import br.com.uepg.operacaoretrolaser.states.PlayState;

import java.awt.Graphics2D;

public interface Interactable {
    boolean isNear(float playerX, float playerY, int width, int height);
    void onInteract(PlayState state);
    void draw(Graphics2D g2d);
    boolean isConsumed();
    void drawPrompt(Graphics2D g2d, Player player);
}