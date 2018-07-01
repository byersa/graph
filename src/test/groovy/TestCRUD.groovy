package org.moqui.addons.graph.test
import spock.lang.*

import org.moqui.context.ExecutionContext
import org.moqui.entity.EntityValue
import org.moqui.impl.entity.EntityDefinition
import org.moqui.impl.entity.EntityFacadeImpl
import org.moqui.Moqui

import org.moqui.addons.graph.JanusGraphDatasourceFactory
import org.moqui.addons.graph.JanusGraphEntityValue
import org.moqui.addons.graph.JanusGraphUtils

import org.janusgraph.core.JanusGraphFactory
import org.janusgraph.core.JanusGraph
import org.janusgraph.core.schema.JanusGraphManagement
import org.janusgraph.graphdb.database.management.ManagementSystem
import org.janusgraph.core.VertexLabel
import org.janusgraph.core.PropertyKey
import org.janusgraph.core.EdgeLabel
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal
import org.janusgraph.graphdb.database.management.ManagementSystem
import org.janusgraph.graphdb.database.StandardJanusGraph
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class TestCRUD extends Specification {
    protected final static Logger logger = LoggerFactory.getLogger(Moqui.class)

    @Shared
    Map <String, Object> pciCreateMap = new HashMap()

    @Shared
    ExecutionContext ec

    @Shared
    StandardJanusGraph janusGraph

    def setupSpec() {
        // init the framework, get the ec
        ec = Moqui.getExecutionContext()
        janusGraph = instantiateDatabase()
        pciCreateMap.put("fullName", "Create User")
        pciCreateMap.put("emailAddress", "createUser@test.com")
        pciCreateMap.put("contactNumber", "801-400-5111")
        pciCreateMap.put("address1", "1151 Regent Court")
        pciCreateMap.put("city", "Orem")
        pciCreateMap.put("stateProvinceGeoId", "UT")
        pciCreateMap.put("postalCode", "84057")
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

    def "create PartyContactInfo"() {
        when:
        EntityValue pci = ec.entity.makeValue("vPartyContactInfo")
        logger.info("in TestCRUD, pci: ${pci}")
        pci.setAll(pciCreateMap)
        logger.info("in TestCRUD, pci(2): ${pci}")
        EntityValue pci2 = pci.create()
        logger.info("in TestCRUD, pci2: ${pci2}")

        then:
        GraphTraversalSource g = janusGraph.traversal()
        logger.info("in testCreate.groovy, g: ${g}")
        GraphTraversal val = g.V(pci2.getId())
        logger.info("in testCreate.groovy, val: ${val}")
        val != null

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
