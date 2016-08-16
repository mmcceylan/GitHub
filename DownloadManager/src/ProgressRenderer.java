import java.awt.*;
import javax.swing.*;
import javax.swing.table.*;

//This class show the JProgressBar in a table cell
class ProgressRenderer extends JProgressBar implements TableCellRenderer {

	//ProgressRenderer constructor
	public ProgressRenderer(int min , int max){
		
		super(min , max);
		
	}
	
	//Fill the table cell to ProgressRenderer
	public Component getTableCellRendererComponent(JTable table , Object value , boolean isSelected , boolean hasFocus , int row , int column){
		
		//Set the percentage of JProgressBar completion
		setValue((int) ((Float) value).floatValue());
		return this;
		
	}

}
