/**
 * Created by konglk on 2019/5/22.
 */
public class Knight {
    static int[] xMove = {-2, -2, -1, 1, 2, 2, 1, -1};
    static int[] yMove = {-1, 1, 2, 2, 1, -1, -2, -2};

//    static int xMove[] = {2, 1, -1, -2, -2, -1, 1, 2};
//    static int yMove[] = {1, 2, 2, 1, -1, -2, -2, -1};


    public static void solveKT() {
        int n = 8;
        int[][] sol = new int[8][8];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                sol[i][j] = -1;
            }
        }
        int x=7, y=7;
        sol[x][y] = 0;
        if (solveKTUtil(x, y, 1, sol)) {
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    System.out.print(sol[i][j] + " ");
                }
                System.out.println();
            }
        }
    }

    public static boolean solveKTUtil(int x, int y, int move, int[][] sol) {
        if (move == 64) {
            return true;
        }
        for (int k = 0; k < 8; k++) {
            int next_x = x + xMove[k];
            int next_y = y + yMove[k];
            if (isSafe(sol, next_x, next_y)) {
                sol[next_x][next_y] = move;
                if (solveKTUtil(next_x, next_y, move + 1, sol)) {
                    return true;
                }
                sol[next_x][next_y] = -1;
            }
        }
        return false;
    }

    public static boolean isSafe(int[][] sol, int x, int y) {
        int n = 8;
        return x >= 0 && x < n && y >= 0 && y < n && sol[x][y] == -1;
    }

    /* Driver program to test above functions */
    public static void main(String args[]) {
        solveKT();
    }
}
