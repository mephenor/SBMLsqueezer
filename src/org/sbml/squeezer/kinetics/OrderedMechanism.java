/*
 * $Id$
 * $URL$
 * ---------------------------------------------------------------------
 * This file is part of SBMLsqueezer, a Java program that creates rate 
 * equations for reactions in SBML files (http://sbml.org).
 *
 * Copyright (C) 2006-2012 by the University of Tuebingen, Germany.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 * ---------------------------------------------------------------------
 */
package org.sbml.squeezer.kinetics;

import java.text.MessageFormat;
import java.util.List;

import org.sbml.jsbml.ASTNode;
import org.sbml.jsbml.LocalParameter;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.SpeciesReference;
import org.sbml.squeezer.RateLawNotApplicableException;
import org.sbml.squeezer.util.Bundles;
import org.sbml.squeezer.util.SBMLtools;

/**
 * Rate law for the bi-uni or bi-bi ordered mechanism.
 * 
 * @author Nadine Hassis
 * @author Andreas Dr&auml;ger
 * @author Sarah R. M&uuml;ller vom Hagen
 * @date Aug 1, 2007
 * @since 1.0
 * @version $Rev$
 */
public class OrderedMechanism extends GeneralizedMassAction implements
		InterfaceBiUniKinetics, InterfaceBiBiKinetics,
		InterfaceReversibleKinetics, InterfaceIrreversibleKinetics,
		InterfaceModulatedKinetics {

	/**
	 * Generated serial version identifier.
	 */
	private static final long serialVersionUID = -258935283201640001L;

	/**
	 * 
	 * @param parentReaction
	 * @param typeParameters
	 * @throws RateLawNotApplicableException
	 */
	public OrderedMechanism(Reaction parentReaction, Object... typeParameters)
			throws RateLawNotApplicableException {
		super(parentReaction, typeParameters);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.sbml.squeezer.kinetics.BasicKineticLaw#createKineticEquation(java
	 * .util.List, java.util.List, java.util.List, java.util.List)
	 */
	ASTNode createKineticEquation(List<String> modE, List<String> modActi,
			List<String> modInhib, List<String> modCat)
			throws RateLawNotApplicableException {
		Reaction reaction = getParentSBMLObject();
		SBMLtools.setSBOTerm(this,429);
		double stoichiometryRight = 0;
		for (int i = 0; i < reaction.getNumProducts(); i++)
			stoichiometryRight += reaction.getProduct(i).getStoichiometry();
		// compulsory-order ternary-complex mechanism (Cornish-Bowden)
		if ((reaction.getNumProducts() == 2) && (stoichiometryRight == 2))
			SBMLtools.setSBOTerm(this,433);
		else if ((reaction.getNumProducts() == 1) && (stoichiometryRight == 1))
			SBMLtools.setSBOTerm(this,434);

		// according to Cornish-Bowden: Fundamentals of Enzyme kinetics
		String numProd = "";
		if ((reaction.getNumProducts() == 2) && (stoichiometryRight == 2))
			numProd = ", " + Bundles.MESSAGES.getString("TWO_PRODUCTS");
		else if ((reaction.getNumProducts() == 1) && (stoichiometryRight == 1))
			numProd = ", " + Bundles.MESSAGES.getString("ONE_PRODUCT");

		setNotes(MessageFormat.format(
				Bundles.MESSAGES.getString("COMPULSORY_ORDER_TERNARY_COMPLEY_MEACHANISM"),
				(!reaction.getReversible() ? Bundles.MESSAGES.getString("IRREVERSIBLE")
						: Bundles.MESSAGES.getString("REVERSIBLE")),
				numProd));

		ASTNode numerator;// I
		ASTNode denominator; // II
		ASTNode catalysts[] = new ASTNode[Math.max(1, modE.size())];

		SpeciesReference specRefE1 = reaction.getReactant(0), specRefE2 = null;
		SpeciesReference specRefP1 = reaction.getProduct(0), specRefP2 = null;

		if (reaction.getNumReactants() == 2)
			specRefE2 = reaction.getReactant(1);
		else if (specRefE1.getStoichiometry() == 2f)
			specRefE2 = specRefE1;
		else
			throw new RateLawNotApplicableException(
					MessageFormat.format(Bundles.WARNINGS.getString("ORDERED_NUM_OF_REACTANTS_MUST_EQUAL"), reaction.getId()));

		boolean exception = false, biuni = false;
		switch (reaction.getNumProducts()) {
		case 1:
			if (specRefP1.getStoichiometry() == 1f)
				biuni = true;
			else if (specRefP1.getStoichiometry() == 2f)
				specRefP2 = specRefP1;
			else
				exception = true;
			break;
		case 2:
			specRefP2 = reaction.getProduct(1);
			break;
		default:
			exception = true;
			break;
		}
		if (exception && reaction.getReversible())
			throw new RateLawNotApplicableException(
					MessageFormat.format(
							Bundles.WARNINGS.getString("ORDERED_NUM_OF_PRODUCTS_MUST_EQUAL"),
							reaction.getId()));

		int enzymeNum = 0;
		do {
			/*
			 * Variables that are needed for the different combinations of
			 * reactants and prodcuts.
			 */
			String enzyme = modE.size() == 0 ? null : modE.get(enzymeNum);
			LocalParameter p_kcatp = parameterFactory.parameterKcatOrVmax(
					enzyme, true);

			/*
			 * Irreversible reaction (bi-bi or bi-uni does not matter)
			 */
			if (!reaction.getReversible()) {
				LocalParameter p_kMr1 = parameterFactory.parameterMichaelis(
						specRefE1.getSpecies(), enzyme, true);
				LocalParameter p_kMr2 = parameterFactory.parameterMichaelis(
						specRefE2.getSpecies(), enzyme, true);
				LocalParameter p_kIr1 = parameterFactory.parameterKi(specRefE1
						.getSpecies(), enzyme);

				numerator = new ASTNode(p_kcatp, this);
				if (modE.size() > 0)
					numerator.multiplyWith(speciesTerm(enzyme));
				numerator = ASTNode.times(numerator, ASTNode.pow(
						speciesTerm(specRefE1), new ASTNode(specRefE1
								.getStoichiometry(), this)));
				denominator = ASTNode.times(this, p_kIr1, p_kMr2);

				if (specRefE2.equals(specRefE1)) {
					denominator = ASTNode.sum(denominator, ASTNode.times(
							ASTNode.sum(this, p_kMr1, p_kMr2),
							speciesTerm(specRefE1)), ASTNode.pow(
							speciesTerm(specRefE1), new ASTNode(2, this)));
				} else {
					numerator = ASTNode.times(numerator, ASTNode.pow(
							speciesTerm(specRefE2), new ASTNode(specRefE2
									.getStoichiometry(), this)));
					denominator = ASTNode.sum(denominator, ASTNode.times(
							new ASTNode(p_kMr2, this), speciesTerm(specRefE1)),
							ASTNode.times(new ASTNode(p_kMr1, this),
									speciesTerm(specRefE2)), ASTNode.times(
									speciesTerm(specRefE1),
									speciesTerm(specRefE2)));
				}
			} else if (!biuni) {
				/*
				 * Reversible Bi-Bi reaction.
				 */
				SBMLtools.setSBOTerm(this, 433);
				LocalParameter p_kIr2 = parameterFactory.parameterKi(specRefE2
						.getSpecies(), enzyme);
				LocalParameter p_kcatn = parameterFactory.parameterKcatOrVmax(
						enzyme, false);
				LocalParameter p_kMr1 = parameterFactory.parameterMichaelis(
						specRefE1.getSpecies(), enzyme, true);
				LocalParameter p_kMr2 = parameterFactory.parameterMichaelis(
						specRefE2.getSpecies(), enzyme, true);
				LocalParameter p_kMp1 = parameterFactory.parameterMichaelis(
						specRefP1.getSpecies(), enzyme, false);
				LocalParameter p_kMp2 = parameterFactory.parameterMichaelis(
						specRefP2.getSpecies(), enzyme, false);
				LocalParameter p_kIr1 = parameterFactory.parameterKi(specRefE1
						.getSpecies(), enzyme);
				LocalParameter p_kIp1 = parameterFactory.parameterKi(specRefP1
						.getSpecies(), enzyme);
				LocalParameter p_kIp2 = parameterFactory.parameterKi(specRefP2
						.getSpecies(), enzyme);
				ASTNode numeratorForward = ASTNode.frac(new ASTNode(p_kcatp,
						this), ASTNode.times(this, p_kIr1, p_kMr2));
				ASTNode numeratorReverse = ASTNode.frac(new ASTNode(p_kcatn,
						this), ASTNode.times(this, p_kIp2, p_kMp1));

				if (modE.size() > 0)
					numeratorForward.multiplyWith(speciesTerm(enzyme));

				denominator = ASTNode.sum(new ASTNode(1, this), ASTNode.frac(
						speciesTerm(specRefE1), new ASTNode(p_kIr1, this)),
						ASTNode.frac(ASTNode.times(new ASTNode(p_kMr1, this),
								speciesTerm(specRefE2)), ASTNode.times(this,
								p_kIr1, p_kMr2)), ASTNode.frac(ASTNode.times(
								new ASTNode(p_kMp2, this),
								speciesTerm(specRefP1)), ASTNode.times(this,
								p_kIp2, p_kMp1)), ASTNode.frac(
								speciesTerm(specRefP2), new ASTNode(p_kIp2,
										this)));

				if (specRefE2.equals(specRefE1)) {
					numeratorForward = ASTNode.times(numeratorForward, ASTNode
							.pow(speciesTerm(specRefE1), 2));
					denominator = ASTNode.sum(denominator, ASTNode.frac(ASTNode
							.pow(speciesTerm(specRefE1), 2), ASTNode.times(
							this, p_kIr1, p_kMr2)));
				} else {
					numeratorForward = ASTNode.times(numeratorForward,
							speciesTerm(specRefE1), speciesTerm(specRefE2));
					denominator = ASTNode.sum(denominator, ASTNode.frac(ASTNode
							.times(speciesTerm(specRefE1),
									speciesTerm(specRefE2)), ASTNode.times(
							this, p_kIr1, p_kMr2)));
				}

				if (modE.size() > 0)
					numeratorReverse.multiplyWith(speciesTerm(enzyme));

				if (specRefP2.equals(specRefP1))
					numeratorReverse = ASTNode.times(numeratorReverse, ASTNode
							.pow(speciesTerm(specRefP1), 2));
				else
					numeratorReverse = ASTNode.times(numeratorReverse, ASTNode
							.times(speciesTerm(specRefP1),
									speciesTerm(specRefP2)));
				numerator = ASTNode.diff(numeratorForward, numeratorReverse);

				denominator = ASTNode.sum(denominator, ASTNode
						.frac(
								ASTNode.times(new ASTNode(p_kMp2, this),
										speciesTerm(specRefE1),
										speciesTerm(specRefP1)), ASTNode.times(
										this, p_kIr1, p_kMp1, p_kIp2)), ASTNode
						.frac(
								ASTNode.times(new ASTNode(p_kMr1, this),
										speciesTerm(specRefE2),
										speciesTerm(specRefP2)), ASTNode.times(
										this, p_kIr1, p_kMr2, p_kIp2)));

				if (specRefP2.equals(specRefP1))
					denominator = ASTNode.sum(denominator, ASTNode.frac(ASTNode
							.pow(speciesTerm(specRefP1), 2), ASTNode.times(
							this, p_kMp1, p_kIp2)));
				else
					denominator = ASTNode.sum(denominator, ASTNode.frac(ASTNode
							.times(speciesTerm(specRefP1),
									speciesTerm(specRefP2)), ASTNode.times(
							this, p_kMp1, p_kIp2)));

				if (specRefE2.equals(specRefE1))
					denominator = ASTNode.sum(denominator, ASTNode.frac(ASTNode
							.times(ASTNode.pow(speciesTerm(specRefE1), 2),
									speciesTerm(specRefP1)), ASTNode.times(
							this, p_kIr1, p_kMr2, p_kIp1)));
				else
					denominator = ASTNode.sum(denominator, ASTNode.frac(ASTNode
							.times(speciesTerm(specRefE1),
									speciesTerm(specRefE2),
									speciesTerm(specRefP1)), ASTNode.times(
							this, p_kIr1, p_kMr2, p_kIp1)));

				if (specRefP2.equals(specRefP1))
					denominator = ASTNode.sum(denominator, ASTNode.frac(ASTNode
							.times(speciesTerm(specRefE2), ASTNode.pow(
									speciesTerm(specRefP1), 2)), ASTNode.times(
							this, p_kIr2, p_kMp1, p_kIp2)));
				else
					denominator = ASTNode.sum(denominator, ASTNode.frac(ASTNode
							.times(speciesTerm(specRefE2), ASTNode.times(
									speciesTerm(specRefP1),
									speciesTerm(specRefP2))), ASTNode.times(
							this, p_kIr2, p_kMp1, p_kIp2)));
			} else {
				/*
				 * Reversible bi-uni reaction
				 */
				SBMLtools.setSBOTerm(this,434);
				LocalParameter p_kcatn = parameterFactory.parameterKcatOrVmax(
						enzyme, false);
				LocalParameter p_kMr1 = parameterFactory.parameterMichaelis(
						specRefE1.getSpecies(), enzyme, true);
				LocalParameter p_kMr2 = parameterFactory.parameterMichaelis(
						specRefE2.getSpecies(), enzyme, true);
				LocalParameter p_kMp1 = parameterFactory.parameterMichaelis(
						specRefP1.getSpecies(), enzyme, false);
				LocalParameter p_kIr1 = parameterFactory.parameterKi(specRefE1
						.getSpecies(), enzyme);
				LocalParameter p_kIp1 = parameterFactory.parameterKi(specRefP1
						.getSpecies(), enzyme);

				ASTNode numeratorForward = ASTNode.frac(new ASTNode(p_kcatp,
						this), ASTNode.times(this, p_kIr1, p_kMr2));
				ASTNode numeratorReverse = ASTNode.frac(this, p_kcatn, p_kMp1);

				if (modE.size() > 0)
					numeratorForward.multiplyWith(speciesTerm(enzyme));

				// numeratorForward = times(numeratorForward, specRefE1
				// .getSpeciesInstance());

				denominator = ASTNode.sum(new ASTNode(1, this), ASTNode.frac(
						speciesTerm(specRefE1), new ASTNode(p_kIr1, this)),
						ASTNode.frac(ASTNode.times(new ASTNode(p_kMr1, this),
								speciesTerm(specRefE2)), ASTNode.times(this,
								p_kIr1, p_kMr2)));

				if (specRefE2.equals(specRefE1)) {
					numeratorForward = ASTNode.times(numeratorForward, ASTNode
							.pow(speciesTerm(specRefE1), 2));
					denominator = ASTNode.sum(denominator, ASTNode.frac(ASTNode
							.pow(speciesTerm(specRefE1), 2), ASTNode.times(
							this, p_kIr1, p_kMr2)));
				} else {
					numeratorForward = ASTNode.times(numeratorForward, ASTNode
							.times(speciesTerm(specRefE1),
									speciesTerm(specRefE2)));
					denominator = ASTNode.sum(denominator, ASTNode.frac(ASTNode
							.times(speciesTerm(specRefE1),
									speciesTerm(specRefE2)), ASTNode.times(
							this, p_kIr1, p_kMr2)));
				}
				if (modE.size() > 0)
					numeratorReverse.multiplyWith(speciesTerm(enzyme));
				numeratorReverse = ASTNode.times(numeratorReverse,
						speciesTerm(specRefP1));
				numerator = ASTNode.diff(numeratorForward, numeratorReverse);
				denominator.plus(
						ASTNode.frac(
								ASTNode.times(new ASTNode(p_kMr1, this),
										speciesTerm(specRefE2),
										speciesTerm(specRefP1)), ASTNode.times(
										this, p_kIr1, p_kMr2, p_kIp1))).plus(
						speciesTerm(specRefP1).divideBy(p_kMp1));
			}

			/*
			 * Construct formula
			 */
			catalysts[enzymeNum++] = ASTNode.frac(numerator, denominator);
		} while (enzymeNum < modE.size());
		return ASTNode.times(activationFactor(modActi),
				inhibitionFactor(modInhib), ASTNode.sum(catalysts));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.squeezer.kinetics.GeneralizedMassAction#getSimpleName()
	 */
	public String getSimpleName() {
		return Bundles.MESSAGES.getString("ORDERED_MECHANISM_SIMPLE_NAME");
	}
}
