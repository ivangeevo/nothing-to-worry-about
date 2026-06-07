package org.btwr.ntwa;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import org.btwr.ntwa.data.ModDataAttachments;
import org.btwr.ntwa.entity.possession.PossessionManager;
import org.btwr.ntwa.entity.possession.PossessionSource;
import org.btwr.ntwa.packet.WolfAttemptingPayload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NTWAMod implements ModInitializer {

    public static final String MOD_ID = "ntwa";

    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        ModDataAttachments.register();
        PossessionManager.registerBehaviors();

        PayloadTypeRegistry.playS2C().register(WolfAttemptingPayload.ID, WolfAttemptingPayload.CODEC);

        ServerLivingEntityEvents.AFTER_DEATH.register((entity, damageSource) -> {
            var data = entity.getAttached(ModDataAttachments.POSSESSABLE);

            if (data != null && data.isPossessed()) {
                PossessionManager.spreadPossession(entity, 16.0, new PossessionSource.EntityDeath(entity));
            }
        });

    }
}
