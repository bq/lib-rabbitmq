package com.bq.corbel.lib.rabbitmq.config;

public class BackoffOptions {

	private long initialInterval;
	private double multiplier;
	private long maxInterval;

	public long getInitialInterval() {
		return initialInterval;
	}

	public void setInitialInterval(long initialInterval) {
		this.initialInterval = initialInterval;
	}

	public double getMultiplier() {
		return multiplier;
	}

	public void setMultiplier(double multiplier) {
		this.multiplier = multiplier;
	}

	public long getMaxInterval() {
		return maxInterval;
	}

	public void setMaxInterval(long maxInterval) {
		this.maxInterval = maxInterval;
	}

}
