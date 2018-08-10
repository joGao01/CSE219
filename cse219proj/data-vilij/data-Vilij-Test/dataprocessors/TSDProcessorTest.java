package dataprocessors;

import org.junit.Test;

import static org.junit.Assert.*;

public class TSDProcessorTest {

    private TSDProcessor processor = new TSDProcessor();

    /** Empty string is run through the processor because it's the simplest boundary case*/
    @Test(expected = Exception.class)
    public void emptyString() throws Exception{
        processor.processString("");

    }

    /** An invalid string is run through the processor because it's also a common error*/
    @Test(expected = Exception.class)
    public void invalidString() throws Exception{
        processor.processString("lks;gstrhlw");

    }

    /** A working string is provided as comparison (and to prove that it works).*/
    @Test
    public void validData() throws Exception{
        processor.processString("@test\tLabel\t3,4");
    }

}