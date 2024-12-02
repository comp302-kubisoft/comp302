package domain.entity;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;

import ui.GamePanel;
import ui.KeyHandler;

public class Hero extends Entity {
    GamePanel gp;
    KeyHandler keyH;

    public Hero(GamePanel gp, KeyHandler keyH) {
        this.gp = gp;
        this.keyH = keyH;
        setDefaultValues();
        getPlayerImage();
    }

    public void setDefaultValues() {
        x = 100;
        y = 100;
        speed = 4;
        direction = "down";
    }

    public void getPlayerImage() {
        try {
            imag = ImageIO.read(getClass().getResourceAsStream("/hero/player.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void update() {
        if (keyH.upPressed == true) {
            direction = "up";
            y -= speed;

        } else if (keyH.downPressed == true) {
            direction = "down";
            y += speed;
        }

        else if (keyH.leftPressed == true) {
            direction = "left";
            x -= speed;
        }

        else if (keyH.rightPressed == true) {
            direction = "right";
            x += speed;
        }
    }

    public void draw(Graphics2D g2) {
        BufferedImage image = imag;
        g2.drawImage(image, x, y, gp.tileSize, gp.tileSize, null);
    }
}
