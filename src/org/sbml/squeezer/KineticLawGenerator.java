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
package org.sbml.squeezer;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;

import org.sbml.jsbml.AssignmentRule;
import org.sbml.jsbml.Compartment;
import org.sbml.jsbml.Constraint;
import org.sbml.jsbml.Event;
import org.sbml.jsbml.EventAssignment;
import org.sbml.jsbml.FunctionDefinition;
import org.sbml.jsbml.InitialAssignment;
import org.sbml.jsbml.KineticLaw;
import org.sbml.jsbml.ListOf;
import org.sbml.jsbml.LocalParameter;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.ModifierSpeciesReference;
import org.sbml.jsbml.Parameter;
import org.sbml.jsbml.QuantityWithUnit;
import org.sbml.jsbml.RateRule;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.Rule;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SBO;
import org.sbml.jsbml.Species;
import org.sbml.jsbml.SpeciesReference;
import org.sbml.jsbml.Unit;
import org.sbml.jsbml.UnitDefinition;
import org.sbml.squeezer.kinetics.BasicKineticLaw;
import org.sbml.squeezer.math.GaussianRank;
import org.sbml.squeezer.util.Bundles;
import org.sbml.squeezer.util.ModelChangeListener;
import org.sbml.squeezer.util.ProgressAdapter;
import org.sbml.squeezer.util.ProgressAdapter.TypeOfProgress;
import org.sbml.squeezer.util.SBMLtools;

import de.zbit.util.AbstractProgressBar;
import de.zbit.util.prefs.Option;
import de.zbit.util.prefs.SBPreferences;

/**
 * This class identifies and generates the missing kinetic laws for a the
 * selected model in the given plug-in.
 * 
 * @since 1.0
 * @version $Rev$
 * @author Nadine Hassis
 * @author Andreas Dr&auml;ger
 * @author Sarah R. M&uuml;ller vom Hagen
 * @date Aug 1, 2007
 */
public class KineticLawGenerator {
	/**
	 * The column rank of the soichiometric matrix of the original model.
	 */
	private int columnRank = -1;

	/**
	 * This list contains all fast reactions of the original model. Since the
	 * fast attribute is still ignored by SBMLsqueezer, we use this list to
	 * decide whether a warning message should be displayed later on.
	 */
	private List<Reaction> listOfFastReactions;

	/**
	 * A copy of the model that only covers all compartments, all parameters,
	 * and all reactions for which kinetic equations are to be created. Hence,
	 * it also contains all other information needed for that purpose, i.e., all
	 * species that participate as reactants, products, or modifiers in at least
	 * one of these reactions.
	 */
	private Model miniModel;

	/**
	 * 
	 */
	private Model modelOrig;

	/**
	 * A hashtable that contains all settings of how to create kinetic
	 * equations.
	 */
	private SBPreferences prefs;

	private ProgressAdapter progressAdapter = null;
	protected AbstractProgressBar progressBar = null;
	
	/**
	 * 
	 */
	private final Logger logger = Logger.getLogger(KineticLawGenerator.class.getName());

	/**
	 * Takes a model and settings for kinetic law generation as input, creates a
	 * copy of the model, updates the enzymatic catalysis information using the
	 * given settings and creates kinetic laws for each reaction in the model
	 * copy. The original model remains unchanged.
	 * 
	 * @param model
	 * @param settings
	 * @throws Throwable
	 */
	public KineticLawGenerator(Model model)
	throws Throwable {
		this.prefs = SBPreferences.getPreferencesFor(SqueezerOptions.class);
		this.modelOrig = model;
		init();
	}

	/**
	 * 
	 * @param model
	 * @param reactionID
	 * @param settings
	 */
	public KineticLawGenerator(Model model, String reactionID) {
		this.prefs = SBPreferences.getPreferencesFor(SqueezerOptions.class);
		this.modelOrig = model;
		init(reactionID);
	}

	/**
	 * 
	 * @param compartment
	 * @param modelToWrite
	 */
	private void checkUnits(Compartment compartment, Model modelToWrite) {
		Model model = compartment.getModel();
		/*
		 *  for level 2 and 3 the compartment size does not have to be set when 
		 *  the spatialDimensions field is 0.  
		 */
		double spatialD = compartment.getSpatialDimensions();
		if (!compartment.isSetSize() && 
				(spatialD != 0) || (modelToWrite.getLevel() < 2)) {
			//TODO: Option for setting the initial compartment size
			compartment.setValue(1d);
		}
    if (!compartment.isSetUnits() && (((short) spatialD) - spatialD == 0d)) {
			UnitDefinition ud;
			switch ((short) spatialD) {
			case 1:
				ud = model.getUnitDefinition(UnitDefinition.LENGTH);
				if(ud == null){
					ud = UnitDefinition.getPredefinedUnit(
							UnitDefinition.LENGTH, 2, 4);
					SBMLtools.setLevelAndVersion(ud, modelToWrite.getLevel(),
							modelToWrite.getVersion());
					modelToWrite.setLengthUnits(ud);
				}
				compartment.setUnits(model.getUnitDefinition(UnitDefinition.LENGTH));
				break;
			case 2:
				ud = model.getUnitDefinition(UnitDefinition.AREA);
				if(ud == null){
					ud = UnitDefinition.getPredefinedUnit(
							UnitDefinition.AREA, 2, 4);
					SBMLtools.setLevelAndVersion(ud, modelToWrite.getLevel(),
							modelToWrite.getVersion());
					modelToWrite.setAreaUnits(ud);
				}
				compartment.setUnits(ud);
				break;
			case 3:
				ud = model.getUnitDefinition(UnitDefinition.VOLUME);
				if (ud == null) {
					ud = UnitDefinition.getPredefinedUnit(
							UnitDefinition.VOLUME, 2, 4);
					SBMLtools.setLevelAndVersion(ud, modelToWrite.getLevel(),
							modelToWrite.getVersion());
					modelToWrite.setVolumeUnits(ud);
				}
				compartment.setUnits(ud);
				break;
			default:
				break;
			}
		}
		if (((spatialD <= 0d) || (spatialD > 3d))
				&& (1 < compartment.getLevel())) {
			compartment.setSpatialDimensions(3d);
			compartment.setUnits(model.getUnitDefinition(UnitDefinition.VOLUME));
			logger.log(Level.WARNING,
					MessageFormat.format(Bundles.WARNINGS.getString("INVALID_COMPARTMENT_DIMENSION")+"\n",
							compartment.getId()));
		}
	}

