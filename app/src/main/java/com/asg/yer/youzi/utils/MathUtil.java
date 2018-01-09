package com.asg.yer.youzi.utils;

/**
 * Created by apple on 2017/12/28.
 */

public class MathUtil {

    /**
     * 数学中的统计方法，用于整数，A(3,2)即3*2,A(5,3)即5*4*3
     * @param first 开始的数
     * @param second 个数
     * @return
     */
    public static int A(int first , int second) {
        int tmp = first;
        int result = first;
        int count = 0;
        while(count < second-1) {
            if(second ==1) {
                return first;
            } else {
                count++;
                tmp--;
                System.out.println(result + "*" + tmp);
                result = result * tmp;
            }
        }
        return result;
    }

    /**
     * 实现了数学中阶乘的方法 factorialA(5)即5!
     * @param number
     * @return
     */
    public static int factorialA(int number) {
        return A(number,number);
    }

    /**
     * 实现了数学中的组合方法C(n,r)即 n!/(n-r)!r!
     * @param first
     * @param second
     * @return
     */
    public static double C(int n, int r) {
        return factorialA(n)/(factorialA(n-r)*factorialA(r));
    }
}
