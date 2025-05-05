package io.github.rubixtheslime.rubix.redfile;

import io.github.rubixtheslime.rubix.imixin.IMixinServerTickManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.math.BlockBox;

public class RedfileInstance {
    private final ServerWorld serverWorld;
    private final ServerCommandSource commandSource;
    private final ProfileEnder profileEnder;
    private final DataCollector collector;
    private final TrialFilter filter;
    private final boolean doLoad;
    private final boolean doSprint;
    private final long startSamples;
    private final long startTicks;
    private final long startTime;

    private boolean running = true;

    private RedfileInstance(ServerWorld serverWorld, ServerCommandSource commandSource, ProfileEnder profileEnder, DataCollector collector, TrialFilter filter, boolean doLoad, boolean doSprint, long startSamples, long startTicks, long startTime) {
        this.serverWorld = serverWorld;
        this.commandSource = commandSource;
        this.profileEnder = profileEnder;
        this.collector = collector;
        this.filter = filter;
        this.doLoad = doLoad;
        this.doSprint = doSprint;
        this.startSamples = startSamples;
        this.startTicks = startTicks;
        this.startTime = startTime;
    }

    static RedfileInstance start(
        BlockBox box,
        long length,
        RedfileTimeUnit unit,
        DetailEnum detail,
        boolean doLoad,
        boolean doSprint,
        ServerCommandSource source
    ) {
        var collector = switch (detail) {
            case SUMMARY -> new SummaryCollector();
            case DETAIL -> new DetailedCollector();
        };
        var world = source.getWorld();
        var filter = box == null ? new TrialFilter.TrivialTrialFilter() : new TrialFilter.BoxTrialFilter(box);
        long startTicks = world.getTime();
        long startTime = System.currentTimeMillis();
        long startSamples = Sampler.getInstance().getTotalSamples();
        var profileEnder = switch (unit) {
            case TICKS -> new ProfileEnder.TickProfileEnder(length);
            case SECONDS -> new ProfileEnder.TimeProfileEnder(length);
            case SAMPLES -> new ProfileEnder.SampleProfileEnder(Sampler.getInstance(), length + startSamples);
            case INDEFINITE -> new ProfileEnder.IndefiniteProfileEnder();
        };
        collector.start(world);
        if (doSprint) {
            ((IMixinServerTickManager) world.getTickManager()).rubix$startForceSprint();
        }

        var res = new RedfileInstance(world, source, profileEnder, collector, filter, doLoad, doSprint, startSamples, startTicks, startTime);
        Sampler.getInstance().bind(world, res);
        return res;
    }

    public void stop() {
        profileEnder.stop();
        long stopTicks = serverWorld.getTime();
        long stopTime = System.currentTimeMillis();
        Sampler.getInstance().unbind(serverWorld, this);
        long totalSamples = Sampler.getInstance().getTotalSamples() - startSamples;
        running = false;

        double tickRate = (double) (stopTime - startTime) / Math.max(1, stopTicks - startTicks);
        if (doSprint) ((IMixinServerTickManager) serverWorld.getTickManager()).rubix$stopForceSprint();
//        commandSource.sendFeedback(() -> Text.literal(String.format("number of samples: %d", sampler.getTotalSamples())), false);
        collector.finish(totalSamples, tickRate, commandSource, serverWorld);
    }

    public boolean isRunning() {
        return running;
    }

    public void tick() {
        if (profileEnder.tick()) stop();
    }

    public TrialFilter getFilter() {
        return filter;
    }

    public DataCollector getCollector() {
        return collector;
    }

    public enum DetailEnum implements StringIdentifiable {
        SUMMARY("summary"),
        DETAIL("detail"),
//            TRACK("track"),
        ;
        private final String name;

        DetailEnum(String name) {
            this.name = name;
        }

        @Override
        public String asString() {
            return name;
        }
    }
}
