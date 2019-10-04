package comp1206.sushi.common;

import comp1206.sushi.common.Ingredient;
import comp1206.sushi.common.Supplier;

public class Ingredient extends Model {

	private String name;
	private String unit;
	private Supplier supplier;
	private Number restockThreshold;
	private Number restockAmount;
	private Number weight;
	private boolean needsRestock;
	
	public Ingredient(String name, String unit, Supplier supplier, Number restockThreshold,
			Number restockAmount, Number weight) {
		this.setRestockNeed(true);
		this.setName(name);
		this.setUnit(unit);
		this.setSupplier(supplier);
		this.setRestockThreshold(restockThreshold);
		this.setRestockAmount(restockAmount);
		this.setWeight(weight);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		notifyUpdate("name", this.name, name);
		this.name = name;
	}

	public String getUnit() {
		return unit;
	}

	public void setUnit(String unit) {
		notifyUpdate("unit", this.unit, unit);
		this.unit = unit;
	}

	public Supplier getSupplier() {
		return supplier;
	}

	public void setSupplier(Supplier supplier) {
		notifyUpdate("supplier", this.supplier, supplier);
		this.supplier = supplier;
	}

	public Number getRestockThreshold() {
		return restockThreshold;
	}

	public void setRestockThreshold(Number restockThreshold) {
		notifyUpdate("restockThreshold", this.restockThreshold, restockThreshold);
		this.restockThreshold = restockThreshold;
	}

	public Number getRestockAmount() {
		return restockAmount;
	}

	public void setRestockAmount(Number restockAmount) {
		notifyUpdate("restockAmount", this.restockAmount, restockAmount);
		this.restockAmount = restockAmount;
	}

	public Number getWeight() {
		return weight;
	}

	public void setWeight(Number weight) {
		this.weight = weight;
	}
	
	public Number getBatchWeight()
	{
		return (Integer) this.getWeight() * (Integer) this.getRestockAmount();
	}

	public boolean needsRestock()
	{
		return this.needsRestock;
	}
	
	public void setRestockNeed(boolean need)
	{
		this.notifyUpdate("needsRestock", this.needsRestock, need);
		this.needsRestock = need;
	}
}
