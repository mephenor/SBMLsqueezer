package org.sbmlsqueezer.kinetics;

import java.io.IOException;
import java.util.List;
import java.util.Vector;

import jp.sbi.celldesigner.plugin.PluginModel;
import jp.sbi.celldesigner.plugin.PluginReaction;
import jp.sbi.celldesigner.plugin.PluginSpeciesReference;

/**
 * TODO: comment missing
 * 
 * @since 2.0
 * @version
 * @author Nadine Hassis <Nadine.hassis@gmail.com>
 * @author Andreas Dr&auml;ger (draeger) <andreas.draeger@uni-tuebingen.de>
 *         Copyright (c) ZBiT, University of T&uuml;bingen, Germany Compiler:
 *         JDK 1.6.0
 * @author Hannes Borch <hannes.borch@googlemail.com>
 * @date Aug 1, 2007
 */
public class GeneralizedMassAction extends BasicKineticLaw {

	protected double reactantOrder;

	protected double productOrder;

	private String sbo = null;

	/**
	 * 
	 * @param parentReaction
	 * @param model
	 * @throws RateLawNotApplicableException
	 * @throws IOException
	 * @throws IllegalFormatException 
	 */
	public GeneralizedMassAction(PluginReaction parentReaction,
			PluginModel model) throws RateLawNotApplicableException,
			IOException, IllegalFormatException {
		super(parentReaction, model);
	}

	/**
	 * @param parentReaction
	 * @param model
	 * @param reversibility
	 * @param listOfPossibleEnzymes
	 * @throws RateLawNotApplicableException
	 * @throws IOException
	 * @throws IllegalFormatException 
	 */
	public GeneralizedMassAction(PluginReaction parentReaction,
			PluginModel model, List<String> listOfPossibleEnzymes)
			throws RateLawNotApplicableException, IOException, IllegalFormatException {
		super(parentReaction, model, listOfPossibleEnzymes);
	}

	public static boolean isApplicable(PluginReaction reaction) {
		// TODO
		return true;
	}

