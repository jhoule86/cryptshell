/*
 * File: CryptShell.java
 * Author: jhoule
 */
package org.jhoule.cryptshell;

import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import jhcrypt.JHCrypt;

/**
 * The CryptShell class contains the main functionality for user interaction
 * with an encryption library.
 *
 * @author jhoule
 */
public class CryptShell implements Runnable
{

    final static String PATH_SUFFIX = "_work";
    final static String PATH_NPP = "C:\\Progra~2\\Notepad++\\notepad++.exe";
    String ptEditior = PATH_NPP;
    String ptInput = "";
    String ptOutput = null;
    String keyOne = "";
    String keyTwo = "";
    File fiSource, fiWorking;
    Long tmBegin, tmAfter;
    
    private Transferable clipboardContent = null;

    /**
     * overridden because the generic shell cannot be run.
     */
    @Override
    public void run()
    {
        throw new UnsupportedOperationException("Running the generic shell is not supported.");
    }

    /**
     * The main function of this class is to run the specialized shell for user
     * interaction with an encryption library.
     *
     * If an argument is supplied, it is used for the input path.
     *
     * @param args - the arguments passed to the executable.
     */
    public static void main(String[] args)
    {
        EncryptedFileEditShell shell = new EncryptedFileEditShell();

        if (args.length > 0)
        {
            shell.ptInput = args[0];
        }
        
        if (args.length > 1)
        {
            shell.ptEditior = args[1];
        }
        
        if (args.length > 2)
        {
            shell.ptOutput = args[2];
        }

        // just run the only type of shell we have right now.
        shell.run();
    }

    /**
     * Creates the temporary working file for editing a file that has been
     * decrypted.
     */
    void createWorkingFile()
    {
        // create the tempFile

        if (fiWorking.delete())
        {
            // we deleted it
        } else
        {
            // it never existed anyway
        }
        try
        {
            if (fiWorking.createNewFile())
            {
                // we created it
            } else
            {
                System.err.println("could not create working file!");
                System.exit(3);
            }
        } catch (IOException ex)
        {
            Logger.getLogger(EncryptedFileEditShell.class.getName()).log(Level.SEVERE, "could not create working file!", ex);
            System.exit(3);
        }

        // we want to delete on exit.
        fiWorking.deleteOnExit();
    }

    /**
     * The prompt for changes. Note: eats whole string upon input.
     *
     * @param kb - the keyboard scanner
     * @param ps - the printstream to print to.
     * @return the first character of the string returned from the prompt.
     */
    static char promptUponChange(Scanner kb, PrintStream ps)
    {
        // ask user if they want to re-encrypt.
        // y/Y is go, anything else is quit.

        ps.println("Changes detected.");
        ps.println("Re-encrypt (Y/n)");

        String decision = kb.nextLine();

        // BUGFIX 2012/4/9 : default to yes upon blank line.
        return (decision.isEmpty()) ? 'y' : decision.toLowerCase().charAt(0);
    }

    /**
     * Handles changes to the working file, by getting the user's desired
     * reaction(s) to the change.
     *
     * @param kb - the keyboard scanner for user interaction
     * @param ps - the PrintStream to output prompts/messages to.
     */
    void handleChanges(Scanner kb, PrintStream ps)
    {
        char chDec = promptUponChange(kb, ps);

        while (!(chDec == 'y' || chDec == 'n'))
        {
            chDec = promptUponChange(kb, ps);
        }
        switch (chDec)
        {
            case 'y':
            {
                try
                {
                    // user wants this re-encrypted.
                    encryptFile();
                }
                catch (Exception ex)
                {
                    System.err.println(ex.getMessage());
                    System.out.println("Unable to encrypt the new file. "
                            + "Original file will remain as it was before session.");
                }
                
                break;
            }

            case 'n':
            {
                // user wants the file to be left as-is.
                System.out.println("Okay. "
                        + "Original file will remain as it was before this session.");
                break;
            }
        }

    }

    /**
     * (Re)Encrypts the file and saves to original location.
     */
    void encryptFile()
    {
        System.out.println("Okay. "
                + "Re-encrypting and saving to " + ptInput + ".");

        // encrypt the source of the working file and save to path
        // of source file.
        String encryptArgs[] =
        {
            "e", ptOutput, ptInput, keyOne, keyTwo
        };
        JHCrypt.main(encryptArgs);
    }

    /**
     * Decrypts the file at <ptInput> and stores it in the file at <ptOutput>,
     * according to the chosen cypher key(s).
     */
    void decryptFile()
    {
        // decrypt the file to the working file.
        String decryptArgs[] =
        {
            "d", ptInput, ptOutput, keyOne, keyTwo
        };


        JHCrypt.main(decryptArgs);
    }

    /**
     * Prompts the user for inputs.
     *
     * @param kb - the keyboard scanner for user interaction
     * @param ps - the PrintStream to output prompts/messages to.
     */
    void getInputs(Scanner kb, PrintStream ps)
    {
        if (ptInput != null && ptInput.length() > 0)
        {
            ps.println("Using source: " + ptInput);
        } else
        {
            // get the path.
            ps.print("Source path? ");
            ptInput = kb.nextLine();
        }

        //get decryption values
        ps.print("Key one?: ");
        keyOne = kb.nextLine();
        ps.println();
        ps.print("Key two?: ");
        keyTwo = kb.nextLine();
        ps.println();

        // clear out newline
        kb.nextLine();
    }

    /**
     * Attempts to decrypt the file at <fiSource> into <ptOutput> by using
     * decryptFile
     *
     * @param ps - the printstream to use for messages.
     */
    boolean loadFile(PrintStream ps)
    {
        // check if file exists.
        if (! fiSource.exists())
        {
            // file does not exist.
            ps.println("WARNING: The source file does not exist.");
            return false;
        }

        decryptFile();
        return true;
    }

    /**
     * Runs the user's editor program, passing the path of the output file to
     * it.
     *
     * @param ptEditior - the path of the user's editor program.
     * @param ptOutput - the path of the working file.
     */
    static void runEditor(String ptEditior, String ptOutput)
    {
        // open the file in the editor.
        System.out.println("The working file is opening in your editor.");
        Runtime rt = Runtime.getRuntime();
        Process procEdit;
        try
        {
            // TODO: make editor launch new session, without plugins
            procEdit = rt.exec(ptEditior + " " + ptOutput);
            procEdit.waitFor();
        } catch (IOException ex)
        {
            Logger.getLogger(EncryptedFileEditShell.class.getName()).log(Level.SEVERE,
                    "Unable to open file in editor!", ex);
            System.exit(1);
        } catch (InterruptedException ex)
        {
            Logger.getLogger(EncryptedFileEditShell.class.getName()).log(Level.SEVERE,
                    "Wait for editior was interrupted!", ex);
            System.exit(2);
        }
    }
    
    void cacheClipboard()
    {
        cacheClipboard(true);
    }
    
    void cacheClipboard(boolean clear)
    {
        clipboardContent = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(this);
        
        if (clear)
            clearClipboard();
    }
    
    void restoreClipboard()
    {
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(clipboardContent, null);
    }
    
    static void clearClipboard()
    {
        StringSelection sel = new StringSelection("");
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(sel, null);   
    }
}