	/**
	 * check units of species and surrounding compartment.
	 * 
	 * @param species
	 */
	private void checkUnits(Species species, Model miniModel) {
		if (!species.isSetSubstanceUnits()
				|| !species.getSubstanceUnitsInstance().isVariantOfSubstance()) {
			UnitDefinition ud = species.getModel().getUnitDefinition(
					UnitDefinition.SUBSTANCE);
			if(ud == null){
				ud = UnitDefinition.getPredefinedUnit(
						UnitDefinition.SUBSTANCE, 2, 4);
				SBMLtools.setLevelAndVersion(ud, miniModel.getLevel(),
						miniModel.getVersion());
				miniModel.setSubstanceUnits(ud);
			}
			species.setSubstanceUnits(ud);
		}
	}

	/**
	 * 
	 * @param compartmenOrig
	 * @param miniModel
	 * @return
	 */
	private Compartment copyCopmpartment(Compartment compartmenOrig,
			Model miniModel) {
		if (miniModel.getCompartment(compartmenOrig.getId()) == null) {
			miniModel.addCompartment(compartmenOrig.clone());
		}
		Compartment compartment = miniModel.getCompartment(compartmenOrig
				.getId());
		if (compartment.isSetUnits()
				&& !Unit.isUnitKind(compartment.getUnits(), compartment
						.getLevel(), compartment.getVersion())) {
			if (miniModel.getUnitDefinition(compartment.getUnits()) == null) {
				UnitDefinition ud = compartmenOrig.getUnitsInstance();
				if (ud != null) {
					miniModel.addUnitDefinition(ud.clone());
				} else {
					checkUnits(compartment, miniModel);
				}
			}
			compartment.setUnits(miniModel.getUnitDefinition(compartment
					.getUnits()));
		} else {
			checkUnits(compartment, miniModel);
		}
		return compartment;
	}

	/**
	 * 
	 * @param speciesOrig
	 * @param miniModel
	 * @return
	 */
	private Species copySpecies(Species speciesOrig, Model miniModel) {
		if (miniModel.getSpecies(speciesOrig.getId()) == null) {
			miniModel.addSpecies(speciesOrig.clone());
		}
		Species spec = miniModel.getSpecies(speciesOrig.getId());
		Compartment compartment = miniModel.getCompartment(speciesOrig
				.getCompartment());
		if (compartment == null) {
			compartment = copyCopmpartment(speciesOrig.getCompartmentInstance(),
					miniModel);
		}
		spec.setCompartment(compartment);

		if (spec.isSetSubstanceUnits()
				&& !Unit.isUnitKind(spec.getUnits(), spec
						.getLevel(), spec.getVersion())) {
			if (miniModel.getUnitDefinition(spec.getUnits()) == null) {
				UnitDefinition ud = speciesOrig.getSubstanceUnitsInstance();
				if (ud != null) {
					miniModel.addUnitDefinition(ud.clone());
				} else {
					checkUnits(spec, miniModel);
				}
			}
			spec.setSubstanceUnits(miniModel.getUnitDefinition(spec
					.getUnits()));
		} else {
			checkUnits(spec, miniModel);
		}
		/*
		if (speciesOrig.isSetSubstanceUnits()
				&& !Unit.isUnitKind(speciesOrig.getUnits(), speciesOrig
						.getLevel(), speciesOrig.getVersion())) {
			if (miniModel.getUnitDefinition(speciesOrig.getSubstanceUnits()) == null) {
				miniModel.addUnitDefinition(((UnitDefinition) speciesOrig
						.getSubstanceUnitsInstance()).clone());
			}
			spec.setSubstanceUnits(miniModel.getUnitDefinition(speciesOrig
					.getSubstanceUnits()));
		} else {
			checkUnits(spec);
		}*/
		return spec;
	}

	/**
	 * Creates a kinetic law for the given reaction, which can be assigned to the
	 * given reaction.
	 * 
	 * @param r
	 *        The reaction for which a kinetic law is to be created.
	 * @param kineticsClass
	 *        The type of rate law to be used.
	 * @param reversibility
	 *        If true this reaction will be set to reversible and the kinetic
	 *        equation will be created accordingly. If this parameter is false,
	 *        the reversibility property of this reaction will not be changed.
	 * @param kinetic
	 *        an element from the Kinetics enum.
	 * @return A kinetic law for the given reaction.
	 * @throws Throwable
	 */
	public BasicKineticLaw createKineticLaw(Reaction r, Class<?> kineticsClass,
			boolean reversibility) throws Throwable {
		Reaction reaction = miniModel.getReaction(r.getId());
		if (reaction == null) {
			reaction = r;
		}
		reaction.setReversible(reversibility || reaction.getReversible());
		try {
			Object typeParameters[] = new Object[] {
					prefs.get(SqueezerOptions.TYPE_STANDARD_VERSION),
					Boolean.valueOf(hasFullColumnRank(modelOrig)),
					prefs.get(SqueezerOptions.TYPE_UNIT_CONSISTENCY),
					prefs.get(SqueezerOptions.OPT_DEFAULT_VALUE_OF_NEW_PARAMETERS) };
			Constructor<?> constructor = kineticsClass.getConstructor(reaction.getClass(), typeParameters.getClass());
			return (BasicKineticLaw) constructor.newInstance(reaction, typeParameters);
		} catch (InstantiationException e) {
			logger.warning(e.getLocalizedMessage());
			throw e.getCause();
		} catch (IllegalAccessException e) {
		  logger.warning(e.getLocalizedMessage());
			throw e.getCause();
		} catch (InvocationTargetException e) {
		  logger.warning(e.getLocalizedMessage());
			throw e.getCause();
		}
	}

