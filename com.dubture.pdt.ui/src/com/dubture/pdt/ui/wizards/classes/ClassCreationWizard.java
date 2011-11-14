/*******************************************************************************
 * This file is part of the PDT Extensions eclipse plugin.
 * 
 * (c) Robert Gruendler <r.gruendler@gmail.com>
 * 
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 ******************************************************************************/
package com.dubture.pdt.ui.wizards.classes;

import org.eclipse.dltk.ui.wizards.NewSourceModulePage;
import org.eclipse.dltk.ui.wizards.NewSourceModuleWizard;

public class ClassCreationWizard extends NewSourceModuleWizard {

	@Override
	protected NewSourceModulePage createNewSourceModulePage() {

		return new ClassCreationWizardPage(getSelection(), "");

	}	
}
