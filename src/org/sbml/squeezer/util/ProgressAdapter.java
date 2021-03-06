/*
 * $Id$
 * $URL$
 * ---------------------------------------------------------------------
 * This file is part of SBMLsqueezer, a Java program that creates rate
 * equations for reactions in SBML files (http://sbml.org).
 *
 * Copyright (C) 2006-2016 by the University of Tuebingen, Germany.
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

import java.text.MessageFormat;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.sbml.jsbml.Model;
import org.sbml.jsbml.Reaction;

import de.zbit.util.ResourceManager;
import de.zbit.util.progressbar.AbstractProgressBar;

/**
 * 
 * @author Sarah R. M&uuml;ller vom Hagen
 * @version $Rev$
 * @since 2.0
 */
public class ProgressAdapter {
  
  public static final transient ResourceBundle MESSAGES = ResourceManager.getBundle(Bundles.MESSAGES);
  public static final transient ResourceBundle LABELS = ResourceManager.getBundle(Bundles.LABELS);
  
  /**
   * 
   * @author Sarah R. M&uuml;ller vom Hagen
   * @version $Rev$
   * @since 2.0
   */
  public static enum TypeOfProgress {
    /**
     * store a single kinetic law
     */
    storeKineticLaw,
    /**
     * store all kinetic Laws of a Model
     */
    storeKineticLaws,
    /**
     * generate kinetic Laws
     */
    generateLaws,
    /**
     * create mini model
     */
    createMiniModel;
  }
  
  private long startTime = 0;
  private int numberOfTotalCalls = 0;
  private int callNr = 0;
  private double percent = 0d;
  AbstractProgressBar progressBar;
  TypeOfProgress progressType;
  private final Logger logger = Logger.getLogger(ProgressAdapter.class.getName());
  
  /**
   * 
   */
  public void progressOn() {
    if (numberOfTotalCalls > 0) {
      callNr++;
      double curPercent = ((double) 100 * callNr) / numberOfTotalCalls;
      if ((percent < curPercent) && (curPercent <= 100)) {
        percent = curPercent;
      }
      long consumedTime = System.currentTimeMillis() - startTime;
      double totalTime = consumedTime / percent * 100d;
      double remainingTime = totalTime - consumedTime;
      progressBar.percentageChanged((int) percent, remainingTime, "");
    }
  }
  
  /**
   * 
   */
  public ProgressAdapter(AbstractProgressBar progressBar, TypeOfProgress progressType) {
    this.progressBar = progressBar;
    this.progressType = progressType;
    
    startTime = System.currentTimeMillis();
    switch(progressType) {
      case storeKineticLaw:
        logger.log(Level.INFO, MESSAGES.getString("STORE_KINETIC_EQUATION"));
        break;
      case storeKineticLaws:
        logger.log(Level.INFO, MESSAGES.getString("STORE_KINETIC_EQUATIONS"));
        break;
      case generateLaws:
        logger.log(Level.INFO, MESSAGES.getString("GENERATE_KINETIC_EQUATION"));
        break;
      case createMiniModel:
        logger.log(Level.INFO, MESSAGES.getString("CREATE_MINI_MODEL"));
        break;
    }
  }
  
  /**
   * 
   */
  public void finished() {
    progressBar.finished();
    logger.info("    "+MessageFormat.format(MESSAGES.getString("DONE_IN_MS"), (System.currentTimeMillis() - startTime)));
    logger.log(Level.INFO, LABELS.getString("READY"));
  }
  
  /**
   * 
   * @param modelOrig
   * @param miniModel
   * @param removeUnnecessaryParametersAndUnits
   */
  public void setNumberOfTags(Model modelOrig, Model miniModel, boolean removeUnnecessaryParametersAndUnits) {
    switch (progressType) {
      case storeKineticLaw:
        numberOfTotalCalls = 0;
        // storeUnits loops
        numberOfTotalCalls += miniModel.getUnitDefinitionCount() +
            miniModel.getCompartmentCount() +
            miniModel.getSpeciesCount();
        // storeParameters loops
        numberOfTotalCalls += miniModel.getParameterCount();
        if (removeUnnecessaryParametersAndUnits) {
          // storekineticLaw loops
          numberOfTotalCalls += modelOrig.getUnitDefinitionCount();
          // removeUnnecessaryParameters loops
          numberOfTotalCalls += modelOrig.getParameterCount() +
              modelOrig.getReactionCount();
        }
        break;
      case storeKineticLaws:
        numberOfTotalCalls = 0;
        // storeKineticLaw loop; only the storeParameter function is called as
        // removeParametersAndStoreUnits is set to false
        numberOfTotalCalls += miniModel.getReactionCount() * miniModel.getParameterCount();
        // storeUnits loops
        numberOfTotalCalls += miniModel.getUnitDefinitionCount() +
            miniModel.getCompartmentCount() +
            miniModel.getSpeciesCount();
        if (removeUnnecessaryParametersAndUnits) {
          // removeUnnecessaryParameters loops
          numberOfTotalCalls += modelOrig.getParameterCount() +
              modelOrig.getReactionCount();
        }
        break;
      case generateLaws:
        numberOfTotalCalls = 0;
        numberOfTotalCalls += (2 * miniModel.getReactionCount());
        break;
      case createMiniModel:
        boolean create = false;
        
        numberOfTotalCalls = 0;
        if (modelOrig.isSetListOfReactions()) {
          for (Reaction reac : modelOrig.getListOfReactions()) {
            if (reac.isSetKineticLaw()) {
              String formula = reac.getKineticLaw().getMath().toFormula();
              if ((formula == null) || formula.isEmpty() || formula.equals(" ")) {
                create = true;
              }
            }
            if (!reac.isSetKineticLaw() || create) {
              numberOfTotalCalls += reac.getReactantCount();
              numberOfTotalCalls += reac.getProductCount();
              numberOfTotalCalls += reac.getModifierCount();
              if (reac.isSetKineticLaw()) {
                numberOfTotalCalls += modelOrig.getParameterCount();
              }
              numberOfTotalCalls += reac.getReactantCount();
            }
          }
        }
        break;
    }
  }
}
