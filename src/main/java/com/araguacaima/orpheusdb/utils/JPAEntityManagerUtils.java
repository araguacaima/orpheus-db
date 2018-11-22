package com.araguacaima.orpheusdb.utils;

import com.araguacaima.commons.utils.ReflectionUtils;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.*;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class JPAEntityManagerUtils {
    private static EntityManagerFactory entityManagerFactory;
    private static EntityManager entityManager;
    private static boolean autocommit = true;

    @Transient
    @JsonIgnore
    private static ReflectionUtils reflectionUtils = new ReflectionUtils(null);

    private static Logger log = LoggerFactory.getLogger(JPAEntityManagerUtils.class);

    public static void closeAll() {
        close(entityManager, entityManagerFactory);
    }

    public static void close() {
        close(entityManager, null);
    }

    private static void close(EntityManager em, EntityManagerFactory emf) {
        if (em != null) {
            em.clear();
            em.close();
        }
        if (emf != null) {
            emf.close();
        }
    }

    public static void init(Map<String, String> map) {
        entityManagerFactory = OrpheusDb.createEntityManagerFactory("open-archi", map);
        entityManager = entityManagerFactory.createEntityManager();
        entityManager.unwrap(Session.class);
    }

    public static EntityManager getEntityManager() {
        return entityManager;
    }

    public static <T> T find(Class<T> clazz, Object key) {
        return entityManager.find(clazz, key);
    }

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
        TypedQuery<T> namedQuery = entityManager.createNamedQuery(query, clazz);
        if (params != null) {
            for (Map.Entry<String, Object> param : params.entrySet()) {
                namedQuery.setParameter(param.getKey(), param.getValue());
            }
        }
        try {
            return namedQuery.getResultList();
        } catch (javax.persistence.NoResultException ignored) {
            return null;
        }
    }

    public static <T> T findByQuery(Class<T> clazz, String query) {
        return findByQuery(clazz, query, null);
    }

    public static <T> T findByQuery(Class<T> clazz, String query, Map<String, Object> params) {
        TypedQuery<T> namedQuery = entityManager.createNamedQuery(query, clazz);
        if (params != null) {
            for (Map.Entry<String, Object> param : params.entrySet()) {
                namedQuery.setParameter(param.getKey(), param.getValue());
            }
        }
        try {
            return namedQuery.getSingleResult();
        } catch (javax.persistence.NoResultException ignored) {
            return null;
        }
    }


    public static <T> T findByNativeQuery(Class<T> clazz, String query) {
        return findByNativeQuery(clazz, query, null);
    }

    public static <T> T findByNativeQuery(Class<T> clazz, String query, Map<String, Object> params) {
        Query namedQuery = entityManager.createNativeQuery(query, clazz);
        if (params != null) {
            for (Map.Entry<String, Object> param : params.entrySet()) {
                namedQuery.setParameter(param.getKey(), param.getValue());
            }
        }
        try {
            Object singleResult = namedQuery.getSingleResult();
            return (T) singleResult;
        } catch (javax.persistence.NoResultException ignored) {
            return null;
        }
    }

    public static <T> T merge(T entity) {
        return merge(entity, getAutocommit());
    }

    public static <T> T merge(T entity, boolean autocommit) {
        if (entity == null) {
            return null;
        }
        if (autocommit) {
            begin();
        }
        try {
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

    public static void persist(Object entity) {
        persist(entity, getAutocommit());
    }

    public static void persist(Object entity, boolean autocommit) {
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
                rollback();
            }
            throw t;
        }
    }

    public static void delete(Object entity) {
        delete(entity, getAutocommit());
    }

    public static void delete(Object entity, boolean autocommit) {
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
                rollback();
            }
            throw t;
        }
    }

    public static void delete(Class<?> clazz, String key) {
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
                rollback();
            }
            throw t;
        }
    }

    public static void detach(Object entity) {
        detach(entity, getAutocommit());
    }

    public static void detach(Object entity, boolean autocommit) {
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
                rollback();
            }
            throw t;
        }
    }

    public static void update(Object entity) {
        update(entity, getAutocommit());
    }

    public static void update(Object entity, boolean autocommit) {
        merge(entity, autocommit);
    }

    public static void executeNativeQuery(String query) {
        executeNativeQuery(query, getAutocommit());
    }

    public static void executeNativeQuery(String query, boolean autocommit) {
        if (autocommit) {
            begin();
        }
        try {
            Query nativeQuery = entityManager.createNativeQuery(query);
            nativeQuery.executeUpdate();
            if (autocommit) {
                commit();
            }
        } catch (Throwable t) {
            log.error(t.getMessage());
            if (autocommit) {
                rollback();
            }
            throw t;
        }
    }

    public static void begin() {
        try {
            entityManager.getTransaction().begin();
        } catch (java.lang.IllegalStateException ex) {
            if (!"Transaction already active".equals(ex.getMessage())) {
                throw ex;
            }
        }
    }

    private static void logProcessing(Object entity, String action) {
        if (log.isDebugEnabled()) {
            String type = entity.getClass().getSimpleName();
            Object id = reflectionUtils.invokeGetter(entity, "id");
            Field field = reflectionUtils.getFieldByFieldName(entity, "name");
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


    public static void commit() {
        entityManager.getTransaction().commit();
    }

    public static void rollback() {
        entityManager.getTransaction().rollback();
    }

    public static boolean getAutocommit() {
        return autocommit;
    }

    public static boolean isAutocommit() {
        return autocommit;
    }

    public static void setAutocommit(boolean autocommit) {
        JPAEntityManagerUtils.autocommit = autocommit;
    }

    public static void flush() {
        entityManager.flush();
    }
}