package domain.model.entity;

import java.awt.image.BufferedImage;

public class Entity {

  protected int x;
  protected int y;
  protected int speed;
  protected BufferedImage imag;
  protected String direction;

  public int getX() {
    return x;
  }

  public int getY() {
    return y;
  }

  public int getSpeed() {
    return speed;
  }

  public String getDirection() {
    return direction;
  }

  public void setDirection(String dir) {
    this.direction = dir;
  }

  public void setPosition(int x, int y) {
    this.x = x;
    this.y = y;
  }

  public void setX(int x) {
    this.x = x;
  }

  public void setY(int y) {
    this.y = y;
  }

  public void setSpeed(int speed) {
    this.speed = speed;
  }
}
