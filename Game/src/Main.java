import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Random;


public class Main {
    public static JFrame frame = null;
    public static JLabel label = new JLabel();
    public static BufferedImage image = null;
    public static int currentX = 25;
    public static int currentY = 25;
    public static double[][] atan2;
    public static boolean[][] end, wall;
    static int rnd_counter = 0;
    static int rnd_values = new Random().nextInt();


    public static final String PATH_TO_IMAGE = "resource\\man_up.jpg";

    public static boolean DRAW = false;

    public static double getAtan2(int dx, int dy) {
        double val = atan2[Math.min(Math.abs(dx), atan2.length - 1)][Math.min(Math.abs(dy), atan2[0].length - 1)];
        if (dx < 0 && dy < 0) {
            val += Math.PI;
        } else if (dy < 0) {
            val = 2 * Math.PI - val;
        } else if (dx < 0) {
            val = Math.PI - val;
        }
        return val;
    }

    public static int[] colors = {0xf0f000, 0x101010, 0x00fff0, 0x123456, 0x00ff00};
    public static double[] angles = {Math.PI / 15, Math.PI / 12, Math.PI / 9, Math.PI / 6, Math.PI / 5};
    public static int[] sizes = {5, 8, 10, 14, 18};
    public static double a = angles[0];
    public static int cellCount = sizes[0];
    public static int lightColor = colors[0];
    public static int[] arr = {1, 4, 4};

