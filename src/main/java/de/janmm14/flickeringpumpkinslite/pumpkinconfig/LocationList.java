package de.janmm14.flickeringpumpkinslite.pumpkinconfig;

import java.util.ArrayList;
import java.util.Collection;

import org.bukkit.Location;

/**
 * Class to save generics of the list with gson
 */
public class LocationList extends ArrayList<Location> {

	public LocationList(int initialCapacity) {
		super(initialCapacity);
	}

	public LocationList() {
	}

	public LocationList(Collection<? extends Location> c) {
		super(c);
	}

	public LocationList clone() {
		return (LocationList) super.clone();
	}
}
