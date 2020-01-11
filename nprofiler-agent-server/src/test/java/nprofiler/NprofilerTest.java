package nprofiler;

import com.sun.tools.attach.AttachNotSupportedException;
import com.sun.tools.attach.VirtualMachine;
import org.junit.Test;
import sun.tools.attach.HotSpotVirtualMachine;

import java.io.InputStream;

public class NprofilerTest {
    @Test
    public void testObjectHistogram() throws Exception {
        InputStream var3 = ((HotSpotVirtualMachine) attach("6624")).heapHisto(new Object[]{"-live" });
    }

    private  VirtualMachine attach(String var0) {
        try {
            return VirtualMachine.attach(var0);
        } catch (Exception var3) {
            String var2 = var3.getMessage();
            if (var2 != null) {
                System.err.println(var0 + ": " + var2);
            } else {
                var3.printStackTrace();
            }

            if (var3 instanceof AttachNotSupportedException) {
                System.err.println("The -F option can be used when the target process is not responding");
            }

            System.exit(1);
            return null;
        }
    }

}
