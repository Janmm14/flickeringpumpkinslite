package de.janmm14.flickeringpumpkinslite.pumpkinconfig;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.List;

@RequiredArgsConstructor
public abstract class PumpkinConfiguration {

	@Getter
	@NotNull
	private final File file;

	@NotNull
	public abstract List<Location> getPumpkinLocations();

	public abstract void save();

	public abstract void reload();

	public abstract void copyFrom(PumpkinConfiguration pumpkinConfiguration);
}
