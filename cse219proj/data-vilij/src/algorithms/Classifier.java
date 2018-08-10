package algorithms;

import java.util.List;

public abstract class Classifier implements Algorithms {
    /**
     * See Appendix C of the SRS. Defining the output as a
     * list instead of a triple allows for future extension
     * into polynomial curves instead of just straight lines.
     * See 3.4.4 of the SRS.
     */
    protected List<Integer> output;

    public List<Integer> getOutput() { return output; }
}
