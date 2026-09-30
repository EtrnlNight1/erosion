package co.bracesoftware.erosion.data;

import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.imageio.ImageIO;
import co.bracesoftware.erosion.Erosion;
import co.bracesoftware.erosion.ErosionConfig;
import co.bracesoftware.erosion.ErosionExceptions.ErosionDataGenException;
import co.bracesoftware.erosion.ErosionModCompat;
import co.bracesoftware.erosion.ErosionUtils;
import co.bracesoftware.erosion.world.ErosionRegistry;
import co.bracesoftware.erosion.world.blocks.ErosionSimpleBlocks;
import co.bracesoftware.erosion.world.blocks.ErosionSimpleBlocks.RockBlock;
import co.bracesoftware.erosion.data.clientgen.ErosionBlockStateGen;
import co.bracesoftware.erosion.data.commongen.ErosionTextureGen;
import co.bracesoftware.erosion.data.servergen.ErosionAdvGen;
import co.bracesoftware.erosion.data.servergen.ErosionBlockTagGen;
import co.bracesoftware.erosion.data.servergen.ErosionItemTagGen;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementType;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.critereon.ImpossibleTrigger;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.client.model.generators.BlockModelBuilder;
import net.neoforged.neoforge.client.model.generators.ConfiguredModel;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.common.Tags;
import net.minecraft.data.tags.IntrinsicHolderTagsProvider;

public class ErosionDataGeneratorsProgInterface
{
    public static final String resourcePath = ErosionUtils.getResourcesFolder() + "assets/" + Erosion.MODID + "/textures/block/";
    public static final String generatedResourcesPath = ErosionUtils.getResourcesFolder() + "assets/" + Erosion.MODID + "/textures/block/" + ErosionUtils.getGeneratedFolder();
            
    public static class ErosionTags
    {
        public abstract interface ErosionTaggable<T>
        {
            public abstract IntrinsicHolderTagsProvider.IntrinsicTagAppender<T> tagz(TagKey<T> e);
        }
        public static class Items
        {
            public static void createSimpleRawOre(ErosionItemTagGen t, HolderLookup.Provider p, Item i)
            {
                t.tagz(Tags.Items.ORES).add(i);
            }

            public static void createSimplePowder(ErosionItemTagGen t, HolderLookup.Provider p, Item i)
            {
                t.tagz(Tags.Items.DUSTS).add(i);
            }
            public static void createSimpleItem(ErosionItemTagGen t, HolderLookup.Provider p, Item i)
            {
                t.tagz(ItemTags.create(ResourceLocation.fromNamespaceAndPath("c", "misc"))).add(i);
            }
            public static void createSimpleArmorPiece(ErosionItemTagGen t, HolderLookup.Provider p, Item i)
            {
                t.tagz(Tags.Items.ARMORS).add(i);
                t.tagz(ItemTags.create(ResourceLocation.fromNamespaceAndPath("c", "armors"))).add(i);
                t.tagz(ItemTags.create(ResourceLocation.fromNamespaceAndPath("c", "armor"))).add(i);
            }
        }
        public static class Blocks
        {
            public static void createSimpleStone(ErosionBlockTagGen t, HolderLookup.Provider p, Block b)
            {
                t.tagz(BlockTags.MINEABLE_WITH_PICKAXE).add(b);
                t.tagz(BlockTags.create(ResourceLocation.fromNamespaceAndPath("c", "stones"))).add(b);
                t.tagz(BlockTags.create(ResourceLocation.fromNamespaceAndPath("c", "stone"))).add(b);
                return;
            }

            public static void createSimpleOre(ErosionBlockTagGen t, HolderLookup.Provider p, Block b)
            {
                t.tagz(BlockTags.MINEABLE_WITH_PICKAXE).add(b);
                t.tagz(BlockTags.NEEDS_STONE_TOOL).add(b);
                t.tagz(BlockTags.create(ResourceLocation.fromNamespaceAndPath("c", "ores"))).add(b);
                t.tagz(BlockTags.create(ResourceLocation.fromNamespaceAndPath("c", "ore"))).add(b);
                return;
            }

