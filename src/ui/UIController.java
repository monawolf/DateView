package ui;

import java.io.IOException;

import event.CloseEvent;
import event.EventBus;
import util.PreferencesManage;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.Tooltip;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Shadow;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;
import log.WriteLog;

public class UIController {
	/* 页面组件 */
	@FXML BorderPane paneRoot;
	@FXML HBox hboxTitle;
	@FXML Label labelTitle;
	@FXML Slider transparenteSlider;
	@FXML Button buttonMin;
	@FXML Button buttonMax;
	@FXML Button buttonExit;
	
	@FXML BorderPane mainPane;

	private PreferencesManage preManage = PreferencesManage.getPreferencesManage();
	@SuppressWarnings("unused")
	private WriteLog writeLog = new WriteLog();/* 初始化数据记录 */
	
	/* 标题内容 */
	private static final String TITLE = "USB数据可视化软件";
	
	/* 组件边距 */
	private static double STAGE_INSTES_TOP;
	private static double STAGE_INSTES_BOTTOM ;
	private static double STAGE_INSTES_LEFT ;
	private static double STAGE_INSTES_RIGHT;

	/* 组件缩放时的最小高度和宽度 */
	static final int STAGE_MIN_HEIGHT = 300;
	static final int STAGE_MIN_WIDTH = 350;
	
	/* 当前窗体 */
	private Stage stage;
	/* 当前场景 */
	private Scene scene;
	
	@FXML
	private void initialize() {
		labelTitle();
		changeTransparente();
		buttonMin();
		buttonMax();
		buttonExit();
		HBox.setHgrow(transparenteSlider, Priority.ALWAYS);
		
		loadMainPane();
	}
	
	/**
	 *  显示窗口
	 * @param stage
	 */
	public void show(Stage stage){
		this.stage = stage;
		this.scene = stage.getScene();
		changeStageSize();/* 拖拽改变窗体大小 */
		changeMouseCursor();/* 在边缘处改变鼠标手势 */
		loadLastUiSetting();/* 加载上一次界面信息 */
		
		stage.initStyle(StageStyle.UNDECORATED);/* 去掉装饰 */
		stage.show();/* 显示窗口 */
		
		stage.addEventFilter(WindowEvent.WINDOW_CLOSE_REQUEST, e->{/* 解决Ctrl+F4关闭行为 */
			EventBus.getInstance().fireEvent(new CloseEvent());
		});
		
		EventBus.getInstance().addEventListener(CloseEvent.class, e->{
			//Event.fireEvent(stage, new WindowEvent(stage, WindowEvent.WINDOW_CLOSE_REQUEST));/* 向系统发送关闭窗口,会引发文件关闭异常,弃用 */
			System.exit(0);//TODO System.exit太粗暴……但是没有替代方案
		});
	}
	
	/** 
	 * 加载历史界面信息
	 */
	private void loadLastUiSetting() {
		/* 窗口透明度 */
		double stageOpacity = preManage.getMainUiPreferences().getDouble("stageOpacity", 100.0);
		transparenteSlider.setValue(stageOpacity);
		
		/* 窗口是否最大化 */
		isMaximized = preManage.getMainUiPreferences().getBoolean("stageMaximized", false);
		stage.setMaximized(isMaximized);
	
		/* 从注册表获取上一次打开的位置及大小 */
		double stageW = preManage.getMainUiPreferences().getDouble("stageW", 800.0);
		double stageH = preManage.getMainUiPreferences().getDouble("stageH", 600.0);
		stage.setWidth(stageW);
		stage.setHeight(stageH);
		double stageX = preManage.getMainUiPreferences().getDouble("stageX", -1);
		double stageY = preManage.getMainUiPreferences().getDouble("stageY", -1);
		if (stageX == -1 || stageY == -1) {
			stage.centerOnScreen();
		} else {
			stage.setX(stageX);
			stage.setY(stageY);
		}
	}
	
