/*
 *  SBMLsqueezer creates rate equations for reactions in SBML files
 *  (http://sbml.org).
 *  Copyright (C) 2009 ZBIT, University of Tübingen, Andreas Dräger
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.sbml.squeezer.gui.table;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.event.MouseInputListener;
import javax.swing.event.TableModelEvent;

import org.sbml.jsbml.KineticLaw;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.Parameter;
import org.sbml.jsbml.Reaction;
import org.sbml.squeezer.CfgKeys;
import org.sbml.squeezer.KineticLawGenerator;
import org.sbml.squeezer.Kinetics;
import org.sbml.squeezer.RateLawNotApplicableException;
import org.sbml.squeezer.gui.GUITools;
import org.sbml.squeezer.gui.KineticLawSelectionPanel;
import org.sbml.squeezer.kinetics.BasicKineticLaw;

import atp.sHotEqn;

/**
 * Creates a table that displays all created kinetic equationns.
 * 
 * @since 2.0
 * @version
 * @author Andreas Dr&auml;ger (draeger) <andreas.draeger@uni-tuebingen.de>
 *         Copyright (c) ZBiT, University of T&uuml;bingen, Germany Compiler:
 *         JDK 1.6.0
 * @date Nov 13, 2007
 */
public class KineticLawJTable extends JTable implements MouseInputListener {

	/**
	 * Generated serial version ID
	 */
	private static final long serialVersionUID = -1575566223506382693L;

	private KineticLawGenerator klg;

	private boolean reversibility;

	private boolean editing;

	// private static final int widthMultiplier = 7;

