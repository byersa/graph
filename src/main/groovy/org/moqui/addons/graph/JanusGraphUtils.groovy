package org.moqui.addons.graph

import org.janusgraph.graphdb.database.StandardJanusGraph
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource
import org.apache.tinkerpop.gremlin.structure.Edge
import org.apache.tinkerpop.gremlin.structure.Vertex
import org.apache.tinkerpop.gremlin.structure.Property

import java.sql.Timestamp
import org.moqui.context.ExecutionContext
import org.moqui.impl.entity.EntityDefinition
import org.moqui.entity.EntityFacade
import org.moqui.entity.EntityDatasourceFactory
import org.moqui.addons.graph.JanusGraphEntityValue
import org.moqui.entity.EntityCondition

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.moqui.util.MNode 

class JanusGraphUtils {

    protected final static Logger logger = LoggerFactory.getLogger(JanusGraphUtils.class)

    static Object getAttributeValue(String fieldName, Map<String,?>valueMap, EntityDefinition ed) {
    
        Object attrVal = new Object()
        MNode fieldNode = ed.getFieldNode(fieldName)
        String fieldNodeName = fieldNode."@name"
                 logger.info("JanusGraphUtils.getAttributeValue(291) fieldNodeName: ${fieldNodeName}")
        String fieldNodeType = fieldNode."@type"
                 logger.info("JanusGraphUtils.getAttributeValue(293) fieldNodeType: ${fieldNodeType}")
        switch(fieldNodeType) {
            case "id":
            case "id-long":
            case "text-short":
            case "text-medium":
            case "text-long":
            case "text-very-long":
            case "text-indicator":
                 String val = valueMap.get(fieldName)?: ""
                 logger.info("JanusGraphUtils.getAttributeValue(32) val: ${val}")
                 if (val) {
                    attrVal.setS(val)
                 } else {
                     return null
                 }
                 break
            case "number-integer":
                 String val = valueMap.get(fieldName)?: ""
                 logger.info("JanusGraphUtils.getAttributeValue(41) val: ${val}")
                 if (val) {
                    attrVal.setN(val)
                 } else {
                     return null
                 }
                 break
            case "number-decimal":
            case "number-float":
            case "currency-amount":
            case "currency-precise":
                 return attrVal.setN(valueMap.get(fieldName).toString())
                 break
            case "date":
            case "time":
            case "date-time":
                 String dateTimeStr = valueMap.get(fieldName)
                 logger.info("JanusGraphUtils(310) dateTimeStr: ${dateTimeStr}")
                 if (dateTimeStr != null) {
                     Timestamp ts = Timestamp.valueOf(dateTimeStr)
                     logger.info("JanusGraphUtils(312) ts: ${ts.toString()}")
                     attrVal.setS(ts.toString())
                 } else {
                     return null
                 }
                 break
            default:
                 String val = valueMap.get(fieldName)?: ""
                 if (val) {
                     attrVal.setS(val)
                 } else {
                     return null
                 }
        }
        
        return attrVal
    }
    
    static Object getAttributeValueUpdate(String fieldName, Map<String,?>valueMap, EntityDefinition ed) {
    
        Object attrVal = new Object()
        MNode fieldNode = ed.getFieldNode(fieldName)
        String fieldNodeName = fieldNode."@name"
                 logger.info("JanusGraphUtils.getAttributeValue(291) fieldNodeName: ${fieldNodeName}")
        String fieldNodeType = fieldNode."@type"
                 logger.info("JanusGraphUtils.getAttributeValue(293) fieldNodeType: ${fieldNodeType}")
                 
        // do not include hash key field
        if (fieldNodeName in ed.getPkFieldNames()) {
             return null
        }
        // do not include range key field
        String indexName = fieldNode."@index"
        if (indexName) {
                return null
        }
        switch(fieldNodeType) {
            case "id":
            case "id-long":
            case "text-short":
            case "text-medium":
            case "text-long":
            case "text-very-long":
            case "text-indicator":
                 String val = valueMap.get(fieldName)?: ""
                 logger.info("JanusGraphUtils.getAttributeValue(32) val: ${val}")
                 if (val) {
                    attrVal.setValue(new Object().withS(val))
                 } else {
                     return null
                 }
                 break
            case "number-integer":
                 String val = valueMap.get(fieldName)?: ""
                 logger.info("JanusGraphUtils.getAttributeValue(41) val: ${val}")
                 if (val) {
                    attrVal.setValue(new Object().withN(val))
                 } else {
                     return null
                 }
                 break
            case "number-decimal":
            case "number-float":
            case "currency-amount":
            case "currency-precise":
                 return attrVal.setN(valueMap.get(fieldName).toString())
                 break
            case "date":
            case "time":
            case "date-time":
                 String dateTimeStr = valueMap.get(fieldName)
                 logger.info("JanusGraphUtils(310) dateTimeStr: ${dateTimeStr}")
                 Timestamp ts = Timestamp.valueOf(dateTimeStr)
                 logger.info("JanusGraphUtils(312) ts: ${ts.toString()}")
                 attrVal.setValue(new Object().withS(ts.toString()))
                 break
            default:
                 String val = valueMap.get(fieldName)?: ""
                 if (val) {
                    attrVal.setValue(new Object().withS(val))
                 } else {
                     return null
                 }
        }
        
        return attrVal
    }


    static org.apache.tinkerpop.gremlin.structure.Edge addEdge(org.apache.tinkerpop.gremlin.structure.Vertex fromVertex, Object edgeId,
                        org.apache.tinkerpop.gremlin.structure.Vertex toVertex,
                        String label, Map<String,Object> edgeProperties, EntityFacade entityFacade) {
        Edge e = fromVertex.addEdge(label, toVertex, null)
        Property prop
        edgeProperties.each {fieldName, val ->
                prop = e.property(fieldName, val)
                logger.info("Edge Property for (${fieldName}): ${prop.value()}")
        }
        return e
    }

    static void debugStart (fromName) {
        return
    }

    static org.apache.tinkerpop.gremlin.structure.Vertex getRootVertex(  ExecutionContext ec) {

        org.apache.tinkerpop.gremlin.structure.Vertex v
        EntityFacade efi = ec.getEntityFacade()
        JanusGraphDatasourceFactory ddf = efi.getDatasourceFactory("transactional_nosql") as JanusGraphDatasourceFactory
        StandardJanusGraph janusGraph = ddf.getDatabase()
        GraphTraversalSource g = janusGraph.traversal()
        List <org.apache.tinkerpop.gremlin.structure.Vertex> fillList = new ArrayList()
        g.V().hasLabel('root').fill(fillList)
        if (fillList.size()) {
            v = fillList[0]
        } else {
            v = g.addV('root').next()
        }
        g.tx().commit()
        g.close()
        return v
    }

}
