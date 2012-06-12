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
package org.jboss.jbossts.fileio.xalib.txfiles.demo;

import org.jboss.jbossts.fileio.xalib.txfiles.file.XAFile;
import javax.transaction.TransactionManager;
import com.arjuna.ats.internal.jta.transaction.arjunacore.TransactionManagerImple;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * This is a demo class.
 * It demonstrates how to apply read and write operations transactionally
 * to a file
 *
 * @author Ioannis Ganotis
 * @version Aug 20, 2008
 */
public class TXFilesDemoApp
{
  final String FILENAME = "entries.txt";
  final String MSG = "The result of the computation is: ";
  final byte COMPUTATION_RESULT = 49;
  XAFile xaFile;
  TransactionManager txMngr;

  public TXFilesDemoApp() throws Exception {
    initialiseData();
    xaFile = new XAFile(FILENAME, "rw", true);
    txMngr = new TransactionManagerImple();

    executeDemo();

  }

  /**
   * Crreates a file if it does not exist, and writes some
   * data to it.
   *
   * @exception IOException if an I/O error occurs
   */
  private void initialiseData() throws IOException {
    System.out.println("######### Initialise:Begin #########");
    RandomAccessFile raf = new RandomAccessFile(FILENAME, "rw");
    raf.writeBytes(MSG);
    raf.write(COMPUTATION_RESULT);
    raf.seek(0);
    System.out.println(raf.readLine());
    raf.close();
    System.out.println("#########  Initialise:End  #########\n");
  }

  /**
   * Applies simple write and read operations to demonstrate the behaviour
   * of the {@link org.jboss.jbossts.fileio.xalib.txfiles.file.XAFile}.
   *
   * @exception Exception if an error occurs during the transaction
   */
  private void executeDemo() throws Exception {
    try {
      txMngr.begin();
      {
        System.out.println("######### TransactionManager:Started #########");
        xaFile.newTransaction(txMngr);
        // ---------- Read the current value ----------
        xaFile.seek(MSG.length()); // seek to the end of the message
        byte result = xaFile.readByte();

        // ---------- Modify the current value ----------
        result++; // increment by 1

        // ---------- Write the new value to the file ----------
        xaFile.seek(MSG.length());
        xaFile.write(result);
        System.out.println("------ TX modifies data in the file ------");

        // Request a read operation whilst within a tx
        xaFile.seek(0);
        System.out.println("------ TX reads data in the file: <" + xaFile.readLine() + "> ------");

        System.out.println("--- Actual data in file: <" + readActualData() + "> ---");
      }
      txMngr.commit();
      System.out.println("######### TransactionManager:Committed #########\n");
      System.out.println("--- Actual data in file: <" + readActualData() + "> ---");
    } catch (Exception e) {
      txMngr.rollback();
      System.out.println("XXXXXXXX TransactionManager:Rolled-back XXXXXXXX\n");
      System.out.println("--- Actual data in file: <" + readActualData() + "> ---");      
      e.printStackTrace();
    }
    xaFile.close();
  }

  /**
   * Reads data from the file.
   *
   * @return a <code>String</code> containing the data in the file.    * 
   * @exception IOException if an I/O error occurs
   */
  private String readActualData() throws IOException {
    RandomAccessFile raf = new RandomAccessFile(FILENAME, "r");
    String line = raf.readLine();
    raf.close();
    return line;
  }

  public static void main(String[] args) throws Exception {
    new TXFilesDemoApp();
  }
}