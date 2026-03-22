package org.btwr.ntwa.tag;

import net.minecraft.entity.EntityType;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import org.btwr.ntwa.NTWAMod;

public class ModTags {

    public static class EntityTypes {
        public static final TagKey<EntityType<?>> POSSESSABLE = register("possessable");

        private static TagKey<EntityType<?>> register(String name) {
            return TagKey.of(RegistryKeys.ENTITY_TYPE, Identifier.of(NTWAMod.MOD_ID, name));
        }
    }
}
