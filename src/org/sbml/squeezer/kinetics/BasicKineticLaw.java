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
package org.sbml.squeezer.kinetics;

import java.util.IllegalFormatException;
import java.util.LinkedList;
import java.util.List;

import org.sbml.jsbml.ASTNode;
import org.sbml.jsbml.Compartment;
import org.sbml.jsbml.KineticLaw;
import org.sbml.jsbml.ListOf;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.Parameter;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.SBO;
import org.sbml.jsbml.SimpleSpeciesReference;
import org.sbml.jsbml.Species;
import org.sbml.jsbml.SpeciesReference;
import org.sbml.jsbml.Unit;
import org.sbml.jsbml.UnitDefinition;
import org.sbml.squeezer.RateLawNotApplicableException;
import org.sbml.squeezer.ReactionType;
import org.sbml.squeezer.io.StringTools;

/**
 * An abstract super class of specialized kinetic laws.
 * 
 * @since 1.0
 * @version
 * @author <a href="mailto:andreas.draeger@uni-tuebingen.de">Andreas
 *         Dr&auml;ger</a>
 * @author <a href="mailto:Nadine.hassis@gmail.com">Nadine Hassis</a>
 * @author <a href="mailto:hannes.borch@googlemail.com">Hannes Borch</a>
 * @date Aug 1, 2007
 */
public abstract class BasicKineticLaw extends KineticLaw {

	/**
	 * The ids of activators that enhance the progress of this rate law.
	 */
	private List<String> activators;

	/**
	 * If true all species whose hasOnlySubstanceUnits attribute is true are
	 * divided by the size of their surrounding compartment. If false species
	 * whose hasOnlySubstanceUnits attribute is false are multiplied with the
	 * size of their surrounding compartment.
	 */
	private boolean bringToConcentration;

	/**
	 * The default value that is used to initialize new parameters.
	 */
	private double defaultParamValue;

	/**
	 * The ids of the enzymes catalyzing the reaction described by this rate
	 * law.
	 */
	private List<String> enzymes;

	/**
	 * Ids of inhibitors that lower the velocity of this rate law.
	 */
	private List<String> inhibitors;

	/**
	 * The ids of catalysts that are no enzymes.
	 */
	private List<String> nonEnzymeCatalysts;

	/**
	 * Ids of translational or transcriptional activators.
	 */
	private List<String> transActivators;

	/**
	 * Ids of transcriptional or translational inhibitors.
	 */
	private List<String> transInhibitors;

	/**
	 * 
	 */
	private Object typeParameters[];

	/**
	 * True if the reaction system to which the parent reaction belongs has a
	 * full collumn rank.
	 */
	boolean fullRank;

	/**
	 * Allows for zeroth order reverse kinetics.
	 */
	double orderProducts;

	/**
	 * Allows for zeroth order forward kinetics.
	 */
	double orderReactants;

	/**
	 * <ol>
	 * <li>cat</li>
	 * <li>hal</li>
	 * <li>weg</li>
	 * </ol>
	 */
	short type;

	/**
	 * 
	 */
	final Character underscore = StringTools.underscore;

	/**
	 * 
	 * @param parentReaction
	 * @param typeParameters
	 * @throws RateLawNotApplicableException
	 * @throws IllegalFormatException
	 */
	public BasicKineticLaw(Reaction parentReaction, Object... typeParameters)
			throws RateLawNotApplicableException, IllegalFormatException {
		super(parentReaction);
		this.typeParameters = typeParameters;
		this.fullRank = true;
		this.bringToConcentration = false;
		this.defaultParamValue = 1d;
		if (typeParameters.length > 0)
			type = Short.parseShort(getTypeParameters()[0].toString());
		if (typeParameters.length > 1
				&& !Boolean.parseBoolean(getTypeParameters()[1].toString()))
			fullRank = false;
		if (typeParameters.length > 2)
			bringToConcentration = ((Integer) typeParameters[2]).intValue() != 0;
		if (typeParameters.length > 3)
			defaultParamValue = Double
					.parseDouble(typeParameters[3].toString());
		enzymes = new LinkedList<String>();
		activators = new LinkedList<String>();
		transActivators = new LinkedList<String>();
		inhibitors = new LinkedList<String>();
		transInhibitors = new LinkedList<String>();
		nonEnzymeCatalysts = new LinkedList<String>();
		ReactionType.identifyModifers(parentReaction, enzymes, activators,
				transActivators, inhibitors, transInhibitors,
				nonEnzymeCatalysts);
		setMath(createKineticEquation(enzymes, activators, transActivators,
				inhibitors, transInhibitors, nonEnzymeCatalysts));
	}

