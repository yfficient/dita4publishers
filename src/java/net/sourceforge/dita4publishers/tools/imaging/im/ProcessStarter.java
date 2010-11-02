package net.sourceforge.dita4publishers.tools.imaging.im;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This class implements the processing of os-commands using Runtime.exec().
 */

public class ProcessStarter
{
  private static Log log = LogFactory.getLog(ProcessStarter.class);
  /**
   * Buffer size of process input-stream (used for reading the output (sic!) of
   * the process). Currently 64KB.
   */

  public static final int BUFFER_SIZE = 65536;

  /**
   * The InputProvider for the ProcessStarter (if used as a pipe).
   */

  private InputProvider iInputProvider = null;

  /**
   * The OutputConsumer for the ProcessStarter (if used as a pipe).
   */

  private OutputConsumer iOutputConsumer = null;


  /**
   * The ErrorConsumer for the stderr of the ProcessStarter.
   */

  private ErrorConsumer iErrorConsumer = null;


  /**
   * Execution-mode. If true, run asynchronously.
   */

  private boolean iAsyncMode = false;


  /**
   * The ProcessListeners for this ProcessStarter.
   */
  private LinkedList<ProcessListener> iProcessListener;


  /**
   * Constructor.
   */

  protected ProcessStarter()
  {
    iProcessListener = new LinkedList<ProcessListener>();
  }


  /**
   * Set the InputProvider for the ProcessStarter (if used as a pipe).
   */

  public void setInputProvider(
    InputProvider pInputProvider)
  {
    iInputProvider = pInputProvider;
  }


  /**
   * Set the OutputConsumer for the ProcessStarter (if used as a pipe).
   */

  public void setOutputConsumer(
    OutputConsumer pOutputConsumer)
  {
    iOutputConsumer = pOutputConsumer;
  }


  /**
   * Set the ErrorConsumer for the stderr of the ProcessStarter.
   */

  public void setErrorConsumer(
    ErrorConsumer pErrorConsumer)
  {
    iErrorConsumer = pErrorConsumer;
  }


  /**
   * Add a ProcessListener to this ProcessStarter.
   * 
   * @param pProcessListener
   *          the ProcessListener to add
   */
  public void addProcessListener(
    ProcessListener pProcessListener)
  {
    iProcessListener.add(pProcessListener);
  }


  /**
   * Pipe input to the command. This is done asynchronously.
   */

  private void processInput(
    OutputStream pOutputStream)
      throws IOException
  {
    final BufferedOutputStream bos = new BufferedOutputStream(pOutputStream,
        BUFFER_SIZE);
    (new Thread() {
      public void run()
      {
        try
        {
          iInputProvider.provideInput(bos);
        }
        catch (IOException iex)
        {
          // we do nothing, since we are in an asynchronous thread anyway
        }
      }
    }).run();
    bos.close();
    if (pOutputStream != null)
    {
      pOutputStream.close();
    }
  }


  /**
   * Let the OutputConsumer process the output of the command.
   */

  private void processOutput(
    InputStream pInputStream,
    OutputConsumer pConsumer)
      throws IOException
  {
    BufferedInputStream bis = new BufferedInputStream(pInputStream, BUFFER_SIZE);
    pConsumer.consumeOutput(bis);
    bis.close();
    if (pInputStream != null)
    {
      pInputStream.close();
    }
  }


  /**
   * Let the ErrorConsumer process the stderr-stream.
   */

  private void processError(
    InputStream pInputStream,
    ErrorConsumer pConsumer)
      throws IOException
  {
    BufferedInputStream bis = new BufferedInputStream(pInputStream, BUFFER_SIZE);
    pConsumer.consumeError(bis);
    bis.close();
    if (pInputStream != null)
    {
      pInputStream.close();
    }
  }


  /**
   * Execute the command.
   */

  protected int run(
    final LinkedList<String> pArgs)
      throws IOException, InterruptedException
  {
    if (!iAsyncMode)
    {
      Process pr = startProcess(pArgs);
      return waitForProcess(pr);
    }
    else
    {
      Runnable r = new Runnable() {
        public void run()
        {
          int rc;
          ProcessEvent pe = new ProcessEvent();
          try
          {
            Process pr = startProcess(pArgs);
            for (ProcessListener pl : iProcessListener)
            {
              pl.processStarted(pr);
            }
            rc = waitForProcess(pr);
            pe.setReturnCode(rc);
          }
          catch (Exception e)
          {
            pe.setException(e);
          }
          for (ProcessListener pl : iProcessListener)
          {
            pl.processTerminated(pe);
          }
        }
      };
      (new Thread(r)).start();
      return 0;
    }
  }


  /**
   * Execute the command.
   */

  private Process startProcess(
    LinkedList<String> pArgs)
      throws IOException, InterruptedException
  {
    ProcessBuilder builder = new ProcessBuilder(pArgs);
    
    if (log.isDebugEnabled())
    {
      for (String arg: pArgs)
        log.debug("arg: "+arg);
    }
    
    
    return builder.start();
  }


  /**
   * Perform process input/output and wait for process to terminate.
   */

  private int waitForProcess(
    Process pProcess)
      throws IOException, InterruptedException
  {

    if (iInputProvider != null)
    {
      processInput(pProcess.getOutputStream());
    }
    if (iOutputConsumer != null)
    {
      processOutput(pProcess.getInputStream(), iOutputConsumer);
    }
    if (iErrorConsumer != null)
    {
      processError(pProcess.getErrorStream(), iErrorConsumer);
    }

    pProcess.waitFor();
    int rc = pProcess.exitValue();

    // just to be on the safe side
    try
    {
      pProcess.getInputStream().close();
      pProcess.getOutputStream().close();
      pProcess.getErrorStream().close();
    }
    catch (Exception e)
    {
    }
    return rc;
  }


  /**
   * @param pAsyncMode
   *          the iAsyncMode to set
   */
  public void setAsyncMode(
    boolean pAsyncMode)
  {
    iAsyncMode = pAsyncMode;
  }


  /**
   * @return the iAsyncMode
   */
  public boolean isAsyncMode()
  {
    return iAsyncMode;
  }

}
