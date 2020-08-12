package net.cavoj.sleepalone.mixin;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.GameRules;
import net.minecraft.world.MutableWorldProperties;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.function.Supplier;

@Mixin(ServerWorld.class)
public abstract class MixinServerWorld extends World {
    private boolean someoneSleeping = false;

    protected MixinServerWorld(MutableWorldProperties properties, RegistryKey<World> registryKey, DimensionType dimensionType, Supplier<Profiler> supplier, boolean bl, boolean bl2, long l) {
        super(properties, registryKey, dimensionType, supplier, bl, bl2, l);
    }

    @Shadow
    @Final
    private List<ServerPlayerEntity> players;

    @Shadow
    private void wakeSleepingPlayers() {}

    @Shadow
    public void setTimeOfDay(long timeOfDay) {}

    @Shadow
    private void resetWeather() {}

    @Inject(method="tick", at=@At("HEAD"))
    public void tick(CallbackInfo ci) {
        if (this.someoneSleeping && this.players.stream().anyMatch((player) -> !player.isSpectator() && player.isSleepingLongEnough())) {
            // Duplicating this code from ServerWorld.tick, as I don't see a reasonable way to modify the condition there
            this.someoneSleeping = false;
            if (this.getGameRules().getBoolean(GameRules.DO_DAYLIGHT_CYCLE)) {
                long l = this.properties.getTimeOfDay() + 24000L;
                this.setTimeOfDay(l - l % 24000L);
            }
            this.wakeSleepingPlayers();
            if (this.getGameRules().getBoolean(GameRules.DO_WEATHER_CYCLE)) {
                this.resetWeather();
            }
        }
    }

    @Inject(method="updateSleepingPlayers", at=@At("HEAD"), cancellable = true)
    public void updateSleepingPlayers(CallbackInfo ci) {
        this.someoneSleeping = false;
        for (ServerPlayerEntity player : this.players) {
            if (!player.isSpectator() && player.isSleeping()) {
                this.someoneSleeping = true;
                break;
            }
        }
        ci.cancel();
    }
}
