package com.dubture.pdt.core.builder;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.dltk.ast.declarations.ModuleDeclaration;
import org.eclipse.dltk.compiler.problem.DefaultProblem;
import org.eclipse.dltk.compiler.problem.IProblem;
import org.eclipse.dltk.compiler.problem.ProblemSeverity;
import org.eclipse.dltk.core.IMethod;
import org.eclipse.dltk.core.ISourceModule;
import org.eclipse.dltk.core.ISourceRange;
import org.eclipse.dltk.core.IType;
import org.eclipse.dltk.core.ModelException;
import org.eclipse.dltk.core.SourceParserUtil;
import org.eclipse.dltk.core.builder.IBuildContext;
import org.eclipse.dltk.core.builder.IBuildParticipant;

import com.dubture.pdt.core.PDTVisitor;
import com.dubture.pdt.core.compiler.IPDTProblem;

public class PDTBuildParticipant implements IBuildParticipant {

	private IBuildContext context;
	
	@Override
	public void build(IBuildContext context) throws CoreException
	{

		try {
			this.context = context;
			ISourceModule sourceModule = context.getSourceModule();		
			ModuleDeclaration moduleDeclaration = SourceParserUtil.getModuleDeclaration(sourceModule);
			
			PDTVisitor visitor = new PDTVisitor(sourceModule);
			moduleDeclaration.traverse(visitor);			
			
			if (visitor.getUnimplementedMethods().size() > 0) {
				reportUnimplementedMethods(visitor.getUnimplementedMethods(), sourceModule);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}				
	}

	@SuppressWarnings("deprecation")
	private void reportUnimplementedMethods(List<IMethod> unimplementedMethods, ISourceModule sourceModule)
	{

		try {
			
			IType[] types;			
			types = sourceModule.getTypes();
			IType type = types[0];

			ISourceRange range = type.getSourceRange();


			ProblemSeverity severity = ProblemSeverity.WARNING;
			int lineNo = context.getLineTracker().getLineInformationOfOffset(range.getOffset()).getOffset();
			String message = "Missing method implementations: ";

			for (IMethod m : unimplementedMethods) {							
				message += m.getElementName() + ", ";							
			}

			IProblem problem = new DefaultProblem(context.getFileName(), message, IPDTProblem.InterfaceRelated,
					new String[0], severity, range.getOffset(), range.getOffset() + range.getLength(),lineNo);

			context.getProblemReporter().reportProblem(problem);

		} catch (ModelException e) {
			e.printStackTrace();
		}
	}
}
