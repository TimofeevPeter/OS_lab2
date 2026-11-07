import java.util.concurrent.atomic.AtomicInteger;

public class TriangularWorker implements Runnable {

    private final int[][] matrixA;
    private final int[][] matrixB;
    private final int[][] matrixC;
    private final int startRow;
    private final int endRow;
    private final int k;
    private final int n;
    private final AtomicInteger completedRows;
    private final AtomicInteger nextRows;
    private final boolean isDynamic;

    public TriangularWorker(int[][] matrixA, int[][] matrixB, int[][] matrixC,
                            int n, int k, int startRow, int endRow,
                            AtomicInteger completedRows) {
        this.matrixA = matrixA;
        this.matrixB = matrixB;
        this.matrixC = matrixC;
        this.n = n;
        this.k = k;
        this.startRow = startRow;
        this.endRow = endRow;
        this.completedRows = completedRows;
        this.isDynamic = false;
        this.nextRows = null;

    }

    public TriangularWorker(int[][] matrixA, int[][] matrixB, int[][] matrixC,
                            int n, int k, AtomicInteger nextRows,
                            AtomicInteger completedRows) {
        this.matrixA = matrixA;
        this.matrixB = matrixB;
        this.matrixC = matrixC;
        this.n = n;
        this.k = k;
        this.startRow = 0;
        this.endRow = 0;
        this.completedRows = completedRows;

    }


    @Override
    public void run() {
        if (isDynamic) {
            while (true) {
                int row = nextRows.getAndIncrement();
                if (row >= n) {
                    break;
                }
                completedRows(row);
            }
        } else {
            for (int i = startRow; i < endRow; i++) {
                completedRows(i);
            }
        }
    }


    private void computeRow(int i) {
        for (int j = 0; j < k; j++) {
            int sum = 0;
            for (int t = 0; t <= i; t++) {
                sum += matrixA[i][t] * matrixB[t][j];
            }
            matrixC[i][j] = sum;
        }
        completedRows.incrementAndGet();
    }


}