	// @Override
	public String getName() {
		String name = "mass action kinetics";
		String orderF = "", orderR = "";
		if (getParentReaction().getReversible()) {
			if (reactantOrder == 0)
				orderF = "zeroth order forward, ";
			else
				switch (getParentReaction().getNumReactants()) {
				case 1:
					if (getParentReaction().getReactant(0).getStoichiometry() == 1.0)
						orderF = "first order forward, ";
					else if (getParentReaction().getReactant(0)
							.getStoichiometry() == 2.0)
						orderF = "second order forward with one reactant, ";
					else if (getParentReaction().getReactant(0)
							.getStoichiometry() == 3.0)
						orderF = "third order forward with one reactant, ";
					break;
				case 2:
					if ((getParentReaction().getReactant(0).getStoichiometry() == 1.0)
							&& (getParentReaction().getReactant(1)
									.getStoichiometry() == 1.0))
						orderF = "second order forward with two reactants, ";
					else if (((getParentReaction().getReactant(0)
							.getStoichiometry() == 1.0) && (getParentReaction()
							.getReactant(1).getStoichiometry() == 2.0))
							|| ((getParentReaction().getReactant(0)
									.getStoichiometry() == 2.0) && (getParentReaction()
									.getReactant(1).getStoichiometry() == 1.0)))
						orderF = "third order forward with two reactants, ";
					break;
				case 3:
					if ((getParentReaction().getReactant(0).getStoichiometry()
							+ getParentReaction().getReactant(1)
									.getStoichiometry()
							+ getParentReaction().getReactant(2)
									.getStoichiometry() == 3.0)
							&& (getParentReaction().getReactant(0)
									.getStoichiometry() == 1.0)
							&& (getParentReaction().getReactant(1)
									.getStoichiometry() == 1.0)
							&& (getParentReaction().getReactant(2)
									.getStoichiometry() == 1.0))
						orderF = "third order forward with three reactants, ";
					break;
				}

			if (productOrder == 0)
				orderR = "zeroth order reverse";
			else
				switch (getParentReaction().getNumProducts()) {
				case 1:
					if (getParentReaction().getProduct(0).getStoichiometry() == 1.0)
						orderR = "first order reverse";
					else if (getParentReaction().getProduct(0)
							.getStoichiometry() == 2.0)
						orderR = "second order reverse with one product";
					else if (getParentReaction().getProduct(0)
							.getStoichiometry() == 3.0)
						orderR = "third order reverse with one product";
					break;
				case 2:
					if ((getParentReaction().getProduct(0).getStoichiometry() == 1.0)
							&& (getParentReaction().getProduct(1)
									.getStoichiometry() == 1.0))
						orderR = "second order reverse with two products";
					else if (((getParentReaction().getProduct(0)
							.getStoichiometry() == 1.0) && (getParentReaction()
							.getProduct(1).getStoichiometry() == 2.0))
							|| ((getParentReaction().getProduct(0)
									.getStoichiometry() == 2.0) && (getParentReaction()
									.getProduct(1).getStoichiometry() == 1.0)))
						orderR = "third order reverse with two products";
					break;
				case 3:
					if ((getParentReaction().getProduct(0).getStoichiometry()
							+ getParentReaction().getProduct(1)
									.getStoichiometry()
							+ getParentReaction().getProduct(2)
									.getStoichiometry() == 3.0)
							&& ((getParentReaction().getProduct(0)
									.getStoichiometry() == 1.0)
									&& (getParentReaction().getProduct(1)
											.getStoichiometry() == 1.0) && (getParentReaction()
									.getProduct(2).getStoichiometry() == 1.0)))
						orderR = "third order reverse with three products";
					break;
				default:
					break;
				}
			if (orderF == "")
				orderR = "";
			if (orderR == "")
				orderF = "";
			else {
				orderR += ", ";
				name += ", continuous scheme";
			}
			name = orderF + orderR + "reversible " + name;
		} else { // irreversible
			if (reactantOrder == 0)
				orderF = "zeroth order ";
			else if (getParentReaction().getNumReactants() == 1) {
				if (getParentReaction().getReactant(0).getStoichiometry() == 1.0)
					orderF = "first order ";
				else {
					if (getParentReaction().getReactant(0).getStoichiometry() == 2.0) {
						orderF = "second order ";
						name += ", one reactant";
					} else if (getParentReaction().getReactant(0)
							.getStoichiometry() == 3.0) {
						orderF = "third order ";
						name += ", one reactant";
					}
				}
			} else if (getParentReaction().getNumReactants() == 2) {
				if ((getParentReaction().getReactant(0).getStoichiometry() == 1.0)
						&& (getParentReaction().getReactant(1)
								.getStoichiometry() == 1.0)) {
					orderF = "second order ";
					name += ", two reactants";
				} else if (((getParentReaction().getReactant(0)
						.getStoichiometry() == 2.0) && (getParentReaction()
						.getReactant(1).getStoichiometry() == 1.0))
						|| ((getParentReaction().getReactant(1)
								.getStoichiometry() == 2.0) && (getParentReaction()
								.getReactant(0).getStoichiometry() == 1.0))) {
					orderF = "third order ";
					name += ", two reactants";
				}
			} else if (getParentReaction().getNumReactants() == 3) {
				// third order irreversible mass action kinetics, three
				// reactants
				if (((getParentReaction().getReactant(0).getStoichiometry()
						+ getParentReaction().getReactant(1).getStoichiometry()
						+ getParentReaction().getReactant(2).getStoichiometry() == 3.0))
						&& (getParentReaction().getReactant(0)
								.getStoichiometry() == 1.0)
						&& (getParentReaction().getReactant(1)
								.getStoichiometry() == 1.0)
						&& (getParentReaction().getReactant(2)
								.getStoichiometry() == 1.0)) {
					orderF = "third order ";
					name += ", three reactants";
				}
			}
			name = orderF + "irreversible " + name + ", continuous scheme";
		}
		return name;
	}

