package de.janmm14.flickeringpumpkinslite.darkblade12.particlelibrary;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

/**
 * Represents a particle effect packet with all attributes which is used for sending packets to the players
 * <p>
 * This class is part of the <b>ParticleEffect Library</b> and follows the same usage conditions
 *
 * @author DarkBlade12
 * @since 1.5
 */
public final class ParticlePacket {
	private static int version;
	private static Class<?> enumParticle;
	private static Constructor<?> packetConstructor;
	private static Method getHandle;
	private static Field playerConnection;
	private static Method sendPacket;
	private static boolean initialized;
	private final ParticleEffect effect;
	private float offsetX;
	private final float offsetY;
	private final float offsetZ;
	private final float speed;
	private final int amount;
	private final boolean longDistance;
	private final ParticleEffect.ParticleData data;
	private Object packet;

	/**
	 * Construct a new particle packet
	 *
	 * @param effect Particle effect
	 * @param offsetX Maximum distance particles can fly away from the center on the x-axis
	 * @param offsetY Maximum distance particles can fly away from the center on the y-axis
	 * @param offsetZ Maximum distance particles can fly away from the center on the z-axis
	 * @param speed Display speed of the particles
	 * @param amount Amount of particles
	 * @param longDistance Indicates whether the maximum distance is increased from 256 to 65536
	 * @param data Data of the effect
	 * @throws IllegalArgumentException If the speed or amount is lower than 0
	 * @see #initialize()
	 */
	public ParticlePacket(ParticleEffect effect, float offsetX, float offsetY, float offsetZ, float speed, int amount, boolean longDistance, ParticleEffect.ParticleData data) throws IllegalArgumentException {
		initialize();
		if (speed < 0) {
			throw new IllegalArgumentException("The speed is lower than 0");
		}
		if (amount < 0) {
			throw new IllegalArgumentException("The amount is lower than 0");
		}
		this.effect = effect;
		this.offsetX = offsetX;
		this.offsetY = offsetY;
		this.offsetZ = offsetZ;
		this.speed = speed;
		this.amount = amount;
		this.longDistance = longDistance;
		this.data = data;
	}

	/**
	 * Construct a new particle packet of a single particle flying into a determined direction
	 *
	 * @param effect Particle effect
	 * @param direction Direction of the particle
	 * @param speed Display speed of the particle
	 * @param longDistance Indicates whether the maximum distance is increased from 256 to 65536
	 * @param data Data of the effect
	 * @throws IllegalArgumentException If the speed is lower than 0
	 * @see #ParticlePacket(ParticleEffect, float, float, float, float, int, boolean, ParticleEffect.ParticleData)
	 */
	public ParticlePacket(ParticleEffect effect, Vector direction, float speed, boolean longDistance, ParticleEffect.ParticleData data) throws IllegalArgumentException {
		this(effect, (float) direction.getX(), (float) direction.getY(), (float) direction.getZ(), speed, 0, longDistance, data);
	}

	/**
	 * Construct a new particle packet of a single colored particle
	 *
	 * @param effect Particle effect
	 * @param color Color of the particle
	 * @param longDistance Indicates whether the maximum distance is increased from 256 to 65536
	 * @see #ParticlePacket(ParticleEffect, float, float, float, float, int, boolean, ParticleEffect.ParticleData)
	 */
	public ParticlePacket(ParticleEffect effect, ParticleEffect.ParticleColor color, boolean longDistance) {
		this(effect, color.getValueX(), color.getValueY(), color.getValueZ(), 1, 0, longDistance, null);
		if (effect == ParticleEffect.REDSTONE && color instanceof ParticleEffect.OrdinaryColor && ((ParticleEffect.OrdinaryColor) color).getRed() == 0) {
			offsetX = (float) 1 / 255F;
		}
	}

	/**
	 * Initializes {@link #packetConstructor}, {@link #getHandle}, {@link #playerConnection} and {@link #sendPacket} and sets {@link #initialized} to <code>true</code> if it succeeds
	 * <p>
	 * <b>Note:</b> These fields only have to be initialized once, so it will return if {@link #initialized} is already set to <code>true</code>
	 *
	 * @throws VersionIncompatibleException if your bukkit version is not supported by this library
	 */
	public static void initialize() throws VersionIncompatibleException {
		if (initialized) {
			return;
		}
		try {
			version = ReflectionUtils.PackageType.getServerVersionSecondInt();
			if (version > 7) {
				enumParticle = ReflectionUtils.PackageType.MINECRAFT_SERVER.getClass("EnumParticle");
			}
			Class<?> packetClass = ReflectionUtils.PackageType.MINECRAFT_SERVER.getClass(version < 7 ? "Packet63WorldParticles" : "PacketPlayOutWorldParticles");
			packetConstructor = ReflectionUtils.getConstructor(packetClass);
			getHandle = ReflectionUtils.getMethod("CraftPlayer", ReflectionUtils.PackageType.CRAFTBUKKIT_ENTITY, "getHandle");
			playerConnection = ReflectionUtils.getField("EntityPlayer", ReflectionUtils.PackageType.MINECRAFT_SERVER, false, "playerConnection");
			sendPacket = ReflectionUtils.getMethod(playerConnection.getType(), "sendPacket", ReflectionUtils.PackageType.MINECRAFT_SERVER.getClass("Packet"));
		} catch (Exception exception) {
			throw new VersionIncompatibleException("Your current bukkit version seems to be incompatible with this library", exception);
		}
		initialized = true;
	}

	/**
	 * Returns the version of your server (1.x)
	 *
	 * @return The version number
	 */
	public static int getVersion() {
		if (!initialized) {
			initialize();
		}
		return version;
	}

