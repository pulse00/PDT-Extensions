package com.dubture.pdt.ui.contentassist;

import org.eclipse.dltk.ast.declarations.ModuleDeclaration;
import org.eclipse.dltk.core.IMethod;
import org.eclipse.dltk.core.IModelElement;
import org.eclipse.dltk.core.ISourceModule;
import org.eclipse.dltk.core.ISourceRange;
import org.eclipse.dltk.core.IType;
import org.eclipse.dltk.core.SourceParserUtil;
import org.eclipse.dltk.internal.core.ModelElement;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.php.internal.ui.editor.PHPStructuredEditor;
import org.eclipse.php.internal.ui.editor.PHPStructuredTextViewer;
import org.eclipse.php.internal.ui.editor.contentassist.PHPCompletionProposal;
import org.eclipse.php.internal.ui.editor.contentassist.UseStatementInjector;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.texteditor.ITextEditor;

import com.dubture.pdt.core.PDTVisitor;
import com.dubture.pdt.ui.codemanipulation.CodeGeneration;

@SuppressWarnings("restriction")
public class InterfaceMethodCompletionProposal extends PHPCompletionProposal {

	public InterfaceMethodCompletionProposal(String replacementString,
			int replacementOffset, int replacementLength, Image image,
			String displayString, int relevance) {
		super(replacementString, replacementOffset, replacementLength, image,
				displayString, relevance);

	}
	
	

	@Override
	public void apply(ITextViewer viewer, char trigger, int stateMask,
			int offset) {

		IDocument document = viewer.getDocument();
		ITextEditor textEditor = ((PHPStructuredTextViewer) viewer)
				.getTextEditor();

		if (textEditor instanceof PHPStructuredEditor) {
			IModelElement editorElement = ((PHPStructuredEditor) textEditor)
					.getModelElement();
			if (editorElement != null) {
				
				ISourceModule sourceModule = ((ModelElement) editorElement)
						.getSourceModule();
								
				try {

					if (sourceModule.getTypes().length != 1) {
						return;
					}

					IType[] types = sourceModule.getTypes();
					IType type = types[0];
					
					
					ModuleDeclaration module = SourceParserUtil.getModuleDeclaration(sourceModule);
					PDTVisitor visitor = new PDTVisitor(sourceModule);
					module.traverse(visitor);
					
					String code = "";
					
					for (IMethod method : visitor.getUnimplementedMethods()) {
						code += CodeGeneration.getMethodStub(method.getParent().getElementName(), method, "\t", "\n", false);
					}
					
					ISourceRange range = type.getSourceRange();					
					document.replace(range.getOffset() + range.getLength()-2, 0, code);
					
					UseStatementInjector injector = new UseStatementInjector(this);
					injector.inject(document, getTextViewer(), range.getOffset());
					
					
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
}