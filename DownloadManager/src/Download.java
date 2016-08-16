import java.io.*;
import java.net.*;
import java.util.*;

//This class download from URL
class Download extends Observable implements Runnable {

	//Download buffer maximum size 
	private static final int MAX_BUFFER_SIZE = 1024;
	
	//Conditions name
	public static final String STATUSES[] = {"Downloading" , "Paused" , "Complete" , "Cancelled" , "Error"};
	
	//Conditons codes
	public static final int DOWNLOADING = 0;
	public static final int PAUSED = 1;
	public static final int COMPLETE = 2;
	public static final int CANCELLED = 3;
	public static final int ERROR = 4;
	
	private URL url; //URL to download
	private int size; //Downloading size the type of byte
	private int downloaded; //Number of downloaded byte
	private int status; //Downloading current conditions
	
	//Constructor for download
	public Download(URL url){
		
		this.url = url;
		size = -1;
		downloaded = 0;
		status = DOWNLOADING;
		
		//Start the download
		download();
		
	}
	
	//Get the URL of this download operation
	public String getUrl(){
		
		return url.toString();
		
	}
	
	//Get the size of this download operation
	public int getSize(){
		
		return size;
		
	}
	
	//Get the progress of this download operation
	public float getProgress(){
		
		return ((float)downloaded / size) * 100;
		
	}
	
	//Get the status of this download operation
	public int getStatus(){
		
		return status;
		
	}
	
	//Pause the download operation
	public void pause(){
		
		status = PAUSED;
		stateChanged();
		
	}
	
	//Continue the download operation
	public void resume(){
		
		status = DOWNLOADING;
		stateChanged();
		download();
		
	}
	
	//Cancel the download operation
	public void cancel(){
		
		status = CANCELLED;
		stateChanged();
		
	}
	
	//Error sign this download operation
	public void error(){
		
		status = ERROR;
		stateChanged();
		
	}
	
	//Start or continue the download operation
	private void download(){
		
		Thread thread = new Thread(this);
		thread.start();
		
	}
	
	//Get the FileName from URL
	private String getFileName(URL url){
		
		String fileName = url.getFile();
		return fileName.substring(fileName.lastIndexOf('/') + 1);
		
	}
	
	//Download the file
	public void run(){
		
		RandomAccessFile file = null;
		InputStream stream = null;
		
		try{
			
			//Open the URL connection
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			
			//Define the which part to download
			connection.setRequestProperty("Range", "bytes=" + downloaded + "-");
			
			//Connect the server
			connection.connect();
			
			//Check the response to right interval
			if(connection.getResponseCode() / 100 != 2){
				
				error();
				
			}
			
			//Check the valid content length
			int contentLength = connection.getContentLength();
			if (contentLength < 1){
				
				error();
				
			}
			
			//If there is no set the size of download set here
			if (size == -1){
				
				size = contentLength;
				stateChanged();
				
			}
			
			//Open the file and go the last
			file = new RandomAccessFile(getFileName(url) , "rw");
			file.seek(downloaded);
			
			stream = connection.getInputStream();
			while(status == DOWNLOADING){
				
				//Set the buffer size to the help of remain download amount
				byte buffer[];
				if (size - downloaded > MAX_BUFFER_SIZE){
					
					buffer = new byte[MAX_BUFFER_SIZE];
					
				}else{
					
					buffer = new byte[size - downloaded];
					
				}
				
				//Read from server to buffer
				int read = stream.read(buffer);
				if (read == 1)
					break;
				
				//Write the buffer to file
				file.write(buffer , 0 , read);
				downloaded += read;
				stateChanged();
				
			}
			
			//If the download is completed changed the status to complete
			if (status == DOWNLOADING){
				
				status = COMPLETE;
				stateChanged();
				
			}
			
		}catch(Exception e){
			
			error();
			
		}finally{
			
			//Close the file
			if (file != null){
				
				try{
					
					file.close();
					
				}catch(Exception e){}
				
			}
			
			//Close the server connection
			if (stream != null){
				
				try{
					
					stream.close();
					
				}catch(Exception e){}
				
			}
			
		}
		
	}
	
	//Report the status changes to observer
	private void stateChanged(){
		
		setChanged();
		notifyObservers();
		
	}

}