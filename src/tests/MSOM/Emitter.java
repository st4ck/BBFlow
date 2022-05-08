package tests.MSOM;

import java.util.LinkedList;
import bbflow.*;

/**
 * Emitter receives commands from the external and send to workers what to do. Also received feedbacks from Collector as ACK and if there is a new task to execute.
 * For other informations about MSOM read the thesis.
 */
public class Emitter extends defaultEmitter<SOMData> {
    int command_channel = 0;
    int feedback_channel = 1;

    boolean commandRunning = false;

    public LinkedList<SOMData> results = new LinkedList<>();

    @Override
    public void runJob() {
        try {
            SOMData element;
            if (commandRunning) {
                element = in.get(feedback_channel).take();
                // received feedback by postfilter
                commandRunning = false;

                if (element.dataType == SOMData.LEARN) {
                    // search complete, proceed to learn phase
                    commandRunning = true;
                    element.train_i = element.searchResult.besti;
                    element.train_j = element.searchResult.bestj;
                    element.to = element.searchResult.id;
                    int split = element.searchResult.split;
                    int x = element.searchResult.id/split;
                    int y = element.searchResult.id%split;
                    element.searchResult = null;

                    /*element.train_i = 0;
                    element.train_j = 0;
                    element.to = 2;*/

                    if (MSOM.DEBUG) System.out.println("Learn command "+element.dataType + " to "+element.to);
                    element.communicationType = SOMData.LISTEN_NEIGHBOURS;
                    sendOutTo(element, element.to);
                    int to = element.to;

                    element = new SOMData(SOMData.FINISHED, id);
                    element.communicationType = SOMData.LISTEN_NEIGHBOURS;
                    for (int i=Math.max(0, x-1); i<=Math.min(split-1,x+1); i++) {
                        for (int j=Math.max(0, y-1); j<=Math.min(split-1,y+1); j++) {
                            if (((i*split)+j) != (to)) {
                                sendOutTo(element, (i * split) + j);
                            }
                        }
                    }
                    return;
                } else if (element.dataType == SOMData.SEARCH_FINISHED){
                    results.add(element);
                    if (MSOM.DEBUG) System.out.println("Result available");
                } else {
                    if (MSOM.DEBUG) System.out.println("finished feedback");
                }
            } else {
                element = in.get(command_channel).take();
                commandRunning = true;

                if (element.dataType == SOMData.EOS) {
                    sendOutToAll(element);
                    sendEOS();
                    in = new LinkedList<>();
                    return;
                }

                if (MSOM.DEBUG) System.out.println("New command "+element.dataType);
                // sending new command
                sendOutToAll(element);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void EOS() {
        if (MSOM.DEBUG) System.out.println("Collector EOS received");
    }
}
