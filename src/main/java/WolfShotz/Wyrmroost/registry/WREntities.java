package WolfShotz.Wyrmroost.registry;

import WolfShotz.Wyrmroost.Wyrmroost;
import WolfShotz.Wyrmroost.client.ClientEvents;
import WolfShotz.Wyrmroost.client.render.entity.butterfly.ButterflyLeviathanRenderer;
import WolfShotz.Wyrmroost.client.render.entity.canari.CanariWyvernRenderer;
import WolfShotz.Wyrmroost.client.render.entity.dragon_egg.DragonEggRenderer;
import WolfShotz.Wyrmroost.client.render.entity.dragon_fruit.DragonFruitDrakeRenderer;
import WolfShotz.Wyrmroost.client.render.entity.ldwyrm.LDWyrmRenderer;
import WolfShotz.Wyrmroost.client.render.entity.owdrake.OWDrakeRenderer;
import WolfShotz.Wyrmroost.client.render.entity.projectile.BreathWeaponRenderer;
import WolfShotz.Wyrmroost.client.render.entity.projectile.GeodeTippedArrowRenderer;
import WolfShotz.Wyrmroost.client.render.entity.rooststalker.RoostStalkerRenderer;
import WolfShotz.Wyrmroost.client.render.entity.royal_red.RoyalRedRenderer;
import WolfShotz.Wyrmroost.client.render.entity.silverglider.SilverGliderRenderer;
import WolfShotz.Wyrmroost.entities.dragon.*;
import WolfShotz.Wyrmroost.entities.dragonegg.DragonEggEntity;
import WolfShotz.Wyrmroost.entities.projectile.GeodeTippedArrowEntity;
import WolfShotz.Wyrmroost.entities.projectile.breath.FireBreathEntity;
import WolfShotz.Wyrmroost.items.LazySpawnEggItem;
import WolfShotz.Wyrmroost.util.ModUtils;
import net.minecraft.entity.*;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.Heightmap;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.fml.DeferredWorkQueue;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.client.registry.IRenderFactory;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static net.minecraftforge.common.BiomeDictionary.Type;

/**
 * Created by WolfShotz - 7/3/19 19:03 <p>
 * <p>
 * Class responsible for the setup and registration of entities, and their spawning.
 * Entity type generics are defined because a) forge told me so and b) because its broken without it.
 */