            public static void createSimpleGravel(ErosionBlockTagGen t, HolderLookup.Provider p, Block b)
            {
                t.tagz(BlockTags.MINEABLE_WITH_SHOVEL).add(b);
                t.tagz(BlockTags.MINEABLE_WITH_PICKAXE).add(b);
                t.tagz(BlockTags.create(ResourceLocation.fromNamespaceAndPath("c", "gravels"))).add(b);
                t.tagz(BlockTags.create(ResourceLocation.fromNamespaceAndPath("c", "gravel"))).add(b);
                return;
            }

            public static void createSimpleRock(ErosionBlockTagGen t, HolderLookup.Provider p, Block b)
            {
                t.tagz(BlockTags.create(ResourceLocation.fromNamespaceAndPath("c", "rock"))).add(b);
                t.tagz(BlockTags.create(ResourceLocation.fromNamespaceAndPath("c", "rocks"))).add(b);
                return;
            }

            public static void createSimpleDirt(ErosionBlockTagGen t, HolderLookup.Provider p, Block b)
            {
                t.tagz(BlockTags.MINEABLE_WITH_SHOVEL).add(b);
                t.tagz(BlockTags.DIRT).add(b);
                t.tagz(BlockTags.create(ResourceLocation.fromNamespaceAndPath("c", "dirts"))).add(b);
                t.tagz(BlockTags.create(ResourceLocation.fromNamespaceAndPath("c", "dirt"))).add(b);
                return;
            }

            public static void createSimpleMachine(ErosionBlockTagGen t, HolderLookup.Provider p, Block b)
            {
                t.tagz(BlockTags.MINEABLE_WITH_PICKAXE).add(b);
                t.tagz(BlockTags.MINEABLE_WITH_AXE).add(b);

                t.tagz(BlockTags.create(ResourceLocation.fromNamespaceAndPath("c", "functional_blocks"))).add(b);
                t.tagz(BlockTags.create(ResourceLocation.fromNamespaceAndPath("c", "functional_block"))).add(b);
                t.tagz(BlockTags.create(ResourceLocation.fromNamespaceAndPath("c", "crafting_tables"))).add(b);
                t.tagz(BlockTags.create(ResourceLocation.fromNamespaceAndPath("c", "crafting_table"))).add(b);
                return;
            }
        }
    }
    public static class ErosionAdvancement
    {
        private static boolean PARENT_ADVANCEMENT_CREATED = false;

        public static AdvancementHolder generateParentAdvancement(
            ErosionAdvGen.Generator t
        ) throws ErosionDataGenException
        {
            if(PARENT_ADVANCEMENT_CREATED)
            {
                throw new ErosionDataGenException("Parent advancement is already generated!");
            }
            PARENT_ADVANCEMENT_CREATED = true;
            
            ErosionUtils.Log(
                "Generated parent advancement."
            );
            return Advancement.Builder.advancement()
            .display(
                ErosionRegistry.Items.KAOLINIZED_GRANITE.get(),
                Component.literal(Erosion.MODNAME),
                Component.literal(Erosion.SUBTITLE),
                ResourceLocation.withDefaultNamespace("textures/gui/advancements/backgrounds/stone.png"),
                AdvancementType.TASK,
                true,
                true,
                false
            )
            .addCriterion("tick", net.minecraft.advancements.critereon.PlayerTrigger.TriggerInstance.tick())
            .save(t.k, ResourceLocation.fromNamespaceAndPath(Erosion.MODID, Erosion.MODID), t.efh);
        }

        public static AdvancementHolder generateAdvancement(
            ErosionAdvGen.Generator t,
            String title, String desc, Item it, String id,
            AdvancementHolder a
        ) throws ErosionDataGenException
        {
            var b = Advancement.Builder.advancement();
            ResourceLocation bb = (a == null) 
            ? ResourceLocation.withDefaultNamespace("textures/gui/advancements/backgrounds/stone.png")
            : null;

            if(a != null) b.parent(a);

            ErosionUtils.Log(
                "Generated advancement -> " + title
            );
            return b.display(
                it,//icon
                Component.literal(title),//title
                Component.literal(desc),//desc
                bb,
                AdvancementType.TASK,
                true,
                true,
                false
            )
            .addCriterion("has_item", InventoryChangeTrigger.TriggerInstance.hasItems(it))
            .save(t.k, ResourceLocation.fromNamespaceAndPath(Erosion.MODID, id), t.efh);
        }

