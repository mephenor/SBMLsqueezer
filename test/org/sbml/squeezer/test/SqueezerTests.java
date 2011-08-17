package org.sbml.squeezer.test;


import java.io.File;
import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import junit.framework.TestCase;

import org.junit.Test;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.SBMLException;
import org.sbml.jsbml.SBMLInputConverter;
import org.sbml.jsbml.SBMLOutputConverter;
import org.sbml.jsbml.xml.libsbml.LibSBMLReader;
import org.sbml.jsbml.xml.libsbml.LibSBMLWriter;
import org.sbml.squeezer.KineticLawGenerator;
import org.sbml.squeezer.SBMLsqueezer;
import org.sbml.squeezer.io.SBMLio;
import org.sbml.squeezer.io.SqSBMLReader;
import org.sbml.squeezer.io.SqSBMLWriter;

import de.zbit.io.SBFileFilter;

public class SqueezerTests extends TestCase{

  //private String testPath = System.getProperty("user.dir") + "/files/tests/sbml-test-cases-2011-06-15/cases/semantic/00002";
	//String testPath = System.getProperty("user.dir") + "/files/tests/sbml-test-cases-2011-06-15";
  private String testPath = System.getProperty("user.home") + "/workspace/SBMLsimulatorCore/files/SBML_test_cases/cases/semantic/00001";
	/**
	 * List of test files
	 */
	private List<File> listOfFiles;
	/**
	 * List of all models
	 */
	private List<Model> listOfModels = new LinkedList<Model>();
	/**
	 * List of all sBMLsqueezers
	 */
	private List<SBMLsqueezer> listOfsqueezers = new LinkedList<SBMLsqueezer>();
	
	private List<KineticLawGenerator> listOfKineticLawGenerators = new LinkedList<KineticLawGenerator>();
	
	private static final Logger logger = Logger.getLogger(SqueezerTests.class.getName());
	
	public SqueezerTests(){
		long time = System.currentTimeMillis();
		logger.info("search for files...");
		List<File> tempList = new LinkedList<File>();
		tempList.add(new File(testPath));
		listOfFiles = new LinkedList<File>();
		File f = null;
		while (!tempList.isEmpty()) {
			f = tempList.remove(0);
			if (f.isFile()) {
				if (SBFileFilter.isSBMLFile(f) && !(f.getName().contains("sedml"))) {
					listOfFiles.add(f);
				}
			} else if (f.isDirectory()) {
				tempList.addAll(Arrays.asList(f.listFiles()));
			}
		}
		logger.info("    done in " + (System.currentTimeMillis() - time) + " ms.");
	}
	
	/**
	 * test if the given directory contains XML Files, if not, then the 
	 * results of the import and export tests can be ignored
	 */
	@Test
	public void testDirectoryForXMLFiles(){
		long time = System.currentTimeMillis();
		logger.info("test if there are files in the given directory...");
		int numberOfFiles = listOfFiles.size();
		assertTrue(numberOfFiles > 0);	
		logger.info("    done in " + (System.currentTimeMillis() - time) + " ms.");
	}
	
