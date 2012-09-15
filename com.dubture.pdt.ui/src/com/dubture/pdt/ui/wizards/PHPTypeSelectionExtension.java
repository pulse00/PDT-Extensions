/*******************************************************************************
 * This file is part of the PDT Extensions eclipse plugin.
 * 
 * (c) Marek Maksimczyk <marek.maksimczyk@mandos.net.pl> 
 * 
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 ******************************************************************************/
package com.dubture.pdt.ui.wizards;

import org.eclipse.dltk.ui.dialogs.ITypeInfoFilterExtension;
import org.eclipse.dltk.ui.dialogs.ITypeInfoRequestor;
import org.eclipse.dltk.ui.dialogs.TypeSelectionExtension;
import org.eclipse.php.core.compiler.PHPFlags;

public class PHPTypeSelectionExtension extends TypeSelectionExtension {

	// Filter, if false we don't get classes/interfaces in dialog.
	// TODO: I don't like this solution, implement something else.
	private boolean getClasses = true;
	private boolean getInterfaces = true;

	public PHPTypeSelectionExtension() {
	}

	public PHPTypeSelectionExtension(boolean getClasses, boolean getInterfaces) {
		this.getClasses = getClasses;
		this.getInterfaces = getInterfaces;
	}

	@Override
	public ITypeInfoFilterExtension getFilterExtension() {
		// TODO Auto-generated method stub
		return new ITypeInfoFilterExtension() {
			@Override
			public boolean select(ITypeInfoRequestor typeInfoRequestor) {
				if (getInterfaces == true && PHPFlags.isInterface(typeInfoRequestor.getModifiers()))
					return true;

				if (getClasses == true && PHPFlags.isClass(typeInfoRequestor.getModifiers()))
					return true;

				return false;
			}
		};
	}
}
