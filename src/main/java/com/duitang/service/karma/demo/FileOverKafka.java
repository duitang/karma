package com.duitang.service.karma.demo;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;

import com.duitang.service.karma.pipe.CloudPipeBase;
import com.google.common.io.Files;

/**
 * 
 * @author kevx
 * @since 2:08:45 PM Apr 10, 2015
 */
public class FileOverKafka {

    public static void main(String[] args) {
        FilePipe fp = new FilePipe();
        fp.pumpLine(new File(args[0]));
    }
}

class FilePipe extends CloudPipeBase {
    public void pumpLine(File file) {
        try {
            List<String> lst = Files.readLines(file, Charset.defaultCharset());
            System.out.println("filelen:" + lst.size());
            for (String s : lst) {
                pumpString(s);
            }
            System.out.println("done!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @Override
    protected String getBiz() {
        return "test";
    }
}