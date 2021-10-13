package tests.ff_tests;

import bbflow.*;

import java.util.LinkedList;

public class all2all {
    public static void main (String[] args) {
        defaultWorker<Long, Long> firstStage = new defaultWorker<>() {
            public void runJobMulti(Long x, LinkedList<ff_queue<Long>> out) {

            }

            public void init() {
                System.out.println("firstStage started (" + id + ")");

                for (long i = 0; i < 10000; ++i) {
                    sendOut(i);
                }
                sendEOS();
            }

            public void EOS() {
                System.out.println("firstStage ending (" + id + ")");
            }
        };

        defaultWorker<Long, Long> secondStage = new defaultWorker<>() {
            public void runJobMulti(Long x, LinkedList<ff_queue<Long>> out) {
                System.out.println("secondStage received " + x);
            }

            public void EOS() {
                System.out.println("EOS received by secondStage (" + id + ")");
            }
        };

        ff_farm stage1 = new ff_farm<>(3, firstStage);
        stage1.removeEmitter();
        ff_farm stage2 = new ff_farm<>(3, secondStage);

        ff_all2all stages = new ff_all2all();
        stages.combine_farm(stage1,stage2);
        stages.start();
        stages.join();
    }

}
