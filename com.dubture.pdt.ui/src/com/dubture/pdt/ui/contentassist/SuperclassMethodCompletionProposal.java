/*
 * This file is part of the PDT Extensions eclipse plugin.
 *
 * (c) Robert Gruendler <r.gruendler@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */
package com.dubture.pdt.ui.contentassist;

import org.eclipse.jface.text.ITextViewer;

import org.eclipse.php.internal.ui.editor.contentassist.PHPCompletionProposal;
import org.eclipse.swt.graphics.Image;

/**
 *
 */
@SuppressWarnings("restriction")
public class SuperclassMethodCompletionProposal extends PHPCompletionProposal {

	/**
	 * @param replacementString
	 * @param replacementOffset
	 * @param replacementLength
	 * @param image
	 * @param displayString
	 * @param relevance
	 */
	public SuperclassMethodCompletionProposal(String replacementString,
			int replacementOffset, int replacementLength, Image image,
			String displayString, int relevance) {
		super(replacementString, replacementOffset, replacementLength, image,
				displayString, relevance);
		// TODO Auto-generated constructor stub
	}
	
	
	
	/* (non-Javadoc)
	 * @see org.eclipse.dltk.ui.text.completion.AbstractScriptCompletionProposal#apply(org.eclipse.jface.text.ITextViewer, char, int, int)
	 */
	@Override
	public void apply(ITextViewer viewer, char trigger, int stateMask,
			int offset) {

		
		

	}

}
