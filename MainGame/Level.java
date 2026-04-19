package MainGame;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;
import Entities.*;

public class Level {

    public static final int TILE_SIZE = 96;

    private char[][] tiles;
    private int mapWidth;   // in tiles
    private int mapHeight;  // in tiles

    private int playerSpawnX;
    private int playerSpawnY;

    // Spawn descriptors collected during map load
    private static class SpawnPoint {
        int x, y;
        char type;
        SpawnPoint(int x, int y, char type) { this.x = x; this.y = y; this.type = type; }
    }
    private List<SpawnPoint> enemySpawns;

    public Level() {
        enemySpawns = new ArrayList<>();
    }

    // -----------------------------------------------------------------------
    //  Level data
    // -----------------------------------------------------------------------

    /** Main platforming level (Path One from map.md). */
    public void loadPathOne() {
        // Legend:  F = floor / solid   W = wall   P = player spawn
        //          C = Compters   L = LanceGuard   M = MetalChomp   T = Thundorb
        //          . = empty / air
        String[] map = {
            "W......................................................................",  //  0
            "W......................................................................",  //  1
            "W......................................................................",  //  2
            "W......................................................................",  //  3
            "W......................................................................",  //  4
            "W......................................................................",  //  5
            "W.......................................................FFFFFFFFFFF....",  //  6
            "W.....................................................C..C..C..........",  //  7
            "W................................C..........C..................FFFFFFFF..",  //  8
            "W.......................................................FFFFFF.........",  //  9
            "W.....................................................FFFFF............",  // 10
            "W...................................................T......FFFFFF......",  // 11
            "W............................M.........FFFFFFF..FFFFFFF.FFFFFF.........",  // 12
            "W..P...............T..M..L...FFFFFF.........................FFFFFF.....",  // 13
            "FFFFFFFFFFFFFFFFFFFFFFFFFFF........FFFFFFFF...FFFFFFFFFF..FFFFFFFF.T..",  // 14
            "FFFFFFFFFFFFFFFFFFF......................................................",  // 15
            "......................................................................",  // 16
            "......................................................................",  // 17
            "......................................................................",  // 18
            "......................................................................",  // 19
        };
        loadMap(map);
    }

    /** Enclosed arena that locks until all enemies are defeated. */
    public void loadArenaA1() {
        // E = exit block (removed when all enemies are dead)
        String[] map = {
            "FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF",  //  0
            "FF....................................FF",  //  1
            "FF................P...................FF",  //  2
            "FF....................................FF",  //  3
            "FF......C........C........C...........FF",  //  4
            "FF....................................FF",  //  5
            "FF...T..........T..........T..........FF",  //  6
            "FF....................................FF",  //  7
            "FF....................................FF",  //  8
            "FF....................................FF",  //  9
            "FF....................................FF",  // 10
            "FF......FFFFFF.......FFFFFF............FF",  // 11
            "FF..............FFFFFF.......FFFFFF....FF",  // 12
            "FF....FFFFFF...........FFFFFF..........FF",  // 13
            "FF..........FFFFFF.........FFFFFF......EE",  // 14
            "FF....FFFFFF.....................FFFFFFEE",  // 15
            "FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF",  // 16
            "FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF",  // 17
        };
        loadMap(map);
    }

    // -----------------------------------------------------------------------
    //  Map loading
    // -----------------------------------------------------------------------

    private void loadMap(String[] map) {
        mapHeight = map.length;
        mapWidth = 0;
        for (String row : map) {
            if (row.length() > mapWidth) mapWidth = row.length();
        }

        tiles = new char[mapHeight][mapWidth];
        enemySpawns.clear();

        for (int r = 0; r < mapHeight; r++) {
            for (int c = 0; c < mapWidth; c++) {
                char ch = c < map[r].length() ? map[r].charAt(c) : '.';
                switch (ch) {
                    case 'P':
                        playerSpawnX = c * TILE_SIZE;
                        playerSpawnY = r * TILE_SIZE;
                        tiles[r][c] = '.';
                        break;
                    case 'C': case 'L': case 'M': case 'T':
                        enemySpawns.add(new SpawnPoint(c * TILE_SIZE, r * TILE_SIZE, ch));
                        tiles[r][c] = '.';
                        break;
                    case 'F': case 'W': case 'E':
                        tiles[r][c] = ch;
                        break;
                    default:
                        tiles[r][c] = '.';
                }
            }
        }
    }

    // -----------------------------------------------------------------------
    //  Queries
    // -----------------------------------------------------------------------

    public boolean isSolid(int worldX, int worldY) {
        int tx = worldX / TILE_SIZE;
        int ty = worldY / TILE_SIZE;
        if (tx < 0) return true;   // left edge
        if (ty < 0) return false;  // sky
        if (tx >= mapWidth || ty >= mapHeight) return false;
        char t = tiles[ty][tx];
        return t == 'F' || t == 'W' || t == 'E';
    }

    // -----------------------------------------------------------------------
    //  Drawing
    // -----------------------------------------------------------------------

    public void draw(Graphics2D g2) {
        for (int r = 0; r < mapHeight; r++) {
            for (int c = 0; c < mapWidth; c++) {
                char t = tiles[r][c];
                int px = c * TILE_SIZE;
                int py = r * TILE_SIZE;
                if (t == 'F' || t == 'W') {
                    g2.setColor(new Color(100, 100, 100));
                    g2.fillRect(px, py, TILE_SIZE, TILE_SIZE);
                    g2.setColor(new Color(80, 80, 80));
                    g2.drawRect(px, py, TILE_SIZE, TILE_SIZE);
                } else if (t == 'E') {
                    g2.setColor(new Color(200, 50, 50, 180));
                    g2.fillRect(px, py, TILE_SIZE, TILE_SIZE);
                }
            }
        }
    }

    // -----------------------------------------------------------------------
    //  Entity spawning
    // -----------------------------------------------------------------------

    public List<Enemy> createEnemies(Player player) {
        List<Enemy> enemies = new ArrayList<>();
        for (SpawnPoint sp : enemySpawns) {
            switch (sp.type) {
                case 'C': enemies.add(new Compters(sp.x, sp.y, player, this));    break;
                case 'L': enemies.add(new LanceGuard(sp.x, sp.y, player, this)); break;
                case 'M': enemies.add(new MetalChomp(sp.x, sp.y, player, this)); break;
                case 'T': enemies.add(new Thundorb(sp.x, sp.y, player, this));   break;
            }
        }
        return enemies;
    }

    /** Remove exit blocks (call when all arena enemies are defeated). */
    public void clearExitBlocks() {
        for (int r = 0; r < mapHeight; r++) {
            for (int c = 0; c < mapWidth; c++) {
                if (tiles[r][c] == 'E') tiles[r][c] = '.';
            }
        }
    }

    // -----------------------------------------------------------------------
    //  Accessors
    // -----------------------------------------------------------------------

    public int getPlayerSpawnX()  { return playerSpawnX; }
    public int getPlayerSpawnY()  { return playerSpawnY; }
    public int getMapWidth()      { return mapWidth; }
    public int getMapHeight()     { return mapHeight; }
    public int getMapPixelWidth() { return mapWidth * TILE_SIZE; }
    public int getMapPixelHeight(){ return mapHeight * TILE_SIZE; }
}
