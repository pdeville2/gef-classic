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
package org.eclipse.gef.ui.palette;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;

import org.eclipse.gef.ContextMenuProvider;
import org.eclipse.gef.ui.actions.GEFActionConstants;
import org.eclipse.gef.internal.ui.palette.editparts.DrawerEditPart;

/**
 * Provides the context menu for a palette
 * @author Pratik Shah
 */
public class PaletteContextMenuProvider
	extends ContextMenuProvider
{

/**
 * Constructor
 * 
 * @param palette the palette viewer for which the context menu has to be created
 */
public PaletteContextMenuProvider(PaletteViewer palette) {
	super(palette);
}

/**
 * Returns the palette viewer.
 * @return the palette viewer
 */
protected PaletteViewer getPaletteViewer() {
	return (PaletteViewer)getViewer();
}

/**
 * This is the actual method that builds the context menu.
 * 
 * @param menu The IMenuManager to which actions for the palette's context menu can be
 * 				added
 * @see org.eclipse.gef.ui.parts.ContextMenuProvider#buildContextMenu(org.eclipse.jface.action.IMenuManager)
 */
public void buildContextMenu(IMenuManager menu) {
	GEFActionConstants.addStandardActionGroups(menu);

	Object selectedPart = getPaletteViewer().getSelectedEditParts().get(0);
	if (selectedPart instanceof DrawerEditPart && ((DrawerEditPart)selectedPart).canBePinned()) {
		menu.appendToGroup(GEFActionConstants.MB_ADDITIONS, 
							new PinDrawerAction((DrawerEditPart)selectedPart));
	}
	menu.appendToGroup(GEFActionConstants.GROUP_VIEW, new LayoutAction(
			getPaletteViewer().getPaletteViewerPreferences()));
	menu.appendToGroup(GEFActionConstants.GROUP_VIEW, new ChangeIconSizeAction(
			getPaletteViewer().getPaletteViewerPreferences()));
	if (getPaletteViewer().getCustomizer() != null) {
		menu.appendToGroup(GEFActionConstants.GROUP_REST, 
							new CustomizeAction(getPaletteViewer()));
	}
	menu.appendToGroup(GEFActionConstants.GROUP_REST, 
						new SettingsAction(getPaletteViewer()));
}

}
