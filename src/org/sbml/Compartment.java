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
package org.sbml;

/**
 * @author Andreas Dr&auml;ger <a
 *         href="mailto:andreas.draeger@uni-tuebingen.de">
 *         andreas.draeger@uni-tuebingen.de</a>
 * 
 */
public class Compartment extends Symbol {

	/**
	 * 
	 */
	private CompartmentType compartmentType;
	/**
	 * 
	 */
	private short spatialDimensions;
	/**
	 * 
	 */
	private Compartment outside;
	/**
	 * 
	 */
	private boolean constant;

	/**
	 * 
	 * @param compartment
	 */
	public Compartment(Compartment compartment) {
		super(compartment);
		this.compartmentType = compartment.getCompartmentType();
		this.spatialDimensions = compartment.getSpatialDimensions();
		this.outside = compartment.getOutside();
		this.constant = compartment.getConstant();
		setValue(compartment.getSize());
	}

	public Compartment(String id) {
		super(id);
		initDefaults();
	}

	public Compartment(String id, String name) {
		super(id, name);
		initDefaults();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.SBase#clone()
	 */
	// @Override
	public Compartment clone() {
		return new Compartment(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.NamedSBase#equals(java.lang.Object)
	 */
	// @Override
	public boolean equals(Object o) {
		boolean equal = super.equals(o);
		if (o instanceof Compartment) {
			Compartment c = (Compartment) o;
			equal &= c.getConstant() == constant;
			if ((!c.isSetOutside() && isSetOutside())
					|| (c.isSetOutside() && !isSetOutside()))
				return false;
			if (c.isSetOutside() && isSetOutside())
				equal &= c.getOutside().equals(getOutside());
			equal &= c.getSize() == getSize();
			equal &= c.getSpatialDimensions() == getSpatialDimensions();
			return equal;
		}
		return false;
	}

	/**
	 * 
	 * @return
	 */
	public CompartmentType getCompartmentType() {
		return compartmentType;
	}

	/**
	 * 
	 * @return
	 */
	public boolean getConstant() {
		return isConstant();
	}

	/**
	 * 
	 * @return
	 */
	public Compartment getOutside() {
		return outside;
	}

	/**
	 * 
	 * @return
	 */
	public double getSize() {
		return getValue();
	}

	/**
	 * 
	 * @return
	 */
	public short getSpatialDimensions() {
		return spatialDimensions;
	}

	/**
	 *(For SBML Level 1) Get the volume of this Compartment
	 * 
	 * This method is identical to getSize(). In SBML Level 1, compartments are
	 * always three-dimensional constructs and only have volumes, whereas in
	 * SBML Level 2, compartments may be other than three-dimensional and
	 * therefore the 'volume' attribute is named 'size' in Level 2. LibSBML
	 * provides both getSize() and getVolume() for easier compatibility between
	 * SBML Levels.
	 * 
	 * @return
	 */
	public double getVolume() {
		return getSize();
	}

	/**
	 * 
	 */
	public void initDefaults() {
		compartmentType = null;
		spatialDimensions = 3;
		constant = true;
		outside = null;
		setSize(Double.NaN);
	}

	/**
	 * 
	 * @return
	 */
	public boolean isConstant() {
		return constant;
	}

	/**
	 * 
	 * @return
	 */
	public boolean isSetCompartmentType() {
		return compartmentType != null;
	}

	/**
	 * 
	 * @return
	 */
	public boolean isSetOutside() {
		return outside != null;
	}

	/**
	 * 
	 * @param compartmentType
	 */
	public void setCompartmentType(CompartmentType compartmentType) {
		this.compartmentType = compartmentType;
	}

	/**
	 * 
	 * @param constant
	 */
	public void setConstant(boolean constant) {
		this.constant = constant;
	}

	/**
	 * 
	 * @param outside
	 */
	public void setOutside(Compartment outside) {
		this.outside = outside;
	}

	/**
	 * 
	 * @param size
	 */
	public void setSize(double size) {
		setValue(size);
	}

	/**
	 * 
	 * @param spatialDimensions
	 */
	public void setSpatialDimensions(short spatialDimensions) {
		if (spatialDimensions >= 0 && spatialDimensions <= 3)
			this.spatialDimensions = spatialDimensions;
		else
			throw new IllegalArgumentException(
					"Spatial dimensions must be between [0, 3].");
	}
}
