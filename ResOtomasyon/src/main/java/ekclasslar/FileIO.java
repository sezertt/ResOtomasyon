package ekclasslar;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FileIO {
    public List<File> getListFiles(File parentDir) {
        ArrayList<File> inFiles = new ArrayList<File>();
        File[] files = parentDir.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                inFiles.addAll(getListFiles(file));
            } else {
                if(file.getName().endsWith(".xml")){
                    inFiles.add(file);
                }
            }
        }
        return inFiles;
    }
}
