package comp1206.sushi.server;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JTable;

class AddButtonListener implements ActionListener {
	
	JTable table;
	ServerWindow window;
	
	public AddButtonListener(JTable table,ServerWindow outer)
	{
		this.table = table;
		this.window = outer;
	}
	
	public void actionPerformed(ActionEvent e) {
		
		AddRowPopUpWindow addPopUp = new AddRowPopUpWindow("Add a new Row",table,window.getServer(),window);
	}
}