	private double moveX = 0.0;/* 记录移动窗体时鼠标相对于组件内部的当前横坐标 */
	private double moveY = 0.0;/* 记录移动窗体时鼠标相对于组件内部的当前纵坐标 */
	private boolean isDragged = false;/* 记录是否正在移动框架 */
	/**
	 * 实现可以拖拽的标题
	 */
	private void labelTitle() {
		labelTitle.setText(TITLE);/* 显示的标题 */

		labelTitle.setOnMousePressed((MouseEvent e) -> {
			/* 被点下未释放时记录组件内部的相对位置 */
			moveX = e.getSceneX();
			moveY = e.getSceneY();
			/* 记录当前正在拖拽避免缩放操作 */
			isDragged = true;
		});
		labelTitle.setOnMouseDragged((MouseEvent e) -> {
			/* 改变窗体位置 -> 鼠标当前屏幕坐标 - 组件内部相对坐标 */
			stage.setX(e.getScreenX() - moveX);
			stage.setY(e.getScreenY() - moveY);
		});
		labelTitle.setOnMouseEntered((MouseEvent e) -> {
			/* 当鼠标移到标签上后的效果 */
			labelTitle.setCursor(Cursor.CLOSED_HAND);
			labelTitle.setScaleX(1.05);
			labelTitle.setScaleY(1.05);
			DropShadow titleEffect = new DropShadow();
			titleEffect.setRadius(2);
			titleEffect.setOffsetX(-2);
			titleEffect.setOffsetY(-2);
			titleEffect.setColor(Color.GRAY);
			labelTitle.setEffect(titleEffect);
			Tooltip tooltip = new Tooltip("拖拽标题可以移动窗口");
			tooltip.setFont(Font.font("微软雅黑", 15));
			Tooltip.install(labelTitle, tooltip);
		});
		labelTitle.setOnMouseExited((MouseEvent e) -> {
			/* 当鼠标从标签上移开恢复原效果 */
			labelTitle.setCursor(Cursor.DEFAULT);
			labelTitle.setEffect(null);
			labelTitle.setScaleX(1);
			labelTitle.setScaleY(1);
		});
		labelTitle.setOnMouseReleased((MouseEvent e) -> {
			isDragged = false;
			/* 记录当前位置 */
			preManage.getMainUiPreferences().putDouble("stageX", stage.getX());
			preManage.getMainUiPreferences().putDouble("stageY", stage.getY());
		});
	}

	/**
	 * 改变框架透明的进度条
	 */
	private void changeTransparente() {
		transparenteSlider.valueProperty().addListener((ObservableValue<? extends Number> ov, Number old_val, Number new_val) -> {
			stage.setOpacity(new_val.doubleValue() / 100);
		});
		transparenteSlider.addEventHandler(MouseEvent.MOUSE_RELEASED, e -> {
			preManage.getMainUiPreferences().putDouble("stageOpacity", transparenteSlider.getValue());
		});
	}

	/**
	 * 最小化按钮
	 */
	private void buttonMin() {
		buttonMin.setShape(new Circle(8));
		buttonMin.setOnMouseEntered((MouseEvent e) -> {
			Shadow shadow = new Shadow();
			shadow.setRadius(1);
			shadow.setColor(Color.CHARTREUSE);
			buttonMin.setEffect(shadow);
			Tooltip tooltip = new Tooltip("最小化");
			tooltip.setFont(Font.font("微软雅黑", 15));
			Tooltip.install(buttonMin, tooltip);
		});
		buttonMin.setOnMouseExited((MouseEvent e) -> {
			buttonMin.setEffect(null);
		});
		buttonMin.setOnMouseClicked((MouseEvent e) -> {
			stage.setIconified(true);
		});
	}

	boolean isMaximized = false;/* 记录当前是否最大化，最大化为真，否则为假 */
	/**
	 * 最大化按钮
	 */
	private void buttonMax() {
		buttonMax.setShape(new Circle(8));
		buttonMax.setOnMouseEntered((MouseEvent e) -> {
			Shadow shadow = new Shadow();
			shadow.setRadius(1);
			shadow.setColor(Color.YELLOW);
			buttonMax.setEffect(shadow);
			Tooltip tooltip = new Tooltip("最大化");
			tooltip.setFont(Font.font("微软雅黑", 15));
			Tooltip.install(buttonMax, tooltip);
		});
		buttonMax.setOnMouseExited((MouseEvent e) -> {
			buttonMax.setEffect(null);
		});
		buttonMax.setOnMouseClicked((MouseEvent e) -> {
			if (!isMaximized) {
				isMaximized = true;
			} else {
				isMaximized = false;
			}
			stage.setMaximized(isMaximized);
			preManage.getMainUiPreferences().putBoolean("stageMaximized", stage.isMaximized());
		});
	}

	/**
	 * 关闭按钮
	 */
	private void buttonExit() {
		buttonExit.setShape(new Circle(8));
		buttonExit.setOnMouseEntered((MouseEvent e) -> {
			Shadow shadow = new Shadow();
			shadow.setRadius(1);
			shadow.setColor(Color.RED);
			buttonExit.setEffect(shadow);
			Tooltip tooltip = new Tooltip("关闭");
			tooltip.setFont(Font.font("微软雅黑", 15));
			Tooltip.install(buttonExit, tooltip);
		});
		buttonExit.setOnMouseExited((MouseEvent e) -> {
			buttonExit.setEffect(null);
		});
		buttonExit.setOnMouseClicked((MouseEvent e) -> {
			EventBus.getInstance().fireEvent(new CloseEvent());
		});
	}

