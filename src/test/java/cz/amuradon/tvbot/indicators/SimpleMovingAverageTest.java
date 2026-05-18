package cz.amuradon.tvbot.indicators;

import java.math.BigDecimal;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class SimpleMovingAverageTest {

	@Test
	public void test() {
		SimpleMovingAverage sma = new SimpleMovingAverage(4);
		
		BigDecimal value = sma.add(new BigDecimal(1));
		Assertions.assertEquals(new BigDecimal(1), value);
		
		value = sma.add(new BigDecimal(3));
		Assertions.assertEquals(new BigDecimal(2), value);
		
		value = sma.add(new BigDecimal(2));
		Assertions.assertEquals(new BigDecimal(2), value);
		
		value = sma.add(new BigDecimal(2));
		Assertions.assertEquals(new BigDecimal(2), value);
		
		value = sma.add(new BigDecimal(5));
		Assertions.assertEquals(new BigDecimal(3), value);
	}
}
