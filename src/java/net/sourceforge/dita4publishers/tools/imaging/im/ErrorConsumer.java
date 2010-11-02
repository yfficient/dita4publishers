package net.sourceforge.dita4publishers.tools.imaging.im;

import java.io.InputStream;
import java.io.IOException;

/**
 * This interface defines an ErrorConsumer. An ErrorConsumer reads
 * output from a process' stderr.
*/

public interface ErrorConsumer 
{

  /**
   * The ErrorConsumer must read the output of a process from the given
   *  InputStream.
   */
  public void consumeError(
    InputStream pInputStream) 
      throws IOException;
}
