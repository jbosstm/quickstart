/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013, Red Hat, Inc. and/or its affiliates, and individual
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
package org.jboss.narayana.quickstart.rest.bridge.inbound.jpa.jaxrs;

import java.net.URI;
import java.util.List;

import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import jakarta.ws.rs.core.UriInfo;

import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import org.jboss.narayana.quickstart.rest.bridge.inbound.jpa.model.Task;
import org.jboss.narayana.quickstart.rest.bridge.inbound.jpa.model.TaskDao;
import org.jboss.narayana.quickstart.rest.bridge.inbound.jpa.model.UserTable;
import org.jboss.narayana.quickstart.rest.bridge.inbound.jpa.model.UserTableDao;

/**
 * A JAX-RS resource for exposing REST endpoints for Task manipulation
 * 
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 * 
 */
@Path("/")
@Stateless
public class TaskResource {

    public static final String USERS_PATH_SEGMENT = "users";

    public static final String TASKS_PATH_SEGMENT = "tasks";

    @EJB
    private UserTableDao userTableDao;

    @EJB
    private TaskDao taskDao;

    @POST
    @Path(USERS_PATH_SEGMENT + "/{username}")
    public Response createUser(@PathParam("username") String username) {
        getUser(username);

        return Response.status(201).build();
    }

    @DELETE
    @Path(USERS_PATH_SEGMENT)
    public void deleteUsers() {
        userTableDao.deleteUsers();
    }

    @POST
    @Path(TASKS_PATH_SEGMENT + "/{username}/{title}")
    @TransactionAttribute
    public Response createTask(@Context UriInfo info, @PathParam("username") String username,
            @PathParam("title") String taskTitle) {

        UserTable user = getUser(username);
        Task task = new Task(taskTitle);

        taskDao.createTask(user, task);

        // Construct the URI for the newly created resource and put in into the Location header of the response
        // (assumes that there is only one occurrence of the task title in the request)
        String rawPath = info.getAbsolutePath().getRawPath().replace(task.getTitle(), task.getId().toString());
        UriBuilder uriBuilder = info.getAbsolutePathBuilder().replacePath(rawPath);
        URI uri = uriBuilder.build();

        return Response.created(uri).build();
    }

    @DELETE
    @Path(TASKS_PATH_SEGMENT + "/{username}/{id}")
    public void deleteTaskById(@PathParam("username") String username, @PathParam("id") Long id) {
        UserTable user = getUser(username);
        Task task = getTask(user, id);
        taskDao.deleteTask(task);
    }

    @DELETE
    @Path(TASKS_PATH_SEGMENT)
    public void deleteAllTasks() {
        taskDao.deleteTasks();
    }

    @GET
    @Path(TASKS_PATH_SEGMENT + "/{username}/{id}")
    @Produces({MediaType.APPLICATION_JSON})
    public String getTaskById(@PathParam("username") String username, @PathParam("id") Long id) {
        UserTable user = getUser(username);
        return getTask(user, id).toJson().toString();
    }

    @GET
    @Path(TASKS_PATH_SEGMENT + "/{username}/{title}")
    @Produces({MediaType.APPLICATION_JSON})
    public String getTasksByTitle(@PathParam("username") String username, @PathParam("title") String title) {
        final JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
        List<Task> tasks = getTasks(getUser(username), title);

        for (Task task : tasks) {
            arrayBuilder.add(task.toJson());
        }

        return arrayBuilder.build().toString();
    }

    @GET
    @Path(TASKS_PATH_SEGMENT + "/{username}")
    @Produces({MediaType.APPLICATION_JSON})
    public String getTasks(@PathParam("username") String username) {
        final JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
        List<Task> tasks = getTasks(getUser(username));

        for (Task task : tasks) {
            arrayBuilder.add(task.toJson());
        }

        return arrayBuilder.build().toString();
    }

    // Utility Methods

    private List<Task> getTasks(UserTable user, String title) {
        return taskDao.getForTitle(user, title);
    }

    private List<Task> getTasks(UserTable user) {
        return taskDao.getAll(user);
    }

    private Task getTask(UserTable user, Long id) {
        for (Task task : taskDao.getAll(user))
            if (task.getId().equals(id))
                return task;

        throw new WebApplicationException(Response.Status.NOT_FOUND);
    }

    private UserTable getUser(String username) {
        try {
            UserTable user = userTableDao.getForUsername(username);

            if (user == null) {
                user = new UserTable(username);

                userTableDao.createUser(user);
            }

            return user;
        } catch (Exception e) {
            throw new WebApplicationException(e);
        }
    }

}
