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
package com.dubture.pdt.internal.ui.wizards;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.dltk.core.IScriptFolder;
import org.eclipse.dltk.core.ISourceModule;
import org.eclipse.dltk.core.IType;
import org.eclipse.dltk.core.search.IDLTKSearchConstants;
import org.eclipse.dltk.internal.core.SourceType;
import org.eclipse.dltk.internal.ui.dialogs.OpenTypeSelectionDialog2;
import org.eclipse.dltk.internal.ui.wizards.dialogfields.DialogField;
import org.eclipse.dltk.internal.ui.wizards.dialogfields.IDialogFieldListener;
import org.eclipse.dltk.internal.ui.wizards.dialogfields.IStringButtonAdapter;
import org.eclipse.dltk.internal.ui.wizards.dialogfields.ITreeListAdapter;
import org.eclipse.dltk.internal.ui.wizards.dialogfields.SelectionButtonDialogFieldGroup;
import org.eclipse.dltk.internal.ui.wizards.dialogfields.StringButtonDialogField;
import org.eclipse.dltk.internal.ui.wizards.dialogfields.StringDialogField;
import org.eclipse.dltk.internal.ui.wizards.dialogfields.TreeListDialogField;
import org.eclipse.dltk.ui.DLTKUIPlugin;
import org.eclipse.dltk.ui.dialogs.StatusInfo;
import org.eclipse.dltk.ui.wizards.NewSourceModulePage;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.php.internal.core.project.PHPNature;
import org.eclipse.php.internal.ui.PHPUILanguageToolkit;
import org.eclipse.php.internal.ui.PHPUIMessages;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.PlatformUI;
import com.dubture.pdt.formatter.core.ast.Formatter;
import com.dubture.pdt.internal.ui.codemanipulation.ClassStub;
import com.dubture.pdt.internal.ui.codemanipulation.ClassStubParameter;
import com.dubture.pdt.ui.PDTPluginImages;

@SuppressWarnings("restriction")
public class NewClassWizardPage extends NewSourceModulePage {

	private static final int IS_ABSTRACT_INDEX = 0;
	private static final int IS_FINAL_INDEX = 1;

	private static final int INTERFACES = 1;
	private static final int CLASSES = 2;

	protected static final String NAME = "ClassName";
	private static final String FILENAME = "NewClassWizardPage.filename";

	private Composite container;
	private int nColumns;
	protected ISelection selection;
	private StatusInfo status;

	protected String initialClassName = null;
	protected String initialNamespace = null;
	protected String initialFilename = null;
	protected IScriptFolder initialFolder = null;


	// Controls field in dialog.
	private StringDialogField classNameField;
	private StringDialogField fileNameField;
	private StringButtonDialogField superClassField;
	private IType superClass;
	private StringDialogField namespaceField;
	private SelectionButtonDialogFieldGroup classModifierField;
	private TreeListDialogField interfaceDialog;
	// TODO: Check creation process.
	private Button commentCheckbox;
	private Button superClassConstructors;
	private Button abstractMethods;

	public NewClassWizardPage(final ISelection selection, String initialFileName) {
		super();
		setImageDescriptor(PDTPluginImages.DESC_WIZBAN_NEW_PHPCLASS);
		this.selection = selection;
		this.initialFilename = initialFileName;

		status = new StatusInfo();
	}

	public NewClassWizardPage(final ISelection selection, String initialFileName, String namespace, String className,
			IScriptFolder scriptFolder) {
		this(selection, initialFileName);

		this.initialNamespace = namespace;
		this.initialClassName = className;
		this.initialFolder = scriptFolder;
	}

	private OpenTypeSelectionDialog2 getDialog(int type, String title, String message, boolean multi) {
		boolean getClasses;
		boolean getInterfaces;

		getClasses = type == NewClassWizardPage.CLASSES ? true : false;
		getInterfaces = type == NewClassWizardPage.INTERFACES ? true : false;

		OpenTypeSelectionDialog2 dialog = new OpenTypeSelectionDialog2(DLTKUIPlugin.getActiveWorkbenchShell(), multi,
				PlatformUI.getWorkbench().getProgressService(), null, IDLTKSearchConstants.TYPE,
				new PHPTypeSelectionExtension(getClasses, getInterfaces), PHPUILanguageToolkit.getInstance());

		dialog.setTitle(title);
		dialog.setMessage(message);

		return dialog;
	}

