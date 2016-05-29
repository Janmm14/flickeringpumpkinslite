package de.janmm14.flickeringpumpkinslite.pumpkinconfig;

import java.io.File;
import java.util.Set;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

import org.bukkit.Location;

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
