package tests.MSOM;

import bbflow.*;

import java.util.LinkedList;

public class Postfilter extends defaultWorker<SOMData,SOMData> {
    int receivedcount = 0;
    bestPosition result = new bestPosition();
    int parts;
    int split;

    public Postfilter(int parts) {
        this.parts = parts;
        this.split = (int) Math.sqrt(parts);
    }

    @Override
    public void runJob() {
        try {
            SOMData x = in.get(0).take();
            if (x.dataType == SOMData.SEARCH_AND_LEARN) {
                receivedcount++;
                if (result.bestdist > x.searchResult.bestdist) {
                    result.bestdist = x.searchResult.bestdist;
                    result.besti = x.searchResult.besti;
                    result.bestj = x.searchResult.bestj;
                    result.id = x.from;
                }

                if (receivedcount == parts) {
                    x.dataType = SOMData.LEARN;
                    receivedcount = 0;
                    x.searchResult = result;
                    x.from = -1;
                    sendOut(x);
                }
            } else if (x.dataType == SOMData.EOS) {
                in = new LinkedList<>();
                return;
            } else {
                x.dataType = SOMData.FINISHED;
                sendOut(x);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void EOS() {
        System.out.println("Collector EOS received");
    }
}
