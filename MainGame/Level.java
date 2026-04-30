package MainGame;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import Entities.*;
import Entities.Enemies.Boss;
import Entities.Enemies.Compters;
import Entities.Enemies.Enemy;
import Entities.Enemies.LanceGuard;
import Entities.Enemies.MetalChomp;
import Entities.Enemies.Thundorb;
import Entities.Items.AbstractItem;
import Entities.Items.DamageUp;
import Entities.Items.GunType;
import Entities.Items.HealthPacks.HealthPackType;
import Entities.Items.MovementUp;
import Entities.Player.Player;
import Factory.ItemFactory;
import ImageManager.SpriteSheetExtractor;

public class Level {

    public static final int TILE_SIZE = 96;

    private static final int SRC_TILE = 16;

    private static final int SRC_TILE_LARGE = 32;

    private static final int TOP_TILE_INDEX = 1;

    private char[][] tiles;
    private int mapWidth;
    private int mapHeight;

    private int playerSpawnX;
    private int playerSpawnY;

    private BufferedImage[] floorTopTiles;

    private BufferedImage groundTile;

    private BufferedImage stoneTile;

    private BufferedImage[] arenaBgTiles;
    private BufferedImage doorClosedTop;
    private BufferedImage doorClosedBottom;
    private BufferedImage doorOpenTop;
    private BufferedImage doorOpenBottom;

    private BufferedImage keyTile;

    private BufferedImage appearingBlock;

    private BufferedImage level2BgTile;

    private boolean keyCollected;

    private static class SpawnPoint {
        int x, y;
        char type;
        SpawnPoint(int x, int y, char type) { this.x = x; this.y = y; this.type = type; }
    }
    private List<SpawnPoint> enemySpawns;
    private List<SpawnPoint> itemSpawns;

    private List<int[]> arenaSpawnPoints;

    private Rectangle arenaBounds;

    private boolean qBlocksActive;

    private List<int[]> spikeEmitters;

    private int doorWorldX = -1;

    private BufferedImage spikeSprite;

    private boolean level2Mode;

    public Level() {
        enemySpawns = new ArrayList<>();
        itemSpawns  = new ArrayList<>();
        arenaSpawnPoints = new ArrayList<>();
        spikeEmitters    = new ArrayList<>();
        loadTileset();
        loadHazardSprites();
    }

    private void loadHazardSprites() {
        try {
            SpriteSheetExtractor ext = SpriteSheetExtractor.getInstance();
            BufferedImage sheet = ext.loadSpriteSheet("Spritesheets/16-bit-spike-Sheet.png");
            if (sheet != null) {

                spikeSprite = ext.extractSprite(sheet, 2 * SRC_TILE, 0, SRC_TILE, SRC_TILE);
            }
        } catch (Exception e) {
            System.out.println("Could not load spike sheet: " + e.getMessage());
        }
    }

