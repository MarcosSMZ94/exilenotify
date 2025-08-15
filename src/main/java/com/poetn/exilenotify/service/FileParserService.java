package com.poetn.exilenotify.service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;

@Service
public class FileParserService {
    public String fileReader(File file) throws FileNotFoundException, IOException{
        try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {
            long length = file.length() - 1;
            StringBuilder sb = new StringBuilder();

            for (long pos = length; pos >= 0; pos--) {
                raf.seek(pos);
                int ch = raf.read();
                if (ch == '\n' && pos != length) {
                    break;
                }
                sb.append((char) ch);
            }

            String lastLine = sb.reverse().toString().trim();
            return lastLine;

        } catch (java.io.FileNotFoundException e) {
            System.err.println("File Not found: " + e.getMessage());
        } catch (java.io.IOException e) {
            System.err.println("IO Exception: " + e.getMessage());
        }

        return null;
    }

    public String fileRegexMatch(String lastLine) {
        Pattern tradeRegex = Pattern.compile("@(From) ([^:]+): (.+)");
        Matcher matcher = tradeRegex.matcher(lastLine);

        if (matcher.find() && matcher.groupCount() >= 3) {
            String from = matcher.group(1);
            String username = matcher.group(2);
            String message = matcher.group(3);

            String tradeMessage = String.format("%s %s: %s", from, username, message);
            return tradeMessage;
        }
        return null;
    }
}
