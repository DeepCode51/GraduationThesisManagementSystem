package cn.jsj.gratuatepager.tools;

import org.springframework.stereotype.Component;

import java.io.*;

@Component
public class FileCopyTool {

    public void copyFile(File source, File dest)throws IOException{
        InputStream input = null;
        OutputStream output = null;
        try {
            input = new FileInputStream(source);
            output = new FileOutputStream(dest);
            byte[] buf = new byte[2048];
            int bytesRead;
            while ((bytesRead = input.read(buf)) > 0) {
                output.write(buf, 0, bytesRead);
            }
        }finally {
            input.close();
            output.close();
        }
    }
}
