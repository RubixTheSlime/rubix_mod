package io.github.rubixtheslime.rubix.item;

import io.github.rubixtheslime.rubix.RubixMod;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModItems {
//    public static final Item UPDATE_SUPPRESSION_BLOCK = register(new Item(new Item.Settings()), "update_suppression_block");

    public static void initialize() {
    }

    public static Item register(Item item, String id) {
        Identifier itemID = Identifier.of(RubixMod.MOD_ID, id);
        return Registry.register(Registries.ITEM, itemID, item);
    }
}