	// @Override
	public String getSBO() {
		if (sbo == null) {
			String name = getName().toLowerCase();
			sbo = "none";
			if (name.endsWith(", continuous scheme")) {
				name = name.substring(0, name.length() - 19);
				if (name
						.equals("zeroth order irreversible mass action kinetics"))
					sbo = "0000047";
				else if (name
						.equals("first order irreversible mass action kinetics"))
					sbo = "0000049"; // child: SBO:0000333 monoexponential
				// decay
				else if (name
						.equals("second order irreversible mass action kinetics, one reactant"))
					sbo = "0000052";
				else if (name
						.equals("second order irreversible mass action kinetics, two reactants"))
					sbo = "0000054";
				else if (name
						.equals("third order irreversible mass action kinetics, one reactant"))
					sbo = "0000057";
				else if (name
						.equals("third order irreversible mass action kinetics, two reactants"))
					sbo = "0000059";
				else if (name
						.equals("third order irreversible mass action kinetics, three reactants"))
					sbo = "0000061";
				else if (name
						.equals("zeroth order forward, first order reverse, reversible mass action kinetics"))
					sbo = "0000070";
				else if (name
						.equals("zeroth order forward, second order reverse with one product, reversible mass action kinetics"))
					sbo = "0000072";
				else if (name
						.equals("zeroth order forward, second order reverse with two products, reversible mass action kinetics"))
					sbo = "0000073";
				else if (name
						.equals("zeroth order forward, third order reverse with one product, reversible mass action kinetics"))
					sbo = "0000075";
				else if (name
						.equals("zeroth order forward, third order reverse with two products, reversible mass action kinetics"))
					sbo = "0000076";
				else if (name
						.equals("zeroth order forward, third order reverse with three products, reversible mass action kinetics"))
					sbo = "0000077";
				else if (name
						.equals("first order forward, zeroth order reverse, reversible mass action kinetics"))
					sbo = "0000079";
				else if (name
						.equals("first order forward, first order reverse, reversible mass action kinetics"))
					sbo = "0000080";
				else if (name
						.equals("first order forward, second order reverse with one product, reversible mass action kinetics"))
					sbo = "0000082";
				else if (name
						.equals("first order forward, second order reverse with two products, reversible mass action kinetics"))
					sbo = "0000083";
				else if (name
						.equals("first order forward, third order reverse with one product, reversible mass action kinetics"))
					sbo = "0000085";
				else if (name
						.equals("first order forward, third order reverse with two products, reversible mass action kinetics"))
					sbo = "0000086";
				else if (name
						.equals("first order forward, third order reverse with three products, reversible mass action kinetics"))
					sbo = "0000087";
				else if (name
						.equals("second order forward with one reactant, zeroth order reverse, reversible mass action kinetics"))
					sbo = "0000090";
				else if (name
						.equals("second order forward with one reactant, first order reverse, reversible mass action kinetics"))
					sbo = "0000091";
				else if (name
						.equals("second order forward with one reactant, second order reverse with one product, reversible mass action kinetics"))
					sbo = "0000093";
				else if (name
						.equals("second order forward with one reactant, second order reverse with two products, reversible mass action kinetics"))
					sbo = "0000094";
				else if (name
						.equals("second order forward with one reactant, third order reverse with one product, reversible mass action kinetics"))
					sbo = "0000096";
				else if (name
						.equals("second order forward with one reactant, third order reverse with two products, reversible mass action kinetics"))
					sbo = "0000097";
				else if (name
						.equals("second order forward with one reactant, third order reverse with three products, reversible mass action kinetics"))
					sbo = "0000098";
				else if (name
						.equals("second order forward with two reactants, zeroth order reverse, reversible mass action kinetics"))
					sbo = "0000100";
				else if (name
						.equals("second order forward with two reactants, first order reverse, reversible mass action kinetics"))
					sbo = "0000101";
				else if (name
						.equals("second order forward with two reactants, second order reverse with one product, reversible mass action kinetics"))
					sbo = "0000103";
				else if (name
						.equals("second order forward with two reactants, second order reverse with two products, reversible mass action kinetics"))
					sbo = "0000104";
				else if (name
						.equals("second order forward with two reactants, third order reverse with one product, reversible mass action kinetics"))
					sbo = "0000106";
				else if (name
						.equals("second order forward with two reactants, third order reverse with two products, reversible mass action kinetics"))
					sbo = "0000107";
				else if (name
						.equals("second order forward with two reactants, third order reverse with three products, reversible mass action kinetics"))
					sbo = "0000108";
				else if (name
						.equals("third order forward with two reactants, zeroth order reverse, reversible mass action kinetics"))
					sbo = "0000111";
				else if (name
						.equals("third order forward with two reactants, first order reverse, reversible mass action kinetics"))
					sbo = "0000112";
				else if (name
						.equals("third order forward with two reactants, second order reverse with one product, reversible mass action kinetics"))
					sbo = "0000114";
				else if (name
						.equals("third order forward with two reactants, second order reverse with two products, reversible mass action kinetics"))
					sbo = "0000115";
				else if (name
						.equals("third order forward with two reactants, third order reverse with one product, reversible mass action kinetics"))
					sbo = "0000117";
				else if (name
						.equals("third order forward with two reactants, third order reverse with two products, reversible mass action kinetics"))
					sbo = "0000118";
				else if (name
						.equals("third order forward with two reactants, third order reverse with three products, reversible mass action kinetics"))
					sbo = "0000119";
				else if (name
						.equals("third order forward with three reactants, zeroth order reverse, reversible mass action kinetics"))
					sbo = "0000121";
				else if (name
						.equals("third order forward with three reactants, first order reverse, reversible mass action kinetics"))
					sbo = "0000122";
				else if (name
						.equals("third order forward with three reactants, second order reverse with one product, reversible mass action kinetics"))
					sbo = "0000124";
				else if (name
						.equals("third order forward with three reactants, second order reverse with two products, reversible mass action kinetics"))
					sbo = "0000125";
				else if (name
						.equals("third order forward with three reactants, third order reverse with one product, reversible mass action kinetics"))
					sbo = "0000127";
				else if (name
						.equals("third order forward with three reactants, third order reverse with two products, reversible mass action kinetics"))
					sbo = "0000128";
				else if (name
						.equals("third order forward with three reactants, third order reverse with three products, reversible mass action kinetics"))
					sbo = "0000129";
				else if (name
						.equals("third order forward with one reactant, zeroth order reverse, reversible mass action kinetics"))
					sbo = "0000131";
				else if (name
						.equals("third order forward with one reactant, first order reverse, reversible mass action kinetics"))
					sbo = "0000132";
				else if (name
						.equals("third order forward with one reactant, second order reverse with one product, reversible mass action kinetics"))
					sbo = "0000134";
				else if (name
						.equals("third order forward with one reactant, second order reverse with two products, reversible mass action kinetics"))
					sbo = "0000135";
				else if (name
						.equals("third order forward with one reactant, third order reverse with one product, reversible mass action kinetics"))
					sbo = "0000137";
				else if (name
						.equals("third order forward with one reactant, third order reverse with two products, reversible mass action kinetics"))
					sbo = "0000138";
				else if (name
						.equals("third order forward with one reactant, third order reverse with three products, reversible mass action kinetics"))
					sbo = "0000139";
				else if (name.equals("irreversible mass action kinetics"))
					sbo = "0000163"; // an inner node.
			} else {
				// other inner vertices
				if (name.equals("reversible mass action kinetics"))
					sbo = "0000042";
				else if (name.equals("irreversible mass action kinetics"))
					sbo = "0000041";
				else if (name
						.equals("zeroth order irreversible mass action kinetics"))
					sbo = "0000043";
				else if (name
						.equals("first order irreversible mass action kinetics"))
					sbo = "0000044";
				else if (name
						.equals("second order irreversible mass action kinetics"))
					sbo = "0000045";
				else if (name
						.equals("second order irreversible mass action kinetics, one reactant"))
					sbo = "0000050";
				else if (name
						.equals("second order irreversible mass action kinetics, two reactants"))
					sbo = "0000053";
				else if (name
						.equals("third order irreversible mass action kinetics"))
					sbo = "0000055";
				else if (name
						.equals("third order irreversible mass action kinetics, one reactant"))
					sbo = "0000056";
				else if (name
						.equals("third order irreversible mass action kinetics, two reactants"))
					sbo = "0000058";
				else if (name
						.equals("third order irreversible mass action kinetics, three reactants"))
					sbo = "0000060";
				else if (name
						.equals("zeroth order forward reversible mass action kinetics"))
					sbo = "0000069";
				else if (name
						.equals("zeroth order forward, second order reverse, reversible mass action kinetics"))
					sbo = "0000071";
				else if (name
						.equals("zeroth order forward, third order reverse, reversible mass action kinetics"))
					sbo = "0000074";
				else if (name
						.equals("first order forward reversible mass action kinetics"))
					sbo = "0000078";
				else if (name
						.equals("first order forward, second order reverse, reversible mass action kinetics"))
					sbo = "0000081";
				else if (name
						.equals("first order forward, third order reverse, reversible mass action kinetics"))
					sbo = "0000084";
				else if (name
						.equals("second order forward reversible mass action kinetics"))
					sbo = "0000088";
				else if (name
						.equals("second order forward with one reactant reversible mass action kinetics"))
					sbo = "0000089";
				else if (name
						.equals("second order forward with one reactant, second order reverse, reversible mass action kinetics"))
					sbo = "0000092";
				else if (name
						.equals("second order forward with one reactant, third order reverse, reversible mass action kinetics"))
					sbo = "0000095";
				else if (name
						.equals("second order forward with two reactants, second order reverse, reversible mass action kinetics"))
					sbo = "0000102";
				else if (name
						.equals("second order forward with two reactants, third order reverse, reversible mass action kinetics"))
					sbo = "0000105";
				else if (name
						.equals("third order forward with two reactants reversible mass action kinetics"))
					sbo = "0000110";
				else if (name
						.equals("third order forward with two reactants, second order reverse, reversible mass action kinetics"))
					sbo = "0000113";
				else if (name
						.equals("third order forward with two reactants, third order reverse, reversible mass action kinetics"))
					sbo = "0000116";
				else if (name
						.equals("third order forward with three reactants reversible mass action kinetics "))
					sbo = "0000120";
				else if (name
						.equals("third order forward with three reactants, second order reverse, reversible mass action kinetics"))
					sbo = "0000123";
				else if (name
						.equals("third order forward with three reactants, third order reverse, reversible mass action kinetics"))
					sbo = "0000126";
				else if (name
						.equals("third order forward with one reactant reversible mass action kinetics"))
					sbo = "0000130";
				else if (name
						.equals("third order forward with one reactant, second order reverse, reversible mass action kinetics"))
					sbo = "0000133";
				else if (name
						.equals("third order forward with one reactant, third order reverse, reversible mass action kinetics"))
					sbo = "0000136";
				else if (name
						.equals("third order forward with one reactant, third order reverse, reversible mass action kinetics"))
					sbo = "0000136";
			}
		}
		return sbo;
	}

