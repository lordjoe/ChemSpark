package com.lordjoe.branch;

import com.lordjoe.testing.*;
import org.junit.*;

/**
 * com.lordjoe.branch.AlkyneTests
 * just like branch AlkyneTests but using spark
 * User: Steve
 * Date: 12/4/2015
 */
public class AlkyneTests extends  FormulaTest {

    @Test
    public void c2H2Test() {
        int c2H2 = countNFromAtom("C2H2");
        junit.framework.Assert.assertEquals(1, c2H2);
    }

    @Test
    public void c3H4Test() {
        int c3H4 = countNFromAtom("C3H4");
        junit.framework.Assert.assertEquals(3, c3H4);
    }

    @Test
    public void c4H6Test() {
        int c4H6 = countNFromAtom("C4H6");
        junit.framework.Assert.assertEquals(9, c4H6);
    }

    @Test
    public void c5H8Test() {
        int c5H8 = countNFromAtom("C5H8");
        junit.framework.Assert.assertEquals(26, c5H8);
    }

    @Test
    public void c6H10Test() {
        int c6H10 = countNFromAtom("C6H10");
        junit.framework.Assert.assertEquals(77, c6H10);
    }

    // @Test   // added SLewis
     public void c12H20Test() {
         ElapsedTimer timer = new ElapsedTimer();
         int c12H20 = countNFromAtom("C12H20");
 //        Assert.assertEquals(77, c12H20);
         timer.showElapsed("C12H20 - found " + c12H20);
     }
     @Test   // added SLewis
     public void c11H18Test() {
         ElapsedTimer timer = new ElapsedTimer();
         int c12H20 = countNFromAtom("C11H18");
 //        Assert.assertEquals(77, c12H20);
         timer.showElapsed("C11H18 - found " + c12H20);
     }

    //   @Test   // added SLewis for bigger run
    public void c10H18Test() {
        ElapsedTimer timer = new ElapsedTimer();
        int c12H20 = countNFromAtom("C10H18");
        junit.framework.Assert.assertEquals(5572, c12H20);
        timer.showElapsed("C10H18- found " + c12H20);  // this test took 89 minutes
    }

     @Test   // added SLewis for bigger run
    public void c9H16Test() {
        ElapsedTimer timer = new ElapsedTimer();
        int c12H20 = countNFromAtom("C9H16");
        junit.framework.Assert.assertEquals(1903, c12H20);
        timer.showElapsed("C9H16- found " + c12H20);  // this test took 518  sec
    }

    @Test   // added SLewis for bigger run
    public void c8H14Test() {
        ElapsedTimer timer = new ElapsedTimer();
        int c12H20 = countNFromAtom("C8H14");
        junit.framework.Assert.assertEquals(654, c12H20);
        timer.showElapsed("C8H14- found " + c12H20);      // this test took 53 sec but 26 simgle machine spark
    }

    @Test   // added SLewis  for bigger run
    public void c7H12Test() {
        ElapsedTimer timer = new ElapsedTimer();
        int c12H20 = countNFromAtom("C7H12");
        junit.framework.Assert.assertEquals(222, c12H20);
        timer.showElapsed("C7H12- found " + c12H20);
    }

}