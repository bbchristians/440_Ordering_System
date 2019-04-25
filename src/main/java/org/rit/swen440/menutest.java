package org.rit.swen440;

import org.rit.swen440.presentation.menumgr;


public class menutest
{
    public static void main(String[] args)
    {
        menumgr mgr = new menumgr();
        boolean done;
        do {
            done = mgr.loadLevel(0);
        } while (!done);

        System.out.println("Thank you for shopping at Hippolyta.com!");
    }
}