package com.containerstore.lost.dirty;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;

/**
 * <p>Mark an static method of an {enum} type with this annotation to indicate that it returns the default
 * value for this enum.</p>
 *
 * <p>This is used by {@link DirtyObject} to assign default values for enumerations</p>
 *
 * <p>Here is an example:</p>
 *
 * <p><pre><code>
 * public class Foo {
 *     enum Ternary {
 *         YES,
 *         NO,
 *         UNSURE;
 *
 *         {@literal @}Defaulted
 *         public static Ternary defaulted() {
 *             return UNSURE;
 *         }
 *     }
 * }
 *
 * </code></pre></p>
 */
@Target(METHOD)
@Retention(RUNTIME)
@Documented
public @interface Defaulted {
}
