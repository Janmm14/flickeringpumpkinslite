package de.janmm14.flickeringpumpkinslite.pumpkinconfig;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;

public class YamlPumpkinConfiguration extends PumpkinConfiguration { //TODO support not loaded worlds

	private static final String PUMPKIN_LOCATIONS_PATH = "pumpkinLocations";
	private YamlConfiguration cfg;
	@Getter
	@NotNull
	private final Set<Location> pumpkinLocations = Collections.synchronizedSet(new HashSet<Location>());

	public YamlPumpkinConfiguration(@NotNull File file) {
		super(file);
	}

	@Override
	public void save() {
		serialize();
		try {
			cfg.save(getFile());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void serialize() {
		cfg.set(PUMPKIN_LOCATIONS_PATH, pumpkinLocations);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void reload() {
		cfg = YamlConfiguration.loadConfiguration(getFile());
		pumpkinLocations.clear();
		Object data = cfg.get(PUMPKIN_LOCATIONS_PATH);
		if (data != null) {
			pumpkinLocations.addAll((Collection<? extends Location>) data);
		}
	}

	@Override
	public void copyFrom(PumpkinConfiguration pumpkinConfiguration) {
		pumpkinLocations.clear();
		pumpkinLocations.addAll(pumpkinConfiguration.getPumpkinLocations());
	}
}
