/*
 * This file is part of the PDT Extensions eclipse plugin.
 *
 * (c) Robert Gruendler <r.gruendler@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */
package com.dubture.pdt.ui.contentassist;

import org.eclipse.dltk.core.CompletionProposal;
import org.eclipse.dltk.core.ISourceModule;
import org.eclipse.dltk.ui.text.completion.IScriptCompletionProposal;
import org.eclipse.dltk.ui.text.completion.ScriptCompletionProposal;
import org.eclipse.jface.text.IDocument;
import org.eclipse.php.internal.ui.editor.contentassist.PHPCompletionProposalCollector;
import org.eclipse.swt.graphics.Image;

import com.dubture.pdt.core.codeassist.PDTCompletionInfo;
import com.dubture.pdt.core.model.SuperclassMethod;

/**
 *
 */
@SuppressWarnings("restriction")
public class PDTScriptCompletionProposalCollector extends
		PHPCompletionProposalCollector {

	/**
	 * @param document
	 * @param cu
	 * @param explicit
	 */
	public PDTScriptCompletionProposalCollector(IDocument document,
			ISourceModule cu, boolean explicit) {
		super(document, cu, explicit);

	}
	
	
	@Override
	protected IScriptCompletionProposal createScriptCompletionProposal(
			CompletionProposal proposal) {
		
		Object info = proposal.getExtraInfo();
		
		
		if ( !(info instanceof PDTCompletionInfo) )
			return super.createScriptCompletionProposal(proposal);
				
		
		String completion = new String(proposal.getCompletion());
		int replaceStart = proposal.getReplaceStart();
		int length = getLength(proposal);
		Image image = getImage(getLabelProvider().createTypeImageDescriptor(proposal));
		String displayString = (getLabelProvider()).createLabel(proposal);

		ScriptCompletionProposal scriptProposal = new SuperclassMethodCompletionProposal(completion, replaceStart, length, image, displayString, 0);

		scriptProposal.setRelevance(computeRelevance(proposal));
		//			scriptProposal.setProposalInfo(new BundleProposalInfo(getSourceModule().getScriptProject(), typeProposal));

		return scriptProposal;

		
	}


}
