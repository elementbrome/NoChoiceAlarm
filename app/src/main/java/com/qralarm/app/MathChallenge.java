package com.qralarm.app;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

public final class MathChallenge {
    public final String expression;
    public final int answer;

    private static final SecureRandom RNG = new SecureRandom();

    private MathChallenge(String expression, int answer) {
        this.expression = expression;
        this.answer = answer;
    }

    public static MathChallenge generate(String difficulty) {
        if (AlarmModel.MATH_HARD.equals(difficulty)) return hard();
        if (AlarmModel.MATH_BRUTAL.equals(difficulty)) return brutal();
        return easy();
    }

    /** Easy: two 2-digit values, addition or subtraction. */
    private static MathChallenge easy() {
        int a = between(10, 99);
        int b = between(10, 99);
        if (RNG.nextBoolean()) return new MathChallenge(a + " + " + b, a + b);
        int hi = Math.max(a,b), lo = Math.min(a,b);
        return new MathChallenge(hi + " − " + lo, hi - lo);
    }

    /** Hard: multiplication of two 2-digit values. */
    private static MathChallenge hard() {
        int a = between(10, 99);
        int b = between(10, 99);
        return new MathChallenge(a + " × " + b, a * b);
    }

    /** Five operands, standard precedence, exact divisions and final result 1..200. */
    private static MathChallenge brutal() {
        for (int attempt=0; attempt<20_000; attempt++) {
            int[] nums = new int[5];
            for (int i=0;i<nums.length;i++) nums[i] = between(2, 30);
            char[] ops = new char[]{'+','-','*','/'};
            for (int i=ops.length-1;i>0;i--) {
                int j=RNG.nextInt(i+1);
                char tmp=ops[i]; ops[i]=ops[j]; ops[j]=tmp;
            }
            Integer value = evaluateInteger(nums, ops);
            if (value == null || value < 1 || value > 200) continue;
            StringBuilder e = new StringBuilder();
            for (int i=0;i<nums.length;i++) {
                if (i>0) {
                    char op=ops[i-1];
                    e.append(op=='*' ? " × " : op=='/' ? " ÷ " : op=='-' ? " − " : " + ");
                }
                e.append(nums[i]);
            }
            return new MathChallenge(e.toString(), value);
        }
        return new MathChallenge("18 ÷ 3 × 4 + 9 − 11", 22);
    }

    private static Integer evaluateInteger(int[] nums, char[] ops) {
        List<Integer> values = new ArrayList<>();
        List<Character> lowOps = new ArrayList<>();
        int current = nums[0];
        for (int i=0;i<ops.length;i++) {
            char op=ops[i];
            int next=nums[i+1];
            if (op=='*') {
                long v=(long)current*next;
                if (Math.abs(v)>1_000_000) return null;
                current=(int)v;
            } else if (op=='/') {
                if (next==0 || current % next != 0) return null;
                current /= next;
            } else {
                values.add(current);
                lowOps.add(op);
                current=next;
            }
        }
        values.add(current);
        int result=values.get(0);
        for (int i=0;i<lowOps.size();i++) {
            result = lowOps.get(i)=='+' ? result + values.get(i+1) : result - values.get(i+1);
        }
        return result;
    }

    private static int between(int min, int max) {
        return min + RNG.nextInt(max-min+1);
    }
}
