/*******************************************************************************
 * This file is part of the PDT Extensions eclipse plugin.
 * 
 * (c) Robert Gruendler <r.gruendler@gmail.com>
 * 
 * Modified by Marek Maksimczyk <marek.maksimczyk@mandos.net.pl>
 * 
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 ******************************************************************************/
package com.dubture.pdt.internal.ui.codemanipulation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.dltk.core.IScriptProject;
import org.eclipse.dltk.core.IType;
import org.eclipse.php.internal.core.PHPCoreConstants;
import org.eclipse.php.internal.core.PHPCorePlugin;

/**
 * Utilities for class generation.
 * 
 * @author Robert Gruendler <r.gruendler@gmail.com>
 * @author Marek Maksimczyk <marek.maksimczyk@mandos.net.pl>
 */
public class ClassStub {

	private String code = null;

	private String name;
	private String namespace;
	private IType superclass;
	private boolean isFinal;
	private boolean isAbstract;
	private List<IType> interfaces;

	public ClassStub(ClassStubParameter parameters) {
		name = parameters.getName();
		superclass = parameters.getSuperclass();
		namespace = parameters.getNamespace();
		isFinal = parameters.isFinalClass();
		isAbstract = parameters.isAbstractClass();
		interfaces = parameters.getInterfaces();
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	// Is this useful?
	private static Map setupOptions(IScriptProject project) {

		Map options = new HashMap(PHPCorePlugin.getOptions());

		IScopeContext[] contents = new IScopeContext[] { new ProjectScope(project.getProject()),
				InstanceScope.INSTANCE, DefaultScope.INSTANCE };

		for (int i = 0; i < contents.length; i++) {

			IScopeContext scopeContext = contents[i];
			IEclipsePreferences inode = scopeContext.getNode(PHPCorePlugin.ID);

			if (inode != null) {

				if (!options.containsKey(PHPCoreConstants.FORMATTER_USE_TABS)) {

					String useTabs = inode.get(PHPCoreConstants.FORMATTER_USE_TABS, null);
					if (useTabs != null) {
						options.put(PHPCoreConstants.FORMATTER_USE_TABS, useTabs);
					}
				}

				if (!options.containsKey(PHPCoreConstants.FORMATTER_INDENTATION_SIZE)) {

					String size = inode.get(PHPCoreConstants.FORMATTER_INDENTATION_SIZE, null);

					if (size != null) {
						options.put(PHPCoreConstants.FORMATTER_INDENTATION_SIZE, size);
					}
				}
			}
		}

		return options;
	}

	/**
	 * Retrieve the code for a class stub.
	 * 
	 * @param parameterObject
	 */
	private void generateCode() {

		String lineDelim = "\n";

		StringBuilder buffer = new StringBuilder("<?php");
		buffer.append(lineDelim);

		buffer.append(generateNamespacePart());

		// try {
		// if (parameterObject.isComments()) {
		// String typeComment =
		// org.eclipse.php.ui.CodeGeneration.getTypeComment(parameterObject.getProject(),
		// parameterObject.getName(), lineDelim);
		// buffer.append(typeComment);
		// buffer.append(lineDelim);
		// }
		// } catch (CoreException e1) {
		// e1.printStackTrace();
		// }

		if (isFinal == true) {
			buffer.append("final ");
		}

		if (isAbstract == true) {
			buffer.append("abstract ");
		}

		buffer.append("class " + name);

		buffer.append(generateSuperclassPart());

		buffer.append(generateInterfacesPart());

		buffer.append("{}");

		// if (method.isConstructor() && parameterObject.isConstructor()) {
		// buffer.append(getMethodStub(parameterObject.getName(), method,
		// indent, lineDelim,
		// parameterObject.isComments()));
		// }
		// }
		// } catch (ModelException e) {
		// e.printStackTrace();
		// }
		// }
		//
		code = buffer.toString();
	}

	private String generateSuperclassPart() {
		if (superclass != null && superclass.getElementName() != null) {

			return " extends " + superclass.getElementName();
		}

		return "";
	}

	private String generateInterfacesPart() {

		String code = new String();
		if (!interfaces.isEmpty()) {
			code = " implements";

			int size = interfaces.size();
			int i = 1;
			for (IType interfaceObject : interfaces) {
				if (i < size) {
					code += " " + interfaceObject.getElementName() + ",";
				} else {
					code += " " + interfaceObject.getElementName();
				}
				i = i + 1;
			}
		}

		return code;
	}

	private String generateNamespacePart() {
		String code = new String();
		if (namespace != null && namespace.length() > 0) {
			code = "namespace " + namespace + ";\n\n";
		}

		if (superclass != null && superclass.getParent() != null && getNamespace(superclass) != null) {
			code += "use " + getNamespace(superclass) + ";\n";
		}
		
		if (interfaces != null) {
			for (IType interfaceObject : interfaces) {
				if (interfaceObject.getParent() != null && getNamespace(interfaceObject) != null) {
					code += "use " + getNamespace(superclass) + ";\n";
				}
			}
		}

		code += "\n";

		return code;
	}

	private String getNamespace(IType type) {
		// I'm not sure it is good way to check namespaces for class/interface
		if (type.getParent() != null && type.getParent().getElementType() == IType.TYPE) {

			return type.getParent().getElementName();
		}

		return null;
	}

	public String toString() {
		if (code == null) {
			generateCode();
		}

		return code;
	}
}
