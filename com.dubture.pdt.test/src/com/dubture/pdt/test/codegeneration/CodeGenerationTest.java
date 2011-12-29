package com.dubture.pdt.test.codegeneration;

import junit.framework.TestCase;

import org.eclipse.dltk.core.ModelException;
import org.eclipse.php.internal.core.typeinference.FakeMethod;
import org.junit.Test;

import com.dubture.pdt.ui.codemanipulation.CodeGeneration;

@SuppressWarnings("restriction")
public class CodeGenerationTest extends TestCase {
	
	@Test
	public void testMethodGeneration() {
		
		try {
			
			FakeMethod fMethod = new FakeMethod(null, "test");		
			String stub = CodeGeneration.getMethodStub(null, fMethod, "\t", "\n", false);			
			assertEquals(68, stub.length());			
			assertEquals("test()", stub.substring(17, 23));

			
		} catch (ModelException e) {
			fail();
		}		
	}
	
}
