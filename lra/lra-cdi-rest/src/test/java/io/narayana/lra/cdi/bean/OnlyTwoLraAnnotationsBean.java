/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2017, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package io.narayana.lra.cdi.bean;

import io.narayana.lra.annotation.Compensate;
import io.narayana.lra.annotation.Complete;
import io.narayana.lra.annotation.LRA;
import io.narayana.lra.annotation.Status;

/**
 * {@link LRA} bean which contains only two annotations - {@link Complete} and {@link Status} 
 * but the LRA prescribe for the bean to contain three: {@link Compensate}, {@link Complete} and {@link Status}.
 *
 * @author Ondra Chaloupka <ochaloup@redhat.com>
 */
@LRA
public class OnlyTwoLraAnnotationsBean {

    @Complete
    public void complete() {
        // no implementation needed
    }
    
    @Status
    public void status() {
        // no implementation needed
    }
}
