package de.janmm14.flickeringpumpkinslite;

import com.google.gson.InstanceCreator;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.io.IOException;
import java.lang.reflect.Type;

public class LocationJsonAdapter extends TypeAdapter<Location> implements InstanceCreator<Location>, JsonSerializer<Location>, JsonDeserializer<Location> {

	private static final Location UNNEEDED_LOCATION = new Location(Bukkit.getWorlds().get(0), 0, 0, 0);

	@Override
	public Location createInstance(Type type) {
		return UNNEEDED_LOCATION;
	}

	@Override
	public Location deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext ctx) throws JsonParseException {
		JsonObject json = jsonElement.getAsJsonObject();
		String worldName = json.get("world").getAsString();
		World world = Bukkit.getWorld(worldName);
		if (world == null) {
			return null;
		}
		int x = json.get("x").getAsInt();
		int y = json.get("y").getAsInt();
		int z = json.get("z").getAsInt();
		return new Location(world, x, y, z);
	}

	@Override
	public JsonElement serialize(Location src, Type type, JsonSerializationContext ctx) {
		ctx.serialize(src.getWorld().getName(), String.class);
		ctx.serialize(src.getBlockX(), double.class);
		ctx.serialize(src.getBlockY(), double.class);
		ctx.serialize(src.getBlockZ(), double.class);
		ctx.serialize(0, float.class);
		ctx.serialize(0, float.class);
		return null;
	}

	@Override
	public void write(JsonWriter out, Location value) throws IOException {
		//@formatter:off
		out.beginObject()
				.name("world").value(value.getWorld().toString())
				.name("x").value(value.getBlockX())
				.name("y").value(value.getBlockY())
				.name("z").value(value.getBlockZ())
				.name("pitch").value(0)
				.name("yaw").value(0)
			.endObject();
		//@formatter:on
	}

	@Override
	public Location read(JsonReader in) throws IOException {
		in.beginObject();
		String worldName = "";
		int x = 0, y = 0, z = 0;
		while (in.hasNext()) {
			if (in.peek() == JsonToken.END_OBJECT) {
				break;
			}
			String prop = in.nextName().toLowerCase().trim();
			switch (prop) {
				case "world": {
					worldName = in.nextString();
					break;
				}
				case "x": {
					x = in.nextInt();
					break;
				}
				case "y": {
					y = in.nextInt();
					break;
				}
				case "z": {
					z = in.nextInt();
					break;
				}
				default: {
					in.skipValue();
				}
			}
		}
		World world = Bukkit.getWorld(worldName);
		if (world == null) {
			return null;
		}
		return new Location(world, x, y, z);
	}
}
