package io.github.rubixtheslime.rubix.block;

import io.github.rubixtheslime.rubix.RubixMod;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.function.Function;

public class ModBlocks {

    public static final SuppressionBlock UPDATE_SUPPRESSION_BLOCK = registerSuppressionBlock("update_suppress_block");
    public static final SuppressionBlock STATE_CHANGE_SUPPRESS_BLOCK = registerSuppressionBlock("state_change_suppress_block");
    public static final SuppressionBlock COMPARATOR_SUPPRESSION_BLOCK = registerSuppressionBlock("comparator_suppress_block");
//    public static final SuppressionBlock LIGHT_SUPPRESSION_BLOCK = registerSuppressionBlock("light_suppress_block");

    public static final SuppressionBlock TILE_TICK_SUPPRESSION_BLOCK = registerSuppressionBlock("tile_tick_suppress_block");
    public static final SuppressionBlock RANDOM_TICK_SUPPRESSION_BLOCK = registerSuppressionBlock("random_tick_suppress_block");
    public static final SuppressionBlock BLOCK_EVENT_SUPPRESSION_BLOCK = registerSuppressionBlock("block_event_suppress_block");
    public static final SuppressionBlock ENTITY_SUPPRESSION_BLOCK = registerSuppressionBlock("entity_suppress_block");
    public static final SuppressionBlock TILE_ENTITY_SUPPRESSION_BLOCK = registerSuppressionBlock("tile_entity_suppress_block");

//    public static final SuppressionBlock EXPLOSION_SUPPRESSION_BLOCK = registerSuppressionBlock("explosion_suppress_block");

    public static ArrayList<SuppressionBlock> SUPPRESSION_BLOCKS;

    public static void initialize() {
        for (SuppressionBlock block : SUPPRESSION_BLOCKS) {
            ItemGroupEvents
                .modifyEntriesEvent(ItemGroups.OPERATOR)
                .register((itemGroup) -> itemGroup.add(block.asItem()));
        }
    }

    private static SuppressionBlock registerSuppressionBlock(String path) {
        SuppressionBlock block = (SuppressionBlock) register(
            AbstractBlock.Settings.create()
                .sounds(BlockSoundGroup.STONE)
                .allowsSpawning(Blocks::never),
            path,
            SuppressionBlock::of,
            true
        );
        if (SUPPRESSION_BLOCKS == null) {
            SUPPRESSION_BLOCKS = new ArrayList<>();
        }
        SUPPRESSION_BLOCKS.add(block);
        return block;
    }

    public static Block register(AbstractBlock.Settings settings, String path, Function<AbstractBlock.Settings, Block> factory, boolean andItem) {
        Identifier full_id = Identifier.of(RubixMod.MOD_ID, path);
        RegistryKey<Block> key = RegistryKey.of(RegistryKeys.BLOCK, full_id);

        settings = settings.registryKey(key);

        Block block = Registry.register(Registries.BLOCK, key, factory.apply(settings));

        if (andItem) {
            RegistryKey<Item> item_key = RegistryKey.of(RegistryKeys.ITEM, full_id);

            Item.Settings item_settings = new Item.Settings()
                    .useBlockPrefixedTranslationKey()
                    .registryKey(item_key);

            Registry.register(Registries.ITEM, item_key, new BlockItem(block, item_settings));
        }

        return block;
    }
}