	/**
	 * Determine if {@link #packetConstructor}, {@link #getHandle}, {@link #playerConnection} and {@link #sendPacket} are initialized
	 *
	 * @return Whether these fields are initialized or not
	 * @see #initialize()
	 */
	public static boolean isInitialized() {
		return initialized;
	}

	/**
	 * Initializes {@link #packet} with all set values
	 *
	 * @param center Center location of the effect
	 * @throws PacketInstantiationException If instantion fails due to an unknown error
	 */
	private void initializePacket(Location center) throws PacketInstantiationException {
		if (packet != null) {
			return;
		}
		try {
			packet = packetConstructor.newInstance();
			if (version < 8) {
				String name = effect.getName();
				if (data != null) {
					name += data.getPacketDataString();
				}
				ReflectionUtils.setValue(packet, true, "a", name);
			} else {
				ReflectionUtils.setValue(packet, true, "a", enumParticle.getEnumConstants()[effect.getId()]);
				ReflectionUtils.setValue(packet, true, "j", longDistance);
				if (data != null) {
					int[] packetData = data.getPacketData();
					ReflectionUtils.setValue(packet, true, "k", effect == ParticleEffect.ITEM_CRACK ? packetData : new int[]{packetData[0] | (packetData[1] << 12)});
				}
			}
			ReflectionUtils.setValue(packet, true, "b", (float) center.getX());
			ReflectionUtils.setValue(packet, true, "c", (float) center.getY());
			ReflectionUtils.setValue(packet, true, "d", (float) center.getZ());
			ReflectionUtils.setValue(packet, true, "e", offsetX);
			ReflectionUtils.setValue(packet, true, "f", offsetY);
			ReflectionUtils.setValue(packet, true, "g", offsetZ);
			ReflectionUtils.setValue(packet, true, "h", speed);
			ReflectionUtils.setValue(packet, true, "i", amount);
		} catch (Exception exception) {
			throw new PacketInstantiationException("Packet instantiation failed", exception);
		}
	}

	/**
	 * Sends the packet to a single player and caches it
	 *
	 * @param center Center location of the effect
	 * @param player Receiver of the packet
	 * @throws PacketInstantiationException If instantion fails due to an unknown error
	 * @throws PacketSendingException If sending fails due to an unknown error
	 * @see #initializePacket(Location)
	 */
	public void sendTo(Location center, Player player) throws PacketInstantiationException, PacketSendingException {
		initializePacket(center);
		try {
			sendPacket.invoke(playerConnection.get(getHandle.invoke(player)), packet);
		} catch (Exception exception) {
			throw new PacketSendingException("Failed to send the packet to player '" + player.getName() + "'", exception);
		}
	}

	/**
	 * Sends the packet to all players in the list
	 *
	 * @param center Center location of the effect
	 * @param players Receivers of the packet
	 * @throws IllegalArgumentException If the player list is empty
	 * @see #sendTo(Location center, Player player)
	 */
	public void sendTo(Location center, List<Player> players) throws IllegalArgumentException {
		if (players.isEmpty()) {
			throw new IllegalArgumentException("The player list is empty");
		}
		for (Player player : players) {
			sendTo(center, player);
		}
	}

	/**
	 * Sends the packet to all players in a certain range
	 *
	 * @param center Center location of the effect
	 * @param range Range in which players will receive the packet (Maximum range for particles is usually 16, but it can differ for some types)
	 * @throws IllegalArgumentException If the range is lower than 1
	 * @see #sendTo(Location center, Player player)
	 */
	public void sendTo(Location center, double range) throws IllegalArgumentException {
		if (range < 1) {
			throw new IllegalArgumentException("The range is lower than 1");
		}
		String worldName = center.getWorld().getName();
		double squared = range * range;
		for (Player player : Bukkit.getOnlinePlayers()) {
			if (!player.getWorld().getName().equals(worldName) || player.getLocation().distanceSquared(center) > squared) {
				continue;
			}
			sendTo(center, player);
		}
	}

	/**
	 * Represents a runtime exception that is thrown if a bukkit version is not compatible with this library
	 * <p>
	 * This class is part of the <b>ParticleEffect Library</b> and follows the same usage conditions
	 *
	 * @author DarkBlade12
	 * @since 1.5
	 */
	private static final class VersionIncompatibleException extends RuntimeException {
		private static final long serialVersionUID = 3203085387160737484L;

		/**
		 * Construct a new version incompatible exception
		 *
		 * @param message Message that will be logged
		 * @param cause Cause of the exception
		 */
		public VersionIncompatibleException(String message, Throwable cause) {
			super(message, cause);
		}
	}

	/**
	 * Represents a runtime exception that is thrown if packet instantiation fails
	 * <p>
	 * This class is part of the <b>ParticleEffect Library</b> and follows the same usage conditions
	 *
	 * @author DarkBlade12
	 * @since 1.4
	 */
	private static final class PacketInstantiationException extends RuntimeException {
		private static final long serialVersionUID = 3203085387160737484L;

		/**
		 * Construct a new packet instantiation exception
		 *
		 * @param message Message that will be logged
		 * @param cause Cause of the exception
		 */
		public PacketInstantiationException(String message, Throwable cause) {
			super(message, cause);
		}
	}

	/**
	 * Represents a runtime exception that is thrown if packet sending fails
	 * <p>
	 * This class is part of the <b>ParticleEffect Library</b> and follows the same usage conditions
	 *
	 * @author DarkBlade12
	 * @since 1.4
	 */
	private static final class PacketSendingException extends RuntimeException {
		private static final long serialVersionUID = 3203085387160737484L;

		/**
		 * Construct a new packet sending exception
		 *
		 * @param message Message that will be logged
		 * @param cause Cause of the exception
		 */
		public PacketSendingException(String message, Throwable cause) {
			super(message, cause);
		}
	}
}
