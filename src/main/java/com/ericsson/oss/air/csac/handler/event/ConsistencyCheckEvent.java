/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.csac.handler.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * This customized event class  extends the Spring ApplicationEvent {@link ApplicationEvent} class and
 * it is consumed by the {@link CsacEventListener} for external notifications.
 * <p>
 * The event payload will be encapsulated in a data bean and will comprise:
 *
 * <ul>
 * <li>ConsistencyCheckEvent.Type: enum of OK, SUSPECT, FAILURE, CLEAR </li>
 * <li>count of the number of inconsistencies identified in this event.  Can be ignored if Type is OK </li>
 * </ul>
 */
@Getter
public class ConsistencyCheckEvent extends ApplicationEvent {

    private static final long serialVersionUID = 6912777563980482556L;

    private final transient Payload payload;

    @Getter
    public static class Payload {

        private final Type type;

        private final int count;

        public Payload(final Type type, final int count) {
            this.type = type;
            this.count = count;
        }

        public enum Type {
            OK,
            SUSPECT,
            FAILURE,
            CLEAR
        }
    }

    /**
     * Constructor for ConsistencyCheckEvent object
     *
     * @param source  source of Object class
     * @param payload payload received in the event
     */
    public ConsistencyCheckEvent(final Object source, final Payload payload) {
        super(source);
        this.payload = payload;
    }

}
