package bbflow;

import java.lang.reflect.InvocationTargetException;

/**
 * Combine two nodes (of any type) in one
 * @param <T> Custom type of channels
 */
public class ff_comb<T,V> extends ff_pipeline<T,V> {
    public ff_comb(ff_node<T,Object> node1, ff_node<Object,V> node2, int bufferSize) {
        super(null, null, 0);
        combine(node1,node2,bufferSize);
    }

    private void combine(ff_node<T, Object> node1, ff_node<Object,V> node2, int bufferSize) {
        if ((node1.mynode != null) && (node2.mynode != null)
                && (node1.mynode.job instanceof defaultWorker) && (node2.mynode.job instanceof defaultWorker)
                && (node1.mynode.job.runType == defaultJob.INLINE) && (node2.mynode.job.runType == defaultJob.INLINE)
        ) {
            this.bufferSize = bufferSize;
            pipeline_generic<T,Object,V> p = new pipeline_generic<>(bufferSize);

            defaultWorker<T, V> combinedStage = new defaultWorker<>() {
                public void init() {
                    node1.mynode.job.init();
                    node2.mynode.job.init();
                }

                public V runJob(T x) {
                    try {
                        V t = (V) node1.mynode.job.runJob(x);
                        return (V) node2.mynode.job.runJob(t);
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

                    return null;
                }

                public void EOS() {
                    node1.mynode.job.EOS();
                    node2.mynode.job.EOS();
                }

                public void sendOut(V element, int combined_side) {
                    try {
                        if (combined_side == 0) {
                            sendOut((V) node2.mynode.job.runJob(element));
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

            p.pipe.add(new ff_node(combinedStage));
            pipe = p;
        } else {
            ff_pipelineConstructor(node1, node2, bufferSize, TYPE_1_1);
        }
    }

    public ff_comb(ff_node<T,Object> node1, ff_node<Object,V> node2) {
        super(null,null);
        combine(node1,node2,bb_settings.defaultBufferSize);
    }
}
