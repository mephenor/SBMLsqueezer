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
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.sbml.squeezer.gui;

import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.util.HashSet;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.BevelBorder;
import javax.swing.border.TitledBorder;

import org.sbml.SBO;
import org.sbml.squeezer.Kinetics;

/**
 * This class is a panel, which contains all necessary options to be specified
 * for an appropriate creation of kinetic laws. It is also a data structure,
 * providing get and set methods to alter the current status of the user
 * settings.
 * 
 * @since 1.0
 * @version
 * @author <a href="mailto:andreas.draeger@uni-tuebingen.de">Andreas
 *         Dr&auml;ger</a>
 * @date Nov 15, 2007
 */
public class JSettingsPanel extends JPanel {

	/**
	 * Generated serial version ID.
	 */
	private static final long serialVersionUID = 5242359204965070649L;

	private JCheckBox jCheckBoxTreatAllReactionsAsEnzyeReaction;

	private JCheckBox jCheckBoxAddAllParametersGlobally;

	private JSpinner jSpinnerMaxRealisticNumOfReactants;

	private JCheckBox jCheckBoxWarnings;

	private JRadioButton jRadioButtonGenerateForAllReactions;

	private JRadioButton jRadioButtonForceReacRev;

	private JCheckBox jCheckBoxPossibleEnzymeRNA;

	private JCheckBox jCheckBoxPossibleEnzymeAsRNA;

	private JCheckBox jCheckBoxPossibleEnzymeGenericProtein;

	private JCheckBox jCheckBoxPossibleEnzymeTruncatedProtein;

	private JCheckBox jCheckBoxPossibleEnzymeSimpleMolecule;

	private JCheckBox jCheckBoxPossibleEnzymeComplex;

	private JCheckBox jCheckBoxPossibleEnzymeReceptor;

	private JCheckBox jCheckBoxPossibleEnzymeUnknown;

	private JRadioButton jRadioButtonGMAK;

	private JRadioButton jRadioButtonUniUniMMK;

	private JRadioButton jRadioButtonUniUniCONV;

	private JRadioButton jRadioButtonBiUniRND;

	private JRadioButton jRadioButtonBiUniCONV;

	private JRadioButton jRadioButtonBiBiRND;

	private JRadioButton jRadioButtonBiBiCONV;

	private JRadioButton jRadioButtonBiBiORD;

	private JRadioButton jRadioButtonBiBiPP;

	private JRadioButton jRadioButtonOtherEnzymCONV;

	private JRadioButton jRadioButtonBiUniORD;

	private boolean possibleEnzymeAllNotChecked;

