package com.araguacaima.orpheusdb.annotations;

import java.lang.annotation.Annotation;

public class VersionableImpl implements Versionable {
    @Override
    public Class<? extends Annotation> annotationType() {
        return VersionableImpl.class;
    }
}