    private void loadTileset() {
        SpriteSheetExtractor ext = SpriteSheetExtractor.getInstance();

        try {
            BufferedImage grass = ext.loadSpriteSheet("TileSets/Plains/Grassland_Terrain_47Tiles.png");
            if (grass != null) {
                int cols = grass.getWidth() / SRC_TILE;
                floorTopTiles = ext.extractRow(grass, 0, cols, SRC_TILE, SRC_TILE);
                groundTile = ext.extractSprite(grass, 3 * SRC_TILE, 1 * SRC_TILE, SRC_TILE, SRC_TILE);
            }
        } catch (Exception e) {
            System.out.println("Could not load grassland tileset: " + e.getMessage());
        }

        try {
            BufferedImage dungeon = ext.loadSpriteSheet("TileSets/Dungeon_Tile_Set.png");
            if (dungeon != null) {
                stoneTile = ext.extractSprite(dungeon, 1 * SRC_TILE, 1 * SRC_TILE, SRC_TILE, SRC_TILE);
                arenaBgTiles = new BufferedImage[] {
                    ext.extractSprite(dungeon, 12 * SRC_TILE, 1 * SRC_TILE, SRC_TILE, SRC_TILE),
                    ext.extractSprite(dungeon, 13 * SRC_TILE, 1 * SRC_TILE, SRC_TILE, SRC_TILE),
                    ext.extractSprite(dungeon, 12 * SRC_TILE, 2 * SRC_TILE, SRC_TILE, SRC_TILE),
                    ext.extractSprite(dungeon, 13 * SRC_TILE, 2 * SRC_TILE, SRC_TILE, SRC_TILE),
                };
                doorClosedTop    = ext.extractSprite(dungeon, 12 * SRC_TILE, 10 * SRC_TILE, SRC_TILE, SRC_TILE);
                doorClosedBottom = ext.extractSprite(dungeon, 12 * SRC_TILE, 11 * SRC_TILE, SRC_TILE, SRC_TILE);
                doorOpenTop      = ext.extractSprite(dungeon, 13 * SRC_TILE, 10 * SRC_TILE, SRC_TILE, SRC_TILE);
                doorOpenBottom   = ext.extractSprite(dungeon, 13 * SRC_TILE, 11 * SRC_TILE, SRC_TILE, SRC_TILE);
                keyTile          = ext.extractSprite(dungeon, 11 * SRC_TILE,  9 * SRC_TILE, SRC_TILE, SRC_TILE);

                level2BgTile     = ext.extractSprite(dungeon, 12 * SRC_TILE,  3 * SRC_TILE, SRC_TILE, SRC_TILE);
            }
        } catch (Exception e) {
            System.out.println("Could not load dungeon tileset: " + e.getMessage());
        }

        try {
            BufferedImage indust = ext.loadSpriteSheet("TileSets/Industrial/1_Industrial_Tileset_1B.png");
            if (indust != null) {
                appearingBlock = ext.extractSprite(indust,
                    1 * SRC_TILE_LARGE, 0 * SRC_TILE_LARGE,
                    SRC_TILE_LARGE, SRC_TILE_LARGE);
            }
        } catch (Exception e) {
            System.out.println("Could not load industrial tileset: " + e.getMessage());
        }
    }

    public void loadPathOne() {

        String[] map = {
            "W..............................................................................................................................................................................S",
            "W..............................................................................................................................................................................S",
            "W.................................................................................................................................HH...........................................S",
            "W.......................................................................................................................FFFFFFFFFFFFFFF.QQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQS",
            "W............................................................................................................FFFFFFFFFFF................S++++++++++++++++++++++++++++++++++++++S",
            "W..............................................................................................FFFFFFFFFFFFFF...........................S++++++++++++++++++++++++++++++++++++++S",
            "W........................................................................FFFFFFFFFFFFFFFFFFFFFF.........................................SSSSSSS++++++++++++++++++++++++++++++++S",
            "W............................FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF...............................................................S++++++SSSSSS++++++++++++++++SSSSSSS+++S",
            "W......................FFFFF............................................................................................................S+SSS+++++++++++++++++SSSSS++++++++++++S",
            "W...............FFFFFF..................................................................................................................S+++++++++++++SSSSSS+++++++++++++SSSS++S",
            "W........FFFFFF.........................................................................................................................S++++SSSSSSS+++++++++++++++++SSSS++++++S",
            "W.................FFFFFFF...............................................................................................................S++++++++++++++++SSSSSSSSSSS+++++++++++S",
            "W........FFFFFF...............................L.........................................................................................SSSSS++++++SSSS+++++++++++++++++++SSSS+S",
            "WFFFFFF...................J.L.......FFFFFFFFFF..........................................................................................S+++++SSSS+++++++SSSSS++++++++SSSS+++++S",
            "W.......FFFFFFFFFFFF......FFFFFFFF...............FFFFFFFFFFFFFF.........................................................................S+SSS+++++++SSS+++++++++SSSS+++++++++++S",
            "W.....W............................FFFFFFFFFFFF.................FFFFFFFFFF..............................................................S+++++SSSS+++++++SSSSS+++++++++SSSSSS++S",
            "W.............................................................................FFFFFFFFFFF...............................................SSSSSSS++++SSSSS+++++++SSSSSS++++++++++S",
            "W..K..D................................................C..C..C...........FFF............................................................S++++++SS+++++++++SSS++++++++++SSSSS+++S",
            "WWWWWWW...........................C..........C..................FFFFFFFF................................................................S+SSS++++++SSSSS++++++++SSSS+++++++++++S",
            "W.......................................................FFFFFF..........................................................................S++++SSSSSS++++++SSSSS+++++++SSSSSSS+++S",
            "W...................................................FFF.................................................................................SSSS++++++++SSS++++++++SSSSS+++++++++++S",
            "W...................................................T......FFFFFF.......................................................................S++++SSSSSS++++++SSSSS++++++++SSSSSSSS+S",
            "W............................M.........FFFFFFF..FFFFFFF.FF.......T......................................................................SSSS+++++++++SSS++++++++SSSS+++++++++++S",
            "W..P.......................FFFFFF.........................FFFFFF........................................................................S++++SSSS+++++++++SSSS+++++++++SSSS++++S",
            "FFFFFFFFFFFFFFFFFFFFFFFFFFF........FFFFFFFF...FFFFFFFFFF..FF................................S...S.......................................S++++R++++++++R++++++++R+++++++++R+++++D",
            "GGGGGGGGGGGGGGGGGGGGGGGGGGG........GGGGGGGG...GGGGGGGGGG..GG...............................S...S........................................SSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSS",
        };
        loadMap(map);
    }

