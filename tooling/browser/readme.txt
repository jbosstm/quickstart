/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2014, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

OVERVIEW
--------

Interactive browser for examining transaction log MBeans

USAGE
-----
mvn -e compile exec:java -Dexec.mainClass=org.jboss.narayana.jta.quickstarts.BrowserCommand -Dexec.args="path to tx-object-store"

EXPECTED OUTPUT
---------------
help - show command options and syntax
quit - exit the browser
store_dir - get/set the location of the object store
probe - refresh the view of the object store
exception_trace - true | false - show full exception traces
types - list record types
select - <type> - start browsing a particular transaction type
ls - [type] - list transactions of type type. Use the select command to set the default type
> 

WHAT JUST HAPPENED?
-------------------
The quickstart shows how to browse transaction record MBeans. Starting the example presents a list of
possible commands and instructions on how to proceed. Type help and press return for help on all commands.
