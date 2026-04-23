package MainGame;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import Entities.*;
import Entities.Enemies.Compters;
import Entities.Enemies.Enemy;
import Entities.Enemies.LanceGuard;
import Entities.Enemies.MetalChomp;
import Entities.Enemies.Thundorb;
import Entities.Player.Player;
import ImageManager.SpriteSheetExtractor;

public class Level {

    public static final int TILE_SIZE = 96;

    /** Source tile size in the Grassland tileset (pixels). */
    private static final int SRC_TILE = 16;
    /** Which tile from row 0 to use as the default flat top. */
    private static final int TOP_TILE_INDEX = 1;

    private char[][] tiles;
    private int mapWidth;   // in tiles
    private int mapHeight;  // in tiles

    private int playerSpawnX;
    private int playerSpawnY;

    /** First-row tiles from Grassland_Terrain_47Tiles.png (12 tiles, used as the top layer of floors). */
    private BufferedImage[] floorTopTiles;

    // Spawn descriptors collected during map load
    private static class SpawnPoint {
        int x, y;
        char type;
        SpawnPoint(int x, int y, char type) { this.x = x; this.y = y; this.type = type; }
    }
    private List<SpawnPoint> enemySpawns;

    public Level() {
        enemySpawns = new ArrayList<>();
        loadTileset();
    }

    /** Loads the Grassland 47-tile tileset and slices its first row of 16x16 tiles. */
    private void loadTileset() {
        try {
            SpriteSheetExtractor ext = SpriteSheetExtractor.getInstance();
            BufferedImage sheet = ext.loadSpriteSheet("TileSets/Plains/Grassland_Terrain_47Tiles.png");
            if (sheet == null) return;
            int cols = sheet.getWidth() / SRC_TILE; // 192 / 16 = 12
            floorTopTiles = ext.extractRow(sheet, 0, cols, SRC_TILE, SRC_TILE);
        } catch (Exception e) {
            System.out.println("Could not load grassland tileset: " + e.getMessage());
        }
    }

    // -----------------------------------------------------------------------
    //  Level data
    // -----------------------------------------------------------------------