	/**
	 * 加载内容界面
	 */
	private void loadMainPane(){
		FXMLLoader loader = new FXMLLoader();
		try {
			Node rootPane = loader.load(getClass().getResource("/ui/ContentPanel.fxml").openStream());
			mainPane.setCenter(rootPane);
			// ContentPaneController contentPaneController;
			// contentPaneController = ((ContentPaneController)loader.getController());
		} catch (IOException e) {
			System.err.println("主界面加载失败");
			e.printStackTrace();
		}
	}
	
	/* 记录当前缩放状态 */
	private boolean isUp = false;
	private boolean isDown = false;
	private boolean isLeft = false;
	private boolean isRight = false;
	/* 记录组件缩放前的高度和宽度 */
	private double currentHeight = 0.0;
	private double currentWidth = 0.0;
	/* 记录缩放时相对于组件内部当前位置 */
	private double zoomX = 0.0;
	private double zoomY = 0.0;
	/**
	 * 实现拖拽边框改变窗体大小
	 */
	private void changeStageSize() {
		stage.showingProperty().addListener(e -> {
			if (stage.isShowing()) {
				STAGE_INSTES_TOP = paneRoot.getInsets().getTop();
				STAGE_INSTES_BOTTOM = paneRoot.getInsets().getBottom();
				STAGE_INSTES_LEFT = paneRoot.getInsets().getLeft();
				STAGE_INSTES_RIGHT = paneRoot.getInsets().getRight();
			}
		});
		
		stage.addEventHandler(MouseEvent.MOUSE_DRAGGED, e -> {
			if (!isDragged && !isMaximized) {/* 非框架移动状态非最大化状态 */
				/* 判断缩放状态 */
				if (e.getSceneY() > 0 && e.getSceneY() < STAGE_INSTES_TOP) { /* 上边栏 */
					isUp = true;
					zoomY = stage.getY();
					currentHeight = stage.getHeight();
				} else if (e.getSceneY() > (stage.getHeight() - STAGE_INSTES_BOTTOM) && e.getSceneY() < stage.getHeight()) {/* 下边栏 */
					isDown = true;
				}
				if (e.getSceneX() > 0 && e.getSceneX() < STAGE_INSTES_LEFT) {/* 左边栏 */
					isLeft = true;
					zoomX = stage.getX();
					currentWidth = stage.getWidth();
				} else if (e.getSceneX() > stage.getWidth() - STAGE_INSTES_RIGHT && e.getSceneX() < stage.getWidth()) {/* 右边栏 */
					isRight = true;
				}
				/* 执行相关缩放 */
				if (isUp) {
					if ((stage.getHeight() - e.getSceneY()) > STAGE_MIN_HEIGHT) {/* 最小高度 */
						stage.setY(e.getScreenY());
						stage.setHeight(currentHeight - stage.getY() + zoomY);
					}
				}
				if (isDown) {
					if ((e.getScreenY() - stage.getY()) > STAGE_MIN_HEIGHT) {/* 最小高度 */
						stage.setHeight(e.getScreenY() - stage.getY());
					}
				}
				if (isLeft) {
					if ((stage.getWidth() - e.getSceneX()) > STAGE_MIN_WIDTH) {/* 最小高度 */
						stage.setX(e.getScreenX());
						stage.setWidth(currentWidth + zoomX - stage.getX());
					}
				}
				if (isRight) {
					if ((e.getScreenX() - stage.getX()) > STAGE_MIN_WIDTH) {/* 最小宽度 */
						stage.setWidth(e.getScreenX() - stage.getX());
					}
				}
			}
			/*
			 * 手动向JVM发送调用收集垃圾的申请
			 * 解决问题：如果一直拖拽鼠标不放,电脑屏幕重绘速度不够快的话,
			 * 一段时间后会报java.lang.OutOfMemoryError:Java heapspace错误
			 */
			System.gc();
		});
		stage.addEventHandler(MouseEvent.MOUSE_RELEASED, e -> {
			/* 缩放后释放鼠标时重置状态 */
			isUp = false;
			isDown = false;
			isLeft = false;
			isRight = false;
			
			/* 记录当前大小及位置 */
			preManage.getMainUiPreferences().putDouble("stageX", stage.getX());
			preManage.getMainUiPreferences().putDouble("stageY", stage.getY());
			preManage.getMainUiPreferences().putDouble("stageW", stage.getWidth());
			preManage.getMainUiPreferences().putDouble("stageH", stage.getHeight());
		});
		/* 超出屏幕处理 -> 保证即使程序在屏幕外也可以拖动回来 */
		stage.addEventHandler(MouseEvent.MOUSE_DRAGGED, e -> {
			if (stage.getX() < 0) {
				stage.setX(0);
			}
			Rectangle2D primaryScreenBounds = Screen.getPrimary().getVisualBounds();
			if (stage.getWidth() > primaryScreenBounds.getWidth()) {
				stage.setWidth(primaryScreenBounds.getWidth());
			}
			if (stage.getY() < 0) {
				stage.setY(0);
			}
			if (stage.getHeight() > primaryScreenBounds.getHeight()) {
				stage.setHeight(primaryScreenBounds.getHeight());
			}
		});
	}