	protected StringBuffer createKineticEquation(int catNumber,
			int reactionNum, List<String> modCat, List<String> modActi,
			List<String> modInhib, boolean zeroReact, boolean zeroProd) {
		reactionNum++;
		PluginReaction reaction = getParentReaction();
		try {
			StringBuffer ass = getReactionPartners(reaction, reactionNum,
					modCat, catNumber, FORWARD, zeroReact);
			StringBuffer diss = getReactionPartners(reaction, reactionNum,
					modCat, catNumber, REVERSE, zeroProd);
			return times(createModificationFactor(reactionNum, modActi,
					ACTIVATION), createModificationFactor(reactionNum, modInhib,
					INHIBITION), diff(ass, diss));
		} catch (IllegalFormatException exc) {
			exc.printStackTrace();
			return new StringBuffer();
		}
	}

	// @Override
	protected StringBuffer createKineticEquation(PluginModel model,
			int reactionNum, List<String> modE, List<String> modActi,
			List<String> modTActi, List<String> modInhib,
			List<String> modTInhib, List<String> modCat)
			throws RateLawNotApplicableException, IllegalFormatException {
		reactantOrder = productOrder = Double.NaN;
		boolean zeroReact = false, zeroProd = false;
		if (modCat.isEmpty())
			return createKineticEquation(-1, reactionNum, modCat, modActi,
					modInhib, zeroReact, zeroProd);
		StringBuffer[] equations = new StringBuffer[modCat.size()];
		for (int i = 0; i < equations.length; i++)
			if (equations.length == 1)
				equations[i] = removeBrackets(createKineticEquation(i,
						reactionNum, modCat, modActi, modInhib, zeroReact,
						zeroProd));
			else
				equations[i] = createKineticEquation(i, reactionNum, modCat,
						modActi, modInhib, zeroReact, zeroProd);
		return removeBrackets(sum(equations));

	}

