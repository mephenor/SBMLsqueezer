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

import java.awt.Window;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.ImageIcon;
import javax.swing.UIManager;

import org.sbml.jsbml.SBMLException;
import org.sbml.jsbml.SBMLInputConverter;
import org.sbml.jsbml.SBMLOutputConverter;
import org.sbml.jsbml.util.IOProgressListener;
import org.sbml.jsbml.xml.libsbml.LibSBMLReader;
import org.sbml.jsbml.xml.libsbml.LibSBMLWriter;
import org.sbml.squeezer.gui.SBMLsqueezerUI;
import org.sbml.squeezer.io.IOOptions;
import org.sbml.squeezer.io.SBMLio;
import org.sbml.squeezer.io.SqSBMLReader;
import org.sbml.squeezer.io.SqSBMLWriter;
import org.sbml.squeezer.kinetics.BasicKineticLaw;
import org.sbml.squeezer.kinetics.InterfaceArbitraryEnzymeKinetics;
import org.sbml.squeezer.kinetics.InterfaceBiBiKinetics;
import org.sbml.squeezer.kinetics.InterfaceBiUniKinetics;
import org.sbml.squeezer.kinetics.InterfaceGeneRegulatoryKinetics;
import org.sbml.squeezer.kinetics.InterfaceIntegerStoichiometry;
import org.sbml.squeezer.kinetics.InterfaceIrreversibleKinetics;
import org.sbml.squeezer.kinetics.InterfaceModulatedKinetics;
import org.sbml.squeezer.kinetics.InterfaceNonEnzymeKinetics;
import org.sbml.squeezer.kinetics.InterfaceReversibleKinetics;
import org.sbml.squeezer.kinetics.InterfaceUniUniKinetics;
import org.sbml.squeezer.kinetics.InterfaceZeroProducts;
import org.sbml.squeezer.kinetics.InterfaceZeroReactants;
import org.sbml.squeezer.util.Bundles;
import org.sbml.tolatex.LaTeXOptions;
import org.sbml.tolatex.SBML2LaTeX;
import org.sbml.tolatex.io.LaTeXOptionsIO;

import de.zbit.AppConf;
import de.zbit.Launcher;
import de.zbit.gui.GUIOptions;
import de.zbit.io.FileWalker;
import de.zbit.io.filefilter.SBFileFilter;
import de.zbit.util.ResourceManager;
import de.zbit.util.prefs.KeyProvider;
import de.zbit.util.prefs.SBPreferences;
import de.zbit.util.prefs.SBProperties;
import de.zbit.util.progressbar.ProgressBar;

