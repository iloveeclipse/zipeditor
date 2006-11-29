/*
 * (c) Copyright 2002, 2005 Uwe Voigt
 * All Rights Reserved.
 */
package zipeditor.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.ActionGroup;
import org.eclipse.ui.dialogs.PropertyDialogAction;

import zipeditor.Utils;
import zipeditor.ZipEditor;

public class ZipActionGroup extends ActionGroup {
	private IAction fAddAction;
	private IAction fDeleteAction;
	private IAction fExtractAction;
	private IAction fSortAction;
	private IAction fSaveAction;
	private IAction fRevertAction;
	private IAction fPreferencesAction;
	private IAction fPropertiesAction;
	private ZipEditor fEditor;
	
	public ZipActionGroup(ZipEditor editor) {
		fAddAction = new AddAction(editor);
		fDeleteAction = new DeleteAction(editor);
		fExtractAction = new ExtractAction(editor);
		fSortAction = new SortAction(editor);
		fRevertAction = new RevertAction(editor);
		fSaveAction = new SaveAction(editor);
		fPreferencesAction = new PreferencesAction(editor);

		fEditor = editor;
	}
	
	public void fillContextMenu(IMenuManager menu) {
		menu.add(new Separator());
		menu.add(fSaveAction);
		menu.add(fRevertAction);
		menu.add(new Separator());
		menu.add(fAddAction);
		menu.add(fExtractAction);
		menu.add(new Separator());
		menu.add(fDeleteAction);
		menu.add(new Separator());
		if (fPropertiesAction == null)
			fPropertiesAction = new PropertyDialogAction(fEditor.getSite(), fEditor.getViewer());
		menu.add(fPropertiesAction);
		
		updateActionBars();
	}
	
    public void fillToolBarManager(IToolBarManager manager, int mode) {
		manager.add(new Separator());
		manager.add(fAddAction);
		manager.add(fExtractAction);
		manager.add(new Separator());
		manager.add(fDeleteAction);
		manager.add(new Separator());
		manager.add(fSortAction);
		if (mode == ToggleViewModeAction.MODE_FOLDER) {
			manager.add(new Separator());
			manager.add(fPreferencesAction);
		}
	}
    
    public void fillActionBars(IActionBars actionBars) {
		actionBars.setGlobalActionHandler(ActionFactory.DELETE.getId(), fDeleteAction);
		actionBars.setGlobalActionHandler(ActionFactory.SAVE.getId(), fSaveAction);
		actionBars.setGlobalActionHandler(ActionFactory.REVERT.getId(), fRevertAction);
		actionBars.setGlobalActionHandler(ActionFactory.PROPERTIES.getId(), fPropertiesAction);
    }
	
	public void updateActionBars() {
		IStructuredSelection selection = (IStructuredSelection) getContext().getSelection();
		boolean empty = selection.isEmpty();
		boolean onlyFilesSelected = Utils.allNodesAreFileNodes(selection);
		boolean singleSelection = selection.size() == 1;
		
		fDeleteAction.setEnabled(!empty);
		fSaveAction.setEnabled(fEditor.isDirty());
		fRevertAction.setEnabled(fEditor.isDirty());
		if (fPropertiesAction != null)
			fPropertiesAction.setEnabled(onlyFilesSelected && singleSelection);
	}
}
