package com.speed.toncore.util;

public class FeeCalculator {

	private static final int BIT_PRICE = 26_214_400;
	private static final long CELL_PRICE = 2_621_440_000L;
	private static final int LUMP_PRICE = 400_000;
	private static final int BIT16 = 1 << 16;
	private static final int GAS_PRICE = 26214400;

	public static void main(String[] args) {
		int step1Fwd = calculateFwdFee(6, 2289);
		System.out.println("Step 1 Total: " + step1Fwd);
	}

	private static int calculateFwdFee(int cell, int bit) {
		double fee = LUMP_PRICE + Math.ceil((double) ((long) BIT_PRICE * bit + CELL_PRICE * cell) / BIT16);
		return (int) fee;
	}

	private static int calculateGasFee(int gasUsed) {
		return (int) Math.ceil((double) gasUsed * GAS_PRICE / BIT16);
	}
}
