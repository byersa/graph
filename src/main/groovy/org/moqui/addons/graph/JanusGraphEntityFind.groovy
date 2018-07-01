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

import org.moqui.addons.graph.JanusGraphEntityValue
import org.moqui.addons.graph.JanusGraphDatasourceFactory
import org.janusgraph.core.JanusGraph
import org.moqui.impl.context.ExecutionContextFactoryImpl

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.moqui.util.MNode 

class JanusGraphEntityFind {
    protected final static Logger logger = LoggerFactory.getLogger(JanusGraphEntityFind.class)
    protected JanusGraph janusGraph
    protected JanusGraphDatasourceFactory ddf

    JanusGraphEntityFind(EntityFacadeImpl efi, String entityName, JanusGraphDatasourceFactory ddf) {
        super(efi, entityName)
        this.ddf = ddf
        this.janusGraph = ddf.getDatabase()
    }

    // ======================== Run Find Methods ==============================

    EntityValue one() throws EntityException {

        EntityDefinition ed = this.getEntityDef()
        List <String> skipFieldNames = new ArrayList()
        //Map<String,Object> valMap = getValueMap()
        // if over-constrained (anything in addition to a full PK), just use the full PK
        logger.info("JanusGraphEntityFind.one (73), simpleAndMap: ${simpleAndMap}")
//        EntityCondition whereCondition = this.getWhereEntityCondition()
//            logger.info("JanusGraphFindEntity(96), whereCondition: ${whereCondition.toString()}")
//        /*
//        if (ed.containsPrimaryKey(simpleAndMap)) {
//            whereCondition = (JanusGraphEntityConditionImplBase) this.conditionFactory.makeCondition(simpleAndMap)
//        } else {
//            throw(new EntityException("Primary key not contained in ${simpleAndMap}"))
//        }
//        */
//
//
//        JanusGraphEntityValue entValue = null
//        try {
//                String entName = ed.getFullEntityName()
//                Table table = janusGraph.getTable(entName)
//            String hashVal = whereCondition.getJanusGraphHashValue(ed)
//            if (hashVal) {
//                GetItemSpec getItemSpec = new GetItemSpec()
//                logger.info("JanusGraphEntityFind.one (107), hashVal: ${hashVal}")
//                String hashFieldName = ed.getFieldNames(true, false)[0]
//                skipFieldNames.add(hashFieldName)
//                //PrimaryKey primaryKey = new PrimaryKey(hashFieldName, hashVal)
//                //RangeKeyCondition rangeCondition = whereCondition.getRangeCondition(ed)
//                //logger.info("JanusGraphFindEntity(111), rangeCondition: ${rangeCondition}")
//                String rangeValue = whereCondition.getJanusGraphRangeValue(ed)
//                logger.info("JanusGraphEntityFind.one (125), rangeValue: ${rangeValue}")
//                if (rangeValue) {
//                    String rangeFieldName = JanusGraphUtils.getRangeFieldName(ed)
//                    skipFieldNames.add(rangeFieldName)
//                    getItemSpec = getItemSpec.withPrimaryKey(hashFieldName, hashVal, rangeFieldName, rangeValue)
//                } else {
//                    getItemSpec = getItemSpec.withPrimaryKey(hashFieldName, hashVal)
//                }
//
//                logger.info("JanusGraphEntityFind.one getKeyComponents: ${getItemSpec.getKeyComponents()}")
//                Item item = table.getItem(getItemSpec)
//
//                logger.info("JanusGraphEntityFind.one item: ${item}")
//                Map<java.lang.String,java.lang.Object> itemAsMap
//                if (item) {
//                    itemAsMap = item.asMap()
//                    logger.info("JanusGraphEntityFind.list itemAsMap: ${itemAsMap}")
//                    entValue = ddf.makeEntityValue(entName)
//                    //entValue.buildEntityValueMap()
//                    entValue.setAll(itemAsMap)
//                } else {
//                    entValue = null
//                }
//            } else {
//                Map <String, String> indexValMap = whereCondition.getJanusGraphIndexValue(ed)
//                    logger.info("JanusGraphEntityFind.list indexValMap: ${indexValMap}")
//                if (indexValMap) {
//                    EntityList entList = this.queryIndex(indexValMap, table, entName, skipFieldNames, whereCondition, ed)
//                    if (entList) {
//                        entValue = entList[0]
//                    }
//                } else {
//                    EntityList entList = this.scan(whereCondition, table, entName, ed, skipFieldNames)
//                    if (entList) {
//                        entValue = entList[0]
//                    }
//                }
//            }
//
//        } catch(Exception e6) {
//            throw new Exception(e6.getMessage())
//        }finally {
//        }
        return null
    }