/**
 * The main program of SBMLsqueezer. This class initializes all required
 * objects, starts the GUI if desired and loads all settings from the user.
 * 
 * @author Andreas Dr&auml;ger
 * @author Nadine Hassis
 * @author Hannes Borch
 * @author Sarah R. M&uuml;ller vom Hagen
 * @since 1.0
 * @version $Rev$
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class SBMLsqueezer extends Launcher implements IOProgressListener {
	
	/**
	 * 
	 */
  public static final transient ResourceBundle MESSAGES = ResourceManager.getBundle(Bundles.MESSAGES);
  /**
   * 
   */
  public static final transient ResourceBundle WARNINGS = ResourceManager.getBundle(Bundles.WARNINGS);
  
  /**
   * The possible location of this class in a jar file if used in plug-in
   * mode.
   */
  public static final String JAR_LOCATION = "plugin" + File.separatorChar;

  /**
   * The package where all kinetic equations are located.
   */
  public static final Package KINETICS_PACKAGE = BasicKineticLaw.class.getPackage();

  /**
   * {@link Set}s of kinetics with certain characteristics.
   */
  private static Set<Class> kineticsArbitraryEnzymeMechanism, kineticsBiUni,
    kineticsGeneRegulatoryNetworks, kineticsIntStoichiometry,
    kineticsIrreversible, kineticsModulated, kineticsNonEnzyme,
    kineticsReversible, kineticsUniUni, kineticsZeroProducts,
    kineticsZeroReactants;
  
  private static Set<Class> kineticsBiBi;

  /**
   * The {@link Logger} for this class.
   */
  private static final transient Logger logger = Logger.getLogger(SBMLsqueezer.class.getName());
  
  static {
		/*
     * Load all available kinetic equations and the user's settings from the
     * configuration file.
     */
  	long time = System.currentTimeMillis();
    logger.info("Loading kinetic equations...");
    kineticsBiBi = new HashSet<Class>();
    kineticsBiUni = new HashSet<Class>();
    kineticsGeneRegulatoryNetworks = new HashSet<Class>();
    kineticsNonEnzyme = new HashSet<Class>();
    kineticsArbitraryEnzymeMechanism = new HashSet<Class>();
    kineticsUniUni = new HashSet<Class>();
    kineticsReversible = new HashSet<Class>();
    kineticsIrreversible = new HashSet<Class>();
    kineticsZeroReactants = new HashSet<Class>();
    kineticsZeroProducts = new HashSet<Class>();
    kineticsModulated = new HashSet<Class>();
    kineticsIntStoichiometry = new HashSet<Class>();
    // removed Reflect for JavaWebStart
    /*Class<BasicKineticLaw> classes[] = Reflect.getAllClassesInPackage(
        KINETICS_PACKAGE.getName(), false, true, BasicKineticLaw.class,
        JAR_LOCATION, true);*/
    Class[] classes = {
    		org.sbml.squeezer.kinetics.AdditiveModelLinear.class, 
    		org.sbml.squeezer.kinetics.AdditiveModelNonLinear.class, 
    		org.sbml.squeezer.kinetics.CommonModularRateLaw.class, 
    		org.sbml.squeezer.kinetics.ConvenienceKinetics.class, 
    		org.sbml.squeezer.kinetics.DirectBindingModularRateLaw.class, 
    		org.sbml.squeezer.kinetics.ForceDependentModularRateLaw.class, 
    		org.sbml.squeezer.kinetics.GeneralizedMassAction.class, 
    		org.sbml.squeezer.kinetics.HSystem.class, 
    		org.sbml.squeezer.kinetics.HillEquation.class, 
    		org.sbml.squeezer.kinetics.HillHinzeEquation.class, 
    		org.sbml.squeezer.kinetics.HillRaddeEquation.class, 
    		org.sbml.squeezer.kinetics.IrrevCompetNonCooperativeEnzymes.class, 
    		org.sbml.squeezer.kinetics.IrrevNonModulatedNonInteractingEnzymes.class, 
    		org.sbml.squeezer.kinetics.MichaelisMenten.class, 
    		org.sbml.squeezer.kinetics.NetGeneratorLinear.class, 
    		org.sbml.squeezer.kinetics.NetGeneratorNonLinear.class, 
    		org.sbml.squeezer.kinetics.OrderedMechanism.class, 
    		org.sbml.squeezer.kinetics.PingPongMechanism.class, 
    		org.sbml.squeezer.kinetics.PowerLawModularRateLaw.class, 
    		org.sbml.squeezer.kinetics.RandomOrderMechanism.class, 
    		org.sbml.squeezer.kinetics.RestrictedSpaceKinetics.class, 
    		org.sbml.squeezer.kinetics.SSystem.class, 
    		org.sbml.squeezer.kinetics.SimultaneousBindingModularRateLaw.class, 
    		org.sbml.squeezer.kinetics.Vohradsky.class, 
    		org.sbml.squeezer.kinetics.Weaver.class, 
    		org.sbml.squeezer.kinetics.ZerothOrderForwardGMAK.class, 
    		org.sbml.squeezer.kinetics.ZerothOrderReverseGMAK.class
    };
    
    for (Class<BasicKineticLaw> c : classes) {
      Set<Class<?>> s = new HashSet<Class<?>>();
      for (Class<?> interf : c.getInterfaces()) {
        s.add(interf);
      }
      if (s.contains(InterfaceIrreversibleKinetics.class)) {
        kineticsIrreversible.add(c);
      }
      if (s.contains(InterfaceReversibleKinetics.class)) {
        kineticsReversible.add(c);
      }
      if (s.contains(InterfaceUniUniKinetics.class)) {
        kineticsUniUni.add(c);
      }
      if (s.contains(InterfaceBiUniKinetics.class)) {
        kineticsBiUni.add(c);
      }
      if (s.contains(InterfaceBiBiKinetics.class)) {
        kineticsBiBi.add(c);
      }
      if (s.contains(InterfaceArbitraryEnzymeKinetics.class)) {
        kineticsArbitraryEnzymeMechanism.add(c);
      }
      if (s.contains(InterfaceGeneRegulatoryKinetics.class)) {
        kineticsGeneRegulatoryNetworks.add(c);
      }
      if (s.contains(InterfaceNonEnzymeKinetics.class)) {
        kineticsNonEnzyme.add(c);
      }
      if (s.contains(InterfaceZeroReactants.class)) {
        kineticsZeroReactants.add(c);
      }
      if (s.contains(InterfaceZeroProducts.class)) {
        kineticsZeroProducts.add(c);
      }
      if (s.contains(InterfaceModulatedKinetics.class)) {
        kineticsModulated.add(c);
      }
      if (s.contains(InterfaceIntegerStoichiometry.class)) {
        kineticsIntStoichiometry.add(c);
      }
    }
    logger.info(MessageFormat.format(MESSAGES.getString("DONE_IN_MS"), (System.currentTimeMillis() - time)));
  }

  /**
   * 
   */
  private static SBMLInputConverter reader = null;

  /**
   * Generated serial version identifier.
   */
  private static final long serialVersionUID = 8751196023375780898L;
  
  /**
   * 
   */
  private static SBMLOutputConverter writer = null;
  
  /**
   * 
   * @return
   */
  public static List<Class<? extends KeyProvider>> getInteractiveConfigOptions() {
  	List<Class<? extends KeyProvider>> list = new ArrayList<Class<? extends KeyProvider>>(4);
    list.add(SqueezerOptions.class);
    list.add(LaTeXOptions.class);
    return list;
	}

  
  /**
   * @return the kineticsArbitraryEnzymeMechanism
   */
  public static Set<Class> getKineticsArbitraryEnzymeMechanism() {
    return kineticsArbitraryEnzymeMechanism;
  }

  /**
   * 
   * @return
   */
  public static Set<Class> getKineticsIrreversibleArbitraryEnzymeMechanism() {
	  return getKineticsIrreversible(kineticsArbitraryEnzymeMechanism);
  }

  /**
   * 
   * @return
   */
  public static Set<Class> getKineticsReversibleArbitraryEnzymeMechanism() {
  	return getKineticsReversible(kineticsArbitraryEnzymeMechanism);
  }

  /**
   * @return the kineticsBiBi
   */
  public static Set<Class> getKineticsBiBi() {
    return kineticsBiBi;
  }
  
  /**
   * 
   * @return
   */
	public static Set<Class> getKineticsReversibleBiBi() {
	  return getKineticsReversible(kineticsBiBi);
	}
	
	/**
	 * 
	 * @return
	 */
	public static Set<Class> getKineticsIrreversibleBiBi() {
		return getKineticsIrreversible(kineticsBiBi);
	}
	
	/**
	 * 
	 * @param type
	 * @return
	 */
	public static Set<Class> getKineticsReversible(Set<Class> type) {
	  Set<Class> rev = new HashSet<Class>(type);
	  rev.retainAll(kineticsReversible);
	  return rev;
	}
	
	/**
	 * 
	 * @param type
	 * @return
	 */
	public static Set<Class> getKineticsIrreversible(Set<Class> type) {
	  Set<Class> rev = new HashSet<Class>(type);
	  rev.retainAll(kineticsIrreversible);
	  return rev;
	}

  /**
   * @return the kineticsBiUni
   */
  public static Set<Class> getKineticsBiUni() {
    return kineticsBiUni;
  }

  /**
   * @return the kineticsGeneRegulatoryNetworks
   */
  public static Set<Class> getKineticsGeneRegulatoryNetworks() {
    return kineticsGeneRegulatoryNetworks;
  }

  /**
   * @return the kineticsIntStoichiometry
   */
  public static Set<Class> getKineticsIntStoichiometry() {
    return kineticsIntStoichiometry;
  }

  /**
   * @return the kineticsIrreversible
   */
  public static Set<Class> getKineticsIrreversible() {
    return kineticsIrreversible;
  }

  /**
   * @return the kineticsModulated
   */
  public static Set<Class> getKineticsModulated() {
    return kineticsModulated;
  }

  /**
   * @return the kineticsNonEnzyme
   */
  public static Set<Class> getKineticsNonEnzyme() {
    return kineticsNonEnzyme;
  }
  
  /**
   * @return the kineticsReversible
   */
  public static Set<Class> getKineticsReversible() {
    return kineticsReversible;
  }
  
  /**
   * @return the kineticsUniUni
   */
  public static Set<Class> getKineticsUniUni() {
    return kineticsUniUni;
  }

  /**
   * @return the kineticsZeroProducts
   */
  public static Set<Class> getKineticsZeroProducts() {
    return kineticsZeroProducts;
  }

  /**
   * @return the kineticsZeroReactants
   */
  public static Set<Class> getKineticsZeroReactants() {
    return kineticsZeroReactants;
  }
  
  /**
	   * Returns an array of Strings that can be interpreted as enzymes. In
	   * particular this array will contain those configuration keys as strings
	   * for which in the current configuration the corresponding value is set to
	   * true.
	   * 
	   * @return
	   */
	  public static String[] getPossibleEnzymeTypes() {
	    logger.log(Level.INFO, MESSAGES.getString("LOADING_USER_SETTINGS"));
	    SBPreferences preferences = new SBPreferences(SqueezerOptions.class);
	    logger.log(Level.INFO, "    " + MESSAGES.getString("DONE"));
	    Set<String> enzymeTypes = new HashSet<String>();
	    String prefix = "POSSIBLE_ENZYME_";
	    for (Object key : preferences.keySet()) {
	      if (key.toString().startsWith(prefix)) {
	        if (preferences.getBoolean(key)) {
	          enzymeTypes.add(key.toString().substring(prefix.length()));
	        }
	      }
	    }
	    return enzymeTypes.toArray(new String[] {});
	  }
	  
	  /**
		 * Loads the required icons for SBMLsqueezer into the {@link UIManager}.
		 */
		public static void initImages() {
			String iconPaths[] = {
					"ICON_DIAGRAM_TINY.png",
					"ICON_DOWN_ARROW.png",
					"ICON_DOWN_ARROW_TINY.png",
					"ICON_FORWARD.png",
					"ICON_GEAR_TINY.png",
					"ICON_LEFT_ARROW.png",
					"ICON_LEMON_SMALL.png",
					"ICON_LEMON_TINY.png",
					"ICON_LOGO_SMALL.png",
					"ICON_PICTURE_TINY.png",
					"ICON_RIGHT_ARROW.png",
					"ICON_RIGHT_ARROW_TINY.png",
					"ICON_STABILITY_SMALL.png",
					"ICON_STRUCTURAL_MODELING_TINY.png",
					"IMAGE_LEMON.png"
			    };
		    for (String path : iconPaths) {
		      URL u = SBMLsqueezer.class.getResource("resources/img/" + path);
		      if (u!=null) {
		        UIManager.put(path.substring(0, path.lastIndexOf('.')), new ImageIcon(u));
		      }
		    }
		}

	/**
	   * Does initialization for creating a SBMLsqueezer Object.
	   * Checks if libSBML is available and initializes the Reader/Writer.
	   * @param tryLoadingLibSBML 
	   * @param reader
	   * @param writer
	   */
	  private static void initializeReaderAndWriter(boolean tryLoadingLibSBML){
	    boolean libSBMLAvailable = false;
	    if (tryLoadingLibSBML) {
	    	try {
	    		// In order to initialize libSBML, check the java.library.path.
	    		System.loadLibrary("sbmlj");
	    		// Extra check to be sure we have access to libSBML:
	    		Class.forName("org.sbml.libsbml.libsbml");
	    		logger.info(MESSAGES.getString("LOADING_LIBSBML"));
	    		libSBMLAvailable = true;
	    	} catch (Error e) {
	    	} catch (Throwable e) {
	    	}
	    }
	    if (libSBMLAvailable) {
	      reader = new LibSBMLReader();
	      writer = new LibSBMLWriter();
	    } else {
	      logger.info(MESSAGES.getString("LOADING_JSBML"));
	      reader = new SqSBMLReader();
	      writer = new SqSBMLWriter();
	    }
	  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    new SBMLsqueezer(args);
  }
  
  /**
   * 
   */
  private SBMLio sbmlIo;

  /**
   * 
   */
  public SBMLsqueezer() {
    super();
  }


  /**
   * This constructor allows the integration of SBMLsqueezer into third-party
   * programs, i.e., as a CellDesigner plug-in.
   * 
   * @param sbmlReader
   * @param sbmlWriter
   */
  public SBMLsqueezer(SBMLInputConverter sbmlReader,
      SBMLOutputConverter sbmlWriter) {
  	this();
    sbmlIo = new SBMLio(sbmlReader, sbmlWriter);
    // sbmlIo.addIOProgressListener(this);
  }

  /**
   * 
   * @param args
   */
  public SBMLsqueezer(String[] args) {
    super(args);
  }

  /* (non-Javadoc)
   * @see de.zbit.Launcher#commandLineMode(de.zbit.AppConf)
   */
  public void commandLineMode(AppConf appConf) {
    SBProperties properties = appConf.getCmdArgs();
    if ((getSBMLIO().getNumErrors() > 0)
        && properties.getBoolean(SqueezerOptions.SHOW_SBML_WARNINGS)) {
      for (SBMLException exc : getSBMLIO().getWarnings()) {
        logger.log(Level.WARNING, exc.getMessage());
      }
    }
    if (properties.containsKey(IOOptions.SBML_OUT_FILE)) {
      try {
        squeeze(properties.get(IOOptions.SBML_IN_FILE).toString(),
          properties.get(IOOptions.SBML_OUT_FILE).toString());
      } catch (Throwable e) {
        e.printStackTrace();
      }
    }
  }

  /* (non-Javadoc)
   * @see de.zbit.Launcher#getAppName()
   */
  @Override
  public String getAppName() {
    return getClass().getSimpleName();
  }

  /* (non-Javadoc)
   * @see de.zbit.Launcher#getCmdLineOptions()
   */
  public List<Class<? extends KeyProvider>> getCmdLineOptions() {
    List<Class<? extends KeyProvider>> list = new ArrayList<Class<? extends KeyProvider>>(4);
    list.add(IOOptions.class);
    list.add(SqueezerOptions.class);
    list.add(GUIOptions.class);
    list.add(LaTeXOptions.class);
    return list;
  }


	/* (non-Javadoc)
   * @see de.zbit.Launcher#getInteractiveOptions()
   */
  public List<Class<? extends KeyProvider>> getInteractiveOptions() {
    return getInteractiveConfigOptions();
  }
  
  /* (non-Javadoc)
   * @see de.zbit.Launcher#getLogPackages()
   */
  public String[] getLogPackages() {
    return new String[] {"org.sbml", "de.zbit"};
  }
  
  /**
   * 
   * @return
   */
  public SBMLio getSBMLIO() {
    if (sbmlIo == null) {
      sbmlIo = new SBMLio(reader, writer);
    }
    return sbmlIo;
  }
  
  /* (non-Javadoc)
   * @see de.zbit.Launcher#getURLlicenseFile()
   */
  public URL getURLlicenseFile() {
    URL url = null;
    try {
      url = new URL("http://www.gnu.org/licenses/gpl-3.0-standalone.html");
    } catch (MalformedURLException exc) {
      logger.log(Level.FINER, exc.getLocalizedMessage(), exc);
    }
    return url;
  }
  
  /* (non-Javadoc)
   * @see de.zbit.Launcher#getURLOnlineUpdate()
   */
  public URL getURLOnlineUpdate() {
    URL url = null;
    try {
      url = new URL("http://www.cogsys.cs.uni-tuebingen.de/software/SBMLsqueezer/downloads/");
    } catch (MalformedURLException exc) {
      logger.log(Level.FINER, exc.getLocalizedMessage(), exc);
    }
    return url;
  }
  
  /* (non-Javadoc)
   * @see de.zbit.Launcher#getVersionNumber()
   */
  public String getVersionNumber() {
    return "1.4.0";
  }
  
  /* (non-Javadoc)
   * @see de.zbit.Launcher#getYearOfProgramRelease()
   */
  public short getYearOfProgramRelease() {
    return (short) 2012;
  }
  
  /* (non-Javadoc)
   * @see de.zbit.Launcher#getYearWhenProjectWasStarted()
   */
  public short getYearWhenProjectWasStarted() {
    return (short) 2006;
  }
  
  /* (non-Javadoc)
   * @see de.zbit.Launcher#initGUI(de.zbit.AppConf)
   */
  public Window initGUI(AppConf appConf) {
    SBProperties properties = appConf.getCmdArgs();
    if (properties.containsKey(IOOptions.SBML_IN_FILE)) {
      readSBMLSource(properties.get(IOOptions.SBML_IN_FILE));
    }
    return new SBMLsqueezerUI(getSBMLIO(), appConf);
  }
  
  /* (non-Javadoc)
   * @see org.sbml.jsbml.util.IOProgressListener#ioProgressOn(java.lang.Object)
   */
  public void ioProgressOn(Object currObject) {
    if (currObject != null) {
      logger.info(currObject.toString());
    }
  }
  
  /**
   * 
   * @param sbmlSource
   */
  public void readSBMLSource(Object sbmlSource) {
    long time = System.currentTimeMillis();
    logger.info(MESSAGES.getString("READING_SBML_FILE"));
    try {
      getSBMLIO().convertModel(sbmlSource);
      logger.info(MessageFormat.format(MESSAGES.getString("DONE_IN_MS"), (System.currentTimeMillis() - time)));
    } catch (Exception exc) {
      logger.log(Level.WARNING, String.format(WARNINGS.getString("CANT_READ_MODEL"), exc.getLocalizedMessage()));
    }
  }
  
  /* (non-Javadoc)
   * @see de.zbit.Launcher#setUp()
   */
  @Override
  protected void setUp() {
  	if ((reader == null) && (writer == null)) {
  		initializeReaderAndWriter(getAppConf().getCmdArgs().getBooleanProperty(
  			IOOptions.TRY_LOADING_LIBSBML));
  	}
  }

  /**
   * 
   * @param source
   * @param outFile
   * @param showProgress
   * @throws Throwable
   */
  public void squeeze(File source, File outFile, boolean showProgress) throws Throwable {
  	long workTime = System.currentTimeMillis();
  	readSBMLSource(source.getAbsolutePath());
  	boolean errorFatal = false;
  	SBMLException exception = null;
  	for (SBMLException exc : sbmlIo.getWarnings())
  		if (exc.isFatal() || exc.isXML() || exc.isError()) {
  			errorFatal = true;
  			exception = exc;
  		}
  	if (errorFatal) {
  		throw new SBMLException(exception);
  	} else if (!sbmlIo.getListOfOpenedFiles().isEmpty()) {
  		
  		KineticLawGenerator klg = new KineticLawGenerator(sbmlIo.getSelectedModel());
  		ProgressBar progressBar = null;
  		
  		if (showProgress) {
  			progressBar = new ProgressBar(0);
  			klg.setProgressBar(progressBar);
  		}
  		long time = System.currentTimeMillis();
  		// TODO: Localize!
  		logger.info("Creating kinetic laws.");
  		klg.generateLaws();
  		logger.info(MessageFormat.format(MESSAGES.getString("DONE_IN_MS"), (System.currentTimeMillis() - time)));
  		
  		if (showProgress) {
  			progressBar = new ProgressBar(0);
  			klg.setProgressBar(progressBar);
  		}
  		klg.storeKineticLaws();
  		
  		time = System.currentTimeMillis();
  		logger.info(MESSAGES.getString("SAVING_TO_FILE"));
  		sbmlIo.saveChanges(this);
  		if ((outFile != null)
  				&& (SBFileFilter.createSBMLFileFilter().accept(outFile))) {
  			sbmlIo.writeSelectedModelToSBML(outFile.getAbsolutePath());
  			logger.info(MessageFormat.format(MESSAGES.getString("DONE_IN_MS"), (System.currentTimeMillis() - time)));
  			SBPreferences preferences = new SBPreferences(SqueezerOptions.class);
  			if (preferences.getBoolean(SqueezerOptions.SHOW_SBML_WARNINGS)) {
  				for (SBMLException exc : sbmlIo.getWriteWarnings()) {
  					logger.log(Level.WARNING, exc.getMessage());
  				}
  			}
  		} else {
  			logger.log(Level.WARNING, WARNINGS.getString("OUTPUT_ERROR"));
  		}
  	} else {
  		logger.log(Level.WARNING, WARNINGS.getString("FILE_CONTAINS_NO_MODEL"));
  	}
  	// TODO: Localize!
  	logger.info((System.currentTimeMillis() - workTime)/1000d + " s needed for squeezing file " + source.getName() + '.');
  }


  /**
   * Reads in the given SBML file, squeezes kinetic equations in and writes
   * the result back to the given SBML file. This method only works if
   * SBMLsqueezer is used as a stand-alone program.
   * 
   * @param sbmlSource
   *            the path to a file that contains SBML code or another object
   *            that can be read by the current reader used by SBMLsqueezer.
   * @param outfile
   *            The absolute path to a file where the result should be stored.
   *            This must be a file that ends with .xml or .sbml (case
   *            insensitive).
   * @throws Throwable
   */
  public void squeeze(Object sbmlSource, String outfile) throws Throwable {
  	File outFile = outfile != null ? new File(outfile) : null;
  	File inFile = sbmlSource != null ? new File(sbmlSource.toString()) : null;
  	// TODO: Localize!
  	logger.info("Scanning input files");
  	Map<File, String> ioPairs = FileWalker.filterAndCreate(inFile, outFile, SBFileFilter.createSBMLFileFilter(), true);
  	if (ioPairs.size() == 0) {
  		logger.info(MESSAGES.getString("EMPTY_INPUT_FILE_LIST"));
  	} else {
  		for (Map.Entry<File, String> entry : ioPairs.entrySet()) {
  			try {
  				inFile = entry.getKey();
  				outFile = new File(entry.getValue());
  				logger.info(MessageFormat.format("Squeezing file {0} into {1}", inFile.getAbsolutePath(), outFile.getAbsolutePath()));
  				squeeze(inFile, outFile, false);
  			} catch (Throwable t) {
  				logger.log(Level.SEVERE, MessageFormat.format(
  					WARNINGS.getString("SQUEEZE_ERROR"),
  					entry.getKey().getAbsolutePath(), entry.getValue()), t);
  			}
  		}
  	}
  }

  /**
   * Convenient method that writes a LaTeX file from the given SBML source.
   * 
   * @param sbmlInfile
   * @param latexFile
   * @throws IOException
   * @throws SBMLException 
   */
  public void toLaTeX(Object sbmlSource, String latexFile) throws IOException, SBMLException {
    readSBMLSource(sbmlSource);
    SBPreferences prefsLaTeX = SBPreferences.getPreferencesFor(LaTeXOptionsIO.class);
    String dir = prefsLaTeX.get(LaTeXOptionsIO.LATEX_DIR).toString();
    if (latexFile != null) {
      File out = new File(latexFile);
      if (SBFileFilter.createTeXFileFilter().accept(out)) {
        String path = out.getParent();
        if (!path.equals(dir)) {
          prefsLaTeX.put(LaTeXOptionsIO.LATEX_DIR, path);
        }
        if (!out.exists()) {
          long time = System.currentTimeMillis();
          logger.info(MESSAGES.getString("WRITING_LATEX_OUTPUT"));
          SBML2LaTeX.convert(sbmlIo.getSelectedModel(), out);
          logger.info(MessageFormat.format(
        		  MESSAGES.getString("DONE_IN_MS"), (System.currentTimeMillis() - time)) + "\n");
        }
      } else {
        logger.log(Level.WARNING, MessageFormat.format(
        		WARNINGS.getString("INVALID_TEX_FILE"), latexFile) +"\n");
      }
    } else {
      logger.log(Level.WARNING, WARNINGS.getString("NO_TEX_FILE_PROVIDED"));
    }
  }

  /**
   * 
   * @return
   */
	public static Set<Class> getKineticsReversibleBiUni() {
		return getKineticsReversible(kineticsBiUni);
	}

	/**
	 * 
	 * @return
	 */
	public static Set<Class> getKineticsIrreversibleBiUni() {
		return getKineticsIrreversible(kineticsBiUni);
	}

	/**
	 * 
	 * @return
	 */
	public static Set<Class> getKineticsReversibleUniUni() {
		return getKineticsReversible(kineticsUniUni);
	}

	/**
	 * 
	 * @return
	 */
	public static Set<Class> getKineticsIrreversibleUniUni() {
		return getKineticsIrreversible(kineticsUniUni);
	}

	/**
	 * 
	 * @return
	 */
	public static Set<Class> getKineticsReversibleNonEnzyme() {
		return getKineticsReversible(kineticsNonEnzyme);
	}

	/**
	 * 
	 * @return
	 */
	public static Set<Class> getKineticsIrreversibleNonEnzyme() {
		return getKineticsIrreversible(kineticsNonEnzyme);
	}
  
}
