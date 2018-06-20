/*
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

import org.moqui.impl.entity.EntityConditionFactoryImpl
import org.moqui.impl.entity.janusgraph.JanusGraphEntityConditionFactoryImpl
import org.moqui.impl.entity.EntityQueryBuilder
import org.moqui.impl.entity.EntityDefinition
import org.moqui.entity.EntityCondition
import org.moqui.impl.entity.condition.ListCondition
import org.moqui.impl.entity.janusgraph.condition.JanusGraphEntityConditionImplBase

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.moqui.util.MNode 

class JanusGraphListCondition extends JanusGraphEntityConditionImplBase {
    protected final static Logger logger = LoggerFactory.getLogger(JanusGraphListCondition.class)
    protected Class localClass = null
    protected List<JanusGraphEntityConditionImplBase> conditionList
    protected EntityCondition.JoinOperator operator

    JanusGraphListCondition(JanusGraphEntityConditionFactoryImpl ecFactoryImpl,
            List<JanusGraphEntityConditionImplBase> conditionList, EntityCondition.JoinOperator operator) {
        super(ecFactoryImpl)
        this.conditionList = conditionList ?: new LinkedList()
        this.operator = operator ?: EntityCondition.JoinOperator.AND

    }

    void addCondition(JanusGraphEntityConditionImplBase condition) {
         conditionList.add(condition) 
                   logger.info("JanusGraphListCondition, conditionList: ${conditionList}")
    }

    boolean mapMatches(Map<String, ?> map) { return null }
    EntityCondition ignoreCase() { return null }

    String toString() {
        return this.conditionList
    }

    // Dummied out calls
    void readExternal(java.io.ObjectInput obj) { return }
    boolean mapMatchesAny(java.util.Map obj) { return null }
    void writeExternal(java.io.ObjectOutput obj) { return }

}
