package de.klarlernen.app;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/** Bounded affine expression parser; never evaluates code or uses a script engine. */
public final class SafeMath {
    public static final class Invalid extends IllegalArgumentException { Invalid(String s) { super(s); } }
    public static final class Fraction {
        final BigInteger n,d;
        Fraction(BigInteger n,BigInteger d){
            if(d.signum()==0)throw new Invalid("division_zero");if(d.signum()<0){n=n.negate();d=d.negate();}
            BigInteger g=n.abs().gcd(d);this.n=n.divide(g);this.d=d.divide(g);
            if(this.n.abs().bitLength()>100||this.d.bitLength()>100)throw new Invalid("too_large");
        }
        Fraction(long n){this(BigInteger.valueOf(n),BigInteger.ONE);}
        Fraction add(Fraction v){return new Fraction(n.multiply(v.d).add(v.n.multiply(d)),d.multiply(v.d));}
        Fraction sub(Fraction v){return add(v.neg());} Fraction neg(){return new Fraction(n.negate(),d);}
        Fraction mul(Fraction v){return new Fraction(n.multiply(v.n),d.multiply(v.d));}
        Fraction div(Fraction v){return new Fraction(n.multiply(v.d),d.multiply(v.n));}
        boolean zero(){return n.signum()==0;} boolean eq(Fraction v){return n.equals(v.n)&&d.equals(v.d);}
        public String toString(){return d.equals(BigInteger.ONE)?n.toString():n+"/"+d;}
        double value(){return n.doubleValue()/d.doubleValue();}
        static Fraction number(String text){String s=text.replace(',','.');if(!s.matches("[+-]?\\d+(?:\\.\\d{1,6})?"))throw new Invalid("syntax");int i=s.indexOf('.');return i<0?new Fraction(new BigInteger(s),BigInteger.ONE):new Fraction(new BigInteger(s.replace(".","")),BigInteger.TEN.pow(s.length()-i-1));}
    }
    static final class Affine { final Fraction a,b; Affine(Fraction a,Fraction b){this.a=a;this.b=b;} }
    public static final class Equation { final Affine left,right; final Fraction solution; Equation(Affine l,Affine r){left=l;right=r;Fraction a=l.a.sub(r.a),b=l.b.sub(r.b);if(a.zero())throw new Invalid(b.zero()?"identity":"contradiction");solution=b.neg().div(a);} }
    private final List<String> tokens=new ArrayList<>();private int pos=0,depth=0;
    SafeMath(String raw){
        if(raw==null||raw.length()>240)throw new Invalid("too_long");String s=raw.replace('−','-').replace('–','-').replace('×','*').replace('·','*').replace('÷','/');int i=0;
        while(i<s.length()){char c=s.charAt(i);if(Character.isWhitespace(c)){i++;continue;}if(Character.isDigit(c)){int start=i++;while(i<s.length()&&Character.isDigit(s.charAt(i)))i++;if(i<s.length()&&(s.charAt(i)==','||s.charAt(i)=='.')){i++;int startDecimal=i;while(i<s.length()&&Character.isDigit(s.charAt(i)))i++;if(i==startDecimal||i-startDecimal>6)throw new Invalid("syntax");}tokens.add(s.substring(start,i));}
            else if("xX+-*/()".indexOf(c)>=0){tokens.add(String.valueOf(Character.toLowerCase(c)));i++;}else throw new Invalid("unsupported");if(tokens.size()>120)throw new Invalid("too_long");}
        if(tokens.isEmpty())throw new Invalid("empty");
    }
    String peek(){return pos<tokens.size()?tokens.get(pos):"";}String take(){return pos<tokens.size()?tokens.get(pos++):"";}
    Affine atom(){if(++depth>20)throw new Invalid("too_long");String t=take();Affine v;
        if(t.equals("+")||t.equals("-")){Affine w=atom();v=t.equals("-")?new Affine(w.a.neg(),w.b.neg()):w;}
        else if(t.equals("(")){v=sum();if(!take().equals(")"))throw new Invalid("syntax");}
        else if(t.equals("x"))v=new Affine(new Fraction(1),new Fraction(0));
        else if(t.matches("\\d.*"))v=new Affine(new Fraction(0),Fraction.number(t));else throw new Invalid("syntax");depth--;return v;
    }
    Affine product(){Affine v=atom();while(true){String op=peek();boolean implicit=op.equals("x")||op.equals("(");if(!implicit&&!op.equals("*")&&!op.equals("/"))break;if(!implicit)take();Affine w=atom();if(op.equals("/")){if(!w.a.zero())throw new Invalid("nonlinear");v=new Affine(v.a.div(w.b),v.b.div(w.b));}else{if(!v.a.zero()&&!w.a.zero())throw new Invalid("nonlinear");v=new Affine(v.a.mul(w.b).add(w.a.mul(v.b)),v.b.mul(w.b));}}return v;}
    Affine sum(){Affine v=product();while(peek().equals("+")||peek().equals("-")){String op=take();Affine w=product();v=new Affine(op.equals("+")?v.a.add(w.a):v.a.sub(w.a),op.equals("+")?v.b.add(w.b):v.b.sub(w.b));}return v;}
    static Affine expression(String s){SafeMath p=new SafeMath(s);Affine a=p.sum();if(p.pos!=p.tokens.size())throw new Invalid("syntax");return a;}
    public static Equation equation(String s){String[] sides=s.split("=",-1);if(sides.length!=2)throw new Invalid("equation_required");return new Equation(expression(sides[0]),expression(sides[1]));}
    public static Fraction answer(String raw){String s=raw.trim();if(s.contains("=")){String[] p=s.split("=",-1);if(p.length!=2||!p[0].trim().equalsIgnoreCase("x"))throw new Invalid("answer_form");s=p[1];}Affine a=expression(s);if(!a.a.zero())throw new Invalid("answer_form");return a.b;}
    public static final class Result {public final String status,code;public final int line;Result(String s,String c,int l){status=s;code=c;line=l;}}
    public static Result check(String original,String supplied,String steps){
        Equation initial;try{initial=equation(original);}catch(Invalid e){return new Result("content_error",e.getMessage(),0);}
        Equation prev=initial;String[] lines=steps.split("\\r?\\n");int n=0;String last="";
        for(String line:lines){if(line.trim().isEmpty())continue;if(++n>16)return new Result("unsupported","too_many_steps",n);Equation next;try{next=equation(line);}catch(Invalid e){return new Result(e.getMessage().equals("identity")||e.getMessage().equals("contradiction")?"incorrect":"unsupported",e.getMessage(),n);}if(!next.solution.eq(initial.solution)){boolean l=prev.left.a.eq(next.left.a)&&prev.left.b.eq(next.left.b),r=prev.right.a.eq(next.right.a)&&prev.right.b.eq(next.right.b);return new Result("incorrect",l!=r?"one_side":"unknown",n);}prev=next;last=line;}
        String finalAnswer=supplied.trim();if(finalAnswer.isEmpty()&&last.trim().matches("(?i)x\\s*=.*"))finalAnswer=last;
        try{return new Result(answer(finalAnswer).eq(initial.solution)?"correct":"incorrect","final",0);}catch(Invalid e){return new Result("unsupported",e.getMessage(),0);}
    }
}
