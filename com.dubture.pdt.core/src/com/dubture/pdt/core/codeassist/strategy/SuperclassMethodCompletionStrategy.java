/*******************************************************************************
 * This file is part of the PDT Extensions eclipse plugin.
 * 
 * (c) Robert Gruendler <r.gruendler@gmail.com>
 * 
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 ******************************************************************************/
package com.dubture.pdt.core.codeassist.strategy;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.dltk.core.IMethod;
import org.eclipse.dltk.core.IModelElement;
import org.eclipse.dltk.core.ISourceModule;
import org.eclipse.dltk.core.IType;
import org.eclipse.dltk.core.index2.search.ISearchEngine.MatchRule;
import org.eclipse.dltk.core.search.IDLTKSearchScope;
import org.eclipse.dltk.core.search.SearchEngine;
import org.eclipse.dltk.internal.core.SourceRange;
import org.eclipse.dltk.internal.core.SourceType;
import org.eclipse.php.core.codeassist.ICompletionContext;
import org.eclipse.php.core.codeassist.ICompletionStrategy;
import org.eclipse.php.core.compiler.PHPFlags;
import org.eclipse.php.internal.core.codeassist.CodeAssistUtils;
import org.eclipse.php.internal.core.codeassist.ICompletionReporter;
import org.eclipse.php.internal.core.codeassist.strategies.AbstractCompletionStrategy;
import org.eclipse.php.internal.core.model.PhpModelAccess;

import com.dubture.pdt.core.codeassist.PDTCompletionInfo;
import com.dubture.pdt.core.codeassist.context.SuperclassMethodContext;

/**
 * Completionstrategy to insert method stubs from superclasses.
 * 
 */
@SuppressWarnings({ "restriction", "deprecation" })
public class SuperclassMethodCompletionStrategy extends
		AbstractCompletionStrategy implements ICompletionStrategy {

	/**
	 * @param context
	 */
	public SuperclassMethodCompletionStrategy(ICompletionContext context) {
		super(context);

	}

	/* (non-Javadoc)
	 * @see org.eclipse.php.core.codeassist.ICompletionStrategy#apply(org.eclipse.php.internal.core.codeassist.ICompletionReporter)
	 */
	@Override
	public void apply(ICompletionReporter reporter) throws Exception {

		SuperclassMethodContext context = (SuperclassMethodContext) getContext();
		ISourceModule module = context.getSourceModule();		
		
		IModelElement element = module.getElementAt(context.getOffset());
		
		if (!(element instanceof SourceType)) {			
			while(element.getParent() != null) {				
				element = element.getParent();				
				if (element instanceof SourceType) {
					break;
				}
			}
		}
		
		if (element == null || !(element instanceof SourceType)) {			
			return;
		}
		
		IDLTKSearchScope scope = SearchEngine.createSearchScope(module.getScriptProject());		
		SourceType type = (SourceType) element;
		SourceRange range = getReplacementRange(context);
		String prefix = context.getPrefix();
		
		for (String sClass : type.getSuperClasses()) {			
			IType[] superTypes = PhpModelAccess.getDefault().findTypes(sClass, MatchRule.EXACT, 0, 0, scope, new NullProgressMonitor());			
			for (IType superType : superTypes) {
				
				if (PHPFlags.isInterface(superType.getFlags()))
					continue;
				
				for (IMethod method : superType.getMethods()) {												
					if (CodeAssistUtils.startsWithIgnoreCase(method.getElementName(), prefix) && 
							(!PHPFlags.isPrivate(method.getFlags())) ) {
												
						reporter.reportMethod(method, "", range, new PDTCompletionInfo(module));
					}
				}				
			}
		}
	}
}
