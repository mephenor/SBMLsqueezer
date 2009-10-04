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
package org.sbml.squeezer;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.sbml.jsbml.ModifierSpeciesReference;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.SBO;
import org.sbml.jsbml.Species;
import org.sbml.jsbml.SpeciesReference;

/**
 * @author Andreas Dr&auml;ger <a
 *         href="mailto:andreas.draeger@uni-tuebingen.de">
 *         andreas.draeger@uni-tuebingen.de</a>
 * @date 2009-10-01
 * @since 1.3
 */
public class ReactionType {

	/**
	 * Set of those kinetic equations that can only be reversible.
	 */
	private static Set<String> notIrreversible;

	private static Set<String> notReversible;

	static {
		notIrreversible = new HashSet<String>();
		notIrreversible.addAll(SBMLsqueezer.getKineticsReversible());
		notIrreversible.removeAll(SBMLsqueezer.getKineticsIrreversible());
		notReversible = new HashSet<String>();
		notReversible.addAll(SBMLsqueezer.getKineticsIrreversible());
		notReversible.removeAll(SBMLsqueezer.getKineticsReversible());
	}

	/**
	 * Checks if the given set of kinetics can be used given the property if all
	 * reactions should be treated reversibly.
	 * 
	 * @param treatReactionsReversible
	 * @param allKinetics
	 * @return A set of kinetics that can be used given the reversible property.
	 */
	private static Set<String> checkReactions(boolean treatReactionsReversible,
			Set<String> allKinetics) {
		Set<String> kinetics = new HashSet<String>();
		kinetics.addAll(allKinetics);
		kinetics.removeAll(notReversible);
		if (!treatReactionsReversible)
			kinetics.removeAll(notIrreversible);
		/*
		 * else kinetics.retainAll(notIrreversible);
		 */
		return kinetics;
	}

	/**
	 * 
	 * @param treatReactionsReversible
	 * @return
	 */
	public static Set<String> getKineticsArbitraryEnzyme(
			boolean treatReactionsReversible) {
		return checkReactions(treatReactionsReversible, SBMLsqueezer
				.getKineticsArbitraryEnzymeMechanism());
	}

	/**
	 * 
	 * @param treatReactionsReversible
	 * @return
	 */
	public static Set<String> getKineticsBiBi(boolean treatReactionsReversible) {
		return checkReactions(treatReactionsReversible, SBMLsqueezer
				.getKineticsBiBi());
	}

	/**
	 * 
	 * @param treatReactionsReversible
	 * @return
	 */
	public static Set<String> getKineticsBiUni(boolean treatReactionsReversible) {
		return checkReactions(treatReactionsReversible, SBMLsqueezer
				.getKineticsBiUni());
	}

	/**
	 * 
	 * @param treatReactionsReversible
	 * @return
	 */
	public static Set<String> getKineticsGeneRegulation(
			boolean treatReactionsReversible) {
		return checkReactions(treatReactionsReversible, SBMLsqueezer
				.getKineticsGeneRegulatoryNetworks());
	}

	/**
	 * 
	 * @param treatReactionsReversible
	 * @return
	 */
	public static Set<String> getKineticsNonEnzyme(
			boolean treatReactionsReversible) {
		return checkReactions(treatReactionsReversible, SBMLsqueezer
				.getKineticsNonEnzyme());
	}

	/**
	 * 
	 * @param treatReactionsReversible
	 * @return
	 */
	public static Set<String> getKineticsUniUni(boolean treatReactionsReversible) {
		return checkReactions(treatReactionsReversible, SBMLsqueezer
				.getKineticsUniUni());
	}

	/**
	 * identify which Modifer is used
	 * 
	 * @param reactionNum
	 */
	public static final void identifyModifers(Reaction reaction,
			List<String> enzymes, List<String> activators,
			List<String> transActiv, List<String> inhibitors,
			List<String> transInhib, List<String> nonEnzymeCatalysts) {
		enzymes.clear();
		activators.clear();
		transActiv.clear();
		inhibitors.clear();
		transInhib.clear();
		nonEnzymeCatalysts.clear();
		int type;
		for (ModifierSpeciesReference modifier : reaction.getListOfModifiers()) {
			type = modifier.getSBOTerm();
			if (SBO.isModifier(type)) {
				// Ok, this is confusing...
				// inhibitors.add(modifier.getSpecies());
				// activators.add(modifier.getSpecies());
			} else if (SBO.isInhibitor(type))
				inhibitors.add(modifier.getSpecies());
			else if (SBO.isTranscriptionalActivation(type)
					|| SBO.isTranslationalActivation(type))
				transActiv.add(modifier.getSpecies());
			else if (SBO.isTranscriptionalInhibitor(type)
					|| SBO.isTranslationalInhibitor(type))
				transInhib.add(modifier.getSpecies());
			else if (SBO.isTrigger(type) || SBO.isStimulator(type))
				// no extra support for unknown catalysis anymore...
				// physical stimulation is now also a stimulator.
				activators.add(modifier.getSpecies());
			else if (SBO.isCatalyst(type)) {
				if (SBO.isEnzymaticCatalysis(type))
					enzymes.add(modifier.getSpecies());
				else
					nonEnzymeCatalysts.add(modifier.getSpecies());
			}
		}
	}

