package org.hoohoot.homelab.manager.it.config

@Target(
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY_GETTER,
    AnnotationTarget.PROPERTY_SETTER,
    AnnotationTarget.CONSTRUCTOR,
    AnnotationTarget.FIELD
)
@Retention(
    AnnotationRetention.RUNTIME
)
@MustBeDocumented
annotation class InjectWireMock 