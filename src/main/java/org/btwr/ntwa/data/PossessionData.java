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

    public PossessionData(int timer, int level) {
        this.timer = timer;
        this.level = level;
    }

    public static final Codec<PossessionData> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.INT.fieldOf("timer").forGetter(PossessionData::getTimer),
                    Codec.INT.fieldOf("level").forGetter(PossessionData::getLevel)
            ).apply(instance, PossessionData::new)
    );

    public static PacketCodec<ByteBuf, PossessionData> PACKET_CODEC = PacketCodecs.codec(CODEC);

    public boolean isPossessed() {
        return level > 0;
    }

    public boolean isFullyPossessed() {
        return level > 1;
    }

    public int getTimer() {
        return timer;
    }

    public int getLevel() {
        return level;
    }

    public void setTimer(int value) {
        this.timer = value;
    }

    public void setLevel(int value) {
        this.level = value;
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

                PossessionManager.onFullPossession(entity, this);
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
