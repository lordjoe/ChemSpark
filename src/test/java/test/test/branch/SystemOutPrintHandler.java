package test.test.branch;

import handler.Handler;
import io.AtomContainerPrinter;
import org.openscience.cdk.interfaces.IAtomContainer;

import java.io.*;

/**
 * test.test.branch.SystemOutPrintHandler
 * User: Steve
 * Date: 2/16/2016
 */
public class SystemOutPrintHandler  implements Handler<IAtomContainer> {
    private int count;

    private boolean showCount;
    private transient BufferedWriter sysOut;

    public SystemOutPrintHandler() {
        this( true);
    }

    public SystemOutPrintHandler(boolean showCount) {
         this.showCount = showCount;
        this.count = 0;
    }

    protected BufferedWriter getOut()
    {
        if(sysOut == null)  {
            sysOut =  new BufferedWriter(new PrintWriter(System.out));
        }
        return sysOut;
    }
    @Override
    public void handle(IAtomContainer atomContainer) {
        BufferedWriter out = getOut();
        try {
            if (showCount) {
                out.write(String.valueOf(count));
                out.write("\t");
            }
            out.write(AtomContainerPrinter.toString(atomContainer));
            out.newLine();
            out.flush();
            count++;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void finish() {

    }

}
