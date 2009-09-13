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
package org.sbml.squeezer.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.sbml.jsbml.Model;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.SBMLException;
import org.sbml.squeezer.CfgKeys;
import org.sbml.squeezer.SBMLsqueezer;
import org.sbml.squeezer.io.LaTeXExport;
import org.sbml.squeezer.io.SBFileFilter;
import org.sbml.squeezer.io.SBMLio;
import org.sbml.squeezer.io.TextExport;
import org.sbml.squeezer.resources.Resource;

/**
 * @author Andreas Dr&auml;ger <a
 *         href="mailto:andreas.draeger@uni-tuebingen.de">
 *         andreas.draeger@uni-tuebingen.de</a>
 * 
 */
public class SBMLsqueezerUI extends JFrame implements ActionListener,
		WindowListener, ChangeListener {
	/**
	 * 
	 * @author Andreas Dr&auml;ger <a
	 *         href="mailto:andreas.draeger@uni-tuebingen.de"
	 *         >andreas.draeger@uni-tuebingen.de</a>
	 * @date 2009-09-11
	 */
	public static enum Command {
		/**
		 * 
		 */
		SQUEEZE,
		/**
		 * 
		 */
		TO_LATEX,
		/**
		 * 
		 */
		ONLINE_HELP,
		/**
		 * 
		 */
		OPEN_FILE,
		/**
		 * 
		 */
		SAVE_FILE,
		/**
		 * 
		 */
		CLOSE_FILE,
		/**
		 * 
		 */
		SET_PREFERENCES
	}

	/**
	 * Generated serial version uid.
	 */
	private static final long serialVersionUID = 5461285676322858005L;

	/**
	 * Manages all models, storage, loading and selecting models.
	 */
	private SBMLio sbmlIO;

	private JTabbedPaneWithCloseIcons tabbedPane;
	private JToolBar toolbar;

	/**
	 * 
	 */
	private Properties settings;

	/**
	 * @throws HeadlessException
	 */
	public SBMLsqueezerUI(SBMLio io, Properties settings)
			throws HeadlessException {
		super("SBMLsqueezer " + SBMLsqueezer.getVersionNumber());
		this.settings = settings;
		this.sbmlIO = io;
		init();
		pack();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	// @Override
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals(Command.SQUEEZE.toString())) {
			KineticLawSelectionDialog klsd;
			if (e.getSource() instanceof Reaction) {
				// just one reaction
				klsd = new KineticLawSelectionDialog(this, settings, sbmlIO,
						((Reaction) e.getSource()).getId());
			} else {
				// whole model
				klsd = new KineticLawSelectionDialog(this, settings, sbmlIO);
				klsd.setVisible(true);
			}
			if (klsd.isKineticsAndParametersStoredInSBML()) {
				SBMLModelSplitPane split = (SBMLModelSplitPane) tabbedPane
						.getSelectedComponent();
				split.init(sbmlIO.getSelectedModel(), true);
			}

		} else if (e.getActionCommand().equals(Command.TO_LATEX.toString())) {
			if (e.getSource() instanceof Reaction) {
				new KineticLawSelectionDialog(this, settings, (Reaction) e
						.getSource());
			} else if (e.getSource() instanceof Model) {
				new KineticLawSelectionDialog(this, settings, (Model) e
						.getSource());
			} else {
				String dir = settings.get(CfgKeys.LATEX_DIR).toString();
				JFileChooser chooser = GUITools.createJFileChooser(dir, false,
						false, JFileChooser.FILES_ONLY,
						SBFileFilter.TeX_FILE_FILTER);
				if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
					File out = chooser.getSelectedFile();
					String path = out.getParent();
					if (!path.equals(dir))
						settings.put(CfgKeys.LATEX_DIR, path);
					if (!out.exists()
							|| GUITools.overwriteExistingFileDialog(this, out) == JOptionPane.YES_OPTION)
						writeLaTeX(out);
				}
			}

		} else if (e.getActionCommand().equals(
				Command.SET_PREFERENCES.toString())) {
			SettingsDialog dialog = new SettingsDialog(this);
			if (dialog.showSettingsDialog((Properties) settings.clone()) == SettingsDialog.APPROVE_OPTION)
				this.settings = dialog.getSettings();

		} else if (e.getActionCommand().equals(Command.OPEN_FILE.toString())) {
			JFileChooser chooser = GUITools.createJFileChooser(settings.get(
					CfgKeys.OPEN_DIR).toString(), false, false,
					JFileChooser.FILES_ONLY, SBFileFilter.SBML_FILE_FILTER);
			if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
				Model model = sbmlIO.readModel(chooser.getSelectedFile()
						.getAbsolutePath());
				checkForSBMLErrors(model);
				if (model != null) {
					addModel(model);
					String path = chooser.getSelectedFile().getAbsolutePath();
					path = path.substring(0, path.lastIndexOf('/'));
					if (!path.equals(settings.get(CfgKeys.OPEN_DIR).toString()))
						settings.put(CfgKeys.OPEN_DIR, path);
				}
			}

		} else if (e.getActionCommand().equals(Command.SAVE_FILE.toString())) {
			SBFileFilter filterText = SBFileFilter.TEXT_FILE_FILTER;
			SBFileFilter filterTeX = SBFileFilter.TeX_FILE_FILTER;
			SBFileFilter filterSBML = SBFileFilter.SBML_FILE_FILTER;
			JFileChooser chooser = GUITools.createJFileChooser(settings.get(
					CfgKeys.SAVE_DIR).toString(), false, false,
					JFileChooser.FILES_ONLY, filterSBML, filterTeX, filterText);
			if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
				File out = chooser.getSelectedFile();
				if (out.getParentFile() != null)
					settings.put(CfgKeys.SAVE_DIR, out.getParentFile()
							.getAbsolutePath());
				if (!out.exists()
						|| GUITools.overwriteExistingFileDialog(this, out) == JOptionPane.YES_OPTION) {
					if (filterSBML.accept(out))
						writeSBML(out);
					else if (filterTeX.accept(out))
						writeLaTeX(out);
					else if (filterText.accept(out))
						writeText(out);
				}
			}

		} else if (e.getActionCommand().equals(Command.CLOSE_FILE.toString())) {
			if (tabbedPane.getComponentCount() > 0)
				tabbedPane.remove(tabbedPane.getSelectedComponent());
			if (tabbedPane.getComponentCount() == 0)
				setEnabled(false, Command.SAVE_FILE, Command.CLOSE_FILE,
						Command.SQUEEZE, Command.TO_LATEX);

		} else if (e.getActionCommand().equals(Command.ONLINE_HELP.toString())) {
			JHelpBrowser helpBrowser = new JHelpBrowser(this, "SBMLsqueezer "
					+ SBMLsqueezer.getVersionNumber() + " - Online Help");
			helpBrowser.addWindowListener(this);
			helpBrowser.setLocationRelativeTo(this);
			helpBrowser.setSize(640, 640);
			helpBrowser.setVisible(true);
			helpBrowser.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			setEnabled(false, Command.ONLINE_HELP);

		} else if (e.getSource() instanceof JMenuItem) {
			JMenuItem item = (JMenuItem) e.getSource();
			if (item.getText().equals("Exit")) {
				SBMLsqueezer.saveProperties(settings);
				System.exit(0);
			} else if (item.getText().equals("About")) {
				JBrowser browser = new JBrowser(Resource.class
						.getResource("html/about.htm"));
				browser.removeHyperlinkListener(browser);
				browser.addHyperlinkListener(new SystemBrowser());
				browser.setBorder(BorderFactory.createEtchedBorder());
				JOptionPane.showMessageDialog(this, browser,
						"About SBMLsqueezer", JOptionPane.INFORMATION_MESSAGE);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent
	 * )
	 */
	public void stateChanged(ChangeEvent e) {
		if (e.getSource().equals(tabbedPane)) {
			if (tabbedPane.getComponentCount() == 0)
				setEnabled(false, Command.SAVE_FILE, Command.CLOSE_FILE,
						Command.SQUEEZE, Command.TO_LATEX);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.event.WindowListener#windowActivated(java.awt.event.WindowEvent)
	 */
	public void windowActivated(WindowEvent arg0) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.event.WindowListener#windowClosed(java.awt.event.WindowEvent)
	 */
	public void windowClosed(WindowEvent arg0) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.event.WindowListener#windowClosing(java.awt.event.WindowEvent)
	 */
	public void windowClosing(WindowEvent we) {
		if (we.getSource() instanceof JHelpBrowser)
			setEnabled(true, Command.ONLINE_HELP);
		else if (we.getSource() instanceof SBMLsqueezerUI)
			SBMLsqueezer.saveProperties(settings);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.event.WindowListener#windowDeactivated(java.awt.event.WindowEvent
	 * )
	 */
	public void windowDeactivated(WindowEvent arg0) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.event.WindowListener#windowDeiconified(java.awt.event.WindowEvent
	 * )
	 */
	public void windowDeiconified(WindowEvent arg0) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.event.WindowListener#windowIconified(java.awt.event.WindowEvent)
	 */
	public void windowIconified(WindowEvent arg0) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.event.WindowListener#windowOpened(java.awt.event.WindowEvent)
	 */
	public void windowOpened(WindowEvent arg0) {
	}

	/**
	 * Adds the given new model into the tabbed pane on the main panel.
	 * 
	 * @param model
	 */
	private void addModel(Model model) {
		SBMLModelSplitPane split = new SBMLModelSplitPane(model);
		split.addActionListener(this);
		tabbedPane.add(model.getId(), split);
		tabbedPane.setSelectedIndex(tabbedPane.getComponentCount() - 1);
		setEnabled(true, Command.SAVE_FILE, Command.CLOSE_FILE,
				Command.SQUEEZE, Command.TO_LATEX);
	}

	/**
	 * Checks if the model loaded as the last one contains any errors or
	 * warnings.
	 */
	private void checkForSBMLErrors(Model m) {
		if (sbmlIO.getNumErrors() > 0) {
			String warnings = GUITools.toHTML("<p>"
					+ sbmlIO.getWarnings().replace("<", "&lt;").replace(">",
							"&gt;").replace(
							System.getProperty("line.separator"), "</p><p>")
					+ "</p>", 200);
			JEditorPane area = new JEditorPane("text/html", warnings);
			area.setEditable(false);
			JScrollPane scroll = new JScrollPane(area,
					JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
					JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
			scroll.setPreferredSize(new Dimension(450, 200));
			JOptionPane.showMessageDialog(this, scroll, "SBML warnings",
					JOptionPane.WARNING_MESSAGE);
			if (m == null) {
				String message = "Unable to load this model "
						+ "due to one or several errors. "
						+ "Please use the SBML online validator "
						+ "to check why this model is not correct.";
				JOptionPane.showMessageDialog(this, GUITools
						.toHTML(message, 40), "Error",
						JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	/**
	 * Creates the menu bar for SBMLsqueezer's UI.
	 * 
	 * @return
	 */
	private JMenuBar createMenuBar() {
		/*
		 * File menu
		 */
		JMenu fileMenu = new JMenu("File");
		fileMenu.setMnemonic(fileMenu.getText().charAt(0));
		JMenuItem openItem = new JMenuItem("Open", UIManager
				.getIcon("FileView.directoryIcon"));
		openItem.setActionCommand(Command.OPEN_FILE.toString());
		openItem.setAccelerator(KeyStroke.getKeyStroke('O',
				InputEvent.CTRL_DOWN_MASK));
		JMenuItem saveItem = new JMenuItem("Save as", UIManager
				.getIcon("FileView.floppyDriveIcon"));
		saveItem.addActionListener(this);
		saveItem.setActionCommand(Command.SAVE_FILE.toString());
		saveItem.setAccelerator(KeyStroke.getKeyStroke('S',
				InputEvent.CTRL_DOWN_MASK));
		JMenuItem closeItem = new JMenuItem("Close", new CloseIcon(false));
		closeItem.setAccelerator(KeyStroke.getKeyStroke('W',
				InputEvent.CTRL_DOWN_MASK));
		closeItem.setActionCommand(Command.CLOSE_FILE.toString());
		JMenuItem exitItem = new JMenuItem("Exit");
		exitItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F4,
				InputEvent.ALT_DOWN_MASK));
		fileMenu.add(openItem);
		fileMenu.add(saveItem);
		fileMenu.add(closeItem);
		fileMenu.add(exitItem);
		openItem.addActionListener(this);
		closeItem.addActionListener(this);
		exitItem.addActionListener(this);

		/*
		 * Edit menu
		 */
		JMenu editMenu = new JMenu("Edit");
		editMenu.setMnemonic(editMenu.getText().charAt(0));
		JMenuItem squeezeItem = new JMenuItem("Squeeze");
		squeezeItem.setAccelerator(KeyStroke.getKeyStroke('Q',
				InputEvent.CTRL_DOWN_MASK));
		squeezeItem.addActionListener(this);
		squeezeItem.setActionCommand(Command.SQUEEZE.toString());
		squeezeItem.setIcon(GUITools.LEMON_ICON_TINY);
		JMenuItem latexItem = new JMenuItem("Export to LaTeX");
		latexItem.setAccelerator(KeyStroke.getKeyStroke('E',
				InputEvent.CTRL_DOWN_MASK));
		latexItem.setIcon(GUITools.LATEX_ICON_TINY);
		latexItem.addActionListener(this);
		latexItem.setActionCommand(Command.TO_LATEX.toString());
		JMenuItem preferencesItem = new JMenuItem("Preferences");
		preferencesItem.setActionCommand(Command.SET_PREFERENCES.toString());
		preferencesItem.addActionListener(this);
		preferencesItem.setMnemonic(preferencesItem.getText().charAt(0));
		editMenu.add(squeezeItem);
		editMenu.add(latexItem);
		editMenu.add(preferencesItem);

		/*
		 * Help menu
		 */
		JMenu helpMenu = new JMenu("Help");
		helpMenu.setMnemonic(helpMenu.getText().charAt(0));
		JMenuItem about = new JMenuItem("About");
		about.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0));
		about.addActionListener(this);
		JMenuItem help = new JMenuItem("Online help");
		help.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0));
		help.addActionListener(this);
		help.setActionCommand(Command.ONLINE_HELP.toString());
		helpMenu.add(help);
		helpMenu.add(about);

		JMenuBar mBar = new JMenuBar();
		mBar.add(fileMenu);
		mBar.add(editMenu);
		try {
			mBar.setHelpMenu(helpMenu);
		} catch (Error e) {
			mBar.add(helpMenu);
		}
		return mBar;
	}

	/**
	 * Creates the tool bar for SBMLsqueezer's UI.
	 * 
	 * @return
	 */
	private JToolBar createToolBar() {
		toolbar = new JToolBar("Edit", JToolBar.HORIZONTAL);
		JButton openButton = new JButton(UIManager
				.getIcon("FileView.directoryIcon"));
		openButton.addActionListener(this);
		openButton.setActionCommand(Command.OPEN_FILE.toString());
		toolbar.add(openButton);
		JButton saveButton = new JButton(UIManager
				.getIcon("FileView.floppyDriveIcon"));
		saveButton.addActionListener(this);
		saveButton.setActionCommand(Command.SAVE_FILE.toString());
		toolbar.add(saveButton);
		JButton closeButton = new JButton(new CloseIcon(false));
		closeButton.setActionCommand(Command.CLOSE_FILE.toString());
		closeButton.addActionListener(this);
		toolbar.add(closeButton);
		toolbar.addSeparator();
		if (GUITools.LEMON_ICON_TINY != null) {
			JButton squeezeButton = new JButton(GUITools.LEMON_ICON_TINY);
			squeezeButton.setActionCommand(Command.SQUEEZE.toString());
			squeezeButton.addActionListener(this);
			toolbar.add(squeezeButton);
		}
		if (GUITools.LATEX_ICON_TINY != null) {
			JButton latexButton = new JButton(GUITools.LATEX_ICON_TINY);
			latexButton.addActionListener(this);
			latexButton.setActionCommand(Command.TO_LATEX.toString());
			toolbar.add(latexButton);
		}
		toolbar.addSeparator();
		JButton helpButton = new JButton("?");
		helpButton.addActionListener(this);
		helpButton.setActionCommand(Command.ONLINE_HELP.toString());
		toolbar.add(helpButton);
		return toolbar;
	}

	/**
	 * Sets up this GUI.
	 */
	private void init() {
		setJMenuBar(createMenuBar());
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		addWindowListener(this);
		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(createToolBar(), BorderLayout.NORTH);
		setEnabled(false, Command.SAVE_FILE, Command.CLOSE_FILE,
				Command.SQUEEZE, Command.TO_LATEX);
		tabbedPane = new JTabbedPaneWithCloseIcons();
		for (Model m : sbmlIO.getListOfModels()) {
			checkForSBMLErrors(m);
			if (m != null)
				addModel(m);
		}
		tabbedPane.addChangeListener(this);
		tabbedPane.addChangeListener(sbmlIO);
		getContentPane().add(tabbedPane, BorderLayout.CENTER);
		setIconImage(GUITools.LEMON_ICON);
	}

	/**
	 * Enables or disables actions that can be performed by SBMLsqueezer, i.e.,
	 * all menu items and buttons that are associated with the given actions are
	 * enabled or disabled.
	 * 
	 * @param state
	 *            if true buttons, items etc. are enabled, otherwise disabled.
	 * @param commands
	 */
	private void setEnabled(boolean state, Command... commands) {
		int i, j;
		Set<String> setOfCommands = new HashSet<String>();
		for (Command command : commands)
			setOfCommands.add(command.toString());
		for (i = 0; i < getJMenuBar().getMenuCount(); i++) {
			JMenu menu = getJMenuBar().getMenu(i);
			for (j = 0; j < menu.getItemCount(); j++) {
				JMenuItem item = menu.getItem(j);
				if (setOfCommands.contains(item.getActionCommand()))
					item.setEnabled(state);
			}
		}
		for (i = 0; i < toolbar.getComponentCount(); i++) {
			Object o = toolbar.getComponent(i);
			if (o instanceof JButton) {
				JButton b = (JButton) o;
				if (setOfCommands.contains(b.getActionCommand())) {
					b.setEnabled(state);
					if (b.getIcon() != null && b.getIcon() instanceof CloseIcon)
						((CloseIcon) b.getIcon()).setColor(state ? Color.BLACK
								: Color.GRAY);
				}
			}
		}
	}

	/**
	 * Write the selected model into a LaTeX file.
	 * 
	 * @param out
	 */
	private void writeLaTeX(File out) {
		try {
			LaTeXExport.writeLaTeX(sbmlIO.getSelectedModel(), out, settings);
		} catch (Exception exc) {
			JOptionPane.showMessageDialog(this, exc.getMessage(), exc
					.getClass().getName(), JOptionPane.WARNING_MESSAGE);
			exc.printStackTrace();
		}
	}

	/**
	 * Write the selected model into an SBML file.
	 * 
	 * @param out
	 */
	private void writeSBML(File out) {
		try {
			sbmlIO.writeSelectedModelToSBML(out.getAbsolutePath());
		} catch (SBMLException exc) {
			JOptionPane.showMessageDialog(null, GUITools.toHTML(exc
					.getMessage(), 40), exc.getClass().getName(),
					JOptionPane.ERROR_MESSAGE);
			exc.printStackTrace();
		}
	}

	/**
	 * Writes the currently selected model into an ASCII text file.
	 * 
	 * @param out
	 */
	private void writeText(File out) {
		try {
			new TextExport(sbmlIO.getSelectedModel(), out);
		} catch (IOException exc) {
			JOptionPane.showMessageDialog(this, exc.getMessage(), exc
					.getClass().getName(), JOptionPane.WARNING_MESSAGE);
			exc.printStackTrace();
		}
	}
}