	private List<String> activators;

	private boolean biBi;

	private boolean biUni;

	private List<String> enzymes;

	private List<String> inhibitors;

	private boolean integerStoichiometry;

	private boolean nonEnzyme;

	private List<String> nonEnzymeCatalysts;

	private Reaction reaction;

	private boolean reactionWithGenes = false;

	private boolean reactionWithRNAs = false;

	private Properties settings;

	private double stoichiometry;

	private boolean stoichiometryIntLeft = true;

	private double stoichiometryLeft = 0d;

	private double stoichiometryRight = 0d;

	private List<String> transActiv;

	private List<String> transInhib;

	private boolean uniUni;

	private boolean withoutModulation;

	/**
	 * Analyses the given reaction for several properties.
	 * 
	 * @param r
	 *            The reaction to be analyzed.
	 * @throws RateLawNotApplicableException
	 */
	public ReactionType(Reaction r, Properties settings)
			throws RateLawNotApplicableException {
		int i;
		this.reaction = r;
		this.settings = settings;

		/*
		 * Analyze properties of the reaction
		 */

		// compute stoichiometric properties
		for (i = 0; i < reaction.getNumReactants(); i++) {
			stoichiometry = reaction.getReactant(i).getStoichiometry();
			stoichiometryLeft += stoichiometry;
			if (((int) stoichiometry) - stoichiometry != 0d)
				stoichiometryIntLeft = false;
			// Transcription or translation?
			Species species = reaction.getReactant(i).getSpeciesInstance();
			if (SBO.isGeneOrGeneCodingRegion(species.getSBOTerm()))
				reactionWithGenes = true;
			else if (SBO.isRNAOrMessengerRNA(species.getSBOTerm()))
				reactionWithRNAs = true;
		}

		// is at least one modifier a gene or RNA?
		for (ModifierSpeciesReference msr : reaction.getListOfModifiers()) {
			if (SBO.isGeneOrGeneCodingRegion(msr.getSpeciesInstance()
					.getSBOTerm())) {
				reactionWithGenes = true;
				break;
			}
			if (SBO.isRNAOrMessengerRNA(msr.getSpeciesInstance().getSBOTerm())) {
				reactionWithRNAs = true;
				break;
			}
		}

		// boolean stoichiometryIntRight = true;
		for (i = 0; i < reaction.getNumProducts(); i++) {
			stoichiometry = reaction.getProduct(i).getStoichiometry();
			stoichiometryRight += stoichiometry;
			// if (((int) stoichiometry) - stoichiometry != 0.0)
			// stoichiometryIntRight = false;
		}

		// identify types of modifiers
		nonEnzymeCatalysts = new LinkedList<String>();
		inhibitors = new LinkedList<String>();
		activators = new LinkedList<String>();
		transActiv = new LinkedList<String>();
		transInhib = new LinkedList<String>();
		enzymes = new LinkedList<String>();
		if (reaction.getNumModifiers() > 0)
			identifyModifers(reaction, enzymes, activators, transActiv,
					inhibitors, transInhib, nonEnzymeCatalysts);
		nonEnzyme = ((!((Boolean) this.settings
				.get(CfgKeys.OPT_ALL_REACTIONS_ARE_ENZYME_CATALYZED))
				.booleanValue() && enzymes.size() == 0)
				|| (nonEnzymeCatalysts.size() > 0)
				|| reaction.getNumProducts() == 0 || (SBO.isEmptySet(reaction
				.getProduct(0).getSpeciesInstance().getSBOTerm())));
		uniUni = stoichiometryLeft == 1d && stoichiometryRight == 1d;
		biUni = stoichiometryLeft == 2d && stoichiometryRight == 1d;
		biBi = stoichiometryLeft == 2d && stoichiometryRight == 2d;
		integerStoichiometry = stoichiometryIntLeft;
		withoutModulation = inhibitors.size() == 0 && activators.size() == 0
				&& transActiv.size() == 0 && transInhib.size() == 0;

		/*
		 * Check if this reaction makes sense at all.
		 */
		if (reactionWithGenes) {
			boolean transcription = false;
			for (i = 0; i < reaction.getNumProducts(); i++) {
				Species species = reaction.getProduct(i).getSpeciesInstance();
				if (SBO.isRNA(species.getSBOTerm())
						|| SBO.isMessengerRNA(species.getSBOTerm()))
					transcription = true;
			}
			if (transcription && SBO.isTranslation(reaction.getSBOTerm()))
				throw new RateLawNotApplicableException("Reaction "
						+ reaction.getId() + " must be a transcription.");
		} else if (reactionWithRNAs) {
			boolean translation = false;
			for (i = 0; i < reaction.getNumProducts(); i++) {
				Species species = reaction.getProduct(i).getSpeciesInstance();
				if (!SBO.isProtein(species.getSBOTerm()))
					translation = true;
			}
			if (SBO.isTranscription(reaction.getSBOTerm()) && translation)
				throw new RateLawNotApplicableException("Reaction "
						+ reaction.getId() + " must be a translation.");
		}
		if (uniUni) {
			Species species = reaction.getReactant(0).getSpeciesInstance();
			if (SBO.isGeneOrGeneCodingRegion(species.getSBOTerm())) {
				if (((Boolean) settings
						.get(CfgKeys.OPT_SET_BOUNDARY_CONDITION_FOR_GENES))
						.booleanValue())
					setBoundaryCondition(species, true);
				if (SBO.isTranslation(reaction.getSBOTerm()))
					throw new RateLawNotApplicableException("Reaction "
							+ reaction.getId() + " must be a transcription.");
			} else if (SBO.isRNAOrMessengerRNA(species.getSBOTerm())) {
				if (SBO.isTranscription(reaction.getSBOTerm()))
					throw new RateLawNotApplicableException("Reaction "
							+ reaction.getId() + " must be a translation.");
			}
		}
		if ((SBO.isTranslation(reaction.getSBOTerm()) || SBO
				.isTranscription(reaction.getSBOTerm()))
				&& !(reactionWithGenes || reactionWithRNAs))
			throw new RateLawNotApplicableException("Reaction "
					+ reaction.getId() + " must be a state transition.");
	}

