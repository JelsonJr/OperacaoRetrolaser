package br.com.uepg.operacaoretrolaser.states;

import br.com.uepg.operacaoretrolaser.GamePanel;
import br.com.uepg.operacaoretrolaser.settings.SoundManager;
import br.com.uepg.operacaoretrolaser.ui.NeonButton;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

public class TutorialState implements GameState {

    private final GamePanel game;
    private final NeonButton btnVoltar;
    private final NeonButton btnProximo;
    private final NeonButton btnAnterior;

    private int paginaAtual = 0;
    private final int TOTAL_PAGINAS = 3;

    public TutorialState(GamePanel game) {
        this.game = game;

        Color cianoNeon = new Color(102, 252, 241);
        Color magentaNeon = new Color(255, 0, 128);

        int bottomY = GamePanel.HEIGHT - 70;
        btnVoltar = new NeonButton(30, bottomY, 180, 45, "VOLTAR", cianoNeon);
        btnAnterior = new NeonButton(GamePanel.WIDTH - 480, bottomY, 220, 45, "< ANTERIOR", magentaNeon);
        btnProximo = new NeonButton(GamePanel.WIDTH - 250, bottomY, 220, 45, "PRÓXIMO >", magentaNeon);
    }

    @Override
    public void update() {
    }

    @Override
    public void draw(Graphics2D g2d) {
        g2d.setColor(new Color(15, 15, 25));
        g2d.fillRect(0, 0, GamePanel.WIDTH, GamePanel.HEIGHT);

        g2d.setFont(game.getTitleFont().deriveFont(38f));
        g2d.setColor(new Color(102, 252, 241));
        String title = "TUTORIAL - PÁGINA " + (paginaAtual + 1) + "/" + TOTAL_PAGINAS;
        g2d.drawString(title, 40, 60);

        g2d.setColor(new Color(102, 252, 241, 100));
        g2d.fillRect(40, 75, GamePanel.WIDTH - 80, 2);

        switch (paginaAtual) {
            case 0 -> drawPaginaControlesEMecanicas(g2d);
            case 1 -> drawPaginaHabilidadesPart1(g2d);
            case 2 -> drawPaginaHabilidadesPart2(g2d);
        }

        btnVoltar.draw(g2d, game.getPixelFont());
        if (paginaAtual > 0) btnAnterior.draw(g2d, game.getPixelFont());
        if (paginaAtual < TOTAL_PAGINAS - 1) btnProximo.draw(g2d, game.getPixelFont());
    }

    private void drawPaginaControlesEMecanicas(Graphics2D g2d) {
        int startX = 50;
        int startY = 110;

        drawSectionTitle(g2d, "CONTROLES DE JOGO", startX, startY);

        int y = startY + 35;
        drawInfoText(g2d, "WASD", "Movimentação do personagem", startX, y);
        drawInfoText(g2d, "MOUSE ESQUERDO", "Dispara o tiro principal da arma", startX, y += 28);
        drawInfoText(g2d, "MOUSE DIREITO", "Dispara o tiro secundário (quando liberado)", startX, y += 28);
        drawInfoText(g2d, "TECLAS 1 e 2", "Troca de arma (liberado pelo Perk Pistoleiro)", startX, y += 28);
        drawInfoText(g2d, "TECLA G", "Cria um clone falso (liberado pelo Perk Replicante)", startX, y += 28);
        drawInfoText(g2d, "TECLA F", "Interagir com portas, máquinas de Perk e Upgrade", startX, y += 28);

        int y2 = y + 45;
        drawSectionTitle(g2d, "UPGRADE STATION & MECÂNICAS", startX, y2);

        y2 += 35;
        drawInfoText(g2d, "UPGRADE STATION", "Melhora armas de fogo (Nível 1: $5.000 | Nível 2: $10.000 | Nível 3: $15.000)", startX, y2);
        drawInfoText(g2d, "TIRO SECUNDÁRIO", "Desbloqueado automaticamente a partir do Nível 2 na Upgrade Station", startX, y2 += 28);
        drawInfoText(g2d, "PROGRESSÃO MELEE", "A cada round concluído, você ganha +3 de ataque Melee naturalmente", startX, y2 += 28);
        drawInfoText(g2d, "INTERVALO ROUND", "Entre cada round existe um tempo de descanso e preparo de 15 segundos", startX, y2 += 28);
    }

