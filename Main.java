import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicInteger;


public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Выберите тип умножения:");
        while (true) {
            System.out.println("1. Обычное умножение");
            System.out.println("2. Нижнетреугольная матрица");
            System.out.println("3. Генерация в отдельном процессе");
            System.out.println("0. Выход");
            int mode = getValue(scanner, "Выберите действие: ", 0);

            if (mode == 1) {
                runReg(scanner);
            } else if (mode == 2) {
                runTriaug(scanner);
            } else if (mode == 0) {
                scanner.close();
                break;
            } else if (mode == 3) {
                runGenerator(scanner);
            } else {
                System.out.println("Неверный режим.");
            }
        }

    }


    private static int getValue(Scanner scanner, String prompt, int minValue) {
        while (true) {
            System.out.println(prompt);
            String input = scanner.nextLine();
            try {
                int value = Integer.parseInt(input);
                if (value >= minValue) {
                    return value;
                } else {
                    System.out.println("Ошибка: значение должно быть больше " + minValue +  ". Повторите попытку");
                }
            } catch (NumberFormatException e) {
                System.out.println("Ошибка: введено не число. Повторите попытку");
            }
        }
    }



    private static void runReg(Scanner scanner) {
            System.out.println("Введите значения размеров матриц A и B.");
            System.out.println("n (Строка матрицы A): ");
            int n = scanner.nextInt();
            scanner.nextLine();

            System.out.println("m (Столбец матрицы A и строка матрицы B): ");
            int m = scanner.nextInt();
            scanner.nextLine();

            System.out.println("k (Стобец матрицы B): ");
            int k = scanner.nextInt();
            scanner.nextLine();

            System.out.println("Введите число потоков T: ");
            int threadsCount = scanner.nextInt();
            scanner.nextLine();

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
            Thread progressThread = ProgressTracker.startProgress(completedRows, n);

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
            scanner.nextLine();

            System.out.println("Размер столбцов матриц B и C: ");
            int k = scanner.nextInt();
            scanner.nextLine();

            System.out.println("Введите число потоков T: ");
            int threadsCount = scanner.nextInt();
            scanner.nextLine();

            System.out.println("Выберите режим распределения потоков (static / dynamic)");
            String distrMode = scanner.next();
            scanner.nextLine();

            int[][] matrixA = new int[n][];
            Random random =new Random(42);
            for (int i = 0; i < n; i++) {
                matrixA[i] = new int[i + 1];
                for (int j = 0; j <= i; j++) {
                    matrixA[i][j] = random.nextInt(10);
                }
            }

            int[][] matrixB = new int[n][k];
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

                    threads[t] = new Thread(new TriangularWorker(
                            matrixA, matrixB, matrixC,
                            n, k, startRow, endRow, completedRows
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

        class ProgressTracker {
            public static Thread startProgress(AtomicInteger completedRows, int totalRows) {
                Thread progressThread = new Thread(() -> {
                    int lastPercent = -1;
                    while (completedRows.get() < totalRows) {
                        int percent = (completedRows.get() * 100) / totalRows;
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
                return progressThread;
            }

            public static void waitThread(Thread thread) {
                if (thread != null) {
                    try {
                        thread.join();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        e.printStackTrace();
                    }
                }
            }
        }

        private static void runGenerator (Scanner scanner){

            System.out.println("Генерация в отдельном процессе IPC");
            int n = getValue(scanner, "Строки A и B: ", 1);
            int m = getValue(scanner, "Столбцы A и строки B: ", 1);
            int k = getValue(scanner, "Столбцы B и C: ", 1);
            int threadsCount = getValue(scanner, "Число потоков", 1);

            System.out.println("Запуск процесса...");
            try {
                ProcessBuilder pb = new ProcessBuilder("java", "MatrixGenerator",
                        String.valueOf(n), String.valueOf(m), String.valueOf(k));

                pb.redirectError(ProcessBuilder.Redirect.INHERIT);
                Process process = pb.start();

                Scanner processScanner = new Scanner(process.getInputStream());


                if (!processScanner.hasNextInt()) throw new RuntimeException("Ошибка: процесс не вернул размеры.");
                int readN = processScanner.nextInt();
                int readM = processScanner.nextInt();
                int readK = processScanner.nextInt();

                int[][] matrixA = new int[readN][readM];
                for (int i = 0; i < readN; i++) {
                    for (int j = 0; j < readM; j++) {
                        matrixA[i][j] = processScanner.nextInt();
                    }
                }

                int[][] matrixB = new int[readM][readK];
                for (int i = 0; i < readM; i++) {
                    for (int j = 0; j < readK; j++) {
                        matrixB[i][j] = processScanner.nextInt();
                    }
                }


                processScanner.close();

                int exitCode = process.waitFor();
                if (exitCode != 0) {
                    System.out.println("Процесс завершился с ошибкой: " + exitCode);
                    return;
                }
                System.out.println("Матрицы успешно получены через IPC.");

                int[][] matrixC = new int[readN][readK];
                AtomicInteger completedRows = new AtomicInteger(0);
                long startTime = System.nanoTime();


                Thread[] threads = new Thread[threadsCount];
                int rowsThread = readN / threadsCount;
                int remainder = readN % threadsCount;
                int startRow = 0;

                for (int t = 0; t < threadsCount; t++) {
                    int rows = rowsThread + (t < remainder ? 1 : 0);
                    int endRow = startRow + rows;

                    threads[t] = new Thread(new Worker(
                            matrixA, matrixB, matrixC,
                            startRow, endRow, readK, readM, completedRows
                    ));
                    threads[t].start();
                    startRow = endRow;

                }

                Thread progressThread = ProgressTracker.startProgress(completedRows, readN);

                for (Thread t : threads) {
                    try {
                        t.join();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                ProgressTracker.waitThread((progressThread));

                long endTime = System.nanoTime();
                double timesMs = (endTime - startTime) / 1_000_000.0;

                long checksum = 0;
                for (int i = 0; i < readN; i++) {
                    for (int j = 0; j < readK; j++) {
                        checksum += matrixC[i][j];
                    }
                }

                System.out.println("Время умножения: " + timesMs + "ms");
                System.out.println("Контрольная сумма: " + checksum);

            } catch (Exception e) {
                System.out.println("Ошибка работы с процкссом.");
                e.printStackTrace();
            }

    }

}





