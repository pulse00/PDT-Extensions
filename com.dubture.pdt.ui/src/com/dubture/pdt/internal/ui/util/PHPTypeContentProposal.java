package com.dubture.pdt.internal.ui.util;

import org.eclipse.dltk.core.IType;
import org.eclipse.jface.fieldassist.ContentProposal;

public class PHPTypeContentProposal extends ContentProposal {

	private IType object;

	public PHPTypeContentProposal(String content, IType object) {
		super(content);
		this.object = object;
	}

	public IType getObject() {
		return object;
	}
}