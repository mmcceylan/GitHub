import java.awt.*;
import java.awt.event.*;
import java.net.*;
import java.util.*;
import java.util.jar.JarOutputStream;
import javax.swing.*;
import javax.swing.event.*;


//Download Manager
public class DownloadManager extends JFrame implements Observer {
	
	//Add the downloading test field
	private JTextField addTextField;
	
	//Data model of download table
	private DownloadsTableModel tableModel;
	
	//Showing table to download
	private JTable table;
	
	//Download management buttons
	private JButton pauseButton, resumeButton;
	private JButton cancelButton, clearButton;
	
	//Chosen download operation
	private Download selectedDownload;
	
	//Sign of the clear or not for chosen table field
	private boolean clearing;
	
	//Download Manager Constructor
	public DownloadManager(){
		
		//Application title
		setTitle("CCeylan Download Manager");
		
		//Window size
		setSize(640,480);
		
		//Manage the window operation 
		addWindowListener(new WindowAdapter(){
			
			public void windowClosing(WindowEvent e){
				
				actionExit();
				
			}
			
		});
		
		//Prepare the FileMenu
		JMenuBar menuBar = new JMenuBar();
		JMenu fileMenu = new JMenu("File");
		fileMenu.setMnemonic(KeyEvent.VK_F);
		JMenuItem fileExitMenuItem = new JMenuItem("Exit",KeyEvent.VK_X);
		fileExitMenuItem.addActionListener(new ActionListener(){
			
			public void actionPerformed(ActionEvent e){
				
				actionExit();
				
			}
			
		});
		
		fileMenu.add(fileExitMenuItem);
		menuBar.add(fileMenu);
		setJMenuBar(menuBar);
		
		//Prepare the add panel
		JPanel addPanel = new JPanel();
		addTextField = new JTextField(30);
		addPanel.add(addTextField);
		JButton addButton = new JButton("Add Download");
		addButton.addActionListener(new ActionListener(){
			
			public void actionPerformed(ActionEvent e){
				
				actionAdd();

			}
			
		});
		addPanel.add(addButton);
		
		//Prepare the Download Table
		tableModel = new DownloadsTableModel();
		table = new JTable(tableModel);
		table.getSelectionModel().addListSelectionListener(new ListSelectionListener(){
			
			public void valueChanged(ListSelectionEvent e){
				
				tableSelectionChanged();
				
			}

		});
		
		//Give the permission on once only one row choose
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		
		//Prepare the ProgressBar to show Progress column
		ProgressRenderer renderer = new ProgressRenderer(0, 100);
		renderer.setStringPainted(true); //show the progress
		table.setDefaultRenderer(JProgressBar.class, renderer);
		
		//Configure the rows' height for JProgressBar borders
		table.setRowHeight((int) renderer.getPreferredSize().getHeight());
		
		//Prepare the download panel
		JPanel downloadsPanel = new JPanel();
		downloadsPanel.setBorder(BorderFactory.createTitledBorder("Downloads"));
		downloadsPanel.setLayout(new BorderLayout());
		downloadsPanel.add(new JScrollPane(table), BorderLayout.CENTER);
		
		//Prepare the buttons panel
		JPanel buttonsPanel = new JPanel();
		
		pauseButton = new JButton("Pause");
		pauseButton.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				actionPause();
			}
		});
		pauseButton.setEnabled(false);
		buttonsPanel.add(pauseButton);
		
		resumeButton = new JButton("Resume");
		resumeButton.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				actionResume();
			}
		});
		resumeButton.setEnabled(false);
		buttonsPanel.add(resumeButton);
		
		cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				actionCancel();
			}
		});
		cancelButton.setEnabled(false);
		buttonsPanel.add(cancelButton);
		
		clearButton = new JButton("Clear");
		clearButton.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				actionClear();
			}
		});
		clearButton.setEnabled(false);
		buttonsPanel.add(clearButton);
		
		//Add the panel to be showed
		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(addPanel, BorderLayout.NORTH);
		getContentPane().add(downloadsPanel, BorderLayout.CENTER);
		getContentPane().add(buttonsPanel, BorderLayout.SOUTH);
		
	}
	
	//Close the program
	private void actionExit(){
		System.exit(0);
	}
	
	//Add new download operation
	private void actionAdd(){
		URL verifiedUrl = verifyUrl(addTextField.getText());
		if(verifiedUrl != null){
			tableModel.addDownload(new Download(verifiedUrl));
			addTextField.setText("");//Ýnitiate the text enter area
		} else {
			JOptionPane.showMessageDialog(this, "Invalid Download URL", "Error", JOptionPane.ERROR_MESSAGE);
		}
	}
	
	//verify the download URL
	private URL verifyUrl(String url){
		//Only allow HTTP URLs
		if(!url.toLowerCase().startsWith("http://"))
			return null;
		
		//verify the URL form
		URL verifiedUrl = null;
		try {
			verifiedUrl = new URL(url);
		} catch (Exception e) {
			// TODO: handle exception
			return null;
		}
		
		//Make sure the url indicate the any file 
		if(verifiedUrl.getFile().length() < 2)
			return null;
		
		return verifiedUrl;
	}
	
	//to be Called when table's row is changed
	private void tableSelectionChanged(){
		
		//Erase the last download operation's notifications' log
		if(selectedDownload != null)
			selectedDownload.deleteObserver(DownloadManager.this);
		
		//Bir indirme iþlemini silmenin ortasýnda deðilse seçili indirme iþlemini ayarla ve bundan bildirimler almak için kaydol.
		if(!clearing && table.getSelectedRow() > -1){
			selectedDownload = tableModel.getDownload(table.getSelectedRow());
			selectedDownload.addObserver(DownloadManager.this);
			updateButtons();
		}
	}
	
	//Pause the selected operations
	private void actionPause(){
		selectedDownload.pause();
		updateButtons();
	}
	
	//Continue the selected download operations
	private void actionResume(){
		selectedDownload.resume();
		updateButtons();
	}
	
	//Cancel the selected download operations
	private void actionCancel(){
		selectedDownload.cancel();
		updateButtons();
	}
	
	//Erase the selected download oparetions
	private void actionClear(){
		clearing = true;
		tableModel.clearDownload(table.getSelectedRow());
		clearing = false;
		selectedDownload = null;
		updateButtons();
	}
	
	//Update the button every selected download operations' conditions
	private void updateButtons(){
		if(selectedDownload != null){
			int status = selectedDownload.getStatus();
			switch (status) {
			case Download.DOWNLOADING:
				pauseButton.setEnabled(true);
				resumeButton.setEnabled(false);
				cancelButton.setEnabled(true);
				clearButton.setEnabled(false);
				break;
			case Download.PAUSED:
				pauseButton.setEnabled(false);
				resumeButton.setEnabled(true);
				cancelButton.setEnabled(true);
				clearButton.setEnabled(false);
				break;
			case Download.ERROR:
				pauseButton.setEnabled(false);
				resumeButton.setEnabled(true);
				cancelButton.setEnabled(false);
				clearButton.setEnabled(true);
				break;
			default: //COMPLETE or CANCELLED
				pauseButton.setEnabled(false);
				resumeButton.setEnabled(false);
				cancelButton.setEnabled(false);
				clearButton.setEnabled(true);
				break;
			}
		} else {
			//there is no selected download operations
			pauseButton.setEnabled(false);
			resumeButton.setEnabled(false);
			cancelButton.setEnabled(false);
			clearButton.setEnabled(false);
		}
	}
	
	//Observer part for any change of condition
	public void update(Observable o, Object arg){
		//If the any change of download operation update the buttons
		if(selectedDownload != null && selectedDownload.equals(o))
			updateButtons();
	}
	
	//Start the download manager
	public static void main(String[] args){
		DownloadManager manager = new DownloadManager();
		manager.setVisible(true);
	}

}
