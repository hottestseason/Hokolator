package com.hottestseason.hokolator;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

public class AgentsScheduler {
    private static final AgentsScheduler instance = new AgentsScheduler();

    private final Map<String, WaitersScheduler> waitersSchedulerMap = new HashMap<>();
    private final Map<String, SequentialScheduler> sequentialSchedulerMap = new HashMap<>();

    public static void update(Set<? extends Agent> agents, double time) throws InterruptedException {
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
    }

    public static void clear() {
        instance.waitersSchedulerMap.clear();
        instance.sequentialSchedulerMap.clear();
    }

    public static void finished(String tag, Agent agent) {
        instance.getOrRegisterWaitersScheduler(tag).finished(agent);
    }

    public static void waitOthers(String tag, Agent waiter, Set<? extends Agent> others) throws InterruptedException {
        instance.getOrRegisterWaitersScheduler(tag).waitOthers(waiter, others);
    }

    public static void orderIf(String tag, Agent agent, Set<? extends Agent> agents, Comparator<Agent> comparator, Runnable runnable) {
        instance.getOrRegisterSequentialScheduler(tag).orderIf(agent, agents, comparator, runnable);
    }

    private WaitersScheduler getOrRegisterWaitersScheduler(String tag) {
        synchronized (waitersSchedulerMap) {
            if (!waitersSchedulerMap.containsKey(tag)) {
                waitersSchedulerMap.put(tag, new WaitersScheduler());
            }
        }
        return waitersSchedulerMap.get(tag);
    }

    private SequentialScheduler getOrRegisterSequentialScheduler(String tag) {
        synchronized (sequentialSchedulerMap) {
            if (!sequentialSchedulerMap.containsKey(tag)) {
                sequentialSchedulerMap.put(tag, new SequentialScheduler());
            }
        }
        return sequentialSchedulerMap.get(tag);
    }

    class WaitersScheduler {
        private final Map<Agent, CountDownLatch> countDownLatches = new HashMap<>();
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

        private void waitOthers(Agent waiter, Set<? extends Agent> others) throws InterruptedException {
            synchronized (this) {
                int latchSize = others.size();
                for (Agent other : others) {
                    if (finishedFlags.containsKey(other)) {
                        latchSize--;
                    } else {
                        if (!waitersMap.containsKey(other)) {
                            waitersMap.put(other, new HashSet<>());
                        }
                        waitersMap.get(other).add(waiter);
                    }
                }
                countDownLatches.put(waiter, new CountDownLatch(latchSize));
            }
            countDownLatches.get(waiter).await();
        }
    }

    class SequentialScheduler {
        private final Map<Set<?>, Set<Entry>> registeredEntriesMap = new HashMap<>();

        private void orderIf(Agent agent, Set<? extends Agent> agents, Comparator<Agent> comparator, Runnable runnable) {
            Set<Entry> entries = getOrRegisterRegisteredEntries(agents);
            synchronized (entries) {
                entries.add(new Entry(agent, runnable));
                if (isRunnable(entries, agents)) {
                    runAll(entries, comparator);
                }
            }
        }

        private Set<Entry> getOrRegisterRegisteredEntries(Set<?> agents) {
            synchronized (registeredEntriesMap) {
                Set<Entry> entries = registeredEntriesMap.get(agents);
                if (entries == null) {
                    entries = new HashSet<>();
                    registeredEntriesMap.put(agents, entries);
                }
                return entries;
            }
        }

        private boolean isRunnable(Set<Entry> entries, Set<?> agents) {
            for (Object agent : agents) {
                boolean containsFlag = false;
                for (Entry entry : entries) {
                    if (entry.agent.equals(agent)) {
                        containsFlag = true;
                        break;
                    }
                }
                if (!containsFlag) {
                    return false;
                }
            }
            return true;
        }

        private void runAll(Set<Entry> entries, Comparator<Agent> comparator) {
            PriorityQueue<Entry> queue =  new PriorityQueue<>(entries.size(), (entry1, entry2) -> comparator.compare(entry1.agent, entry2.agent));
            queue.addAll(entries);
            for (Entry entry : queue) {
                entry.runnable.run();
            }
        }

        class Entry {
            public final Agent agent;
            public final Runnable runnable;

            public Entry(Agent agent, Runnable runnable) {
                this.agent = agent; this.runnable = runnable;
            }

            @Override
            public boolean equals(Object obj) {
                if (obj instanceof Entry) {
                    return agent.equals(((Entry) obj).agent);
                } else {
                    return false;
                }
            }

            @Override
            public int hashCode() {
                return agent.hashCode();
            }
        }
    }
}
