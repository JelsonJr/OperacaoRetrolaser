package br.com.uepg.operacaoretrolaser.enemies;

import br.com.uepg.operacaoretrolaser.ui.GameMap;
import br.com.uepg.operacaoretrolaser.player.Player;

import java.awt.Point;
import java.util.LinkedList;
import java.util.Queue;

public class FlowField {
    private final Point[][] flowGrid; // Vetores de direção (ex: 1,0 para direita; 0,-1 para cima)
    private final int cols, rows;

    public FlowField(int mapWidth, int mapHeight) {
        this.cols = mapWidth / GameMap.TILE_SIZE;
        this.rows = mapHeight / GameMap.TILE_SIZE;
        this.flowGrid = new Point[cols][rows];
    }

    public void updateField(GameMap map, Player player) {
        int targetCol = (int) (player.getX() + (float) player.getWidth() / 2) / GameMap.TILE_SIZE;
        int targetRow = (int) (player.getY() + (float) player.getHeight() / 2) / GameMap.TILE_SIZE;

        int[][] distanceGrid = new int[cols][rows];
        for (int i = 0; i < cols; i++) {
            for (int j = 0; j < rows; j++) {
                distanceGrid[i][j] = 9999; // Representa inalcançável
                flowGrid[i][j] = new Point(0, 0);
            }
        }

        Queue<Point> queue = new LinkedList<>();

        // Garante que o alvo está dentro dos limites
        if(targetCol >= 0 && targetCol < cols && targetRow >= 0 && targetRow < rows) {
            distanceGrid[targetCol][targetRow] = 0;
            queue.add(new Point(targetCol, targetRow));
        }

        // Dijkstra / BFS para calcular distâncias (Apenas 4 direções para evitar corte de quina)
        int[][] dirs = {{0, -1}, {0, 1}, {-1, 0}, {1, 0}};

        while (!queue.isEmpty()) {
            Point current = queue.poll();

            for (int[] dir : dirs) {
                int nCol = current.x + dir[0];
                int nRow = current.y + dir[1];

                if (nCol >= 0 && nCol < cols && nRow >= 0 && nRow < rows) {
                    // Testa o centro do tile para ver se é parede (ajuste conforme seu map.isFree)
                    if (map.isFree(nCol * GameMap.TILE_SIZE + GameMap.TILE_SIZE/2f, nRow * GameMap.TILE_SIZE + GameMap.TILE_SIZE/2f, 2, 2)) {
                        if (distanceGrid[nCol][nRow] > distanceGrid[current.x][current.y] + 1) {
                            distanceGrid[nCol][nRow] = distanceGrid[current.x][current.y] + 1;
                            queue.add(new Point(nCol, nRow));
                        }
                    }
                }
            }
        }

        // Gerar os Vetores baseados no menor vizinho
        for (int x = 0; x < cols; x++) {
            for (int y = 0; y < rows; y++) {
                if (distanceGrid[x][y] == 9999 || distanceGrid[x][y] == 0) continue;

                int bestCost = distanceGrid[x][y];
                Point bestDir = new Point(0, 0);

                for (int[] dir : dirs) {
                    int nCol = x + dir[0];
                    int nRow = y + dir[1];

                    if (nCol >= 0 && nCol < cols && nRow >= 0 && nRow < rows) {
                        if (distanceGrid[nCol][nRow] < bestCost) {
                            bestCost = distanceGrid[nCol][nRow];
                            bestDir = new Point(dir[0], dir[1]);
                        }
                    }
                }
                flowGrid[x][y] = bestDir;
            }
        }
    }

    public Point getVector(int col, int row) {
        if (col >= 0 && col < cols && row >= 0 && row < rows) {
            return flowGrid[col][row];
        }
        return new Point(0, 0);
    }
}