package com.bonitasoft.connector

import groovy.json.JsonOutput
import groovy.json.JsonSlurper

class JsonUtil {
	static JsonSlurper slurper = new JsonSlurper()

	static def fromJson(String json) {
		slurper.parseText(json)
	}

	static def toJson(def object) {
		JsonOutput.toJson(object)
	}

	static def toPrettyJson(def object) {
		// this can be resource consuming
		JsonOutput.prettyPrint(JsonOutput.toJson(object))
	}
}