    /** Main platforming level (Path One from map.md). */
    public void loadPathOne() {
        // Legend:  F = floor / solid   W = wall   P = player spawn
        //          C = Compters   L = LanceGuard   M = MetalChomp   T = Thundorb
        //          . = empty / air
        //          S = Stone, for the underground part of the level(solid and a type of floor but visually distinct from F)
        // * = exit block (removed when all arena enemies are defeated)
        //r - Random Enemy Spawn
        // + = Arena Area (This is still empty space, however this is a signifying empty space that will identify that the player has stepped into an arena, when entering this area, the exit block will activate, closing off until all enemies in all waves in the arena block are destroyed)
        // It should be noted that the arena will not be visually distinct from the rest of the level and as soon as
        String[] map = {
            "W......................................................................",  //  0
            "W..P....................................................................",  //  1
            "W.......................................L....................................................",  //  2
            "W...........................L.......FFFFFFFFFF..........T....................................",  //  3
            "W........................FFFFFFFF...............FFFFFFFFFFFFFF..............................",  //  4
            "W..................................FFFFFFFFFFFF.................FFFFFFFFFF..................",  //  5
            "W............................................................................FFFFFFFFFFF....",  //  6
            "W.....................................................C..C..C...........FFF...",  //  7
            "W................................C..........C..................FFFFFFFF..",  //  8
            "W.......................................................FFFFFF.........",  //  9
            "W...................................................FFF................",  // 10
            "W...................................................T......FFFFFF......",  // 11
            "W............................M.........FFFFFFF..FFFFFFF.FF.......T.......",  // 12
            "W.................T..M..L...FFFFFF.........................FFFFFF.....",  // 13
            "FFFFFFFFFFFFFFFFFFFFFFFFFFF........FFFFFFFF...FFFFFFFFFF..FF..........",  // 14
            "GGGGGGGGGGGGGGGGGGGGGGGGGGG........GGGGGGGG...GGGGGGGGGG..GG..........",  // 15
            "GGGGGGGGGGGGGGGGGGGGGGGGGGG........GGGGGGGG...GGGGGGGGGG..GG............",  // 16
            "GGGGGGGGGGGGGGGGGGGGGGGGGGG........GGGGGGGG...GGGGGGGGGG..GG...........",  // 17
            "S..................................GGGGGGGG...GGGGGGGGGG..GG...........",  // 18
            "S..................................SSSSSSSS...SSSSSSSSSS..SS...........",  // 19
            "S.........................................................SSSSSS.......",  //  20
            "SSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSS...........SSSSSSSSSSSSSSSSSSS.....",  //  21
            "SSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSS............SSSSSSSSSSSSSSSSSSS",  //  22                                      Arena 1 Is below here
            "S............................................SSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSS",  //  23
            "SSSSSSSSSSSSSS.............SSSSSSSSSS........SSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSS+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++S",  //  24
            "S......SSSSSSSSSSSSSSSSSSSSSSSSSSSSSS........SSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSS+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++S",  //  25
            "S............................................SSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSS+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++S",  //  26
            "S....................................SSS.....SSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSS+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++S",  //  27
            "S..........SSSSSSS...SSSSS...SSSSSS..........SSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSS++++++SSSSSSSS++++SSSSSSSSS++++SSSSSSSS++++SSSSSSSS++++++++++++SSSSSSSSS+++S",  //  28
            "SSSSSSSS..........................................................................*+++++++++++++++++++++++++++++++++++++++++++++++++++++SSSSSSSS++++++++++++++S",  //  29
            "S.................................................................................*+++++++++++++++++++++++++++++++++++++++++++SSSSSSSSS+++++++++++++++++++++++S",  // 30
            "S........SSSSSSSS...SSSSSSSSS...SSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSS++++++++++++++++++++++++++++++++SSSSSSSSS++++++++++++++++++++++++++++++++++S",  // 31
            "S............................................SSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSS++++++++++++SSSSSSSS++++++++++SSSSSSSSS++++++++++++++++++++++++S",  // 32
            "SSSSSSSSS....................................SSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSS+++++++++++++SSSSSSSSS+++++++++++++++++++++++++++++++++++++++++++++++++++++S",  // 33
            "S...........SSSSSSSSS...SSSSSSSSSSSSS........SSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSS++++++++++++++++++++++++++++++++++++++++++++++++++++++++SSSSSSSSSSSSSSS++++S",  // 34
            "S............................................SSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSS++++++++++++++++++++++++++++++++++SSSSSSSS+++SSSSSSSSS+++++++++++++++++++++S",  // 35
            "SSSSSSSSSSS..SSSSSSSS........................SSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSS++++++++++++++++++SSSSSSSSSSSSSS+++++++++++++++++++++++++++++++++++++++++++S",  // 36
            "SSSSSSSSSSS..SSSSSSSS...SSSSSSSSSSSSS........SSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSS+++++SSSSSSSSSSS+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++S",  // 37
            "SSSSSSSSSSS..SSSSSSSS...SSSSSSSSSSSSS........SSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSS+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++S",  // 38
            "S.......................SSSSSSSSSSSSS........SSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSS++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++S",
            "SSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSS+++++++++++++++++SSSSSSSSSSSS++++++++++++++++++++++++++++++++++++++++++++++S",  // 39
            "SSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSS++++++++++++++++++++++++++++++++SSSSSSSSS++++++++++++++++++++++++++++++++++S",  // 40
            "SSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSS+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++S",  // 41
            "SSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSS++++++++++++SSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSS++++++++++++++++++S",  // 42
            "SSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSS+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++S",  // 43
            "SSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSS++++++++++++++++++++++++++++++++++++++++++++++++++SSSSSSSSS++++++S",  // 44
            "SSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSS+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++S",  //  45
            "SSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSS",
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
                    boolean topExposed = (r == 0) || tiles[r - 1][c] != 'F' && tiles[r - 1][c] != 'W';
                    if (topExposed && floorTopTiles != null && floorTopTiles.length > 0) {
                        // Draw the chosen top-layer tile from row 0 of the tileset, scaled up.
                        BufferedImage top = floorTopTiles[TOP_TILE_INDEX % floorTopTiles.length];
                        g2.drawImage(top, px, py, TILE_SIZE, TILE_SIZE, null);
                    } else {
                        // Buried floor / wall body — plain fill until body tiles are wired in.
                        g2.setColor(new Color(100, 100, 100));
                        g2.fillRect(px, py, TILE_SIZE, TILE_SIZE);
                        g2.setColor(new Color(80, 80, 80));
                        g2.drawRect(px, py, TILE_SIZE, TILE_SIZE);
                    }
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
