package com.dubture.pdt.core.refactoring;

import org.eclipse.core.resources.IFile;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.dltk.core.DLTKCore;
import org.eclipse.dltk.core.IModelElement;
import org.eclipse.dltk.core.IModelElementVisitor;
import org.eclipse.dltk.core.ModelException;
import org.eclipse.dltk.internal.core.SourceModule;
import org.eclipse.dltk.internal.core.SourceType;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;
import org.eclipse.ltk.core.refactoring.participants.MoveParticipant;
import org.eclipse.php.core.compiler.PHPFlags;

@SuppressWarnings("restriction")
public class ClassMoveParticipant extends MoveParticipant
{
    private IFile file;

    /* (non-Javadoc)
     * @see org.eclipse.ltk.core.refactoring.participants.RefactoringParticipant#initialize(java.lang.Object)
     */
    @Override
    protected boolean initialize(Object element)
    {
        System.err.println("moved " + element.getClass());
        if (element instanceof IFile) {
            file = (IFile) element;
            return true;
        }
        
        return false;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ltk.core.refactoring.participants.RefactoringParticipant#getName()
     */
    @Override
    public String getName()
    {
        System.err.println("get name");
        return "ClassMove participant";
    }

    /* (non-Javadoc)
     * @see org.eclipse.ltk.core.refactoring.participants.RefactoringParticipant#checkConditions(org.eclipse.core.runtime.IProgressMonitor, org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext)
     */
    @Override
    public RefactoringStatus checkConditions(IProgressMonitor pm,
            CheckConditionsContext context) throws OperationCanceledException
    {
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ltk.core.refactoring.participants.RefactoringParticipant#createChange(org.eclipse.core.runtime.IProgressMonitor)
     */
    @Override
    public Change createChange(IProgressMonitor pm) throws CoreException,
            OperationCanceledException
    {
        SourceModule module = (SourceModule) DLTKCore.create(file);

        module.accept(new IModelElementVisitor()
        {
            
            @Override
            public boolean visit(IModelElement element)
            {
                if (element.getElementType() == IModelElement.TYPE) {
                    
                    SourceType type = (SourceType) element;
                    
                    try {
                        
                        if (PHPFlags.isNamespace(type.getFlags())) {
                            
                        }
                        
                    } catch (ModelException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    
                    
                }
                return true;
            }
        });
        
        
//        RenameElementsOperation rename = new RenameElementsOperation(null, null, null, true);
        System.err.println("create change");
        return null;
    }

}
