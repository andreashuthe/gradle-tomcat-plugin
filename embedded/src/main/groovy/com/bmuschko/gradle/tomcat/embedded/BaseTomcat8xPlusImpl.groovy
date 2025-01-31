package com.bmuschko.gradle.tomcat.embedded

import java.lang.reflect.Constructor

/**
 * Base Tomcat 8x and higher implementation.
 */
abstract class BaseTomcat8xPlusImpl extends BaseTomcat7xPlusImpl {
    @Override
    void setRealm(realm) {
        tomcat.engine.realm = realm
    }

    @Override
    void createContext(String fullContextPath, String webAppPath) {
        super.createContext(fullContextPath, webAppPath)
        logger.info "webapp path ${webAppPath}"
        final Class standardRootClass = loadClass('org.apache.catalina.webresources.StandardRoot')
        final Class contextClass = loadClass('org.apache.catalina.Context')
        final Constructor constructor = standardRootClass.getConstructor(contextClass)
        context.resources = constructor.newInstance(context)
        final Class jasperInitializer = loadClass('org.apache.jasper.servlet.JasperInitializer')
        context.addServletContainerInitializer(jasperInitializer.newInstance(), null)
    }

    @Override
    void addWebappResource(File resource) {
        logger.info "webappres start ${resource.getName()}"
        if (resource.getName().endsWith(".jar")) {
            context.resources.createWebResourceSet(getResourceSetType('CLASSES_JAR'), '/WEB-INF/classes', resource.toURI().toURL(), '/')
        } else {
            context.resources.createWebResourceSet(getResourceSetType('PRE'), '/WEB-INF/classes', resource.toURI().toURL(), '/')
        }
        logger.info "webappres end"
    }

    def getResourceSetType(String name) {
        final Class resourceSetTypeClass = loadClass('org.apache.catalina.WebResourceRoot$ResourceSetType')
        resourceSetTypeClass.enumConstants.find { it.name() == name }
    }

    void setResourcesCacheSize(int cacheSize) {
        if (cacheSize > 0) {
            context.resources.cacheMaxSize = cacheSize
        } else if (cacheSize < 0) {
            context.resources.cachingAllowed = false
        }
    }
}
