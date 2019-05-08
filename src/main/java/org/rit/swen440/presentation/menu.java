package org.rit.swen440.presentation;

import org.rit.swen440.dataLayer.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class menu
{

    private static final Logger LOGGER = Logger.OSLogger;

    private List<String> menuList;
    Scanner sc;
    public menu()
    {
        sc = new Scanner(System.in);
    }

    public void loadMenu(List<String> menuItems)
    {
        menuList = menuItems;
    }

    public void addMenuItem(String item)
    {
        menuList.add(item);
    }

    public void printMenu()
    {
        System.out.println("");
        for (int i = 0; i < menuList.size(); i++)
        {
            System.out.println(i+": " + menuList.get(i));
        }
        System.out.println("Press 'q' to quit.");
    }

    public String getSelection()
    {
        String result = "x";

        sc.reset();
        result = sc.nextLine();
        LOGGER.log("INFO", "Retrieved a selection: " + result);
        return result;
    }
}