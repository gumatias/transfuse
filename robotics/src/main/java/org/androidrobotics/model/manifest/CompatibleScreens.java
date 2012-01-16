package org.androidrobotics.model.manifest;

import com.thoughtworks.xstream.annotations.XStreamImplicit;

import java.util.List;

/**
 * child elements:
 * <screen>
 *
 * @author John Ericksen
 */
public class CompatibleScreens {

    @XStreamImplicit(itemFieldName = "screen")
    private List<ScreenOrientation> screens;

    public List<ScreenOrientation> getScreens() {
        return screens;
    }

    public void setScreens(List<ScreenOrientation> screens) {
        this.screens = screens;
    }
}
