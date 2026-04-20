package Entities;

import Entities.Player;
public class HealthPacks extends AbstractItem {
    public enum HealthPackType {
        SMALL,
        MEDIUM,
        LARGE;
    }
    public HealthPackType healthPackType;
    int healAmount;
    public HealthPacks(int x, int y, String imagePath, HealthPackType healthPackType) {
        super(x, y, imagePath, ItemType.HealthPack);
        this.healthPackType = healthPackType;
        setHealAmount();
    }
    
    public void setHealAmount() {
        switch (healthPackType) {
            case SMALL: healAmount = 15; break;
            case MEDIUM: healAmount = 30; break;
            case LARGE: healAmount = 45; break;
        }
    }

    public int getHealAmount() {
        return healAmount;
    }
    
    public void applyToPlayer(Player player) {
        if (isVisible) {
            player.heal(healAmount);
            isVisible = false; // Consume the health pack
        }
    }

}
