package fr.xopi.mushocraft.mixin.epicfight;

import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import yesman.epicfight.api.utils.math.OpenMatrix4f;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;

/**
 * Epic Fight builds the player's model matrix with a constant scale and ignores the vanilla {@code scale}
 * attribute, which MushoCraft uses to make children small. Applies the entity scale on top (server side:
 * weapon colliders follow the body size).
 */
@Mixin(value = PlayerPatch.class, remap = false)
public abstract class PlayerPatchScaleMixin {
    @Inject(method = "getModelMatrix", at = @At("RETURN"))
    private void mushocraft$applyEntityScale(float partialTick, CallbackInfoReturnable<OpenMatrix4f> cir) {
        Player player = ((PlayerPatch<?>) (Object) this).getOriginal();
        float scale = player.getScale();
        if (scale != 1.0F) {
            cir.getReturnValue().scale(scale, scale, scale);
        }
    }
}
