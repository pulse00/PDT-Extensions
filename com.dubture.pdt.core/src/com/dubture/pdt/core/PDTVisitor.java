package com.dubture.pdt.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.dltk.ast.declarations.MethodDeclaration;
import org.eclipse.dltk.ast.references.TypeReference;
import org.eclipse.dltk.compiler.problem.DefaultProblem;
import org.eclipse.dltk.compiler.problem.IProblem;
import org.eclipse.dltk.compiler.problem.ProblemSeverity;
import org.eclipse.dltk.core.IMethod;
import org.eclipse.dltk.core.IType;
import org.eclipse.dltk.core.builder.IBuildContext;
import org.eclipse.dltk.core.index2.search.ISearchEngine.MatchRule;
import org.eclipse.dltk.core.search.IDLTKSearchScope;
import org.eclipse.dltk.core.search.SearchEngine;
import org.eclipse.php.internal.core.compiler.ast.nodes.ClassDeclaration;
import org.eclipse.php.internal.core.compiler.ast.nodes.FullyQualifiedReference;
import org.eclipse.php.internal.core.compiler.ast.visitor.PHPASTVisitor;
import org.eclipse.php.internal.core.model.PhpModelAccess;

import com.dubture.pdt.core.compiler.IPDTProblem;
import com.dubture.pdt.core.util.ModelUtils;

@SuppressWarnings("restriction")
public class PDTVisitor extends PHPASTVisitor {

	private final IBuildContext context;

	public PDTVisitor(IBuildContext context) {

		this.context = context;
	}
	
	@SuppressWarnings("deprecation")
	public boolean endvisit(ClassDeclaration s) throws Exception {

		Collection<TypeReference> interfaces = s.getInterfaceList();		
		IDLTKSearchScope scope = SearchEngine.createSearchScope(context.getSourceModule().getScriptProject());
		
		PhpModelAccess model = PhpModelAccess.getDefault();
		for (TypeReference interf : interfaces) {
			
			if (interf instanceof FullyQualifiedReference) {
				
				FullyQualifiedReference fqr = (FullyQualifiedReference) interf;				
				IType[] types = model.findTypes(fqr.getName(), MatchRule.EXACT, 0, 0, scope, new NullProgressMonitor());
				
				for (IType type : types) {
					
					List<IMethod> unimplemented = new ArrayList<IMethod>();
					
					for (IMethod method : type.getMethods()) {

						String methodSignature = ModelUtils.getMethodSignature(method);
						
						boolean implemented = false;
						for (MethodDeclaration typeMethod : s.getMethods()) {					
							
							String signature = ModelUtils.getMethodSignature(typeMethod);
							
							if (methodSignature.equals(signature)) {
								implemented = true;
								break;
							}
						}
						
						if (!implemented) {
							unimplemented.add(method);
						}
					}
					
					if (unimplemented.size() > 0) {
						
						//TODO: add preference page for that
						ProblemSeverity severity = ProblemSeverity.WARNING;
						int lineNo = context.getLineTracker().getLineInformationOfOffset(fqr.sourceStart()).getOffset();
						String message = "Missing method implementations: ";
						
						for (IMethod m : unimplemented) {							
							message += m.getElementName() + ", ";							
						}
						
						IProblem problem = new DefaultProblem(context.getFileName(), message, IPDTProblem.InterfaceRelated,
								new String[0], severity, fqr.sourceStart(), fqr.sourceEnd(),lineNo);
						
						context.getProblemReporter().reportProblem(problem);
						
					}					
				}
				
			}
		}
		
		return super.endvisit(s);
	}	

}
