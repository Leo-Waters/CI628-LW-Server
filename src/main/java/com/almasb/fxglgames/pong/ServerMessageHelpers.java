package com.almasb.fxglgames.pong;

import java.util.*;


public class ServerMessageHelpers {



    public static List<ServerCommand> GetCommands(String message){

        List<ServerCommand> commands = new ArrayList<>();

        String[] CommandStrings=message.split("\\|");

        for (String command : CommandStrings) {
            List<String> Args = new ArrayList<String>();
            String Name = "";
            if (command.contains((","))) {

                var args = command.split(",");
                Name = args[0];
                for (int a = 1; a < args.length; a++) {
                    Args.add(args[a]);
                }
                commands.add(new ServerCommand(Name, Args));
            }else {
                commands.add(new ServerCommand(command, Args));
            }

        }
        return commands;
    }
    public static String CompressString(String str) {
        //step 1 Shorten duplicate repetitive args such as 1,1,1,1
        //step 2 identify duplicate non-repetitive args
        //step 3 create key
        //step 4 create new string replacing args with key
        //keys value added to beginning of string

        StringBuilder CompressedString= new StringBuilder();

        Map<String, Integer> ArgumentAnalysis = new HashMap<String, Integer>();

        List<ServerCommand> Commands= GetCommands(str);



        for(ServerCommand cmd : Commands){//loop through all commands
            if(cmd.Command.isEmpty()){
                continue;
            }
            //shorten duplicate repetitive args such as 1,1,1,1,1,1,1,1----------------------------
            List<String> ShrunkArgs = new ArrayList<>();
            if(!cmd.Args.isEmpty()) {//does the command have arguments

                String LastArg = cmd.Args.get(0);//first argument
                int Count = 1;//how many of thes arguments have appeared sequncialy 1,1,1,1,
                for (int i = 1; i < cmd.Args.size(); i++) {

                    //is this the same as the last arg
                    if (cmd.Args.get(i).equals(LastArg)) {
                        Count++;//new repetitive duplicate found
                    }
                    //not the same
                    else {
                        if (Count > 1) {//has duplicates add short form
                            ShrunkArgs.add('"' + LastArg + '"' + "x" + Count);
                            Count = 1;
                        } else {//no duplicates
                            ShrunkArgs.add(LastArg);//add normal arg back to list as not duplicate
                            Count = 1;
                        }
                        LastArg = cmd.Args.get(i);
                    }
                }

                //add remaining data

                if (Count > 1) {//has duplicates add short form
                    ShrunkArgs.add('"' + LastArg + '"' + "x" + Count);
                } else {//no duplicates
                    ShrunkArgs.add(LastArg);
                }
            }

            //add command name to analysis
            if (ArgumentAnalysis.containsKey(cmd.Command)) {
                ArgumentAnalysis.put(cmd.Command, ArgumentAnalysis.get(cmd.Command) + 1);//increase duplicate counter
            } else {
                ArgumentAnalysis.put(cmd.Command, 1);//initalize duplicate counter

            }

            //analise duplicate args
            for (String key : ShrunkArgs) {
                if(key.length()>2) {//dont add keys shorter than 3 as can use more bytes
                    if (ArgumentAnalysis.containsKey(key)) {
                        ArgumentAnalysis.put(key, ArgumentAnalysis.get(key) + 1);//increase duplicate counter
                    } else {
                        ArgumentAnalysis.put(key, 1);//initalize duplicate counter

                    }
                }
            }
            cmd.Args=ShrunkArgs;//set args to short form args



            //-------------------------------------------------------------------------------------------------
        }





        int keyid=0;
        //key and ID
        Map<String, Integer> KeyIDS = new HashMap<String, Integer>();
        //Create final Compressed string
        for(ServerCommand cmd : Commands) {

            //if analysis counted more than 2 of these cmd names create a key
            if (ArgumentAnalysis.containsKey(cmd.Command)&&ArgumentAnalysis.get(cmd.Command)>2) {
                if(KeyIDS.containsKey(cmd.Command)){
                    cmd.Command= "*" + KeyIDS.get(cmd.Command);//replace cmd string with * and ID
                }else{
                    KeyIDS.put(cmd.Command,keyid);
                    cmd.Command= "*" + cmd.Command;// first command Add * to beginning of command name so that decompressor knows the key to replace others
                    keyid++;
                }
            }

            for (int a = 0; a < cmd.Args.size(); a++) {
                if(cmd.Command.isEmpty()){
                    continue;
                }
                //if analysis counted more than 2 of these cmd names create a key
                if (ArgumentAnalysis.containsKey(cmd.Args.get(a))&&ArgumentAnalysis.get(cmd.Args.get(a))>2) {

                    if(KeyIDS.containsKey(cmd.Args.get(a))){
                        cmd.Args.set(a,"*" + KeyIDS.get(cmd.Args.get(a)));//replace cmd string with * and ID
                    }else{
                        KeyIDS.put(cmd.Args.get(a),keyid);
                        cmd.Args.set(a,"*" + cmd.Args.get(a));// first command Add * to beginning of command name so that decompressor knows the key to replace others
                        keyid++;
                    }
                }
            }

            //convert compressed data into string form again
            CompressedString.append(cmd.Command);//add command name

            if(!cmd.Args.isEmpty()){//has args
                CompressedString.append(",");
                for (String shrunkArg : cmd.Args) {
                    CompressedString.append(shrunkArg);//add arg to string
                    CompressedString.append(",");
                }
            }
            //signifies end of command
            CompressedString.append("|");
        }

        return CompressedString.toString();

    }


    public static String DecompressString(String str){
        StringBuilder Data=new StringBuilder();

        var Commands =GetCommands(str);

        int Index=0;
        Map<String, String> Keys = new HashMap<String, String>();

        //-----------------------------------------------Identify Keys----------------
        for(ServerCommand cmd : Commands){
            if(cmd.Command.isEmpty()){
                continue;
            }
            if(cmd.Command.charAt(0)=='*'){//found command Key
                Keys.put("*"+Index,cmd.Command.substring(1));//add Command key
                Index++;//Increase Index for next find
            }

            for (int i = 0; i < cmd.Args.size(); i++) {
                if(cmd.Args.get(i).isEmpty()){
                    continue;
                }
                if(cmd.Args.get(i).charAt(0)=='*'){//found command Key
                    Keys.put("*"+Index,cmd.Args.get(i).substring(1));//add Command key
                    Index++;//Increase Index for next find
                }
            }
        }
        //------------------------------------------------------------------------------------------------

        //Convert Data back to full

        return  Data.toString();
    }
}