    public void loadLevel2() {

        String[] map = {
            "GGGGGGGGGGGGGGGGGGGGGGGGGGG********GGGGGGGG***GGGGGGGGGG**GG***********",
            "S**********************************GGGGGGGG***GGGGGGGGGG**GG***********",
            "S**********************************SSSSSSSS***SSSSSSSSSS**SS***********",
            "S**P*******************************************************SSSSSS*******",
            "SSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSS***********SSSSSSSSSSSSSSSSSSS*****",
            "SSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSS************SSSSSSSSSSSSSSSSSSS",
            "S********************************************SSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSS",
            "SSSSSSSSSSSSSS*************SSSSSSSSSS********SSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSS++++++++++VVV++++++VVVV++++++++VVV+++++VV++V++++++++VVVVVVVVVVVV+++++++++++S",
            "S******SSSSSSSSSSSSSSSSSSSSSSSSSSSSSS********SSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSS+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++S",
            "S********************************************SSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSS+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++S",
            "S************************************SSS*****SSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSS+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++S",
            "S**********SSSSSSS***SSSSS***SSSSSS**********SSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSS+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++S",
            "SSSSSSSS**************************************************************************Q+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++S",
            "S*********************************************************************************Q+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++S",
            "S********SSSSSSSS***SSSSSSSSS***SSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSS++++++++++++++++++++++++++++++++SSSSSSSSS++++++++++++++++++++++++++++++++++S",
            "S********************************************SSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSS+++++R++++++++++++++++++++++++++I+++++++++++++++++++++++R+++++SS",
            "SSSSSSSSS************************************SSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSS",
            "S***********SSSSSSSSS***SSSSSSSSSSSSS********SSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSS",
            "S********************************************SSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSS",
            "SSSSSSSSSSS**SSSSSSSS************************SSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSS",
            "SSSSSSSSSSS**SSSSSSSS***SSSSSSSSSSSSS********SSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSS",
            "SSSSSSSSSSS**SSSSSSSS***SSSSSSSSSSSSS********SSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSS",
            "S***********************SSSSSSSSSSSSS********SSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSS",
            "SSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSS",
            "SSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSS",
            "SSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSS",
            "SSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSS",
            "SSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSS",
            "SSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSS",
            "SSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSS",
            "SSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSS",
        };
        loadMap(map);
        level2Mode = true;
    }

