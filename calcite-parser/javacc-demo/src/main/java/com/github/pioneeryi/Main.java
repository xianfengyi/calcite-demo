package com.github.pioneeryi;

import com.github.pioneeryi.gen.Calculator;

public class Main {
    public static void main(String[] args)throws Exception {
        Calculator parser = new Calculator(System.in);
        parser.Start(System.out);
    }
}