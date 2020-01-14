package com.araguacaima.orpheusdb.utils;

import com.araguacaima.commons.utils.ReflectionUtils;
import com.araguacaima.commons.utils.StringUtils;
import com.araguacaima.orpheusdb.core.OrpheusDbPersistence;
import com.araguacaima.orpheusdb.helpers.AnnotationHelper;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.*;
import javax.transaction.*;
import javax.transaction.RollbackException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@SuppressWarnings({"WeakerAccess", "unused"})
public class OrpheusDbJPAEntityManagerUtils {
    //private static TransactionManager transactionManager;
    private static EntityManagerFactory entityManagerFactory;
    private static EntityManager entityManager;
    private static boolean autocommit = true;
    public static final String VERSIONABLE_OBJECT = "VersionableObject";

    @Transient
    @JsonIgnore
    private static ReflectionUtils reflectionUtils = ReflectionUtils.getInstance();

    private static Logger log = LoggerFactory.getLogger(OrpheusDbJPAEntityManagerUtils.class);

    public static void closeAll() {
        try {
            close(true);
        } catch (SystemException | NotSupportedException e) {
            e.printStackTrace();
        }
    }

    public static void close() {
        try {
            close(false);
        } catch (SystemException | NotSupportedException e) {
            e.printStackTrace();
        }
    }

    private static void close(boolean cloaseAll) throws SystemException, NotSupportedException {
        if (entityManager != null) {
            if (entityManager.isOpen()) {
                entityManager.clear();
                entityManager.close();
               // transactionManager = com.arjuna.ats.jta.TransactionManager.transactionManager();
               // transactionManager.begin();
                entityManager = entityManagerFactory.createEntityManager();
            }
        }
        if (entityManagerFactory != null) {
            if (entityManagerFactory.isOpen()) {
                entityManagerFactory.close();
            }
        }
    }

    public static void init(String persistenceUnitName, Map<String, String> map) throws SystemException, NotSupportedException {
        entityManagerFactory = OrpheusDbPersistence.createEntityManagerFactory(persistenceUnitName, map);
        //transactionManager = com.arjuna.ats.jta.TransactionManager.transactionManager();
        //transactionManager.begin();
        entityManager = entityManagerFactory.createEntityManager();
        entityManager.unwrap(Session.class);
    }

    public static <T> T find(Class<T> clazz, Object key) {
        return entityManager.find(clazz, key);
    }

    @SuppressWarnings("unchecked")
    public static <T> T find(T entity) {
        try {
            Object key = extractId(entity);
            if (key != null) {
                return find((Class<T>) entity.getClass(), key);
            }
        } catch (Throwable t) {
            log.error(t.getMessage());
            throw new RuntimeException(t);
        }
        return null;
    }

    public static <T> List<T> executeQuery(Class<T> clazz, String query) {
        return executeQuery(clazz, query, null);
    }

    public static <T> List<T> executeQuery(Class<T> clazz, String query, Map<String, Object> params) {
        try {
            TypedQuery<T> namedQuery = entityManager.createNamedQuery(query, clazz);
            if (params != null) {
                for (Map.Entry<String, Object> param : params.entrySet()) {
                    try {
                        namedQuery.setParameter(param.getKey(), param.getValue());
                    } catch (Throwable ignored) {
                    }
                }
            }
            try {
                return namedQuery.getResultList();
            } catch (javax.persistence.NoResultException ignored) {
                return null;
            }
        } catch (Throwable t) {
            try {
                rollback();
            } catch (Throwable ignored) {
                log.warn("Closing transaction!");
                close();
            }
            throw t;
        }
    }

    public static <T> T findByQuery(Class<T> clazz, String query) {
        return findByQuery(clazz, query, null);
    }

    public static <T> T findByQuery(Class<T> clazz, String query, Map<String, Object> params) {
        try {
            TypedQuery<T> namedQuery = entityManager.createNamedQuery(query, clazz);
            if (params != null) {
                for (Map.Entry<String, Object> param : params.entrySet()) {
                    try {
                        namedQuery.setParameter(param.getKey(), param.getValue());
                    } catch (Throwable ignored) {
                    }
                }
            }
            try {
                return namedQuery.getSingleResult();
            } catch (javax.persistence.NoResultException ignored) {
                return null;
            }
        } catch (Throwable t) {
            try {
                rollback();
            } catch (Throwable ignored) {
                log.warn("Closing transaction!");
                close();
            }
            throw t;
        }
    }

    public static <T> T findByNativeQuery(Class<T> clazz, String query) {
        return findByNativeQuery(clazz, query, null);
    }

