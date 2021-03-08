import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;


public class Main {
    public static JFrame frame = null;
    public static JLabel label = new JLabel();
    public static BufferedImage image = null;
    public static int currentX = 25;
    public static int currentY = 25;
    public static double[][] atan2;
    public static boolean[][] end, wall;
    static int rnd_counter = 0;
    static int rnd_values = 0;


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

    public static int[] colors = {0xf0f000, 0xf0f0f0, 0x00fff0, 0x123456, 0x00ff00};
    public static double[] angles = {Math.PI / 15, Math.PI / 12, Math.PI / 9, Math.PI / 6, Math.PI / 5};
    public static int[] sizes = {5, 8, 10, 14, 18};
    public static double a = angles[0];
    public static int cellCount = sizes[0];
    public static int lightColor = colors[0];
    public static int[] arr = {1, 3, 1};

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

    public static int getRand() {
        rnd_counter++;
        rnd_values = Math.abs(rnd_values * 123 ^ rnd_counter ^ (rnd_counter << 4));
        return rnd_values;
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
    public static int[] showingImage = null;
    public static int showingW = 0;
    public static int showingH = 0;
    public static Enemy[] enemies = null;
    public static int[] enemyMap = null;

    public static void main(String[] args) throws IOException {
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
                heartsCount = 5;
                {
                    try {
                        manImage = ImageIO.read(Main.class.getResource("man.jpg"));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                menu = new Menu(arr, manImage);

                Thread thread = new Thread(() -> {
                    while (!menu.started && !exit) {
                        try {
                            Thread.sleep(6);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        display(menu.image);
                        menu.UpdateImage();
                    }
                });
                thread.start();
                thread.join();

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

                display(false);

                exit = true;
                repeat = false;
                stop = false;

                Thread thread2 = new Thread(() -> {
                    while (!stop && !repeat) {
                        try {
                            Thread.sleep(6);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                });
                thread2.start();
                thread2.join();
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
        int maxSpeed = 5;
        if (px < -maxSpeed) px = -maxSpeed;
        if (py < -maxSpeed) py = -maxSpeed;
        if (px > maxSpeed) px = maxSpeed;
        if (py > maxSpeed) py = maxSpeed;
        currentX += curPx;
        currentY += curPy;

    }

    static int getPosition(int pos, int cellSize) {
        return pos * cellSize + cellSize / 2;
    }

    static Enemy createNewEnemy(int x, int y) {
        return new Enemy(x, y, Main.class.getResource("enemy.png"));
    }

    static BufferedImage loadImage(String name) throws IOException {

        return ImageIO.read(Main.class.getResource(name));

    }

    static int heartsCount = 5;

    static void generateEnemies(int count, int cellW, int cellH, int cellSize) throws Exception {
        if (cellW * cellH - 4 * 4 < count) {
            throw new Exception("too many enemies");
        }
        enemies = new Enemy[count];
        int[][] positions = new int[count][2];
        for (int i = 0; i < count; i++) {
            int x = 0;
            int y = 0;
            boolean bad = false;
            while (x <= cellSize || y <= cellSize || bad) {
                x = getRand() % cellW;
                y = getRand() % cellH;
                x = getPosition(x, cellSize);
                y = getPosition(y, cellSize);
                bad = false;
                for (int j = 0; j < i; j++) {
                    if (positions[j][0] == x && positions[j][1] == y) {
                        bad = true;
                        break;
                    }
                }
            }
            positions[i][0] = x;
            positions[i][1] = y;
            enemies[i] = createNewEnemy(x, y);
        }
    }

    static void relaxEnemies() {
        if (enemies == null) return;
        for (int i = 0; i < enemies.length; i++) {
            Enemy enemy = enemies[i];
            if (enemy == null) continue;
            int w = enemy.image.getWidth();
            int h = enemy.image.getHeight() / 8;
            boolean possible = true;

            int nx = enemy.x + Enemy.directions[enemy.currentDirection][0] * Enemy.speed;
            int ny = enemy.y + Enemy.directions[enemy.currentDirection][1] * Enemy.speed;

            int dx = nx - currentX;
            int dy = ny - currentY;
            int dr = Math.min(w, h) / 2 + Math.min(manImage.getWidth(), manImage.getHeight()) / 2;
            if (dx * dx + dy * dy <= dr * dr) {
                heartsCount = Math.max(0, heartsCount - 1);
                enemies[i] = null;
                continue;
            }

            for (int x = 0; x < w && possible; x++) {
                for (int y = 0; y < h; y++) {
                    int posX = nx + x - w / 2;
                    int posY = ny + y - h / 2;
                    if (posX < 0 || posY < 0 || posX >= showingW || posY >= showingH
                            || wall[posX][posY] || end[posX][posY]) {
                        possible = false;
                        break;
                    }
                }
            }
            long curTime = System.currentTimeMillis();
            int deltaTime = 10000 / sizes[arr[2]];
            if (curTime - enemy.prevTime > deltaTime) {
                possible = false;
                enemy.prevTime = curTime + (getRand() % 500);
                enemy.state = (enemy.state + 1) % 8;
            }
            if (possible) {
                enemy.x = nx;
                enemy.y = ny;
            } else {
                enemy.currentDirection = getRand() % Enemy.directions.length;
            }
        }
    }

    static void drawEnemies() {
        if (enemies == null) return;
        for (Enemy enemy : enemies) {
            if (enemy == null) continue;
            int w = enemy.image.getWidth();
            int h = enemy.image.getHeight() / 8;
            int dy = h * enemy.state;
            int cent = w / 2;
            for (int x = 0; x < w; x++) {
                for (int y = 0; y < h; y++) {
                    int nx = enemy.x - w / 2 + x;
                    int ny = enemy.y - h / 2 + y;
                    if ((x - w / 2) * (x - w / 2 + 1) + (y - w / 2) * (y - w / 2 + 1) <= (w / 2 + 1) * (w / 2 + 1)) {
                        enemyMap[nx * showingH + ny] = enemy.image.getRGB(x, y + dy);
                    }
                }
            }
        }
    }

    static BufferedImage hearts = null;

    static void showHearts() throws IOException {
        if (hearts == null) {
            hearts = loadImage("hearts.png");
        }
        int dy = hearts.getHeight() / 6 * (5 - heartsCount);
        for (int x = 0; x < hearts.getWidth(); x++) {
            for (int y = 0; y < hearts.getHeight() / 6; y++) {
                int nx = x;
                int ny = showingH - 25 + y;
                showingImage[nx * showingH + ny] = hearts.getRGB(x, y + dy);
            }
        }
    }

    static void runLevel(int cellW, int cellH) throws Exception {
        assert manImage != null;
        int manW = manImage.getWidth();
        int manH = manImage.getHeight();

        int[][] man = new int[manW][manH];
        for (int i = 0; i < manW; i++) {
            for (int j = 0; j < manH; j++) {
                man[i][j] = manImage.getRGB(i, j);
            }
        }

        int cellSize = Math.min(700 / cellW, 700 / cellH);
        int w = cellW * cellSize + 5;
        int h = cellH * cellSize + 5 + 20;
        image = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        int[][] maze = new MazeGenerator(cellW, cellH).get();
        applyMazeToImage(maze, cellW, cellH, cellSize, w, h);
        atan2 = new double[w][h];
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                atan2[x][y] = Math.atan2(y, x);
            }
        }

        wall = new boolean[w][h];
        end = new boolean[w][h];

        int ex = getRand() % (cellW - 1) + 1;
        int ey = getRand() % (cellH - 1) + 1;

        setEndPosition(ex, ey, cellSize);


        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                wall[x][y] = (image.getRGB(x, y) & 0xffffff) != 0;
            }
        }

        image = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        showingW = w;
        showingH = h;


        final int ENEMY_COUNT = sizes[arr[2]];
        generateEnemies(ENEMY_COUNT, cellW, cellH, cellSize);

        try {
            exit = false;


            Thread thread = new Thread(() -> {
                int cnt = 0;
                double direction = 0;
                AtomicBoolean stopThread = new AtomicBoolean(false);
                ArrayDeque<Point> queue = new ArrayDeque<>();


                showingImage = new int[w * h];
                enemyMap = new int[w * h];
                while (!stopThread.get()) {
                    cnt++;
                    Arrays.fill(showingImage, 0);
                    Arrays.fill(enemyMap, -1);

                    relaxEnemies();
                    drawEnemies();
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
                                color = end[x][y] ? getRand() : wall[x][y] ? 0xff0000 : lightColor;
                            }
                            if (enemyMap[x * h + y] != -1 && color != -1) {
                                color = enemyMap[x * h + y];
                            }
                            if (color != -1) {
                                showingImage[x * h + y] = color;

                            }
                        }
                    }

                    int DLT = 100;
                    for (int dx = -DLT; dx <= DLT; dx++) {
                        for (int dy = -DLT; dy <= DLT; dy++) {
                            int nx = dx + currentX;
                            int ny = dy + currentY;
                            if (0 <= nx && nx < w && 0 <= ny && ny < h) {
                                if (dx * dx + dy * dy <= DLT * DLT) {
                                    int color = wall[nx][ny] ? 0xff0000 :
                                            end[nx][ny] ? getRand() :
                                                    enemyMap[nx * h + ny] == -1 ? lightColor : enemyMap[nx * h + ny];
                                    showingImage[nx * h + ny] = color;
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
                                showingImage[x * h + y] = rotatedMan[dx + centX][dy + centY];
                            }
                        }
                    }

                    Point location = MouseInfo.getPointerInfo().getLocation();
                    Point cur = new Point(0, 0);
                    SwingUtilities.convertPointToScreen(cur, label);

                    double x = location.getX() - cur.getX() - currentX;
                    double y = location.getY() - cur.getY() - currentY;

                    if (DRAW) {
                        int curX = (int) x + currentX;
                        int curY = (int) y + currentY;
                        if (curX < 0 || curY < 0 || curX >= w || curY >= h) continue;
                        queue.add(new Point(cnt, (curY << 16) ^ curX));
                    }

                    while (!queue.isEmpty() && (cnt - queue.peekFirst().x) > 200) {
                        queue.pollFirst();
                    }

                    for (Point pt : queue) {
                        int xy = pt.y;
                        int curY = (xy >> 16);
                        int curX = xy % (1 << 16);

                        for (int dx = -1; dx <= 1; dx++) {
                            for (int dy = -1; dy <= 1; dy++) {
                                int nx = dx + curX;
                                int ny = dy + curY;
                                if (nx < 0 || ny < 0 || nx >= w || ny >= h) continue;
                                showingImage[nx * h + ny] = getRand();
                            }
                        }
                    }


                    direction = getAtan2((int) (x), (int) (y));
                    while (direction >= 2 * Math.PI) {
                        direction -= 2 * Math.PI;
                    }

                    handlePosition(centX, centY, manW, manH, w, h);

                    try {
                        showHearts();
                        if (heartsCount == 0) {
                            exit = true;
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    display(true);
                    if (end[currentX][currentY] || exit) {
                        stopThread.set(true);
                    }
                    try {
                        Thread.sleep(6);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            });
            thread.start();
            thread.join();

            menu.started = false;
        } catch (Exception exception) {
            exception.printStackTrace();
            System.out.println(exception.getMessage());
        }
        System.out.println("Player has finished the level");
    }

    public static void display(BufferedImage img) {
        image = img;
        display(false);
    }

    public static void display(boolean flag) {
        if (frame == null) {
            frame = new JFrame();
            frame.addKeyListener(new KeyAdapter() {
                @Override
                public void keyTyped(KeyEvent e) {
                    switch (e.getKeyChar()) {
                        case 'd' -> px = 3;
                        case 's' -> py = 3;
                        case 'w' -> py = -3;
                        case 'a' -> px = -3;
                        case 'x' -> DRAW = true;
                        case 'z' -> {
                            if (menu.started) {
                                menu.started = false;
                                exit = true;
                            }
                        }

                    }
                }

                @Override
                public void keyReleased(KeyEvent e) {
                    switch (e.getKeyChar()) {
                        case 'a' -> px = Math.max(px, 0);
                        case 'd' -> px = Math.min(px, 0);
                        case 'w' -> py = Math.max(py, 0);
                        case 's' -> py = Math.min(py, 0);
                        case 'x' -> DRAW = false;
                    }
                }
            });

            frame.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseReleased(MouseEvent e) {
                    DRAW = false;
                }

                @Override
                public void mousePressed(MouseEvent e) {
                    if (menu.started) {
                        DRAW = true;
                    } else if (exit) {
                        Point location = MouseInfo.getPointerInfo().getLocation();
                        Point cur = new Point(0, 0);
                        SwingUtilities.convertPointToScreen(cur, label);
                        int x = (int) (location.getX() - cur.getX());
                        int y = (int) (location.getY() - cur.getY());
                        if (x >= 270 && x <= 425 && y >= 171 && y <= 210) {
                            repeat = true;
                            stop = false;
                        } else {
                            stop = true;
                            repeat = false;
                        }
                    } else {
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
                            System.exit(0);
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
            frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            label = new JLabel();
            frame.getContentPane().add(label, BorderLayout.PAGE_START);
            frame.setLocationRelativeTo(null);
            frame.pack();
            frame.setVisible(true);
        }
        if (flag) {
            image = new BufferedImage(showingW, showingH, BufferedImage.TYPE_INT_RGB);
            for (int x = 0; x < showingW; x++) {
                for (int y = 0; y < showingH; y++) {
                    image.setRGB(x, y, showingImage[x * showingH + y]);
                }
            }
        }
        frame.setLocation(50, 50);
        frame.setSize(image.getWidth() + 10, image.getHeight() + 32);
        label.setIcon(new ImageIcon(image));
    }

}
