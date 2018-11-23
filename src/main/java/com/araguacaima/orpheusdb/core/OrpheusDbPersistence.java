package com.araguacaima.orpheusdb.core;

import com.araguacaima.commons.utils.*;
import com.araguacaima.orpheusdb.annotations.Generated;
import com.araguacaima.orpheusdb.annotations.GeneratedImpl;
import com.araguacaima.orpheusdb.annotations.Versionable;
import com.araguacaima.orpheusdb.annotations.VersionableImpl;
import com.araguacaima.orpheusdb.helpers.AnnotationHelper;
import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.NotFoundException;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.ClassFile;
import javassist.bytecode.ConstPool;
import javassist.bytecode.annotation.Annotation;
import javassist.bytecode.annotation.StringMemberValue;
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
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;

@SuppressWarnings("unused")
public class OrpheusDbPersistence extends Persistence {

    private static final String GENERATED_PACKAGE = "generated";
    private static final String VERSIONABLE_NAME = com.araguacaima.orpheusdb.Versionable.class.getName();
    private static final String INDEXABLE_NAME = com.araguacaima.orpheusdb.Indexable.class.getName();
    private static final String VERSIONABLE_SIMPLE_NAME = com.araguacaima.orpheusdb.Versionable.class.getSimpleName();
    private static final String INDEXABLE_SIMPLE_NAME = com.araguacaima.orpheusdb.Indexable.class.getSimpleName();

    private static final VersionableImpl versionableAnnotation = new VersionableImpl();
    private static final GeneratedImpl generatedAnnotation = new GeneratedImpl();

