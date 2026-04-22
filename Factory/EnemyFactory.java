package Factory;

import Entities.Enemies.Compters;
import Entities.Enemies.Enemy;
import Entities.Enemies.LanceGuard;
import Entities.Enemies.MetalChomp;
import Entities.Enemies.Thundorb;
import Entities.Player.Player;
import MainGame.Level;

/**
 * Builds enemies on demand. Needs the player + level so spawned enemies
 * have the references they need to chase / shoot / collide.
 */
public class EnemyFactory extends AbstractFactory<Enemy, EnemyFactory.EnemyKind> {

    public enum EnemyKind { COMPTERS, COMPTERS_ALT, LANCE_GUARD, METAL_CHOMP, THUNDORB }

    private final Player player;
    private final Level level;

    public EnemyFactory(Player player, Level level) {
        this.player = player;
        this.level  = level;
    }

    @Override
    public Enemy create(EnemyKind kind, int x, int y) {
        if (kind == null) {
            throw new IllegalArgumentException("EnemyKind must not be null");
        }
        switch (kind) {
            case COMPTERS:    return new Compters(x, y, player, level);
            case COMPTERS_ALT:return new Compters(x, y, player, level, Compters.Variant.ALT);
            case LANCE_GUARD: return new LanceGuard(x, y, player, level);
            case METAL_CHOMP: return new MetalChomp(x, y, player, level);
            case THUNDORB:    return new Thundorb(x, y, player, level);
            default:
                throw new IllegalArgumentException("Unknown enemy kind: " + kind);
        }
    }

    /** Convenience for callers that loaded a map with enemy chars (C/L/M/T). */
    public Enemy createFromChar(char c, int x, int y) {
        switch (c) {
            case 'C': return create(EnemyKind.COMPTERS,    x, y);
            case 'c': return create(EnemyKind.COMPTERS_ALT,x, y);
            case 'L': return create(EnemyKind.LANCE_GUARD, x, y);
            case 'M': return create(EnemyKind.METAL_CHOMP, x, y);
            case 'T': return create(EnemyKind.THUNDORB,    x, y);
            default:  return null;
        }
    }
}
