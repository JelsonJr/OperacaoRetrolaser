package br.com.uepg.operacaoretrolaser.states;

import br.com.uepg.operacaoretrolaser.GamePanel;
import br.com.uepg.operacaoretrolaser.settings.SoundManager;
import br.com.uepg.operacaoretrolaser.ui.NeonButton;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

public class GameOverState implements GameState {

    private final GamePanel game;
    private final NeonButton btnReiniciar, btnMenu;
    private final int robosMortos;
    private final int round;
    private final long tempoMs;
    private final int pontosTotais;
    private final int pontosGastos;
    private final int portasAbertas;

    public GameOverState(GamePanel game, int robosMortos, int round, long tempoMs, int pontosTotais, int pontosGastos, int portasAbertas) {
        this.game = game;
        this.robosMortos = robosMortos;
        this.round = round;
        this.tempoMs = tempoMs;
        this.pontosTotais = pontosTotais;
        this.pontosGastos = pontosGastos;
        this.portasAbertas = portasAbertas;

        Color vermelhoNeon = new Color(255, 50, 50);
        Color cianoNeon = new Color(102, 252, 241);

        int center = GamePanel.WIDTH / 2 - 150;
        int startY = GamePanel.HEIGHT - 120;

        btnReiniciar = new NeonButton(center - 160, startY, 300, 50, "REINICIAR", cianoNeon);
        btnMenu = new NeonButton(center + 160, startY, 300, 50, "MENU PRINCIPAL", vermelhoNeon);
    }

    @Override
    public void update() {}

    @Override
    public void draw(Graphics2D g2d) {
        // Overlay de Fundo Escuro Avermelhado
        g2d.setColor(new Color(15, 5, 5, 245));
        g2d.fillRect(0, 0, GamePanel.WIDTH, GamePanel.HEIGHT);

        // Título GAME OVER com Sombra
        g2d.setFont(game.getTitleFont().deriveFont(55f));
        String title = "GAME OVER";
        FontMetrics fmTitle = g2d.getFontMetrics();
        int titleX = (GamePanel.WIDTH - fmTitle.stringWidth(title)) / 2;

        g2d.setColor(new Color(255, 0, 0, 120));
        g2d.drawString(title, titleX + 4, 104);
        g2d.setColor(new Color(255, 50, 50));
        g2d.drawString(title, titleX, 100);

        // Painel Central de Estatísticas
        int boxWidth = 520;
        int boxHeight = 290;
        int boxX = (GamePanel.WIDTH - boxWidth) / 2;
        int boxY = 140;

        g2d.setColor(new Color(0, 0, 0, 190));
        g2d.fillRoundRect(boxX, boxY, boxWidth, boxHeight, 16, 16);
        g2d.setColor(new Color(255, 50, 50, 160));
        g2d.drawRoundRect(boxX, boxY, boxWidth, boxHeight, 16, 16);

        // Formatação das Estatísticas
        g2d.setFont(game.getPixelFont().deriveFont(15f));

        long totalSegundos = tempoMs / 1000;
        long min = totalSegundos / 60;
        long seg = totalSegundos % 60;
        String tempoFormatado = String.format("%02d:%02d", min, seg);

        int lineY = boxY + 45;
        int stepY = 38;
        int leftX = boxX + 40;

        desenharLinhaStat(g2d, "ROBÔS ELIMINADOS:", String.valueOf(robosMortos), leftX, lineY);
        desenharLinhaStat(g2d, "ROUND ALCANÇADO:", String.valueOf(round), leftX, lineY + stepY);
        desenharLinhaStat(g2d, "TEMPO DE PARTIDA:", tempoFormatado, leftX, lineY + stepY * 2);
        desenharLinhaStat(g2d, "PONTOS TOTAIS:", String.valueOf(pontosTotais), leftX, lineY + stepY * 3);
        desenharLinhaStat(g2d, "PONTOS GASTOS:", String.valueOf(pontosGastos), leftX, lineY + stepY * 4);
        desenharLinhaStat(g2d, "PORTAS ABERTAS:", String.valueOf(portasAbertas), leftX, lineY + stepY * 5);

        // Desenho dos Botões
        btnReiniciar.draw(g2d, game.getPixelFont());
        btnMenu.draw(g2d, game.getPixelFont());
    }

    private void desenharLinhaStat(Graphics2D g2d, String label, String valor, int x, int y) {
        g2d.setColor(Color.LIGHT_GRAY);
        g2d.drawString(label, x, y);
        g2d.setColor(new Color(57, 255, 20)); // Verde Neon
        g2d.drawString(valor, x + 300, y);
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        Point p = e.getPoint();
        btnReiniciar.setHovered(btnReiniciar.contains(p));
        btnMenu.setHovered(btnMenu.contains(p));
    }

    @Override
    public void mousePressed(MouseEvent e) {
        Point p = e.getPoint();
        SoundManager.playSFX("clique");

        if (btnReiniciar.contains(p)) {
            game.reiniciarJogo();
        } else if (btnMenu.contains(p)) {
            game.setState(game.getMenuState());
        }
    }

    @Override public void mouseReleased(MouseEvent e) {}
    @Override public void keyPressed(KeyEvent e) {}
    @Override public void keyReleased(KeyEvent e) {}
}