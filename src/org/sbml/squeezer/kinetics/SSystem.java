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
package org.sbml.squeezer.kinetics;

import java.util.List;

import org.sbml.jsbml.ASTNode;
import org.sbml.jsbml.ModifierSpeciesReference;
import org.sbml.jsbml.Parameter;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.SBO;
import org.sbml.jsbml.Species;
import org.sbml.squeezer.RateLawNotApplicableException;

/**
 * 
 * This class creates a S-System based equation as defined in the papers "A
 * model-based optimization framework for the inference on gene regulatory
 * networks from DNA array data" and "A model-based optimization framework for
 * the inference of regulatory interactions using time-course DNA microarray
 * expression data" of Thomas et al. 2004 and 2007
 * 
 * @author <a href="mailto:snitschm@gmx.de">Sandra Nitschmann</a>
 * 
 */
public class SSystem extends BasicKineticLaw {


	/**
	 * @param parentReaction
	 * @param typeParameters
	 * @throws RateLawNotApplicableException
	 */
	public SSystem(Reaction parentReaction, Object... typeParameters)
			throws RateLawNotApplicableException {
		super(parentReaction, typeParameters);
	}


	/* (Kein Javadoc)
	 * @see org.sbml.squeezer.kinetics.BasicKineticLaw#createKineticEquation(java.util.List, java.util.List, java.util.List, java.util.List, java.util.List, java.util.List)
	 */
	ASTNode createKineticEquation(List<String> modE, List<String> modActi,
			List<String> modTActi, List<String> modInhib,
			List<String> modTInhib, List<String> modCat)
			throws RateLawNotApplicableException {

		// TODO: Verwendung von modTActi und modTInhib ?
		// TODO: Exceptions überarbeiten

		/*
		 * if (!modActi.isEmpty()) modTActi.addAll(modActi); if
		 * (!modInhib.isEmpty()) modTInhib.addAll(modInhib); if
		 * (!modE.isEmpty()) modTActi.addAll(modE); if (!modCat.isEmpty())
		 * modTActi.addAll(modCat);
		 * 
		 * // Exceptions for (ModifierSpeciesReference modifier :
		 * r.getListOfModifiers()) { if
		 * (SBO.isGene(r.getReactant(0).getSpeciesInstance().getSBOTerm()) &&
		 * (SBO.isTranslationalActivation(modifier.getSBOTerm()) || SBO
		 * .isTranslationalInhibitor(modifier.getSBOTerm()))) throw new
		 * ModificationException( "Wrong activation in reaction " + r.getId() +
		 * ". Only transcriptional modification is allowed here."); else if
		 * ((SBO.isMessengerRNA(r.getReactant(0).getSpeciesInstance()
		 * .getSBOTerm()) || SBO.isRNA(r.getReactant(0)
		 * .getSpeciesInstance().getSBOTerm())) &&
		 * (SBO.isTranscriptionalActivation(modifier.getSBOTerm()) || SBO
		 * .isTranscriptionalInhibitor(modifier.getSBOTerm()))) throw new
		 * ModificationException("Wrong activation in reaction " + r.getId() +
		 * ". Only translational modification is allowed here."); }
		 */

		Reaction r = getParentSBMLObject();
		ASTNode kineticLaw = new ASTNode(this);
		ASTNode kineticLawPart = new ASTNode(this);
		String rId = getParentSBMLObject().getId();

		Parameter a = createOrGetParameter("a_", rId, underscore);
		Parameter b = createOrGetParameter("b_", rId, underscore);
		Parameter c = createOrGetParameter("c_", rId, underscore);
		Parameter d = createOrGetParameter("d_", rId, underscore);

		Species product = r.getProduct(0).getSpeciesInstance();
		ASTNode productnode = speciesTerm(product);

		// Transkription
		if (SBO.isTranscription(r.getSBOTerm())) {

			// System.out.println("Das ist eine Transkription! SBOTerm: "
			// + r.getSBOTerm());
			for (int modifierNum = 0; modifierNum < r.getNumModifiers(); modifierNum++) {
				Species modifierspec = r.getModifier(modifierNum)
						.getSpeciesInstance();
				ModifierSpeciesReference modifier = r.getModifier(modifierNum);

				if (SBO.isProtein(modifierspec.getSBOTerm())) {

					// System.out.println("Modifier " + modifierspec
					// + " ist ein Protein! SBOTerm: "
					// + modifierspec.getSBOTerm());
					Parameter e = null;

					if (SBO.isStimulator(modifier.getSBOTerm())) {
						// System.out.println("Modifier " + modifier
						// + " wirkt als Aktivator! SBOTerm: "
						// + modifier.getSBOTerm());
						e = createOrGetParameter("e_", modifierNum, underscore,
								rId, underscore, "pos");
						// System.out.println("Parameter " + e.toString()
						// + " hat positives Vorzeichen! value 1");
					} else if (SBO.isInhibitor(modifier.getSBOTerm())) {
						// System.out.println("Modifier " + modifier
						// + " wirkt als Inhibitor! SBOTerm: "
						// + modifier.getSBOTerm());
						e = createOrGetParameter("e_", modifierNum, underscore,
								rId, underscore, "neg");
						// System.out.println("Parameter " + e.toString()
						// + " hat negatives Vorzeichen! set value -1");
						e.setValue(-1);
					}

					ASTNode modnode = speciesTerm(modifierspec);
					ASTNode enode = new ASTNode(e, this);
					if (kineticLawPart.isUnknown())
						kineticLawPart = ASTNode.pow(modnode, enode);
					else
						kineticLawPart = ASTNode.times(kineticLawPart, ASTNode
								.pow(modnode, enode));

					kineticLaw = ASTNode.diff(ASTNode.times(
							new ASTNode(a, this), kineticLawPart), ASTNode
							.times(new ASTNode(b, this), productnode));
				}
			}
		} else if (SBO.isTranslation(r.getSBOTerm())) {

			// System.out.println("Das ist eine Translation! SBOTerm: "
			// + r.getSBOTerm());
			for (int modifierNum = 0; modifierNum < r.getNumModifiers(); modifierNum++) {
				Species modifier = r.getModifier(modifierNum)
						.getSpeciesInstance();
				if (SBO.isRNAOrMessengerRNA(modifier.getSBOTerm())) {
					// System.out.println("Modifier " + modifier
					// + " ist eine RNA! SBOTerm: "
					// + modifier.getSBOTerm());

					ASTNode modnode = speciesTerm(modifier);

					kineticLaw = ASTNode.diff(ASTNode.times(
							new ASTNode(c, this), modnode), ASTNode.times(
							new ASTNode(d, this), productnode));
				}
			}
		}
		// System.out.println(kineticLaw.toLaTeX());
		return kineticLaw;
	}


	/* (Kein Javadoc)
	 * @see org.sbml.squeezer.kinetics.BasicKineticLaw#getSimpleName()
	 */
	public String getSimpleName() {
		return "S-System based kinetic";
	}
}