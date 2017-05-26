package com.greco.image;

import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

/**
 * RawFileVisitor is an implementation of Java's SimpleFileVisitot
 * that is tailored to the raw conversion implementation.  This class allows
 * for the use of an output directory in case the user wants the files to be placed
 * in a separate directory from the original files.
 *
 * @author  T.J. Greco
 * @version 1.0
 * @since   2017-05-26
 */
public class RawFileVisitor extends SimpleFileVisitor<Path> {

    private static final String SEPARATOR = "--------------------------------------------------------------";
    private static final String DIRECTORY_NAME_LABEL = " DIRECTORY NAME: ";
    private static final String LOCATION_LABEL = "LOCATION: ";
    private static final String FILE_NAME_LABEL = "FILE NAME: ";
    private static final String EXTENSION_SEPARATOR = ".";
    private static final String RAW_FILE_EXTENSION_BIG = "ORF";
    private static final String RAW_FILE_EXTENSION_LITTLE = "orf";
    private static final String JPG_FILE_EXTENSION = "jpg";
    private static final String SKIPPING_LABEL = "SKIPPING: ";
    private static final String PROCESSING_LABEL = "PROCESSING: ";
    private static final String CONVERT_COMMAND = "convert";
    private static final String SPACE = " ";
    private static final String CONVERT_OUTPUT_MSG = "CONVERT OPERATION OUTPUT: ";
    private static final String CONVERT_ERROR_OUTPUT_MSG = "CONVERT ERROR OUTPUT: ";
    private static final String LINE_TERMINATOR = "\n";

    private String outputDirectory;

    /**
     * Creates a new RawFileVisitor object passing in an optional
     * outputDirectory
     * @param outputDir The path where you would like the JPG output files to be placed
     */
    public RawFileVisitor(String outputDir){
        outputDirectory = outputDir;
    }

    @Override
    public FileVisitResult visitFile(Path visitedFile, BasicFileAttributes attributes) {
        System.out.println(FILE_NAME_LABEL + visitedFile.getFileName());

        if (shouldProcessFile(visitedFile)){
            System.out.println(PROCESSING_LABEL + visitedFile.getFileName());
            try{
                convertRawtoJPG(visitedFile);
            }catch (Exception e){
                System.out.println("ERROR PROCESSING FILE: " +visitedFile.getFileName() );
            }
        }else{
            System.out.println(SKIPPING_LABEL + visitedFile.getFileName());
        }
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult postVisitDirectory(Path directory, IOException e) throws IOException {
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult preVisitDirectory(Path dir,
                                             BasicFileAttributes attributes) throws IOException {
        System.out.println(SEPARATOR);
        System.out.println(DIRECTORY_NAME_LABEL + dir.getFileName()
                + LOCATION_LABEL+ " " + dir.toFile().getPath());
        System.out.println(SEPARATOR);
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
        System.out.println("A file traversal error ocurred");
        return super.visitFileFailed(file, exc);
    }

    /**
     * This method is used to determine if a file should be processed or skipped
     * It is looking for an extension of "ORF" or "orf", which is the
     * Olympus Raw file format.  If a JPG file exists for the RAW image, it will also return false.
     * @param visitedFile This is the Java NIO Path
     * @return boolean True if the file should be procesed, false if not
     */
    private boolean shouldProcessFile(Path visitedFile){
        String fileName = visitedFile.getFileName().toString();
        String fileEnding = fileName.substring(fileName.lastIndexOf(EXTENSION_SEPARATOR) + 1);
        if (fileEnding.equalsIgnoreCase(RAW_FILE_EXTENSION_BIG)){
            File f = new File(getFileNameWithoutExtension(visitedFile.toString()) + "." + JPG_FILE_EXTENSION);
            if(!f.exists()){
                return true;
            }else{
                System.out.println("SKIPPING (Converted file already exists):" + fileName);
                return false;
            }
        }else{
            return false;
        }
    }

    /**
     * This method returns the base filename without extension
     * @param fileName The filename to be tested
     * @return String the filename absent the extension.
     */
    private String getFileNameWithoutExtension(String fileName){
        return fileName.substring(0,fileName.lastIndexOf(EXTENSION_SEPARATOR));
    }

    /**
     * This method uses the Imagemagick convert program to convert the Olympus
     * RAW file into jpg.  It uses all of the defaults of convert (for now).
     * @param visitedFile This is the Java NIO Path
     */
    private void convertRawtoJPG(Path visitedFile) throws Exception{
        String s = null;
        String fileName = visitedFile.toString();
        String newFileName = StringUtils.replace(fileName,RAW_FILE_EXTENSION_LITTLE,JPG_FILE_EXTENSION);
        String finalNewFileName = StringUtils.replace(newFileName,RAW_FILE_EXTENSION_BIG,JPG_FILE_EXTENSION);
        String theConvertCommand;
        if(outputDirectory != null){
            theConvertCommand = CONVERT_COMMAND + SPACE + visitedFile.toString() + SPACE + outputDirectory + "/" + finalNewFileName.substring(finalNewFileName.lastIndexOf("/"),finalNewFileName.length()) ;
        }else{
            theConvertCommand = CONVERT_COMMAND + SPACE + visitedFile.toString() + SPACE + finalNewFileName;
        }
        Process p = Runtime.getRuntime().exec(theConvertCommand);
        BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
        BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));
        // read the output from the command
        System.out.println(CONVERT_OUTPUT_MSG + LINE_TERMINATOR);
        while ((s = stdInput.readLine()) != null) {
            System.out.println(s);
        }
        // read any errors from the attempted command
        System.out.println(CONVERT_ERROR_OUTPUT_MSG + LINE_TERMINATOR);
        while ((s = stdError.readLine()) != null) {
            System.out.println(s);
        }
    }

}