    private void loadMap(String[] map) {
        mapHeight = map.length;
        mapWidth = 0;
        for (String row : map) {
            if (row.length() > mapWidth) mapWidth = row.length();
        }

        tiles = new char[mapHeight][mapWidth];
        enemySpawns.clear();
        itemSpawns.clear();
        arenaSpawnPoints.clear();
        spikeEmitters.clear();
        arenaBounds = null;
        qBlocksActive = false;
        keyCollected = false;
        doorWorldX = -1;
        level2Mode = false;

        int aMinX = Integer.MAX_VALUE, aMinY = Integer.MAX_VALUE;
        int aMaxX = Integer.MIN_VALUE, aMaxY = Integer.MIN_VALUE;
        boolean sawArena = false;

        for (int r = 0; r < mapHeight; r++) {
            for (int c = 0; c < mapWidth; c++) {
                char ch;
                if (c < map[r].length()) {
                    ch = map[r].charAt(c);
                } else {
                    ch = '.';
                }
                switch (ch) {
                    case 'P':
                        playerSpawnX = c * TILE_SIZE;
                        playerSpawnY = r * TILE_SIZE;
                        tiles[r][c] = '.';
                        break;
                    case 'C': case 'L': case 'M': case 'T': case 'I':
                        enemySpawns.add(new SpawnPoint(c * TILE_SIZE, r * TILE_SIZE, ch));
                        tiles[r][c] = '.';
                        break;
                    case 'h': case 'H': case 's': case 'J':
                    case 'A': case 'O': case 'B': case 'K':
                        itemSpawns.add(new SpawnPoint(c * TILE_SIZE, r * TILE_SIZE, ch));
                        tiles[r][c] = '.';
                        break;
                    case 'R':

                        arenaSpawnPoints.add(new int[] { c * TILE_SIZE, r * TILE_SIZE });
                        tiles[r][c] = '+';
                        sawArena = true;
                        if (c * TILE_SIZE < aMinX) aMinX = c * TILE_SIZE;
                        if (r * TILE_SIZE < aMinY) aMinY = r * TILE_SIZE;
                        if ((c + 1) * TILE_SIZE > aMaxX) aMaxX = (c + 1) * TILE_SIZE;
                        if ((r + 1) * TILE_SIZE > aMaxY) aMaxY = (r + 1) * TILE_SIZE;
                        break;
                    case '+':
                        tiles[r][c] = ch;
                        sawArena = true;
                        if (c * TILE_SIZE < aMinX) aMinX = c * TILE_SIZE;
                        if (r * TILE_SIZE < aMinY) aMinY = r * TILE_SIZE;
                        if ((c + 1) * TILE_SIZE > aMaxX) aMaxX = (c + 1) * TILE_SIZE;
                        if ((r + 1) * TILE_SIZE > aMaxY) aMaxY = (r + 1) * TILE_SIZE;
                        break;
                    case 'V':

                        spikeEmitters.add(new int[] { c * TILE_SIZE, (r + 1) * TILE_SIZE });
                        tiles[r][c] = '.';
                        break;
                    case 'F': case 'W': case 'E':
                    case 'S': case 'G': case 'Q': case '*':
                        tiles[r][c] = ch;
                        break;
                    case 'D':
                        tiles[r][c] = ch;
                        if (c * TILE_SIZE > doorWorldX) doorWorldX = c * TILE_SIZE;
                        break;
                    default:
                        tiles[r][c] = '.';
                }
            }
        }

        if (sawArena) {
            arenaBounds = new Rectangle(aMinX, aMinY, aMaxX - aMinX, aMaxY - aMinY);
        }
    }

