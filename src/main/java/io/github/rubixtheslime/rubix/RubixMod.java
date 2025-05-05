package io.github.rubixtheslime.rubix;

import io.github.rubixtheslime.rubix.command.ModCommands;
import io.github.rubixtheslime.rubix.mixin.MixinEntity;
import io.github.rubixtheslime.rubix.network.RedfileResultPacket;
import io.github.rubixtheslime.rubix.redfile.Sampler;
import io.wispforest.owo.network.OwoNetChannel;
import net.fabricmc.api.ModInitializer;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// extra copy because sometimes the IDE deletes it
//import io.github.rubixtheslime.rubix.RubixConfig;
import io.github.rubixtheslime.rubix.RubixConfig;

public class RubixMod implements ModInitializer {
    public static final String MOD_ID = "rubix";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static final OwoNetChannel RUBIX_MOD_CHANNEL = OwoNetChannel.create(Identifier.of(MOD_ID, "main_channel"));
    public static final RubixConfig CONFIG = RubixConfig.createAndLoad();

    @Override
    public void onInitialize() {
        ModCommands.init();
        if (EnabledMods.REDFILE) RUBIX_MOD_CHANNEL.registerClientboundDeferred(RedfileResultPacket.class);
        Sampler.getInstance();
    }
}
