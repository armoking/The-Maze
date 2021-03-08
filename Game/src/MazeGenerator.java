import java.util.ArrayList;
import java.util.Random;

public class MazeGenerator {
    static Random rnd = new Random();

    static class DSU {
        int[] p;
        int[] d;

        DSU(int n) {
            p = new int[n];
            d = new int[n];
            for (int i = 0; i < n; i++) {
                p[i] = i;
                d[i] = 1;
            }
        }

        int get(int v) {
            if (p[v] == v) {
                return v;
            } else {
                return p[v] = get(p[v]);
            }
        }

        void uni(int a, int b) {
            a = get(a);
            b = get(b);
            if (a == b) return;
            if (d[a] < d[b]) {
                int tmp = a;
                a = b;
                b = tmp;
            }
            d[a] += d[b];
            p[b] = a;
        }

    }


    DSU dsu;
    int[][] ans;
    int w, h, n;

    MazeGenerator(int w, int h) {
        n = w * h;
        this.w = w;
        this.h = h;
        dsu = new DSU(n);
        ans = new int[w][h];
    }

    public static final int[][] DELTAS = {{1, 0}, {0, 1}};

    int[][] get() {
        ArrayList<Long> edges = new ArrayList<>();
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                for (int[] dlt : DELTAS) {
                    int nx = x + dlt[0];
                    int ny = y + dlt[1];
                    if (nx >= w || ny >= h) continue;
                    long edge = ((long) nx * h + ny) * w * h + ((long) x * h + y);
                    edges.add(edge);
                }
            }
        }
        for (int i = 1; i < edges.size(); i++) {
            int pre = rnd.nextInt(i);
            long tmp = edges.get(pre);
            edges.set(pre, edges.get(i));
            edges.set(i, tmp);
        }

        int[][] ans = new int[w][h];


        for (long edge : edges) {
            int firstNode = (int) (edge % n);
            int secondNode = (int) (edge / n);
            int x1 = firstNode / h;
            int y1 = firstNode % h;
            int x2 = secondNode / h;

            if (dsu.get(firstNode) != dsu.get(secondNode)) {
                if (x1 != x2) {
                    ans[x1][y1] |= 1;
                } else {
                    ans[x1][y1] |= 2;
                }
                dsu.uni(firstNode, secondNode);
            }

        }
        return ans;
    }

}
