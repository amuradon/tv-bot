package cz.amuradon.tvbot.indicators;

import java.math.BigDecimal;

public class Kline {
	
	public final Long startTime;
	public final Long closeTime;
	public BigDecimal open;
	public BigDecimal close;
	public BigDecimal high;
	public BigDecimal low;
	public BigDecimal volume;
	
	public Kline(Long startTime, Long closeTime, BigDecimal price, BigDecimal volume) {
		this.startTime = startTime;
		this.closeTime = closeTime;
		open = price;
		high = price;
		low = price;
		close = price;
		this.volume = volume;
	}

}
