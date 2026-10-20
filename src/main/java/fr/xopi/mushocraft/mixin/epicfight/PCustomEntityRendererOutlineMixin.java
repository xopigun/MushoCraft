package fr.xopi.mushocraft.mixin.epicfight;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import fr.xopi.mushocraft.monster.MushoMonster;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import yesman.epicfight.client.renderer.patched.entity.PCustomEntityRenderer;

/**
 * Keeps our monsters visible while Epic Fight outlines them (crosshair target, lock-on, glowing).
 * <p>
 * For a glowing entity, Epic Fight's custom-mesh renderer swaps the body's render type for {@code RenderType.outline},
 * which draws the mesh into the outline target only: the red outline shows and the body disappears. Vanilla instead
 * draws the body normally into the outline buffer source it is handed, which adds the outline on top. For a MushoCraft
 * monster we give it back its normal render type, so it is drawn and outlined, as any glowing mob.
 */
@Mixin(value = PCustomEntityRenderer.class, remap = false)
public abstract class PCustomEntityRendererOutlineMixin {
    @WrapOperation(method = "render", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/RenderType;outline(Lnet/minecraft/resources/ResourceLocation;)Lnet/minecraft/client/renderer/RenderType;",
            remap = true))
    private RenderType mushocraft$keepBodyWhenOutlined(ResourceLocation texture, Operation<RenderType> outline,
                                                       @Local(argsOnly = true) LivingEntity entity) {
        if (entity instanceof MushoMonster) {
            return RenderType.entityCutoutNoCull(texture);
        }
        return outline.call(texture);
    }
}
