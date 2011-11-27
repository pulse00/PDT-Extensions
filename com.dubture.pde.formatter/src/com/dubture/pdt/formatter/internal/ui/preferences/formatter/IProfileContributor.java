package com.dubture.pdt.formatter.internal.ui.preferences.formatter;

import java.util.List;

import com.dubture.pdt.formatter.internal.ui.preferences.formatter.ProfileManager.BuiltInProfile;

public interface IProfileContributor {
	
	
	List<BuiltInProfile> getBuiltinProfiles(IProfileVersioner versioner);

}
