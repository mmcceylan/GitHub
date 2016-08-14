import java.util.*;
import javax.swing.*;
import javax.swing.table.*;

//This class manage the data of download table 
class DownloadsTableModel extends AbstractTableModel implements Observer{
	
	//Table columns' name
	private static final String[] columnNames = {"URL" , "Size" , "Progress" , "Status"};
	
	//Class of every column value
	private static final Class[] columnClasses = {String.class , String.class , JProgressBar.class , String.class};
	
	//List of the downloading names
	private ArrayList<Download> downloadList = new ArrayList<Download>();
	
	//Add the new download operation
	public void addDownload(Download download){
		
		//Sign up the notify when download operation finishes
		download.addObserver(this);
		
		downloadList.add(download);
		
		//To add the new table row sent the notification to table
		fireTableRowsInserted(getRowCount() - 1 , getRowCount() - 1);
		
	}
	
	//Get the download operation to the stated row
	public Download getDownload(int row){
		
		return(Download) downloadList.get(row);
		
	}
	
	//Delete a download operation to downloadList
	public void clearDownload(int row){
		
		downloadList.remove(row);
		
		//Sent the notification about removed table's row
		fireTableRowsDeleted(row , row);
		
	}
	
	//Get the number of table column
	public int getColumnCount(){
		
		return columnNames.length;
		
	}
	
	//Get the name of table column
	public String getColumnName(int col){
		
		return columnNames[col];
		
	}
	
	//Get the class of table column
	public Class getColumnClass(int col){
		
		return columnClasses[col];
		
	}
	
	//Get the number of table's row
	public int getRowCount(){
		
		return downloadList.size();
		
	}
	
	//Get the value for certain row and column
	public Object getValueAt(int row , int column){
		
		Download download = downloadList.get(row);
		
		switch(column){
		
		case 0: //URL
			return download.getUrl();
			
		case 1: //Size
			int size = download.getSize();
			return (size == -1) ? "" : Integer.toString(size);
			
		case 2: //Progress
			return new Float(download.getProgress());
			
		case 3: //Status
			return Download.STATUSES[download.getStatus()];
		
		}
		
		return "";
		
	}
	
	//Call the update() method if there is any changes notify
	public void update(Observable o , Object arg){
		
		int index = downloadList.indexOf(o);
		
		//Sent the table to table rows' update
		fireTableRowsUpdated(index , index);
		
	}

}
