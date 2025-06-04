package io.github.rubixtheslime.rubix;

import io.github.rubixtheslime.rubix.block.ModBlocks;
import net.fabricmc.fabric.api.client.datagen.v1.provider.FabricModelProvider;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.block.Block;
import net.minecraft.client.data.BlockStateModelGenerator;
import net.minecraft.client.data.ItemModelGenerator;
import net.minecraft.client.data.Models;
import net.minecraft.item.Item;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

import java.util.concurrent.CompletableFuture;

public class RubixModDataGenerator implements DataGeneratorEntrypoint {

    @Override
    public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
        FabricDataGenerator.Pack pack = fabricDataGenerator.createPack();

        pack.addProvider(RubixModBlockTagGenerator::new);
        pack.addProvider(RubixModItemTagGenerator::new);
        pack.addProvider(RubixModModelGenerator::new);
    }

    private static class RubixModItemTagGenerator extends FabricTagProvider.ItemTagProvider {
        private static final TagKey<Item> SUPPRESSION_BLOCKS = TagKey.of(RegistryKeys.ITEM, Identifier.of(RubixMod.MOD_ID, "suppression_blocks"));

        public RubixModItemTagGenerator(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> completableFuture) {
            super(output, completableFuture);
        }

        @Override
        protected void configure(RegistryWrapper.WrapperLookup wrapperLookup) {
            for (Block block : ModBlocks.SUPPRESSION_BLOCKS) {
                getOrCreateTagBuilder(SUPPRESSION_BLOCKS).add(block.asItem());
            }
        }
    }

    private static class RubixModBlockTagGenerator extends FabricTagProvider.BlockTagProvider {
        private static final TagKey<Block> SUPPRESSION_BLOCKS = TagKey.of(RegistryKeys.BLOCK, Identifier.of(RubixMod.MOD_ID, "suppression_blocks"));

        public RubixModBlockTagGenerator(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> completableFuture) {
            super(output, completableFuture);
        }

        @Override
        protected void configure(RegistryWrapper.WrapperLookup wrapperLookup) {
            for (Block block : ModBlocks.SUPPRESSION_BLOCKS) {
                getOrCreateTagBuilder(SUPPRESSION_BLOCKS).add(block);
            }
        }
    }

    private static class RubixModModelGenerator extends FabricModelProvider {
        public RubixModModelGenerator(FabricDataOutput output) {
            super(output);
        }

        @Override
        public void generateBlockStateModels(BlockStateModelGenerator blockStateModelGenerator) {
            for (Block block : ModBlocks.SUPPRESSION_BLOCKS) {
                blockStateModelGenerator.registerSimpleCubeAll(block);
            }
        }

        @Override
        public void generateItemModels(ItemModelGenerator itemModelGenerator) {
            for (Block block : ModBlocks.SUPPRESSION_BLOCKS) {
                itemModelGenerator.register(block.asItem(), Models.GENERATED);
            }
        }
    }
}
