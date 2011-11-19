package com.dubture.pde.formatter.internal.ui.preferences;

import org.eclipse.core.resources.IProject;
import org.eclipse.php.internal.ui.preferences.IStatusChangeListener;
import org.eclipse.php.ui.preferences.IPHPFormatterConfigurationBlockWrapper;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.preferences.IWorkbenchPreferenceContainer;

@SuppressWarnings("restriction")
public class PDTFormatterConfigurationBlockWrapper implements
	IPHPFormatterConfigurationBlockWrapper {
	
	private PDEFormatterConfigurationBlock pConfigurationBlock;

	public void init(IStatusChangeListener context, IProject project,
			IWorkbenchPreferenceContainer container) {
		pConfigurationBlock = new PDEFormatterConfigurationBlock(context,
				project, container);
	}

	public Control createContents(Composite composite) {
		return pConfigurationBlock.createContents(composite);
	}

	public void dispose() {
		pConfigurationBlock.dispose();
	}

	public boolean hasProjectSpecificOptions(IProject project) {
		return pConfigurationBlock.hasProjectSpecificOptions(project);
	}

	public void performApply() {
		pConfigurationBlock.performApply();
	}

	public void performDefaults() {
		pConfigurationBlock.performDefaults();
	}

	public boolean performOk() {
		return pConfigurationBlock.performOk();
	}

	public void useProjectSpecificSettings(boolean useProjectSpecificSettings) {
		pConfigurationBlock
				.useProjectSpecificSettings(useProjectSpecificSettings);
	}

	public String getDescription() {
		return null;
	}
}
