package ui;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import date.DateDemo;
import date.DateFormat;
import event.EventBus;
import event.UsbDeviceEvent;
import event.ChangeUsbDeviceEvent;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.SequentialTransition;
import javafx.animation.Timeline;
import javafx.animation.Transition;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.PieChart.Data;
import javafx.scene.chart.ScatterChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.util.Duration;

public class ContentPaneController{
	@FXML BorderPane mainPane;
	@FXML ToggleGroup tableGroup;
	/* 筛选控制 */
	@FXML HBox filterController;
	@FXML Button filterButton;
	@FXML TextField filterInput1;
	@FXML TextField filterInput2;
	/* 数据恢复 */
	@FXML VBox dataRecovery;
	@FXML TextField fileUrl;
	@FXML Button selectFileButton;
	@FXML ToggleGroup viewGroup;
	@FXML HBox filterController2;
	@FXML Button filterButton2;
	@FXML TextField filterInput3;
	@FXML TextField filterInput4;
	/* 设备选择 */
	@FXML ChoiceBox<UsbDeviceEvent> usbDeviceChoiceBox;
	
	private DateDemo dateDemo;
	private SimpleDateFormat dateFormat= new SimpleDateFormat("MM/dd/yyyy  HH:mm:ss  SSS");/* 时间格式 */
	@FXML
	private void initialize() {
		/* 增加驱动 */
		EventBus.getInstance().addEventListener(UsbDeviceEvent.class, e->{
			usbDeviceChoiceBox.getItems().add(e);
			usbDeviceChoiceBox.getSelectionModel().selectFirst();
		});
		/* 选择驱动 */
		usbDeviceChoiceBox.valueProperty().addListener((observable, oldValue, newValue)->{
			EventBus.getInstance().fireEvent(new ChangeUsbDeviceEvent(newValue));
		});
		/* 初始化设备 */
		dateDemo = new DateDemo();/* 数据初始化 */
		
		tableGroup.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
			if (newValue == null) {
				oldValue.setSelected(true);
				return;
			}
			
			if (newValue != null || oldValue != newValue) {
				mainPane.setCenter(null);
				cleanDate();
				filterController.setVisible(false);
				filterController.setManaged(false);
				dataRecovery.setVisible(false);
				dataRecovery.setManaged(false);
				
				dateDemo.stopDate();
				System.gc();
				
				if (newValue.getUserData().equals("LineChart")) {
					dateDemo.startDate(false,null);
					showLineChart();
				}else if(newValue.getUserData().equals("RainfallMap")){
					dateDemo.startDate(false,null);
					showRainfallMap();
				}else if(newValue.getUserData().equals("PieChart")){
					dateDemo.startDate(false,null);
					showPieChart();
				}else if(newValue.getUserData().equals("FilterPieChart")){
					dateDemo.startDate(false,null);
					showFilterPieChart();
				}else if(newValue.getUserData().equals("DataRecovery")){
					showDataRecovery();
				}
			}
		});

		/* 用事件总线的方式获取数据事件并set到Map中 */
		EventBus.getInstance().addEventListener(DateFormat.class, e -> {
			setDate(e.getDateX(), e.getDateY1(), e.getDateY2());
		});
	}
	
	private Transition animation;/* 坐标轴移动时，描绘点的动画 */
	private List<Number> timeList = new ArrayList<Number>();/* 记录时间索引 */
	private long firstTime = -1;/* 记录第一条数据的时间用于计算当前时间差 */
	private Map<Number, Number> amplitudeMap = new HashMap<Number, Number>();/* 坐标点集(数据) */
	private Map<Number, Number> frequencyMap = new HashMap<Number, Number>();/* 坐标点集(数据) */
	/**
	 * 两个二维坐标轴的原始数据
	 */
	@SuppressWarnings("unchecked")
	private void showLineChart() {
		
		/* 波幅图 */
		LineChart.Series<Number, Number> amplitudeLineDate = new LineChart.Series<>();/* 散列点集(显示) */
		NumberAxis amplitudeXAxis = new NumberAxis();
		NumberAxis amplitudeYAxis = new NumberAxis(-200, 200, 10);/* 开始值 结束值 单元格最小值 */
		LineChart<Number, Number> amplitudeLineChart = new LineChart<>(amplitudeXAxis, amplitudeYAxis);
		
		amplitudeLineChart.setLegendVisible(false);/* 隐藏图例 */
		amplitudeXAxis.setForceZeroInRange(false);/* 坐标是否移动 */
		amplitudeXAxis.setLabel("时间(毫秒)");
		amplitudeXAxis.setAnimated(false);/* 是否有移动动画 */
		amplitudeYAxis.setLabel("波幅");
	        amplitudeYAxis.setAutoRanging(false);/* 自动扩充轴 */
	        
	        amplitudeLineChart.setTitle("原始数据图");
	        amplitudeLineChart.getData().addAll(amplitudeLineDate);
		
		/* 频率图 */
	        LineChart.Series<Number, Number> amplitudeLineDate2 = new LineChart.Series<>();/* 只用作显示波幅图的图例无实际数据 */
	        LineChart.Series<Number, Number> frequencyLineDate = new LineChart.Series<>();
		NumberAxis frequencyXAxis = new NumberAxis();
		NumberAxis frequencyYAxis = new NumberAxis(-200, 200, 10);
		LineChart<Number, Number> frequencyLineChart = new LineChart<>(frequencyXAxis, frequencyYAxis);
		
		amplitudeLineDate2.setName("波幅");
		frequencyLineDate.setName("频率");
		frequencyXAxis.setForceZeroInRange(false);/* 坐标是否移动 */
		frequencyXAxis.setLabel("时间(毫秒)");
		frequencyXAxis.setAnimated(false);/* 是否有移动动画 */
		frequencyYAxis.setLabel("频率");
	        frequencyYAxis.setAutoRanging(false);/* 自动扩充轴 */
	        
		frequencyLineDate.getData().add(new LineChart.Data<Number, Number>(0d, 0d));// 初始化图例的图标，初始化一个数据添加进scatterChart后删除数据
		amplitudeLineDate2.getData().add(new LineChart.Data<Number, Number>(0d, 0d));
		frequencyLineChart.getData().addAll(amplitudeLineDate2, frequencyLineDate);
		frequencyLineDate.getData().clear();
		amplitudeLineDate2.getData().clear();

		Label startTimeLable = new Label();
		Label currentTimeLable = new Label();
		
		/* 数据变更 */
		Timeline timeline = new Timeline();
		timeline.getKeyFrames().add(new KeyFrame(Duration.millis(100), new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent actionEvent) {
				long showTime;
				if(timeList.isEmpty()){
					return;
				}else{
					showTime = (long)timeList.get(0);
					timeList.remove(0);
					if (firstTime == -1) {
						firstTime = showTime;
						String showTimeString = dateFormat.format(firstTime).toString();
						startTimeLable.setText("数据开始时间："+showTimeString);
					}
				}
				
				String currentTimeString = dateFormat.format(showTime).toString();
				currentTimeLable.setText("当前显示时间："+currentTimeString);
				
				amplitudeLineDate.getData().add(new XYChart.Data<Number, Number>(showTime - firstTime, getAmplitude(showTime)));
				if (amplitudeLineDate.getData().size() > 50) {/* 当前界面内可显示的点 */
					amplitudeLineDate.getData().remove(0);
				}
				amplitudeLineDate.getData().get(amplitudeLineDate.getData().size()-1).getNode().setStyle("-fx-background-color:orange;");

				frequencyLineDate.getData().add(new XYChart.Data<Number, Number>(showTime - firstTime, getFrequency(showTime)));
				if (frequencyLineDate.getData().size() > 50) {
					frequencyLineDate.getData().remove(0);
				}
				frequencyLineDate.getData().get(amplitudeLineDate.getData().size()-1).getNode().setStyle("-fx-background-color:gold;");
			}
		}));
		timeline.setCycleCount(Animation.INDEFINITE);

		/* 重置动画 */
		stopAnimation();
		animation = new SequentialTransition();
		((SequentialTransition)animation).getChildren().addAll(timeline);
		startAnimation();

		frequencyLineChart.getStylesheets().add("ui/Chart.css");
		VBox timeVBox = new VBox(10, startTimeLable, currentTimeLable);
		timeVBox.setAlignment(Pos.CENTER_RIGHT);
		mainPane.setCenter(new VBox(10, amplitudeLineChart, frequencyLineChart, timeVBox));
	}
	
	/**
	 * 一个二维坐标的最新的点阵分布(雨点)
	 */
	@SuppressWarnings("unchecked")
	private void showRainfallMap(){
		/* 分布图 */
		ScatterChart.Series<Number, Number> seriesDate1 = new ScatterChart.Series<>();
		ScatterChart.Series<Number, Number> seriesDate2 = new ScatterChart.Series<>();/* 只是为了多显示一个图例,此图例没有实际数据 */
		NumberAxis xAxis = new NumberAxis(-200, 200, 10);
		NumberAxis yAxis = new NumberAxis(-200, 200, 10);
		ScatterChart<Number, Number> scatterChart = new ScatterChart<>(xAxis, yAxis);
		
		seriesDate1.setName("旧数据");
		seriesDate2.setName("新数据");
		xAxis.setLabel("频率");
		yAxis.setLabel("波幅");
	        xAxis.setAutoRanging(false);/* 自动扩充轴 */
	        yAxis.setAutoRanging(false);
	        
	        scatterChart.setTitle("近期数据分布图");
		seriesDate1.getData().add(new ScatterChart.Data<Number, Number>(0d, 0d));// 初始化图例的图标，初始化一个数据添加进scatterChart后删除数据
		seriesDate2.getData().add(new ScatterChart.Data<Number, Number>(0d, 0d));
	        scatterChart.getData().addAll(seriesDate1,seriesDate2);
		seriesDate1.getData().clear();
		seriesDate2.getData().clear();
	        
		/* 数据变更 */
		Timeline timeline = new Timeline();
		timeline.getKeyFrames().add(new KeyFrame(Duration.millis(100), new EventHandler<ActionEvent>() {/* 刷新频率 */
			@Override
			public void handle(ActionEvent actionEvent) {
				long showTime;
				if(timeList.isEmpty()){
					return;
				}else{
					showTime = (long)timeList.get(0);
					timeList.remove(0);
				}
				
				seriesDate1.getData().add(new ScatterChart.Data<Number, Number>(getAmplitude(showTime), getFrequency(showTime)));
				if (seriesDate1.getData().size() > 50) {/* 当前界面内可显示的点 */
					seriesDate1.getData().remove(0);
				}
				
				int size = seriesDate1.getData().size()-1;
				double R=100;
				double G=0;
				double B=0;
				for (int i = size; i >= 0 ; i--) {
					R -= 0.5;
					G += 4;
					if(G==100){
						B += 2;
					} else {
						B += 0.3;
					}
					R = R > 100 ? 100 : R;
					G = G > 100 ? 100 : G;
					B = B > 100 ? 100 : B;
					R = R < 0 ? 0 : R;
					G = G < 0 ? 0 : G;
					B = B < 0 ? 0 : B;
					seriesDate1.getData().get(i).getNode().setStyle("-fx-background-color:rgb(" + R + "%, +" + G + "%, " + B + "%);"
							+ "-fx-background-radius: 10");
					/*
					 * 菱形：-fx-padding: 7px 5px 7px 5px;-fx-shape: \"M5,0 L10,9 L5,18 L0,9 Z\";
					 * 三角：-fx-background-radius: 0;-fx-background-insets: 0;-fx-shape: \"M5,0 L10,8 L0,8 Z\";
					 * X：-fx-background-radius: 0;-fx-background-insets: 0;-fx-shape: \"M2,0 L5,4 L8,0 L10,0 L10,2 L6,5 L10,8 L10,10 L8,10 L5,6 L2,10 L0,10 L0,8 L4,5 L0,2 L0,0 Z\";
					 * 
					 * */
				}
			}
		}));
		timeline.setCycleCount(Animation.INDEFINITE);

		/* 重置动画 */
		stopAnimation();
		animation = new SequentialTransition();
		((SequentialTransition)animation).getChildren().addAll(timeline);
		startAnimation();
		
		scatterChart.getStylesheets().add("ui/RainfallMap.css");
		mainPane.setCenter(scatterChart);
	}
	
	/**
	 * 非筛选条件的饼状图
	 */
	private void showPieChart(){
		PieChart chart = new PieChart();
		chart.setTitle("频率分布图");
		chart.setClockwise(true);/*true为顺时针 false为逆时针*/
		
		/* 数据变更 */
		Timeline timeline = new Timeline();
		timeline.getKeyFrames().add(new KeyFrame(Duration.millis(100), new EventHandler<ActionEvent>() {/* 刷新频率 */
			@Override
			public void handle(ActionEvent actionEvent) {
				long showTime;
				if(timeList.isEmpty()){
					return;
				}else{
					showTime = (long)timeList.get(0);
					timeList.remove(0);
				}
				
				int frequency = getFrequency(showTime).intValue();
				if(chart.getData().size()==0){
					chart.getData().add(new PieChart.Data(String.valueOf(frequency)+":"+1, 1));
					return;
				}

				boolean isExist = false;
				for (Data data : chart.getData()) {
					int data_frequency = Integer.valueOf(data.getName().split(":")[0]);
					if (data_frequency == frequency) {
						isExist = true;
						data.setPieValue(data.getPieValue() + 1);
						data.setName(String.valueOf(frequency)+":"+data.getPieValue());
					}
				}
				if(!isExist){
					chart.getData().add(new PieChart.Data(String.valueOf(frequency) +":"+ 1, 1));
				}
			}
		}));
		timeline.setCycleCount(Animation.INDEFINITE);

		/* 重置动画 */
		stopAnimation();
		animation = new SequentialTransition();
		((SequentialTransition)animation).getChildren().addAll(timeline);
		startAnimation();

		mainPane.setCenter(chart);
	}
	
	private int smallerInput = -1;
	private int biggishInput = -1;
	/**
	 * 筛选条件的饼状图
	 */
	private void showFilterPieChart(){
		filterController.setVisible(true);
		filterController.setManaged(true);
		
		filterButton.addEventFilter(MouseEvent.MOUSE_CLICKED, e -> {
			boolean isInputNull = false;
			try {
				if (filterInput1.getText().equals("")) {
					isInputNull = true;
				} else {
					smallerInput = Integer.valueOf(filterInput1.getText());
				}

				if (filterInput2.getText().equals("")) {
					isInputNull = true;
				} else {
					biggishInput = Integer.valueOf(filterInput2.getText());
				}
			} catch (NumberFormatException e_num) {
				isInputNull = true;
			}
			
			if(isInputNull==true){/* 输入为空 */
				return;
			}else{
				if(smallerInput>biggishInput){/* 按大小排序 */
					smallerInput=smallerInput+biggishInput;
					biggishInput=smallerInput-biggishInput;
					smallerInput=smallerInput-biggishInput;
				}
			}
			
			/* 显示部分的界面 */
			PieChart chart = new PieChart();
			chart.setTitle("特定频率范围的波幅分布图");
			chart.setClockwise(true);/* true为顺时针 false为逆时针 */

			/* 数据变更 */
			Timeline timeline = new Timeline();
			timeline.getKeyFrames().add(new KeyFrame(Duration.millis(100), new EventHandler<ActionEvent>() {/* 刷新频率 */
				@Override
				public void handle(ActionEvent actionEvent) {
					long showTime;
					if(timeList.isEmpty()){
						return;
					}else{
						showTime = (long)timeList.get(0);
						timeList.remove(0);
					}
					
					int frequency = getAmplitude(showTime).intValue();
					if (chart.getData().size() == 0) {
						if (frequency > smallerInput && frequency < biggishInput){
							chart.getData().add(new PieChart.Data(String.valueOf(frequency) + ":" + 1, 1));
							return;
						}
					}

					if (frequency > smallerInput && frequency < biggishInput){
						boolean isExist = false;
						for (Data data : chart.getData()) {
							int data_frequency = Integer.valueOf(data.getName().split(":")[0]);
							if (data_frequency == frequency) {
								isExist = true;
								data.setPieValue(data.getPieValue() + 1);
								data.setName(String.valueOf(frequency) + ":" + data.getPieValue());
							}
						}
						if (!isExist) {
							chart.getData().add(new PieChart.Data(String.valueOf(frequency) + ":" + 1, 1));
						}
					}
				}
			}));
			timeline.setCycleCount(Animation.INDEFINITE);

			/* 重置动画 */
			stopAnimation();
			animation = new SequentialTransition();
			((SequentialTransition) animation).getChildren().addAll(timeline);
			startAnimation();

			mainPane.setCenter(chart);
		});
	}
	
	/**
	 * 数据恢复
	 */
	private File selectDataRecoveryFile;
	private FileChooser fileChooser = new FileChooser();
	public void showDataRecovery(){
		dataRecovery.setVisible(true);
		dataRecovery.setManaged(true);
		
		selectDataRecoveryFile = null;
		fileUrl.setText("");
		selectFileButton.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> {
			configureFileChooser(fileChooser);
			if(fileUrl.getText().equals("")){
				selectDataRecoveryFile = fileChooser.showOpenDialog(mainPane.getScene().getWindow());
			}
			if (selectDataRecoveryFile != null) {
				fileUrl.setText(selectDataRecoveryFile.getPath());
			}
		});
		
		viewGroup.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
			if (newValue == null) {
				oldValue.setSelected(true);
				return;
			}
			
			if (newValue != null || oldValue != newValue) {
				if (newValue.getUserData().equals("FilterPieChart")) {
					filterController2.setVisible(true);
					filterController2.setManaged(true);
				}else{
					filterController2.setVisible(false);
					filterController2.setManaged(false);
				}
			}
		});
		
		filterInput3.textProperty().addListener(e->{
			filterInput1.setText(filterInput3.getText());
		});
		
		filterInput4.textProperty().addListener(e->{
			filterInput2.setText(filterInput4.getText());
		});

		filterButton2.addEventHandler(MouseEvent.MOUSE_CLICKED, e->{
			if(fileUrl.getText().equals("")||viewGroup.getSelectedToggle()==null){
				return;
			}
			if (filterController2.isVisible() && (filterInput3.getText().equals("") || filterInput4.getText().equals(""))) {
				return;
			}
			
			cleanDate();
			dateDemo.stopDate();
			System.gc();
			dataRecovery.setVisible(false);
			dataRecovery.setManaged(false);
			dateDemo.startDate(true,selectDataRecoveryFile);
			
			if (viewGroup.getSelectedToggle().getUserData().equals("LineChart")) {
				showLineChart();
			}else if(viewGroup.getSelectedToggle().getUserData().equals("RainfallMap")){
				showRainfallMap();
			}else if(viewGroup.getSelectedToggle().getUserData().equals("PieChart")){
				showPieChart();
			}else if(viewGroup.getSelectedToggle().getUserData().equals("FilterPieChart")){
				showFilterPieChart();
				filterButton.fireEvent(e);
			}
		});
	}
	
	/**
	 * 通过扩展过滤波器获取文件
	 * 
	 * @param fileChooser
	 *            文件选择器
	 */
	private static void configureFileChooser(FileChooser fileChooser) {
		fileChooser.setTitle("选择恢复数据");
		fileChooser.setInitialDirectory(new File(System.getProperty("user.dir")+"\\log"));
		fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("数据文件", "*.txt"));/* 设置过滤器 */
	}
	
	/**
	 * 获取波幅
	 * @param x 时间
	 * @return 波幅值
	 */
	private Number getAmplitude(Number x) {
		return amplitudeMap.get(x) == null ? 0 : amplitudeMap.get(x);
	}

	/**
	 * 获取频率
	 * @param x 时间
	 * @return 频率值
	 */
	private Number getFrequency(Number x) {
		return frequencyMap.get(x) == null ? 0 : frequencyMap.get(x);
	}
	
	/**
	 * 增加数据
	 * @param time 时间
	 * @param y1 波幅值
	 * @param frequency 频率值
	 */
	private void setDate(Number time, Number amplitude, Number frequency) {
		timeList.add(time);
		amplitudeMap.put(time, amplitude);
		frequencyMap.put(time, frequency);
	}
	
	/**
	 * 重置数据
	 */
	private void cleanDate(){
		firstTime = -1;
		timeList.clear();
		amplitudeMap.clear();
		frequencyMap.clear();
	}
	
	/**
	 * 开始动画
	 */
	public void startAnimation() {
		if (animation != null) {
			animation.play();
		}
	}
	
	/**
	 * 暂停动画
	 */
	public void pauseAnimation() {
		if (animation != null) {
			animation.pause();
		}
	}
	
	/**
	 * 停止动画
	 */
	public void stopAnimation(){
		if (animation != null) {
			animation.stop();
		}
	}
	
	/**
	 * 根据Color对象获取十六进制数,因为CSS样式根据RGB值获取颜色不连续，此方法暂且废弃
	 * @param color Color对象
	 * @return 十六进制数
	 */
//	private static String getStringByColor(Color color) {
//	        String R = Integer.toHexString((int)(color.getRed()*255));
//	        R = R.length() < 2 ? ('0' + R) : R;  
//	        String G = Integer.toHexString((int)(color.getGreen()*255));  
//	        G = G.length() < 2 ? ('0' + G) : G;  
//	        String B = Integer.toHexString((int)(color.getBlue()*255));  
//	        B = B.length() < 2 ? ('0' + B) : B;  
//	        return '#' + R + G + B;  
//    }  
}
