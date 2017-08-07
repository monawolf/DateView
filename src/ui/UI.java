package ui;

import util.PreferencesManage;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class UI extends Application {

	@SuppressWarnings("unused")
	private PreferencesManage preManage = new PreferencesManage();/* 初始化单例的注册表管理类 */
	
	/* 初始化窗口大小 */
	private double stageW = 800.0;
	private double stageH = 600.0;
	
	public void start(Stage primaryStage) throws Exception {
		FXMLLoader loader = new FXMLLoader();
		Parent rootPane = (Parent) loader.load(getClass().getResource("/ui/UI.fxml").openStream());
		Scene myScene = new Scene(rootPane, stageW, stageH);
		primaryStage.setScene(myScene);
		((UIController)loader.getController()).show(primaryStage);
	}
}
