package de.janmm14.flickeringpumpkinslite.pumpkinconfig;

import com.google.common.base.Joiner;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.janmm14.flickeringpumpkinslite.LocationJsonAdapter;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.Location;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Collections;
import java.util.List;

public class JsonPumpkinConfiguration extends PumpkinConfiguration { //TODO support not loaded worlds

	private static final transient Gson GSON = new GsonBuilder()
		.registerTypeAdapter(Location.class, new LocationJsonAdapter())
		.create();
	private static final Joiner EMPTY_JOINER = Joiner.on("");

	@Getter
	@NonNull
	private final List<Location> pumpkinLocations = new LocationList();

	public JsonPumpkinConfiguration(File file) {
		super(file);
	}

	@Override
	public void save() {
		String json = GSON.toJson(pumpkinLocations);
		try {
			Files.write(getFile().toPath(), Collections.singletonList(json), StandardCharsets.UTF_8,
				StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void reload() {
		try {
			List<String> json = Files.readAllLines(getFile().toPath(), StandardCharsets.UTF_8);
			String jsonStr = EMPTY_JOINER.join(json);
			LocationList locations = GSON.fromJson(jsonStr, LocationList.class);
			pumpkinLocations.clear();
			pumpkinLocations.addAll(locations);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void copyFrom(PumpkinConfiguration pumpkinConfiguration) {
		pumpkinLocations.clear();
		pumpkinLocations.addAll(pumpkinConfiguration.getPumpkinLocations());
	}
}
