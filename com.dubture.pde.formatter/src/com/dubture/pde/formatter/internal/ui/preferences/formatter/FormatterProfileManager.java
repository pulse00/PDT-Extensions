/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package com.dubture.pde.formatter.internal.ui.preferences.formatter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IScopeContext;

import com.dubture.pde.formatter.FormatterPlugin;
import com.dubture.pde.formatter.internal.core.formatter.CodeFormatterConstants;
import com.dubture.pde.formatter.internal.ui.preferences.PreferencesAccess;

public class FormatterProfileManager extends ProfileManager {

	public final static String ECLIPSE_PROFILE = FormatterPlugin.PLUGIN_ID
			+ ".default.eclipse_profile"; //$NON-NLS-1$

	public final static String DEFAULT_PROFILE = ECLIPSE_PROFILE;

	private final static KeySet[] KEY_SETS = new KeySet[] { new KeySet(
			FormatterPlugin.PLUGIN_ID, new ArrayList(CodeFormatterConstants
					.getDefaultSettings().keySet())) };

	private final static String PROFILE_KEY = FormatterPlugin.FORMATTER_PROFILE;
	private final static String FORMATTER_SETTINGS_VERSION = FormatterPlugin.PLUGIN_ID
			+ ".formatter_settings_version"; //$NON-NLS-1$

	public FormatterProfileManager(List profiles, IScopeContext context,
			PreferencesAccess preferencesAccess,
			IProfileVersioner profileVersioner) {
		super(addBuiltinProfiles(profiles, profileVersioner), context,
				preferencesAccess, profileVersioner, KEY_SETS, PROFILE_KEY,
				FORMATTER_SETTINGS_VERSION);
	}

	private static List addBuiltinProfiles(List profiles,
			IProfileVersioner profileVersioner) {
		final Profile eclipseProfile = new BuiltInProfile(ECLIPSE_PROFILE,
				FormatterMessages.ProfileManager_eclipse_profile_name,
				getEclipseSettings(), 2, profileVersioner.getCurrentVersion(),
				profileVersioner.getProfileKind());
		profiles.add(eclipseProfile);
		return profiles;
	}

	/**
	 * @return Returns the settings for the new eclipse profile.
	 */
	public static Map getEclipseSettings() {
		final Map options = CodeFormatterConstants.getDefaultSettings();

		ProfileVersioner.setLatestCompliance(options);
		return options;
	}

	/**
	 * @return Returns the default settings.
	 */
	public static Map getDefaultSettings() {
		return getEclipseSettings();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.ui.preferences.formatter.ProfileManager#getSelectedProfileId(org.eclipse.core.runtime.preferences.IScopeContext)
	 */
	protected String getSelectedProfileId(IScopeContext instanceScope) {
		String profileId = instanceScope.getNode(FormatterPlugin.PLUGIN_ID)
				.get(PROFILE_KEY, null);
		if (profileId == null) {
			profileId = new DefaultScope().getNode(FormatterPlugin.PLUGIN_ID)
					.get(PROFILE_KEY, null);
		}
		return profileId;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.ui.preferences.formatter.ProfileManager#getDefaultProfile()
	 */
	public Profile getDefaultProfile() {
		return getProfile(DEFAULT_PROFILE);
	}

}
