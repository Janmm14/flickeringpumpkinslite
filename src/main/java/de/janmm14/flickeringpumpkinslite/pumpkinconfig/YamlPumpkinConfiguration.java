package de.janmm14.flickeringpumpkinslite.pumpkinconfig;

import lombok.Getter;
import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class YamlPumpkinConfiguration extends PumpkinConfiguration { //TODO support not loaded worlds

	private static final String PUMPKIN_LOCATIONS_PATH = "pumpkinLocations";
	private YamlConfiguration cfg;
	@Getter
	@NonNull
	private final List<Location> pumpkinLocations = Collections.synchronizedList(new ArrayList<>());

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
		pumpkinLocations.addAll((Collection<? extends Location>) cfg.get(PUMPKIN_LOCATIONS_PATH));
	}

	public void copyFrom(PumpkinConfiguration pumpkinConfiguration) {
		pumpkinLocations.clear();
		pumpkinLocations.addAll(pumpkinConfiguration.getPumpkinLocations());
	}
}
