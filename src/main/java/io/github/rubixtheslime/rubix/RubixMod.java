package io.github.rubixtheslime.rubix;

import io.github.rubixtheslime.rubix.block.ModBlocks;
import io.github.rubixtheslime.rubix.command.ModCommands;
import io.github.rubixtheslime.rubix.item.ModItems;
import io.github.rubixtheslime.rubix.network.RedfileResultPacket;
import io.wispforest.owo.network.OwoNetChannel;
import net.fabricmc.api.ModInitializer;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RubixMod implements ModInitializer {
    public static final String MOD_ID = "rubix";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static final OwoNetChannel RUBIX_MOD_CHANNEL = OwoNetChannel.create(Identifier.of(MOD_ID, "main_channel"));
    public static final io.github.rubixtheslime.rubix.RubixConfig CONFIG = io.github.rubixtheslime.rubix.RubixConfig.createAndLoad();

    @Override
    public void onInitialize() {
        ModCommands.init();
        ModBlocks.initialize();
        ModItems.initialize();
        if (EnabledMods.REDFILE) RUBIX_MOD_CHANNEL.registerClientboundDeferred(RedfileResultPacket.class);
    }
}
