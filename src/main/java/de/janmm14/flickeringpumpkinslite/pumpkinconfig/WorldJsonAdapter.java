package de.janmm14.flickeringpumpkinslite.pumpkinconfig;

import java.io.IOException;

import org.bukkit.Bukkit;
import org.bukkit.World;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

public class WorldJsonAdapter extends TypeAdapter<World> {

	@Override
	public void write(JsonWriter out, World value) throws IOException {
		out.value(value.getName());
	}

	@Override
	public World read(JsonReader in) throws IOException {
		return Bukkit.getWorld(in.nextString());
	}
}
