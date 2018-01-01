/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.sandholz.semp.http;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

/**
 *
 * @author Markus Sandholz
 */
public class ReverseLineInputStream extends InputStream {
     RandomAccessFile in;

    long currentLineStart = -1;
    long currentLineEnd = -1;
    long currentPos = -1;
    long lastPosInFile = -1;
    int lastChar = -1;


    public ReverseLineInputStream(File file) throws FileNotFoundException {
        in = new RandomAccessFile(file, "r");
        currentLineStart = file.length();
        currentLineEnd = file.length();
        lastPosInFile = file.length() -1;
        currentPos = currentLineEnd; 

    }
    
    
    public int read() throws IOException {

        if (currentPos < currentLineEnd ) {
            in.seek(currentPos++);
            int readByte = in.readByte();            
            return readByte;
        } else if (currentPos > lastPosInFile && currentLineStart < currentLineEnd) {
            // last line in file (first returned)
            findPrevLine();
            if (lastChar != '\n' && lastChar != '\r') {
                // last line is not terminated
                return '\n';
            } else {
                return read();
            }
        } else if (currentPos < 0) {
            return -1;
        } else {
            findPrevLine();
            return read();
        }
    }
    
    

    private void findPrevLine() throws IOException {
        if (lastChar == -1) {
            in.seek(lastPosInFile);
            lastChar = in.readByte();
        }

        currentLineEnd = currentLineStart; 

        // There are no more lines, since we are at the beginning of the file and no lines.
        if (currentLineEnd == 0) {
            currentLineEnd = -1;
            currentLineStart = -1;
            currentPos = -1;
            return; 
        }

        long filePointer = currentLineStart -1;

        while ( true) {
            filePointer--;

            // we are at start of file so this is the first line in the file.
            if (filePointer < 0) {  
                break; 
            }

            in.seek(filePointer);
            int readByte = in.readByte();

            // We ignore last LF in file. search back to find the previous LF.
            if (readByte == 0xA && filePointer != lastPosInFile ) {   
                break;
            }
        }
        // we want to start at pointer +1 so we are after the LF we found or at 0 the start of the file.   
        currentLineStart = filePointer + 1;
        currentPos = currentLineStart;
    }


    @Override
    public void close() throws IOException {
        if (in != null) {
            in.close();
            in = null;
        }
    }
}
