package com.araguacaima.orpheusdb.core;

import com.araguacaima.orpheusdb.utils.OrpheusDbJPAEntityManagerUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

import static com.araguacaima.orpheusdb.utils.OrpheusDbJPAEntityManagerUtils.getVersionedClass;

@SuppressWarnings("unchecked")
@Aspect
public class OrpheusDbAspect {

    private static Logger log = LoggerFactory.getLogger(OrpheusDbJPAEntityManagerUtils.class);

    @Around("execution(* com.araguacaima.orpheusdb.utils.OrpheusDbJPAEntityManagerUtils.executeQuery(Class, String))")
    public <T> List<T> executeQueryClassQuery(ProceedingJoinPoint joinPoint) throws Throwable {
        if (!OrpheusDbPersistence.HAS_VERSIONED_CLASSES) {
            return (List<T>) joinPoint.proceed(joinPoint.getArgs());
        }
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
        if (!OrpheusDbPersistence.HAS_VERSIONED_CLASSES) {
            return (List<T>) joinPoint.proceed(joinPoint.getArgs());
        }
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
        if (!OrpheusDbPersistence.HAS_VERSIONED_CLASSES) {
            return (T) joinPoint.proceed(joinPoint.getArgs());
        }
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
        if (!OrpheusDbPersistence.HAS_VERSIONED_CLASSES) {
            return (T) joinPoint.proceed(joinPoint.getArgs());
        }
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
        if (!OrpheusDbPersistence.HAS_VERSIONED_CLASSES) {
            return (T) joinPoint.proceed(joinPoint.getArgs());
        }
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
        if (!OrpheusDbPersistence.HAS_VERSIONED_CLASSES) {
            return (T) joinPoint.proceed(joinPoint.getArgs());
        }
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