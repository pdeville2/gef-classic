package org.eclipse.gef.examples.logicdesigner.edit;
/*
 * Licensed Material - Property of IBM
 * (C) Copyright IBM Corp. 2001, 2002 - All Rights Reserved.
 * US Government Users Restricted Rights - Use, duplication or disclosure
 * restricted by GSA ADP Schedule Contract with IBM Corp.
 */

import org.eclipse.draw2d.Label;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editpolicies.DirectEditPolicy;
import org.eclipse.gef.examples.logicdesigner.model.LogicLabel;
import org.eclipse.gef.examples.logicdesigner.model.commands.LogicLabelCommand;

import org.eclipse.gef.requests.DirectEditRequest;

public class LabelDirectEditPolicy 
	extends DirectEditPolicy {

/**
 * @see DirectEditPolicy#getDirectEditCommand(DirectEditRequest)
 */
protected Command getDirectEditCommand(DirectEditRequest edit) {
	String labelText = (String)edit.getCellEditor().getValue();
	LogicLabelEditPart label = (LogicLabelEditPart)getHost();
	LogicLabelCommand command = new LogicLabelCommand((LogicLabel)label.getModel(),labelText);
	return command;
}

/**
 * @see DirectEditPolicy#showCurrentEditValue(DirectEditRequest)
 */
protected void showCurrentEditValue(DirectEditRequest request) {
	String value = (String)request.getCellEditor().getValue();
	((Label)getHostFigure()).setText(value);
	//hack to prevent async layout from placing the cell editor twice.
	getHostFigure().getUpdateManager().performUpdate();
}

}