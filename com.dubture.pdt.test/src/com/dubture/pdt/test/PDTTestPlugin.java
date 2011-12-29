package com.dubture.pdt.test;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.dltk.internal.core.ModelManager;
import org.eclipse.php.internal.core.PHPVersion;
import org.eclipse.php.internal.core.project.PHPNature;
import org.eclipse.php.internal.core.project.ProjectOptions;
import org.osgi.framework.BundleContext;

@SuppressWarnings("restriction")
public class PDTTestPlugin extends Plugin{

	private static PDTTestPlugin plugin;
	
	private static IProject project = null;


	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext bundleContext) throws Exception {
		super.start(bundleContext);				
		plugin = this;		
	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext bundleContext) throws Exception {
		super.stop(bundleContext);
		plugin = null;
	}

	public static PDTTestPlugin getDefault() {		
		return plugin;
	}
	
	public static void waitForIndexer() {
		ModelManager.getModelManager().getIndexManager().waitUntilReady();
	}

	/**
	 * Wait for autobuild notification to occur, that is for the autbuild to
	 * finish.
	 */
	public static void waitForAutoBuild() {
		boolean wasInterrupted = false;
		do {
			try {
				Job.getJobManager().join(ResourcesPlugin.FAMILY_AUTO_BUILD,
						null);
				wasInterrupted = false;
			} catch (OperationCanceledException e) {
				throw (e);
			} catch (InterruptedException e) {
				wasInterrupted = true;
			}
		} while (wasInterrupted);
	}
	
	public static IProject createProject(String name) throws CoreException {
		
		if (project != null && project.getName().equals(name)) {			
			return project;
		}
		
		project = getProject(name);
		IWorkspaceRunnable create = new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException {
				project.create(null);
				project.open(null);
				
				IProjectDescription desc = project.getDescription();
				desc.setNatureIds(new String[] { PHPNature.ID });
				project.setDescription(desc, null);

				ProjectOptions.setPhpVersion(PHPVersion.PHP5_3, project);
				
			}
		};
		
		getWorkspace().run(create, null);
		return project;
		
	}
	
	/**
	 * Returns the IWorkspace this test suite is running on.
	 */
	public static IWorkspace getWorkspace() {
		return ResourcesPlugin.getWorkspace();
	}

	public static IWorkspaceRoot getWorkspaceRoot() {
		return getWorkspace().getRoot();
	}

	public static IProject getProject(String project) {
		return getWorkspaceRoot().getProject(project);
	}
	
	
}
