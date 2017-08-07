package event;

/**
 * 中间件事件 
 */
public class MiddlewareEvent {
	Number date1;
	Number date2;

	public MiddlewareEvent(Number date1, Number date2) {
		this.date1 = date1;
		this.date2 = date2;
	}

	public Number getDate1() {
		return date1;
	}

	public Number getDate2() {
		return date2;
	}
}