    /** @see org.moqui.entity.EntityFind#list() */
    EntityList list() throws EntityException {
        long startTime = System.currentTimeMillis()
        EntityDefinition ed = this.getEntityDef()
        List retList = null
        JanusGraphEntityValue entValue = null
        EntityList entList = new EntityListImpl(this.efi)
            logger.info("JanusGraphEntityFind.list efi: ${this.efi}")
//        List <String> skipFieldNames = new ArrayList()
//        try {
//            JanusGraphEntityConditionImplBase whereCondition = this.getWhereEntityCondition()
//            logger.info("JanusGraphEntityFind.list whereCondition: ${whereCondition}")
//            String hashVal = whereCondition.getJanusGraphHashValue(ed)
//            logger.info("JanusGraphEntityFind.list , hashVal: ${hashVal.toString()}")
//            String entName = ed.getFullEntityName()
//            Table table = janusGraph.getTable(entName)
//            if (hashVal) {
//                String hashFieldName = ed.getFieldNames(true, false)[0]
//                skipFieldNames.add(hashFieldName)
//                QuerySpec querySpec = new QuerySpec().withHashKey(hashFieldName, hashVal)
//                RangeKeyCondition rangeCondition = whereCondition.getRangeCondition(ed)
//                logger.info("JanusGraphFindEntity(170), rangeCondition: ${rangeCondition}")
//                if (rangeCondition) {
//                    querySpec = querySpec.withRangeKeyCondition(rangeCondition)
//                    String rangeFieldName = JanusGraphUtils.getRangeFieldName(ed)
//                    skipFieldNames.add(rangeFieldName)
//                }
//
//                Map expressMap = whereCondition.getJanusGraphFilterExpressionMap(ed, skipFieldNames)
//                if (expressMap) {
//                    logger.info("JanusGraphEntityFind.list expressMap: ${expressMap}")
//                    querySpec = querySpec
//                                 .withFilterExpression(expressMap.filterExpression)
//                                 .withNameMap(expressMap.nameMap)
//                                 .withValueMap(expressMap.valueMap)
//                }
//                logger.info("JanusGraphEntityFind.list entName: ${entName}")
//                logger.info("JanusGraphEntityFind.list table: ${table}")
//                ItemCollection <QueryOutcome> queryOutcomeList = table.query(querySpec)
//                //List <Item> itemList = queryOutcome.getItems()
//
//                logger.info("JanusGraphEntityFind.list queryOutcomeList: ${queryOutcomeList}")
//                Map<java.lang.String,java.lang.Object> itemAsMap
//                queryOutcomeList.each() {item ->
//                    itemAsMap = item.asMap()
//                    logger.info("JanusGraphEntityFind.list itemAsMap: ${itemAsMap}")
//                    entValue = ddf.makeEntityValue(entName)
//                    //entValue.buildEntityValueMap()
//                    entValue.setAll(itemAsMap)
//                    entList.add(entValue)
//                }
//            } else {
//                Map <String, String> indexValMap = whereCondition.getJanusGraphIndexValue(ed)
//                    logger.info("JanusGraphEntityFind.list indexValMap: ${indexValMap}")
//                if (indexValMap) {
//                    entList = this.queryIndex(indexValMap, table, entName, skipFieldNames, whereCondition, ed)
//                } else {
//                    entList = this.scan(whereCondition, table, entName, ed, skipFieldNames)
//                }
//            }
//
//
//
//        } catch(Exception e6) {
//            throw new Exception(e6.getMessage())
//        }finally {
//        }
        return entList
    }

}
