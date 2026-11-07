import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicInteger;


public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Выберите тип умножения:");
        System.out.println("1. Обычное умножение");
        System.out.println("2. Нижнетреугольная матрица");
        int mode = scanner.nextInt();

        if (mode == 1) {
            runReg(scanner);
        } else if (mode == 2) {

        } else {
            System.out.println("Неверный режим.");
        }

        scanner.close();
    }

    private static void runReg(Scanner scanner) {
            System.out.println("Введите значения размеров матриц A и B.");
            System.out.println("n (Строка матрицы A): ");
            int n = scanner.nextInt();

            System.out.println("m (Столбец матрицы A и строка матрицы B): ");
            int m = scanner.nextInt();

            System.out.println("k (Стобец матрицы B): ");
            int k = scanner.nextInt();

            System.out.println("Введите число потоков T: ");
            int threadsCount = scanner.nextInt();

            scanner.close();

            System.out.println("Размеры: A(" + n + "x" + m + ") * B(" + m + "x" + k + ") = C(" + n + "x" + k + ")");
            System.out.println("Число потоков: " + threadsCount);

            int[][] matrixA = new int[n][m];
            int[][] matrixB = new int[m][k];
            int[][] matrixC = new int[n][k];

            Random random = new Random(42); //42 - сид для повторяемости результатов

            //Заполняем случайными числами от 0 до 9
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < m; j++) {
                    matrixA[i][j] = random.nextInt(10);
                }
            }
            for (int i = 0; i < m; i++) {
                for (int j = 0; j < k; j++) {
                    matrixB[i][j] = random.nextInt(10);
                }
            }

            System.out.println("Матрицы успешно сгенерированы в памяти.");

            //Атомарный счётчик выполненных строк
            AtomicInteger completedRows = new AtomicInteger(0);

            //Замер начального времени
            long startTime = System.nanoTime();

            //Потоки
            Thread[] threads = new Thread[threadsCount];
            int rowsPerThread = n / threadsCount;
            int remainder = n % threadsCount;
            int startRow = 0;

            for (int t = 0; t < threadsCount; t++) {
                int rows = rowsPerThread + (t < remainder ? 1 : 0);
                int endRow = startRow + rows;

                threads[t] = new Thread(new Worker(
                        matrixA, matrixB, matrixC,
                        startRow, endRow, k, m, completedRows
                ));
                threads[t].start();
                startRow = endRow;
            }

            // Поток прогресса
            Thread progressThread = new Thread(() -> {
                int lastPercent = -1;
                while (completedRows.get() < n) {
                    int percent = (completedRows.get() * 100) / n;
                    if (percent != lastPercent && percent % 5 == 0) {
                        System.out.println("Прогресс: " + percent + "%");
                        lastPercent = percent;
                    }
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
                System.out.println("Прогресс: 100%");

            });
            progressThread.start();

            for (Thread t : threads) {
                try {
                    t.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            try {
                progressThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }


            //Замер окончания времени
            long endTime = System.nanoTime();
            double timeMs = (endTime - startTime) / 1_000_000.0;

            //Контрольная сумма
            long checksum = 0;
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < k; j++) {
                    checksum += matrixC[i][j];
                }
            }

            System.out.println("Время умножения: " + timeMs + "мс");
            System.out.println("Контрольная сумма: " + checksum);

        }

        private static void runTriaug(Scanner scanner) {
            System.out.println("Введите значения размеров матриц A и B.");
            System.out.println("Размер квадратной матрицы A: ");
            int n = scanner.nextInt();

            System.out.println("Размер столбцов матриц B и C: ");
            int k = scanner.nextInt();

            System.out.println("Введите число потоков T: ");
            int threadsCount = scanner.nextInt();

            System.out.println("Выберите режим распределения потоков (static / dynamic)");
            String distrMode = scanner.next();

            int[][] matrixA = new int[n][];
            Random random =new Random(42);
            for (int i = 0; i < n; i++) {
                matrixA[i] = new int[i + 1];
                for (int j = 0; j <= i; j++) {
                    matrixA[i][j] = random.nextInt(10);
                }
            }

            int[][] matrixB = new int[][];
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < k; j++) {
                    matrixB[i][j] = random.nextInt(10);
                }
            }


            int[][] matrixC =new int[n][k];
            System.out.println("Матрицы сгенерированы.");

            AtomicInteger completedRows = new AtomicInteger(0);
            long startTime = System.nanoTime();

            Thread[] threads = new Thread[threadsCount];

            if (distrMode.equals("static")) {
                int rowsPerThread = n / threadsCount;
                int remainder = n % threadsCount;
                int startRow = 0;

                for (int t = 0; t < threadsCount; t++) {
                    int rows = rowsPerThread + (t < remainder ? 1 : 0);
                    int endRow = startRow + rows;

                    threads[t] = new Thread(new Worker(
                            matrixA, matrixB, matrixC,
                            startRow, endRow, k, n, completedRows
                    ));
                    threads[t].start();
                    startRow = endRow;
                }
            } else if (distrMode.equals("dynamic")) {
                AtomicInteger nextRow = new AtomicInteger(0);
                for (int t = 0; t < threadsCount; t++) {
                    threads[t] = new Thread(new TriangularWorker(
                            matrixA, matrixB, matrixC,
                            n, k, nextRow, completedRows
                    ));
                    threads[t].start();
                }

            } else {
                System.out.println("Неверный режим распределения");
                return;
            }

            for (Thread t : threads) {
                try {
                    t.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            long endTime = System.nanoTime();
            double timeMs = (endTime - startTime) / 1_000_000.0;

            long checksum = 0;
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < k; j++) {
                    checksum += matrixC[i][j];
                }
            }

            System.out.println("Режим: " + distrMode);
            System.out.println("Время: " + timeMs);
            System.out.println("Контрольная сумма: " + checksum);

 
        }
}














