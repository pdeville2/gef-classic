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
package org.eclipse.gef.examples.logicdesigner.model.commands;

import org.eclipse.draw2d.*;

import org.eclipse.gef.examples.logicdesigner.model.WireBendpoint;

public class MoveBendpointCommand
	extends BendpointCommand 
{

private Bendpoint oldBendpoint;

public void execute() {
	WireBendpoint bp = new WireBendpoint();
	bp.setRelativeDimensions(getFirstRelativeDimension(), 
					getSecondRelativeDimension());
	setOldBendpoint((Bendpoint)getWire().getBendpoints().get(getIndex()));
	getWire().setBendpoint(getIndex(), bp);
	super.execute();
}

protected Bendpoint getOldBendpoint() {
	return oldBendpoint;
}

public void setOldBendpoint(Bendpoint bp) {
	oldBendpoint = bp;
}

public void undo() {
	super.undo();
	getWire().setBendpoint(getIndex(), getOldBendpoint());
}

}