	/**
	 * Returns the product of either all the activation or inhibition terms of a
	 * reaction.
	 * 
	 * @param reactionNumber
	 * @param modifiers
	 * @param type
	 * @return
	 * @throws IllegalFormatException
	 */
	protected StringBuffer createModificationFactor(int reactionNumber,
			List<String> modifiers, boolean type) throws IllegalFormatException {
		if (type == ACTIVATION || type == INHIBITION) {
			if (!modifiers.isEmpty()) {
				StringBuffer[] mods = new StringBuffer[modifiers.size()];
				for (int i = 0; i < mods.length; i++) {
					StringBuffer k = new StringBuffer(
							(type == ACTIVATION) ? "kA_" : "kI_");
					k.append(reactionNumber);
					k.append('_');
					k.append(modifiers.get(i));
					mods[i] = frac(k,
							sum(k, new StringBuffer(modifiers.get(i))));
					if (!listOfLocalParameters.contains(k))
						listOfLocalParameters.add(k);
				}
				return times(mods);
			} else
				return new StringBuffer();
		} else {
			throw new IllegalFormatException(
					"Invalid type argument for reaction modifiers");
		}
	}

	/**
	 * Returns either the forward reaction or backward reaction term for a
	 * reaction with GMAK. Both types of terms are formed by the product of the
	 * reaction's reactant or, respectively, product concentrations to the power
	 * of the belonging stoichiometry. Each product is also multiplied with the
	 * reaction's forward or backward directed equilibrium constant.
	 * 
	 * @param reaction
	 * @param reactionNumber
	 * @param catalysators
	 * @param catalysatorNumber
	 * @param type
	 * @param b
	 * @return
	 * @throws IllegalFormatException
	 */
	private StringBuffer getReactionPartners(PluginReaction reaction,
			int reactionNumber, List<String> catalysators,
			int catalysatorNumber, boolean type, boolean b)
			throws IllegalFormatException {
		if (type == FORWARD || type == REVERSE) {
			if ((type == FORWARD)
					|| (type == REVERSE && reaction.getReversible())) {
				StringBuffer k = new StringBuffer((type == FORWARD) ? "kass_"
						: "kdiss_");
				k.append(reactionNumber);
				listOfLocalParameters.add(k);
				Vector<StringBuffer> parts = new Vector<StringBuffer>();
				parts.add(k);
				if (!b)
					for (int i = 0; i < ((type == FORWARD) ? reaction
							.getNumReactants() : reaction.getNumProducts()); i++) {
						PluginSpeciesReference ref = (type == FORWARD) ? reaction
								.getReactant(i)
								: reaction.getProduct(i);
						parts.add(pow(getSpecies(ref), getStoichiometry(ref)));
					}

				if (catalysatorNumber >= 0)
					parts.add(new StringBuffer(catalysators
							.get(catalysatorNumber)));

				return times(parts.toArray(new StringBuffer[parts.size()]));
			} else {
				return new StringBuffer();
			}
		} else {
			throw new IllegalFormatException(
					"Invalid type argument for GMAK reaction partners");
		}
	}

	/**
	 * Returns the name of a PluginSpeciesReference object's belonging species
	 * as an object of type StringBuffer.
	 * 
	 * @param ref
	 * @return
	 */
	protected StringBuffer getSpecies(PluginSpeciesReference ref) {
		return new StringBuffer(ref.getSpecies());
	}

	/**
	 * Returns the value of a PluginSpeciesReference object's stoichiometry
	 * either as a double or, if the stoichiometry has an integer value, as an
	 * int object.
	 * 
	 * @param ref
	 * @return
	 */
	protected StringBuffer getStoichiometry(PluginSpeciesReference ref) {
		double stoich = ref.getStoichiometry();
		if ((int) stoich - stoich == 0)
			return new StringBuffer(Integer.toString((int) stoich));
		else
			return new StringBuffer(Double.toString(stoich));
	}

}
