/**
 * Copyright 2013 John Ericksen
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.androidtransfuse.bootstrap;

import com.sun.codemodel.JBlock;
import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JExpression;
import org.androidtransfuse.TransfuseAnalysisException;
import org.androidtransfuse.adapter.ASTVoidType;
import org.androidtransfuse.analysis.astAnalyzer.ASTInjectionAspect;
import org.androidtransfuse.gen.*;
import org.androidtransfuse.gen.variableBuilder.VariableBuilder;
import org.androidtransfuse.gen.variableDecorator.TypedExpressionFactory;
import org.androidtransfuse.model.FieldInjectionPoint;
import org.androidtransfuse.model.InjectionNode;
import org.androidtransfuse.model.MethodInjectionPoint;
import org.androidtransfuse.model.TypedExpression;
import org.androidtransfuse.transaction.TransactionRuntimeException;
import org.androidtransfuse.validation.Validator;

import javax.inject.Inject;

public class ExistingVariableInjectionBuilder implements VariableBuilder {

    private final JExpression expression;
    private final InvocationBuilder injectionInvocationBuilder;
    private final InjectionExpressionBuilder injectionExpressionBuilder;
    private final TypedExpressionFactory typedExpressionFactory;
    private final ExceptionWrapper exceptionWrapper;
    private final ExpressionMatchingIterableFactory generatorFactory;
    private final Validator validator;

    @Inject
    public ExistingVariableInjectionBuilder(JExpression expression,
                                            InvocationBuilder injectionInvocationBuilder,
                                            InjectionExpressionBuilder injectionExpressionBuilder,
                                            TypedExpressionFactory typedExpressionFactory,
                                            ExceptionWrapper exceptionWrapper,
                                            ExpressionMatchingIterableFactory generatorFactory,
                                            Validator validator) {
        this.expression = expression;
        this.injectionInvocationBuilder = injectionInvocationBuilder;
        this.injectionExpressionBuilder = injectionExpressionBuilder;
        this.typedExpressionFactory = typedExpressionFactory;
        this.exceptionWrapper = exceptionWrapper;
        this.generatorFactory = generatorFactory;
        this.validator = validator;
    }

    @Override
    public TypedExpression buildVariable(final InjectionBuilderContext injectionBuilderContext, InjectionNode injectionNode) {
        try {

            injectionExpressionBuilder.setupInjectionRequirements(injectionBuilderContext, injectionNode);

            final ASTInjectionAspect injectionAspect = injectionNode.getAspect(ASTInjectionAspect.class);
            JBlock block = injectionBuilderContext.getBlock();

            if (injectionAspect == null) {
                validator.error(injectionNode.getASTType() + " injection not specified")
                        .element(injectionNode.getASTType())
                        .build();
                throw new TransactionRuntimeException("Injection node not mapped: " + injectionNode.getASTType());
            } else if (injectionNode.getAspect(ASTInjectionAspect.class).getConstructorInjectionPoint() == null) {
                validator.error("Injection requires either a default no-argument constructor or an @Inject annotated constructor.")
                        .element(injectionNode.getASTType())
                        .build();
                throw new TransfuseAnalysisException("No-Arg Constructor required for injection point: " + injectionNode.getClassName());
            }
            else{

                for (ASTInjectionAspect.InjectionGroup injectionGroup : injectionAspect.getGroups()) {
                    //field injection
                    for (FieldInjectionPoint fieldInjectionPoint : injectionGroup.getFieldInjectionPoints()) {
                        block.add(
                                injectionInvocationBuilder.buildFieldSet(
                                        injectionBuilderContext.getVariableMap().get(fieldInjectionPoint.getInjectionNode()),
                                        fieldInjectionPoint,
                                        expression));
                    }

                    //method injection
                    for (final MethodInjectionPoint methodInjectionPoint : injectionGroup.getMethodInjectionPoints()) {
                        exceptionWrapper.wrapException(block,
                                methodInjectionPoint.getThrowsTypes(),
                                new ExceptionWrapper.BlockWriter<Void>() {
                                    @Override
                                    public Void write(JBlock block) throws ClassNotFoundException, JClassAlreadyExistsException {
                                        block.add(
                                                injectionInvocationBuilder.buildMethodCall(
                                                        ASTVoidType.VOID,
                                                        methodInjectionPoint,
                                                        generatorFactory.buildExpressionMatchingIterable(
                                                                injectionBuilderContext.getVariableMap(),
                                                                methodInjectionPoint.getInjectionNodes()),
                                                        expression));
                                        return null;
                                    }
                                });
                    }
                }
            }

        } catch (ClassNotFoundException e) {
            throw new TransfuseAnalysisException("Unable to parse class: " + injectionNode.getClassName(), e);
        } catch (JClassAlreadyExistsException e) {
            throw new TransfuseAnalysisException("JClassAlreadyExistsException while generating injection: " + injectionNode.getClassName(), e);
        }

        return typedExpressionFactory.build(injectionNode.getASTType(), expression);
    }
}