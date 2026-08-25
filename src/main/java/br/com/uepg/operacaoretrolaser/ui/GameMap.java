package br.com.uepg.operacaoretrolaser.ui;

import br.com.uepg.operacaoretrolaser.GamePanel;
import br.com.uepg.operacaoretrolaser.interactables.*;
import br.com.uepg.operacaoretrolaser.player.Camera;
import br.com.uepg.operacaoretrolaser.weapons.Weapon;
import br.com.uepg.operacaoretrolaser.weapons.WeaponFactory;

import java.awt.*;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class GameMap {
    public static final int TILE_SIZE = 32;
    public static int COLS = 0;
    public static int ROWS = 0;
    private final List<Point> spawnPoints = new ArrayList<>();
    private final List<Interactable> interactables = new ArrayList<>();
    public int[][] matrizMapa;
    public Point playerSpawn = new Point(100, 100);

    public GameMap(String resourcePath) {
        carregarMapaDoArquivo(resourcePath);
    }

    public List<Point> getSpawnPoints() {
        return spawnPoints;
    }

    private void carregarMapaDoArquivo(String path) {
        try {
            InputStream is = getClass().getResourceAsStream(path);
            if (is == null) {
                System.err.println("ERRO: Arquivo de mapa não encontrado: " + path);
                return;
            }

            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            List<String> linhas = new ArrayList<>();
            String linha;

            while ((linha = br.readLine()) != null) {
                if (!linha.trim().isEmpty()) {
                    linhas.add(linha);
                }
            }
            br.close();

            if (linhas.isEmpty()) return;

            ROWS = linhas.size();
            COLS = linhas.getFirst().length();
            matrizMapa = new int[ROWS][COLS];

            for (int y = 0; y < ROWS; y++) {
                String l = linhas.get(y);
                for (int x = 0; x < COLS; x++) {
                    char c = (x < l.length()) ? l.charAt(x) : '#';

                    if (c == '#') {
                        matrizMapa[y][x] = 1; // Parede
                    } else if (c == 'D') {
                        matrizMapa[y][x] = 2; // Porta
                    } else if (c == 'X') {
                        matrizMapa[y][x] = 3; // Caixa de Suprimentos
                    } else if (c == '*') {
                        matrizMapa[y][x] = 4; // Mesa Tática
                    } else if (c == 'Y') {
                        matrizMapa[y][x] = 5; // Óleo no chão
                    } else if (c == '+') {
                        matrizMapa[y][x] = 6; // Chão destruído
                    } else if (c == '=') {
                        matrizMapa[y][x] = 7; // Faixa de Alerta
                    } else if (c == 'O') {
                        matrizMapa[y][x] = 8; // Grelha de Ventilação
                    } else if (c == ',') {
                        matrizMapa[y][x] = 9; // Sangue / Ferrugem
                    } else if (c == 'P') {
                        matrizMapa[y][x] = 0; // Chão (Spawn Player)
                        playerSpawn = new Point(x * TILE_SIZE, y * TILE_SIZE);
                    } else if (c == '@') {
                        matrizMapa[y][x] = 0; // Chão (Spawn Inimigo)
                        spawnPoints.add(new Point(x, y));
                    } else if (c >= 'A' && c <= 'L') {
                        matrizMapa[y][x] = 1; // Arma na parede funciona como colisão
                        Weapon armaParede = null;
                        int custo = 500;
                        switch (c) {
                            case 'A' -> {
                                armaParede = WeaponFactory.criarSubmetralhadora();
                                custo = 750;
                            }
                            case 'B' -> {
                                armaParede = WeaponFactory.criarRifleAssalto();
                                custo = 1250;
                            }
                            case 'C' -> {
                                armaParede = WeaponFactory.criarEscopeta();
                                custo = 1500;
                            }
                            case 'E' -> {
                                armaParede = WeaponFactory.criarSniper();
                                custo = 2000;
                            }
                            case 'F' -> {
                                armaParede = WeaponFactory.criarMetralhadoraPesada();
                                custo = 2500;
                            }
                            case 'G' -> {
                                armaParede = WeaponFactory.criarPlasma();
                                custo = 3000;
                            }
                            case 'J' -> {
                                armaParede = WeaponFactory.criarRevolver();
                                custo = 2200;
                            }
                            case 'K' -> {
                                armaParede = WeaponFactory.criarEscopetaK();
                                custo = 2500;
                            }
                            case 'L' -> {
                                armaParede = WeaponFactory.criarRifleGaussL();
                                custo = 1750;
                            }
                        }
                        interactables.add(new WallWeapon(x * TILE_SIZE, y * TILE_SIZE, armaParede, custo));
                    } else if (c >= '1' && c <= '8') {
                        matrizMapa[y][x] = 1;

                        PerkType tipo = switch (c) {
                            case '2' -> PerkType.CHANCE_EXTRA;
                            case '3' -> PerkType.PULMAO_ATLETA;
                            case '4' -> PerkType.GOLPE_DURO;
                            case '5' -> PerkType.VISAO_AGUIA;
                            case '6' -> PerkType.PISTOLEIRO;
                            case '7' -> PerkType.TIRO_DUPLO;
                            case '8' -> PerkType.REPLICANTE;
                            default -> PerkType.TANQUE;
                        };

                        interactables.add(new PerkMachine(x * TILE_SIZE, y * TILE_SIZE, tipo));
                    } else if (c == '0') {
                        matrizMapa[y][x] = 1; // Transforma a máquina em um objeto sólido (parede)
                        interactables.add(new UpgradeStation(x * TILE_SIZE, y * TILE_SIZE));
                    } else {
                        matrizMapa[y][x] = 0; // Chão limpo
                    }
                }
            }

            agruparPortasConectadas();

        } catch (Exception e) {
            System.out.println("Erro ao carregar mapa: " + e.getMessage());
        }
    }

    private void agruparPortasConectadas() {
        boolean[][] verificados = new boolean[ROWS][COLS];

        for (int y = 0; y < ROWS; y++) {
            for (int x = 0; x < COLS; x++) {
                if (matrizMapa[y][x] == 2 && !verificados[y][x]) {
                    List<Point> blocosDaPorta = new ArrayList<>();
                    buscarVizinhos(x, y, verificados, blocosDaPorta);

                    double dist = playerSpawn.distance(x * TILE_SIZE, y * TILE_SIZE);
                    int custo = 500;
                    if (dist > 1200) custo = 1500;
                    else if (dist > 600) custo = 1000;

                    interactables.add(new Door(blocosDaPorta, custo));
                }
            }
        }
    }

    private void buscarVizinhos(int x, int y, boolean[][] verificados, List<Point> blocos) {
        if (x < 0 || x >= COLS || y < 0 || y >= ROWS) return;
        if (verificados[y][x] || matrizMapa[y][x] != 2) return;

        verificados[y][x] = true;
        blocos.add(new Point(x, y));

        buscarVizinhos(x + 1, y, verificados, blocos);
        buscarVizinhos(x - 1, y, verificados, blocos);
        buscarVizinhos(x, y + 1, verificados, blocos);
        buscarVizinhos(x, y - 1, verificados, blocos);
    }

    public void liberarTilesDaPorta(List<Point> tiles) {
        for (Point p : tiles) {
            if (p.y >= 0 && p.y < ROWS && p.x >= 0 && p.x < COLS) {
                matrizMapa[p.y][p.x] = 0;
            }
        }
    }

    public List<Interactable> getInteractables() {
        return interactables;
    }

    public boolean isFree(float nextX, float nextY, int width, int height) {
        int leftTile = (int) Math.floor(nextX / TILE_SIZE);
        int rightTile = (int) Math.floor((nextX + width - 1) / TILE_SIZE);
        int topTile = (int) Math.floor(nextY / TILE_SIZE);
        int bottomTile = (int) Math.floor((nextY + height - 1) / TILE_SIZE);

        if (leftTile < 0 || rightTile >= COLS || topTile < 0 || bottomTile >= ROWS) return false;

        return isWalkable(matrizMapa[topTile][leftTile]) && isWalkable(matrizMapa[topTile][rightTile]) && isWalkable(matrizMapa[bottomTile][leftTile]) && isWalkable(matrizMapa[bottomTile][rightTile]);
    }

    private boolean isWalkable(int tileCode) {
        return tileCode == 0 || (tileCode >= 5 && tileCode <= 9);
    }

    private int getTileHash(int x, int y) {
        int hash = x * 374761393 + y * 668265263;
        hash = (hash ^ (hash >> 13)) * 1274126177;
        return Math.abs(hash ^ (hash >> 16));
    }

    public void render(Graphics2D g2d, Camera camera) {
        int xStart = Math.max(0, (int) (camera.getX() / TILE_SIZE));
        int xEnd = Math.min(COLS, (int) ((camera.getX() + GamePanel.WIDTH) / TILE_SIZE) + 2);
        int yStart = Math.max(0, (int) (camera.getY() / TILE_SIZE));
        int yEnd = Math.min(ROWS, (int) ((camera.getY() + GamePanel.HEIGHT) / TILE_SIZE) + 2);

        int wallHeight = 14;

        // RENDERIZAÇÃO DE CHÃO E DETALHES
        for (int y = yStart; y < yEnd; y++) {
            for (int x = xStart; x < xEnd; x++) {
                int tile = matrizMapa[y][x];
                int px = x * TILE_SIZE;
                int py = y * TILE_SIZE;
                int hash = getTileHash(x, y);

                if (tile != 1) { // Base do Piso
                    int colorVar = (hash % 12) - 6;
                    g2d.setColor(new Color(Math.clamp(32 + colorVar, 0, 255), Math.clamp(35 + colorVar, 0, 255), Math.clamp(42 + colorVar, 0, 255)));
                    g2d.fillRect(px, py, TILE_SIZE, TILE_SIZE);
                    g2d.setColor(new Color(22, 25, 30));
                    g2d.drawRect(px, py, TILE_SIZE, TILE_SIZE);
                    g2d.setColor(new Color(45, 50, 60));
                    g2d.fillRect(px + 2, py + 2, 2, 2);
                    g2d.fillRect(px + TILE_SIZE - 4, py + 2, 2, 2);
                }

                // Renderização Específica
                switch (tile) {
                    case 5: // Óleo
                        g2d.setColor(new Color(8, 10, 8, 230));
                        g2d.fillOval(px - 6, py - 4, 44, 38);
                        g2d.setColor(new Color(18, 22, 20, 200));
                        g2d.fillOval(px, py + 2, 30, 26);
                        g2d.setColor(new Color(0, 180, 210, 45));
                        g2d.fillOval(px + 6, py + 8, 18, 12);
                        break;
                    case 6: // Destruído
                        g2d.setColor(new Color(10, 10, 12, 180));
                        g2d.fillOval(px + 2, py + 2, 28, 28);
                        g2d.setColor(new Color(220, 220, 230, 40));
                        g2d.drawLine(px + 4, py + 16, px + 14, py + 8);
                        g2d.drawLine(px + 14, py + 8, px + 26, py + 22);
                        break;
                    case 7: // Faixa de Alerta (=)
                        g2d.setColor(new Color(200, 160, 0, 180));
                        g2d.fillRect(px, py, TILE_SIZE, TILE_SIZE);
                        g2d.setColor(new Color(20, 20, 20, 200));
                        for (int i = -TILE_SIZE; i < TILE_SIZE; i += 8) {
                            g2d.drawLine(px + i, py, px + i + TILE_SIZE, py + TILE_SIZE);
                            g2d.drawLine(px + i + 1, py, px + i + TILE_SIZE + 1, py + TILE_SIZE);
                        }
                        break;
                    case 8: // Grelha (O)
                        g2d.setColor(new Color(15, 18, 22));
                        g2d.fillRect(px + 2, py + 2, TILE_SIZE - 4, TILE_SIZE - 4);
                        g2d.setColor(new Color(60, 65, 70));
                        for (int i = 4; i < TILE_SIZE; i += 6) {
                            g2d.drawLine(px + i, py + 2, px + i, py + TILE_SIZE - 2);
                            g2d.drawLine(px + 2, py + i, px + TILE_SIZE - 2, py + i);
                        }
                        break;
                    case 9: // Sangue / Ferrugem (,)
                        g2d.setColor(new Color(80, 20, 20, 180));
                        g2d.fillPolygon(new int[]{px + 10, px + 25, px + 20, px + 8}, new int[]{py + 15, py + 8, py + 25, py + 20}, 4);
                        g2d.fillOval(px + 5, py + 5, 8, 8);
                        g2d.fillOval(px + 22, py + 18, 6, 6);
                        break;
                    case 3: // Caixa
                        g2d.setColor(new Color(65, 48, 30));
                        g2d.fillRect(px + 2, py + 2, TILE_SIZE - 4, TILE_SIZE - 4);
                        g2d.setColor(new Color(110, 85, 55));
                        g2d.drawRect(px + 2, py + 2, TILE_SIZE - 4, TILE_SIZE - 4);
                        g2d.setColor(new Color(45, 32, 20));
                        g2d.drawLine(px + 3, py + 3, px + TILE_SIZE - 3, py + TILE_SIZE - 3);
                        break;
                    case 4: // Mesa
                        g2d.setColor(new Color(45, 50, 60));
                        g2d.fillRect(px + 2, py + 4, TILE_SIZE - 4, TILE_SIZE - 8);
                        g2d.setColor(new Color(102, 252, 241, 160));
                        g2d.drawRect(px + 5, py + 7, TILE_SIZE - 10, TILE_SIZE - 14);
                        break;
                }
            }
        }

        // RENDERIZAÇÃO DE PAREDES
        for (int y = yStart; y < yEnd; y++) {
            for (int x = xStart; x < xEnd; x++) {
                if (matrizMapa[y][x] == 1) {
                    int px = x * TILE_SIZE;
                    int py = y * TILE_SIZE;
                    boolean wallAbove = (y > 0) && matrizMapa[y - 1][x] == 1;
                    boolean wallBelow = (y < ROWS - 1) && matrizMapa[y + 1][x] == 1;
                    boolean wallLeft = (x > 0) && matrizMapa[y][x - 1] == 1;
                    boolean wallRight = (x < COLS - 1) && matrizMapa[y][x + 1] == 1;

                    if (!wallBelow) {
                        g2d.setColor(new Color(0, 0, 0, 160));
                        g2d.fillRect(px, py + TILE_SIZE, TILE_SIZE, 12);
                    }

                    g2d.setColor(new Color(38, 46, 58));
                    if (wallBelow) {
                        g2d.fillRect(px, py, TILE_SIZE, TILE_SIZE);
                    } else {
                        g2d.fillRect(px, py, TILE_SIZE, TILE_SIZE - wallHeight);
                        g2d.setColor(new Color(18, 22, 28));
                        g2d.fillRect(px, py + TILE_SIZE - wallHeight, TILE_SIZE, wallHeight);
                    }

                    g2d.setColor(new Color(102, 252, 241, 100));
                    if (!wallAbove) g2d.drawLine(px, py, px + TILE_SIZE, py);

                    int i3 = py + TILE_SIZE - (wallBelow ? 0 : wallHeight);

                    if (!wallLeft) g2d.drawLine(px, py, px, i3);
                    if (!wallRight) g2d.drawLine(px + TILE_SIZE - 1, py, px + TILE_SIZE - 1, i3);

                    if (!wallBelow) {
                        g2d.setColor(new Color(102, 252, 241, 180));
                        g2d.drawLine(px, py + TILE_SIZE - wallHeight, px + TILE_SIZE, py + TILE_SIZE - wallHeight);
                    }
                }
            }
        }

        for (Interactable obj : interactables) {
            obj.draw(g2d);
        }
    }
}