	/**
	 * Creates a minimal copy of the original model that only covers those
	 * elements needed for the creation of rate equations.
	 * 
	 * @param reactionID
	 * 
	 * @return
	 */
	@SuppressWarnings("deprecation")
	private Model createMinimalModel(String reactionID) {
		boolean create = (prefs
				.getBoolean(SqueezerOptions.OPT_GENERATE_KINETIC_LAW_FOR_EACH_REACTION));
		SBMLDocument miniDoc = new SBMLDocument(modelOrig.getLevel(),
				modelOrig.getVersion());
		Model miniModel = miniDoc.createModel(modelOrig.getId());	  
		//miniModel.addChangeListener(new ModelChangeListener());

		/*
		 * Create and/or re-scale default units for time, substance and volume 
		 */	  
		// set time unit definition if it is not already set
		UnitDefinition timeUD = miniModel.getUnitDefinition(UnitDefinition.TIME);
		if(timeUD == null){
			timeUD = UnitDefinition.getPredefinedUnit(
					UnitDefinition.TIME, 2, 4);
			SBMLtools.setLevelAndVersion(timeUD, miniModel.getLevel(),
					miniModel.getVersion());
			miniModel.setTimeUnits(timeUD);
		}	
		// set substance unit definition if it is not already set
		UnitDefinition ud = miniModel.getUnitDefinition(UnitDefinition.SUBSTANCE);
		if (ud == null) {
			ud = UnitDefinition.getPredefinedUnit(UnitDefinition.SUBSTANCE, 2, 4);
			SBMLtools.setLevelAndVersion(ud, miniModel.getLevel(), miniModel.getVersion());
			miniModel.setSubstanceUnits(ud);
		}
		// TODO: Check user option whether re-scaling of units should be performed or not.
		Unit u = ud.getListOfUnits().getFirst();
		if (u.isMole()) {
			u.setScale(-3);
			ud.setName("mmole");
		}
		ud = miniModel.getUnitDefinition(UnitDefinition.VOLUME);
		if (ud != null) {
			u = ud.getListOfUnits().getFirst();
			if (u.isLitre()) {
				u.setScale(-3);
				ud.setName("ml");
			}
		}

		/*
		 * Copy needed species and reactions.
		 */
		for (Reaction reacOrig : modelOrig.getListOfReactions()) {
			/*
			 * Let us find all fast reactions. This feature is currently
			 * ignored.
			 */
			if (reacOrig.getFast()) {
				listOfFastReactions.add(reacOrig);
			}
			if (reactionID != null) {
				if (!reacOrig.getId().equals(reactionID)) {
					continue;
				} else {
					create = true;
				}
			}
			if (reacOrig.isSetKineticLaw()) {
				String formula = reacOrig.getKineticLaw().getFormula();
				if (formula.equals("") || formula.equals(" ")) {
					logger.warning(MessageFormat.format(Bundles.WARNINGS.getString("INVALID_REACTION_FORMAT") + "\n",
							reacOrig.getId()));
					create = true;
				}
			}
			if (!reacOrig.isSetKineticLaw() || create) {
				Reaction reac = new Reaction(reacOrig.getId(), reacOrig
						.getLevel(), reacOrig.getVersion());
				if (reacOrig.isSetSBOTerm()) {
					SBMLtools.setSBOTerm(reac,reacOrig.getSBOTerm());
				}
				miniModel.addReaction(reac);
				reac.setFast(reacOrig.getFast());
				reac.setReversible(reacOrig.getReversible());
				for (SpeciesReference specRefOrig : reacOrig.getListOfReactants()) {
					Species speciesOrig = specRefOrig.getSpeciesInstance();
					SpeciesReference sr = specRefOrig.clone();
					sr.setSpecies(copySpecies(speciesOrig, miniModel));
					reac.addReactant(sr);
				}
				for (SpeciesReference s : reacOrig.getListOfProducts()) {
					Species speciesOrig = s.getSpeciesInstance();
					SpeciesReference sr = s.clone();
					sr.setSpecies(copySpecies(speciesOrig, miniModel));
					reac.addProduct(sr);
				}
				for (ModifierSpeciesReference modifierReferenceOrig : reacOrig.getListOfModifiers()) {
					Species speciesOrig = modifierReferenceOrig.getSpeciesInstance();
					ModifierSpeciesReference modifierReference = modifierReferenceOrig.clone();
					modifierReference.setSpecies(copySpecies(speciesOrig, miniModel));
					reac.addModifier(modifierReference);
				}
				/*
				 * This will be over written later on anyway but ignoring it
				 * would be confusing for users...
				 */
				if (reacOrig.isSetKineticLaw()) {
					KineticLaw l = reacOrig.getKineticLaw();
					if (l.isSetMath()) {
						for (Parameter parameter : modelOrig.getListOfParameters()) {
              if (l.getMath().refersTo(parameter.getId())
                  && (miniModel.getParameter(parameter.getId()) != null)) {
								miniModel.addParameter(parameter.clone());
							}
						}
					}
					reac.setKineticLaw(l.clone());
				}
			}
		}

		return miniModel;
	}

