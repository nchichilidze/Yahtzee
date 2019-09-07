/*
 * File: Yahtzee.java
 * ------------------
 * This program will eventually play the Yahtzee game.
 */

import java.util.ArrayList;

import acm.io.*;
import acm.program.*;
import acm.util.*;

public class Yahtzee extends GraphicsProgram implements YahtzeeConstants {
	/* Private instance variables */
	private int nPlayers;
	private String[] playerNames;
	private YahtzeeDisplay display;
	private RandomGenerator rgen = new RandomGenerator();
	private ArrayList <String> winners = new ArrayList<String>();
	
	int[] dice;
	int[] freq;
	boolean[] cat;
	int category;
	int[][] scoreCounter;
	boolean[][] isSelected;

	public static void main(String[] args) {
		new Yahtzee().start(args);
	}

	public void run() {
		getPlayers();
		dice = new int[N_DICE]; // array for dice rolled
		scoreCounter = new int[nPlayers + 1][N_CATEGORIES + 1]; // array that
																// stores scores
																// for each
																// player
		isSelected = new boolean[nPlayers + 1][N_CATEGORIES + 1]; // array that
																	// stores
																	// whether a
																	// player
																	// has
																	// selected
																	// certain
																	// categories.
		playGame();
	}

	// * gets the amount and names of players, stores them in an array of
	// Strings playerNames.
	private void getPlayers() {
		IODialog dialog = getDialog();
		nPlayers = dialog.readInt("Enter number of players");
		while (nPlayers < 1 || nPlayers > MAX_PLAYERS) {
			dialog.println("illegal number");
			nPlayers = dialog.readInt("Enter number of players");
		}
		playerNames = new String[nPlayers];
		for (int i = 1; i <= nPlayers; i++) {
			playerNames[i - 1] = dialog.readLine("Enter name for player " + i);
		}
		display = new YahtzeeDisplay(getGCanvas(), playerNames);
	}

	private void playGame() {
		for (int j = 0; j < 13; j++) { // 13 rounds
			for (int i = 1; i <= nPlayers; i++) { // switching the players
				turn(i); // a single turn
				updateTotal(i); // updates the total score after each turn
			}
		}
		updateOtherScores();
		announceWinner();
	}
	// displays the winner or the winners with their scores. 
	private void announceWinner() {
		int maximum = 0;
		for (int i = 1; i < nPlayers + 1; i++) {
			if (scoreCounter[i][TOTAL] >= maximum) {
				maximum = scoreCounter[i][TOTAL];
			}
		}
		for (int j = 1; j < nPlayers + 1; j++) {
			if (scoreCounter[j][TOTAL] == maximum) {
				winners.add(playerNames[j-1]);
			}
		}

		String names = "";
		for (int k = 0; k < winners.size(); k++) { 
			names = names + winners.get(k) + ",";
		}
		display.printMessage("CONGRATULATIONS " + names + " YOU ARE THE WINNER(S) WITH " + maximum + " SCORES");
	}

	private void turn(int i) {
		rollDice(i); // rolls a random set of dice
		for (int j = 0; j < 2; j++) { // re-rolling twice
			reRollDice();
		}
		chooseCategory(i);
	}

	private void rollDice(int i) {
		display.printMessage(
				playerNames[i - 1] + "'s turn. Click " + '"' + " Roll Dice " + '"' + " button to roll the dice");
		display.waitForPlayerToClickRoll(i);
		for (int p = 0; p < N_DICE; p++) { // displays 5 random dice.
			dice[p] = rgen.nextInt(1, 6); // renews the array of dice.
		}
		display.displayDice(dice);
	}

	// * lets the player select the dice they want to re-roll, re-randomizes
	// those dice and displays them.
	private void reRollDice() {
		display.printMessage("select the die you wish to re-roll and click " + '"' + " Roll Again " + '"');
		display.waitForPlayerToSelectDice();
		for (int k = 0; k <= N_DICE - 1; k++) {
			if (display.isDieSelected(k)) {
				dice[k] = rgen.nextInt(1, 6);
			}
		}
		display.displayDice(dice);
	}

	// updates the total score after each turn.
	private void updateTotal(int i) {
		int sum = 0;
		for (int p = ONES; p < TOTAL; p++) { // sums up all the categories.
			sum = sum + scoreCounter[i][p];
		}
		display.updateScorecard(TOTAL, i, sum);
	}

	/**
	 * at the end of the game this method sums up the 1. Upper 2. Bonus 3. Total
	 * scores.
	 */

	private void updateOtherScores() {
		for (int i = 1; i <= nPlayers; i++) {
			UpperAndBonus(i);
			lower(i);
			total(i);
			updateAll(i);
		}
	}

	/**
	 * counts the upper score, checks if its more than 63 then assigns a bonus
	 * score accordingly.
	 */
	private void UpperAndBonus(int i) {
		int upperScore = 0;
		for (int j = 1; j < UPPER_SCORE; j++) {
			upperScore = upperScore + scoreCounter[i][j];
		}
		scoreCounter[i][UPPER_SCORE] = upperScore;
		if (upperScore >= 63) {
			scoreCounter[i][UPPER_BONUS] = 35;
		} else {
			scoreCounter[i][UPPER_BONUS] = 0;
		}
	}

