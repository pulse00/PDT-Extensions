package com.dubture.pdt.ui.quickfix;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.dltk.core.ISourceModule;
import org.eclipse.dltk.ui.text.completion.IScriptCompletionProposal;
import org.eclipse.php.internal.ui.text.correction.IInvocationContext;
import org.eclipse.php.internal.ui.text.correction.IProblemLocation;
import org.eclipse.php.internal.ui.text.correction.IQuickFixProcessor;

import com.dubture.pdt.core.compiler.IPDTProblem;
import com.dubture.pdt.ui.contentassist.InterfaceMethodCompletionProposal;

@SuppressWarnings("restriction")
public class InterfaceMethodQuickFixProcessor implements IQuickFixProcessor {

	public InterfaceMethodQuickFixProcessor() {

	}

	@Override
	public boolean hasCorrections(ISourceModule unit, int problemId)
	{

		if (problemId == IPDTProblem.InterfaceRelated) {
			System.err.println("has fix");
			return true;
		}
		
		return false;
	}

	@Override
	public IScriptCompletionProposal[] getCorrections(
			IInvocationContext context, IProblemLocation[] locations)
			throws CoreException
	{
		
		InterfaceMethodCompletionProposal prop = new InterfaceMethodCompletionProposal("foo", 0, 100, null, "Add unimplemented methods", 100);
		
		return new IScriptCompletionProposal[] { prop };		

		
	}

}
