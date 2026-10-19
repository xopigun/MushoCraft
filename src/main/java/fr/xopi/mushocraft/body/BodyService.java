package fr.xopi.mushocraft.body;

import fr.xopi.mushocraft.MushoCraft;
import fr.xopi.mushocraft.body.aura.BattleAuraService;
import fr.xopi.mushocraft.core.MushoAttachments;
import fr.xopi.mushocraft.life.LifeService;
import fr.xopi.mushocraft.talent.TalentService;
import fr.xopi.mushocraft.talent.Talents;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

/**
 * Single entry point for the physical stats: training them and turning them into attribute modifiers.
 * Modifiers are transient and recomputed from the stored training, so they never drift or pile up.
 */
public final class BodyService {
    private static final ResourceLocation CHILD_HEALTH = MushoCraft.id("body/maturity/max_health");
    private static final ResourceLocation CHILD_DAMAGE = MushoCraft.id("body/maturity/attack_damage");
    /** Health a newborn lacks compared with an adult (a newborn has 13 health instead of 20). */
    private static final double NEWBORN_MISSING_HEALTH = 7.0;
    private static final double MIN_CHANGE = 1.0E-3;

    private BodyService() {
    }

    public static BodyTraining training(ServerPlayer player) {
        return player.getData(MushoAttachments.BODY_TRAINING);
    }

    /** Current fraction of a stat's maximal bonus, before the age and the aura are applied. */
    public static double progress(ServerPlayer player, BodyStat stat) {
        double talent = TalentService.talents(player).map(talents -> talents.get(stat.talent())).orElse(Talents.NEUTRAL);
        return BodyRules.progress(training(player).experience(stat), talent);
    }

    /**
     * Adds training to a stat. Children learn faster; creative players do not train.
     */
    public static void train(ServerPlayer player, BodyStat stat, double amount) {
        if (amount <= 0.0 || player.isCreative() || player.isSpectator()) {
            return;
        }
        double gained = amount * BodyRules.trainingSpeed(LifeService.ageInYears(player).orElse(0.0));
        setExperience(player, stat, training(player).experience(stat) + gained);
        BattleAuraService.onTraining(player, gained);
    }

    public static void setExperience(ServerPlayer player, BodyStat stat, double xp) {
        player.setData(MushoAttachments.BODY_TRAINING, training(player).with(stat, xp));
    }

    /**
     * Fraction of a stat's maximal bonus the body really gives now: training and talent, limited by the body's
     * maturity, raised by the Battle Aura (except vitality: the aura strengthens the body, it gives no hearts).
     */
    public static double effectiveShare(ServerPlayer player, BodyStat stat) {
        double share = progress(player, stat) * BodyRules.bodyMaturity(LifeService.ageInYears(player).orElse(0.0));
        return stat == BodyStat.VITALITY ? share : share + BattleAuraService.bonus(player);
    }

    /** Recomputes every modifier given by the stats, the body's maturity and the aura. Cheap when nothing changed. */
    public static void apply(ServerPlayer player) {
        double maturity = BodyRules.bodyMaturity(LifeService.ageInYears(player).orElse(0.0));
        for (BodyAttribute entry : BodyAttribute.ALL) {
            set(player, entry.attribute().get(), entry.modifierId(), entry.maxBonus() * effectiveShare(player, entry.stat()),
                    entry.operation());
        }
        set(player, Attributes.MAX_HEALTH, CHILD_HEALTH,
                -NEWBORN_MISSING_HEALTH * (1.0 - maturity) / (1.0 - BodyRules.bodyMaturity(0.0)),
                AttributeModifier.Operation.ADD_VALUE);
        set(player, Attributes.ATTACK_DAMAGE, CHILD_DAMAGE, maturity - 1.0, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
        if (player.getHealth() > player.getMaxHealth()) {
            player.setHealth(player.getMaxHealth());
        }
    }

    private static void set(ServerPlayer player, Holder<Attribute> attribute, ResourceLocation id, double amount,
                            AttributeModifier.Operation operation) {
        AttributeInstance instance = player.getAttribute(attribute);
        if (instance == null) {
            return;
        }
        AttributeModifier current = instance.getModifier(id);
        if (Math.abs(amount) < MIN_CHANGE) {
            if (current != null) {
                instance.removeModifier(id);
            }
        } else if (current == null || Math.abs(current.amount() - amount) >= MIN_CHANGE) {
            instance.addOrUpdateTransientModifier(new AttributeModifier(id, amount, operation));
        }
    }
}
