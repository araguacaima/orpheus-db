package com.araguacaima.orpheusdb.core;

import com.araguacaima.orpheusdb.utils.OrpheusDbJPAEntityManagerUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

@SuppressWarnings("unchecked")
@Aspect
public class OrpheusDbAspect {


    private static Logger log = LoggerFactory.getLogger(OrpheusDbJPAEntityManagerUtils.class);

    private Class getVersionedClass(Class clazz) {
        Class versionedClass = null;
        String versionedPackageName = clazz.getPackage().getName() + OrpheusDbPersistence.GENERATED_PACKAGE;
        String versionedClassName = versionedPackageName + clazz.getSimpleName() + OrpheusDbPersistence.VERSIONABLE_SIMPLE_NAME;
        try {
            versionedClass = Class.forName(versionedClassName);
            log.debug("Class '" + clazz.getName() + "' is versioned by '" + versionedClass.getName() + ". Orpheus is going to intercept query in order to provide versioned data properly");
        } catch (Throwable t) {
            log.error(t.getMessage());
            log.debug("Class '" + clazz.getName() + "' is not versioned. Continuing with no intervention!");
        }
        return versionedClass;
    }

    @Around("execution(* com.araguacaima.orpheusdb.utils.OrpheusDbJPAEntityManagerUtils.find(Class, Object))")
    public <T> T findClassAndObject(ProceedingJoinPoint joinPoint) throws Throwable {
        Object[] args = joinPoint.getArgs();
        Class<T> clazz = (Class<T>) args[0];
        Object object = args[1];
        log.debug("Processing c.a.o.u.OrpheusDbJPAEntityManagerUtils.find(Class, Object) with values: class '" + clazz.getName() + "' and key '" + object.toString() + "'");
        Class versionedClass = getVersionedClass(clazz);
        if (versionedClass == null) {
            return (T) joinPoint.proceed(args);
        } else {
            return OrpheusDbJPAEntityManagerUtils.findVersioned(clazz, object);
        }
    }

    @Around("execution(* com.araguacaima.orpheusdb.utils.OrpheusDbJPAEntityManagerUtils.find(Object))")
    public <T> T findEntity(ProceedingJoinPoint joinPoint) throws Throwable {
        Object[] args = joinPoint.getArgs();
        T object = (T) args[0];
        Class<T> clazz = (Class<T>) object.getClass();
        log.debug("Processing c.a.o.u.OrpheusDbJPAEntityManagerUtils.find(Object) with values: entity '" + clazz.getName() + "'");
        Class versionedClass = getVersionedClass(clazz);
        if (versionedClass == null) {
            return (T) joinPoint.proceed(args);
        } else {
            return OrpheusDbJPAEntityManagerUtils.findVersioned(object);
        }
    }

    @Around("execution(* com.araguacaima.orpheusdb.utils.OrpheusDbJPAEntityManagerUtils.executeQuery(Class, String))")
    public <T> List<T> executeQueryClassQuery(ProceedingJoinPoint joinPoint) throws Throwable {
        Object[] args = joinPoint.getArgs();
        Class<T> clazz = (Class<T>) args[0];
        String query = (String) args[1];
        log.debug("Processing c.a.o.u.OrpheusDbJPAEntityManagerUtils.executeQuery(Class, String) with values: class '" + clazz.getName() + "' and query '" + query + "'");
        Class versionedClass = getVersionedClass(clazz);
        if (versionedClass == null) {
            return (List<T>) joinPoint.proceed(args);
        } else {
            return OrpheusDbJPAEntityManagerUtils.executeQueryVersioned(clazz, query);
        }
    }

    @Around("execution(* com.araguacaima.orpheusdb.utils.OrpheusDbJPAEntityManagerUtils.executeQuery(Class, String, Object))")
    public <T> List<T> executeQueryClassQueryParams(ProceedingJoinPoint joinPoint) throws Throwable {
        Object[] args = joinPoint.getArgs();
        Class<T> clazz = (Class<T>) args[0];
        String query = (String) args[1];
        Map<String, Object> params = (Map<String, Object>) args[2];
        log.debug("Processing c.a.o.u.OrpheusDbJPAEntityManagerUtils.executeQuery(Class, String, Object) with values: class '" + clazz.getName() + "', query '" + query + "' and params '" + params + "'");
        Class versionedClass = getVersionedClass(clazz);
        if (versionedClass == null) {
            return (List<T>) joinPoint.proceed(args);
        } else {
            return OrpheusDbJPAEntityManagerUtils.executeQueryVersioned(clazz, query, params);
        }
    }

