package br.com.uepg.operacaoretrolaser.states;

import br.com.uepg.operacaoretrolaser.GamePanel;
import br.com.uepg.operacaoretrolaser.settings.SoundManager;
import br.com.uepg.operacaoretrolaser.ui.NeonButton;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class MenuState implements GameState {
    private final GamePanel game;
    private final NeonButton btnIniciar, btnGithub, btnSair, btnConfig, btnTutorial;

    public MenuState(GamePanel game) {
        this.game = game;
        Color cianoNeon = new Color(102, 252, 241);

        int center = GamePanel.WIDTH / 2 - 150;
        int startY = GamePanel.HEIGHT / 2 - 40;

        btnIniciar = new NeonButton(center, startY, 300, 50, "INICIAR JOGO", cianoNeon);
        btnConfig = new NeonButton(center, startY + 70, 300, 50, "CONFIGURAÇÕES", cianoNeon);
        btnGithub = new NeonButton(center, startY + 140, 300, 50, "GITHUB", cianoNeon);
        btnTutorial =  new NeonButton(center, startY + 210, 300, 50, "TUTORIAL", cianoNeon);
        btnSair = new NeonButton(center, startY + 280, 300, 50, "SAIR", new Color(255, 0, 85)); // Rosa avermelhado pro sair
    }

    @Override
    public void update() {
    }

    @Override
    public void draw(Graphics2D g2d) {
        var state = this.game.getPlayState();
        state.drawInMenu(g2d);

        g2d.setColor(new Color(0, 0, 0, 200));
        g2d.fillRect(0, 0, GamePanel.WIDTH, GamePanel.HEIGHT);

        g2d.setFont(game.getTitleFont());
        String title = "OPERAÇÃO RETROLASER";
        FontMetrics fm = g2d.getFontMetrics();

        int titleX = -fm.stringWidth(title) / 2;

        AffineTransform oldTransform = g2d.getTransform();

        g2d.translate(GamePanel.WIDTH / 2.0, GamePanel.HEIGHT / 3.5);
        g2d.scale(1.0, 0.5);
        g2d.shear(-0.25, 0.0);
        g2d.setColor(Color.BLACK);
        g2d.drawString(title, titleX + 8, 8); // Sombra

        g2d.setColor(new Color(255, 232, 31)); // Amarelo
        g2d.drawString(title, titleX, 0);

        g2d.setTransform(oldTransform);

        btnIniciar.draw(g2d, game.getPixelFont());
        btnGithub.draw(g2d, game.getPixelFont());
        btnConfig.draw(g2d, game.getPixelFont());
        btnTutorial.draw(g2d, game.getPixelFont());
        btnSair.draw(g2d, game.getPixelFont());
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        Point p = e.getPoint();
        btnIniciar.setHovered(btnIniciar.contains(p));
        btnGithub.setHovered(btnGithub.contains(p));
        btnSair.setHovered(btnSair.contains(p));
        btnConfig.setHovered(btnConfig.contains(p));
        btnTutorial.setHovered(btnTutorial.contains(p));
    }

    @Override
    public void mousePressed(MouseEvent e) {
        Point p = e.getPoint();
        SoundManager.playSFX("clique");

        if (btnIniciar.contains(p)) {
            game.setState(game.getPlayState());
        } else if (btnGithub.contains(p)) {
            try {
                Desktop.getDesktop().browse(new URI("https://github.com/JelsonJr"));
            } catch (IOException | URISyntaxException ex) {
                System.out.println("Erro ao tentar abrir github: " +  ex.getMessage());
            }
        } else if (btnSair.contains(p)) {
            System.exit(0);
        } else if (btnConfig.contains(e.getPoint())) {
            game.setState(new SettingsState(game, this));
        }
        else if (btnTutorial.contains(e.getPoint())) {
            game.setState(new TutorialState(game));
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {}

    @Override
    public void keyPressed(KeyEvent e) {
    }

    @Override
    public void keyReleased(KeyEvent e) {}
}
