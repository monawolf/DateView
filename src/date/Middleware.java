package date;

import java.util.ArrayList;
import java.util.List;

import event.EventBus;
import event.MiddlewareEvent;

/**
 * 生成显示数据用中间件
 */
public class Middleware {
	List<Number[]> dateList = new ArrayList<Number[]>();

	public void addDate(byte[] readData) {
		Number[] date = new Number[readData.length];
		for (int i = 0; i < readData.length; i++) {
			System.out.print(readData[i] + " ");
			date[i] = Integer.valueOf(readData[i]);
		}
		System.out.println();

		dateList.add(date);

		/* 统计数据 */
		if (dateList.size() >= 10) {
			EventBus.getInstance().fireEvent(new MiddlewareEvent(getAmplitude(), getFrequency()));
			cleanDate();
		}
	}

	/* 波幅 TODO 第一位的最大值+随机*/
	private Number getAmplitude() {
		Integer amplitude = Integer.MIN_VALUE;
		for (Number[] date : dateList) {
			if ((Integer) date[0] > (Integer) amplitude) {
				amplitude = (Integer) date[1];
			}
		}
		
		int ram = (int) (Math.random() * 50 + 1);
		return amplitude + ram;
	}

	/* 频率 TODO 第二位的平均数+随机*/
	private Number getFrequency() {
		Integer frequency = 0;
		for (Number[] date : dateList) {
			if ((Integer) date[1] > (Integer) frequency) {
				// frequency = new Random(System.currentTimeMillis()).nextInt();
				frequency = frequency + (Integer) date[2];
			}
		}
		frequency = frequency / dateList.size();

		int ram = (int) (Math.random() * 10 + 1);
		return frequency + ram;
	}

	private void cleanDate() {
		dateList.clear();
	}
}
