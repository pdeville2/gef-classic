/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.gef.ui.parts;

import java.util.*;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.*;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.widgets.*;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.util.Assert;

import org.eclipse.draw2d.ExclusionSearch;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.LightweightSystem;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.gef.*;
import org.eclipse.gef.editparts.*;
import org.eclipse.gef.editparts.LayerManager;

public class GraphicalViewerImpl
	extends AbstractEditPartViewer
	implements GraphicalViewer
{

final private LightweightSystem lws = createLightweightSystem();
IFigure rootFigure;
private DomainEventDispatcher eventDispatcher;

public GraphicalViewerImpl() {
	createDefaultRoot();
}

public Control createControl(Composite composite){
	setControl(new Canvas(composite, SWT.NO_BACKGROUND));
	return getControl();
}

protected void createDefaultRoot(){
	setRootEditPart(new GraphicalRootEditPart());
}

protected LightweightSystem createLightweightSystem(){
	return new LightweightSystem();
}

public void handleDispose(DisposeEvent e) {
	super.handleDispose(e);
	getLightweightSystem().getUpdateManager().dispose();
}

public Handle findHandleAt(Point p){
	LayerManager layermanager = (LayerManager)getEditPartRegistry().get(LayerManager.ID);
	if (layermanager == null)
		return null;
	List list = new ArrayList(3);
	list.add(layermanager.getLayer(LayerConstants.PRIMARY_LAYER));
	list.add(layermanager.getLayer(LayerConstants.CONNECTION_LAYER));
	list.add(layermanager.getLayer(LayerConstants.FEEDBACK_LAYER));
	IFigure handle = getLightweightSystem().getRootFigure().findFigureAtExcluding(p.x, p.y, list);
	if (handle instanceof Handle)
		return (Handle)handle;
	return null;
}

public EditPart findObjectAtExcluding(Point pt, Collection exclude, final Conditional condition) {
	class ConditionalTreeSearch extends ExclusionSearch {
		ConditionalTreeSearch (Collection coll) {
			super(coll);
		}
		public boolean accept(IFigure figure) {
			EditPart editpart = null;
			while (editpart == null && figure != null) {
				editpart = (EditPart)getVisualPartMap().get(figure);
				figure = figure.getParent();
			}
			return editpart != null
				&& (condition == null || condition.evaluate(editpart));
		}
	};
	IFigure figure = getLightweightSystem()
		.getRootFigure()
		.findFigureAt(pt.x, pt.y, new ConditionalTreeSearch(exclude));
	EditPart part = null;
	while (part == null && figure != null) {
		part = (EditPart)getVisualPartMap().get(figure);
		figure = figure.getParent();
	}
	if (part == null)
		return getContents();
	return part;
}

public void flush(){
	getLightweightSystem().getUpdateManager().performUpdate();
}

/**@deprecated*/
protected DomainEventDispatcher getEventDispatcher(){
	return eventDispatcher;
}

protected LayerManager getLayerManager(){
	return (LayerManager)getEditPartRegistry().get(LayerManager.ID);
}

protected LightweightSystem getLightweightSystem(){
	return lws;
}

/**@deprecated There is no reason to call this method*/
protected IFigure getRootFigure(){
	return rootFigure;
}

protected void hookDropTarget() {
	//Allow the real drop targets to make their changes first.
	super.hookDropTarget();
	
	//Then force and update since async paints won't occurs during a Drag operation
	getDropTarget().addDropListener(new DropTargetAdapter() {
		public void dragEnter(DropTargetEvent event){
			flush();
		}
		public void dragLeave(DropTargetEvent event){
			flush();
		}
		public void dragOver(DropTargetEvent event){
			flush();
		}
	});
}

protected void hookControl(){
	super.hookControl();
	getLightweightSystem().setControl((Canvas)getControl());
}

public void registerAccessibleEditPart(AccessibleEditPart acc) {
	Assert.isNotNull(acc);
	getEventDispatcher().putAccessible(acc);
}

/**
 * @see org.eclipse.gef.EditPartViewer#reveal(EditPart)
 */
public void reveal(EditPart part) {
	if (part == null)
		return;
	EditPart current = part.getParent();
	while (current != null) {
		if (current instanceof IAdaptable) {
			IAdaptable adaptable = (IAdaptable) current;
			ExposeHelper helper = (ExposeHelper)adaptable.getAdapter(ExposeHelper.class);
			if (helper != null)
				helper.exposeDescendant(part);
		}
		current = current.getParent();
	}
	AccessibleEditPart acc = (AccessibleEditPart)part.getAdapter(AccessibleEditPart.class);
	if (acc != null)
		getControl().getAccessible().setFocus(acc.getAccessibleID());
}

/**
 * Extended implementation to flush asynchronous paints in draw2d.
 * @see org.eclipse.gef.EditPartViewer#setContextMenu(org.eclipse.jface.action.MenuManager)
 */
public void setContextMenu(MenuManager contextMenu) {
	super.setContextMenu(contextMenu);
	if (contextMenu != null)
		contextMenu.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				flush();
			}
		});
}

public void setCursor(Cursor newCursor){
	if(getEventDispatcher() != null)
		getEventDispatcher().setOverrideCursor(newCursor);
}

protected void setDragSource(DragSource source) {
	super.setDragSource(source);

	class TheLastListener extends DragSourceAdapter {
		public void dragStart(DragSourceEvent event) {
			// If the EventDispatcher has captured the mouse, don't perform native drag.
			if (getEventDispatcher().isCaptured())
				event.doit = false;
			if (event.doit) {
				//A drag is going to occur, tell the EditDomain
				getEventDispatcher().dispatchNativeDragStarted(event, GraphicalViewerImpl.this);
				flush(); //deferred events are not processed during native Drag-and-Drop.
			}
		}
		public void dragFinished(DragSourceEvent event) {
			getEventDispatcher().dispatchNativeDragFinished(event, GraphicalViewerImpl.this);
	}
	}
	// The DragSource may be set to null if there are no listeners.  If there are listeners,
	// this should be *the* last listener because all other listeners are hooked in super().
	if (source != null)
		getDragSource().addDragListener(new TheLastListener());
}

public void setEditDomain(EditDomain domain){
	super.setEditDomain(domain);
	getLightweightSystem()
		.setEventDispatcher(eventDispatcher = new DomainEventDispatcher(domain, this));
}

public void setRootEditPart(RootEditPart editpart){
	super.setRootEditPart(editpart);
	setRootFigure(((GraphicalEditPart)editpart).getFigure());
}

protected void setRootFigure(IFigure figure){
	rootFigure = figure;
	getLightweightSystem().setContents(rootFigure);
}

public void setRouteEventsToEditDomain(boolean value){
	getEventDispatcher().setRouteEventsToEditor(value);
}

public void unregisterAccessibleEditPart(AccessibleEditPart acc) {
	Assert.isNotNull(acc);
	getEventDispatcher().removeAccessible(acc);
}

}