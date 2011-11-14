/*******************************************************************************
 * This file is part of the PDT Extensions eclipse plugin.

 * 
 * (c) Robert Gruendler <r.gruendler@gmail.com>
 * 
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 ******************************************************************************/
package com.dubture.pdt.core.codeassist.context;

import org.eclipse.dltk.core.CompletionRequestor;
import org.eclipse.dltk.core.ISourceModule;
import org.eclipse.php.internal.core.codeassist.contexts.AbstractCompletionContext;

/**
 *
 */
@SuppressWarnings("restriction")
public class SuperclassMethodContext extends AbstractCompletionContext {
	
	/* (non-Javadoc)
	 * @see org.eclipse.php.internal.core.codeassist.contexts.AbstractCompletionContext#isValid(org.eclipse.dltk.core.ISourceModule, int, org.eclipse.dltk.core.CompletionRequestor)
	 */
	@Override
	public boolean isValid(ISourceModule sourceModule, int offset,
			CompletionRequestor requestor) {


		return super.isValid(sourceModule, offset, requestor);
	}

}
