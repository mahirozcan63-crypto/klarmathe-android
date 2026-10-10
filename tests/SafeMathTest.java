package de.klarlernen.app;
public class SafeMathTest {
    static void expect(String expression,String answer,String steps,String status){String actual=SafeMath.check(expression,answer,steps).status;if(!actual.equals(status))throw new AssertionError(expression+" expected "+status+" got "+actual);}
    public static void main(String[] args){expect("2x+4=10","3","x+2=5\nx=3","correct");expect("2x+4=10","3","2x=6\nx=3","correct");expect("2x+4=10","3","2x=10","incorrect");expect("-2(x-3)=12","-3","","correct");expect("0,5x=0.75","3/2","","correct");expect("(x+1)/3=-2","-7","","correct");expect("x/2=0","0","","correct");expect("2x+4=10","eval(1)","","unsupported");expect("2x+4=10","3","0=0","incorrect");System.out.println("9 native SafeMath checks passed");}
}
