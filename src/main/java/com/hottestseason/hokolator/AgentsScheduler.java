package com.hottestseason.hokolator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;

public class AgentsScheduler {
    private static final AgentsScheduler instance = new AgentsScheduler();

    private final Map<String, BarrierScheduler> barrierSchedulerMap = new HashMap<>();
    private final Map<String, OrderedScheduler> orderedSchedulerMap = new HashMap<>();

    public static void update(Collection<? extends Agent> agents, double time) throws InterruptedException {
        AgentsScheduler.clear();
        List<Thread> threads = new ArrayList<>();
        for (Agent agent : agents) {
            Thread thread = new Thread(() -> {
                try {
                    agent.update(time);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
            threads.add(thread);
        }
        for (Thread thread : threads) thread.start();
        for (Thread thread : threads) thread.join();
        System.gc();
    }

    public static void clear() {
        instance.barrierSchedulerMap.clear();
        instance.orderedSchedulerMap.clear();
    }

    public static void finished(String tag, Agent agent) {
        instance.getOrRegisterWaitersScheduler(tag).finished(agent);
    }

    public static void barrier(String tag, Agent waiter, Set<? extends Agent> others) throws InterruptedException {
        instance.getOrRegisterWaitersScheduler(tag).barrier(waiter, others);
    }

    public static void ordered(String tag, Agent agent, Set<? extends Agent> agents, Comparator<Agent> comparator, Runnable runnable) throws InterruptedException {
        instance.getOrRegisterSequentialScheduler(tag).ordered(agent, agents, comparator, runnable);
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
        private final Map<Agent, CountDownLatch> countDownLatches = new ConcurrentHashMap<>();
        private final Map<Agent, Boolean> finishedFlags = new HashMap<>();
        private final Map<Agent, Set<Agent>> waitersMap = new HashMap<>();

        private synchronized void finished(Agent agent) {
            finishedFlags.put(agent, true);
            if (waitersMap.containsKey(agent)) {
                for (Agent waiter : waitersMap.get(agent)) {
                    countDownLatches.get(waiter).countDown();
                }
            }
        }

        private void barrier(Agent waiter, Set<? extends Agent> others) throws InterruptedException {
            if (others.size() == 1 && others.contains(waiter)) return;
            int latchSize = others.size();
            synchronized (this) {
                for (Agent other : others) {
                    if (finishedFlags.containsKey(other) && finishedFlags.get(other)) {
                        latchSize--;
                    } else {
                        if (!waitersMap.containsKey(other)) {
                            waitersMap.put(other, new HashSet<>());
                        }
                        waitersMap.get(other).add(waiter);
                    }
                }
                if (latchSize == 0) return;
                countDownLatches.put(waiter, new CountDownLatch(latchSize));
            }
            countDownLatches.get(waiter).await();
        }
    }

    class OrderedScheduler {
        private final String tag;
        private final Map<Agent, SortedSet<Agent>> barrierMap = new ConcurrentHashMap<>();
        private final Map<Agent, Runnable> runnableMap = new ConcurrentHashMap<>();

        public OrderedScheduler(String tag) {
            this.tag = tag;
        }

        private void ordered(Agent agent, Set<? extends Agent> agents, Comparator<Agent> comparator, Runnable runnable) throws InterruptedException {
            if (agents.size() == 1 && agents.contains(agent)) {
                runnable.run();
                return;
            }

            runnableMap.put(agent, runnable);
            SortedSet<Agent> sorted = new ConcurrentSkipListSet<>(comparator);
            sorted.addAll(agents);
            barrierMap.put(agent, sorted);

            AgentsScheduler.finished(tag, agent);
            recursiveBarrier(agent);

            if (sorted.first() == agent) {
                for (Agent _agent : sorted) {
                    runnableMap.get(_agent).run();
                }
            }
        }

        private void recursiveBarrier(Agent agent) throws InterruptedException {
            Set<Agent> agents = barrierMap.get(agent);
            AgentsScheduler.barrier(tag, agent, agents);
            int beforeSize, afterSize;
            synchronized (agents) {
                beforeSize = agents.size();
                agents.addAll(agents.stream().flatMap(waiting -> barrierMap.get(waiting).stream()).collect(Collectors.toSet()));
                afterSize = agents.size();
            }
            if (beforeSize != afterSize) recursiveBarrier(agent);
        }
    }
}