	/**
	 * - tests the files can be imported as models
	 * - 
	 */
	@Test
	public void testModels() {
		long time = System.currentTimeMillis();
		logger.info("Generate SBMLreader and SBMLwriter.");
		boolean libSBMLAvailable = false;
		try {
			// In order to initialize libSBML, check the java.library.path.
			System.loadLibrary("sbmlj");
			// Extra check to be sure we have access to libSBML:
			Class.forName("org.sbml.libsbml.libsbml");
			libSBMLAvailable = true;
		} catch (Error e) {
		} catch (Throwable e) {
		} 
		SBMLInputConverter reader = null;
		SBMLOutputConverter writer = null;
		if (!libSBMLAvailable) {
			reader = new SqSBMLReader() ;
			writer = new SqSBMLWriter() ;
		} else {
			reader = new LibSBMLReader();
			writer = new LibSBMLWriter();
		}
		logger.info("    done in " + (System.currentTimeMillis() - time) + " ms.");
		
		
		
		time = System.currentTimeMillis();
		logger.info("Generate SBMLio and SBMLsqueezerUI.");
		SBMLio io = new SBMLio(reader,writer);
		logger.info("Test file import and model conversion.");
		boolean failed;
		Model model;
		KineticLawGenerator klg;
		for(int i=0; i<listOfFiles.size(); i++){
			// try to extract models from files
			failed = false;
			File f = listOfFiles.get(i);
			try {
				logger.info("(file to model): " + f.getAbsolutePath());
				model = io.convertModel(f.getAbsolutePath());
				listOfModels.add(model);
			} catch (Throwable e) {
				logger.log(Level.WARNING, "failed to convert Model: ");
				logger.log(Level.WARNING, f.getAbsolutePath(), e);
				failed = true;
				fail();
			}
			// try to generate kinetic laws for a model
			if(!failed){
				try {
					logger.info("(KineticLawGenerator for a model): " + f.getAbsolutePath());
					klg = new KineticLawGenerator(listOfModels.get(i));
					listOfKineticLawGenerators.add(klg);
				} catch (Throwable e) {
					logger.log(Level.WARNING, "failed to generate kinetic equations: ");
					logger.log(Level.WARNING, f.getAbsolutePath(), e);
					failed = true;
					fail();
				}
			}
			// try to generate kinetic laws for the reactions
			if(!failed){
				logger.info("(KineticLawGenerator for reaction)"+ f.getAbsolutePath());
				for(Reaction reac : listOfModels.get(i).getListOfReactions()){
					try {
						logger.info("    (Reaction): "+reac.getId());
						new KineticLawGenerator(listOfModels.get(i),reac.getId());
					} catch (Throwable e) {
						logger.log(Level.WARNING, "failed to generate kinetic equation for reaction: ");
						logger.log(Level.WARNING, "file: "+f.getAbsolutePath());
						logger.log(Level.WARNING, "id: "+reac.getId(), e);
						failed = true;
						fail();
					}
				}
			}			
			// try to get the miniModels
			if(!failed){
				try {
					logger.info("(get MiniModel for a model): " + f.getAbsolutePath());
					listOfKineticLawGenerators.get(i).getMiniModel();
				} catch (Throwable e) {
					logger.log(Level.WARNING, "failed to generate the MiniModel: ");
					logger.log(Level.WARNING, f.getAbsolutePath(), e);
					failed = true;
					fail();
				}
			}
			
			
			
		}
		logger.info("    done in " + (System.currentTimeMillis() - time) + " ms.");
		
		
		/*
		//time = System.currentTimeMillis();
		logger.info("test squeezer.");
		for(File file : listOfFiles) {
			// test models
			try {
				SBMLsqueezer squeezer = new SBMLsqueezer(reader, writer);
				listOfsqueezers.add(squeezer);
				logger.info("(squeeze file): " + file.getAbsolutePath());
				squeezer.squeeze(file.getAbsolutePath(), System.getProperty("user.home") + "/test.xml");
				
			} catch (Throwable e) {
				logger.log(Level.WARNING, file.getAbsolutePath(), e);
				fail();
			}
		}
		//logger.info("    done in " + (System.currentTimeMillis() - time) + " ms.");
		*/
		
	}
	
	/**
	 * test, if the libSBML is available.
	 * Note that a failure of this test does not lead to any problems
	 * as the functional components of this library are completely
	 * substituted. 
	 */
	@Test
	public void testLibSBML() {
		boolean libSBMLAvailable = false;
		try {
			// In order to initialize libSBML, check the java.library.path.
			System.loadLibrary("sbmlj");
			// Extra check to be sure we have access to libSBML:
			Class.forName("org.sbml.libsbml.libsbml");
			libSBMLAvailable = true;
		} catch (Error e) {
		} catch (Throwable e) {
		} 
		assertTrue(libSBMLAvailable);
	}
	
	/**
	 * test if the program can be started with default settings
	 * @throws MalformedURLException 
	 */
	@Test
	public void testProgramStart() {
		/*
		try {
			SBMLsqueezer.main(new String[]{});
		} catch (MalformedURLException e) {
			e.printStackTrace();
			fail();
		}
		*/
	}
	
	
}