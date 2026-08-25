package br.com.uepg.operacaoretrolaser.states;

import br.com.uepg.operacaoretrolaser.GamePanel;
import br.com.uepg.operacaoretrolaser.settings.Settings;
import br.com.uepg.operacaoretrolaser.settings.SoundManager;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

public class SettingsState implements GameState {
    private final GamePanel game;
    private final GameState previousState;

    private int indexSelecionado = 0;
    private int draggingIndex = -1;

    private final String[] opcoes = {"Volume Geral", "Musica", "Efeitos (SFX)", "Brilho"};
    private final Rectangle btnVoltar = new Rectangle(300, 550, 150, 40);

    public SettingsState(GamePanel game, GameState previousState) {
        this.game = game;
        this.previousState = previousState;
    }

    @Override
    public void update() {}

    @Override
    public void draw(Graphics2D g2d) {
        g2d.setColor(new Color(15, 20, 25));
        g2d.fillRect(0, 0, GamePanel.WIDTH, GamePanel.HEIGHT);

        g2d.setFont(game.getTitleFont().deriveFont(40f));
        g2d.setColor(Color.WHITE);
        g2d.drawString("CONFIGURACOES", 350, 100);

        g2d.setFont(game.getPixelFont());

        for (int i = 0; i < opcoes.length; i++) {
            g2d.setColor((i == indexSelecionado) ? new Color(102, 252, 241) : Color.GRAY);
            int y = 250 + (i * 60);
            g2d.drawString(opcoes[i], 300, y);

            g2d.drawRect(700, y - 20, 200, 20);

            float valor = obterValorOpcao(i);
            g2d.fillRect(700, y - 20, (int)(200 * valor), 20);
        }

        g2d.setColor((indexSelecionado == 4) ? new Color(255, 50, 50) : Color.GRAY);
        g2d.drawString("VOLTAR", btnVoltar.x, btnVoltar.y + 25);
    }

    private float obterValorOpcao(int index) {
        return switch(index) {
            case 0 -> Settings.masterVolume;
            case 1 -> Settings.musicVolume;
            case 2 -> Settings.sfxVolume;
            case 3 -> (Settings.brilho - 0.2f) / 0.8f;
            default -> 0;
        };
    }

    private void definirValorOpcao(int index, float pct) {
        pct = Math.clamp(pct, 0f, 1f);
        switch(index) {
            case 0 -> {
                Settings.masterVolume = pct;
                SoundManager.updateBackgroundMusicVolume();
                System.out.println("Volume Geral alterado para: " + pct);
            }
            case 1 -> {
                Settings.musicVolume = pct;
                SoundManager.updateBackgroundMusicVolume();
                System.out.println("Volume Música alterado para: " + pct);
            }
            case 2 -> {
                Settings.sfxVolume = pct;
                System.out.println("Volume SFX alterado para: " + pct);
            }
            case 3 -> Settings.brilho = 0.2f + (pct * 0.8f);
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
        Point p = e.getPoint();

        for (int i = 0; i < opcoes.length; i++) {
            Rectangle sliderBounds = new Rectangle(700, 250 + (i * 60) - 20, 200, 20);
            if (sliderBounds.contains(p)) {
                draggingIndex = i;
                indexSelecionado = i;
                float pct = (e.getX() - 700) / 200f;
                definirValorOpcao(i, pct);
                return;
            }
        }

        if (btnVoltar.contains(p)) {
            SoundManager.playSFX("clique");
            game.setState(previousState);
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        draggingIndex = -1;
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        // Se arrastando uma barra, atualiza o valor
        if (draggingIndex != -1) {
            float pct = (e.getX() - 700) / 200f;
            definirValorOpcao(draggingIndex, pct);
        } else {
            // Comportamento normal de apenas mover o mouse (Hover)
            Point p = e.getPoint();
            for (int i = 0; i < opcoes.length; i++) {
                Rectangle sliderBounds = new Rectangle(700, 250 + (i * 60) - 20, 200, 20);
                if (sliderBounds.contains(p)) {
                    indexSelecionado = i;
                }
            }
            if (btnVoltar.contains(p)) {
                indexSelecionado = 4;
            }
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();
        if (code == KeyEvent.VK_UP) indexSelecionado = (indexSelecionado - 1 + 5) % 5;
        if (code == KeyEvent.VK_DOWN) indexSelecionado = (indexSelecionado + 1) % 5;

        float ajuste = 0.05f;
        if (code == KeyEvent.VK_LEFT || code == KeyEvent.VK_RIGHT) {
            float dir = (code == KeyEvent.VK_RIGHT) ? ajuste : -ajuste;
            if (indexSelecionado < 4) {
                float atual = obterValorOpcao(indexSelecionado);
                definirValorOpcao(indexSelecionado, atual + dir);
            }
        }

        if (code == KeyEvent.VK_ENTER && indexSelecionado == 4) {
            game.setState(previousState);
        }
    }

    @Override public void keyReleased(KeyEvent e) {}

}