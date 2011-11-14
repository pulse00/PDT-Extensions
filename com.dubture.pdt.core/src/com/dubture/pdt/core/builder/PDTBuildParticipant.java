package com.dubture.pdt.core.builder;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.dltk.ast.declarations.ModuleDeclaration;
import org.eclipse.dltk.core.ISourceModule;
import org.eclipse.dltk.core.SourceParserUtil;
import org.eclipse.dltk.core.builder.IBuildContext;
import org.eclipse.dltk.core.builder.IBuildParticipant;

import com.dubture.pdt.core.PDTVisitor;

public class PDTBuildParticipant implements IBuildParticipant {

	@Override
	public void build(IBuildContext context) throws CoreException
	{

		try {
			ISourceModule sourceModule = context.getSourceModule();		
			ModuleDeclaration moduleDeclaration = SourceParserUtil.getModuleDeclaration(sourceModule);							
			moduleDeclaration.traverse(new PDTVisitor(context));			
		} catch (Exception e) {
			e.printStackTrace();
		}				
	}

}