    private static final Logger log = LoggerFactory.getLogger(OrpheusDbPersistence.class);
    private static final JarUtils jarUtils = new JarUtils();
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
            log.info("Properties 'orpheus.db.versionable.classes' found with values: " + StringUtils.join(classes, ","));
        } catch (ClassCastException | NullPointerException ignored) {
            try {
                String str = (String) properties.get("orpheus.db.versionable.classes");
                if (StringUtils.isNotBlank(str)) {
                    log.info("Properties 'orpheus.db.versionable.classes' found with values: " + str);
                }
                Arrays.asList(StringUtils.split(str, ","))
                        .forEach(className -> {
                            try {
                                classes.add(Class.forName(className));
                            } catch (Throwable ignored1) {
                            }
                        });
            } catch (Throwable ignored2) {
            }
        }

        String packages = (String) properties.get("orpheus.db.versionable.packages");
        if (packages != null) {
            log.info("Properties 'orpheus.db.versionable.packages' found with values: " + packages);
            Arrays.asList(StringUtils.split(packages, ",")).forEach(packageName -> {
                try {
                    Reflections reflections = new Reflections(packageName);
                    Set<Class<?>> entityTypes = reflections.getTypesAnnotatedWith(Entity.class);
                    entityTypes.forEach(entity -> {
                        if (AnnotationHelper.getAnnotation(entity, Generated.class) == null) {
                            classes.add(entity);
                        }
                    });
                } catch (Throwable ignored1) {
                }
            });
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
        Set<Class<?>> typesAnnotatedWithVersionable = reflections.getTypesAnnotatedWith(Versionable.class);
        typesAnnotatedWithVersionable.forEach(entity -> {
            if (AnnotationHelper.getAnnotation(entity, Generated.class) == null) {
                classes.add(entity);
            }
        });
        log.info("Classes to load: " + classes);

        if (CollectionUtils.isNotEmpty(classes)) {
            Set<String> packagesToScanList = new TreeSet<>();
            ClassPool pool = ClassPool.getDefault();
            classes.forEach((Class<?> clazz) -> {
                String fullyQualifiedClassName;
                String name = clazz.getSimpleName();
                String packageName = clazz.getPackage().getName();
                String newVersionableName = name + VERSIONABLE_SIMPLE_NAME;
                String newIndexableName = name + INDEXABLE_SIMPLE_NAME;
                URL aClass = classLoaderUtils.findClass(clazz.getName());
                String path = aClass.getPath();
                path = path.replace(packageName.replaceAll("\\.", "/") + "/" + name + ".class", StringUtils.EMPTY);
                if (path.startsWith("/")) {
                    path = path.substring(1);
                }
                if (path.endsWith("/")) {
                    path = path.substring(0, path.length() - 1);
                }
                path = path.replace("file:", StringUtils.EMPTY);

                CtClass cc;
                try {
                    fullyQualifiedClassName = packageName + "." + GENERATED_PACKAGE + "." + newVersionableName;
                    cc = pool.get(VERSIONABLE_NAME);
                    cc.setName(fullyQualifiedClassName);
                    fixAnnotations(cc, newVersionableName, packageName, VERSIONABLE_NAME, clazz, fullyQualifiedClassName);
                    writeClass(pool, path, fullyQualifiedClassName, clazz);

                    fullyQualifiedClassName = packageName + "." + GENERATED_PACKAGE + "." + newIndexableName;
                    cc = pool.get(INDEXABLE_NAME);
                    cc.setName(fullyQualifiedClassName);
                    fixAnnotations(cc, newIndexableName, packageName, INDEXABLE_NAME, clazz, fullyQualifiedClassName);
                    writeClass(pool, path, fullyQualifiedClassName, clazz);
                } catch (CannotCompileException | IOException | ClassNotFoundException | NotFoundException | IllegalAccessException | NoSuchFieldException e) {
                    e.printStackTrace();
                }
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

    private static void fixAnnotations(CtClass cc, String newClassName, String newPackageName,
                                       String name, Class clazz, String fullyQualifiedName)
            throws ClassNotFoundException, CannotCompileException {

        Table table_ = AnnotationHelper.getAnnotation(clazz, Table.class);
        String tableSchema;
        if (table_ == null) {
            tableSchema = null;
        } else {
            tableSchema = table_.schema();
        }
        PersistenceUnit persistenceUnit = (PersistenceUnit) clazz.getAnnotation(PersistenceUnit.class);

        ClassFile classFile = cc.getClassFile();
        ConstPool constPool = classFile.getConstPool();
        AnnotationsAttribute attribute = new AnnotationsAttribute(constPool, AnnotationsAttribute.visibleTag);
        if (persistenceUnit != null) {
            Annotation persistenceUnitAnnotation = new Annotation(PersistenceUnit.class.getName(), constPool);
            persistenceUnitAnnotation.addMemberValue("name", new StringMemberValue(persistenceUnit.name(), constPool));
            persistenceUnitAnnotation.addMemberValue("unitName", new StringMemberValue(persistenceUnit.unitName(), constPool));
            attribute.addAnnotation(persistenceUnitAnnotation);
        }
        Table table = (Table) cc.getAnnotation(Table.class);
        if (table != null) {
            Annotation tableAnnotation = new Annotation(Table.class.getName(), constPool);
            tableAnnotation.addMemberValue("name", new StringMemberValue(newClassName, constPool));
            if (StringUtils.isNotBlank(table.catalog())) {
                tableAnnotation.addMemberValue("catalog", new StringMemberValue(table.catalog(), constPool));
            }
            if (tableSchema != null) {
                tableAnnotation.addMemberValue("schema", new StringMemberValue(tableSchema, constPool));
            }
            attribute.addAnnotation(tableAnnotation);
        }
        Annotation generatedAnnotation_ = new Annotation(Generated.class.getName(), constPool);
        attribute.addAnnotation(generatedAnnotation_);
        Entity entity = (Entity) cc.getAnnotation(Entity.class);
        if (entity != null) {
            Annotation entityAnnotation_ = new Annotation(Entity.class.getName(), constPool);
            attribute.addAnnotation(entityAnnotation_);
        }
        classFile.addAttribute(attribute);
    }

    private static void writeClass(ClassPool pool, String path, String fullyQualifiedName, Class<?> clazz) throws CannotCompileException, IOException, ClassNotFoundException, NotFoundException, NoSuchFieldException, IllegalAccessException {
        CtClass cc = pool.get(fullyQualifiedName);
        boolean isJar = path.endsWith(".jar!");
        String replacedFullyQualifiedName = fullyQualifiedName.replaceAll("\\.", "/");
        String resource;
        if (!isJar) {
            FileUtils.forceMkdir(new File(path));
            cc.writeFile(path);
            resource = path + "/" + replacedFullyQualifiedName + ".class'";
            log.info("Class '" + resource + " written!");
        } else {
            String parent = new File(path).getParent() + "/generated-classes";
            File file = new File(parent);
            FileUtils.forceMkdir(file);
            cc.writeFile(parent);
            resource = parent + "/" + replacedFullyQualifiedName + ".class";
            log.info("Class '" + resource + "' written!");
        }
        pool.getClassLoader().loadClass(fullyQualifiedName);
    }
}
