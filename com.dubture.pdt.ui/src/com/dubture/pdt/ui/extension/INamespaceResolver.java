package com.dubture.pdt.ui.extension;

import org.eclipse.dltk.core.IScriptFolder;

/**
 * 
 * Interface for the namespacersolver extension point.
 *
 * 
 * 
 * @author Robert Gruendler <r.gruendler@gmail.com>
 *
 */
public interface INamespaceResolver {

	/**
	 * Resolve the namespace of an IScriptFolder
	 * @param container
	 * @return the resolved namespace or null if unable to resolve
	 */
	String resolve(IScriptFolder container);

}
