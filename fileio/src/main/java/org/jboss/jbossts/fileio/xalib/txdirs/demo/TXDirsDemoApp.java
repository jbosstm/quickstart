/*     JBoss, Home of Professional Open Source Copyright 2008, Red Hat
 *  Middleware LLC, and individual contributors as indicated by the
 *  @author tags.
 *     See the copyright.txt in the distribution for a full listing of
 *  individual contributors. This copyrighted material is made available
 *  to anyone wishing to use, modify, copy, or redistribute it subject to
 *  the terms and conditions of the GNU Lesser General Public License, v. 2.1.
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT A WARRANTY; without even the implied warranty of MERCHANTABILITY
 *  or FITNESS FOR A PARTICULAR PURPOSE.
 *     See the GNU Lesser General Public License for more details. You should
 *  have received a copy of the GNU Lesser General Public License, v.2.1
 *  along with this distribution; if not, write to the Free Software
 *  Foundation, Inc., 51 Franklin Street, Fifth Floor,
 *  Boston, MA  02110-1301, USA.
 *
 *  (C) 2008,
 *  @author Red Hat Middleware LLC.
 */
package org.jboss.jbossts.fileio.xalib.txdirs.demo;

import org.jboss.jbossts.fileio.xalib.txdirs.dir.XADir;
import org.jboss.jbossts.fileio.xalib.txdirs.dir.XADirFile;
import javax.transaction.TransactionManager;
import java.io.File;
import java.io.IOException;
import com.arjuna.ats.internal.jta.transaction.arjunacore.TransactionManagerImple;

/**
 * This is a demo class.
 * Demonstrates a file operation applied transactionally on a file
 * under some directory.
 *
 * @author Ioannis Ganotis
 * @version Aug 23, 2008
 */
public class TXDirsDemoApp
{
  final String EXISTING_FILENAME = "file.txt";
  final String NEW_FILENAME = "new_file.txt";
  final String TX_FOLDER_NAME = "businesstxdir";
  XADir xadir;
  TransactionManager txMngr;

  public TXDirsDemoApp() throws Exception {
    initialiseFiles();

    xadir = new XADir(new File(TX_FOLDER_NAME));
    txMngr = new TransactionManagerImple();

    executeDemo();
  }

  /**
   * Creates a directory if it does not exists and also
   * creates a file in it. The file will be used to apply
   * file operations on it.
   *
   * @exception IOException if an I/O error occurs
   */
  private void initialiseFiles() throws IOException  {
    System.out.println("######### Initialise:Begin #########");
    File folder = new File(TX_FOLDER_NAME);
    if (folder.exists()) {
      deleteFiles(folder.listFiles());
      folder.delete();
    }
    // Create the folder and a file in it
    folder.mkdir();
    File file = new File(TX_FOLDER_NAME + "/" + EXISTING_FILENAME);
    file.createNewFile();
    System.out.println("--- File name in the tx directory: <" + file.getName() + "> ---");
    System.out.println("#########  Initialise:End  #########\n");    
  }

  /**
   * Deletes all the given <code>files</code>
   *
   * @param files the files to delete
   */
  private void deleteFiles(File[] files) {
    for (File file : files) {
      if (file.isDirectory())
        deleteFiles(file.listFiles());
      file.delete();
    }
  }

  /**
   * The method performs a simple rename operation to an existing
   * file in the transactional directory during the execution of a
   * transaction.
   *
   * @exception Exception if an error occurs during the execution of
   *                      the transaction
   */
  private void executeDemo() throws Exception {
    try {
      txMngr.begin();
      {
        System.out.println("######### TransactionManager:Started #########");
        xadir.startTransactionOn(txMngr);

        // Get a list of all the files in the directory
        XADirFile[] files = xadir.listTXFiles();

        // Apply a file operation - rename old file to something else
        files[0].renameTo(new File(NEW_FILENAME));
        System.out.println("------ TX renamed old file ------");
        System.out.println("------ TX reads new filename: <" + files[0].getName() + "> ------");

        System.out.println("--- Actual filename: <" + getFileName() + "> ---");
      }
      txMngr.commit();
      System.out.println("######### TransactionManager:Committed #########\n");
      System.out.println("--- Actual filename: <" + getFileName() + "> ---");
    } catch (Exception e) {
      txMngr.rollback();
      System.out.println("XXXXXXXX TransactionManager:Rolled-back XXXXXXXX\n");
      System.out.println("--- Actual filename: <" + getFileName() + "> ---");      
    }
    xadir.close();
  }

  /**
   * Returns the name of the file in the folder
   *
   * @return a <code>String</code> representing the name of the
   *         file in the folder
   */
  private String getFileName() {
    File folder = new File(TX_FOLDER_NAME);
    return folder.list()[0]; // return the filename
  }

  public static void main (String[] args) throws Exception {
    new TXDirsDemoApp();
  }
}