	/**
	 * TODO
	 * 
	 * @param klg
	 * @param maxEducts
	 * @param reversibility
	 */
	public KineticLawJTable(KineticLawGenerator klg) {
		super(new KineticLawTableModel(klg));
		this.klg = klg;
		this.reversibility = ((Boolean) klg.getSettings().get(
				CfgKeys.OPT_TREAT_ALL_REACTIONS_REVERSIBLE)).booleanValue();
		getModel().addTableModelListener(this);
		// setRowHeightAppropriately();
		setColumnWidthAppropriately();
		int maxNumReactnats = ((Integer) (klg.getSettings()
				.get(CfgKeys.OPT_MAX_NUMBER_OF_REACTANTS))).intValue();
		setDefaultRenderer(Object.class, new KineticLawCellRenderer(
				maxNumReactnats));
		getTableHeader()
				.setToolTipText(
						"<html>Double click on the kinetic law to apply another formalism.<br>"
								+ "Single click on any other column to get a formatted equation preview.</html>");
		setCellSelectionEnabled(true);
		setEnabled(true);
		addMouseListener(this);
		editing = false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
	 */
	public void mouseClicked(MouseEvent e) {
		// Point p = e.getPoint();
		// int rowIndex = rowAtPoint(p);
		// int colIndex = columnAtPoint(p);
		// "\tSpalte: "
		// // + colIndex);
		// if (convertColumnIndexToModel(colIndex) == 1) {
		// // Kinetic Law column
		// setCellEditor(rowIndex);
		// }
		Point p = e.getPoint();
		int rowIndex = rowAtPoint(p);
		int colIndex = convertColumnIndexToModel(columnAtPoint(p));
		if (colIndex != 1) {
			Object o = dataModel.getValueAt(rowIndex, 1);
			if (o instanceof BasicKineticLaw) {
				BasicKineticLaw kinetic = (BasicKineticLaw) o;
				String LaTeX = kinetic.getMath().toLaTeX().toString().replace(
						"text", "mbox").replace("mathrm", "mbox").replace(
						"mathtt", "mbox");
				JComponent component = new sHotEqn("\\begin{equation}" + LaTeX
						+ "\\end{equation}");
				JPanel panel = new JPanel(new BorderLayout());
				component.setBackground(Color.WHITE);
				panel.setBackground(Color.WHITE);
				panel.add(component, BorderLayout.CENTER);
				panel.setLocation(((int) MouseInfo.getPointerInfo()
						.getLocation().getX())
						- this.getTopLevelAncestor().getX(), this.getY() + 10);
				panel.setBorder(BorderFactory.createLoweredBevelBorder());
				JOptionPane.showMessageDialog(getParent(), panel,
						"Rate Law of Reaction "
								+ kinetic.getParentSBMLObject().getId(),
						JOptionPane.INFORMATION_MESSAGE);
				// JLayeredPane.getLayeredPaneAbove(getParent()).add(component,
				// JLayeredPane.POPUP_LAYER);
				validate();
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.event.MouseMotionListener#mouseDragged(java.awt.event.MouseEvent
	 * )
	 */
	public void mouseDragged(MouseEvent e) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
	 */
	public void mouseEntered(MouseEvent e) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
	 */
	public void mouseExited(MouseEvent e) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.event.MouseMotionListener#mouseMoved(java.awt.event.MouseEvent)
	 */
	public void mouseMoved(MouseEvent e) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
	 */
	public void mousePressed(MouseEvent e) {
		Point p = e.getPoint();
		int rowIndex = rowAtPoint(p);
		int colIndex = columnAtPoint(p);
		// Kinetic Law column
		if ((convertColumnIndexToModel(colIndex) == 1) && !editing) {
			// setCellEditor(null);
			setCellEditor(rowIndex);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
	 */
	public void mouseReleased(MouseEvent e) {
	}

	/**
	 * Specifies weather or not all reactions will be modeled in a reversible
	 * manner or as specified by the SBML model.
	 * 
	 * @param reversibility
	 */
	public void setReversibility(boolean reversibility) {
		this.reversibility = reversibility;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.JTable#tableChanged(javax.swing.event.TableModelEvent)
	 */
	// @Override
	public void tableChanged(TableModelEvent e) {
		super.tableChanged(e);
	}

	/**
	 * Sets up a combo box, which allows to select an appropriate value for a
	 * kinetic law in the given row.
	 * 
	 * @param rowIndex
	 */
	private void setCellEditor(int rowIndex) {
		if ((dataModel.getRowCount() > 0) && (dataModel.getColumnCount() > 0)) {
			Model model = klg.getModel();
			Reaction reaction = model.getReaction(dataModel.getValueAt(
					rowIndex, 0).toString());
			try {
				Kinetics possibleTypes[] = this.klg
						.identifyPossibleReactionTypes(reaction.getId());
				KineticLaw possibleLaws[] = new KineticLaw[possibleTypes.length];
				int selected = 0;
				for (int i = 0; i < possibleLaws.length; i++) {
					possibleLaws[i] = klg.createKineticLaw(reaction,
							possibleTypes[i], reversibility);
					if (possibleLaws[i].isSetSBOTerm()
							&& reaction.getKineticLaw().isSetSBOTerm()
							&& possibleLaws[i].getSBOTerm() == reaction
									.getKineticLaw().getSBOTerm()) {
						selected = i;
					}
				}
				KineticLawSelectionPanel klsp = new KineticLawSelectionPanel(
						possibleLaws, selected);
				if (JOptionPane
						.showConfirmDialog(getParent(), klsp,
								"Choose an alternative kinetic law",
								JOptionPane.OK_CANCEL_OPTION,
								JOptionPane.QUESTION_MESSAGE,
								GUITools.LEMON_ICON_SMALL) == JOptionPane.OK_OPTION) {
					int i = 0;
					while (i < possibleTypes.length
							&& !possibleTypes[i].equals(klsp
									.getSelectedKinetic()))
						i++;
					KineticLaw kineticLaw = possibleLaws[i];

					// Reaction Identifier, Kinetic Law, SBO, #Reactants,
					// Reactants, Products, Parameters, Formula
					StringBuffer params = new StringBuffer();
					for (i = kineticLaw.getNumParameters() - 1; i > 0; i--) {
						params.append(kineticLaw.getParameter(i));
						if (i > 0)
							params.append(", ");
					}
					List<Parameter> referencedGlobalParameters = KineticLawGenerator
							.findReferencedGlobalParameters(kineticLaw
									.getMath());
					for (i = referencedGlobalParameters.size() - 1; i > 0; i--) {
						params.append(referencedGlobalParameters.get(i));
						if (i > 0)
							params.append(", ");
					}
					dataModel.setValueAt(kineticLaw.toString(),
							getSelectedRow(), 1);
					dataModel.setValueAt(kineticLaw.getSBOTermID(),
							getSelectedRow(), 2);
					dataModel.setValueAt(params, getSelectedRow(), dataModel
							.getColumnCount() - 2);
					dataModel.setValueAt(kineticLaw.getFormula(),
							getSelectedRow(), dataModel.getColumnCount() - 1);
					i = 0;
					while ((i < klg.getModel().getNumReactions())
							&& (!klg.getModel().getReaction(i).getId().equals(
									kineticLaw.getParentSBMLObject().getId())))
						i++;
					klg.getModel().getReaction(i).setKineticLaw(kineticLaw);
					setColumnWidthAppropriately();
					editing = false;
				}
			} catch (RateLawNotApplicableException e) {
				JOptionPane.showMessageDialog(this, e.getMessage(), e
						.getClass().getName(), JOptionPane.WARNING_MESSAGE);
			}
		}
	}

	/**
	 * 
	 */
	private void setColumnWidthAppropriately() {
		for (int col = 0; col < getColumnCount(); col++) {
			int maxLength = getColumnModel().getColumn(col).getHeaderValue()
					.toString().length();
			for (int row = 0; row < getRowCount(); row++)
				if (maxLength < getValueAt(row, col).toString().length())
					maxLength = getValueAt(row, col).toString().length();
			getColumnModel().getColumn(col).setPreferredWidth(
					3 * getFont().getSize() / 5 * maxLength + 10);
		}
	}

}
