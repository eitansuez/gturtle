package gturtle.ui;

import javax.swing.*;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintStream;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: Eitan Suez
 * Date: Nov 19, 2008
 * Time: 4:16:57 PM
 */
public class ConsolePiper
{
   ConsolePane cPane;

   public ConsolePiper(ConsolePane cPane)
   {
      this.cPane = cPane;
   }
   
   public void pipeStreams()
   {
      try
      {
         PipedInputStream piOut = new PipedInputStream();
         PipedOutputStream poOut = new PipedOutputStream(piOut);
         System.setOut(new PrintStream(poOut, true));

         PipedInputStream piErr = new PipedInputStream();
         PipedOutputStream poErr = new PipedOutputStream(piErr);
         System.setErr(new PrintStream(poErr, true));

         new ReaderThread(piOut, false).start();
         new ReaderThread(piErr, true).start();
      }
      catch (IOException e)
      {
         e.printStackTrace();
      }
   }

   class ReaderThread extends Thread
   {
      private PipedInputStream input;
      private boolean isErr = false;

      ReaderThread(PipedInputStream input, boolean isErr)
      {
         this.input = input;
         this.isErr = isErr;
      }

      public void run()
      {
         byte[] buf = new byte[1024];
         try
         {
            while (true)
            {
               int len = input.read(buf);
               if (len == -1)
               {
                  break;
               }
               final String msg = new String(buf, 0, len);
               SwingUtilities.invokeLater(new Runnable()
               {
                  public void run()
                  {
                     if (isErr)
                     {
                        cPane.error(msg);
                     }
                     else
                     {
                        cPane.normal(msg);
                     }
                  }
               });
            }
         }
         catch (IOException e) { }
      }
   }
}
