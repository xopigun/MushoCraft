package fr.xopi.mushocraft.body;

import fr.xopi.mushocraft.MushoCraft;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import yesman.epicfight.registry.entries.EpicFightAttributes;

import java.util.List;
import java.util.Locale;
import java.util.function.Supplier;

/**
 * One attribute moved by a physical stat, and how far a fully trained stat moves it.
 * The list below is the whole mapping between MushoCraft stats and vanilla / Epic Fight attributes.
 */
public record BodyAttribute(BodyStat stat, String name, Supplier<Holder<Attribute>> attribute, double maxBonus,
                            AttributeModifier.Operation operation) {

    public static final List<BodyAttribute> ALL = List.of(
            new BodyAttribute(BodyStat.STRENGTH, "attack_damage", () -> Attributes.ATTACK_DAMAGE, 4.0,
                    AttributeModifier.Operation.ADD_VALUE),
            new BodyAttribute(BodyStat.STRENGTH, "impact", () -> EpicFightAttributes.IMPACT, 0.8,
                    AttributeModifier.Operation.ADD_VALUE),
            new BodyAttribute(BodyStat.VITALITY, "max_health", () -> Attributes.MAX_HEALTH, 10.0,
                    AttributeModifier.Operation.ADD_VALUE),
            new BodyAttribute(BodyStat.VITALITY, "stamina", () -> EpicFightAttributes.MAX_STAMINA, 10.0,
                    AttributeModifier.Operation.ADD_VALUE),
            new BodyAttribute(BodyStat.SPEED, "movement_speed", () -> Attributes.MOVEMENT_SPEED, 0.15,
                    AttributeModifier.Operation.ADD_MULTIPLIED_BASE),
            new BodyAttribute(BodyStat.SPEED, "attack_speed", () -> Attributes.ATTACK_SPEED, 0.4,
                    AttributeModifier.Operation.ADD_VALUE),
            new BodyAttribute(BodyStat.SPEED, "stamina_regen", () -> EpicFightAttributes.STAMINA_REGEN, 0.5,
                    AttributeModifier.Operation.ADD_VALUE),
            new BodyAttribute(BodyStat.RESISTANCE, "armor", () -> Attributes.ARMOR, 6.0,
                    AttributeModifier.Operation.ADD_VALUE),
            new BodyAttribute(BodyStat.RESISTANCE, "stun_armor", () -> EpicFightAttributes.STUN_ARMOR, 6.0,
                    AttributeModifier.Operation.ADD_VALUE),
            new BodyAttribute(BodyStat.RESISTANCE, "knockback_resistance", () -> Attributes.KNOCKBACK_RESISTANCE, 0.3,
                    AttributeModifier.Operation.ADD_VALUE)
    );

    public ResourceLocation modifierId() {
        return MushoCraft.id("body/" + stat.name().toLowerCase(Locale.ROOT) + "/" + name);
    }
}
