package com.bonitasoft.connector

import org.apache.poi.ss.usermodel.*
import org.bonitasoft.engine.bpm.document.Document
import org.bonitasoft.engine.connector.AbstractConnector
import org.bonitasoft.engine.connector.ConnectorException
import org.bonitasoft.engine.connector.ConnectorValidationException
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.nio.file.Files

class BonitaReadExcelConnector extends AbstractConnector {

    def static final EXCEL_DOCUMENT_INPUT = "excelDocument"
    def static final OUTPUT_DATA = "excelData"

    Logger logger = LoggerFactory.getLogger(this.class)

    /**
     * Perform validation on the inputs defined on the connector definition (src/main/resources/bonita-connector-read-excel.def)
     * You should:
     * - validate that mandatory inputs are presents
     * - validate that the content of the inputs is coherent with your use case (e.g: validate that a date is / isn't in the past ...)
     */
    @Override
    void validateInputParameters() throws ConnectorValidationException {
        checkMandatoryDocumentInput(EXCEL_DOCUMENT_INPUT)
    }

    def checkMandatoryDocumentInput(inputName) throws ConnectorValidationException {
        def value = getInputParameter(inputName)
        if (!value) {
            throw new ConnectorValidationException(this, "Mandatory parameter '$inputName' is missing.")
        }

    }

    /**
     * Core method:
     * - Execute all the business logic of your connector using the inputs (connect to an external service, compute some values ...).
     * - Set the output of the connector execution. If outputs are not set, connector fails.
     */
    @Override
    void executeBusinessLogic() throws ConnectorException {
        Document defaultInput = getInputParameter(EXCEL_DOCUMENT_INPUT)
        logger.info("EXCEL_DOCUMENT_INPUT : {}", defaultInput)


        def bytes = apiAccessor.getProcessAPI().getDocumentContent(defaultInput.getContentStorageId())
        File file = Files.createTempFile("rh", ".xlsx").toFile()
        file.bytes = bytes

        def result = []
        Workbook workbook = WorkbookFactory.create(file)
        def sheet = workbook.getSheetAt(0)
        int rowIndex = 1
        def firstRow = sheet.getRow(0)
        def columnCount = firstRow.getLastCellNum()

        Row row = sheet.getRow(rowIndex++)
        def lastRowNum = sheet.getLastRowNum() + 1
        while (rowIndex <= lastRowNum) {
            boolean rowHasData = false
            def data = [:]
            for (columnIndex in 0..columnCount) {
                def cell = row?.getCell(columnIndex)
                def cellKey = firstRow?.getCell(columnIndex)?.getStringCellValue()
                if (cellKey) {
                    def value
                    value = getCellValue(cell)
                    data.put(cellKey, value)
                    if (value) {
                        rowHasData = true
                    }
                }
            }
            if (rowHasData) {
                result.add(data)
            }
            row = sheet.getRow(rowIndex++)
        }

        logger.debug('connector output:')
        result.each {
            logger.debug("{}", it)
        }

        result

        setOutputParameter(OUTPUT_DATA, result)
    }

    def getCellValue(Cell cell) {
        def value
        if (cell) {
            switch (cell.getCellType()) {
                case CellType.NUMERIC:
                    if (DateUtil.isCellDateFormatted(cell)) {
                        value = cell.getDateCellValue()
                    } else {
                        value = cell.getNumericCellValue()
                    }
                    break
                case CellType.BLANK:
                    value = ''
                    break
                case CellType.BOOLEAN:
                    value = cell.getBooleanCellValue()
                    break
                case CellType.STRING:
                    value = cell.getStringCellValue()
                    break
                default:
                    log.error("cell  row:${cell.row.getRowNum()} column: ${cell.getColumnIndex()} has not sopported type:${cell.getCellType().name()} not supported. Will return empty content")
                    value = ""
            }
        }
        value
    }

/**
 * [Optional] Open a connection to remote server
 */
    @Override
    void connect() throws ConnectorException {}

/**
 * [Optional] Close connection to remote server
 */
    @Override
    void disconnect() throws ConnectorException {}

}