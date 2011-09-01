/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIESOR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.aries.jpa.container.unit.impl;

import java.util.Hashtable;
import java.util.concurrent.atomic.AtomicReference;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.apache.aries.jpa.container.impl.NLS;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JndiDataSource extends DelayedLookupDataSource {
  /** Logger */
  private static final Logger _logger = LoggerFactory.getLogger("org.apache.aries.jpa.container");
  
  private AtomicReference<DataSource> ds = new AtomicReference<DataSource>();
  
  private final String jndiName;
  private final Bundle persistenceBundle;
  
  public JndiDataSource (String jndi, Bundle persistenceBundle) {
    jndiName = jndi;
    this.persistenceBundle = persistenceBundle;
  }
  
  @Override
  protected DataSource getDs() {
    if(ds.get() == null) {
      try {
        Hashtable<String, Object> props = new Hashtable<String, Object>();
        
        BundleContext bCtx = persistenceBundle.getBundleContext();
        if(bCtx == null)
          throw new IllegalStateException(NLS.MESSAGES.getMessage("persistence.bundle.not.active", persistenceBundle.getSymbolicName(), persistenceBundle.getVersion()));
        props.put("osgi.service.jndi.bundleContext", bCtx);
        InitialContext ctx = new InitialContext(props);
        ds.compareAndSet(null, (DataSource) ctx.lookup(jndiName));
      } catch (NamingException e) {
        String message = NLS.MESSAGES.getMessage("no.data.source.found", jndiName, persistenceBundle.getSymbolicName(), persistenceBundle.getVersion());
        _logger.error(message, e);
        throw new RuntimeException(message, e);
      }
    }
    return ds.get();
  }

}
