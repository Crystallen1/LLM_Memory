package org.crystallen.lc.util;

import java.util.List;

class Count extends Thread {
    static int n = 0;
    @Override
    public void run() {
        int temp;
        for (int i = 0; i < 1000; i++) {
            synchronized (Count.class) {
                temp = n;
                n = temp + 1;
            }
//             try {
//             sleep(1);
//             } catch (InterruptedException e) {
//
//             }
        }
    }
    public static void main(String[] args) {
        Count p = new Count();
        Count q = new Count();
        p.start();
        q.start();
        try { p.join(); q.join(); }
        catch (InterruptedException e) { }
        System.out.println("The final value of n is " + n);
    }
}
