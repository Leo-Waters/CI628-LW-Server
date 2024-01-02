package com.almasb.fxglgames.pong;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;


public class Level {
    public int Width,Height;
    public int[][] LevelData;
    Level(int[][] Data ,int _Width,int _Height){
        LevelData=Data;
        Width=_Width;
        Height=_Height;
    }
    Level(String File){
        LoadFromFile(File);
    }

    private void LoadFromFile(String File) {

        //open level file
        File LevelFile = new File("src/main/resources/assets/Levels/"+File+".txt");
        Scanner Reader = null;
        try {
            Reader = new Scanner(LevelFile);
        } catch (FileNotFoundException e) {
            System.out.println("Level File does not Exist:"+e.getMessage());
            throw new RuntimeException(e);
        }

        if(Reader.hasNextLine()){//load width and height
            var size=Reader.nextLine().split(",");
            Width= Integer.parseInt(size[0]);
            Height=Integer.parseInt(size[1]);
            System.out.println("Level File Size W"+Width+" H"+Height);
        }else {
        System.out.println("Level File does not contain data");
        }
        //initialize level data array
        LevelData=new int[Height][Width];

        int y=0;
        while (Reader.hasNextLine()){
            //split comma seperated values
            var size=Reader.nextLine().replace(" ","").split(",");
            for (var x=0; x<Width; x++){
                System.out.println("y"+y+" data"+size[x]);
                //store loaded level data
                LevelData[y][x]=Integer.parseInt(size[x]);
            }
            y++;
        }
        Reader.close();

    }
}
