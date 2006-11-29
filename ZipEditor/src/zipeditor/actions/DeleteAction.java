/*
 * (c) Copyright 2002, 2005 Uwe Voigt
 * All Rights Reserved.
 */
package zipeditor.actions;

import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

import zipeditor.ZipEditor;
import zipeditor.model.ZipNode;

public class DeleteAction extends EditorAction {
	public DeleteAction(ZipEditor editor) {
		super(ActionMessages.getString("DeleteAction.0"), editor); //$NON-NLS-1$
		setToolTipText(ActionMessages.getString("DeleteAction.1")); //$NON-NLS-1$
		setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(
				ISharedImages.IMG_TOOL_DELETE));
	}

	public void run() {
		ZipNode[] nodes = fEditor.getSelectedNodes();
		for (int i = 0; i < nodes.length; i++) {
			nodes[i].getParent().remove(nodes[i]);
		}
		if (nodes.length > 0) {
			fEditor.getViewer().refresh();
			fEditor.setDirty(true);
		}
	}
}
