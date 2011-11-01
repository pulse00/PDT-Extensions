package com.dubture.pdt.ui.wizards.classes;

import org.eclipse.dltk.ui.wizards.NewSourceModulePage;
import org.eclipse.dltk.ui.wizards.NewSourceModuleWizard;

public class ClassCreationWizard extends NewSourceModuleWizard {

	@Override
	protected NewSourceModulePage createNewSourceModulePage() {

		return new ClassCreationWizardPage(getSelection(), "");

	}	
}
