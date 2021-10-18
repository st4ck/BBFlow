package tests.MSOM;

import java.util.LinkedList;
import bbflow.*;

public class Emitter extends defaultEmitter<SOMData> {
    int command_channel = 0;
    int feedback_channel = 1;

    boolean commandRunning = false;

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
                    element.searchResult = null;

                    /*element.train_i = 0;
                    element.train_j = 0;
                    element.to = 2;*/

                    if (MSOM.DEBUG) System.out.println("Learn command "+element.dataType + " to "+element.to);
                    sendOutTo(element, element.to);
                    return;
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