    @SuppressWarnings("unchecked")
    public static <T> T findByNativeQuery(Class<T> clazz, String query, Map<String, Object> params) {
        try {
            Query namedQuery = entityManager.createNativeQuery(query, clazz);
            if (params != null) {
                for (Map.Entry<String, Object> param : params.entrySet()) {
                    try {
                        namedQuery.setParameter(param.getKey(), param.getValue());
                    } catch (Throwable ignored) {
                    }
                }
            }
            try {
                Object singleResult = namedQuery.getSingleResult();
                return (T) singleResult;
            } catch (javax.persistence.NoResultException ignored) {
                return null;
            }
        } catch (Throwable t) {
            try {
                rollback();
            } catch (Throwable ignored) {
                log.warn("Closing transaction!");
                close();
            }
            throw t;
        }
    }

    public static <T> List<T> findListByNativeQuery(Class<T> clazz, String query) {
        return findListByNativeQuery(clazz, query, null);
    }

    public static <T> List<T> findListByNativeQuery(Class<T> clazz, String query, Map<String, Object> params) {
        try {
            Query namedQuery = entityManager.createNativeQuery(query, clazz);
            if (params != null) {
                for (Map.Entry<String, Object> param : params.entrySet()) {
                    try {
                        namedQuery.setParameter(param.getKey(), param.getValue());
                    } catch (Throwable ignored) {
                    }
                }
            }
            try {
                return namedQuery.getResultList();
            } catch (javax.persistence.NoResultException ignored) {
                return null;
            }
        } catch (Throwable t) {
            try {
                rollback();
            } catch (Throwable ignored) {
                log.warn("Closing transaction!");
                close();
            }
            throw t;
        }
    }

    public static <T> T merge(T entity) throws SystemException, NotSupportedException {
        return merge(entity, getAutocommit());
    }

    public static <T> T merge(T entity, boolean autocommit) throws SystemException, NotSupportedException {
        if (entity == null) {
            return null;
        }
        try {
            if (autocommit) {
                begin();
            }
            Object key = extractId(entity);
            if (key != null && find(entity.getClass(), key) == null) {
                logProcessing(entity, "persist");
                entityManager.persist(entity);
                if (log.isDebugEnabled()) {
                    log.debug("Done!");
                }
            } else {
                logProcessing(entity, "merge");
                entity = entityManager.merge(entity);
                if (log.isDebugEnabled()) {
                    log.debug("Done!");
                }
            }
            if (autocommit) {
                commit();
            }
        } catch (Throwable t) {
            log.error(t.getMessage());
            if (autocommit) {
                rollback();
            }
            throw new RuntimeException(t);
        }
        return entity;
    }

    public static <T> Object extractId(T entity) throws IllegalAccessException {
        Collection<Field> fields = reflectionUtils.getAllFieldsIncludingParents(entity);
        for (Field field : fields) {
            if (field.getAnnotation(Id.class) != null || field.getAnnotation(EmbeddedId.class) != null) {
                field.setAccessible(true);
                return field.get(entity);
            }
        }
        return null;
    }

    public static void persist(Object entity) throws HeuristicRollbackException, RollbackException, NotSupportedException, HeuristicMixedException, SystemException {
        persist(entity, getAutocommit());
    }

    public static void persist(Object entity, boolean autocommit) throws HeuristicRollbackException, RollbackException, NotSupportedException, HeuristicMixedException, SystemException {
        if (autocommit) {
            begin();
        }
        try {
            logProcessing(entity, "persist");
            entityManager.persist(entity);
            if (log.isDebugEnabled()) {
                log.debug("Done!");
            }
            if (autocommit) {
                commit();
            }
        } catch (Throwable t) {
            log.error(t.getMessage());
            if (autocommit) {
                try {
                    rollback();
                } catch (Throwable ignored) {
                    log.warn("Closing transaction!");
                    close();
                }
            }
            throw t;
        }
    }

    public static void delete(Object entity) throws HeuristicRollbackException, RollbackException, NotSupportedException, HeuristicMixedException, SystemException {
        delete(entity, getAutocommit());
    }

    public static void delete(Object entity, boolean autocommit) throws HeuristicRollbackException, RollbackException, NotSupportedException, HeuristicMixedException, SystemException {
        if (autocommit) {
            begin();
        }
        try {
            entityManager.remove(entity);
            if (autocommit) {
                flush();
                commit();
            }
        } catch (Throwable t) {
            log.error(t.getMessage());
            if (autocommit) {
                try {
                    rollback();
                } catch (Throwable ignored) {
                    log.warn("Closing transaction!");
                    close();
                }
            }
            throw t;
        }
    }

