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
import org.janusgraph.graphdb.vertices.StandardVertex
import org.moqui.impl.entity.EntityDatasourceFactoryImpl
import org.janusgraph.core.schema.JanusGraphManagement
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal

import org.apache.tinkerpop.gremlin.structure.Vertex
import org.apache.tinkerpop.gremlin.structure.VertexProperty

import java.sql.Timestamp
import java.util.Date
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
    //protected long id
    protected JanusGraphDatasourceFactory ddf
    protected org.apache.tinkerpop.gremlin.structure.Vertex vertex
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

    JanusGraphEntityValue(EntityDefinition ed, EntityFacadeImpl efip, org.apache.tinkerpop.gremlin.structure.Vertex v) {
        super(ed, efip)
        setVertex(v)
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
        GraphTraversalSource g = janusGraph.traversal()
        //JanusGraphTransaction tx = janusGraph.buildTransaction().start()
        //ManagementSystem mgmt = janusGraph.openManagement()
        //StandardJanusGraphTx tx = mgmt.getWrappedTx()
        EntityDefinition ed = getEntityDefinition()
        String labelName = ed.getEntityNode().attribute("entity-name")
        Vertex v = g.addV(labelName).iterator().next()
        setVertex(v)
        this.set("id", v.id())
        java.util.Date sameDate = new java.util.Date(new Timestamp(System.currentTimeMillis()).getTime())
        this.set("createdDate", sameDate)
        this.set("lastUpdatedStamp", sameDate)
        List fieldNames = ed.getAllFieldNames()
        EntityValue _this = this
        VertexProperty vProp
        fieldNames.each {fieldName ->
            if (_this.get(fieldName)) {
                vProp = v.property(fieldName,
                        _this.getDataValue(fieldName)
                )
                logger.info("Property for (${fieldName}): ${vProp.value()}")
            }
        }
        //java.util.Date sameDate = new java.util.Date()
        //v.property("createdDate", sameDate)
        //v.property("lastUpdatedStamp", sameDate)
        //Map map = new HashMap()
        //map.putAll(v.properties().collectEntries {it ->[(it.key()):it.value()]})
        //logger.info("Created vertex: ${map}")
        //logger.info("Created entity: ${this.getValueMap()}")
        g.tx().commit()
        g.close()
        return this
    }

    public EntityValue update() {
        StandardJanusGraph janusGraph = getDatabase()
        GraphTraversalSource g = janusGraph.traversal()
        //JanusGraphTransaction tx = janusGraph.buildTransaction().start()
        //ManagementSystem mgmt = janusGraph.openManagement()
        //StandardJanusGraphTx tx = mgmt.getWrappedTx()
        EntityDefinition ed = getEntityDefinition()
        //JanusGraphVertex v = mgmt.getSchemaVertex(mgmt.getSchemaElement(this.getId()))
        Vertex v
        if (!vertex) {
            v = g.V(this.getId()).iterator().next()
            setVertex(v)
        } else {
            v = vertex
        }

        java.util.Date sameDate = new java.util.Date(new Timestamp(System.currentTimeMillis()).getTime())
        this.set("lastUpdatedStamp", sameDate)
        List fieldNames = ed.getAllFieldNames()
        EntityValue _this = this
        VertexProperty vProp
        fieldNames.each {fieldName ->
            if (_this.get(fieldName)) {
                vProp = v.property(fieldName,
                        _this.getDataValue(fieldName)
                )
                logger.info("Property for (${fieldName}): ${vProp.value()}")
            }
        }
        //java.util.Date sameDate = new java.util.Date()
        //v.property("createdDate", sameDate)
        //v.property("lastUpdatedStamp", sameDate)
        Map map = new HashMap()
        map.putAll(v.properties().collectEntries {it ->[(it.key()):it.value()]})
        logger.info("Updated vertex: ${map}")
        logger.info("Updated entity: ${this.getValueMap()}")
        g.tx().commit()
        g.close()
        //JanusGraphTransaction tx2 = janusGraph.buildTransaction().start()
        //JanusGraphVertex v2 = tx2.getVertex(this.getId())
        //Map map2 = new HashMap()
        //map2.putAll(v2.properties().collectEntries {it ->[(it.key()):it.value()]})
        //logger.info("Updated vertex(2): ${map2}")
        return this
    }

    public EntityValue delete() {
        StandardJanusGraph janusGraph = getDatabase()
        GraphTraversalSource g = janusGraph.traversal()
        //JanusGraphTransaction tx = janusGraph.buildTransaction().start()
        //JanusGraphVertex v = tx.getVertex(this.getId())
        Vertex v = g.V(this.getId()).iterator().next()
        //JanusGraphTransaction tx = janusGraph.buildTransaction().start()
        //ManagementSystem mgmt = janusGraph.openManagement()
        //StandardJanusGraphTx tx = mgmt.getWrappedTx()
        //EntityDefinition ed = getEntityDefinition()
        //JanusGraphVertex v = mgmt.getSchemaVertex(mgmt.getSchemaElement(this.getId()))
        v.remove()
        g.tx().commit()
        g.close()
        return
    }

    Object getId() {
        return this.get("id")
    }

    void testFunction() {
        return;
    }

    EntityValue cloneValue() {
        // FIXME
        return this
    }

    EntityValue cloneDbValue(boolean b) {
        // FIXME
        return this
    }

    public void createExtended(FieldInfo[] fieldInfoArray, Connection con) {return }
    public void updateExtended(FieldInfo[] pkFieldArray, FieldInfo[] nonPkFieldArray, Connection con) {return }
    public void deleteExtended(Connection con) {return }
    public boolean refreshExtended() { return null}

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

    Object getDataValue(fieldName) {

        Object retVal
        String labelString, fieldType
        EntityDefinition ed = getEntityDefinition()
        MNode fieldNode = ed.getFieldNode(fieldName)
        fieldType = fieldNode.attribute("type")
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
                retVal = new java.util.Date(this.get(fieldName).getTime())
                break
            default:
                retVal = get(fieldName)
        }
        return retVal
    }

    EntityValue setAll(Map <String,Object> fieldMap) {
        EntityValue entityValue = super.setAll(fieldMap)
        return entityValue
    }
    org.apache.tinkerpop.gremlin.structure.Vertex getVertex() {
            return vertex
    }
    void setVertex(org.apache.tinkerpop.gremlin.structure.Vertex v) {
        vertex = v
    }
}
