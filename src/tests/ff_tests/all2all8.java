package tests.ff_tests;

import bbflow.*;

import javax.xml.crypto.dsig.keyinfo.KeyValue;
import java.util.ArrayList;
import java.util.LinkedList;

/*
 *    ________________________________________
 *   |                                        |
 *   |      | --> Filter1 -->|                |
 *   |      |                | --> Filter2 -->|
 *   |----> | --> Filter1 -->|                |
 *   |      |                | --> Filter2 -->|
 *   |      | --> Filter1 -->|                |
 *   |________________________________________|
 */

/**
 * feedback testing 2 stage all2all
 */
public class all2all8 {
    public static void main (String[] args) {
        class mypair<T,U> {
            T first;
            U second;
        }

        defaultWorker<mypair<Long,Long>, mypair<Long,Long>> Filter1 = new defaultWorker<>() {
            int nfilter2 = 2;
            int ntasks;
            public void init() {
                ntasks = 10*nfilter2;

                for(int i=0; i<ntasks; ++i) {
                    mypair<Long,Long> out = new mypair();
                    out.first = (long)id;
                    out.second = (long)i;

                    sendOutTo(out, i%nfilter2);
                }
            }

            public void runJobMulti(mypair<Long,Long> in, LinkedList<ff_queue<mypair<Long,Long>>> o) {
                System.out.println("Filter1 "+id+" got back result");
                if (--ntasks == 0) sendEOS();
            }
        };

        defaultWorker<mypair<Long,Long>, mypair<Long,Long>> Filter2 = new defaultWorker<>() {
            public void runJobMulti(mypair<Long,Long> x, LinkedList<ff_queue<mypair<Long,Long>>> o) {
                System.out.println("Filter 2 (" + id + "): "+x.second+" from: "+x.first);
                sendOutTo(x, x.first.intValue());
            }
        };

        ff_farm firstStage = new ff_farm<>(3, Filter1);
        firstStage.removeEmitter();
        ff_farm secondStage = new ff_farm<>(2, Filter2);
        secondStage.removeCollector();

        ff_all2all a2a = new ff_all2all();
        a2a.combine_farm(firstStage,secondStage);

        // pipeline used as feedback (2nd stage -> 1st stage) in N*M connection
        ff_pipeline all = new ff_pipeline(secondStage,firstStage,ff_pipeline.TYPE_NxM);

        all.start();
        all.join();
    }

}
