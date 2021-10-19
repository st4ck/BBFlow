package tests.ff_tests;

import bbflow.*;

import java.lang.reflect.InvocationTargetException;
import java.util.LinkedList;
import java.util.concurrent.TimeUnit;

/*
 *
 *  |<------ farm without collector ------->|   |<--------- comb2 --------->|
 *  |                |<---- comb1 ---->|        |<- multi-input ->|
 *
 *
 *               |--> Worker1-->Worker2 -->|
 *               |                         |
 *   Emitter --> |--> Worker1-->Worker2 -->|-------> Filter1 ---------> Filter2
 *               |                         |
 *               |--> Worker1-->Worker2 -->|
 *
 */

/**
 * Two combined workers as single one in ff_farm
 */
public class combine {
    public static final long ntasks = 100000;
    public static final long worktime = 1*2400;
    public static final boolean busy_waiting = false;

    private static final long SLEEP_PRECISION = TimeUnit.MILLISECONDS.toNanos(2);
    private static final long SPIN_YIELD_PRECISION = TimeUnit.MILLISECONDS.toNanos(2);

    public static void sleepNanos(long nanoDuration) throws InterruptedException {
        final long end = System.nanoTime() + nanoDuration;
        long timeLeft = nanoDuration;
        do {
            if (timeLeft > SLEEP_PRECISION) {
                Thread.sleep(1);
            } else {
                if (timeLeft > SPIN_YIELD_PRECISION) {
                    Thread.yield();
                }
            }
            timeLeft = end - System.nanoTime();

            if (Thread.interrupted())
                throw new InterruptedException();
        } while (timeLeft > 0);
    }

    public static void ticks_wait(long nanos) {
        if (busy_waiting) {
            long start = System.nanoTime();
            while(start + nanos >= System.nanoTime()) { }
        } else {
            try {
                sleepNanos(nanos);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main (String[] args) throws InterruptedException, InvocationTargetException, InstantiationException, IllegalAccessException, NoSuchMethodException {
        defaultWorker<Long, Long> firstStage = new defaultWorker<>() {
            public Long runJob(Long x) {
                return null;
            }

            public void init() {
                for (long i = 1; i<=ntasks; ++i) {
                    sendOut(i);
                }
                sendEOS();
            }
        };

        defaultWorker<Long, Long> secondStage = new defaultWorker<>() {
            @Override
            public Long runJob(Long x) {
                ticks_wait(worktime);
                return x;
            }
        };

        defaultWorker<Long, Long> secondStage2 = new defaultWorker<>() {
            @Override
            public Long runJob(Long x) {
                ticks_wait(worktime);
                return x;
            }
        };

        defaultWorker<Long, Long> secondStage3 = new defaultWorker<>() {
            @Override
            public Long runJob(Long x) {
                ticks_wait(worktime);
                return x;
            }
        };

        defaultWorker<Long, Long> thirdStage = new defaultWorker<>() {
            @Override
            public Long runJob(Long x) {
                ticks_wait(worktime);
                return null;
            }
        };

        ff_node _1 = new ff_node(firstStage);
        ff_node _2 = new ff_node(secondStage);
        ff_node _3 = new ff_node(secondStage2);
        ff_node _4 = new ff_node(secondStage3);
        ff_node _5 = new ff_node(thirdStage);

        System.out.println("TEST  FOR  Time");
        customWatch w = new customWatch();
        w.start();
        Long r;
        for (int i=1; i<=ntasks; i++) {
            r = firstStage.runJob((long) i);
            r = secondStage.runJob(r);
            r = secondStage2.runJob(r);
            r = secondStage3.runJob(r);
            r = thirdStage.runJob(r);
        }
        w.end();
        w.printReport(true);

        ff_pipeline<Long,Long> all = new ff_pipeline<Long,Long>(_1,_2);
        all.appendBlock(_3,ff_pipeline.TYPE_1_1);
        all.appendBlock(_4,ff_pipeline.TYPE_1_1);
        all.appendBlock(_5,ff_pipeline.TYPE_1_1);
        all.addOutputChannel(new ff_queue<>());

        System.out.println("TEST  PIPE  Time");
        w = new customWatch();
        w.start();
        all.start();
        all.join();
        w.end();
        w.printReport(true);

        
    }
}