	/**
	 * @throws Throwable
	 */
	public void generateLaws() throws Throwable {
		
		if(progressBar != null){
			progressAdapter = new ProgressAdapter(progressBar, TypeOfProgress.generateLaws);
			progressAdapter.setNumberOfTags(modelOrig, miniModel, prefs);
		}
		
		boolean reversibility = prefs
				.getBoolean(SqueezerOptions.OPT_TREAT_ALL_REACTIONS_REVERSIBLE);
		
		for (Reaction r : miniModel.getListOfReactions()) {
			ReactionType rt = new ReactionType(r);
			
			Class<?> kineticsClass = rt.identifyPossibleKineticLaw();
			
			if(progressAdapter != null){
				progressAdapter.setNumberOfTags(modelOrig, miniModel, prefs);
				progressAdapter.progressOn();
			}
			
			KineticLawGeneratorWorker klgw = new KineticLawGeneratorWorker(this, r, kineticsClass, reversibility);
			
			klgw.run();
			
			if(progressAdapter != null){
				progressAdapter.setNumberOfTags(modelOrig, miniModel, prefs);
				progressAdapter.progressOn();
			}
		}
		
		if(progressAdapter != null){
			progressAdapter.finished();
		}
	}

	/**
	 * Returns all reactions of the model that have the attribute to be fast.
	 * 
	 * @return Returns all reactions of the model that have the attribute to be
	 *         fast.
	 */
	public List<Reaction> getFastReactions() {
		return listOfFastReactions;
	}

	/**
	 * Returns the copy of the model that contains only those reactions together
	 * with all required species, compartments, species- and compartment types,
	 * and units for which kinetic equations are to be created.
	 * 
	 * @return
	 */
	public Model getMiniModel() {
		return miniModel;
	}

	/**
	 * 
	 * @return
	 */
	public Model getModel() {
		return modelOrig;
	}

	/**
	 * 
	 * @param i
	 * @return
	 */
	public Reaction getModifiedReaction(int i) {
		return miniModel.getReaction(i);
	}

	/**
	 * 
	 * @param reactionID
	 * @return
	 */
	public Reaction getModifiedReaction(String reactionID) {
		return miniModel.getReaction(reactionID);
	}

	/**
	 * 
	 * @return
	 */
	public int getNumCreatedKinetics() {
		return miniModel.getNumReactions();
	}

	/**
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public SortedSet<Integer> getPossibleEnzymes() {
		SortedSet<Integer> possibleEnzymes = new TreeSet<Integer>();
		String name;
		Option<Boolean> options[] = (Option<Boolean>[]) new Option[] {
				SqueezerOptions.POSSIBLE_ENZYME_ANTISENSE_RNA,
				SqueezerOptions.POSSIBLE_ENZYME_COMPLEX,
				SqueezerOptions.POSSIBLE_ENZYME_GENERIC,
				SqueezerOptions.POSSIBLE_ENZYME_RECEPTOR,
				SqueezerOptions.POSSIBLE_ENZYME_RNA,
				SqueezerOptions.POSSIBLE_ENZYME_SIMPLE_MOLECULE,
				SqueezerOptions.POSSIBLE_ENZYME_TRUNCATED,
				SqueezerOptions.POSSIBLE_ENZYME_UNKNOWN 
		};
		for (Option<Boolean> option : options) {
			name = option.getName();
			if (prefs.getBoolean(option)) {
				possibleEnzymes.add(Integer.valueOf(SBO.convertAlias2SBO(name.substring(name.lastIndexOf('_') + 1))));
			}
		}
		// One more enzyme type that is not reflected in CellDesigner:
		if (prefs.getBoolean(SqueezerOptions.POSSIBLE_ENZYME_MACROMOLECULE)) {
			name = SqueezerOptions.POSSIBLE_ENZYME_MACROMOLECULE.getName();
			possibleEnzymes.add(Integer.valueOf(245));
		}
		return possibleEnzymes;
	}

	/**
	 * 
	 * @return
	 */
	public SBPreferences getPreferences() {
		return prefs;
	}

	/**
	 * 
	 * @param reactionID
	 * @return
	 * @throws RateLawNotApplicableException
	 */
	public ReactionType getReactionType(String reactionID)
	throws RateLawNotApplicableException {
		return new ReactionType(miniModel.getReaction(reactionID));
	}

	/**
	 * Returns true if the given model's stoichiometric matrix has full column
	 * rank.
	 * 
	 * @param model
	 * @return
	 */
	private boolean hasFullColumnRank(Model model) {
		boolean fullRank = false;
		if ((model.getNumSpecies() >= model.getNumReactions())
				&& (columnRank == -1)) {
			GaussianRank gaussian = new GaussianRank(stoechMatrix(model));
			columnRank = gaussian.getColumnRank();
			fullRank = gaussian.hasFullRank();
		} else if (columnRank == model.getNumReactions()) {
			fullRank = true;
		}
		return fullRank;
	}

	/**
	 * load default settings and initialize this object.
	 * 
	 * @param reactionID
	 */
	private void init() {
		init(null);
	}

	/**
	 * load default settings and initialize this object.
	 * 
	 * @param reactionID
	 */
	private void init(String reactionID) {
		if (prefs == null) {
			prefs = SBPreferences.getPreferencesFor(SqueezerOptions.class);
		}
		listOfFastReactions = new LinkedList<Reaction>();
		this.miniModel = createMinimalModel(reactionID);
		updateEnzymeCatalysis();
	}

