package com.dubture.pdt.ui.preferences;

import org.eclipse.jface.preference.IPreferenceStore;

import com.dubture.pdt.ui.PDTUIPlugin;

public class PreferenceConstants {
	
	public static final String CODE_TEMPLATES_KEY = "com.dubture.pdt.ui.text.custom_code_templates";

	public static IPreferenceStore getPreferenceStore() {
		return PDTUIPlugin.getDefault().getPreferenceStore();
	}
	

}
