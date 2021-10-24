package bbflow;

import java.lang.reflect.InvocationTargetException;
import java.util.LinkedList;

/**
 * Combine two nodes (of type ff_node or ff_comb) in one. The combination is linear, first runJob called and the returned value (if returned) is passed to second one. Also init() and EOS() are called when it's time.
 * @param <T> Custom type of input channels
 * @param <V> Custom type of output channels
 */
public class ff_comb<T,V> extends ff_node<T,V> {
    public static void preload() {
    }

    /**
     * Combine constructor
     * @param node1 first node
     * @param node2 second node
     * @param bufferSize buffer size of channels
     */
    public ff_comb(ff_node<T,Object> node1, ff_node<Object,V> node2, int bufferSize) {
        combine(node1,node2,bufferSize);
    }

    /**
     * Function called by default constructor to combine the 2 nodes
     * @param node1 first node
     * @param node2 second node
     * @param bufferSize buffersize of channel
     */
    private void combine(ff_node<T, Object> node1, ff_node<Object,V> node2, int bufferSize) {
        if (!((node1.getClass().equals(ff_node.class)) || (node1.getClass().equals(ff_comb.class)))) {
            System.out.println("Wrong node1 type passed to the combiner. Must be of type ff_comb or ff_node");
            System.exit(0);
        } else if (!((node2.getClass().equals(ff_node.class)) || (node2.getClass().equals(ff_comb.class)))) {
            System.out.println("Wrong node2 type passed to the combiner. Must be of type ff_comb or ff_node");
            System.exit(0);
        }

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

    /**
     * Combine constructor
     * @param node1 first node
     * @param node2 second node
     */
    public ff_comb(ff_node<T,Object> node1, ff_node<Object,V> node2) {
        combine(node1,node2,bb_settings.defaultBufferSize);
    }
}
