package com.arjuna.demo.recovery.xaresource;

import java.io.IOException;
import java.io.Serializable;

/**
 * This is a trivial implementation of a Serializable XAResource. It will crash (<CODE>System.exit</CODE>) when commit
 * is called on it for the first time and will then complete normally when it is recovered.
 *
 * @author  Arnaud Simon <Arnaud.Simon@arjuna.com>
 */
public class ExampleXAResource extends NonSerializableExampleXAResource implements Serializable
{

   //-----------------------------------------------------------------------------------------
   //---------------  Class Constructors
   //-----------------------------------------------------------------------------------------

   /**
    * Create a new ExampleXAResource. This resource is used to crash the VM.
    *
    * @param name          The name to associate with the XA resource.
    */
   public ExampleXAResource(String name)
   {
      super(name, false);
      log("ExampleXAResource (Constructor) name: " + name);
   }

   //-----------------------------------------------------------------------------------------
   //---------------  Utility methods
   //-----------------------------------------------------------------------------------------

   /**
    * Override default serialization code.
    *
    * @param out           The stream to write the state of this object to.
    * @throws IOException  Not thrown by this implementation.
    */
   private void writeObject(java.io.ObjectOutputStream out)
         throws IOException
   {
      log("Serialized");
      out.writeUTF( getName() );
   }

   /**
    * Override default serialization code.
    *
    * @param in                        The stream to read the state of this object from.
    * @throws IOException              Not thrown by this implementation.
    */
   private void readObject(java.io.ObjectInputStream in)
         throws IOException
   {
      setMame( in.readUTF() );
      setRecovered( true );
      log("Deserialized");
   }
}