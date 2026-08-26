package br.com.uepg.operacaoretrolaser.player;

import br.com.uepg.operacaoretrolaser.interactables.PerkType;
import br.com.uepg.operacaoretrolaser.settings.SoundManager;
import br.com.uepg.operacaoretrolaser.ui.GameMap;
import br.com.uepg.operacaoretrolaser.weapons.MeleeWeapon;
import br.com.uepg.operacaoretrolaser.weapons.Weapon;
import br.com.uepg.operacaoretrolaser.weapons.WeaponFactory;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

public class Player {
    private final int width = 32, height = 32;
    private final boolean[] keys = new boolean[256];
    private final List<Weapon> inventarioArmas = new ArrayList<>();
    private final MeleeWeapon ataqueCorpoACorpo;
    private final long INVINCIBILITY_TIME = 900;
    private final List<PerkType> perksAtivos = new ArrayList<>();
    private float baseSpeed = 4f;
    private float x, y;
    private double angle = 0;
    private float sprintSpeed = 6f;
    private float currentSpeed = baseSpeed;
    private float maxStamina = 150f;
    private float stamina = 150f;
    private boolean exhausted = false;
    private int hp = 3;
    private int maxHp = 3;
    private int indiceArmaAtual = 0;
    private boolean realizandoMelee = false;
    private long timerMeleeVisual = 0;
    private long lastDamageTime = 0;
    private long lastRegenTime = 0;
    private float deathAnimProgress = 0.f;
    private boolean meleeHitAplicado = false;
    private float walkAnim = 0;
    private boolean isMoving = false;
    private long lastShotVisualTime = 0;
    private long lastReviveTime = 0;

    public Player(float startX, float startY) {
        this.x = startX;
        this.y = startY;
        this.inventarioArmas.add(WeaponFactory.criarPistola());
        this.ataqueCorpoACorpo = new MeleeWeapon(30, 1.5f, 56f);
    }

    public boolean isRealizandoMelee() {
        return realizandoMelee;
    }

    public boolean isMeleeHitAplicado() {
        return meleeHitAplicado;
    }

    public void setMeleeHitAplicado(boolean valor) {
        this.meleeHitAplicado = valor;
    }

    public MeleeWeapon getAtaqueCorpoACorpo() {
        return ataqueCorpoACorpo;
    }

    public long getLastReviveTime() {
        return lastReviveTime;
    }

    public int getHp() {
        return hp;
    }

    public int getMaxHp() {
        return maxHp;
    }

    public void revive() {
        resetPerks();

        hp = maxHp;
        stamina = maxStamina;
        exhausted = false;
        lastReviveTime = System.currentTimeMillis();
    }

    public Weapon getArmaAtual() {
        return inventarioArmas.get(indiceArmaAtual);
    }

    public boolean hasWeapon(Weapon weapon) {
        return inventarioArmas.contains(weapon);
    }

    public void darArma(Weapon novaArma) {
        int maxArmas = hasPerk(PerkType.PISTOLEIRO) ? 2 : 1;
        if (inventarioArmas.size() < maxArmas) {
            inventarioArmas.add(novaArma);
            indiceArmaAtual = inventarioArmas.size() - 1;
        } else {
            inventarioArmas.set(indiceArmaAtual, novaArma);
        }
    }

    public List<PerkType> getPerksAtivos() {
        return perksAtivos;
    }

    public boolean hasPerk(PerkType perk) {
        return perksAtivos.contains(perk);
    }

    public void addPerk(PerkType perk) {
        perksAtivos.add(perk);
        aplicarEfeitosPerk(perk);
    }

    public void addExtraMeleeDamage(int force) {
        int danoAtual = ataqueCorpoACorpo.getDano();

        // Verifica o dano base real desconsiderando o multiplicador do perk
        int danoBase = hasPerk(PerkType.GOLPE_DURO) ? danoAtual / 4 : danoAtual;

        if (danoBase >= 100) return;

        // Se o perk estiver ativo, o dano adicionado também deve ser multiplicado para manter a proporção
        int forcaReal = hasPerk(PerkType.GOLPE_DURO) ? force * 4 : force;

        this.ataqueCorpoACorpo.setDano(danoAtual + forcaReal);
    }

    public void substituirPerk(int index, PerkType novoPerk) {
        if (index >= 0 && index < perksAtivos.size()) {
            removerEfeitosPerk(perksAtivos.get(index));
            perksAtivos.set(index, novoPerk);
            aplicarEfeitosPerk(novoPerk);
        }
    }

