/*
 * Copyright 2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.morpheus.grails.compiler

import groovy.transform.AnnotationCollector
import groovy.transform.CompileStatic

/**
 *
 * @since 2.4
 *
 */
@AnnotationCollector
@CompileStatic(extensions=['org.grails.compiler.ValidateableTypeCheckingExtension',
                           'org.grails.compiler.NamedQueryTypeCheckingExtension',
                           'org.grails.compiler.HttpServletRequestTypeCheckingExtension',
                           'org.grails.compiler.WhereQueryTypeCheckingExtension',
                           'org.grails.compiler.DynamicFinderTypeCheckingExtension',
                           'org.grails.compiler.DomainMappingTypeCheckingExtension',
                           'com.morpheus.grails.compiler.DomainSearchableTypeCheckingExtension',
                           'org.grails.compiler.RelationshipManagementMethodTypeCheckingExtension'])
@interface GrailsSearchableCompileStatic {
}
