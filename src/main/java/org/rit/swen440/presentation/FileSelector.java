package org.rit.swen440.presentation;

import java.awt.*;

public class FileSelector {

    public FileSelector() {}

    public String requestFileName() {
        FileDialog fd = new FileDialog(new Frame(), "Choose a file", FileDialog.LOAD);
        fd.setDirectory("data");
        fd.setFile("*.db");
        fd.setVisible(true);
        return fd.getFile();
    }
}
