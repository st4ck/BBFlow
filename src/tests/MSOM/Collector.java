package tests.MSOM;

import bbflow.*;

import java.util.LinkedList;

/**
 * this is our Collector, waiting result from ALL workers in case of SEARCH and waiting ONLY from node involved in case of LEARN.
 * Result sent to Emitter through feedback channel
 */
public class Collector extends defaultCollector<SOMData> {
    int receivedcount = 0;
    bestPosition result = new bestPosition();
    int parts;
    int split;

    int listeningState = defaultCollector.ROUNDROBIN;
    public final int WAITSINGLE = 10;
    int waitingnode = 0;
    boolean[] ignore_channels;

    public Collector(int parts) {
        this.parts = parts;
        this.split = (int) Math.sqrt(parts);
    }

    public void initChannels() {
        ignore_channels = new boolean[in.size()];
        for (int i=0; i<in.size(); i++) {
            ignore_channels[i] = false;
        }
    }

    @Override
    public void runJob() throws InterruptedException {
        SOMData received = null;
        ff_queue<SOMData> out_channel = out.get(0);
        int last_position = 0;

        switch (listeningState) {
            case defaultCollector.ROUNDROBIN:
                while (received == null) {
                    if (!ignore_channels[position]) {
                        received = in.get(position).poll();
                        if ((received == null) && in.get(position).getEOS()) {
                            in.remove(position); // input channel not needed anymore
                            position--; // stepping back of 1 position because next there's the increment to the next position that now has the same index

                            if (in.size() == 0) { // no more input channels, EOS only last time
                                out_channel.setEOS();
                                return;
                            }
                        }
                    }

                    last_position = position;

                    position++;
                    if (position >= in.size()) {
                        position = 0;
                    }
                }
                break;
            case WAITSINGLE:
                //waitingnode = 2;
                if (MSOM.DEBUG) System.out.println("Waiting on node "+waitingnode);
                received = in.get(waitingnode).take();
                if (MSOM.DEBUG) System.out.println("Done waiting on node "+waitingnode);
                position = 0;
                listeningState = defaultCollector.ROUNDROBIN;
                break;
        }

        if (received.dataType == SOMData.SEARCH_AND_LEARN) {
            ignore_channels[last_position] = true;

            receivedcount++;
            if (result.bestdist > received.searchResult.bestdist) {
                result.bestdist = received.searchResult.bestdist;
                result.besti = received.searchResult.besti;
                result.bestj = received.searchResult.bestj;
                result.id = received.from;
                result.split = this.split;
            }

            if (receivedcount == parts) {
                received.dataType = SOMData.LEARN;
                receivedcount = 0;
                received.searchResult = result;
                received.from = -1;
                sendOut(received);
                for (int i = 0; i < in.size(); i++) {
                    ignore_channels[i] = false;
                }
                listeningState = WAITSINGLE;
                waitingnode = result.id;

            }
        } else if (received.dataType == SOMData.SEARCH) {
            ignore_channels[last_position] = true;

            receivedcount++;
            if (result.bestdist > received.searchResult.bestdist) {
                result.bestdist = received.searchResult.bestdist;
                result.besti = received.searchResult.besti;
                result.bestj = received.searchResult.bestj;
                result.id = received.from;
                result.split = this.split;
            }

            if (receivedcount == parts) {
                received.dataType = SOMData.SEARCH_FINISHED;
                receivedcount = 0;
                received.searchResult = result;
                received.from = -1;
                sendOut(received);
                for (int i = 0; i < in.size(); i++) {
                    ignore_channels[i] = false;
                }
                listeningState = defaultCollector.ROUNDROBIN;
            }
        } else if (received.dataType == SOMData.EOS) {
            in = new LinkedList<>();
            return;
        } else {
            received.dataType = SOMData.FINISHED;
            for (int i=0; i<in.size(); i++) {
                ignore_channels[i] = false;
            }
            sendOut(received);
        }
    }

    public void EOS() {
        if (MSOM.DEBUG) System.out.println("Collector EOS received");
    }
}