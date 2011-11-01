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
import org.eclipse.php.internal.core.format.DefaultCodeFormattingProcessor;

import com.dubture.pdt.core.util.PDTModelUtils;

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
	
	
	
	@SuppressWarnings("rawtypes")
	public static String getClassStub(IScriptProject project, String name, String namespace, String modifier, IType superclass, 
			List<IType> interfaces, boolean constructor, boolean abstractMethods) {

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
				buffer.append(String.format("use %s;%s", useStatement.getTypeName(), lineDelim));
			}
			
		}
		
		buffer.append(lineDelim + lineDelim);
		
		if (modifier != null && modifier.length() > 0)
			buffer.append(modifier + " " );
		
		buffer.append("class " + name);
		
		if (superclass != null) {			
			buffer.append(" extends " + superclass);
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
		
		buffer.append("{");
		buffer.append(lineDelim);
		buffer.append(lineDelim);
		buffer.append("}");
		
		Map options = setupOptions(project);		
		DefaultCodeFormattingProcessor formatter = new DefaultCodeFormattingProcessor(options);		
		String indent = formatter.createIndentationString(1);
		
		
		return buffer.toString();
	}
	


}