        public static AdvancementHolder generateSimpleAdvancement(
            ErosionAdvGen.Generator t,
            String title, String desc, Item it, String id,
            AdvancementHolder a
        ) throws ErosionDataGenException
        {
            var b = Advancement.Builder.advancement();
            ResourceLocation bb = (a == null) 
            ? ResourceLocation.withDefaultNamespace("textures/gui/advancements/backgrounds/stone.png")
            : null;

            if(a != null) b.parent(a);

            ErosionUtils.Log(
                "Generated simple advancement -> " + title
            );
            return b.display(
                it,
                Component.literal(title),
                Component.literal(desc),
                bb,
                AdvancementType.TASK,
                true,
                true,
                false
            )
            .addCriterion("manual_trigger", CriteriaTriggers.IMPOSSIBLE.createCriterion(
                new ImpossibleTrigger.TriggerInstance()
            ))
            .save(t.k, ResourceLocation.fromNamespaceAndPath(Erosion.MODID, id), t.efh);
        }
    }
    public static class ErosionRecipe
    {
        public static void generateRecipe(
            String r,
            String o,
            List<String> p,
            Map<String, String> pd
        )
        {
            ErosionModCompat.JsonRecipeGenerator.generateCraftingRecipe(r, o, p, pd);
        }
    }

