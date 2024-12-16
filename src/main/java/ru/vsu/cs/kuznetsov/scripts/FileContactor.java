package ru.vsu.cs.kuznetsov.scripts;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.file.Paths;
import java.util.Scanner;

public class FileContactor {
    public static Image readImage(String imgName) throws Exception{
        String path = Paths.get("assets",
                imgName).toAbsolutePath().toString();
        return ImageIO.read(new File(path));
    }

    public static void saveNotation(String filePath, String notation) throws Exception{
        FileWriter fw = new FileWriter(filePath);
        fw.write(notation);
        fw.close();
    }

    public static String readGameConfiguration(String filePath) throws Exception{
        Scanner reader = new Scanner(new File(filePath));
        StringBuilder res = new StringBuilder();
        while (reader.hasNextLine()){
            res.append(reader.nextLine());
        }
        reader.close();
        return res.toString();
    }
}
