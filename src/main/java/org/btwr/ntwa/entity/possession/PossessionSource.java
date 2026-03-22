package org.btwr.ntwa.entity.possession;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.WolfEntity;

import java.util.Optional;

public abstract class PossessionSource<T> {
    private final PossessionType type;
    private final Optional<T> data;

    protected PossessionSource(PossessionType type, T data) {
        this.type = type;
        this.data = Optional.of(data);
    }

    protected PossessionSource(PossessionType type) {
        this.type = type;
        this.data = Optional.empty();
    }

    public PossessionType type() { return type; }
    public Optional<T> data() { return data; }

    public static class NetherExposure extends PossessionSource<Void> {
        public NetherExposure() { super(PossessionType.NETHER_EXPOSURE); }
    }

    public static class NetherPortal extends PossessionSource<Void> {
        public NetherPortal() { super(PossessionType.NETHER_PORTAL); }
    }

    public static class EntityDeath extends PossessionSource<LivingEntity> {
        public EntityDeath(LivingEntity entity) { super(PossessionType.ENTITY_DEATH, entity); }
    }

    public static class Wolf extends PossessionSource<WolfEntity> {
        public Wolf(WolfEntity wolf) { super(PossessionType.WOLF, wolf); }
    }

    public static class SoulUrn extends PossessionSource<Void> {
        public SoulUrn() { super(PossessionType.SOUL_URN); }
    }

    public static class HopperFiltering extends PossessionSource<Void> {
        public HopperFiltering() { super(PossessionType.HOPPER_FILTERING); }
    }

    public static class HopperExplosion extends PossessionSource<Void> {
        public HopperExplosion() { super(PossessionType.HOPPER_EXPLOSION); }
    }
}
