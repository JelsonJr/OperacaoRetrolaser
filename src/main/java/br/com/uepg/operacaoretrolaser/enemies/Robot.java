package br.com.uepg.operacaoretrolaser.enemies;

import br.com.uepg.operacaoretrolaser.player.PlayerClone;
import br.com.uepg.operacaoretrolaser.ui.GameMap;
import br.com.uepg.operacaoretrolaser.player.Player;
import java.awt.Graphics2D;
import java.awt.Point;
import java.util.List;

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

    // Variáveis de Interpolação de Movimento para evitar Flick
    protected float currentVelX = 0;
    protected float currentVelY = 0;

    public Robot(float x, float y, int size, int hp, boolean isSprinter, long attackCooldown) {
        this.x = x;
        this.y = y;
        this.width = size;
        this.height = size;
        this.hp = hp;
        this.isSprinter = isSprinter;
        this.attackCooldown = attackCooldown;

        if (isSprinter) {
            this.speed = 5f;
        } else {
            this.speed = 2.3f + (float) (Math.random() * 1.5f);
        }
    }

    public void update(GameMap map, Player player, PlayerClone clone, boolean chanceExtra, FlowField flowField, List<Robot> activeRobots) {
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
            this.angle = angleAway;

        } else if (clone != null && clone.isAlive()) {
            float cCenterX = clone.getX() + 16;
            float cCenterY = clone.getY() + 16;

            if (temVisaoPonto(cCenterX, cCenterY, map)) {
                float angleToClone = (float) Math.atan2(cCenterY - myCenterY, cCenterX - myCenterX);
                boolean isRangedAndClose = this instanceof RangedRobot && Math.hypot(cCenterX - myCenterX, cCenterY - myCenterY) < 350;

                if (!isRangedAndClose) {
                    moverComDeslizamento((float) Math.cos(angleToClone) * speed, (float) Math.sin(angleToClone) * speed, map);
                }

                this.angle = angleToClone;
                tentarAtacarClone(clone, map);
            } else {
                calcularMovimento(map, player, flowField, activeRobots);
            }
        } else {
            calcularMovimento(map, player, flowField, activeRobots);
            float targetCenterX = player.getX() + player.getWidth() / 2f;
            float targetCenterY = player.getY() + player.getHeight() / 2f;
            this.angle = Math.atan2(targetCenterY - myCenterY, targetCenterX - myCenterX);
            tentarAtacar(player, map);
        }

        if (x != oldX || y != oldY) walkAnim += speed * 0.15f;
    }

    protected void calcularMovimento(GameMap map, Player player, FlowField flowField, List<Robot> activeRobots) {
        float desiredX = 0;
        float desiredY = 0;

        float myCenterX = x + width / 2f;
        float myCenterY = y + height / 2f;
        float targetCenterX = player.getX() + player.getWidth() / 2f;
        float targetCenterY = player.getY() + player.getHeight() / 2f;

        if (temVisao(player, map)) {
            float dx = targetCenterX - myCenterX;
            float dy = targetCenterY - myCenterY;
            float dist = (float) Math.hypot(dx, dy);
            if (dist > 0.01f) {
                desiredX = dx / dist;
                desiredY = dy / dist;
            }
        } else {
            int tileSize = GameMap.TILE_SIZE;
            int myCol = (int) myCenterX / tileSize;
            int myRow = (int) myCenterY / tileSize;

            Point vetorFluxo = flowField.getVector(myCol, myRow);
            if (vetorFluxo.x != 0 || vetorFluxo.y != 0) {
                // SOLUÇÃO: Mira no centro do próximo tile, puxando o robô para o meio do caminho
                float targetTileX = (myCol + vetorFluxo.x) * tileSize + (tileSize / 2f);
                float targetTileY = (myRow + vetorFluxo.y) * tileSize + (tileSize / 2f);

                float dirX = targetTileX - myCenterX;
                float dirY = targetTileY - myCenterY;
                float magDir = (float) Math.hypot(dirX, dirY);

                if (magDir > 0.1f) {
                    desiredX = dirX / magDir;
                    desiredY = dirY / magDir;
                }
            }
        }

        // Separação Boids Suave
        float sepX = 0;
        float sepY = 0;

        for (Robot outro : activeRobots) {
            if (outro != this && !outro.isMorto()) {
                float dx = myCenterX - (outro.getX() + outro.getWidth() / 2f);
                float dy = myCenterY - (outro.getY() + outro.getHeight() / 2f);
                float distSq = dx * dx + dy * dy;
                float distMin = (this.width + outro.getWidth()) / 2f + 4f;
                float distMinSq = distMin * distMin;

                if (distSq > 0.1f && distSq < distMinSq) {
                    float dist = (float) Math.sqrt(distSq);
                    // O peso diminui quanto mais longe estão (Linear Falloff)
                    float force = 1.0f - (dist / distMin);
                    sepX += (dx / dist) * force;
                    sepY += (dy / dist) * force;
                }
            }
        }

        float moveX = desiredX + sepX * 1.8f;
        float moveY = desiredY + sepY * 1.8f;

        float mag = (float) Math.hypot(moveX, moveY);
        if (mag > 0.01f) {
            moveX = (moveX / mag) * speed;
            moveY = (moveY / mag) * speed;
        }

        // LERP: Aplica a força na velocidade atual de forma elástica ao invés de instantânea
        currentVelX += (moveX - currentVelX) * 0.2f;
        currentVelY += (moveY - currentVelY) * 0.2f;

        moverComDeslizamento(currentVelX, currentVelY, map);
    }

    protected void moverComDeslizamento(float velX, float velY, GameMap map) {
        // Reduz a hitbox de movimento para perdoar esbarrões leves em quinas
        int hitW = width - 8;
        int hitH = height - 8;
        float offX = (width - hitW) / 2f;
        float offY = (height - hitH) / 2f;

        // Tenta mover no X de forma independente
        if (velX != 0 && map.isFree(x + velX + offX, y + offY, hitW, hitH)) {
            x += velX;
        }

        // Tenta mover no Y de forma independente (o verdadeiro deslizamento ocorre aqui)
        if (velY != 0 && map.isFree(x + offX, y + velY + offY, hitW, hitH)) {
            y += velY;
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
                return false;
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