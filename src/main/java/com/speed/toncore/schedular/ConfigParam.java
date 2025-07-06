package com.speed.toncore.schedular;

import java.util.concurrent.atomic.AtomicLong;

public class ConfigParam {

	private final AtomicLong BIT_PRICE = new AtomicLong(0);
	private final AtomicLong CELL_PRICE = new AtomicLong(0);
	private final AtomicLong GAS_PRICE = new AtomicLong(0);
	private final AtomicLong LUMP_PRICE = new AtomicLong(0);

	public long getBitPrice() {
		return BIT_PRICE.get();
	}

	public void setBitPrice(long bitPrice) {
		this.BIT_PRICE.set(bitPrice);
	}

	public long getCellPrice() {
		return CELL_PRICE.get();
	}

	public void setCellPrice(long cellPrice) {
		this.CELL_PRICE.set(cellPrice);
	}

	public long getGasPrice() {
		return GAS_PRICE.get();
	}

	public void setGasPrice(long gasPrice) {
		this.GAS_PRICE.set(gasPrice);
	}

	public long getLumpPrice() {
		return LUMP_PRICE.get();
	}

	public void setLumpPrice(long gasPrice) {
		this.LUMP_PRICE.set(gasPrice);
	}
}

