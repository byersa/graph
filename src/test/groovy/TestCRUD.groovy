package org.moqui.addons.graph.test
import spock.lang.*

import org.moqui.context.ExecutionContext
import org.moqui.entity.EntityValue
import org.moqui.impl.entity.EntityDefinition
import org.moqui.impl.entity.EntityFacadeImpl
import org.moqui.Moqui

import org.moqui.addons.graph.JanusGraphDatasourceFactory
import org.moqui.addons.graph.JanusGraphEntityValue
import org.moqui.addons.graph.JanusGraphEntityFind
import org.moqui.addons.graph.JanusGraphUtils

import org.janusgraph.core.JanusGraphFactory
import org.janusgraph.core.JanusGraph
import org.janusgraph.core.schema.JanusGraphManagement
import org.janusgraph.graphdb.database.management.ManagementSystem
import org.janusgraph.core.VertexLabel
import org.janusgraph.core.PropertyKey
import org.janusgraph.core.EdgeLabel
import org.janusgraph.graphdb.database.management.ManagementSystem
import org.janusgraph.graphdb.database.StandardJanusGraph
import org.janusgraph.graphdb.vertices.StandardVertex
import org.janusgraph.core.JanusGraphTransaction
import org.janusgraph.core.JanusGraphVertex

import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal
import org.apache.tinkerpop.gremlin.structure.Graph
import org.apache.tinkerpop.gremlin.structure.Vertex
import org.apache.tinkerpop.gremlin.structure.Edge
import org.apache.tinkerpop.gremlin.structure.Direction
import org.apache.tinkerpop.gremlin.driver.Cluster
import org.apache.tinkerpop.gremlin.driver.Client
import org.apache.tinkerpop.gremlin.driver.ResultSet
import org.apache.tinkerpop.gremlin.driver.Result

import org.slf4j.Logger
import org.slf4j.LoggerFactory

class TestCRUD extends Specification {
    protected final static Logger logger = LoggerFactory.getLogger(Moqui.class)

    //@Shared
    //Map <String, Object> pciCreateMap = new HashMap()

    EntityValue pci

    EntityValue pci2

    @Shared ExecutionContext ec

    @Shared Graph janusGraph

    def setupSpec() {
        // init the framework, get the ec
        logger.info("in TestCRUD, setupSpec")
        System.out.println("IN TestCRUD")
        ec = Moqui.getExecutionContext()
        janusGraph = JanusGraphUtils.getDatabase(ec)
    }

    def setup() {
            //ec.user.loginUser("john.doe", "moqui")

        Map <String, Object> pciCreateMap = new HashMap()
        pciCreateMap.put("fullName", "Create User")
        pciCreateMap.put("emailAddress", "createUser@test.com")
        pciCreateMap.put("contactNumber", "801-400-5111")
        pciCreateMap.put("address1", "1151 Regent Court")
        pciCreateMap.put("city", "Orem")
        pciCreateMap.put("stateProvinceGeoId", "UT")
        pciCreateMap.put("postalCode", "84057")

        //pci = ec.entity.makeValue("vPartyContactInfo")
        EntityDefinition ed = ec.entity.getEntityDefinition("vPartyContactInfo")
        //pci = ec.entity.makeValue("vPartyContactInfo")
        pci = new JanusGraphEntityValue(ed, ec.entity, null)
        logger.info("in TestCRUD, pciCreateMap: ${pciCreateMap}")
        pci.setAll(pciCreateMap)
        logger.info("in TestCRUD, pci(2): ${pci}")

        //pci2 = ec.entity.makeValue("vPartyContactInfo")
        pci2 = new JanusGraphEntityValue(ed, ec.entity, null)
        pci2.setAll(pciCreateMap)
        pci2.set("fullName", "Create User 2")
        pci2.set("contactNumber", "801-555-5111")
        pci2.set("emailAddress", "createUser2@test.com")
        logger.info("in TestCRUD, pci(2): ${pci}")

    }

    def cleanupSpec() {
        //janusGraph.close()
        ec.destroy()
    }

    def cleanup() {
        //ec.user.logoutUser()
    }

//    def "get_root_vertex"() {
//        setup:
//        logger.info("in TestCRUD, get_root_vertex")
//
//        when:
//        org.apache.tinkerpop.gremlin.structure.Vertex v=JanusGraphUtils.getRootVertex(ec)
//        logger.info("in TestCRUD, get_root_vertex, v: ${v}")
//
//        then:
//        assert v
//    }
//
//    def "create_PartyContactInfo"() {
//
//        setup:
//        org.apache.tinkerpop.gremlin.driver.Client client = JanusGraphUtils.getClient(ec)
//        logger.info("in TestCRUD, create_PartyContactInfo, client: ${client}")
//
//        when:
//        //JanusGraphTransaction tx = janusGraph.buildTransaction().start()
//        pci = pci.create()
//        logger.info("in TestCRUD, create_PartyContactInfo, pci (3): ${pci}")
//
//        Object id = pci.getId()
//        logger.info("in TestCRUD, create_PartyContactInfo, pci.id: ${id}")
//        String gremlin = "g.V('${id}').next()"
//        //String vrtxId = g.V().hasId(pci.getId()).valueMap().next().id[0]
//        org.apache.tinkerpop.gremlin.driver.ResultSet results = client.submit(gremlin.toString())
//        org.apache.tinkerpop.gremlin.structure.Vertex v = results.one().getVertex()
//        logger.info("in TestCRUD, create_PartyContactInfo, v.id(): ${v.id()}")
//
//        then:
//        id == v.id()
//
//        cleanup:
//        client.close()
//        return
//    }

