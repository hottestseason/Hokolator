package com.hottestseason.hokolator;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.SortedSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class AgentsScheduler {
    private static final int numOfCores = 8;
    private static final int awaitTime = 60 * 60;
    private static final AgentsScheduler instance = new AgentsScheduler();
    private static final Queue<Runnable> newlyCreatedJobs = new ConcurrentLinkedQueue<>();

    private final Map<String, BarrierScheduler> barrierSchedulerMap = new HashMap<>();
    private final Map<String, OrderedScheduler> orderedSchedulerMap = new HashMap<>();

    public static void update(Collection<? extends Agent> agents, double time) throws InterruptedException {
        AgentsScheduler.clear();
        ExecutorService executor = Executors.newFixedThreadPool(numOfCores);
        for (Agent agent : agents) {
            executor.execute(() -> {
                try {
                    agent.update(time);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
        }
        executor.shutdown();
        if (!executor.awaitTermination(awaitTime, TimeUnit.SECONDS)) throw new RuntimeException("Cannot finished in " + awaitTime);
        while (!newlyCreatedJobs.isEmpty()) {
            processNewlyCreatedJobs();
        }
        System.gc();
    }

    public static void clear() {
        instance.barrierSchedulerMap.clear();
        instance.orderedSchedulerMap.clear();
    }

    public static void finished(String tag, Agent agent) {
        instance.getOrRegisterWaitersScheduler(tag).finished(agent);
    }

    public static void barrier(String tag, Agent waiter, Set<? extends Agent> others, Runnable block) {
        instance.getOrRegisterWaitersScheduler(tag).barrier(waiter, others, block);
    }

    public static void ordered(String tag, Agent agent, Set<? extends Agent> agents, Comparator<Agent> comparator, Runnable runnable) {
        instance.getOrRegisterSequentialScheduler(tag).ordered(agent, agents, comparator, runnable);
    }

    private static void processNewlyCreatedJobs() throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(numOfCores);
        while (!newlyCreatedJobs.isEmpty()) {
            Runnable job = newlyCreatedJobs.poll();
            executor.execute(job);
        }
        executor.shutdown();
        if (!executor.awaitTermination(awaitTime, TimeUnit.SECONDS)) throw new RuntimeException("Cannot finished in " + awaitTime);
    }

    private BarrierScheduler getOrRegisterWaitersScheduler(String tag) {
        synchronized (barrierSchedulerMap) {
            if (!barrierSchedulerMap.containsKey(tag)) {
                barrierSchedulerMap.put(tag, new BarrierScheduler());
            }
        }
        return barrierSchedulerMap.get(tag);
    }

    private OrderedScheduler getOrRegisterSequentialScheduler(String tag) {
        synchronized (orderedSchedulerMap) {
            if (!orderedSchedulerMap.containsKey(tag)) {
                orderedSchedulerMap.put(tag, new OrderedScheduler(tag));
            }
        }
        return orderedSchedulerMap.get(tag);
    }

    class BarrierScheduler {
        private final Map<Agent, Boolean> finishedFlags = new HashMap<>();
        private final Map<Agent, Set<Agent>> waitersMap = new HashMap<>();
        private final Map<Agent, Set<? extends Agent>> waitingsMap = new HashMap<>();
        private final Map<Agent, Runnable> blockMap = new HashMap<>();

        private synchronized void finished(Agent agent) {
            finishedFlags.put(agent, true);
            if (waitersMap.containsKey(agent)) {
                for (Agent waiter : waitersMap.get(agent)) {
                    waitingsMap.get(waiter).remove(agent);
                    if (waitingsMap.get(waiter).isEmpty()) {
                        newlyCreatedJobs.add(blockMap.get(waiter));
                    }
                }
            }
        }

        private synchronized void barrier(Agent waiter, Set<? extends Agent> others, Runnable block) {
            Set<? extends Agent> waitings = new HashSet<>(others);
            for (Agent other : others) {
                if (finishedFlags.containsKey(other)) {
                    waitings.remove(other);
                } else {
                    if (!waitersMap.containsKey(other)) {
                        waitersMap.put(other, new HashSet<>());
                    }
                    waitersMap.get(other).add(waiter);
                }
            }
            if (waitings.isEmpty()) {
                newlyCreatedJobs.add(block);
            } else {
                waitingsMap.put(waiter, waitings);
                blockMap.put(waiter, block);
            }
        }
    }

    class OrderedScheduler {
        private final String tag;
        private final Map<Agent, SortedSet<Agent>> barrierMap = new ConcurrentHashMap<>();
        private final Map<Agent, Runnable> runnableMap = new ConcurrentHashMap<>();

        public OrderedScheduler(String tag) {
            this.tag = tag;
        }

        private void ordered(Agent agent, Set<? extends Agent> agents, Comparator<Agent> comparator, Runnable runnable) {
            if (agents.size() == 1 && agents.contains(agent)) {
                runnable.run();
                return;
            }

            runnableMap.put(agent, runnable);
            SortedSet<Agent> sorted = new ConcurrentSkipListSet<>(comparator);
            sorted.addAll(agents);
            barrierMap.put(agent, sorted);

            AgentsScheduler.finished(tag, agent);
            recursiveBarrier(agent, () -> {
                if (sorted.first() == agent) {
                    for (Agent _agent : sorted) {
                        runnableMap.get(_agent).run();
                    }
                }
            });
        }

        private void recursiveBarrier(Agent agent, Runnable runnable) {
            Set<Agent> agents = barrierMap.get(agent);
            AgentsScheduler.barrier(tag, agent, agents, () -> {
                int beforeSize, afterSize;
                synchronized (agents) {
                    beforeSize = agents.size();
                    agents.addAll(agents.stream().flatMap(waiting -> barrierMap.get(waiting).stream()).collect(Collectors.toSet()));
                    afterSize = agents.size();
                }
                if (beforeSize == afterSize) {
                    runnable.run();
                } else {
                    recursiveBarrier(agent, runnable);
                }
            });
        }
    }
}
