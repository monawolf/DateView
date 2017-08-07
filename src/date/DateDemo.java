package date;

import java.io.File;
import java.util.List;

import ch.ntb.usb.Device;
import ch.ntb.usb.LibusbJava;
import ch.ntb.usb.USB;
import ch.ntb.usb.USBException;
import ch.ntb.usb.USBTimeoutException;
import ch.ntb.usb.Usb_Bus;
import ch.ntb.usb.Usb_Device;
import log.ReadLog;
import event.CloseEvent;
import event.EventBus;
import event.MiddlewareEvent;
import event.UsbDeviceEvent;
import event.ChangeUsbDeviceEvent;

public class DateDemo extends Thread {
	private boolean isDataRecovery = false;
	private File dataFile;
	private List<String> dataList;
	
	/* USB设备变量 */
	private Device dev;
	private byte[] data = new byte[] { 0, 1, 2, 3 };//TODO 自定义接受数组
	private byte[] readData = new byte[data.length];
	
	private Middleware middleware = new Middleware();/* 转换数据中间件 */
	private Usb_Device publicUsbDevice;
	public DateDemo() {
		this.start();
		
		/* 初始化USB设备 */
		LibusbJava.usb_init();
		LibusbJava.usb_find_busses();
		LibusbJava.usb_find_devices();
		Usb_Bus usb_Bus = LibusbJava.usb_get_busses();
		while (usb_Bus != null) {
			/* 获取设备信息 */
			Usb_Device usbDevice = usb_Bus.getDevices();
			while (usbDevice != null) {
				String[] deviceMessage = usbDevice.toString().split("-");
				String stringVendorId = deviceMessage[deviceMessage.length - 2];
				String stringProductId = deviceMessage[deviceMessage.length - 1];
				System.out.println("设备供应商ID " + stringVendorId + " 产品编号 " + stringProductId);
				
				/* 转换类型 16进制String转10进制short */
				short shortVendorId = 0;
				for (char c : stringVendorId.toCharArray()) {
					shortVendorId = (short) (shortVendorId * 16 + Character.digit(c, 16));
				}
				short shortProductId = 0;
				for (char c : stringProductId.toCharArray()) {
					shortProductId = (short) (shortProductId * 16 + Character.digit(c, 16));
				}
				
				/* 添加设备信息 */
				EventBus.getInstance().fireEvent(new UsbDeviceEvent(usbDevice, stringVendorId, stringProductId, shortVendorId, shortProductId));/* 增加设备 */
				publicUsbDevice = usbDevice;
				dev = USB.getDevice(shortVendorId, shortProductId);
				
				usbDevice = usbDevice.getNext();
			}
			usb_Bus = usb_Bus.getNext();
		}

		/* 变更设备 */
		EventBus.getInstance().addEventListener(ChangeUsbDeviceEvent.class, e->{
			try {
				if (dev.isOpen()) {
					dev.close();
				}
				dev = USB.getDevice(e.getUsbDeviceEvent().getShortVendorId(), e.getUsbDeviceEvent().getShortProductId());
				dev.open(1, 0, -1);
			} catch (USBException e_usb) {
//				System.err.println("class DateDemo.java catch open or close USBException, the USBException is \n");
//				e_usb.printStackTrace();
			}

			publicUsbDevice = e.getUsbDeviceEvent().getUsbDevice();
		});
	}

	private boolean isStop = true;
	 @SuppressWarnings("unused")
	private int x = 1;/* 测试用变量 */

	@Override
	public void run() {
		super.run();
		while (true) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException ex) {
				System.err.println("class DateDemo.java catch InterruptedException, the InterruptedException is \n");
				ex.printStackTrace();
			}
			if (isStop) {
				continue;
			}

			if (!isDataRecovery) {
				long currentTime = (long) System.currentTimeMillis();
				
				try {
					String endPointMessage = publicUsbDevice.getConfig()[0].getInterface()[0].getAltsetting()[0].getEndpoint()[0].toString();
					String endPoint = endPointMessage.replace("Usb_Endpoint_Descriptor bEndpointAddress: ", "");
					short endPointId = 0;
					for (char c : endPoint.toCharArray()) {
						endPointId = (short) (endPointId * 16 + Character.digit(c, 16));
					}
					System.out.println("当前设备端口:" + endPoint + "/" + endPointId);
					dev.readInterrupt(endPointId, readData, readData.length, 2000, false);
				}catch(USBTimeoutException e_timeout){
					/* 屏蔽USB设备不发送数据时的USB连接超时异常 */
				} catch (USBException e_usb) {
//					System.err.println("class DateDemo.java catch USBException, the USBException is \n");
//					e_usb.printStackTrace();
				}
				middleware.addDate(readData);
				
				EventBus.getInstance().addEventListener(MiddlewareEvent.class, e->{
//					/* 发送数据(生成数据) 测试用*/
//					 EventBus.getInstance().fireEvent(new DateFormat(currentTime, Math.sin(Math.toRadians(x * 10)) * 100, Math.sin(Math.toRadians((x - 10) * 10)) * 100, true));
//					 x++;
					
					/* 中间件统计数据 */
					EventBus.getInstance().fireEvent(new DateFormat(currentTime, e.getDate1(), e.getDate2(), true));
				});
				
				/* 关闭驱动监听 */
				EventBus.getInstance().addEventListener(CloseEvent.class, e -> {
					try {
						dev.close();
					} catch (Exception e_close) {
						System.err.println("class DateDemo.java catch CloseException, the CloseException is \n");
						e_close.printStackTrace();
					}
				});
			} else {
				/* 数据恢复 */
				if (dataFile != null || !dataFile.isFile()) {
					if (dataList == null) {
						dataList = new ReadLog().read(dataFile);
					}
					if (dataList.size() > 0) {
						String[] date = dataList.get(0).split(" ");
						dataList.remove(0);
						Number time = Long.valueOf(date[0]);
						Number amplitude = Double.valueOf(date[1]);
						Number frequency = Double.valueOf(date[2]);
						EventBus.getInstance().fireEvent(new DateFormat(time, amplitude, frequency, false));
					}
				} else {
					System.err.println("未选择恢复数据的文件");
				}
			}
		}
	}

	/**
	 * @param isDataRecovery 是否数据恢复
	 * @param dataFile 文件
	 */
	public void startDate(boolean isDataRecovery, File dataFile) {
		this.isDataRecovery = isDataRecovery;
		this.dataFile = dataFile;

		 x = 1;/* 测试用变量 */
		isStop = false;
		dataList = null;
	}

	public void stopDate() {
		isStop = true;
	}
}