    public void resetPerks() {
        for (PerkType p : perksAtivos) {
            removerEfeitosPerk(p);
        }
        perksAtivos.clear();
    }

    private void aplicarEfeitosPerk(PerkType perk) {
        if (perk == PerkType.TANQUE) {
            maxHp = 6;
            hp = Math.min(hp + 3, maxHp); // Cura instantânea equivalente aos corações novos
        }
        if (perk == PerkType.GOLPE_DURO) {
            ataqueCorpoACorpo.setAlcance(ataqueCorpoACorpo.getAlcance() * 2.5f);
            ataqueCorpoACorpo.setDano(ataqueCorpoACorpo.getDano() * 4);
            ataqueCorpoACorpo.setCadencia(ataqueCorpoACorpo.getCadencia() * 1.75f);
        }
        if (perk == PerkType.PULMAO_ATLETA) {
            maxStamina = 175f;
            sprintSpeed = 6.8f;
            baseSpeed = 4.8f;
        }
    }

    private void removerEfeitosPerk(PerkType perk) {
        if (perk == PerkType.TANQUE) {
            maxHp = 3;
            if (hp > maxHp) hp = maxHp;
        }
        if (perk == PerkType.GOLPE_DURO) {
            ataqueCorpoACorpo.setAlcance(ataqueCorpoACorpo.getAlcance() / 2.5f);
            ataqueCorpoACorpo.setDano(ataqueCorpoACorpo.getDano() / 4);
            ataqueCorpoACorpo.setCadencia(ataqueCorpoACorpo.getCadencia() / 1.75f);
        }
        if (perk == PerkType.PULMAO_ATLETA) {
            maxStamina = 150f;
            sprintSpeed = 6.5f;
            baseSpeed = 4.5f;
            if (stamina > maxStamina) stamina = maxStamina;
        }
        if (perk == PerkType.PISTOLEIRO) {
            if (inventarioArmas.size() > 1) {
                inventarioArmas.remove(1); // Perde a arma secundária
                indiceArmaAtual = 0;
            }
        }
    }

    public void registrarTiroVisual() {
        this.lastShotVisualTime = System.currentTimeMillis();
    }

    public void tomarDano(int dano) {
        if (System.currentTimeMillis() - lastDamageTime > INVINCIBILITY_TIME) {
            this.hp -= dano;
            this.lastDamageTime = System.currentTimeMillis();
            if (this.hp < 0) this.hp = 0;

            SoundManager.playSFX("dano");
        }
    }

    public Point2D.Float getBarrelPosition() {
        float centerX = x + width / 2f;
        float centerY = y + height / 2f;
        float offsetX = 28f;
        float offsetY = 6f;

        float barrelX = centerX + (float) (Math.cos(angle) * offsetX - Math.sin(angle) * offsetY);
        float barrelY = centerY + (float) (Math.sin(angle) * offsetX + Math.cos(angle) * offsetY);

        return new Point2D.Float(barrelX, barrelY);
    }

    public void update(GameMap map, int mouseX, int mouseY, Camera camera) {
        float moveX = 0;
        float moveY = 0;

        if (isPressed(KeyEvent.VK_W, KeyEvent.VK_UP)) moveY -= 1;
        if (isPressed(KeyEvent.VK_S, KeyEvent.VK_DOWN)) moveY += 1;
        if (isPressed(KeyEvent.VK_A, KeyEvent.VK_LEFT)) moveX -= 1;
        if (isPressed(KeyEvent.VK_D, KeyEvent.VK_RIGHT)) moveX += 1;

        isMoving = (moveX != 0 || moveY != 0);

        if (isMoving) {
            walkAnim += currentSpeed * 0.18f;
        } else {
            walkAnim = 0;
        }

        boolean isTryingToSprint = isPressed(KeyEvent.VK_SHIFT, KeyEvent.VK_SHIFT);
        boolean hasPulmaoDeAtleta = hasPerk(PerkType.PULMAO_ATLETA);

        if (isTryingToSprint && isMoving && !exhausted) {
            currentSpeed = sprintSpeed;
            stamina -= hasPulmaoDeAtleta ? 0.5f : 1.0f;
            if (stamina <= 0) {
                stamina = 0;
                exhausted = true;
            }
        } else {
            currentSpeed = baseSpeed;
            if (stamina < maxStamina) {
                stamina += hasPulmaoDeAtleta ? 0.55f : 0.35f;
                if (stamina > maxStamina) stamina = maxStamina;
            }
            float minExausted = hasPulmaoDeAtleta ? 20f : 30f;
            if (exhausted && stamina >= minExausted) exhausted = false;
        }

        if (isMoving) {
            float length = (float) Math.sqrt((moveX * moveX) + (moveY * moveY));
            moveX /= length;
            moveY /= length;
        }

        float nextX = x + (moveX * currentSpeed);
        float nextY = y + (moveY * currentSpeed);

        if (map.isFree(nextX, y, width, height)) x = nextX;
        if (map.isFree(x, nextY, width, height)) y = nextY;

        if (this.hp < this.maxHp) {
            long currentTime = System.currentTimeMillis();
            var hasChanceExtra = hasPerk(PerkType.CHANCE_EXTRA);

            long REGEN_DELAY = 4000;
            long REGEN_TICK = 850;

            long realRengeDelay = hasChanceExtra ? (long) (REGEN_DELAY / 1.85) : REGEN_DELAY;
            long realRengeTick = hasChanceExtra ? (long) (REGEN_TICK / 1.7) : REGEN_TICK;

            if (currentTime - lastDamageTime >= realRengeDelay) {
                if (currentTime - lastRegenTime >= realRengeTick) {
                    this.hp++;
                    this.lastRegenTime = currentTime;
                }
            } else {
                this.lastRegenTime = currentTime;
            }
        }

        float playerScreenX = (x + width / 2f) - camera.getX();
        float playerScreenY = (y + height / 2f) - camera.getY();

        this.angle = Math.atan2(mouseY - playerScreenY, mouseX - playerScreenX);
    }