    public static void delete(Class<?> clazz, String key) throws HeuristicRollbackException, RollbackException, NotSupportedException, HeuristicMixedException, SystemException {
        begin();
        try {
            Session session = entityManager.unwrap(Session.class);
            Object entity = find(clazz, key);
            Query query = session.createQuery("delete " + clazz.getName() + " c where c.id = :id");
            query.setParameter("id", key);
            query.executeUpdate();
            session.detach(entity);
            session.flush();
            session.evict(entity);
            flush();
            commit();
        } catch (Throwable t) {
            log.error(t.getMessage());
            if (autocommit) {
                try {
                    rollback();
                } catch (Throwable ignored) {
                    log.warn("Closing transaction!");
                    close();
                }
            }
            throw t;
        }
    }

    public static void detach(Object entity) throws SystemException, NotSupportedException, HeuristicRollbackException, HeuristicMixedException, RollbackException {
        detach(entity, getAutocommit());
    }

    public static void detach(Object entity, boolean autocommit) throws NotSupportedException, SystemException, HeuristicRollbackException, HeuristicMixedException, RollbackException {
        if (autocommit) {
            begin();
        }
        try {
            entityManager.detach(entity);
            if (autocommit) {
                commit();
            }
        } catch (Throwable t) {
            log.error(t.getMessage());
            if (autocommit) {
                try {
                    rollback();
                } catch (Throwable ignored) {
                    log.warn("Closing transaction!");
                    close();
                }
            }
            throw t;
        }
    }

    public static void update(Object entity) throws SystemException, NotSupportedException {
        update(entity, getAutocommit());
    }

    public static void update(Object entity, boolean autocommit) throws SystemException, NotSupportedException {
        merge(entity, autocommit);
    }

    public static void executeNativeQuery(String query) throws HeuristicRollbackException, RollbackException, NotSupportedException, HeuristicMixedException, SystemException {
        executeNativeQuery(query, getAutocommit());
    }

    public static void executeNativeQuery(String query, boolean autocommit) throws HeuristicRollbackException, RollbackException, NotSupportedException, HeuristicMixedException, SystemException {
        try {
            if (autocommit) {
                begin();
            }
            Query nativeQuery = entityManager.createNativeQuery(query);
            nativeQuery.executeUpdate();
            if (autocommit) {
                commit();
            }
        } catch (Throwable t) {
            log.error(t.getMessage());
            if (autocommit) {
                try {
                    rollback();
                } catch (Throwable ignored) {
                    log.warn("Closing transaction!");
                    close();
                }
            }
            throw t;
        }
    }

    public static void begin() throws SystemException, NotSupportedException {
        try {
            if (!entityManager.isOpen()) {
                entityManager = entityManagerFactory.createEntityManager();
            }
            entityManager.getTransaction().begin();
        } catch (Throwable ex) {
            String message = ex.getMessage();
            if (!"Transaction already active".equals(message) && !message.contains("thread is already associated with a transaction!")) {
                throw ex;
            }
        }
    }

