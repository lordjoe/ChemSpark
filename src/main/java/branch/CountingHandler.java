package branch;

import org.openscience.cdk.interfaces.IAtomContainer;

public class CountingHandler implements Handler {
    
    private int counter;
    private boolean done;   // added SLewis
    
    @Override
    public void handle(IAtomContainer atomContainer) {
        counter++;
     }
    
    public int getCount() {
        return counter;
    }

    @Override
    public void finish() {
        done = true; // see when and how this is called - SLewis
        // no op
    }

}
