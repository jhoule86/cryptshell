/*
 * 
 */
package org.jhoule.cryptshell;

import java.io.File;
import java.io.PrintStream;
import java.util.Scanner;

/**
 *
 * @author jhoule
 */
public class EncryptedFileEditShell extends CryptShell
{

    /**
     * Adapted from the batch file it used to be.
     *
     * @param args the command line arguments
     */
    @Override
    public void run()
    {

        // create a keyboard scanner.
        Scanner kb = new Scanner(System.in);

        // set up the printstream for normal output.
        PrintStream ps = System.out;

        getInputs(kb, ps);
        
        if (ptOutput == null || ptOutput.trim().length() == 0)
            ptOutput = ptInput + PATH_SUFFIX;

        // create file objects.
        fiSource = new File(ptInput);
        fiWorking = new File(ptOutput);

        // create working file
        createWorkingFile();

        boolean fileExisted = loadFile(ps);

        //get last modified time of the working file, for later comparison.
        tmBegin = fiWorking.lastModified();

        cacheClipboard();
        // run the editior
        runEditor(ptEditior, ptOutput);

        // editor process has completed or failed.
        ps.println("The editing session has ended.");

        // check for changes
        tmAfter = fiWorking.lastModified();
        if (fileExisted && tmAfter.equals(tmBegin))
        {
            // no modification
            ps.println("No modifications found.");
        } else
        {
            // modification detected.
            handleChanges(kb, ps);
        }

        // tell user that we are deleting the working copy.
        System.out.println("Working copy is now being deleted.");

        if (fiWorking.delete())
        {
            System.out.println("Working copy has been deleted.");
        } else
        {
            System.err.println("ERROR: unable to delete the working copy at "
                    + ptOutput + "!");
        }

        // restore clipboard
        restoreClipboard();
        
        // notify user that we are exiting.
        System.out.println("Now Exiting...");
        System.exit(0);
    }

   
}
