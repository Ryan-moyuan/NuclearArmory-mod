package com.nucleararmory.init;

import com.nucleararmory.NuclearArmory;
import com.nucleararmory.items.GunProjectile;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(
        ForgeRegistries.ENTITY_TYPES, NuclearArmory.MOD_ID);

    public static final RegistryObject<EntityType<GunProjectile>> BULLET =
        ENTITY_TYPES.register("bullet", () -> EntityType.Builder.<GunProjectile>of(
            (type, level) -> new GunProjectile(type, level), MobCategory.MISC)
            .sized(0.25f, 0.25f)
            .clientTrackingRange(4)
            .updateInterval(20)
            .build("bullet"));

    public static void register(IEventBus modEventBus) {
        ENTITY_TYPES.register(modEventBus);
    }
}
