package com.araguacaima.orpheusdb.annotations;

import java.lang.annotation.Annotation;

public class GeneratedImpl implements Generated {
    @Override
    public Class<? extends Annotation> annotationType() {
        return GeneratedImpl.class;
    }
}