	/**
	 * Checks whether for this reaction the given kinetic law can be applied
	 * just based on the reversibility property (nothing else is checked).
	 * 
	 * @param reaction
	 * @param className
	 * @return
	 */
	private boolean checkReversibility(Reaction reaction, String className) {
		return (reaction.getReversible() && SBMLsqueezer
				.getKineticsReversible().contains(className))
				|| (!reaction.getReversible() && SBMLsqueezer
						.getKineticsIrreversible().contains(className));
	}

	/**
	 * @return the activators
	 */
	public List<String> getActivators() {
		return activators;
	}

	/**
	 * @return the enzymes
	 */
	public List<String> getEnzymes() {
		return enzymes;
	}

	/**
	 * @return the inhibitors
	 */
	public List<String> getInhibitors() {
		return inhibitors;
	}

	/**
	 * @return the nonEnzymeCatalysts
	 */
	public List<String> getNonEnzymeCatalysts() {
		return nonEnzymeCatalysts;
	}

	/**
	 * @return the reaction
	 */
	public Reaction getReaction() {
		return reaction;
	}

	/**
	 * @return the settings
	 */
	public Properties getSettings() {
		return settings;
	}

	/**
	 * @return the stoichiometry
	 */
	public double getStoichiometry() {
		return stoichiometry;
	}

	/**
	 * @return the stoichiometryLeft
	 */
	public double getStoichiometryLeft() {
		return stoichiometryLeft;
	}

	/**
	 * @return the stoichiometryRight
	 */
	public double getStoichiometryRight() {
		return stoichiometryRight;
	}

	/**
	 * @return the transActiv
	 */
	public List<String> getTransActivators() {
		return transActiv;
	}

	/**
	 * @return the transInhib
	 */
	public List<String> getTransInhibitors() {
		return transInhib;
	}