	/**
	 * 
	 * @param species
	 * @param initialValue
	 * @param compartmentInstance
	 * @param sizeValue
	 */
	private void initializeSpeciesAndCompartmentIfNecessary(Species species,
			double initialValue, Compartment compartment, double sizeValue) {
		if (!species.isSetInitialAmount()
				&& !species.isSetInitialConcentration()) {
			if (species.getHasOnlySubstanceUnits()) {
				species.setInitialAmount(initialValue);
			} else {
				species.setInitialConcentration(initialValue);
			}
		}
		if (compartment != null && !compartment.isSetSize()) {
			// set size, if the spatialDimension Property is set and > 0
			if(compartment.isSetSpatialDimensions() && compartment.getSpatialDimensions() > 0){
				compartment.setSize(sizeValue);
			}
		}
	}

	/**
	 * Delete unnecessary paremeters from the model. A parameter is defined to
	 * be unnecessary if and only if no kinetic law, no event assignment, no
	 * rule and no function makes use of this parameter.
	 * 
	 * @param selectedModel
	 */
	@SuppressWarnings("deprecation")
	private void removeUnnecessaryParameters(Model model) {
		boolean isNeeded;
		int i, j, k = 0;
		Parameter p;
		// remove unnecessary global parameters
		for (i = model.getNumParameters() - 1; i >= 0; i--) {
			isNeeded = false;
			p = model.getParameter(i);
			/*
			 * Is this parameter necessary for some kinetic law or is this
			 * parameter a conversion factor in some stoichiometric math?
			 */
			for (j = 0; (j < model.getNumReactions()) && !isNeeded; j++) {
				Reaction r = model.getReaction(j);
				if (r.isSetKineticLaw() && r.getKineticLaw().isSetMath()
						&& r.getKineticLaw().getMath().refersTo(p.getId())) {
					/*
					 * ok, parameter occurs here but there could also be a local
					 * parameter with the same id.
					 */
					if (r.getKineticLaw().getParameter(p.getId()) == null) {
						isNeeded = true;
					}
				}
				if (isNeeded) {
					break;
				}
				SpeciesReference specRef;
				for (k = 0; k < r.getNumReactants(); k++) {
					specRef = r.getReactant(k);
					if (specRef.isSetStoichiometryMath()
							&& specRef.getStoichiometryMath().isSetMath()
							&& specRef.getStoichiometryMath().getMath()
							.refersTo(p.getId())) {
						isNeeded = true;
					}
				}
				if (isNeeded) {
					break;
				}
				for (k = 0; k < r.getNumProducts(); k++) {
					specRef = r.getProduct(k);
					if (specRef.isSetStoichiometryMath()
							&& specRef.getStoichiometryMath().isSetMath()
							&& specRef.getStoichiometryMath().getMath()
							.refersTo(p.getId())) {
						isNeeded = true;
					}
				}
			}

			// is this parameter necessary for some rule?
			for (j = 0; (j < model.getNumRules()) && !isNeeded; j++) {
				Rule r = model.getRule(j);
				if (r instanceof AssignmentRule
						&& ((AssignmentRule) r).getVariable().equals(p.getId())) {
					isNeeded = true;
				}
				if (r instanceof RateRule
						&& ((RateRule) r).getVariable().equals(p.getId())) {
					isNeeded = true;
				}
				if (r.isSetMath() && r.getMath().refersTo(p.getId())) {
					isNeeded = true;
				}
			}

			// is this parameter necessary for some event?
			for (j = 0; (j < model.getNumEvents()) && !isNeeded; j++) {
				Event e = model.getEvent(j);
				if (e.isSetTrigger() && e.getTrigger().isSetMath()
						&& e.getTrigger().getMath().refersTo(p.getId())) {
					isNeeded = true;
				}
				if (e.isSetDelay() && e.getDelay().isSetMath()
						&& e.getDelay().getMath().refersTo(p.getId())) {
					isNeeded = true;
				}
				for (k = 0; k < model.getEvent(j).getNumEventAssignments()
				&& !isNeeded; k++) {
					EventAssignment ea = e.getEventAssignment(k);
					if ((ea.isSetVariable() && ea.getVariable().equals(
							p.getId()))
							|| ea.isSetMath()
							&& ea.getMath().refersTo(p.getId())) {
						isNeeded = true;
					}
				}
			}

			// is this parameter necessary for some function?
			for (j = 0; j < model.getNumFunctionDefinitions() && !isNeeded; j++) {
				FunctionDefinition fd = model.getFunctionDefinition(j);
				if (fd.isSetMath() && fd.getMath().refersTo(p.getId())) {
					isNeeded = true;
				}
			}

			// is this parameter necessary for some initial assignment?
			for (j = 0; j < model.getNumInitialAssignments() && !isNeeded; j++) {
				InitialAssignment ia = model.getInitialAssignment(j);
				if ((ia.isSetVariable() && ia.getVariable().equals(p.getId()))
						|| ia.isSetMath() && ia.getMath().refersTo(p.getId())) {
					isNeeded = true;
				}
			}

			// is this parameter necessary for some constraint?
			for (j = 0; j < model.getNumConstraints() && !isNeeded; j++) {
				Constraint c = model.getConstraint(j);
				if (c.isSetMath() && c.getMath().refersTo(p.getId())) {
					isNeeded = true;
				}
			}

			if (!isNeeded) { // is this parameter necessary at all?
				model.getListOfParameters().remove(i);
			}
			
			if(progressAdapter != null){
				progressAdapter.progressOn();
			}
			
		}
		// remove unnecessary local parameters
		for (i = 0; i < model.getNumReactions(); i++) {
			Reaction r = model.getReaction(i);
			if (r.isSetKineticLaw()) {
				KineticLaw law = r.getKineticLaw();
				for (j = law.getNumParameters() - 1; j >= 0; j--) {
					if (law.isSetMath()
							&& !law.getMath().refersTo(
									law.getParameter(j).getId())) {
						law.removeParameter(j);
					}
				}
			}
			if(progressAdapter != null){
				progressAdapter.progressOn();
			}
			
		}
	}

