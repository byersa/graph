/*
 * This Work is in the public domain and is provided on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied,
 * including, without limitation, any warranties or conditions of TITLE,
 * NON-INFRINGEMENT, MERCHANTABILITY, or FITNESS FOR A PARTICULAR PURPOSE.
 * You are solely responsible for determining the appropriateness of using
 * this Work and assume any risks associated with your use of this Work.
 *
 * This Work includes contributions authored by Al Byers, not as a
 * "work for hire", who hereby disclaims any copyright to the same.
 */
package org.moqui.impl.entity.janusgraph

import org.apache.tinkerpop.gremlin.structure.Edge
import org.moqui.impl.entity.EntityDefinition
import org.moqui.impl.entity.EntityFacadeImpl
import org.moqui.entity.EntityFind
import org.moqui.impl.entity.EntityFindImpl
import org.moqui.entity.EntityValue
import org.moqui.impl.entity.EntityValueImpl

import org.joda.time.format.*

import javax.sql.DataSource

import org.moqui.entity.*
import java.sql.Types

import java.util.HashMap
import java.util.Map
import java.lang.Exception

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.moqui.util.MNode

import org.janusgraph.core.JanusGraphFactory
import org.janusgraph.core.JanusGraph
import org.janusgraph.core.VertexLabel
import org.janusgraph.core.EdgeLabel
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource
import org.janusgraph.graphdb.database.management.ManagementSystem



/**
 * To use this:
 * 1. add a datasource under the entity-facade element in the Moqui Conf file; for example:
 *      <datasource group-name="transactional_nosql" object-factory="org.moqui.impl.entity.janusgraph.JanusGraphDatasourceFactory">
 *          <inline-other uri="local:runtime/db/orient/transactional" username="moqui" password="moqui"/>
 *      </datasource>
 *
 * 3. add the group-name attribute to entity elements as needed to point them to the new datasource; for example:
 *      group-name="transactional_nosql"
 */

class JanusGraphDatasourceFactory implements EntityDatasourceFactory {
    protected final static Logger logger = LoggerFactory.getLogger(JanusGraphDatasourceFactory.class)

    protected EntityFacadeImpl efi
    protected MNode datasourceNode
    protected String tenantId

    protected JanusGraph janusGraph
    protected GraphTraversalSource janusGraphClient
    protected ManagementSystem janusGraphMgmt

JanusGraph janusGraph

    JanusGraphDatasourceFactory() { 
    }

    EntityDatasourceFactory init(org.moqui.entity.EntityFacade ef, org.moqui.util.MNode nd) {
        // local fields
        this.efi = (EntityFacadeImpl) ef
        this.datasourceNode = nd
        this.tenantId = tenantId

        janusGraph = this.open('inmemory')
        janusGraphClient = janusGraph.traversal()
        janusGraphMgmt - janusGraph.openManagement()
        logger.info("janusGraphClient: ${janusGraphClient}")
        return this
    }

    GraphTraversalSource getJanusGraphClient() { return janusGraphClient}

    /** Returns the main database access object for OrientDB.
     * Remember to call close() on it when you're done with it (preferably in a try/finally block)!
     */
    JanusGraph getDatabase() { return janusGraph}

    @Override
    void destroy() {
        return
    }
    @Override
    EntityValue makeEntityValue(String entityName) {
        EntityDefinition entityDefinition = efi.getEntityDefinition(entityName)
        if (!entityDefinition) {
            throw new EntityException("Entity not found for name [${entityName}]")
        }
        return new JanusGraphEntityValue(entityDefinition, efi, this)
    }

    @Override
    EntityFind makeEntityFind(String entityName) {
        return new JanusGraphEntityFind(efi, entityName, this)
    }
    @Override
    DataSource getDataSource() { return null }

    @Override
    void checkAndAddTable(java.lang.String tableName) {

            logger.info("checking: ${tableName}")
            // check to see if table is in schema. put it there if not

            String labelString
            def ed = efi.getEntityDefinition(tableName)
            MNode entityNode = ed.getEntityNode()
            String graphMode = entityNode.attribute("graphMode")
            if (graphMode && graphMode.toLowerCase() == "edge") {
                EdgeLabel edgeLabel = janusGraphMgmt.getEdgeLabel(tableName)
                if (!edgeLabel) {
                    janusGraphMgmt.makeEdgeLabel(tableName)
                }
                edgeLabel = janusGraphMgmt.getEdgeLabel(tableName)
                if (!edgeLabel || (edgeLabel.label != tableName)) {
                    throw new Exception( "Cannot create vertex label: " + tableName)
                }
            } else {
                VertexLabel vertexLabel = janusGraphMgmt.getVertexLabel(tableName)
                if (!vertexLabel) {
                    janusGraphMgmt.makeVertexLabel(tableName)
                }
                vertexLabel = janusGraphMgmt.getVertexLabel(tableName)
                if (!vertexLabel || (vertexLabel.label != tableName)) {
                    throw new Exception( "Cannot create vertex label: " + tableName)
                }
            }

            List <String> fieldNames = ed.getFieldNames(true, true)
            MNode nd
            String typ
            Class clazz
            fieldNames.each() { nodeName ->
                nd = ed.getFieldNode(nodeName)
                typ = nd.attribute("type")
                clazz = getDataType(typ)
                janusGraphMgmt.makePropertyKey(nodeName).dataType(clazz).make()
            }
        janusGraphMgmt.commit()
        return
    }

    void createTable(tableName) {
//        CreateTableRequest request = this.getCreateTableRequest(tableName)
//        CreateTableResult createTableResult = janusGraphClient.createTable(request)
//                    logger.info("isActive: ${createTableResult.getTableDescription()}")
    } 


    Class getDataType(fieldType) {

                   Class dataTypeClass
                   switch(fieldType) {
                        case "id":
                        case "id-long":
                        case "text-short":
                        case "text-medium":
                        case "text-long":
                        case "text-very-long":
                        case "text-indicator":
                            dataTypeClass = (Class)String.class
                            break
                        case "number-integer":
                        case "number-decimal":
                        case "number-float":
                        case "currency-amount":
                        case "currency-precise":
                            dataTypeClass = (Class)Double.class
                            break
                        case "date":
                        case "time":
                        case "date-time":
                             dataTypeClass = (Class)Date.class
                             break
                        default:
                             dataTypeClass = (Class)String.class
                   }
                   return dataTypeClass
    }
    // Dummied out methods
    boolean checkTableExists(java.lang.String s) {return null}
}
