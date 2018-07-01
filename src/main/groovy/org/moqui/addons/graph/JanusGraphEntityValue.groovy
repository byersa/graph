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
package org.moqui.addons.graph

import org.janusgraph.core.JanusGraphTransaction
import org.janusgraph.core.JanusGraphVertex
import org.janusgraph.graphdb.database.StandardJanusGraph
import org.janusgraph.graphdb.database.management.ManagementSystem
import org.janusgraph.graphdb.transaction.StandardJanusGraphTx
import org.moqui.impl.entity.EntityDatasourceFactoryImpl
import org.janusgraph.core.schema.JanusGraphManagement

import java.sql.Timestamp
import java.sql.Date
import java.sql.Connection
import java.text.SimpleDateFormat
import org.apache.commons.collections.set.ListOrderedSet

import org.moqui.entity.EntityException
import org.moqui.impl.entity.EntityDefinition
import org.moqui.impl.entity.EntityFacadeImpl
import org.moqui.impl.entity.EntityValueBase
import org.moqui.impl.entity.EntityValueImpl
import org.moqui.entity.EntityValue
import org.moqui.entity.EntityFind
import org.moqui.impl.entity.FieldInfo
import org.moqui.addons.graph.JanusGraphDatasourceFactory
import org.moqui.addons.graph.JanusGraphEntityFind

import org.janusgraph.core.JanusGraph

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.moqui.util.MNode

class JanusGraphEntityValue extends EntityValueBase {
    protected final static Logger logger = LoggerFactory.getLogger(JanusGraphEntityValue.class)
    protected String id
    protected JanusGraphDatasourceFactory ddf
    //protected JanusGraphEntityConditionFactoryImpl conditionFactory

    JanusGraphEntityValue(EntityDefinition ed, EntityFacadeImpl efip, JanusGraphDatasourceFactory ddf) {
        super(ed, efip)
        //this.conditionFactory = new JanusGraphEntityConditionFactoryImpl(efip)
        this.ddf = ddf
        return
    }

    JanusGraphEntityValue(EntityDefinition ed, EntityFacadeImpl efip, JanusGraphDatasourceFactory ddf, Map valMap) {
        super(ed, efip)
        this.ddf = ddf
    }

    StandardJanusGraph getDatabase() {
        EntityFacadeImpl efi = getEntityFacadeImpl()
        EntityDefinition ed = getEntityDefinition()
        JanusGraphDatasourceFactory edfi = efi.getDatasourceFactory(ed.getEntityGroupName()) as JanusGraphDatasourceFactory
        StandardJanusGraph janusGraph = edfi.getDatabase()
        return janusGraph
    }
    public EntityValue create() {
        StandardJanusGraph janusGraph = getDatabase()
        //JanusGraphTransaction tx = janusGraph.buildTransaction().start()
        ManagementSystem mgmt = janusGraph.openManagement()
        StandardJanusGraphTx tx = mgmt.getWrappedTx()
        EntityDefinition ed = getEntityDefinition()
        String labelName = ed.getEntityNode().attribute("entity-name")
        JanusGraphVertex v = tx.addVertex(labelName)
        this.id = v.id()
        List fieldNames = ed.getAllFieldNames()
        EntityValue _this = this
        fieldNames.each {fieldName ->
            if (_this.get(fieldName)) {
                v.property(fieldName, _this.get(fieldName))
            }
        }
        tx.commit()
        return this
    }

    String getId() {
        return this.id
    }
    void buildAttributeValueMap( Map<String, Object> item, Map<String, Object> valueMap) {
        EntityDefinition entityDefinition = getEntityDefinition()
        ListOrderedSet fieldNames = entityDefinition.getFieldNames(true, true)
        for(String fieldName in fieldNames) {
            Object attrVal = JanusGraphUtils.getAttributeValue(fieldName, valueMap, entityDefinition)
            logger.info("JanusGraphEntityValue.buildAttributeValueMap(250) attrVal: ${attrVal}")
            if (attrVal != null) {
                logger.info("JanusGraphEntityValue.buildAttributeValueMap(252) fieldName: ${fieldName}, attrVal: ${attrVal}")
                item.put(fieldName, attrVal)
            } else {
                logger.info("JanusGraphEntityValue.buildAttributeValueMap(remove - 255) fieldName: ${fieldName}")
                item.remove(fieldName)
            }
        }

    }

