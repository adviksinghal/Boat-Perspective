package com.bp.client.mixin;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.vehicle.boat.AbstractBoat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LocalPlayer.class)
public abstract class ClientPlayerEntityMixin {

    @Inject(method = "aiStep", at = @At("TAIL"))
    private void onAiStep(CallbackInfo ci) {
        LocalPlayer player = (LocalPlayer) (Object) this;

        if (player.getVehicle() instanceof AbstractBoat boat) {

            if (player.input != null && player.input.keyPresses != null && player.input.keyPresses.forward()) {

                float playerYaw = player.getYRot();
                float boatYaw = boat.getYRot();

                float deltaYaw = playerYaw - boatYaw;
                while (deltaYaw < -180.0F) deltaYaw += 360.0F;
                while (deltaYaw >= 180.0F) deltaYaw -= 360.0F;

                // Increased dead-zone slightly to absorb minor shaky hand movements
                float deadZone = 2.5F;

                if (Math.abs(deltaYaw) > deadZone) {
                    // Capped maximum turn speed slightly lower for maximum control
                    float maxSteerSpeed = 3.8F;

                    // Softened response curve by dividing by 75 instead of 45.
                    // This requires larger mouse sweeps to trigger sharp turns.
                    float targetTurnSpeed = (deltaYaw / 75.0F) * maxSteerSpeed;

                    if (targetTurnSpeed > maxSteerSpeed) targetTurnSpeed = maxSteerSpeed;
                    if (targetTurnSpeed < -maxSteerSpeed) targetTurnSpeed = -maxSteerSpeed;

                    boat.setYRot(boat.getYRot() + targetTurnSpeed);

                    boolean steerRight = deltaYaw > 0;
                    net.minecraft.world.entity.player.Input currentKeys = player.input.keyPresses;
                    player.input.keyPresses = new net.minecraft.world.entity.player.Input(
                            currentKeys.forward(), currentKeys.backward(), !steerRight, steerRight,
                            currentKeys.jump(), currentKeys.shift(), currentKeys.sprint()
                    );
                }
            }
        }
    }
}
