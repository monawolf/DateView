package util;

import java.util.prefs.Preferences;

public class PreferencesManage {

	private static PreferencesManage preferencesManage;
	private Preferences rootPre = Preferences.userRoot();/* 向HKEY_LOCAL_MACHINE/Software/JavaSoft/prefs 下写入注册表值. */
	
	public PreferencesManage() {
		preferencesManage = this;
	}

	public static PreferencesManage getPreferencesManage() {
		return preferencesManage;
	}

	/**
	 * 注册表的根节点HKEY_LOCAL_MACHINE/Software/JavaSoft/prefs目录
	 * @return
	 */
	public Preferences getRootPreferences() {
		return rootPre;
	}
	
	/**
	 * 主界面信息的记录位置
	 * @return
	 */
	public Preferences getMainUiPreferences(){
		Preferences userPre = rootPre.node("/project/mainui");
		return userPre;
	}
}
