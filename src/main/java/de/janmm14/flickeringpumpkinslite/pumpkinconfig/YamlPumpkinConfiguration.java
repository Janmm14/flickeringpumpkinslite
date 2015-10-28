package de.janmm14.flickeringpumpkinslite.pumpkinconfig;

import lombok.Getter;
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
	@NotNull
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
		Object data = cfg.get(PUMPKIN_LOCATIONS_PATH);
		if (data != null) {
			pumpkinLocations.addAll((Collection<? extends Location>) data);
		}
	}

	public void copyFrom(PumpkinConfiguration pumpkinConfiguration) {
		pumpkinLocations.clear();
		pumpkinLocations.addAll(pumpkinConfiguration.getPumpkinLocations());
	}
}