    public static class ErosionBlockState
    {
        public static void generateCustomTextures()
        {
            final String PNG = "." + ErosionConfig.ErosionDataGen.ErosionTextureGen.OUTPUT_FORMAT;
            //----------------------------MATERIAL PURIFIER
            ErosionUtils.Log("Generating custom textures...");
            String BLOCKID = ErosionRegistry.RawRegistry.MATERIAL_PURIFIER.getId();
            ErosionUtils.Log("Generating custom texture for -> " + BLOCKID);
            File baseFile = new File(resourcePath + BLOCKID + "_front" + PNG);

            for(int fuel = 0; fuel <= ErosionConfig.MAX_PURIFIER_FUEL; fuel++)
            {
                for(boolean finished : new boolean[]{false, true})
                {
                    String status = finished ? "on" : "off";

                    File fuelLayer = new File(resourcePath + "layers/fuel_" + fuel + PNG);
                    File lampLayer = new File(resourcePath + "layers/lamp_" + status + PNG);
                    
                    List<File> layers = List.of(
                        fuelLayer,
                        lampLayer
                    );

                    File outputFile = new File(generatedResourcesPath + BLOCKID + "_front_fuel_" + fuel + "_" + status + PNG);

                    ErosionTextureGen.combine(baseFile, layers, outputFile);
                }
            }
            
            //-------------------------------------CRUCIBLE
            BLOCKID = ErosionRegistry.RawRegistry.CRUCIBLE.getId();
            ErosionUtils.Log("Generating custom texture for -> " + BLOCKID);
            File baseCrucibleContent = new File(resourcePath + BLOCKID + PNG);

            for(int i = 1; i <= ErosionConfig.CRUCIBLE_SECONDS; i++)
            {
                File outputTex = new File(generatedResourcesPath + BLOCKID + "_heat_" + i + PNG);
                ErosionTextureGen.generateHeatedTexture(baseCrucibleContent, outputTex, i, ErosionConfig.CRUCIBLE_SECONDS);
            }

            //---------------------------CHEMICAL REACTOR COMPONENTS
            BLOCKID = ErosionRegistry.RawRegistry.CHEMICAL_REACTOR_MODULE.getId();
            ErosionUtils.Log("Generating custom texture for -> " + BLOCKID);
            String what = "top";
            String base = resourcePath + BLOCKID + "_" + what + PNG;
            String layer = new String();
            String output = new String();
            int howMany = 4;
            for(int i = 0; i < howMany; i++)
            {
                int idx = i + 1;
                layer = resourcePath + "layers/hand_" + idx + PNG;
                output = generatedResourcesPath + BLOCKID + "_" + what + "_" + idx + PNG;
                ErosionTextureGen.combine(new File(base), List.of(new File(layer)), new File(output));
            }
            
            BLOCKID = ErosionRegistry.RawRegistry.CHEMICAL_REACTOR_MODULE.getId();
            ErosionUtils.Log("Generating custom texture for -> " + BLOCKID);
            what = "bottom";
            base = resourcePath + BLOCKID + "_" + what + PNG;
            howMany = 12;
            for(int i = 0; i < howMany; i++)
            {
                int idx = i + 1;
                layer = resourcePath + "layers/indicator_" + idx + PNG;
                output = generatedResourcesPath + BLOCKID + "_" + what + "_" + idx + PNG;
                ErosionTextureGen.combine(new File(base), List.of(new File(layer)), new File(output));
            }

            //-------------------------------------------------
            BLOCKID = ErosionRegistry.RawRegistry.CHEMICAL_REACTOR_COOLING_SYSTEM.getId();
            ErosionUtils.Log("Generating custom texture for -> " + BLOCKID);
            what = "side";
            base = resourcePath + BLOCKID + "_" + what + PNG;
            layer = new String();
            output = new String();
            howMany = 4;
            for(int i = 0; i < howMany; i++)
            {
                int idx = i + 1;
                layer = resourcePath + "layers/console_" + idx + PNG;
                output = generatedResourcesPath + BLOCKID + "_" + what + "_" + idx + PNG;
                ErosionTextureGen.combine(new File(base), List.of(new File(layer)), new File(output));
            }

            BLOCKID = ErosionRegistry.RawRegistry.CHEMICAL_REACTOR_COOLING_SYSTEM.getId();
            ErosionUtils.Log("Generating custom texture for -> " + BLOCKID);
            what = "top";
            base = resourcePath + BLOCKID + "_" + what + PNG;
            howMany = 36;
            for(int i = 0; i < howMany; i++)
            {
                int idx = i + 1;
                layer = createRotatedTexture(
                    resourcePath + "layers/fan" + PNG,
                    generatedResourcesPath + "fan_" + idx + PNG,
                    i * 10
                );
                output = generatedResourcesPath + BLOCKID + "_" + what + "_" + idx + PNG;
                ErosionTextureGen.combine(new File(base), List.of(new File(layer)), new File(output));
            }
            return;
        }

        public static String createRotatedTexture(String from, String who, int angle)
        {
            final String CRASH = "wdym who?";
            var lol = new File(from);
            if(lol == null || !lol.exists())
            {
                return CRASH;
            }
            
            try
            {
                var org = ImageIO.read(lol);
                int h = org.getHeight();
                int w = org.getWidth();
                var img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
                var g = img.createGraphics();
                g.setRenderingHint(
                    RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR
                );
                g.rotate(Math.toRadians(angle), w/2.0,h/2.0);
                g.drawImage(org, 0,0,null);
                g.dispose();
                var f = new File(who);
                f.createNewFile();
                ImageIO.write(img,ErosionConfig.ErosionDataGen.ErosionTextureGen.OUTPUT_FORMAT,f);
                ErosionUtils.Log("Rotated successfully -> " + who + "::" + angle);
                return who;
            }
            catch(Exception e)
            {
                e.printStackTrace();
                return CRASH;
            }
        }

