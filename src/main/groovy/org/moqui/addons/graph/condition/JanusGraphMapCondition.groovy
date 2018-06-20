package org.moqui.impl.entity.janusgraph.condition

import org.moqui.impl.entity.janusgraph.JanusGraphEntityConditionFactoryImpl
import org.moqui.impl.entity.EntityDefinition
import org.moqui.entity.EntityCondition
import org.moqui.entity.EntityCondition.ComparisonOperator
import org.moqui.entity.EntityCondition.JoinOperator
import org.moqui.impl.entity.janusgraph.condition.JanusGraphEntityConditionImplBase
import org.moqui.impl.entity.janusgraph.JanusGraphUtils

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.moqui.util.MNode 

class JanusGraphMapCondition {

    protected final static Logger logger = LoggerFactory.getLogger(JanusGraphMapCondition.class)
    
    protected Class internalClass = null
    protected Map<String, ?> fieldMap
    protected org.moqui.entity.EntityCondition.ComparisonOperator comparisonOperator
    protected org.moqui.entity.EntityCondition.JoinOperator joinOperator
    protected boolean ignoreCase = false

    String toString() {
        return this.fieldMap
    }

    // Dummied out calls
    void readExternal(java.io.ObjectInput obj) { return }
    boolean mapMatchesAny(java.util.Map obj) { return null }
    void writeExternal(java.io.ObjectOutput obj) { return }

}
