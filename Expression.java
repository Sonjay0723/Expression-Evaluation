package app;

import java.io.*;
import java.util.*;
import java.util.regex.*;

import structures.Stack;

public class Expression {

	public static String delims = " \t*+-/()[]";
			
    /**
     * Populates the vars list with simple variables, and arrays lists with arrays
     * in the expression. For every variable (simple or array), a SINGLE instance is created 
     * and stored, even if it appears more than once in the expression.
     * At this time, values for all variables and all array items are set to
     * zero - they will be loaded from a file in the loadVariableValues method.
     * 
     * @param expr The expression
     * @param vars The variables array list - already created by the caller
     * @param arrays The arrays array list - already created by the caller
     */
    public static void 
    makeVariableLists(String expr, ArrayList<Variable> vars, ArrayList<Array> arrays) {
    	/** COMPLETE THIS METHOD **/
    	/** DO NOT create new vars and arrays - they are already created before being sent in
    	 ** to this method - you just need to fill them in.
    	 **/
    	String temp = "";
    	
    	A: for (int i = 0; i < expr.length(); i++) {
    		if (Character.isLetter(expr.charAt(i)))
    			temp += expr.charAt(i);
    		else if (temp.length() > 0){
    			if (expr.charAt(i) == '[') {
    				for (int iArrays = 0; iArrays < arrays.size(); iArrays++) {
    					if (temp.equals(arrays.get(iArrays).name)) {
    						temp = "";
    						continue A;
    					}
    				}
    				arrays.add(new Array(temp));
    				temp = "";
    			}
    			else {
    				for (int iVars = 0; iVars < vars.size(); iVars++) {
    					if (temp.equals(vars.get(iVars).name)) {
    						temp = "";
    						continue A;
    					}
    				}
    				vars.add(new Variable(temp));
    				temp = "";
    			}
    		}
    	}
    	
    	for (int iVars = 0; iVars < vars.size(); iVars++) {
			if (temp.equals(vars.get(iVars).name)) {
				temp = "";
			}
		}
    	if (temp.length() > 0) {
    		vars.add(new Variable(temp));
    	}
    	
    	System.out.println(vars.toString());
    	System.out.println(arrays.toString());
    }
    
    /**
     * Loads values for variables and arrays in the expression
     * 
     * @param sc Scanner for values input
     * @throws IOException If there is a problem with the input 
     * @param vars The variables array list, previously populated by makeVariableLists
     * @param arrays The arrays array list - previously populated by makeVariableLists
     */
    public static void 
    loadVariableValues(Scanner sc, ArrayList<Variable> vars, ArrayList<Array> arrays) 
    throws IOException {
        while (sc.hasNextLine()) {
            StringTokenizer st = new StringTokenizer(sc.nextLine().trim());
            int numTokens = st.countTokens();
            String tok = st.nextToken();
            Variable var = new Variable(tok);
            Array arr = new Array(tok);
            int vari = vars.indexOf(var);
            int arri = arrays.indexOf(arr);
            if (vari == -1 && arri == -1) {
            	continue;
            }
            int num = Integer.parseInt(st.nextToken());
            if (numTokens == 2) { // scalar symbol
                vars.get(vari).value = num;
            } else { // array symbol
            	arr = arrays.get(arri);
            	arr.values = new int[num];
                // following are (index,val) pairs
                while (st.hasMoreTokens()) {
                    tok = st.nextToken();
                    StringTokenizer stt = new StringTokenizer(tok," (,)");
                    int index = Integer.parseInt(stt.nextToken());
                    int val = Integer.parseInt(stt.nextToken());
                    arr.values[index] = val;              
                }
            }
        }
    }
    