    public static int[][] rotate(int[][] man, double dir) {
        double cos = Math.cos(dir);
        double sin = Math.sin(dir);
        int n = man.length;
        int m = man[0].length;
        int[][] res = new int[n][m];
        int[][] cnt = new int[n][m];
        int[][] r = new int[n][m];
        int[][] g = new int[n][m];
        int[][] b = new int[n][m];
        int[][] ar = new int[n][m];
        int[][] ag = new int[n][m];
        int[][] ab = new int[n][m];

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                r[i][j] = (man[i][j] & 0xff0000) >> 16;
                g[i][j] = (man[i][j] & 0x00ff00) >> 8;
                b[i][j] = (man[i][j] & 0x0000ff);
            }
        }

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                i -= n / 2;
                j -= m / 2;
                double nx = (i * cos - j * sin);
                double ny = (i * sin + j * cos);
                i += n / 2;
                j += m / 2;
                nx += n * 0.5;
                ny += m * 0.5;
                nx = Math.min(nx, n - 1);
                ny = Math.min(ny, m - 1);
                nx = Math.max(nx, 0);
                ny = Math.max(ny, 0);
                ar[(int) nx][(int) ny] += r[i][j];
                ag[(int) nx][(int) ny] += g[i][j];
                ab[(int) nx][(int) ny] += b[i][j];
                cnt[(int) nx][(int) ny] += 1;

                if ((int) nx != nx && (int) nx + 1 < n) {
                    ar[(int) nx + 1][(int) ny] += r[i][j];
                    ag[(int) nx + 1][(int) ny] += g[i][j];
                    ab[(int) nx + 1][(int) ny] += b[i][j];
                    cnt[(int) nx + 1][(int) ny] += 1;
                }

                if ((int) ny != ny && (int) ny + 1 < m && (int) nx != nx && (int) nx + 1 < n) {
                    ar[(int) nx + 1][(int) ny + 1] += r[i][j];
                    ag[(int) nx + 1][(int) ny + 1] += g[i][j];
                    ab[(int) nx + 1][(int) ny + 1] += b[i][j];
                    cnt[(int) nx + 1][(int) ny + 1] += 1;
                }

                if ((int) ny != ny && (int) ny + 1 < m) {
                    ar[(int) nx][(int) ny + 1] += r[i][j];
                    ag[(int) nx][(int) ny + 1] += g[i][j];
                    ab[(int) nx][(int) ny + 1] += b[i][j];
                    cnt[(int) nx][(int) ny + 1] += 1;
                }
            }
        }
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                if (cnt[i][j] == 0) continue;
                ar[i][j] /= cnt[i][j];
                ag[i][j] /= cnt[i][j];
                ab[i][j] /= cnt[i][j];
                res[i][j] = (ar[i][j] << 16) ^ (ag[i][j] << 8) ^ ab[i][j];
            }
        }
        return res;
    }

    public static int getRand(int border) {
        rnd_values *= 15;
        rnd_values ^= (++rnd_counter);
        return Math.abs(rnd_values) % border;
    }

    public static int px = 0;
    public static int py = 0;
    public static Menu menu = null;
    public static Menu.States state = Menu.States.none;
    public static boolean exit = false;
    public static boolean stop = false;
    public static boolean repeat = false;
    public static BufferedImage manImage;
    public static String output = "LOGGERS\n";

    public static void main(String[] args) {
        try {
            do {
                exit = false;
                stop = false;
                px = 0;
                py = 0;
                currentY = 25;
                currentX = 25;
                state = Menu.States.none;
                manImage = null;
                {
                    try {
                        manImage = ImageIO.read( Main.class.getResource("man_up.jpg"));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                menu = new Menu(arr, manImage);


                while (!menu.started && !exit) {
                    display(menu.image);
                    menu.UpdateImage();
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                long start_time = System.currentTimeMillis();
                if (!exit) runLevel(cellCount, cellCount);

                image = new BufferedImage(700, 300, BufferedImage.TYPE_INT_RGB);
                Graphics2D g2d = image.createGraphics();
                g2d.setFont(new Font("Arial", Font.PLAIN, 30));
                g2d.drawString("Congratulations!!!", 100, 50);
                g2d.drawString("You wasted more than you received...", 100, 100);
                long end_time = System.currentTimeMillis();
                long wasted_time = (end_time - start_time) / 1000;
                g2d.drawString("Time is " + wasted_time + " seconds...", 100, 150);
                g2d.drawString("LOOSER. :)  Try again?", 100, 200);
                for (int x = 273; x <= 419; x++) {
                    for (int y = 173; y <= 208; y++) {
                        int color = image.getRGB(x, y);
                        image.setRGB(x, y, Integer.MAX_VALUE ^ color);
                    }
                }

                g2d.drawString("Developers: dora9000 and armoking", 100, 250);
                display(700, 300);
                exit = true;
                repeat = false;
                stop = false;
                while (!stop && !repeat) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                System.out.println(stop + " " + repeat);
            } while (!stop);
            System.out.println("End of the game");
        } catch (Exception ex) {
            output += ex.toString() + "\n";
        }
        output += "done\n";
        try {
            BufferedWriter out = new BufferedWriter(new FileWriter("output.txt"));
            out.write(output);
            out.flush();
            out.close();
        } catch (Exception ignored) {
            System.out.println("here");
        }
        System.exit(0);
    }

    static final int thicknessOfWall = 1;

    static void applyMazeToImage(int[][] maze, int cellW, int cellH, int cellSize, int w, int h) {
        for (int x = 0; x < cellW; x++) {
            for (int y = 0; y < cellH; y++) {
                for (int dx = -thicknessOfWall; dx < cellSize + thicknessOfWall; dx++) {
                    for (int dy = -thicknessOfWall; dy < cellSize + thicknessOfWall; dy++) {
                        int cx = x * cellSize + dx;
                        int cy = y * cellSize + dy;
                        if (cx < 0 || cy < 0 || cx >= w || cy >= h) continue;
                        if ((maze[x][y] & 1) == 0 && (Math.abs(dx - cellSize + 1) < 2 * thicknessOfWall)
                                || (maze[x][y] & 2) == 0 && Math.abs(dy - cellSize + 1) < 2 * thicknessOfWall
                                || cx <= thicknessOfWall || cy <= thicknessOfWall) {
                            image.setRGB(cx, cy, 0xffffff);
                        }
                    }
                }
            }
        }
    }

    static void setEndPosition(int ex, int ey, int cellSize) {
        int mid = cellSize / 2;
        for (int x = 0; x < cellSize; x++) {
            for (int y = 0; y < cellSize; y++) {
                int dx = (x - mid);
                int dy = (y - mid);
                if (dx * dx + dy * dy <= mid * mid - 5) {
                    end[cellSize * ex + x][cellSize * ey + y] = true;
                }
            }
        }
    }

    static void handlePosition(int centX, int centY, int manW, int manH, int w, int h) {
        int curPx = px;
        int curPy = py;
        boolean bad = true;
        int counter = 0;
        while (bad && counter < 100) {
            bad = false;
            counter++;
            for (int dx = -centX; dx < manW - centX && !bad; dx++) {
                for (int dy = -centY; dy < manH - centY; dy++) {
                    int curX = currentX + curPx + dx;
                    int curY = currentY + curPy + dy;
                    if (curX < 0 || curY < 0 || curX >= w || curY >= h || wall[curX][curY]) {
                        bad = true;
                        break;
                    }
                }
            }
            curPx -= Math.signum(curPx);
            curPy -= Math.signum(curPy);
        }

        px += px / 3;
        py += py / 3;
        if (px < -10) px = -10;
        if (py < -10) py = -10;
        if (px > 10) px = 10;
        if (py > 10) py = 10;
        currentX += curPx;
        currentY += curPy;

    }

    static void runLevel(int cellW, int cellH) {
        assert manImage != null;
        int manW = manImage.getWidth();
        int manH = manImage.getHeight();

        int[][] man = new int[manW][manH];
        for (int i = 0; i < manW; i++) {
            for (int j = 0; j < manH; j++) {
                man[i][j] = manImage.getRGB(i, j);
            }
        }

        int cellSize = Math.min(800 / cellW, 800 / cellH);
        int w = cellW * cellSize + 5;
        int h = cellH * cellSize + 5;
        image = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        if (frame != null) {
            frame.setSize(image.getWidth(), image.getHeight());
        }
        int[][] maze = new MazeGenerator(cellW, cellH).get();
        applyMazeToImage(maze, cellW, cellH, cellSize, w, h);

        ArrayDeque<Point> queue = new ArrayDeque<>();


        atan2 = new double[w][h];
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                atan2[x][y] = Math.atan2(y, x);
            }
        }

        wall = new boolean[w][h];
        end = new boolean[w][h];

        int ex = getRand(cellW - 1) + 1;
        int ey = getRand(cellH - 1) + 1;

        setEndPosition(ex, ey, cellSize);


        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                wall[x][y] = (image.getRGB(x, y) & 0xffffff) != 0;
            }
        }

        image = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);

        int cnt = 0;
        double direction = 0;

        try {

            do {
                cnt++;
                image = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);

                double leftAngle = direction - a;
                double rightAngle = direction + a;

                for (int x = 0; x < w; x++) {
                    for (int y = 0; y < h; y++) {
                        int dx = x - currentX;
                        int dy = y - currentY;
                        int color = -1;
                        double cur = getAtan2(dx, dy);
                        while (cur < leftAngle) cur += Math.PI * 2;
                        while (cur > rightAngle) cur -= Math.PI * 2;
                        if (leftAngle < cur && cur < rightAngle) {
                            color = end[x][y] ? getRand(Integer.MAX_VALUE) : wall[x][y] ? 0xff0000 : lightColor;
                        }
                        if (color != -1) {
                            image.setRGB(x, y, color);
                        }
                    }
                }

                int DLT = 40;
                for (int dx = -DLT; dx <= DLT; dx++) {
                    for (int dy = -DLT; dy <= DLT; dy++) {
                        int nx = dx + currentX;
                        int ny = dy + currentY;
                        if (0 <= nx && nx < w && 0 <= ny && ny < h) {
                            if (dx * dx + dy * dy <= DLT * DLT) {
                                if (wall[nx][ny]) {
                                    image.setRGB(nx, ny, 0xff0000);
                                } else if (end[nx][ny]) {
                                    image.setRGB(nx, ny, getRand(Integer.MAX_VALUE));
                                } else {
                                    image.setRGB(nx, ny, lightColor);
                                }
                            }
                        }
                    }
                }

                int[][] rotatedMan = rotate(man, direction);
                int centX = manW / 2;
                int centY = manH / 2;
                for (int dx = -centX; dx < manW - centX; dx++) {
                    for (int dy = -centY; dy < manH - centY; dy++) {
                        int x = currentX + dx;
                        int y = currentY + dy;
                        if (x < w && y < h && x >= 0 && y >= 0 && dx * dx + dy * dy <= Math.pow(Math.min(centX, centY), 2)) {
                            image.setRGB(x, y, rotatedMan[dx + centX][dy + centY]);
                        }
                    }
                }

                Point location = MouseInfo.getPointerInfo().getLocation();
                Point cur = new Point(0, 0);
                SwingUtilities.convertPointToScreen(cur, label);

                double x = location.getX() - cur.getX() - currentX;
                double y = location.getY() - cur.getY() - currentY;

                if (DRAW) {
                    int curx = (int) x + currentX;
                    int cury = (int) y + currentY;
                    if (curx < 0 || cury < 0 || curx >= w || cury >= h) continue;
                    queue.add(new Point(cnt, (cury << 16) ^ curx));
                }

                while (!queue.isEmpty() && (cnt - queue.peekFirst().x) > 200) {
                    queue.pollFirst();
                }

                for (Point pt : queue) {
                    int xy = pt.y;
                    int cury = (xy >> 16);
                    int curx = xy % (1 << 16);

                    for (int dx = -1; dx <= 1; dx++) {
                        for (int dy = -1; dy <= 1; dy++) {
                            int nx = dx + curx;
                            int ny = dy + cury;
                            if (nx < 0 || ny < 0 || nx >= w || ny >= h) continue;
                            image.setRGB(nx, ny, getRand(Integer.MAX_VALUE));
                        }
                    }
                }


                direction = getAtan2((int) (x), (int) (y));
                while (direction >= 2 * Math.PI) {
                    direction -= 2 * Math.PI;
                }

                handlePosition(centX, centY, manW, manH, w, h);

                display();
                image.flush();
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            } while (!end[currentX][currentY] && !exit);
        } catch (Exception exception) {
            exception.printStackTrace();
            System.out.println("Game failed with counter = " + cnt + " and message:");
            System.out.println(exception.getMessage());
        }
        System.out.println("Finished");
    }

    public static void display(BufferedImage img) {
        image = img;
        display(image.getWidth(), image.getHeight());
    }

    public static void display(int w, int h) {
        display();
        frame.setSize(w, h);
        display();
    }

    public static void display() {
        if (frame == null) {
            frame = new JFrame();
            frame.addKeyListener(new KeyAdapter() {
                @Override
                public void keyTyped(KeyEvent e) {
                    switch (e.getKeyChar()) {
                        case 'd' -> px = Math.min(10, Math.max(px, 3) + 3);
                        case 's' -> py = Math.min(10, Math.max(py, 3) + 3);
                        case 'w' -> py = Math.max(-10, Math.min(py, -3) - 3);
                        case 'a' -> px = Math.max(-10, Math.min(px, -3) - 3);
                        case 'x' -> DRAW = true;
                        case 'z' -> {
                            exit = true;
                        }
                    }
                }

                @Override
                public void keyReleased(KeyEvent e) {
                    switch (e.getKeyChar()) {
                        case 'a', 'd' -> px = 0;
                        case 'w', 's' -> py = 0;
                        case 'x' -> DRAW = false;
                    }
                }
            });

            frame.addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    if (exit) {
                        Point location = MouseInfo.getPointerInfo().getLocation();
                        Point cur = new Point(0, 0);
                        SwingUtilities.convertPointToScreen(cur, label);
                        int x = (int) (location.getX() - cur.getX());
                        int y = (int) (location.getY() - cur.getY());
                        System.out.println(x + "  " + y);
                        if (x >= 273 && x <= 419 && y >= 153 && y <= 188) {
                            System.out.println("Repeat");
                            repeat = true;
                            stop = false;
                        } else {
                            stop = true;
                            repeat = false;
                        }
                    } else if (menu != null) {
                        Point location = MouseInfo.getPointerInfo().getLocation();
                        Point cur = new Point(0, 0);
                        SwingUtilities.convertPointToScreen(cur, label);
                        double x = location.getX() - cur.getX();
                        double y = location.getY() - cur.getY();
                        state = menu.handlePress((int) x, (int) y, state);
                        if (state == Menu.States.start) {
                            menu.started = true;
                            menu.authors = false;
                            menu.settings = false;
                            menu.menu = false;
                            lightColor = colors[menu.arr[0]];
                            a = angles[menu.arr[1]];
                            cellCount = sizes[menu.arr[2]];
                            System.arraycopy(menu.arr, 0, arr, 0, 3);
                        } else if (state == Menu.States.exit) {
                            exit = true;
                        } else if (state == Menu.States.authors) {
                            menu.started = false;
                            menu.authors = true;
                            menu.settings = false;
                            menu.menu = false;
                        } else if (state == Menu.States.settings) {
                            menu.started = false;
                            menu.authors = false;
                            menu.settings = true;
                            menu.menu = false;
                        } else if (state == Menu.States.menu) {
                            menu.started = false;
                            menu.authors = false;
                            menu.settings = false;
                            menu.menu = true;
                        }
                    }
                }
            });

            frame.setSize(image.getWidth(), image.getHeight());
            frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            label = new JLabel();
            label.setIcon(new ImageIcon(image));
            frame.getContentPane().add(label, BorderLayout.CENTER);
            frame.setLocationRelativeTo(null);
            frame.pack();
            frame.setVisible(true);
        } else {
            label.setIcon(new ImageIcon(image));
        }
    }

}
