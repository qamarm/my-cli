package com.example.mycli;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks code that JaCoCo excludes from coverage reports (its built-in
 * generated-code filter matches any annotation whose simple name contains
 * "Generated"). Used for {@link MyCli#main}, which just delegates to
 * {@link MyCli#execute} and calls {@code System.exit}, and can't be
 * exercised by a unit test without terminating the test JVM.
 */
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.METHOD)
@interface Generated {
}
