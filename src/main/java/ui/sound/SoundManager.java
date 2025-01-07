package ui.sound;

public class SoundManager {
  private static SoundManager instance;
  private Sound music;
  private Sound sfx;

  private SoundManager() {
    music = new Sound();
    sfx = new Sound();
  }

  public static SoundManager getInstance() {
    if (instance == null) {
      instance = new SoundManager();
    }
    return instance;
  }

  public void playMusic(int i) {
    music.setFile(i);
    music.play();
    music.loop();
  }

  public void stopMusic() {
    music.stop();
  }

  public void playSFX(int i) {
    sfx.setFile(i);
    sfx.play();
  }
}
