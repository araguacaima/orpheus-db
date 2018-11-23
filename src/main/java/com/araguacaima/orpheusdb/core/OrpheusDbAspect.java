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

    @Around("execution(* com.araguacaima.orpheusdb.utils.OrpheusDbJPAEntityManagerUtils.find(Class, Object))")
    public static <T> T findClassAndObject(ProceedingJoinPoint joinPoint) throws Throwable {
        Object[] args = joinPoint.getArgs();
        Class clazz = (Class) args[0];
        Object object = args[1];
        log.debug("Processing com.araguacaima.orpheusdb.utils.OrpheusDbJPAEntityManagerUtils.find(Class, Object) with values: ");
        log.debug("\tclass: " + clazz.getName());
        log.debug("\tkey: " + object.toString());
        return (T) joinPoint.proceed(args);
    }

    @Around("execution(* com.araguacaima.orpheusdb.utils.OrpheusDbJPAEntityManagerUtils.find(Object))")
    public static <T> T findEntity(ProceedingJoinPoint joinPoint) throws Throwable {
        Object[] args = joinPoint.getArgs();
        Object object = args[0];
        log.debug("Processing com.araguacaima.orpheusdb.utils.OrpheusDbJPAEntityManagerUtils.find(Class, Object) with values: ");
        log.debug("\tentity: " + object.toString());
        return (T) joinPoint.proceed(args);
    }

    @Around("execution(* com.araguacaima.orpheusdb.utils.OrpheusDbJPAEntityManagerUtils.executeQuery(Class, String))")
    public static <T> List<T> executeQueryClassQuery(ProceedingJoinPoint joinPoint) throws Throwable {
        Object[] args = joinPoint.getArgs();
        Class clazz = (Class) args[0];
        String query = (String) args[1];
        log.debug("Processing com.araguacaima.orpheusdb.utils.OrpheusDbJPAEntityManagerUtils.executeQuery(Class, String) with values: ");
        log.debug("\tclass: " + clazz.getName());
        log.debug("\tquery: " + query);
        return (List<T>) joinPoint.proceed(args);
    }

    @Around("execution(* com.araguacaima.orpheusdb.utils.OrpheusDbJPAEntityManagerUtils.executeQuery(Class, String, Object))")
    public static <T> List<T> executeQueryClassQueryParams(ProceedingJoinPoint joinPoint) throws Throwable {
        Object[] args = joinPoint.getArgs();
        Class clazz = (Class) args[0];
        String query = (String) args[1];
        Map<String, Object> params = (Map<String, Object>) args[2];
        log.debug("Processing com.araguacaima.orpheusdb.utils.OrpheusDbJPAEntityManagerUtils.executeQuery(Class, String) with values: ");
        log.debug("\tclass: " + clazz.getName());
        log.debug("\tquery: " + query);
        log.debug("\tparams: " + params);
        return (List<T>) joinPoint.proceed(args);
    }

    @Around("execution(* com.araguacaima.orpheusdb.utils.OrpheusDbJPAEntityManagerUtils.findByQuery(Class, String))")
    public static <T> T findByQueryClassQuery(ProceedingJoinPoint joinPoint) throws Throwable {
        Object[] args = joinPoint.getArgs();
        Class clazz = (Class) args[0];
        String query = (String) args[1];
        log.debug("Processing com.araguacaima.orpheusdb.utils.OrpheusDbJPAEntityManagerUtils.findByQuery(Class, Object) with values: ");
        log.debug("\tclass: " + clazz.getName());
        log.debug("\tquery: " + query);
        return (T) joinPoint.proceed(args);
    }

    @Around("execution(* com.araguacaima.orpheusdb.utils.OrpheusDbJPAEntityManagerUtils.findByQuery(Class, String, Object))")
    public static <T> T findByQueryClassQueryParams(ProceedingJoinPoint joinPoint) throws Throwable {
        Object[] args = joinPoint.getArgs();
        Class clazz = (Class) args[0];
        String query = (String) args[1];
        Map<String, Object> params = (Map<String, Object>) args[2];
        log.debug("Processing com.araguacaima.orpheusdb.utils.OrpheusDbJPAEntityManagerUtils.findByQuery(Class, Object) with values: ");
        log.debug("\tclass: " + clazz.getName());
        log.debug("\tquery: " + query);
        log.debug("\tparams: " + params);
        return (T) joinPoint.proceed(args);
    }

    @Around("execution(* com.araguacaima.orpheusdb.utils.OrpheusDbJPAEntityManagerUtils.findByNativeQuery(Class, String))")
    public static <T> T findByNativeQueryClassQuery(ProceedingJoinPoint joinPoint) throws Throwable {
        Object[] args = joinPoint.getArgs();
        Class clazz = (Class) args[0];
        String query = (String) args[1];
        log.debug("Processing com.araguacaima.orpheusdb.utils.OrpheusDbJPAEntityManagerUtils.findByNativeQuery(Class, Object) with values: ");
        log.debug("\tclass: " + clazz.getName());
        log.debug("\tquery: " + query);
        return (T) joinPoint.proceed(args);
    }

    @Around("execution(* com.araguacaima.orpheusdb.utils.OrpheusDbJPAEntityManagerUtils.findByNativeQuery(Class, String, Object))")
    public static <T> T findByNativeQueryClassQueryParams(ProceedingJoinPoint joinPoint) throws Throwable {
        Object[] args = joinPoint.getArgs();
        Class clazz = (Class) args[0];
        String query = (String) args[1];
        Map<String, Object> params = (Map<String, Object>) args[2];
        log.debug("Processing com.araguacaima.orpheusdb.utils.OrpheusDbJPAEntityManagerUtils.findByNativeQuery(Class, Object) with values: ");
        log.debug("\tclass: " + clazz.getName());
        log.debug("\tquery: " + query);
        log.debug("\tparams: " + params);
        return (T) joinPoint.proceed(args);
    }


}