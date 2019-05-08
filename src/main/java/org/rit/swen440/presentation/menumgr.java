package org.rit.swen440.presentation;

import org.rit.swen440.control.Controller;
import org.rit.swen440.dataLayer.Category;
import org.rit.swen440.dataLayer.DataLayerException;
import org.rit.swen440.dataLayer.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;


public class menumgr
{
    int currentLevel = 0;
    String currentCategoryName;
    String currentItemName;
    category currentCategory;
    item currentItem;
    private Controller controller;
    private static final Logger LOGGER = Logger.OSLogger;

    public menumgr()
    {
        LOGGER.log("INFO", "Starting the Ordering System.");
        try {
            String fileName = (new FileSelector()).requestFileName();
            if( fileName == null ) {
                throw new DataLayerException("Please open a .db file.");
            }
            controller = new Controller(fileName);
            LOGGER.log("Access", "Database has been opened.");
        } catch (DataLayerException e) {
            LOGGER.log("CRASH", "System failed to open a .db file.");
            System.out.println(e.getMessage());
            System.exit(0);
        }
    }

    public boolean loadLevel(int level)
    {
//        System.out.println("Loading level:" + currentLevel);
        switch (currentLevel)
        {
            case -1:
                return true;
            case 0:
                Level0();
                break;
            case 1:
                Level1();
                break;
            case 2:
                Level2();
                break;
            default:
                System.out.println("Returning to main org.rit.swen440.presentation.menu");
                currentLevel = 0;
                Level0();
                break;
        }

        return false;
    }

    public void Level0()
    {
        boolean broken = false;
        int iSel = -1;
        if( this.controller == null ) {
            return;
        }
        menu m = new menu();
        List<String> categories = controller.getCategories();
        m.loadMenu(categories);
        System.out.println("The following categories are available");
        m.printMenu();
        String result = "0";
        try
        {
            result = m.getSelection();
        }
        catch (Exception e) {
            result = "q";
        }
        try {
            iSel = Integer.parseInt(result);
            broken &= (iSel >= categories.size());
            broken &= (iSel < 0);
        } catch (NumberFormatException e) {
            broken = false;
        }
        if (broken) {
            LOGGER.log("INFO",  "User entered an invalid category selection. Selection: " + result);
            System.out.println("Please select a valid option.");
        } else {
            if (result.equals("q")) {
                currentLevel--;
            } else {
                currentLevel++;

                currentCategoryName = categories.get(iSel);
                System.out.println("\nYour Selection was:" + currentCategoryName);
            }
        }
    }

    public void Level1()
    {

        if (this.controller == null) {
            return;
        }
        menu m = new menu();

        //items it = new items("orderSys/" + currentCategory.getName());

        // List<item> itemList = controller.getProducts(currentCategoryName);
        List<String> itemList = controller.getProducts(currentCategoryName);
        List<String> l = new ArrayList<>();
        System.out.println("");
        for (String itm: itemList)
            l.add(controller.getProductInformation(currentCategoryName, itm, Controller.PRODUCT_FIELD.NAME)
             + "($" + controller.getProductInformation(currentCategoryName, itm, Controller.PRODUCT_FIELD.COST) + ")");
        
        m.loadMenu(l);
        System.out.println("The following items are available");
        m.printMenu();
        String result = m.getSelection();
        try
        {
            int iSel = Integer.parseInt(result);//Item  selected
            currentItemName = itemList.get(iSel);
            //currentItem = itemList.get(iSel);
            //Now read the file and print the org.rit.swen440.presentation.items in the catalog
            System.out.println("You want item from the catalog: " + currentItemName);
        }
        catch (Exception e)
        {
            result = "q";
        }
        if (result == "q")
            currentLevel--;
        else
        {
            //currentLevel++;//Or keep at same level?
            OrderQty(currentCategoryName, currentItemName);
        }
    }


    public void Level2()
    {

    }

    public void OrderQty(String category, String item)
    {
        int iSel = -1;
        if( this.controller == null ) {
            return;
        }
        System.out.println("Please select a quantity");
        System.out.println(controller.getProductInformation(category, item, Controller.PRODUCT_FIELD.NAME) +
                " availability:" + controller.getProductInformation(category, item, Controller.PRODUCT_FIELD.INVENTORY));
        System.out.print(">");
        menu m = new menu();
        String result = m.getSelection();
        try {
            iSel = Integer.parseInt(result);
        } catch (NumberFormatException e){
            LOGGER.log("INFO", "User entered invalid option for order quantity. Input: " + result);
        }

        System.out.println("ISEL " + Integer.toString(iSel));
        boolean victory = (iSel > 0);
        victory &= controller.tryOrder(category, item, iSel);

        if (victory) {
            System.out.println("You ordered: " + result);
        } else {
            System.out.println("Order failed.");
        }
    }
}