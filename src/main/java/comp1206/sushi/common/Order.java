package comp1206.sushi.common;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

public class Order extends Model {

	private String status;
	private User user;
	private  Map<Dish, Number> contents;
	private Number weight;
	private boolean needsDelivery;
	
	public Order(User user, Map<Dish, Number> contents)
	{
		this.user = user;
		this.contents = contents;
	
		this.status = "Pending";
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/YYYY HH:mm:ss");  
		LocalDateTime now = LocalDateTime.now();  
		this.name = dtf.format(now);
		this.needsDelivery = true;
		this.calculateWeight(contents);
	}

	public Number getDistance() {
		return 1;
	}

	@Override
	public String getName() {
		return this.name;
	}

	public String getStatus() {
		return status;
	}
	
	public boolean needsDelivery()
	{
		return this.needsDelivery;
	}
	
	public void setDeliveryNeed(boolean need)
	{
		notifyUpdate("needsDelivery",this.needsDelivery, need);
		this.needsDelivery = need;
	}

	public void setStatus(String status) {
		notifyUpdate("status",this.status,status);
		this.status = status;
	}
	
	public User getCustomer()
	{
		return this.user;
	}
	
	public Map<Dish,Number> getContents() {
		return this.contents;
	}

	public boolean isComplete() {
		return true;
	}
	
	public Number getCost()
	{
		Integer cost = 0;
		for (Map.Entry<Dish, Number> entry: contents.entrySet())
		{
			Integer dishPrice = (Integer) entry.getKey().getPrice();
			Integer quantity = (Integer) entry.getValue();
			cost += dishPrice*quantity;
		}
		return cost;
	}
	
	public Number getWeight()
	{
		return this.weight;
	}
	
	public void setWeight(Number weight)
	{
		notifyUpdate("weight", this.weight, weight);
		this.weight = weight;
	}
	
	private Number calculateWeight(Map<Dish,Number> contents)
	{
		Double weight = Double.valueOf(0);
		for (Map.Entry<Dish, Number> entry: contents.entrySet())
		{
			weight += (Double) entry.getKey().getWeight();
		}
		this.setWeight(weight);
		return weight;
	}
}
