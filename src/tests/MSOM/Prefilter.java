package tests.MSOM;

import java.util.LinkedList;
import bbflow.*;

public class Prefilter extends defaultWorker<SOMData,SOMData> {
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
                    element.to = 4;*/

                    sendOut(element);
                    return;
                }
            } else {
                element = in.get(command_channel).take();
                commandRunning = true;

                if (element.dataType == SOMData.EOS) {
                    sendOut(element);
                    sendEOS();
                    in = new LinkedList<>();
                    return;
                }

                // sending new command
                sendOut(element);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void EOS() {
        System.out.println("Collector EOS received");
    }
}
