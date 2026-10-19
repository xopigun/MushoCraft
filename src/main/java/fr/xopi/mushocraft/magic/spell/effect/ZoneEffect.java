package fr.xopi.mushocraft.magic.spell.effect;

import fr.xopi.mushocraft.magic.entity.SpellZone;
import fr.xopi.mushocraft.magic.entity.ZoneVisual;
import fr.xopi.mushocraft.magic.spell.SpellContext;
import fr.xopi.mushocraft.magic.spell.SpellEffect;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;

import java.util.function.Supplier;

/**
 * A lasting spell set down where the caster aims (or around the caster): a {@link SpellZone} with its behaviour.
 * A wand or staff widens the zone like it enlarges any spell.
 */
public final class ZoneEffect implements SpellEffect {
    private final Supplier<SpellZone.Behavior> behavior;
    private final ZoneVisual visual;
    private final double radius;
    private final int durationTicks;
    private final double range;
    private final SoundEvent sound;
    private final boolean followsCaster;

    /**
     * @param range how far the zone can be cast; 0 sets it around the caster
     */
    public ZoneEffect(Supplier<SpellZone.Behavior> behavior, double radius, int durationTicks, double range, SoundEvent sound) {
        this(behavior, ZoneVisual.NONE, radius, durationTicks, range, sound);
    }

    /**
     * @param visual the body the client draws for the zone (a tornado's funnel)
     */
    public ZoneEffect(Supplier<SpellZone.Behavior> behavior, ZoneVisual visual, double radius, int durationTicks, double range,
                      SoundEvent sound) {
        this(behavior, visual, radius, durationTicks, range, sound, false);
    }

    private ZoneEffect(Supplier<SpellZone.Behavior> behavior, ZoneVisual visual, double radius, int durationTicks, double range,
                       SoundEvent sound, boolean followsCaster) {
        this.behavior = behavior;
        this.visual = visual;
        this.radius = radius;
        this.durationTicks = durationTicks;
        this.range = range;
        this.sound = sound;
        this.followsCaster = followsCaster;
    }

    /** The zone goes with the caster wherever they move, and its body is drawn on them (a flame jet from the hands). */
    public ZoneEffect followingCaster() {
        return new ZoneEffect(behavior, visual, radius, durationTicks, range, sound, true);
    }

    @Override
    public boolean cast(SpellContext context) {
        Vec3 center = range <= 0.0 ? context.caster().position()
                : Vec3.atBottomCenterOf(SpellTargeting.groundBelow(context, SpellTargeting.aimedPoint(context, range)));
        SpellZone zone = SpellZone.create(context.level(), context.caster(), behavior.get(), visual, radius * context.scale(),
                context.power(), durationTicks);
        zone.setPos(center);
        if (followsCaster) {
            zone.following(context.caster());
        }
        context.level().addFreshEntity(zone);
        context.level().playSound(null, zone.blockPosition(), sound, SoundSource.PLAYERS, 2.0F, 0.8F);
        return true;
    }
}
