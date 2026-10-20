package fr.xopi.mushocraft.compat.epicfight;

import yesman.epicfight.api.collider.Collider;
import yesman.epicfight.api.collider.OBBCollider;

/**
 * Attack colliders of the monsters, given as one thinks of them in Minecraft and converted to the frame Epic Fight
 * reads them in.
 * <p>
 * Epic Fight places a collider in the frame of the joint it follows, and our skeletons' joints keep Blender's axes
 * (tools/bestiary): x to the right, <b>y forward</b>, <b>z up</b>. A collider written straight in Minecraft's axes
 * (y up, -z forward) lands somewhere else: "in front of the head" became "under the head". Every collider goes through
 * {@link #box} so that cannot happen again (found 20/10/2026 with the Iron Crawler, checked by a gametest).
 */
public final class MonsterColliders {
    private MonsterColliders() {
    }

    /**
     * A box around a joint.
     *
     * @param halfWidth  half its size across (x)
     * @param halfHeight half its size up and down
     * @param halfLength half its size front to back
     * @param right      offset to the right of the joint
     * @param up         offset above the joint
     * @param forward    offset ahead of the joint, the way the creature faces
     */
    public static Collider box(double halfWidth, double halfHeight, double halfLength, double right, double up, double forward) {
        return new OBBCollider(halfWidth, halfLength, halfHeight, right, forward, up);
    }
}
