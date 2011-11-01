package com.dubture.pdt.ui.preferences;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.templates.ContextTypeRegistry;
import org.eclipse.php.internal.ui.preferences.PHPTemplateStore;

@SuppressWarnings("restriction")
public class PDTTemplateStore extends PHPTemplateStore {

	public PDTTemplateStore(ContextTypeRegistry registry,
			IPreferenceStore store, String key) {
		super(registry, store, key);
	}

}
