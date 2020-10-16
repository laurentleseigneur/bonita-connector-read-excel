package com.bonitasoft.connector

import org.bonitasoft.engine.api.APIAccessor
import org.bonitasoft.engine.api.ProcessAPI
import org.bonitasoft.engine.bpm.document.Document
import org.bonitasoft.engine.bpm.process.impl.DocumentBuilder
import spock.lang.Specification

import java.nio.file.Files
import java.time.Instant
import java.time.LocalDate

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
                        [customer: "john", "date of birth": 20534.0, income: 4561.0],
                        [customer: "jack", "date of birth": 17782.0, income: 8652.0]
                ]
        ]
    }
}
