/*
 * $Id$
 * $URL$
 * ---------------------------------------------------------------------
 * This file is part of SBMLsqueezer, a Java program that creates rate 
 * equations for reactions in SBML files (http://sbml.org).
 *
 * Copyright (C) 2006-2011 by the University of Tuebingen, Germany.
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
package org.sbml.squeezer.util;

import org.sbml.jsbml.Model;
import org.sbml.squeezer.SqueezerOptions;

import de.zbit.util.AbstractProgressBar;
import de.zbit.util.prefs.SBPreferences;

/**
 * 
 * @author Sarah R. M&uuml;ller vom Hagen
 * @since 1.4
 * @version $Rev$
 */
public class ProgressAdapter {

	public static enum TypeOfProgress {
		/**
		 * store a single kinetic law
		 */
		storeKineticLaw,
		/**
		 * store all kinetic Laws of a Model
		 */
		storeKineticLaws;
	}

	private long startTime = System.currentTimeMillis();
	private int numberOfTotalCalls = 0;
	private int callNr = 0;
	private double percent = 0;
	AbstractProgressBar progressBar;
	TypeOfProgress progressType;

	public void progressOn() {
		if (numberOfTotalCalls > 0) {
			callNr++;
			double curPercent = ((double) 100 * callNr) / numberOfTotalCalls;
			if(percent < curPercent){
				percent = curPercent;
			}
			double remainingTime = 100 * ((System.currentTimeMillis()-startTime)/percent);
			this.progressBar.percentageChanged( (int) percent, remainingTime, "");
		}
	}

	/**
	 * 
	 */
	public ProgressAdapter(AbstractProgressBar progressBar, TypeOfProgress progressType) {
		this.progressBar = progressBar;
		this.progressType = progressType;
	}
	
	public void finished(){
		this.progressBar.finished();
	}

	public void setNumberofTags(Model modelOrig, Model miniModel, SBPreferences prefs){
		switch(progressType){
		case storeKineticLaw:
			numberOfTotalCalls = 0;
			// storeUnits loops
			numberOfTotalCalls += miniModel.getNumUnitDefinitions() + 
			miniModel.getNumCompartments() + 
			miniModel.getNumSpecies();	
			if(prefs.getBoolean(SqueezerOptions.OPT_REMOVE_UNNECESSARY_PARAMETERS_AND_UNITS)){
				// storekineticLaw loops
				numberOfTotalCalls += modelOrig.getNumUnitDefinitions() * 
				(	modelOrig.getNumReactions() +
						modelOrig.getNumParameters() + 
						modelOrig.getNumSpecies() + 
						modelOrig.getNumCompartments()	);

				// removeUnnecessaryParameters loops
				numberOfTotalCalls += modelOrig.getNumParameters() + 
				modelOrig.getNumReactions();			
			}
			// storeParameters loops
			numberOfTotalCalls += miniModel.getNumParameters();
			break;
		case storeKineticLaws:	
			numberOfTotalCalls = 0;
			// storeKineticLaw loop; only the storeParameter function is called as 
			// removeParametersAndStoreUnits is set to false
			numberOfTotalCalls += miniModel.getNumReactions() * miniModel.getNumParameters();
			// storeUnits loops
			numberOfTotalCalls += miniModel.getNumUnitDefinitions() + 
			miniModel.getNumCompartments() + 
			miniModel.getNumSpecies();	
			if(prefs.getBoolean(SqueezerOptions.OPT_REMOVE_UNNECESSARY_PARAMETERS_AND_UNITS)){
				// removeUnnecessaryParameters loops
				numberOfTotalCalls += modelOrig.getNumParameters() + 
				modelOrig.getNumReactions();			
			}
			break;
		}
	}
}
