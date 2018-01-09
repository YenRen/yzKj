package com.asg.yer.youzi.utils;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import com.asg.yer.youzi.Others.Card;
import com.asg.yer.youzi.Others.UpPecentEvent;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import de.greenrobot.event.EventBus;

/**
 * Created by YER on 2018/1/4.
 * <p>
 * 德州扑克算法
 */

public class Caculation {

    private static Caculation caculation;
    private Context context;
    private int intAccuracy = 400;
    ArrayList<Card> lstPlayer1 = new ArrayList<Card>();
    ArrayList<Card> lstTable = new ArrayList<Card>();
    ArrayList<Card> lstHold = new ArrayList<Card>();
    ArrayList<ArrayList<Card>> lstAllHands = new ArrayList<ArrayList<Card>>();
    ArrayList<Card> lstPlayer1Odds = new ArrayList<Card>();
    ArrayList<Card> lstPlayer2Odds = new ArrayList<Card>();
    ArrayList<Card> lstPlayer3Odds = new ArrayList<Card>();
    ArrayList<Card> lstPlayer4Odds = new ArrayList<Card>();
    ArrayList<Card> lstPlayer5Odds = new ArrayList<Card>();
    ArrayList<Card> lstPlayer6Odds = new ArrayList<Card>();
    ArrayList<Card> lstPlayer7Odds = new ArrayList<Card>();
    ArrayList<Card> lstPlayer8Odds = new ArrayList<Card>();
    ArrayList<Card> lstPlayer9Odds = new ArrayList<Card>();
    ArrayList<Card> lstTableOdds = new ArrayList<Card>();
    DecimalFormat decOdds = new DecimalFormat("00.00");
    ArrayList<Integer> intHandRank = new ArrayList<Integer>();
    long mLastClick = 0;
    Card[] cards = new Card[52];
    Integer intIndex = 0, intClub = 0, intSpade = 0, intDiamond = 0, intHeart = 0, intRandom, intTotalGames = 0,
            intPlayer1Wins = 0, intPlayerRankHold = 0, intTotalPlayers = 0, intTie = 0, intLose = 0,
            intCardSrc = 0;
    String strWin = "", strTie = "", strLose = "";
    Double dblOdds = 0.00, dblTie = 0.00, dblLose = 0.00;
    AsyncTaskRunner runner = new AsyncTaskRunner();
    Boolean FlagCancelled = false;
    Boolean bolPlayer1Wins = true;
    Boolean bolTie = false;
    Boolean bolGame = true;

    ArrayList<Integer> lstRandom = new ArrayList<Integer>();
    ArrayList<Card> lstBurn = new ArrayList<Card>();


    public static Caculation getInstant(Context context) {
        if (null == caculation) {
            caculation = new Caculation(context);
        }
        return caculation;
    }

    public Caculation(Context context) {
        this.context = context;
    }

    public void setIntTotalPlayers(int Players) {
        this.intTotalPlayers = Players;
    }

    public void initCaculation(int Players) {
        intTotalPlayers = Players;
        //Loads the deck and cards to the main view
        for (int i = 0; i < 52; i++) {
            cards[i] = new Card(i);
            //Adds list for random shuffle
            lstRandom.add(i);
        }

//        Makes sure async task stops if the program has been exited and started up again
        runner.cancel(true);
    }


    /*
 *开始计算概率
 * @param view
 */
    public void doCalculation(int[] jniUselyInfo) {
        Reset();
        initCardsList(jniUselyInfo);
        runner.cancel(true);
        runner.cancel(false);
        runner = new AsyncTaskRunner();
        runner.execute();
    }


    /*
    * 初使化牌
    */
    int type, rank;

    private void initCardsList(int[] inputCards) {

        for (int i = 0; i < inputCards.length; i++) {

            if (i % 2 == 0) {
                if ((inputCards[i] + "").equals("n")) {
                    inputCards[i] = '1';
                }
                type = Integer.parseInt(inputCards[i] + "") - 1;
            }

            if (i % 2 == 1) {
                if (inputCards[i] != 1) {
                    rank = inputCards[i] - 1;
                } else {
                    rank = 13;
                }
                Logger.e("Exception", "doInBackground: ==============" + (type * 13 + rank));
                if (lstPlayer1.size() < 2) {
                    lstPlayer1.add(cards[type * 13 + rank - 1]);
                } else {
                    lstTable.add(cards[type * 13 + rank - 1]);
                }

            }
        }

    }


