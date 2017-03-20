/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat, Inc. and/or its affiliates,
 * and individual contributors as indicated by the @author tags.
 * See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU Lesser General Public License, v. 2.1.
 * This program is distributed in the hope that it will be useful, but WITHOUT A
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License,
 * v.2.1 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA  02110-1301, USA.
 *
 * (C) 2014,
 * @author JBoss, by Red Hat.
 */
package org.jboss.narayana.jta.quickstarts;

import java.io.IOException;
import java.io.PrintStream;
import java.util.*;

import com.arjuna.ats.arjuna.common.CoreEnvironmentBean;
import com.arjuna.ats.arjuna.common.ObjectStoreEnvironmentBean;
import com.arjuna.ats.arjuna.common.RecoveryEnvironmentBean;
import com.arjuna.ats.arjuna.objectstore.StoreManager;
import com.arjuna.ats.arjuna.recovery.RecoveryManager;
import com.arjuna.ats.arjuna.state.InputObjectState;
import com.arjuna.ats.arjuna.tools.osb.mbean.*;
import com.arjuna.ats.arjuna.tools.osb.util.JMXServer;
import com.arjuna.common.internal.util.propertyservice.BeanPopulator;

import javax.management.*;

public abstract class BrowserCommand {
    private static String currentStoreDir;// = "/home/mmusgrov/tmp/tx-object-store";
    private static ObjStoreBrowser osb;
    private static String currentType = "";
    private static List<String> recordTypes = new ArrayList<String> ();

    private enum CommandName {
        HELP("show command options and syntax"),
        SELECT("<type> - start browsing a particular transaction type"),
        STORE_DIR("get/set the location of the object store"),
        START(null),
        TYPES("list record types"),
        PROBE("refresh the view of the object store"),
        LS("[type] - list transactions of type type. Use the select command to set the default type"),
        QUIT("exit the browser"),
        EXCEPTION_TRACE("true | false - show full exception traces");

        String cmdHelp;

        CommandName(String cmdHelp) {
            this.cmdHelp = cmdHelp;
        }
    }

    static BrowserCommand getCommand(CommandName name) {
        return getCommand(name.name());
    }

    static BrowserCommand getCommand(String name) {
        name = name.toUpperCase();

        for (BrowserCommand command : commands) {
            if (command.name.name().startsWith(name))
                return command;
        }

        return getCommand(CommandName.HELP);
    }

    public static void main(String[] args) throws Exception {
        BrowserCommand.getCommand(CommandName.START).execute(new PrintStream(System.out, true), Arrays.asList(args));
    }


    CommandName name;
    boolean verbose;

    private BrowserCommand(CommandName name) {
        this.name = name;
    }

    abstract void execute(PrintStream printStream, List<String> args) throws Exception;
    protected void help(PrintStream printStream) {
        if (name.cmdHelp != null)
            printStream.printf("%s - %s%n", name.name().toLowerCase(), name.cmdHelp);
    }

    protected boolean cancel() {return true;}

    private static void setupStore(String storeDir) throws Exception {
        BeanPopulator.getDefaultInstance(RecoveryEnvironmentBean.class).setRecoveryModuleClassNames(Arrays.asList(
                "com.arjuna.ats.internal.jta.recovery.arjunacore.CommitMarkableResourceRecordRecoveryModule",
                "com.arjuna.ats.internal.arjuna.recovery.AtomicActionRecoveryModule",
                "com.arjuna.ats.internal.jta.recovery.arjunacore.XARecoveryModule"
        ));
        BeanPopulator.getDefaultInstance(ObjectStoreEnvironmentBean.class).setObjectStoreDir(storeDir);
        BeanPopulator.getNamedInstance(ObjectStoreEnvironmentBean.class, "communicationStore").setObjectStoreDir(storeDir);
        BeanPopulator.getDefaultInstance(CoreEnvironmentBean.class).setNodeIdentifier("no-recovery");

        osb = new ObjStoreBrowser(storeDir);
        osb.start(); // only required if we want to use JMX
    }

