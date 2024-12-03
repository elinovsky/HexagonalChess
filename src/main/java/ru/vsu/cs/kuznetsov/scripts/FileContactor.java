package ru.vsu.cs.kuznetsov.scripts;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.net.URL;
import java.nio.file.Paths;
import java.util.List;

public class FileContactor {
    public static Image readImageForFigure(String imgName) throws Exception{
        String path = Paths.get("assets",
                imgName).toAbsolutePath().toString();
        return ImageIO.read(new File(path));
    }

//    public static void saveBoardConfig(String filePath, List<Figure> boardFigures) throws Exception{
//        StringBuilder fileContaining = new StringBuilder();
//        for (Figure fig : boardFigures){
//            fileContaining.append()
//        }
//    }
}
