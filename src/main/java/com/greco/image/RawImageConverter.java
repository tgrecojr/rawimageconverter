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
    /**
     * This method starts the process of examining the files, using RawFileVisitor to do the work.
     * @param directoryName The directory path where the files exist
     */
    protected void processDirectory(String directoryName) {
        FileSystem fileSystem = FileSystems.getDefault();
        Path rootPath = fileSystem.getPath(directoryName);
        try {
            RawFileVisitor rfv = new RawFileVisitor("");
            Files.walkFileTree(rootPath, rfv);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }


    }




}
