package org.bdwyer.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import net.miginfocom.swing.MigLayout;

import org.bdwyer.scanner.FileFinder;
import org.bdwyer.scanner.FilesStringFinder;
import org.bdwyer.scanner.StringFinder.StringMatchVisitor;

public class StringFinderPanel {

	private final List<File> files;
	private final JPanel panel;
	private final JTabbedPane tabs;
	private final JTextField baseDirInput;
	private final JTextField fileInput;
	private final JTextField searchInput;
	private final JButton searchButton;
	private final JLabel baseDirPrompt;
	private final JLabel filePrompt;
	private final JLabel searchPrompt;
	private final JLabel filesPrompt;
	private final JLabel filesResult;
	private final JButton getFilesButton;

	public StringFinderPanel() {
		this.files = new ArrayList<File>();

		this.baseDirPrompt = new JLabel("Base Dir");
		this.baseDirInput = new JTextField("C:\\dev\\workspaceTrunk\\gotham\\PGClient");// System.getProperty("user.dir"));
		this.filePrompt = new JLabel("File Pattern");
		this.fileInput = new JTextField(FileFinder.getExtensionPatterns(Arrays.asList("java", "properties", "xml", "jsp", "js")));

		this.filesPrompt = new JLabel("Files");
		this.filesResult = new JLabel("0 files found");
		this.getFilesButton = new JButton("Get Files");

		this.searchPrompt = new JLabel("Search String");
		this.searchInput = new JTextField();
		this.searchButton = new JButton("Search");
		this.tabs = new JTabbedPane();

		this.panel = new JPanel(new MigLayout("fill, ins 8, gap 8", "[p!][fill]", "[p!][p!][p!][p!][fill]"));
		this.panel.add(baseDirPrompt, "cell 0 0, sgy 0");
		this.panel.add(baseDirInput, "cell 1 0, sgy 0");
		this.panel.add(filePrompt, "cell 0 1, sgy 0");
		this.panel.add(fileInput, "cell 1 1, sgy 0");

		this.panel.add(filesPrompt, "cell 0 2, sgy 0");
		this.panel.add(filesResult, "cell 1 2, sgy 0, split 2, growx");
		this.panel.add(getFilesButton, "cell 1 2, sgy 0, sgx 1");

		this.panel.add(searchPrompt, "cell 0 3, sgy 0");
		this.panel.add(searchInput, "cell 1 3, sgy 0, split 2, growx");
		this.panel.add(searchButton, "cell 1 3, sgy 0, sgx 1");
		this.panel.add(tabs, "cell 0 4, spanx 2, grow");

		getFilesButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				reloadFiles();
			}
		});

		searchInput.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				enqueueSearch();
			}
		});

		searchButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				enqueueSearch();
			}
		});
	}

	private void reloadFiles() {
		final String baseDir = baseDirInput.getText();
		final String filePattern = fileInput.getText();
		files.clear();
		files.addAll(FileFinder.getFilesMatching(baseDir, filePattern));
		filesResult.setText(String.format("%d files found", files.size()));
	}

	private void enqueueSearch() {
		// get parameters
		final String target = searchInput.getText();

		// add new tab
		final DefaultListModel model = new DefaultListModel();
		final JList list = new JList(model);
		final JScrollPane scroller = new JScrollPane(list);
		scroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		tabs.addTab("Searching...", scroller);
		final int tabIndex = tabs.indexOfComponent(scroller);
		tabs.setSelectedIndex(tabIndex);

		// start new thread
		final Thread thread = new Thread() {
			public void run() {
				// update tab title
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						tabs.setTitleAt(tabIndex, String.format("Searching for \"%s\"...", target));
					}
				});

				// do search
				FilesStringFinder.findThreaded(8, files, target, new StringMatchVisitor() {
					public void found(final File file, final int lineOffset) {
						SwingUtilities.invokeLater(new Runnable() {
							public void run() {
								final String foundString = String.format("%s line %d (%s)", file.getName(), lineOffset, file
										.getAbsolutePath());
								model.addElement(foundString);
								list.repaint();
								tabs.setTitleAt(tabIndex, String.format("Searching for \"%s\" (%d)...", target, model.getSize()));
							}
						});
					}
				});

				// update tab title
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						tabs.setTitleAt(tabIndex, String.format("Search for \"%s\" (%d) [Done]", target, model.getSize()));
					}
				});
			}
		};

		thread.setName(String.format("Search thread for \"%s\"", target));
		thread.start();
	}

	public static void main(String[] args) {
		System.setProperty("awt.useSystemAAFontSettings", "lcd");
		System.setProperty("swing.aatext", "true");
		final StringFinderPanel finderPanel = new StringFinderPanel();

		final JFrame f = new JFrame("Multi-Threaded String Finder");
		f.setContentPane(finderPanel.panel);
		f.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		f.setSize(600, 800);
		f.setVisible(true);
	}
}
