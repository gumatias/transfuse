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
package org.androidtransfuse.gen.variableBuilder;

import android.app.Activity;
import com.sun.codemodel.JCodeModel;
import org.androidtransfuse.TransfuseAnalysisException;
import org.androidtransfuse.adapter.ASTAnnotation;
import org.androidtransfuse.analysis.AnalysisContext;
import org.androidtransfuse.analysis.Analyzer;
import org.androidtransfuse.analysis.InjectionPointFactory;
import org.androidtransfuse.annotations.View;
import org.androidtransfuse.model.InjectionNode;
import org.androidtransfuse.model.InjectionSignature;

import javax.inject.Inject;

/**
 * @author John Ericksen
 */
public class ViewInjectionNodeBuilder extends InjectionNodeBuilderSingleAnnotationAdapter {

    private final JCodeModel codeModel;
    private final InjectionPointFactory injectionPointFactory;
    private final VariableInjectionBuilderFactory variableInjectionBuilderFactory;
    private final Analyzer analyzer;

    @Inject
    public ViewInjectionNodeBuilder(JCodeModel codeModel,
                                    InjectionPointFactory injectionPointFactory,
                                    VariableInjectionBuilderFactory variableInjectionBuilderFactory,
                                    Analyzer analyzer) {
        super(View.class);
        this.codeModel = codeModel;
        this.injectionPointFactory = injectionPointFactory;
        this.variableInjectionBuilderFactory = variableInjectionBuilderFactory;
        this.analyzer = analyzer;
    }

    @Override
    public InjectionNode buildInjectionNode(InjectionSignature signature, AnalysisContext context, ASTAnnotation annotation) {
        Integer viewId = annotation.getProperty("value", Integer.class);
        String viewTag = annotation.getProperty("tag", String.class);

        InjectionNode injectionNode = analyzer.analyze(signature, context);

        InjectionNode activityInjectionNode = injectionPointFactory.buildInjectionNode(Activity.class, context);

        try {
            injectionNode.addAspect(VariableBuilder.class, variableInjectionBuilderFactory.buildViewVariableBuilder(viewId, viewTag, activityInjectionNode, codeModel.parseType(signature.getType().getName())));
        } catch (ClassNotFoundException e) {
            throw new TransfuseAnalysisException("Unable to parse type " + signature.getType().getName(), e);
        }

        return injectionNode;
    }
}
