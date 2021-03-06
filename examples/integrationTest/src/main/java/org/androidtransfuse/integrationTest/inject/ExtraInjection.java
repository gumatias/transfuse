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
package org.androidtransfuse.integrationTest.inject;

import android.widget.TextView;
import org.androidtransfuse.annotations.*;
import org.androidtransfuse.integrationTest.R;
import org.androidtransfuse.integrationTest.SerializableValue;
import org.androidtransfuse.util.DeclareField;

import javax.inject.Inject;

/**
 * @author John Ericksen
 */
@Activity(label = "Extras")
@Layout(R.layout.extras)
@DeclareField
public class ExtraInjection {

    public static final String EXTRA_ONE = "extraOne";
    public static final String EXTRA_TWO = "extraTwo";
    public static final String EXTRA_THREE = "extraThree";
    public static final String EXTRA_FOUR = "extraFour";
    public static final String EXTRA_PARCELABLE = "extraParcelable";

    @Inject
    @Extra(EXTRA_ONE)
    private String extraOne;

    private long extraTwo;

    @Inject
    @Extra(value = EXTRA_THREE, optional = true)
    private String extraThree;

    @Inject
    @Extra(value = EXTRA_FOUR)
    private SerializableValue extraFour;

//    @Inject
//    @Extra(value = EXTRA_PARCELABLE)
//    private ParcelExample parcelExample;

    @Inject
    @View(R.id.extrasText)
    private TextView textView;

    @Inject
    @Extra(EXTRA_TWO)
    public void setExtraTwo(long extraTwo) {
        this.extraTwo = extraTwo;
    }

    @OnCreate
    public void updateUI(){
        StringBuilder builder = new StringBuilder();

        output(builder, EXTRA_ONE, extraOne);
        output(builder, EXTRA_TWO, extraTwo);
        output(builder, EXTRA_THREE, extraThree);
        output(builder, EXTRA_FOUR, extraFour);
//        output(builder, EXTRA_PARCELABLE, parcelExample);

        textView.setText(builder.toString());
    }

    private void output(StringBuilder builder, String name, Object value) {
        builder.append(name);
        builder.append(":\n\t");
        if(value != null){
            builder.append(value.toString().replaceAll("\\n", "\n\t"));
        }
        else{
            builder.append("null");
        }
        builder.append("\n");
    }

    public String getExtraOne() {
        return extraOne;
    }

    public long getExtraTwo() {
        return extraTwo;
    }

    public String getExtraThree() {
        return extraThree;
    }

    public SerializableValue getExtraFour() {
        return extraFour;
    }

//    public ParcelExample getParcelExample() {
//        return parcelExample;
//    }
}
