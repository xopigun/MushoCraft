package fr.xopi.mushocraft.combat.move;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * The nine slots of the move bar. Immutable; every change returns a new loadout.
 */
public final class MoveLoadout {
    public static final int SIZE = 9;

    private record Entry(int slot, MoveRef move) {
        static final Codec<Entry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.intRange(0, SIZE - 1).fieldOf("slot").forGetter(Entry::slot),
                MoveRef.CODEC.fieldOf("move").forGetter(Entry::move)
        ).apply(instance, Entry::new));
    }

    public static final Codec<MoveLoadout> CODEC = Entry.CODEC.listOf().xmap(MoveLoadout::fromEntries, MoveLoadout::entries);

    public static final StreamCodec<ByteBuf, MoveLoadout> STREAM_CODEC =
            ByteBufCodecs.optional(MoveRef.STREAM_CODEC).apply(ByteBufCodecs.list(SIZE))
                    .map(MoveLoadout::new, loadout -> loadout.slots);

    private final List<Optional<MoveRef>> slots;

    private MoveLoadout(List<Optional<MoveRef>> slots) {
        List<Optional<MoveRef>> copy = new ArrayList<>(Collections.nCopies(SIZE, Optional.empty()));
        for (int i = 0; i < Math.min(SIZE, slots.size()); i++) {
            copy.set(i, slots.get(i));
        }
        this.slots = List.copyOf(copy);
    }

    public static MoveLoadout empty() {
        return new MoveLoadout(List.of());
    }

    private static MoveLoadout fromEntries(List<Entry> entries) {
        List<Optional<MoveRef>> slots = new ArrayList<>(Collections.nCopies(SIZE, Optional.empty()));
        entries.forEach(entry -> slots.set(entry.slot(), Optional.of(entry.move())));
        return new MoveLoadout(slots);
    }

    private List<Entry> entries() {
        List<Entry> entries = new ArrayList<>();
        for (int i = 0; i < SIZE; i++) {
            int slot = i;
            slots.get(i).ifPresent(move -> entries.add(new Entry(slot, move)));
        }
        return entries;
    }

    public Optional<MoveRef> get(int slot) {
        return slot >= 0 && slot < SIZE ? slots.get(slot) : Optional.empty();
    }

    public MoveLoadout with(int slot, Optional<MoveRef> move) {
        List<Optional<MoveRef>> copy = new ArrayList<>(slots);
        copy.set(slot, move);
        return new MoveLoadout(copy);
    }

    public boolean contains(MoveRef move) {
        return slots.contains(Optional.of(move));
    }

    public Optional<Integer> firstEmptySlot() {
        for (int i = 0; i < SIZE; i++) {
            if (slots.get(i).isEmpty()) {
                return Optional.of(i);
            }
        }
        return Optional.empty();
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof MoveLoadout loadout && slots.equals(loadout.slots);
    }

    @Override
    public int hashCode() {
        return slots.hashCode();
    }
}
