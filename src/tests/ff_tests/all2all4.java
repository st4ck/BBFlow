package tests.ff_tests;

import bbflow.*;

import java.util.ArrayList;
import java.util.LinkedList;

public class all2all4 {
    public static void main (String[] args) {
        defaultWorker<Long, Long> Generator = new defaultWorker<>() {
            public Long runJob(Long x) {
                return null;
            }

            public void init() {
                long start = id*10 + 1;
                long stop  = start + 10 + 1;

                for(long i=start;i<stop;++i) {
                    sendOutTo(i, ((int)i)%3);
                }
                sendEOS();
            }
        };

        defaultWorker<Long, Long> Filter1 = new defaultWorker<>() {
            public void runJobMulti(Long x, LinkedList<ff_queue<Long>> out) {
                System.out.println("Filter1 received "+x);
                sendOut(x);
            }
        };

        defaultWorker<Long, Long> Filter2 = new defaultWorker<>() {
            public void runJobMulti(Long x, LinkedList<ff_queue<Long>> out) {
                System.out.println("Filter2 received "+x);
                sendOut(x);
            }
        };

        defaultWorker<Long, Long> Filter3 = new defaultWorker<>() {
            public void runJobMulti(Long x, LinkedList<ff_queue<Long>> out) {
                System.out.println(id + ": "+x);
            }
        };

        ff_farm _1_1 = new ff_farm<>(3, Generator);
        _1_1.removeEmitter();
        ff_farm _1_2 = new ff_farm<>(3, Filter1);
        _1_2.removeCollector();

        ff_farm _2_1 = new ff_farm<>(3, Filter2);
        _2_1.removeEmitter();
        ff_farm _2_2 = new ff_farm<>(2, Filter3);
        _2_2.removeCollector();

        ff_all2all stage1 = new ff_all2all();
        stage1.combine_farm(_1_1,_1_2);

        ff_all2all stage2 = new ff_all2all();
        stage2.combine_farm(_2_1,_2_2);

        ff_pipeline all = new ff_pipeline(stage1,stage2,true);
        all.start();
        all.join();
    }

}
