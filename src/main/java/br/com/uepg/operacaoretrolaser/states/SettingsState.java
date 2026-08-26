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

    private final String[] opcoes = {
            "Volume Geral",
            "Musica",
            "Efeitos (SFX)",
            "Brilho",
            "Tela Cheia: ",
            "Resolucao: ",
            "Esticar Tela: ",
            "APLICAR VIDEO"
    };

    private final Rectangle btnVoltar = new Rectangle(250, 620, 150, 40);

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
            int y = 200 + (i * 50);

            if (i < 4) {
                // Desenha os Sliders para Áudio e Brilho
                g2d.drawString(opcoes[i], 250, y);
                g2d.drawRect(650, y - 20, 200, 20);
                float valor = obterValorOpcao(i);
                g2d.fillRect(650, y - 20, (int)(200 * valor), 20);
            } else if (i == 4) {
                g2d.drawString(opcoes[i] + (Settings.isFullScreen ? "SIM" : "NAO"), 250, y);
            } else if (i == 5) {
                int[] res = Settings.RESOLUTIONS[Settings.resolutionIndex];
                g2d.drawString(opcoes[i] + res[0] + "x" + res[1], 250, y);
            } else if (i == 6) {
                g2d.drawString(opcoes[i] + (Settings.stretchScreen ? "SIM" : "NAO"), 250, y);
            } else if (i == 7) {
                g2d.setColor((i == indexSelecionado) ? Color.YELLOW : Color.GRAY);
                g2d.drawString(opcoes[i], 250, y);
            }
        }

        g2d.setColor((indexSelecionado == 8) ? new Color(255, 50, 50) : Color.GRAY);
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
            }
            case 1 -> {
                Settings.musicVolume = pct;
                SoundManager.updateBackgroundMusicVolume();
            }
            case 2 -> Settings.sfxVolume = pct;
            case 3 -> Settings.brilho = 0.2f + (pct * 0.8f);
        }
    }

    private void alterarOpcaoToggle(int dir) {
        SoundManager.playSFX("clique");
        if (indexSelecionado == 4) {
            Settings.isFullScreen = !Settings.isFullScreen;
        } else if (indexSelecionado == 5) {
            Settings.resolutionIndex = (Settings.resolutionIndex + dir + Settings.RESOLUTIONS.length) % Settings.RESOLUTIONS.length;
        } else if (indexSelecionado == 6) {
            Settings.stretchScreen = !Settings.stretchScreen;
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
        Point p = e.getPoint();

        for (int i = 0; i < opcoes.length; i++) {
            Rectangle bound = new Rectangle(250, 200 + (i * 50) - 35, 600, 40);
            if (bound.contains(p)) {
                indexSelecionado = i;
                if (i < 4) {
                    float pct = (e.getX() - 650) / 200f;
                    definirValorOpcao(i, pct);
                    draggingIndex = i;
                } else if (i <= 6) {
                    alterarOpcaoToggle(1);
                } else if (i == 7) {
                    SoundManager.playSFX("clique");
                    Settings.salvar();
                    game.applyVideoSettings();
                }
                return;
            }
        }

        if (btnVoltar.contains(p)) {
            SoundManager.playSFX("clique");
            Settings.salvar();
            game.setState(previousState);
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        draggingIndex = -1;
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        if (draggingIndex != -1) {
            float pct = (e.getX() - 650) / 200f;
            definirValorOpcao(draggingIndex, pct);
        } else {
            Point p = e.getPoint();
            for (int i = 0; i < opcoes.length; i++) {
                Rectangle bound = new Rectangle(250, 200 + (i * 50) - 35, 600, 40);
                if (bound.contains(p)) {
                    indexSelecionado = i;
                }
            }
            if (btnVoltar.contains(p)) {
                indexSelecionado = 8;
            }
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();
        if (code == KeyEvent.VK_UP) indexSelecionado = (indexSelecionado - 1 + 9) % 9;
        if (code == KeyEvent.VK_DOWN) indexSelecionado = (indexSelecionado + 1) % 9;

        if (code == KeyEvent.VK_LEFT || code == KeyEvent.VK_RIGHT) {
            int dir = (code == KeyEvent.VK_RIGHT) ? 1 : -1;
            if (indexSelecionado < 4) {
                float atual = obterValorOpcao(indexSelecionado);
                definirValorOpcao(indexSelecionado, atual + (dir * 0.05f));
            } else if (indexSelecionado <= 6) {
                alterarOpcaoToggle(dir);
            }
        }

        if (code == KeyEvent.VK_ENTER) {
            if (indexSelecionado >= 4 && indexSelecionado <= 6) {
                alterarOpcaoToggle(1);
            } else if (indexSelecionado == 7) {
                SoundManager.playSFX("clique");
                game.applyVideoSettings();
            } else if (indexSelecionado == 8) {
                SoundManager.playSFX("clique");
                game.setState(previousState);
            }
        }
    }

    @Override public void keyReleased(KeyEvent e) {}
}