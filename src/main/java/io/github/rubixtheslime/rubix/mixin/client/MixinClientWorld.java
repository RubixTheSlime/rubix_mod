package io.github.rubixtheslime.rubix.mixin.client;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import io.github.rubixtheslime.rubix.block.ModBlocks;
import io.github.rubixtheslime.rubix.redfile.RedfileManager;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.world.WorldView;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ClientWorld.class)
public class MixinClientWorld {
    @WrapMethod(method = "tickEntities")
    void tickEntitiesWrap(Operation<Void> original) {
        RedfileManager.lockWorld(this);
        original.call();
        RedfileManager.unlockWorld(this);
    }

    @WrapMethod(method = "tickEntity")
    void tickEntityWrap(Entity entity, Operation<Void> original) {
        if (ModBlocks.ENTITY_SUPPRESSION_BLOCK.isAround(() -> (WorldView) this, entity::getBlockPos) && !entity.isPlayer()) return;
        original.call(entity);
    }

}
