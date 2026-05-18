package cz.amuradon.tvbot.indicators;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedList;

public class SimpleMovingAverage {

	private final int period;
	
	private final LinkedList<BigDecimal> window;
	
	private BigDecimal runningSum;
	
	public SimpleMovingAverage(int period) {
		this.period = period;
		window = new LinkedList<>();
		runningSum = BigDecimal.ZERO;
	}
	
	public BigDecimal add(BigDecimal value) {
		window.add(value);
		if (window.size() > period) {
			BigDecimal removed = window.remove();
			runningSum = runningSum.subtract(removed);
		}
		
		runningSum = runningSum.add(value);
		return runningSum.divide(new BigDecimal(window.size()), value.scale(), RoundingMode.HALF_UP);
	}
}
