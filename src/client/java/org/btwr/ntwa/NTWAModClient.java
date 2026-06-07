package org.btwr.ntwa;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.entity.passive.WolfEntity;
import net.minecraft.util.math.Box;
import org.btwr.ntwa.data.ModDataAttachments;
import org.btwr.ntwa.data.PossessionData;
import org.btwr.ntwa.entity.possession.behavior.WolfPossessionBehavior;
import org.btwr.ntwa.packet.WolfAttemptingPayload;

public class NTWAModClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        // Receive server signal to start the attempt
        ClientPlayNetworking.registerGlobalReceiver(WolfAttemptingPayload.ID, (payload, context) -> {
            context.client().execute(() -> {
                if (context.client().world == null) return;
                if (!(context.client().world.getEntityById(payload.entityId()) instanceof WolfEntity wolf)) return;

                var data = wolf.getAttached(ModDataAttachments.POSSESSABLE);
                if (data == null) return;

                var wolfData = data.getOrCreate(PossessionData.WolfData.class, PossessionData.WolfData::new);
                wolfData.attempting = true;
                wolfData.attemptCountdown = WolfPossessionBehavior.ATTEMPT_DURATION;
                wolfData.didHeadSpin = false;
                System.out.println("NTWA CLIENT: received packet, set attempting=true for wolf " + wolf.getId());
            });
        });

        // Drive the visual every tick
        ClientTickEvents.END_WORLD_TICK.register(world -> {
            Box searchBox = new Box(-1000, -1000, -1000, 1000, 1000, 1000);

            for (WolfEntity entity : world.getEntitiesByClass(WolfEntity.class, searchBox, e -> true)) {
                var data = entity.getAttached(ModDataAttachments.POSSESSABLE);
                if (data != null) {
                    WolfPossessionBehavior.tickClient(entity, data);
                }
            }
        });

    }

}