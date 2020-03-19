package calculator;

import javax.naming.InsufficientResourcesException;
import java.util.*;
import java.math.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {


    public static boolean isNumber(Character c) {
        return c >= '0' && c <= '9';
    }

    public static boolean isOperator(Character c) {
        return c == '-' || c == '+' || c == '*' || c == '/' || c == '^';
    }

    public static boolean isAlpha(Character c) {
        return (c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z');
    }

    public static int precedence (Character c) {
        if (c == '-' || c == '+') {
            return 0;
        } else if (c == '*' || c == '/') {
            return 1;
        } else if (c == '^') {
            return 2;
        } else {
            return -1;
        }
    }

    public static BigInteger doOpt (Character ch, BigInteger a, BigInteger b) {
        switch (ch) {
            case '-':
                return b.subtract(a);
            case '+':
                return b.add(a);
            case '*':
                return b.multiply(a);
            case '/':
                 if (a.equals(BigInteger.ZERO)) {
                     throw new ArithmeticException("Division by zero!");
                 } else {
                     return b.divide(a);
                 }
            case '^':
                return b.pow(Integer.parseInt(a.toString()));
        }
        return new BigInteger("-1");
    }

    public static Character retChar(String str, int i) {
        int sign = 0;

        if (str.charAt(i) == '-' || str.charAt(i) == '+') {
            while (str.charAt(i) == '-' || str.charAt(i) == '+') {
                if (str.charAt(i) == '-') {
                    sign++;
                }
                i++;
            }
            return (sign % 2 == 1) ? '-' : '+';
        } else {
            if (i + 1 != str.length()) {
                if (str.charAt(i + 1) == ' ' || isNumber(str.charAt(i + 1)) || str.charAt(i + 1) == '(' || str.charAt(i + 1) == ')' || str.charAt(i + 1) == '-') {
                    return str.charAt(i);
                } else {
                    throw new ArithmeticException("Invalid expression");
                }
            }
            return str.charAt(i);
        }
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        String str;
        String[] arrStr;
        BigInteger a;
        int res;
        boolean minus;
        boolean invalid;
        Map<String, BigInteger> var = new HashMap<String, BigInteger>();
        Deque<Character> stackOperators = new ArrayDeque<>();
        Deque<BigInteger> stackOperands = new ArrayDeque<>();


        do {
            invalid = false;
            str = scanner.nextLine();
            if (str.equals("/help")) {
                System.out.println("Hello! I'm calculator with braces:)\nYou can use +-*/^.. and even variables!!!\nE.g.: a=10 and then a+10");
            } else if (!str.isEmpty() && !str.equals("/exit")) {
                if (str.charAt(0) == '/') {
                    System.out.println("Unknown command");
                    continue;
                }

                if (str.matches(".*=.*")) {
                    String expr;
                    Pattern p = Pattern.compile("\\s+");
                    Matcher m = p.matcher(str);
                    expr = m.replaceAll("");
                    arrStr = expr.split("=");
                    if (arrStr.length > 2) {
                        System.out.println("Invalid assignment");
                        continue;
                    }
                    if (arrStr.length == 1) {
                        if (var.containsKey(arrStr[0])) {
                            System.out.println(var.get(arrStr[0]));
                        } else {
                            System.out.println("Unknown variable");
                        }
                        continue;
                    }
                    if (!arrStr[0].matches("[a-zA-Z]+")) {
                        System.out.println("Invalid identifier");
                        continue;
                    }
                    if (!arrStr[1].matches("[a-zA-Z]+")) {
                        try {
                            a = new BigInteger(arrStr[1]);
                        } catch (NumberFormatException e) {
                            System.out.println("Invalid assignment");
                            continue;
                        }
                    } else {
                        if (var.containsKey(arrStr[1])) {
                            a = var.get(arrStr[1]);
                        } else {
                            System.out.println("Unknown variable");
                            continue;
                        }
                    }
                    var.put(arrStr[0], a);
                    continue;
                }
                int i = 0;
                int start;
                int end;
                Character oper;
                while (i < str.length() && !invalid) {
                    if ((str.charAt(i) == '-' && isNumber(str.charAt(i + 1)))|| isNumber(str.charAt(i)) || isAlpha(str.charAt(i))) {
                        start = i;
                        end = start;
                        if (str.charAt(i) == '-') {
                            end++;
                        }
                        if (isNumber(str.charAt(end))) {
                            while (end < str.length() && isNumber(str.charAt(end))) {
                                end++;
                            }
                            stackOperands.offerLast(new BigInteger(str.substring(start, end)));
                        } else {
                            while (end < str.length() && isAlpha(str.charAt(end))) {
                                end++;
                            }
                            String v = str.substring(start, end);
                            if (var.containsKey(v)) {
                                stackOperands.offerLast(var.get(v));
                            } else {
                                System.out.println("Unknown variable");
                                invalid = true;
                                continue;
                            }
                        }
                        i = end - 1;
                    } else if (isOperator(str.charAt(i)) || str.charAt(i) == '(' || str.charAt(i) == ')') {
                        try {
                            oper = retChar(str, i);
                            while (str.charAt(i) == '+' || str.charAt(i) == '-') {
                                i++;
                            }
                            if (i - 1 > 0 && (str.charAt(i - 1) == '+' || str.charAt(i - 1) == '-')) {
                                i--;
                            }
                        } catch (ArithmeticException e) {
                            invalid = true;
                            continue;
                        }
                        Character ch;
                        if (oper == ')') {
                            if (stackOperands.isEmpty()) {
                                invalid = true;
                                continue;
                            }
                            ch = stackOperators.pollLast();
                            while (!stackOperators.isEmpty() && ch != '(') {
                                try {
                                    stackOperands.offerLast(doOpt(ch, stackOperands.pollLast(), stackOperands.pollLast()));
                                } catch (ArithmeticException e) {
                                    System.out.println(e.getLocalizedMessage());
                                    invalid = true;
                                    continue;
                                }
                                ch = stackOperators.pollLast();
                            }
                        } else if (oper == '(' || stackOperators.isEmpty()) {
                            stackOperators.offerLast(oper);
                        } else {
                            ch = stackOperators.peekLast();
                            if (ch != '(') {
                                if (precedence(oper) <= precedence(ch)) {
                                    while (!stackOperators.isEmpty() && !stackOperands.isEmpty()
                                            && (ch != '(' || precedence(ch) >= precedence(oper)) && !invalid) {
                                        ch = stackOperators.pollLast();
                                        try {
                                            stackOperands.offerLast(doOpt(ch, stackOperands.pollLast(), stackOperands.pollLast()));
                                        } catch (ArithmeticException e) {
                                            System.out.println(e.getLocalizedMessage());
                                            invalid = true;
                                        }
                                        ch = stackOperators.peekLast();
                                    }
                                }
                            }
                            stackOperators.offerLast(oper);
                        }
                    } else if (str.charAt(i) != ' ') {
                        invalid = true;
                        continue;
                    }
                    i++;
                }
                while (!stackOperators.isEmpty() && !stackOperands.isEmpty() && !invalid) {
                    try {
                        BigInteger x = stackOperands.pollLast();
                        if (stackOperands.isEmpty()) {
                            invalid = true;
                            break;
                        }
                        stackOperands.offerLast(doOpt(stackOperators.pollLast(), x, stackOperands.pollLast()));
                    } catch (ArithmeticException e) {
                        System.out.println(e.getMessage());
                        invalid = true;
                    }
                }
                if (stackOperands.size() > 1 || invalid) {
                    System.out.println("Invalid expression");
                } else {
                    System.out.println(stackOperands.pollLast());
                }
                stackOperands.clear();
                stackOperators.clear();
            }
        } while (!str.equals("/exit"));
        System.out.println("Bye!");
    }
}