    def "update_PartyContactInfo"() {
        setup:
        org.apache.tinkerpop.gremlin.driver.Client client = JanusGraphUtils.getClient(ec)
        logger.info("in TestCRUD, update_PartyContactInfo, client: ${client}")

        when:
        //JanusGraphTransaction tx = janusGraph.buildTransaction().start()
        logger.info("in TestCRUD, update_PartyContactInfo, pci(1): ${pci}")
        pci.create()
        logger.info("in TestCRUD, update_PartyContactInfo, pci(1b): ${pci}")
        String updatedEmail = "updatedUser@test.com"
        logger.info("in TestCRUD, update_PartyContactInfo, updatedEmail(2): ${updatedEmail}")
        pci.set("emailAddress", updatedEmail)
        logger.info("in TestCRUD, update_PartyContactInfo, pci(3): ${pci}")
        pci = pci.update()
        logger.info("in TestCRUD, update_PartyContactInfo, pci (4): ${pci}")

        Object id = pci.getId()
        String emailAddress = pci.get('emailAddress')
        logger.info("in TestCRUD, update_PartyContactInfo, emailAddress: ${emailAddress}")
        String gremlin = "g.V('${id}').valueMap('emailAddress').next()"
        org.apache.tinkerpop.gremlin.driver.ResultSet results = client.submit(gremlin.toString())
        Result r = results.one()
        String email = r?.getObject().getValue()?.get(0)
        logger.info("in TestCRUD, update_PartyContactInfo, email: ${email}")
        logger.info("in TestCRUD, update_PartyContactInfo, email == emailAddress: ${email == emailAddress}")

        then:
        email == emailAddress

        cleanup:
        client.close()

        return
    }

//    def "delete_PartyContactInfo"() {
//        setup:
//        org.apache.tinkerpop.gremlin.driver.Client client = JanusGraphUtils.getClient(ec)
//        when:
//        logger.info("in TestCRUD.delete, pci(5): ${pci}")
//        Object id = pci.getId()
//        pci.delete(null)
//        logger.info("in TestCRUD.delete, pci(6): ${pci}")
//
//        org.apache.tinkerpop.gremlin.structure.Vertex vrtx = null
//        try {
//            String gremlin = "g.V('${id}').next()"
//            org.apache.tinkerpop.gremlin.driver.ResultSet results = client.submit(gremlin.toString())
//            org.apache.tinkerpop.gremlin.structure.Vertex v = results.one().getVertex()
//            logger.info("in testCreate.groovy delete, vrtx: ${v}")
//        } catch (java.util.NoSuchElementException e) {
//            logger.info("in testCreate.groovy delete, vrtx: ${vrtx}")
//        }
//
//        then:
//        !vrtx
//
//        cleanup:
//        client.close()
//
//        return
//    }

//    def "create_PartyContactInfo_edge"() {
//
//        setup:
//        org.apache.tinkerpop.gremlin.driver.Client client = JanusGraphUtils.getClient(ec)
//        pci.create()
//        pci2.create()
//        logger.info("in TestCRUD create_PartyContactInfo_edge, pci2: ${pci2}")
//        org.apache.tinkerpop.gremlin.structure.Vertex fromVertex = pci.getVertex()
//        org.apache.tinkerpop.gremlin.structure.Vertex toVertex = pci2.getVertex()
//
//        when:
//
//        //org.apache.tinkerpop.gremlin.structure.Edge
//        logger.info("in TestCRUD, fromVertex: ${fromVertex}")
//        logger.info("in TestCRUD, toVertex: ${toVertex}")
//        Map <String,Object> edgePropMap = [prorate: 25]
//        Object id = JanusGraphUtils.addEdge(fromVertex, toVertex, "edgeLabel", edgePropMap, null, ec)
//        logger.info("in TestCRUD, edge id: ${id}")
//
//        String gremlin = g.V('${fromVertex.id()}').outE().where(__.otherV().hasId('${toVertex.id()}')).toList()
//        org.apache.tinkerpop.gremlin.driver.ResultSet results = client.submit(gremlin.toString())
//        List edgeList = results.one().getList()
//        logger.info("in testCreate.groovy, edgeList: ${edgeList}")
//
//        then:
//        edgeList
//
//        cleanup:
//        client.close()
//
//        return
//    }

//    def "find_PartyContactInfo_list"() {
//        setup:
//        GraphTraversalSource g = JanusGraphUtils.getTraversal(ec)
//        JanusGraphDatasourceFactory ddf = ec.entity.getDatasourceFactory("transactional_nosql") as JanusGraphDatasourceFactory
//        //pci = ec.entity.makeValue("vPartyContactInfo")
//        List<Vertex > arr = []
//        //EntityValue pci
//        (0..5).eachWithIndex{ int entry, int i ->
//            arr[i] = g.addV("vPartyContactInfo")
//                    .property("fullName", "Create User" + i)
//                    .property("emailAddress", "createUser${i}@test.com")
//                    .property("contactNumber", "801-40${i}-5111")
//                    .property("address1", "1151 ${i} Regent Court")
//                    .property("city", "Orem")
//                    .property("stateProvinceGeoId", "UT")
//                    .property("postalCode", "84057")
//                    .property("sales", new Float(i * 1000000))
//                    .next()
//            logger.info("in TestCRUD find_PartyContactInfo_list, v: ${arr[i]}")
//        }
//        String nme
//        (0..5).eachWithIndex{ int entry, int i ->
//            nme = "testEdge ${i}"
//            arr[0].addEdge("testEdge", arr[i], "name", nme, "prorate", new Float(i * 10))
//        }
//        logger.info("in TestCRUD find_PartyContactInfo_list, arr: ${arr}")
//
//
//        when:
//        JanusGraphEntityFind fnd = new JanusGraphEntityFind(ec.entity)
//        def resultList = fnd.condition(arr[0].id(), "vPartyContactInfo", "testEdge", [["prorate", "gt", new Float(20.0)]], null).list()
//        logger.info("in TestCRUD, resultList: ${resultList}")
//        def resultList2 = fnd.condition(arr[0].id(), "vPartyContactInfo", "testEdge", [["prorate", "lte", new Float(40.0)]],
//                [["sales", "gte", new Float(3000000.0)]]).list()
//        logger.info("in TestCRUD, resultList2: ${resultList2}")
//
//        then:
//        assert resultList.size() == 3
//        assert resultList2.size() == 3
//        cleanup:
//        //g.tx().commit()
//        g.close()
//        return
//    }
//
//    def "find_PartyContactInfo_one"() {
//        setup:
//        GraphTraversalSource g = JanusGraphUtils.getTraversal(ec)
//        JanusGraphDatasourceFactory ddf = ec.entity.getDatasourceFactory("transactional_nosql") as JanusGraphDatasourceFactory
//        //pci = ec.entity.makeValue("vPartyContactInfo")
//        List<Vertex > arr = []
//        //EntityValue pci
//        (0..5).eachWithIndex{ int entry, int i ->
//            arr[i] = g.addV("testVertex")
//            .property("fullName", "Create User" + i)
//                                      .property("emailAddress", "createUser{i}@test.com")
//                                      .property("contactNumber", "801-40${i}-5111")
//                                      .property("address1", "1151 ${i} Regent Court")
//                                      .property("city", "Orem")
//                                      .property("stateProvinceGeoId", "UT")
//                                      .property("postalCode", "84057")
//                    .property("sales", new Float(i * 1000000))
//            .next()
//            logger.info("in TestCRUD find_PartyContactInfo_one, v: ${arr[i]}")
//        }
//        String nme
//        (0..5).eachWithIndex{ int entry, int i ->
//            nme = "testEdge ${i}"
//            arr[0].addEdge("testEdge", arr[i], "name", nme, "prorate", new Float(i * 10))
//        }
//        logger.info("in TestCRUD find_PartyContactInfo_one, arr: ${arr}")
//
//
//        when:
//        JanusGraphEntityFind fnd = new JanusGraphEntityFind(ec.entity)
//        def result = fnd.condition(arr[0].id(), null, null, null).one()
//        logger.info("in TestCRUD, result: ${result}")
//        def result2 = fnd.condition(null, null, null, [["sales", "gte", new Float(3000000.0)]]).one()
//        logger.info("in TestCRUD, result2: ${result2}")
//
//        then:
//        assert result
//        assert result2
//
//        cleanup:
//        //g.tx().commit()
//        g.close()
//        return
//    }


    def instantiateDatabase() {
        EntityFacadeImpl efi = ec.getEntityFacade()
        EntityDefinition ed = efi.getEntityDefinition("vPartyContactInfo")
        JanusGraphDatasourceFactory edfi = efi.getDatasourceFactory(ed.getEntityGroupName()) as JanusGraphDatasourceFactory
        StandardJanusGraph jG = edfi.getDatabase()
        //janusGraph = jG
        return jG
    }

}
