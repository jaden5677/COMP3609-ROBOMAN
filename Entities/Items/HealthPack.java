package Entities.Items;

/**
 * Convenience alias so new code can use the singular name "HealthPack"
 * while existing references to {@link HealthPacks} keep working.
 */
public class HealthPack extends HealthPacks {
    public HealthPack(int x, int y, String imagePath, HealthPackType type) {
        super(x, y, imagePath, type);
    }
}
