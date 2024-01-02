package com.almasb.fxglgames.pong;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static java.awt.SystemColor.text;

public class ScoreSystem {
    public  static  int HighLevel,HighKills;
    //load previous scores
    static  void LoadHighScore(){
        var ScorePath = Paths.get("ScoreSaves.txt");
        List<String> Scores= new ArrayList<>();
        if (Files.exists(ScorePath)) {//load old score
            try {
                Scores= Files.readAllLines(ScorePath);
                System.out.println("Loaded High Level"+ Scores.get(0) +" Kills "+ Scores.get(1));
                HighLevel=Integer.parseInt(Scores.get(0));
                HighKills=Integer.parseInt(Scores.get(1));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        }else{
            // no previous high score, initialize values
            HighLevel=HighKills=0;
            SaveNewHighScore(0,0);
        }
    }

    //save a score if it's higher than the previous high score
    static  void SaveNewHighScore(int level, int CombinedKills){
        //is higher than old
        if(level>HighLevel||(level==HighLevel&&CombinedKills>HighKills)){
            //set new high score
            HighLevel=level;
            HighKills=CombinedKills;
            //file path
            var ScorePath = Paths.get("ScoreSaves.txt");
            //Array of lines representing different variables
            List<String> ScoreInfo= new ArrayList<>();
            ScoreInfo.add(String.valueOf(level));
            ScoreInfo.add(String.valueOf(CombinedKills));
            //try to write variables to file
            try {
                Files.write(ScorePath,ScoreInfo);
            } catch (IOException e) {
                System.out.println("Failed to save scores");
                throw new RuntimeException(e);
            }
        }


    }

}
