package org.btwr.ntwa.entity.possession.behavior;

import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.WolfEntity;
import net.minecraft.sound.SoundEvents;
import org.btwr.ntwa.data.PossessionData;
import org.btwr.ntwa.entity.possession.PossessionManager;
import org.btwr.ntwa.entity.possession.PossessionSource;
import org.btwr.ntwa.packet.WolfAttemptingPayload;

public class WolfPossessionBehavior {

    public static final int ATTEMPT_DURATION = 200; // 10s
    private static final int ATTEMPT_CHANCE = 12000; // ~10min average

    public static void tick(LivingEntity entity, PossessionData data) {
        if (!(entity instanceof WolfEntity wolf)) return;
        if (!data.isFullyPossessed()) return;

        var wolfData = data.getOrCreate(PossessionData.WolfData.class, PossessionData.WolfData::new);

        // START of attempt — send packet once, then begin countdown
        if (!wolfData.attempting && wolf.getRandom().nextInt(ATTEMPT_CHANCE) < 1) {
            wolfData.attempting = true;
            wolfData.attemptCountdown = ATTEMPT_DURATION;
            wolfData.didHeadSpin = false;
            System.out.println("NTWA SERVER: attempt started for wolf " + wolf.getId());

            // Send ONCE when attempt begins
            PlayerLookup.tracking(wolf).forEach(player -> {
                System.out.println("NTWA SERVER: sending packet to " + player.getName().getString());
                ServerPlayNetworking.send(player, new WolfAttemptingPayload(wolf.getId(), true));
            }
            );
        }

        // Active attempt
        if (wolfData.attempting) {
            // first tick -> play sound -> start spin
            if (!wolfData.didHeadSpin) {
                wolfData.didHeadSpin = true;

                wolf.playSound(
                        SoundEvents.BLOCK_PORTAL_AMBIENT,
                        3.0F,
                        0.75F + wolf.getRandom().nextFloat() * 0.1F
                );
            }

            // countdown
            wolfData.attemptCountdown--;
            if (wolfData.attemptCountdown <= 0) {
                wolfData.attempting = false;

                PossessionManager.spreadPossession(wolf, 16, new PossessionSource.Wolf(wolf));
            }
        }
    }

    public static void tickClient(WolfEntity wolf, PossessionData data) {
        var wolfData = data.getOrCreate(PossessionData.WolfData.class, PossessionData.WolfData::new);

        if (wolfData.attempting) {
            wolfData.headRotation += 0.3f;

            wolfData.attemptCountdown--;
            if (wolfData.attemptCountdown <= 0) {
                wolfData.attempting = false;
            }
        }
    }

}