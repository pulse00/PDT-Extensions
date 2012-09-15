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
package com.dubture.pdt.ui.codemanipulation;

import java.util.ArrayList;
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
import org.eclipse.dltk.ti.types.IEvaluatedType;
import org.eclipse.php.internal.core.PHPCoreConstants;
import org.eclipse.php.internal.core.PHPCorePlugin;
import com.dubture.pdt.core.util.PDTModelUtils;

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

	public ClassStub(ClassStubParameter parameters) {
		name = parameters.getName();
		superclass = parameters.getSuperclass();
		namespace = parameters.getNamespace();
		isFinal = parameters.isFinalClass();
		isAbstract = parameters.isAbstractClass();
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

		//
		// generateInterfacesPart(parameterObject, lineDelim, buffer);
		//
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

		// if (parameterObject.getInterfaces() != null &&
		// parameterObject.getInterfaces().size() > 0) {
		// buffer.append(" implements ");
		//
		// int i = 0;
		//
		// for (IType iface : parameterObject.getInterfaces()) {
		// buffer.append(iface.getElementName());
		//
		// if (i++ < parameterObject.getInterfaces().size() - 1) {
		// buffer.append(", ");
		// }
		// }
		// }
		//
		buffer.append("{}");
		//
		// Map options = setupOptions(parameterObject.getProject());
		// DefaultCodeFormattingProcessor formatter = new
		// DefaultCodeFormattingProcessor(options);
		// String indent = formatter.createIndentationString(1);
		//
		// if (parameterObject.getSuperclass() != null) {
		// try {
		// for (IMethod method : parameterObject.getSuperclass().getMethods()) {
		//
		// if (PHPFlags.isAbstract(method.getFlags()) &&
		// parameterObject.isAbstractMethods()) {
		// buffer.append(getMethodStub(parameterObject.getName(), method,
		// indent, lineDelim,
		// parameterObject.isComments()));
		// buffer.append(lineDelim);
		// }
		//
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
		// for (IType type : parameterObject.getInterfaces()) {
		// try {
		// for (IMethod method : type.getMethods()) {
		// buffer.append(getMethodStub(parameterObject.getName(), method,
		// indent, lineDelim,
		// parameterObject.isComments()));
		// }
		// } catch (ModelException e) {
		// e.printStackTrace();
		// }
		// }

		code = buffer.toString();
	}

	private String generateSuperclassPart() {
		if (superclass != null && superclass.getElementName() != null) {

			return " extends " + superclass.getElementName();
		}

		return "";
	}

	private void generateInterfacesPart(ClassStubParameter parameterObject, String lineDelim, StringBuilder buffer) {
		List<IType> types = new ArrayList<IType>();
		if (parameterObject.getSuperclass() != null)
			types.add(parameterObject.getSuperclass());

		types.addAll(parameterObject.getInterfaces());

		List<IEvaluatedType> useStatements = PDTModelUtils.collectUseStatements(types,
				parameterObject.isAbstractMethods());

		if (useStatements.size() > 0) {
			buffer.append(lineDelim + lineDelim);
			for (IEvaluatedType useStatement : useStatements) {

				String typeName = useStatement.getTypeName();

				if (typeName.startsWith("\\")) {
					typeName = typeName.replaceFirst("\\\\", "");
				}
				buffer.append(String.format("use %s;%s", typeName, lineDelim));
			}
		}
	}

	private String generateNamespacePart() {
		String code = new String();
		if (namespace != null && namespace.length() > 0) {
			code = "namespace " + namespace + ";\n\n";
		}

		if (superclass != null && superclass.getParent() != null && getNamespace(superclass) != null) {
			code += "use " + getNamespace(superclass) + ";\n";
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
