package br.com.uepg.operacaoretrolaser.enemies;

import br.com.uepg.operacaoretrolaser.player.PlayerClone;
import br.com.uepg.operacaoretrolaser.ui.GameMap;
import br.com.uepg.operacaoretrolaser.player.Player;
import br.com.uepg.operacaoretrolaser.settings.SoundManager;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.util.List;

public class RangedRobot extends Robot {
    private final EnemyManager manager;

    public RangedRobot(float x, float y, int size, int hp, boolean isSprinter, long cooldown, EnemyManager manager) {
        super(x, y, size, hp, isSprinter, cooldown);
        this.manager = manager;
    }

    @Override
    protected void calcularMovimento(GameMap map, Player player, FlowField flowField, List<Robot> activeRobots) {
        float myCenterX = x + width / 2f;
        float myCenterY = y + height / 2f;
        float pCenterX = player.getX() + player.getWidth() / 2f;
        float pCenterY = player.getY() + player.getHeight() / 2f;

        float distToPlayer = (float) Math.hypot(pCenterX - myCenterX, pCenterY - myCenterY);
        float angleToPlayer = (float) Math.atan2(pCenterY - myCenterY, pCenterX - myCenterX);

        boolean visaoLimpa = temVisao(player, map);
        float desiredX = 0, desiredY = 0;

        if (visaoLimpa) {
            if (distToPlayer > 350) {
                desiredX = (float) Math.cos(angleToPlayer);
                desiredY = (float) Math.sin(angleToPlayer);
            } else if (distToPlayer < 200) {
                desiredX = -(float) Math.cos(angleToPlayer); // Se afasta na direcao oposta
                desiredY = -(float) Math.sin(angleToPlayer);
            }
        } else {
            super.calcularMovimento(map, player, flowField, activeRobots);
            return;
        }

        // Separação orgânica também para os atiradores
        float sepX = 0, sepY = 0;
        for (Robot outro : activeRobots) {
            if (outro != this && !outro.isMorto()) {
                float dx = myCenterX - (outro.getX() + outro.getWidth() / 2f);
                float dy = myCenterY - (outro.getY() + outro.getHeight() / 2f);
                float distSq = dx * dx + dy * dy;
                float distMin = (this.width + outro.getWidth()) / 2f + 15f;
                float distMinSq = distMin * distMin;

                if (distSq > 0.1f && distSq < distMinSq) {
                    float distReal = (float) Math.sqrt(distSq);
                    float force = 1.0f - (distReal / distMin);
                    sepX += (dx / distReal) * force;
                    sepY += (dy / distReal) * force;
                }
            }
        }

        float moveX = desiredX + sepX * 2.0f;
        float moveY = desiredY + sepY * 2.0f;

        float mag = (float) Math.hypot(moveX, moveY);
        if (mag > 0.01f) {
            moveX = (moveX / mag) * speed;
            moveY = (moveY / mag) * speed;
        }

        // Atiradores são um pouco mais lentos pra mudar de direção (0.15f ao invés de 0.2f)
        currentVelX += (moveX - currentVelX) * 0.15f;
        currentVelY += (moveY - currentVelY) * 0.15f;

        moverComDeslizamento(currentVelX, currentVelY, map);
    }

    @Override
    protected boolean podeAcertar(Player p, GameMap map) {
        return Math.hypot(p.getX() - x, p.getY() - y) <= 450 && temVisao(p, map);
    }

    @Override
    protected void executarAtaque(Player p) {
        float startX = this.x + width / 2f;
        float startY = this.y + height / 2f;
        float targetX = p.getX() + p.getWidth() / 2f;
        float targetY = p.getY() + p.getHeight() / 2f;

        EnemyLaser laser = new EnemyLaser(startX, startY, targetX, targetY);
        manager.addLaser(laser);
        SoundManager.playSFX("tiro-inimigo");
    }

    @Override
    public void draw(Graphics2D g2d) {
        AffineTransform oldTransform = g2d.getTransform();
        float centerX = x + width / 2f;
        float centerY = y + height / 2f;

        g2d.setColor(new Color(0, 0, 0, 110));
        g2d.fillOval((int)centerX - width/2, (int)centerY, width, height/2 + 4);

        g2d.rotate(angle, centerX, centerY);
        float legSwing = (float) Math.sin(walkAnim) * 5;

        g2d.setColor(new Color(25, 20, 35));
        g2d.fillRoundRect((int)centerX - 12 + (int)legSwing, (int)centerY - 14, 16, 8, 3, 3);
        g2d.fillRoundRect((int)centerX - 12 - (int)legSwing, (int)centerY + 6, 16, 8, 3, 3);

        g2d.setColor(new Color(80, 40, 95));
        g2d.fillRoundRect((int)centerX - 10, (int)centerY - 12, 20, 24, 4, 4);

        g2d.setColor(new Color(30, 25, 40));
        g2d.fillRect((int)centerX, (int)centerY + 6, 24, 6);
        g2d.setColor(new Color(255, 0, 255));
        g2d.drawRect((int)centerX, (int)centerY + 6, 24, 6);

        g2d.setColor(Color.MAGENTA);
        g2d.fillRect((int)centerX + 4, (int)centerY - 6, 6, 6);

        g2d.setTransform(oldTransform);
    }

    @Override
    protected boolean podeAcertarClone(PlayerClone c, GameMap map) {
        return Math.hypot(c.getX() - x, c.getY() - y) <= 450;
    }

    @Override
    protected void executarAtaqueClone(PlayerClone c) {
        float startX = this.x + width / 2f;
        float startY = this.y + height / 2f;
        float targetX = c.getX() + 16;
        float targetY = c.getY() + 16;

        EnemyLaser laser = new EnemyLaser(startX, startY, targetX, targetY);
        manager.addLaser(laser);
    }
}