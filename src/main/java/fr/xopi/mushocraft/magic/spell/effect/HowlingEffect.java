package fr.xopi.mushocraft.magic.spell.effect;

import fr.xopi.mushocraft.compat.epicfight.EpicFightAnimations;
import fr.xopi.mushocraft.magic.fx.CameraShake;
import fr.xopi.mushocraft.magic.fx.SpellFx;
import fr.xopi.mushocraft.magic.spell.SpellContext;
import fr.xopi.mushocraft.magic.spell.SpellEffect;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

/**
 * Howling (wiki, page <i>Magic Spells</i>): the Beast Race's voice magic, a howl infused with mana that stuns whoever
 * hears it. Other races can learn it, but it is much less effective in their mouths: in a human's, a short stun close
 * around. Creatures that are stunned reel (Epic Fight's own stun) and stay sluggish for a while.
 */
public final class HowlingEffect implements SpellEffect {
    private static final double RADIUS = 5.0;
    private static final float STUN_SECONDS = 1.0F;
    private static final int SLUGGISH_TICKS = 60;
    private static final int WAVE_COLOUR = 0xDDE6EE;

    @Override
    public boolean cast(SpellContext context) {
        ServerLevel level = context.level();
        LivingEntity user = context.caster();
        double reach = RADIUS * context.scale();
        for (LivingEntity heard : level.getEntitiesOfClass(LivingEntity.class, user.getBoundingBox().inflate(reach),
                entity -> entity != user && entity.isAlive() && entity.distanceToSqr(user) <= reach * reach)) {
            EpicFightAnimations.stun(heard, (float) (STUN_SECONDS * context.power()));
            heard.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, SLUGGISH_TICKS, 1), user);
        }
        // as in the anime (S1 ep. 13, Gyes' howl): great pale ripples roll out from the mouth over whoever listens, ever
        // wider; kept clear of the eyes; and the voice spreads all around along the ground
        Vec3 look = context.look();
        Vec3 mouth = user.getEyePosition().subtract(0.0, 0.2, 0.0);
        for (int ripple = 0; ripple < 3; ripple++) {
            SpellFx.shockWave(level, mouth.add(look.scale(1.8 + 1.4 * ripple)), look, (float) ((0.9 + 0.6 * ripple) * context.scale()),
                    WAVE_COLOUR);
        }
        SpellFx.shockWave(level, user.position().add(0.0, 0.15, 0.0), new Vec3(0.0, 1.0, 0.0), (float) (reach * 0.55), WAVE_COLOUR);
        level.playSound(null, user.blockPosition(), SoundEvents.RAVAGER_ROAR, SoundSource.PLAYERS, 1.2F, 1.4F);
        level.playSound(null, user.blockPosition(), SoundEvents.WOLF_HOWL, SoundSource.PLAYERS, 1.0F, 0.9F);
        CameraShake.around(level, user.position(), 0.6F, 8, reach * 2.0);
        return true;
    }
}
