package com.dubture.pdt.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.dltk.ast.declarations.MethodDeclaration;
import org.eclipse.dltk.ast.references.TypeReference;
import org.eclipse.dltk.core.IMethod;
import org.eclipse.dltk.core.ISourceModule;
import org.eclipse.dltk.core.IType;
import org.eclipse.dltk.core.index2.search.ISearchEngine.MatchRule;
import org.eclipse.dltk.core.search.IDLTKSearchScope;
import org.eclipse.dltk.core.search.SearchEngine;
import org.eclipse.php.internal.core.compiler.ast.nodes.ClassDeclaration;
import org.eclipse.php.internal.core.compiler.ast.nodes.FullyQualifiedReference;
import org.eclipse.php.internal.core.compiler.ast.visitor.PHPASTVisitor;
import org.eclipse.php.internal.core.model.PhpModelAccess;

import com.dubture.pdt.core.util.PDTModelUtils;

@SuppressWarnings("restriction")
public class PDTVisitor extends PHPASTVisitor {

	private final ISourceModule context;
	
	private List<IMethod> unimplemented = new ArrayList<IMethod>();

	public PDTVisitor(ISourceModule sourceModule) {

		this.context = sourceModule;
	}
	
	public List<IMethod> getUnimplementedMethods() {
		
		return unimplemented;
	}
	
	public boolean endvisit(ClassDeclaration s) throws Exception {

		Collection<TypeReference> interfaces = s.getInterfaceList();		
		IDLTKSearchScope scope = SearchEngine.createSearchScope(context.getScriptProject());
		
		PhpModelAccess model = PhpModelAccess.getDefault();
		for (TypeReference interf : interfaces) {
			
			if (interf instanceof FullyQualifiedReference) {
				
				FullyQualifiedReference fqr = (FullyQualifiedReference) interf;				
				IType[] types = model.findTypes(fqr.getName(), MatchRule.EXACT, 0, 0, scope, new NullProgressMonitor());
				
				for (IType type : types) {
					
					for (IMethod method : type.getMethods()) {

						String methodSignature = PDTModelUtils.getMethodSignature(method);
						
						boolean implemented = false;
						for (MethodDeclaration typeMethod : s.getMethods()) {					
							
							String signature = PDTModelUtils.getMethodSignature(typeMethod);
							
							if (methodSignature.equals(signature)) {
								implemented = true;
								break;
							}
						}
						
						if (!implemented) {
							unimplemented.add(method);
						}
					}					
				}				
			}
		}
		
		return super.endvisit(s);
	}	

}
