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
import org.apache.tinkerpop.gremlin.structure.Vertex
import org.apache.tinkerpop.gremlin.structure.Edge

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import sun.security.provider.certpath.Vertex

class TestCRUD extends Specification {
    protected final static Logger logger = LoggerFactory.getLogger(Moqui.class)

    //@Shared
    //Map <String, Object> pciCreateMap = new HashMap()

    @Shared
    EntityValue pci

    @Shared
    EntityValue pci2

    @Shared
    ExecutionContext ec

    @Shared
    StandardJanusGraph janusGraph

    def setupSpec() {
        // init the framework, get the ec
        logger.info("in TestCRUD, setupSpec")
        System.out.println("IN TestCRUD")
        ec = Moqui.getExecutionContext()
        janusGraph = instantiateDatabase()
        Map <String, Object> pciCreateMap = new HashMap()
        pciCreateMap.put("fullName", "Create User")
        pciCreateMap.put("emailAddress", "createUser@test.com")
        pciCreateMap.put("contactNumber", "801-400-5111")
        pciCreateMap.put("address1", "1151 Regent Court")
        pciCreateMap.put("city", "Orem")
        pciCreateMap.put("stateProvinceGeoId", "UT")
        pciCreateMap.put("postalCode", "84057")

        pci = ec.entity.makeValue("vPartyContactInfo")
        logger.info("in TestCRUD, pciCreateMap: ${pciCreateMap}")
        pci.setAll(pciCreateMap)
        logger.info("in TestCRUD, pci(2): ${pci}")

        pci2 = ec.entity.makeValue("vPartyContactInfo")
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

    def setup() {
        //ec.user.loginUser("john.doe", "moqui")
    }

    def cleanup() {
        //ec.user.logoutUser()
    }

    def "create_PartyContactInfo"() {
        when:
        //JanusGraphTransaction tx = janusGraph.buildTransaction().start()
        pci = pci.create()
        logger.info("in TestCRUD, pci (3): ${pci}")

        then:
        GraphTraversalSource g = janusGraph.traversal()
        logger.info("in testCreate.groovy, g: ${g}")
        StandardVertex vrtx = g.V(pci.getId()).next()
        logger.info("in testCreate.groovy, vrtx: ${vrtx}")

        logger.info("contactNumber equal: ${vrtx.property('contactNumber').value() == pci.get('contactNumber')}")
        vrtx.property('contactNumber').value() == pci.get('contactNumber')
        g.close()
        //tx.commit()
        return
    }

    def "update_PartyContactInfo"() {
        when:
        //JanusGraphTransaction tx = janusGraph.buildTransaction().start()
        logger.info("in TestCRUD.update, pci(4): ${pci}")
        String updatedEmail = "updatedUser@test.com"
        pci.set("emailAddress", updatedEmail)
        pci = pci.update()
        logger.info("in TestCRUD.update, pci (3): ${pci}")

        then:
        GraphTraversalSource g = janusGraph.traversal()
        logger.info("in testCreate.groovy update, g: ${g}")
        StandardVertex vrtx = g.V(pci.getId()).next()
        logger.info("in testCreate.groovy update, vrtx: ${vrtx}")
        Map map = new HashMap()
        map.putAll(vrtx.properties().collectEntries {it ->[(it.key()):it.value()]})
        logger.info("Updated vertex (update test): ${map}")

        logger.info("emailAddress equal: ${vrtx.property('emailAddress').value() == pci.get('emailAddress')}")
        vrtx.property('emailAddress').value() == pci.get('emailAddress')
        g.close()
        //tx.commit()
        return
    }

    def "delete_PartyContactInfo"() {
        when:
        logger.info("in TestCRUD.delete, pci(5): ${pci}")
        Object id = pci.getId()
        pci.delete()
        logger.info("in TestCRUD.delete, pci(6): ${pci}")

        then:
        GraphTraversalSource g = janusGraph.traversal()
        Vertex vrtx = null
        try {
            vrtx = g.V(id).next()
        } catch (java.util.NoSuchElementException e) {
            logger.info("in testCreate.groovy delete, vrtx: ${vrtx}")
        }

        assert (!vrtx)
        return
    }

    def "create_PartyContactInfo_edge"() {
        when:
        pci.create()
        pci2.create()
        logger.info("in TestCRUD create_PartyContactInfo_edge, pci2: ${pci2}")

        long fromId = (long)pci.get("id")
        long toId = (long)pci2.get("id")
        logger.info("in TestCRUD, fromId: ${fromId}")
        logger.info("in TestCRUD, toId: ${toId}")
        Map <String,Object> edgePropMap = [prorate: 25]
        def edgeId = JanusGraphUtils.addEdge(fromId, null, toId, "edgeLabel", edgePropMap, ec.entity)
        logger.info("in TestCRUD, edgeId: ${edgeId}")

        then:
        GraphTraversalSource g = janusGraph.traversal()
        Edge edge = g.V(fromId).outE().has("prorate", 25).next()
        logger.info("in testCreate.groovy, edge: ${edge}")
        logger.info("in testCreate.groovy, edge.prorate: ${edge.property('prorate')}")

        logger.info("prorate equal: ${edge.property('prorate').value() == 25}")
        edge.property('prorate').value() == 25
        g.close()
        return
    }

    def "find_PartyContactInfo_list"() {
        setup:
        GraphTraversalSource g = janusGraph.traversal()
        JanusGraphDatasourceFactory ddf = ec.entity.getDatasourceFactory("transactional_nosql") as JanusGraphDatasourceFactory
        //pci = ec.entity.makeValue("vPartyContactInfo")
        List<Vertex > arr = []
        //EntityValue pci
        (0..5).eachWithIndex{ int entry, int i ->
            arr[i] = g.addV("testVertex")
                    .property("fullName", "Create User" + i)
                    .property("emailAddress", "createUser{i}@test.com")
                    .property("contactNumber", "801-40${i}-5111")
                    .property("address1", "1151 ${i} Regent Court")
                    .property("city", "Orem")
                    .property("stateProvinceGeoId", "UT")
                    .property("postalCode", "84057")
                    .property("sales", new Float(i * 1000000))
                    .next()
            logger.info("in TestCRUD find_PartyContactInfo_list, v: ${arr[i]}")
        }
        String nme
        (0..5).eachWithIndex{ int entry, int i ->
            nme = "testEdge ${i}"
            arr[0].addEdge("testEdge", arr[i], "name", nme, "prorate", new Float(i * 10))
        }
        logger.info("in TestCRUD find_PartyContactInfo_list, arr: ${arr}")


        when:
        JanusGraphEntityFind fnd = new JanusGraphEntityFind(ddf)
        def resultList = fnd.condition(arr[0].id(), "testEdge", [["prorate", "gt", new Float(20.0)]], null).list()
        logger.info("in TestCRUD, resultList: ${resultList}")
        def resultList2 = fnd.condition(arr[0].id(), "testEdge", [["prorate", "lte", new Float(40.0)]],
                [["sales", "gte", new Float(3000000.0)]]).list()
        logger.info("in TestCRUD, resultList2: ${resultList2}")

        then:
        assert resultList.size() == 3
        assert resultList2.size() == 3
        cleanup:
        g.close()
        return
    }

    def "find_PartyContactInfo_one"() {
        setup:
        GraphTraversalSource g = janusGraph.traversal()
        JanusGraphDatasourceFactory ddf = ec.entity.getDatasourceFactory("transactional_nosql") as JanusGraphDatasourceFactory
        //pci = ec.entity.makeValue("vPartyContactInfo")
        List<Vertex > arr = []
        //EntityValue pci
        (0..5).eachWithIndex{ int entry, int i ->
            arr[i] = g.addV("testVertex")
            .property("fullName", "Create User" + i)
                                      .property("emailAddress", "createUser{i}@test.com")
                                      .property("contactNumber", "801-40${i}-5111")
                                      .property("address1", "1151 ${i} Regent Court")
                                      .property("city", "Orem")
                                      .property("stateProvinceGeoId", "UT")
                                      .property("postalCode", "84057")
                    .property("sales", new Float(i * 1000000))
            .next()
            logger.info("in TestCRUD find_PartyContactInfo_one, v: ${arr[i]}")
        }
        String nme
        (0..5).eachWithIndex{ int entry, int i ->
            nme = "testEdge ${i}"
            arr[0].addEdge("testEdge", arr[i], "name", nme, "prorate", new Float(i * 10))
        }
        logger.info("in TestCRUD find_PartyContactInfo_one, arr: ${arr}")


        when:
        JanusGraphEntityFind fnd = new JanusGraphEntityFind(ddf)
        def result = fnd.condition(arr[0].id(), null, null, null).one()
        logger.info("in TestCRUD, result: ${result}")
        def result2 = fnd.condition(null, null, null, [["sales", "gte", new Float(3000000.0)]]).one()
        logger.info("in TestCRUD, result2: ${result2}")

        then:
        assert result
        assert result2

        cleanup:
        g.close()
        return
    }


    def instantiateDatabase() {
        EntityFacadeImpl efi = ec.getEntityFacade()
        EntityDefinition ed = efi.getEntityDefinition("vPartyContactInfo")
        JanusGraphDatasourceFactory edfi = efi.getDatasourceFactory(ed.getEntityGroupName()) as JanusGraphDatasourceFactory
        StandardJanusGraph jG = edfi.getDatabase()
        //janusGraph = jG
        return jG
    }

}
