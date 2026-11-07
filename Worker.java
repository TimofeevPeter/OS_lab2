import java.util.concurrent.atomic.AtomicInteger;

public class Worker implements Runnable {

    private final int[][] matrixA;
    private final int[][] matrixB;
    private final int[][] matrixC;
    private final int startRow;
    private final int endRow;
    private final int k;
    private final int m;
    private final AtomicInteger completedRows;

    public Worker (int[][] matrixA, int[][] matrixB, int[][] matrixC,
                   int startRow, int endRow, int k, int m,
                   AtomicInteger completedRows) {
        this.matrixA = matrixA;
        this.matrixB = matrixB;
        this.matrixC = matrixC;
        this.startRow = startRow;
        this.endRow = endRow;
        this.k = k;
        this.m = m;
        this.completedRows = completedRows;

    }

    @Override
    public void run () {
        for (int i = startRow; i < endRow; i++) {
            for (int j = 0; j < k; j++) {
                int sum = 0;
                for (int t = 0; t < m; t++) {
                    sum += matrixA[i][t] * matrixB[t][j];
                }
                matrixC[i][j] = sum;
            }
            //Увеличение счётчика выполненных строк
            completedRows.incrementAndGet();
        }
    }



}
