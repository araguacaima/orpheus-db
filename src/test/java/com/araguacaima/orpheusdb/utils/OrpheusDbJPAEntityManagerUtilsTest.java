package com.araguacaima.orpheusdb.utils;

import com.araguacaima.commons.utils.MapUtils;
import com.araguacaima.orpheusdb.model.A;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class OrpheusDbJPAEntityManagerUtilsTest {

    private static final String PERSISTENCE_UNIT_NAME = "orpheus-db-test";
    private static Logger log = LoggerFactory.getLogger(OrpheusDbJPAEntityManagerUtilsTest.class);
    private static Map<String, String> environment;
    private static ProcessBuilder processBuilder = new ProcessBuilder();

    @Before
    public void init() {

        environment = processBuilder.environment();
        URL url = OrpheusDbJPAEntityManagerUtils.class.getResource("/config.properties");
        Properties properties = new Properties();
        try {
            properties.load(url.openStream());
            Map<String, String> map = MapUtils.fromProperties(properties);
            if (!map.isEmpty()) {
                environment.putAll(map);
                log.info("Properties taken from config file '" + url.getFile().replace("file:" + File.separator, "") + "'");
            } else {
                log.info("Properties taken from system map...");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        environment.put("hibernate.connection.url", environment.get("JDBC_DATABASE_URL"));
        environment.put("hibernate.connection.username", environment.get("JDBC_DATABASE_USERNAME"));
        environment.put("hibernate.connection.password", environment.get("JDBC_DATABASE_PASSWORD"));
        environment.put("hibernate.archive.autodetection", "class");
        environment.put("hibernate.default_schema", "orpheusdb");
        environment.put("hibernate.dialect", "org.hibernate.dialect.H2Dialect");
        environment.put("hibernate.connection.driver_class", "org.h2.Driver");
        environment.put("hibernate.show_sql", "true");
        environment.put("hibernate.flushMode", "FLUSH_AUTO");
        environment.put("hibernate.hbm2ddl.auto", "update");
        environment.put("packagesToScan", "com.araguacaima.orpheusdb.model,com.araguacaima.orpheusdb.jar.model");
        environment.put("orpheus.db.versionable.packages", "com.araguacaima.orpheusdb.model.versionable,com.araguacaima.orpheusdb.jar.model.versionable");
        environment.put("orpheus.db.versionable.classes", "com.araguacaima.orpheusdb.model.B,com.araguacaima.orpheusdb.jar.model.H");
        OrpheusDbJPAEntityManagerUtils.init(PERSISTENCE_UNIT_NAME, environment);
        A a1 = new A();
        a1.setTestField1("testField1-1");
        a1.setTestField2(1);
        a1.setTestField7("testField7-1");
        a1.setTestField9("testField9-1");
        OrpheusDbJPAEntityManagerUtils.persist(a1);
        A a2 = new A();
        a2.setTestField1("testField1-2");
        a2.setTestField2(2);
        a2.setTestField7("testField7-2");
        a2.setTestField9("testField9-2");
        OrpheusDbJPAEntityManagerUtils.persist(a2);
    }

    @Test
    public void testExecuteVersionedQuery() {
        String query = "SELECT f.* FROM orpheusdb.f f WHERE f.testField2 = 2";
        List<A> list = OrpheusDbJPAEntityManagerUtils.findListByNativeQuery(A.class, query);
        Assert.assertNotNull(list);
        Assert.assertEquals(list.size(), 1);
        A result = list.get(0);
        Assert.assertEquals(result.getTestField1(),"testField1-2");
        Assert.assertEquals(result.getTestField2(),2);
        Assert.assertEquals(result.getTestField7(),"testField7-2");
        Assert.assertEquals(result.getTestField9(),"testField9-2");

    }
}
