package br.com.uepg.operacaoretrolaser.enemies;

import br.com.uepg.operacaoretrolaser.player.Player;
import br.com.uepg.operacaoretrolaser.player.PlayerClone;
import br.com.uepg.operacaoretrolaser.ui.GameMap;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.*;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class EnemyManager {
    private final List<Robot> activeRobots = new CopyOnWriteArrayList<>();
    private final List<EnemyLaser> enemyLasers = new ArrayList<>();
    private final List<PendingSpawn> pendingSpawns = new ArrayList<>();
    private final int MAX_ACTIVE = 75;
    private final List<Spawner> spawners = new ArrayList<>();
    private int currentRound;
    private int totalEnemiesThisRound;
    private int enemiesSpawnedThisRound;
    private int enemiesKilledThisRound;

    private int currentHpLimit;
    private long currentCooldown;
    private double currentSprintChance;
    private long lastGlobalSpawnTime = 0;
    private long currentSpawnInterval = 1000;

    public void iniciarRound(int round) {
        this.currentRound = round;
        enemiesSpawnedThisRound = 0;
        enemiesKilledThisRound = 0;
        activeRobots.clear();
        enemyLasers.clear();
        pendingSpawns.clear();
        totalEnemiesThisRound = 15 + ((round - 1) * round >= 15 ? 10 : 5);

        currentHpLimit = Math.min(900, 20 + 20 * (round - 1));
        currentCooldown = Math.max(2000, 10000 - 800L * (round - 1));
        currentSprintChance = Math.min(0.7, 0.10 + 0.05 * (round - 1));

        calcularProximoIntervalo();
    }

    public void addLaser(EnemyLaser laser) {
        enemyLasers.add(laser);
    }

    public void update(GameMap map, Player player, FlowField flowField, PlayerClone pclone, boolean chanceExtra) {
        if (spawners.isEmpty() && map.getSpawnPoints() != null) {
            for (Point p : map.getSpawnPoints()) {
                spawners.add(new Spawner(p.x * 32, p.y * 32));
            }
        }

        processPendingSpawns(map);

        for (Spawner s : spawners) {
            s.update();
        }

        int totalEmJogoEOuPendentes = activeRobots.size() + pendingSpawns.size();
        if (totalEmJogoEOuPendentes < MAX_ACTIVE && enemiesSpawnedThisRound < totalEnemiesThisRound) {
            if (System.currentTimeMillis() - lastGlobalSpawnTime >= currentSpawnInterval) {
                spawnBatch(map, player);
            }
        }

        for (int i = 0; i < enemyLasers.size(); i++) {
            EnemyLaser l = enemyLasers.get(i);
            l.update(map, player);

            if (!l.isActive()) {
                enemyLasers.remove(i);
                i--;
            }
        }

        for (int i = 0; i < activeRobots.size(); i++) {
            Robot r = activeRobots.get(i);
            r.update(map, player, pclone, chanceExtra, flowField);

            if (r.isMorto()) {
                activeRobots.remove(i);
                enemiesKilledThisRound++;
                i--;
            }
        }
    }

    private void calcularProximoIntervalo() {
        // Base de tempo que diminui conforme o round avança, mas com variação aleatória de 0 a 300ms
        long base = Math.max(400, (currentRound >= 10 ? 650 : 900) - (currentRound * 15L));
        currentSpawnInterval = base + (long)(Math.random() * 300);
    }

    private void processPendingSpawns(GameMap map) {
        long now = System.currentTimeMillis();
        Iterator<PendingSpawn> iterator = pendingSpawns.iterator();

        while (iterator.hasNext()) {
            PendingSpawn pending = iterator.next();
            if (now >= pending.releaseTime) {
                Point2D.Float safePos = findSafeSpawnPosition(map, pending.spawner.x, pending.spawner.y, pending.size);

                Robot novoRobo;
                if (pending.isRanged) {
                    novoRobo = new RangedRobot(safePos.x, safePos.y, pending.size, (int) (currentHpLimit / 1.30f), false, currentCooldown + 1000, this);
                } else {
                    novoRobo = new MeleeRobot(safePos.x, safePos.y, pending.size, pending.hp, pending.isSprinter, currentCooldown);
                }

                activeRobots.add(novoRobo);
                iterator.remove();
            }
        }
    }

    private Point2D.Float findSafeSpawnPosition(GameMap map, float startX, float startY, int size) {
        float centeredX = startX + (32 - size) / 2f;
        float centeredY = startY + (32 - size) / 2f;

        if (map.isFree(centeredX, centeredY, size, size)) {
            return new Point2D.Float(centeredX, centeredY);
        }

        int[][] directions = {
                {0, 1}, {0, -1}, {1, 0}, {-1, 0},
                {1, 1}, {-1, -1}, {1, -1}, {-1, 1}
        };

        for (int[] dir : directions) {
            float testX = centeredX + (dir[0] * 12);
            float testY = centeredY + (dir[1] * 12);

            if (map.isFree(testX, testY, size, size)) {
                return new Point2D.Float(testX, testY);
            }
        }

        return new Point2D.Float(centeredX, centeredY);
    }

    private void spawnBatch(GameMap map, Player player) {
        long now = System.currentTimeMillis();
        List<SpawnerCandidate> candidates = new ArrayList<>();

        float px = player.getX();
        float py = player.getY();

        for (Spawner s : spawners) {
            if (now < s.cooldownEndTime) continue;
            if (!isReachable(map, s.x, s.y, px, py)) continue;

            float dist = (float) Math.hypot(s.x - px, s.y - py);

            // Aumentada a distância mínima para 180 (evita que nasçam muito na cara)
            if (dist < 180) continue;

            double score = 100.0;

            // Zona ideal deslocada um pouco mais para longe (250 a 600)
            if (dist >= 250 && dist <= 600) {
                score *= 3.5;
            } else if (dist > 800) {
                score *= 0.4;
            }

            double spawnerAngle = Math.atan2(s.y - py, s.x - px);
            for (Robot r : activeRobots) {
                double robotAngle = Math.atan2(r.getY() - py, r.getX() - px);
                if (Math.abs(spawnerAngle - robotAngle) > Math.PI / 2) {
                    score *= 1.2;
                }
            }

            candidates.add(new SpawnerCandidate(s, score));
        }

        if (candidates.isEmpty()) return;

        Spawner chosen = selectWeightedSpawner(candidates);
        if (chosen == null) return;

        int slotsAvailable = MAX_ACTIVE - (activeRobots.size() + pendingSpawns.size());
        int maxPossible = Math.min(3, slotsAvailable);
        maxPossible = Math.min(maxPossible, totalEnemiesThisRound - enemiesSpawnedThisRound);

        if (maxPossible <= 0) return;

        // Nova lógica de probabilidade para a rajada (1, 2 ou 3)
        int burstSize = 1;
        double randBurst = Math.random();

        // Chance menor de nascerem 3 de uma vez (ex: 15% + 1.5% por round)
        double chanceForThree = 0.15 + (currentRound * 0.015);
        // Chance média de nascerem 2 (ex: 40% + 2% por round)
        double chanceForTwo = 0.40 + (currentRound * 0.02);

        if (maxPossible == 3 && randBurst < chanceForThree) {
            burstSize = 3;
        } else if (maxPossible >= 2 && randBurst < chanceForTwo) {
            burstSize = 2;
        }

        chosen.triggerOpen(burstSize);

        for (int i = 0; i < burstSize; i++) {
            boolean isSprinter = Math.random() <= currentSprintChance;
            int size = isSprinter ? 24 : 32;
            int hp = isSprinter ? Math.min(380, currentHpLimit / 2) : currentHpLimit;
            boolean isRanged = false;

            if (currentRound > 3) {
                long rangedCount = activeRobots.stream().filter(r -> r instanceof RangedRobot).count();
                rangedCount += pendingSpawns.stream().filter(p -> p.isRanged).count();
                int limiteMaximo = Math.min(MAX_ACTIVE, totalEnemiesThisRound);
                int limiteRanged = (int) Math.max(1, limiteMaximo * 0.20);

                if (rangedCount < limiteRanged && Math.random() <= 0.25) {
                    isRanged = true;
                }
            }

            long releaseDelay = 250L + (i * 250L); // Aumentado o intervalo de saída para 250ms
            pendingSpawns.add(new PendingSpawn(chosen, now + releaseDelay, size, hp, isSprinter, isRanged));
            enemiesSpawnedThisRound++;
        }

        lastGlobalSpawnTime = now;
        calcularProximoIntervalo();

        chosen.spawnCount += burstSize;
        if (chosen.spawnCount >= 5) {
            chosen.spawnCount = 0;
            chosen.cooldownEndTime = now + 4000;
        }
    }

    private Spawner selectWeightedSpawner(List<SpawnerCandidate> candidates) {
        double totalWeight = candidates.stream().mapToDouble(c -> c.score).sum();
        double r = Math.random() * totalWeight;
        double countWeight = 0;

        for (SpawnerCandidate candidate : candidates) {
            countWeight += candidate.score;
            if (countWeight >= r) {
                return candidate.spawner;
            }
        }
        return candidates.getFirst().spawner;
    }

    private boolean isReachable(GameMap map, float startX, float startY, float targetX, float targetY) {
        int tileSize = 32;
        int sCol = (int) (startX / tileSize);
        int sRow = (int) (startY / tileSize);
        int tCol = (int) (targetX / tileSize);
        int tRow = (int) (targetY / tileSize);

        Queue<Point> queue = new LinkedList<>();
        Set<Point> visited = new HashSet<>();

        Point start = new Point(sCol, sRow);
        queue.add(start);
        visited.add(start);

        int[][] dirs = {{0, 1}, {1, 0}, {0, -1}, {-1, 0}};

        while (!queue.isEmpty()) {
            Point p = queue.poll();
            if (p.x == tCol && p.y == tRow) return true;

            for (int[] d : dirs) {
                int nx = p.x + d[0];
                int ny = p.y + d[1];
                Point np = new Point(nx, ny);

                if (!visited.contains(np)) {
                    if (map.isFree(nx * tileSize + 15, ny * tileSize + 15, 2, 2)) {
                        visited.add(np);
                        queue.add(np);
                    }
                }
            }
        }
        return false;
    }

    public void draw(Graphics2D g2d) {
        for (Spawner s : spawners) {
            s.draw(g2d);
        }
        for (Robot r : activeRobots) r.draw(g2d);
        for (EnemyLaser l : enemyLasers) l.draw(g2d);
    }

    public List<Robot> getActiveRobots() {
        return activeRobots;
    }

    public boolean isRoundConcluido() {
        return enemiesKilledThisRound >= totalEnemiesThisRound;
    }

    private static class PendingSpawn {
        Spawner spawner;
        long releaseTime;
        int size;
        int hp;
        boolean isSprinter;
        boolean isRanged;

        PendingSpawn(Spawner spawner, long releaseTime, int size, int hp, boolean isSprinter, boolean isRanged) {
            this.spawner = spawner;
            this.releaseTime = releaseTime;
            this.size = size;
            this.hp = hp;
            this.isSprinter = isSprinter;
            this.isRanged = isRanged;
        }
    }

    private static class SpawnerCandidate {
        Spawner spawner;
        double score;

        SpawnerCandidate(Spawner spawner, double score) {
            this.spawner = spawner;
            this.score = score;
        }
    }

    private static class Spawner {
        float x, y;
        int spawnCount = 0;
        long cooldownEndTime = 0;

        float doorProgress = 0.0f;
        long openTime = 0;
        long activeOpenDuration = 800;

        public Spawner(float x, float y) {
            this.x = x;
            this.y = y;
        }

        public void triggerOpen(int burstSize) {
            this.openTime = System.currentTimeMillis();
            this.activeOpenDuration = 400L + (burstSize * 300L);
        }

        public void update() {
            if (System.currentTimeMillis() - openTime < activeOpenDuration) {
                doorProgress = Math.min(1.0f, doorProgress + 0.15f);
            } else {
                doorProgress = Math.max(0.0f, doorProgress - 0.08f);
            }
        }

        public void draw(Graphics2D g2d) {
            int tileSize = 32;
            int px = (int) x;
            int py = (int) y;

            g2d.setColor(new Color(10, 10, 12));
            g2d.fillRect(px, py, tileSize, tileSize);

            if (doorProgress > 0.05f) {
                boolean piscar = (System.currentTimeMillis() % 160) < 80;
                g2d.setColor(piscar ? new Color(255, 30, 30) : new Color(90, 0, 0));
                g2d.fillRect(px + 2, py + 2, 4, 4);
                g2d.fillRect(px + tileSize - 6, py + 2, 4, 4);
            }

            int doorWidth = (int) (((float) tileSize / 2) * (1.0f - doorProgress));
            g2d.setColor(new Color(45, 52, 65));

            g2d.fillRect(px, py, doorWidth, tileSize);
            g2d.fillRect(px + tileSize - doorWidth, py, doorWidth, tileSize);

            if (doorWidth > 2) {
                g2d.setColor(new Color(220, 170, 0));
                g2d.fillRect(px + doorWidth - 2, py, 2, tileSize);
                g2d.fillRect(px + tileSize - doorWidth, py, 2, tileSize);
            }

            g2d.setColor(new Color(20, 24, 30));
            g2d.drawRect(px, py, tileSize - 1, tileSize - 1);
        }
    }
}