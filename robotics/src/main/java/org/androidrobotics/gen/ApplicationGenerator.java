package org.androidrobotics.gen;

import android.app.Application;
import com.sun.codemodel.*;
import org.androidrobotics.analysis.astAnalyzer.MethodCallbackAspect;
import org.androidrobotics.model.ApplicationDescriptor;
import org.androidrobotics.model.InjectionNode;
import org.androidrobotics.model.r.RResource;

import javax.inject.Inject;
import java.io.IOException;
import java.util.Map;
import java.util.Set;

/**
 * @author John Ericksen
 */
public class ApplicationGenerator {

    private JCodeModel codeModel;
    private InjectionFragmentGenerator injectionFragmentGenerator;

    @Inject
    public ApplicationGenerator(JCodeModel codeModel, InjectionFragmentGenerator injectionFragmentGenerator) {
        this.codeModel = codeModel;
        this.injectionFragmentGenerator = injectionFragmentGenerator;
    }

    public void generate(ApplicationDescriptor descriptor, RResource rResource) throws JClassAlreadyExistsException, IOException, ClassNotFoundException {

        final JDefinedClass definedClass = codeModel._class(JMod.PUBLIC, descriptor.getPackageClass().getFullyQualifiedName(), ClassType.CLASS);

        definedClass._extends(Application.class);

        if (!descriptor.getInjectionNodes().isEmpty()) {

            final JMethod onCreateMethod = definedClass.method(JMod.PUBLIC, codeModel.VOID, "onCreate");
            JBlock block = onCreateMethod.body();
            block.invoke(JExpr._super(), onCreateMethod);

            for (InjectionNode injectionNode : descriptor.getInjectionNodes()) {


                Map<InjectionNode, JExpression> expressionMap = injectionFragmentGenerator.buildFragment(block, definedClass, injectionNode, rResource);

                addMethodCallbacks("onCreate", expressionMap, new AlreadyDefinedMethodGenerator(onCreateMethod));
                // onLowMemory
                addLifecycleMethod("onLowMemory", definedClass, expressionMap);
                // onTerminate
                addLifecycleMethod("onTerminate", definedClass, expressionMap);
            }

            //temp
            JType singletonType = codeModel.parseType("org.androidrobotics.example.simple.SingletonTarget");
            JMethod getSingletonTargetMethod = definedClass.method(JMod.SYNCHRONIZED & JMod.PUBLIC, singletonType, "getSingletonTarget");

            JBlock getSingletonTargetBody = getSingletonTargetMethod.body();

            JFieldVar singletonField = definedClass.field(JMod.PRIVATE, singletonType, "singleton");

            JBlock conditionBlock = getSingletonTargetBody._if(singletonField.eq(JExpr._null()))._then();

            conditionBlock.assign(singletonField, JExpr._new(singletonType));

            getSingletonTargetBody._return(singletonField);

        }
    }

    private void addLifecycleMethod(String name, JDefinedClass definedClass, Map<InjectionNode, JExpression> expressionMap) {
        addMethodCallbacks(name, expressionMap, new SimpleMethodGenerator(name, definedClass, codeModel));
    }

    private void addMethodCallbacks(String name, Map<InjectionNode, JExpression> expressionMap, MethodGenerator lazyMethodGenerator) {
        JMethod method = null;
        for (Map.Entry<InjectionNode, JExpression> injectionNodeJExpressionEntry : expressionMap.entrySet()) {
            MethodCallbackAspect methodCallbackAspect = injectionNodeJExpressionEntry.getKey().getAspect(MethodCallbackAspect.class);

            if (methodCallbackAspect != null) {
                Set<MethodCallbackAspect.MethodCallback> methods = methodCallbackAspect.getMethodCallbacks(name);

                if (methods != null) {

                    //define method
                    if (method == null) {
                        method = lazyMethodGenerator.buildMethod();
                    }
                    JBlock body = method.body();

                    for (MethodCallbackAspect.MethodCallback methodCallback : methodCallbackAspect.getMethodCallbacks(name)) {
                        //todo: non-public access
                        body.add(injectionNodeJExpressionEntry.getValue().invoke(methodCallback.getMethod().getName()));
                    }
                }
            }
        }
        if (method != null) {
            lazyMethodGenerator.closeMethod();
        }
    }

    private interface MethodGenerator {
        JMethod buildMethod();

        void closeMethod();
    }

    private static final class SimpleMethodGenerator implements MethodGenerator {
        private String name;
        private JDefinedClass definedClass;
        private JCodeModel codeModel;

        public SimpleMethodGenerator(String name, JDefinedClass definedClass, JCodeModel codeModel) {
            this.name = name;
            this.definedClass = definedClass;
            this.codeModel = codeModel;
        }

        @Override
        public JMethod buildMethod() {
            JMethod method = definedClass.method(JMod.PUBLIC, codeModel.VOID, name);
            JBlock body = method.body();
            body.add(JExpr._super().invoke(name));
            return method;
        }

        @Override
        public void closeMethod() {
            //noop
        }
    }

    private static final class AlreadyDefinedMethodGenerator implements MethodGenerator {

        private JMethod method;

        private AlreadyDefinedMethodGenerator(JMethod method) {
            this.method = method;
        }

        @Override
        public JMethod buildMethod() {
            return method;
        }

        @Override
        public void closeMethod() {
            //noop
        }
    }
}