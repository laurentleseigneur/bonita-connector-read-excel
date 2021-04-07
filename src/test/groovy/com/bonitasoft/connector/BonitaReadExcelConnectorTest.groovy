package com.bonitasoft.connector

import org.bonitasoft.engine.api.APIAccessor
import org.bonitasoft.engine.api.ProcessAPI
import org.bonitasoft.engine.bpm.process.impl.DocumentBuilder
import spock.lang.Specification

class BonitaReadExcelConnectorTest extends Specification {

    public static final String DATE_FORMAT = "yyyy-MM-dd"
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
                        [customer: "john", "date of birth": Date.parse(DATE_FORMAT, '1956-03-20'), income: 4561.0, emptyColumn: null],
                        [customer: "jack", "date of birth": Date.parse(DATE_FORMAT, '1948-09-06'), income: 8652.0, emptyColumn: null],
                        [customer: "helen", "date of birth": Date.parse(DATE_FORMAT, '1970-12-06'), income: 6587.0, emptyColumn: null]
                ]
        ]
    }
}
