package com.example.demo.delay;

import java.util.Date;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

import ch.qos.logback.core.util.TimeUtil;

public class DelayEvent implements Delayed {
	private Date startDate;
	
	public DelayEvent(Date startDate) {
		super();
		this.startDate = startDate;
	}

	@Override
	public int compareTo(Delayed o) {
		long result = this.getDelay(TimeUnit.NANOSECONDS) - o.getDelay(TimeUnit.NANOSECONDS);
        if (result < 0) {
            return -1;
        } else if (result > 0) {
            return 1;
        } else {
            return 0;
        }
	}

	/**
	 * 当前时间与对象生成时的时间的时间差：单位毫秒
	 */
	@Override
	public long getDelay(TimeUnit unit) {
		Date now = new Date();
        long diff = startDate.getTime() - now.getTime();
        return unit.convert(diff, TimeUnit.MILLISECONDS);
	}
	

}
