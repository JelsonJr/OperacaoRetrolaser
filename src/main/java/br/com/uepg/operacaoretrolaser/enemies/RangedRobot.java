package br.com.uepg.operacaoretrolaser.enemies;

import br.com.uepg.operacaoretrolaser.player.PlayerClone;
import br.com.uepg.operacaoretrolaser.ui.GameMap;
import br.com.uepg.operacaoretrolaser.player.Player;
import br.com.uepg.operacaoretrolaser.settings.SoundManager;

import java.awt.*;
import java.awt.geom.AffineTransform;

public class RangedRobot extends Robot {
    private final EnemyManager manager;

    public RangedRobot(float x, float y, int size, int hp, boolean isSprinter, long cooldown, EnemyManager manager) {
        super(x, y, size, hp, isSprinter, cooldown);
        this.manager = manager;
    }

    @Override
    protected void calcularMovimento(GameMap map, Player player, FlowField flowField) {
        float dist = (float) Math.hypot(player.getX() - x, player.getY() - y);
        float angle = (float) Math.atan2(player.getY() - y, player.getX() - x);

        boolean visaoLimpa = temVisao(player, map);
        float moveX = 0, moveY = 0;

        if (visaoLimpa) {
            // Mantém distância ideal entre 200 e 350
            if (dist > 350) {
                moveX = (float) Math.cos(angle) * speed;
                moveY = (float) Math.sin(angle) * speed;
            } else if (dist < 200) {
                moveX = (float) Math.cos(angle + Math.PI) * speed;
                moveY = (float) Math.sin(angle + Math.PI) * speed;
            }
        } else {
            // Se perdeu a visão, usa o FlowField da classe pai para caçar o player
            super.calcularMovimento(map, player, flowField);
            return;
        }

        if (moveX != 0 || moveY != 0) {
            if (map.isFree(x + moveX, y, width, height)) x += moveX;
            if (map.isFree(x, y + moveY, width, height)) y += moveY;
        }
    }

    @Override
    protected boolean podeAcertar(Player p, GameMap map) {
        return Math.hypot(p.getX() - x, p.getY() - y) <= 450 && temVisao(p, map);
    }

    @Override
    protected void executarAtaque(Player p) {
        // Calcula o centro do robô e o centro do jogador para o tiro sair perfeitamente alinhado
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

        // Sombra
        g2d.setColor(new Color(0, 0, 0, 110));
        g2d.fillOval((int)centerX - width/2, (int)centerY, width, height/2 + 4);

        g2d.rotate(angle, centerX, centerY);
        float legSwing = (float) Math.sin(walkAnim) * 5;

        // Pernas Articuladas
        g2d.setColor(new Color(25, 20, 35));
        g2d.fillRoundRect((int)centerX - 12 + (int)legSwing, (int)centerY - 14, 16, 8, 3, 3);
        g2d.fillRoundRect((int)centerX - 12 - (int)legSwing, (int)centerY + 6, 16, 8, 3, 3);

        // Chassi Principal Roxo/Magenta
        g2d.setColor(new Color(80, 40, 95));
        g2d.fillRoundRect((int)centerX - 10, (int)centerY - 12, 20, 24, 4, 4);

        // Canhão/Sniper (Braço Direito)
        g2d.setColor(new Color(30, 25, 40));
        g2d.fillRect((int)centerX, (int)centerY + 6, 24, 6);
        g2d.setColor(new Color(255, 0, 255)); // Detalhe Neon
        g2d.drawRect((int)centerX, (int)centerY + 6, 24, 6);

        // Visor Magenta
        g2d.setColor(Color.MAGENTA);
        g2d.fillRect((int)centerX + 4, (int)centerY - 6, 6, 6);

        g2d.setTransform(oldTransform);
    }

    @Override
    protected boolean podeAcertarClone(PlayerClone c, GameMap map) {
        // Dispara se estiver na distância de tiro sem checar visibilidade refinada para o clone
        return Math.hypot(c.getX() - x, c.getY() - y) <= 450;
    }

    @Override
    protected void executarAtaqueClone(PlayerClone c) {
        float startX = this.x + width / 2f;
        float startY = this.y + height / 2f;
        float targetX = c.getX() + 16;
        float targetY = c.getY() + 16;

        // Cria o laser apontando para o clone
        EnemyLaser laser = new EnemyLaser(startX, startY, targetX, targetY);
        manager.addLaser(laser);
    }
}