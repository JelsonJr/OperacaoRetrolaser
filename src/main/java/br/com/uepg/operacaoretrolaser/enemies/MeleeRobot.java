package br.com.uepg.operacaoretrolaser.enemies;

import br.com.uepg.operacaoretrolaser.player.PlayerClone;
import br.com.uepg.operacaoretrolaser.ui.GameMap;
import br.com.uepg.operacaoretrolaser.player.Player;

import java.awt.*;
import java.awt.geom.AffineTransform;

public class MeleeRobot extends Robot {
    private long tempoUltimoAtaqueVisual = 0;
    private static final long DURACAO_ATAQUE_VISUAL = 200; // Tempo do efeito em milissegundos

    public MeleeRobot(float x, float y, int size, int hp, boolean isSprinter, long cooldown) {
        super(x, y, size, hp, isSprinter, cooldown);
    }

    @Override
    protected boolean podeAcertar(Player p, GameMap map) {
        return Math.hypot(p.getX() - x, p.getY() - y) <= 45 && temVisao(p, map);
    }

    @Override
    protected void executarAtaque(Player p) {
        tempoUltimoAtaqueVisual = System.currentTimeMillis();

        if (isSprinter) {
            // Kamikaze: Causa dano garantido e se destrói no processo
            p.tomarDano(1);
            this.hp = 0;
            return;
        }

        // Robô Padrão: 15% de chance de errar o golpe (0.15)
        double chanceDeEsquiva = attackCooldown >= 1000L ? 0.15 : 0.2;

        if (Math.random() > chanceDeEsquiva) {
            p.tomarDano(1);
        }
    }

    @Override
    protected boolean podeAcertarClone(PlayerClone c, GameMap map) {
        return Math.hypot(c.getX() - x, c.getY() - y) <= 45;
    }

    @Override
    protected void executarAtaqueClone(PlayerClone c) {
        c.receberDano(1);
        tempoUltimoAtaqueVisual = System.currentTimeMillis();

        if (isSprinter) {
            this.hp = 0; // O kamikaze é destruído ao se chocar com o clone
        }
    }

    @Override
    public boolean isMorto() {
        // Se for Kamikaze e atacou, aguarda a animação da explosão terminar para ser removido
        if (isSprinter && tempoUltimoAtaqueVisual > 0) {
            return (System.currentTimeMillis() - tempoUltimoAtaqueVisual) >= DURACAO_ATAQUE_VISUAL;
        }
        return hp <= 0;
    }

    @Override
    public void draw(Graphics2D g2d) {
        AffineTransform oldTransform = g2d.getTransform();
        float centerX = x + width / 2f;
        float centerY = y + height / 2f;

        // Sombra Projetada
        g2d.setColor(new Color(0, 0, 0, 110));
        g2d.fillOval((int)centerX - width/2, (int)centerY, width, height/2 + 4);

        // Gira o desenho na direção em que o robô está mirando
        g2d.rotate(angle, centerX, centerY);

        float legSwing = (float) Math.sin(walkAnim) * 5;

        // Renderização do Corpo do Robô (desenhado apenas se ainda houver vida)
        if (hp > 0) {
            if (isSprinter) {
                // KAMIKAZE
                g2d.setColor(new Color(40, 30, 20));
                g2d.fillRoundRect((int)centerX - 10 + (int)legSwing, (int)centerY - 12, 14, 6, 2, 2);
                g2d.fillRoundRect((int)centerX - 10 - (int)legSwing, (int)centerY + 6, 14, 6, 2, 2);

                g2d.setColor(new Color(220, 100, 20));
                g2d.fillRoundRect((int)centerX - 8, (int)centerY - 8, 16, 16, 4, 4);

                g2d.setColor(Color.YELLOW);
                g2d.fillOval((int)centerX - 2, (int)centerY - 2, 4, 4);
                g2d.fillRect((int)centerX + 4, (int)centerY - 2, 4, 4);
            } else {
                // ROBO BASE
                g2d.setColor(new Color(25, 25, 30));
                g2d.fillRoundRect((int)centerX - 12 + (int)legSwing, (int)centerY - 14, 16, 8, 3, 3);
                g2d.fillRoundRect((int)centerX - 12 - (int)legSwing, (int)centerY + 6, 16, 8, 3, 3);

                g2d.setColor(new Color(50, 55, 65));
                g2d.fillRoundRect((int)centerX - 10, (int)centerY - 10, 20, 20, 4, 4);

                g2d.setColor(new Color(255, 50, 50));
                g2d.fillRect((int)centerX + 4, (int)centerY - 3, 6, 6);
            }
        }

        long tempoDesdeAtaque = System.currentTimeMillis() - tempoUltimoAtaqueVisual;

        if (tempoUltimoAtaqueVisual > 0 && tempoDesdeAtaque <= DURACAO_ATAQUE_VISUAL) {
            float progresso = (float) tempoDesdeAtaque / DURACAO_ATAQUE_VISUAL; // 0.0 a 1.0
            int alpha = (int) (255 * (1.0f - progresso));
            alpha = Math.clamp(alpha, 0, 255);

            if (isSprinter) {
                int raioMax = 50;
                int raioAtual = (int) (raioMax * progresso);

                // Onda de Choque Laranja/Amarela em expansão
                g2d.setColor(new Color(255, 120, 0, alpha));
                g2d.fillOval((int)centerX - raioAtual, (int)centerY - raioAtual, raioAtual * 2, raioAtual * 2);

                // Anel de fogo incandescente
                g2d.setColor(new Color(255, 255, 180, alpha));
                g2d.drawOval((int)centerX - raioAtual, (int)centerY - raioAtual, raioAtual * 2, raioAtual * 2);

                // Flash de luz central
                g2d.setColor(new Color(255, 255, 255, Math.min(255, alpha + 50)));
                g2d.fillOval((int)centerX - raioAtual / 2, (int)centerY - raioAtual / 2, raioAtual, raioAtual);
            } else {
                // Arco de corte vermelho neon projetado à frente do robô
                g2d.setColor(new Color(255, 30, 30, alpha));
                g2d.fillArc((int)centerX - 5, (int)centerY - 25, 40, 50, -50, 100);

                // Traço branco do impacto
                g2d.setColor(new Color(255, 220, 220, alpha));
                g2d.drawArc((int)centerX - 2, (int)centerY - 22, 36, 44, -45, 90);
            }
        }

        g2d.setTransform(oldTransform);
    }
}