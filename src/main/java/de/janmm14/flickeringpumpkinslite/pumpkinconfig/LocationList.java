package de.janmm14.flickeringpumpkinslite.pumpkinconfig;

import org.bukkit.Location;

import java.util.ArrayList;
import java.util.Collection;

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
