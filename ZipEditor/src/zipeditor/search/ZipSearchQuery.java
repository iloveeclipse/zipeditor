/*
 * (c) Copyright 2002, 2016 Uwe Voigt
 * All Rights Reserved.
 */
package zipeditor.search;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.content.IContentDescription;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.search.ui.ISearchQuery;
import org.eclipse.search.ui.ISearchResult;

import zipeditor.ZipEditorPlugin;
import zipeditor.model.Node;
import zipeditor.model.ZipContentDescriber;
import zipeditor.model.ZipModel;

public class ZipSearchQuery implements ISearchQuery {

	private ZipSearchOptions fOptions;
	private ZipSearchResult fResult;
	private List fElements;

	public ZipSearchQuery(ZipSearchOptions options, List elements) {
		fOptions = options;
		fElements = elements;
    }

	public boolean canRerun() {
		return true;
	}

	public boolean canRunInBackground() {
		return true;
	}

	public String getLabel() {
		return SearchMessages.getString("ZipSearchQuery.0"); //$NON-NLS-1$
	}

	public ISearchResult getSearchResult() {
		if (fResult == null)
			fResult = new ZipSearchResult(this);
		return fResult;
	}

	public ZipSearchOptions getOptions() {
		return fOptions;
	}

	private List expandToFiles(File file, List files, List fileNames, List fileExtensions, IProgressMonitor monitor) throws OperationCanceledException {
		if (monitor.isCanceled())
			throw new OperationCanceledException();

		if (file.isDirectory()) {
			monitor.subTask(file.getAbsolutePath());
			File[] children = file.listFiles();
			if (children != null) {
				for (int i = 0; i < children.length; i++) {
					expandToFiles(children[i], files, fileNames, fileExtensions, monitor);
				}
			}
		} else if (file.isFile() && ZipContentDescriber.matchesFileSpec(file.getName(), fileNames, fileExtensions)) {
			files.add(file);
		}
		return files;
	}

	private ZipModel[] getModelsFromElements(IProgressMonitor monitor) throws OperationCanceledException {
		final IContentType archiveContentType = ZipContentDescriber.getArchiveContentType();
		List fileNames = ZipContentDescriber.getFileNamesAssociatedWithArchives();
		List fileExtensions = ZipContentDescriber.getFileExtensionsAssociatedWithArchives();

		final List models = new ArrayList();
		monitor.setTaskName(SearchMessages.getString("ZipSearchQuery.1")); //$NON-NLS-1$

		for (Iterator it = fElements.iterator(); it.hasNext(); ) {
			try {
				Object element = it.next();
				if (element instanceof IFile) {
					IFile file = (IFile) element;
					addFile(archiveContentType, models, file);
				} else if (element instanceof File) {
					List files = expandToFiles((File) element, new ArrayList(), fileNames, fileExtensions, monitor);
					monitor.subTask(""); //$NON-NLS-1$
					for (int i = 0; i < files.size(); i++) {
						File file = (File) files.get(i);
						try {
							ZipModel model = new ZipModel(file, new FileInputStream(file));
							models.add(model);
						} catch (FileNotFoundException e) {
							ZipEditorPlugin.log(e);
						}
					}
				} else if (element instanceof IContainer) {
					IContainer container = (IContainer) element;
					container.accept(new IResourceVisitor() {
						public boolean visit(IResource resource) throws CoreException {
							if (resource instanceof IFile) {
								addFile(archiveContentType, models, (IFile) resource);
							}
							return true;
						}
					}, IResource.DEPTH_INFINITE, false);
				} else if (element instanceof Node) {
					Node node = (Node) element;
					if (node.getModel().getZipPath() != null)
						models.add(node.getModel());
				}
			} catch (CoreException e) {
				ZipEditorPlugin.log(e);
			}
		}
		monitor.done();
		return (ZipModel[]) models.toArray(new ZipModel[models.size()]);
	}

	private void addFile(IContentType archiveContentType, List models, IFile file) throws CoreException {
		IContentDescription description = file.getContentDescription();
		IContentType contentType = description != null ? description.getContentType() : null;
		IContentType baseType = contentType != null ? contentType.getBaseType() : null;
		if (baseType != null && baseType.equals(archiveContentType)) {
			ZipModel model = new ZipModel(file.getLocation().toFile(), file.getContents());
			models.add(model);
		}
	}

	public IStatus run(IProgressMonitor monitor) throws OperationCanceledException {
		ZipSearchResult result = (ZipSearchResult) getSearchResult();
		result.removeAll();
		ZipModel[] models = getModelsFromElements(monitor);
		ZipSearchResultCollector collector = new ZipSearchResultCollector(result);
		collector.setProgressMonitor(monitor);
		ZipSearchEngine engine = new ZipSearchEngine();
		return engine.search(fOptions, models, collector);
	}
}