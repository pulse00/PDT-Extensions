/*******************************************************************************
 * This file is part of the PDT Extensions eclipse plugin.
 * 
 * (c) Robert Gruendler <r.gruendler@gmail.com>
 * 
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 ******************************************************************************/
package com.dubture.pdt.core.model;

import org.eclipse.dltk.internal.core.ModelElement;
import org.eclipse.dltk.internal.core.SourceMethod;

/**
 * @author sobert
 *
 */
@SuppressWarnings("restriction")
public class SuperclassMethod extends SourceMethod {

	/**
	 * @param parent
	 * @param name
	 */
	public SuperclassMethod(ModelElement parent, String name) {
		super(parent, name);

	}

}
