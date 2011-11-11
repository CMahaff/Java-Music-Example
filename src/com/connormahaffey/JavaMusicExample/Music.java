package com.connormahaffey.JavaMusicExample;

import java.io.File;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.SourceDataLine;

/**
 *
 * @author Connor Mahaffey
 *
 * This class is a rewrite of the example from: http://www.javazoom.net/mp3spi/documents.html
 *
 * This class needs the following libraries:
 * JLayer - http://www.javazoom.net/javalayer/javalayer.html
 * MP3 SPI - http://www.javazoom.net/mp3spi/mp3spi.html
 * Tritonus Share (tritonus_share-0.3.6.jar) - http://www.tritonus.org/plugins.html
 *
 * All credit goes to the creators of JLayer, MP3 SPI, and Tritonus.
 *
 * This simple re-write adds loop, mute, pause, restart, and stop methods to the example mentioned above.
 *
 * This code is completely free to use for any purpose whatsoever. JLayer, MP3 SPI, and Tritonus are
 * all released under the LGPL.
 *
 *
 * Known Issues:
 *
 * - Though using .stop() and then .play() *technically* works for restarting the audio, doing this too fast
 *   causes problems because the old audio stream is never stopped (writing to the audio line takes a bit,
 *   and it can't be stopped once it's started).
 * - Distorted audio (rarely? Problem with code or with audio APIs?)
 * - General Efficiency
 *
 */
public class Music implements Runnable{

    private File file;
    private boolean running, mute, pause, loop, restart;
    private final int byteChunkSize = 1024;//number of bytes to read at one time
    private byte[] muteData;

