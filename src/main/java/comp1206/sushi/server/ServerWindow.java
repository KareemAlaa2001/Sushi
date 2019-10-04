package comp1206.sushi.server;

import java.awt.BorderLayout;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import comp1206.sushi.common.*;

/**
 * Provides the Sushi Server user interface
 *
 */
public class ServerWindow extends JFrame implements UpdateListener {

	private static final long serialVersionUID = -4661566573959270000L;
	private ServerInterface server;
	private BasicPanel postCodes, staff, drones, suppliers, ingredients;
	DishesPanel dishes;
	DataPopulator pop;
	ViewPanel users, orders;
	/**
	 * Create a new server window
	 * @param server instance of server to interact with
	 */
	public ServerWindow(ServerInterface server) {
		super("Sushi Server");
		this.server = server;
		this.setTitle(server.getRestaurantName() + " Server");
		server.addUpdateListener(this);
		

		init();
		
		//Start timed updates
		startTimer();
	}
	
	//	initialize GUI
	public void init() {
			
		//	setting content pane and adding tabbed pane as main container
		JPanel contentPane = new JPanel();
		this.setContentPane(contentPane);
		contentPane.setLayout(new BorderLayout());
		JTabbedPane serverMenu = new JTabbedPane();
		contentPane.add(serverMenu, BorderLayout.CENTER); 	
		
		//  Creating tabs of the tabbedpane
		postCodes = new BasicPanel(this, new Object[] {"Code","Latitude","Longitude","Distance"});
		
		staff = new BasicPanel(this, new Object[] {"Name","Status","Fatigue"});
		
		drones = new BasicPanel(this, new Object[] {"Name","Speed","Capacity","Battery","Status","Source","Destination","Progress"} );
		
		suppliers = new BasicPanel(this, new Object[] {"Name","PostCode","Distance"});
		
		ingredients = new BasicPanel(this, new Object[] {"Name","Unit","Supplier","Restock Threshold", "Restock Amount","Current Stock Level"});
		
		dishes = new DishesPanel(this, new Object[] {"Name","Description","Price","Restock Threshold","Restock Amount","Current Stock Level"});
		
		users = new ViewPanel(this, new Object[] {"Username", "Password","Address","PostCode","Distance"});
		
		orders = new ViewPanel(this, new Object[] {"Name","Status","Distance","Cost"});
			
		
			
		pop = new DataPopulator(this);
		pop.populateServerData();
		
		
		//	Adding tabs to tabbed pane
		serverMenu.addTab("Postcodes", postCodes);
		serverMenu.addTab("Staff", staff);
		serverMenu.addTab("Drones", drones);
		serverMenu.addTab("Suppliers", suppliers);
		serverMenu.addTab("Ingredients", ingredients);
		serverMenu.addTab("Dishes", dishes);
		serverMenu.addTab("Users",users);
		serverMenu.addTab("Orders", orders);
		
		
		
		//Display window
		setSize(800,600);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setVisible(true);
	}
	
	/**
	 * Start the timer which updates the user interface based on the given interval to update all panels
	 */
	public void startTimer() {
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);     
        int timeInterval = 5;
        
