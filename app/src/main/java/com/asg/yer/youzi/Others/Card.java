package com.asg.yer.youzi.Others;

/**
 * Created by YER on 2017/12/18.
 * 黑桃 1，红桃 2，梅花 3，方块 4;
 * T 表10  Y表小王 Z表大王
 */

public class Card {

    //	private static int[] pic = {R.drawable.c2, R.drawable.c3, R.drawable.c4, R.drawable.c5,
//		R.drawable.c6, R.drawable.c7, R.drawable.c8, R.drawable.c9, R.drawable.c10,
//		R.drawable.cj, R.drawable.cq, R.drawable.ck, R.drawable.c1, R.drawable.d2,
//		R.drawable.d3, R.drawable.d4, R.drawable.d5, R.drawable.d6, R.drawable.d7,
//		R.drawable.d8, R.drawable.d9, R.drawable.d10, R.drawable.dj, R.drawable.dq,
//		R.drawable.dk, R.drawable.d1, R.drawable.s2, R.drawable.s3, R.drawable.s4,
//		R.drawable.s5, R.drawable.s6, R.drawable.s7, R.drawable.s8, R.drawable.s9,
//		R.drawable.s10, R.drawable.sj, R.drawable.sq, R.drawable.sk, R.drawable.s1,
//		R.drawable.h2, R.drawable.h3, R.drawable.h4, R.drawable.h5, R.drawable.h6,
//		R.drawable.h7, R.drawable.h8, R.drawable.h9, R.drawable.h10, R.drawable.hj,
//		R.drawable.hq, R.drawable.hk, R.drawable.h1};
    private static int[] rank = {
            1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13,
            1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13,
            1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13,
            1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13};
    private static int[] suit = {
            1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,  //梅花
            2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2,  //方块
            3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3,  //黑桃
            4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4}; //红桃
    public int pic_rsc;
    public int Rank;
    public int Suit;

    public Card(int i) {
        // TODO Auto-generated constructor stub
//		pic_rsc = pic[i];
        Rank = rank[i];
        Suit = suit[i];
    }

    //	public Integer getPic(){ return pic_rsc; }
    public Integer getRank(){ return Rank; }
    public Integer getSuit(){ return Suit; }



    /**
     * @param args
     */
    public static void main(String[] args) {
        // TODO Auto-generated method stub

    }

}
