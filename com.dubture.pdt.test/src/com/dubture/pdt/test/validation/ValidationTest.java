package com.dubture.pdt.test.validation;

import java.io.IOException;
import java.net.URL;

import junit.framework.TestCase;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.dltk.ast.declarations.ModuleDeclaration;
import org.eclipse.dltk.core.DLTKCore;
import org.eclipse.dltk.core.ISourceModule;
import org.eclipse.dltk.core.SourceParserUtil;
import org.junit.Test;
import org.osgi.framework.Bundle;

import com.dubture.pdt.core.visitor.PDTVisitor;
import com.dubture.pdt.test.PDTTestPlugin;


public class ValidationTest extends TestCase {
	
	private IFile testFile;
	
	private IProject project = null;
	
	protected ISourceModule getSource(String path, String name) throws CoreException, IOException {
		
		if (project == null) {
			project = PDTTestPlugin.createProject("TestProject");	
		}
		 			
		Bundle bundle = PDTTestPlugin.getDefault().getBundle();
		URL test = bundle.getResource(path + "/" + name);
		testFile = project.getFile(name);
		testFile.create(test.openStream(), true, null);

		project.refreshLocal(IResource.DEPTH_INFINITE, null);
		project.build(IncrementalProjectBuilder.FULL_BUILD, null);

		PDTTestPlugin.waitForIndexer();
		PDTTestPlugin.waitForAutoBuild();

		return DLTKCore.createSourceModuleFrom(testFile);		
		
	}
	
	protected PDTVisitor getVisitor(ISourceModule source) throws Exception {
				
		assertNotNull(source);			
		ModuleDeclaration moduleDeclaration = SourceParserUtil.getModuleDeclaration(source);
		PDTVisitor visitor = new PDTVisitor(source);
		moduleDeclaration.traverse(visitor);
		return visitor;
		
	}
	
	
	@Test
	public void testValidation() {
		
		try {
			
			ISourceModule source = getSource("workspace/validation", "Test.php");
			assertEquals(1, getVisitor(source).getUnimplementedMethods().size());			
			
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}		
	}
	
	@Test
	public void testImplementationSuccess() {

		try {
			
			ISourceModule source = getSource("workspace/validation", "Test2.php");
			assertEquals(0, getVisitor(source).getUnimplementedMethods().size());
			
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}		
	}
}
