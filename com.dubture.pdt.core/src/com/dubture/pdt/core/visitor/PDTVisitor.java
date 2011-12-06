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
import java.util.Map;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.dltk.ast.declarations.MethodDeclaration;
import org.eclipse.dltk.ast.declarations.ModuleDeclaration;
import org.eclipse.dltk.ast.parser.IModuleDeclaration;
import org.eclipse.dltk.ast.references.TypeReference;
import org.eclipse.dltk.core.IMethod;
import org.eclipse.dltk.core.IScriptProject;
import org.eclipse.dltk.core.ISourceModule;
import org.eclipse.dltk.core.IType;
import org.eclipse.dltk.core.SourceParserUtil;
import org.eclipse.dltk.core.index2.search.ISearchEngine.MatchRule;
import org.eclipse.dltk.core.search.IDLTKSearchScope;
import org.eclipse.dltk.core.search.SearchEngine;
import org.eclipse.php.internal.core.compiler.ast.nodes.ClassDeclaration;
import org.eclipse.php.internal.core.compiler.ast.nodes.FullyQualifiedReference;
import org.eclipse.php.internal.core.compiler.ast.nodes.NamespaceReference;
import org.eclipse.php.internal.core.compiler.ast.nodes.UsePart;
import org.eclipse.php.internal.core.compiler.ast.visitor.PHPASTVisitor;
import org.eclipse.php.internal.core.model.PhpModelAccess;
import org.eclipse.php.internal.core.typeinference.PHPModelUtils;

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
		IScriptProject project = context.getScriptProject();
		IDLTKSearchScope scope = SearchEngine.createSearchScope(project);		
		PhpModelAccess model = PhpModelAccess.getDefault();		
		nameStart = s.getNameStart();
		nameEnd = s.getNameEnd();			
		IModuleDeclaration module = SourceParserUtil.parse(context, null);
		List<IMethod> unimplemented = new ArrayList<IMethod>();
		
		for (TypeReference interf : interfaces) {
			
			if (interf instanceof FullyQualifiedReference) {
				
				FullyQualifiedReference fqr = (FullyQualifiedReference) interf;				
				NamespaceReference ns = fqr.getNamespace();
				String typeName = "";
				if (ns != null) {
					typeName = fqr.getFullyQualifiedName();
					
				} else {

					IType currentNamespace = PHPModelUtils.getCurrentNamespace(context, fqr.sourceStart());
					final Map<String, UsePart> result = PHPModelUtils.getAliasToNSMap(fqr.getName()	, (ModuleDeclaration) module, fqr.sourceStart(), currentNamespace, true);

					if (result.containsKey(fqr.getName())) {
						
						typeName = result.get(fqr.getName()).getNamespace().getFullyQualifiedName();
					}					
				}

				IType[] types = model.findTypes(typeName, MatchRule.EXACT, 0, 0, scope, new NullProgressMonitor());
				
				if (types.length != 1) {
					continue;
				} 
				
				IType type = types[0];
				
				for (IMethod method : type.getMethods()) {

					String methodSignature = PDTModelUtils.getMethodSignature(method);
					boolean implemented = false;
					for (MethodDeclaration typeMethod : s.getMethods()) {					
						
						String signature = PDTModelUtils.getMethodSignature(typeMethod, project);						
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
