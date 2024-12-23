package domain.model.entity;

import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;

public class Monster extends Entity {
    public enum Type {
        FIGHTER,
        WIZARD,
        ARCHER
    }

    private Type monsterType;
    private BufferedImage image;
    private long lastAttackTime = 0;
    private static final long ARCHER_ATTACK_COOLDOWN = 1000; // 1 second in milliseconds
    private static final int ARCHER_ATTACK_RANGE = 4; // 4 tiles range

    public Monster(Type type, int x, int y) {
        this.monsterType = type;
        this.x = x;
        this.y = y;
        loadImage();
    }

    private void loadImage() {
        try {
            String imagePath = switch (monsterType) {
                case FIGHTER -> "/monsters/fighter.png";
                case WIZARD -> "/monsters/wizard.png";
                case ARCHER -> "/monsters/archer.png";
            };
            image = ImageIO.read(getClass().getResourceAsStream(imagePath));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public BufferedImage getImage() {
        return image;
    }

    public Type getType() {
        return monsterType;
    }

    /**
     * Checks if this monster can attack based on its type and cooldown.
     * 
     * @return true if the monster can attack, false otherwise
     */
    public boolean canAttack() {
        if (monsterType != Type.ARCHER) {
            return false;
        }
        long currentTime = System.currentTimeMillis();
        return currentTime - lastAttackTime >= ARCHER_ATTACK_COOLDOWN;
    }

    /**
     * Marks this monster as having just attacked, starting its cooldown.
     */
    public void setAttackCooldown() {
        lastAttackTime = System.currentTimeMillis();
    }

    /**
     * Gets the attack range for this monster.
     * 
     * @return The number of tiles this monster can attack from
     */
    public int getAttackRange() {
        return monsterType == Type.ARCHER ? ARCHER_ATTACK_RANGE : 0;
    }
}