package com.greco.image;

import org.apache.commons.cli.*;
import org.apache.commons.lang3.StringUtils;


import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

public class RawImageConverter {

    public static void main(String[] args) {

        CommandLineParser clp = new BasicParser();
        Options options = new Options();
        Option d = new Option( "d", "directory", true, "The default input folder location" );
        d.setRequired(true);
        options.addOption( d );
        try {
            CommandLine line = clp.parse( options, args );
            RawImageConverter ric = new RawImageConverter();
            File f = new File(line.getOptionValue( "directory" ));
            if (f.exists() && f.isDirectory()){
                ric.processDirectory(line.getOptionValue( "directory" ));
            }else{
                System.out.println("This directory does not seem to exist.");
            }

        }

        catch( ParseException exp ) {
            System.out.println( "Unexpected exception:" + exp.getMessage() );
        }

    }

    public void processDirectory(String directoryName) {
        FileSystem fileSystem = FileSystems.getDefault();
        Path rootPath = fileSystem.getPath(directoryName);
        try {
            Files.walkFileTree(rootPath, simpleFileVisitor);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }


    }


    FileVisitor<Path> simpleFileVisitor = new SimpleFileVisitor<Path>() {

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

        @Override
        public FileVisitResult preVisitDirectory(Path dir,BasicFileAttributes attrs)
                throws IOException {
            System.out.println(SEPARATOR);
            System.out.println(DIRECTORY_NAME_LABEL + dir.getFileName()
                    + LOCATION_LABEL+ " " + dir.toFile().getPath());
            System.out.println(SEPARATOR);
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFile(Path visitedFile,BasicFileAttributes fileAttributes) throws IOException {
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

        private boolean shouldProcessFile(Path visitedFile){
            String fileName = visitedFile.getFileName().toString();
            String fileEnding = fileName.substring(fileName.lastIndexOf(EXTENSION_SEPARATOR) + 1);
            if (fileEnding.equalsIgnoreCase(RAW_FILE_EXTENSION_BIG)){
                return true;
            }else{
                return false;
            }
        }

        private void convertRawtoJPG(Path visitedFile) throws Exception{
            String s = null;
            String fileName = visitedFile.toString();
            String newFileName = StringUtils.replace(fileName,RAW_FILE_EXTENSION_LITTLE,JPG_FILE_EXTENSION);
            String finalNewFileName = StringUtils.replace(fileName,RAW_FILE_EXTENSION_BIG,JPG_FILE_EXTENSION);

            Process p = Runtime.getRuntime().exec(CONVERT_COMMAND + SPACE + visitedFile.toString() + SPACE + finalNewFileName);
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

    };

}
