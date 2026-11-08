import java.util.Random;

public class MatrixGenerator {
    public static void main(String[] args) {
        if (args.length != 3) {
            System.out.println("Использование: java MatrixGenerator <n> <m> <k>");
            System.exit(1);
        }


        int n = Integer.parseInt(args[0]);
        int m = Integer.parseInt(args[1]);
        int k = Integer.parseInt(args[2]);

        System.out.println(n + " " + m + " " + k);
        Random random = new Random(42);

        //Генерация матрицы A
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                System.out.println(random.nextInt(10));
                if (j < m - 1) System.out.println(" ");

            }
            System.out.println();
        }

        //Генерация матрицы B
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < k; j++) {
                System.out.println(random.nextInt(10));
                if (j < k-1) System.out.println(" ");
            }
            System.out.println();
        }
    }
}
