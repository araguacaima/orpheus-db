package com.araguacaima.orpheusdb.utils;

import com.araguacaima.commons.utils.ClassLoaderUtils;
import com.araguacaima.orpheusdb.annotations.TableWrapper;
import com.araguacaima.orpheusdb.annotations.Versionable;
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

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.PersistenceException;
import javax.persistence.Table;
import javax.persistence.spi.PersistenceProvider;
import javax.persistence.spi.PersistenceProviderResolver;
import javax.persistence.spi.PersistenceProviderResolverHolder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class OrpheusDb extends Persistence {


    public static Reflections reflections;
    private static final String GENERATED_PACKAGE = ".generated";
    private static final String VERSIONABLE_NAME = com.araguacaima.orpheusdb.Versionable.class.getName();
    private static final String INDEXABLE_NAME = com.araguacaima.orpheusdb.Indexable.class.getName();

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
    public static EntityManagerFactory createEntityManagerFactory(String persistenceUnitName, Map properties) {

        final List<Class<?>> classes = new ArrayList<>();

        try {
            classes.addAll((List<Class<?>>) properties.get("orpheus.db.versionable.classes"));
        } catch (ClassCastException ignored) {
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
        reflections = new Reflections(builder);
        classes.addAll(reflections.getTypesAnnotatedWith(Versionable.class));

        if (CollectionUtils.isNotEmpty(classes)) {
            String incomingPackage = packagesToScan.split(",")[0];
            ClassLoaderUtils clu = new ClassLoaderUtils(Package.getPackage(incomingPackage).getClass().getClassLoader());
            List<String> packagesToScanList = new ArrayList<>();
            ClassPool pool = ClassPool.getDefault();
            classes.forEach((Class<?> clazz) -> {
                String name = clazz.getSimpleName();
                String packageName = clazz.getPackage().getName();
                String newVersionableName = name + com.araguacaima.orpheusdb.Versionable.class.getSimpleName();
                String newIndexableName = name + com.araguacaima.orpheusdb.Indexable.class.getSimpleName();
                fixTableAnnotation(clu, newVersionableName, pool, VERSIONABLE_NAME);
                fixTableAnnotation(clu, newIndexableName, pool, INDEXABLE_NAME);
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

    private static void fixTableAnnotation(ClassLoaderUtils clu, String newClassName, ClassPool pool, String name) {
        CtClass cc;
        try {
            cc = pool.get(name);
            cc.setName(GENERATED_PACKAGE + "." + newClassName);
            Table table_ = (Table) cc.getAnnotation(Table.class);
            com.araguacaima.orpheusdb.annotations.Table table = TableWrapper.fromPersistenceTable(table_);
            table.setName(newClassName);
            Class<?> clazz = cc.toClass();
            AnnotationHelper.alterAnnotationOn(clazz, Table.class, table);
            clu.loadClass(clazz);
        } catch (NotFoundException | ClassNotFoundException | CannotCompileException e) {
            e.printStackTrace();
        }
    }
}
