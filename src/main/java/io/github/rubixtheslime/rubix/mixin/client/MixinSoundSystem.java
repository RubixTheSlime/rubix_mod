package io.github.rubixtheslime.rubix.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.github.rubixtheslime.rubix.RubixMod;
import net.minecraft.client.sound.SoundSystem;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(SoundSystem.class)
public class MixinSoundSystem {
    @Unique
    private static boolean saidTheThingAlready = false;

    @WrapOperation(method = "reloadSounds", at = @At(value = "INVOKE", target = "Lorg/slf4j/Logger;warn(Ljava/lang/String;Ljava/lang/Object;)V"))
    void annoyingWarnWrap(Logger instance, String string, Object o, Operation<Void> original) {
        if (saidTheThingAlready) return;
        saidTheThingAlready = true;
        original.call(instance, string, o);
        RubixMod.LOGGER.warn("future \"missing sound for event\" messages are being suppressed for this session as they are really annoying");
    }

}
