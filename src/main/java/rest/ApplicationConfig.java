/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rest;

import java.util.Set;
import javax.ws.rs.core.Application;

/**
 *
 * @author plaul1
 */
@javax.ws.rs.ApplicationPath("api")
public class ApplicationConfig extends Application {

    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> resources = new java.util.HashSet<>();
        addRestResourceClasses(resources);
        return resources;
    }

    /**
     * Do not modify addRestResourceClasses() method.
     * It is automatically populated with
     * all resources defined in the project.
     * If required, comment out calling this method in getClasses().
     */
    private void addRestResourceClasses(Set<Class<?>> resources) {
        resources.add(rest.Admin.class);
        resources.add(rest.AllRoles.class);
        resources.add(rest.GenericResource.class);
        resources.add(rest.Login.class);
        resources.add(rest.RememoteLogin.class);
        resources.add(rest.ScriptService.class);
        resources.add(rest.Student.class);
        resources.add(rest.StudyAdministration.class);
        resources.add(rest.TestResource.class);
        resources.add(rest.ThirdPartAccess.class);
        resources.add(rest.errorhandling.AuthenticationExceptionMapper.class);
        resources.add(rest.errorhandling.ConstraintValidationExceptionMapper.class);
        resources.add(rest.errorhandling.GenericExceptionMapper.class);
        resources.add(rest.errorhandling.NonexistentEntityExceptionMapper.class);
        resources.add(rest.errorhandling.NotAuthorizedExceptionMapper.class);
        resources.add(rest.errorhandling.PreExistentEntityExceptionMapper.class);
        resources.add(rest.errorhandling.ScriptExceptionMapper.class);
        resources.add(rest.errorhandling.SecurityExceptionMapper.class);
        resources.add(rest.errorhandling.StudyPointExceptionHandler.class);
        resources.add(security.JWTAuthenticationFilter.class);
        resources.add(security.ResetPasswordService.class);
        resources.add(security.RolesAllowedFilter.class);
    }
    
}
