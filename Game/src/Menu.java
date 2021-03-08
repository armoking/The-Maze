import java.awt.*;
import java.awt.image.BufferedImage;

public class Menu {
    final int W = 500;
    final int H = 300;
    public volatile boolean started;
    public boolean settings;
    public boolean authors;
    public boolean menu;

    public static int[] colors = {0xf0f000, 0xf0f0f0, 0x00fff0, 0x123456, 0x00ff00};
    public static double[] angles = {Math.PI / 15, Math.PI / 12, Math.PI / 9, Math.PI / 6, Math.PI / 5};
    public static int[] sizes = {5, 8, 10, 14, 18};
    static long prevTime = System.currentTimeMillis();


    enum States {
        menu,
        start,
        settings,
        authors,
        exit,
        none
    }

    int[] arr = {2, 2, 2};
    int[] xs = {209, 232, 257, 283, 306, 333};
    int[] ys = {50, 100, 150, 200};
    int[][] maze = null;
    BufferedImage theMan;
    BufferedImage image;

    Menu(int[] arr, BufferedImage man) {
        started = false;
        settings = false;
        authors = false;
        menu = true;
        System.arraycopy(arr, 0, this.arr, 0, 3);
        UpdateImage();
        theMan = man;
    }

    void UpdateImage() {
        try {
            if (menu) {
                image = new BufferedImage(W, H, BufferedImage.TYPE_INT_RGB);
                Graphics2D g2d = image.createGraphics();
                g2d.setFont(new Font("Arial", Font.PLAIN, 33));
                g2d.drawString("THE MAZE", 150, 50);
                boolean good;
                boolean pre = false;
                int itr = -1;
                int[] rainbow = {0xff0000, 0xff7700, 0xffff000, 0x00ff00, 0x00ffff, 0x0000ff, 0xff00ff};
                for (int x = 0; x < W; x++) {
                    good = false;
                    for (int y = 0; y < 50; y++) {
                        if ((image.getRGB(x, y) & 1) > 0) {
                            good = true;
                        }
                    }
                    if (!pre && good) {
                        itr++;
                    }
                    for (int y = 0; y < 50; y++) {
                        if ((image.getRGB(x, y) & 1) > 0) {
                            image.setRGB(x, y, rainbow[itr]);
                        }
                    }
                    pre = good;
                }
                g2d.setFont(new Font("Arial", Font.PLAIN, 30));
                g2d.drawString("Start", 50, 100);
                g2d.drawString("Settings", 50, 150);
                g2d.drawString("Authors", 50, 200);
                g2d.drawString("Exit", 50, 250);
            } else if (authors) {
                image = new BufferedImage(W, H, BufferedImage.TYPE_INT_RGB);
                Graphics2D g2d = image.createGraphics();
                g2d.setFont(new Font("Arial", Font.PLAIN, 30));
                g2d.drawString("Authors:", 50, 100);
                g2d.drawString("dora9000 and armoking", 50, 150);
            } else if (settings) {
                image = new BufferedImage(W, H, BufferedImage.TYPE_INT_RGB);
                Graphics2D g2d = image.createGraphics();
                g2d.setFont(new Font("Arial", Font.PLAIN, 30));
                g2d.drawString("Color: 1 2 3 4 5", 121, 100);
                g2d.drawString("Angle: 1 2 3 4 5", 116, 150);
                g2d.drawString("Size: 1 2 3 4 5", 134, 200);
                int backGroundColor = colors[arr[0]];
                int color = 0xff0000;
                for (int i = 0; i < 3; i++) {
                    int type = arr[i];
                    for (int x = xs[type] - 2; x < xs[type + 1] - 6; x++) {
                        for (int y = ys[i] + 25; y < ys[i + 1] + 2; y++) {
                            int curColor = image.getRGB(x, y);
                            if ((curColor & 0xffffff) == 0xffffff) {
                                image.setRGB(x, y, color);
                            } else {
                                image.setRGB(x, y, backGroundColor);
                            }
                        }
                    }
                }

                {
                    double angle = angles[arr[1]];
                    color = colors[arr[0]];
                    int r = 50;
                    for (int x = 50; x < 100; x++) {
                        for (int y = 150; y < 250; y++) {
                            int dx = x - 50;
                            int dy = y - 200;
                            double curAngle = Math.atan2(dy, dx);
                            if (curAngle > -angle && curAngle < angle) {
                                if (dx * dx + dy * dy <= r * r) {
                                    image.setRGB(x, y, color);
                                }
                            }
                        }
                    }
                }

                int manW = theMan.getWidth();
                int manH = theMan.getHeight();


                int centX = manW / 2;
                int centY = manH / 2;

                for (int i = 0; i < manW; i++) {
                    for (int j = 0; j < manH; j++) {
                        image.setRGB(50 + i - centX, 200 + j - centY, theMan.getRGB(i, j));
                    }
                }

                int cellW = sizes[arr[2]];
                int cellH = sizes[arr[2]];

                int cellSize = Math.min(150 / cellW, 150 / cellH);
                int w = cellW * cellSize;
                int h = cellH * cellSize;

                if (System.currentTimeMillis() - prevTime > 300 || maze == null || maze.length != cellW || maze[0].length != cellH) {
                    prevTime = System.currentTimeMillis();
                    maze = new MazeGenerator(cellW, cellH).get();
                }
                for (int x = 0; x < cellW; x++) {
                    for (int y = 0; y < cellH; y++) {
                        for (int dx = -1; dx < cellSize + 1; dx++) {
                            for (int dy = -1; dy < cellSize + 1; dy++) {
                                int cx = x * cellSize + dx;
                                int cy = y * cellSize + dy;
                                if (cx < 0 || cy < 0 || cx >= w || cy >= h) continue;
                                if ((maze[x][y] & 1) == 0 && dx == cellSize - 1
                                        || (maze[x][y] & 2) == 0 && dy == cellSize - 1
                                        || cx == 0 || cy == 0) {
                                    image.setRGB(W - w + cx - 20, H - h + cy - 23, 0xffffff);
                                }
                            }
                        }
                    }
                }


            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public States handlePress(int x, int y, States state) {
        y -= 20;
        if (y < 0 || y >= H || x < 0 || x >= W) return States.menu;
        if (state == States.menu) {
            if (y < 50) return States.none;
            if (y < 100) return States.start;
            if (y < 150) return States.settings;
            if (y < 200) return States.authors;
            if (y < 250) return States.exit;
            return States.menu;
        } else if (state == States.start) {
            return States.menu;
        } else if (state == States.authors) {
            return States.menu;
        } else if (state == States.settings) {
            if (y < 50 || y >= 200 || x < 209 || x > 325) return States.menu;
            // 232, 257, 283, 306, 325
            int level;
            if (x < 232) {
                level = 0;
            } else if (x < 257) {
                level = 1;
            } else if (x < 283) {
                level = 2;
            } else if (x < 306) {
                level = 3;
            } else {
                level = 4;
            }

            int type;
            if (y < 100) {
                type = 0;
            } else if (y < 150) {
                type = 1;
            } else {
                type = 2;
            }
            arr[type] = level;
            return States.settings;
        } else if (state == States.exit) {
            return States.exit;
        }
        return States.menu;
    }

}
