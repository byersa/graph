/**
 * This Work is in the public domain and is provided on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied,
 * including, without limitation, any warranties or conditions of TITLE,
 * NON-INFRINGEMENT, MERCHANTABILITY, or FITNESS FOR A PARTICULAR PURPOSE.
 * You are solely responsible for determining the appropriateness of using
 * this Work and assume any risks associated with your use of this Work.
 *
 * This Work includes contributions authored by David E. Jones, not as a
 * "work for hire", who hereby disclaims any copyright to the same.
 */
package org.moqui.impl.entity.janusgraph.condition

import org.moqui.entity.EntityCondition
import org.moqui.impl.entity.janusgraph.JanusGraphEntityConditionFactoryImpl

import static org.moqui.entity.EntityCondition.ComparisonOperator.*
import org.moqui.impl.entity.EntityDefinition
import org.moqui.impl.entity.janusgraph.JanusGraphUtils
import org.moqui.impl.entity.janusgraph.condition.JanusGraphEntityConditionImplBase
import org.moqui.impl.entity.condition.ConditionField
import org.moqui.impl.entity.condition.FieldValueCondition

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.moqui.util.MNode 

class JanusGraphFieldValueCondition extends JanusGraphEntityConditionImplBase {

    protected final static Logger logger = LoggerFactory.getLogger(JanusGraphFieldValueCondition.class)
    protected Class localClass = null
    protected ConditionField field
    protected EntityCondition.ComparisonOperator operator
    protected Object value
    protected boolean ignoreCase = false


    JanusGraphFieldValueCondition(JanusGraphEntityConditionFactoryImpl ecFactoryImpl,
            ConditionField field, EntityCondition.ComparisonOperator operator, Object value) {
        super(ecFactoryImpl)
        this.field = field
        this.operator = operator ?: EQUALS
        this.value = value
    }

    boolean mapMatches(Map<String, ?> map) { return null }
    EntityCondition ignoreCase() { return null }

    // Dummied out calls
    boolean populateMap(Map<String, ?> map) { return false }

    void readExternal(java.io.ObjectInput obj) { return }
    boolean mapMatchesAny(java.util.Map obj) { return null }
    void writeExternal(java.io.ObjectOutput obj) { return }
}