    /***************************Async Task to calculate odds of winning************************************/
    private class AsyncTaskRunner extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... params) {
            Logger.e("Exception", "doInBackground: ==============********************");
            // TODO Auto-generated method stub
            try {
                for (int i = 0; i < intAccuracy; i++) {
                    synchronized (this) {
                        checkWinner();
                    }
                    if (i % 100 == 0) {
                        publishProgress(strWin, strTie, strLose);
//						publishProgress(strTie);
//						publishProgress(strLose);
                    }

                    if (isCancelled() || FlagCancelled) {

                        break;
                    }


                }
            } catch (Exception e) {

                strWin = e.toString(); //String.valueOf(Thread.currentThread().getStackTrace()[1].getLineNumber());

            }

            return null;
        }

        @Override
        protected void onProgressUpdate(String... text) {
//            winText.setText(text[0]);
//            pieText.setText(text[1]);
//            loseText.setText(text[2]);
            Logger.e("Exception", "onProgressUpdate: ==============000000000000");
        }

        @Override
        protected void onPostExecute(String result) {

            EventBus.getDefault().post(new UpPecentEvent(strWin));

//            winText.setText(strWin);
//            pieText.setText(strTie);
//            loseText.setText(strLose);
            Logger.e("Exception", "onPostExecute: ==============111111111111===" + strWin);
//            MainActivity.this.isFinishing();
        }

        @Override
        protected void onPreExecute() {
            Logger.e("Exception", "onPreExecute: ==============2222222222");
        }

        @Override
        protected void onCancelled() {
            return;
        }
    }


    //Sorts the cards based on Rank - Highest to Lowest
    class CardRankSort implements Comparator<Card> {
        public int compare(Card card1, Card card2) {
            return card2.getRank().compareTo(card1.getRank());
        }
    }

    //Sorts cards by suit - Clubs, Diamonds, Spade, Hearts
    class CardSuitSort implements Comparator<Card> {
        public int compare(Card card1, Card card2) {
            return card1.getSuit().compareTo(card2.getSuit());
        }
    }


    /*********************************Method to run through simulation******************************************/
    public void checkWinner() {
        lstAllHands.clear();
        intHandRank.clear();
        lstPlayer2Odds.clear();
        lstPlayer3Odds.clear();
        lstPlayer4Odds.clear();
        lstPlayer5Odds.clear();
        lstPlayer6Odds.clear();
        lstPlayer7Odds.clear();
        lstPlayer8Odds.clear();
        lstPlayer9Odds.clear();
        lstTableOdds.clear();
        lstPlayer1Odds.clear();
        lstBurn.clear();
        intPlayerRankHold = 0;
        dblOdds = 0.00;
        dblTie = 0.00;
        dblLose = 0.00;

        lstPlayer1Odds.addAll(lstPlayer1);
        lstTableOdds.addAll(lstTable);

        Collections.shuffle(lstRandom);


        //Adds another card to player 1's hand if two have not been selected
        while (lstPlayer1Odds.size() < 2) {
//			intRandom = rnd.nextInt(52);
            Collections.shuffle(lstRandom);

            if (!lstPlayer1Odds.contains(cards[lstRandom.get(0)]) && !lstTableOdds.contains(cards[lstRandom.get(0)]) && lstBurn.add(cards[lstRandom.get(0)])) {
                lstPlayer1Odds.add(cards[lstRandom.get(0)]);
            }
        }

        if (intTotalPlayers > 1) {

            //selects two random cards for the player 2's hand
            while (lstPlayer2Odds.size() < 2) {
//				intRandom = rnd.nextInt(52);
                Collections.shuffle(lstRandom);
                if (!lstPlayer1Odds.contains(cards[lstRandom.get(0)]) && !lstPlayer2Odds.contains(cards[lstRandom.get(0)])
                        && !lstTableOdds.contains(cards[lstRandom.get(0)]) && lstBurn.add(cards[lstRandom.get(0)])) {
                    lstPlayer2Odds.add(cards[lstRandom.get(0)]);
                }
            }
        }


        if (intTotalPlayers > 2) {

            //selects two random cards for the player 2's hand
            while (lstPlayer3Odds.size() < 2) {
//				intRandom = rnd.nextInt(52);
                Collections.shuffle(lstRandom);
                if (!lstPlayer1Odds.contains(cards[lstRandom.get(0)]) && !lstPlayer2Odds.contains(cards[lstRandom.get(0)])
                        && !lstPlayer3Odds.contains(cards[lstRandom.get(0)]) && !lstTableOdds.contains(cards[lstRandom.get(0)])
                        && lstBurn.add(cards[lstRandom.get(0)])) {
                    lstPlayer3Odds.add(cards[lstRandom.get(0)]);
                }
            }
        }

        if (intTotalPlayers > 3) {

            //selects two random cards for the player 2's hand
            while (lstPlayer4Odds.size() < 2) {
//				intRandom = rnd.nextInt(52);
                Collections.shuffle(lstRandom);
                if (!lstPlayer1Odds.contains(cards[lstRandom.get(0)]) && !lstPlayer2Odds.contains(cards[lstRandom.get(0)])
                        && !lstPlayer3Odds.contains(cards[lstRandom.get(0)]) && !lstPlayer4Odds.contains(cards[lstRandom.get(0)])
                        && !lstTableOdds.contains(cards[lstRandom.get(0)]) && lstBurn.add(cards[lstRandom.get(0)])) {
                    lstPlayer4Odds.add(cards[lstRandom.get(0)]);
                }
            }
        }

        if (intTotalPlayers > 4) {

            //selects two random cards for the player 2's hand
            while (lstPlayer5Odds.size() < 2) {
//				intRandom = rnd.nextInt(52);
                Collections.shuffle(lstRandom);
                if (!lstPlayer1Odds.contains(cards[lstRandom.get(0)]) && !lstPlayer2Odds.contains(cards[lstRandom.get(0)])
                        && !lstPlayer3Odds.contains(cards[lstRandom.get(0)]) && !lstPlayer4Odds.contains(cards[lstRandom.get(0)])
                        && !lstPlayer5Odds.contains(cards[lstRandom.get(0)]) && !lstTableOdds.contains(cards[lstRandom.get(0)])
                        && lstBurn.add(cards[lstRandom.get(0)])) {
                    lstPlayer5Odds.add(cards[lstRandom.get(0)]);
                }
            }
        }

        if (intTotalPlayers > 5) {

            //selects two random cards for the player 2's hand
            while (lstPlayer6Odds.size() < 2) {
//				intRandom = rnd.nextInt(52);
                Collections.shuffle(lstRandom);
                if (!lstPlayer1Odds.contains(cards[lstRandom.get(0)]) && !lstPlayer2Odds.contains(cards[lstRandom.get(0)])
                        && !lstPlayer3Odds.contains(cards[lstRandom.get(0)]) && !lstPlayer4Odds.contains(cards[lstRandom.get(0)])
                        && !lstPlayer5Odds.contains(cards[lstRandom.get(0)]) && !lstPlayer6Odds.contains(cards[lstRandom.get(0)])
                        && !lstTableOdds.contains(cards[lstRandom.get(0)]) && lstBurn.add(cards[lstRandom.get(0)])) {
                    lstPlayer6Odds.add(cards[lstRandom.get(0)]);
                }
            }
        }

        if (intTotalPlayers > 6) {
            //selects two random cards for the player 2's hand
            while (lstPlayer7Odds.size() < 2) {
//				intRandom = rnd.nextInt(52);
                Collections.shuffle(lstRandom);
                if (!lstPlayer1Odds.contains(cards[lstRandom.get(0)]) && !lstPlayer2Odds.contains(cards[lstRandom.get(0)])
                        && !lstPlayer3Odds.contains(cards[lstRandom.get(0)]) && !lstPlayer4Odds.contains(cards[lstRandom.get(0)])
                        && !lstPlayer5Odds.contains(cards[lstRandom.get(0)]) && !lstPlayer6Odds.contains(cards[lstRandom.get(0)])
                        && !lstPlayer7Odds.contains(cards[lstRandom.get(0)]) && !lstTableOdds.contains(cards[lstRandom.get(0)])
                        && lstBurn.add(cards[lstRandom.get(0)])) {
                    lstPlayer7Odds.add(cards[lstRandom.get(0)]);
                }
            }
        }

        if (intTotalPlayers > 7) {
            //selects two random cards for the player 2's hand
            while (lstPlayer8Odds.size() < 2) {
                Collections.shuffle(lstRandom);

                if (!lstPlayer1Odds.contains(cards[lstRandom.get(0)]) && !lstPlayer2Odds.contains(cards[lstRandom.get(0)])
                        && !lstPlayer3Odds.contains(cards[lstRandom.get(0)]) && !lstPlayer4Odds.contains(cards[lstRandom.get(0)])
                        && !lstPlayer5Odds.contains(cards[lstRandom.get(0)]) && !lstPlayer6Odds.contains(cards[lstRandom.get(0)])
                        && !lstPlayer7Odds.contains(cards[lstRandom.get(0)]) && !lstPlayer8Odds.contains(cards[lstRandom.get(0)])
                        && !lstTableOdds.contains(cards[lstRandom.get(0)]) && lstBurn.add(cards[lstRandom.get(0)])) {
                    lstPlayer8Odds.add(cards[lstRandom.get(0)]);
                }
            }
        }

        if (intTotalPlayers > 8) {

            //selects two random cards for the player 2's hand
            while (lstPlayer9Odds.size() < 2) {
                Collections.shuffle(lstRandom);

                if (!lstPlayer1Odds.contains(cards[lstRandom.get(0)]) && !lstPlayer2Odds.contains(cards[lstRandom.get(0)])
                        && !lstPlayer3Odds.contains(cards[lstRandom.get(0)]) && !lstPlayer4Odds.contains(cards[lstRandom.get(0)])
                        && !lstPlayer5Odds.contains(cards[lstRandom.get(0)]) && !lstPlayer6Odds.contains(cards[lstRandom.get(0)])
                        && !lstPlayer7Odds.contains(cards[lstRandom.get(0)]) && !lstPlayer8Odds.contains(cards[lstRandom.get(0)])
                        && !lstPlayer9Odds.contains(cards[lstRandom.get(0)]) && !lstTableOdds.contains(cards[lstRandom.get(0)])
                        && lstBurn.add(cards[lstRandom.get(0)])) {
                    lstPlayer9Odds.add(cards[lstRandom.get(0)]);
                }
            }
        }

        //Add cards to table if 5 haven't already been selected
        while (lstTableOdds.size() < 5) {
            //Burn cards
            if (lstTableOdds.size() < 1 && lstBurn.size() == 0) {
                lstBurn.add(cards[lstRandom.get(0)]);
            } else if (lstTableOdds.size() == 3 && lstBurn.size() == 1) {
                lstBurn.add(cards[lstRandom.get(0)]);
            } else if (lstTableOdds.size() == 4 && lstBurn.size() == 2) {
                lstBurn.add(cards[lstRandom.get(0)]);
            }

            Collections.shuffle(lstRandom);
            if (!lstTableOdds.contains(cards[lstRandom.get(0)]) && !lstPlayer1Odds.contains(cards[lstRandom.get(0)])
                    && lstBurn.add(cards[lstRandom.get(0)])) {
                lstTableOdds.add(cards[lstRandom.get(0)]);//
            }
        }

        //Adds each player's cards to the master list of all hands
        if (!lstPlayer1Odds.isEmpty()) {
            lstPlayer1Odds.addAll(lstTableOdds);
            lstAllHands.add(lstPlayer1Odds);

        }
        if (!lstPlayer2Odds.isEmpty()) {
            lstPlayer2Odds.addAll(lstTableOdds);
            lstAllHands.add(lstPlayer2Odds);
        }
        if (!lstPlayer3Odds.isEmpty()) {
            lstPlayer3Odds.addAll(lstTableOdds);
            lstAllHands.add(lstPlayer3Odds);
        }
        if (!lstPlayer4Odds.isEmpty()) {
            lstPlayer4Odds.addAll(lstTableOdds);
            lstAllHands.add(lstPlayer4Odds);
        }
        if (!lstPlayer5Odds.isEmpty()) {
            lstPlayer5Odds.addAll(lstTableOdds);
            lstAllHands.add(lstPlayer5Odds);
        }
        if (!lstPlayer6Odds.isEmpty()) {
            lstPlayer6Odds.addAll(lstTableOdds);
            lstAllHands.add(lstPlayer6Odds);
        }
        if (!lstPlayer7Odds.isEmpty()) {
            lstPlayer7Odds.addAll(lstTableOdds);
            lstAllHands.add(lstPlayer7Odds);
        }
        if (!lstPlayer8Odds.isEmpty()) {
            lstPlayer8Odds.addAll(lstTableOdds);
            lstAllHands.add(lstPlayer8Odds);
        }
        if (!lstPlayer9Odds.isEmpty()) {
            lstPlayer9Odds.addAll(lstTableOdds);
            lstAllHands.add(lstPlayer9Odds);
        }

        for (ArrayList<Card> lstPlayerHand : lstAllHands) {
            //Adds cards on table to each player's hand
//			lstPlayerHand.addAll(lstTable);
            //Sorts Rank
            Collections.sort(lstPlayerHand, new CardRankSort());

            //Adds blank rank so later set methods work
            intHandRank.add(0);

            //Clears the Hold list for each player
            lstHold.removeAll(lstHold);

            //Resets suit count
            intClub = 0;
            intDiamond = 0;
            intSpade = 0;
            intHeart = 0;

/***************************Straight Flush & Flush ***************************/
            //Loop to count the number of each suit and then add the card to Hold list
            for (Card cards : lstPlayerHand) {

                if (cards.getSuit() == 1) {
                    intClub++;
                    lstHold.add(cards);
                }

                if (cards.getSuit() == 2) {
                    intDiamond++;
                    lstHold.add(cards);

                }

                if (cards.getSuit() == 3) {
                    intSpade++;
                    lstHold.add(cards);

                }

                if (cards.getSuit() == 4) {
                    intHeart++;
                    lstHold.add(cards);
                }

            }

            if (intClub > 4 || intDiamond > 4 || intSpade > 4 || intHeart > 4) {
                //Sorts Suit - Club, Diamond, Spade, then Hearts
                Collections.sort(lstHold, new CardSuitSort());

                //Checks straight flush
                for (int i = 0; i < lstHold.size() - 4; i++) {
                    if ((lstHold.get(i).Rank - lstHold.get(i + 1).Rank == 1) &&
                            (lstHold.get(i + 1).Rank - lstHold.get(i + 2).Rank == 1) &&
                            (lstHold.get(i + 2).Rank - lstHold.get(i + 3).Rank == 1) &&
                            (lstHold.get(i + 3).Rank - lstHold.get(i + 4).Rank == 1) &&
                            (lstHold.get(i).Suit == lstHold.get(i + 1).Suit) &&
                            (lstHold.get(i).Suit == lstHold.get(i + 2).Suit) &&
                            (lstHold.get(i).Suit == lstHold.get(i + 3).Suit) &&
                            (lstHold.get(i).Suit == lstHold.get(i + 4).Suit)) {
                        lstPlayerHand.removeAll(lstPlayerHand);
                        lstPlayerHand.add(lstHold.get(i));
                        lstPlayerHand.add(lstHold.get(i + 1));
                        lstPlayerHand.add(lstHold.get(i + 2));
                        lstPlayerHand.add(lstHold.get(i + 3));
                        lstPlayerHand.add(lstHold.get(i + 4));
                        intHandRank.set(intPlayerRankHold, 8);
                        break;
                    }
                }

                if (intHandRank.get(intPlayerRankHold) != 8) {
                    //Checks Ace 2 straight flush if Ace is in first index
                    for (int i = 0; i < lstHold.size() - 3; i++) {
                        if (lstHold.get(i).Rank == 4 && lstHold.get(i + 1).Rank == 3 &&
                                lstHold.get(i + 2).Rank == 2 && lstHold.get(i + 3).Rank == 1 &&
                                (lstHold.get(0).Rank == 13 &&
                                        lstHold.get(0).Suit == lstHold.get(i).Suit) &&
                                (lstHold.get(i).Suit == lstHold.get(i + 1).Suit) &&
                                (lstHold.get(i).Suit == lstHold.get(i + 2).Suit) &&
                                (lstHold.get(i).Suit == lstHold.get(i + 3).Suit)) {
                            lstPlayerHand.removeAll(lstPlayerHand);
                            lstPlayerHand.add(lstHold.get(i));
                            lstPlayerHand.add(lstHold.get(i + 1));
                            lstPlayerHand.add(lstHold.get(i + 2));
                            lstPlayerHand.add(lstHold.get(i + 3));
                            lstPlayerHand.add(lstHold.get(0));
                            intHandRank.set(intPlayerRankHold, 8);
                            break;
                        }
                    }

                    //Checks Ace 2 straight flush if Ace is in second index
                    for (int i = 0; i < lstHold.size() - 3; i++) {
                        if (lstHold.get(i).Rank == 4 && lstHold.get(i + 1).Rank == 3 &&
                                lstHold.get(i + 2).Rank == 2 && lstHold.get(i + 3).Rank == 1 &&
                                (lstHold.get(1).Rank == 13 &&
                                        lstHold.get(1).Suit == lstHold.get(i).Suit) &&
                                (lstHold.get(i).Suit == lstHold.get(i + 1).Suit) &&
                                (lstHold.get(i).Suit == lstHold.get(i + 2).Suit) &&
                                (lstHold.get(i).Suit == lstHold.get(i + 3).Suit)) {
                            lstPlayerHand.removeAll(lstPlayerHand);
                            lstPlayerHand.add(lstHold.get(i));
                            lstPlayerHand.add(lstHold.get(i + 1));
                            lstPlayerHand.add(lstHold.get(i + 2));
                            lstPlayerHand.add(lstHold.get(i + 3));
                            lstPlayerHand.add(lstHold.get(1));
                            intHandRank.set(intPlayerRankHold, 8);
                            break;
                        }
                    }
                }
/***********************************FLUSH***************************************/
                if (intHandRank.get(intPlayerRankHold) < 5) {

                    if (intClub > 4) {
                        lstPlayerHand.removeAll(lstPlayerHand);
                        for (int j = 0; j < lstHold.size(); j++) {
                            if (lstHold.get(j).Suit == 1) {
                                lstPlayerHand.add(lstHold.get(j));
                            }
                            if (lstPlayerHand.size() == 5) {
                                intHandRank.set(intPlayerRankHold, 5);
                                break;
                            }
                        }
                    }
                    if (intDiamond > 4) {
                        lstPlayerHand.removeAll(lstPlayerHand);
                        for (int j = 0; j < lstHold.size(); j++) {
                            if (lstHold.get(j).Suit == 2) {
                                lstPlayerHand.add(lstHold.get(j));
                            }
                            if (lstPlayerHand.size() == 5) {
                                intHandRank.set(intPlayerRankHold, 5);
                                break;
                            }
                        }
                    }
                    if (intSpade > 4) {
                        lstPlayerHand.removeAll(lstPlayerHand);
                        for (int j = 0; j < lstHold.size(); j++) {
                            if (lstHold.get(j).Suit == 3) {
                                lstPlayerHand.add(lstHold.get(j));
                            }
                            if (lstPlayerHand.size() == 5) {
                                intHandRank.set(intPlayerRankHold, 5);
                                break;
                            }
                        }
                    }
                    if (intHeart > 4) {
                        lstPlayerHand.removeAll(lstPlayerHand);
                        for (int j = 0; j < lstHold.size(); j++) {
                            if (lstHold.get(j).Suit == 4) {
                                lstPlayerHand.add(lstHold.get(j));
                            }
                            if (lstPlayerHand.size() == 5) {
                                intHandRank.set(intPlayerRankHold, 5);
                                break;
                            }
                        }
                    }
                }
            }
            /**************************Four of a Kind**********************************/
            if (intHandRank.get(intPlayerRankHold) < 7) {
                for (int i = 0; i < lstHold.size() - 3; i++) {
                    if (lstHold.get(i).Rank == lstHold.get(i + 1).Rank &&
                            lstHold.get(i + 1).Rank == lstHold.get(i + 2).Rank &&
                            lstHold.get(i + 2).Rank == lstHold.get(i + 3).Rank) {

                        intHandRank.set(intPlayerRankHold, 7);

                        lstPlayerHand.removeAll(lstPlayerHand);
                        lstPlayerHand.add(lstHold.get(i));
                        lstPlayerHand.add(lstHold.get(i + 1));
                        lstPlayerHand.add(lstHold.get(i + 2));
                        lstPlayerHand.add(lstHold.get(i + 3));
                        break;
                    }
                }

                //Adds 5th card if four of a kind
                if (intHandRank.get(intPlayerRankHold) == 7) {
                    for (int i = 0; i < lstHold.size(); i++) {
                        if (!lstPlayerHand.contains(lstHold.get(i))) {
                            lstPlayerHand.add(lstHold.get(i));
                            break;
                        }
                    }
                }
            }
/***************************************Full House******************************/
            if (intHandRank.get(intPlayerRankHold) < 6) {
                synchronized (this) {
                    //lstPlayerHand.removeAll(lstPlayerHand);

                    //First Loop to get the three of a kind
                    for (int i = 0; i < lstHold.size() - 2; i++) {
                        if (lstHold.get(i).Rank == lstHold.get(i + 1).Rank &&
                                lstHold.get(i).Rank == lstHold.get(i + 2).Rank &&
                                (!lstPlayerHand.contains(lstHold.get(i).Rank))) {
                            lstPlayerHand.removeAll(lstPlayerHand);
                            lstPlayerHand.add(lstHold.get(i));
                            lstPlayerHand.add(lstHold.get(i + 1));
                            lstPlayerHand.add(lstHold.get(i + 2));
                            intHandRank.set(intPlayerRankHold, 6);
                            break;
                        }
                    }

                    if (intHandRank.get(intPlayerRankHold) == 6) {
                        //Second Loop to check the pair
                        for (int i = 0; i < lstHold.size() - 1; i++) {
                            if (!lstPlayerHand.contains(lstHold.get(i)) &&
                                    lstHold.get(i).Rank == lstHold.get(i + 1).Rank) {
                                lstPlayerHand.add(lstHold.get(i));
                                lstPlayerHand.add(lstHold.get(i + 1));
                            }
                            if (lstPlayerHand.size() == 5) {
                                intHandRank.set(intPlayerRankHold, 6);
                                break;
                            }

                        }
                        if (lstPlayerHand.size() != 5 || intHandRank.get(intPlayerRankHold) == 5) {
                            intHandRank.set(intPlayerRankHold, 0);
                        }
                    }
                }
            }
/**********************************Straight*************************************/
            if (intHandRank.get(intPlayerRankHold) < 4) {
                ArrayList<Integer> lstStraight = new ArrayList<Integer>();
                ArrayList<Integer> lstStrIndex = new ArrayList<Integer>();

                for (int j = 0; j < lstHold.size(); j++) {
                    if (!lstStraight.contains(lstHold.get(j).Rank)) {
                        lstStraight.add(lstHold.get(j).Rank);
                        lstStrIndex.add(j);
                    }
                }


                if (lstStraight.size() > 4) {
                    //Loop to check normal straight
                    for (int i = 0; i < lstStraight.size() - 4; i++) {
                        if ((lstStraight.get(i) - lstStraight.get(i + 1) == 1) &&
                                (lstStraight.get(i + 1) - lstStraight.get(i + 2) == 1) &&
                                (lstStraight.get(i + 2) - lstStraight.get(i + 3) == 1) &&
                                (lstStraight.get(i + 3) - lstStraight.get(i + 4) == 1)) {
                            lstPlayerHand.removeAll(lstPlayerHand);
                            lstPlayerHand.add(lstHold.get(lstStrIndex.get(i)));
                            lstPlayerHand.add(lstHold.get(lstStrIndex.get(i + 1)));
                            lstPlayerHand.add(lstHold.get(lstStrIndex.get(i + 2)));
                            lstPlayerHand.add(lstHold.get(lstStrIndex.get(i + 3)));
                            lstPlayerHand.add(lstHold.get(lstStrIndex.get(i + 4)));
                            intHandRank.set(intPlayerRankHold, 4);
                            break;
                        }
                    }

                    if (intHandRank.get(intPlayerRankHold) != 4) {
                        //loop to check Ace 2 straight
                        for (int i = 0; i < lstStraight.size() - 3; i++) {
                            if (lstStraight.get(i) == 4 && lstStraight.get(i + 1) == 3 &&
                                    lstStraight.get(i + 2) == 2 && lstStraight.get(i + 3) == 1 &&
                                    lstStraight.get(0) == 13) {
                                lstPlayerHand.removeAll(lstPlayerHand);
                                lstPlayerHand.add(lstHold.get(lstStrIndex.get(i)));
                                lstPlayerHand.add(lstHold.get(lstStrIndex.get(i + 1)));
                                lstPlayerHand.add(lstHold.get(lstStrIndex.get(i + 2)));
                                lstPlayerHand.add(lstHold.get(lstStrIndex.get(i + 3)));
                                lstPlayerHand.add(lstHold.get(lstStrIndex.get(0)));
                                intHandRank.set(intPlayerRankHold, 4);
                                break;
                            }
                        }
                    }
                }
            }

/************************************Three of a Kind*****************************/
            if (intHandRank.get(intPlayerRankHold) < 3) {
                //Loop to find three of a kind
                for (int i = 0; i < lstHold.size() - 2; i++) {
                    if (lstHold.get(i).Rank == lstHold.get(i + 1).Rank &&
                            lstHold.get(i).Rank == lstHold.get(i + 2).Rank &&
                            (!lstPlayerHand.contains(lstHold.get(i).Rank))) {
                        lstPlayerHand.removeAll(lstPlayerHand);
                        lstPlayerHand.add(lstHold.get(i));
                        lstPlayerHand.add(lstHold.get(i + 1));
                        lstPlayerHand.add(lstHold.get(i + 2));
                        intHandRank.set(intPlayerRankHold, 3);
                        break;
                    }
                }
                //second loop to add the next two highest cards
                if (intHandRank.get(intPlayerRankHold) == 3) {
                    for (int i = 0; i < lstHold.size(); i++) {
                        if (!lstPlayerHand.contains(lstHold.get(i))) {
                            lstPlayerHand.add(lstHold.get(i));
                        }
                        if (lstPlayerHand.size() == 5) {

                            break;
                        }
                    }
                }
            }

/*********************************Two Pair**************************************/
            if (intHandRank.get(intPlayerRankHold) < 2) {
                lstPlayerHand.removeAll(lstPlayerHand);

                //First Loop to get the first pair
                for (int i = 0; i < lstHold.size() - 1; i++) {
                    if (lstHold.get(i).Rank == lstHold.get(i + 1).Rank &&
                            (!lstPlayerHand.contains(lstHold.get(i).Rank))) {
                        lstPlayerHand.removeAll(lstPlayerHand);
                        lstPlayerHand.add(lstHold.get(i));
                        lstPlayerHand.add(lstHold.get(i + 1));
                        break;
                    }
                }

                //Second Loop to check the second pair
                for (int i = 0; i < lstHold.size() - 1; i++) {
                    if (!lstPlayerHand.contains(lstHold.get(i)) &&
                            lstHold.get(i).Rank == lstHold.get(i + 1).Rank) {
                        lstPlayerHand.add(lstHold.get(i));
                        lstPlayerHand.add(lstHold.get(i + 1));
                        intHandRank.set(intPlayerRankHold, 2);
                        break;
                    }
                }

                //Adds highest card as last
                if (intHandRank.get(intPlayerRankHold) == 2) {
                    for (int i = 0; i < lstHold.size(); i++) {
                        if (!lstPlayerHand.contains(lstHold.get(i))) {
                            lstPlayerHand.add(lstHold.get(i));
                            break;
                        }
                    }
                }
            }

/**********************************One Pair*************************************/
            if (intHandRank.get(intPlayerRankHold) < 2) {
                //Loop to find a pair
                for (int i = 0; i < lstHold.size() - 1; i++) {
                    if (lstHold.get(i).Rank == lstHold.get(i + 1).Rank &&
                            (!lstPlayerHand.contains(lstHold.get(i).Rank))) {
                        lstPlayerHand.removeAll(lstPlayerHand);
                        lstPlayerHand.add(lstHold.get(i));
                        lstPlayerHand.add(lstHold.get(i + 1));
                        intHandRank.set(intPlayerRankHold, 1);
                        break;
                    }
                }

                //Loop to add the next three cards
                if (intHandRank.get(intPlayerRankHold) == 1) {
                    for (int i = 0; i < lstHold.size(); i++) {
                        if (!lstPlayerHand.contains(lstHold.get(i))) {
                            lstPlayerHand.add(lstHold.get(i));
                        }
                        if (lstPlayerHand.size() == 5) {
                            break;
                        }
                    }
                }
            }

/*************************************No Hand /High Card*************************/
            if (intHandRank.get(intPlayerRankHold) < 1) {
                lstPlayerHand.removeAll(lstPlayerHand);

                //Loop to add cards
                for (int i = 0; i < 5; i++) {
                    if (!lstPlayerHand.contains(lstHold.get(i))) {
                        lstPlayerHand.add(lstHold.get(i));
                    }
                }

                intHandRank.set(intPlayerRankHold, 0);
            }

            lstAllHands.set(intPlayerRankHold, lstPlayerHand);

            //Increment the player's rank position in list
            intPlayerRankHold++;
        }


        //Increments game count
        intTotalGames++;

        bolPlayer1Wins = true;
        bolTie = false;

        for (int i = 1; i < lstAllHands.size(); i++) {
            if (intHandRank.get(0) < intHandRank.get(i)) {
                bolPlayer1Wins = false;
                break;
            } else if (intHandRank.get(0) == intHandRank.get(i)) {
                for (int j = 0; j < lstAllHands.get(i).size(); j++) {
                    if ((intHandRank.get(0) == 8 && intHandRank.get(i) == 8) &&
                            lstAllHands.get(0).get(4).Rank != 13 && lstAllHands.get(i).get(4).Rank == 13) {
                        break;

                    }

                    if (lstAllHands.get(0).get(j).Rank < lstAllHands.get(i).get(j).Rank) {
                        bolPlayer1Wins = false;
                        break;
                    } else if (lstAllHands.get(0).get(j).Rank > lstAllHands.get(i).get(j).Rank) {
                        break;
                    }

                    if (lstAllHands.get(0).get(0).Rank == lstAllHands.get(i).get(0).Rank &&
                            lstAllHands.get(0).get(1).Rank == lstAllHands.get(i).get(1).Rank &&
                            lstAllHands.get(0).get(2).Rank == lstAllHands.get(i).get(2).Rank &&
                            lstAllHands.get(0).get(3).Rank == lstAllHands.get(i).get(3).Rank &&
                            lstAllHands.get(0).get(4).Rank == lstAllHands.get(i).get(4).Rank) {
                        bolPlayer1Wins = false;
                        bolTie = true;
                        break;
                    }
                }
            }
//			if (!bolPlayer1Wins)
//			{
//				break;
//			}
        }

//		if (intHandRank.get(0) < intHandRank.get(1) || intHandRank.get(0) == intHandRank.get(1))
//		{
//			bolPlayer1Wins = false;
//		}

        if (bolPlayer1Wins) {
            intPlayer1Wins++;
        } else if (bolTie) {
            intTie++;
        } else {
            intLose++;
        }


        dblOdds = (((double) intPlayer1Wins / (double) intTotalGames) * 100.0);
        dblTie = (((double) intTie / (double) intTotalGames) * 100.0);
        dblLose = (((double) intLose / (double) intTotalGames) * 100.0);

        strWin = "Win: " + decOdds.format(dblOdds) + "%";
        strTie = "Tie: " + decOdds.format(dblTie) + "%";
        strLose = "Lose: " + decOdds.format(dblLose) + "%";

    }


    /*
     * 计算器重置
     */
    public void Reset() {
        runner.cancel(false);
        intIndex = 0;
        intTotalGames = 0;
        intPlayer1Wins = 0;
        intTie = 0;
        intLose = 0;
        dblLose = 0.00;
        dblTie = 0.00;
        dblOdds = 0.00;

        lstPlayer1.clear();
        lstTable.clear();
        lstHold.clear();
        lstAllHands.clear();
        lstPlayer1Odds.clear();
        lstPlayer2Odds.clear();
        lstPlayer3Odds.clear();
        lstPlayer4Odds.clear();
        lstPlayer5Odds.clear();
        lstPlayer6Odds.clear();
        lstPlayer7Odds.clear();
        lstPlayer8Odds.clear();
        lstPlayer9Odds.clear();
        lstTableOdds.clear();
        intHandRank.clear();
        bolGame = true;
    }

}
