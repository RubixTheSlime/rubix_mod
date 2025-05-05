package io.github.rubixtheslime.rubix.redfile;

import net.minecraft.util.TimeHelper;

import java.util.Timer;
import java.util.TimerTask;

public interface ProfileEnder {
    default void stop() {}

    boolean tick();

    final class TickProfileEnder implements ProfileEnder {
        private long ticksRemaining;

        public TickProfileEnder(long ticks) {
            this.ticksRemaining = ticks;
        }

        @Override
        public boolean tick() {
            return --ticksRemaining < 0;
        }
    }

    final class TimeProfileEnder implements ProfileEnder {
        private boolean finished = false;
        private final Timer timer = new Timer();
        private final TimerTask timeoutTask = new TimerTask() {
            @Override
            public void run() {
                finished = true;
            }
        };

        public TimeProfileEnder(long seconds) {
            timer.schedule(timeoutTask,  seconds * TimeHelper.SECOND_IN_MILLIS);
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

    final class IndefiniteProfileEnder implements ProfileEnder {

        @Override
        public boolean tick() {
            return false;
        }
    }

    final class SampleProfileEnder implements ProfileEnder {
        private final Sampler sampler;
        private final long targetSamples;

        public SampleProfileEnder(Sampler sampler, long targetSamples) {
            this.sampler = sampler;
            this.targetSamples = targetSamples;
        }

        @Override
        public boolean tick() {
            return sampler.getTotalSamples() >= targetSamples;
        }
    }
}