	/**
	 * Returns a string that gives a simple description of this rate equation.
	 * 
	 * @return
	 */
	public abstract String getSimpleName();

	/**
	 * 
	 * @return
	 */
	public Object[] getTypeParameters() {
		return typeParameters;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.MathContainer#setMath(org.sbml.jsbml.ASTNode)
	 */
	public void setMath(ASTNode ast) {
		if (!isSetMath())
			super.setMath(ast);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.MathContainer#toString()
	 */
	// @Override
	public String toString() {
		return isSetSBOTerm() ? SBO.getTerm(getSBOTerm()).getDescription()
				.replace("\\,", ",") : getClass().getSimpleName();
	}

	/**
	 * 
	 * @param modE
	 * @param modActi
	 * @param modTActi
	 * @param modInhib
	 * @param modTInhib
	 * @param modCat
	 * @return
	 * @throws RateLawNotApplicableException
	 * @throws IllegalFormatException
	 */
	abstract ASTNode createKineticEquation(List<String> modE,
			List<String> modActi, List<String> modTActi, List<String> modInhib,
			List<String> modTInhib, List<String> modCat)
			throws RateLawNotApplicableException, IllegalFormatException;

	/**
	 * Concatenates the given name parts of the identifier and returns a global
	 * parameter with this id.
	 * 
	 * @param idParts
	 * @return
	 */
	Parameter createOrGetGlobalParameter(Object... idParts) {
		return createOrGetGlobalParameter(StringTools.concat(idParts)
				.toString());
	}

	/**
	 * If the parent model does not yet contain a parameter with the given id, a
	 * new parameter is created, its value is set to 1 and it is added to the
	 * list of global parameters in the model. If there is already a parameter
	 * with this id, a reference to it will be returned.
	 * 
	 * @param id
	 *            the identifier of the global parameter.
	 */
	Parameter createOrGetGlobalParameter(String id) {
		Model m = getModel();
		Parameter p = m.getParameter(id);
		if (p == null) {
			p = new Parameter(id, getLevel(), getVersion());
			p.setValue(defaultParamValue);
			p.setConstant(true);
			m.addParameter(p);
		}
		return p;
	}

	/**
	 * Equvivalent to {@see createOrGetParameter} but the parts of the id can be
	 * given separately to this method and are concatenated.
	 * 
	 * @param idParts
	 * @return
	 */
	Parameter createOrGetParameter(Object... idParts) {
		return createOrGetParameter(StringTools.concat(idParts).toString());
	}

	/**
	 * If a parameter with the given identifier has already been created and is
	 * contained in the list of local parameters for this kinetic law, a pointer
	 * to it will be returned. If no such parameter exists, a new parameter with
	 * a value of 1 will be created and a pointer to it will be returned.
	 * 
	 * @param id
	 *            the identifier of the local parameter.
	 * @return
	 */
	Parameter createOrGetParameter(String id) {
		Parameter p = getParameter(id);
		if (p == null) {
			p = new Parameter(id, getLevel(), getVersion());
			p.setValue(defaultParamValue);
			p.setConstant(true);
			addParameter(p);
		}
		return p;
	}

	/**
	 * Association constant from mass action kinetics.
	 * 
	 * 
	 * @param catalyst
	 * @return
	 */
	Parameter parameterAssociationConst(String catalyst) {
		boolean zerothOrder = orderReactants == 0d;
		Reaction r = getParentSBMLObject();
		StringBuffer kass = StringTools.concat("kass_", r.getId());
		if (zerothOrder)
			kass.insert(0, 'z');
		if (catalyst != null)
			StringTools.append(kass, underscore, catalyst);
		Parameter p_kass = createOrGetParameter(kass.toString());
		if (!p_kass.isSetName()) {
			p_kass.setName("ssociation constant of reaction " + r.getId());
			if (zerothOrder)
				p_kass.setName("Zeroth order a" + p_kass.getName());
			else
				p_kass.setName("A" + p_kass.getName());
		}
		if (!p_kass.isSetSBOTerm())
			p_kass.setSBOTerm(zerothOrder ? 48 : 153);
		if (!p_kass.isSetUnits())
			p_kass.setUnits(unitPerTimeAndConcentrationOrSubstance(r
					.getListOfReactants(), zerothOrder));
		return p_kass;
	}

	/**
	 * biochemical cooperative inhibitor substrate coefficient.
	 * 
	 * @param name
	 * @param inhibitor
	 * @param enzyme
	 * @return
	 */
	Parameter parameterCooperativeInhibitorSubstrateCoefficient(String name,
			String inhibitor, String enzyme) {
		StringBuffer id = new StringBuffer();
		id.append(name.replace(' ', '_'));
		if (inhibitor != null)
			StringTools.append(id, underscore, inhibitor);
		if (enzyme != null)
			StringTools.append(id, underscore, enzyme);
		Parameter coeff = createOrGetParameter(id);
		if (!coeff.isSetSBOTerm())
			coeff.setSBOTerm(385);
		if (!coeff.isSetUnits())
			coeff.setUnits(Unit.Kind.DIMENSIONLESS);
		return coeff;
	}

	/**
	 * Dissociation constant in mass action kinetics
	 * 
	 * 
	 * @param catalyst
	 * @return
	 */
	Parameter parameterDissociationConst(String catalyst) {
		boolean zerothOrder = orderProducts == 0;
		Reaction r = getParentSBMLObject();
		StringBuffer kdiss = StringTools.concat("kdiss_", r.getId());
		if (zerothOrder)
			kdiss.insert(0, 'z');
		if (catalyst != null)
			StringTools.append(kdiss, underscore, catalyst);
		Parameter p_kdiss = createOrGetParameter(kdiss.toString());
		if (!p_kdiss.isSetName()) {
			p_kdiss.setName("issociation constant of reaction " + r.getId());
			if (zerothOrder)
				p_kdiss.setName("Zeroth order d" + p_kdiss.getName());
			else
				p_kdiss.setName("D" + p_kdiss.getName());
		}
		if (!p_kdiss.isSetSBOTerm())
			p_kdiss.setSBOTerm(156);
		if (!p_kdiss.isSetUnits())
			p_kdiss.setUnits(unitPerTimeAndConcentrationOrSubstance(r
					.getListOfProducts(), zerothOrder));
		return p_kdiss;
	}

	/**
	 * Equilibrium constant of the parent reaction.
	 * 
	 * @return
	 */
	Parameter parameterEquilibriumConstant() {
		String reactionID = getParentSBMLObject().getId();
		Parameter keq = createOrGetParameter("keq_", reactionID);
		if (!keq.isSetSBOTerm())
			keq.setSBOTerm(281);
		if (!keq.isSetName())
			keq.setName(StringTools.concat("equilibrium constant of reaction ",
					reactionID).toString());
		if (!keq.isSetUnits()) {
			double x = 0;
			Reaction r = getParentSBMLObject();
			for (SpeciesReference specRef : r.getListOfReactants())
				x += specRef.getStoichiometry();
			if (r.getReversible())
				for (SpeciesReference specRef : r.getListOfProducts())
					x -= specRef.getStoichiometry();
			keq.setUnits(unitmM((int) x));
		}
		return keq;
	}

	/**
	 * Bolzman's gas constant.
	 * 
	 * @return
	 */
	Parameter parameterGasConstant() {
		Parameter R = createOrGetGlobalParameter("R");
		if (!R.isSetValue())
			R.setValue(8.31447215);
		if (!R.isSetName())
			R.setName("ideal gas constant");
		if (!R.isSetUnits())
			R.setUnits(unitJperKandM());
		return R;
	}

	/**
	 * Hill coefficient.
	 * 
	 * @param enzyme
	 * @return
	 */
	Parameter parameterHillCoefficient(String enzyme) {
		String reactionID = getParentSBMLObject().getId();
		StringBuffer id = StringTools.concat("h_", reactionID);
		if (enzyme != null)
			StringTools.append(id, underscore, enzyme);
		Parameter hr = createOrGetParameter(id.toString());
		if (!hr.isSetSBOTerm())
			hr.setSBOTerm(190);
		if (!hr.isSetName())
			hr.setName("Hill coefficient");
		if (!hr.isSetUnits())
			hr.setUnits(Unit.Kind.DIMENSIONLESS);
		return hr;
	}

	/**
	 * Activation constant.
	 * 
	 * @param activatorSpecies
	 * @return
	 */
	Parameter parameterKa(String activatorSpecies) {
		String reactionID = getParentSBMLObject().getId();
		Parameter kA = createOrGetParameter("ka_", reactionID, underscore,
				activatorSpecies);
		if (!kA.isSetSBOTerm())
			kA.setSBOTerm(363);
		if (!kA.isSetUnits())
			kA.setUnits(bringToConcentration ? unitmM() : getModel()
					.getUnitDefinition("substance"));
		return kA;
	}

	/**
	 * Catalytic constant
	 * 
	 * @param enzyme
	 *            the id of the enzyme that catalyzes this reaction
	 * @param forward
	 *            forward or reverse (true or false).
	 * @return
	 */
	Parameter parameterKcat(String enzyme, boolean forward) {
		return parameterKcatOrVmax(enzyme, forward);
	}

	/**
	 * Turn over rate if enzyme is null and limiting rate otherwise.
	 * 
	 * @param enzyme
	 * @param forward
	 * @return The parameter vmax if and only if the number of enzymes is zero,
	 *         otherwise kcat.
	 */
	Parameter parameterKcatOrVmax(String enzyme, boolean forward) {
		String reactionID = getParentSBMLObject().getId();
		Parameter kr;
		if (enzyme != null) {
			StringBuffer id = StringTools.concat("kcat_");
			if (getParentSBMLObject().getReversible())
				StringTools.append(id, forward ? "fwd" : "bwd", underscore);
			StringTools.append(id, reactionID, underscore, enzyme);
			kr = createOrGetParameter(id);
			if (!kr.isSetSBOTerm()) {
				if (getParentSBMLObject().getReversible())
					kr.setSBOTerm(forward ? 320 : 321);
				else
					kr.setSBOTerm(25);
			}
			if (!kr.isSetUnits())
				kr.setUnits(unitPerTimeOrSizePerTime(getModel().getSpecies(
						enzyme).getCompartmentInstance()));
		} else {
			kr = createOrGetParameter("vmax_", forward ? "fwd" : "bwd",
					underscore, reactionID);
			if (!kr.isSetSBOTerm()) {
				if (getParentSBMLObject().getReversible())
					kr.setSBOTerm(forward ? 324 : 325);
				else
					kr.setSBOTerm(186);
			}
			if (!kr.isSetUnits())
				kr.setUnits(/*
							 * bringToConcentration ? unitmMperSecond() :
							 */unitmmolePerSecond());
		}
		return kr;
	}

	/**
	 * energy constant of given species
	 * 
	 * @param species
	 * @return
	 */
	Parameter parameterKG(String species) {
		Parameter kG = createOrGetGlobalParameter("kG_", species);
		if (!kG.isSetUnits())
			kG.setUnits(Unit.Kind.DIMENSIONLESS);
		if (!kG.isSetName())
			kG.setName(StringTools.concat("energy constant of species ",
					species).toString());
		return kG;
	}

	/**
	 * Inhibitory constant
	 * 
	 * @param inhibitorSpecies
	 *            species that lowers this velocity.
	 * @return
	 */
	Parameter parameterKi(String inhibitorSpecies) {
		return parameterKi(inhibitorSpecies, null);
	}

	/**
	 * Inhibitory constant in an enzyme catalyzed reaction.
	 * 
	 * @param inhibitorSpecies
	 * @param enzyme
	 * @return
	 */
	Parameter parameterKi(String inhibitorSpecies, String enzyme) {
		return parameterKi(inhibitorSpecies, enzyme, 0);
	}

	/**
	 * 
	 * @param inhibitorSpecies
	 * @param enzymeID
	 * @param bindingNum
	 *            if <= 0 not considered.
	 * @return
	 */
	Parameter parameterKi(String inhibitorSpecies, String enzymeID,
			int bindingNum) {
		StringBuffer id = new StringBuffer();
		id.append("ki");
		if (bindingNum > 0)
			id.append(bindingNum);
		String reactionID = getParentSBMLObject().getId();
		StringTools.append(id, underscore, reactionID);
		if (inhibitorSpecies != null)
			StringTools.append(id, underscore, inhibitorSpecies);
		if (enzymeID != null)
			StringTools.append(id, underscore, enzymeID);
		Parameter kI = createOrGetParameter(id.toString());
		if (!kI.isSetSBOTerm())
			kI.setSBOTerm(261);
		if (!kI.isSetUnits())
			if (bringToConcentration)
				kI.setUnits(unitmM());
			else
				kI.setUnits(unitmmole());
		return kI;
	}

	/**
	 * Half saturation constant.
	 * 
	 * @param enzyme
	 * @return
	 */
	Parameter parameterKS(String enzyme) {
		Parameter kS = createOrGetParameter("kSp_", getParentSBMLObject()
				.getId());
		if (!kS.isSetSBOTerm())
			kS.setSBOTerm(194);
		if (bringToConcentration)
			kS.setUnits(unitmM());
		else
			kS.setUnits(unitmmole());
		return kS;
	}

	/**
	 * Michaelis constant.
	 * 
	 * @param species
	 * @param enzyme
	 * @return
	 */
	Parameter parameterMichaelis(String species, String enzyme) {
		String reactionID = getParentSBMLObject().getId();
		StringBuffer id = StringTools.concat("kM_", reactionID, underscore,
				species);
		if (enzyme != null)
			StringTools.append(id, underscore, enzyme);
		Parameter kM = createOrGetParameter(id.toString());
		if (kM.isSetSBOTerm())
			kM.setSBOTerm(27);
		if (!kM.isSetName())
			kM.setName(StringTools.concat("Michaelis constant of species ",
					species, " in reaction ", reactionID).toString());
		if (!kM.isSetUnits())
			kM.setUnits(bringToConcentration ? unitmM() : getModel()
					.getUnitDefinition("substance"));
		return kM;
	}

	/**
	 * Michaelis constant.
	 * 
	 * @param species
	 * @param enzyme
	 * @param substrate
	 *            If true it returns the Michaels constant for substrate, else
	 *            the Michaelis constant for the product.
	 * @return
	 */
	Parameter parameterMichaelis(String species, String enzyme,
			boolean substrate) {
		Parameter kM = parameterMichaelis(species, enzyme);
		kM.setSBOTerm(substrate ? 322 : 323);
		return kM;
	}

	/**
	 * Number of binding sites for the inhibitor on the enzyme that catalyses
	 * this reaction.
	 * 
	 * @param enzyme
	 * @param inhibitor
	 * @return
	 */
	Parameter parameterNumBindingSites(String enzyme, String inhibitor) {
		StringBuffer exponent = StringTools.concat("m_", getParentSBMLObject()
				.getId());
		if (enzyme != null)
			StringTools.append(exponent, underscore, enzyme);
		if (inhibitor != null)
			StringTools.append(exponent, underscore, inhibitor);
		Parameter p_exp = createOrGetParameter(exponent.toString());
		if (!p_exp.isSetSBOTerm())
			p_exp.setSBOTerm(189);
		if (!p_exp.isSetUnits())
			p_exp.setUnits(Unit.Kind.DIMENSIONLESS);
		return p_exp;
	}

	/**
	 * Rho activator according to Liebermeister et al.
	 * 
	 * @param species
	 * @return
	 */
	Parameter parameterRhoActivation(String species) {
		Parameter rhoA = createOrGetParameter("rho_act_", getParentSBMLObject()
				.getId(), underscore, species);
		if (!rhoA.isSetUnits())
			rhoA.setUnits(Unit.Kind.DIMENSIONLESS);
		return rhoA;
	}

	/**
	 * standard chemical potential
	 * 
	 * @param species
	 * @return
	 */
	Parameter parameterStandardChemicalPotential(String species) {
		Parameter mu = createOrGetGlobalParameter("mu0_", species);
		if (!mu.isSetName())
			mu.setName("standard chemical potential");
		if (!mu.isSetSBOTerm())
			mu.setSBOTerm(463);
		if (!mu.isSetUnits())
			mu.setUnits(unitkJperMole());
		return mu;
	}

	/**
	 * Standard Temperature
	 * 
	 * @return
	 */
	Parameter parameterTemperature() {
		Parameter T = createOrGetGlobalParameter("T");
		if (!T.isSetSBOTerm())
			T.setSBOTerm(147);
		if (!T.isSetValue())
			T.setValue(298.15);
		if (!T.isSetUnits())
			T.setUnits(Unit.Kind.KELVIN);
		if (!T.isSetName())
			T.setName("The temperature under standard conditions.");
		return T;
	}

	/**
	 * Creates and annotates the velocity constant for the reaction with the
	 * given id and the number of enzymes.
	 * 
	 * @param numEnzymes
	 * @return
	 */
	Parameter parameterVelocityConstant(String enzyme) {
		Parameter kVr;
		String reactionID = getParentSBMLObject().getId();
		if (enzyme != null) {
			kVr = createOrGetParameter("kv_", reactionID, underscore, enzyme);
			if (!kVr.isSetName())
				kVr.setName(StringTools.concat("KV value of reaction ",
						reactionID).toString());
			if (!kVr.isSetUnits())
				kVr.setUnits(unitPerTimeOrSizePerTime(getModel().getSpecies(
						enzyme).getCompartmentInstance()));
		} else {
			kVr = createOrGetParameter("vmax_geom_", reactionID);
			if (!kVr.isSetName())
				kVr
						.setName(StringTools
								.concat(
										"limiting maximal velocity, geometric mean of reaction ",
										reactionID).toString());
			if (!kVr.isSetUnits())
				kVr.setUnits(bringToConcentration ? unitmMperSecond()
						: unitmmolePerSecond());
		}
		return kVr;
	}

	/**
	 * Limiting rate
	 * 
	 * @param forward
	 * @return
	 */
	Parameter parameterVmax(boolean forward) {
		return parameterKcatOrVmax(null, forward);
	}

	/**
	 * 
	 * @param simpleSpecRef
	 * @return
	 */
	ASTNode speciesTerm(SimpleSpeciesReference simpleSpecRef) {
		return speciesTerm(simpleSpecRef.getSpeciesInstance());
	}

	/**
	 * 
	 * @param species
	 * @return
	 */
	ASTNode speciesTerm(Species species) {
		ASTNode specTerm = new ASTNode(species, this);
		if (species.getHasOnlySubstanceUnits()) {
			// set initial amount and units appropriately
			if (bringToConcentration)
				specTerm.divideBy(species.getCompartmentInstance());
		} else {
			// multiply with compartment size.
			if (!bringToConcentration)
				specTerm.multiplyWith(species.getCompartmentInstance());
		}
		return specTerm;
	}

	/**
	 * 
	 * @param species
	 * @return
	 */
	ASTNode speciesTerm(String species) {
		return speciesTerm(getModel().getSpecies(species));
	}

	/**
	 * 
	 * @return
	 */
	UnitDefinition unitJperKandM() {
		UnitDefinition ud = new UnitDefinition("J_per_K_and_mole", getLevel(),
				getVersion());
		ud.addUnit(new Unit(Unit.Kind.JOULE, getLevel(), getVersion()));
		ud.addUnit(new Unit(1, 0, Unit.Kind.KELVIN, -1, getLevel(),
				getVersion()));
		ud
				.addUnit(new Unit(1, 0, Unit.Kind.MOLE, -1, getLevel(),
						getVersion()));
		getModel().addUnitDefinition(ud);
		return ud;
	}

	/**
	 * 
	 * @return
	 */
	UnitDefinition unitkJperMole() {
		UnitDefinition kJperMole = new UnitDefinition("kJ_per_mole",
				getLevel(), getVersion());
		kJperMole
				.addUnit(new Unit(3, Unit.Kind.JOULE, getLevel(), getVersion()));
		kJperMole.addUnit(new Unit(Unit.Kind.MOLE, getLevel(), getVersion()));
		getModel().addUnitDefinition(kJperMole);
		return kJperMole;
	}

	/**
	 * 
	 * @return
	 */
	UnitDefinition unitmM() {
		return unitmM(1);
	}

	/**
	 * 
	 * @param exponent
	 * @return
	 */
	UnitDefinition unitmM(int exponent) {
		String id = "mM";
		if (exponent != 1)
			id += exponent;
		UnitDefinition mM = getModel().getUnitDefinition(id);
		if (mM == null) {
			mM = new UnitDefinition(id, getLevel(), getVersion());
			mM.addUnit(unitmmole());
			mM.addUnit(new Unit(Unit.Kind.LITRE, -exponent, getLevel(),
					getVersion()));
			getModel().addUnitDefinition(mM);
		}
		return mM;
	}

	/**
	 * Creates and returns the unit milli mole.
	 * 
	 * @return A Unit object that represents the unit milli mole.
	 */
	Unit unitmmole() {
		return new Unit(-3, Unit.Kind.MOLE, getLevel(), getVersion());
	}

	/**
	 * 
	 * @return
	 */
	UnitDefinition unitmmolePerSecond() {
		Model model = getModel();
		String id = "mmolePerSecond";
		UnitDefinition mmolePerSecond = model.getUnitDefinition(id);
		if (mmolePerSecond == null) {
			mmolePerSecond = new UnitDefinition(id, getLevel(), getVersion());
			mmolePerSecond.addUnit(new Unit(-3, Unit.Kind.MOLE, getLevel(),
					getVersion()));
			mmolePerSecond.addUnit(new Unit(Unit.Kind.SECOND, -1, getLevel(),
					getVersion()));
			model.addUnitDefinition(mmolePerSecond);
		}
		return mmolePerSecond;
	}

	/**
	 * 
	 * @return Unit milli mole per metre.
	 */
	UnitDefinition unitmMperMetre() {
		UnitDefinition mMperMetre = unitmM().clone();
		mMperMetre.addUnit(new Unit(Unit.Kind.METRE, -1, getLevel(),
				getVersion()));
		return mMperMetre;
	}

	/**
	 * Returns the unit milli mole per litre per second.
	 * 
	 * @return
	 */
	UnitDefinition unitmMperSecond() {
		Model model = getModel();
		String id = "mMperSecond";
		UnitDefinition mMperSecond = model.getUnitDefinition(id);
		if (mMperSecond == null) {
			mMperSecond = unitmM().clone();
			mMperSecond.setId(id);
			mMperSecond.addUnit(new Unit(Unit.Kind.SECOND, -1, getLevel(),
					getVersion()));
			model.addUnitDefinition(mMperSecond);
		}
		return mMperSecond;
	}

	/**
	 * 
	 * @return Unit milli mole per square metre.
	 */
	UnitDefinition unitmMperSquareMetre() {
		UnitDefinition mMperSquareMetre = unitmM().clone();
		mMperSquareMetre.addUnit(new Unit(Unit.Kind.METRE, -2, getLevel(),
				getVersion()));
		return mMperSquareMetre;
	}

	/**
	 * 1/s, equivalent to Hz.
	 * 
	 * @return
	 */
	UnitDefinition unitPerTime() {
		UnitDefinition ud = getModel().getUnitDefinition("time").clone();
		if (ud.getNumUnits() == 1) {
			ud.getListOfUnits().getFirst().setExponent(-1);
			ud.setId("perTime");
		} else {
			ud = new UnitDefinition("perSecond", getLevel(), getVersion());
			Unit unit = new Unit(Unit.Kind.SECOND, -1, getLevel(), getVersion());
			ud.addUnit(unit);
		}
		return ud;
	}

	/**
	 * 
	 * @param listOf
	 * @return
	 */
	UnitDefinition unitPerTimeAndConcentrationOrSubstance(
			ListOf<SpeciesReference> listOf) {
		return unitPerTimeAndConcentrationOrSubstance(listOf, false);
	}

	/**
	 * 
	 * @param listOf
	 * @param zerothOrder
	 * @return
	 */
	UnitDefinition unitPerTimeAndConcentrationOrSubstance(
			ListOf<SpeciesReference> listOf, boolean zerothOrder) {
		Model model = getModel();
		UnitDefinition ud = new UnitDefinition("ud", getLevel(), getVersion());
		ud.divideBy(model.getUnitDefinition("time"));
		UnitDefinition amount = new UnitDefinition("amount", getLevel(),
				getVersion());
		int i;
		if (!zerothOrder) {
			UnitDefinition substance;
			SpeciesReference specRef;
			Species species;
			for (i = 0; i < listOf.size(); i++) {
				specRef = listOf.get(i);
				species = specRef.getSpeciesInstance();
				substance = species.getSubstanceUnitsInstance().clone();
				if (bringToConcentration)
					substance.divideBy(species.getCompartmentInstance()
							.getUnitsInstance());
				substance
						.raiseByThePowerOf((int) specRef.getStoichiometry() - 1);
				amount.multiplyWith(substance);
			}
		}
		ud = ud.divideBy(amount).multiplyWith(
				model.getUnitDefinition("substance")).simplify();
		StringBuilder sb = new StringBuilder();
		for (i = 0; i < ud.getNumUnits(); i++) {
			Unit u = ud.getUnit(i);
			if (i > 0)
				sb.append('_');
			if (u.getExponent() < 0)
				sb.append("per_");
			sb.append(u.getPrefix());
			sb.append(u.getKind().getName());
		}
		ud.setId(sb.toString());
		UnitDefinition def = model.getUnitDefinition(ud.getId());
		if (def == null)
			model.addUnitDefinition(ud);
		ud = model.getUnitDefinition(ud.getId());
		return ud;
	}

	/**
	 * 
	 * @param c
	 * @return
	 */
	UnitDefinition unitPerTimeOrSizePerTime(Compartment c) {
		if (bringToConcentration) {
			Model model = getModel();
			StringBuilder name = new StringBuilder();
			if (!c.isSetUnits())
				c.setUnits(model.getUnitDefinition("volume"));
			UnitDefinition sizeUnit = c.getUnitsInstance();
			if (sizeUnit.isVariantOfVolume())
				name.append("volume");
			else if (sizeUnit.isVariantOfArea())
				name.append("area");
			else if (sizeUnit.isVariantOfLength())
				name.append("length");
			name.append(" per time");
			String id = name.toString().replace(' ', '_');
			UnitDefinition ud = model.getUnitDefinition(id);
			if (ud == null) {
				ud = new UnitDefinition(sizeUnit);
				ud.setId(id);
				ud.divideBy(model.getUnitDefinition("time"));
				ud.setName(name.toString());
				model.addUnitDefinition(ud);
			}
			return ud;
		} else {
			return unitPerTime();
		}
	}
}