public class WREntities
{
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITIES, Wyrmroost.MOD_ID);

    public static final RegistryObject<EntityType<LDWyrmEntity>> LESSER_DESERTWYRM = Builder.creature("lesser_desertwyrm", LDWyrmEntity::new)
            .spawnEgg(0xD6BCBC, 0xDEB6C7)
            .renderer(LDWyrmRenderer::new)
            .spawnPlacement(t -> LDWyrmEntity.setSpawnPlacements())
            .build(b -> b.size(0.6f, 0.2f));

    public static final RegistryObject<EntityType<OWDrakeEntity>> OVERWORLD_DRAKE = Builder.creature("overworld_drake", OWDrakeEntity::new)
            .spawnEgg(0x788716, 0x3E623E)
            .renderer(OWDrakeRenderer::new)
            .spawnPlacement(t -> basicSpawnConditions(t, 8, 1, 3, ModUtils.getBiomesByTypes(Type.SAVANNA, Type.PLAINS)))
            .build(b -> b.size(2.376f, 2.58f));

    public static final RegistryObject<EntityType<SilverGliderEntity>> SILVER_GLIDER = Builder.creature("silver_glider", SilverGliderEntity::new)
            .spawnEgg(0xC8C8C8, 0xC4C4C4)
            .renderer(SilverGliderRenderer::new)
            .spawnPlacement(t -> SilverGliderEntity.setSpawnPlacements())
            .build(b -> b.size(1.5f, 0.75f));

    public static final RegistryObject<EntityType<RoostStalkerEntity>> ROOSTSTALKER = Builder.creature("roost_stalker", RoostStalkerEntity::new)
            .spawnEgg(0x52100D, 0x959595)
            .renderer(RoostStalkerRenderer::new)
            .spawnPlacement(t -> basicSpawnConditions(t, 7, 2, 9, ModUtils.getBiomesByTypes(Type.FOREST, Type.PLAINS, Type.MOUNTAIN)))
            .build(b -> b.size(0.65f, 0.5f));

    public static final RegistryObject<EntityType<ButterflyLeviathanEntity>> BUTTERFLY_LEVIATHAN = Builder.withClassification("butterfly_leviathan", ButterflyLeviathanEntity::new, EntityClassification.WATER_CREATURE)
            .spawnEgg(0x17283C, 0x7A6F5A)
            .renderer(ButterflyLeviathanRenderer::new)
            .spawnPlacement(t -> ButterflyLeviathanEntity.setSpawnConditions())
            .build(b -> b.size(4f, 3f));

    public static final RegistryObject<EntityType<DragonFruitDrakeEntity>> DRAGON_FRUIT_DRAKE = Builder.creature("dragon_fruit_drake", DragonFruitDrakeEntity::new)
            .spawnEgg(0xe05c9a, 0x788716)
            .renderer(DragonFruitDrakeRenderer::new)
            .spawnPlacement(t -> DragonFruitDrakeEntity.setSpawnConditions())
            .build(b -> b.size(1.5f, 1.9f));

    public static final RegistryObject<EntityType<CanariWyvernEntity>> CANARI_WYVERN = Builder.creature("canari_wyvern", CanariWyvernEntity::new)
            .spawnEgg(0x1D1F28, 0x492E0E)
            .renderer(CanariWyvernRenderer::new)
            .spawnPlacement(t -> basicSpawnConditions(t, 9, 2, 5, BiomeDictionary.getBiomes(Type.SWAMP)))
            .build(b -> b.size(0.7f, 0.85f));

    public static final RegistryObject<EntityType<RoyalRedEntity>> ROYAL_RED = Builder.creature("royal_red", RoyalRedEntity::new)
            .spawnEgg(0x8a0900, 0x0)
            .renderer(RoyalRedRenderer::new)
            .build(b -> b.size(3f, 3.9f).immuneToFire());

    public static final RegistryObject<EntityType<GeodeTippedArrowEntity>> BLUE_GEODE_ARROW = Builder.<GeodeTippedArrowEntity>withClassification("blue_geode_tipped_arrow", (t, w) -> new GeodeTippedArrowEntity(t, 3d, WRItems.BLUE_GEODE_ARROW.get(), w), EntityClassification.MISC)
            .renderer(manager -> new GeodeTippedArrowRenderer(manager, Wyrmroost.rl("textures/entity/projectiles/blue_geode_tipped_arrow.png")))
            .build(b -> b.size(0.5f, 0.5f));
    public static final RegistryObject<EntityType<GeodeTippedArrowEntity>> RED_GEODE_ARROW = Builder.<GeodeTippedArrowEntity>withClassification("red_geode_tipped_arrow", (t, w) -> new GeodeTippedArrowEntity(t, 3.5d, WRItems.RED_GEODE_ARROW.get(), w), EntityClassification.MISC)
            .renderer(manager -> new GeodeTippedArrowRenderer(manager, Wyrmroost.rl("textures/entity/projectiles/red_geode_tipped_arrow.png")))
            .build(b -> b.size(0.5f, 0.5f));
    public static final RegistryObject<EntityType<GeodeTippedArrowEntity>> PURPLE_GEODE_ARROW = Builder.<GeodeTippedArrowEntity>withClassification("purple_geode_tipped_arrow", (t, w) -> new GeodeTippedArrowEntity(t, 4d, WRItems.PURPLE_GEODE_ARROW.get(), w), EntityClassification.MISC)
            .renderer(manager -> new GeodeTippedArrowRenderer(manager, Wyrmroost.rl("textures/entity/projectiles/purple_geode_tipped_arrow.png")))
            .build(b -> b.size(0.5f, 0.5f));

    public static final RegistryObject<EntityType<FireBreathEntity>> FIRE_BREATH = Builder.<FireBreathEntity>withClassification("fire_breath", FireBreathEntity::new, EntityClassification.MISC)
            .renderer(BreathWeaponRenderer::new)
            .build(b -> b.size(0.75f, 0.75f).disableSerialization().disableSummoning());

    public static final RegistryObject<EntityType<DragonEggEntity>> DRAGON_EGG = Builder.<DragonEggEntity>withClassification("dragon_egg", DragonEggEntity::new, EntityClassification.MISC)
            .renderer(DragonEggRenderer::new)
            .build(EntityType.Builder::disableSummoning);

