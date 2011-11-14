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

	private IMethod method;



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
					code += CodeGeneration.getMethodStub(method.getParent().getElementName(), method, indent, TextUtilities.getDefaultLineDelimiter(document), false);
					document.replace(offset, 0, code);

				}
			}		

		} catch (Exception e) {

			e.printStackTrace();
		}
	}



	/**
	 * @param modelElement
	 */
	public void setMethod(IMethod modelElement) {

		this.method = modelElement;
		
	}

}
