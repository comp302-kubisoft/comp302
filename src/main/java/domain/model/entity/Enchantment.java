package domain.model.entity;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import javax.imageio.ImageIO;

public class Enchantment implements Serializable {
  private static final long serialVersionUID = 1L;

  public enum Type {
    EXTRA_TIME,
    REVEAL,
    CLOAK_OF_PROTECTION,
    LURING_GEM,
    EXTRA_LIFE
  }

  private Type type;
  private int x;
  private int y;
  private long spawnTime;
  private long totalPauseDuration = 0;
  private transient BufferedImage image;
  private static final long DURATION = 6000; // 6 seconds in milliseconds
  private static final long BLINK_START_TIME = 2000; // Start blinking 2 seconds before expiry
  private static final long BLINK_INTERVAL_FAST = 150; // Blink interval for last second (milliseconds)
  private static final long BLINK_INTERVAL_SLOW = 300; // Blink interval for second-to-last second
  private boolean isGamePaused = false;
  private long currentPauseStart = 0;

  public Enchantment(Type type, int x, int y) {
    this.type = type;
    this.x = x;
    this.y = y;
    this.spawnTime = System.currentTimeMillis();
    loadImage();
  }

  private void loadImage() {
    try {
      String imagePath = switch (type) {
        case EXTRA_TIME -> "/enchantments/extra_time.png";
        case REVEAL -> "/enchantments/reveal.png";
        case CLOAK_OF_PROTECTION -> "/enchantments/cloak.png";
        case LURING_GEM -> "/enchantments/luring_gem.png";
        case EXTRA_LIFE -> "/enchantments/extra_life.png";
      };
      image = ImageIO.read(getClass().getResourceAsStream(imagePath));
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /** Gets the actual time elapsed since spawn, excluding all pause durations. */
  private long getEffectiveTime() {
    long currentTime = System.currentTimeMillis();
    long pauseTime = totalPauseDuration;

    // If currently paused, add the current pause duration
    if (isGamePaused && currentPauseStart > 0) {
      pauseTime += (currentTime - currentPauseStart);
    }

    return currentTime - spawnTime - pauseTime;
  }

  public boolean hasExpired() {
    return getEffectiveTime() >= DURATION;
  }

  /**
   * Determines if the enchantment should be visible based on its blinking state.
   */
  public boolean isVisible() {
    // If game is paused, always show the enchantment
    if (isGamePaused) {
      return true;
    }

    long effectiveTime = getEffectiveTime();
    long remainingTime = DURATION - effectiveTime;

    // If not in blinking phase, always visible
    if (remainingTime > BLINK_START_TIME) {
      return true;
    }

    // Determine blink interval based on remaining time
    long blinkInterval = remainingTime > BLINK_START_TIME / 2 ? BLINK_INTERVAL_SLOW : BLINK_INTERVAL_FAST;

    // Calculate if visible based on effective time and blink interval
    return (effectiveTime / blinkInterval) % 2 == 0;
  }

  /** Updates the pause state of the enchantment. */
  public void setPaused(boolean isPaused) {
    if (isPaused != isGamePaused) {
      long currentTime = System.currentTimeMillis();

      if (isPaused) {
        // Game is being paused
        currentPauseStart = currentTime;
      } else {
        // Game is being unpaused
        if (currentPauseStart > 0) {
          totalPauseDuration += (currentTime - currentPauseStart);
          currentPauseStart = 0;
        }
      }
      isGamePaused = isPaused;
    }
  }

  /** This method is no longer needed as pause duration is handled in setPaused */
  public void addPauseDuration(long duration) {
    // Method kept for compatibility but no longer used
  }

  public Type getType() {
    return type;
  }

  public int getX() {
    return x;
  }

  public int getY() {
    return y;
  }

  public BufferedImage getImage() {
    return image;
  }

  /** Gets the remaining time before this enchantment expires. */
  public long getRemainingTime() {
    return Math.max(0, DURATION - getEffectiveTime());
  }

  private void writeObject(ObjectOutputStream out) throws IOException {
    out.defaultWriteObject();
    out.writeInt(x);
    out.writeInt(y);
    out.writeLong(spawnTime);
    out.writeLong(totalPauseDuration);
    out.writeBoolean(isGamePaused);
    out.writeLong(currentPauseStart);
    out.writeObject(type);
  }

  private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
    in.defaultReadObject();
    x = in.readInt();
    y = in.readInt();
    spawnTime = in.readLong();
    totalPauseDuration = in.readLong();
    isGamePaused = in.readBoolean();
    currentPauseStart = in.readLong();
    type = (Type)in.readObject();
    loadImage();
  }
}
