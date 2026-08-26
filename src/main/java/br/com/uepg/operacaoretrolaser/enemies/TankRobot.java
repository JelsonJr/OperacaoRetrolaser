package br.com.uepg.operacaoretrolaser.enemies;

import br.com.uepg.operacaoretrolaser.player.Player;
import br.com.uepg.operacaoretrolaser.player.PlayerClone;
import br.com.uepg.operacaoretrolaser.ui.GameMap;

import java.awt.*;
import java.awt.geom.AffineTransform;

public class TankRobot extends MeleeRobot {

    public TankRobot(float x, float y, int hp, EnemyManager manager) {
        super(x, y, 48, hp, false, 1500L);
        this.speed = 1.5f;
    }

    @Override
    protected boolean podeAcertar(Player p, GameMap map) {
        return Math.hypot(p.getX() - x, p.getY() - y) <= 60 && temVisao(p, map);
    }

    @Override
    protected boolean podeAcertarClone(PlayerClone c, GameMap map) {
        return Math.hypot(c.getX() - x, c.getY() - y) <= 60;
    }

    @Override
    public void draw(Graphics2D g2d) {
        AffineTransform oldTransform = g2d.getTransform();
        float centerX = x + width / 2f;
        float centerY = y + height / 2f;

        // Sombra Projetada maior
        g2d.setColor(new Color(0, 0, 0, 110));
        g2d.fillOval((int)centerX - width/2, (int)centerY, width, height/2 + 6);

        // Gira o desenho na direção em que o robô está mirando
        g2d.rotate(angle, centerX, centerY);

        float legSwing = (float) Math.sin(walkAnim) * 5;

        // Renderização do Corpo do Tanque (desenhado apenas se ainda houver vida)
        if (hp > 0) {
            // Pernas mais grossas e industriais
            g2d.setColor(new Color(20, 20, 22));
            g2d.fillRoundRect((int)centerX - 16 + (int)legSwing, (int)centerY - 18, 20, 12, 4, 4);
            g2d.fillRoundRect((int)centerX - 16 - (int)legSwing, (int)centerY + 8, 20, 12, 4, 4);

            // Chassi blindado (Maior e mais escuro)
            g2d.setColor(new Color(35, 40, 45));
            g2d.fillRoundRect((int)centerX - 14, (int)centerY - 16, 28, 32, 6, 6);

            // Placas de armadura reforçada no topo
            g2d.setColor(new Color(75, 80, 90));
            g2d.fillRoundRect((int)centerX - 8, (int)centerY - 10, 16, 20, 2, 2);

            // Olho vermelho mais intimidador
            g2d.setColor(new Color(255, 30, 30));
            g2d.fillRect((int)centerX + 8, (int)centerY - 4, 6, 8);
        }

        // Animação de Ataque usando a variável lastAttackTime originada na superclasse Robot
        long tempoDesdeAtaque = System.currentTimeMillis() - lastAttackTime;

        if (lastAttackTime > 0 && tempoDesdeAtaque <= 200) {
            float progresso = (float) tempoDesdeAtaque / 200f; // 0.0 a 1.0
            int alpha = (int) (255 * (1.0f - progresso));
            alpha = Math.clamp(alpha, 0, 255);

            // Arco de corte laranja escuro, maior para combinar com o brutamontes
            g2d.setColor(new Color(255, 100, 30, alpha));
            g2d.fillArc((int)centerX - 10, (int)centerY - 35, 60, 70, -50, 100);

            // Traço do impacto
            g2d.setColor(new Color(255, 200, 150, alpha));
            g2d.drawArc((int)centerX - 6, (int)centerY - 32, 52, 64, -45, 90);
        }

        g2d.setTransform(oldTransform);
    }
}