    /**
     * Evaluates the expression.
     * 
     * @param vars The variables array list, with values for all variables in the expression
     * @param arrays The arrays array list, with values for all array items
     * @return Result of evaluation
     */
    public static float evaluate(String expr, ArrayList<Variable> vars, ArrayList<Array> arrays) {
    	/** COMPLETE THIS METHOD **/
    	// following line just a placeholder for compilation
    	//get rid of spaces
    	for (int i = 0; i < expr.length(); i++) {
    		if (expr.charAt(i) == ' ')
    			expr = expr.substring(0, i) + expr.substring(i + 1);
    	}
    	
    	try {
    		return Float.parseFloat(expr);
    	} catch (NumberFormatException e) {}
    	
    	for (int i = 0; i < vars.size(); i++) {
    		while (expr.contains(vars.get(i).name)) {
    			int index = expr.indexOf(vars.get(i).name);
    			
    			expr = expr.substring(0, index) + vars.get(i).value + expr.substring(index + vars.get(i).name.length());
    		}
    	}
    	
    	Stack<Integer> openPare = new Stack<Integer>();
    	Stack<Integer> openBrack = new Stack<Integer>();
    	Stack<Integer> closedBrack = new Stack<Integer>();
    	
    	for (int i = 0; i < expr.length(); i++) {
    		if (expr.charAt(i) == '(')
    			openPare.push(i);
    	}
    	
    	while (!openPare.isEmpty()) {
    		for (int i = 0; i < expr.length(); i++) {
    			if (expr.charAt(i) == ')' && i > openPare.peek()) {
    				System.out.println(expr.substring(openPare.peek() + 1, i));
    				float res = evaluate(expr.substring(openPare.peek() + 1, i), vars, arrays);
    				
    				expr = expr.substring(0, openPare.pop()) + res + expr.substring(i + 1);
    				break;
    			}
    		}
    	}
    	
    	String changeVars = expr;
    	for (int i = 0; i < expr.length(); i++) {
    		//vars
    		if (Character.isLetter(expr.charAt(i))) {
    		for (int iVars = 0; iVars < vars.size(); iVars++) {
    			if (i + vars.get(iVars).name.length() <= expr.length()) {
    				if (expr.substring(i, i + vars.get(iVars).name.length()).equals(vars.get(iVars).name)) {
        				float res = vars.get(iVars).value;
        				
        				changeVars = changeVars.substring(0, i) + res + expr.substring(i + vars.get(iVars).name.length());
        				i += vars.get(iVars).name.length();
        				break;
        			}
    			}
    		}
    		}
    	}
    	expr = changeVars;
    	
    	for (int i = 0; i < expr.length(); i++) {
    		if (expr.charAt(i) == '[')
    			openBrack.push(i);
    	}
    	//System.out.println("peek: " + openBrack.peek());
    	
    	for (int i = 0; i < expr.length(); i++) {
    		if (expr.charAt(i) == ']')
    			closedBrack.push(i);
    	}
    	
    	System.out.println("exprBegin: " + expr);
    	while (!openBrack.isEmpty()) {
    		for (int i = 0; i < changeVars.length(); i++) {
    			if (changeVars.charAt(i) == '[')
    				break;
    			else if (i == changeVars.length() - 1)
    				openBrack.pop();
    		}
    		
    		System.out.println("exprBegin-: " + expr);
    		System.out.println("changeVarsBigin: " + changeVars);
    		A: for (int i = 0; i < changeVars.length(); i++) {
    			if (changeVars.charAt(i) == ']' && i > openBrack.peek()) {
    				int open = openBrack.pop();
    				System.out.println("me: " + changeVars.substring(open + 1, i));
    				float res = evaluate(changeVars.substring(open + 1, i), vars, arrays);
    				System.out.println("res: " + res);
    				for (int iArrays = 0; iArrays < arrays.size(); iArrays++) {
    					//arrays
    		    		if (open - arrays.get(iArrays).name.length() >= 0) {
    		    			if (changeVars.substring(open - arrays.get(iArrays).name.length(), open).equals(arrays.get(iArrays).name)) {
    		       				System.out.println("changeVars: " + changeVars);
    		       				System.out.println("arrays value: " + arrays.get(iArrays).values[(int)res]);
    		       				changeVars = changeVars.substring(0, open - arrays.get(iArrays).name.length()) + arrays.get(iArrays).values[(int)res] + changeVars.substring(i + 1);
    		       				if (!closedBrack.isEmpty())
    		       					closedBrack.pop();
    		    				break A;
   		        			}
   		    			}
    				}
    		   	}
    		}
    	}
    	expr = changeVars;
    	
    	//substituting vars and arrays
    	/*String changeVars = expr;
    	for (int i = 0; i < expr.length(); i++) {
    		//vars
    		for (int iVars = 0; iVars < vars.size(); iVars++) {
    			if (i + vars.get(iVars).name.length() <= expr.length()) {
    				if (expr.substring(i, i + vars.get(iVars).name.length()).equals(vars.get(iVars).name)) {
        				float res = vars.get(iVars).value;
        				
        				changeVars = changeVars.substring(0, i) + res + expr.substring(i + vars.get(iVars).name.length());
        				i += vars.get(iVars).name.length();
        				break;
        			}
    			}
    		}
    	}
    	expr = changeVars;*/
    	
    	/*for (int i = 0; i < expr.length(); i++) {
    		//arrays
    		A: for (int iArrays = 0; iArrays < arrays.size(); iArrays++) {
    			if (i + arrays.get(iArrays).name.length() <= expr.length()) {
    				if (expr.substring(i, i + arrays.get(iArrays).name.length()).equals(arrays.get(iArrays).name)) {
        				for (int closed = i + arrays.get(iArrays).name.length() + 1; closed < expr.length(); closed++) {
        					if (expr.charAt(closed) == ']' || closed == expr.length() - 1) {
        						System.out.println("fail-");
        						float res = Float.parseFloat(expr.substring(i + arrays.get(iArrays).name.length() + 1, closed));
        						System.out.println("fail");
        						changeVars = changeVars.substring(0, i) + arrays.get(iArrays).values[(int)res] + expr.substring(closed + 1);
        						i = closed + 1;
        						break A;
        					}
        				}
        			}
    			}
    		}
    	}
    	expr = changeVars;*/

    	String original = expr;
    	
    	for (int i = 1; i < expr.length(); i++) {
    		if (expr.charAt(i) == '*' || expr.charAt(i) == '/') {
    			System.out.println("*/");
    			int left = 0, right = 0;
    			float temp1 = 0, temp2 = 0, result;
    			
    			for (int iLeft = i - 1; iLeft >= 0; iLeft--) {
    				try {
    					temp1 = Float.parseFloat(expr.substring(iLeft, i));
    					if (expr.charAt(iLeft) == '+')
    						break;
    					left = iLeft;
    				} catch (NumberFormatException e) {
    					break;
    				}
    			}
    			
    			for (int iRight = i + 2; iRight <= expr.length(); iRight++) {
    				if (!expr.substring(i + 1, iRight).equals("-")) {
    					try {
    						temp2 = Float.parseFloat(expr.substring(i + 1, iRight));
    						right = iRight;
    					} catch (NumberFormatException e) {
    						break;
    					}
    				}
    			}
    			
    			if (expr.charAt(i) == '*')
    				result = temp1 * temp2;
    			else
    				result = temp1 / temp2;
    			
    			original = expr.substring(0, left) + result + expr.substring(right);
    			
    			try {
    				float res = Float.parseFloat(original);
    				return res;
    			} catch (NumberFormatException e) {
    				System.out.println("original*: " + original);
    				System.out.println("expr*: " + expr);
    				return evaluate(original, vars, arrays);
    				//break;
    			}
    		}
    	}
    		
    	for (int i = 1; i < expr.length(); i++) {
    		if (expr.charAt(i) == '+' || expr.charAt(i) == '-') {
    			System.out.println("+-");
    			int left = 0, right = 0;
    			float temp1 = 0, temp2 = 0, result;
    			
    			for (int iLeft = i - 1; iLeft >= 0; iLeft--) {
    				try {
    					temp1 = Float.parseFloat(expr.substring(iLeft, i));
    					left = iLeft;
    				} catch (NumberFormatException e) {
    					break;
    				}
    			}
    		
    			for (int iRight = i + 2; iRight <= expr.length(); iRight++) {
    				if (!expr.substring(i + 1, iRight).equals("-")) {
    					try {
    						temp2 = Float.parseFloat(expr.substring(i + 1, iRight));
    						right = iRight;
    					} catch (NumberFormatException e) {
    						break;
    					}
    				}
    			}
    			
    			if (expr.charAt(i) == '+')
    				result = temp1 + temp2;
    			else
    				result = temp1 - temp2;
    			
    			original = expr.substring(0, left) + result + expr.substring(right);
    			
    			try {
    				float res = Float.parseFloat(original);
    				return res;
    			} catch (NumberFormatException e) {
    				System.out.println("original+: " + original);
    				System.out.println("expr+: " + expr);
    				return evaluate(original, vars, arrays);
    				//break;
    			}
    		}
    	}
    	
    	System.out.println("exprEnd: " + expr);
    	System.out.println("ogEnd: " + original);
    	
    	return evaluate(original, vars, arrays);
    }
}