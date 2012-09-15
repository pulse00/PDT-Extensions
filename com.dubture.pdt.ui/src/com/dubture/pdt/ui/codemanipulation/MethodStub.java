/*******************************************************************************
 * This file is part of the PDT Extensions eclipse plugin.
 * 
 * (c) Robert Gruendler <r.gruendler@gmail.com>
 * 
 * Modified by Marek Maksimczyk <marek.maksimczyk@mandos.net.pl>
 * 
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 ******************************************************************************/
package com.dubture.pdt.ui.codemanipulation;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.dltk.core.IMethod;
import org.eclipse.dltk.core.IParameter;
import org.eclipse.dltk.core.ModelException;
import org.eclipse.php.core.compiler.PHPFlags;

import com.dubture.pdt.core.util.PDTModelUtils;

public class MethodStub {

	/**
	 * 
	 * Retrieve the code stub for a given {@link IMethod}
	 * 
	 */
	public static String getMethodStub(String parent, IMethod method, String indent, String lineDelim, boolean comments)
			throws ModelException {

		StringBuilder buffer = new StringBuilder();
		String comment = null;
		if (comments) {
			try {
				comment = org.eclipse.php.ui.CodeGeneration.getMethodComment(method, null, lineDelim);
				comment = indentPattern(lineDelim + comment, indent, lineDelim);
			} catch (CoreException e) {
				e.printStackTrace();
			}
		}

		if (comments && comment != null) {
			buffer.append(comment);
		}

		String modifier = "public";

		try {
			if (PHPFlags.isPrivate(method.getFlags())) {
				modifier = "private";
			} else if (PHPFlags.isProtected(method.getFlags())) {
				modifier = "protected";
			}
		} catch (ModelException e) {
			e.printStackTrace();
		}

		String signatureIndent = comments ? "" : indent;
		buffer.append(signatureIndent + modifier + " function ");

		String methodName = method.isConstructor() ? parent : method.getElementName();
		buffer.append(methodName);
		buffer.append("(");

		int i = 0;
		int size = method.getParameters().length;

		for (IParameter param : method.getParameters()) {

			if (PDTModelUtils.isValidType(param.getType(), method.getScriptProject())) {
				buffer.append(param.getType() + " ");
			}

			buffer.append(param.getName());

			if (param.getDefaultValue() != null) {
				if ("array".equals(param.getType()) && param.getDefaultValue().trim().length() == 0) {
					buffer.append(" = array()");
				} else {
					buffer.append(" = " + param.getDefaultValue());
				}
			}
			if (i++ < size - 1) {
				buffer.append(", ");
			}
		}

		buffer.append(") {");
		buffer.append(lineDelim);

		buffer.append(indent + indent + "// TODO: Auto-generated method stub");
		buffer.append(lineDelim);
		buffer.append(lineDelim);

		buffer.append(indent + "}");
		buffer.append(lineDelim);

		return buffer.toString();

	}

	private static String indentPattern(String originalPattern, String indentation, String lineDelim) {

		String delimPlusIndent = lineDelim + indentation;
		String indentedPattern = originalPattern.replaceAll(lineDelim, delimPlusIndent) + delimPlusIndent;

		return indentedPattern;
	}

}
