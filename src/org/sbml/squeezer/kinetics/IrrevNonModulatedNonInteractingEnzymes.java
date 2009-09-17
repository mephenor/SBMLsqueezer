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

import java.util.IllegalFormatException;
import java.util.List;

import org.sbml.jsbml.ASTNode;
import org.sbml.jsbml.Parameter;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.SpeciesReference;
import org.sbml.squeezer.RateLawNotApplicableException;

/**
 * This class implements SBO:0000150 and all of its special cases. It is an
 * kinetic law of an irreversible enzyme reaction, which is not modulated, and
 * in which all reacting species do not interact: Kinetics of enzymes that react
 * with one or several substances, their substrates, that bind independently.
 * The enzymes do not catalyse the reactions in both directions.
 * 
 * @since 1.0
 * @version
 * @author <a href="mailto:andreas.draeger@uni-tuebingen.de">Andreas
 *         Dr&auml;ger</a>
 * @date Feb 6, 2008
 */
public class IrrevNonModulatedNonInteractingEnzymes extends BasicKineticLaw {

	private int numOfEnzymes;

	/**
	 * 
	 * @param parentReaction
	 * @throws RateLawNotApplicableException
	 * @throws IllegalFormatException
	 */
	public IrrevNonModulatedNonInteractingEnzymes(Reaction parentReaction)
			throws RateLawNotApplicableException, IllegalFormatException {
		super(parentReaction);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.sbml.squeezer.kinetics.BasicKineticLaw#createKineticEquation(java
	 * .util.List, java.util.List, java.util.List, java.util.List,
	 * java.util.List, java.util.List)
	 */
	// @Override
	ASTNode createKineticEquation(List<String> modE, List<String> modActi,
			List<String> modTActi, List<String> modInhib,
			List<String> modTInhib, List<String> modCat)
			throws RateLawNotApplicableException, IllegalFormatException {
		if ((modActi.size() > 0) || (modInhib.size() > 0)
				|| (modTActi.size() > 0) || (modTInhib.size() > 0))
			throw new RateLawNotApplicableException(
					"This rate law can only be applied to non-modulated reactions.");
		if ((modCat.size() > 0))
			throw new RateLawNotApplicableException(
					"This rate law can only be applied to enzyme-catalyzed reactions.");
		if (getParentSBMLObject().getReversible())
			throw new RateLawNotApplicableException(
					"This rate law can only be applied to irreversible reactions.");
		numOfEnzymes = modE.size();
		Reaction reaction = getParentSBMLObject();
		ASTNode enzymes[] = new ASTNode[Math.max(1, modE.size())];
		for (int enzymeNum = 0; enzymeNum < enzymes.length; enzymeNum++) {
			StringBuffer kcat = (modE.size() == 0) ? concat("V_", reaction
					.getId()) : concat("kcat_", reaction.getId());
			if (modE.size() > 1)
				append(kcat, underscore, modE.get(enzymeNum));
			Parameter p_kcat = createOrGetParameter(kcat.toString());
			p_kcat.setSBOTerm(modE.size() == 0 ? 324 : 320);
			ASTNode numerator = new ASTNode(p_kcat, this);

			ASTNode[] denominator = new ASTNode[reaction.getNumReactants()];
			for (int i = 0; i < reaction.getNumReactants(); i++) {
				SpeciesReference si = reaction.getReactant(i);
				if (((int) si.getStoichiometry()) - si.getStoichiometry() != 0)
					throw new RateLawNotApplicableException(
							"This rate law can only be applied if all reactants have integer stoichiometries.");
				StringBuffer kM = concat("kM_", reaction.getId());
				if (modE.size() > 1)
					append(kM, underscore, modE.get(enzymeNum));
				append(kM, underscore, si.getSpecies());
				Parameter p_kM = createOrGetParameter(kM.toString());
				p_kM.setSBOTerm(322);
				ASTNode frac = ASTNode
						.frac(this, si.getSpeciesInstance(), p_kM);
				numerator = ASTNode.times(numerator, ASTNode.pow(ASTNode.frac(
						this, si.getSpeciesInstance(), p_kM), si
						.getStoichiometry()));
				denominator[i] = ASTNode.pow(ASTNode.sum(new ASTNode(1, this),
						frac), new ASTNode(si.getStoichiometry(), this));
			}

			if (modE.size() >= 1)
				numerator = ASTNode.times(
						new ASTNode(modE.get(enzymeNum), this), numerator);
			enzymes[enzymeNum] = ASTNode.frac(numerator, ASTNode
					.times(denominator));
		}

		double stoichiometry = 0;
		for (int i = 0; i < getParentSBMLObject().getNumReactants(); i++)
			stoichiometry += getParentSBMLObject().getReactant(i)
					.getStoichiometry();
		switch ((int) Math.round(stoichiometry)) {
		case 1:
			setSBOTerm(numOfEnzymes == 0 ? 199 : 29);
		case 2:
			setSBOTerm(151);
		case 3:
			setSBOTerm(152);
		default:
			setSBOTerm(150);
		}

		return ASTNode.sum(enzymes);
	}
}
