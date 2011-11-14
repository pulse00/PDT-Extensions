/*
 * This file is part of the PDT Extensions eclipse plugin.
 *
 * (c) Robert Gruendler <r.gruendler@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */
package com.dubture.pdt.core.visitor;

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

import com.dubture.pdt.core.compiler.MissingMethodImplementation;
import com.dubture.pdt.core.util.PDTModelUtils;

@SuppressWarnings("restriction")
public class PDTVisitor extends PHPASTVisitor {

	private final ISourceModule context;
	
	private List<MissingMethodImplementation> missingInterfaceImplemetations = new ArrayList<MissingMethodImplementation>();
	
	private int nameStart;
	private int nameEnd;

	public PDTVisitor(ISourceModule sourceModule) {

		this.context = sourceModule;
	}
	
	public List<MissingMethodImplementation> getUnimplementedMethods() {
		
		return missingInterfaceImplemetations;
	}
	
	public boolean endvisit(ClassDeclaration s) throws Exception {

		Collection<TypeReference> interfaces = s.getInterfaceList();		
		IDLTKSearchScope scope = SearchEngine.createSearchScope(context.getScriptProject());
		
		PhpModelAccess model = PhpModelAccess.getDefault();
		
		nameStart = s.getNameStart();
		nameEnd = s.getNameEnd();		
		
		List<IMethod> unimplemented = new ArrayList<IMethod>();
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
		
		if (unimplemented.size() > 0) {			
			MissingMethodImplementation missing = new MissingMethodImplementation(s, unimplemented);
			missingInterfaceImplemetations.add(missing);
		}
		
		return super.endvisit(s);
	}

	public int getNameEnd() {
		return nameEnd;
	}

	public int getNameStart() {
		return nameStart;
	}

}
