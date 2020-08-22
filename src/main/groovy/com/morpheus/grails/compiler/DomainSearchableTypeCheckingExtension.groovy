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

import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.expr.ArgumentListExpression
import org.codehaus.groovy.ast.expr.ClosureExpression
import org.codehaus.groovy.ast.expr.MethodCall
import org.codehaus.groovy.ast.stmt.EmptyStatement
import org.grails.compiler.injection.GrailsASTUtils
import org.codehaus.groovy.transform.stc.GroovyTypeCheckingExtensionSupport.TypeCheckingDSL

/**
 * Makes GrailsSearchableCompileStatic work on Domain Classes with Searchable ElasticSearch things
 * @author David Estes
 */
class DomainSearchableTypeCheckingExtension extends TypeCheckingDSL {
    
    @Override
    public Object run() {
        setup { newScope() }

        finish { scopeExit() }
        
        beforeVisitClass { ClassNode classNode ->
            def searchableProperty = classNode.getField('searchable')
            if(searchableProperty && searchableProperty.isStatic() && searchableProperty.initialExpression instanceof ClosureExpression) {
                def sourceUnit = classNode?.module?.context
                if(GrailsASTUtils.isDomainClass(classNode, sourceUnit)) {
                    newScope {
                        searchableClosureCode = searchableProperty.initialExpression.code
                    }
                    searchableProperty.initialExpression.code = new EmptyStatement()
                }
            }
        }

        afterVisitClass { ClassNode classNode ->
            if(currentScope.mappingClosureCode) {
                def searchableProperty = classNode.getField('searchable')
                searchableProperty.initialExpression.code = currentScope.searchableClosureCode
                currentScope.checkingSearchableClosure = true
                withTypeChecker { visitClosureExpression searchableProperty.initialExpression }
                scopeExit()
            }
        }

        methodNotFound { ClassNode receiver, String name, ArgumentListExpression argList, ClassNode[] argTypes, MethodCall call ->
            def dynamicCall
            if(currentScope.searchableClosureCode && currentScope.checkingSearchableClosure) {
                if(receiver.getField(name) || 'setIndexName' == name || 'root' == name || 'setExcept' == name) {
                    dynamicCall = makeDynamic (call)
                }
            }
            dynamicCall
        }
        
        null
    }
}