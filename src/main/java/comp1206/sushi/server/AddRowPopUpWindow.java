package comp1206.sushi.server;

import javax.swing.*;
import javax.swing.table.*;

import comp1206.sushi.common.Postcode;
import comp1206.sushi.common.Supplier;
import comp1206.sushi.mock.MockServer;

import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;

@SuppressWarnings("serial")
public class AddRowPopUpWindow extends JFrame{
	
	MyTableModel tableModel;
	JTable table;
	Object[] rowContents;
	ServerInterface server;
	ServerWindow window;
	public AddRowPopUpWindow(String title, JTable table, ServerInterface server,ServerWindow window) {
		
		super(title);
		this.table = table;
		this.tableModel = (MyTableModel) table.getModel();
		this.server = server;
		this.window = window;
		rowContents = new Object[tableModel.getColumnCount()];
		init();
	}
	
	public void init() {
		
		JPanel content = new JPanel();
		this.setContentPane(content);
		content.setLayout(new FlowLayout());
		
		List<JComponent> inputFields = new ArrayList<>();
		String[] pcNames = new String[server.getPostcodes().size()];
		String[] suppNames = new String[server.getSuppliers().size()];
		
		for (int i =0; i < pcNames.length; i++)
		{
			pcNames[i] = server.getPostcodes().get(i).getName();
		}
		
		for (int i =0; i < suppNames.length; i++)
		{
			suppNames[i] = server.getSuppliers().get(i).getName();
		}
		
		JComboBox<String> pcCombo = new JComboBox<>(pcNames);
		JComboBox<String> suppCombo = new JComboBox<>(suppNames);

		if (table.getColumnName(1).equals("PostCode") || table.getColumnName(2).equals("Supplier") || table.getColumnName(1).equals("Description"))
		{
			for (int i = 0; i < tableModel.getColumnCount(); i++) {
				
				switch(tableModel.getColumnName(i)) {
				case "PostCode":
					inputFields.add(pcCombo);
					content.add(pcCombo);
					break;
				case "Source":
					inputFields.add(pcCombo);
					content.add(pcCombo);
					break;
				case "Destination":
					JComboBox<String> newPcCombo = new JComboBox<String>(pcCombo.getModel());
					inputFields.add(newPcCombo);
					content.add(newPcCombo);
					break;
				case "Supplier":
					inputFields.add(suppCombo);
					content.add(suppCombo);
					break;
				case "Distance":
					break;
				case "Current Stock Level":
					break;
					
				default:
					inputFields.add(new JTextField(35));
				}
			}
			
			for (int i = 0; i < inputFields.size(); i++)
			{
				content.add(new JLabel(tableModel.getColumnName(i) + ": "));
				content.add(inputFields.get(i));
			}
		}
		else if (table.getColumnName(1).equals("Speed"))
		{
			content.add(new JLabel(tableModel.getColumnName(0) + ": "));
			inputFields.add(new JTextField(35));
			content.add(inputFields.get(0));
			
			content.add(new JLabel(tableModel.getColumnName(1) + ": "));
			inputFields.add(new JTextField(35));
			content.add(inputFields.get(1));
		}
		
		else 
		{
			content.add(new JLabel(tableModel.getColumnName(0) + ": "));
			inputFields.add(new JTextField(35));
			content.add(inputFields.get(0));
		}

		JButton addAll = new JButton("Add row with field contents");
		
		class AddButtonListener implements ActionListener {
			
			AddRowPopUpWindow outer;
			public AddButtonListener(AddRowPopUpWindow win) {

				this.outer = win;
			}
			
			// TODO ADD CHECKS
			public void actionPerformed(ActionEvent ae) {
				for (int i = 0;i < inputFields.size(); i++)
				{
					if (inputFields.get(i) instanceof JTextField)
					{
						JTextField tf = (JTextField) inputFields.get(i);
						rowContents[i] = tf.getText();
					}
					if (inputFields.get(i) instanceof JComboBox<?>)
					{
						JComboBox<String> tf = (JComboBox<String>) inputFields.get(i);
						rowContents[i] = tf.getSelectedItem();
					}
				}
				
				if (checkUnique(rowContents[0].toString(),0))
				{
					if (rowContents.length > 1)
					{
						if (table.getColumnName(1).equals("PostCode"))
						{
							if (checkUnique(rowContents[1].toString(), 1))
							{
								try {
									window.addDataToServer(rowContents, tableModel);
								} catch (IllegalArgumentException e) {
									JOptionPane.showMessageDialog(window,"Not a number!","Error", JOptionPane.ERROR_MESSAGE);
									return;
								}
								window.refreshAll();
								outer.setVisible(false);
								return;
							}
							else {
								JOptionPane.showMessageDialog(window,"The object you tried to add has a duplicate " + table.getColumnName(1),"Error", JOptionPane.ERROR_MESSAGE);
								return;
							}
						}
					}
					
					try {
						window.addDataToServer(rowContents, tableModel);
					} catch (IllegalArgumentException e) {
						JOptionPane.showMessageDialog(window,e.getMessage(),"Error", JOptionPane.ERROR_MESSAGE);
						return;
					}
					window.refreshAll();
					outer.setVisible(false);
				}
				else JOptionPane.showMessageDialog(window,"The object you tried to add has a duplicate " + table.getColumnName(0),"Error", JOptionPane.ERROR_MESSAGE);
			}
			
			private boolean checkUnique(String name, int colIndex)
			{
				boolean uniq = true;
				for (int i = 0; i < table.getRowCount(); i++)
				{
					if ((table.getValueAt(i, colIndex).toString()).equals(name))
					{
						uniq = false;
					}
				}
				return uniq;
			}
			
		}
		
		addAll.addActionListener(new AddButtonListener(this));
		content.add(addAll);
		setSize(400,300);
		setLocationRelativeTo(null);
		setVisible(true);
	}
	
	public Object[] getRowContents() {
		
		return rowContents;
	}
	
	public MyTableModel getModel() {
		
		return tableModel;
	}
	
	public JTable getTable() {
		
		return table;
	}
}
