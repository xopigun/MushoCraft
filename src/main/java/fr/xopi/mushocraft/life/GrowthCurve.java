package fr.xopi.mushocraft.life;

import java.util.List;

/**
 * Body size relative to an adult, interpolated linearly between keyframes and clamped at both ends.
 */
public final class GrowthCurve {
    /** Default human curve: about a third of adult size at birth, full size at adulthood. */
    public static final GrowthCurve HUMAN = new GrowthCurve(List.of(
            new Keyframe(0.0, 0.35),
            new Keyframe(LifeStage.CHILD.startAge(), 0.55),
            new Keyframe(LifeStage.ADOLESCENT.startAge(), 0.85),
            new Keyframe(LifeStage.ADULT.startAge(), 1.0)
    ));

    private final List<Keyframe> keyframes;

    public GrowthCurve(List<Keyframe> keyframes) {
        if (keyframes.isEmpty()) {
            throw new IllegalArgumentException("A growth curve needs at least one keyframe");
        }
        this.keyframes = List.copyOf(keyframes);
    }

    public double scaleAt(double ageInYears) {
        Keyframe first = keyframes.getFirst();
        if (ageInYears <= first.age()) {
            return first.scale();
        }
        for (int i = 1; i < keyframes.size(); i++) {
            Keyframe previous = keyframes.get(i - 1);
            Keyframe next = keyframes.get(i);
            if (ageInYears < next.age()) {
                double progress = (ageInYears - previous.age()) / (next.age() - previous.age());
                return previous.scale() + progress * (next.scale() - previous.scale());
            }
        }
        return keyframes.getLast().scale();
    }

    public record Keyframe(double age, double scale) {
    }
}
