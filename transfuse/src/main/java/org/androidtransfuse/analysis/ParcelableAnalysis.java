package org.androidtransfuse.analysis;

import org.androidtransfuse.analysis.adapter.ASTMethod;
import org.androidtransfuse.analysis.adapter.ASTType;
import org.androidtransfuse.analysis.adapter.ASTVoidType;
import org.androidtransfuse.annotations.Transient;
import org.androidtransfuse.gen.GetterSetterMethodPair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author John Ericksen
 */
public class ParcelableAnalysis {

    public List<GetterSetterMethodPair> analyze(ASTType astType) {
        List<GetterSetterMethodPair> methodPairs = new ArrayList<GetterSetterMethodPair>();

        Map<String, ASTMethod> methodNameMap = new HashMap<String, ASTMethod>();

        for (ASTMethod astMethod : astType.getMethods()) {
            methodNameMap.put(astMethod.getName(), astMethod);
        }

        //find all applicable getters
        for (ASTMethod astMethod : astType.getMethods()) {
            if (isGetter(astMethod) && !astMethod.isAnnotated(Transient.class)) {
                String setterName = "set" + astMethod.getName().substring(3);
                if (!methodNameMap.containsKey(setterName)) {
                    throw new TransfuseAnalysisException("Unable to find setter " + setterName + " to match getter " + astMethod.getName());
                }

                ASTMethod setterMethod = methodNameMap.get(setterName);

                if (setterMethod.getParameters().size() != 1 || !setterMethod.getParameters().get(0).getASTType().equals(astMethod.getReturnType())) {
                    throw new TransfuseAnalysisException("Setter " + setterName + " has incorrect parameters.");
                }

                methodPairs.add(new GetterSetterMethodPair(astMethod, setterMethod));
            }
        }

        return methodPairs;
    }

    private boolean isGetter(ASTMethod astMethod) {
        boolean isGetter = astMethod.getParameters().size() == 0 && astMethod.getName().startsWith("get");
        if (isGetter && astMethod.getReturnType().equals(ASTVoidType.VOID)) {
            throw new TransfuseAnalysisException("Getter cannot return type void");
        }
        return isGetter;
    }
}
