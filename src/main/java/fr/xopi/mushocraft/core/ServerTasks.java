package fr.xopi.mushocraft.core;

import fr.xopi.mushocraft.MushoCraft;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

/**
 * Actions to run a few ticks later on the server, on behalf of a living entity: the second blow of a combination, a
 * follow-up throw. Dropped if the entity died or left in the meantime.
 */
@EventBusSubscriber(modid = MushoCraft.MODID)
public final class ServerTasks {
    private static final List<Task> PENDING = new ArrayList<>();

    private ServerTasks() {
    }

    public static void later(LivingEntity actor, int ticks, Consumer<LivingEntity> action) {
        synchronized (PENDING) {
            PENDING.add(new Task(actor, Math.max(1, ticks), action));
        }
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        List<Task> due = new ArrayList<>();
        synchronized (PENDING) {
            Iterator<Task> it = PENDING.iterator();
            while (it.hasNext()) {
                Task task = it.next();
                if (--task.remaining <= 0) {
                    it.remove();
                    due.add(task);
                }
            }
        }
        for (Task task : due) {
            if (task.actor.isAlive() && !task.actor.isRemoved()) {
                task.action.accept(task.actor);
            }
        }
    }

    private static final class Task {
        private final LivingEntity actor;
        private final Consumer<LivingEntity> action;
        private int remaining;

        private Task(LivingEntity actor, int remaining, Consumer<LivingEntity> action) {
            this.actor = actor;
            this.remaining = remaining;
            this.action = action;
        }
    }
}