	/**
	 * identify the reactionType for generating the kinetics
	 * 
	 */
	public String identifyPossibleKineticLaw() {
		if (reaction.getNumReactants() == 0)
			for (String kin : SBMLsqueezer.getKineticsZeroReactants())
				return kin;
		if (reaction.getReversible() && reaction.getNumProducts() == 0)
			for (String kin : SBMLsqueezer.getKineticsZeroProducts())
				return kin;

		boolean reversibility = ((Boolean) settings
				.get(CfgKeys.OPT_TREAT_ALL_REACTIONS_REVERSIBLE))
				.booleanValue();
		boolean enzymeCatalyzed = ((Boolean) settings
				.get(CfgKeys.OPT_ALL_REACTIONS_ARE_ENZYME_CATALYZED))
				.booleanValue()
				|| enzymes.size() > 0;
		Object whichkin = settings.get(CfgKeys.KINETICS_NONE_ENZYME_REACTIONS);

		if (enzymeCatalyzed)
			whichkin = settings.get(CfgKeys.KINETICS_OTHER_ENZYME_REACTIONS);
		if (stoichiometryLeft == 1d) {
			SpeciesReference specref = reaction.getReactant(0);
			Species reactant = specref.getSpeciesInstance();
			if (stoichiometryRight == 1d) {
				Species product = specref.getSpeciesInstance();
				if (reactionWithGenes
						|| reactionWithRNAs
						|| (SBO.isEmptySet(reactant.getSBOTerm()) && (SBO
								.isProtein(product.getSBOTerm()) || SBO
								.isRNAOrMessengerRNA(product.getSBOTerm()))))
					whichkin = settings.get(CfgKeys.KINETICS_GENE_REGULATION);
			}
			if (enzymes.size() > 0
					|| enzymeCatalyzed
					&& (stoichiometryRight == 1d || (!reaction.getReversible() && !reversibility)))
				whichkin = settings.get(CfgKeys.KINETICS_UNI_UNI_TYPE);
		} else if (biUni && enzymeCatalyzed) {
			whichkin = settings.get(CfgKeys.KINETICS_BI_UNI_TYPE);
		} else if (biBi && enzymeCatalyzed) {
			whichkin = settings.get(CfgKeys.KINETICS_BI_BI_TYPE);
		}
		return whichkin.toString();
	}

	/**
	 * <ul>
	 * <li>1 = generalized mass action kinetics</li>
	 * <li>2 = Convenience kinetics</li>
	 * <li>3 = Michaelis-Menten kinetics</li>
	 * <li>4 = Random Order ternary kinetics</li>
	 * <li>5 = Ping-Pong</li>
	 * <li>6 = Ordered</li>
	 * <li>7 = Hill equation</li>
	 * <li>8 = Irreversible non-modulated non-interacting enzyme kinetics</li>
	 * <li>9 = Zeroth order forward mass action kinetics</li>
	 * <li>10 = Zeroth order reverse mass action kinetics</li>
	 * <li>11 = Competitive non-exclusive, non-cooperative inihibition</li>
	 * </ul>
	 * 
	 * @return Returns a sorted array of possible kinetic equations for the
	 *         given reaction in the model.
	 * @throws RateLawNotApplicableException
	 */
	public String[] identifyPossibleKineticLaws() {
		Set<String> types = new HashSet<String>();

		if (reaction.getNumReactants() == 0
				|| (reaction.getNumProducts() == 0 && reaction.getReversible())) {
			/*
			 * Special case that occurs if we have at least one emty list of
			 * species references.
			 */
			for (String className : SBMLsqueezer.getKineticsZeroReactants())
				types.add(className);
			for (String className : SBMLsqueezer.getKineticsZeroProducts()) {
				if (SBMLsqueezer.getKineticsReversible().contains(className))
					types.add(className);
			}

		} else {
			if (nonEnzyme) {
				// non enzyme reactions
				for (String className : SBMLsqueezer.getKineticsNonEnzyme())
					types.add(className);
			} else {
				/*
				 * Enzym-Kinetics: Assign possible rate laws for arbitrary
				 * enzyme reations.
				 */
				for (String className : SBMLsqueezer
						.getKineticsArbitraryEnzymeMechanism()) {
					if (checkReversibility(reaction, className)
							&& (!SBMLsqueezer.getKineticsIntStoichiometry()
									.contains(className) || integerStoichiometry)
							&& (SBMLsqueezer.getKineticsModulated().contains(
									className) || withoutModulation))
						types.add(className);
				}
				if (uniUni) {
					Set<String> onlyUniUni = new HashSet<String>();
					onlyUniUni.addAll(SBMLsqueezer.getKineticsUniUni());
					onlyUniUni.removeAll(SBMLsqueezer
							.getKineticsArbitraryEnzymeMechanism());
					if (!integerStoichiometry)
						onlyUniUni.removeAll(SBMLsqueezer
								.getKineticsIntStoichiometry());
					if (!withoutModulation)
						onlyUniUni.retainAll(SBMLsqueezer
								.getKineticsModulated());
					for (String className : onlyUniUni)
						if (checkReversibility(reaction, className))
							types.add(className);
				} else if (biUni) {
					Set<String> onlyBiUni = new HashSet<String>();
					onlyBiUni.addAll(SBMLsqueezer.getKineticsBiUni());
					onlyBiUni.removeAll(SBMLsqueezer
							.getKineticsArbitraryEnzymeMechanism());
					if (!integerStoichiometry)
						onlyBiUni.removeAll(SBMLsqueezer
								.getKineticsIntStoichiometry());
					if (!withoutModulation)
						onlyBiUni
								.retainAll(SBMLsqueezer.getKineticsModulated());
					for (String className : onlyBiUni)
						if (checkReversibility(reaction, className))
							types.add(className);
				} else if (biBi) {
					Set<String> onlyBiBi = new HashSet<String>();
					onlyBiBi.addAll(SBMLsqueezer.getKineticsBiBi());
					onlyBiBi.removeAll(SBMLsqueezer
							.getKineticsArbitraryEnzymeMechanism());
					if (!withoutModulation)
						onlyBiBi.retainAll(SBMLsqueezer.getKineticsModulated());
					for (String className : onlyBiBi)
						if (checkReversibility(reaction, className))
							types.add(className);
				}
			}

			/*
			 * Gene regulation
			 */
			if (uniUni) {
				Species reactant = reaction.getReactant(0).getSpeciesInstance();
				Species product = reaction.getProduct(0).getSpeciesInstance();
				if (SBO.isGeneOrGeneCodingRegion(reactant.getSBOTerm())
						|| (SBO.isEmptySet(reactant.getSBOTerm()) && (SBO
								.isRNAOrMessengerRNA(product.getSBOTerm()) || SBO
								.isProtein(product.getSBOTerm()))))
					for (String className : SBMLsqueezer
							.getKineticsGeneRegulatoryNetworks())
						types.add(className);
			}
		}
		String t[] = types.toArray(new String[] {});
		Arrays.sort(t);
		return t;
	}

