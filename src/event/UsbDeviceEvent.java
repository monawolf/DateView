package event;

import ch.ntb.usb.Usb_Device;

/**
 *  增加USB设备的事件
 */
public class UsbDeviceEvent {
	private Usb_Device usbDevice;
	private String stringVendorId;
	private String stringProductId;
	private short shortVendorId;
	private short shortProductId;

	public UsbDeviceEvent(Usb_Device usbDevice, String stringVendorId, String stringProductId, short shortVendorId, short shortProductId) {
		this.usbDevice = usbDevice;
		this.stringProductId = stringProductId;
		this.stringVendorId = stringVendorId;
		this.shortProductId = shortProductId;
		this.shortVendorId = shortVendorId;
	}

	public Usb_Device getUsbDevice() {
		return usbDevice;
	}

	public short getShortVendorId() {
		return shortVendorId;
	}

	public short getShortProductId() {
		return shortProductId;
	}

	public String toString() {
		return stringVendorId + "-" + stringProductId;
	}
}
