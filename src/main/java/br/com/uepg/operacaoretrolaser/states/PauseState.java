package br.com.uepg.operacaoretrolaser.states;

import br.com.uepg.operacaoretrolaser.GamePanel;
import br.com.uepg.operacaoretrolaser.settings.SoundManager;
import br.com.uepg.operacaoretrolaser.ui.NeonButton;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

public class PauseState implements GameState {

    private final GamePanel game;
    private final NeonButton btnRetomar, btnMenu, btnConfig;

    public PauseState(GamePanel game) {
        this.game = game;
        Color magentaNeon = new Color(255, 0, 255);

        int center = GamePanel.WIDTH / 2 - 150;
        int startY = GamePanel.HEIGHT / 2 - 30;

        btnRetomar = new NeonButton(center, startY, 300, 50, "RETOMAR", magentaNeon);
        btnConfig =  new NeonButton(center, startY + 80, 300, 50, "CONFIGURAÇÕES", magentaNeon);
        btnMenu = new NeonButton(center, startY + 160, 300, 50, "MENU PRINCIPAL", magentaNeon);
    }

    @Override
    public void update() {}

    @Override
    public void draw(Graphics2D g2d) {
        // 1. Desenha a tela do jogo congelada ao fundo
        game.getPlayState().draw(g2d);

        // 2. Overlay escuro transparente
        g2d.setColor(new Color(0, 0, 0, 200));
        g2d.fillRect(0, 0, GamePanel.WIDTH, GamePanel.HEIGHT);

        // 3. HUD do Canto Superior Esquerdo (Tempo e FPS)
        long tempoMs = System.currentTimeMillis() - game.getPlayState().getStartTime();
        long totalSegundos = tempoMs / 1000;
        long min = totalSegundos / 60;
        long seg = totalSegundos % 60;
        String tempoStr = String.format("TEMPO DE JOGO: %02d:%02d", min, seg);
        String fpsStr = "FPS: " + game.getFps();

        g2d.setFont(game.getPixelFont().deriveFont(13f));
        g2d.setColor(Color.WHITE);
        g2d.drawString(tempoStr, 20, 35);
        g2d.setColor(Color.GRAY);
        g2d.drawString(fpsStr, 20, 55);

        // 4. Título PAUSADO com brilho
        g2d.setFont(game.getTitleFont().deriveFont(55f));
        String text = "PAUSADO";
        FontMetrics fm = g2d.getFontMetrics();
        int textX = (GamePanel.WIDTH - fm.stringWidth(text)) / 2;

        g2d.setColor(new Color(255, 0, 255, 100));
        g2d.drawString(text, textX, GamePanel.HEIGHT / 3 + 4);
        g2d.setColor(Color.WHITE);
        g2d.drawString(text, textX, GamePanel.HEIGHT / 3);

        // 5. Botões
        btnRetomar.draw(g2d, game.getPixelFont());
        btnConfig.draw(g2d, game.getPixelFont());
        btnMenu.draw(g2d, game.getPixelFont());
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        Point p = e.getPoint();
        btnRetomar.setHovered(btnRetomar.contains(p));
        btnMenu.setHovered(btnMenu.contains(p));
        btnConfig.setHovered(btnConfig.contains(p));
    }

    @Override
    public void mousePressed(MouseEvent e) {
        Point p = e.getPoint();
        SoundManager.playSFX("clique");

        if (btnRetomar.contains(p)) {
            game.setState(game.getPlayState());
        } else if (btnMenu.contains(p)) {
            game.setState(game.getMenuState());
        } else if (btnConfig.contains(e.getPoint())) {
            game.setState(new SettingsState(game, this));
        }
    }

    @Override public void mouseReleased(MouseEvent e) {}

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            game.setState(game.getPlayState());
        }
    }

    @Override public void keyReleased(KeyEvent e) {}
}