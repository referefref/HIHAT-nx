/* This file is part of HIHAT v1.1
   ================================
   Copyright (c) 2007 HIHAT-Project                   

  This program is free software; you can redistribute it and/or
  modify it under the terms of the GNU General Public License
  as published by the Free Software Foundation; either version 2
  of the License, or (at your option) any later version.
  
  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.
  
  You should have received a copy of the GNU General Public License
  along with this program; if not, write to the Free Software
  Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 
*/

 
// import input -and output operations 
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/* Honeypot-Creator: Transforms php-based web applications into a high-interaction web-based honeypot
                     Inserts all code contained in "insertionfile.txt" into each file of the source code, 
                     requires temporary write access in target directory of the web application
                     requires file insertionfile.txt            
                              
   Usage example: "java honeypot_creator /var/www/phpmyadmin/"                                          */ 
public class honeypot_creator {
    // counter for number of processed files
    public static int counter = 0;
    
    // file with code to insert at beginning of each filtered file
    // (HIHAT-nx: the file ships as "insertionFile.txt"; the old lower-case name is still accepted)
    private static String insertionFile = new File( "insertionFile.txt" ).exists() ? "insertionFile.txt" : "insertionfile.txt";

    // HIHAT-nx: number of files skipped (already transformed, or code that cannot be prefixed)
    public static int skipped = 0;

    /* HIHAT-nx: current PHP requires declare(strict_types=1) and a namespace statement to come first in a file.
       For such files the logging code is inserted directly after these statements, without its own
       opening and closing PHP tags. Byte-preserving (ISO-8859-1); comments before the statements are allowed. */
    private static final String COMMENTS = "(?:\\s+|/\\*.*?\\*/|//[^\\n]*|#(?!\\[)[^\\n]*)*";
    private static final Pattern LEADING_STATEMENTS = Pattern.compile(
        "\\A(?:\\xEF\\xBB\\xBF)?<\\?php" + COMMENTS
        + "(?:(declare\\s*\\([^)]*\\)\\s*;)" + COMMENTS + ")?"
        + "(?:(namespace\\s+[A-Za-z0-9_\\\\]+\\s*;)" + COMMENTS + ")?",
        Pattern.DOTALL );
    private static final Pattern BRACED_NAMESPACE = Pattern.compile( "^\\s*namespace(\\s+[A-Za-z0-9_\\\\]+)?\\s*\\{", Pattern.MULTILINE );
   
   
    // empty constructor
    public honeypot_creator() {        
    }
    
    
    /* selects files to insert the code into
       @param filename : String with name of file 
       @returns        : true if code should be added + file should be considered (current setting: php files only) */    
    private static boolean filterOk( String filename ) {
        return ((filename.endsWith( ".php")) // | (filename.endsWith( "index.php")) |
                );  
    }
    
   /* lists all directories and processes each element, needs method "listContent"
      @param dir : File object of directory
      @returns   : void                       */  
   public static void listDir(File dir) {
   File[] files = dir.listFiles();
   if (files != null) {
      for (int i = 0; i < files.length; i++) {
         System.out.print(files[i].getAbsolutePath() + "\n");
         if (files[i].isDirectory()) {
            
            listDir(files[i]); 
            }
         else {
            if ( filterOk ( files[i].getAbsolutePath() ) )
              listContent( files[i].getAbsolutePath() );
         }
      }
    } else 
          System.out.print( "Directory not found! " );
    }
    
   
   /* inserts the content of the insertionfile into a single file of the web application
      @param fileName : String with name of file of web application
      @returns        : void                                                  */
   public static void listContent( String fileName ) {
       counter++;
       File sourcefile = new File (fileName );

       // temporary buffer file (HIHAT-nx: next to the target file, so that the final rename stays on one file system)
       String tempFileString = sourcefile.getAbsolutePath() + ".hihat.tmp";

        // main part
        try {
          String insertion = new String( Files.readAllBytes( new File( insertionFile ).toPath() ), StandardCharsets.ISO_8859_1 );
          String original  = new String( Files.readAllBytes( sourcefile.toPath() ), StandardCharsets.ISO_8859_1 );
          String result;

          if ( original.contains( "$thisModule_23cdx_" ) ) {
              System.err.println( "Skipped (already transformed): " + fileName );
              skipped++;
              return;
          }

          // code outside braced namespace blocks is not allowed, so such files cannot be prefixed
          if ( BRACED_NAMESPACE.matcher( original ).find() ) {
              System.err.println( "Skipped (braced namespace): " + fileName );
              skipped++;
              return;
          }

          Matcher m = LEADING_STATEMENTS.matcher( original );
          if ( m.lookingAt() && ( m.group( 1 ) != null || m.group( 2 ) != null ) ) {
              // strip the opening and closing tags of the insertion and place it after declare/namespace
              String body = insertion.replaceFirst( "\\A\\s*<\\?php", "" ).replaceFirst( "\\?>\\s*\\z", "" );
              String nl = original.contains( "\r\n" ) ? "\r\n" : "\n";
              result = original.substring( 0, m.end() ) + nl + body + nl + original.substring( m.end() );
          } else {
              // original behaviour: the insertion followed by the original data
              result = insertion + original;
          }

          DataOutputStream out =      new DataOutputStream(
                                      new BufferedOutputStream(
                                      new FileOutputStream( tempFileString )));
          out.write( result.getBytes( StandardCharsets.ISO_8859_1 ) );
          out.close();

          File newRenamedSourcefile = new File( sourcefile.getAbsolutePath() + "."
                        +  Math.abs(( new java.util.Random().nextInt() % 1000)) + "old" );

          if (!sourcefile.renameTo( newRenamedSourcefile ))
                    System.err.println( "Couldn't rename file!");


          if ( ! (new File( tempFileString )).renameTo( new File( fileName))) {
              System.err.println( "Error: Couldn't create modified File! Data stored in " + sourcefile.getAbsolutePath());
          } else {
                if (!newRenamedSourcefile.delete())
                    System.err.println( "Error: Couldn't delete renamed Sourcefile " + newRenamedSourcefile.getAbsolutePath());
            }
          } catch (IOException e) {
          System.err.println(e.toString());
        }
   }

    /* main program  
       usage example: "java honeypot_creator /var/www/phpmyadmin/"     */ 
    public static void main(String[] args) {         
      String directory;    
      // check arguments
      if ( args.length > 1 ) {                    
              System.out.println( "Too many parameters!" );
              System.exit( 0 );
      } else if ( args.length == 1 ) {
          // default directory
          if (args[ 0 ].equals("default" ))
              directory = "C:\\Dokumente und Einstellungen\\Michael Mustermann\\Eigene Dateien\\xampp\\htdocs\\phpmyadm"; //initial vaule for the directory - change for your settings
          else
              directory = args[ 0 ];             
                  
          System.out.println( "Processing " + args[ 0 ] + ":\n" );    
                  
          listDir( new File( directory ));
          //System.out.println( new File("C:\\Dokumente und Einstellungen\\Michael Mustermann\\Eigene Dateien\\xampp\\htdocs\\" + directory));          
          System.out.println( "\n" + counter + " files have been processed for directory " + directory );
          if ( skipped > 0 )
              System.out.println( skipped + " of them were skipped (see messages above)" );                
      } else 
          System.out.println( "Missing parameter: please add full directory path!" );
    }
    
}