	@Override
	protected void createContentControls(Composite composite, int nColumns) {

		container = composite;
		this.nColumns = nColumns;

		createContainerControls(composite, nColumns);

		createClassControls();

		if (initialFolder != null) {
			setScriptFolder(initialFolder, true);
		}

	}

	private void createClassControls() {

		createNameControls();

		createClassModifierControls();

		createFileNameControls();

		createNamespaceControls();

		createSuperClassControls();

		createSeparator();

		createInterfaceArea();

		createAdditionalPart();

	}

	private void createAdditionalPart() {
		GridData gd;

		gd = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		gd.horizontalSpan = nColumns;
		gd.heightHint = 20;

		Label methodLabel = new Label(container, SWT.NONE);
		methodLabel.setText("Which method stubs would you like to create?");
		methodLabel.setLayoutData(gd);

		Label methodDummy = new Label(container, SWT.NONE);
		methodDummy.setText("");

		gd = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		gd.horizontalSpan = nColumns - 1;

		superClassConstructors = new Button(container, SWT.CHECK);
		superClassConstructors.setText("Su&perclass constructor");
		superClassConstructors.setLayoutData(gd);
		superClassConstructors.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				superClassConstructors.getSelection();
			}
		});

		Label methodDummy2 = new Label(container, SWT.NONE);
		methodDummy2.setText("");

		abstractMethods = new Button(container, SWT.CHECK);
		abstractMethods.setText("In&herited abstract methods");
		abstractMethods.setSelection(true);
		abstractMethods.setLayoutData(gd);
		abstractMethods.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {

				abstractMethods.getSelection();
			}
		});

		gd = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		gd.horizontalSpan = nColumns;
		gd.heightHint = 20;
		Label commentLabel = new Label(container, SWT.NONE);
		commentLabel.setText("Do you want to add comments?");
		commentLabel.setLayoutData(gd);

		Label dummy = new Label(container, SWT.NONE);
		dummy.setText("");

		gd = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		gd.horizontalSpan = nColumns - 1;

		commentCheckbox = new Button(container, SWT.CHECK);
		commentCheckbox.setText("&Generate element comments");
		commentCheckbox.setLayoutData(gd);
		commentCheckbox.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				commentCheckbox.getSelection();
			}
		});

		// dialogChanged();
		// setControl(container);
		// Dialog.applyDialogFont(container);
	}

	private void createInterfaceArea() {

		String[] buttons = { "&Add...", "&Remove" };
		interfaceDialog = new TreeListDialogField(new ITreeListAdapter() {

			@Override
			public void selectionChanged(TreeListDialogField field) {
				// TODO Auto-generated method stub
			}

			@Override
			public void keyPressed(TreeListDialogField field, KeyEvent event) {
				// TODO Auto-generated method stub

			}

			@Override
			public boolean hasChildren(TreeListDialogField field, Object element) {
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			public Object getParent(TreeListDialogField field, Object element) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public Object[] getChildren(TreeListDialogField field, Object element) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public void doubleClicked(TreeListDialogField field) {
				// TODO Auto-generated method stub

			}

			@Override
			public void customButtonPressed(TreeListDialogField field, int index) {
				if (index == 0) {
					OpenTypeSelectionDialog2 dialog = getDialog(NewClassWizardPage.INTERFACES, "Interface selection",
							"Select interface", true);
					int result = dialog.open();
					if (result != IDialogConstants.OK_ID)
						return;
					IType interfaceObject;
					Object[] types = dialog.getResult();
					if (types != null && types.length > 0) {
						for (int i = 0; i < types.length; i++) {
							interfaceObject = (IType) types[i];
							interfaceDialog.addElement(interfaceObject);
						}
					}
				}

			}
		}, buttons , new ILabelProvider() {

			@Override
			public void removeListener(ILabelProviderListener listener) {
				// TODO Auto-generated method stub

			}

			@Override
			public boolean isLabelProperty(Object element, String property) {
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			public void dispose() {
				// TODO Auto-generated method stub

			}

			@Override
			public void addListener(ILabelProviderListener listener) {
				// TODO Auto-generated method stub
				@SuppressWarnings("unused")
				int xst = 4;
			}

			@Override
			public String getText(Object element) {
				IType test = (IType) element;
				return test.getFullyQualifiedName();
			}

			@Override
			public Image getImage(Object element) {
				// TODO Auto-generated method stub
				return null;
			}
		});

		interfaceDialog.setRemoveButtonIndex(1);
		
		interfaceDialog.doFillIntoGrid(container, nColumns);
	}

	private void createSeparator() {
		GridData gd;
		gd = new GridData(GridData.FILL_HORIZONTAL, SWT.CENTER, true, true, 3, 3);
		gd.heightHint = 20;

		Label separator = new Label(container, SWT.SEPARATOR | SWT.HORIZONTAL);
		separator.setLayoutData(gd);
	}

	private void createClassModifierControls() {
		String[] buttonsName = { "abs&tract", "fina&l" };
		classModifierField = new SelectionButtonDialogFieldGroup(SWT.CHECK, buttonsName, nColumns);
		classModifierField.setLabelText("Modifiers:");
		classModifierField.doFillIntoGrid(container, nColumns);

		classModifierField.setDialogFieldListener(new IDialogFieldListener() {

			@Override
			public void dialogFieldChanged(DialogField field) {

				handleFieldChanged("");
			}
		});
	}

	protected StatusInfo classModifiedChanged() {
		StatusInfo status = new StatusInfo();
		if (classModifierField.getSelectionButton(IS_ABSTRACT_INDEX).getSelection()
				&& classModifierField.getSelectionButton(IS_FINAL_INDEX).getSelection()) {
			status.setError("A class cannot be abstract and final at the same time");
		}
		return status;
	}

	private void createFileNameControls() {

		fileNameField = new StringDialogField();
		fileNameField.setLabelText("Fil&ename:");
		fileNameField.doFillIntoGrid(container, nColumns - 1);
		DialogField.createEmptySpace(container);

		fileNameField.setDialogFieldListener(new IDialogFieldListener() {

			@Override
			public void dialogFieldChanged(DialogField field) {
				status = filenameChanged();
				handleFieldChanged(FILENAME);
			}
		});
	}

	private StatusInfo filenameChanged() {
		StatusInfo status = new StatusInfo();
		if (!fileNameField.equals("") && getScriptFolder().getSourceModule(fileNameField.getText()).exists()) { //$NON-NLS-1$
			status.setError("The specified class already exists");
			return status;
		}

		int dotIndex = fileNameField.getText().lastIndexOf('.');
		if ((fileNameField.getText().length() == 0 || dotIndex == 0) && fileNameField.getText().length() > 0) {
			status.setError(PHPUIMessages.PHPFileCreationWizardPage_15);
			return status;
		}

		if (dotIndex != -1) {
			String fileNameWithoutExtention = fileNameField.getText().substring(0, dotIndex);
			for (int i = 0; i < fileNameWithoutExtention.length(); i++) {
				char ch = fileNameWithoutExtention.charAt(i);
				if (!(Character.isJavaIdentifierPart(ch) || ch == '.' || ch == '-')) {
					status.setError(PHPUIMessages.PHPFileCreationWizardPage_16); //$NON-NLS-1$
					return status;
				}
			}
		}
		return status;
	}

	private void createNamespaceControls() {
		namespaceField = new StringDialogField();
		namespaceField.setLabelText("Na&mespace:");
		namespaceField.doFillIntoGrid(container, nColumns - 1);
		DialogField.createEmptySpace(container);

		// TODO: Autocomplete namespaces?
		// List<INamespaceResolver> resolvers =
		// ExtensionManager.getDefault().getNamespaceResolvers();
		//
		// IScriptFolder folder = getScriptFolder();
		//
		// for (INamespaceResolver resolver : resolvers) {
		// String ns = resolver.resolve(folder);
		// if (ns != null && ns.length() > 0) {
		// namespaceField.setText(ns);
		// break;
		// }
		// }
	}

	/**
	 * TODO: Create autocomplete on field.
	 */
	private void createSuperClassControls() {

		superClassField = new StringButtonDialogField(new IStringButtonAdapter() {


			@Override
			public void changeControlPressed(DialogField field) {

				OpenTypeSelectionDialog2 dialog = getDialog(NewClassWizardPage.CLASSES, "Superclass selection",
						"Select superclass", false);
				int result = dialog.open();
				if (result != IDialogConstants.OK_ID)
					return;

				Object searchedObject[] = dialog.getResult();
				superClass = (SourceType) searchedObject[0];
				((StringDialogField) field).setText(superClass.getFullyQualifiedName());
				
			}
		});

		superClassField.setLabelText("&Superclass:");
		superClassField.setButtonLabel("Bro&wse...");
		superClassField.doFillIntoGrid(container, nColumns);
	}

	private void createNameControls() {

		classNameField = new StringDialogField();
		classNameField.setLabelText("&Name:");
		classNameField.doFillIntoGrid(container, nColumns - 1);
		DialogField.createEmptySpace(container);

		classNameField.setDialogFieldListener(new IDialogFieldListener() {

			@Override
			public void dialogFieldChanged(DialogField field) {

				status = nameChanged();
				handleFieldChanged(NAME);
			}
		});
	}

	protected StatusInfo nameChanged() {
		StatusInfo status = new StatusInfo();
		if (classNameField.getText().length() == 0) {
			status.setError("Enter name of class.");
			fileNameField.setTextWithoutUpdate("");
		} else {
			fileNameField.setText(classNameField.getText() + ".php");
		}

		if (classNameField.getText().length() > 0 && Character.isLowerCase(classNameField.getText().charAt(0))) {
			status.setWarning("Classes starting with lowercase letters are discouraged");
		}

		return status;
	}

	@Override
	protected String getPageTitle() {

		return "PHP Class";
	}

	@Override
	protected String getPageDescription() {

		return "Create a new PHP Class";
	}

	@Override
	protected String getRequiredNature() {

		return PHPNature.ID;

	}

	@Override
	protected String getFileContent(ISourceModule module) throws CoreException {
		ClassStubParameter classStubParameter = new ClassStubParameter();

		classStubParameter.setName(classNameField.getText());
		classStubParameter.setAbstractClass(classModifierField.isSelected(IS_ABSTRACT_INDEX));
		classStubParameter.setFinalClass(classModifierField.isSelected(IS_FINAL_INDEX));
		classStubParameter.setNamespace(namespaceField.getText());
		classStubParameter.setInterfaces(interfaceDialog.getElements());
		classStubParameter.setSuperclass(superClass);

		ClassStub classStub = new ClassStub(classStubParameter);
		String content = classStub.toString();

		IDocument doc = Formatter.createPHPDocument();
		Formatter formatter = new Formatter();
		doc.set(content);
		formatter.format(doc);

		return doc.get();
	}

	@Override
	protected void handleFieldChanged(String fieldName) {

		status.isOK();

		super.handleFieldChanged(fieldName);

		final List<IStatus> statuses = new ArrayList<IStatus>();
		if (containerStatus != null) {
			statuses.add(containerStatus);
		}

		// TODO: Check why few field aren't initialized here (during construct
		// object?)
		if (fieldName != CONTAINER) {
			statuses.add(classModifiedChanged());
		}

		if (fieldName != CONTAINER && fieldName != FILENAME) {
			statuses.add(nameChanged());
		}
		statuses.add(status);

		updateStatus(statuses.toArray(new IStatus[statuses.size()]));
	}

	@Override
	protected void setFocus() {
		classNameField.setFocus();
	}
}
