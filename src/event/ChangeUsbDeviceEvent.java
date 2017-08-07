package event;

/**
 * 变更选择USB设备事件
 */
public class ChangeUsbDeviceEvent {
	private UsbDeviceEvent usbDeviceEvent;

	public ChangeUsbDeviceEvent(UsbDeviceEvent usbDeviceEvent) {
		this.usbDeviceEvent = usbDeviceEvent;
	}

	public UsbDeviceEvent getUsbDeviceEvent() {
		return usbDeviceEvent;
	}
}
