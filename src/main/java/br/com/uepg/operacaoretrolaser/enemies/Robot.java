package br.com.uepg.operacaoretrolaser.enemies;

import br.com.uepg.operacaoretrolaser.player.PlayerClone;
import br.com.uepg.operacaoretrolaser.ui.GameMap;
import br.com.uepg.operacaoretrolaser.player.Player;
import java.awt.Graphics2D;
import java.awt.Point;

public abstract class Robot {
    protected float x, y;
    protected int width, height;
    protected int hp;
    protected float speed;
    public boolean isSprinter;
    protected double angle = 0;
    protected float walkAnim = 0;
    protected long attackCooldown;
    protected long lastAttackTime = 0;

    public Robot(float x, float y, int size, int hp, boolean isSprinter, long attackCooldown) {
        this.x = x;
        this.y = y;
        this.width = size;
        this.height = size;
        this.hp = hp;
        this.isSprinter = isSprinter;
        this.attackCooldown = attackCooldown;

        if (isSprinter) {
            this.speed = 5.2f + (float) (Math.random() * 0.5f);
        } else {
            this.speed = 3.0f + (float) (Math.random() * 0.5f);
        }
    }

    public void update(GameMap map, Player player, PlayerClone clone, boolean chanceExtra, FlowField flowField) {
        if (isMorto()) return;

        float oldX = x;
        float oldY = y;
        float myCenterX = x + width / 2f;
        float myCenterY = y + height / 2f;

        if (chanceExtra) {
            float pCenterX = player.getX() + player.getWidth() / 2f;
            float pCenterY = player.getY() + player.getHeight() / 2f;
            float angleAway = (float) Math.atan2(myCenterY - pCenterY, myCenterX - pCenterX);

            moverComDeslizamento((float) Math.cos(angleAway) * speed, (float) Math.sin(angleAway) * speed, map);
            this.angle = angleAway; // Olha para onde está correndo

        } else if (clone != null && clone.isAlive()) {
            float cCenterX = clone.getX() + 16;
            float cCenterY = clone.getY() + 16;

            // Só corre para o clone se não houver paredes no caminho
            if (temVisaoPonto(cCenterX, cCenterY, map)) {
                float angleToClone = (float) Math.atan2(cCenterY - myCenterY, cCenterX - myCenterX);
                boolean isRangedAndClose = this instanceof RangedRobot && Math.hypot(cCenterX - myCenterX, cCenterY - myCenterY) < 350;

                // Se não for um Ranged perto do clone, ele pode continuar andando
                if (!isRangedAndClose) {
                    moverComDeslizamento((float) Math.cos(angleToClone) * speed, (float) Math.sin(angleToClone) * speed, map);
                }

                this.angle = angleToClone;
                tentarAtacarClone(clone, map);
            } else {
                // Se o clone estiver atrás da parede, o robô navega pelo mapa normalmente para não travar
                calcularMovimento(map, player, flowField);
            }
        } else {
            calcularMovimento(map, player, flowField);
            float targetCenterX = player.getX() + player.getWidth() / 2f;
            float targetCenterY = player.getY() + player.getHeight() / 2f;
            this.angle = Math.atan2(targetCenterY - myCenterY, targetCenterX - myCenterX);
            tentarAtacar(player, map);
        }

        // Atualiza a animação de caminhada se ele saiu do lugar
        if (x != oldX || y != oldY) walkAnim += speed * 0.15f;
    }

