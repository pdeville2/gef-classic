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
package org.eclipse.gef.editparts;

import java.util.*;

import org.eclipse.swt.accessibility.ACC;
import org.eclipse.swt.accessibility.AccessibleControlEvent;

import org.eclipse.core.runtime.IAdaptable;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.LayoutManager;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;

import org.eclipse.gef.*;

/**
 * Default implementation for {@link org.eclipse.gef.GraphicalEditPart}.
 * <P>
 * This is an implementation class, and the documentation here is targeted at subclassing
 * this class. Callers of public API should refer to the interface's documentation.
 * <Table>
 * 	 <tr>
 * 	   <TD><img src="../doc-files/green.gif"/>
 * 	   <TD>Indicates methods that subclasses <em>should</em> override.
 *   </tr>
 *   <tr>
 *     <TD><img src="../doc-files/blue.gif"/>
 *     <TD>These methods might be overridden.
 * 	 </tr>
 * 	 <tr>
 * 	   <TD><img src="../doc-files/black.gif"/>
 * 	   <TD>Should rarely be overridden.
 * 	 </tr>
 *   <tr>
 * 	   <TD><img src="../doc-files/dblack.gif"/>
 * 	   <TD>Essentially "internal" and should never be overridden.
 *   </tr>
 * </table>
 */
