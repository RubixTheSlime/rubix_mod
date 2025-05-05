package io.github.rubixtheslime.rubix.command.client;

import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

public class FakeServerCommandSource extends ServerCommandSource {
    public FakeServerCommandSource(FabricClientCommandSource source) {
        super(null, source.getPosition(), source.getRotation(), null, 0, "", Text.empty(), null, source.getEntity());
    }
}
