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

import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource
import org.apache.tinkerpop.gremlin.process.traversal.P
import org.apache.tinkerpop.gremlin.structure.Vertex

import java.sql.ResultSet
import java.sql.Connection
import java.sql.SQLException

import org.moqui.entity.EntityDynamicView

import org.moqui.entity.EntityCondition.JoinOperator
import org.moqui.entity.EntityList
import org.moqui.entity.EntityValue
import org.moqui.entity.EntityFacade
import org.moqui.entity.EntityListIterator
import org.moqui.entity.EntityException
import org.moqui.entity.EntityCondition
import org.moqui.impl.entity.EntityFacadeImpl
import org.moqui.impl.entity.EntityDefinition
import org.moqui.impl.entity.EntityListImpl
import org.moqui.impl.entity.EntityFindBuilder
import org.moqui.impl.entity.EntityFindBase
import org.moqui.impl.entity.condition.*
import org.moqui.impl.entity.EntityValueBase
import org.moqui.impl.entity.FieldInfo
import org.moqui.entity.*
import org.moqui.impl.entity.EntityJavaUtil.FieldOrderOptions

import org.moqui.addons.graph.JanusGraphEntityValue
import org.moqui.addons.graph.JanusGraphDatasourceFactory
import org.janusgraph.core.JanusGraph
import org.moqui.impl.context.ExecutionContextFactoryImpl

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.moqui.util.MNode 

class JanusGraphEntityFind { //extends EntityFindBase
    protected final static Logger logger = LoggerFactory.getLogger(JanusGraphEntityFind.class)
    protected JanusGraph janusGraph
    protected JanusGraphDatasourceFactory ddf
    protected Object fromVertexId
    protected String edgeLabel
    protected List <List<Object>> edgeProperties
    protected List <List<Object>> vertexProperties

    JanusGraphEntityFind(JanusGraphDatasourceFactory ddf) {
        this.ddf = ddf
        this.janusGraph = this.ddf.getDatabase()
        return
    }

    // ======================== Run Find Methods ==============================

    org.apache.tinkerpop.gremlin.structure.Vertex one() throws EntityException {

        long startTime = System.currentTimeMillis()
        List retList = null
        def g2,g3,g4,g5,g6,g7,g8,g9
        GraphTraversalSource g = janusGraph.traversal()
        if(fromVertexId) {
            g2=g.V(fromVertexId) //.as('a')
        } else {
            g2=g.V() //.as('a')
        }
        if (vertexProperties && vertexProperties.size()) {
            def gNext = g2
            vertexProperties.each { tuple ->
                gNext=applyPredicate(gNext, tuple[0], tuple[1], tuple[2])
            }
            g7 = gNext
        } else {
            g7 = g2
        }

        List <Vertex> resultList = g7.toList()
        if (resultList.size()) {
            return resultList[0]
        } else {
            return null
        }
    }

    /** @see org.moqui.entity.EntityFind#list() */
    List <Map<String,Object>> list() {
        long startTime = System.currentTimeMillis()
        //EntityDefinition ed = this.getEntityDef()
        List retList = null
        //JanusGraphEntityValue entValue = null
        //EntityList entList = new EntityListImpl(this.efi)
        //logger.info("JanusGraphEntityFind.list efi: ${this.efi}")
        def g2,g3,g4,g5,g6,g7,g8,g9
        GraphTraversalSource g = janusGraph.traversal()
        if(fromVertexId) {
            g2=g.V(fromVertexId) //.as('a')
        } else {
            g2=g.V() //.as('a')
        }
        if(edgeLabel) {
            g3=g2.outE(edgeLabel) //.as('e')
        } else {
            g3=g2.outE() //.as('e')
        }
        if (edgeProperties && edgeProperties.size()) {
            def gNext = g3
            edgeProperties.each { tuple ->
                gNext=applyPredicate(gNext, tuple[0], tuple[1], tuple[2])
            }
            g4 = gNext
        } else {
            g4 = g3
        }
        g6 = g4.inV()
        if (vertexProperties && vertexProperties.size()) {
            def gNext = g6
            vertexProperties.each { tuple ->
                gNext=applyPredicate(gNext, tuple[0], tuple[1], tuple[2])
            }
            g7 = gNext
        } else {
            g7 = g6
        }
        List <Map <String, Object>> resultList = g7.toList()
        return resultList
    }

    JanusGraphEntityFind condition(Object fromVertexId, String edgeLabel, List <List<Object>> edgeProperties, List <List<Object>> vertexProperties) {
        this.fromVertexId = fromVertexId
        this.edgeLabel = edgeLabel
        this.edgeProperties = edgeProperties
        this.vertexProperties = vertexProperties
        return this
    }

    def applyPredicate( edgeOrVertex, String propName, String predicateText, Object propValue) {
        switch (predicateText) {
            case "eq":
                return edgeOrVertex.has(propName, P.eq(propValue))
                break
            case "neq":
                return edgeOrVertex.has(propName, P.neq(propValue))
                break
            case "lt":
                return edgeOrVertex.has(propName, P.lt(propValue))
                break
            case "lte":
                return edgeOrVertex.has(propName, P.lte(propValue))
                break
            case "gt":
                return edgeOrVertex.has(propName, P.gt(propValue))
                break
            case "gte":
                return edgeOrVertex.has(propName, P.gte(propValue))
                break
            case "inside":
                return edgeOrVertex.has(propName, P.inside(propValue))
                break
            case "outside":
                return edgeOrVertex.has(propName, P.outside(propValue))
                break
            case "between":
                return edgeOrVertex.has(propName, P.between(propValue))
                break
            case "within":
                return edgeOrVertex.has(propName, P.within(propValue))
                break
            case "without":
                return edgeOrVertex.has(propName, P.without(propValue))
                break
            default:
                return edgeOrVertex.has(propName, P.eq(propValue))
                break
        }
    }
}
