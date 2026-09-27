package co.bracesoftware.erosion.world;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;
import com.mojang.serialization.Codec;
import co.bracesoftware.erosion.Erosion;
import co.bracesoftware.erosion.ErosionConfig;
import co.bracesoftware.erosion.ErosionUtils;
import co.bracesoftware.erosion.data.ErosionDataGenerators;
import co.bracesoftware.erosion.data.ErosionDataGeneratorsProgInterface;
import co.bracesoftware.erosion.data.clientgen.ErosionBlockStateGen;
import co.bracesoftware.erosion.data.clientgen.ErosionItemModelGen;
import co.bracesoftware.erosion.data.clientgen.ErosionLang;
import co.bracesoftware.erosion.data.clientgen.ErosionSoundGen;
import co.bracesoftware.erosion.data.servergen.ErosionAdvGen;
import co.bracesoftware.erosion.data.servergen.ErosionBlockTagGen;
import co.bracesoftware.erosion.data.servergen.ErosionItemTagGen;
import co.bracesoftware.erosion.data.servergen.ErosionLootGen;
import co.bracesoftware.erosion.data.servergen.ErosionLootGen.ErosionLootGenSubProvider;
import co.bracesoftware.erosion.data.servergen.ErosionRecipeGen;
import co.bracesoftware.erosion.network.server.ErosionNetworkSafeVariants.ErosionNetworkSafeBlockEntity;
import co.bracesoftware.erosion.world.items.ErosionSimpleItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.BlockEntityType.BlockEntitySupplier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ErosionModContentManager
{
    public static final class ErosionModContentResourceLocation
    {
        private String id = null;
        private String name = null;

        public ErosionModContentResourceLocation(String id, String name)
        {
            this.id = id;
            this.name = name;
        }

        public String getId()
        {
            return this.id;
        }
        public String getName()
        {
            return this.name;
        }
    }
    public static final DeferredRegister<MenuType<?>> EROSION_MOD_MENUS = DeferredRegister.create(Registries.MENU, Erosion.MODID);
    public static final DeferredRegister.Blocks EROSION_MOD_BLOCKS = DeferredRegister.createBlocks(Erosion.MODID);
    public static final DeferredRegister.Items EROSION_MOD_ITEMS = DeferredRegister.createItems(Erosion.MODID);
    public static final DeferredRegister<BlockEntityType<?>> EROSION_MOD_BLOCK_ENTITY_TYPES = DeferredRegister.create(
        Registries.BLOCK_ENTITY_TYPE, Erosion.MODID
    );
    public static final DeferredRegister<CreativeModeTab> EROSION_MOD_CREATIVE_MODE_TABS = DeferredRegister.create(
        Registries.CREATIVE_MODE_TAB, Erosion.MODID
    );
    public static final DeferredRegister<ArmorMaterial> EROSION_MOD_ARMOR_MATERIALS = DeferredRegister.create(
        Registries.ARMOR_MATERIAL, Erosion.MODID
    );
    public static final DeferredRegister<SoundEvent> EROSION_MOD_SOUND_EVENTS = DeferredRegister.create(
        Registries.SOUND_EVENT, Erosion.MODID
    );
    public static final DeferredRegister.DataComponents EROSION_MOD_DATA_COMPONENTS = DeferredRegister.createDataComponents(
        Registries.DATA_COMPONENT_TYPE, Erosion.MODID
    );
    public static final DeferredRegister<RecipeSerializer<?>> EROSION_MOD_SERIALIZERS = DeferredRegister.create(
        Registries.RECIPE_SERIALIZER, Erosion.MODID
    );

    protected static int EROSION_MENU_COUNT = 0;
    protected static int EROSION_BLOCK_COUNT = 0;
    protected static int EROSION_ITEM_COUNT = 0;
    protected static int EROSION_BLOCK_ENTITY_COUNT = 0;
    protected static int EROSION_SOUND_COUNT = 0;
    protected static int EROSION_DATA_COMPONENT_COUNT = 0;
    protected static int EROSION_SERIALIZER_COUNT = 0;

    //DATAGEN
    public static final List<Runnable> EROSION_BLOCK_STATE_GEN_TASKS = new ArrayList<>();
    public static final List<Runnable> EROSION_LANG_GEN_TASKS = new ArrayList<>();
    public static final List<Runnable> EROSION_ITEM_MODEL_GEN_TASKS = new ArrayList<>();
    public static final List<Runnable> EROSION_SOUND_GEN_TASKS = new ArrayList<>();
    public static final List<Runnable> EROSION_LOOT_GEN_TASKS = new ArrayList<>();
    public static final List<Runnable> EROSION_RECIPE_GEN_TASKS = new ArrayList<>();
    public static final List<Runnable> EROSION_ADVANCEMENT_GEN_TASKS = new ArrayList<>();
    public static final List<Runnable> EROSION_BLOCK_TAG_GEN_TASKS = new ArrayList<>();
    public static final List<Runnable> EROSION_ITEM_TAG_GEN_TASKS = new ArrayList<>();

    public static final List<ErosionModContent.ErosionBlock> EROSION_KNOWN_BLOCKS = new ArrayList<>();
    public static final List<ErosionModContent.ErosionItem> EROSION_CREATIVE_TAB_ITEMS = new ArrayList<>();

    public static final ErosionBlockStateGen getBlockStateResourceGenerator()
    {
        return ErosionDataGenerators.EROSION_BLOCK_STATE_GENERATOR;
    }
    // --------------------------------------------------------------------------- //
    public static final ErosionLang getLanguageResourceGenerator()
    {
        return ErosionDataGenerators.EROSION_LANG_GENERATOR;
    }
    // --------------------------------------------------------------------------- //
    public static final ErosionItemModelGen getItemModelResourceGenerator()
    {
        return ErosionDataGenerators.EROSION_ITEM_MODEL_GENERATOR;
    }
    // --------------------------------------------------------------------------- //
    public static final ErosionSoundGen getSoundResourceGenerator()
    {
        return ErosionDataGenerators.EROSION_SOUND_GENERATOR;
    }
    // --------------------------------------------------------------------------- //
    public static final ErosionLootGen getLootResourceGenerator()
    {
        return ErosionDataGenerators.EROSION_LOOT_GENERATOR;
    }
    // --------------------------------------------------------------------------- //
    public static final ErosionRecipeGen getRecipeResourceGenerator()
    {
        return ErosionDataGenerators.EROSION_RECIPE_GENERATOR;
    }
    // --------------------------------------------------------------------------- //
    public static final ErosionAdvGen getAdvancementResourceGenerator()
    {
        return ErosionDataGenerators.EROSION_ADVANCEMENT_GENERATOR;
    }
    // --------------------------------------------------------------------------- //
    public static final ErosionBlockTagGen getBlockTagResourceGenerator()
    {
        return ErosionDataGenerators.EROSION_BLOCK_TAG_GENERATOR;
    }
    // --------------------------------------------------------------------------- //
    public static final ErosionItemTagGen getItemTagResourceGenerator()
    {
        return ErosionDataGenerators.EROSION_ITEM_TAG_GENERATOR;
    }

    //SPECIFIC
    public static final HolderLookup.Provider getBlockTagResourceGeneratorProvider()
    {
        return ErosionBlockTagGen.gProvider;
    }
    public static final HolderLookup.Provider getItemTagResourceGeneratorProvider()
    {
        return ErosionItemTagGen.provajda;
    }
    public static final RecipeOutput getRecipeResourceGeneratorOutput()
    {
        return ErosionRecipeGen.resourceOutput;
    }
    public static final ErosionLootGenSubProvider getLootResourceGeneratorSubProvider()
    {
        return ErosionLootGen.ErosionLootGenSubProvider.subProvider;
    }

    public static final class ErosionModContentBuilders
    {
        public static final class OreBlockPairBuilder
        {
            private Supplier<? extends Block> oreBlockClassSupplier;
            private Supplier<? extends Block> rockBlockClassSupplier;
            private ErosionModContentResourceLocation oreId;
            private ErosionModContentResourceLocation rockId;
            private ErosionModContent.OreItemPair from;
            
            public OreBlockPairBuilder() {}

            public static OreBlockPairBuilder make()
            {
                return new OreBlockPairBuilder();
            }

            public OreBlockPairBuilder makeOreBlock(final Supplier<? extends Block> s)
            {
                this.oreBlockClassSupplier = s;
                return this;
            }

            public OreBlockPairBuilder makeRockBlock(final Supplier<? extends Block> s)
            {
                this.rockBlockClassSupplier = s;
                return this;
            }

            public OreBlockPairBuilder setOreResourceLocation(final ErosionModContentResourceLocation text)
            {
                this.oreId = text;
                return this;
            }

            public OreBlockPairBuilder setRockResourceLocation(final ErosionModContentResourceLocation text)
            {
                this.rockId = text;
                return this;
            }

            public OreBlockPairBuilder buildFromOreItemPair(ErosionModContent.OreItemPair s)
            {
                this.from = s;
                return this;
            }

            public ErosionModContent.OreBlockPair build()
            {
                var oreBlock = new ErosionModContent.ErosionBlock(
                    this.oreId, this.oreBlockClassSupplier
                );
                var rockBlock = new ErosionModContent.ErosionBlock(
                    this.rockId, this.rockBlockClassSupplier
                );

                oreBlock
                .blockStateResourceGenerator(() -> {
                    ErosionDataGeneratorsProgInterface.ErosionBlockState.generateRandomRotations(
                        ErosionModContentManager.getBlockStateResourceGenerator(),
                        oreBlock.get()
                    );
                })
                .languageResourceGenerator(() -> {
                    ErosionModContentManager.getLanguageResourceGenerator()
                    .add(
                        oreBlock.get(),
                        this.oreId.getName()
                    );
                })
                .blockTagResourceGenerator(() -> {
                    ErosionDataGeneratorsProgInterface.ErosionTags.Blocks.createSimpleGravel(
                        ErosionModContentManager.getBlockTagResourceGenerator(),
                        ErosionModContentManager.getBlockTagResourceGeneratorProvider(),
                        oreBlock.get()
                    );
                })
                .lootResourceGenerator(() -> {
                    ErosionModContentManager.getLootResourceGeneratorSubProvider()
                    .add(oreBlock.get(),
                        b -> ErosionModContentManager.getLootResourceGeneratorSubProvider().createOreDrop(
                            b, this.from.getDrop().get()
                        )
                    );
                })
                .addKnownBlock(oreBlock)
                .dummy();

                rockBlock
                .lootResourceGenerator(() -> {
                    ErosionModContentManager.getLootResourceGeneratorSubProvider()
                    .dropSelf(rockBlock.get());
                })
                .blockStateResourceGenerator(() -> {
                    ErosionDataGeneratorsProgInterface.ErosionBlockState.generateRockWithRandomRotations(
                        ErosionModContentManager.getBlockStateResourceGenerator(),
                        this.from.getDrop().get(),
                        rockBlock.get(),
                        ErosionDataGeneratorsProgInterface.ErosionBlockState.createRockModel(
                            ErosionModContentManager.getBlockStateResourceGenerator(),
                            this.rockId.getId(), //item for texture
                            this.oreId.getId() //block texture
                        )
                    );
                })
                .blockTagResourceGenerator(() -> {
                    ErosionDataGeneratorsProgInterface.ErosionTags.Blocks.createSimpleRock(
                        ErosionModContentManager.getBlockTagResourceGenerator(),
                        ErosionModContentManager.getBlockTagResourceGeneratorProvider(),
                        oreBlock.get()
                    );
                })
                .addKnownBlock(rockBlock)
                .dummy();

                return new ErosionModContent.OreBlockPair(oreBlock, rockBlock);
            }
        }
        public static final class OreItemPairBuilder
        {
            private ErosionModContent.OreBlockPair source;
            private ErosionModContentResourceLocation oreId;
            private ErosionModContentResourceLocation rockId;

            public OreItemPairBuilder() {}

            public static OreItemPairBuilder make()
            {
                return new OreItemPairBuilder();
            }

            public OreItemPairBuilder setOreResourceLocation(final ErosionModContentResourceLocation text)
            {
                this.oreId = text;
                return this;
            }

            public OreItemPairBuilder setRockResourceLocation(final ErosionModContentResourceLocation text)
            {
                this.rockId = text;
                return this;
            }

            public OreItemPairBuilder buildFromOreBlockPair(final ErosionModContent.OreBlockPair b)
            {
                this.source = b;
                return this;
            }

            public ErosionModContent.OreItemPair build()
            {
                var oreBlockItem = new ErosionModContent.ErosionItem(
                    this.oreId, () -> new BlockItem(
                        this.source.getSource().get(), new Item.Properties()
                    )
                );
                var rockBlockItem = new ErosionModContent.ErosionItem(
                    this.rockId, () -> new ErosionSimpleItems.ErosionRockBlockItem(
                        this.source.getDrop().get()
                    )
                );

                rockBlockItem
                .itemModelResourceGenerator(() -> {
                    ErosionModContentManager.getItemModelResourceGenerator()
                    .basicItem(rockBlockItem.get());
                })
                .languageResourceGenerator(() -> {
                    ErosionModContentManager.getLanguageResourceGenerator()
                    .add(
                        rockBlockItem.get(),
                        this.rockId.getName()
                    );
                })
                .itemTagResourceGenerator(() -> {
                    ErosionDataGeneratorsProgInterface.ErosionTags.Items.createSimpleRawOre(
                        ErosionModContentManager.getItemTagResourceGenerator(),
                        ErosionModContentManager.getItemTagResourceGeneratorProvider(),
                        rockBlockItem.get()
                    );
                })
                .dummy();

                return new ErosionModContent.OreItemPair(oreBlockItem, rockBlockItem);
            }
        }
    }

    //MAIN CLASS
    public static class ErosionModContent<T> implements Supplier<T>
    {
        //wrappers around the wrappers
        public static final class OreBlockPair
        {
            private final ErosionBlock source;
            private final ErosionBlock drop;

            public OreBlockPair(ErosionBlock s, ErosionBlock d)
            {
                this.source = s;
                this.drop = d;
            }

            public final ErosionBlock getSource() { return this.source; }
            public final ErosionBlock getDrop() { return this.drop; }
        }
        public static final class OreItemPair
        {
            private final ErosionItem source;
            private final ErosionItem drop;

            public OreItemPair(ErosionItem s, ErosionItem d)
            {
                this.source = s;
                this.drop = d;
            }

            public final ErosionItem getSource() { return this.source; }
            public final ErosionItem getDrop() { return this.drop; }
        }

        //internal stuff
        protected DeferredBlock<Block> blockHolder;
        protected DeferredItem<Item> itemHolder;
        protected Supplier<BlockEntityType<? extends ErosionNetworkSafeBlockEntity<?>>> blockEntityHolder;
        protected Supplier<SoundEvent> soundHolder;
        protected DeferredHolder<MenuType<?>, MenuType<? extends AbstractContainerMenu>> menuHolder;
        protected Object serializerHolder;
        protected DeferredHolder<DataComponentType<?>, DataComponentType<Boolean>> booleanDataComponentHolder;

        public Runnable blockStateGenerator;
        public Runnable languageGenerator;
        public Runnable soundGenerator;
        public Runnable itemModelGenerator;
        public Runnable lootGenerator;
        public Runnable recipeGenerator;
        public Runnable advGenerator;
        public Runnable itemTaggen;
        public Runnable blockTaggen;

        @Override public T get()
        {
            return null;
        }

        public final ErosionModContent<T> dummy()
        {
            return this;
        }

        public final ErosionModContent<T> blockStateResourceGenerator(Runnable r)
        {
            this.blockStateGenerator = r;
            EROSION_BLOCK_STATE_GEN_TASKS.add(this.blockStateGenerator);
            return this;
        }
        public final ErosionModContent<T> languageResourceGenerator(Runnable r)
        {
            this.languageGenerator = r;
            EROSION_LANG_GEN_TASKS.add(this.languageGenerator);
            return this;
        }
        public final ErosionModContent<T> itemModelResourceGenerator(Runnable r)
        {
            this.itemModelGenerator = r;
            EROSION_ITEM_MODEL_GEN_TASKS.add(this.itemModelGenerator);
            return this;
        }
        public final ErosionModContent<T> soundResourceGenerator(Runnable r)
        {
            this.soundGenerator = r;
            EROSION_SOUND_GEN_TASKS.add(this.soundGenerator);
            return this;
        }
        public final ErosionModContent<T> advancementResourceGenerator(Runnable r)
        {
            this.advGenerator = r;
            EROSION_ADVANCEMENT_GEN_TASKS.add(this.advGenerator);
            return this;
        }
        public final ErosionModContent<T> recipeResourceGenerator(Runnable r)
        {
            this.recipeGenerator = r;
            EROSION_RECIPE_GEN_TASKS.add(this.recipeGenerator);
            return this;
        }
        public final ErosionModContent<T> lootResourceGenerator(Runnable r)
        {
            this.lootGenerator = r;
            EROSION_LOOT_GEN_TASKS.add(this.lootGenerator);
            return this;
        }
        public final ErosionModContent<T> blockTagResourceGenerator(Runnable r)
        {
            this.blockTaggen = r;
            EROSION_BLOCK_TAG_GEN_TASKS.add(this.blockTaggen);
            return this;
        }
        public final ErosionModContent<T> itemTagResourceGenerator(Runnable r)
        {
            this.itemTaggen = r;
            EROSION_ITEM_TAG_GEN_TASKS.add(this.itemTaggen);
            return this;
        }

        public final ErosionModContent<T> addKnownBlock(ErosionBlock e)
        {
            EROSION_KNOWN_BLOCKS.add(e);
            return this;
        }

        public static final class ErosionBooleanDataComponent extends ErosionModContent<DataComponentType<Boolean>>
        {
            public ErosionBooleanDataComponent(ErosionModContentResourceLocation loc)
            {
                ErosionUtils.Log("Setting up boolean data component -> " + loc.getName());
                this.booleanDataComponentHolder = EROSION_MOD_DATA_COMPONENTS.registerComponentType(
                    loc.getId(),
                    ComponentBuilder -> ComponentBuilder
                    .persistent(Codec.BOOL) 
                    .networkSynchronized(ByteBufCodecs.BOOL)
                );
                EROSION_DATA_COMPONENT_COUNT++;
            }

            @Override public DataComponentType<Boolean> get()
            {
                return this.booleanDataComponentHolder.get();
            }
        }

        public static final class ErosionBlock extends ErosionModContent<Block>
        {
            public ErosionBlock(ErosionModContentResourceLocation id, Supplier<? extends Block> s)
            {
                ErosionUtils.Log("Setting up block -> " + id.getName());
                this.blockHolder = EROSION_MOD_BLOCKS.register(id.getId(), s);
                EROSION_BLOCK_COUNT++;
            }

            @Override public Block get()
            {
                return this.blockHolder.get();
            }
        }
        public static final class ErosionItem extends ErosionModContent<Item>
        {
            public ErosionItem(ErosionModContentResourceLocation id, Supplier<? extends Item> s)
            {
                ErosionUtils.Log("Setting up item -> " + id.getName());
                this.itemHolder = EROSION_MOD_ITEMS.register(id.getId(), s);
                EROSION_ITEM_COUNT++;
                EROSION_CREATIVE_TAB_ITEMS.add(this);
            }
            
            @Override public Item get()
            {
                return this.itemHolder.get();
            }
        }

        public static final class ErosionBlockEntity<T> extends ErosionModContent<BlockEntityType<?>>
        {
            public ErosionBlockEntity(
                ErosionModContentResourceLocation loc,
                BlockEntitySupplier<? extends ErosionNetworkSafeBlockEntity<T>> s,
                ErosionBlock... b
            )
            {
                ErosionUtils.Log("Setting up block entity -> " + loc.getName());
                this.blockEntityHolder = EROSION_MOD_BLOCK_ENTITY_TYPES.register(
                    loc.getId(), () -> BlockEntityType.Builder.of(
                        s, Arrays.stream(b)
                        .map(ErosionBlock::get)
                        .toArray(Block[]::new)
                    ).build(null)
                );
                EROSION_BLOCK_ENTITY_COUNT++;
            }

            @Override public BlockEntityType<?> get()
            {
                return this.blockEntityHolder.get();
            }
        }
        public static final class ErosionSound extends ErosionModContent<SoundEvent>
        {
            public ErosionSound(
                ErosionModContentResourceLocation loc,
                Supplier<SoundEvent> s
            )
            {
                ErosionUtils.Log("Setting up sound -> " + loc.getName());
                this.soundHolder = EROSION_MOD_SOUND_EVENTS.register(
                    loc.getId(), s
                );
                EROSION_SOUND_COUNT++;
            }

            @Override public SoundEvent get()
            {
                return this.soundHolder.get();
            }
        }

        public static final class ErosionMenu<G extends AbstractContainerMenu> extends ErosionModContent<MenuType<?>>
        {
            protected DeferredHolder<MenuType<?>, MenuType<G>> menuHolderSpecific;
            public ErosionMenu(
                ErosionModContentResourceLocation loc,
                Supplier<? extends MenuType<G>> s
            )
            {
                ErosionUtils.Log("Setting up menu -> " + loc.getName());
                this.menuHolderSpecific = EROSION_MOD_MENUS.register(
                    loc.getId(), s
                );
                EROSION_MENU_COUNT++;
            }

            @Override public MenuType<G> get()
            {
                return this.menuHolderSpecific.get();
            }
        }

        public static final class ErosionSerializer<G extends CustomRecipe> extends ErosionModContent<SimpleCraftingRecipeSerializer<?>>
        {
            protected DeferredHolder<
                RecipeSerializer<?>, SimpleCraftingRecipeSerializer<G>
            > serializerHolderSpecific;
            public ErosionSerializer(
                ErosionModContentResourceLocation loc,
                Supplier<? extends SimpleCraftingRecipeSerializer<G>> s
            )
            {
                ErosionUtils.Log("Setting up serializer -> " + loc.getName());
                this.serializerHolderSpecific = EROSION_MOD_SERIALIZERS.register(
                    loc.getId(), s
                );
                EROSION_SERIALIZER_COUNT++;
            }

            @Override public SimpleCraftingRecipeSerializer<G> get()
            {
                return this.serializerHolderSpecific.get();
            }
        }

        public final DeferredItem<Item> getItemHolder()
        {
            return this.itemHolder;
        }

        public final DeferredBlock<Block> getBlockHolder()
        {
            return this.blockHolder;
        }

        public final Supplier<BlockEntityType<? extends ErosionNetworkSafeBlockEntity<?>>> getBlockEntityHolder()
        {
            return this.blockEntityHolder;
        }

        public final Supplier<SoundEvent> getSoundHolder()
        {
            return this.soundHolder;
        }

        public final DeferredHolder<MenuType<?>, MenuType<?>> getMenuHolder()
        {
            return this.menuHolder;
        }

        public final Object getSerializerHolder()
        {
            return this.serializerHolder;
        }
    }

    public static final void loadClasses(List<Class<?>> l)
    {
        for(var c : l)
        {
            try
            {
                Class.forName(c.getName(), true, c.getClassLoader());
            }
            catch(Exception e)
            {
                ErosionUtils.Log("Could not load class -> " + c.getName());
                e.printStackTrace();
            }
        }
    }

    public static final void loadClasses(Class<?>... l)
    {
        loadClasses(List.of(l));
    }

    public static final void registerContent(IEventBus b, Runnable what)
    {
        ErosionModContentManager.EROSION_MOD_ARMOR_MATERIALS.register(b);
        ErosionModContentManager.EROSION_MOD_BLOCKS.register(b);
        ErosionModContentManager.EROSION_MOD_ITEMS.register(b);
        ErosionModContentManager.EROSION_MOD_CREATIVE_MODE_TABS.register(b);
        ErosionModContentManager.EROSION_MOD_BLOCK_ENTITY_TYPES.register(b);
        ErosionModContentManager.EROSION_MOD_MENUS.register(b);
        ErosionModContentManager.EROSION_MOD_SOUND_EVENTS.register(b);
        ErosionModContentManager.EROSION_MOD_SERIALIZERS.register(b);
        ErosionModContentManager.EROSION_MOD_DATA_COMPONENTS.register(b);

        ErosionUtils.Log(
            String.format(
                "Sucessfully loaded following content: %d item(s), %d block(s), %d block entities, %d menu(s), %d sound(s), %d data component(s), %d serializer(s)",
                EROSION_ITEM_COUNT,
                EROSION_BLOCK_COUNT,
                EROSION_BLOCK_ENTITY_COUNT,
                EROSION_MENU_COUNT,
                EROSION_SOUND_COUNT,
                EROSION_DATA_COMPONENT_COUNT,
                EROSION_SERIALIZER_COUNT
            )
        );

        what.run();
    }

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> EROSION_TAB = ErosionModContentManager.
    EROSION_MOD_CREATIVE_MODE_TABS.register(
        ErosionConfig.CREATIVE_TAB_NAME, () -> CreativeModeTab.builder()
        .title(Component.translatable(ErosionConfig.CREATIVE_TAB_ID))
        .icon(() -> ErosionRegistry.Items.KAOLINIZED_GRANITE.getItemHolder().get().getDefaultInstance())
        .displayItems((p, o) -> {
            for(var i : EROSION_CREATIVE_TAB_ITEMS)
            {
                o.accept(i.get());
            }
            //ALL ITEMS GO HERE
            //SIMPLE BLOCKS
            /* 
            output.accept(ErosionRegistry.Items.DRIED_DIRT.getItemHolder().get());

            output.accept(ErosionRegistry.Items.KAOLINIZED_GRANITE.getItemHolder().get());
            output.accept(ErosionRegistry.Items.CRACKED_STONE.getItemHolder().get());
            output.accept(ErosionRegistry.Items.ALBITIZED_GRANITE.getItemHolder().get());
            output.accept(ErosionRegistry.Items.QUARTZ_GRAVEL.getItemHolder().get());
            output.accept(ErosionRegistry.Items.PROPYLITIZED_DIORITE.getItemHolder().get());
            output.accept(ErosionRegistry.Items.CRACKED_CALCITE.getItemHolder().get());

            output.accept(ErosionRegistry.Items.LIMONITE_ORE.getItemHolder().get());
            output.accept(ErosionRegistry.Items.CALCITE_MALACHITE_ORE.getItemHolder().get());
            output.accept(ErosionRegistry.Items.HEMATITE_ORE.getItemHolder().get());
            output.accept(ErosionRegistry.Items.BORAX_DEPOSIT.getItemHolder().get());
            output.accept(ErosionRegistry.Items.MAGNETITE_ORE.getItemHolder().get());

            output.accept(ErosionRegistry.Items.RAW_HEMATITE.getItemHolder().get());
            output.accept(ErosionRegistry.Items.RAW_LIMONITE.getItemHolder().get());
            output.accept(ErosionRegistry.Items.RAW_MAGNETITE.getItemHolder().get());
            output.accept(ErosionRegistry.Items.RAW_MALACHITE.getItemHolder().get());

            output.accept(ErosionRegistry.Items.NATIVE_GOLD_DEPOSIT.getItemHolder().get());
            output.accept(ErosionRegistry.Items.NATIVE_GOLD.getItemHolder().get());
            output.accept(ErosionRegistry.Items.RAW_CASSITERITE.getItemHolder().get());
            output.accept(ErosionRegistry.Items.TIN_CHUNK.getItemHolder().get());
            output.accept(ErosionRegistry.Items.LEAD_CHUNK.getItemHolder().get());
            output.accept(ErosionRegistry.Items.CASSITERITE_DEPOSIT.getItemHolder().get());

            output.accept(ErosionRegistry.Items.NATIVE_SILVER.getItemHolder().get());
            output.accept(ErosionRegistry.Items.NATIVE_SILVER_DEPOSIT.getItemHolder().get());
            output.accept(ErosionRegistry.Items.SILVER_CHUNK.getItemHolder().get());

            output.accept(ErosionRegistry.Items.BISMUTH_CHUNK.getItemHolder().get());
            output.accept(ErosionRegistry.Items.BISMUTHINITE_ORE.getItemHolder().get());
            output.accept(ErosionRegistry.Items.RAW_BISMUTHINITE.getItemHolder().get());

            output.accept(ErosionRegistry.Items.ZINC_CHUNK.getItemHolder().get());
            output.accept(ErosionRegistry.Items.SPHALERITE_ORE.getItemHolder().get());
            output.accept(ErosionRegistry.Items.RAW_SPHALERITE.getItemHolder().get());
            output.accept(ErosionRegistry.Items.MINERAL_RICH_DIRT.getItemHolder().get());

            output.accept(ErosionRegistry.Items.RAW_AZURITE.getItemHolder().get());
            output.accept(ErosionRegistry.Items.RAW_GOETHITE.getItemHolder().get());
            output.accept(ErosionRegistry.Items.AZURITE_ORE.getItemHolder().get());
            output.accept(ErosionRegistry.Items.GOETHITE_ORE.getItemHolder().get());
            output.accept(ErosionRegistry.Items.TETRAHEDRITE_ORE.getItemHolder().get());
            output.accept(ErosionRegistry.Items.ARSENOPYRITE_ORE.getItemHolder().get());
            output.accept(ErosionRegistry.Items.PYRITE_ORE.getItemHolder().get());
            output.accept(ErosionRegistry.Items.ANGLESITE_ORE.getItemHolder().get());
            output.accept(ErosionRegistry.Items.HALITE_ORE.getItemHolder().get());
            output.accept(ErosionRegistry.Items.GALENA_ORE.getItemHolder().get());
            output.accept(ErosionRegistry.Items.RAW_TETRAHEDRITE.getItemHolder().get());
            output.accept(ErosionRegistry.Items.RAW_ARSENOPYRITE.getItemHolder().get());
            output.accept(ErosionRegistry.Items.RAW_PYRITE.getItemHolder().get());
            output.accept(ErosionRegistry.Items.RAW_ANGLESITE.getItemHolder().get());
            output.accept(ErosionRegistry.Items.RAW_GALENA.getItemHolder().get());
            output.accept(ErosionRegistry.Items.RAW_HALITE.getItemHolder().get());
            output.accept(ErosionRegistry.Items.RUBY_ORE.getItemHolder().get());
            output.accept(ErosionRegistry.Items.SAPPHIRE_ORE.getItemHolder().get());

            //SIMPLE ITEMS
            output.accept(ErosionRegistry.Items.FLUX.getItemHolder().get());
            output.accept(ErosionRegistry.Items.SALT.getItemHolder().get());
            output.accept(ErosionRegistry.Items.RUBY.getItemHolder().get());
            output.accept(ErosionRegistry.Items.BORAX.getItemHolder().get());
            output.accept(ErosionRegistry.Items.DEHYDRATED_BORAX.get());
            output.accept(ErosionRegistry.Items.BUCKET_OF_SULFURIC_ACID.get());
            output.accept(ErosionRegistry.Items.BORIC_ACID_CRYSTAL.get());
            output.accept(ErosionRegistry.Items.SAPPHIRE.get());
            output.accept(ErosionRegistry.Items.SULFUR_SLAG.get());
            output.accept(ErosionRegistry.Items.ANTIMONY_SLAG.get());
            output.accept(ErosionRegistry.Items.DEBRIS.get());
            output.accept(ErosionRegistry.Items.CRUSHED_EGG_SHELL.get());
            output.accept(ErosionRegistry.Items.FELDSPAR_POWDER.get());

            //MACHINES
            output.accept(ErosionRegistry.Items.MATERIAL_PURIFIER.getItemHolder().get());
            output.accept(ErosionRegistry.Items.CRUCIBLE.getItemHolder().get());
            output.accept(ErosionRegistry.Items.CHEMICAL_REACTOR.getItemHolder().get());
            output.accept(ErosionRegistry.Items.CHEMICAL_REACTOR_SCRUBBER.getItemHolder().get());
            output.accept(ErosionRegistry.Items.CHEMICAL_REACTOR_MODULE.getItemHolder().get());
            output.accept(ErosionRegistry.Items.CHEMICAL_REACTOR_COOLING_SYSTEM.getItemHolder().get());
            output.accept(ErosionRegistry.Items.BASIC_MASK.getItemHolder().get());
            output.accept(ErosionRegistry.Items.GAS_MASK.getItemHolder().get());
            output.accept(ErosionRegistry.Items.GAS_FILTER.getItemHolder().get());
            */
        })
        .build()
    );
}