    private void drawPaginaHabilidadesPart1(Graphics2D g2d) {
        int startX = 50;
        int startY = 110;

        drawSectionTitle(g2d, "PERKS & HABILIDADES (PARTE 1)", startX, startY);

        int y = startY + 40;
        drawPerkBox(g2d, "CHANCE EXTRA", "Revive o jogador ao morrer em troca de todas as habilidades adquiridas. Também regenera a saúde mais rápido.", startX, y);
        drawPerkBox(g2d, "TANQUE", "Aumenta a vida máxima do jogador, permitindo aguentar mais dano dos robôs.", startX, y += 80);
        drawPerkBox(g2d, "PULMÃO DE ATLETA", "Consome menos stamina ao correr, permite correr mais rápido e regenera stamina mais rapidamente.", startX, y += 80);
        drawPerkBox(g2d, "VISÃO DE ÁGUIA", "Remove completamente a Fog of War (névoa de guerra) do mapa.", startX, y += 80);
    }

    private void drawPaginaHabilidadesPart2(Graphics2D g2d) {
        int startX = 50;
        int startY = 110;

        drawSectionTitle(g2d, "PERKS & HABILIDADES (PARTE 2)", startX, startY);

        int y = startY + 40;
        drawPerkBox(g2d, "GOLPE DURO", "Aumenta significativamente a potência dos seus ataques corpo a corpo (Melee).", startX, y);
        drawPerkBox(g2d, "PISTOLEIRO", "Permite carregar e utilizar duas armas de fogo simultaneamente (troque com as teclas 1 e 2).", startX, y += 80);
        drawPerkBox(g2d, "TIRO DUPLO", "Duplica todo o dano provocado por suas armas de fogo.", startX, y += 80);
        drawPerkBox(g2d, "REPLICANTE", "1 vez por round, aperte G para criar um clone de si mesmo que atrai e distrai os robôs.", startX, y += 80);
    }

    private void drawSectionTitle(Graphics2D g2d, String text, int x, int y) {
        g2d.setFont(game.getPixelFont().deriveFont(18f));
        g2d.setColor(new Color(255, 204, 0));
        g2d.drawString(text, x, y);
    }

    private void drawInfoText(Graphics2D g2d, String key, String desc, int x, int y) {
        g2d.setFont(game.getPixelFont().deriveFont(13f));
        g2d.setColor(new Color(102, 252, 241));
        g2d.drawString(key + ": ", x, y);

        int keyWidth = g2d.getFontMetrics().stringWidth(key + ": ");
        g2d.setColor(Color.WHITE);
        g2d.drawString(desc, x + keyWidth, y);
    }

    private void drawPerkBox(Graphics2D g2d, String nome, String desc, int x, int y) {
        g2d.setColor(new Color(25, 25, 40, 200));
        g2d.fillRoundRect(x, y, GamePanel.WIDTH - 100, 68, 10, 10);

        g2d.setColor(new Color(255, 0, 128, 180));
        g2d.drawRoundRect(x, y, GamePanel.WIDTH - 100, 68, 10, 10);

        g2d.setFont(game.getPixelFont().deriveFont(15f));
        g2d.setColor(new Color(255, 204, 0));
        g2d.drawString(nome, x + 15, y + 26);

        g2d.setFont(game.getPixelFont().deriveFont(12f));
        g2d.setColor(Color.WHITE);
        g2d.drawString(desc, x + 15, y + 50);
    }

    @Override
    public void mousePressed(MouseEvent e) {
        Point p = e.getPoint();

        if (btnVoltar.contains(p) || btnVoltar.contains(p)) {
            SoundManager.playSFX("clique");
            game.setState(new MenuState(game));
        } else if (paginaAtual < TOTAL_PAGINAS - 1 && (btnProximo.contains(p) || btnProximo.contains(p))) {
            SoundManager.playSFX("clique");
            paginaAtual++;
        } else if (paginaAtual > 0 && (btnAnterior.contains(p) || btnAnterior.contains(p))) {
            SoundManager.playSFX("clique");
            paginaAtual--;
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {}

    @Override
    public void mouseMoved(MouseEvent e) {
        Point p = e.getPoint();

        btnAnterior.setHovered(btnAnterior.contains(p));
        btnProximo.setHovered(btnProximo.contains(p));
        btnVoltar.setHovered(btnVoltar.contains(p));
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            SoundManager.playSFX("clique");
            game.setState(new MenuState(game));
        } else if (e.getKeyCode() == KeyEvent.VK_RIGHT && paginaAtual < TOTAL_PAGINAS - 1) {
            SoundManager.playSFX("clique");
            paginaAtual++;
        } else if (e.getKeyCode() == KeyEvent.VK_LEFT && paginaAtual > 0) {
            SoundManager.playSFX("clique");
            paginaAtual--;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {}
}