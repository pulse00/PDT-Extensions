/*******************************************************************************
 * This file is part of the PDT Extensions eclipse plugin.
 * 
 * (c) Robert Gruendler <r.gruendler@gmail.com>
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
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.dltk.core.IMethod;
import org.eclipse.dltk.core.IParameter;
import org.eclipse.dltk.core.IScriptProject;
import org.eclipse.dltk.core.IType;
import org.eclipse.dltk.core.ModelException;
import org.eclipse.dltk.ti.types.IEvaluatedType;
import org.eclipse.php.core.compiler.PHPFlags;
import org.eclipse.php.internal.core.PHPCoreConstants;
import org.eclipse.php.internal.core.PHPCorePlugin;
import org.eclipse.php.internal.core.format.DefaultCodeFormattingProcessor;

import com.dubture.pdt.core.util.PDTModelUtils;

/**
 * Utilities for code generation.
 * 
 * 
 * @author Robert Gruendler <r.gruendler@gmail.com>
 *
 */
@SuppressWarnings("restriction")
public class CodeGeneration {
	

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static Map setupOptions(IScriptProject project) {

		Map options = new HashMap(PHPCorePlugin.getOptions());
		
		IScopeContext[] contents = new IScopeContext[] {
				new ProjectScope(project
						.getProject()),
						InstanceScope.INSTANCE, DefaultScope.INSTANCE };
		
		for (int i = 0; i < contents.length; i++) {
			
			IScopeContext scopeContext = contents[i];
			IEclipsePreferences inode = scopeContext.getNode(PHPCorePlugin.ID);
			
			if (inode != null) {
				
				if (!options.containsKey(PHPCoreConstants.FORMATTER_USE_TABS)) {
					
					String useTabs = inode.get(PHPCoreConstants.FORMATTER_USE_TABS,null);
					if (useTabs != null) {
						options.put(PHPCoreConstants.FORMATTER_USE_TABS, useTabs);
					}
				}
				
				if (!options.containsKey(PHPCoreConstants.FORMATTER_INDENTATION_SIZE)) {
					
					String size = inode.get(PHPCoreConstants.FORMATTER_INDENTATION_SIZE,null);
					
					if (size != null) {
						options.put(PHPCoreConstants.FORMATTER_INDENTATION_SIZE,size);
					}
				}
			}
		}
		
		return options;
	}
	
	
	/**
	 * Retrieve the code for a class stub.
	 */
	@SuppressWarnings("rawtypes")
	public static String getClassStub(IScriptProject project, String name, String namespace, String modifier, IType superclass, 
			List<IType> interfaces, boolean constructor, boolean abstractMethods, boolean comments) {

		String lineDelim = "\n";
		
		StringBuilder buffer = new StringBuilder("<?php");
		buffer.append(lineDelim);
		
		if (namespace != null && namespace.length() > 0) {
			buffer.append("namespace ");
			buffer.append(namespace + ";");
		}
		
		List<IType> types = new ArrayList<IType>();
		if (superclass != null)
			types.add(superclass);
		
		types.addAll(interfaces);
		
		List<IEvaluatedType> useStatements = PDTModelUtils.collectUseStatements(types, abstractMethods);
		
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
		
		buffer.append(lineDelim + lineDelim);
		
		if (modifier != null && modifier.length() > 0)
			buffer.append(modifier + " " );
		
		
		try {
			if (comments) {
				String typeComment = org.eclipse.php.ui.CodeGeneration.getTypeComment(project, name, lineDelim);
				buffer.append(typeComment);
				buffer.append(lineDelim);
			}			
		} catch (CoreException e1) {
			e1.printStackTrace();
		}
		buffer.append("class " + name);
		
		if (superclass != null) {
			buffer.append(" extends " + superclass.getElementName());
		}
		
		if (interfaces != null && interfaces.size() > 0) {			
			buffer.append(" implements ");
			
			int i=0;
			
			for (IType iface : interfaces) {				
				buffer.append(iface.getElementName());
				
				if (i++ < interfaces.size()-1) {
					buffer.append(", ");
				}
			}
		}
		
		buffer.append(" {");
		buffer.append(lineDelim);
		buffer.append(lineDelim);
		
		Map options = setupOptions(project);		
		DefaultCodeFormattingProcessor formatter = new DefaultCodeFormattingProcessor(options);		
		String indent = formatter.createIndentationString(1);
				
		if (superclass != null) {
			try {
				for (IMethod method : superclass.getMethods()) {
								
					if (PHPFlags.isAbstract(method.getFlags()) && abstractMethods) {						
						buffer.append(getMethodStub(name, method, indent, lineDelim, comments));
						buffer.append(lineDelim);
					}
					
					if (method.isConstructor() && constructor) {
						buffer.append(getMethodStub(name, method, indent, lineDelim, comments));
					}
				}
			} catch (ModelException e) {
				e.printStackTrace();
			}			
		}
		
		for (IType type : interfaces) {			
			try {
				for (IMethod method : type.getMethods()) {
					buffer.append(getMethodStub(name, method, indent, lineDelim, comments));				
				}
			} catch (ModelException e) {
				e.printStackTrace();
			}			
		}
		
		buffer.append("}");
		
		return buffer.toString();
	}


	/**
	 * 
	 * Retrieve the code stub for a given {@link IMethod}
	 * 
	 */
	public static String getMethodStub(String parent, IMethod method, String indent, String lineDelim, boolean comments) throws ModelException {

		StringBuilder buffer = new StringBuilder();				
		String comment = null;
		if (comments) {
			try {
				comment = org.eclipse.php.ui.CodeGeneration.getMethodComment(method, null, lineDelim);
				comment = indentPattern(lineDelim + comment, indent, lineDelim);
			} catch (CoreException e) {
				e.printStackTrace();
			}
		}
		
		if (comments && comment != null) {
			buffer.append(comment);
		}
		
		String modifier = "public";
		
		try {
			if (PHPFlags.isPrivate(method.getFlags())) {
				modifier = "private";
			} else if (PHPFlags.isProtected(method.getFlags())) {
				modifier = "protected";
			}
		} catch (ModelException e) {
			e.printStackTrace();
		} 
		
		String signatureIndent = comments ? "" : indent;
		buffer.append(signatureIndent + modifier + " function ");
		 
		String methodName = method.isConstructor() ? parent : method.getElementName();
		buffer.append(methodName);
		buffer.append("(");
		
		int i=0;
		int size = method.getParameters().length;
		
		for (IParameter param : method.getParameters()) {
			
			if (PDTModelUtils.isValidType(param.getType(), method.getScriptProject())) {
				buffer.append(param.getType() + " ");
			}
			
			buffer.append(param.getName());
			
			if (param.getDefaultValue() != null) {
				buffer.append(" = " + param.getDefaultValue());
			}
			if (i++ < size-1) {
				buffer.append(", ");
			}			
		}
		
		buffer.append(") {");
		buffer.append(lineDelim);
		
		buffer.append(indent + indent + "// TODO: Auto-generated method stub");
		buffer.append(lineDelim);
		buffer.append(lineDelim);
		
		buffer.append(indent + "}");
		buffer.append(lineDelim);
		
		return buffer.toString();		
		
	}
	
	
	private static String indentPattern(String originalPattern, String indentation,
			String lineDelim) {
		
		String delimPlusIndent = lineDelim + indentation;
		String indentedPattern = originalPattern.replaceAll(lineDelim,delimPlusIndent) + delimPlusIndent;

		return indentedPattern;
	}
	
}
