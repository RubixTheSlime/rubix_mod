package io.github.rubixtheslime.rubix.redfile;

import io.github.rubixtheslime.rubix.imixin.IMixinServerTickManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;

public class RedfileInstance {
    private final ServerWorld serverWorld;
    private final ServerCommandSource commandSource;
    private final RedfileEndCondition.Builder runEndConditionBuilder;
    private RedfileEndCondition runEndCondition = null;
    private final RedfileEndCondition.Builder trialEndConditionBuilder;
    private RedfileEndCondition trialEndCondition = null;
    private final DataCollector collector;
    private final TrialFilter filter;
    private final boolean doLoad;
    private final boolean doSprint;
    private long startSamples;
    private long startTicks;
    private long startTime;

    private boolean running = true;

    private RedfileInstance(ServerWorld serverWorld, ServerCommandSource commandSource, RedfileEndCondition.Builder runEndConditionBuilder, RedfileEndCondition.Builder trialEndConditionBuilder, DataCollector collector, TrialFilter filter, boolean doLoad, boolean doSprint) {
        this.serverWorld = serverWorld;
        this.commandSource = commandSource;
        this.runEndConditionBuilder = runEndConditionBuilder;
        this.trialEndConditionBuilder = trialEndConditionBuilder;
        this.collector = collector;
        this.filter = filter;
        this.doLoad = doLoad;
        this.doSprint = doSprint;
    }

    static RedfileInstance start(
        BlockBox box,
        RedfileEndCondition.Builder runEndConditionBuilder,
        RedfileEndCondition.Builder trialEndConditionBuilder,
        DataCollector.Builder collectorBuilder,
        boolean doLoad,
        boolean doSprint,
        ServerCommandSource source
    ) {
        var collector = collectorBuilder.get();
        var world = source.getWorld();
        var filter = box == null ? new TrialFilter.TrivialTrialFilter() : new TrialFilter.BoxTrialFilter(box);
        collector.start(world);
        if (doSprint) {
            ((IMixinServerTickManager) world.getTickManager()).rubix$startForceSprint();
        }

        var res = new RedfileInstance(world, source, runEndConditionBuilder, trialEndConditionBuilder, collector, filter, doLoad, doSprint);
        res.trialEndCondition= trialEndConditionBuilder.start();
        Sampler.getInstance().bind(world, res);
        return res;
    }

    public void stop() {
        trialEndCondition.stop();
        runEndCondition.stop();
        long stopTicks = serverWorld.getTime();
        long stopTime = System.currentTimeMillis();
        Sampler.getInstance().unbind(serverWorld, this);
        long totalSamples = Sampler.getInstance().getTotalSamples() - startSamples;
        running = false;

        double tickRate = (double) (stopTime - startTime) / Math.max(1, stopTicks - startTicks);
        if (doSprint) ((IMixinServerTickManager) serverWorld.getTickManager()).rubix$stopForceSprint();
//        commandSource.sendFeedback(() -> Text.literal(String.format("number of samples: %d", sampler.getTotalSamples())), false);
        collector.finish(commandSource, serverWorld);
    }

    public boolean isRunning() {
        return running;
    }

    public void tick() {
        if (runEndCondition == null) {
            if (!trialEndConditionBuilder.ready()) return;
            startSamples = Sampler.getInstance().getTotalSamples();
            startTicks = serverWorld.getTime();
            startTime = System.currentTimeMillis();
            trialEndCondition = trialEndConditionBuilder.start();
            runEndCondition = runEndConditionBuilder.start();
        } else if (trialEndCondition.tick()) {
            long samples = Sampler.getInstance().getTotalSamples();
            long ticks = serverWorld.getTime();
            long time = System.currentTimeMillis();
            // tick count should never be 0 if we reach here
            double tickRate = (double) (time - startTime) / (ticks - startTicks);
            collector.split(samples - startSamples, tickRate);
            startSamples = samples;
            startTime = time;
            startTicks = ticks;
            runEndCondition.endTrial();
            trialEndCondition = trialEndConditionBuilder.start();
        }
        
        if (runEndCondition.tick()) stop();
    }
    
    public void inc(BlockPos blockPos) {
        if (runEndCondition != null && filter.test(blockPos)) collector.inc(blockPos);
    }

}
