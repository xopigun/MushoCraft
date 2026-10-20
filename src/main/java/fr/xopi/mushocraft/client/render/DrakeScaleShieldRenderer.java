package fr.xopi.mushocraft.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import fr.xopi.mushocraft.MushoCraft;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ShieldModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;

/**
 * Draws the Snow Drake scale shield on the vanilla shield's shape, with its own texture
 * (textures/entity/snow_drake_shield.png, painted by tools/bestiary/drake_armour.py).
 */
public class DrakeScaleShieldRenderer extends BlockEntityWithoutLevelRenderer {
    private static final ResourceLocation TEXTURE = MushoCraft.id("textures/entity/snow_drake_shield.png");

    private ShieldModel model;

    public DrakeScaleShieldRenderer() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
    }

    @Override
    public void onResourceManagerReload(ResourceManager resourceManager) {
        model = new ShieldModel(Minecraft.getInstance().getEntityModels().bakeLayer(ModelLayers.SHIELD));
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext context, PoseStack pose, MultiBufferSource buffers,
                             int light, int overlay) {
        if (model == null) {
            onResourceManagerReload(Minecraft.getInstance().getResourceManager());
        }
        pose.pushPose();
        pose.scale(1.0F, -1.0F, -1.0F);
        VertexConsumer vertices = ItemRenderer.getFoilBufferDirect(buffers, model.renderType(TEXTURE), true, stack.hasFoil());
        model.handle().render(pose, vertices, light, overlay);
        model.plate().render(pose, vertices, light, overlay);
        pose.popPose();
    }

    /** Hands the renderer to the item. */
    public static IClientItemExtensions extensions() {
        return new IClientItemExtensions() {
            private DrakeScaleShieldRenderer renderer;

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (renderer == null) {
                    renderer = new DrakeScaleShieldRenderer();
                }
                return renderer;
            }
        };
    }
}