	// lower score counter.
	private void lower(int i) {
		int lowerScore = 0;
		for (int k = THREE_OF_A_KIND; k < LOWER_SCORE; k++) {
			lowerScore = lowerScore + scoreCounter[i][k];
		}
		scoreCounter[i][LOWER_SCORE] = lowerScore;
	}

	// puts the scores into the scoreCounter array.
	private void updateScores(int i, int category, int score) {
		scoreCounter[i][category] = score;
	}

	// choosing the category.
	private void chooseCategory(int i) {
		display.printMessage("select a category for this roll");
		category = display.waitForPlayerToSelectCategory();
		while (isSelected[i][category] == true) { // makes sure the category has
													// not been selected yet.
			display.printMessage("that category has already been selected");
			category = display.waitForPlayerToSelectCategory();
		}
		if (checkCategory(category) == true) { // if this category is
												// appropriate for the set of
												// dice rolled.
			display.updateScorecard(category, i, score(category));
			updateScores(i, category, score(category));
			isSelected[i][category] = true;
		} else {
			display.updateScorecard(category, i, 0);
			updateScores(i, category, 0); // assigns a score of 0
			isSelected[i][category] = true;
		}
	}

	private void total(int i) { // total counter
	//	scoreCounter[i][TOTAL] = 158;
		scoreCounter[i][TOTAL] = scoreCounter[i][UPPER_SCORE] + scoreCounter[i][UPPER_BONUS]
				+ scoreCounter[i][LOWER_SCORE];
	}

	private void updateAll(int i) { // displays scores.
		display.updateScorecard(UPPER_SCORE, i, scoreCounter[i][UPPER_SCORE]);
		display.updateScorecard(LOWER_SCORE, i, scoreCounter[i][LOWER_SCORE]);
		display.updateScorecard(UPPER_BONUS, i, scoreCounter[i][UPPER_BONUS]);
		display.updateScorecard(TOTAL, i, scoreCounter[i][TOTAL]);
	}

	// whether this set of dice is appropriate for the category selected.
	private boolean checkCategory(int category) {
		categories();
		if (cat[category] == true) {
			return true;
		}
		return false;

	}

	// returns the correct scores for each category.
	private int score(int category) {
		int diceSum = 0;
		for (int i = 0; i < 5; i++) {
			diceSum = diceSum + dice[i];
		}
		if (category < 7) {
			return freq[category] * category;
		} else if (category == YAHTZEE) {
			return 50;
		} else if (category == FULL_HOUSE) {
			return 25;
		} else if (category == SMALL_STRAIGHT) {
			return 30;
		} else if (category == LARGE_STRAIGHT) {
			return 40;
		} else if (category == CHANCE) {
			return diceSum;
		} else if (category == THREE_OF_A_KIND) {
			return diceSum;
		} else if (category == FOUR_OF_A_KIND) {
			return diceSum;
		} else {
			return 0;
		}
	}

	private void categories() {
		countfreq(dice);
		cat = new boolean[N_CATEGORIES + 1];
		cat[CHANCE] = true; // this category is always available.
		sameNumberChecker();
		int mult = 1;
		for (int i = 1; i < 7; i++) {
			if (freq[i] != 0) {
				mult = mult * freq[i];
			}
			if (mult == 6)
				cat[FULL_HOUSE] = true;
			if (freq[i] >= 3)
				cat[THREE_OF_A_KIND] = true;
			if (freq[i] >= 4)
				cat[FOUR_OF_A_KIND] = true;
			if (freq[i] == 5)
				cat[YAHTZEE] = true;
		}
		StraightChecker();
	}

	private void sameNumberChecker() {
		if (freq[1] > 0)
			cat[ONES] = true;
		if (freq[2] > 0)
			cat[TWOS] = true;
		if (freq[3] > 0)
			cat[THREES] = true;
		if (freq[4] > 0)
			cat[FOURS] = true;
		if (freq[5] > 0)
			cat[FIVES] = true;
		if (freq[6] > 0)
			cat[SIXES] = true;
	}

	private void StraightChecker() {
		int case1 = freq[1] * freq[2] * freq[3] * freq[4] * freq[5];
		int case2 = freq[2] * freq[3] * freq[4] * freq[5] * freq[6];
		if (case1 == 1 || case2 == 1) {
			cat[LARGE_STRAIGHT] = true;
		}
		int multOne = 1;
		int multTwo = 1;
		int multThree = 1;
		for (int i = 1; i <= 4; i++) {
			multOne = multOne * freq[i];
		}
		for (int i = 2; i <= 5; i++) {
			multTwo = multTwo * freq[i];
		}
		for (int i = 3; i <= 6; i++) {
			multThree = multThree * freq[i];
		}
		if (multThree != 0 || multTwo != 0 || multOne != 0) {
			cat[SMALL_STRAIGHT] = true;
		}

	}

	// counts the frequencies of each number.
	private void countfreq(int[] dice) {
		freq = new int[7];
		for (int i = 0; i < 5; i++) {
			if (dice[i] == 1)
				freq[1]++;
			if (dice[i] == 2)
				freq[2]++;
			if (dice[i] == 3)
				freq[3]++;
			if (dice[i] == 4)
				freq[4]++;
			if (dice[i] == 5)
				freq[5]++;
			if (dice[i] == 6)
				freq[6]++;
		}
	}

}
