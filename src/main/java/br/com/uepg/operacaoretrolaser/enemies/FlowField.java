package br.com.uepg.operacaoretrolaser.enemies;

import br.com.uepg.operacaoretrolaser.ui.GameMap;
import br.com.uepg.operacaoretrolaser.player.Player;
import java.awt.Point;

public class FlowField {
    private final Point[][] flowGrid;
    private final int[][] distanceGrid;
    private final int cols, rows;

    // Fila primitiva para evitar alocação de memória no Garbage Collector (Zera os engasgos do jogo)
    private final int[] queueX;
    private final int[] queueY;

    public FlowField(int mapWidth, int mapHeight) {
        this.cols = mapWidth / GameMap.TILE_SIZE;
        this.rows = mapHeight / GameMap.TILE_SIZE;
        this.flowGrid = new Point[cols][rows];
        this.distanceGrid = new int[cols][rows];

        int maxNodes = cols * rows * 8;
        this.queueX = new int[maxNodes];
        this.queueY = new int[maxNodes];

        for (int i = 0; i < cols; i++) {
            for (int j = 0; j < rows; j++) {
                flowGrid[i][j] = new Point(0, 0); // Pré-instanciado
            }
        }
    }

    public void updateField(GameMap map, Player player) {
        int targetCol = (int) (player.getX() + player.getWidth() / 2f) / GameMap.TILE_SIZE;
        int targetRow = (int) (player.getY() + player.getHeight() / 2f) / GameMap.TILE_SIZE;

        for (int i = 0; i < cols; i++) {
            for (int j = 0; j < rows; j++) {
                distanceGrid[i][j] = 999999;
            }
        }

        int head = 0, tail = 0;

        if(targetCol >= 0 && targetCol < cols && targetRow >= 0 && targetRow < rows) {
            distanceGrid[targetCol][targetRow] = 0;
            queueX[tail] = targetCol;
            queueY[tail] = targetRow;
            tail++;
        }

        int[][] dirs = {
                {0, -1, 10}, {0, 1, 10}, {-1, 0, 10}, {1, 0, 10},
                {-1, -1, 14}, {1, -1, 14}, {-1, 1, 14}, {1, 1, 14}
        };

        while (head < tail) {
            int cX = queueX[head];
            int cY = queueY[head];
            head++;

            int cCost = distanceGrid[cX][cY];

            for (int[] dir : dirs) {
                int nCol = cX + dir[0];
                int nRow = cY + dir[1];
                int moveCost = dir[2];

                if (nCol >= 0 && nCol < cols && nRow >= 0 && nRow < rows) {
                    boolean diagonalSegura = true;

                    // SOLUÇÃO DEFINITIVA DAS QUINAS:
                    // Se for movimento diagonal (custo 14), os dois blocos cardeais ao lado DEVEM estar livres.
                    if (moveCost == 14) {
                        boolean livreX = map.isFree((cX + dir[0]) * GameMap.TILE_SIZE, cY * GameMap.TILE_SIZE, GameMap.TILE_SIZE, GameMap.TILE_SIZE);
                        boolean livreY = map.isFree(cX * GameMap.TILE_SIZE, (cY + dir[1]) * GameMap.TILE_SIZE, GameMap.TILE_SIZE, GameMap.TILE_SIZE);
                        if (!livreX || !livreY) diagonalSegura = false;
                    }

                    // Se a diagonal for segura E o destino final estiver livre
                    if (diagonalSegura && map.isFree(nCol * GameMap.TILE_SIZE, nRow * GameMap.TILE_SIZE, GameMap.TILE_SIZE, GameMap.TILE_SIZE)) {
                        int newCost = cCost + moveCost;
                        if (newCost < distanceGrid[nCol][nRow]) {
                            distanceGrid[nCol][nRow] = newCost;
                            queueX[tail] = nCol;
                            queueY[tail] = nRow;
                            tail++;
                        }
                    }
                }
            }
        }

        for (int x = 0; x < cols; x++) {
            for (int y = 0; y < rows; y++) {
                if (distanceGrid[x][y] == 999999 || distanceGrid[x][y] == 0) {
                    flowGrid[x][y].setLocation(0, 0); // Sem dar NEW
                    continue;
                }

                int bestCost = distanceGrid[x][y];
                int bdx = 0, bdy = 0;

                for (int[] dir : dirs) {
                    int nCol = x + dir[0];
                    int nRow = y + dir[1];

                    if (nCol >= 0 && nCol < cols && nRow >= 0 && nRow < rows) {
                        if (distanceGrid[nCol][nRow] < bestCost) {
                            bestCost = distanceGrid[nCol][nRow];
                            bdx = dir[0];
                            bdy = dir[1];
                        }
                    }
                }
                flowGrid[x][y].setLocation(bdx, bdy);
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