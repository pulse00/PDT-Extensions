package com.dubture.pdt.core.util;

import org.eclipse.dltk.ast.declarations.MethodDeclaration;
import org.eclipse.dltk.core.IMethod;
import org.eclipse.dltk.core.IParameter;
import org.eclipse.dltk.core.ModelException;
import org.eclipse.php.internal.core.compiler.ast.nodes.FormalParameter;

@SuppressWarnings("restriction")
public class ModelUtils {
	
	public static String getMethodSignature(MethodDeclaration method) {
		
		String signature = method.getName();
		
		for (Object o: method.getArguments()) {

			try {
				FormalParameter param = (FormalParameter) o;

				if (param.getParameterType() != null) {										
					signature += param.getParameterType().getName();										
				}
				
			} catch (ClassCastException e) {

			}
		}
		
		return signature;
				
	}
	
	public static String getMethodSignature(IMethod method) {
		
		String methodSignature = method.getElementName();
		
		try {
			for (IParameter param: method.getParameters()) {

				try {
					
					if (param.getType() != null) {										
						methodSignature += param.getType();										
					}
					
				} catch (ClassCastException e) {

				}
			}
		} catch (ModelException e) {
			e.printStackTrace();
		}
		
		return methodSignature;
		
		
	}	

}
