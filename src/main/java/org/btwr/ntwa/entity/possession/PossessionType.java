package org.btwr.ntwa.entity.possession;


import net.minecraft.util.Identifier;
import org.btwr.ntwa.NTWAMod;

public record PossessionType(Identifier id) {
	public static final PossessionType ENTITY_DEATH = new PossessionType(Identifier.of(NTWAMod.MOD_ID, "entity_death"));
	public static final PossessionType NETHER_EXPOSURE = new PossessionType(Identifier.of(NTWAMod.MOD_ID, "nether_exposure"));
	public static final PossessionType NETHER_PORTAL = new PossessionType(Identifier.of(NTWAMod.MOD_ID, "nether_portal"));
	public static final PossessionType WOLF = new PossessionType(Identifier.of(NTWAMod.MOD_ID, "wolf"));
	public static final PossessionType SOUL_URN = new PossessionType(Identifier.of(NTWAMod.MOD_ID, "soul_urn"));
	public static final PossessionType HOPPER_FILTERING = new PossessionType(Identifier.of(NTWAMod.MOD_ID, "hopper_filtering"));
	public static final PossessionType HOPPER_EXPLOSION = new PossessionType(Identifier.of(NTWAMod.MOD_ID, "hopper_explosion"));
}
