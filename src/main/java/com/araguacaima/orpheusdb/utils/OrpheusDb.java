package com.araguacaima.orpheusdb.utils;

import com.araguacaima.commons.utils.*;
import com.araguacaima.orpheusdb.annotations.Generated;
import com.araguacaima.orpheusdb.annotations.GeneratedImpl;
import com.araguacaima.orpheusdb.annotations.Versionable;
import com.araguacaima.orpheusdb.annotations.VersionableImpl;
import com.araguacaima.orpheusdb.helpers.AnnotationHelper;
import javassist.*;
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
public class OrpheusDb extends Persistence {

    private static final String GENERATED_PACKAGE = "generated";
    private static final String VERSIONABLE_NAME = com.araguacaima.orpheusdb.Versionable.class.getName();
    private static final String INDEXABLE_NAME = com.araguacaima.orpheusdb.Indexable.class.getName();
    private static final VersionableImpl versionableAnnotation = new VersionableImpl();
    private static final GeneratedImpl generatedAnnotation = new GeneratedImpl();

    private static final Logger log = LoggerFactory.getLogger(OrpheusDb.class);
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
                log.info("Properties 'orpheus.db.versionable.classes' found with values: " + str);
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
            List<String> classesToAdd = new ArrayList<>();
            classes.forEach((Class<?> clazz) -> {
                ClassLoaderUtils clu = new ClassLoaderUtils(clazz.getClassLoader());
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
                log.info("Processing class '" + clazz.getName() + "'");
                log.info("Path to store class '" + path + "'");

                ClassPool pool = ClassPool.getDefault();
                pool.insertClassPath(new ClassClassPath(com.araguacaima.orpheusdb.Indexable.class));

                try {
                    CtClass cc;
                    String fullyQualifiedName = packageName + "." + GENERATED_PACKAGE + "." + newVersionableName;
                    URL urlVersionable = clu.findClass(fullyQualifiedName);
                    if (urlVersionable == null) {
                        cc = pool.get(VERSIONABLE_NAME);
                        cc.setName(fullyQualifiedName);
                        try {
                            fixAnnotations(clu, cc, newVersionableName, packageName, VERSIONABLE_NAME, persistenceUnit, tableSchema, fullyQualifiedName);
                            classesToAdd.add(writeClass(clu, pool, path, fullyQualifiedName, clazz));
                        } catch (CannotCompileException | IOException | ClassNotFoundException | NotFoundException | IllegalAccessException | NoSuchFieldException e) {
                            e.printStackTrace();
                        }
                    } else {
                        log.info(urlVersionable + " Previously processed!");
                    }
                    fullyQualifiedName = packageName + "." + GENERATED_PACKAGE + "." + newIndexableName;
                    URL urlIndexable = clu.findClass(fullyQualifiedName);
                    if (urlIndexable == null) {
                        cc = pool.get(INDEXABLE_NAME);
                        cc.setName(fullyQualifiedName);
                        try {
                            fixAnnotations(clu, cc, newIndexableName, packageName, INDEXABLE_NAME, persistenceUnit, tableSchema, fullyQualifiedName);
                            classesToAdd.add(writeClass(clu, pool, path, fullyQualifiedName, clazz));
                        } catch (CannotCompileException | IOException | ClassNotFoundException | NotFoundException | IllegalAccessException | NoSuchFieldException e) {
                            e.printStackTrace();
                        }

                    } else {
                        log.info(urlIndexable + " Previously processed!");
                    }
                } catch (NotFoundException e) {
                    e.printStackTrace();
                }
                packagesToScanList.add(packageName + "." + GENERATED_PACKAGE);
            });
            ClassLoaderUtils clu = new ClassLoaderUtils(null, null);
            clu.init();
            classesToAdd.forEach(clazzStr -> {
                try {
                    clu.addResourceToDependencies(clazzStr);
                } catch (IOException | ClassNotFoundException | NoSuchFieldException | IllegalAccessException e) {
                    e.printStackTrace();
                }
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

    private static void fixAnnotations(ClassLoaderUtils clu, CtClass cc, String newClassName, String newPackageName,
                                       String name, java.lang.annotation.Annotation persistenceUnit,
                                       String tableSchema, String fullyQualifiedName) throws ClassNotFoundException, CannotCompileException {
        Class<?> clazz;
        ClassFile classFile = cc.getClassFile();
        ConstPool constPool = classFile.getConstPool();
        AnnotationsAttribute attribute = new AnnotationsAttribute(constPool, AnnotationsAttribute.visibleTag);
        if (persistenceUnit != null) {
            Annotation persistenceUnitAnnotation = new Annotation(PersistenceUnit.class.getName(), constPool);
            PersistenceUnit persistenceUnit1 = (PersistenceUnit) persistenceUnit;
            persistenceUnitAnnotation.addMemberValue("name", new StringMemberValue(persistenceUnit1.name(), constPool));
            persistenceUnitAnnotation.addMemberValue("unitName", new StringMemberValue(persistenceUnit1.unitName(), constPool));
            attribute.addAnnotation(persistenceUnitAnnotation);
        }
        Annotation tableAnnotation = new Annotation(Table.class.getName(), constPool);
        Table table1 = (Table) cc.getAnnotation(Table.class);
        tableAnnotation.addMemberValue("name", new StringMemberValue(newClassName, constPool));
        if (StringUtils.isNotBlank(table1.catalog())) {
            tableAnnotation.addMemberValue("catalog", new StringMemberValue(table1.catalog(), constPool));
        }
        if (tableSchema != null) {
            tableAnnotation.addMemberValue("schema", new StringMemberValue(tableSchema, constPool));
        }
        attribute.addAnnotation(tableAnnotation);
        Annotation generatedAnnotation_ = new Annotation(Generated.class.getName(), constPool);
        attribute.addAnnotation(generatedAnnotation_);
        classFile.addAttribute(attribute);
        cc.toClass();

    }

    private static String writeClass(ClassLoaderUtils clu, ClassPool pool, String path, String fullyQualifiedName, Class<?> clazz) throws CannotCompileException, IOException, ClassNotFoundException, NotFoundException, NoSuchFieldException, IllegalAccessException {
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
        return resource;
    }
}
