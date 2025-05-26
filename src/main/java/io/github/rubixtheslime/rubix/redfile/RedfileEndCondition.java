package io.github.rubixtheslime.rubix.redfile;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.minecraft.registry.RegistryKey;
import net.minecraft.world.World;

import java.util.*;

public interface RedfileEndCondition {
    default void stop() {}
    default void endTrial() {}
    boolean tick();

    interface Builder {
        default boolean ready() { return true; }
        RedfileEndCondition start();
    }

    static Builder tickCondition(long ticks) {
        return () -> new TickCondition(ticks);
    }

    static Builder timeCondition(long millis) {
        return () -> new TimeCondition(millis);
    }

    static Builder sampleCondition(long count) {
        var sampler = Sampler.getInstance();
        return () -> new SampleCountCondition(sampler, count + sampler.getTotalSamples());
    }

    static Builder trialCountCondition(long count) {
        return () -> new TrialCountCondition(count);
    }

    static Builder signalCondition(World world, String name, int grouping) {
        return SignalCondition.SignalBuilder.create(world, name, grouping);
    }
    
    static Builder indefiniteCondition() {
        return IndefiniteCondition::new;
    }
    
    final class TickCondition implements RedfileEndCondition {
        private long ticksRemaining;

        public TickCondition(long ticks) {
            this.ticksRemaining = ticks;
        }

        @Override
        public boolean tick() {
            return --ticksRemaining < 0;
        }
    }
    
    final class TimeCondition implements RedfileEndCondition {
        private boolean finished = false;
        private final Timer timer = new Timer();
        private final TimerTask timeoutTask = new TimerTask() {
            @Override
            public void run() {
                finished = true;
            }
        };

        public TimeCondition(long millis) {
            timer.schedule(timeoutTask,  millis);
        }

        @Override
        public void stop() {
            timeoutTask.cancel();
        }

        @Override
        public boolean tick() {
            return finished;
        }

    }

    final class SampleCountCondition implements RedfileEndCondition {
        private final Sampler sampler;
        private final long targetSamples;

        public SampleCountCondition(Sampler sampler, long targetSamples) {
            this.sampler = sampler;
            this.targetSamples = targetSamples;
        }

        @Override
        public boolean tick() {
            return sampler.getTotalSamples() >= targetSamples;
        }
    }
    
    final class TrialCountCondition implements RedfileEndCondition {
        private long remaining;

        public TrialCountCondition(long amount) {
            this.remaining = amount;
        }

        @Override
        public void endTrial() {
            remaining--;
        }

        @Override
        public boolean tick() {
            return remaining <= 0;
        }
    }

    sealed class SignalCondition implements RedfileEndCondition {
        protected static final Map<RegistryKey<World>, Map<String, Map<SignalCondition, Integer>>> WAITING = new Object2ObjectOpenHashMap<>();
        protected boolean activated = false;

        @Override
        public boolean tick() {
            return activated;
        }

        public static void activate(World world, String name) {
            var worldEntry = WAITING.get(world.getRegistryKey());
            if (worldEntry == null) return;
            if (name == null) {
                List<String> toRemove = new ArrayList<>();
                worldEntry.forEach((a, b) -> {
                    activate(b);
                    if (b.isEmpty()) toRemove.addLast(a);
                });
                toRemove.forEach(worldEntry::remove);
            } else {
                var nameEntry = worldEntry.get(name);
                activate(nameEntry);
                if (nameEntry.isEmpty()) worldEntry.remove(name);
            }
            if (worldEntry.isEmpty()) WAITING.remove(world.getRegistryKey());
        }

        private static void activate(Map<SignalCondition, Integer> nameEntry) {
            if (nameEntry == null) return;
            nameEntry.replaceAll((a, b) -> b - 1);
            List<SignalCondition> toRemove = new ArrayList<>();
            nameEntry.forEach((a, b) -> {
                if (b > 0) return;
                a.activated = true;
                toRemove.addLast(a);
            });
            toRemove.forEach(nameEntry::remove);
        }

        public static void activateAll(World world) {
            activate(world, null);
        }

        protected static void add(World world, String name, int count, SignalCondition signalCondition) {
            WAITING
                .computeIfAbsent(world.getRegistryKey(), x -> new Object2ObjectOpenHashMap<>())
                .computeIfAbsent(name, x -> new Reference2ObjectOpenHashMap<>())
                .put(signalCondition, count);
        }

        private final static class SignalBuilder extends SignalCondition implements Builder {
            private final World world;
            private final String name;
            private final int count;

            private SignalBuilder(World world, String name, int count) {
                this.world = world;
                this.name = name;
                this.count = count;
            }

            public static SignalBuilder create(World world, String name, int count) {
                var res = new SignalBuilder(world, name, count);
                add(world, name, 1, res);
                return res;
            }

            @Override
            public boolean ready() {
                return activated;
            }

            @Override
            public RedfileEndCondition start() {
                var res = new SignalCondition();
                add(world, name, count, res);
                return res;
            }

        }

    }

    final class IndefiniteCondition implements RedfileEndCondition {
        @Override
        public boolean tick() {
            return false;
        }
    }
}
