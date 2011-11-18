package com.dubture.pde.formatter.internal.ui.preferences;

import java.util.Iterator;
import java.util.Map;


import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

import com.dubture.pde.formatter.FormatterPlugin;
import com.dubture.pde.formatter.internal.core.formatter.CodeFormatterConstants;

public class PreferenceInitializer extends AbstractPreferenceInitializer {

	public PreferenceInitializer() {
	}

	@Override
	public void initializeDefaultPreferences() {
		IPreferenceStore store = FormatterPlugin.getDefault()
				.getPreferenceStore();
		Map<?, ?> map = CodeFormatterConstants.getDefaultSettings();
		Iterator<?> it = map.keySet().iterator();
		while (it.hasNext()) {
			String key = (String) it.next();
			store.setDefault(key, (String) map.get(key));
		}
	}
}
