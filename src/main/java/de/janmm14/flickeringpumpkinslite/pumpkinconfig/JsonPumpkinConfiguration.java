package de.janmm14.flickeringpumpkinslite.pumpkinconfig;

import com.google.common.base.Joiner;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class JsonPumpkinConfiguration extends PumpkinConfiguration { //TODO support not loaded worlds

	private static final transient Gson GSON = new GsonBuilder()
		.registerTypeAdapter(World.class, new WorldJsonAdapter())
		.create();
	private static final Joiner EMPTY_JOINER = Joiner.on("\n");

	@Getter
	@NotNull
	private final Set<Location> pumpkinLocations = new HashSet<>();

	public JsonPumpkinConfiguration(File file) {
		super(file);
	}

	@Override
	public void save() {
		if (true) { //saving json should not be neccessary
			return;
		}
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
			System.out.println(jsonStr);
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
