package com.asg.yer.youzi.Others;

/**
 * Created by apple on 2017/12/27.
 */

public class UpdateUiEvent {
    public int percent;
    public String strCards;
    public int[] cards;


    public UpdateUiEvent(String strCards) {
        this.strCards = strCards;
    }

    public UpdateUiEvent(int[] cards) {
        this.cards = cards;
    }

    public UpdateUiEvent(int percent, String strCards) {
        this.percent = percent;
        this.strCards = strCards;
    }


}
