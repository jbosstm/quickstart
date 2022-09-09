/*
 * JBoss, Home of Professional Open Source
 * Copyright 2018, Red Hat, Inc. and/or its affiliates, and individual
 * contributors by the @authors tag. See the copyright.txt in the
 * distribution for a full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.narayana.quickstarts.cmr.web;

import java.io.IOException;
import java.io.PrintWriter;

import jakarta.inject.Inject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.jboss.narayana.quickstarts.cmr.BookEntity;
import org.jboss.narayana.quickstarts.cmr.BookProcessorCmr;

@WebServlet(name="BookServlet", urlPatterns={"/servlet"})
public class BookServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Inject
    private BookProcessorCmr bookProcessor;
        
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        out.println("<h1>Book repository</h1>");

        String action = request.getParameter("action");
        if(action == null) action = "get";
        String title = request.getParameter("title");

        switch(action) {
            case "set":
                if(title == null) {
                    out.println("Parameter 'title' was not set. No book added.");
                }
                bookProcessor.fileBook(title);
                out.printf("<b>Book title %s was saved.%n", title);
                break;
            case "get":
            default:
                out.println("<table><th><td>Id</td><td>Title</td></th>");
                for(BookEntity book: bookProcessor.getBooks()) {
                    out.printf("<tr><td>%s</td><td>%s</td></tr>%n", book.getId(), book.getTitle());
                }
                out.print("</table>");
        }
    }
}