    void buildAttributeValueUpdateMap( Map<String, Object> item, Map<String, Object> valueMap) {
        EntityDefinition entityDefinition = getEntityDefinition()
        ListOrderedSet fieldNames = entityDefinition.getFieldNames(true, true)
        for(String fieldName in fieldNames) {
            Object attrVal = JanusGraphUtils.getAttributeValueUpdate(fieldName, valueMap, entityDefinition)
            logger.info("JanusGraphEntityValue.buildAttributeValueMap(250) attrVal: ${attrVal}")
            if (attrVal != null) {
                logger.info("JanusGraphEntityValue.buildAttributeValueMap(252) fieldName: ${fieldName}, attrVal: ${attrVal}")
                item.put(fieldName, attrVal)
            } else {
                logger.info("JanusGraphEntityValue.buildAttributeValueMap(remove - 255) fieldName: ${fieldName}")
                item.remove(fieldName)
            }
        }

    }

    void testFunction() {
        return;
    }

    //void buildEntityValueMap( Map<String, AttributeValue> attributeValueItem) {
    void buildEntityValueMap( ) {
        return;

        String fieldName, fieldType
        Object attrVal
        def tm, num
        for(Map.Entry fieldEntry in attributeValueItem) {
            fieldName = fieldEntry.key
            logger.info("JanusGraphEntityValue.buildEntityValueMap(280) fieldName: ${fieldName}")
            MNode fieldNode = this.getEntityDefinition().getFieldNode(fieldName)
            fieldType = fieldNode."@type"
            logger.info("JanusGraphEntityValue.buildEntityValueMap(282) type: ${fieldType}")
            switch(fieldType) {
                case "id":
                case "id-long":
                case "text-short":
                case "text-medium":
                case "text-long":
                case "text-very-long":
                case "text-indicator":
                    this.set(fieldName, attributeValueItem[fieldName].getS())
                    break
                case "number-integer":
                case "number-decimal":
                case "number-float":
                case "currency-amount":
                case "currency-precise":
                case "time":
                    attrVal = attributeValueItem[fieldName]
                    num = attrVal.getN()
                    this.set(fieldName, Long.parseLong(num))
                    break
                case "date":
                    attrVal = attributeValueItem[fieldName]
                    logger.info("JanusGraphEntityValue.buildEntityValueMap(300) attrVal: ${attrVal}")
                    tm = attrVal.getN()
                    Date dt = new Date(tm)
                    logger.info("JanusGraphEntityValue.buildEntityValueMap(303) tm: ${tm}, dt: ${dt}")
                    this.set(fieldName, dt )
                    break
                case "date-time":
                    attrVal = attributeValueItem[fieldName]
                    logger.info("JanusGraphEntityValue.buildEntityValueMap(313) attrVal: ${attrVal}")
                    tm = attrVal.getS()
                    logger.info("JanusGraphEntityValue.buildEntityValueMap(315) tm: ${tm}")
                    Timestamp ts = Timestamp.valueOf(tm)
                    logger.info("JanusGraphEntityValue.buildEntityValueMap(317) ts: ${ts}")
                    this.set(fieldName, ts )
                    break
                default:
                    this.set(fieldName, null)
            }
        }

    }

    EntityValue cloneValue() {
        // FIXME
        return this
    }

    EntityValue cloneDbValue(boolean b) {
        // FIXME
        return this
    }

    HashMap<String, Object> getValueMap() {
        HashMap<String, Object> newValueMap = new HashMap()
        HashMap<String, Object> parentValueMap = super.getValueMap()
        logger.info("parentValueMap: ${parentValueMap}")
        parentValueMap.each{k,v ->
            if (v instanceof Timestamp) {
                logger.info("${k} is Timestamp")
                newValueMap[k] = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(v)
            } else {
                if (v) {
                    newValueMap[k] = v
                }
            }
        }
        logger.info("newValueMap: ${newValueMap}")
        return newValueMap
    }

    Class getDataTypeValue(fieldName) {

        Object retVal
        String labelString
        EntityDefinition ed = getEntityDefinition()
        MNode entityNode = ed.getEntityNode()
        String fieldType = entityNode.attribute("type")
        switch(fieldType) {
            case "id":
            case "id-long":
            case "text-short":
            case "text-medium":
            case "text-long":
            case "text-very-long":
            case "text-indicator":
                retVal = getString(fieldName)
                break
            case "number-integer":
            case "number-decimal":
            case "number-float":
            case "currency-amount":
            case "currency-precise":
                retVal = getDouble(fieldName)
                break
            case "date":
            case "time":
            case "date-time":
                retVal = getDate(fieldName
                )
                break
            default:
                retVal = get(fieldName)
        }
        return retVal
    }
}
