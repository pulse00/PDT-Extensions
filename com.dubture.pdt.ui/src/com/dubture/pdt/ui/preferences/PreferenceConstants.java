/*******************************************************************************
 * This file is part of the PDT Extensions eclipse plugin.
 * 
 * (c) Robert Gruendler <r.gruendler@gmail.com>
 * 
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 ******************************************************************************/
package com.dubture.pdt.ui.preferences;

import org.eclipse.jface.preference.IPreferenceStore;

import com.dubture.pdt.ui.PDTUIPlugin;

public class PreferenceConstants {
	
	public static final String CODE_TEMPLATES_KEY = "com.dubture.pdt.ui.text.custom_code_templates";

	public static IPreferenceStore getPreferenceStore() {
		return PDTUIPlugin.getDefault().getPreferenceStore();
	}
	

}