    /**
     * Declares default variable values.
     */
    public Music(){
        file = null;
        running = false;
        mute = false;
        pause = false;
        loop = false;
        restart = false;
        muteData = setMuteData();
    }
    /**
     * Creates a file object. If the file path exists on the system, the given file is an mp3, and
     * a song is not currently playing in this instance of the program, true is returned.
     * @param filePath Path to the file.
     * @return If the file is loaded or not.
     */
    public boolean loadFile(String filePath){
        file = new File(filePath);
        if(file.exists() && file.getName().toLowerCase().endsWith(".mp3") && !running){
            return true;
        }
        else{
            file = null;
            return false;
        }
    }
    /**
     * Starts playing the audio in a new thread.
     */
    public void play(){
        if(file != null && !running){
            running = true;
            try{
                Thread t = new Thread(this);
                t.start();
            }catch(Exception e){
                System.err.println("Could not start new thread for audio!");
                e.printStackTrace();
            }
        }
    }
    /**
     * Pauses the audio at its current place. Calling this method once pauses the audio stream, calling it
     * again unpauses the audio stream.
     */
    public void pause(){
        if(file != null){
            if(pause){
                pause = false;
            }
            else{
                pause = true;
            }
        }
    }
    /**
     * Closes the audio stream. This method takes some time to execute, and as such you should never call
     * .stop() followed immediately by .play(). If you need to restart a song, use .restart().
     */
    public void stop(){
        if(file != null){
            running = false;
        }
    }
    /**
     * Stream continues to play, but no sound is heard. Calling this method once mutes the audio stream,
     * calling it again unmutes the audio stream.
     */
    public void mute(){
        if(file != null){
            if(mute){
                mute = false;
            }
            else{
                mute = true;
            }
        }
    }
    /**
     * Makes a given audio file loop back to the beginning when the end is reached. Calling this method once
     * will make it loop, calling it again will make it stop looping, but will not stop the audio from playing
     * to the end of a given loop.
     */
    public void loop(){
        if(file != null){
            if(loop){
                loop = false;
            }
            else{
                loop = true;
            }
        }
    }
    /**
     * Restarts the current song. Always use this method to restart a song and never .stop() followed
     * by .play(), which is not safe.
     */
    public void restart(){
        restart = true;
    }
    /**
     * Returns whether or not a clip will loop when it reaches the end.
     * @return Status of the variable loop.
     */
    public boolean isLooping(){
        return loop;
    }
    /**
     * Returns if the audio is muted or not.
     * @return Status of mute variable.
     */
    public boolean isMuted(){
        return mute;
    }
    /**
     * Returns if the audio is paused or not.
     * @return Status of pause variable.
     */
    public boolean isPaused(){
        return pause;
    }
    /**
     * Returns if audio is currently playing (if the audio is muted, this will still be true).
     * @return If the thread is running or not.
     */
    public boolean isPlaying(){
        return running;
    }
    /**
     * Returns if a file is loaded or not.
     * @return File status of null or a file.
     */
    public boolean isLoaded(){
        if(file == null){
            return false;
        }
        else{
            return true;
        }
    }
    /**
     * Retrieves the audio stream information and starts the stream. When the stream ends, this method
     * checks to see if it should loop and start again.
     */
    public void run() {
        try{
            do{
                restart = false;
                AudioInputStream in = AudioSystem.getAudioInputStream(file);
                AudioInputStream din = null;
                AudioFormat baseFormat = in.getFormat();
                AudioFormat decodedFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,
                                                            baseFormat.getSampleRate(),
                                                            16,
                                                            baseFormat.getChannels(),
                                                            baseFormat.getChannels() * 2,
                                                            baseFormat.getSampleRate(),
                                                            false);
                din = AudioSystem.getAudioInputStream(decodedFormat, in);
                stream(decodedFormat, din);
                in.close();
            }while((loop || restart) && running);
            running = false;
        }catch(Exception e){
            System.err.println("Problem getting audio stream!");
            e.printStackTrace();
        }
    }
    /**
     * Small sections of audio bytes are read off, watching for a call to stop, pause, restart, or mute the audio.
     * @param targetFormat Format the audio will be changed to.
     * @param din The audio stream.
     */
    private void stream(AudioFormat targetFormat, AudioInputStream din){
        try{
            byte[] data = new byte[byteChunkSize];
            SourceDataLine line = getLine(targetFormat);
            if(line != null){
                line.start();
                int nBytesRead = 0;
                while(nBytesRead != -1 && running && !restart){
                    nBytesRead = din.read(data, 0, data.length);
                    if(nBytesRead != -1){
                        if(mute){
                            line.write(muteData, 0, nBytesRead);
                        }
                        else{
                            line.write(data, 0, nBytesRead);
                        }
                    }
                    
                    while(pause && running){
                        wait(15);
                    }
                }
                line.drain();
                line.stop();
                line.close();
                din.close();
            }
        }catch(Exception e){
            System.err.println("Problem playing audio!");
            e.printStackTrace();
        }
    }
    /**
     * Gets the line of audio.
     * @param audioFormat The format of the audio.
     * @return The line of audio.
     */
    private SourceDataLine getLine(AudioFormat audioFormat){
        SourceDataLine res = null;
        DataLine.Info info = new DataLine.Info(SourceDataLine.class, audioFormat);
        
        try{
            res = (SourceDataLine) AudioSystem.getLine(info);
            res.open(audioFormat);
        }catch(Exception e){
            System.err.println("Could not get audio line!");
            e.printStackTrace();
        }

        return res;
    }
    /**
     * Waits a specified number of milliseconds.
     * @param time Time to wait (in milliseconds).
     */
    private void wait(int time){
        try{
            Thread.sleep(time);
        }catch(Exception e){
            System.err.println("Could not wait!");
            e.printStackTrace();
        }
    }
    /**
     * Sets a byte array of all zeros to "play" when audio is muted. This produces no sound.
     * @return Byte array of all zeros.
     */
    private byte[] setMuteData(){
        byte[] x = new byte[byteChunkSize];
        for(int i = 0; i < x.length; i++){
            x[i] = 0;
        }

        return x;
    }
}