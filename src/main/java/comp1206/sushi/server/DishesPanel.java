package comp1206.sushi.server;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import comp1206.sushi.common.Ingredient;
import comp1206.sushi.server.ServerInterface.UnableToDeleteException;

@SuppressWarnings("serial")
public class DishesPanel extends JPanel {

	Object[] columns;
	ServerWindow window;
	List<MyTableModel> recipeModels;
	JTable dishTable;
	JTable recipeTable;
	
	public DishesPanel(ServerWindow window, Object[] columns)
	{
		this.columns = columns;
		this.window = window;
		recipeModels = new ArrayList<>();
		
		init();
	}
	
	private void init() 
	{
		this.setLayout(new GridLayout(2,1));
			
		JPanel dishUpperHalf = new JPanel();
		dishUpperHalf.setLayout(new BorderLayout());
		
		JPanel dishLowerHalf = new JPanel();
		dishLowerHalf.setLayout(new BorderLayout());
		
		MyTableModel dishModel = new MyTableModel(columns,0);
		dishTable = new JTable();
		dishTable.setModel(dishModel); 
		
		JScrollPane dishScroll = new JScrollPane(dishTable);
		dishTable.setFillsViewportHeight(true);
		
		recipeTable = new JTable();
		
		JScrollPane recipeScroll = new JScrollPane(recipeTable);
		recipeTable.setFillsViewportHeight(true);
		
		dishLowerHalf.add(recipeScroll,BorderLayout.CENTER);
		
		JPanel dishDeepSouth = new JPanel();
		dishDeepSouth.setLayout(new FlowLayout());
		
		JButton addDish = new JButton("Add new dish");
		JButton rmDish = new JButton("Remove selected dish");
		JButton editSelected = new JButton("Edit Selected Dish's Recipe");
		
		dishDeepSouth.add(addDish);
		dishDeepSouth.add(rmDish);
		dishDeepSouth.add(editSelected);

		editSelected.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				int selectedRow = dishTable.getSelectedRow();
				
				if (selectedRow >= 0)
				{
					@SuppressWarnings("unused")
					EditRecipeWindow editRecipe = new EditRecipeWindow(recipeModels.get(selectedRow), selectedRow);
				}
			}
			
			
		});
		dishLowerHalf.add(new JLabel("Recipe of Selected Dish:"),BorderLayout.NORTH);
		dishLowerHalf.add(dishDeepSouth,BorderLayout.SOUTH);
		
		
		dishUpperHalf.add(new JLabel("Dishes:"),BorderLayout.NORTH);
		dishUpperHalf.add(dishScroll,BorderLayout.CENTER);
		
		this.add(dishUpperHalf);
		this.add(dishLowerHalf);
		
		addDish.addActionListener(new AddDishListener());
		rmDish.addActionListener(new RmDishListener());
		dishTable.getSelectionModel().addListSelectionListener(new DishTableListener(dishTable, recipeModels, recipeTable));
		
		JButton edit = new JButton("Edit Restock Threshold & Amount");
		edit.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				int selectedRow = dishTable.getSelectedRow();
				if (selectedRow >= 0)
				{
					@SuppressWarnings("unused")
					EditWindow editor = new EditWindow("Edit Restock Threshold & Amount", dishTable, selectedRow, window);
				}
			}
		});
		dishDeepSouth.add(edit);
	}
	
	public JTable getRecipeTable()
	{
		return this.recipeTable;
	}
	
	public JTable getDishTable()
	{
		return this.dishTable;
	}
	
	public List<MyTableModel> getRecipeModels()
	{
		return this.recipeModels;
	}

	class DishTableListener implements ListSelectionListener 
	{
		JTable dishes;
		MyTableModel dishModel;
		List<MyTableModel> recipes;
		JTable recipeTable;
		  
		public DishTableListener(JTable dishes, List<MyTableModel> recipes, JTable recipeTable)
		{
			this.dishes = dishes;
			this.recipes = recipes;
			this.recipeTable = recipeTable;
			this.dishModel = (MyTableModel) dishes.getModel();
		}
		
		public void valueChanged(ListSelectionEvent e) 
		{
			int selectedRow = dishes.getSelectedRow();
			if (selectedRow >= 0) {
				if (recipeModels.size() == recipes.size() && recipes.size() == window.getServer().getDishes().size())
				{
					recipeTable.setModel(recipes.get(selectedRow));
				}
				
				else
				{
					this.recipes = recipeModels;
					recipeTable.setModel(recipes.get(selectedRow));
				}
			}
			
		}
	}
	
	class AddDishListener implements ActionListener
	{
		public void actionPerformed(ActionEvent e) {

			@SuppressWarnings("unused")
			AddRowPopUpWindow addPopUp = new AddRowPopUpWindow("Add a new Row",dishTable,window.getServer(),window);
		}
		
	}
	
	class RmDishListener implements ActionListener{

		ServerInterface server;
		public RmDishListener() {
			
			this.server = window.getServer();
		}
		
		public void actionPerformed(ActionEvent e) {
			removeSelectedDishes();
		}
		
		public void removeSelectedDishes(){
			
			int[] rows = dishTable.getSelectedRows();
			try {
				removeDishesFromServer(rows);
				window.refreshAll();
			} catch (UnableToDeleteException e) {
				JOptionPane.showMessageDialog(window,e.getMessage(),"Error", JOptionPane.ERROR_MESSAGE);
			} catch (Exception e) {
				JOptionPane.showMessageDialog(window,e.getMessage(),"Error", JOptionPane.ERROR_MESSAGE);			
			}
		}
		
		public void removeDishesFromServer(int[] rows) throws UnableToDeleteException {
			
			for (int i = 0; i < rows.length; i++)
			{
				server.removeDish(window.findDishByName((String) dishTable.getModel().getValueAt(rows[i], 0)));
			}
		}
		
	}
	
	class EditRecipeWindow extends JFrame {
		
		MyTableModel selectedRecipe;
		int dishIndex;
		
		public EditRecipeWindow(MyTableModel selectedRecipe, int recipeIndex)
		{
			super("Edit Recipe");
			this.selectedRecipe = selectedRecipe;
			this.dishIndex = recipeIndex;
			
			this.init();
		}
		
		public void init()
		{
			JPanel content = new JPanel();
			content.setLayout(new BorderLayout());
			this.setContentPane(content);
			
			content.add(new JLabel("Recipe Table:"), BorderLayout.NORTH);
			
			JTable viewRecipe = new JTable(selectedRecipe);
			
			JScrollPane scroll = new JScrollPane(viewRecipe);
			viewRecipe.setFillsViewportHeight(true);
			content.add(scroll, BorderLayout.CENTER);
			
			
			JPanel south = new JPanel();
			south.setLayout(new BoxLayout(south, BoxLayout.Y_AXIS));
			
			JPanel southFields = new JPanel();
			south.add(southFields);
			
			JComboBox<String> newIng = new JComboBox<String>();
			for (Ingredient ing: window.getServer().getIngredients())
			{
				newIng.addItem(ing.getName());
			}
			
			JTextField newNum = new JTextField(15);
			
			southFields.add(new JLabel("Ingredient:"));
			southFields.add(newIng);
			southFields.add(new JLabel("Number: "));
			southFields.add(newNum);
			
			JPanel southButtons = new JPanel();
			south.add(southButtons);
			
			JButton addIng = new JButton("Add Ingredient");
			JButton rmIng = new JButton("Remove Selected Ingredient");
			JButton confirm = new JButton("Confirm Changes");
			
			southButtons.add(addIng);
			southButtons.add(rmIng);
			southButtons.add(confirm);
			
			addIng.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					// TODO VALIDATE INGREDIENTS MAKE SURE NO DUPLICATES
					if (checkUnique(newIng.getSelectedItem().toString(), 0))
						selectedRecipe.addRow(new Object[]{newIng.getSelectedItem(), newNum.getText()});
					else JOptionPane.showMessageDialog(window,"Ingredient already in use in the recipe!","Error", JOptionPane.ERROR_MESSAGE);

				}
				
				private boolean checkUnique(String name, int colIndex)
				{
					boolean uniq = true;
					for (int i = 0; i < viewRecipe.getRowCount(); i++)
					{
						if ((viewRecipe.getValueAt(i, colIndex).toString()).equals(name))
						{
							uniq = false;
						}
					}
					return uniq;
				}
			});
			
			rmIng.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					int selectedRow = viewRecipe.getSelectedRow();
					if (selectedRow >= 0)
					{
						selectedRecipe.removeRow(selectedRow);
					}
					
				}
			
			});
			
			confirm.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent ae) {
					
					
				
					
					Map<Ingredient, Number> newRecipe = new HashMap<>();
					
					for (int i = 0; i < selectedRecipe.getRowCount(); i++)
					{
						newRecipe.put(window.findIngredientByName(selectedRecipe.getValueAt(i, 0).toString()),
								Integer.valueOf(selectedRecipe.getValueAt(i, 1).toString()));
						
					}
					
					
					window.findDishByName(dishTable.getValueAt(dishIndex, 0).toString()).setRecipe(newRecipe);
					
					
					setVisible(false);
				}
				
			});
			content.add(south,BorderLayout.SOUTH);
			
			this.setLocationRelativeTo(null);
			this.setSize(600,400);
			this.setVisible(true);
		}
	}

}