    private static void logProcessing(Object entity, String action) {
        if (log.isDebugEnabled()) {
            String type = entity.getClass().getSimpleName();
            Object id = reflectionUtils.invokeGetter(entity, "id");
            Field field = reflectionUtils.getField(entity, "name");
            if (field != null) {
                Object name = null;
                try {
                    field.setAccessible(true);
                    name = field.get(entity);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
                log.debug("Attempting to " + action + " entity of type '" + type + "' with name '" + name + "' and id '" + id + "'");
            } else {
                log.debug("Attempting to " + action + " entity of type '" + type + "' with id '" + id + "'");
            }
        }
    }


    public static void commit() throws HeuristicRollbackException, RollbackException, HeuristicMixedException, SystemException, NotSupportedException {
        entityManager.getTransaction().commit();
        begin();
    }

    public static void rollback() throws SystemException, NotSupportedException {
        entityManager.getTransaction().rollback();
        begin();
    }

    public static boolean getAutocommit() {
        return autocommit;
    }

    public static boolean isAutocommit() {
        return autocommit;
    }

    public static void setAutocommit(boolean autocommit) {
        OrpheusDbJPAEntityManagerUtils.autocommit = autocommit;
    }

    public static void flush() {
        entityManager.flush();
    }

    public static <T, V> List<V> executeQueryVersioned(Class<T> clazz, String query) {
        //TODO Finish Orpheus DB versioning implementation
        Table table = AnnotationHelper.getAnnotation(clazz, Table.class);
        String query_ = "SELECT children FROM " + table.schema() + "." + clazz.getSimpleName() + " WHERE vid = 'HEAD'";
        List<T> ids = findListByNativeQuery(clazz, query_);
        List<V> result = new ArrayList<>();
        Class<V> unversionedClazz = getUnversionedClass(clazz);
        List<V> list2 = executeQuery(unversionedClazz, query);
        list2.forEach(entity -> {
            Object id = reflectionUtils.invokeGetter(entity, "id");
            ids.forEach(id_ -> {
                Object vid = reflectionUtils.invokeGetter(id_, "vid");
                if (id_.equals(vid)) {
                    result.add(entity);
                }
            });
        });
        return result;
    }

    public static <T> List<T> executeQueryVersioned(Class<T> clazz, String query, Map<String, Object> params) {
        //TODO Finish Orpheus DB versioning implementation
        return executeQuery(clazz, query, params);
    }

    public static <T> T findByQueryVersioned(Class<T> clazz, String query) {
        //TODO Finish Orpheus DB versioning implementation
        return findByQuery(clazz, query);
    }

    public static <T> T findByQueryVersioned(Class<T> clazz, String query, Map<String, Object> params) {
        //TODO Finish Orpheus DB versioning implementation
        return findByQuery(clazz, query, params);
    }

    public static <T> T findByNativeQueryVersioned(Class<T> clazz, String query) {
        //TODO Finish Orpheus DB versioning implementation
        return findByNativeQuery(clazz, query);
    }

    public static <T> T findByNativeQueryVersioned(Class<T> clazz, String query, Map<String, Object> params) {
        //TODO Finish Orpheus DB versioning implementation
        return findByNativeQuery(clazz, query, params);
    }


    public static <T, V> List<V> findListByNativeQueryVersioned(Class<T> clazz, String query) {
        //TODO Finish Orpheus DB versioning implementation
        Table table = AnnotationHelper.getAnnotation(clazz, Table.class);
        String query_ = "SELECT t.children FROM " + table.schema() + "." + clazz.getSimpleName() + " t WHERE t.vid = 'HEAD'";
        List<T> ids = findListByNativeQuery(clazz, query_);
        List<V> result = new ArrayList<>();
        Class<V> unversionedClazz = getUnversionedClass(clazz);
        List<V> list2 = findListByNativeQuery(unversionedClazz, query);
        list2.forEach(entity -> {
            Object id = reflectionUtils.invokeGetter(entity, "id");
            ids.forEach(id_ -> {
                Object vid = reflectionUtils.invokeGetter(id_, "vid");
                if (id_.equals(vid)) {
                    result.add(entity);
                }
            });
        });
        return result;
    }

    public static <T, V> List<V> findListByNativeQueryVersioned(Class<T> clazz, String query, Map<String, Object> params) {
        //TODO Finish Orpheus DB versioning implementation
        return findListByNativeQueryVersioned(clazz, query, params);
    }


    public static <T, V> Class<V> getVersionedClass(Class<T> clazz) {
        Class<V> versionedClass = null;
        String versionedPackageName = clazz.getPackage().getName() + "." + OrpheusDbPersistence.GENERATED_PACKAGE;
        String versionedClassName = versionedPackageName + "." + clazz.getSimpleName() + OrpheusDbPersistence.VERSIONABLE_SIMPLE_NAME;
        try {
            versionedClass = (Class<V>) Class.forName(versionedClassName);
            log.debug("Class '" + clazz.getName() + "' is versioned by '" + versionedClass.getName() + ". Orpheus is going to intercept query in order to provide versioned data properly");
        } catch (Throwable t) {
            log.error("Class '" + clazz.getName() + "' is not versioned. Continuing with no intervention!");
        }
        return versionedClass;
    }

    public static <T, V> Class<V> getUnversionedClass(Class<T> clazz) {
        Class<V> unversionedClass = null;
        String unversionedPackageName = clazz.getPackage().getName();
        String suffix = "." + OrpheusDbPersistence.GENERATED_PACKAGE;
        if (unversionedPackageName.endsWith(suffix)) {
            unversionedPackageName = unversionedPackageName.replace(suffix, StringUtils.EMPTY);
        }
        String unversionedClassName = clazz.getSimpleName();
        if (unversionedClassName.endsWith(OrpheusDbPersistence.VERSIONABLE_SIMPLE_NAME)) {
            unversionedClassName = unversionedPackageName + "." + unversionedClassName.replace(OrpheusDbPersistence.VERSIONABLE_SIMPLE_NAME, StringUtils.EMPTY);
        }
        try {
            unversionedClass = (Class<V>) Class.forName(unversionedClassName);
            log.debug("Class '" + clazz.getName() + "' is versioned by '" + unversionedClass.getName() + ". Orpheus is going to intercept query in order to provide versioned data properly");
        } catch (Throwable t) {
            log.error("Class '" + clazz.getName() + "' is not versioned. Continuing with no intervention!");
        }
        return unversionedClass;
    }

    public static EntityManager getEntityManager() {
        return entityManager;
    }
}