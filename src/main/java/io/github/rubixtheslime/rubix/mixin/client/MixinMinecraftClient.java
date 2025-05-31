package io.github.rubixtheslime.rubix.mixin.client;

import io.github.rubixtheslime.rubix.RubixMod;
import io.github.rubixtheslime.rubix.gaygrass.PrideFlagManager;
import io.github.rubixtheslime.rubix.redfile.client.RedfileResultManager;
import io.github.rubixtheslime.rubix.EnabledMods;
import io.github.rubixtheslime.rubix.client.RubixModClient;
import io.github.rubixtheslime.rubix.imixin.client.IMixinMinecraftClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.RunArgs;
import net.minecraft.client.gui.screen.DownloadingTerrainScreen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.resource.ReloadableResourceManagerImpl;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public class MixinMinecraftClient implements IMixinMinecraftClient {
    @Shadow @Final private ReloadableResourceManagerImpl resourceManager;

    @Shadow @Nullable public ClientPlayerEntity player;
    @Unique
    private final RedfileResultManager redfileResultManager = new RedfileResultManager();

    @Override
    public RedfileResultManager rubix$getRedfileResultManager() {
        return redfileResultManager;
    }

    @Inject(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/resource/language/LanguageManager;<init>(Ljava/lang/String;Ljava/util/function/Consumer;)V"))
    public void init(RunArgs args, CallbackInfo ci) {
        if (EnabledMods.GAY_GRASS) {
            RubixModClient.prideFlagManager = new PrideFlagManager(resourceManager);
            resourceManager.registerReloader(RubixModClient.prideFlagManager);
        }
    }

    @Inject(method = "tick", at = @At("HEAD"))
    void tick(CallbackInfo ci) {
        if (RubixMod.CONFIG.gayGrassOptions.matchRotate()) {
            if (player == null) return;
            double yaw = Math.toDegrees(RubixModClient.prideFlagManager.getRotate(player.lastX, player.lastZ, Math.max(0.0001, RubixMod.CONFIG.gayGrassOptions.rotateDamp())) + Math.PI);
            double delta = (yaw - player.lastYaw + 720) % 360;
            if (delta >= 180) delta -= 360;
            double speed = RubixMod.CONFIG.gayGrassOptions.rotateSpeed();
            delta *= speed;
            player.setYaw((float) (player.lastYaw + delta));
        }
    }

    @Inject(method = "joinWorld", at = @At("HEAD"))
    public void joinWorld(ClientWorld world, DownloadingTerrainScreen.WorldEntryReason worldEntryReason, CallbackInfo ci) {
        if (EnabledMods.GAY_GRASS) {
            RubixModClient.prideFlagManager.setBiomeSeed(world.getBiomeAccess().seed);
        }
    }
}