	/* 记录是否在边缘 */
	private boolean isMargin = false;
	/**
	 * 实现在边界处改变鼠标手势
	 * 
	 * @param stage
	 *            需要增加边缘手势的框架
	 * @param scene
	 *            改变手势的结点
	 */
	private void changeMouseCursor() {
		stage.addEventHandler(MouseEvent.ANY, e -> {
			/* 非边缘 */
			if (isMargin) {
				scene.setCursor(Cursor.DEFAULT);
			}
			/* 上边栏 */
			if (e.getSceneX() > STAGE_INSTES_LEFT && e.getSceneX() < (stage.getWidth() - STAGE_INSTES_RIGHT) && e.getSceneY() > 0 && e.getSceneY() < STAGE_INSTES_TOP) {
				isMargin = true;
				scene.setCursor(Cursor.V_RESIZE);
			}
			/* 下边栏 */
			if (e.getSceneX() > STAGE_INSTES_LEFT && e.getSceneX() < (stage.getWidth() - STAGE_INSTES_RIGHT) && e.getSceneY() > (stage.getHeight() - STAGE_INSTES_BOTTOM)
					&& e.getSceneY() < stage.getHeight()) {
				isMargin = true;
				scene.setCursor(Cursor.V_RESIZE);
			}
			/* 左边栏 */
			if (e.getSceneX() > 0 && e.getSceneX() < STAGE_INSTES_LEFT && e.getSceneY() > STAGE_INSTES_TOP && e.getSceneY() < (stage.getHeight() - STAGE_INSTES_BOTTOM)) {
				isMargin = true;
				scene.setCursor(Cursor.H_RESIZE);
			}
			/* 右边栏 */
			if (e.getSceneX() > (stage.getWidth() - STAGE_INSTES_RIGHT) && e.getSceneX() < stage.getWidth() && e.getSceneY() > STAGE_INSTES_TOP
					&& e.getSceneY() < (stage.getHeight() - STAGE_INSTES_BOTTOM)) {
				isMargin = true;
				scene.setCursor(Cursor.H_RESIZE);
			}
			/* 右下角 */
			if (e.getSceneX() > (stage.getWidth() - STAGE_INSTES_RIGHT) && e.getSceneX() < stage.getWidth() && e.getSceneY() > (stage.getHeight() - STAGE_INSTES_BOTTOM)
					&& e.getSceneY() < stage.getHeight()) {
				isMargin = true;
				scene.setCursor(Cursor.NW_RESIZE);
			}
			/* 左下角 */
			if (e.getSceneX() > 0 && e.getSceneX() < STAGE_INSTES_LEFT && e.getSceneY() > (stage.getHeight() - STAGE_INSTES_BOTTOM) && e.getSceneY() < stage.getHeight()) {
				isMargin = true;
				scene.setCursor(Cursor.NE_RESIZE);
			}
			/* 左上角 */
			if (e.getSceneX() > 0 && e.getSceneX() < STAGE_INSTES_LEFT && e.getSceneY() > 0 && e.getSceneY() < STAGE_INSTES_TOP) {
				isMargin = true;
				scene.setCursor(Cursor.SE_RESIZE);
			}
			/* 右上角 */
			if (e.getSceneX() > (stage.getWidth() - STAGE_INSTES_RIGHT) && e.getSceneX() < stage.getWidth() && e.getSceneY() > 0 && e.getSceneY() < STAGE_INSTES_TOP) {
				isMargin = true;
				scene.setCursor(Cursor.SW_RESIZE);
			}
		});
	}
}
