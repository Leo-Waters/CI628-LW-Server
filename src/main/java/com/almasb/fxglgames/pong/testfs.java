package com.almasb.fxglgames.pong;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class testfs {
    //file system test for save data
    public static void main(String[] args) throws IOException {
        var Path = Paths.get("Test.txt");
        List<String> Lines= new ArrayList<>();
        Lines.add("0");
        Lines.add("2");
        Files.write(Path,Lines);

        Lines= new ArrayList<>();

        Lines= Files.readAllLines(Path);

        System.out.println(Lines);

    }
}
