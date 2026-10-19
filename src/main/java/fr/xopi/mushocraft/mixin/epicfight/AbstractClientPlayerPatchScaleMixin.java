package fr.xopi.mushocraft.mixin.epicfight;

import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import yesman.epicfight.api.utils.math.OpenMatrix4f;
import yesman.epicfight.client.world.capabilites.entitypatch.player.AbstractClientPlayerPatch;

/**
 * Client counterpart of {@link PlayerPatchScaleMixin}: the client player patch rebuilds the model matrix
 * without calling its parent, so the entity scale has to be applied here too for the body to be drawn small.
 */
@Mixin(value = AbstractClientPlayerPatch.class, remap = false)
public abstract class AbstractClientPlayerPatchScaleMixin {
    @Inject(method = "getModelMatrix", at = @At("RETURN"))
    private void mushocraft$applyEntityScale(float partialTick, CallbackInfoReturnable<OpenMatrix4f> cir) {
        Player player = ((AbstractClientPlayerPatch<?>) (Object) this).getOriginal();
        float scale = player.getScale();
        if (scale != 1.0F) {
            cir.getReturnValue().scale(scale, scale, scale);
        }
    }
}
