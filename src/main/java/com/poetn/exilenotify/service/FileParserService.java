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
    
    public String getLastTradeMessage(File file) {
        try {
            String lastLine = fileReader(file);
            if (lastLine != null) {
                return fileRegexMatch(lastLine);
            }
        } catch (Exception e) {
            return "Error" + e.getMessage();
        }
        return null;
    }

    private String fileReader(File file) throws FileNotFoundException, IOException {
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

            return sb.reverse().toString().trim();
        }
    }

    private String fileRegexMatch(String lastLine) {
        Pattern tradeRegex = Pattern.compile("@(From) ([^:]+): (.+)");
        Matcher matcher = tradeRegex.matcher(lastLine);

        if (matcher.find() && matcher.groupCount() >= 3) {
            String from = matcher.group(1);
            String username = matcher.group(2);
            String message = matcher.group(3);

            return String.format("%s %s: %s", from, username, message);
        }
        return null;
    }
}