    private static BrowserCommand[] commands = {

            new BrowserCommand(CommandName.HELP) {
                @Override
                void execute(PrintStream printStream, List<String> args) {
                    for (BrowserCommand command : commands)
                        command.help(printStream);
                }
            },

            new BrowserCommand(CommandName.START) {
                boolean finished;

                @Override
                void execute(PrintStream printStream, List<String> args) throws Exception {
                    if (args.size() == 0)
                        currentStoreDir = BeanPopulator.getDefaultInstance(ObjectStoreEnvironmentBean.class).getObjectStoreDir();
                    else
                        currentStoreDir = args.get(0);

                    setupStore(currentStoreDir);

                    RecoveryManager.manager(RecoveryManager.INDIRECT_MANAGEMENT).suspend(true);
                    Scanner scanner = new Scanner(System.in);

                    while (!finished)
                        processCommand(printStream, scanner);

                    RecoveryManager.manager().terminate();
                    StoreManager.shutdown();
                }

                protected boolean cancel() {
                    finished = true;
                    return true;
                }

                private void processCommand(PrintStream printStream, Scanner scanner) {
                    printStream.printf("%s> ", currentType);

                    List<String> args = new ArrayList<String> (Arrays.asList(scanner.nextLine().split("\\s+")));
                    BrowserCommand command = args.size() == 0 ? getCommand(CommandName.HELP) : getCommand(args.remove(0));

                    try {
                        command.execute(printStream, args);
                    } catch (Exception e) {
                        printStream.printf("%s%n", e.getMessage());

                        if (verbose)
                            e.printStackTrace(printStream);
                    }
                }
            },

            new BrowserCommand(CommandName.QUIT) {

                @Override
                void execute(PrintStream printStream, List<String> args) throws Exception {
                    getCommand(CommandName.START).cancel();
                }
            },

            new BrowserCommand(CommandName.STORE_DIR) {

                @Override
                void execute(PrintStream printStream, List<String> args) throws Exception {
                    if (args.size() == 0)
                        printStream.print(currentStoreDir);

                    setupStore(args.get(0));
                }
            },

            new BrowserCommand(CommandName.PROBE) {

                @Override
                void execute(PrintStream printStream, List<String> args) {
                    osb.probe();
                }
            },

            new BrowserCommand(CommandName.EXCEPTION_TRACE) {

                @Override
                void execute(PrintStream printStream, List<String> args) throws Exception {
                    BrowserCommand startCmd = getCommand(CommandName.START);

                    if (args.size() == 1)
                        startCmd.verbose = Boolean.parseBoolean(args.get(0));

                    printStream.printf("exceptionTrace is %b", startCmd.verbose);
                }
            },

            new BrowserCommand(CommandName.TYPES) {
                @Override
                void execute(PrintStream printStream, List<String> args) throws Exception {
                    recordTypes.clear();

                    InputObjectState types = new InputObjectState();

                    if (StoreManager.getRecoveryStore().allTypes(types)) {
                        String typeName;

                        do {
                            try {
                                typeName = types.unpackString();
                                recordTypes.add(typeName);
                                printStream.printf("%s%n", typeName);
                            } catch (IOException e1) {
                                typeName = "";
                            }
                        } while (typeName.length() != 0);
                    }
                }
            },

            new BrowserCommand(CommandName.SELECT) {

                @Override
                void execute(PrintStream printStream, List<String> args) throws Exception {
                    if (args.size() < 1)
                        currentType = "";
                    else if (!recordTypes.contains(args.get(0)))
                        printStream.printf("%s is not a valid transaction type%n", args.get(0));
                    else
                        currentType = args.get(0);
                }
            },

            new BrowserCommand(CommandName.LS) {
                @Override
                void execute(PrintStream printStream, List<String> args) throws Exception {
                    if (args.size() > 0)
                        getCommand(CommandName.SELECT).execute(printStream, args);

                    if (currentType.length() == 0) {
                        printStream.printf("No type selected. Choose one of:%n");
                        getCommand(CommandName.TYPES).execute(printStream, null);
                        help(printStream);
                    } else {
                        //List<UidWrapper> uids = osb.probe(currentType);
                        listMBeans(printStream, currentType);
                    }
                }

                void listMBeans(PrintStream printStream, String itype) throws MalformedObjectNameException, ReflectionException, InstanceNotFoundException, IntrospectionException {
                    MBeanServer mbs = JMXServer.getAgent().getServer();
                    String osMBeanName = "jboss.jta:type=ObjectStore,itype=" + itype;
                    //Set<ObjectInstance> allTransactions = mbs.queryMBeans(new ObjectName("jboss.jta:type=ObjectStore,*"), null);
                    Set<ObjectInstance> transactions = mbs.queryMBeans(new ObjectName(osMBeanName + ",*"), null);

                    printStream.printf("Transactions of type %s%n", osMBeanName);
                    for (ObjectInstance oi : transactions) {
                        String transactionId = oi.getObjectName().getCanonicalName();

                        if (!transactionId.contains("puid") && transactionId.contains("itype")) {
                            printStream.printf("Transaction: %s%n", oi.getObjectName());
                            String participantQuery =  transactionId + ",puid=*";
                            Set<ObjectInstance> participants = mbs.queryMBeans(new ObjectName(participantQuery), null);

                            printAtrributes(printStream, "\t", mbs, oi);

                            printStream.printf("\tParticipants:%n");
                            for (ObjectInstance poi : participants) {
                                printStream.printf("\t\tParticipant: %s%n", poi);
                                printAtrributes(printStream, "\t\t\t", mbs, poi);
                            }
                        }
                    }
                }

                void printAtrributes(PrintStream printStream, String printPrefix, MBeanServer mbs, ObjectInstance oi)
                        throws IntrospectionException, InstanceNotFoundException, ReflectionException {
                    MBeanInfo info = mbs.getMBeanInfo( oi.getObjectName() );
                    MBeanAttributeInfo[] attributeArray = info.getAttributes();
                    int i = 0;
                    String[] attributeNames = new String[attributeArray.length];

                    for (MBeanAttributeInfo ai : attributeArray)
                        attributeNames[i++] = ai.getName();

                    AttributeList attributes = mbs.getAttributes(oi.getObjectName(), attributeNames);

                    for (javax.management.Attribute attribute : attributes.asList()) {
                        Object value = attribute.getValue();
                        String v =  value == null ? "null" : value.toString();

                        printStream.printf("%s%s=%s%n", printPrefix, attribute.getName(), v);
                    }
                }
            },
    };
}
