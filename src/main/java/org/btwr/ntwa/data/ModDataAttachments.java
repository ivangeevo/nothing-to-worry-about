package org.btwr.ntwa.data;

import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentSyncPredicate;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;
import org.btwr.ntwa.NTWAMod;
import org.btwr.ntwa.tag.ModTags;
import org.btwr.shared_library.api.data.EntityAttachmentBase;
import org.btwr.shared_library.api.event.BTWREvents;

public class ModDataAttachments {

    public static final AttachmentType<PossessionData> POSSESSABLE = AttachmentRegistry.create(
            Identifier.of(NTWAMod.MOD_ID, "possessable"),
            builder -> builder
                    .initializer(() -> new PossessionData(-1, 0))
                    .persistent(PossessionData.CODEC)
                    .syncWith(PossessionData.PACKET_CODEC, AttachmentSyncPredicate.all())
    );

    public static void register() {
        NTWAMod.LOGGER.info("Registering {} attachments", NTWAMod.MOD_ID);

        BTWREvents.LIVING_TICK.add(living -> {
            if (living.getType().isIn(ModTags.EntityTypes.POSSESSABLE)) {
                tickAndSync(POSSESSABLE, living);
            }
        });
    }

    private static <T extends Entity, A extends EntityAttachmentBase<T>> void tickAndSync(AttachmentType<A> type, LivingEntity entity) {
        A attachment = entity.getAttachedOrCreate(type);
        attachment.tick((T) entity);
        if (attachment.isDirty()) {
            entity.setAttached(type, attachment);
        }
    }

}