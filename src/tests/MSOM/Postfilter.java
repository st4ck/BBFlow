package tests.MSOM;

import bbflow.*;

import java.util.LinkedList;

public class Postfilter extends defaultCollector<SOMData> {
    int receivedcount = 0;
    bestPosition result = new bestPosition();
    int parts;
    int split;

    int listeningState = defaultCollector.ROUNDROBIN;
    public final int WAITSINGLE = 10;
    int waitingnode = 0;

    public Postfilter(int parts) {
        this.parts = parts;
        this.split = (int) Math.sqrt(parts);
    }

    @Override
    public void runJob() throws InterruptedException {
        SOMData received = null;
        ff_queue<SOMData> out_channel = out.get(0);

        switch (listeningState) {
            case defaultCollector.ROUNDROBIN:
                received = in.get(position).take();
                if (received == null) {
                    in.remove(position); // input channel not needed anymore
                    position--;

                    if (in.size() == 0) { // no more input channels, EOS only last time
                        out_channel.setEOS();
                    }
                }

                position++;
                if (position >= in.size()) {
                    position = 0;
                }
                break;
            case WAITSINGLE:
                received = in.get(waitingnode).take();
                position = 0;
                listeningState = defaultCollector.ROUNDROBIN;
                break;
        }

        if (received.dataType == SOMData.SEARCH_AND_LEARN) {
            receivedcount++;
            if (result.bestdist > received.searchResult.bestdist) {
                result.bestdist = received.searchResult.bestdist;
                result.besti = received.searchResult.besti;
                result.bestj = received.searchResult.bestj;
                result.id = received.from;
            }

            if (receivedcount == parts) {
                received.dataType = SOMData.LEARN;
                receivedcount = 0;
                received.searchResult = result;
                received.from = -1;
                sendOut(received);
                listeningState = WAITSINGLE;
                waitingnode = result.id;
            }
        } else if (received.dataType == SOMData.EOS) {
            in = new LinkedList<>();
            return;
        } else {
            received.dataType = SOMData.FINISHED;
            sendOut(received);
        }
    }

    public void EOS() {
        System.out.println("Collector EOS received");
    }
}
