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
package org.androidtransfuse.gen.componentBuilder;

import com.sun.codemodel.JDefinedClass;
import org.androidtransfuse.model.MethodDescriptor;

/**
 * @author John Ericksen
 */
public class ExistingMethod implements MethodGenerator {

    private final MethodDescriptor methodDescriptor;

    public ExistingMethod(MethodDescriptor methodDescriptor) {
        this.methodDescriptor = methodDescriptor;
    }

    @Override
    public MethodDescriptor buildMethod(JDefinedClass definedClass) {
        return methodDescriptor;
    }

    @Override
    public void closeMethod(MethodDescriptor methodDescriptor) {
        //none
    }
}
