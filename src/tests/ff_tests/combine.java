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

        ff_node _1, _2, _3, _4, _5;
        _1 = new ff_node(defaultJob.uniqueJob(firstStage));
        _2 = new ff_node(defaultJob.uniqueJob(secondStage));
        _3 = new ff_node(defaultJob.uniqueJob(secondStage2));
        _4 = new ff_node(defaultJob.uniqueJob(secondStage3));
        _5 = new ff_node(defaultJob.uniqueJob(thirdStage));

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

        _1 = new ff_node(defaultJob.uniqueJob(firstStage));
        _2 = new ff_node(defaultJob.uniqueJob(secondStage));
        _3 = new ff_node(defaultJob.uniqueJob(secondStage2));
        _4 = new ff_node(defaultJob.uniqueJob(secondStage3));
        _5 = new ff_node(defaultJob.uniqueJob(thirdStage));

        System.out.println("TEST0 Time");
        ff_comb comb1 = new ff_comb(_1,_2);
        ff_comb comb2 = new ff_comb(_3,_4);
        ff_pipeline pipe = new ff_pipeline(comb1,comb2);
        pipe.appendBlock(_5, ff_pipeline.TYPE_1_1);
        pipe.addOutputChannel(new ff_queue());

        w = new customWatch();
        w.start();
        pipe.start();
        pipe.join();
        w.end();
        w.printReport(true);

        _1 = new ff_node(defaultJob.uniqueJob(firstStage));
        _2 = new ff_node(defaultJob.uniqueJob(secondStage));
        _3 = new ff_node(defaultJob.uniqueJob(secondStage2));
        _4 = new ff_node(defaultJob.uniqueJob(secondStage3));
        _5 = new ff_node(defaultJob.uniqueJob(thirdStage));

        System.out.println("TEST1 Time");
        ff_comb comb = new ff_comb(_1,_2);
        comb = new ff_comb(comb,_3);
        comb = new ff_comb(comb,_4);
        comb = new ff_comb(comb,_5);
        comb.addOutputChannel(new ff_queue());

        w = new customWatch();
        w.start();
        comb.start();
        comb.join();
        w.end();
        w.printReport(true);

        _1 = new ff_node(defaultJob.uniqueJob(firstStage));
        _2 = new ff_node(defaultJob.uniqueJob(secondStage));
        _3 = new ff_node(defaultJob.uniqueJob(secondStage2));
        _4 = new ff_node(defaultJob.uniqueJob(secondStage3));
        _5 = new ff_node(defaultJob.uniqueJob(thirdStage));

        System.out.println("TEST2 Time");
        comb1 = new ff_comb(_1,_2);
        comb2 = new ff_comb(_4,_5);
        pipe = new ff_pipeline(comb1,_3);
        pipe.appendBlock(comb2, ff_pipeline.TYPE_1_1);
        pipe.addOutputChannel(new ff_queue());

        w = new customWatch();
        w.start();
        pipe.start();
        pipe.join();
        w.end();
        w.printReport(true);

        _1 = new ff_node(defaultJob.uniqueJob(firstStage));
        _2 = new ff_node(defaultJob.uniqueJob(secondStage));
        _3 = new ff_node(defaultJob.uniqueJob(secondStage2));
        _4 = new ff_node(defaultJob.uniqueJob(secondStage3));
        _5 = new ff_node(defaultJob.uniqueJob(thirdStage));

        System.out.println("TEST3 Time");
        pipe = new ff_pipeline(_1,_2);
        pipe.appendBlock(_3, ff_pipeline.TYPE_1_1);
        comb2 = new ff_comb(_4,_5);
        pipe.appendBlock(comb2, ff_pipeline.TYPE_1_1);
        pipe.addOutputChannel(new ff_queue());

        w = new customWatch();
        w.start();
        pipe.start();
        pipe.join();
        w.end();
        w.printReport(true);
    }
}
