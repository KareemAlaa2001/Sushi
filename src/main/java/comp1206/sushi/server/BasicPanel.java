package comp1206.sushi.server;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import comp1206.sushi.server.AddButtonListener;
import comp1206.sushi.server.RemoveListener;

public class BasicPanel extends JPanel {

	ServerWindow window;
	JTable table;
	Object[] columns;
	
	public BasicPanel(ServerWindow window, Object[] columns) 
	{
		this.window = window;
		this.columns = columns;
		
		init();
	}
	
	public void init() 
	{
		this.setLayout(new BorderLayout());
		
		MyTableModel model = new MyTableModel(columns,0);

		this.table = new JTable(model);
		
		JScrollPane scroll = new JScrollPane(table);
		table.setFillsViewportHeight(true);
		
		this.add(scroll);
		
		//	south panel with buttons
		JPanel south = new JPanel();
		south.setLayout(new FlowLayout());
		this.add(south, BorderLayout.SOUTH);
		
		//	buttons
		JButton add = new JButton("Add a new Item");
		JButton rm = new JButton("Remove Selected Item");
		south.add(add);
		south.add(rm);
			
		add.addActionListener(new AddButtonListener(table,window));
		rm.addActionListener(new RemoveListener(table,window));
		
		if (window.checkForRestocks(columns))
		{
			JButton edit = new JButton("Edit Restock Threshold & Amount");
			edit.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					int selectedRow = table.getSelectedRow();
					if (selectedRow >= 0)
					{
						@SuppressWarnings("unused")
						EditWindow editor = new EditWindow("Edit Restock Threshold & Amount", table, selectedRow, window);
					}
				}
			});
			south.add(edit);
		}
	}
	
	public JTable getTable()
	{
		return table;
	}
}