    private boolean isPressed(int primaryKey, int secondaryKey) {
        return (primaryKey < keys.length && keys[primaryKey]) || (secondaryKey < keys.length && keys[secondaryKey]);
    }

    public void draw(Graphics2D g2d) {
        AffineTransform oldTransform = g2d.getTransform();

        float centerX = x + width / 2f;
        float centerY = y + height / 2f;

        if (hp <= 0) {
            deathAnimProgress += 0.02f;
            g2d.rotate(deathAnimProgress * 5, centerX, centerY);
            float scale = Math.max(0f, 1.0f - (deathAnimProgress * 0.2f));
            g2d.scale(scale, scale);
        }

        g2d.setColor(new Color(0, 0, 0, 110));
        g2d.fillOval((int) centerX - 16, (int) centerY + 6, 32, 16);

        if (hp > 0) {
            g2d.rotate(angle, centerX, centerY);
        }

        boolean invencivel = (System.currentTimeMillis() - lastDamageTime < INVINCIBILITY_TIME);
        boolean recemRevivido = (System.currentTimeMillis() - lastReviveTime < 2000);

        if (invencivel || recemRevivido) {
            if ((System.currentTimeMillis() / 150) % 2 == 0) {
                // Deixa o jogador translúcido quando pisca
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.3f));
            }
        }

        float legSwing = isMoving ? (float) Math.sin(walkAnim) * 7 : 0;

        g2d.setColor(new Color(25, 28, 35));
        g2d.fillRoundRect((int) centerX - 14 + (int) legSwing, (int) centerY - 17, 18, 7, 3, 3);
        g2d.fillRoundRect((int) centerX - 14 - (int) legSwing, (int) centerY + 10, 18, 7, 3, 3);

        g2d.setColor(new Color(70, 75, 85));
        g2d.fillRect((int) centerX - 10 + (int) legSwing, (int) centerY - 16, 4, 5);
        g2d.fillRect((int) centerX - 10 - (int) legSwing, (int) centerY + 11, 4, 5);

        g2d.setColor(new Color(35, 38, 46));
        g2d.fillRoundRect((int) centerX - 16, (int) centerY - 10, 8, 20, 4, 4);
        g2d.setColor(new Color(57, 255, 20, 200));
        g2d.fillRect((int) centerX - 14, (int) centerY - 6, 3, 12);

        g2d.setColor(new Color(45, 52, 65));
        g2d.fillRoundRect((int) centerX - 10, (int) centerY - 12, 20, 24, 6, 6);
        g2d.setColor(new Color(70, 80, 95));
        g2d.fillRoundRect((int) centerX - 6, (int) centerY - 10, 14, 20, 4, 4);

        float pulse = (float) (Math.sin(System.currentTimeMillis() * 0.008) * 0.2 + 0.8);
        g2d.setColor(new Color(0, 220, 255, (int) (230 * pulse)));
        g2d.fillOval((int) centerX - 3, (int) centerY - 3, 6, 6);
        g2d.setColor(Color.WHITE);
        g2d.fillOval((int) centerX - 1, (int) centerY - 1, 2, 2);

        g2d.setColor(new Color(30, 35, 45));
        g2d.fillOval((int) centerX - 4 - (int) legSwing, (int) centerY - 16, 10, 8);
        g2d.setColor(new Color(102, 252, 241));
        g2d.drawOval((int) centerX - 4 - (int) legSwing, (int) centerY - 16, 10, 8);

        long timeSinceShot = System.currentTimeMillis() - lastShotVisualTime;
        float recoilX = 0f;
        boolean showingFlash = false;

        if (timeSinceShot < 120) {
            recoilX = -(1.0f - (timeSinceShot / 120f)) * 7f;
            if (timeSinceShot < 40) showingFlash = true;
        }

        int gunX = (int) (centerX + 4 + recoilX);
        int gunY = (int) (centerY + 7);

        g2d.setColor(new Color(20, 20, 25));
        g2d.fillRect(gunX, gunY, 22, 6);
        g2d.setColor(new Color(60, 65, 75));
        g2d.fillRect(gunX + 4, gunY - 2, 10, 4);

        g2d.setColor(Color.LIGHT_GRAY);
        g2d.fillRect(gunX + 22, gunY + 1, 6, 2);

        if (showingFlash) {
            g2d.setColor(new Color(255, 200, 50, 220));
            g2d.fillOval(gunX + 26, gunY - 4, 12, 14);
            g2d.setColor(Color.WHITE);
            g2d.fillOval(gunX + 28, gunY - 1, 6, 8);
        }

        g2d.setColor(new Color(25, 30, 38));
        g2d.fillRoundRect((int) centerX - 6, (int) centerY - 8, 14, 16, 5, 5);

        g2d.setColor(new Color(255, 0, 85));
        g2d.fillRect((int) centerX + 2, (int) centerY - 5, 5, 10);
        g2d.setColor(Color.WHITE);
        g2d.drawLine((int) centerX + 4, (int) centerY - 4, (int) centerX + 4, (int) centerY + 4);

        g2d.setTransform(oldTransform);
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));

        if (realizandoMelee) {
            if (System.currentTimeMillis() - timerMeleeVisual < 150) {
                boolean temGolpeDuro = hasPerk(PerkType.GOLPE_DURO);

                if (temGolpeDuro) {
                    int raioImpacto = (int) ataqueCorpoACorpo.getAlcance();
                    int offset = raioImpacto / 2;

                    g2d.setStroke(new BasicStroke(4f));
                    g2d.setColor(new Color(255, 69, 0, 180));
                    g2d.drawOval((int) centerX - offset, (int) centerY - offset, raioImpacto, raioImpacto);

                    g2d.setColor(new Color(255, 140, 0, 90));
                    g2d.fillOval((int) centerX - offset + 6, (int) centerY - offset + 6, raioImpacto - 12, raioImpacto - 12);
                    g2d.setStroke(new BasicStroke(1f));
                } else {
                    g2d.setColor(new Color(102, 252, 241, 140));
                    g2d.drawOval((int) centerX - 28, (int) centerY - 28, 56, 56);
                    g2d.setColor(new Color(255, 255, 255, 80));
                    g2d.fillOval((int) centerX - 24, (int) centerY - 24, 48, 48);
                }
            } else {
                realizandoMelee = false;
            }
        }
    }

    public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();
        if (code < keys.length) keys[code] = true;

        if (e.getKeyCode() == KeyEvent.VK_SPACE) {
            if (ataqueCorpoACorpo.podeAtacar()) {
                ataqueCorpoACorpo.registrarAtaque();
                realizandoMelee = true;
                meleeHitAplicado = false;
                timerMeleeVisual = System.currentTimeMillis();
            }
        }

        if (e.getKeyCode() == KeyEvent.VK_1 && !inventarioArmas.isEmpty()) indiceArmaAtual = 0;
        if (e.getKeyCode() == KeyEvent.VK_2 && inventarioArmas.size() > 1) indiceArmaAtual = 1;
    }

    public void keyReleased(KeyEvent e) {
        int code = e.getKeyCode();
        if (code < keys.length) keys[code] = false;
    }

    public void alternarArmaScroll(int direcao) {
        if (inventarioArmas.size() > 1) {
            if (direcao > 0) {
                indiceArmaAtual = (indiceArmaAtual + 1) % inventarioArmas.size();
            } else if (direcao < 0) {
                indiceArmaAtual = (indiceArmaAtual - 1 + inventarioArmas.size()) % inventarioArmas.size();
            }
        }
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public float getStamina() {
        return stamina;
    }

    public float getMaxStamina() {
        return maxStamina;
    }

    public boolean isExhausted() {
        return exhausted;
    }
}