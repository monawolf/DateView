package date;

/**
 * 显示用数据格式
 */
public class DateFormat {
	private Number dateX;/* 数据索引 */
	private Number dateY1;/* 数据1 */
	private Number dateY2;/* 数据2 */
	private boolean isWriteLog = false;/* 是否需要数据记录 */

	/**
	 * 数据格式
	 * @param dateX 横向度量
	 * @param dateY1 纵向度量1
	 * @param dateY2 纵向度量2
	 * @param isWriteLog 是否写日志
	 */
	public DateFormat(Number dateX, Number dateY1, Number dateY2, boolean isWriteLog) {
		this.dateX = dateX;
		this.dateY1 = dateY1;
		this.dateY2 = dateY2;
		this.isWriteLog = isWriteLog;
	}

	public Number getDateX() {
		return dateX;
	}

	public Number getDateY1() {
		return dateY1;
	}

	public Number getDateY2() {
		return dateY2;
	}

	public boolean isNeedWriteLog() {
		return isWriteLog;
	}
}