	/**
	 * Sets the boundary condition of all species referenced by the list of
	 * {@link SpeciesReference}s.
	 * 
	 * @param numReactants
	 * @param setBoundary
	 */
	private void setBoundaryCondition(ListOf<SpeciesReference> listOf,
			boolean setBoundary) {
		for (int i = 0; i < listOf.size(); i++) {
			Species species = listOf.get(i).getSpeciesInstance();
			if ((SBO.isGeneOrGeneCodingRegion(species.getSBOTerm()) || SBO
					.isEmptySet(species.getSBOTerm()))
					&& setBoundary) {
				setBoundaryCondition(species, true);
			}
		}
	}

	/**
	 * set the boundaryCondition for a gene to the given value
	 * 
	 * @param species
	 * @param condition
	 */
	private void setBoundaryCondition(Species species, boolean condition) {
		if (condition != species.getBoundaryCondition()) {
			species.setBoundaryCondition(condition);
		}
	}

	/**
	 * Sets the initial amounts of all modifiers, reactants and products to the
	 * specified value.
	 * 
	 * @param reaction
	 * @param initialValue
	 * @param sizeValue
	 */
	private void setInitialConcentrationTo(Reaction reaction,
			double initialValue, double sizeValue) {
		Species species;
		for (int reactant = 0; reactant < reaction.getNumReactants(); reactant++) {
			species = reaction.getReactant(reactant).getSpeciesInstance();
			initializeSpeciesAndCompartmentIfNecessary(species, initialValue,
					species.getCompartmentInstance(), sizeValue);
		}
		for (int product = 0; product < reaction.getNumProducts(); product++) {
			species = reaction.getProduct(product).getSpeciesInstance();
			initializeSpeciesAndCompartmentIfNecessary(species, initialValue,
					species.getCompartmentInstance(), sizeValue);
		}
		for (int modifier = 0; modifier < reaction.getNumModifiers(); modifier++) {
			species = reaction.getModifier(modifier).getSpeciesInstance();
			initializeSpeciesAndCompartmentIfNecessary(species, initialValue,
					species.getCompartmentInstance(), sizeValue);
		}
	}

	/**
	 * 
	 * @param progressBar
	 */
	public void setProgressBar(AbstractProgressBar progressBar) {
		this.progressBar = progressBar;
	}

	/**
	 * Sets the reaction with the given id in the minimal model copy to the
	 * given reversibility value.
	 * 
	 * @param reactionID
	 * @param reversible
	 */
	public void setReversible(String reactionID, boolean reversible) {
		Reaction r = miniModel.getReaction(reactionID);
		if (r != null) {
			r.setReversible(reversible);
		} else {
			throw new IllegalArgumentException(MessageFormat.format(
					Bundles.WARNINGS.getString("INVALID_REACTION_ID_FOR_RATE_LAW_CREATION"), reactionID));
		}
	}


	/**
	 * Computes the stoichiometric matrix of the model system.
	 * 
	 * @return
	 */
	public double[][] stoechMatrix(Model model) {
		double[][] N = new double[model.getNumSpecies()][model
		                                                 .getNumReactions()];
		int reactionNum, speciesNum;
		SpeciesReference speciesRef;
		HashMap<String, Integer> speciesIDandNum = new HashMap<String, Integer>();
		int i = 0;
		for (Species s : model.getListOfSpecies())
			speciesIDandNum.put(s.getId(), Integer.valueOf(i++));
		for (reactionNum = 0; reactionNum < model.getNumReactions(); reactionNum++) {
			Reaction reaction = model.getReaction(reactionNum);
			for (speciesNum = 0; speciesNum < reaction.getNumReactants(); speciesNum++) {
				speciesRef = reaction.getReactant(speciesNum);
				N[speciesIDandNum.get(speciesRef.getSpecies())][reactionNum] = -speciesRef
				.getStoichiometry();
			}
			for (speciesNum = 0; speciesNum < reaction.getNumProducts(); speciesNum++) {
				speciesRef = reaction.getProduct(speciesNum);
				N[speciesIDandNum.get(speciesRef.getSpecies())][reactionNum] = speciesRef
				.getStoichiometry();
			}
		}
		return N;
	}

