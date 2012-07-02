package org.androidtransfuse.integrationTest.observes;

import com.xtremelabs.robolectric.RobolectricTestRunner;
import org.androidtransfuse.event.EventManager;
import org.androidtransfuse.integrationTest.DelegateUtil;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Provider;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

/**
 * @author John Ericksen
 */
@RunWith(RobolectricTestRunner.class)
public class EventObserverTest {

    private EventObserver eventObserver;
    private EventManager eventManager;


    @Before
    public void setup() {
        EventObserverActivity eventObserverActivity = new EventObserverActivity();
        eventObserverActivity.onCreate(null);

        eventManager = eventObserverActivity.getScope().getScopedObject(EventManager.class, new Provider<EventManager>() {
            @Override
            public EventManager get() {
                return new EventManager();
            }
        });

        eventObserver = DelegateUtil.getDelegate(eventObserverActivity, EventObserver.class);
    }

    @Test
    public void testEventOne(){
        assertFalse(eventObserver.isEventOneTriggered());
        eventManager.trigger(new EventOne("test"));
        assertTrue(eventObserver.isEventOneTriggered());
    }

    @Test
    public void testEventTwo(){
        assertFalse(eventObserver.isEventTwoTriggered());
        eventManager.trigger(new EventTwo());
        assertTrue(eventObserver.isEventTwoTriggered());
    }


}