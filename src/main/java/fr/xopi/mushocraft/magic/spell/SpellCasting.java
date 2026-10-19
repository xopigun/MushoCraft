package fr.xopi.mushocraft.magic.spell;

import fr.xopi.mushocraft.MushoCraft;
import fr.xopi.mushocraft.combat.move.CooldownService;
import fr.xopi.mushocraft.combat.move.MoveUseResult;
import fr.xopi.mushocraft.magic.mana.ManaService;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Casting by full incantation, the common method (wiki, page <i>Magic</i>): the caster chants for a while,
 * slowed down, and the spell takes shape at the end. Mana is paid when the spell takes shape, so an
 * interrupted chant costs nothing. Being hurt breaks the chant.
 */
public final class SpellCasting {
    private static final ResourceLocation SLOW_MODIFIER = MushoCraft.id("casting/slow");
    private static final double CHANT_SLOWDOWN = -0.5;

    private static final Map<UUID, ActiveCast> ACTIVE = new ConcurrentHashMap<>();

    private SpellCasting() {
    }

    public static MoveUseResult begin(ServerPlayer caster, Spell spell) {
        if (!MagicService.knows(caster, spell)) {
            return MoveUseResult.fail("message.mushocraft.magic.unknown_spell", spell.displayName());
        }
        if (ACTIVE.containsKey(caster.getUUID())) {
            return MoveUseResult.fail("message.mushocraft.magic.already_chanting");
        }
        if (ManaService.snapshot(caster).current() < spell.manaCost()) {
            return MoveUseResult.fail("message.mushocraft.magic.not_enough_mana", spell.displayName());
        }
        if (spell.incantationTicks() <= 0) {
            return complete(caster, spell);
        }
        long now = CooldownService.now(caster);
        ACTIVE.put(caster.getUUID(), new ActiveCast(spell, now, now + spell.incantationTicks()));
        setSlowed(caster, true);
        caster.serverLevel().playSound(null, caster.blockPosition(), SoundEvents.ENCHANTMENT_TABLE_USE,
                SoundSource.PLAYERS, 0.6F, 1.4F);
        showProgress(caster, spell, 0.0);
        return MoveUseResult.SUCCESS;
    }

    public static Optional<Spell> chanting(ServerPlayer caster) {
        return Optional.ofNullable(ACTIVE.get(caster.getUUID())).map(ActiveCast::spell);
    }

    public static void tick(ServerPlayer caster) {
        ActiveCast cast = ACTIVE.get(caster.getUUID());
        if (cast == null) {
            return;
        }
        long now = CooldownService.now(caster);
        if (now >= cast.end()) {
            finish(caster);
            MoveUseResult result = complete(caster, cast.spell());
            result.failure().ifPresent(message -> caster.displayClientMessage(message, true));
        } else if ((now - cast.start()) % 5 == 0) {
            showProgress(caster, cast.spell(), (now - cast.start()) / (double) (cast.end() - cast.start()));
        }
    }

    public static void interrupt(ServerPlayer caster) {
        ActiveCast cast = ACTIVE.get(caster.getUUID());
        if (cast != null) {
            finish(caster);
            caster.displayClientMessage(Component.translatable("message.mushocraft.magic.interrupted",
                    cast.spell().displayName()), true);
        }
    }

    /** Drops any chant without message, for logouts and deaths. */
    public static void clear(ServerPlayer caster) {
        if (ACTIVE.containsKey(caster.getUUID())) {
            finish(caster);
        }
    }

    private static MoveUseResult complete(ServerPlayer caster, Spell spell) {
        if (!ManaService.trySpend(caster, spell.manaCost())) {
            return MoveUseResult.fail("message.mushocraft.magic.not_enough_mana", spell.displayName());
        }
        double power = MagicService.proficiency(caster).power(spell);
        if (!spell.effect().cast(new SpellContext(caster, spell, power))) {
            ManaService.restore(caster, spell.manaCost());
            return MoveUseResult.fail("message.mushocraft.magic.fizzled", spell.displayName());
        }
        MagicService.gainExperience(caster, spell.school(), spell.manaCost());
        CooldownService.start(caster, spell.cooldownKey(), spell.cooldownTicks());
        return MoveUseResult.SUCCESS;
    }

    private static void finish(ServerPlayer caster) {
        ACTIVE.remove(caster.getUUID());
        setSlowed(caster, false);
    }

    private static void setSlowed(ServerPlayer caster, boolean slowed) {
        AttributeInstance speed = caster.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speed == null) {
            return;
        }
        if (slowed) {
            speed.addOrUpdateTransientModifier(new AttributeModifier(
                    SLOW_MODIFIER, CHANT_SLOWDOWN, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        } else {
            speed.removeModifier(SLOW_MODIFIER);
        }
    }

    private static void showProgress(ServerPlayer caster, Spell spell, double progress) {
        int filled = (int) Math.round(progress * 10);
        String bar = "■".repeat(filled) + "□".repeat(10 - filled);
        caster.displayClientMessage(Component.translatable("message.mushocraft.magic.chanting",
                spell.displayName(), bar), true);
    }

    private record ActiveCast(Spell spell, long start, long end) {
    }
}