public abstract class AbstractGraphicalEditPart
	extends AbstractEditPart
	implements GraphicalEditPart
{

/**
 * The Figure
 */
protected IFigure figure;

/**
 * List of <i>source</i> ConnectionEditParts
 */
protected List sourceConnections;

/**
 * List of <i>source</i> ConnectionEditParts
 */
protected List targetConnections;

/**
 * A default implementation of {@link AccessibleEditPart}. Subclasses can extend this
 * implementation to get base accessibility for free.
 * @since 2.0 */
protected abstract class AccessibleGraphicalEditPart
	extends AccessibleEditPart
{
	/**	 * @see AccessibleEditPart#getChildCount(AccessibleControlEvent)	 */
	public void getChildCount(AccessibleControlEvent e) {
		e.detail  = AbstractGraphicalEditPart.this.getChildren().size();
	}
	
	/**	 * @see AccessibleEditPart#getChildren(AccessibleControlEvent)	 */
	public void getChildren(AccessibleControlEvent e) {
		List list = AbstractGraphicalEditPart.this.getChildren();
		Object children[] = new Object[list.size()];
		for (int i = 0; i < list.size(); i++) {
			EditPart part = (EditPart)list.get(i);
			AccessibleEditPart access =
				(AccessibleEditPart) part.getAdapter(AccessibleEditPart.class);
			if (access == null)
				return; //fail if any children aren't accessible.
			children[i] = new Integer(access.getAccessibleID());
		}
		e.children = children;
	}
	
	/**	 * @see AccessibleEditPart#getLocation(AccessibleControlEvent)	 */
	public void getLocation(AccessibleControlEvent e) {
		Rectangle bounds = getFigure().getBounds().getCopy();
		getFigure().translateToAbsolute(bounds);
		org.eclipse.swt.graphics.Point p = new org.eclipse.swt.graphics.Point(0, 0);
		p = getViewer().getControl().toDisplay(p);
		e.x = bounds.x + p.x;
		e.y = bounds.y + p.y;
		e.width = bounds.width;
		e.height = bounds.height;
	}

	/**	 * @see AccessibleEditPart#getState(AccessibleControlEvent)	 */
	public void getState(AccessibleControlEvent e) {
		if (getSelected() != EditPart.SELECTED_NONE)
			e.detail = ACC.STATE_SELECTED;
		else
			e.detail = ACC.STATE_NORMAL;
	}
}

/**
 * The default implementation of {@link AccessibleAnchorProvider} returned in {@link
 * #getAdapter(Class)}. This implementation creates an accessible location located along
 * the right edge of the EditPart's Figure.
 * @since 2.0 */
protected class DefaultAccessibleAnchorProvider
	implements AccessibleAnchorProvider
{
	private List getDefaultLocations() {
		List list = new ArrayList();
		Rectangle r = getFigure().getBounds();
		Point p = r.getTopRight().translate(-1, r.height / 3);
		getFigure().translateToAbsolute(p);
		list.add(p);
		return list;
	}
	/**	 * @see AccessibleAnchorProvider#getSourceAnchorLocations()	 */
	public List getSourceAnchorLocations() {
		return getDefaultLocations();
	}
	/**	 * @see AccessibleAnchorProvider#getTargetAnchorLocations()	 */
	public List getTargetAnchorLocations() {
		return getDefaultLocations();
	}
}

static class MergedAccessibleHandles
	implements AccessibleHandleProvider
{
	List locations = new ArrayList();
	MergedAccessibleHandles(EditPolicyIterator iter) {
		while (iter.hasNext()) {
			EditPolicy policy = iter.next();
			if (!(policy instanceof IAdaptable))
				continue;
			IAdaptable adaptable = (IAdaptable) policy;
			AccessibleHandleProvider adapter =
				(AccessibleHandleProvider) adaptable.getAdapter(
					AccessibleHandleProvider.class);
			if (adapter != null)
				locations.addAll(adapter.getAccessibleHandleLocations());
		}
	}
	public List getAccessibleHandleLocations() {
		return locations;
	}
}

/**
 * Extends {@link AbstractEditPart#activate() to also activate all <i>source</i>
 * ConnectionEditParts.
 * @see org.eclipse.gef.EditPart#activate() */
public void activate() {
	super.activate();
	List l = getSourceConnections();
	for (int i = 0; i < l.size(); i++)
		((EditPart)l.get(i)).activate();
}

/**
 * Adds the child's Figure to the {@link #getContentPane() contentPane}.
 * @see org.eclipse.gef.editparts.AbstractEditPart#addChildVisual(EditPart, int) */
protected void addChildVisual(EditPart childEditPart, int index) {
	IFigure child = ((GraphicalEditPart)childEditPart).getFigure();
	getContentPane().add(child, index);
}

/**
 * @see org.eclipse.gef.GraphicalEditPart#addNodeListener(org.eclipse.gef.NodeListener)
 */
public void addNodeListener(NodeListener listener) {
	eventListeners.addListener(NodeListener.class, listener);
}

/**
 * <img src="../doc-files/black.gif"/>
 * Adds a <i>source</i> ConnectionEditPart at the specified index. This method is called
 * from {@link #refreshSourceConnections()}. There should be no reason to call or override
 * this method. Source connection are created as a result of overriding {@link
 * #getModelSourceConnections()}.
 * <P>
 * {@link #primAddSourceConnection(ConnectionEditPart, int)} is called to perform the
 * actual update of the {@link #sourceConnections} <code>List</code>. The connection will
 * have its source set to <code>this</code>.
 * <P>
 * If active, this EditPart will activate the ConnectionEditPart.
 * <P>
 * Finally, all {@link NodeListener}s are notified of the new connection.
 * @param connection  Connection being added
 * @param index Index where it is being added
 */
protected void addSourceConnection(ConnectionEditPart connection, int index) {
	primAddSourceConnection(connection, index);
	connection.setSource(this);
	if (isActive())
		connection.activate();
	fireSourceConnectionAdded(connection, index);
}

/**
 * <img src="../doc-files/black.gif"/>
 * Adds a <i>target</i> ConnectionEditPart at the specified index. This method is called
 * from {@link #refreshTargetConnections()}. There should be no reason to call or override
 * this method. Target connection are created as a result of overriding {@link
 * #getModelTargetConnections()}.
 * <P>
 * {@link #primAddTargetConnection(ConnectionEditPart, int)} is called to perform the
 * actual update of the {@link #targetConnections} <code>List</code>. The connection will
 * have its target set to <code>this</code>.
 * <P> Finally, all {@link NodeListener}s are notified of the new connection.
 * @param connection  Connection being added
 * @param index Index where it is being added
 */
protected void addTargetConnection(ConnectionEditPart connection, int index) {
	primAddTargetConnection(connection, index);
	connection.setTarget(this);
	fireTargetConnectionAdded(connection, index);
}

/**
 * <img src="../doc-files/black.gif"/> Creates a {@link ConnectionEditPart} for the given
 * model. Similar to {@link AbstractEditPart#createChild(Object)}. This method is called
 * indirectly during {@link #refreshSourceConnections()}, and {@link
 * #refreshTargetConnections()}.
 * <P>
 * The default implementation goes to the EditPartViewer's {@link EditPartFactory} to
 * create the connection. This method should not be overridden unless factories are not
 * being used.
 * @param model the connection model object
 * @return the new ConnectionEditPart
 */
protected ConnectionEditPart createConnection(Object model) {
	return (ConnectionEditPart)getViewer()
		.getEditPartFactory()
		.createEditPart(this, model);
}

/**
 * Creates the <code>Figure</code> to be used as this part's <i>visuals</i>. This is
 * called from {@link #getFigure()} if the figure has not been created.
 * @return a Figure
 */
protected abstract IFigure createFigure();

/**
 * <img src="../doc-files/black.gif"/> Searches for an existing
 * <code>ConnectionEditPart</code> in the Viewer's {@link
 * EditPartViewer#getEditPartRegistry() EditPart registry} and returns it if one is found.
 * Otherwise, {@link #createConnection(Object)} is called to create a new
 * ConnectionEditPart.  Override this method only if you need to find an existing
 * connection some other way.
 * @param model the Connection's model
 * @return the ConnectionEditPart
 */
protected ConnectionEditPart createOrFindConnection(Object model) {
	ConnectionEditPart conx = (ConnectionEditPart)getViewer()
		.getEditPartRegistry().get(model);
	if (conx != null)
		return conx;
	return createConnection(model);
}

/**
 * <img src="../doc-files/green.gif"/> Extends {@link AbstractEditPart#deactivate()} to
 * also deactivate the source ConnectionEditParts. Subclasses should <em>extend</em> this
 * method to remove any listeners added in {@link #activate}.
 * @see org.eclipse.gef.EditPart#deactivate() */
public void deactivate() {
	List l = getSourceConnections();
	for (int i = 0; i < l.size(); i++)
		((EditPart)l.get(i)).deactivate();

	super.deactivate();
}

/**
 * <img src="../doc-files/dblack.gif"/> Notifies listeners that a source connection has
 * been removed. Called from {@link #removeSourceConnection(ConnectionEditPart)}. There is
 * no reason for subclasses to call or override this method.
 * @param connection  <code>ConnectionEditPart</code> being added as child.
 * @param index  Position child is being added into.
 */
protected void fireRemovingSourceConnection(ConnectionEditPart connection, int index) {
	if (eventListeners == null)
		return;
	Iterator listeners = eventListeners.getListeners(NodeListener.class);
	NodeListener listener = null;
	while (listeners.hasNext()) {
		listener = (NodeListener)listeners.next();
		listener.removingSourceConnection(connection, index);
	}
}

/**
 * <img src="../doc-files/dblack.gif"/> Notifies listeners that a target connection has
 * been removed. Called from {@link #removeTargetConnection(ConnectionEditPart)}. There is
 * no reason for subclasses to call or override this method.
 * @param connection  <code>ConnectionEditPart</code> being added as child.
 * @param index  Position child is being added into.
 */
protected void fireRemovingTargetConnection(ConnectionEditPart connection, int index) {
	if (eventListeners == null)
		return;
	Iterator listeners = eventListeners.getListeners(NodeListener.class);
	NodeListener listener = null;
	while (listeners.hasNext()) {
		listener = (NodeListener)listeners.next();
		listener.removingTargetConnection(connection, index);
	}
}

/**
 * <img src="../doc-files/dblack.gif"/> Notifies listeners that a source connection has
 * been added. Called from {@link #addSourceConnection(ConnectionEditPart, int)}. There is
 * no reason for subclasses to call or override this method.
 * 
 * @param connection  <code>ConnectionEditPart</code> being added as child.
 * @param index  Position child is being added into.
 */
protected void fireSourceConnectionAdded(ConnectionEditPart connection, int index) {
	if (eventListeners == null)
		return;
	Iterator listeners = eventListeners.getListeners(NodeListener.class);
	NodeListener listener = null;
	while (listeners.hasNext()) {
		listener = (NodeListener)listeners.next();
		listener.sourceConnectionAdded(connection, index);
	}
}

/**
 * <img src="../doc-files/dblack.gif"/> Notifies listeners that a target connection has
 * been added. Called from {@link #addTargetConnection(ConnectionEditPart, int)}. There is
 * no reason for subclasses to call or override this method.
 * @param connection  <code>ConnectionEditPart</code> being added as child.
 * @param index  Position child is being added into.
 */
protected void fireTargetConnectionAdded(ConnectionEditPart connection, int index) {
	if (eventListeners == null)
		return;
	Iterator listeners = eventListeners.getListeners(NodeListener.class);
	NodeListener listener = null;
	while (listeners.hasNext()) {
		listener = (NodeListener)listeners.next();
		listener.targetConnectionAdded(connection, index);
	}
}

/**
 * Extends {@link AbstractEditPart#getAdapter(Class)} to handle additional adapter types.
 * Currently, these types include {@link AccessibleHandleProvider} and {@link
 * AccessibleAnchorProvider}. Subclasses should <em>extend</em> this method to support
 * additional adapter types, or to replace the default provided adapaters.
 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(Class) */
public Object getAdapter(Class key) {
	if (key == AccessibleHandleProvider.class)
		return new MergedAccessibleHandles(getEditPolicyIterator());

	if (key == AccessibleAnchorProvider.class)
		return new DefaultAccessibleAnchorProvider();

	return super.getAdapter(key);
}

/**
 * <img src="../doc-files/green.gif"/> If the children's Figures should be  * @see GraphicalEditPart#getContentPane() */
public IFigure getContentPane() {
	return getFigure();
}

/**
 * <img src="../doc-files/blue.gif"/> Overridden to return a default
 * <code>DragTracker</code> for GraphicalEditParts.
 * @see org.eclipse.gef.EditPart#getDragTracker(Request) */
public DragTracker getDragTracker(Request request) {
	return new org.eclipse.gef.tools.DragEditPartsTracker(this);
}

/**
 * The default implementation calls {@link #createFigure()} if the figure is currently
 * <code>null</code>.
 * @see org.eclipse.gef.GraphicalEditPart#getFigure() */
public IFigure getFigure() {
	if (figure == null)
		setFigure(createFigure());
	return figure;
}

/**
 * A convenience method for obtaining the specified layer from the
 * <code>LayerManager</code>.
 * @param layer ID of the Layer
 * @return The requested layer or <code>null</code> if it doesn't exist
 */
protected IFigure getLayer(Object layer) {
	LayerManager manager = (LayerManager)getViewer().getEditPartRegistry()
		.get(LayerManager.ID);
	return manager.getLayer(layer);
}

/**
 * <img src="../doc-files/green.gif"/> Returns the <code>List</code> of the connection
 * model objects for which this EditPart's model is the <b>source</b>. {@link
 * #refreshSourceConnections()} calls this method.  For each connection model object,
 * {@link #createConnection(Object)} will be called automatically to obtain a
 * corresponding {@link ConnectionEditPart}.
 * @return the List of model source connections
 */
protected List getModelSourceConnections() {
	return Collections.EMPTY_LIST;
}

/**
 * <img src="../doc-files/green.gif"/> Returns the <code>List</code> of the connection
 * model objects for which this EditPart's model is the <b>target</b>. {@link
 * #refreshTargetConnections()} calls this method.  For each connection model object,
 * {@link #createConnection(Object)} will be called automatically to obtain a
 * corresponding {@link ConnectionEditPart}.
 * @return the List of model target connections
 */
protected List getModelTargetConnections() {
	return Collections.EMPTY_LIST;
}

/** * @see org.eclipse.gef.GraphicalEditPart#getSourceConnections() */
public List getSourceConnections() {
	if (sourceConnections == null)
		return Collections.EMPTY_LIST;
	return sourceConnections;
}

/** * @see org.eclipse.gef.GraphicalEditPart#getTargetConnections() */
public List getTargetConnections() {
	if (targetConnections == null)
		return Collections.EMPTY_LIST;
	return targetConnections;
}

/**
 * <img src="../doc-files/black.gif"/>
 * Adds the specified source <code>ConnectionEditPart</code> at an index. This method is
 * used to update the {@link #sourceConnections} List. This method is called from {@link
 * #addSourceConnection(ConnectionEditPart, int)}. Subclasses should not call or override
 * this method.
 * @param connection the ConnectionEditPart
 * @param index the index of the add
 */
protected void primAddSourceConnection(ConnectionEditPart connection, int index) {
	if (sourceConnections == null)
		sourceConnections = new ArrayList();
	sourceConnections.add(index, connection);
}

/**
 * <img src="../doc-files/black.gif"/>
 * Adds the specified target <code>ConnectionEditPart</code> at an index. This method is
 * used to update the {@link #targetConnections} List. This method is called from {@link
 * #addTargetConnection(ConnectionEditPart, int)}. Subclasses should not call or override
 * this method.
 * @param connection the ConnectionEditPart
 * @param index the index of the add
 */
protected void primAddTargetConnection(ConnectionEditPart connection, int index) {
	if (targetConnections == null)
		targetConnections = new ArrayList();
	targetConnections.add(index, connection);
}

/**
 * <img src="../doc-files/black.gif"/>
 * Removes the specified source <code>ConnectionEditPart</code> from the {@link
 * #sourceConnections} List. This method is called from {@link
 * #removeSourceConnection(ConnectionEditPart)}. Subclasses should not call or override
 * this method.
 * @param connection  Connection to remove.
 */
protected void primRemoveSourceConnection(ConnectionEditPart connection) {
	sourceConnections.remove(connection);
}

/**
 * <img src="../doc-files/black.gif"/>
 * Removes the specified target <code>ConnectionEditPart</code> from the {@link
 * #targetConnections} List. This method is called from {@link
 * #removeTargetConnection(ConnectionEditPart)}. Subclasses should not call or override
 * this method.
 * @param connection  Connection to remove.
 */
protected void primRemoveTargetConnection(ConnectionEditPart connection) {
	targetConnections.remove(connection);
}

/**
 * <img src="../doc-files/blue.gif"/>
 * Extends {@link AbstractEditPart#refresh()} to refresh two additional structural
 * features: <i>source</i> and <i>target</i> connections. Subclasses should probably
 * override {@link AbstractEditPart#refreshVisuals()} instead of this method.
 * @see org.eclipse.gef.EditPart#refresh() */
public void refresh() {
	super.refresh();
	refreshSourceConnections();
	refreshTargetConnections();
}

/**
 * <img src="../doc-files/black.gif"/> Updates the set of <i>source</i>
 * ConnectionEditParts so that it is in sync with the model source connections. This
 * method is called from {@link #refresh()}, and may also be called in response to
 * notification from the model.
 * <P>
 * The update is performed by comparing the exising source ConnectionEditParts with the
 * set of model source connections returned from {@link #getModelSourceConnections()}.
 * EditParts whose model no longer exists are {@link
 * #removeSourceConnection(ConnectionEditPart) removed}. New models have their
 * ConnectionEditParts {@link #createConnection(Object) created}. Subclasses should
 * override <code>getModelSourceChildren()</code>.
 * <P>
 * This method should <em>not</em> be overridden.
 */
protected void refreshSourceConnections() {
	int i;
	ConnectionEditPart editPart;
	Object model;

	Map modelToEditPart = new HashMap();
	List editParts = getSourceConnections();

	for (i = 0; i < editParts.size(); i++) {
		editPart = (ConnectionEditPart)editParts.get(i);
		modelToEditPart.put(editPart.getModel(), editPart);
	}

	List modelObjects = getModelSourceConnections();
	if (modelObjects == null) modelObjects = new ArrayList();

	for (i = 0; i < modelObjects.size(); i++) {
		model = modelObjects.get(i);
		
		if (i < editParts.size()
			&& ((EditPart) editParts.get(i)).getModel() == model)
				continue;
		
		editPart = (ConnectionEditPart) modelToEditPart.get(model);
		if (editPart != null)
			reorderSourceConnection(editPart, i);
		else {
			editPart = createOrFindConnection(model);
			addSourceConnection(editPart, i);
		}
	}

	//Remove the remaining EditParts
	List trash = new ArrayList ();
	for (; i < editParts.size(); i++)
		trash.add(editParts.get(i));
	for (i = 0; i < trash.size(); i++)
		removeSourceConnection((ConnectionEditPart)trash.get(i));
}

/**
 * <img src="../doc-files/black.gif"/> Updates the set of <i>target</i>
 * ConnectionEditParts so that it is in sync with the model target connections. This
 * method is called from {@link #refresh()}, and may also be called in response to
 * notification from the model.
 * <P>
 * The update is performed by comparing the exising source ConnectionEditParts with the
 * set of model source connections returned from {@link #getModelTargetConnections()}.
 * EditParts whose model no longer exists are {@link
 * #removeTargetConnection(ConnectionEditPart) removed}. New models have their
 * ConnectionEditParts {@link #createConnection(Object) created}. Subclasses should
 * override <code>getModelTargetChildren()</code>.
 * <P>
 * This method should <em>not</em> be overridden.
 */
protected void refreshTargetConnections() {
	int i;
	ConnectionEditPart editPart;
	Object model;

	Map mapModelToEditPart = new HashMap();
	List connections = getTargetConnections();

	for (i = 0; i < connections.size(); i++) {
		editPart = (ConnectionEditPart)connections.get(i);
		mapModelToEditPart.put(editPart.getModel(), editPart);
	}

	List modelObjects = getModelTargetConnections();
	if (modelObjects == null) modelObjects = new ArrayList();

	for (i = 0; i < modelObjects.size(); i++) {
		model = modelObjects.get(i);
		
		if (i < connections.size()
			&& ((EditPart) connections.get(i)).getModel() == model)
				continue;

		editPart = (ConnectionEditPart)mapModelToEditPart.get(model);
		if (editPart != null)
			reorderTargetConnection(editPart, i);
		else {
			editPart = createOrFindConnection(model);
			addTargetConnection(editPart, i);
		}
	}

	//Remove the remaining Connection EditParts
	List trash = new ArrayList ();
	for (; i < connections.size(); i++)
		trash.add(connections.get(i));
	for (i = 0; i < trash.size(); i++)
		removeTargetConnection((ConnectionEditPart)trash.get(i));
}

/**
 * Registers the EditPart's Figure in the Viewer. This is what makes it possible for the
 * Viewer to map a mouse location to an EditPart.
 * @see org.eclipse.gef.editparts.AbstractEditPart#registerVisuals() */
protected void registerVisuals() {
	getViewer().getVisualPartMap().put(getFigure(), this);
}

/**
 * Adds the child's Figure to the {@link #getContentPane() contentPane}. * @see AbstractEditPart#removeChildVisual(EditPart) */
protected void removeChildVisual(EditPart childEditPart) {
	IFigure child = ((GraphicalEditPart)childEditPart).getFigure();
	getContentPane().remove(child);
}

/**
 * @see org.eclipse.gef.GraphicalEditPart#removeNodeListener(org.eclipse.gef.NodeListener)
 */
public void removeNodeListener(NodeListener listener) {
	eventListeners.removeListener(NodeListener.class, listener);
}

/**
 * Extends {@link AbstractEditPart#removeNotify()} to cleanup
 * <code>ConnectionEditParts</code>.
 * @see EditPart#removeNotify() */
public void removeNotify() {
	List conns;
	conns = getSourceConnections();
	for (int i = 0; i < conns.size(); i++)
		((ConnectionEditPart)conns.get(i)).setSource(null);
	conns = getTargetConnections();
	for (int i = 0; i < conns.size(); i++)
		((ConnectionEditPart)conns.get(i)).setTarget(null);
	super.removeNotify();
}

/**
 * <img src="../doc-files/black.gif"/>
 * Removes the given connection for which this EditPart is the <B>source</b>.
 * <BR>Fires notification.
 * <BR>Inverse of {@link #addSourceConnection(ConnectionEditPart, int)}
 * @param connection Connection being removed
 */
protected void removeSourceConnection(ConnectionEditPart connection) {
	fireRemovingSourceConnection(connection, getSourceConnections().indexOf(connection));
	connection.deactivate();
	connection.setSource(null);
	primRemoveSourceConnection(connection);
}

/**
 * <img src="../doc-files/black.gif"/>
 * Removes the given connection for which this EditPart is the <B>target</b>.
 * <BR>Fires notification.
 * <BR>Inverse of {@link #addTargetConnection(ConnectionEditPart, int)}
 * @param connection Connection being removed
 */
protected void removeTargetConnection(ConnectionEditPart connection) {
	fireRemovingTargetConnection(connection, getTargetConnections().indexOf(connection));
	connection.setTarget(null);
	primRemoveTargetConnection(connection);
}

/**
 * This method is extended to preserve a LayoutManager constraint if one exists.
 * @see org.eclipse.gef.editparts.AbstractEditPart#reorderChild(EditPart, int) */
protected void reorderChild(EditPart child, int index) {
	// Save the constraint of the child so that it does not
	// get lost during the remove and re-add.
	IFigure childFigure = ((GraphicalEditPart) child).getFigure();
	LayoutManager layout = getContentPane().getLayoutManager();
	Object constraint = null;
	if (layout != null)
		constraint = layout.getConstraint(childFigure);

	super.reorderChild(child, index);
	getContentPane().setConstraint(childFigure, constraint);
}

/**
 * <img src="../doc-files/black.gif"/>
 * Moves a source <code>ConnectionEditPart</code> into a lower index than it currently
 * occupies. This method is called from {@link #refreshSourceConnections()}.
 * @param connection the ConnectionEditPart
 * @param index the new index
 */
protected void reorderSourceConnection(ConnectionEditPart connection, int index) {
	primRemoveSourceConnection(connection);
	primAddSourceConnection(connection, index);
}

/**
 * <img src="../doc-files/black.gif"/>
 * Moves a target <code>ConnectionEditPart</code> into a lower index than it currently
 * occupies. This method is called from {@link #refreshTargetConnections()}.
 * @param connection the ConnectionEditPart
 * @param index the new index
 */
protected void reorderTargetConnection(ConnectionEditPart connection, int index) {
	primRemoveTargetConnection(connection);
	primAddTargetConnection(connection, index);
}

/**
 * Sets the Figure
 * @param figure the Figure
 */
protected void setFigure(IFigure figure) {
	this.figure = figure;
}

/** * @see org.eclipse.gef.GraphicalEditPart#setLayoutConstraint(EditPart, IFigure, Object) */
public void setLayoutConstraint(EditPart child, IFigure childFigure, Object constraint) {
	getContentPane().setConstraint(childFigure, constraint);
}

/**
 * Implemented to remove the Figure from the Viewer's registry.
 * @see org.eclipse.gef.editparts.AbstractEditPart#unregisterVisuals() */
protected void unregisterVisuals() {
	getViewer().getVisualPartMap().remove(getFigure());
}

}
