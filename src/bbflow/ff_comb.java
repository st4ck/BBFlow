package bbflow;

import java.lang.reflect.InvocationTargetException;
import java.util.LinkedList;

/**
 * Combine two nodes (of any type) in one
 * @param <T> Custom type of channels
 */
public class ff_comb<T,V> extends ff_node<T,V> {
    public ff_comb(ff_node<T,Object> node1, ff_node<Object,V> node2, int bufferSize) {
        combine(node1,node2,bufferSize);
    }

    private void combine(ff_node<T, Object> node1, ff_node<Object,V> node2, int bufferSize) {
        defaultWorker<T, V> combinedStage = new defaultWorker<>() {
            public void init() {
                node1.mynode.job.init();
                node2.mynode.job.init();
            }

            public void runJobMulti(T x, LinkedList<ff_queue<V>> o) {
                try {
                    V t = (V) node1.mynode.job.runJob(x);
                    if (t != null) {
                        t = (V) node2.mynode.job.runJob(t);
                        if (t != null) sendOut(t);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                } catch (InstantiationException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                }
            }

            public void EOS() {
                node1.mynode.job.EOS();
                node2.mynode.job.EOS();
            }

            public void sendOut(V element, int combined_side) {
                if (element == null) { return; }

                try {
                    if (combined_side == 0) {
                        V res = (V) node2.mynode.job.runJob(element);
                        if (res == null) { return; }
                        sendOut(res);
                    } else {
                        sendOut(element);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                } catch (InstantiationException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                }
            }
            public void sendOutTo(V element, int combined_side, int index) {
                if (element == null) { return; }
                sendOutTo(element, index);
            }


            public void sendEOS(int combined_side) {
                if (combined_side == 0) {
                    node2.mynode.job.EOS();
                }
                sendEOS();
            }
        };

        node1.mynode.job.combined = combinedStage;
        node2.mynode.job.combined = combinedStage;
        node1.mynode.job.combined_side = 0;
        node2.mynode.job.combined_side = 1;

        this.mynode = new node(combinedStage);
        this.mynode.job.runType = defaultJob.INLINE_MULTI;
    }

    public ff_comb(ff_node<T,Object> node1, ff_node<Object,V> node2) {
        combine(node1,node2,bb_settings.defaultBufferSize);
    }
}
