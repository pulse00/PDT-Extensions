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

import java.util.List;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.dltk.core.IScriptProject;
import org.eclipse.dltk.core.IType;
import org.eclipse.php.ui.CodeGeneration;

/**
 * Utilities for class generation.
 * 
 * @author Robert Gruendler <r.gruendler@gmail.com>
 * @author Marek Maksimczyk <marek.maksimczyk@mandos.net.pl>
 */
public class ClassStub {

	private String code = null;
	private IScriptProject scriptProject = null;

	private String name;
	private String namespace;
	private IType superclass;
	private boolean isFinal;
	private boolean isAbstract;
	private List<IType> interfaces;
	private boolean generateComments;
	private boolean generateConstructor;
	private boolean generateInheritedMethods;

	public ClassStub(IScriptProject scriptProject, ClassStubParameter parameters) {
		this.scriptProject = scriptProject;

		name = parameters.getName();
		superclass = parameters.getSuperclass();
		namespace = parameters.getNamespace();
		isFinal = parameters.isFinalClass();
		isAbstract = parameters.isAbstractClass();
		interfaces = parameters.getInterfaces();
		generateComments = parameters.isComments();
		generateConstructor = parameters.isConstructor();
		generateInheritedMethods = parameters.isAbstractMethods();
	}

	/**
	 * Retrieve the code for a class stub.
	 * 
	 * @throws CoreException
	 */
	private void generateCode() throws CoreException {

		String lineDelim = "\n";

		StringBuilder buffer = new StringBuilder("<?php");
		buffer.append(lineDelim);

		buffer.append(generateNamespacePart());

		if (generateComments == true)
			buffer.append(CodeGeneration.getTypeComment(scriptProject, name, lineDelim) + lineDelim);

		if (isFinal == true) {
			buffer.append("final ");
		}

		if (isAbstract == true) {
			buffer.append("abstract ");
		}

		buffer.append("class " + name);

		buffer.append(generateSuperclassPart());

		buffer.append(generateInterfacesPart());

		buffer.append("{" + lineDelim);

		if(generateConstructor) {
			//TODO: generate Constructor
		}
		
		if(generateInheritedMethods) {
			// TODO: generate abstract and interfaces' methods.
		}
		
		buffer.append(lineDelim + "}");
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
					code += "use " + getNamespace(interfaceObject) + ";\n";
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
			try {
				generateCode();
			} catch (CoreException e) {
				e.printStackTrace();
			}
		}

		return code;
	}
}