        @Deprecated public static BlockModelBuilder createRockModelOld(
            ErosionBlockStateGen g,
            String modelName, String texturePath
        )
        {
            return g.models().withExistingParent(modelName, g.mcLoc("block/block"))
            .texture("particle", g.modLoc("block/" + texturePath))
            .texture("texture", g.modLoc("block/" + texturePath))
            
            .element()
            .from(RockBlock.SHAPE_FIRSTDIM_X1, RockBlock.SHAPE_FIRSTDIM_Y1, RockBlock.SHAPE_FIRSTDIM_Z1)
            .to(RockBlock.SHAPE_FIRSTDIM_X2, RockBlock.SHAPE_FIRSTDIM_Y2, RockBlock.SHAPE_FIRSTDIM_Z2)
            .allFaces((direction, builder) -> builder.texture("#texture"))
            .end()

            .element()
            .from(RockBlock.SHAPE_SECONDDIM_X1, RockBlock.SHAPE_SECONDDIM_Y1, RockBlock.SHAPE_SECONDDIM_Z1)
            .to(RockBlock.SHAPE_SECONDDIM_X2, RockBlock.SHAPE_SECONDDIM_Y2, RockBlock.SHAPE_SECONDDIM_Z2)
            .allFaces((direction, builder) -> builder.texture("#texture"))
            .end()

            .element()
            .from(RockBlock.SHAPE_THIRDDIM_X1, RockBlock.SHAPE_THIRDDIM_Y1, RockBlock.SHAPE_THIRDDIM_Z1)
            .to(RockBlock.SHAPE_THIRDDIM_X2, RockBlock.SHAPE_THIRDDIM_Y2, RockBlock.SHAPE_THIRDDIM_Z2)
            .allFaces((direction, builder) -> builder.texture("#texture"))
            .end();
        }

        public static List<BlockModelBuilder> createRockModel(
            ErosionBlockStateGen g,
            String modelName, String texturePath
        )
        {
            var m = new ArrayList<BlockModelBuilder>();

            for(int i = 0; i < ErosionSimpleBlocks.RockBlock.NORTH_SHAPES.size(); i++)
            {
                var variantModelName = modelName + "_var" + i;
                var shape = ErosionSimpleBlocks.RockBlock.NORTH_SHAPES.get(i);

                var builder = g.models().withExistingParent(variantModelName, g.mcLoc("block/block"))
                    .texture("particle", g.modLoc("block/" + texturePath))
                    .texture("texture", g.modLoc("block/" + texturePath));

                shape.forAllBoxes((minX, minY, minZ, maxX, maxY, maxZ) -> {
                    builder.element()
                        .from((float) (minX * 16.0), (float) (minY * 16.0), (float) (minZ * 16.0))
                        .to((float) (maxX * 16.0), (float) (maxY * 16.0), (float) (maxZ * 16.0))
                        .allFaces((direction, faceBuilder) -> faceBuilder.texture("#texture"))
                        .end();
                });

                m.add(builder);
            }

            return m;
        }

        public static void generateRandomRotations(ErosionBlockStateGen g, Block b)
        {
            generateRandomRotationsForModel(g, b, g.cubeAll(b));
            return;
        }

        public static void generateRandomRotationsForModel(ErosionBlockStateGen g, Block b, ModelFile m)
        {
            var model = m;
            g.getVariantBuilder(b)
            .forAllStates(
                s -> new ConfiguredModel[] {
                    new ConfiguredModel(model, 0, 0, false),
                    new ConfiguredModel(model, 0, 90, false),
                    new ConfiguredModel(model, 0, 180, false),
                    new ConfiguredModel(model, 0, 270, false),
                    new ConfiguredModel(model, 90, 0, false),
                    new ConfiguredModel(model, 90, 90, false),
                    new ConfiguredModel(model, 90, 180, false),
                    new ConfiguredModel(model, 90, 270, false),
                    new ConfiguredModel(model, 180, 0, false),
                    new ConfiguredModel(model, 180, 90, false),
                    new ConfiguredModel(model, 180, 180, false),
                    new ConfiguredModel(model, 180, 270, false),
                    new ConfiguredModel(model, 270, 0, false),
                    new ConfiguredModel(model, 270, 90, false),
                    new ConfiguredModel(model, 270, 180, false),
                    new ConfiguredModel(model, 270, 270, false)
                }
            );
            g.simpleBlockItem(b, model);
            return;
        }

