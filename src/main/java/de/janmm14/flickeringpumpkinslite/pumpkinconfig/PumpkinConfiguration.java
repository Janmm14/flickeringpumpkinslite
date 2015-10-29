package de.janmm14.flickeringpumpkinslite.pumpkinconfig;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Set;

@RequiredArgsConstructor
public abstract class PumpkinConfiguration {

	@Getter
	@NotNull
	private final File file;

	@NotNull
	public abstract Set<Location> getPumpkinLocations();

	public abstract void save();

	public abstract void reload();

	public abstract void copyFrom(PumpkinConfiguration pumpkinConfiguration);
}