    public boolean isSolid(int worldX, int worldY) {
        int tx = worldX / TILE_SIZE;
        int ty = worldY / TILE_SIZE;
        if (tx < 0) return true;
        if (ty < 0) return false;
        if (tx >= mapWidth || ty >= mapHeight) return false;
        char t = tiles[ty][tx];
        switch (t) {
            case 'F': case 'W': case 'E':
            case 'S': case 'G':
                return true;
            case 'Q':
                return qBlocksActive;
            case 'D':
                return !keyCollected;
            default:
                return false;
        }
    }

    public void draw(Graphics2D g2) {
        for (int r = 0; r < mapHeight; r++) {
            for (int c = 0; c < mapWidth; c++) {
                char t = tiles[r][c];
                int px = c * TILE_SIZE;
                int py = r * TILE_SIZE;
                switch (t) {
                    case 'F': case 'W':
                        drawFloorOrWall(g2, r, c, px, py);
                        break;
                    case 'S':
                        drawScaled(g2, stoneTile, px, py);
                        break;
                    case 'G':
                        drawScaled(g2, groundTile, px, py);
                        break;
                    case 'Q':
                        if (qBlocksActive) drawScaled(g2, appearingBlock, px, py);
                        break;
                    case '+':
                        if (arenaBgTiles != null && arenaBgTiles.length == 4) {
                            int idx = ((r & 1) << 1) | (c & 1);
                            drawScaled(g2, arenaBgTiles[idx], px, py);
                        }
                        break;
                    case 'D':
                        drawDoor(g2, px, py);
                        break;
                    case '*':

                        drawScaled(g2, level2BgTile, px, py);
                        break;
                    case 'E':
                        g2.setColor(new Color(200, 50, 50, 180));
                        g2.fillRect(px, py, TILE_SIZE, TILE_SIZE);
                        break;
                    default:

                        if (level2Mode) drawScaled(g2, level2BgTile, px, py);
                        break;
                }
            }
        }
    }

    private void drawFloorOrWall(Graphics2D g2, int r, int c, int px, int py) {
        char above;
        if (r > 0) {
            above = tiles[r - 1][c];
        } else {
            above = '.';
        }
        boolean topExposed = (above != 'F' && above != 'W');
        if (topExposed && floorTopTiles != null && floorTopTiles.length > 0) {
            BufferedImage top = floorTopTiles[TOP_TILE_INDEX % floorTopTiles.length];
            g2.drawImage(top, px, py, TILE_SIZE, TILE_SIZE, null);
        } else {
            g2.setColor(new Color(100, 100, 100));
            g2.fillRect(px, py, TILE_SIZE, TILE_SIZE);
            g2.setColor(new Color(80, 80, 80));
            g2.drawRect(px, py, TILE_SIZE, TILE_SIZE);
        }
    }

    private void drawScaled(Graphics2D g2, BufferedImage tile, int px, int py) {
        if (tile != null) {
            g2.drawImage(tile, px, py, TILE_SIZE, TILE_SIZE, null);
        } else {
            g2.setColor(Color.MAGENTA);
            g2.fillRect(px, py, TILE_SIZE, TILE_SIZE);
        }
    }

    private void drawDoor(Graphics2D g2, int px, int py) {
        BufferedImage top;
        BufferedImage bottom;
        if (keyCollected) {
            top = doorOpenTop;
            bottom = doorOpenBottom;
        } else {
            top = doorClosedTop;
            bottom = doorClosedBottom;
        }
        if (top != null) {
            g2.drawImage(top, px, py - TILE_SIZE, TILE_SIZE, TILE_SIZE, null);
        }
        if (bottom != null) {
            g2.drawImage(bottom, px, py, TILE_SIZE, TILE_SIZE, null);
        }
    }

    public BufferedImage getKeySprite() { return keyTile; }

    public void collectKey() { keyCollected = true; }
    public boolean hasKey()  { return keyCollected; }

    public boolean isLevel2Mode() { return level2Mode; }

    public boolean hasArena() { return arenaBounds != null; }
    public Rectangle getArenaBounds() { return arenaBounds; }
    public List<int[]> getArenaSpawnPoints() { return arenaSpawnPoints; }
    public boolean areQBlocksActive() { return qBlocksActive; }

