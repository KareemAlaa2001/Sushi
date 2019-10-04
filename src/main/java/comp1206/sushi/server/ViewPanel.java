package comp1206.sushi.server;

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

public class ViewPanel extends JPanel {

	ServerWindow window;
	JTable table;
	Object[] columns;
	
	public ViewPanel(ServerWindow window, Object[] columns) {
		this.columns = columns;
		this.window = window;
		init();
	}
	
	private void init() 
	{
		this.setLayout(new BorderLayout());
		
		table = new JTable(new MyTableModel(columns,0));
		
		JScrollPane scroll = new JScrollPane(table);
		table.setFillsViewportHeight(true); 
		
		this.add(scroll,BorderLayout.CENTER);
	}
	
	public JTable getTable()
	{
		return table;
	}
}
