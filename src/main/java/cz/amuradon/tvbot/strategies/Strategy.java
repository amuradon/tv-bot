package cz.amuradon.tvbot.strategies;

import java.math.BigDecimal;

public interface Strategy {

	public void start(String symbol, BigDecimal bigDecimal);
}