    public List<int[]> getSpikeEmitters() { return spikeEmitters; }

    public int getDoorWorldX() { return doorWorldX; }

    public BufferedImage getSpikeSprite() { return spikeSprite; }

    public void activateQBlocks() { qBlocksActive = true; }

    public boolean isPlayerInArena(Rectangle2D bbox) {
        if (arenaBounds == null || bbox == null) return false;
        if (!arenaBounds.intersects(bbox)) return false;
        int minTx = Math.max(0, (int) bbox.getMinX() / TILE_SIZE);
        int maxTx = Math.min(mapWidth - 1, (int) bbox.getMaxX() / TILE_SIZE);
        int minTy = Math.max(0, (int) bbox.getMinY() / TILE_SIZE);
        int maxTy = Math.min(mapHeight - 1, (int) bbox.getMaxY() / TILE_SIZE);
        for (int r = minTy; r <= maxTy; r++) {
            for (int c = minTx; c <= maxTx; c++) {
                if (tiles[r][c] == '+') return true;
            }
        }
        return false;
    }

    public List<Enemy> createEnemies(Player player) {
        List<Enemy> enemies = new ArrayList<>();
        for (SpawnPoint sp : enemySpawns) {
            switch (sp.type) {
                case 'C': enemies.add(new Compters(sp.x, sp.y, player, this));    break;
                case 'L': enemies.add(new LanceGuard(sp.x, sp.y, player, this)); break;
                case 'M': enemies.add(new MetalChomp(sp.x, sp.y, player, this)); break;
                case 'T': enemies.add(new Thundorb(sp.x, sp.y, player, this));   break;
                case 'I': {

                    int bx = sp.x;
                    int by = sp.y + TILE_SIZE - Boss.DRAW_HEIGHT;
                    enemies.add(new Boss(bx, by, player, this));
                    break;
                }
            }
        }
        return enemies;
    }

    public List<AbstractItem> createItems() {
        List<AbstractItem> items = new ArrayList<>();
        ItemFactory factory = new ItemFactory();
        for (SpawnPoint sp : itemSpawns) {
            int ix = sp.x + (TILE_SIZE - 32) / 2;
            int iy = sp.y +  TILE_SIZE - 32;
            switch (sp.type) {
                case 'h': items.add(factory.createHealthPack(ix, iy, HealthPackType.SMALL));        break;
                case 'H': items.add(factory.createHealthPack(ix, iy, HealthPackType.LARGE));        break;
                case 's': items.add(factory.createMovementUp(ix, iy, MovementUp.BoostType.SPEED, 2)); break;
                case 'J': items.add(factory.createMovementUp(ix, iy, MovementUp.BoostType.JUMP,  4)); break;
                case 'A': items.add(factory.createDamageUp(ix, iy, DamageUp.Tier.MEDIUM));          break;
                case 'O': items.add(factory.createGunType(ix, iy, GunType.Variant.CHARGE_SHOT));    break;
                case 'B': items.add(factory.createGunType(ix, iy, GunType.Variant.TRIPLE_SHOT));    break;
                case 'K': items.add(factory.createKey(ix, iy, this));                                break;
            }
        }
        return items;
    }

    public void clearExitBlocks() {
        for (int r = 0; r < mapHeight; r++) {
            for (int c = 0; c < mapWidth; c++) {
                if (tiles[r][c] == 'E') tiles[r][c] = '.';
            }
        }
    }

    public int getPlayerSpawnX()  { return playerSpawnX; }
    public int getPlayerSpawnY()  { return playerSpawnY; }
    public int getMapWidth()      { return mapWidth; }
    public int getMapHeight()     { return mapHeight; }
    public int getMapPixelWidth() { return mapWidth * TILE_SIZE; }
    public int getMapPixelHeight(){ return mapHeight * TILE_SIZE; }

}
