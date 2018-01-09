package com.asg.yer.youzi.Others;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.NotNull;
import org.greenrobot.greendao.annotation.Generated;

/**
 * Created by YER on 2017/12/20.
 * 记录一盘游戏
 */
@Entity
public class CardGame {
    @Id(autoincrement = true)
    private Long id;
    //对手数量
    private Integer opponentsNum;

    @NotNull
    private String strCards;
    //String.ToCharArray

    public String getStrCards() {
        return this.strCards;
    }

    public void setStrCards(String strCards) {
        this.strCards = strCards;
    }

    public Integer getOpponentsNum() {
        return this.opponentsNum;
    }

    public void setOpponentsNum(Integer opponentsNum) {
        this.opponentsNum = opponentsNum;
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Generated(hash = 1583014319)
    public CardGame(Long id, Integer opponentsNum, @NotNull String strCards) {
        this.id = id;
        this.opponentsNum = opponentsNum;
        this.strCards = strCards;
    }

    @Generated(hash = 1870345175)
    public CardGame() {
    }
}
