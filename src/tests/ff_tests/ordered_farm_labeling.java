package tests.ff_tests;

import bbflow.*;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.TreeSet;

/**
 * Example of Ordered Farm using packets with label sorted on the collector side and emitted in the correct order by Filter1
 */
public class ordered_farm_labeling {
    public static void main (String[] args) {
        class packet<T> {
            T value;
            int id;
            public packet(T val, int id) {
                this.value = val;
                this.id = id;
            }
        }

        defaultWorker<packet, packet> Generator = new defaultWorker<>() {
            public packet runJob(packet x) {
                return null;
            }

            public void init() {
                for (Integer i = 0; i < 1000; ++i) {
                    packet<Integer> x = new packet(i, i);
                    sendOutTo(x, i % out.size());
                }
                sendEOS();
            }
        };

        defaultWorker<packet, packet> Worker1 = new defaultWorker<>() {
            public packet runJob(packet x) {
                try {
                    Thread.sleep(5);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return x;
            }
        };

        defaultWorker<packet, packet> Filter1 = new defaultWorker<>() {
            TreeSet<packet> sortedqueue = new TreeSet<packet>(new Comparator<packet>() {
                @Override
                public int compare(packet s1, packet s2) {
                    if (s1.id<s2.id) { return -1; }
                    else if (s1.id==s2.id) { return 0; }
                    else { return 1; }
                }
            });

            int lastid = -1;
            public packet runJob(packet x) {
                if (x.id == (lastid+1)) {
                    sendOut(x);
                    lastid++;
                    while(sortedqueue.size() > 0) {
                        packet p = sortedqueue.first();
                        if (p.id == (lastid+1)) {
                            sendOut(p);
                            sortedqueue.remove(p);
                            lastid++;
                        } else {
                            break;
                        }
                    }
                } else {
                    sortedqueue.add(x);
                }

                return null;
            }

            @Override
            public void EOS() {
                for (packet p : sortedqueue) {
                    sendOut(p);
                }
                sortedqueue.clear();
            }
        };

        defaultWorker<packet, packet> Filter2 = new defaultWorker<>() {
            public void runJobMulti(packet x, LinkedList<ff_queue<packet>> out) {
                System.out.println("Filter2 received: "+x.value);
            }
        };

        ff_node stage1 = new ff_node(Generator);
        ff_node stage3 = new ff_node(Filter1);
        ff_node stage4 = new ff_node(Filter2);

        int n_workers = 64;
        if (args.length == 1) {
            n_workers = Integer.parseInt(args[0]);
        }

        ff_farm stage2 = new ff_farm<Integer, Integer>(0, null);
        for (int i = 0; i < n_workers; i++) {
            stage2.workers.push(new ff_node(defaultJob.uniqueJob(Worker1, i)));
        }

        stage2.emitter = new ff_node(new defaultEmitter(defaultEmitter.ROUNDROBIN));
        stage2.collector = new ff_node(new defaultCollector<Integer>(defaultCollector.FIRSTCOME));
        stage2.connectWorkersCollector();
        stage2.connectEmitterWorkers();


        ff_pipeline all = new ff_pipeline(stage1, stage2);
        all.appendBlock(stage3, ff_pipeline.TYPE_1_1);
        all.appendBlock(stage4, ff_pipeline.TYPE_1_1);

        customWatch x = new customWatch();
        x.start();
        all.start();
        all.join();
        x.end();
        x.printReport(true);
    }
}