	/**
	 * 
	 * @param kineticLaw
	 * @param removeParametersAndStoreUnits
	 * @param l
	 * @return
	 */
	private Reaction storeKineticLaw(KineticLaw kineticLaw,
			boolean removeParametersAndStoreUnits) {
		int i;
		boolean reversibility = prefs.getBoolean(SqueezerOptions.OPT_TREAT_ALL_REACTIONS_REVERSIBLE);
		Reaction reaction = modelOrig.getReaction(kineticLaw.getParentSBMLObject().getId());
		reaction.setReversible(reversibility || reaction.getReversible());
		reaction.setKineticLaw(kineticLaw);
		// set the BoundaryCondition to true for genes if not set anyway:
		boolean setBoundary = prefs.getBoolean(SqueezerOptions.OPT_SET_BOUNDARY_CONDITION_FOR_GENES);
		setBoundaryCondition(reaction.getListOfReactants(), setBoundary);
		setBoundaryCondition(reaction.getListOfProducts(), setBoundary);
		setInitialConcentrationTo(reaction, 
		    prefs.getDouble(SqueezerOptions.OPT_DEFAULT_SPECIES_INITIAL_VALUE), 
				prefs.getDouble(SqueezerOptions.OPT_DEFAULT_COMPARTMENT_INITIAL_SIZE));
		if (removeParametersAndStoreUnits) {
			storeUnits();
		}
		storeParamters(reaction);
		if (removeParametersAndStoreUnits
				&& prefs.getBoolean(SqueezerOptions.OPT_REMOVE_UNNECESSARY_PARAMETERS_AND_UNITS)) {
			/*
			 * delete unnecessary units.
			 */
			removeUnnecessaryParameters(modelOrig);

			for (int j = modelOrig.getNumUnitDefinitions() - 1; j >= 0; j--) {
				UnitDefinition udef = modelOrig.getUnitDefinition(j);
				boolean isNeeded = Unit.isPredefined(udef.getId(), udef.getLevel());
				for (i = 0; i < modelOrig.getNumCompartments() && !isNeeded; i++) {
					Compartment c = modelOrig.getCompartment(i);
					if (c.isSetUnits() && c.getUnits().equals(udef.getId())) {
						isNeeded = true;
					}
				}
				for (i = 0; i < modelOrig.getNumSpecies() && !isNeeded; i++) {
					Species s = modelOrig.getSpecies(i);
					if (s.isSetSubstanceUnits() && s.getSubstanceUnits().equals(udef.getId())) {
						isNeeded = true;
					}
				}
				for (i = 0; i < modelOrig.getNumParameters() && !isNeeded; i++) {
					Parameter p = modelOrig.getParameter(i);
					if (p.isSetUnits() && p.getUnits().equals(udef.getId())) {
						isNeeded = true;
					}
				}
				for (i = 0; i < modelOrig.getNumReactions() && !isNeeded; i++) {
					Reaction r = modelOrig.getReaction(i);
					if (r.isSetKineticLaw()) {
						for (LocalParameter p : r.getKineticLaw()
								.getListOfLocalParameters()) {
							if (p.isSetUnits() && p.getUnits().equals(udef.getId())) {
								isNeeded = true;
							}
						}
					}
				}
				if (!isNeeded) {
					modelOrig.removeUnitDefinition(udef);
				}
				if (progressAdapter != null) {
					progressAdapter.progressOn();
				}
			}
		}
		
		
		return reaction;
	}

	/**
	 * This method stores a {@link KineticLaw} for the given {@link Reaction} in
	 * the currently selected {@link Model} given by the user. The
	 * {@link KineticLaw} is passed to this method. A boolean variable tells this
	 * method weather the formula is for a reversible or for an irreversible
	 * {@link Reaction} (in the user's {@link Preferences}). Afterwards all
	 * parameters within this {@link KineticLaw} are also stored in the given
	 * {@link Model}. There is no need to call the
	 * {@link #storeParamters(Reaction)} method.
	 * 
	 * @param kineticLaw
	 *        A string with the formula to be assigned to the given reaction.
	 * @param l
	 */
	public Reaction storeKineticLaw(KineticLaw kineticLaw) {
		if (progressBar != null) {
			progressAdapter = new ProgressAdapter(progressBar, TypeOfProgress.storeKineticLaw);
			progressAdapter.setNumberOfTags(modelOrig, miniModel, prefs);
		}
		
		Reaction r =  storeKineticLaw(kineticLaw, true);
		
		if (progressAdapter != null) {
			progressAdapter.finished();
		}
		return r;
	}

	/**
	 * store the generated Kinetics in SBML-File as MathML.
	 */
	public void storeKineticLaws() {
		
		if (getFastReactions().size() > 0) {
			logger.log(Level.FINE, MessageFormat.format(Bundles.MESSAGES.getString("THE_MODEL_CONTAINS"), 
											getFastReactions().size(), modelOrig.getId())
									+ " " + Bundles.MESSAGES.getString("FAST_REACTIONS") + "."
									+ " " + Bundles.MESSAGES.getString("NOT_SUPPORTED"));
		}
		
		if(progressBar != null){
			progressAdapter = new ProgressAdapter(progressBar, TypeOfProgress.storeKineticLaws);
			progressAdapter.setNumberOfTags(modelOrig, miniModel, prefs);
		}

		ModelChangeListener chl = new ModelChangeListener();
		modelOrig.addTreeNodeChangeListener(chl);
		for (int i = 0; i < miniModel.getNumReactions(); i++) {
			Reaction r = miniModel.getReaction(i);
			storeKineticLaw(r.getKineticLaw(), false);
		}
		
		storeUnits();
		
		if (prefs.getBoolean(SqueezerOptions.OPT_REMOVE_UNNECESSARY_PARAMETERS_AND_UNITS)) {
			removeUnnecessaryParameters(modelOrig);
		}
		modelOrig.removeTreeNodeChangeListener(chl);

		if(progressAdapter != null){
			progressAdapter.finished();
		}
	}