	/**
	 * @return the biBi
	 */
	public boolean isBiBi() {
		return biBi;
	}

	/**
	 * @return the biUni
	 */
	public boolean isBiUni() {
		return biUni;
	}

	/**
	 * Returns true only if this reaction should be considered enzyme-catalyzed
	 * (independend of any settings, based on the SBO annotation of the current
	 * model).
	 * 
	 * @param reaction
	 * @return
	 */
	public boolean isEnzymeReaction() {
		return (enzymes.size() > 0 && nonEnzymeCatalysts.size() == 0);
	}

	/**
	 * @return the integerStoichiometry
	 */
	public boolean isIntegerStoichiometry() {
		return integerStoichiometry;
	}

	/**
	 * @return the nonEnzyme
	 */
	public boolean isNonEnzyme() {
		return nonEnzyme;
	}

	/**
	 * Returns true only if given the current settings this reaction cannot be
	 * considered enzyme-catalyzed.
	 * 
	 * @param reaction
	 * @return
	 */
	public boolean isNonEnzymeReaction() {
		return enzymes.size() == 0 && nonEnzymeCatalysts.size() > 0;
	}

	/**
	 * @return the reactionWithGenes
	 */
	public boolean isReactionWithGenes() {
		return reactionWithGenes;
	}

	/**
	 * @return the reactionWithRNAs
	 */
	public boolean isReactionWithRNAs() {
		return reactionWithRNAs;
	}

	/**
	 * @return the stoichiometryIntLeft
	 */
	public boolean isStoichiometryIntLeft() {
		return stoichiometryIntLeft;
	}

	/**
	 * @return the uniUni
	 */
	public boolean isUniUni() {
		return uniUni;
	}

	/**
	 * @return the withoutModulation
	 */
	public boolean isWithoutModulation() {
		return withoutModulation;
	}

	/**
	 * set the boundaryCondition for a gen to the given value
	 * 
	 * @param species
	 * @param condition
	 */
	public void setBoundaryCondition(Species species, boolean condition) {
		if (condition != species.getBoundaryCondition()) {
			species.setBoundaryCondition(condition);
			species.stateChanged();
		}
	}

}
