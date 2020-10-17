package com.bonitasoft.connector

import org.bonitasoft.engine.api.APIAccessor
import org.bonitasoft.engine.api.ProcessAPI
import org.bonitasoft.engine.bpm.process.impl.DocumentBuilder
import spock.lang.Specification

import java.time.Instant

class BonitaReadExcelConnectorTest extends Specification {

    def apiAccessorMock = Mock(APIAccessor)
    def processApiMock = Mock(ProcessAPI)

    def setup() {
        apiAccessorMock.getProcessAPI() >> processApiMock
    }

    def shouldReadExcelFile() {
        given:
        def excelFile = new File(this.getClass().getResource("/excelFile.xlsx").toURI())
        def excelDoc = new DocumentBuilder().createNewInstance("excelFile.xlsx", true).done()
        Map<String, Object> params = new HashMap<>()
        params.put(BonitaReadExcelConnector.EXCEL_DOCUMENT_INPUT, excelDoc)
        processApiMock.getDocumentContent(_) >> excelFile.bytes
        BonitaReadExcelConnector connector = new BonitaReadExcelConnector(apiAccessor: apiAccessorMock)
        connector.setInputParameters(params)

        when:
        connector.executeBusinessLogic()
        def result = connector.getOutputParameters()

        then:
        result == [
                excelData: [
                        [customer: "john", "date of birth": Instant.parse('1956-03-19T23:00:00Z'), income: 4561.0],
                        [customer: "jack", "date of birth": Instant.parse('1948-09-05T23:00:00Z'), income: 8652.0],
                        [customer: "helen", "date of birth": Instant.parse('1970-12-05T23:00:00Z'), income: 6587.0]
                ]
        ]
    }

    def "should escape"() {
        given:
        def aa = "Côtes-d'Arm'or"

        when:
        def bb = aa.replaceAll("'", "''")

        then:
        bb == "Côtes-d''Arm''or"
    }
}
