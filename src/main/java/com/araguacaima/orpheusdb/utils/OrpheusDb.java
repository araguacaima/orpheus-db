package com.araguacaima.orpheusdb.utils;

import com.araguacaima.commons.utils.ClassLoaderUtils;
import com.araguacaima.commons.utils.MapUtils;
import com.araguacaima.commons.utils.NotNullOrEmptyStringPredicate;
import com.araguacaima.orpheusdb.annotations.TableWrapper;
import com.araguacaima.orpheusdb.annotations.Versionable;
import com.araguacaima.orpheusdb.annotations.VersionableImpl;
import com.araguacaima.orpheusdb.helpers.AnnotationHelper;
import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.NotFoundException;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.reflections.Reflections;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.*;
import javax.persistence.spi.PersistenceProvider;
import javax.persistence.spi.PersistenceProviderResolver;
import javax.persistence.spi.PersistenceProviderResolverHolder;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@SuppressWarnings("unused")
public class OrpheusDb extends Persistence {

    private static final String GENERATED_PACKAGE = "generated";
    private static final String VERSIONABLE_NAME = com.araguacaima.orpheusdb.Versionable.class.getName();
    private static final String INDEXABLE_NAME = com.araguacaima.orpheusdb.Indexable.class.getName();
    private static final VersionableImpl versionableAnnotation = new VersionableImpl();
    private static final Logger log = LoggerFactory.getLogger(OrpheusDb.class);

    private static final ClassLoaderUtils classLoaderUtils = new ClassLoaderUtils(MapUtils.getInstance(),
            new com.araguacaima.commons.utils.StringUtils(new NotNullOrEmptyStringPredicate(), null));

    /**
     * Create and return an EntityManagerFactory for the named persistence unit
     * using the given properties.
     *
     * @param persistenceUnitName the name of the persistence unit
     * @param properties          Additional properties to use when creating the factory.
     *                            These properties may include properties to control
     *                            schema generation.  The values of these properties override
     *                            any values that may have been configured elsewhere.
     * @return the factory that creates EntityManagers configured according to
     * the specified persistence unit.
     */
    @SuppressWarnings("unchecked")
    public static EntityManagerFactory createEntityManagerFactory(String persistenceUnitName, Map properties) {

        final List<Class<?>> classes = new ArrayList<>();

        try {
            classes.addAll((List<Class<?>>) properties.get("orpheus.db.versionable.classes"));
        } catch (ClassCastException | NullPointerException ignored) {
            try {
                Arrays.asList(StringUtils.split((String) properties.get("orpheus.db.versionable.classes"), ",")).forEach(className -> {
                    try {
                        classes.add(Class.forName(className));
                    } catch (Throwable ignored1) {

                    }
                });
            } catch (Throwable ignored2) {

            }
        }
        String packagesToScan = (String) properties.get("packagesToScan");
        ConfigurationBuilder builder = new ConfigurationBuilder();
        FilterBuilder filter = new FilterBuilder();

        for (String param : packagesToScan.split(",")) {
            String trimmedParam = param.trim();
            builder.addUrls(ClasspathHelper.forPackage(trimmedParam));
            filter.includePackage(trimmedParam);
        }
        builder.filterInputsBy(filter);
        Reflections reflections = new Reflections(builder);
        classes.addAll(reflections.getTypesAnnotatedWith(Versionable.class));

        if (CollectionUtils.isNotEmpty(classes)) {
            ClassLoaderUtils clu = new ClassLoaderUtils(null, null);
            List<String> packagesToScanList = new ArrayList<>();
            ClassPool pool = ClassPool.getDefault();
            classes.forEach((Class<?> clazz) -> {
                String name = clazz.getSimpleName();
                String packageName = clazz.getPackage().getName();
                String newVersionableName = name + com.araguacaima.orpheusdb.Versionable.class.getSimpleName();
                String newIndexableName = name + com.araguacaima.orpheusdb.Indexable.class.getSimpleName();
                Table table = AnnotationHelper.getAnnotation(clazz, Table.class);
                String tableSchema;
                if (table == null) {
                    tableSchema = null;
                } else {
                    tableSchema = table.schema();
                }
                PersistenceUnit persistenceUnit = clazz.getAnnotation(PersistenceUnit.class);
                String path = classLoaderUtils.findClass(clazz.getName()).getPath();
                path = path.replace(packageName.replaceAll("\\.", "/") + "/" + name + ".class", StringUtils.EMPTY);
                if (path.startsWith("/")) {
                    path = path.substring(1);
                }
                if (path.endsWith("/")) {
                    path = path.substring(0, path.length() - 1);
                }
                fixTableAnnotation(clu, newVersionableName, packageName, pool, VERSIONABLE_NAME, persistenceUnit, tableSchema, path);
                fixTableAnnotation(clu, newIndexableName, packageName, pool, INDEXABLE_NAME, persistenceUnit, tableSchema, path);
                packagesToScanList.add(packageName + "." + GENERATED_PACKAGE);
            });
            packagesToScan = packagesToScan + "," + StringUtils.join(packagesToScanList, ",");
            properties.put("packagesToScan", packagesToScan);
        }

        EntityManagerFactory emf = null;
        PersistenceProviderResolver resolver = PersistenceProviderResolverHolder.getPersistenceProviderResolver();

        List<PersistenceProvider> providers = resolver.getPersistenceProviders();

        for (PersistenceProvider provider : providers) {
            emf = provider.createEntityManagerFactory(persistenceUnitName, properties);
            if (emf != null) {
                break;
            }
        }
        if (emf == null) {
            throw new PersistenceException("No Persistence provider for EntityManager named " + persistenceUnitName);
        }
        return emf;
    }

    private static void fixTableAnnotation(ClassLoaderUtils clu, String newClassName, String newPackageName,
                                           ClassPool pool, String name, Annotation persistenceUnit,
                                           String tableSchema, String path) {
        CtClass cc;
        try {
            cc = pool.get(name);
            String fullyQualifiedName = newPackageName + "." + GENERATED_PACKAGE + "." + newClassName;
            cc.setName(fullyQualifiedName);
            Table table_ = (Table) cc.getAnnotation(Table.class);
            com.araguacaima.orpheusdb.annotations.Table table = TableWrapper.fromPersistenceTable(table_);
            table.setName(newClassName);
            if (tableSchema != null) {
                table.setSchema(tableSchema);
            }
            Class<?> clazz = cc.toClass();
            AnnotationHelper.alterAnnotationOn(clazz, Table.class, table);
            AnnotationHelper.alterAnnotationOn(clazz, Versionable.class, versionableAnnotation);
            if (persistenceUnit != null) {
                AnnotationHelper.alterAnnotationOn(clazz, PersistenceUnit.class, persistenceUnit);
            }
            cc.writeFile(path);
            clu.loadClass(clazz);
            log.debug(Class.forName(fullyQualifiedName) + "' fixed and added to classloader!");
        } catch (NotFoundException | ClassNotFoundException | CannotCompileException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
