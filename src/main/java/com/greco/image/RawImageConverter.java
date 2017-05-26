package com.greco.image;

import org.apache.commons.cli.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;


/**
 * RawImageConverter is simple command line application that recursively traverses
 * a directory looking for Olympus RAW pictures (ORF,orf) and converts them to
 * JPG format using the Imagemagick convert command (using defaults).  This program creates the
 * new JPG files in the same location as the originals, but can optionally write them to
 * a separate directory.  In all cases, the originals are untouched.
 *
 * @author  T.J. Greco
 * @version 1.0
 * @since   2017-05-26
 */
public class RawImageConverter {

    public static void main(String[] args) {

        RawImageConverter ric = new RawImageConverter();
        ric.processImages(args);

    }

    private void processImages(String[] args){
        Option helpOption = new Option("h", "help", false, "Shows this message");
        Option directoryOption = new Option( "d", "directory", true, "The default input folder location" );
        directoryOption.setRequired(true);
        Option ouputDirectoryOption = new Option("o","outputDirectory",true,"The (optional) output Directory in case you don't want your new files in the same location");
        Options options = new Options();
        options.addOption(helpOption);
        options.addOption(directoryOption);
        options.addOption(ouputDirectoryOption);
        CommandLineParser parser = new BasicParser();
        try{
            CommandLine cmdLine = parser.parse(options, args);
            if (cmdLine.hasOption("help")) {
                printHelpMessage(options);
            }else{
                String directoryLocation = cmdLine.getOptionValue("d");
                String outputDirectoryLocation = cmdLine.getOptionValue("o");
                File f = new File(directoryLocation);
                if(f.exists() && f.isDirectory()){
                    if (outputDirectoryLocation !=null){
                        File o = new File(outputDirectoryLocation);
                        if(o.exists() && o.isDirectory()){
                            processDirectory(directoryLocation, outputDirectoryLocation);
                        }
                        else{
                            System.out.println("ERROR: The output directory provided is invalid.");
                            printHelpMessage(options);
                        }
                    }else{
                        processDirectory(directoryLocation,outputDirectoryLocation);
                    }
                }else{
                    System.out.println("ERROR: The image directory provided is invalid.");
                    printHelpMessage(options);
                }

            }
        }catch(ParseException pe){
            System.out.println("INVALID OPTIONS: " + pe.getMessage());
            printHelpMessage(options);
        }
    }

    /**
     * This method prints out the command line options and their descriptions.
     * @param options The commons-cli command line options list
     */
    private void printHelpMessage(Options options){
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("Raw Image Converter", options);
    }

    /**
     * This method starts the process of examining the files, using RawFileVisitor to do the work.
     * @param directoryName The directory path where the files exist
     */
    protected void processDirectory(String directoryName, String outputDirectoryName) {
        FileSystem fileSystem = FileSystems.getDefault();
        Path rootPath = fileSystem.getPath(directoryName);
        try {
            RawFileVisitor rfv = new RawFileVisitor(directoryName,outputDirectoryName);
            Files.walkFileTree(rootPath, rfv);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }


    }




}