//    public static final RegistryObject<EntityType<MultiPartEntity>> MULTIPART = register("multipart_entity", MultiPartEntity::new, EntityClassification.MISC, b -> b.disableSummoning().disableSerialization().setShouldReceiveVelocityUpdates(false));

    private static <T extends MobEntity> void basicSpawnConditions(EntityType<T> entity, int frequency, int minAmount, int maxAmount, Set<Biome> biomes)
    {
        biomes.forEach(b -> b.getSpawns(entity.getClassification()).add(new Biome.SpawnListEntry(entity, frequency, minAmount, maxAmount)));
        EntitySpawnPlacementRegistry.register(entity,
                EntitySpawnPlacementRegistry.PlacementType.ON_GROUND,
                Heightmap.Type.MOTION_BLOCKING_NO_LEAVES,
                MobEntity::canSpawnOn);
    }

    // todo in 1.16: Attributes
    private static class Builder<T extends Entity>
    {
        private final String name;
        private final EntityType.IFactory<T> factory;
        private final EntityClassification classification;
        private Consumer<Supplier<EntityType<T>>> builderCallback = i ->
        {};

        public Builder(String name, EntityType.IFactory<T> factory, EntityClassification classification)
        {
            this.name = name;
            this.factory = factory;
            this.classification = classification;
        }

        private Builder<T> spawnEgg(int primColor, int secColor)
        {
            this.builderCallback = builderCallback.andThen(t -> WRItems.register(name + "_egg", () -> new LazySpawnEggItem(t::get, primColor, secColor)));
            return this;
        }

        private Builder<T> renderer(IRenderFactory<T> renderFactory)
        {
            this.builderCallback = builderCallback.andThen(t -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientEvents.CALL_BACKS.add(() -> RenderingRegistry.registerEntityRenderingHandler(t.get(), renderFactory))));
            return this;
        }

        /**
         * Just a consumer, tho it is ran at FMLCommonSetupEvent,
         * so it is advisable to take advantage by using this for spawning logic
         */
        private Builder<T> spawnPlacement(Consumer<EntityType<T>> consumer)
        {
            this.builderCallback = builderCallback.andThen(t -> Wyrmroost.COMMON_CALLBACKS.add(() -> DeferredWorkQueue.runLater(() -> consumer.accept(t.get()))));
            return this;
        }

        private RegistryObject<EntityType<T>> build(Consumer<EntityType.Builder<T>> consumer)
        {
            EntityType.Builder<T> builder = EntityType.Builder.create(factory, classification);
            consumer.accept(builder);

            RegistryObject<EntityType<T>> object = ENTITIES.register(name, () -> builder.build(Wyrmroost.MOD_ID + ":" + name));
            if (builderCallback != null) builderCallback.accept(object);

            return object;
        }

        private static <T extends Entity> Builder<T> creature(String name, EntityType.IFactory<T> factory)
        {
            return new Builder<>(name, factory, EntityClassification.CREATURE);
        }

        private static <T extends Entity> Builder<T> withClassification(String name, EntityType.IFactory<T> factory, EntityClassification classification)
        {
            return new Builder<>(name, factory, classification);
        }
    }
}
