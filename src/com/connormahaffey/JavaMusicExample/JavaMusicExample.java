package com.connormahaffey.JavaMusicExample;

import java.util.Scanner;

/**
 *
 * @author Connor Mahaffey
 */
public class JavaMusicExample {

    /**
     * See the Music Class for details.
     *
     * This is a simple program to show off the capabilities of the Music class.
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        String input;
        String filePath = getPathFromArgs(args);
        Scanner in = new Scanner(System.in);
        Music MU = new Music();

        if(filePath == null){
            System.out.println("No path to a .mp3 in arguements.");
            loadMP3(MU);
        }
        else{
            if(!MU.loadFile(filePath)){
                System.err.println("Invalid path!");
                loadMP3(MU);
            }
        }

        do{
            System.out.println("1. Play");
            System.out.println("2. Pause");
            System.out.println("3. Mute");
            System.out.println("4. Restart");
            System.out.println("5. Stop");
            System.out.println("6. Load");
            System.out.println("7. Loop");
            System.out.println("8. Exit");
            System.out.print("> ");
            input = in.nextLine();

            if(input.equals("1")){
                MU.play();
            }
            else if(input.equals("2")){
                MU.pause();
            }
            else if(input.equals("3")){
                MU.mute();
            }
            else if(input.equals("4")){
                MU.restart();
            }
            else if(input.equals("5")){
                MU.stop();
            }
            else if(input.equals("6")){
                MU.stop();
                loadMP3(MU);
            }
            else if(input.equals("7")){
                MU.loop();
            }
            else if(input.equals("8")){
                MU.stop();
            }
            else{
                System.err.println("Invalid entry.");
            }
        }while(!input.equals("8"));
        
        System.exit(0);
        
    }
    /**
     * Gets a file path from the program's arguements.
     * @param args Arguements.
     * @return The path.
     */
    private static String getPathFromArgs(String[] args){
        String s = null;

        try{
            s = args[0];
        }catch(Exception e){
            //no arguements
        }

        return s;
    }
    /**
     * Gets user command-line input and tries to load a MP3 from the path given.
     * @param MU Music Object Reference
     */
    private static void loadMP3(Music MU){
        boolean validPath;
        String filePath;
        Scanner in = new Scanner(System.in);
        
        do{
            System.out.print("Path to .mp3: ");
            filePath = in.nextLine();
            validPath = MU.loadFile(filePath);
            if(!validPath){
                System.out.println("Invalid path!");
            }
        }while(!validPath);
    }

}
