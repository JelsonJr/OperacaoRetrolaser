package br.com.uepg.operacaoretrolaser.states;

import br.com.uepg.operacaoretrolaser.GamePanel;
import br.com.uepg.operacaoretrolaser.enemies.EnemyManager;
import br.com.uepg.operacaoretrolaser.enemies.FlowField;
import br.com.uepg.operacaoretrolaser.enemies.RangedRobot;
import br.com.uepg.operacaoretrolaser.enemies.Robot;
import br.com.uepg.operacaoretrolaser.interactables.Interactable;
import br.com.uepg.operacaoretrolaser.interactables.PerkMachine;
import br.com.uepg.operacaoretrolaser.interactables.PerkType;
import br.com.uepg.operacaoretrolaser.player.Camera;
import br.com.uepg.operacaoretrolaser.player.Player;
import br.com.uepg.operacaoretrolaser.player.PlayerClone;
import br.com.uepg.operacaoretrolaser.settings.SoundManager;
import br.com.uepg.operacaoretrolaser.ui.FloatingText;
import br.com.uepg.operacaoretrolaser.ui.GameMap;
import br.com.uepg.operacaoretrolaser.weapons.Projectile;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Area;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class PlayState implements GameState {

    private final GamePanel game;
    private final GameMap map;
    private final Player player;
    private final Camera camera;
    private final EnemyManager enemyManager = new EnemyManager();
    private final List<Projectile> projeteis = new CopyOnWriteArrayList<>();
    private final List<FloatingText> floatingTexts = new CopyOnWriteArrayList<>();
    private final FlowField flowField;
    private final long startTime;
    private int pontuacao = 0;
    private int round = 1;
    private int dinheiro = 800;
    private boolean isAtirando = false;
    private boolean isAtirandoSecundario = false;
    private boolean intervaloRound = false;
    private int ticksIntervalo = 0;
    private PlayerClone cloneAtivo = null;
    private boolean cloneAtivoNesseRound = false;
    private long chanceExtraFimMs = 0;
    private int pontosTotais = 500;
    private int pontosGastos = 0;
    private int portasAbertas = 0;
    private boolean morrendo = false;
    private int ticksMorte = 0;
    private boolean trocandoPerk = false;
    private PerkType perkPendente = null;

    public PlayState(GamePanel game) {
        this.game = game;
        this.startTime = System.currentTimeMillis();
        this.map = new GameMap("/files/map.txt");

        float spawnX = map.playerSpawn.x;
        float spawnY = map.playerSpawn.y;

        this.player = new Player(spawnX, spawnY);
        this.camera = new Camera(spawnX, spawnY);

        flowField = new FlowField(GameMap.COLS * GameMap.TILE_SIZE, GameMap.ROWS * GameMap.TILE_SIZE);
        enemyManager.iniciarRound(round);
    }

    public Player getPlayer() { return player; }
    public long getStartTime() { return startTime; }
    public int getDinheiro() { return dinheiro; }

    public void gastarDinheiro(int valor) {
        if (this.dinheiro >= valor) {
            this.dinheiro -= valor;
            this.pontosGastos += valor;
        }
    }

    public void ativarChanceExtra() {
        this.chanceExtraFimMs = System.currentTimeMillis() + 10000;
    }

    public boolean isChanceExtraAtiva() {
        return System.currentTimeMillis() < chanceExtraFimMs;
    }

    public void tentarAtivarReplicante() {
        if (player.hasPerk(PerkType.REPLICANTE) && !cloneAtivoNesseRound && (cloneAtivo == null || !cloneAtivo.isAlive())) {
            cloneAtivo = new PlayerClone(player.getX(), player.getY());
            cloneAtivoNesseRound = true;
        }
    }

    public GameMap getMap() { return this.map; }

    public void iniciarTrocaPerk(PerkType novoPerk) {
        this.perkPendente = novoPerk;
        this.trocandoPerk = true;
        this.isAtirando = false;
    }

    @Override
    public void update() {
        if (cloneAtivo != null && !cloneAtivo.isAlive()) {
            cloneAtivo = null;
        }

        if (trocandoPerk) return;

        // --- CHECAGEM DE GAME OVER E ANIMAÇÃO DE MORTE ---
        if (player.getHp() <= 0) {
            if (player.hasPerk(PerkType.CHANCE_EXTRA)) {
                ativarChanceExtra();
                player.revive();
                floatingTexts.add(new FloatingText(player.getX() - 40, player.getY() - 30, "CHANCE EXTRA ATIVADA!", Color.CYAN, 500, 0.7f, false));
            } else {
                if (!morrendo) {
                    morrendo = true;
                    ticksMorte = 0;
                    SoundManager.playSFX("game-over");
                    SoundManager.stopMusic();
                }

                ticksMorte++;

                if (ticksMorte >= 320) {
                    long tempoPartida = System.currentTimeMillis() - startTime;
                    game.setState(new GameOverState(game, pontuacao, round, tempoPartida, pontosTotais, pontosGastos, portasAbertas));
                }
                return;
            }
        }

        if (enemyManager.isRoundConcluido() && !intervaloRound) {
            intervaloRound = true;
            ticksIntervalo = 0;
        }

        if (intervaloRound) {
            ticksIntervalo++;
            if (ticksIntervalo >= 900) {
                this.round++;
                enemyManager.iniciarRound(this.round);
                player.addExtraMeleeDamage(3);
                intervaloRound = false;
                cloneAtivoNesseRound = false;
            }
        }

        player.update(map, game.getMouseX(), game.getMouseY(), camera);
        camera.tick(player);
        flowField.updateField(map, player);
        enemyManager.update(map, player, flowField, cloneAtivo, isChanceExtraAtiva());

        if (player.isRealizandoMelee() && !player.isMeleeHitAplicado()) {
            SoundManager.playSFX("soco");

            for (Robot robo : enemyManager.getActiveRobots()) {
                float dist = (float) Math.hypot((robo.getX() + robo.getWidth() / 2f) - (player.getX() + player.getWidth() / 2f), (robo.getY() + robo.getHeight() / 2f) - (player.getY() + player.getHeight() / 2f));

                if (dist <= player.getAtaqueCorpoACorpo().getAlcance() / 1.5f) {
                    int dano = player.getAtaqueCorpoACorpo().getDano();
                    robo.tomarDano(dano);

                    dinheiro += 15;
                    pontosTotais += 15;

                    if (robo.isMorto()) {
                        int ganho = robo.isSprinter ? 180 : robo instanceof RangedRobot ? 150 : 110;
                        ganharDinheiro(robo, dano, ganho);
                    } else {
                        floatingTexts.add(new FloatingText(robo.getX(), robo.getY(), "-" + dano, Color.WHITE, 30, 1.0, false));
                    }
                }
            }
            player.setMeleeHitAplicado(true);
        }

        Point2D.Float barrelPos = player.getBarrelPosition();
        float targetX = game.getMouseX() + camera.getX();
        float targetY = game.getMouseY() + camera.getY();

        if (isAtirando) {
            List<Projectile> novosTiros = player.getArmaAtual().atirarPrincipal(barrelPos.x, barrelPos.y, targetX, targetY);
            if (!novosTiros.isEmpty()) {
                projeteis.addAll(novosTiros);
                player.registrarTiroVisual();
                SoundManager.playSFX("tiro");
            }
        }

        if (isAtirandoSecundario) {
            List<Projectile> novosTirosEspeciais = player.getArmaAtual().atirarSecundario(barrelPos.x, barrelPos.y, targetX, targetY);
            if (!novosTirosEspeciais.isEmpty()) {
                projeteis.addAll(novosTirosEspeciais);
                player.registrarTiroVisual();
                SoundManager.playSFX("tiro");
            }
        }

        for (int i = 0; i < projeteis.size(); i++) {
            Projectile p = projeteis.get(i);
            p.update(map);

            for (Robot robo : enemyManager.getActiveRobots()) {
                if (p.isActive() && p.getX() >= robo.getX() && p.getX() <= robo.getX() + robo.getWidth() && p.getY() >= robo.getY() && p.getY() <= robo.getY() + robo.getHeight()) {

                    int dano = p.getDano();

                    if (player.hasPerk(PerkType.TIRO_DUPLO)) {
                        dano *= 2;
                    }

                    robo.tomarDano(dano);
                    p.desativar();

                    dinheiro += 10;
                    pontosTotais += 10;

                    if (robo.isMorto()) {
                        int ganho = robo.isSprinter ? 150 : robo instanceof RangedRobot ? 120 : 100;
                        ganharDinheiro(robo, dano, ganho);
                    } else {
                        floatingTexts.add(new FloatingText(robo.getX(), robo.getY(), "-" + dano, Color.WHITE, 30, 1.0, false));
                    }
                }
            }

            if (!p.isActive()) {
                projeteis.remove(i);
                i--;
            }
        }
        floatingTexts.removeIf(FloatingText::update);
    }

    private void ganharDinheiro(Robot robo, int dano, int ganho) {
        dinheiro += ganho;
        pontosTotais += ganho;
        pontuacao++;
        floatingTexts.add(new FloatingText(robo.getX(), robo.getY(), "-" + dano + " (KILL)", Color.RED, 50, 1.5, false));
        floatingTexts.add(new FloatingText(GamePanel.WIDTH - 100, GamePanel.HEIGHT - 40, "+" + ganho, Color.GREEN, 50, 2.0, true));
    }

    @Override
    public void draw(Graphics2D g2d) {
        int camX = Math.round(camera.getX());
        int camY = Math.round(camera.getY());

        g2d.translate(-camX, -camY);

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        map.render(g2d, camera);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        enemyManager.draw(g2d);
        player.draw(g2d);

        Point2D.Float barrelPos = player.getBarrelPosition();
        float targetX = game.getMouseX() + camera.getX();
        float targetY = game.getMouseY() + camera.getY();

        g2d.setColor(new Color(255, 0, 85, 120));
        g2d.drawLine((int) barrelPos.x, (int) barrelPos.y, (int) targetX, (int) targetY);

        for (Projectile p : projeteis) {
            p.draw(g2d);
        }

        for (FloatingText ft : floatingTexts) {
            if (!ft.isUI()) ft.draw(g2d);
        }

        if (cloneAtivo != null && cloneAtivo.isAlive()) {
            cloneAtivo.draw(g2d);
        }

        if (!player.hasPerk(PerkType.VISAO_AGUIA)) {
            drawFogOfWar(g2d);
        }

        drawInteractablePrompts(g2d);

        g2d.translate(camX, camY);

        drawHUD(g2d);

        for (FloatingText ft : floatingTexts) {
            if (ft.isUI()) ft.draw(g2d);
        }

        if (intervaloRound) {
            drawRoundInterval(g2d);
        }

        if (morrendo) {
            drawDeathScreen(g2d);
        }

        drawCustomCursor(g2d);

        if (trocandoPerk && perkPendente != null) {
            drawPerkExchangeScreen(g2d);
        }
    }

    private void drawFogOfWar(Graphics2D g2d) {
        float cx = player.getX() + player.getWidth() / 2f;
        float cy = player.getY() + player.getHeight() / 2f;
        float raioVisao = 360f;
        int tileSize = GameMap.TILE_SIZE;

        var luzPath = new Path2D.Float();
        boolean primeiroPonto = true;

        int cols = GameMap.COLS;
        int rows = GameMap.ROWS;

        boolean[][] blocosVistos = new boolean[rows][cols];
        boolean[][] chaoVisto = new boolean[rows][cols];

        for (int a = 0; a < 360; a++) {
            double rad = Math.toRadians(a);
            float dirX = (float) Math.cos(rad);
            float dirY = (float) Math.sin(rad);

            float rayX = cx;
            float rayY = cy;
            float dist = 0;
            float stepSize = 3f;

            while (dist < raioVisao) {
                float nextX = rayX + dirX * stepSize;
                float nextY = rayY + dirY * stepSize;

                int tileX = (int) (nextX / tileSize);
                int tileY = (int) (nextY / tileSize);

                if (tileX >= 0 && tileX < cols && tileY >= 0 && tileY < rows) {
                    if (!map.isFree(nextX, nextY, 1, 1)) {
                        blocosVistos[tileY][tileX] = true;
                        rayX = nextX + dirX * 6f;
                        rayY = nextY + dirY * 6f;
                        break;
                    } else {
                        chaoVisto[tileY][tileX] = true;
                    }
                }
                rayX = nextX;
                rayY = nextY;
                dist += stepSize;
            }

            if (primeiroPonto) {
                luzPath.moveTo(rayX, rayY);
                primeiroPonto = false;
            } else {
                luzPath.lineTo(rayX, rayY);
            }
        }
        luzPath.closePath();

        var areaLuz = new Area(luzPath);

        int minTileX = Math.max(0, (int) ((cx - raioVisao) / tileSize));
        int maxTileX = Math.min(cols - 1, (int) ((cx + raioVisao) / tileSize));
        int minTileY = Math.max(0, (int) ((cy - raioVisao) / tileSize));
        int maxTileY = Math.min(rows - 1, (int) ((cy + raioVisao) / tileSize));

        boolean[][] blocosFinais = new boolean[rows][cols];

        for (int y = minTileY; y <= maxTileY; y++) {
            for (int x = minTileX; x <= maxTileX; x++) {
                if (blocosVistos[y][x]) {
                    blocosFinais[y][x] = true;
                } else if (!map.isFree(x * tileSize + 16, y * tileSize + 16, 1, 1)) {
                    boolean horiz = (x > 0 && blocosVistos[y][x - 1]) && (x < cols - 1 && blocosVistos[y][x + 1]);
                    boolean vert = (y > 0 && blocosVistos[y - 1][x]) && (y < rows - 1 && blocosVistos[y + 1][x]);

                    boolean chaoPerto = false;
                    for (int dy = -1; dy <= 1 && !chaoPerto; dy++) {
                        for (int dx = -1; dx <= 1 && !chaoPerto; dx++) {
                            if (dx == 0 && dy == 0) continue;
                            int nx = x + dx;
                            int ny = y + dy;
                            if (nx >= 0 && nx < cols && ny >= 0 && ny < rows) {
                                if (chaoVisto[ny][nx]) {
                                    chaoPerto = true;
                                }
                            }
                        }
                    }

                    if (horiz || vert || chaoPerto) {
                        blocosFinais[y][x] = true;
                    }
                }
            }
        }

        var blocosPath = new Path2D.Float();
        for (int y = minTileY; y <= maxTileY; y++) {
            for (int x = minTileX; x <= maxTileX; x++) {
                if (blocosFinais[y][x]) {
                    blocosPath.append(new Rectangle2D.Float(x * tileSize - 1.0f, y * tileSize - 1.0f, tileSize + 2.0f, tileSize + 2.0f), false);
                }
            }
        }

        areaLuz.add(new Area(blocosPath));

        var escuridao = new Area(new Rectangle(-5000, -5000, 10000, 10000));
        escuridao.subtract(areaLuz);

        g2d.setColor(new Color(0, 0, 0, 245));
        g2d.fill(escuridao);

        float[] fractions = {0.0f, 0.55f, 1.0f};
        Color[] colors = {new Color(0, 0, 0, 0), new Color(0, 0, 0, 110), new Color(0, 0, 0, 245)};

        var gradientePenumbra = new RadialGradientPaint(cx, cy, raioVisao, fractions, colors);

        Paint paintOriginal = g2d.getPaint();
        g2d.setPaint(gradientePenumbra);
        g2d.fill(areaLuz);
        g2d.setPaint(paintOriginal);
    }

    private void drawInteractablePrompts(Graphics2D g2d) {
        float pcx = player.getX() + player.getWidth() / 2f;
        float pcy = player.getY() + player.getHeight() / 2f;

        for (Interactable obj : map.getInteractables()) {
            if (obj.isNear(player.getX(), player.getY(), player.getWidth(), player.getHeight())) {
                boolean temVisao = true;
                if (obj instanceof PerkMachine pm) {
                    float mcx = pm.getX() + 16;
                    float mcy = pm.getY() + 16;
                    temVisao = hasLineOfSight(pcx, pcy, mcx, mcy);
                }
                if (temVisao) {
                    obj.drawPrompt(g2d, player);
                }
            }
        }
    }

    private void drawHUD(Graphics2D g2d) {
        g2d.setFont(game.getPixelFont());
        FontMetrics fm = g2d.getFontMetrics();

        // Textos Superiores (Kills, Round, FPS)
        g2d.setColor(Color.WHITE);
        g2d.drawString("ROBÔS ELIMINADOS: " + pontuacao, 20, 40);

        String roundText = "ROUND: " + round;
        int roundX = GamePanel.WIDTH - fm.stringWidth(roundText) - 20;
        g2d.setColor(new Color(255, 204, 0));
        g2d.drawString(roundText, roundX, 40);

        g2d.setFont(game.getPixelFont().deriveFont(12f));
        g2d.setColor(Color.GRAY);
        g2d.drawString("FPS: " + game.getFps(), 20, 65);

        // --- ARMA ATUAL (Texto menor no Canto Inferior Direito) ---
        g2d.setFont(game.getPixelFont().deriveFont(11f)); // Tamanho de fonte reduzido
        fm = g2d.getFontMetrics();
        String armaText = player.getArmaAtual().getNome().toUpperCase();
        int armaX = GamePanel.WIDTH - fm.stringWidth(armaText) - 20;
        g2d.setColor(Color.WHITE);
        g2d.drawString(armaText, armaX, GamePanel.HEIGHT - 55);

        // Dinheiro
        g2d.setFont(game.getPixelFont());
        fm = g2d.getFontMetrics();
        String moneyText = "$$: " + dinheiro;
        int moneyX = GamePanel.WIDTH - fm.stringWidth(moneyText) - 20;
        g2d.setColor(new Color(57, 255, 20));
        g2d.drawString(moneyText, moneyX, GamePanel.HEIGHT - 30);

        // --- PERKS MINIMALISTAS (Quadrados no Centro Inferior) ---
        List<PerkType> perks = player.getPerksAtivos();
        int iconSize = 34; // Tamanho do quadrado
        int gap = 10;
        int totalWidth = (perks.size() * iconSize) + (Math.max(0, perks.size() - 1) * gap);
        int startX = (GamePanel.WIDTH - totalWidth) / 2;
        int startY = GamePanel.HEIGHT - 60;

        g2d.setFont(game.getPixelFont().deriveFont(12f));
        FontMetrics fmPerk = g2d.getFontMetrics();

        for (int i = 0; i < perks.size(); i++) {
            String nome = perks.get(i).getNome();
            String[] partes = nome.trim().split("\\s+");
            String iniciais;

            // Lógica de 2 letras
            if(partes.length > 1) {
                iniciais = partes.length > 2 ?  partes[0].charAt(0) + partes[2].substring(0, 1) : partes[0].charAt(0) + partes[1].substring(0, 1);
            } else {
                iniciais = nome.substring(0, Math.min(2, nome.length()));
            }

            iniciais = iniciais.toUpperCase();

            int px = startX + i * (iconSize + gap);

            // Fundo e Borda do Quadrado
            g2d.setColor(new Color(0, 0, 0, 180));
            g2d.fillRoundRect(px, startY, iconSize, iconSize, 6, 6);

            g2d.setColor(new Color(102, 252, 241));
            g2d.drawRoundRect(px, startY, iconSize, iconSize, 6, 6);

            // Cálculo de centralização exata (Horizontal e Vertical)
            int textWidth = fmPerk.stringWidth(iniciais);
            int textX = px + (iconSize - textWidth) / 2;
            int textY = startY + (iconSize + fmPerk.getAscent() - fmPerk.getDescent()) / 2;

            g2d.setColor(Color.WHITE);
            g2d.drawString(iniciais, textX, textY);
        }

        // --- BARRAS DE STAMINA E HP (Canto Inferior Esquerdo) ---
        Composite oldComposite = g2d.getComposite();
        boolean recemRevividoUI = (System.currentTimeMillis() - player.getLastReviveTime() < 2000);

        if (recemRevividoUI && (System.currentTimeMillis() / 150) % 2 == 0) {
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.2f));
        }

        int barWidth = 200, barHeight = 15, barX = 20, barY = GamePanel.HEIGHT - 40;
        g2d.setColor(new Color(0, 0, 0, 150));
        g2d.fillRect(barX, barY, barWidth, barHeight);

        float currentStamina = player.getStamina();
        float maxStamina = player.getMaxStamina();
        int fillWidth = (int) ((currentStamina / maxStamina) * barWidth);
        g2d.setColor(player.isExhausted() ? new Color(255, 50, 50) : new Color(50, 150, 255));
        g2d.fillRect(barX, barY, fillWidth, barHeight);

        g2d.setColor(Color.WHITE);
        g2d.drawRect(barX, barY, barWidth, barHeight);
        g2d.setFont(game.getPixelFont().deriveFont(12f));
        g2d.drawString("STAMINA", 20, barY - 8);

        for (int i = 0; i < player.getMaxHp(); i++) {
            int hpX = 20 + (i * 25);
            int hpY = GamePanel.HEIGHT - 100;
            if (i < player.getHp()) {
                g2d.fillRect(hpX, hpY, 20, 20);
            } else {
                g2d.drawRect(hpX, hpY, 20, 20);
            }
        }

        g2d.setComposite(oldComposite);
    }

    private void drawRoundInterval(Graphics2D g2d) {
        int segundosRestantes = (900 - ticksIntervalo) / 60;
        String msg1 = "ROUND " + round + " CONCLUÍDO";
        String msg2 = "PRÓXIMO EM: " + segundosRestantes + "s";

        int boxWidth = 280;
        int boxHeight = 46;
        int boxX = (GamePanel.WIDTH - boxWidth) / 2;
        int boxY = 12;

        g2d.setColor(new Color(0, 0, 0, 190));
        g2d.fillRoundRect(boxX, boxY, boxWidth, boxHeight, 12, 12);
        g2d.setColor(new Color(255, 50, 50, 180));
        g2d.drawRoundRect(boxX, boxY, boxWidth, boxHeight, 12, 12);

        g2d.setFont(game.getTitleFont().deriveFont(16f));
        FontMetrics fmTitle = g2d.getFontMetrics();
        g2d.setColor(new Color(255, 80, 80));
        g2d.drawString(msg1, (GamePanel.WIDTH - fmTitle.stringWidth(msg1)) / 2, boxY + 20);

        g2d.setFont(game.getPixelFont().deriveFont(13f));
        FontMetrics fmSub = g2d.getFontMetrics();
        g2d.setColor(Color.WHITE);
        g2d.drawString(msg2, (GamePanel.WIDTH - fmSub.stringWidth(msg2)) / 2, boxY + 38);
    }

    private void drawDeathScreen(Graphics2D g2d) {
        float alphaOverlay = Math.min(1.0f, ticksMorte / 300.0f);
        g2d.setColor(new Color(180, 0, 0, (int) (alphaOverlay * 190)));
        g2d.fillRect(0, 0, GamePanel.WIDTH, GamePanel.HEIGHT);

        float alphaText = 0;
        if (ticksMorte > 180) {
            alphaText = Math.min(1.0f, (ticksMorte - 180) / 100.0f);
        }

        if (alphaText > 0) {
            g2d.setFont(game.getTitleFont().deriveFont(32f));
            g2d.setColor(new Color(255, 255, 255, (int) (alphaText * 255)));
            String msgDead = "SISTEMA COMPROMETIDO...";
            FontMetrics fmDead = g2d.getFontMetrics();
            g2d.drawString(msgDead, (GamePanel.WIDTH - fmDead.stringWidth(msgDead)) / 2, GamePanel.HEIGHT / 2);
        }
    }

    private void drawCustomCursor(Graphics2D g2d) {
        int mouseX = game.getMouseX();
        int mouseY = game.getMouseY();

        g2d.setColor(new Color(255, 0, 85, 200));
        g2d.drawOval(mouseX - 10, mouseY - 10, 20, 20);
        g2d.drawLine(mouseX - 14, mouseY, mouseX - 4, mouseY);
        g2d.drawLine(mouseX + 4, mouseY, mouseX + 14, mouseY);
        g2d.drawLine(mouseX, mouseY - 14, mouseX, mouseY - 4);
        g2d.drawLine(mouseX, mouseY + 4, mouseX, mouseY + 14);

        g2d.setColor(new Color(102, 252, 241));
        g2d.fillRect(mouseX - 1, mouseY - 1, 2, 2);
    }

    private void drawPerkExchangeScreen(Graphics2D g2d) {
        g2d.setColor(new Color(0, 0, 0, 220));
        g2d.fillRect(0, 0, GamePanel.WIDTH, GamePanel.HEIGHT);

        g2d.setFont(game.getTitleFont().deriveFont(24f));
        g2d.setColor(Color.WHITE);
        g2d.drawString("SUBSTITUIR HABILIDADE", GamePanel.WIDTH / 2 - 150, 100);

        g2d.setFont(game.getPixelFont().deriveFont(16f));
        g2d.setColor(new Color(255, 204, 0));
        g2d.drawString("NOVO: " + perkPendente.getNome(), GamePanel.WIDTH / 2 - 100, 150);

        g2d.setColor(Color.WHITE);
        List<PerkType> ativos = player.getPerksAtivos();
        for (int i = 0; i < ativos.size(); i++) {
            g2d.drawString("[" + (i + 1) + "] " + ativos.get(i).getNome(), GamePanel.WIDTH / 2 - 100, 220 + (i * 30));
        }

        g2d.setColor(Color.GRAY);
        g2d.drawString("[ESC] CANCELAR", GamePanel.WIDTH / 2 - 80, 400);
    }

    public void drawInMenu(Graphics2D g2d) {
        camera.tick(player);
        g2d.translate(-camera.getX(), -camera.getY());
        map.render(g2d, camera);
        player.draw(g2d);
        g2d.translate(camera.getX(), camera.getY());
    }

    public boolean hasLineOfSight(float px, float py, float alvoX, float alvoY) {
        float dist = (float) Math.hypot(alvoX - px, alvoY - py);
        if (dist > 350) return false;

        int steps = Math.max(1, (int) (dist / 10f));
        float dx = (alvoX - px) / steps;
        float dy = (alvoY - py) / steps;

        float currX = px;
        float currY = py;

        for (int i = 0; i < steps - 2; i++) {
            if (!map.isFree(currX, currY, 1, 1)) return false;
            currX += dx;
            currY += dy;
        }
        return true;
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (trocandoPerk) {
            if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                trocandoPerk = false;
                return;
            }

            int slot = -1;
            if (e.getKeyCode() == KeyEvent.VK_1) slot = 0;
            else if (e.getKeyCode() == KeyEvent.VK_2) slot = 1;
            else if (e.getKeyCode() == KeyEvent.VK_3) slot = 2;
            else if (e.getKeyCode() == KeyEvent.VK_4) slot = 3;
            else if (e.getKeyCode() == KeyEvent.VK_5) slot = 4;

            if (slot >= 0 && slot < player.getPerksAtivos().size()) {
                gastarDinheiro(perkPendente.getCusto());
                player.substituirPerk(slot, perkPendente);
                trocandoPerk = false;
                SoundManager.playSFX("comprar");
            }
            return;
        }

        if (e.getKeyCode() == KeyEvent.VK_ESCAPE && !morrendo) {
            game.setState(game.getPauseState());
        }

        if (e.getKeyCode() == KeyEvent.VK_G && !morrendo) {
            tentarAtivarReplicante();
        }

        if (e.getKeyCode() == KeyEvent.VK_F && !morrendo) {
            float pcx = player.getX() + player.getWidth() / 2f;
            float pcy = player.getY() + player.getHeight() / 2f;

            int countBefore = map.getInteractables().size();
            for (Interactable obj : map.getInteractables()) {
                if (obj.isNear(player.getX(), player.getY(), player.getWidth(), player.getHeight())) {

                    boolean temVisao = true;
                    if (obj instanceof PerkMachine pm) {
                        temVisao = hasLineOfSight(pcx, pcy, pm.getX() + 16, pm.getY() + 16);
                    }

                    if (temVisao) {
                        obj.onInteract(this);
                        break;
                    }
                }
            }
            map.getInteractables().removeIf(Interactable::isConsumed);
            int countAfter = map.getInteractables().size();

            if (countBefore > countAfter) {
                portasAbertas++;
            }
        }

        player.keyPressed(e);
    }

    @Override
    public void keyReleased(KeyEvent e) {
        player.keyReleased(e);
    }

    @Override
    public void mouseMoved(MouseEvent e) { }

    @Override
    public void mousePressed(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1 && !morrendo) {
            isAtirando = true;
        }
        if (e.getButton() == MouseEvent.BUTTON3 && !morrendo) {
            isAtirandoSecundario = true;
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1) {
            isAtirando = false;
            if (player.getArmaAtual() != null) {
                player.getArmaAtual().liberarGatilho();
            }
        }
        if (e.getButton() == MouseEvent.BUTTON3) {
            isAtirandoSecundario = false;
        }
    }
}