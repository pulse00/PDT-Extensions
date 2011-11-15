/*
 * This file is part of the PDT Extensions eclipse plugin.
 *
 * (c) Robert Gruendler <r.gruendler@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */
package com.dubture.pdt.ui.contentassist;

import org.eclipse.dltk.core.IMethod;
import org.eclipse.dltk.core.IModelElement;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.TextUtilities;
import org.eclipse.php.internal.core.format.FormatPreferencesSupport;
import org.eclipse.php.internal.ui.editor.PHPStructuredEditor;
import org.eclipse.php.internal.ui.editor.PHPStructuredTextViewer;
import org.eclipse.php.internal.ui.editor.contentassist.PHPCompletionProposal;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.texteditor.ITextEditor;

import com.dubture.pdt.ui.codemanipulation.CodeGeneration;

/**
 *
 */
@SuppressWarnings("restriction")
public class SuperclassMethodCompletionProposal extends PHPCompletionProposal {

	private final IMethod method;
	private boolean replacementComputed = false;
	

	/**
	 * @param replacementString
	 * @param replacementOffset
	 * @param replacementLength
	 * @param image
	 * @param displayString
	 * @param relevance
	 * @param iMethod 
	 */
	public SuperclassMethodCompletionProposal(String replacementString,
			int replacementOffset, int replacementLength, Image image,
			String displayString, int relevance, IMethod iMethod) {
		super(replacementString, replacementOffset, replacementLength, image,
				displayString, relevance);
		
		method = iMethod;

	}


	/* (non-Javadoc)
	 * @see org.eclipse.dltk.ui.text.completion.AbstractScriptCompletionProposal#getReplacementString()
	 */
	@Override
	public String getReplacementString() {
	
		if (!replacementComputed) {
			return computeReplacementString();
		}
		return super.getReplacementString();
	}

	private String computeReplacementString() {

		ITextViewer viewer = getTextViewer();
		IDocument document = viewer.getDocument();
		ITextEditor textEditor = ((PHPStructuredTextViewer) viewer)
				.getTextEditor();

		try {

			if (textEditor instanceof PHPStructuredEditor) {
				IModelElement editorElement = ((PHPStructuredEditor) textEditor)
						.getModelElement();
				if (editorElement != null) {

					char indentChar = FormatPreferencesSupport.getInstance().getIndentationChar(document);
					String indent = String.valueOf(indentChar);

					String code = "";
					code += CodeGeneration.getMethodStub(method.getElementName(), method, indent, TextUtilities.getDefaultLineDelimiter(document), true);
					return code;

				}
			}		

		} catch (Exception e) {

			e.printStackTrace();
		}		
		
		return "";
	}
}
