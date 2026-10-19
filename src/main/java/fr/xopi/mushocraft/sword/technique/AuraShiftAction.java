package fr.xopi.mushocraft.sword.technique;

import fr.xopi.mushocraft.MushoCraft;
import fr.xopi.mushocraft.body.aura.AuraLevel;
import fr.xopi.mushocraft.body.aura.BattleAuraService;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

import java.util.Map;

/**
 * North God Style, Battle Aura shifting (wiki, taught by Auber to Eris): the aura's distribution is shifted to suit the
 * fight, for example more speed at the cost of power. A toggle: the first use shifts the aura towards speed (faster
 * movement and swings, weaker blows), the next one puts it back. It needs a raised aura, and fades when the aura drops.
 */
public final class AuraShiftAction implements TechniqueAction {
    private static final Map<Holder<Attribute>, Double> SHIFT = Map.of(
            Attributes.MOVEMENT_SPEED, 0.18,
            Attributes.ATTACK_SPEED, 0.25,
            Attributes.ATTACK_DAMAGE, -0.2);
    private static final ResourceLocation ID = MushoCraft.id("technique/aura_shift");

    /** Usable at any distance: it changes the user, not the foe. */
    @Override
    public double maxRange() {
        return 32.0;
    }

    /** The Battle Aura is a player's own. */
    @Override
    public boolean usableBy(LivingEntity user) {
        return user instanceof ServerPlayer;
    }

    @Override
    public boolean perform(TechniqueContext context) {
        if (!(context.user() instanceof ServerPlayer player)) {
            return false;
        }
        if (isShifted(player)) {
            settle(player);
            player.displayClientMessage(Component.translatable("message.mushocraft.technique.aura_shift.off"), true);
            return true;
        }
        if (!BattleAuraService.isUp(player) || BattleAuraService.level(player).ordinal() < AuraLevel.CONSCIOUS.ordinal()) {
            player.displayClientMessage(Component.translatable("message.mushocraft.technique.aura_shift.no_aura"), true);
            return false;
        }
        SHIFT.forEach((attribute, share) -> {
            AttributeInstance instance = player.getAttribute(attribute);
            if (instance != null) {
                instance.addOrUpdateTransientModifier(new AttributeModifier(ID, share, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
            }
        });
        context.level().sendParticles(ParticleTypes.CLOUD, player.getX(), player.getY() + 0.1, player.getZ(), 14, 0.4, 0.05, 0.4, 0.06);
        context.level().playSound(null, player.blockPosition(), SoundEvents.BREEZE_JUMP, SoundSource.PLAYERS, 0.8F, 1.3F);
        player.displayClientMessage(Component.translatable("message.mushocraft.technique.aura_shift.on"), true);
        return true;
    }

    public static boolean isShifted(ServerPlayer player) {
        AttributeInstance speed = player.getAttribute(Attributes.MOVEMENT_SPEED);
        return speed != null && speed.getModifier(ID) != null;
    }

    /** The aura goes back to its usual balance. */
    public static void settle(ServerPlayer player) {
        SHIFT.keySet().forEach(attribute -> {
            AttributeInstance instance = player.getAttribute(attribute);
            if (instance != null) {
                instance.removeModifier(ID);
            }
        });
    }

    /** A shift lasts only while the aura is raised. */
    public static void tick(ServerPlayer player) {
        if (player.tickCount % 20 == 0 && isShifted(player) && !BattleAuraService.isUp(player)) {
            settle(player);
        }
    }
}