	/**
	 * This method stores all newly created parameters in the model and sets the
	 * references to the units appropriately. UnitDefinitions that are not
	 * referenced by any object are removed from the model.
	 * 
	 * @param reaction
	 */
	private void storeParamters(Reaction reaction) {
		// setInitialConcentrationTo(reaction, 1d);
		KineticLaw kineticLaw = reaction.getKineticLaw();
		ListOf<LocalParameter> paramListLocal = kineticLaw.getListOfLocalParameters();
		boolean addGlobally = prefs.getBoolean(SqueezerOptions.OPT_ADD_NEW_PARAMETERS_ALWAYS_GLOBALLY);
		for (int paramNum = paramListLocal.size() - 1; paramNum >= 0; paramNum--) {
			if (addGlobally) {
				Parameter p = new Parameter(paramListLocal.remove(paramNum));
				modelOrig.addParameter(p);
				updateUnitReferences(p);
			} else {
				updateUnitReferences(paramListLocal.get(paramNum));
			}
		}
		for (Parameter parameter : miniModel.getListOfParameters()) {
			if (modelOrig.getParameter(parameter.getId()) == null) {
				modelOrig.addParameter(parameter);
				updateUnitReferences(parameter);
			}
			
		}
		kineticLaw.getMath().updateVariables();
		if (progressAdapter != null) {
			progressAdapter.setNumberOfTags(modelOrig, miniModel, prefs);
			progressAdapter.progressOn();
		}
	}

	/**
	 * Stores all units created in the mini model in the original model.
	 * 
	 * @param l
	 */
	private void storeUnits() {
		for (UnitDefinition ud : miniModel.getListOfUnitDefinitions()) {
			int units = ud.getNumUnits();
			if (modelOrig.getUnitDefinition(ud.getId()) == null) {
				modelOrig.addUnitDefinition(ud.clone());
			} else {
				UnitDefinition orig = modelOrig.getUnitDefinition(ud.getId());
				orig.setListOfUnits(ud.getListOfUnits().clone());
				orig.simplify();
				if (ud.isSetName()) {
					orig.setName(new String(ud.getName()));
				}
			}
			if(progressAdapter != null){
				progressAdapter.setNumberOfTags(modelOrig, miniModel, prefs);
				progressAdapter.progressOn();
			}
			if (units != ud.getNumUnits()) {
				logger.log(Level.WARNING, ud.getId() + "\t" + units + "\t->\t"
						+ ud.getNumUnits());
			}
		}

		for (Compartment c : miniModel.getListOfCompartments()) {
			Compartment corig = modelOrig.getCompartment(c.getId());
			// if level > 1, set spatialDimension
			// the property spatialDimension is available since l2v1
			if(miniModel.getLevel() > 1){
				corig.setSpatialDimensions(c.getSpatialDimensions());
			}
			if (c.isSetUnits()) {
				if (Unit.isUnitKind(c.getUnits(), c.getLevel(), c.getVersion())) {
					corig.setUnits(Unit.Kind.valueOf(c.getUnits().toUpperCase()));
				} else {
					corig.setUnits(modelOrig.getUnitDefinition(c.getUnits()));
				}
			}
			checkUnits(corig, modelOrig);
			if(progressAdapter != null){
				progressAdapter.setNumberOfTags(modelOrig, miniModel, prefs);
				progressAdapter.progressOn();
			}
		}

		for (Species s : miniModel.getListOfSpecies()) {
			Species sorig = modelOrig.getSpecies(s.getId());
			// if level > 1, set hasOnlySubstanceUnits
			// the hasOnlySubstanceUnits property is available since l2v1
			if(miniModel.getLevel() > 1){
				sorig.setHasOnlySubstanceUnits(s.getHasOnlySubstanceUnits());
			}
			if (s.isSetSubstanceUnits()) {
				if (Unit.isUnitKind(s.getSubstanceUnits(), s.getLevel(), s
						.getVersion())) {
					sorig.setSubstanceUnits(Unit.Kind.valueOf(s
							.getSubstanceUnits().toUpperCase()));
				} else {
					sorig.setSubstanceUnits(modelOrig.getUnitDefinition(s
							.getSubstanceUnits()));
				}
			}
			checkUnits(sorig, modelOrig);
			
			if(progressAdapter != null){
				progressAdapter.setNumberOfTags(modelOrig, miniModel, prefs);
				progressAdapter.progressOn();
			}
		}
	}

	/**
	 * Sets the SBO annotation of modifiers to more precise values in the local
	 * mini copy of the model.
	 * 
	 * Updates the minimal model so that all possible enzymes are marked as
	 * enzymatic catalyst.
	 */
	public void updateEnzymeCatalysis() {
		Set<Integer> possibleEnzymes = getPossibleEnzymes();
		for (Reaction r : miniModel.getListOfReactions()) {
			for (ModifierSpeciesReference modifier : r.getListOfModifiers()) {
				Species species = modifier.getSpeciesInstance();
				if (SBO.isEnzymaticCatalysis(modifier.getSBOTerm())
						&& species.isSetSBOTerm()
						&& !possibleEnzymes.contains(Integer.valueOf(species
								.getSBOTerm()))) {
					SBMLtools.setSBOTerm(modifier, SBO.getCatalysis());
				} else if (SBO.isCatalyst(modifier.getSBOTerm())
						&& (possibleEnzymes.contains(Integer.valueOf(species
								.getSBOTerm())) || !species.isSetSBOTerm())) {
					SBMLtools.setSBOTerm(modifier, SBO.getEnzymaticCatalysis());
				}
			}
		}
	}


	/**
	 * Moves the pointers from the units in the mini Model to the units in the
	 * original model.
	 * 
	 * @param p
	 */
	private void updateUnitReferences(QuantityWithUnit p) {
		if (p.isSetUnits()) {
			String units = p.getUnits();
			if (Unit.isUnitKind(units, p.getLevel(), p.getVersion())) {
				p.setUnits(Unit.Kind.valueOf(units.toUpperCase()));
			} else {
				UnitDefinition ud = modelOrig.getUnitDefinition(p.getUnits());
				if (ud == null) {
					ud = miniModel.getUnitDefinition(p.getUnits()).clone();
					modelOrig.addUnitDefinition(ud);
				}
				p.setUnits(ud);
			}
		}
	}
}