        @Deprecated public static void generateRockWithRandomRotationsOld(
            ErosionBlockStateGen g,
            Item it, Block b, BlockModelBuilder m
        )
        {
            g.getVariantBuilder(b)
            .forAllStates(
                s -> {
                    Direction d = s.getValue(ErosionSimpleBlocks.RockBlock.FACING);
                    int y = (int) d.toYRot();
                    return ConfiguredModel.builder()
                    .modelFile(m)
                    .rotationY((y + 180) % 360)
                    .build();
                }
            );

            g.simpleBlockItem(b, m);
            g.itemModels().basicItem(it);
            return;
        }
        public static void generateRockWithRandomRotations(
            ErosionBlockStateGen g,
            Item it, Block b, List<BlockModelBuilder> models
        )
        {
            g.getVariantBuilder(b)
            .forAllStates(
                s -> {
                    Direction d = s.getValue(ErosionSimpleBlocks.RockBlock.FACING);
                    int v = s.getValue(ErosionSimpleBlocks.RockBlock.VARIANT);
                    int y = (int) d.toYRot();
                    
                    return ConfiguredModel.builder()
                    .modelFile(models.get(v))
                    .rotationY((y + 180) % 360)
                    .build();
                }
            );

            g.simpleBlockItem(b, models.get(0));
            g.itemModels().basicItem(it);
            return;
        }
    }
    public static final class ErosionCommonUtils
    {
        public static final BlockModelBuilder createModelFromVoxelShapeLegacy(
            ErosionBlockStateGen g,
            String m, VoxelShape s,
            ResourceLocation top,
            ResourceLocation side,
            ResourceLocation bottom
        )
        {
            var b = g.models().getBuilder(m);
            
            b.texture("particle", side);
            b.texture("top", top);
            b.texture("side", side);
            b.texture("bottom", bottom);

            s.forAllBoxes(
                (minX, minY, minZ, maxX, maxY, maxZ) -> {
                    b.element()
                    .from((float) (minX * 16.0), (float) (minY * 16.0), (float) (minZ * 16.0))
                    .to((float) (maxX * 16.0), (float) (maxY * 16.0), (float) (maxZ * 16.0))
                    .faces(
                        (dd, bb) -> {
                            switch(dd)
                            {
                                case UP -> bb.texture("#top");
                                case DOWN -> bb.texture("#bottom");
                                default -> bb.texture("#side");
                            }
                        }
                    )
                    .end();
                }
            );
            return b;
        }
        public static BlockModelBuilder createModelFromVoxelShape(
            ErosionBlockStateGen g,
            String m, VoxelShape s,
            ResourceLocation top,
            ResourceLocation side,
            ResourceLocation bottom
        )
        {
            var b = g.models().getBuilder(m);
            
            b.parent(g.models().getExistingFile(g.mcLoc("block/block")));

            b.texture("particle", side);
            b.texture("top", top);
            b.texture("side", side);
            b.texture("bottom", bottom);

            s.forAllBoxes((minX, minY, minZ, maxX, maxY, maxZ) -> {
                float x1 = (float) (minX * 16.0);
                float y1 = (float) (minY * 16.0);
                float z1 = (float) (minZ * 16.0);
                float x2 = (float) (maxX * 16.0);
                float y2 = (float) (maxY * 16.0);
                float z2 = (float) (maxZ * 16.0);

                b.element()
                    .from(x1, y1, z1)
                    .to(x2, y2, z2)
                    .allFaces((dd, bb) -> {
                        switch(dd) {
                            case UP -> bb.texture("#top").uvs(x1, z1, x2, z2);
                            case DOWN -> bb.texture("#bottom").uvs(x1, 16.0f - z2, x2, 16.0f - z1);
                            case NORTH -> bb.texture("#side").uvs(16.0f - x2, 16.0f - y2, 16.0f - x1, 16.0f - y1);
                            case SOUTH -> bb.texture("#side").uvs(x1, 16.0f - y2, x2, 16.0f - y1);
                            case WEST -> bb.texture("#side").uvs(z1, 16.0f - y2, z2, 16.0f - y1);
                            case EAST -> bb.texture("#side").uvs(16.0f - z2, 16.0f - y2, 16.0f - z1, 16.0f - y1);
                        }
                    })
                    .end();
            });

            return b;
        }
    }
}