    @Around("execution(* com.araguacaima.orpheusdb.utils.OrpheusDbJPAEntityManagerUtils.findByQuery(Class, String))")
    public <T> T findByQueryClassQuery(ProceedingJoinPoint joinPoint) throws Throwable {
        Object[] args = joinPoint.getArgs();
        Class<T> clazz = (Class<T>) args[0];
        String query = (String) args[1];
        log.debug("Processing c.a.o.u.OrpheusDbJPAEntityManagerUtils.findByQuery(Class, String) with values: class '" + clazz.getName() + "' and query '" + query + "'");
        Class versionedClass = getVersionedClass(clazz);
        if (versionedClass == null) {
            return (T) joinPoint.proceed(args);
        } else {
            return OrpheusDbJPAEntityManagerUtils.findByQueryVersioned(clazz, query);
        }
    }

    @Around("execution(* com.araguacaima.orpheusdb.utils.OrpheusDbJPAEntityManagerUtils.findByQuery(Class, String, Object))")
    public <T> T findByQueryClassQueryParams(ProceedingJoinPoint joinPoint) throws Throwable {
        Object[] args = joinPoint.getArgs();
        Class<T> clazz = (Class<T>) args[0];
        String query = (String) args[1];
        Map<String, Object> params = (Map<String, Object>) args[2];
        log.debug("Processing c.a.o.u.OrpheusDbJPAEntityManagerUtils.findByQuery(Class, String, Object) with values: class '" + clazz.getName() + "', query '" + query + "' and params '" + params + "'");
        Class versionedClass = getVersionedClass(clazz);
        if (versionedClass == null) {
            return (T) joinPoint.proceed(args);
        } else {
            return OrpheusDbJPAEntityManagerUtils.findByQueryVersioned(clazz, query, params);
        }
    }

    @Around("execution(* com.araguacaima.orpheusdb.utils.OrpheusDbJPAEntityManagerUtils.findByNativeQuery(Class, String))")
    public <T> T findByNativeQueryClassQuery(ProceedingJoinPoint joinPoint) throws Throwable {
        Object[] args = joinPoint.getArgs();
        Class<T> clazz = (Class<T>) args[0];
        String query = (String) args[1];
        log.debug("Processing c.a.o.u.OrpheusDbJPAEntityManagerUtils.findByNativeQuery(Class, String) with values: class '" + clazz.getName() + "' and query '" + query + "'");
        Class versionedClass = getVersionedClass(clazz);
        if (versionedClass == null) {
            return (T) joinPoint.proceed(args);
        } else {
            return OrpheusDbJPAEntityManagerUtils.findByNativeQueryVersioned(clazz, query);
        }
    }

    @Around("execution(* com.araguacaima.orpheusdb.utils.OrpheusDbJPAEntityManagerUtils.findByNativeQuery(Class, String, Object))")
    public <T> T findByNativeQueryClassQueryParams(ProceedingJoinPoint joinPoint) throws Throwable {
        Object[] args = joinPoint.getArgs();
        Class<T> clazz = (Class<T>) args[0];
        String query = (String) args[1];
        Map<String, Object> params = (Map<String, Object>) args[2];
        log.debug("Processing c.a.o.u.OrpheusDbJPAEntityManagerUtils.findByNativeQuery(Class, String, Object) with values: class '" + clazz.getName() + "', query '" + query + "' and params '" + params + "'");
        Class versionedClass = getVersionedClass(clazz);
        if (versionedClass == null) {
            return (T) joinPoint.proceed(args);
        } else {
            return OrpheusDbJPAEntityManagerUtils.findByNativeQueryVersioned(clazz, query, params);
        }
    }


}