        scheduler.scheduleAtFixedRate(() -> refreshAll(), 0, timeInterval, TimeUnit.SECONDS);
	}
	
	/**
	 * Refresh all parts of the server application based on receiving new data, calling the server afresh
	 */
	public void refreshAll() {
		
		((MyTableModel) postCodes.getTable().getModel()).setRowCount(0);
		((MyTableModel) staff.getTable().getModel()).setRowCount(0);
		((MyTableModel) drones.getTable().getModel()).setRowCount(0);
		((MyTableModel) ingredients.getTable().getModel()).setRowCount(0);
		((MyTableModel) suppliers.getTable().getModel()).setRowCount(0);
		((MyTableModel) dishes.getDishTable().getModel()).setRowCount(0);
		((MyTableModel) users.getTable().getModel()).setRowCount(0);
		((MyTableModel) orders.getTable().getModel()).setRowCount(0);

		dishes.getRecipeModels().clear();
	
		pop.populateServerData();
		
		
	}
	
	@Override
	/**
	 * Respond to the model being updated by refreshing all data displays
	 */
	public void updated(UpdateEvent updateEvent) {
		refreshAll();
	}
	
	public void addDataToServer(Object [] data, MyTableModel model) throws IllegalArgumentException{
		
		String idColName = model.getColumnName(1);
		
		switch(idColName) {
			//TODO VALIDATE PC FORMAT HERE
			case "Latitude":
				String pcName = data[0].toString();
				if (pcName.matches("^[A-Z]{1,2}[0-9R][0-9A-Z]? [0-9][ABD-HJLNP-UW-Z]{2}$"))
					server.addPostcode(pcName.toString());
				else throw new IllegalArgumentException("Not a valid postcode format!");
				break;
				//TODO VALIDATE NUMBER FORMATS EVERYWHERE ELSE 
			case "Status":
				server.addStaff(data[0].toString());
				break;
				
			case "Speed":
				try {
					server.addDrone(Integer.valueOf(data[1].toString()));
				} catch (NumberFormatException e) {
					throw new IllegalArgumentException("The speed entered is not a number!");
				}
				break;
				
			case "Unit":
				try {
					server.addIngredient(data[0].toString(),data[1].toString(), findSupplierByName(data[2].toString()),
							Integer.valueOf(data[3].toString()),Integer.valueOf(data[4].toString()));
				} catch (NumberFormatException e) {
					throw new IllegalArgumentException("The value entered in either the Restock Threshold or Restock Amount field is not a number!");
				}
				
				break;
				
			case "Description":
				try {
					server.addDish(data[0].toString(),data[1].toString(),Integer.valueOf(data[2].toString()),
							Integer.valueOf(data[3].toString()),Integer.valueOf(data[4].toString()));
					} catch (NumberFormatException e) {
					throw new IllegalArgumentException("The value entered in either the Price, Restock Threshold or Restock Amount field is not a number!");
				}
				
				break;
				
			case "PostCode":
		
				server.addSupplier(data[0].toString(),findPostcodeByName(data[1].toString()));
				break;
		}
	}
	
	public ServerInterface getServer()
	{
		return server;
	}
	
	public BasicPanel getPcPanel()
	{
		return postCodes;
	}
	
	public BasicPanel getStaffPanel()
	{
		return staff;
	}
	
	public BasicPanel getDronesPanel()
	{
		return drones;
	}
	
	public BasicPanel getSuppliersPanel()
	{
		return suppliers;
	}
	
	public BasicPanel getIngredientsPanel()
	{
		return ingredients;
	}
	
	public DishesPanel getDishesPanel()
	{
		return dishes;
	}
	
	public ViewPanel getUsersPanel()
	{
		return users;
	}
	
	public ViewPanel getOrdersPanel()
	{
		return orders;
	}
	
	public Dish findDishByName(String name) throws IllegalArgumentException{
		
		for (int i = 0; i < server.getDishes().size() ; i++)
		{
			if (server.getDishes().get(i).getName().equals(name))
			{
				return server.getDishes().get(i);
			}
		}
		throw new IllegalArgumentException("There are no dishes with the given name!");
	}

	
	public Postcode findPostcodeByName(String name) throws IllegalArgumentException {
		for (int i = 0; i < server.getPostcodes().size() ; i++)
		{
			if (server.getPostcodes().get(i).getName().equals(name))
			{
				return server.getPostcodes().get(i);
			}
		}
		throw new IllegalArgumentException("There are no postcodes with the given name!");
	}

	
	public Staff findStaffByName(String name) throws IllegalArgumentException {
		for (int i = 0; i < server.getStaff().size() ; i++)
		{
			if (server.getStaff().get(i).getName().equals(name))
			{
				return server.getStaff().get(i);
			}
		}
		throw new IllegalArgumentException("There are no staff with the given name!");
	}

	
	public Drone findDroneByName(String name) throws IllegalArgumentException {
		for (int i = 0; i < server.getDrones().size() ; i++)
		{
			if (server.getDrones().get(i).getName().equals(name))
			{
				return server.getDrones().get(i);
			}
		}
		throw new IllegalArgumentException("There are no drones with the given name!");
	}

	
	public Supplier findSupplierByName(String name) throws IllegalArgumentException {
		for (int i = 0; i <server.getSuppliers().size() ; i++)
		{
			if (server.getSuppliers().get(i).getName().equals(name))
			{
				return server.getSuppliers().get(i);
			}
		}
		throw new IllegalArgumentException("There are no suppliers with the given name!");
	}

	
	public Ingredient findIngredientByName(String name) throws IllegalArgumentException {
		for (int i = 0; i < server.getIngredients().size() ; i++)
		{
			if (server.getIngredients().get(i).getName().equals(name))
			{
				return server.getIngredients().get(i);
			}
		}
		throw new IllegalArgumentException("There are no ingredients with the given name!");
	}
	
	public Object[] getColumnNames(JTable table)
	{
		Object[] columns = new Object[table.getColumnCount()];
		for (int i = 0; i < table.getColumnCount(); i++)
		{
			columns[i] = table.getColumnName(i);
		}
		
		return columns;
	}
	
	
	public boolean checkForRestocks(Object[] columns)
	{
		boolean thresh = false,amt = false;
		
		for(int i = 0; i < columns.length; i++)
		{
			if (((String) columns[i]).equals("Restock Threshold"))
			{
				thresh = true;
			}
			if (((String) columns[i]).equals("Restock Amount"))
			{
				amt = true;
			}
		}
		
		if (thresh && amt) return true;
		else return false;
	}


}
	



	

