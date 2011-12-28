package com.dubture.pdt.test;

import java.util.LinkedHashMap;
import java.util.Map;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.dltk.core.CompletionProposal;
import org.eclipse.php.core.tests.AbstractPDTTTest;
import org.eclipse.php.core.tests.PHPCoreTests;
import org.eclipse.php.core.tests.codeassist.CodeAssistPdttFile;
import org.eclipse.php.core.tests.codeassist.CodeAssistTests;
import org.eclipse.php.internal.core.PHPVersion;
import org.eclipse.php.internal.core.project.PHPNature;

@SuppressWarnings("restriction")
public class ValidationTest extends AbstractPDTTTest {
		
	protected static IProject project;
	protected static IFile testFile;
	protected static final Map<PHPVersion, String[]> TESTS = new LinkedHashMap<PHPVersion, String[]>();	

	public static void setUpSuite() throws Exception {
		project = ResourcesPlugin.getWorkspace().getRoot().getProject(
				"CodeAssistTests");
		if (project.exists()) {
			return;
		}

		project.create(null);
		project.open(null);

		// configure nature
		IProjectDescription desc = project.getDescription();
		desc.setNatureIds(new String[] { PHPNature.ID });
		project.setDescription(desc, null);
	}
	
	public static void tearDownSuite() throws Exception {
		project.close(null);
		project.delete(true, true, null);
		project = null;
	}
	
	public static Test suite() {

		TestSuite suite = new TestSuite("Auto Code Assist Tests");

		for (final PHPVersion phpVersion : TESTS.keySet()) {
			TestSuite phpVerSuite = new TestSuite(phpVersion.getAlias());

			for (String testsDirectory : TESTS.get(phpVersion)) {

				for (final String fileName : getPDTTFiles(testsDirectory)) {
					try {
						final CodeAssistPdttFile pdttFile = new CodeAssistPdttFile(
								fileName);
						phpVerSuite.addTest(new CodeAssistTests(phpVersion
								.getAlias()
								+ " - /" + fileName) {

							protected void setUp() throws Exception {
								PHPCoreTests.setProjectPhpVersion(project,
										phpVersion);
								pdttFile.applyPreferences();
							}

							protected void tearDown() throws Exception {
								if (testFile != null) {
									testFile.delete(true, null);
									testFile = null;
								}
							}

							protected void runTest() throws Throwable {
								CompletionProposal[] proposals = getProposals(pdttFile
										.getFile());
								compareProposals(proposals, pdttFile);
							}
						});
					} catch (final Exception e) {
						phpVerSuite.addTest(new TestCase(fileName) { // dummy
									// test
									// indicating
									// PDTT
									// file
									// parsing
									// failure
									protected void runTest() throws Throwable {
										throw e;
									}
								});
					}
				}
			}
			suite.addTest(phpVerSuite);
		}

		// Create a setup wrapper
		TestSetup setup = new TestSetup(suite) {
			protected void setUp() throws Exception {
				setUpSuite();
			}

			protected void tearDown() throws Exception {
				tearDownSuite();
			}
		};
		return setup;
	}	
	
	

}
