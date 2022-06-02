package com.bankbazaar.webclient.service.service;
import com.bankbazaar.webclient.core.model.MovieData;
import org.springframework.stereotype.Service;

import java.io.*;

@Service
public class FileUtil {
    public File createFile(String fileName)
    {
        ClassLoader classLoader = getClass().getClassLoader();
        return new File(classLoader.getResource(".").getFile() + fileName+".txt");
    }
    public Boolean writeFile(MovieData movieData){
        try(final FileOutputStream fileOut = new FileOutputStream(createFile(movieData.getTitle()))) {
            final ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(movieData);
            out.flush();
            return true;
        }
        catch (IOException ioException)
        {
            return false;
        }
    }

    public Boolean fileExists(String name)
    {
        return createFile(name).exists();
    }
}

