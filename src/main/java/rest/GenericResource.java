/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rest;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.Path;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("utils")
public class GenericResource {

    @Context
    private UriInfo context;

    
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public String getJson(@HeaderParam("X-Forwarded-For") String who) {   
        return String.format("{\"whoIAM\": \"%s\"}",who);
    }
}
