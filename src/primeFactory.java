/**
 * @Description:
 * @author: tsw
 * @version: 1.0 2011-6-27
 * @since: JDK1.6
 * @copyright: 中国气象信息中心 2011-6-27
 */

public class primeFactory {

    /**
     * @param args
     */
    public static void main (String[] args) {
        // long target = 600851475143L;
        int target = 10000;
        long begin = System.currentTimeMillis();
        long sum = 2;
        System.out.println(begin);
        int[] data = new int[target / 4];
        data[0] = 2;
        int index = 1;
        outer : for (int i = 3; i < target; i++) {
            // if (isPrime(i)) {
            // sum += i;
            // }
            int top = (int) Math.sqrt(i) + 1;
            for (int j = 0; j < index; j++) {
                int one = data[j];
                if (one > top) {
                    break;
                } else if ( (i % one) == 0) {
                    continue outer;
                }
            }
            data[index++] = i;
            sum += i;
        }
        System.out.println(sum);
        System.out.println(System.currentTimeMillis());
    }

    private static boolean isPrime (long num) {
        for (int i = 2; i < Math.sqrt(num) + 1; i++) {
            if (num % i == 0) {
                return false;
            }
        }
        return true;
    }

    // 判断质数的特点为,是否是之前质数的倍数
    public static int Cal (int max) {// 求指定数字下素数的和
        int[] data = new int[max];
        for (int i = 3; i < max; i += 2) {// 将奇数放入数组
            data[i] = i;
        }
        int sum = 2;
        for (int i = 3; i < max; i += 2) {
            if (data[i] > 0) {// 顺序判断奇数数组
                sum += i;
                for (int j = i * i; j < max; j += 2 * i) {// 从i的平方往后遍历，每个i的奇数倍置为0
                    data[j] = 0;
                }
            }
        }
        return sum;
    }
}
