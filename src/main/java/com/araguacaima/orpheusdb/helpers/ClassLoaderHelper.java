package com.araguacaima.orpheusdb.helpers;

import com.araguacaima.commons.utils.ClassLoaderUtils;
import com.araguacaima.commons.utils.FileUtils;
import org.apache.commons.io.filefilter.FileFileFilter;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

public class ClassLoaderHelper {

    private final ClassLoaderUtils classLoaderUtils;

    private static IOFileFilter classesFiles = new FileFileFilter() {
        public boolean accept(File file) {
            return file.isFile() && file.getAbsolutePath().endsWith(".class");
        }
    };

    public ClassLoaderHelper(ClassLoaderUtils classLoaderUtils) {
        this.classLoaderUtils = classLoaderUtils;
    }

    public void addClassesInDirectoryToClasspath(String directoryFullPath) throws InvocationTargetException, NoSuchMethodException, MalformedURLException, IllegalAccessException, ClassNotFoundException {
        File directory = new File(directoryFullPath);
        addClassesInDirectoryToClasspath(directory);
    }

    public void addClassesInDirectoryToClasspath(File directory) throws InvocationTargetException, NoSuchMethodException, MalformedURLException, IllegalAccessException, ClassNotFoundException {
        URLClassLoader child = new URLClassLoader(new URL[]{directory.toURI().toURL()}, ClassLoader.getSystemClassLoader());
        String directoryFullPath = directory.getAbsolutePath();
        FileUtils.iterateFilesAndDirs(directory, classesFiles, TrueFileFilter.INSTANCE).forEachRemaining(file -> {
            try {
                if (file.isFile()) {
                    String clazzName = StringUtils.difference(directoryFullPath + File.separator, file.getAbsolutePath()).replaceAll("\\\\", ".").replaceAll("/", ".");
                    clazzName = clazzName.substring(0, clazzName.length() - 6);
                    Class.forName(clazzName, true, child);
                    classLoaderUtils.addResourceToDependencies(file.getAbsolutePath());
                }
            } catch (ClassNotFoundException | IllegalAccessException | IOException | NoSuchFieldException e) {
                e.printStackTrace();
            }
        });
    }
}