	public JSettingsPanel() {
		super(new GridBagLayout());
		possibleEnzymeAllNotChecked = false;
		ButtonGroup buttonGroup;
		Font titleFont = new Font("Dialog", Font.BOLD, 12);
		Color borderColor = new Color(51, 51, 51);

		// Top Panel
		GridBagLayout layout = new GridBagLayout();
		JPanel jPanelGeneralOptions = new JPanel(layout);
		jPanelGeneralOptions.setBorder(BorderFactory.createTitledBorder(null,
				" General Options ", TitledBorder.CENTER,
				TitledBorder.DEFAULT_POSITION, titleFont, borderColor));
		jCheckBoxTreatAllReactionsAsEnzyeReaction = new JCheckBox(
				"<html>Consider all reactions to be<br>enzyme-catalyzed</html>");
		jCheckBoxTreatAllReactionsAsEnzyeReaction
				.setToolTipText("<html>If checked, all reactions are considered to be enzyme-catalyzed.</html>");
		jCheckBoxTreatAllReactionsAsEnzyeReaction.setBackground(Color.WHITE);
		jCheckBoxAddAllParametersGlobally = new JCheckBox(
				"Add all new parameters globally");
		jCheckBoxAddAllParametersGlobally
				.setToolTipText("<html>If selected, all newly created parameters are stored <br>"
						+ "globally in the model. Otherwise SBMLsqueezer only stores most <br>"
						+ "parameters locally in the respective rate law.</html>");
		jCheckBoxAddAllParametersGlobally.setBackground(Color.WHITE);
		jCheckBoxAddAllParametersGlobally.setSelected(true);
		jCheckBoxWarnings = new JCheckBox("Warnings for too many reactants:");
		jCheckBoxWarnings.setSelected(true);
		jCheckBoxWarnings
				.setToolTipText("<html>If checked, warnings will be shown for reactions<br>"
						+ "with more reactants than specified here.</html>");
		jCheckBoxWarnings.setBackground(Color.WHITE);
		jSpinnerMaxRealisticNumOfReactants = new JSpinner(
				new SpinnerNumberModel(3, 2, 10, 1));
		jSpinnerMaxRealisticNumOfReactants
				.setToolTipText("<html>Specifiy how many reactants are at most likely to collide.</html>");
		jSpinnerMaxRealisticNumOfReactants.setBackground(Color.WHITE);
		jPanelGeneralOptions.setBackground(Color.WHITE);
		LayoutHelper.addComponent(jPanelGeneralOptions, layout,
				jCheckBoxTreatAllReactionsAsEnzyeReaction, 0, 0, 1, 1, 1, 0);
		LayoutHelper.addComponent(jPanelGeneralOptions, layout,
				jCheckBoxAddAllParametersGlobally, 1, 0, 1, 1, 1, 0);
		LayoutHelper.addComponent(jPanelGeneralOptions, layout,
				jCheckBoxWarnings, 0, 1, 1, 1, 1, 0);
		LayoutHelper.addComponent(jPanelGeneralOptions, layout,
				jSpinnerMaxRealisticNumOfReactants, 1, 1, 1, 1, 1, 0);

		// Second Panel
		JRadioButton jRadioButtonGenerateOnlyMissingKinetics = new JRadioButton(
				"Only when missing");
		jRadioButtonGenerateOnlyMissingKinetics
				.setToolTipText("<html>If checked, kinetics are only generated if missing in the SBML file.</html>");
		jRadioButtonGenerateOnlyMissingKinetics.setBackground(Color.WHITE);
		jRadioButtonGenerateForAllReactions = new JRadioButton(
				"For all reactions");
		jRadioButtonGenerateForAllReactions
				.setToolTipText("<html>If checked, already existing kinetic laws will be overwritten.</html>");
		jRadioButtonGenerateForAllReactions.setBackground(Color.WHITE);
		buttonGroup = new ButtonGroup();
		buttonGroup.add(jRadioButtonGenerateForAllReactions);
		buttonGroup.add(jRadioButtonGenerateOnlyMissingKinetics);
		jRadioButtonGenerateOnlyMissingKinetics.setSelected(true);
		layout = new GridBagLayout();
		JPanel jPanelGenerateNewKinetics = new JPanel(layout);
		jPanelGenerateNewKinetics.setBorder(BorderFactory.createTitledBorder(
				null, " Generate New Kinetics ",
				TitledBorder.DEFAULT_JUSTIFICATION,
				TitledBorder.DEFAULT_POSITION, titleFont, borderColor));
		jPanelGenerateNewKinetics.setBackground(Color.WHITE);
		LayoutHelper.addComponent(jPanelGenerateNewKinetics, layout,
				jRadioButtonGenerateOnlyMissingKinetics, 0, 0, 1, 1, 1, 0);
		LayoutHelper.addComponent(jPanelGenerateNewKinetics, layout,
				jRadioButtonGenerateForAllReactions, 0, 1, 1, 1, 1, 0);

		// Third Panel
		jRadioButtonForceReacRev = new JRadioButton(
				"Model all reactions in a reversible manner");
		jRadioButtonForceReacRev.setSelected(true);
		jRadioButtonForceReacRev
				.setToolTipText("<html>If checked, all reactions will be set to reversible no matter what is given by the SBML file.</html>");
		jRadioButtonForceReacRev.setBackground(Color.WHITE);
		JRadioButton jRadioButtonSettingsFrameForceRevAsCD = new JRadioButton(
				"Use information from SBML");
		jRadioButtonSettingsFrameForceRevAsCD
				.setToolTipText("<html>If checked, the information about reversiblity will be left unchanged.</html>");
		jRadioButtonSettingsFrameForceRevAsCD.setBackground(Color.WHITE);
		buttonGroup = new ButtonGroup();
		buttonGroup.add(jRadioButtonSettingsFrameForceRevAsCD);
		buttonGroup.add(jRadioButtonForceReacRev);
		layout = new GridBagLayout();
		JPanel jPanelSettingsReversibility = new JPanel();
		jPanelSettingsReversibility.setLayout(layout);
		jPanelSettingsReversibility.setBorder(BorderFactory.createTitledBorder(
				null, " Reversibility ", TitledBorder.DEFAULT_JUSTIFICATION,
				TitledBorder.DEFAULT_POSITION, titleFont, borderColor));
		jPanelSettingsReversibility.setBackground(Color.WHITE);
		LayoutHelper.addComponent(jPanelSettingsReversibility, layout,
				jRadioButtonSettingsFrameForceRevAsCD, 0, 0, 1, 1, 1, 0);
		LayoutHelper.addComponent(jPanelSettingsReversibility, layout,
				jRadioButtonForceReacRev, 0, 1, 1, 1, 1, 0);

		// Fourth Panel
		jCheckBoxPossibleEnzymeGenericProtein = new JCheckBox("Generic protein");
		jCheckBoxPossibleEnzymeGenericProtein.setSelected(true);
		jCheckBoxPossibleEnzymeGenericProtein
				.setToolTipText("<html>If checked, generic proteins are treated as enzymes.<br>"
						+ "Otherwise, generic protein-catalyzed reactions are<br>"
						+ "not considered to be enzyme reactions.</html>");
		jCheckBoxPossibleEnzymeGenericProtein.setBackground(Color.WHITE);
		jCheckBoxPossibleEnzymeRNA = new JCheckBox("RNA");
		jCheckBoxPossibleEnzymeRNA.setSelected(true);
		jCheckBoxPossibleEnzymeRNA
				.setToolTipText("<html>If checked, RNA is treated as an enzyme.<br>"
						+ "Otherwise RNA catalyzed reactions are not<br>"
						+ "considered to be enzyme-catalyzed reactions.</html>");
		jCheckBoxPossibleEnzymeRNA.setBackground(Color.WHITE);
		jCheckBoxPossibleEnzymeComplex = new JCheckBox("Complex");
		jCheckBoxPossibleEnzymeComplex.setSelected(true);
		jCheckBoxPossibleEnzymeComplex
				.setToolTipText("<html>If checked, complex molecules are treated as enzymes.<br>"
						+ "Otherwise, complex catalized reactions are not<br>"
						+ "considered to be enzyme reactions.</html>");
		jCheckBoxPossibleEnzymeComplex.setBackground(Color.WHITE);
		jCheckBoxPossibleEnzymeTruncatedProtein = new JCheckBox(
				"Truncated protein");
		jCheckBoxPossibleEnzymeTruncatedProtein.setSelected(true);
		jCheckBoxPossibleEnzymeTruncatedProtein
				.setToolTipText("<html>If checked, truncated proteins are treated as enzymes.<br>"
						+ "Otherwise, truncated protein catalized reactions<br>"
						+ "are not considered to be enzyme reactions.</html>");
		jCheckBoxPossibleEnzymeTruncatedProtein.setBackground(Color.WHITE);
		jCheckBoxPossibleEnzymeReceptor = new JCheckBox("Receptor");
		jCheckBoxPossibleEnzymeReceptor
				.setToolTipText("<html>If checked, receptors are treated as enzymes.<br>"
						+ "Otherwise, receptor catalized reactions are not <br>"
						+ "considered to be enzyme reactions.</html>");
		jCheckBoxPossibleEnzymeReceptor.setBackground(Color.WHITE);
		jCheckBoxPossibleEnzymeUnknown = new JCheckBox("Unknown");
		jCheckBoxPossibleEnzymeUnknown
				.setToolTipText("<html>If checked, unknown molecules are treated as enzymes.<br>"
						+ "Otherwise, unknown molecule catalized reactions are not<br>"
						+ "considered to be enzyme reactions.</html>");
		jCheckBoxPossibleEnzymeUnknown.setBackground(Color.WHITE);
		jCheckBoxPossibleEnzymeAsRNA = new JCheckBox("asRNA");
		jCheckBoxPossibleEnzymeAsRNA
				.setToolTipText("<html>If checked, asRNA is treated as an enzyme.<br>"
						+ "Otherwise asRNA catalized reactions are not<br>"
						+ "considered to be enzyme-catalyzed reactions.</html>");
		jCheckBoxPossibleEnzymeAsRNA.setBackground(Color.WHITE);
		jCheckBoxPossibleEnzymeSimpleMolecule = new JCheckBox("Simple molecule");
		jCheckBoxPossibleEnzymeSimpleMolecule
				.setToolTipText("<html>If checked, simple molecules are treated as enzymes.<br>"
						+ "Otherwise, simple molecule catalized reactions are not<br>"
						+ "considered to be enzyme reactions.</html>");
		jCheckBoxPossibleEnzymeSimpleMolecule.setBackground(Color.WHITE);

		layout = new GridBagLayout();
		JPanel jPanelSettingsEnzymes = new JPanel();
		jPanelSettingsEnzymes.setLayout(layout);
		jPanelSettingsEnzymes.setBorder(BorderFactory.createTitledBorder(null,
				" Species to Be Treated as Enzymes ",
				TitledBorder.DEFAULT_JUSTIFICATION,
				TitledBorder.DEFAULT_POSITION, titleFont, borderColor));
		jPanelSettingsEnzymes.setBackground(Color.WHITE);
		LayoutHelper.addComponent(jPanelSettingsEnzymes, layout,
				jCheckBoxPossibleEnzymeGenericProtein, 0, 0, 1, 1, 1, 0);
		LayoutHelper.addComponent(jPanelSettingsEnzymes, layout,
				jCheckBoxPossibleEnzymeRNA, 1, 0, 1, 1, 1, 0);
		LayoutHelper.addComponent(jPanelSettingsEnzymes, layout,
				jCheckBoxPossibleEnzymeComplex, 2, 0, 1, 1, 1, 0);
		LayoutHelper.addComponent(jPanelSettingsEnzymes, layout,
				jCheckBoxPossibleEnzymeTruncatedProtein, 3, 0, 1, 1, 1, 0);
		LayoutHelper.addComponent(jPanelSettingsEnzymes, layout,
				jCheckBoxPossibleEnzymeReceptor, 0, 1, 1, 1, 1, 0);
		LayoutHelper.addComponent(jPanelSettingsEnzymes, layout,
				jCheckBoxPossibleEnzymeAsRNA, 1, 1, 1, 1, 1, 0);
		LayoutHelper.addComponent(jPanelSettingsEnzymes, layout,
				jCheckBoxPossibleEnzymeUnknown, 2, 1, 1, 1, 1, 0);
		LayoutHelper.addComponent(jPanelSettingsEnzymes, layout,
				jCheckBoxPossibleEnzymeSimpleMolecule, 3, 1, 1, 1, 1, 0);

		// Reaction Mechanism Panel
		JPanel jPanelSettingsReactionMechanism = new JPanel();
		jPanelSettingsReactionMechanism.setLayout(new GridLayout(1, 2));
		jPanelSettingsReactionMechanism.setBorder(BorderFactory
				.createTitledBorder(null, " Reaction Mechanisms ",
						TitledBorder.CENTER, TitledBorder.DEFAULT_POSITION,
						titleFont, borderColor));
		jPanelSettingsReactionMechanism.setBackground(Color.WHITE);

		// Sub-panels for the reaction mechanism panel
		JPanel leftMechanismPanel = new JPanel(new GridBagLayout());
		leftMechanismPanel.setBackground(Color.WHITE);

		JPanel rightMechanismPanel = new JPanel(new GridBagLayout());
		rightMechanismPanel.setBackground(Color.WHITE);

		// Non-Enzyme Reactions
		JPanel nonEnzyme = new JPanel(new GridLayout(1, 1));
		nonEnzyme.setBorder(BorderFactory.createTitledBorder(null,
				" Non-Enzyme Reaction ", TitledBorder.DEFAULT_JUSTIFICATION,
				TitledBorder.DEFAULT_POSITION, titleFont, borderColor));
		jRadioButtonGMAK = new JRadioButton(
				"Genereralized mass-action kinetics");
		jRadioButtonGMAK.setSelected(true);
		jRadioButtonGMAK
				.setToolTipText("<html>Reactions, which are not enzyme-catalyzed,<br>"
						+ "are described using generalized mass-action kinetics.</html>");
		jRadioButtonGMAK.setEnabled(false);
		jRadioButtonGMAK.setBackground(Color.WHITE);

		buttonGroup = new ButtonGroup();
		buttonGroup.add(jRadioButtonGMAK);

		nonEnzyme.add(jRadioButtonGMAK);
		nonEnzyme.setBackground(Color.WHITE);

		// Hill equation
		JPanel geneRegulation = new JPanel(new GridLayout(1, 1));
		geneRegulation.setBorder(BorderFactory.createTitledBorder(null,
				" Gene Expression Regulation ",
				TitledBorder.DEFAULT_JUSTIFICATION,
				TitledBorder.DEFAULT_POSITION, titleFont, borderColor));
		JRadioButton hillKineticsRadioButton = new JRadioButton("Hill equation");
		hillKineticsRadioButton
				.setToolTipText("<html>Check this box if you want the Hill equation as reaction scheme<br>"
						+ "for gene regulation (reactions involving genes, RNA and proteins).</html>");
		hillKineticsRadioButton.setBackground(Color.WHITE);
		hillKineticsRadioButton.setSelected(true);
		hillKineticsRadioButton.setEnabled(false);
		buttonGroup = new ButtonGroup();
		buttonGroup.add(hillKineticsRadioButton);
		geneRegulation.add(hillKineticsRadioButton);
		geneRegulation.setBackground(Color.WHITE);

		// Uni-Uni Reactions
		JPanel uniUniReactions = new JPanel(new GridLayout(2, 1));
		uniUniReactions.setBorder(BorderFactory.createTitledBorder(null,
				" Uni-Uni Reaction ", TitledBorder.DEFAULT_JUSTIFICATION,
				TitledBorder.DEFAULT_POSITION, titleFont, borderColor));

		jRadioButtonUniUniMMK = new JRadioButton();
		jRadioButtonUniUniMMK.setSelected(true);
		jRadioButtonUniUniMMK
				.setToolTipText("<html>Check this box if Michaelis-Menten kinetics is to be<br>"
						+ "applied for uni-uni reactions (one reactant, one product).</html>");
		jRadioButtonUniUniMMK.setText("Michaelis-Menten kinetics");
		// uni-uni-type = 3
		jRadioButtonUniUniMMK.setBackground(Color.WHITE);

		jRadioButtonUniUniCONV = new JRadioButton("Convenience kinetics"); //uni-
		// uni
		// -
		// type
		// = 2
		jRadioButtonUniUniCONV
				.setToolTipText("<html>Check this box if convenience kinetics is to be<br>"
						+ "applied for uni-uni reactions (one reactant, one product).</html>");
		jRadioButtonUniUniCONV.setBackground(Color.WHITE);

		buttonGroup = new ButtonGroup();
		buttonGroup.add(jRadioButtonUniUniMMK);
		buttonGroup.add(jRadioButtonUniUniCONV);

		uniUniReactions.add(jRadioButtonUniUniMMK);
		uniUniReactions.add(jRadioButtonUniUniCONV);
		uniUniReactions.setBackground(Color.WHITE);

		// Bi-Uni Reactions
		JPanel biUniReactions = new JPanel(new GridLayout(3, 1));
		biUniReactions.setBorder(BorderFactory.createTitledBorder(null,
				" Bi-Uni Reaction ", TitledBorder.DEFAULT_JUSTIFICATION,
				TitledBorder.DEFAULT_POSITION, titleFont, borderColor));

		this.jRadioButtonBiUniORD = new JRadioButton();
		jRadioButtonBiUniORD.setSelected(false);
		jRadioButtonBiUniORD
				.setToolTipText("<html>Check this box if you want the ordered mechanism scheme as<br>"
						+ "reaction scheme for bi-uni reactions (two reactant, one product).</html>");
		jRadioButtonBiUniORD.setText("Ordered"); // biUiType = 6
		jRadioButtonBiUniORD.setBackground(Color.WHITE);

		this.jRadioButtonBiUniCONV = new JRadioButton("Convenience kinetics"); // biUniType
		// = 2
		jRadioButtonBiUniCONV
				.setToolTipText("<html>Check this box if you want convenience kinetics as<br>"
						+ "reaction scheme for bi-uni reactions (two reactant, one product).</html>");
		jRadioButtonBiUniCONV.setBackground(Color.WHITE);

		jRadioButtonBiUniRND = new JRadioButton("Random"); // biUniType = 4
		jRadioButtonBiUniRND.setSelected(true);
		jRadioButtonBiUniRND
				.setToolTipText("<html>Check this box if you want the random mechanism scheme<br>"
						+ "to be applied for bi-uni reactions (two reactants, one product).</html>");
		jRadioButtonBiUniRND.setBackground(Color.WHITE);

		buttonGroup = new ButtonGroup();
		buttonGroup.add(jRadioButtonBiUniORD);
		buttonGroup.add(jRadioButtonBiUniCONV);
		buttonGroup.add(jRadioButtonBiUniRND);

		biUniReactions.add(jRadioButtonBiUniORD);
		biUniReactions.add(jRadioButtonBiUniCONV);
		biUniReactions.add(jRadioButtonBiUniRND);
		biUniReactions.setBackground(Color.WHITE);

		// right panel
		// Bi-Bi Reactions
		JPanel biBiReactions = new JPanel(new GridLayout(4, 1));
		biBiReactions.setBorder(BorderFactory.createTitledBorder(null,
				" Bi-Bi Reaction ", TitledBorder.DEFAULT_JUSTIFICATION,
				TitledBorder.DEFAULT_POSITION, titleFont, borderColor));

		jRadioButtonBiBiRND = new JRadioButton("Random"); // biBiType = 4
		jRadioButtonBiBiRND.setSelected(true);
		jRadioButtonBiBiRND
				.setToolTipText("<html>Check this box if you want the random mechanism to be applied<br>"
						+ "to bi-bi reactions (two reactants, two products).</html>");
		jRadioButtonBiBiRND.setBackground(Color.WHITE);

		jRadioButtonBiBiCONV = new JRadioButton("Convenience kinetics"); // biBiType
		// = 2
		jRadioButtonBiBiCONV
				.setToolTipText("<html>Check this box if you want convenience kinetics to be applied<br>"
						+ "for bi-bi reactions (two reactants, two products).</html>");
		jRadioButtonBiBiCONV.setBackground(Color.WHITE);

		jRadioButtonBiBiORD = new JRadioButton("Ordered"); // biBiType = 6
		jRadioButtonBiBiORD
				.setToolTipText("<html>Check this box if you want the ordered mechanism as reaction scheme<br>"
						+ "for bi-bi reactions (two reactants, two products).</html>");
		jRadioButtonBiBiORD.setBackground(Color.WHITE);

		jRadioButtonBiBiPP = new JRadioButton("Ping-pong"); // biBiType = 5
		jRadioButtonBiBiPP
				.setToolTipText("<html>Check this box if you want the ping-pong mechanism as reaction scheme<br>"
						+ "for bi-bi reactions (two reactants, two products).</html>");
		jRadioButtonBiBiPP.setBackground(Color.WHITE);

		buttonGroup = new ButtonGroup();
		buttonGroup.add(jRadioButtonBiBiRND);
		buttonGroup.add(jRadioButtonBiBiCONV);
		buttonGroup.add(jRadioButtonBiBiORD);
		buttonGroup.add(jRadioButtonBiBiPP);

		biBiReactions.add(jRadioButtonBiBiRND);
		biBiReactions.add(jRadioButtonBiBiCONV);
		biBiReactions.add(jRadioButtonBiBiORD);
		biBiReactions.add(jRadioButtonBiBiPP);
		biBiReactions.setBackground(Color.WHITE);

		// Other Enzyme Reactions
		JPanel otherEnzymeReactions = new JPanel(new GridLayout(1, 1));
		otherEnzymeReactions.setBorder(BorderFactory.createTitledBorder(null,
				" Other Enzyme Reaction ", TitledBorder.DEFAULT_JUSTIFICATION,
				TitledBorder.DEFAULT_POSITION, titleFont, borderColor));

		jRadioButtonOtherEnzymCONV = new JRadioButton();
		jRadioButtonOtherEnzymCONV.setSelected(true);
		jRadioButtonOtherEnzymCONV.setEnabled(true);
		jRadioButtonOtherEnzymCONV
				.setToolTipText("<html>Reactions, which are enzyme-catalyzed, and follow<br>"
						+ "none of the schemes uni-uni, bi-uni and bi-bi.</html>");
		jRadioButtonOtherEnzymCONV.setText("Convenience kinetics");
		jRadioButtonOtherEnzymCONV.setEnabled(false);
		jRadioButtonOtherEnzymCONV.setBackground(Color.WHITE);

		buttonGroup = new ButtonGroup();
		buttonGroup.add(jRadioButtonOtherEnzymCONV);

		otherEnzymeReactions.add(jRadioButtonOtherEnzymCONV);
		otherEnzymeReactions.setBackground(Color.WHITE);

		// Add all sub-panels to the reaction mechanism panel:
		layout = (GridBagLayout) leftMechanismPanel.getLayout();
		LayoutHelper.addComponent(leftMechanismPanel, layout, nonEnzyme, 0, 0,
				1, 1, 1, 0);
		LayoutHelper.addComponent(leftMechanismPanel, layout, uniUniReactions,
				0, 1, 1, 1, 1, 0);
		LayoutHelper.addComponent(leftMechanismPanel, layout, biUniReactions,
				0, 2, 1, 1, 1, 0);
		layout = (GridBagLayout) rightMechanismPanel.getLayout();
		LayoutHelper.addComponent(rightMechanismPanel, layout, geneRegulation,
				0, 0, 1, 1, 1, 0);
		LayoutHelper.addComponent(rightMechanismPanel, layout, biBiReactions,
				0, 1, 1, 1, 1, 0);
		LayoutHelper.addComponent(rightMechanismPanel, layout,
				otherEnzymeReactions, 0, 2, 1, 1, 1, 0);

		jPanelSettingsReactionMechanism.add(leftMechanismPanel);
		jPanelSettingsReactionMechanism.add(rightMechanismPanel);

		// Add all panels to this settings panel:
		layout = (GridBagLayout) this.getLayout();
		LayoutHelper.addComponent(this, layout, jPanelGeneralOptions, 0, 0, 2,
				1, 1, 0);
		LayoutHelper.addComponent(this, layout, jPanelGenerateNewKinetics, 0,
				1, 1, 1, 1, 0);
		LayoutHelper.addComponent(this, layout, jPanelSettingsReversibility, 1,
				1, 1, 1, 1, 0);
		LayoutHelper.addComponent(this, layout, jPanelSettingsEnzymes, 0, 2, 2,
				1, 1, 0);
		LayoutHelper.addComponent(this, layout,
				jPanelSettingsReactionMechanism, 0, 3, 2, 1, 1, 0);

		setBackground(Color.WHITE);
		setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));

		jRadioButtonUniUniMMK.setEnabled(true);
		jRadioButtonUniUniCONV.setEnabled(true);
		jRadioButtonBiUniORD.setEnabled(true);
		jRadioButtonBiUniCONV.setEnabled(true);
		jRadioButtonBiUniRND.setEnabled(true);
		jRadioButtonBiBiPP.setEnabled(true);
		jRadioButtonBiBiORD.setEnabled(true);
		jRadioButtonBiBiCONV.setEnabled(true);
		jRadioButtonBiBiRND.setEnabled(true);
	}

	/**
	 * Possible values are:
	 * <ul>
	 * <li>1 = generalized mass-action kinetics</li>
	 * <li>2 = Convenience kinetics</li>
	 * <li>4 = Random Order Michealis Menten kinetics</li>
	 * <li>5 = Ping-Pong</li>
	 * <li>6 = Ordered</li>
	 * </ul>
	 */
	public Kinetics getBiBiType() {
		if (jRadioButtonBiBiORD.isSelected())
			return Kinetics.ORDERED_MECHANISM;
		if (jRadioButtonBiBiPP.isSelected())
			return Kinetics.PING_PONG_MECAHNISM;
		if (jRadioButtonBiBiRND.isSelected())
			return Kinetics.RANDOM_ORDER_MECHANISM;
		if (jRadioButtonBiBiCONV.isSelected())
			return Kinetics.CONVENIENCE_KINETICS;
		return Kinetics.GENERALIZED_MASS_ACTION;
	}

	/**
	 * Possible values are:
	 * <ul>
	 * <li>1 = generalized mass-action kinetics</li>
	 * <li>2 = Convenience kinetics</li>
	 * <li>4 = Random Order Michealis Menten kinetics</li>
	 * <li>6 = Ordered</li>
	 * </ul>
	 * 
	 * @return
	 */
	public Kinetics getBiUniType() {
		if (jRadioButtonBiUniORD.isSelected())
			return Kinetics.ORDERED_MECHANISM;
		if (jRadioButtonBiUniRND.isSelected())
			return Kinetics.RANDOM_ORDER_MECHANISM;
		if (jRadioButtonBiUniCONV.isSelected())
			return Kinetics.CONVENIENCE_KINETICS;
		return Kinetics.GENERALIZED_MASS_ACTION;
	}

	public Set<Integer> getPossibleEnzymes() {
		Set<Integer> possibleEnzymes = new HashSet<Integer>();
		if (jCheckBoxPossibleEnzymeAsRNA.isSelected())
			possibleEnzymes.add(Integer.valueOf(SBO.convertAlias2SBO("ANTISENSE_RNA")));
		if (jCheckBoxPossibleEnzymeSimpleMolecule.isSelected())
			possibleEnzymes.add(Integer.valueOf(SBO.convertAlias2SBO("SIMPLE_MOLECULE")));
		if (jCheckBoxPossibleEnzymeReceptor.isSelected())
			possibleEnzymes.add(Integer.valueOf(SBO.convertAlias2SBO("RECEPTOR")));
		if (jCheckBoxPossibleEnzymeUnknown.isSelected())
			possibleEnzymes.add(Integer.valueOf(SBO.convertAlias2SBO("UNKNOWN")));
		if (jCheckBoxPossibleEnzymeComplex.isSelected())
			possibleEnzymes.add(Integer.valueOf(SBO.convertAlias2SBO("COMPLEX")));
		if (jCheckBoxPossibleEnzymeTruncatedProtein.isSelected())
			possibleEnzymes.add(Integer.valueOf(SBO.convertAlias2SBO("TRUNCATED")));
		if (jCheckBoxPossibleEnzymeGenericProtein.isSelected())
			possibleEnzymes.add(Integer.valueOf(SBO.convertAlias2SBO("GENERIC")));
		if (jCheckBoxPossibleEnzymeRNA.isSelected())
			possibleEnzymes.add(Integer.valueOf(SBO.convertAlias2SBO("RNA")));
		return possibleEnzymes;
	}

	public int getMaxRealisticNumberOfReactants() {
		return ((Integer) jSpinnerMaxRealisticNumOfReactants.getValue())
				.intValue();
	}

	public short getNonEnzymeReactionsType() {
		// if (jRadioButtonGMAK.isSelected())
		return (short) 1;
	}

	/**
	 * Possible values are:
	 * <ul>
	 * <li>1 = generalized mass-action kinetics</li>
	 * <li>2 = Convenience kinetics</li>
	 * <li>3 = Michaelis-Menten kinetics</li>
	 * </ul>
	 * 
	 * @return
	 */
	public Kinetics getUniUniType() {
		if (jRadioButtonUniUniMMK.isSelected())
			return Kinetics.MICHAELIS_MENTEN;
		if (jRadioButtonUniUniCONV.isSelected())
			return Kinetics.CONVENIENCE_KINETICS;
		return Kinetics.GENERALIZED_MASS_ACTION;
	}

	public boolean isPossibleEnzymeAllNotChecked() {
		return possibleEnzymeAllNotChecked;
	}

	public boolean isPossibleEnzymeAsRNA() {
		return jCheckBoxPossibleEnzymeAsRNA.isSelected();
	}

	public boolean isPossibleEnzymeEnzymeComplex() {
		return jCheckBoxPossibleEnzymeComplex.isSelected();
	}

	public boolean isPossibleEnzymeGenericProtein() {
		return jCheckBoxPossibleEnzymeGenericProtein.isSelected();
	}

	public boolean isPossibleEnzymeReceptor() {
		return jCheckBoxPossibleEnzymeReceptor.isSelected();
	}

	public boolean isPossibleEnzymeRNA() {
		return jCheckBoxPossibleEnzymeRNA.isSelected();
	}

	public boolean isPossibleEnzymeSimpleMolecule() {
		return jCheckBoxPossibleEnzymeSimpleMolecule.isSelected();
	}

	public boolean isPossibleEnzymeTruncatedProtein() {
		return jCheckBoxPossibleEnzymeTruncatedProtein.isSelected();
	}

	public boolean isPossibleEnzymeUnknownMolecule() {
		return jCheckBoxPossibleEnzymeUnknown.isSelected();
	}

	public boolean isSetAllParametersAreAddedGlobally() {
		return jCheckBoxAddAllParametersGlobally.isSelected();
	}

	public boolean isSetAllReactionsAreEnzymeCatalyzed() {
		return jCheckBoxTreatAllReactionsAsEnzyeReaction.isSelected();
	}

	public boolean isSetGenerateKineticsForAllReactions() {
		return jRadioButtonGenerateForAllReactions.isSelected();
	}

	public boolean isSetTreatAllReactionsReversible() {
		return jRadioButtonForceReacRev.isSelected();
	}

	public boolean isSetWarningsForUnrealisticReactions() {
		return jCheckBoxWarnings.isSelected();
	}

	/**
	 * This method checks, if there is any possible Enzyme checked or not
	 * 
	 * @return: void
	 */
	public boolean possibleEnzymeTestAllNotChecked() {
		boolean possibleEnzymeAllNotChecked;
		if (!jCheckBoxPossibleEnzymeRNA.isSelected()
				&& !jCheckBoxPossibleEnzymeAsRNA.isSelected()
				&& !jCheckBoxPossibleEnzymeGenericProtein.isSelected()
				&& !jCheckBoxPossibleEnzymeTruncatedProtein.isSelected()
				&& !jCheckBoxPossibleEnzymeSimpleMolecule.isSelected()
				&& !jCheckBoxPossibleEnzymeComplex.isSelected()
				&& !jCheckBoxPossibleEnzymeReceptor.isSelected()
				&& !jCheckBoxPossibleEnzymeUnknown.isSelected()
				&& !jCheckBoxTreatAllReactionsAsEnzyeReaction.isSelected()) {
			possibleEnzymeAllNotChecked = true;
			jRadioButtonUniUniMMK.setSelected(false);
			jRadioButtonUniUniCONV.setSelected(false);
			jRadioButtonBiUniORD.setSelected(false);
			jRadioButtonBiUniCONV.setSelected(false);
			jRadioButtonBiUniRND.setSelected(false);
			jRadioButtonBiBiPP.setSelected(false);
			jRadioButtonBiBiORD.setSelected(false);
			jRadioButtonBiBiCONV.setSelected(false);
			jRadioButtonBiBiRND.setSelected(false);
			jRadioButtonOtherEnzymCONV.setSelected(false);

			jRadioButtonUniUniMMK.setEnabled(false);
			jRadioButtonUniUniCONV.setEnabled(false);
			jRadioButtonBiUniORD.setEnabled(false);
			jRadioButtonBiUniCONV.setEnabled(false);
			jRadioButtonBiUniRND.setEnabled(false);
			jRadioButtonBiBiPP.setEnabled(false);
			jRadioButtonBiBiORD.setEnabled(false);
			jRadioButtonBiBiCONV.setEnabled(false);
			jRadioButtonBiBiRND.setEnabled(false);
			jRadioButtonOtherEnzymCONV.setEnabled(false);
		} else {
			possibleEnzymeAllNotChecked = false;

			if (getUniUniType() == Kinetics.MICHAELIS_MENTEN)
				jRadioButtonUniUniMMK.setSelected(true);
			else
				jRadioButtonUniUniMMK.setSelected(false);

			if (getUniUniType() == Kinetics.CONVENIENCE_KINETICS)
				jRadioButtonUniUniCONV.setSelected(true);
			else
				jRadioButtonUniUniCONV.setSelected(false);

			if (getBiUniType() == Kinetics.ORDERED_MECHANISM)
				jRadioButtonBiUniORD.setSelected(true);
			else
				jRadioButtonBiUniORD.setSelected(false);

			if (getBiUniType() == Kinetics.CONVENIENCE_KINETICS)
				jRadioButtonBiUniCONV.setSelected(true);
			else
				jRadioButtonBiUniCONV.setSelected(false);

			if (getBiUniType() == Kinetics.RANDOM_ORDER_MECHANISM)
				jRadioButtonBiUniRND.setSelected(true);
			else
				jRadioButtonBiUniRND.setSelected(false);

			if (getBiBiType() == Kinetics.PING_PONG_MECAHNISM)
				jRadioButtonBiBiPP.setSelected(true);
			else
				jRadioButtonBiBiPP.setSelected(false);

			if (getBiBiType() == Kinetics.ORDERED_MECHANISM)
				jRadioButtonBiBiORD.setSelected(true);
			else
				jRadioButtonBiBiORD.setSelected(false);

			if (getBiBiType() == Kinetics.CONVENIENCE_KINETICS)
				jRadioButtonBiBiCONV.setSelected(true);
			else
				jRadioButtonBiBiCONV.setSelected(false);

			if (getBiBiType() == Kinetics.RANDOM_ORDER_MECHANISM)
				jRadioButtonBiBiRND.setSelected(true);
			else
				jRadioButtonBiBiRND.setSelected(false);

			jRadioButtonOtherEnzymCONV.setSelected(true);

			jRadioButtonUniUniMMK.setEnabled(true);
			jRadioButtonUniUniCONV.setEnabled(true);
			jRadioButtonBiUniORD.setEnabled(true);
			jRadioButtonBiUniCONV.setEnabled(true);
			jRadioButtonBiUniRND.setEnabled(true);
			jRadioButtonBiBiPP.setEnabled(true);
			jRadioButtonBiBiORD.setEnabled(true);
			jRadioButtonBiBiCONV.setEnabled(true);
			jRadioButtonBiBiRND.setEnabled(true);
			jRadioButtonOtherEnzymCONV.setEnabled(true);
		}
		return possibleEnzymeAllNotChecked;
	}

	/**
	 * 
	 */
	public void restoreDefaults() {
		setUniUniType(Kinetics.MICHAELIS_MENTEN);
		setBiUniType(Kinetics.RANDOM_ORDER_MECHANISM);
		setBiBiType(Kinetics.RANDOM_ORDER_MECHANISM);
		jCheckBoxWarnings.setSelected(true);
		jSpinnerMaxRealisticNumOfReactants.setModel(new SpinnerNumberModel(3,
				2, 10, 1));
		jRadioButtonGMAK.setSelected(true);

		jCheckBoxPossibleEnzymeRNA.setSelected(true);
		jCheckBoxPossibleEnzymeGenericProtein.setSelected(true);
		jCheckBoxPossibleEnzymeTruncatedProtein.setSelected(true);
		jCheckBoxPossibleEnzymeComplex.setSelected(true);
		jCheckBoxPossibleEnzymeUnknown.setSelected(false);
		jCheckBoxPossibleEnzymeReceptor.setSelected(false);
		jCheckBoxPossibleEnzymeSimpleMolecule.setSelected(false);
		jCheckBoxPossibleEnzymeAsRNA.setSelected(false);
		jCheckBoxTreatAllReactionsAsEnzyeReaction.setSelected(false);

		jRadioButtonUniUniMMK.setEnabled(true);
		jRadioButtonUniUniCONV.setEnabled(true);
		jRadioButtonBiUniORD.setEnabled(true);
		jRadioButtonBiUniCONV.setEnabled(true);
		jRadioButtonBiUniRND.setEnabled(true);
		jRadioButtonBiBiPP.setEnabled(true);
		jRadioButtonBiBiORD.setEnabled(true);
		jRadioButtonBiBiCONV.setEnabled(true);
		jRadioButtonBiBiRND.setEnabled(true);

		jRadioButtonGenerateForAllReactions.setSelected(false);
		jRadioButtonGenerateForAllReactions.setSelected(false);
		jRadioButtonForceReacRev.setSelected(true);

		possibleEnzymeAllNotChecked = possibleEnzymeTestAllNotChecked();
	}

	/**
	 * 
	 * @param b
	 */
	public void setAllParametersAreGlobal(boolean b) {
		jCheckBoxAddAllParametersGlobally.setSelected(b);
	}

	/**
	 * 
	 * @param b
	 */
	public void setAllReactionsAreEnzymeCatalyzed(boolean b) {
		jCheckBoxTreatAllReactionsAsEnzyeReaction.setSelected(b);
	}

	/**
	 * Possible values are:
	 * <ul>
	 * <li>1 = generalized mass-action kinetics</li>
	 * <li>2 = Convenience kinetics</li>
	 * <li>4 = Random Order Michealis Menten kinetics</li>
	 * <li>5 = Ping-Pong</li>
	 * <li>6 = Ordered</li>
	 * </ul>
	 * 
	 * @param type
	 */
	public void setBiBiType(Kinetics type) {
		switch (type) {
		case ORDERED_MECHANISM:
			jRadioButtonBiBiORD.setSelected(true);
			break;
		case PING_PONG_MECAHNISM:
			jRadioButtonBiBiPP.setSelected(true);
			break;
		case RANDOM_ORDER_MECHANISM:
			jRadioButtonBiBiRND.setSelected(true);
			break;
		case CONVENIENCE_KINETICS:
			jRadioButtonBiBiCONV.setSelected(true);
			break;
		default:
			jRadioButtonGMAK.setSelected(true);
			break;
		}
	}

	/**
	 * Possible values are:
	 * <ul>
	 * <li>1 = generalized mass-action kinetics</li>
	 * <li>2 = Convenience kinetics</li>
	 * <li>4 = Random Order Michealis Menten kinetics</li>
	 * <li>6 = Ordered</li>
	 * </ul>
	 * 
	 * @param type
	 */
	public void setBiUniType(Kinetics type) {
		switch (type) {
		case ORDERED_MECHANISM:
			jRadioButtonBiUniORD.setSelected(true);
			break;
		case RANDOM_ORDER_MECHANISM:
			jRadioButtonBiUniRND.setSelected(true);
			break;
		case CONVENIENCE_KINETICS:
			jRadioButtonBiUniCONV.setSelected(true);
			break;
		default:
			jRadioButtonGMAK.setSelected(true);
			break;
		}
	}

	public void setGenerateKineticsForAllReactions(boolean b) {
		jCheckBoxTreatAllReactionsAsEnzyeReaction.setSelected(true);
	}

	public void setMaxRealisticNumberOfReactants(int value) {
		jSpinnerMaxRealisticNumOfReactants.setValue(Integer.valueOf(value));
	}

	public void setNonEnzymeReactionsType(short type) {
		switch (type) {
		// TODO: other types
		default: // type = 1
			jRadioButtonGMAK.setSelected(true);
			break;
		}
	}

	public void setPossibleEnzymeAllNotChecked(boolean possibleEnzymeAllNotChecked) {
		this.possibleEnzymeAllNotChecked = possibleEnzymeAllNotChecked;
	}

	public void setPossibleEnzymeAsRNA(boolean b) {
		jCheckBoxPossibleEnzymeAsRNA.setSelected(b);
	}

	public void setPossibleEnzymeEnzymeComplex(boolean b) {
		jCheckBoxPossibleEnzymeComplex.setSelected(b);
	}

	public void setPossibleEnzymeGenericProtein(boolean b) {
		jCheckBoxPossibleEnzymeGenericProtein.setSelected(b);
	}

	public void setPossibleEnzymeReceptor(boolean b) {
		jCheckBoxPossibleEnzymeReceptor.setSelected(b);
	}

	public void setPossibleEnzymeRNA(boolean b) {
		jCheckBoxPossibleEnzymeRNA.setSelected(b);
	}

	public void setPossibleEnzymeSimpleMolecule(boolean b) {
		jCheckBoxPossibleEnzymeSimpleMolecule.setSelected(b);
	}

	public void setPossibleEnzymeTruncatedProtein(boolean b) {
		jCheckBoxPossibleEnzymeTruncatedProtein.setSelected(b);
	}

	public void setPossibleEnzymeUnknownMolecule(boolean b) {
		jCheckBoxPossibleEnzymeUnknown.setSelected(b);
	}

	public void setTreatAllReactionsReversible(boolean b) {
		jRadioButtonForceReacRev.setSelected(b);
	}

	/**
	 * Possible values are:
	 * <ul>
	 * <li>1 = generalized mass-action kinetics</li>
	 * <li>2 = Convenience kinetics</li>
	 * <li>3 = Michaelis-Menten kinetics</li>
	 * </ul>
	 * 
	 * @param type
	 */
	public void setUniUniType(Kinetics type) {
		switch (type) {
		case MICHAELIS_MENTEN:
			jRadioButtonUniUniMMK.setSelected(true);
			break;
		case CONVENIENCE_KINETICS:
			jRadioButtonUniUniCONV.setSelected(true);
			break;
		default:
			jRadioButtonGMAK.setSelected(true);
			break;
		}
	}

	public void setWarningsForUnrealisticReactions(boolean b) {
		jCheckBoxWarnings.setSelected(b);
	}

}