    protected void calcularMovimento(GameMap map, Player player, FlowField flowField) {
        if (temVisao(player, map)) {
            float targetCenterX = player.getX() + player.getWidth() / 2f;
            float targetCenterY = player.getY() + player.getHeight() / 2f;
            float myCenterX = x + width / 2f;
            float myCenterY = y + height / 2f;

            float angleToPlayer = (float) Math.atan2(targetCenterY - myCenterY, targetCenterX - myCenterX);
            float moveX = (float) Math.cos(angleToPlayer) * speed;
            float moveY = (float) Math.sin(angleToPlayer) * speed;

            moverComDeslizamento(moveX, moveY, map);
            return;
        }

        int tileSize = 32;
        int myCol = (int) (this.x + width / 2f) / tileSize;
        int myRow = (int) (this.y + height / 2f) / tileSize;

        Point vetorFluxo = flowField.getVector(myCol, myRow);

        if (vetorFluxo.x != 0 || vetorFluxo.y != 0) {
            float targetX = (myCol + vetorFluxo.x) * tileSize + (tileSize - width) / 2f;
            float targetY = (myRow + vetorFluxo.y) * tileSize + (tileSize - height) / 2f;
            float angleToTarget = (float) Math.atan2(targetY - y, targetX - x);

            float moveX = (float) Math.cos(angleToTarget) * speed;
            float moveY = (float) Math.sin(angleToTarget) * speed;

            moverComDeslizamento(moveX, moveY, map);
        }
    }

    protected void moverComDeslizamento(float moveX, float moveY, GameMap map) {
        boolean movedX = false;
        boolean movedY = false;

        if (moveX != 0 && map.isFree(x + moveX, y, width, height)) {
            x += moveX;
            movedX = true;
        }
        if (moveY != 0 && map.isFree(x, y + moveY, width, height)) {
            y += moveY;
            movedY = true;
        }
        if (!movedX && movedY) {
            float extraY = Math.signum(moveY) * (speed - Math.abs(moveY));
            if (map.isFree(x, y + extraY, width, height)) y += extraY;
        } else if (movedX && !movedY) {
            float extraX = Math.signum(moveX) * (speed - Math.abs(moveX));
            if (map.isFree(x + extraX, y, width, height)) x += extraX;
        }
    }

    protected boolean temVisao(Player player, GameMap map) {
        float x1 = this.x + width / 2f;
        float y1 = this.y + height / 2f;
        float x2 = player.getX() + player.getWidth() / 2f;
        float y2 = player.getY() + player.getHeight() / 2f;

        float dist = (float) Math.hypot(x2 - x1, y2 - y1);
        int steps = (int) (dist / 8);

        int testW = this.width;
        int testH = this.height;

        for (int i = 0; i <= steps; i++) {
            float checkX = x1 + (x2 - x1) * (float) i / steps;
            float checkY = y1 + (y2 - y1) * (float) i / steps;

            if (!map.isFree(checkX - testW / 2f, checkY - testH / 2f, testW, testH)) {
                return false;
            }
        }
        return true;
    }

    protected void tentarAtacar(Player player, GameMap map) {
        if (System.currentTimeMillis() - lastAttackTime >= attackCooldown) {
            if (podeAcertar(player, map)) {
                executarAtaque(player);
                lastAttackTime = System.currentTimeMillis();
            }
        }
    }

    protected void tentarAtacarClone(PlayerClone clone, GameMap map) {
        if (System.currentTimeMillis() - lastAttackTime >= attackCooldown) {
            if (podeAcertarClone(clone, map)) {
                executarAtaqueClone(clone);
                lastAttackTime = System.currentTimeMillis();
            }
        }
    }

    protected boolean temVisaoPonto(float targetX, float targetY, GameMap map) {
        float x1 = this.x + width / 2f;
        float y1 = this.y + height / 2f;
        float dist = (float) Math.hypot(targetX - x1, targetY - y1);
        int steps = (int) (dist / 8);

        for (int i = 0; i <= steps; i++) {
            float checkX = x1 + (targetX - x1) * (float) i / steps;
            float checkY = y1 + (targetY - y1) * (float) i / steps;
            if (!map.isFree(checkX - width / 2f, checkY - height / 2f, width, height)) {
                return false; // Bateu numa parede
            }
        }
        return true;
    }

    protected abstract boolean podeAcertar(Player p, GameMap map);
    protected abstract void executarAtaque(Player p);

    protected abstract boolean podeAcertarClone(PlayerClone c, GameMap map);
    protected abstract void executarAtaqueClone(PlayerClone c);

    public abstract void draw(Graphics2D g2d);
    public void tomarDano(int dano) { this.hp -= dano; }
    public boolean isMorto() { return hp <= 0; }
    public float getX() { return x; }
    public float getY() { return y; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
}