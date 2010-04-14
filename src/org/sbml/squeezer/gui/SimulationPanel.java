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
package org.sbml.squeezer.gui;

import java.awt.AWTException;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.plugins.jpeg.JPEGImageWriteParam;
import javax.imageio.stream.FileImageOutputStream;
import javax.swing.AbstractCellEditor;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import org.sbml.jsbml.Compartment;
import org.sbml.jsbml.ListOf;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.Parameter;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.Species;
import org.sbml.jsbml.State;
import org.sbml.jsbml.Symbol;
import org.sbml.jsbml.UnitDefinition;
import org.sbml.squeezer.SBMLsqueezer;
import org.sbml.squeezer.io.SBFileFilter;
import org.sbml.squeezer.math.SBMLinterpreter;
import org.sbml.squeezer.util.HTMLFormula;

import au.com.bytecode.opencsv.CSVReader;
import eva2.gui.FunctionArea;
import eva2.tools.math.des.AbstractDESSolver;

/**
 * @author Andreas Dr&auml;ger
 * @since 1.4
 * 
 */
class ColorEditor extends AbstractCellEditor implements TableCellEditor,
		ActionListener {
	/**
	 * 
	 */
	public static final String EDIT = "edit";
	/**
	 * Generated serial version identifier.
	 */
	private static final long serialVersionUID = -3645125690115981580L;
	/**
	 * 
	 */
	private JButton button;
	/**
	 * 
	 */
	private JColorChooser colorChooser;
	/**
	 * 
	 */
	private Color currentColor;
	/**
	 * 
	 */
	private JDialog dialog;

	/**
	 * 
	 */
	public ColorEditor() {
		button = new JButton();
		button.setActionCommand(EDIT);
		button.addActionListener(this);
		button.setBorderPainted(false);

		// Set up the dialog that the button brings up.
		colorChooser = new JColorChooser();
		dialog = JColorChooser.createDialog(button, "Pick a Color", true,
				colorChooser, this, null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		if (EDIT.equals(e.getActionCommand())) {
			// The user has clicked the cell, so
			// bring up the dialog.
			button.setBackground(currentColor);
			colorChooser.setColor(currentColor);
			dialog.setVisible(true);

			fireEditingStopped(); // Make the renderer reappear.

		} else { // User pressed dialog's "OK" button.
			currentColor = colorChooser.getColor();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.CellEditor#getCellEditorValue()
	 */
	public Object getCellEditorValue() {
		return currentColor;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * javax.swing.table.TableCellEditor#getTableCellEditorComponent(javax.swing
	 * .JTable, java.lang.Object, boolean, int, int)
	 */
	public Component getTableCellEditorComponent(JTable table, Object value,
			boolean isSelected, int row, int column) {
		currentColor = (Color) value;
		return button;
	}
}

/**
 * 
 * @author Andreas Dr&auml;ger
 * @date 2010-04-08
 * @since 1.4
 */
class MyDefaultTableModel extends DefaultTableModel {

	/**
	 * Generated serial version identifier
	 */
	private static final long serialVersionUID = 6339470859385085061L;

	/**
	 * 
	 * @param data
	 * @param columnNames
	 */
	public MyDefaultTableModel(Object[][] data, String[] columnNames) {
		super(data, columnNames);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.table.AbstractTableModel#getColumnClass(int)
	 */
	@Override
	public Class<?> getColumnClass(int c) {
		return getValueAt(0, c).getClass();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.table.DefaultTableModel#isCellEditable(int, int)
	 */
	@Override
	public boolean isCellEditable(int row, int column) {
		return false;
	}
}

/**
 * @author Andreas Dr&auml;ger
 * @since 1.4
 * @date 2010-04-06
 * 
 */
public class SimulationPanel extends JPanel implements ActionListener,
		ChangeListener, ItemListener, TableModelListener {

	/**
	 * Commands that can be understood by this dialog.
	 * 
	 * @author Andreas Dr&auml;ger
	 * 
	 */
	public static enum Command {
		/**
		 * Open file with experimental data.
		 */
		OPEN_DATA,
		/**
		 * Save the plot as an image.
		 */
		SAVE_PLOT_IMAGE,
		/**
		 * Start a new simulation with the current settings.
		 */
		SIMULATION_START,
		/**
		 * Save the results of the simulation to a CSV file.
		 */
		SAVE_SIMULATION,
		/**
		 * Adjust user's preferences
		 */
		SETTINGS
	}

	/**
	 * Generated serial version identifier
	 */
	private static final long serialVersionUID = -7278034514446047207L;

	/**
	 * 
	 * @param index
	 * @return
	 */
	public static Color indexToColor(int index) {
		Color c = Color.black;
		int k = index % 10;
		switch (k) {
		case 0:
			c = Color.black;
			break;
		case 1:
			c = Color.red;
			break;
		case 2:
			c = Color.blue;
			break;
		case 3:
			c = Color.pink;
			break;
		case 4:
			c = Color.green;
			break;
		case 5:
			c = Color.gray;
			break;
		case 6:
			c = Color.magenta;
			break;
		case 7:
			c = Color.cyan;
			break;
		case 8:
			c = Color.orange;
			break;
		case 9:
			c = Color.darkGray;
			break;
		}
		return c;
	}

	private int distanceFunc;

	/**
	 * The names in all tables for the columns (time, compartment, species,
	 * parameters).
	 */
	private String colNames[];
	/**
	 * Compression factor for JPEG output
	 */
	private float compression;

	/**
	 * Table for experimental data.
	 */
	private JTable expTable;

	/**
	 * The index of the class name of the solver to be used
	 */
	private JComboBox solvers;

	/**
	 * Table that contains the legend of this plot.
	 */
	private JTable legend;
	/**
	 * Whether or not to plot in a logarithmic scale.
	 */
	private JCheckBox logScale;

	/**
	 * 
	 */
	private double maxCompartmentValue;

	/**
	 * The maximal allowable parameter value.
	 */
	private double maxParameterValue;

	/**
	 * 
	 */
	private double maxSpeciesValue;

	/**
	 * Maximal allowable number of integration steps per time unit
	 */
	private int maxStepsPerUnit;

	/**
	 * The maximal allowable simulation time
	 */
	private double maxTime;

	/**
	 * Model to be simulated
	 */
	private Model model;

	/**
	 * Standard directory to open data files.
	 */
	private String opendir;

	/**
	 * The step size for the spinner in the interactive parameter scan.
	 */
	private double paramStepSize;

	/**
	 * Plot area
	 */
	private FunctionArea plot;

	/**
	 * This is the quote character in CSV files
	 */
	private char quoteChar;

	/**
	 * 
	 */
	private String saveDir;

	/**
	 * This is the separator char in CSV files
	 */
	private char separatorChar;

	/**
	 * Decides whether or not a grid should be displayed in the plot.
	 */
	private JCheckBox showGrid;

	/**
	 * Table for the simulation data.
	 */
	private JTable simTable;

	/**
	 * The integrator
	 */
	private AbstractDESSolver solver;

	/**
	 * Step size of the simulator
	 */
	private double stepSize;

	/**
	 * The spinner to change the number of integration steps.
	 */
	private SpinnerNumberModel stepsModel;

	/**
	 * Simulation start time
	 */
	private SpinnerNumberModel t1;

	/**
	 * Simulation end time
	 */
	private SpinnerNumberModel t2;

	private double maxSpinVal = 1E10;

	/**
	 * Decides whether or not to add a legend to the plot.
	 */
	private JCheckBox showLegend;
	private SpinnerNumberModel[] spinModSymbol;

	/**
	 * 
	 * @param owner
	 * @param model
	 * @throws InvocationTargetException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 * @throws NoSuchMethodException
	 * @throws IllegalArgumentException
	 * @throws SecurityException
	 */
	public SimulationPanel(Model model) throws SecurityException,
			IllegalArgumentException, NoSuchMethodException,
			InstantiationException, IllegalAccessException,
			InvocationTargetException {
		this(model, 5, 0.1, "files/tests/cases/semantic/00026");
		// System.getProperty("user.dir")
	}

	/**
	 * 
	 * @param owner
	 * @param model
	 * @param endTime
	 * @param stepSize
	 * @param openDir
	 * @throws InvocationTargetException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 * @throws NoSuchMethodException
	 * @throws IllegalArgumentException
	 * @throws SecurityException
	 */
	public SimulationPanel(Model model, double endTime, double stepSize,
			String openDir) throws SecurityException, IllegalArgumentException,
			NoSuchMethodException, InstantiationException,
			IllegalAccessException, InvocationTargetException {
		super();
		if (SBMLsqueezer.getAvailableSolvers().length == 0)
			JOptionPane
					.showMessageDialog(
							this,
							GUITools
									.toHTML("Could not find any solvers for differential equation systems. A simulation is therefore not possible."),
							"Warning", JOptionPane.WARNING_MESSAGE);
		else {
			// This should be included into our settings
			this.model = model;
			UnitDefinition timeUnits = this.model.getTimeUnitsInstance();
			String xLab = "Time";
			if (timeUnits != null)
				xLab += " in " + UnitDefinition.printUnits(timeUnits, true);
			plot = new FunctionArea(xLab, "Value");
			// get rid of this popup menu.
			MouseListener listeners[] = plot.getMouseListeners();
			for (int i = listeners.length - 1; i >= 0; i--)
				plot.removeMouseListener(listeners[i]);
			maxTime = 10000;
			// always for SBML!
			t1 = new SpinnerNumberModel(0, 0, maxTime, stepSize);
			t2 = new SpinnerNumberModel(endTime, ((Double) t1.getValue())
					.doubleValue(), maxTime, stepSize);
			this.stepSize = stepSize;
			showGrid = GUITools.createJCheckBox("Grid", true, "grid", this,
					"Decide whether or not to draw a grid in the plot area.");
			maxStepsPerUnit = 500;
			this.opendir = openDir;
			separatorChar = ',';
			quoteChar = '\'';
			maxCompartmentValue = 10000;
			maxSpeciesValue = 10000;
			maxParameterValue = 10000;
			paramStepSize = 0.01;
			saveDir = System.getProperty("user.home");
			compression = 0.8f;
			colNames = createColNames(model);
			distanceFunc = 0;

			logScale = GUITools
					.createJCheckBox(
							"Log",
							true,
							"log",
							this,
							"Select this checkbox if the y-axis should be drawn in a logarithmic scale. This is, however, only possible if all values are greater than zero.");
			showLegend = GUITools.createJCheckBox("Legend", true, "legend",
					this, "Add or remove a legend in the plot.");

			JPanel simPanel = new JPanel(new BorderLayout());
			simTable = new JTable();
			simPanel.add(new JScrollPane(simTable,
					JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
					JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED),
					BorderLayout.CENTER);

			JPanel expPanel = new JPanel(new BorderLayout());
			expTable = new JTable();
			expPanel.add(new JScrollPane(expTable,
					JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
					JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED),
					BorderLayout.CENTER);

			JPanel legendPanel = new JPanel(new BorderLayout());
			legend = legendTable(model);
			legendPanel.add(new JScrollPane(legend,
					JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
					JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED),
					BorderLayout.CENTER);

			JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
					true, new JSplitPane(JSplitPane.VERTICAL_SPLIT, true,
							legendPanel, interactiveScanPanel(model,
									maxCompartmentValue, maxSpeciesValue,
									maxParameterValue, paramStepSize)), plot);

			JTabbedPane tabbedPane = new JTabbedPane();
			tabbedPane.add("Plot ", split);
			tabbedPane.add("Simulated data", simPanel);
			tabbedPane.add("Experimental data", expPanel);

			setLayout(new BorderLayout());
			add(createToolBar(), BorderLayout.NORTH);
			add(tabbedPane, BorderLayout.CENTER);
			add(createFootPanel(), BorderLayout.SOUTH);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		switch (Command.valueOf(e.getActionCommand())) {
		case SIMULATION_START:
			double t1val = ((Double) t1.getValue()).doubleValue();
			double t2val = ((Double) t2.getValue()).doubleValue();
			double stepSize = (t2val - t1val)
					/ stepsModel.getNumber().doubleValue();
			simulate(model, t1val, t2val, stepSize);
			break;
		case OPEN_DATA:
			JFileChooser chooser = GUITools.createJFileChooser(opendir, false,
					false, JFileChooser.FILES_ONLY,
					SBFileFilter.CSV_FILE_FILTER);
			if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION)
				try {
					plot(readCSVFile(chooser.getSelectedFile()), false,
							showLegend.isSelected());
				} catch (IOException exc) {
					exc.printStackTrace();
					JOptionPane.showMessageDialog(this, exc.getMessage(), exc
							.getClass().getSimpleName(),
							JOptionPane.ERROR_MESSAGE);
				}
			break;
		case SAVE_PLOT_IMAGE:
			try {
				savePlotImage();
			} catch (Exception exc) {
				exc.printStackTrace();
				JOptionPane.showMessageDialog(this, exc.getMessage(), exc
						.getClass().getSimpleName(), JOptionPane.ERROR_MESSAGE);
			}
			break;
		case SAVE_SIMULATION:
			try {
				saveSimulationResults();
			} catch (IOException exc) {
				exc.printStackTrace();
				JOptionPane.showMessageDialog(this, exc.getMessage(), exc
						.getClass().getSimpleName(), JOptionPane.ERROR_MESSAGE);
			}
			break;
		case SETTINGS:
			adjustPreferences();
			break;
		default:
			JOptionPane.showMessageDialog(this, "Invalid option "
					+ e.getActionCommand(), "Warning",
					JOptionPane.WARNING_MESSAGE);
			break;
		}
	}

	/**
	 * 
	 */
	private void adjustPreferences() {
		try {
			SettingsPanelSimulation ps = new SettingsPanelSimulation();
			// Plot
			ps.setLogScale(logScale.isSelected());
			ps.setShowGrid(showGrid.isSelected());
			ps.setShowLegend(showLegend.isSelected());

			// Scan
			ps.setMaxCompartmentValue(maxCompartmentValue);
			ps.setMaxParameterValue(maxParameterValue);
			ps.setMaxSpeciesValue(maxSpeciesValue);
			ps.setParameterStepSize(paramStepSize);

			// Computing
			ps.setSolver(solvers.getSelectedIndex());
			ps.setDistance(distanceFunc);
			ps.setMaxTime(maxTime);
			ps.setSimulationStartTime(((Double) t1.getValue()).doubleValue());
			ps.setSimulationEndTime(((Double) t2.getValue()).doubleValue());
			ps.setSpinnerMaxValue(maxSpinVal);
			ps.setStepsPerUnitTime(maxStepsPerUnit);
			ps.setNumIntegrationSteps(((Integer) stepsModel.getValue())
					.intValue());

			// Parsing
			ps.setOpenDir(opendir);
			ps.setSaveDir(saveDir);
			ps.setQuoteChar(quoteChar);

			JDialog d = new JDialog();
			d.setTitle("Simulatin Preferences");
			d.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			d.getContentPane().add(ps);
			d.pack();
			d.setLocationRelativeTo(null);
			d.setModal(true);
			d.setVisible(true);

			// Plot
			logScale.setSelected(ps.getLogScale());
			showGrid.setSelected(ps.getShowGrid());
			showLegend.setSelected(ps.getShowLegend());

			// Scan
			maxCompartmentValue = ps.getMaxCompartmentValue();
			maxParameterValue = ps.getMaxParameterValue();
			maxSpeciesValue = ps.getMaxSpeciesValue();
			paramStepSize = ps.getParameterStepSize();
			for (int i = 0; i < spinModSymbol.length; i++) {
				if (i < model.getNumCompartments())
					spinModSymbol[i].setMaximum(Double
							.valueOf(maxCompartmentValue));
				else if (i - model.getNumCompartments() < model.getNumSpecies())
					spinModSymbol[i]
							.setMaximum(Double.valueOf(maxSpeciesValue));
				else
					spinModSymbol[i].setMaximum(Double
							.valueOf(maxParameterValue));
				spinModSymbol[i].setStepSize(Double.valueOf(paramStepSize));
			}

			// Computing
			solvers.setSelectedIndex(ps.getSolver());
			distanceFunc = ps.getDistance();
			maxTime = ps.getMaxTime();
			t1.setValue(ps.getSimulationStartTime());
			t2.setValue(Double.valueOf(ps.getSimulationEndTime()));
			maxSpinVal = ps.getSpinnerMaxValue();
			maxStepsPerUnit = ps.getStepsPerUnitTime();
			stepsModel.setValue(Integer.valueOf(ps.getNumIntegrationSteps()));

			// Parsing
			opendir = ps.getOpenDir();
			saveDir = ps.getSaveDir();
			quoteChar = ps.getQuoteChar();

		} catch (Exception exc) {
			exc.printStackTrace();
			JOptionPane.showMessageDialog(this, exc.getMessage(), exc
					.getClass().getSimpleName(), JOptionPane.ERROR_MESSAGE);
		}
	}

	/**
	 * @throws IOException
	 * 
	 */
	private void saveSimulationResults() throws IOException {
		if (simTable.getRowCount() > 0) {
			JFileChooser fc = GUITools.createJFileChooser(saveDir, false,
					false, JFileChooser.FILES_ONLY,
					SBFileFilter.CSV_FILE_FILTER);
			if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
				File out = fc.getSelectedFile();
				if (!out.exists() || GUITools.overwriteExistingFile(this, out)) {
					BufferedWriter buffer = new BufferedWriter(new FileWriter(
							out));
					int i;
					for (i = 0; i < simTable.getColumnCount(); i++) {
						buffer.append(simTable.getColumnName(i));
						if (i < simTable.getColumnCount() - 1)
							buffer.append(separatorChar);
					}
					buffer.newLine();
					for (double[] row : ((TableModelDoubleMatrix) simTable
							.getModel()).getData()) {
						i = 0;
						for (double value : row) {
							buffer.append(Double.toString(value));
							if (i < simTable.getColumnCount() - 1)
								buffer.append(separatorChar);
							i++;
						}
						buffer.newLine();
					}
					buffer.close();
				}
			}
		} else {
			JOptionPane
					.showMessageDialog(
							this,
							GUITools
									.toHTML(
											"No simulation has been performed yet. Please run the simulation first.",
											40));
		}
	}

	/**
	 * @throws AWTException
	 * @throws IOException
	 * 
	 */
	private void savePlotImage() throws AWTException, IOException {
		Rectangle area = plot.getBounds();
		area.setLocation(plot.getLocationOnScreen());
		BufferedImage bufferedImage = (new Robot()).createScreenCapture(area);
		JFileChooser fc = GUITools.createJFileChooser(saveDir, false, false,
				JFileChooser.FILES_ONLY, SBFileFilter.PNG_FILE_FILTER,
				SBFileFilter.JPEG_FILE_FILTER);
		if (fc.showSaveDialog(plot) == JFileChooser.APPROVE_OPTION
				&& !fc.getSelectedFile().exists()
				|| (GUITools.overwriteExistingFile(this, fc.getSelectedFile()))) {
			File file = fc.getSelectedFile();
			if (SBFileFilter.isPNGFile(file))
				ImageIO.write(bufferedImage, "png", file);
			else if (SBFileFilter.isJPEGFile(file)) {
				FileImageOutputStream out = new FileImageOutputStream(file);
				ImageWriter encoder = (ImageWriter) ImageIO
						.getImageWritersByFormatName("JPEG").next();
				JPEGImageWriteParam param = new JPEGImageWriteParam(null);
				param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
				param.setCompressionQuality(compression);
				encoder.setOutput(out);
				encoder.write((IIOMetadata) null, new IIOImage(bufferedImage,
						null, null), param);
				out.close();
			}
		}
	}

	/**
	 * 
	 * @param model
	 * @return
	 */
	private String[] createColNames(Model model) {
		String colNames[] = new String[1 + model.getNumCompartments()
				+ model.getNumSpecies() + model.getNumParameters()];
		colNames[0] = "Time";
		int i;
		for (i = 1; i <= model.getNumCompartments(); i++) {
			Compartment c = model.getCompartment(i - 1);
			colNames[i] = c.isSetName() ? c.getName() : c.getId();
		}
		for (i = model.getNumCompartments() + 1; i <= model
				.getNumCompartments()
				+ model.getNumSpecies(); i++) {
			Species s = model.getSpecies(i - model.getNumCompartments() - 1);
			colNames[i] = s.isSetName() ? s.getName() : s.getId();
		}
		for (i = model.getNumCompartments() + model.getNumSpecies() + 1; i < colNames.length; i++) {
			Parameter p = model.getParameter(i - model.getNumCompartments()
					- model.getNumSpecies() - 1);
			colNames[i] = p.isSetName() ? p.getName() : p.getId();
		}
		return colNames;
	}

	/**
	 * 
	 * @return
	 * @throws NoSuchMethodException
	 * @throws SecurityException
	 * @throws InvocationTargetException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 * @throws IllegalArgumentException
	 */
	private Component createFootPanel() throws SecurityException,
			NoSuchMethodException, IllegalArgumentException,
			InstantiationException, IllegalAccessException,
			InvocationTargetException {

		// Settings
		JSpinner startTime = new JSpinner(t1);
		startTime.addChangeListener(this);
		startTime.setName("t1");
		startTime.setEnabled(false);
		JSpinner endTime = new JSpinner(t2);
		endTime.addChangeListener(this);
		endTime.setName("t2");
		double t1val = ((Double) t1.getValue()).doubleValue();
		double t2val = ((Double) t2.getValue()).doubleValue();
		stepsModel = new SpinnerNumberModel((int) Math.round((t2val - t1val)
				/ stepSize), 1, (int) Math.round((t2val - t1val)
				* maxStepsPerUnit), 1);

		solvers = new JComboBox();
		for (Class<AbstractDESSolver> c : SBMLsqueezer.getAvailableSolvers()) {
			solver = c.getConstructor().newInstance();
			solvers.addItem(solver.getName());
		}
		solvers.setSelectedIndex(0);
		if (solvers.getItemCount() == 1)
			solvers.setEnabled(false);
		JPanel sPanel = new JPanel();
		LayoutHelper settings = new LayoutHelper(sPanel);
		settings.add(new JLabel("Start time: "), 0, 0, 1, 1, 0, 0);
		settings.add(new JPanel(), 1, 0, 1, 1, 0, 0);
		settings.add(startTime, 2, 0, 1, 1, 0, 0);
		settings.add(new JPanel(), 3, 0, 1, 1, 0, 0);
		settings.add(new JLabel("End time: "), 4, 0, 1, 1, 0, 0);
		settings.add(new JPanel(), 5, 0, 1, 1, 0, 0);
		settings.add(endTime, 6, 0, 1, 1, 0, 0);
		settings.add(new JPanel(), 7, 0, 1, 1, 0, 0);
		settings.add(new JLabel("Steps: "), 8, 0, 1, 1, 0, 0);
		settings.add(new JPanel(), 9, 0, 1, 1, 0, 0);
		settings.add(new JSpinner(stepsModel), 10, 0, 5, 1, 0, 0);
		settings.add(new JPanel(), 0, 1, 1, 1, 0, 0);
		settings.add(new JLabel("ODE Solver: "), 0, 2, 1, 1, 0, 0);
		settings.add(solvers, 2, 2, 5, 1, 0, 0);
		settings.add(showGrid, 8, 2, 1, 1, 0, 0);
		settings.add(logScale, 10, 2, 1, 1, 0, 0);
		settings.add(new JPanel(), 11, 1, 1, 1, 0, 0);
		settings.add(showLegend, 12, 2, 1, 1, 0, 0);

		sPanel.setBorder(BorderFactory.createTitledBorder(" Settings "));

		// Actions
		JButton start = GUITools.createButton("Run", GUITools.ICON_GEAR_TINY,
				this, Command.SIMULATION_START,
				"Perform a simulation run with the current settings.");

		JPanel aPanel = new JPanel();
		aPanel.add(start);

		// Main
		JPanel mPanel = new JPanel(new BorderLayout());
		mPanel.add(sPanel, BorderLayout.CENTER);
		mPanel.add(aPanel, BorderLayout.SOUTH);
		return mPanel;
	}

	/**
	 * 
	 * @return
	 */
	private Component createToolBar() {
		JToolBar toolbar = new JToolBar("Tools");
		if (GUITools.ICON_OPEN != null)
			toolbar.add(GUITools.createButton(GUITools.ICON_OPEN, this,
					Command.OPEN_DATA, "Load  experimental data from file."));
		if (GUITools.ICON_SAVE != null)
			toolbar.add(GUITools
					.createButton(GUITools.ICON_SAVE, this,
							Command.SAVE_SIMULATION,
							"Save simulation results to file."));
		if (GUITools.ICON_PICTURE_TINY != null)
			toolbar.add(GUITools.createButton(GUITools.ICON_PICTURE_TINY, this,
					Command.SAVE_PLOT_IMAGE, "Save plot in an image."));
		if (GUITools.ICON_SETTINGS_TINY != null)
			toolbar.add(GUITools.createButton(GUITools.ICON_SETTINGS_TINY,
					this, Command.SETTINGS, "Adjust your preferences"));
		return toolbar;
	}

	/**
	 * 
	 * @param model
	 * @param maxCompartmentValue
	 * @param maxSpeciesValue
	 * @param maxParameterValue
	 * @param paramStepSize
	 * @return
	 */
	private JTabbedPane interactiveScanPanel(Model model,
			double maxCompartmentValue, double maxSpeciesValue,
			double maxParameterValue, double paramStepSize) {
		JTabbedPane tab = new JTabbedPane();
		JPanel parameterPanel = new JPanel();
		parameterPanel.setLayout(new BoxLayout(parameterPanel,
				BoxLayout.PAGE_AXIS));
		spinModSymbol = new SpinnerNumberModel[model.getNumCompartments()
				+ model.getNumSpecies() + model.getNumParameters()];
		boolean hasLocalParameters = false;
		for (Reaction r : model.getListOfReactions())
			if (r.isSetKineticLaw() && r.getKineticLaw().getNumParameters() > 0) {
				hasLocalParameters = true;
				JPanel panel = interactiveScanTable(r.getKineticLaw()
						.getListOfParameters(), maxParameterValue,
						paramStepSize);
				panel.setBorder(BorderFactory.createTitledBorder(String.format(
						" Reaction %s ", r.getId())));
				parameterPanel.add(panel);
			}
		tab.add("Compartments", new JScrollPane(interactiveScanTable(model
				.getListOfCompartments(), maxCompartmentValue, paramStepSize),
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED));
		tab.setEnabledAt(0, model.getNumCompartments() > 0);
		tab.add("Species", new JScrollPane(interactiveScanTable(model
				.getListOfSpecies(), maxParameterValue, paramStepSize),
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED));
		tab.setEnabledAt(1, model.getNumSpecies() > 0);
		tab.add("Global Parameters", new JScrollPane(interactiveScanTable(model
				.getListOfParameters(), maxSpeciesValue, paramStepSize),
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED));
		tab.setEnabledAt(2, model.getNumParameters() > 0);
		tab.add("Local Parameters", new JScrollPane(parameterPanel,
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED));
		tab.setEnabledAt(3, hasLocalParameters);
		return tab;
	}

	/**
	 * 
	 * @param maxValue
	 *            , double stepSize
	 * @param model
	 * @return
	 */
	private JPanel interactiveScanTable(ListOf<? extends Symbol> list,
			double maxValue, double stepSize) {
		JPanel panel = new JPanel();
		LayoutHelper lh = new LayoutHelper(panel);
		int offset = 0;
		for (int i = 0; i < list.size(); i++) {
			Symbol p = list.get(i);
			if (p instanceof Species)
				offset = model.getNumCompartments();
			if (p instanceof Parameter)
				offset = model.getNumCompartments() + model.getNumSpecies();
			spinModSymbol[i + offset] = new SpinnerNumberModel(p.getValue(),
					0d, maxValue, stepSize);
			JSpinner spinner = new JSpinner(spinModSymbol[i + offset]);
			spinner.setName(p.getId());
			spinner.addChangeListener(this);
			lh.add(new JLabel(GUITools.toHTML(p.toString(), 40)), 0, i, 1, 1,
					0, 0);
			lh.add(spinner, 2, i, 1, 1, 0, 0);
			lh.add(new JLabel(GUITools.toHTML(p.isSetUnits() ? HTMLFormula
					.toHTML(p.getUnitsInstance()) : "")), 4, i, 1, 1, 0, 0);
		}
		lh.add(new JPanel(), 1, 0, 1, 1, 0, 0);
		lh.add(new JPanel(), 3, 0, 1, 1, 0, 0);
		return panel;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.event.ItemListener#itemStateChanged(java.awt.event.ItemEvent)
	 */
	public void itemStateChanged(ItemEvent e) {
		if (e.getSource() instanceof JCheckBox) {
			JCheckBox chck = (JCheckBox) e.getSource();
			if (chck.getName().equals("grid")) {
				plot.setGridVisible(chck.isSelected());
			} else if (chck.getName().equals("log")) {
				if (chck.isSelected() && !plot.checkLoggable()) {
					chck.setSelected(false);
					chck.setEnabled(false);
					JOptionPane
							.showMessageDialog(
									this,
									GUITools
											.toHTML(
													"Cannot change to logarithmic scale because at least one value on the y-axis is not greater than zero.",
													40), "Warning",
									JOptionPane.WARNING_MESSAGE);
				}
				plot.toggleLog(chck.isSelected());
			} else if (chck.getName().equals("legend")) {
				plot.showLegend(chck.isSelected());
			}
		}
	}

	/**
	 * 
	 * @param model
	 * @return
	 */
	private JTable legendTable(Model model) {
		JTable tab = new JTable();
		tab.setName("legend");
		tab.setModel(new TableModelLedgend(model));
		tab.setDefaultEditor(Color.class, new ColorEditor());
		tab.setDefaultRenderer(Color.class, new TableCellRendererObjects());
		tab.setDefaultRenderer(Symbol.class, new TableCellRendererObjects());
		tab.getModel().addTableModelListener(this);
		return tab;
	}

	/**
	 * Plots a matrix either by displaying unconnected or connected points
	 * depending on the connected parameter.
	 * 
	 * @param solution
	 *            The solution of a simulation, where the first column is
	 *            assumed to contain the simulation time.
	 * @param connected
	 *            If true, all points will be connected, singular points are
	 *            plotted for false.
	 */
	private void plot(double[][] solution, boolean connected, boolean showLegend) {
		if (solution.length > 0) {
			for (int i = 0; i < solution.length; i++) {
				for (int j = 0; j < solution[i].length - 1; j++) {
					if (connected) {
						if (legend.getRowCount() == 0
								|| ((Boolean) legend.getValueAt(j, 0))
										.booleanValue()) {
							plot.setConnectedPoint(solution[i][0],
									solution[i][j + 1], j);
							plot.setGraphColor(j, (Color) legend.getValueAt(j,
									1));
							plot.setInfoString(j, legend.getValueAt(j, 2)
									.toString(), 2);
						}
					} else {
						plot.setUnconnectedPoint(solution[i][0],
								solution[i][j + 1], colNames.length - 1 + j);
						plot.setInfoString(j, legend.getValueAt(j, 2)
								.toString(), 2);
					}
				}
			}
			plot.showLegend(showLegend);
		}
	}

	/**
	 * 
	 * @param file
	 * @throws IOException
	 */
	@SuppressWarnings("unchecked")
	private double[][] readCSVFile(File file) throws IOException {
		CSVReader csvreader = new CSVReader(new FileReader(file),
				separatorChar, quoteChar, 0);
		List<String[]> input = csvreader.readAll();
		csvreader.close();
		if (input.size() > 0) {
			int i, j;
			short numNumbers = 0;
			String names[] = input.get(0);
			for (i = 0; i < names.length; i++)
				try {
					numNumbers++;
					Double.parseDouble(names[i]);
				} catch (NumberFormatException exc) {
					numNumbers--;
				}
			TableModelDoubleMatrix tabModel = new TableModelDoubleMatrix();
			if (numNumbers == 0)
				tabModel.setColumnNames(input.remove(0));
			double data[][] = new double[input.size()][input.get(0).length];
			for (i = 0; i < data.length; i++)
				for (j = 0; j < data[i].length; j++)
					data[i][j] = Double.valueOf(input.get(i)[j]).doubleValue();
			tabModel.setData(data);
			this.expTable.setModel(tabModel);
			return data;
		}
		return new double[0][0];
	}

	/**
	 * 
	 * @param model
	 * @param t1val
	 * @param t2val
	 * @param stepSize
	 */
	private void simulate(Model model, double t1val, double t2val,
			double stepSize) {
		TableModelDoubleMatrix tabMod = new TableModelDoubleMatrix(
				solveByStepSize(model, t1val, t2val, stepSize),
				createColNames(model));
		simTable.setModel(tabMod);
		plot.clearAll();
		plot(tabMod.getData(), true, showLegend.isSelected());
		if (expTable.getColumnCount() > 0)
			plot(((TableModelDoubleMatrix) expTable.getModel()).getData(),
					false, showLegend.isSelected());
	}

	/**
	 * 
	 * @param model
	 * @param t1
	 *            Time begin
	 * @param t2
	 *            Time end
	 * @param stepSize
	 * @return
	 */
	private double[][] solveByStepSize(Model model, double t1, double t2,
			double stepSize) {
		SBMLinterpreter interpreter = new SBMLinterpreter(model);
		solver.setStepSize(stepSize);
		double solution[][] = solver.solveByStepSizeIncludingTime(interpreter,
				interpreter.getInitialValues(), t1, t2);
		if (solver.isUnstable()) {
			JOptionPane.showMessageDialog(this, "Unstable!",
					"Simulation not possible", JOptionPane.WARNING_MESSAGE);
		}
		return solution;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent
	 * )
	 */
	public void stateChanged(ChangeEvent e) {
		if (e.getSource() instanceof JSpinner) {
			JSpinner spin = (JSpinner) e.getSource();
			if (spin.getName() != null && spin.getName().equals("t2")) {
				double t1val = ((Double) t1.getValue()).doubleValue();
				double t2val = Double.valueOf(spin.getValue().toString())
						.doubleValue();
				stepsModel.setMinimum(1);
				stepsModel.setMaximum(Integer.valueOf((int) Math
						.round((t2val - t1val) * maxStepsPerUnit)));
				stepsModel.setValue(Math.max(Integer.valueOf((int) Math
						.round((t2val - t1val) * stepSize)),
						((Integer) stepsModel.getMinimum()).intValue()));
			} else {
				State s = model.findState(spin.getName());
				if (s != null && s instanceof Symbol) {
					((Symbol) s)
							.setValue(((SpinnerNumberModel) spin.getModel())
									.getNumber().doubleValue());
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seejavax.swing.event.TableModelListener#tableChanged(javax.swing.event.
	 * TableModelEvent)
	 */
	public void tableChanged(TableModelEvent e) {
		if (e.getSource() instanceof TableModelLedgend) {
			if (e.getColumn() <= 1 && 0 < simTable.getRowCount()) {
				plot.clearAll();
				plot(((TableModelDoubleMatrix) simTable.getModel()).getData(),
						true, showLegend.isSelected());
				if (expTable.getRowCount() > 0)
					plot(((TableModelDoubleMatrix) expTable.getModel())
							.getData(), false, showLegend.isSelected());
				if (showLegend.isSelected())
					plot.updateLegend();
			}
		}
	}
}

/**
 * 
 * @author Andreas Dr&auml;ger
 * @since 1.4
 * @date 2010-04-07
 */
class TableCellRendererObjects extends JLabel implements TableCellRenderer {

	/**
	 * Generated serial version identifier
	 */
	private static final long serialVersionUID = 5233542392522297524L;

	/**
	 * 
	 */
	public TableCellRendererObjects() {
		super();
		setOpaque(true);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * javax.swing.table.TableCellRenderer#getTableCellRendererComponent(javax
	 * .swing.JTable, java.lang.Object, boolean, boolean, int, int)
	 */
	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column) {
		setBackground(Color.WHITE);
		if (value instanceof Color) {
			Color newColor = (Color) value;
			setToolTipText("RGB value: " + newColor.getRed() + ", "
					+ newColor.getGreen() + ", " + newColor.getBlue());
			setBackground(newColor);
		} else if (value instanceof Symbol) {
			Symbol s = (Symbol) value;
			setText(s.isSetName() ? s.getName() : s.getId());
			setBackground(Color.WHITE);
		} else
			setText(value.toString());
		return this;
	}

}

/**
 * 
 * @author Andreas Dr&auml;ger
 * @since 1.4
 * @date 2010-04-07
 * 
 */
class TableModelLedgend extends AbstractTableModel {

	/**
	 * Generated serial version identifier.
	 */
	private static final long serialVersionUID = 7360401460080111135L;
	/**
	 * A colored button for each model component and
	 */
	private Object[][] data;

	/**
	 * 
	 * @param model
	 */
	public TableModelLedgend(Model model) {
		data = new Object[model.getNumCompartments() + model.getNumSpecies()
				+ model.getNumParameters()][3];
		int i, j;
		for (i = 0; i < model.getNumCompartments(); i++) {
			data[i][0] = Boolean.TRUE;
			data[i][1] = SimulationPanel.indexToColor(i);
			data[i][2] = model.getCompartment(i);
		}
		j = model.getNumCompartments();
		for (i = 0; i < model.getNumSpecies(); i++) {
			data[i + j][0] = Boolean.TRUE;
			data[i + j][1] = SimulationPanel.indexToColor(i + j);
			data[i + j][2] = model.getSpecies(i);
		}
		j = model.getNumCompartments() + model.getNumSpecies();
		for (i = 0; i < model.getNumParameters(); i++) {
			data[i + j][0] = Boolean.TRUE;
			data[i + j][1] = SimulationPanel.indexToColor(i + j);
			data[i + j][2] = model.getParameter(i);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.table.AbstractTableModel#getColumnClass(int)
	 */
	@Override
	public Class<?> getColumnClass(int c) {
		return getValueAt(0, c).getClass();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.table.TableModel#getColumnCount()
	 */
	public int getColumnCount() {
		return data.length > 0 ? data[0].length : 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.table.AbstractTableModel#getColumnName(int)
	 */
	@Override
	public String getColumnName(int column) {
		if (column == 0)
			return "Plot";
		if (column == 1)
			return "Color";
		if (column == 2)
			return "State";
		throw new IndexOutOfBoundsException("Only " + getColumnCount()
				+ " columns, no column " + column);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.table.TableModel#getRowCount()
	 */
	public int getRowCount() {
		return data.length;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.table.TableModel#getValueAt(int, int)
	 */
	public Object getValueAt(int rowIndex, int columnIndex) {
		return data[rowIndex][columnIndex];
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.table.AbstractTableModel#isCellEditable(int, int)
	 */
	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		if (columnIndex < 2 && rowIndex < getRowCount())
			return true;
		return false;
	}

	/*
	 * 
	 */
	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		data[rowIndex][columnIndex] = aValue;
		fireTableCellUpdated(rowIndex, columnIndex);
	}
}