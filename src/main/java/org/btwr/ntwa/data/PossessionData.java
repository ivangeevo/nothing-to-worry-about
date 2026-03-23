package org.btwr.ntwa.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.LivingEntity;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import org.btwr.ntwa.entity.possession.PossessionManager;
import org.btwr.ntwa.entity.possession.PossessionSource;
import org.btwr.shared_library.api.data.UpdateRequiringData;

public class PossessionData extends UpdateRequiringData<LivingEntity> {

    private int timer;
    private int level;

    // squid-specific
    private int squidLeapCountdown;
    private int squidPropulsionTicks;
    private float squidGhastRoll;
    private double squidLaunchX;
    private double squidLaunchZ;

    public PossessionData(
            int timer,
            int level,
            int squidLeapCountdown,
            int squidPropulsionTicks,
            float squidGhastRoll,
            double squidLaunchX,
            double squidLaunchZ
    ){
        this.timer = timer;
        this.level = level;
        this.squidLeapCountdown = squidLeapCountdown;
        this.squidPropulsionTicks = squidPropulsionTicks;
        this.squidGhastRoll = squidGhastRoll;
        this.squidLaunchX = squidLaunchX;
        this.squidLaunchZ = squidLaunchZ;
    }

    // default constructor
    public PossessionData() {
        this(-1, 0, 0, 0, 1f, 0, 0);
    }

    public static final Codec<PossessionData> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.INT.fieldOf("timer").forGetter(PossessionData::getTimer),
                    Codec.INT.fieldOf("level").forGetter(PossessionData::getLevel),
                    Codec.INT.fieldOf("squidLeapCountdown").forGetter(PossessionData::getSquidLeapCountdown),
                    Codec.INT.fieldOf("squidPropulsionTicks").forGetter(PossessionData::getSquidPropulsionTicks),
                    Codec.FLOAT.fieldOf("squidGhastRoll").forGetter(PossessionData::getSquidGhastRoll),
                    Codec.DOUBLE.fieldOf("squidLaunchX").forGetter(PossessionData::getSquidLaunchX),
                    Codec.DOUBLE.fieldOf("squidLaunchZ").forGetter(PossessionData::getSquidLaunchZ)
            ).apply(instance, PossessionData::new)
    );

    public static PacketCodec<ByteBuf, PossessionData> PACKET_CODEC = PacketCodecs.codec(CODEC);

    public boolean isPossessed() {
        return level > 0;
    }

    public boolean isFullyPossessed() {
        return level > 1;
    }

    // --- BASE GETTERS ---
    public int getTimer() { return timer; }
    public int getLevel() { return level; }

    // --- BASE SETTERS ---
    public void setTimer(int value) { this.timer = value; }
    public void setLevel(int value) { this.level = value; }

    // --- SQUID GETTERS ---
    public int getSquidLeapCountdown() { return squidLeapCountdown; }
    public int getSquidPropulsionTicks() { return squidPropulsionTicks; }
    public float getSquidGhastRoll() { return squidGhastRoll; }
    public double getSquidLaunchX() { return squidLaunchX; }
    public double getSquidLaunchZ() { return squidLaunchZ; }

    // --- SQUID SETTERS ---
    public void setSquidLeapCountdown(int v) { this.squidLeapCountdown = v; }
    public void setSquidPropulsionTicks(int v) { this.squidPropulsionTicks = v; }
    public void setSquidGhastRoll(float v) { this.squidGhastRoll = v; }
    public void setSquidLaunchX(double v) { this.squidLaunchX = v; }
    public void setSquidLaunchZ(double v) { this. squidLaunchZ = v; }

    public void resetSquidState() {
        squidLeapCountdown = 0;
        squidPropulsionTicks = 0;
        squidGhastRoll = 1f;
    }

    @Override
    public void tick(LivingEntity entity) {
        if (entity.getWorld().isClient()) return;

        // start possession if not yet possessed
        if (!isPossessed()) {
            PossessionSource<?> source = PossessionManager.determineSource(entity);
            if (source != null) {
                PossessionManager.initiatePossession(entity, this);
                markDirty();
            }
            return;
        }

        // babies don't progress
        if (entity.isBaby()) return;

        // progress to full possession
        if (level == 1) {
            timer--;

            if (timer < 0) timer = 0;

            if (timer == 0) {
                level = 2;

                PossessionManager.onFullPossession(entity);
                markDirty();
            }
        }

        // tick the entity-specific possessed behavior
        PossessionManager.tickPossessed(entity, this);
    }

    private int getInitialChance(LivingEntity entity) {
        return 1000;
    }

    public int getTimeToFullPossession(LivingEntity entity) {
        return 2400 + entity.getRandom().nextInt(2400);
    }

}
