import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.Random;

public class Enemy {
    int x, y;
    int state = 0;
    long prevTime = System.currentTimeMillis();
    BufferedImage image;
    static int[][] directions = {};
    static final int speed = 2;
    int currentDirection;

    Enemy(int x, int y, URL urlToImage) {
        if (directions.length == 0) {
            int itr = 0;
            directions = new int[25][2];
            for (int dx = -2; dx <= 2; dx++) {
                for (int dy = -2; dy <= 2; dy++) {
                    directions[itr][0] = dx;
                    directions[itr][1] = dy;
                    itr++;
                }
            }
        }

        this.x = x;
        this.y = y;
        currentDirection = new Random().nextInt(4);
        try {
            image = ImageIO.read(urlToImage);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
